/*-- $Id$ --

 ============================================================================
				   The Apache Software License, Version 1.1
 ============================================================================

	Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of	source code must  retain the above copyright  notice,
	this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
	this list of conditions and the following disclaimer in the documentation
	and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
	include  the following	acknowledgment:  "This product includes  software
	developed  by the  Apache Software Foundation  (http://www.apache.org/)."
	Alternately, this  acknowledgment may  appear in the software itself,  if
	and wherever such third-party acknowledgments normally appear.

 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
	endorse  or promote  products derived  from this  software without	prior
	written permission. For written permission, please contact
	apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
	"Apache" appear  in their name,  without prior written permission  of the
	Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR	PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT	OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)	HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,	WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR	OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software	consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software	Foundation and was	originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */

package org.apache.fop.render.pdf;

// FOP
import org.apache.fop.render.Renderer;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.FopImage;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.svg.*;
import org.apache.fop.pdf.*;
import org.apache.fop.layout.*;
import org.apache.fop.image.*;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.extensions.*;
import org.apache.fop.datatypes.IDReferences;

import org.apache.batik.bridge.*;
import org.apache.batik.swing.svg.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.gvt.*;
import org.apache.batik.gvt.renderer.*;
import org.apache.batik.gvt.filter.*;
import org.apache.batik.gvt.event.*;

import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.w3c.dom.css.*;
import org.w3c.dom.svg.SVGLength;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.awt.Rectangle;
import java.util.Vector;
import java.util.Hashtable;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.Dimension;

/**
 * Renderer that renders areas to PDF
 */
public class PDFRenderer implements Renderer {

    private static final boolean OPTIMIZE_TEXT = true;
    

    /** the PDF Document being created */
    protected PDFDocument pdfDoc;

    /** the /Resources object of the PDF document being created */
    protected PDFResources pdfResources;

    /** the IDReferences for this document */
    protected IDReferences idReferences;

    /** the current stream to add PDF commands to */
    PDFStream currentStream;

    /** the current annotation list to add annotations to */
    PDFAnnotList currentAnnotList;

    /** the current page to add annotations to */
    PDFPage currentPage;

    /** the current (internal) font name */
    protected String currentFontName;

    /** the current font size in millipoints */
    protected int currentFontSize;

    /** the current color/gradient for borders, letters, etc. */
    protected PDFPathPaint currentStroke = null;

    /** the current color/gradient to fill shapes with */
    protected PDFPathPaint currentFill = null;

    /** the current colour's red component */
    protected float currentRed = 0;

    /** the current colour's green component */
    protected float currentGreen = 0;

    /** the current colour's blue component */
    protected float currentBlue = 0;

    /** the current vertical position in millipoints from bottom */
    protected int currentYPosition = 0;

    /** the current horizontal position in millipoints from left */
    protected int currentXPosition = 0;

    /** the horizontal position of the current area container */
    private int currentAreaContainerXPosition = 0;

    /** the current colour for use in svg */
    private PDFColor currentColour = new PDFColor(0, 0, 0);

    private FontInfo fontInfo;
    
    // previous values used for text-decoration drawing
    int prevUnderlineXEndPos;
    int prevUnderlineYEndPos;
    int prevUnderlineSize;
    PDFColor prevUnderlineColor;
    int prevOverlineXEndPos;
    int prevOverlineYEndPos;
    int prevOverlineSize;
    PDFColor prevOverlineColor;
    int prevLineThroughXEndPos;
    int prevLineThroughYEndPos;
    int prevLineThroughSize;
    PDFColor prevLineThroughColor;

    /** true if a TJ command is left to be written */
    boolean textOpen = false;

    /** the previous Y coordinate of the last word written.
      Used to decide if we can draw the next word on the same line. */
    int prevWordY = 0;

    /** the previous X coordinate of the last word written.
     used to calculate how much space between two words */
    int prevWordX = 0;

    /** The  width of the previous word. Used to calculate space between */
    int prevWordWidth = 0;

    private PDFOutline rootOutline;
	
    /**
     * create the PDF renderer
     */
    public PDFRenderer() {
        this.pdfDoc = new PDFDocument();
    }

    /**
       * set the PDF document's producer
       *
       * @param producer string indicating application producing PDF
       */
    public void setProducer(String producer) {
        this.pdfDoc.setProducer(producer);
    }

