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
import org.apache.fop.datatypes.*;
import org.apache.fop.svg.PathPoint;
import org.apache.fop.pdf.*;
import org.apache.fop.layout.*;
import org.apache.fop.image.*;

import org.w3c.dom.svg.*;
import org.w3c.dom.svg.SVGLength;

import org.apache.fop.dom.svg.*;
import org.apache.fop.dom.svg.SVGRectElementImpl;
import org.apache.fop.dom.svg.SVGTextElementImpl;
import org.apache.fop.dom.svg.SVGLineElementImpl;
import org.apache.fop.dom.svg.SVGArea;

// Java
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.awt.Rectangle;
import java.util.Vector;
import java.util.Hashtable;

/**
 * Renderer that renders areas to PDF
 */
public class PDFRenderer implements Renderer {
			
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
	 * @param writer the PrintWriter to write the PDF with
	 */
	public void render(AreaTree areaTree, PrintWriter writer)
        throws IOException, FOPException {      
            MessageHandler.logln("rendering areas to PDF");
            idReferences=areaTree.getIDReferences();           
            this.pdfResources = this.pdfDoc.getResources();            
            this.pdfDoc.setIDReferences(idReferences);
	Enumeration e = areaTree.getPages().elements();
            while ( e.hasMoreElements() ) {
		this.renderPage((Page) e.nextElement());
	}
    
            if ( !idReferences.isEveryIdValid() ) {
                throw new FOPException("The id \""+idReferences.getNextInvalidId()+"\" was referenced but does not exist\n");            
            }
    
            MessageHandler.logln("writing out PDF");
	this.pdfDoc.output(writer);
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
    protected void addLine(int x1, int y1, int x2, int y2, int th, PDFPathPaint stroke)
    {
		currentStream.add(stroke.getColorSpaceOut(false));
		currentStream.add(th / 1000f + " w\n");
		addLine(x1/1000f, y1/1000f, x2/1000f, y2/1000f, false);
		currentStream.add("0 G\n");
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
    protected void addLine(float x1, float y1, float x2, float y2, boolean fill)
	{
    	String str;
    	str = "" + x1 + " " + y1 + " m "
			  + x2 + " " + y2 + " l";
		if(fill)
			currentStream.add(str + " f\n");
		currentStream.add(str + " S\n");
    }

    protected void addCircle(float cx, float cy, float r, boolean fill)
    {
    	String str;
    	str = "" + cx + " " + (cy - r) + " m\n"
			+ "" + (cx + 21 * r / 40f) + " " + (cy - r) + " " + (cx + r) + " " + (cy - 21 * r / 40f) + " " + (cx + r) + " " + cy + " c\n"
			+ "" + (cx + r) + " " + (cy + 21 * r / 40f) + " " + (cx + 21 * r / 40f) + " " + (cy + r) + " " + cx + " " + (cy + r) + " c\n"
			+ "" + (cx - 21 * r / 40f) + " " + (cy + r) + " " + (cx - r) + " " + (cy + 21 * r / 40f) + " " + (cx - r) + " " + cy + " c\n"
			+ "" + (cx - r) + " " + (cy - 21 * r / 40f) + " " + (cx - 21 * r / 40f) + " " + (cy - r) + " " + cx + " " + (cy - r) + " c\n";

		if(fill)
			currentStream.add(str + "f\n");
		currentStream.add(str + "S\n");
    }

    protected void addEllipse(float cx, float cy, float rx, float ry, boolean fill)
    {
    	String str;
    	str = "" + cx + " " + (cy - ry) + " m\n"
			+ "" + (cx + 21 * rx / 40f) + " " + (cy - ry) + " " + (cx + rx) + " " + (cy - 21 * ry / 40f) + " " + (cx + rx) + " " + cy + " c\n"
			+ "" + (cx + rx) + " " + (cy + 21 * ry / 40f) + " " + (cx + 21 * rx / 40f) + " " + (cy + ry) + " " + cx + " " + (cy + ry) + " c\n"
			+ "" + (cx - 21 * rx / 40f) + " " + (cy + ry) + " " + (cx - rx) + " " + (cy + 21 * ry / 40f) + " " + (cx - rx) + " " + cy + " c\n"
			+ "" + (cx - rx) + " " + (cy - 21 * ry / 40f) + " " + (cx - 21 * rx / 40f) + " " + (cy - ry) + " " + cx + " " + (cy - ry) + " c\n";
		if(fill)
			currentStream.add(str + "f\n");
		currentStream.add(str + "S\n");
    }

    /**
     * add a filled rectangle to the current stream
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param r the red component of edges
     * @param g the green component of edges
     * @param b the blue component of edges
     * @param fr the red component of the fill
     * @param fg the green component of the fill
     * @param fb the blue component of the fill
     */
    protected void addRect(float x, float y, float w, float h, boolean fill)
    {
    	String str = "" + x + " " + y + " "
				  + w + " " + h + " re";
		if(fill)
			currentStream.add(str + " f\n");
		currentStream.add(str + " S\n");
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
	currentStream.add(stroke.getColorSpaceOut(false)
			  + (x/1000f) + " " + (y/1000f) + " "
			  + (w/1000f) + " " + (h/1000f) + " re S\n"
			  + "0 G\n");
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
			   PDFPathPaint stroke,
			   PDFPathPaint fill) {
	currentStream.add(fill.getColorSpaceOut(true)
			  + stroke.getColorSpaceOut(false)
			  + (x/1000f) + " " + (y/1000f) + " "
			  + (w/1000f) + " " + (h/1000f) + " re S\n"
			  + (x/1000f) + " " + (y/1000f) + " "
			  + (w/1000f) + " " + (h/1000f) + " re f\n"
			  + "0 g\n");
    }

    protected void addPath(Vector points, int posx, int posy, boolean fill)
    {
    	SVGPathSegImpl pathmoveto = null;
    	float lastx = 0;
    	float lasty = 0;
    	for(Enumeration e = points.elements(); e.hasMoreElements(); ) {
    		SVGPathSegImpl pc = (SVGPathSegImpl)e.nextElement();
    		float[] vals = pc.getValues();
    		float lastcx = 0;
    		float lastcy = 0;
    		switch(pc.getPathSegType()) {
    			case SVGPathSeg.SVG_PATHSEG_MOVETO_ABS:
    				pathmoveto = pc;
	    			lastx = vals[0];
	    			lasty = vals[1];
					currentStream.add(lastx + " " + lasty + " m\n");
    			break;
    			case SVGPathSeg.SVG_PATHSEG_MOVETO_REL:
	    			if(pathmoveto == null) {
	    				lastx = vals[0];
		    			lasty = vals[1];
		    			pathmoveto = pc;
						currentStream.add(lastx + " " + lasty + " m\n");
	    			} else {
	    				lastx += vals[0];
		    			lasty += vals[1];
						currentStream.add(lastx + " " + lasty + " l\n");
	    			}
    			break;
    			case SVGPathSeg.SVG_PATHSEG_LINETO_ABS:
	    			lastx = vals[0];
	    			lasty = vals[1];
					currentStream.add(lastx + " " + lasty + " l\n");
    			break;
    			case SVGPathSeg.SVG_PATHSEG_LINETO_REL:
	    			lastx += vals[0];
	    			lasty += vals[1];
					currentStream.add(lastx + " " + lasty + " l\n");
    			break;
    			case SVGPathSeg.SVG_PATHSEG_LINETO_VERTICAL_ABS:
	    			lasty = vals[0];
					currentStream.add(lastx + " " + lasty + " l\n");
    			break;
    			case SVGPathSeg.SVG_PATHSEG_LINETO_VERTICAL_REL:
	    			lasty += vals[0];
					currentStream.add(lastx + " " + lasty + " l\n");
    			break;
    			case SVGPathSeg.SVG_PATHSEG_LINETO_HORIZONTAL_ABS:
	    			lastx = vals[0];
					currentStream.add(lastx + " " + lasty + " l\n");
    			break;
    			case SVGPathSeg.SVG_PATHSEG_LINETO_HORIZONTAL_REL:
	    			lastx += vals[0];
					currentStream.add(lastx + " " + lasty + " l\n");
    			break;
    			case SVGPathSeg.SVG_PATHSEG_CURVETO_CUBIC_ABS:
	    			lastx = vals[4];
	    			lasty = vals[5];
	    			lastcx = vals[2];
	    			lastcy = vals[3];
					currentStream.add((vals[0]) + " " + (vals[1]) + " " +
										(vals[2]) + " " + (vals[3]) + " " +
										lastx + " " + lasty +
										" c\n");
    			break;
    			case SVGPathSeg.SVG_PATHSEG_CURVETO_CUBIC_REL:
					currentStream.add((vals[0] + lastx) + " " + (vals[1] + lasty) + " " +
										(vals[2] + lastx) + " " + (vals[3] + lasty) + " " +
										(vals[4] + lastx) + " " + (vals[5] + lasty) +
										" c\n");
	    			lastcx = vals[2] + lastx;
	    			lastcy = vals[3] + lasty;
	    			lastx += vals[4];
	    			lasty += vals[5];
    			break;
    			case SVGPathSeg.SVG_PATHSEG_CURVETO_CUBIC_SMOOTH_ABS:
    				if(lastcx == 0) {
    					lastcx = lastx;
    				}
    				if(lastcy == 0) {
    					lastcy = lasty;
    				}
	    			lastx = vals[2];
	    			lasty = vals[3];
					currentStream.add(lastcx + " " + lastcy + " " +
										(vals[0]) + " " + (vals[1]) + " " +
										lastx + " " + lasty +
										" c\n");
    			break;
    			case SVGPathSeg.SVG_PATHSEG_CURVETO_CUBIC_SMOOTH_REL:
    				if(lastcx == 0) {
    					lastcx = lastx;
    				}
    				if(lastcy == 0) {
    					lastcy = lasty;
    				}
					currentStream.add(lastcx + " " + lastcy + " " +
										(vals[0] + lastx) + " " + (vals[1] + lasty) + " " +
										(vals[2] + lastx) + " " + (vals[3] + lasty) +
										" c\n");
	    			lastx += vals[2];
	    			lasty += vals[3];
    			break;
    			case SVGPathSeg.SVG_PATHSEG_CURVETO_QUADRATIC_ABS:
    				if(lastcx == 0) {
    					lastcx = lastx;
    				}
    				if(lastcy == 0) {
    					lastcy = lasty;
    				}
	    			lastx = vals[0];
	    			lasty = vals[1];
	    			lastcx = 0;
	    			lastcy = 0;
					currentStream.add(lastcx + " " + lastcy + " " +
										lastx + " " + lasty +
										" y\n");
    			break;
    			case SVGPathSeg.SVG_PATHSEG_CURVETO_QUADRATIC_REL:
    				if(lastcx == 0) {
    					lastcx = lastx;
    				}
    				if(lastcy == 0) {
    					lastcy = lasty;
    				}
					currentStream.add(lastcx + " " + lastcy + " " +
										(vals[0] + lastx) + " " + (vals[1] + lasty) +
										" y\n");
	    			lastcx = 0;
	    			lastcy = 0;
	    			lastx += vals[0];
	    			lasty += vals[1];
    			break;
    			case SVGPathSeg.SVG_PATHSEG_CLOSEPATH:
					currentStream.add("h\n");
    			break;
    		}
    	}
		if(fill)
			currentStream.add("B\n");
		currentStream.add("S\n");
    }

    protected void addPolyline(Vector points, int posx, int posy, boolean fill, boolean close)
    {
		PathPoint pc;
    	float lastx = 0;
    	float lasty = 0;
    	Enumeration e = points.elements();
    	if(e.hasMoreElements()) {
    		pc = (PathPoint)e.nextElement();
			lastx = pc.x;
			lasty = pc.y;
			currentStream.add(lastx + " " + lasty + " m\n");
    	}
    	while(e.hasMoreElements()) {
    		pc = (PathPoint)e.nextElement();
			lastx = pc.x;
			lasty = pc.y;
			currentStream.add(lastx + " " + lasty + " l\n");
    	}
    	if(close)
			currentStream.add("h\n");
		if(fill)
			currentStream.add("b\n");
		currentStream.add("S\n");
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
 	  this.currentYPosition = area.getYPosition() - 2 * area.getPaddingTop() - 2 * area.borderWidthTop;
	  this.currentAreaContainerXPosition = area.getXPosition();
		} else if (area.getPosition() == Position.RELATIVE) {
	  this.currentYPosition -= area.getYPosition();
		  this.currentAreaContainerXPosition += area.getXPosition();
		} else if (area.getPosition() == Position.STATIC) {
	  this.currentYPosition -= area.getPaddingTop() + area.borderWidthTop;
		  this.currentAreaContainerXPosition += area.getPaddingLeft() + area.borderWidthLeft;
		}

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
	
	private void doFrame(Area area) {
		int w, h;
	int rx = this.currentAreaContainerXPosition;
	w = area.getContentWidth();
		if (area instanceof BlockArea)
	  rx += ((BlockArea)area).getStartIndent();
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
		this.addRect(rx, ry, w, -h,
			 new PDFColor(bg),
			 new PDFColor(bg));
	}
	
		rx = rx - area.borderWidthLeft;
		ry = ry + area.borderWidthTop;
		w = w + area.borderWidthLeft + area.borderWidthRight;
		h = h + area.borderWidthTop + area.borderWidthBottom;
		
		if (area.borderWidthTop != 0)
		  addLine(rx, ry, rx + w, ry, 
				area.borderWidthTop,  
				new PDFColor(area.borderColorTop));
		if (area.borderWidthLeft != 0)
		  addLine(rx, ry, rx, ry - h, 
				area.borderWidthLeft,  
				new PDFColor(area.borderColorLeft));
		if (area.borderWidthRight != 0)
		  addLine(rx + w, ry, rx + w, ry - h, 
				area.borderWidthRight,	
				new PDFColor(area.borderColorRight));
		if (area.borderWidthBottom != 0)
		  addLine(rx, ry - h, rx + w, ry - h, 
				area.borderWidthBottom,  
				new PDFColor(area.borderColorBottom));

	} 
   

	/**
	 * render block area to PDF
	 *
	 * @param area the block area to render
	 */
	public void renderBlockArea(BlockArea area) {
		doFrame(area);
	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
		Box b = (Box) e.nextElement();
		b.render(this);
	}
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
	int x = this.currentAreaContainerXPosition +
		area.getXOffset();
	int y = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();

	this.currentYPosition -= h;

	FopImage img = area.getImage();

	int xObjectNum = this.pdfDoc.addImage(img);

	currentStream.add("ET\nq\n" + (((float) w) / 1000f) + " 0 0 " +
			  (((float) h) / 1000f) + " " + 
			  (((float) x) / 1000f) + " " + 
			  (((float) (y - h)) / 1000f) + " cm\n" +
			  "/Im" + xObjectNum + " Do\nQ\nBT\n"); 
    }
    
