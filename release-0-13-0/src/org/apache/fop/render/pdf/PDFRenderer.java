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
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.FopImage;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.svg.*;
import org.apache.fop.pdf.*;

// Java
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.awt.Rectangle;
import java.util.Vector;

/**
 * Renderer that renders areas to PDF
 */
public class PDFRenderer implements Renderer {
	
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
	throws IOException {
	System.err.println("rendering areas to PDF");
	this.pdfResources = this.pdfDoc.getResources();
	Enumeration e = areaTree.getPages().elements();
	while (e.hasMoreElements()) {
		this.renderPage((Page) e.nextElement());
	}
	System.err.println("writing out PDF");
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
	 * @param stroke the stroke color/gradient
	 */
	protected void addLine(int x1, int y1, int x2, int y2, int th,
			PDFPathPaint stroke) {
		
	currentStream.add(stroke.getColorSpaceOut(false)
			  + (x1/1000f) + " " + (y1/1000f) + " m "
			  + (x2/1000f) + " " + (y2/1000f) + " l "
			  + (th/1000f) + " w S\n"
			  + "0 0 0 RG\n");
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
			  + "0 0 0 RG\n");
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
			  + "0 0 0 RG 0 0 0 rg\n");
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

	currentStream.add("ET\nq\n" + (img.getWidth()/1000f) + " 0 0 " +
			  (img.getHeight()/1000f) + " " + 
			  ((x + img.getX())/1000f) + " " + 
			  (((y - h) - img.getY())/1000f) + " cm\n" +
			  "/Im" + xObjectNum + " Do\nQ\nBT\n"); 
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
	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
		Object o = e.nextElement();
		if (o instanceof RectGraphic) {
		int rx = ((RectGraphic)o).x;
		int ry = ((RectGraphic)o).y;
		int rw = ((RectGraphic)o).width;
		int rh = ((RectGraphic)o).height;
		addRect(x+rx,y-ry,rw,-rh,new PDFColor(0,0,0));
		} else if (o instanceof LineGraphic) {
		int x1 = ((LineGraphic)o).x1;
		int y1 = ((LineGraphic)o).y1;
		int x2 = ((LineGraphic)o).x2;
		int y2 = ((LineGraphic)o).y2;
		addLine(x+x1,y-y1,x+x2,y-y2,0,new PDFColor(0,0,0));
		} else if (o instanceof TextGraphic) {
		int tx = ((TextGraphic)o).x;
		int ty = ((TextGraphic)o).y;
		String s = ((TextGraphic)o).s;
		currentStream.add("1 0 0 1 "
				  + ((x+tx)/1000f) + " "
				  + ((y-ty)/1000f) + " Tm " 
				  + "(" + s + ") Tj\n");
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

	String s = area.getText();
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
				 page.getHeight()/1000);

	if (page.hasLinks()) {
		currentAnnotList = this.pdfDoc.makeAnnotList();
		currentPage.setAnnotList(currentAnnotList);

		Enumeration e = page.getLinkSets().elements();
		while (e.hasMoreElements()) {
		LinkSet linkSet = (LinkSet) e.nextElement();

		linkSet.align();
		String dest = linkSet.getDest();
		Enumeration f = linkSet.getRects().elements();
		while (f.hasMoreElements()) {
			LinkedRectangle lrect = (LinkedRectangle) f.nextElement();
			currentAnnotList.addLink(
				this.pdfDoc.makeLink(lrect.getRectangle(), dest));
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