    /**
       * render the areas into PDF
       *
       * @param areaTree the laid-out area tree
       * @param stream the OutputStream to write the PDF to
       */
    public void render(AreaTree areaTree,
                       OutputStream stream) throws IOException, FOPException {
        MessageHandler.logln("rendering areas to PDF");
        idReferences = areaTree.getIDReferences();
        this.pdfResources = this.pdfDoc.getResources();
        this.pdfDoc.setIDReferences(idReferences);
        Enumeration e = areaTree.getPages().elements();
        while (e.hasMoreElements()) {
            this.renderPage((Page) e.nextElement());
        }

        if (!idReferences.isEveryIdValid()) {
            //          throw new FOPException("The following id's were referenced but not found: "+idReferences.getInvalidIds()+"\n");
            MessageHandler.errorln("WARNING: The following id's were referenced but not found: "+
                                   idReferences.getInvalidIds() + "\n");

        }
	renderRootExtensions(areaTree);
	
        FontSetup.addToResources(this.pdfDoc, fontInfo);
        
        MessageHandler.logln("writing out PDF");
        this.pdfDoc.output(stream);
    }

    /**
       * add a line to the current stream
       *
       * @param x1 the start x location in millipoints
       * @param y1 the start y location in millipoints
       * @param x2 the end x location in millipoints
       * @param y2 the end y location in millipoints
       * @param th the thickness in millipoints
       * @param r the red component
       * @param g the green component
       * @param b the blue component
       */
    protected void addLine(int x1, int y1, int x2, int y2, int th,
                           PDFPathPaint stroke) {
	closeText();
	
        currentStream.add("ET\nq\n" + stroke.getColorSpaceOut(false) +
                          (x1 / 1000f) + " "+ (y1 / 1000f) + " m " +
                          (x2 / 1000f) + " "+ (y2 / 1000f) + " l " +
                          (th / 1000f) + " w S\n" + "Q\nBT\n");
    }

    /**
      * add a line to the current stream
      *
      * @param x1 the start x location in millipoints
      * @param y1 the start y location in millipoints
      * @param x2 the end x location in millipoints
      * @param y2 the end y location in millipoints
      * @param th the thickness in millipoints
      * @param rs the rule style as String containing dashArray + dashPhase
      * @param r the red component
      * @param g the green component
      * @param b the blue component
      */
    protected void addLine(int x1, int y1, int x2, int y2, int th,
                           String rs, PDFPathPaint stroke) {
	closeText();
        currentStream.add("ET\nq\n" + stroke.getColorSpaceOut(false) +
                          rs + (x1 / 1000f) + " "+ (y1 / 1000f) + " m " +
                          (x2 / 1000f) + " "+ (y2 / 1000f) + " l " +
                          (th / 1000f) + " w S\n" + "Q\nBT\n");
    }

    /**
       * add a rectangle to the current stream
       *
       * @param x the x position of left edge in millipoints
       * @param y the y position of top edge in millipoints
       * @param w the width in millipoints
       * @param h the height in millipoints
       * @param stroke the stroke color/gradient
       */
    protected void addRect(int x, int y, int w, int h,
                           PDFPathPaint stroke) {
	closeText();
        currentStream.add("ET\nq\n" + stroke.getColorSpaceOut(false) +
                          (x / 1000f) + " " + (y / 1000f) + " " + (w / 1000f) +
                          " " + (h / 1000f) + " re s\n" + "Q\nBT\n");
    }

    /**
       * add a filled rectangle to the current stream
       *
       * @param x the x position of left edge in millipoints
       * @param y the y position of top edge in millipoints
       * @param w the width in millipoints
       * @param h the height in millipoints
       * @param fill the fill color/gradient
       * @param stroke the stroke color/gradient
       */
    protected void addRect(int x, int y, int w, int h,
                           PDFPathPaint stroke, PDFPathPaint fill) {
	closeText();
        currentStream.add("ET\nq\n" + fill.getColorSpaceOut(true) +
                          stroke.getColorSpaceOut(false) + (x / 1000f) + " " +
                          (y / 1000f) + " " + (w / 1000f) + " " + (h / 1000f) +
                          " re b\n" + "Q\nBT\n");
    }

    /**
       * render area container to PDF
       *
       * @param area the area container to render
       */
    public void renderAreaContainer(AreaContainer area) {

        int saveY = this.currentYPosition;
        int saveX = this.currentAreaContainerXPosition;

        if (area.getPosition() == Position.ABSOLUTE) {
            // Y position is computed assuming positive Y axis, adjust for negative postscript one
            this.currentYPosition =
              area.getYPosition() - 2 * area.getPaddingTop() -
              2 * area.borderWidthTop;
            this.currentAreaContainerXPosition = area.getXPosition();
        } else if (area.getPosition() == Position.RELATIVE) {
            this.currentYPosition -= area.getYPosition();
            this.currentAreaContainerXPosition += area.getXPosition();
        } else if (area.getPosition() == Position.STATIC) {
            this.currentYPosition -=
              area.getPaddingTop() + area.borderWidthTop;
            this.currentAreaContainerXPosition +=
              area.getPaddingLeft() + area.borderWidthLeft;
        }

        this.currentXPosition = this.currentAreaContainerXPosition;
        doFrame(area);

        Enumeration e = area.getChildren().elements();
        while (e.hasMoreElements()) {
            Box b = (Box) e.nextElement();
            b.render(this);
        }
        if (area.getPosition() != Position.STATIC) {
            this.currentYPosition = saveY;
            this.currentAreaContainerXPosition = saveX;
        } else
            this.currentYPosition -= area.getHeight();
    }