	public void renderImage(String href, float x, float y, float width, float height)
	{
		try {
			FopImage img = FopImageFactory.Make(href);
			if(img != null) {
				int xObjectNum = this.pdfDoc.addImage(img);
				currentStream.add("q\n" + width + " 0 0 " +
						  height + " " +
						  x + " " +
						  y + " cm\n" +
						  "/Im" + xObjectNum + " Do\nQ\n");
//				img.close();
			}
		} catch(Exception e) {
			System.err.println("could not add image to SVG: " + href);
		}
	}

    /**
     * render SVG area to PDF
     *
     * @param area the SVG area to render
     */
	public void renderSVGArea(SVGArea area) {
		int x = this.currentAreaContainerXPosition;
		int y = this.currentYPosition;
		int w = area.getContentWidth();
		int h = area.getHeight();
		this.currentYPosition -= h;

		currentStream.add("ET\n");
		/*
		 * Clip to the svg area.
		 * Note: To have the svg overlay (under) a text area then use
		 * an fo:block-container
		 */
		currentStream.add("q\n");
		currentStream.add(x / 1000f + " " + y / 1000f + " m\n");
		currentStream.add((x + w) / 1000f + " " + y / 1000f + " l\n");
		currentStream.add((x + w) / 1000f + " " + (y - h) / 1000f + " l\n");
		currentStream.add(x / 1000f + " " + (y - h) / 1000f + " l\n");
		currentStream.add("h\n");
		currentStream.add("W\n");
		currentStream.add("n\n");
		// transform so that the coordinates (0,0) is from the top left
		// and positive is down and to the right
		currentStream.add(1 + " " + 0 + " " + 0 + " " + (-1) + " " + x / 1000f + " " + y / 1000f + " cm\n");

		// TODO - translate and clip to viewbox

		Enumeration e = area.getChildren().elements();
		while (e.hasMoreElements()) {
			Object o = e.nextElement();
			if(o instanceof GraphicImpl) {
				renderElement(area, (GraphicImpl)o, x, y);
			}
		}

		currentStream.add("Q\n");
		currentStream.add("BT\n");
	}

