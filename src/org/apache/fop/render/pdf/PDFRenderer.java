/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.render.pdf;

// FOP
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.FopImage;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.pdf.*;
import org.apache.fop.layout.*;
import org.apache.fop.image.*;
import org.apache.fop.extensions.*;
import org.apache.fop.datatypes.IDReferences;

import org.w3c.dom.svg.*;

import org.apache.fop.dom.svg.*;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

/**
 * Renderer that renders areas to PDF
 */
public class PDFRenderer extends PrintRenderer {

    private static final boolean OPTIMIZE_TEXT = true;
    
    /** the PDF Document being created */
    protected PDFDocument pdfDoc;

    /** the /Resources object of the PDF document being created */
    protected PDFResources pdfResources;

    /** the current stream to add PDF commands to */
    PDFStream currentStream;

    /** the current annotation list to add annotations to */
    PDFAnnotList currentAnnotList;

    /** the current page to add annotations to */
    PDFPage currentPage;

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
      * @param rs the rule style
      * @param r the red component
      * @param g the green component
      * @param b the blue component
      */
    protected void addLine(int x1, int y1, int x2, int y2, int th,
                           int rs, PDFPathPaint stroke) {
	closeText();
        currentStream.add("ET\nq\n" + stroke.getColorSpaceOut(false) +
                          setRuleStylePattern(rs) + (x1 / 1000f) + " "+ (y1 / 1000f) + " m " +
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

	// translate and scale according to viewbox.
	if (svg.getViewBox () != null) {
	    SVGRect view = svg.getViewBox().getBaseVal();

	    // TODO take aspect constraints (attribute preserveAspectRatio)
	    // into account.
	    // Viewbox coordinates are all relative to the viewport
	    // (ie. the x,y,w and h values calculated above).
	    sx = svg.getWidth().getBaseVal().getValue() / view.getWidth ();
	    sy = svg.getHeight().getBaseVal().getValue() / view.getHeight ();

	    // move the origin
	    xOffset -= (int)(sx * view.getX () * 1000f);
	    yOffset -= (int)(sy * view.getY () * 1000f);

	    sy = -sy;
	}

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

        SVGRenderer svgRenderer =
          new SVGRenderer(area.getFontState(), pdfDoc,
                          currentFontName, currentFontSize, currentXPosition,
                          currentYPosition);
        svgRenderer.renderSVG(svg, 0, 0);
        currentStream.add(svgRenderer.getString());

        currentStream.add("Q\n");
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

		addWordLines(area, rx, bl, size, theAreaColor);

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
        AreaContainer before, after, start, end;

        currentStream = this.pdfDoc.makeStream();
        body = page.getBody();
        before = page.getBefore();
        after = page.getAfter();
        start = page.getStart();
        end = page.getEnd();

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

		if (start != null) {
            renderAreaContainer(start);
        }

		if (end != null) {
            renderAreaContainer(end);
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
}