	public void renderBodyAreaContainer(BodyAreaContainer area) {
        int saveY = this.currentYPosition;
        int saveX = this.currentAreaContainerXPosition;

        if (area.getPosition() == Position.ABSOLUTE) {
            // Y position is computed assuming positive Y axis, adjust for negative postscript one
            this.currentYPosition = area.getYPosition();
            this.currentAreaContainerXPosition = area.getXPosition();
        } else if (area.getPosition() == Position.RELATIVE) {
            this.currentYPosition -= area.getYPosition();
            this.currentAreaContainerXPosition += area.getXPosition();
        }

        this.currentXPosition = this.currentAreaContainerXPosition;
        int w, h;
        int rx = this.currentAreaContainerXPosition;
        w = area.getContentWidth();
        h = area.getContentHeight();
        int ry = this.currentYPosition;
        ColorType bg = area.getBackgroundColor();

        // I'm not sure I should have to check for bg being null
        // but I do
        if ((bg != null) && (bg.alpha() == 0)) {
            this.addRect(rx, ry, w, -h, new PDFColor(bg), new PDFColor(bg));
        }

		// floats & footnotes stuff
		renderAreaContainer(area.getBeforeFloatReferenceArea());
  		renderAreaContainer(area.getFootnoteReferenceArea());

		// main reference area
		Enumeration e = area.getMainReferenceArea().getChildren().elements();
		while (e.hasMoreElements()) {
			Box b = (Box) e.nextElement();
			b.render(this);	// span areas
		}

        if (area.getPosition() != Position.STATIC) {
            this.currentYPosition = saveY;
            this.currentAreaContainerXPosition = saveX;
        } else
            this.currentYPosition -= area.getHeight();

	}

	public void renderSpanArea(SpanArea area) {
		Enumeration e = area.getChildren().elements();
		while (e.hasMoreElements()) {
			Box b = (Box) e.nextElement();
			b.render(this);	// column areas
		}
	}

    private void doFrame(Area area) {
        int w, h;
        int rx = this.currentAreaContainerXPosition;
        w = area.getContentWidth();
        if (area instanceof BlockArea)
            rx += ((BlockArea) area).getStartIndent();
        h = area.getContentHeight();
        int ry = this.currentYPosition;
        ColorType bg = area.getBackgroundColor();

        rx = rx - area.getPaddingLeft();
        ry = ry + area.getPaddingTop();
        w = w + area.getPaddingLeft() + area.getPaddingRight();
        h = h + area.getPaddingTop() + area.getPaddingBottom();

        // I'm not sure I should have to check for bg being null
        // but I do
        if ((bg != null) && (bg.alpha() == 0)) {
            this.addRect(rx, ry, w, -h, new PDFColor(bg), new PDFColor(bg));
        }

        rx = rx - area.borderWidthLeft;
        ry = ry + area.borderWidthTop;
        w = w + area.borderWidthLeft + area.borderWidthRight;
        h = h + area.borderWidthTop + area.borderWidthBottom;

        if (area.borderWidthTop != 0)
            addLine(rx, ry, rx + w, ry, area.borderWidthTop,
                    new PDFColor(area.borderColorTop));
        if (area.borderWidthLeft != 0)
            addLine(rx, ry, rx, ry - h, area.borderWidthLeft,
                    new PDFColor(area.borderColorLeft));
        if (area.borderWidthRight != 0)
            addLine(rx + w, ry, rx + w, ry - h, area.borderWidthRight,
                    new PDFColor(area.borderColorRight));
        if (area.borderWidthBottom != 0)
            addLine(rx, ry - h, rx + w, ry - h, area.borderWidthBottom,
                    new PDFColor(area.borderColorBottom));

    }


    /**
       * render block area to PDF
       *
       * @param area the block area to render
       */
    public void renderBlockArea(BlockArea area) {
      // KLease: Temporary test to fix block positioning
      // Offset ypos by padding and border widths
      // this.currentYPosition -= (area.getPaddingTop() + area.borderWidthTop);
        doFrame(area);
        Enumeration e = area.getChildren().elements();
        while (e.hasMoreElements()) {
            Box b = (Box) e.nextElement();
            b.render(this);
        }
	//  this.currentYPosition -= (area.getPaddingBottom() + area.borderWidthBottom);
    }

    /**
       * render display space to PDF
       *
       * @param space the display space to render
       */
    public void renderDisplaySpace(DisplaySpace space) {
        int d = space.getSize();
        this.currentYPosition -= d;
    }