	void handleGradient(String sp, boolean fill, GraphicImpl area)
	{
		// should be a url to a gradient
		String url = (String)sp;
		if(url.startsWith("url(")) {
			String address;
			int b1 = url.indexOf("(");
			int b2 = url.indexOf(")");
			address = url.substring(b1 + 1, b2);
			address = address.trim();
			// local reference
			if(address.startsWith("#")) {
				// find the gradient element
				GraphicImpl gi = area.locateDef(address.substring(1, address.length()));
//						System.out.println(gi + ":" + address.substring(1, address.length()));
				if(gi instanceof SVGLinearGradientElement) {
					SVGLinearGradientElement linear = (SVGLinearGradientElement)gi;

					Vector theCoords = new Vector();
					theCoords.addElement(new Double(linear.getX1().getValue()));
					theCoords.addElement(new Double(linear.getY1().getValue()));
					theCoords.addElement(new Double(linear.getX2().getValue()));
					theCoords.addElement(new Double(linear.getY2().getValue()));

					Vector theExtend = new Vector();
					theExtend.addElement(new Boolean(true));
					theExtend.addElement(new Boolean(true));

					Vector theDomain = new Vector();
					theDomain.addElement(new Double(0));
					theDomain.addElement(new Double(1));

					Vector theEncode = new Vector();
					theEncode.addElement(new Double(0));
					theEncode.addElement(new Double(1));
					theEncode.addElement(new Double(0));
					theEncode.addElement(new Double(1));

					Vector theBounds = new Vector();
					theBounds.addElement(new Double(0));
					theBounds.addElement(new Double(1));

					Vector theFunctions = new Vector();

					org.w3c.dom.NodeList nl = linear.getChildNodes();
					Vector someColors = new Vector();
					float lastoffset = 0;
					Vector lastVector = null;
					SVGStopElementImpl stop;
					Hashtable table;
					for(int count = 0; count < nl.getLength(); count++) {
						stop = (SVGStopElementImpl)nl.item(count);
						table = stop.getStyle();
						ColorType sc = (ColorType)table.get("stop-color");
						if(sc == null) {
							// maybe using color
							sc = (ColorType)table.get("color");
						}
						if(sc == null) {
							// problems
							System.err.println("no stop-color or color in stop element");
							continue;
						}
						float offset = stop.getOffset();
						PDFColor color = new PDFColor(sc.red(), sc.green(), sc.blue());
						Vector colVector = color.getVector();
						// create bounds from last to offset
						if(lastVector != null) {
							Vector theCzero = lastVector;
							Vector theCone = colVector;
							PDFFunction myfunc = this.pdfDoc.makeFunction(2, theDomain, null,
													theCzero, theCone, 1.0);
							theFunctions.addElement(myfunc);
						}
						lastoffset = offset;
						lastVector = colVector;
						someColors.addElement(color);
					}
					ColorSpace aColorSpace = new ColorSpace(ColorSpace.DEVICE_RGB);
/*							PDFFunction myfunky = this.pdfDoc.makeFunction(3,
						theDomain, null,
						theFunctions, null,
						theEncode);
					PDFShading myShad = null;
					myShad = this.pdfDoc.makeShading(
						2, aColorSpace,
						null, null, false,
						theCoords, null, myfunky,theExtend);
					PDFPattern myPat = this.pdfDoc.makePattern(2, myShad, null, null, null);*/
					PDFPattern myPat = this.pdfDoc.createGradient(false, aColorSpace, someColors,null,theCoords);
					currentStream.add(myPat.getColorSpaceOut(fill));
				} else if(gi instanceof SVGRadialGradientElement) {
					SVGRadialGradientElement radial = (SVGRadialGradientElement)gi;
					ColorSpace aColorSpace = new ColorSpace(ColorSpace.DEVICE_RGB);
					org.w3c.dom.NodeList nl = radial.getChildNodes();
					SVGStopElementImpl stop;
					Hashtable table;
					Vector someColors = new Vector();
					Vector theCoords = new Vector();
					Vector theBounds = new Vector();
					// todo handle gradient units
					SVGRect bbox = null;
					if(area instanceof SVGRectElement) {
						bbox = ((SVGRectElement)area).getBBox();
					}
					short units = radial.getGradientUnits();
					switch(units) {
						case SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX:
						break;
						case SVGUnitTypes.SVG_UNIT_TYPE_UNKNOWN:
						default:
					}
					// check value types, eg. %
					if(bbox != null) {
						theCoords.addElement(new Double(bbox.getX().getValue() +
							radial.getCx().getValue() * bbox.getWidth().getValue()));
						theCoords.addElement(new Double(bbox.getY().getValue() +
							radial.getCy().getValue() * bbox.getHeight().getValue()));
						theCoords.addElement(new Double(radial.getR().getValue() *
							bbox.getHeight().getValue()));
						theCoords.addElement(new Double(bbox.getX().getValue() +
							radial.getFx().getValue() * bbox.getWidth().getValue()));
						theCoords.addElement(new Double(bbox.getY().getValue() +
							radial.getFy().getValue() * bbox.getHeight().getValue()));
						theCoords.addElement(new Double(radial.getR().getValue()));
					} else {
						theCoords.addElement(new Double(radial.getCx().getValue()));
						theCoords.addElement(new Double(radial.getCy().getValue()));
						theCoords.addElement(new Double(radial.getR().getValue()));
						theCoords.addElement(new Double(radial.getFx().getValue())); // Fx
						theCoords.addElement(new Double(radial.getFy().getValue())); // Fy
						theCoords.addElement(new Double(radial.getR().getValue()));
					}
					float lastoffset = 0;
					for(int count = 0; count < nl.getLength(); count++) {
						stop = (SVGStopElementImpl)nl.item(count);
						table = stop.getStyle();
						ColorType sc = (ColorType)table.get("stop-color");
						if(sc == null) {
							// maybe using color
							sc = (ColorType)table.get("color");
						}
						if(sc == null) {
							// problems
							System.err.println("no stop-color or color in stop element");
							continue;
						}
						float offset = stop.getOffset();
						PDFColor color = new PDFColor(sc.red(), sc.green(), sc.blue());
						// create bounds from last to offset
						lastoffset = offset;
						someColors.addElement(color);
					}
					PDFPattern myPat = this.pdfDoc.createGradient(true, aColorSpace, someColors,null,theCoords);

					currentStream.add(myPat.getColorSpaceOut(fill));
				} else {
					System.err.println("WARNING Invalid fill reference :" + gi + ":" + address);
				}
			}
		}
	}