    /**
       * render image area to PDF
       *
       * @param area the image area to render
       */
    public void renderImageArea(ImageArea area) {
        // adapted from contribution by BoBoGi
        int x = this.currentAreaContainerXPosition + area.getXOffset();
        int y = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        this.currentYPosition -= h;

        FopImage img = area.getImage();
        if (img instanceof SVGImage) {
            try {
		closeText();
		
                SVGSVGElement svg =
                  ((SVGImage) img).getSVGDocument().getRootElement();
                currentStream.add("ET\nq\n" + (((float) w) / 1000f) +
                                  " 0 0 " + (((float) h) / 1000f) + " " +
                                  (((float) x) / 1000f) + " " +
                                  (((float)(y - h)) / 1000f) + " cm\n");
                //        renderSVG(svg, (int) x, (int) y);
                currentStream.add("Q\nBT\n");
            } catch (FopImageException e) {
            }
        } else {
            int xObjectNum = this.pdfDoc.addImage(img);
	    closeText();
	    
            currentStream.add("ET\nq\n" + (((float) w) / 1000f) +
                              " 0 0 " + (((float) h) / 1000f) + " " +
                              (((float) x) / 1000f) + " " +
                              (((float)(y - h)) / 1000f) + " cm\n" + "/Im" +
                              xObjectNum + " Do\nQ\nBT\n");
        }
    }

    /** render a foreign object area */
    public void renderForeignObjectArea(ForeignObjectArea area) {
        // if necessary need to scale and align the content
        this.currentXPosition = this.currentXPosition + area.getXOffset();
        this.currentYPosition = this.currentYPosition;
        switch (area.getAlign()) {
            case TextAlign.START:
                break;
            case TextAlign.END:
                break;
            case TextAlign.CENTER:
            case TextAlign.JUSTIFY:
                break;
        }
        switch (area.getVerticalAlign()) {
            case VerticalAlign.BASELINE:
                break;
            case VerticalAlign.MIDDLE:
                break;
            case VerticalAlign.SUB:
                break;
            case VerticalAlign.SUPER:
                break;
            case VerticalAlign.TEXT_TOP:
                break;
            case VerticalAlign.TEXT_BOTTOM:
                break;
            case VerticalAlign.TOP:
                break;
            case VerticalAlign.BOTTOM:
                break;
        }
	closeText();
	
        // in general the content will not be text
        currentStream.add("ET\n");
        // align and scale
        currentStream.add("q\n");
        switch (area.scalingMethod()) {
            case Scaling.UNIFORM:
                break;
            case Scaling.NON_UNIFORM:
                break;
        }
        // if the overflow is auto (default), scroll or visible
        // then the contents should not be clipped, since this
        // is considered a printing medium.
        switch (area.getOverflow()) {
            case Overflow.VISIBLE:
            case Overflow.SCROLL:
            case Overflow.AUTO:
                break;
            case Overflow.HIDDEN:
                break;
        }

        area.getObject().render(this);
        currentStream.add("Q\n");
        currentStream.add("BT\n");
        this.currentXPosition += area.getEffectiveWidth();
//        this.currentYPosition -= area.getEffectiveHeight();
    }

    /**
       * render SVG area to PDF
       *
       * @param area the SVG area to render
       */
    public void renderSVGArea(SVGArea area) {
        // place at the current instream offset
        int x = this.currentXPosition;
	// Buggy: Method getXOffset() not found in class org.apache.fop.dom.svg.SVGArea
        //int x = this.currentAreaContainerXPosition + area.getXOffset();
        int y = this.currentYPosition;
        SVGSVGElement svg = area.getSVGDocument().getRootElement();
        int w = (int)(svg.getWidth().getBaseVal().getValue() * 1000);
        int h = (int)(svg.getHeight().getBaseVal().getValue() * 1000);
	float sx = 1, sy = -1;
	int xOffset = x, yOffset = y;

        /*
         * Clip to the svg area.
         * Note: To have the svg overlay (under) a text area then use
         * an fo:block-container
         */
        currentStream.add("q\n");
	if (w != 0 && h != 0) {
        currentStream.add(x / 1000f + " " + y / 1000f + " m\n");
        currentStream.add((x + w) / 1000f + " " + y / 1000f + " l\n");
        currentStream.add((x + w) / 1000f + " " + (y - h) / 1000f + " l\n");
        currentStream.add(x / 1000f + " " + (y - h) / 1000f + " l\n");
        currentStream.add("h\n");
        currentStream.add("W\n");
        currentStream.add("n\n");
	}
        // transform so that the coordinates (0,0) is from the top left
        // and positive is down and to the right. (0,0) is where the
	// viewBox puts it.
        currentStream.add(sx + " 0 0 " + sy + " " +
                          xOffset / 1000f + " " + yOffset / 1000f + " cm\n");


        SVGDocument doc = area.getSVGDocument();

        UserAgent userAgent = new MUserAgent(new AffineTransform());

        GVTBuilder builder = new GVTBuilder();
        GraphicsNodeRenderContext rc = getRenderContext();
        BridgeContext ctx = new BridgeContext(userAgent, rc);
        GraphicsNode root;
		//System.out.println("creating PDFGraphics2D");
        PDFGraphics2D graphics = new PDFGraphics2D(true, area.getFontState(), pdfDoc,
                          currentFontName, currentFontSize, currentXPosition,
                          currentYPosition);
        graphics.setGraphicContext(new org.apache.batik.ext.awt.g2d.GraphicContext());
        graphics.setRenderingHints(rc.getRenderingHints());
        try {
            root = builder.build(ctx, doc);
            root.paint(graphics, rc);
            currentStream.add(graphics.getString());
        } catch(Exception e) {
            e.printStackTrace();
        }

        currentStream.add("Q\n");
    }