	/*
	 * This sets up the style for drawing objects.
	 * Should only set style for elements that have changes.
	 *
	 */
	protected boolean applyStyle(GraphicImpl area, Hashtable style)
	{
		boolean fill = false;
		Object sp;
		sp = style.get("fill");
		if(sp != null) {
			if(sp instanceof ColorType) {
				ColorType ct = (ColorType)sp;
				PDFColor fillColour = new PDFColor(ct.red(), ct.green(), ct.blue());
				currentStream.add(fillColour.getColorSpaceOut(true));
			} else if (sp instanceof String) {
				handleGradient((String)sp, true, area);
			}
			fill = true;
		} else {
		}
		sp = style.get("stroke");
		if(sp != null) {
			if(sp instanceof ColorType) {
				ColorType ct = (ColorType)sp;
				PDFColor fillColour = new PDFColor(ct.red(), ct.green(), ct.blue());
				currentStream.add(fillColour.getColorSpaceOut(false));
			} else if(sp instanceof String) {
				handleGradient((String)sp, false, area);
			}
		} else {
		}
		sp = style.get("stroke-width");
		if(sp != null) {
			float width;
			width = ((SVGLengthImpl)sp).getValue();
			PDFNumber pdfNumber = new PDFNumber();
			currentStream.add(pdfNumber.doubleOut(width) + " w\n");
		}
		sp = style.get("stroke-dasharray");
		if(sp != null) {
			Vector list;
			list = (Vector)sp;
			currentStream.add("[ ");
			for(Enumeration e = list.elements(); e.hasMoreElements(); ) {
				Integer val = (Integer)e.nextElement();
				currentStream.add(val.intValue() + " ");
			}
			sp = style.get("stroke-offset");
			if(sp != null) {
				float width;
				width = ((SVGLengthImpl)sp).getValue();
				PDFNumber pdfNumber = new PDFNumber();
				currentStream.add("] " + pdfNumber.doubleOut(width) + " d\n");
			} else {
				currentStream.add("] 0 d\n");
			}

		}
		sp = style.get("mask");
		if(sp != null) {
			String maskurl;
			maskurl = (String)sp;
			maskurl = maskurl.substring(1, maskurl.length());
			// get def of mask and set mask
//			Hashtable defs;
			GraphicImpl graph = null;
			graph = area.locateDef(maskurl);
//			defs = area.getDefs();
//			if(defs != null)
//				graph = (GraphicImpl)defs.get(maskurl);
			if(graph != null) {
				GraphicImpl parent = graph.getGraphicParent();
				graph.setParent(area);
//				renderElement(svgarea, graph, posx, posy);
				graph.setParent(parent);
			}
		}
		return fill;
	}

	protected void applyTransform(Vector trans)
	{
		PDFNumber pdfNumber = new PDFNumber();
		for(Enumeration e = trans.elements(); e.hasMoreElements(); ) {
			SVGTransform t = (SVGTransform)e.nextElement();
			SVGMatrix matrix = t.getMatrix();
			currentStream.add(pdfNumber.doubleOut(matrix.getA()) + " " + pdfNumber.doubleOut(matrix.getB()) + " " + pdfNumber.doubleOut(matrix.getC())
					+ " " + pdfNumber.doubleOut(matrix.getD()) + " " + pdfNumber.doubleOut(matrix.getE().getValue()) + " " + pdfNumber.doubleOut(matrix.getF().getValue()) + " cm\n");
		}
	}

	public void renderElement(SVGArea svgarea, GraphicImpl area, int posx, int posy)
	{
		int x = posx;
		int y = posy;
		Hashtable style = area.getStyle();
		boolean fill = false;

		currentStream.add("q\n");
		Vector trans = area.oldgetTransform();
		if(trans != null) {
			applyTransform(trans);
		}

		if(style != null) {
			fill = applyStyle(area, style);
		}

		if (area instanceof SVGRectElement) {
			SVGRectElement rg = (SVGRectElement)area;
			float rx = rg.getX().getValue();
			float ry = rg.getY().getValue();
			float rw = rg.getWidth().getValue();
			float rh = rg.getHeight().getValue();
			addRect(rx, ry, rw, rh, fill);
		} else if (area instanceof SVGLineElement) {
			SVGLineElement lg = (SVGLineElement)area;
			float x1 = lg.getX1().getValue();
			float y1 = lg.getY1().getValue();
			float x2 = lg.getX2().getValue();
			float y2 = lg.getY2().getValue();
			addLine(x1,y1,x2,y2, fill);
		} else if (area instanceof SVGTextElementImpl) {
//			currentStream.add("q\n");
//			currentStream.add(1 + " " + 0 + " " + 0 + " " + 1 + " " + 0 + " " + 0 + " cm\n");
			currentStream.add("BT\n");
			renderText(svgarea, (SVGTextElementImpl)area, 0, 0);
			currentStream.add("ET\n");
//			currentStream.add("Q\n");
		} else if (area instanceof SVGCircleElement) {
			SVGCircleElement cg = (SVGCircleElement)area;
			float cx = cg.getCx().getValue();
			float cy = cg.getCy().getValue();
			float r = cg.getR().getValue();
			addCircle(cx,cy,r, fill);
		} else if (area instanceof SVGEllipseElement) {
			SVGEllipseElement cg = (SVGEllipseElement)area;
			float cx = cg.getCx().getValue();
			float cy = cg.getCy().getValue();
			float rx = cg.getRx().getValue();
			float ry = cg.getRy().getValue();
			addEllipse(cx,cy,rx,ry, fill);
		} else if (area instanceof SVGPathElementImpl) {
			addPath(((SVGPathElementImpl)area).pathElements, posx, posy, fill);
		} else if (area instanceof SVGPolylineElementImpl) {
			addPolyline(((SVGPolylineElementImpl)area).points, posx, posy, fill, false);
		} else if (area instanceof SVGPolygonElementImpl) {
			addPolyline(((SVGPolygonElementImpl)area).points, posx, posy, fill, true);
		} else if (area instanceof SVGGElementImpl) {
			renderGArea(svgarea, (SVGGElementImpl)area, x, y);
		} else if(area instanceof SVGUseElementImpl) {
			SVGUseElementImpl ug = (SVGUseElementImpl)area;
			String ref = ug.link;
			ref = ref.substring(1, ref.length());
//			Hashtable defs;
			GraphicImpl graph = null;
			graph = area.locateDef(ref);
//			defs = ug.getDefs();
//			if(defs != null)
//				graph = (GraphicImpl)defs.get(ref);
//			if(graph == null) {
				// find element inside parent.
//			}
			if(graph != null) {
				// probably not the best way to do this, should be able
				// to render without the style being set.
				GraphicImpl parent = graph.getGraphicParent();
				graph.setParent(area);
				// need to clip (if necessary) to the use area
				// the style of the linked element is as if is was
				// a direct descendant of the use element.
				renderElement(svgarea, graph, posx, posy);
				graph.setParent(parent);
			}
		} else if (area instanceof SVGImageElementImpl) {
			SVGImageElementImpl ig = (SVGImageElementImpl)area;
			renderImage(ig.link, ig.x, ig.y, ig.width, ig.height);
		} else if (area instanceof SVGArea) {
			// the x and y pos will be wrong!
			Enumeration e = ((SVGArea)area).getChildren().elements();
			currentStream.add("q\n");
			while (e.hasMoreElements()) {
				Object o = e.nextElement();
				if(o instanceof GraphicImpl) {
					renderElement((SVGArea)area, (GraphicImpl)o, x, y);
				}
			}
			currentStream.add("Q\n");
		}
		// should be done with some cleanup code, so only
		// required values are reset.
		currentStream.add("Q\n");
	}

// need to escape certain chars
	public void renderText(SVGArea svgarea, SVGTextElementImpl tg, float x, float y)
	{
		FontState fontState = svgarea.getFontState();
		PDFNumber pdfNumber = new PDFNumber();

		Hashtable styles;
		styles = tg.getStyle();
		applyStyle(tg, styles);
		// apply transform
		// text has a Tm and need to handle each element
		SVGTransformList trans = tg.getTransform();
		SVGMatrix matrix = trans.consolidate().getMatrix();
		String transstr = (pdfNumber.doubleOut(matrix.getA())
							+ " " + pdfNumber.doubleOut(matrix.getB())
							+ " " + pdfNumber.doubleOut(matrix.getC())
							+ " " + pdfNumber.doubleOut(-matrix.getD()) + " ");

		String fontFamily;
		fontFamily = (String)styles.get("font-family");
		if(fontFamily == null)
			fontFamily = fontState.getFontFamily();
		String fontStyle;
		fontStyle = (String)styles.get("font-style");
		if(fontStyle == null)
			fontStyle = fontState.getFontStyle();
		String fontWeight;
		fontWeight = (String)styles.get("font-weight");
		if(fontWeight == null)
			fontWeight = fontState.getFontWeight();
		SVGLength len;
		len = (SVGLength)styles.get("font-size");
		float fontSize;
		fontSize = fontState.getFontSize() / 1000f;
		if(len != null)
			fontSize = len.getValue();
		FontState fs = fontState;
		try {
			fs = new FontState(fontState.getFontInfo(), fontFamily, fontStyle,
									fontWeight, (int)(fontSize * 1000));
		} catch(Exception fope) {
//			fope.printStackTrace();
		}

		currentStream.add("/" + fs.getFontName() + " " + fontSize + " Tf\n");

		float tx = tg.x;
		float ty = tg.y;
		float currentX = x + tx;
		Vector list = tg.textList;
		for(Enumeration e = list.elements(); e.hasMoreElements(); ) {
			Object o = e.nextElement();
			if(o instanceof String) {
				String str = (String)o;
				currentStream.add(transstr
					+ (currentX + matrix.getE().getValue()) + " "
					+ (y+ty + matrix.getF().getValue()) + " Tm " 
					+ "(" + str + ") Tj\n");
				for(int count = 0; count < str.length(); count++) {
					currentX += fs.width(str.charAt(count)) / 1000f;
				}
				currentX += fs.width(' ') / 1000f;
			} else if(o instanceof SVGTextPathElementImpl) {
				SVGTextPathElementImpl tpg = (SVGTextPathElementImpl)o;
				String ref = tpg.str;
//				Hashtable defs;
				GraphicImpl graph = null;
				graph = tpg.locateDef(ref);
//				defs = tpg.getDefs();
//				if(defs != null)
//					graph = (GraphicImpl)defs.get(ref);
				if(graph != null && graph instanceof SVGPathElementImpl) {
					// probably not the best way to do this, should be able
					// to render without the style being set.
					GraphicImpl parent = graph.getGraphicParent();
					graph.setParent(tpg);
					// set text path??
					// how should this work
					graph.setParent(parent);
				}
			} else if(o instanceof SVGTRefElementImpl) {
				SVGTRefElementImpl trg = (SVGTRefElementImpl)o;
				String ref = trg.ref;
				ref = ref.substring(1, ref.length());
//				Hashtable defs;
				GraphicImpl graph = null;
				graph = trg.locateDef(ref);
//				defs = trg.getDefs();
//				if(defs != null)
//					graph = (GraphicImpl)defs.get(ref);
				if(graph != null && graph instanceof SVGTextElementImpl) {
					GraphicImpl parent = graph.getGraphicParent();
					graph.setParent(trg);
					SVGTextElementImpl te = (SVGTextElementImpl)graph;
					renderText(svgarea, te, (int)(x + tx), (int)(y + ty));
					graph.setParent(parent);
				}
			} else if(o instanceof SVGTSpanElementImpl) {
				// TODO handle dy properly
				SVGTSpanElementImpl tsg = (SVGTSpanElementImpl)o;
				styles = tsg.getStyle();
				applyStyle(tsg, styles);
				boolean changed = false;

				String newprop;
				newprop = (String)styles.get("font-family");
				if(newprop != null && !newprop.equals(fontFamily)) {
					fontFamily = newprop;
					changed = true;
				}
				newprop = (String)styles.get("font-style");
				if(newprop != null && !newprop.equals(fontStyle)) {
					fontStyle = newprop;
					changed = true;
				}
				newprop = (String)styles.get("font-weight");
				if(newprop != null && !newprop.equals(fontWeight)) {
					fontWeight = newprop;
					changed = true;
				}
				len = (SVGLengthImpl)styles.get("font-size");
				if(len != null) {
					float newSize = len.getValue();
					if(fontSize != newSize) {
						fontSize = newSize;
						changed = true;
					}
				}
				if(changed) {
					try {
						fs = new FontState(fontState.getFontInfo(), fontFamily, fontStyle,
											fontWeight, (int)(fontSize * 1000));
					} catch(Exception fope) {
					}

					currentStream.add("/" + fs.getFontName() + " " + fontSize + " Tf\n");
				}

				if(tsg.ylist != null) {
					ty = ((Float)tsg.ylist.elementAt(0)).floatValue();
				}
				if(tsg.xlist != null) {
					Enumeration enum = tsg.xlist.elements();
					int count = 0;
					while(enum.hasMoreElements() && count < tsg.str.length()) {
						float pos = ((Float)enum.nextElement()).floatValue();
						currentStream.add(transstr
							+ (x + pos + matrix.getE().getValue()) + " "
							+ (y + ty + tsg.dy + matrix.getF().getValue()) + " Tm " 
							+ "(" + tsg.str.charAt(count) + ") Tj\n");
						currentX = x + pos + fs.width(tsg.str.charAt(count)) / 1000f;
						count++;
					}
					if(enum.hasMoreElements()) {
						// do nothing
					} else if(count < tsg.str.length()) {
						currentStream.add(transstr
							+ (currentX + matrix.getE().getValue()) + " "
							+ (y + ty + tsg.dy + matrix.getF().getValue()) + " Tm " 
							+ "(" + tsg.str.substring(count, tsg.str.length()) + ") Tj\n");
					}
				} else if(tsg.dxlist != null) {
					Enumeration enum = tsg.dxlist.elements();
					int count = 0;
					while(enum.hasMoreElements() && count < tsg.str.length()) {
						float pos = ((Float)enum.nextElement()).floatValue();
						currentStream.add(transstr
							+ (currentX + pos + matrix.getE().getValue()) + " "
							+ (y + ty + tsg.dy + matrix.getF().getValue()) + " Tm " 
							+ "(" + tsg.str.charAt(count) + ") Tj\n");
						currentX += pos + fs.width(tsg.str.charAt(count)) / 1000f;
						count++;
					}
					if(enum.hasMoreElements()) {
						// do nothing
					} else if(count < tsg.str.length()) {
						currentStream.add(transstr
							+ (currentX + matrix.getE().getValue()) + " "
							+ (y + ty + tsg.dy + matrix.getF().getValue()) + " Tm " 
							+ "(" + tsg.str.substring(count, tsg.str.length()) + ") Tj\n");
					}
				} else {
/*					currentStream.add(transstr
						+ (((tsg.x == 0) ? (currentX) : (x))/1000f + tsg.x) + " "
						+ (y / 1000f - ((tsg.y == 0) ? (ty + tsg.dy) : tsg.y)) + " Tm " 
						+ "(" + tsg.str + ") Tj\n");
					currentX = (tsg.x == 0) ? (currentX + tsg.dx) : (x + tsg.x);
					for(int count = 0; count < tsg.str.length(); count++) {
						currentX += fs.width(tsg.str.charAt(count));
					}*/
				}
				currentX += fs.width(' ') / 1000f;
			} else {
				System.err.println("Error: unknown text element " + o);
			}
		}
	}