    public GraphicsNodeRenderContext getRenderContext() {
        GraphicsNodeRenderContext nodeRenderContext = null;
        if (nodeRenderContext == null) {
            RenderingHints hints = new RenderingHints(null);
            hints.put(RenderingHints.KEY_ANTIALIASING,
                  RenderingHints.VALUE_ANTIALIAS_ON);

            hints.put(RenderingHints.KEY_INTERPOLATION,
                  RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            FontRenderContext fontRenderContext =
                new FontRenderContext(new AffineTransform(), true, true);

            TextPainter textPainter = new StrokingTextPainter();

            GraphicsNodeRableFactory gnrFactory =
                new ConcreteGraphicsNodeRableFactory();

            nodeRenderContext =
                new GraphicsNodeRenderContext(new AffineTransform(),
                                          null,
                                          hints,
                                          fontRenderContext,
                                          textPainter,
                                          gnrFactory);
                nodeRenderContext.setTextPainter(textPainter);
            }

        return nodeRenderContext;
    }

    /**
       * render inline area to PDF
       *
       * @param area inline area to render
       */
    public void renderWordArea(WordArea area) {
	//  char ch;
        StringBuffer pdf = new StringBuffer();

	Hashtable kerning = null;
	boolean kerningAvailable = false;
	
        kerning = area.getFontState().getKerning();
        if (kerning != null && !kerning.isEmpty()) {
            kerningAvailable = true;
        }

        String name = area.getFontState().getFontName();
        int size = area.getFontState().getFontSize();

            // This assumes that *all* CIDFonts use a /ToUnicode mapping
        boolean useMultiByte = false;
        Font f = (Font)area.getFontState().getFontInfo().getFonts().get(name);
        if (f instanceof CIDFont)
            useMultiByte=true;
            //String startText = useMultiByte ? "<FEFF" : "(";
        String startText = useMultiByte ? "<" : "(";
        String endText = useMultiByte ? ">" : ")";
        
        PDFColor theAreaColor = new PDFColor((double) area.getRed(),
                                             (double) area.getGreen(), 
					     (double) area.getBlue());

        if ((!name.equals(this.currentFontName)) ||
                (size != this.currentFontSize)) {
	    closeText();
	    
            this.currentFontName = name;
            this.currentFontSize = size;
            pdf = pdf.append("/" + name + " " + (size / 1000) + " Tf\n");
        }


        if (!(theAreaColor.equals(this.currentFill))) {
	    closeText();
	    this.currentFill = theAreaColor;
	    pdf.append(this.currentFill.getColorSpaceOut(true));
        }


        int rx = this.currentXPosition;
        int bl = this.currentYPosition;


        if (area.getUnderlined()) {
            int yPos = bl - size/10;
            addLine(rx, yPos, rx + area.getContentWidth(),
                    yPos, size/14, theAreaColor);
            // save position for underlining a following InlineSpace
            prevUnderlineXEndPos = rx + area.getContentWidth();
            prevUnderlineYEndPos = yPos;
            prevUnderlineSize = size/14;
            prevUnderlineColor = theAreaColor;
        }

        if (area.getOverlined()) {
            int yPos = bl + area.getFontState().getAscender() + size/10;
            addLine(rx, yPos, rx + area.getContentWidth(),
                    yPos, size/14, theAreaColor);
            prevOverlineXEndPos = rx + area.getContentWidth();
            prevOverlineYEndPos = yPos;
            prevOverlineSize = size/14;
            prevOverlineColor = theAreaColor;
        }

        if (area.getLineThrough()) {
            int yPos = bl + area.getFontState().getAscender() * 3/8;
            addLine(rx, yPos, rx + area.getContentWidth(),
                    yPos, size/14, theAreaColor);
            prevLineThroughXEndPos = rx + area.getContentWidth();
            prevLineThroughYEndPos = yPos;
            prevLineThroughSize = size/14;
            prevLineThroughColor = theAreaColor;
        }


	if (OPTIMIZE_TEXT) {
	    if (!textOpen || bl != prevWordY) {
		closeText();
		
		pdf.append("1 0 0 1 " +(rx / 1000f) + " " + 
			   (bl / 1000f) + " Tm [" + startText);
		prevWordY = bl;
		textOpen = true;
	    }
	    else {
		// express the space between words in thousandths of an em
		int space = prevWordX - rx + prevWordWidth;
		float emDiff = (float)space / (float)currentFontSize * 1000f;
		pdf.append(emDiff + " " + startText);
	    }
	    prevWordWidth = area.getContentWidth();
	    prevWordX = rx;

	}
	else {
	    // original text render code sets the text transformation matrix
	    // for every word.
	    pdf.append("1 0 0 1 " +(rx / 1000f) + " " + (bl / 1000f) + " Tm ");
	    if (kerningAvailable) {
		pdf.append(" [" + startText);
	    }
	    else {
		pdf.append(" " + startText);
	    }
	}
	
        String s;
        if (area.getPageNumberID() != null) { // this text is a page number, so resolve it
            s = idReferences.getPageNumber(area.getPageNumberID());
            if (s == null) {
                s = "";
            }
        } else {
            s = area.getText();
        }

        int l = s.length();

        for (int i = 0; i < l; i++) {
            char ch = s.charAt(i);
	    String prepend = "";
	    
            if (!useMultiByte) {
                if(ch > 127) {
                    pdf.append("\\");
                    pdf.append(Integer.toOctalString((int) ch));
                } else {
                    switch (ch) {
                        case '(':
                        case ')':
                        case '\\':
                            prepend = "\\";
                            break;
                    }
                    pdf.append(getUnicodeString(prepend+ch, useMultiByte));
                }
	    } else {
                    pdf.append(getUnicodeString(prepend+ch, useMultiByte));
            }                

	    if (kerningAvailable && (i+1) < l) {
		pdf.append(addKerning((new Integer((int)ch)),
                                                       (new Integer((int)s.charAt(i+1))),
                                                       kerning,
                                                       startText, endText));
            }
            
        }
        pdf.append(endText + " ");
	if (!OPTIMIZE_TEXT) {
	    if (kerningAvailable) {
		pdf.append("] TJ\n");
	    }
	    else {
		pdf.append("Tj\n");
	    }
	    
	}
	


        currentStream.add(pdf.toString());

        this.currentXPosition += area.getContentWidth();
	
    }


        /**
         * Convert a string to a unicode hex representation
         */
    private String getUnicodeString(StringBuffer str, boolean useMultiByte) {
        return getUnicodeString(str.toString(), useMultiByte);
    }
    
        /**
         * Convert a string to a multibyte hex representation
         */
    private String getUnicodeString(String str, boolean useMultiByte) {
        if (!useMultiByte) {
            return str;
        } else {
            StringBuffer buf = new StringBuffer(str.length()*4);
            byte[] uniBytes = null;
            try {
                uniBytes = str.getBytes("UnicodeBigUnmarked");
            } catch (Exception e) {
                    // This should never fail
            }
            
            for (int i = 0; i < uniBytes.length; i++) {
                int b = (uniBytes[i] < 0) ? (int)(256+uniBytes[i])
                    : (int)uniBytes[i];
                
                String hexString=Integer.toHexString(b);
                if (hexString.length()==1)
                    buf=buf.append("0"+hexString);
                else
                    buf=buf.append(hexString);
            }

            return buf.toString();
        }
    }
    
    
    /** Checks to see if we have some text rendering commands open
     * still and writes out the TJ command to the stream if we do
     */
    private void closeText() 
    {
	if (OPTIMIZE_TEXT && textOpen) {
	    currentStream.add("] TJ\n");
	    textOpen = false;
	    prevWordX = 0;
	    prevWordY = 0;
	}
    }
    
	

    /**
       * render inline space to PDF
       *
       * @param space space to render
       */
    public void renderInlineSpace(InlineSpace space) {
        this.currentXPosition += space.getSize();
        if (space.getUnderlined()) {
            if (prevUnderlineColor != null) {
               addLine(prevUnderlineXEndPos, prevUnderlineYEndPos,
                       prevUnderlineXEndPos + space.getSize(),
                       prevUnderlineYEndPos, prevUnderlineSize, prevUnderlineColor);
            }
        }
        if (space.getOverlined()) {
            if (prevOverlineColor != null) {
                addLine(prevOverlineXEndPos, prevOverlineYEndPos,
                        prevOverlineXEndPos + space.getSize(),
                        prevOverlineYEndPos, prevOverlineSize, prevOverlineColor);
            }
        }
        if (space.getLineThrough()) {
            if (prevLineThroughColor != null) {
                addLine(prevLineThroughXEndPos, prevLineThroughYEndPos,
                        prevLineThroughXEndPos + space.getSize(),
                        prevLineThroughYEndPos, prevLineThroughSize, prevLineThroughColor);
            }
         }

    }

    /**
       * render line area to PDF
       *
       * @param area area to render
       */
    public void renderLineArea(LineArea area) {
        int rx = this.currentAreaContainerXPosition + area.getStartIndent();
        int ry = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        this.currentYPosition -= area.getPlacementOffset();
        this.currentXPosition = rx;

        int bl = this.currentYPosition;

        Enumeration e = area.getChildren().elements();
        while (e.hasMoreElements()) {
            Box b = (Box) e.nextElement();
            if(b instanceof InlineArea) {
                InlineArea ia = (InlineArea)b;
                this.currentYPosition = ry - ia.getYOffset();
            } else {
                this.currentYPosition = ry - area.getPlacementOffset();
            }
            b.render(this);
        }

        this.currentYPosition = ry - h;
        this.currentXPosition = rx;
    }


   private StringBuffer addKerning(Integer ch1, Integer ch2,
                                   Hashtable kerning, String startText,
                                   String endText) {
      Hashtable h2=(Hashtable)kerning.get(ch1);
      int pwdt=0;
      StringBuffer buf=new StringBuffer("");

      if (h2!=null) {
         Integer wdt=(Integer)h2.get(ch2);
         if (wdt!=null) {
            pwdt=-wdt.intValue();
            buf=buf.append(endText + " " + pwdt + " " + startText);
         }
      }
      return buf;
   }

    /**
       * render page into PDF
       *
       * @param page page to render
       */
    public void renderPage(Page page) {
		BodyAreaContainer body;
        AreaContainer before, after;

        currentStream = this.pdfDoc.makeStream();
        body = page.getBody();
        before = page.getBefore();
        after = page.getAfter();

        this.currentFontName = "";
        this.currentFontSize = 0;

	currentStream.add("BT\n");

        renderBodyAreaContainer(body);

        if (before != null) {
            renderAreaContainer(before);
        }

        if (after != null) {
            renderAreaContainer(after);
        }
	closeText();
	
        currentStream.add("ET\n");

        currentPage = this.pdfDoc.makePage(this.pdfResources, currentStream,
                                           page.getWidth() / 1000,
					   page.getHeight() / 1000, page);

        if (page.hasLinks()) {
            currentAnnotList = this.pdfDoc.makeAnnotList();
            currentPage.setAnnotList(currentAnnotList);

            Enumeration e = page.getLinkSets().elements();
            while (e.hasMoreElements()) {
                LinkSet linkSet = (LinkSet) e.nextElement();

                linkSet.align();
                String dest = linkSet.getDest();
                int linkType = linkSet.getLinkType();
                Enumeration f = linkSet.getRects().elements();
                while (f.hasMoreElements()) {
                    LinkedRectangle lrect =
                      (LinkedRectangle) f.nextElement();
                    currentAnnotList.addLink(
                      this.pdfDoc.makeLink(lrect.getRectangle(),
                                           dest, linkType));
                }
            }
        } else {
            // just to be on the safe side
            currentAnnotList = null;
        }
    }

    /**
       * render leader area into PDF
       *
       * @param area area to render
       */
    public void renderLeaderArea(LeaderArea area) {
        int rx = this.currentXPosition;
        ;
        int ry = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();
        int th = area.getRuleThickness();
        int st = area.getRuleStyle();
        String rs = setRuleStylePattern(st);
        //checks whether thickness is = 0, because of bug in pdf (or where?),
        //a line with thickness 0 is still displayed
        if (th != 0) {
            switch (st) {
                case org.apache.fop.fo.properties.RuleStyle.DOUBLE:
                    addLine(rx, ry, rx + w, ry, th / 3, rs,
                            new PDFColor(area.getRed(),
                                         area.getGreen(), area.getBlue()));
                    addLine(rx, ry + (2 * th / 3), rx + w,
                            ry + (2 * th / 3), th / 3, rs,
                            new PDFColor(area.getRed(),
                                         area.getGreen(), area.getBlue()));
                    break;
                case org.apache.fop.fo.properties.RuleStyle.GROOVE:
                    addLine(rx, ry, rx + w, ry, th / 2, rs,
                            new PDFColor(area.getRed(),
                                         area.getGreen(), area.getBlue()));
                    addLine(rx, ry + (th / 2), rx + w, ry + (th / 2),
                            th / 2, rs, new PDFColor(255, 255, 255));
                    break;
                case org.apache.fop.fo.properties.RuleStyle.RIDGE:
                    addLine(rx, ry, rx + w, ry, th / 2, rs,
                            new PDFColor(255, 255, 255));
                    addLine(rx, ry + (th / 2), rx + w, ry + (th / 2),
                            th / 2, rs,
                            new PDFColor(area.getRed(),
                                         area.getGreen(), area.getBlue()));
                    break;
                default:
                    addLine(rx, ry, rx + w, ry, th, rs,
                            new PDFColor(area.getRed(),
                                         area.getGreen(), area.getBlue()));
            }
            this.currentXPosition += area.getContentWidth();
            this.currentYPosition += th;
        }
    }

    /**
       * set up the font info
       *
       * @param fontInfo font info to set up
       */
    public void setupFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
        FontSetup.setup(fontInfo);
    }