	public void renderGArea(SVGArea svgarea, SVGGElementImpl area, int posx, int posy)
	{
		Enumeration e = area.getChildren().elements();
		while (e.hasMoreElements()) {
			Object o = e.nextElement();
			if(o instanceof GraphicImpl) {
				renderElement(svgarea, (GraphicImpl)o, posx, posy);
			}
		}
	}

    /**
     * render inline area to PDF
     *
     * @param area inline area to render
     */
    public void renderInlineArea(InlineArea area) {
	char ch;
	StringBuffer pdf = new StringBuffer();
		
	String name = area.getFontState().getFontName();
	int size = area.getFontState().getFontSize();
	
	PDFColor theAreaColor = new PDFColor(
				(double)area.getRed(),
				(double)area.getGreen(),
				(double)area.getBlue()  );
		
	if ((!name.equals(this.currentFontName))
		|| (size != this.currentFontSize)) {
		this.currentFontName = name;
		this.currentFontSize = size;
		pdf = pdf.append("/" + name + " " + (size/1000) + " Tf\n");
	}

	//if (theAreaColor.isEquivalent(this.currentFill)) {
		this.currentFill = theAreaColor;
		
		pdf = pdf.append(this.currentFill.getColorSpaceOut(true));
	//}
		
	int rx = this.currentXPosition;
	int bl = this.currentYPosition;

	pdf = pdf.append("1 0 0 1 "
			 +(rx/1000f) + " " + (bl/1000f)
			 + " Tm (");

	String s;
	if ( area.getPageNumberID()!=null ) { // this text is a page number, so resolve it
            s = idReferences.getPageNumber(area.getPageNumberID());            
        }
        else {
            s = area.getText();
        }
	
	int l = s.length();

	for (int i=0; i < l; i++) {
		ch = s.charAt(i);
		if (ch > 127) {
		pdf = pdf.append("\\");
		pdf = pdf.append(Integer.toOctalString((int)ch));
		} else {
		switch (ch) {
		case '('  : pdf = pdf.append("\\("); break;
		case ')'  : pdf = pdf.append("\\)"); break;
		case '\\' : pdf = pdf.append("\\\\"); break;
		default   : pdf = pdf.append(ch); break;
		}
		}
	}
	pdf = pdf.append(") Tj\n");

	currentStream.add(pdf.toString());

	this.currentXPosition += area.getContentWidth();
	}