    /**
       * defines a string containing dashArray and dashPhase for the rule style
       */
    private String setRuleStylePattern (int style) {
        String rs = "";
        switch (style) {
            case org.apache.fop.fo.properties.RuleStyle.SOLID:
                rs = "[] 0 d ";
                break;
            case org.apache.fop.fo.properties.RuleStyle.DASHED:
                rs = "[3 3] 0 d ";
                break;
            case org.apache.fop.fo.properties.RuleStyle.DOTTED:
                rs = "[1 3] 0 d ";
                break;
            case org.apache.fop.fo.properties.RuleStyle.DOUBLE:
                rs = "[] 0 d ";
                break;
            default:
                rs = "[] 0 d ";
        }
        return rs;
    }

    protected void renderRootExtensions(AreaTree areaTree) 
    {
	Vector v = areaTree.getExtensions();
	if (v != null) {
	    Enumeration e = v.elements();
	    while (e.hasMoreElements()) {
		ExtensionObj ext = (ExtensionObj)e.nextElement();
		if (ext instanceof Outline) {
		    renderOutline((Outline)ext);
		}
	    }
	}
	
    }

    private void renderOutline(Outline outline) 
    {
	if (rootOutline == null) {
	    rootOutline = this.pdfDoc.makeOutlineRoot();
	}
	PDFOutline pdfOutline = null;
	Outline parent = outline.getParentOutline();
	if (parent == null) {
	    pdfOutline = this.pdfDoc.makeOutline(rootOutline, 
						 outline.getLabel().toString(),
						 outline.getInternalDestination());
	}
	else {
	    PDFOutline pdfParentOutline = (PDFOutline)parent.getRendererObject();
	    if (pdfParentOutline == null) {
		MessageHandler.errorln("Error: pdfParentOutline is null");
	    }
	    else {
		pdfOutline = this.pdfDoc.makeOutline(pdfParentOutline, 
						     outline.getLabel().toString(),
						     outline.getInternalDestination());
	    }
	   
	}
	outline.setRendererObject(pdfOutline);
	
	// handle sub outlines	
	Vector v = outline.getOutlines();
	Enumeration e = v.elements();
	while (e.hasMoreElements()) {
	    renderOutline((Outline)e.nextElement());
	}
    }
 
    protected class MUserAgent implements UserAgent {
        AffineTransform currentTransform = null;
        /**
         * Creates a new SVGUserAgent.
         */
        protected MUserAgent(AffineTransform at) {
            currentTransform = at;
        }

        /**
         * Displays an error message.
         */
        public void displayError(String message) {
            System.err.println(message);
        }
    
        /**
         * Displays an error resulting from the specified Exception.
         */
        public void displayError(Exception ex) {
            ex.printStackTrace(System.err);
        }

        /**
         * Displays a message in the User Agent interface.
         * The given message is typically displayed in a status bar.
         */
        public void displayMessage(String message) {
            System.out.println(message);
        }

        /**
         * Returns a customized the pixel to mm factor.
         */
        public float getPixelToMM() {
            return 0.264583333333333333333f; // 72 dpi
        }

        /**
         * Returns the language settings.
         */
        public String getLanguages() {
            return "en";//userLanguages;
        }

        /**
         * Returns the user stylesheet uri.
         * @return null if no user style sheet was specified.
         */
        public String getUserStyleSheetURI() {
            return null;//userStyleSheetURI;
        }

        /**
         * Returns the class name of the XML parser.
         */
        public String getXMLParserClassName() {
	String parserClassName =
	    System.getProperty("org.xml.sax.parser");
	if (parserClassName == null) {
	    parserClassName = "org.apache.xerces.parsers.SAXParser";
	}
            return parserClassName;//application.getXMLParserClassName();
        }

        /**
         * Opens a link in a new component.
         * @param doc The current document.
         * @param uri The document URI.
         */
        public void openLink(SVGAElement elt)
        {
            //application.openLink(uri);
        }

        public Point getClientAreaLocationOnScreen()
        {
            return new Point(0, 0);
        }

        public void setSVGCursor(java.awt.Cursor cursor)
        {
        }

        public AffineTransform getTransform()
        {
            return currentTransform;
        }

        public Dimension2D getViewportSize()
        {
            return new Dimension(100, 100);
        }

        public EventDispatcher getEventDispatcher()
        {
            return null;
        }

        public boolean supportExtension(String str)
	{
	    return false;
	}

        public boolean hasFeature(String str)
	{
            return false;
	}

        public void registerExtension(BridgeExtension be)
        {
        }
    }
}