	/**
	 * render inline space to PDF
	 *
	 * @param space space to render
	 */
	public void renderInlineSpace(InlineSpace space) {
	this.currentXPosition += space.getSize();
	}

	/**
	 * render line area to PDF
	 *
	 * @param area area to render
	 */
	public void renderLineArea(LineArea area) {
	int rx = this.currentAreaContainerXPosition
		+ area.getStartIndent();
	int ry = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();

	this.currentYPosition -= area.getPlacementOffset();
	this.currentXPosition = rx;

	int bl = this.currentYPosition;

	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
		Box b = (Box) e.nextElement();
		b.render(this);
	}

	this.currentYPosition = ry-h;
	}

	/**
	 * render page into PDF
	 *
	 * @param page page to render
	 */
	public void renderPage(Page page) {
	AreaContainer body, before, after;
	  
	currentStream = this.pdfDoc.makeStream();
	body = page.getBody();
	before = page.getBefore();
	after = page.getAfter();

	this.currentFontName = "";
	this.currentFontSize = 0;

	currentStream.add("BT\n");
	renderAreaContainer(body);

	if (before != null) {
		renderAreaContainer(before);
	}

	if (after != null) {
		renderAreaContainer(after);
	}
	
	currentStream.add("ET\n");

	currentPage = this.pdfDoc.makePage(this.pdfResources, currentStream,
				 page.getWidth()/1000,
				 page.getHeight()/1000, page);

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
			LinkedRectangle lrect = (LinkedRectangle) f.nextElement();
			currentAnnotList.addLink(
				this.pdfDoc.makeLink(lrect.getRectangle(), dest, linkType));
		}
		}
	} else {
		// just to be on the safe side
		currentAnnotList = null;
	}
	}

	/**
	 * render rule area into PDF
	 *
	 * @param area area to render
	 */
	public void renderRuleArea(RuleArea area) {
	int rx = this.currentAreaContainerXPosition
		+ area.getStartIndent();
	int ry = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();
	int th = area.getRuleThickness();
	
	addLine(rx, ry, rx+w, ry, th, new PDFColor(area.getRed(), area.getGreen(),area.getBlue()));
	}

	/**
	 * set up the font info
	 *
	 * @param fontInfo font info to set up
	 */
	public void setupFontInfo(FontInfo fontInfo) {
	FontSetup.setup(fontInfo);
	FontSetup.addToResources(this.pdfDoc, fontInfo);
	}
}
