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

// Author : Seshadri G




package org.apache.fop.render.mif;

// FOP
import org.apache.fop.render.Renderer;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.FopImage;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.*; 
import org.apache.fop.svg.*; 
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.svg.PathPoint;
import org.apache.fop.mif.*;
import org.apache.fop.layout.*;
import org.apache.fop.image.*;

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
/**
 * Renderer that renders areas to MIF
 */
public class MIFRenderer implements Renderer {

	private String currentFontName;
	private String currentFontSize;
	private int pageHeight;
	private int pageWidth;

	/** the current vertical position in millipoints from bottom */
	protected int currentYPosition = 0;

	/** the current horizontal position in millipoints from left */
	protected int currentXPosition = 0;

	/** the horizontal position of the current area container */
	private int currentAreaContainerXPosition = 0;

	
	/** the MIF Document being created */
	protected MIFDocument mifDoc;


	/* is a table currently open? */
	private boolean inTable=false;



	/**
	 * create the MIF renderer
	 */
	public MIFRenderer() {
	this.mifDoc = new MIFDocument();
	}
	
	
	/**
	 * render the areas into MIF
	 *
	 * @param areaTree the laid-out area tree
	 * @param writer the PrintWriter to write the MIF with
	 */

	public void render(AreaTree areaTree,
                       OutputStream stream) throws IOException, FOPException {
    
            MessageHandler.logln("rendering areas to MIF");
           // idReferences=areaTree.getIDReferences();           
            //this.pdfResources = this.pdfDoc.getResources();            
            //this.pdfDoc.setIDReferences(idReferences);
	      Enumeration e = areaTree.getPages().elements();
              while (e.hasMoreElements()) {
		this.renderPage((Page) e.nextElement());
	      }
    
    
          //  MessageHandler.logln("writing out MIF");

		this.mifDoc.output(stream);
		stream.close();
    }


    
 
    /** set up the given FontInfo */
    public void setupFontInfo(FontInfo fontInfo) {

	FontSetup.setup(fontInfo);
	//FontSetup.addToFontFormat(this.mifDoc, fontInfo);


    } 

    /** set the producer of the rendering */
    public void setProducer(String producer) {} 

   	
    public void renderAreaContainer(AreaContainer area) {

	if (area.foCreator != null && area.foCreator.getName() == "fo:table") {
	
		this.mifDoc.createTable();
		this.inTable=true;
	}
	else 
	if (area.foCreator != null && area.foCreator.getName() == "fo:table-body") {
	
		this.mifDoc.setCurrent("fo:table-body");
	}
	else 
	if (area.foCreator != null && area.foCreator.getName() == "fo:table-column") {
		
		int colWidth=((org.apache.fop.fo.flow.TableColumn) area.foCreator).getColumnWidth();	
		this.mifDoc.setColumnProp(colWidth);
	}
	else 
	if (area.foCreator != null && area.foCreator.getName() == "fo:table-row") {
	
		this.mifDoc.startRow();
	}
	else 
	if (area.foCreator != null && area.foCreator.getName() == "fo:table-cell") {
		
		int rowSpan=((org.apache.fop.fo.flow.TableCell) area.foCreator).getNumRowsSpanned();
		int colSpan=((org.apache.fop.fo.flow.TableCell) area.foCreator).getNumColumnsSpanned();
		this.mifDoc.startCell(rowSpan,colSpan);
	}
	else
	if (inTable) {
	
		inTable=false;
		this.mifDoc.endTable();

	} 
        int saveY = this.currentYPosition;
        int saveX = this.currentAreaContainerXPosition;

        if (area.getPosition() == Position.ABSOLUTE) {
   	     // Y position is computed assuming positive Y axis, adjust for negative postscript one
   		this.currentYPosition =area.getYPosition() - 2 * area.getPaddingTop() - 2 * 						area.borderWidthTop;
        
		this.currentAreaContainerXPosition = area.getXPosition();
        } else if (area.getPosition() == Position.RELATIVE) {
	
	        this.currentYPosition -= area.getYPosition();
        	this.currentAreaContainerXPosition += area.getXPosition();
        
	} else if (area.getPosition() == Position.STATIC) {
        	
		this.currentYPosition -=
        	area.getPaddingTop() + area.borderWidthTop;
        	this.currentAreaContainerXPosition +=area.getPaddingLeft() + 		area.borderWidthLeft;
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

	/*

        // I'm not sure I should have to check for bg being null
        // but I do
        if ((bg != null) && (bg.alpha() == 0)) {
            this.addRect(rx, ry, w, -h, new PDFColor(bg), new PDFColor(bg));
        }

	*/
	/*
	
	// floats & footnotes stuff
	renderAreaContainer(area.getBeforeFloatReferenceArea());
  	renderAreaContainer(area.getFootnoteReferenceArea());
	
	*/
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
	
	/*	
	// I'm not sure I should have to check for bg being null
	// but I do
	if ((bg != null) && (bg.alpha() == 0)) {
		this.addRect(rx, ry, w, -h,
			 new PDFColor(bg),
			 new PDFColor(bg));
	}

	*/
	
	rx = rx - area.borderWidthLeft;
	ry = ry + area.borderWidthTop;
	w = w + area.borderWidthLeft + area.borderWidthRight;
	h = h + area.borderWidthTop + area.borderWidthBottom;

	//Create a textrect with these dimensions.
	//The y co-ordinate is measured +ve downwards so subtract page-height
		
	this.mifDoc.setTextRectProp(rx,pageHeight-ry,w,h);


	/*	

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

	*/
    } 

    public void renderSpanArea(SpanArea area) {

	 	//A span maps to a textframe

		
		this.mifDoc.createTextRect(area.getColumnCount());
				
		Enumeration e = area.getChildren().elements();
		while (e.hasMoreElements()) {
			Box b = (Box) e.nextElement();
			b.render(this);	// column areas
		}				
	}	




   

    /** render the given block area */
    public void renderBlockArea(BlockArea area) {

	this.mifDoc.setBlockProp(area.getStartIndent(),area.getEndIndent());
	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
		Box b = (Box) e.nextElement();
		b.render(this);
	}

    } 

    /** render the given display space */
    public void renderDisplaySpace(DisplaySpace space) {

	int d = space.getSize();
	this.currentYPosition -= d;

    } 

    /** render the given SVG area */
    public void renderSVGArea(SVGArea area) {} 

    /** render a foreign object area */
    public void renderForeignObjectArea(ForeignObjectArea area) {
    } 

	public void renderWordArea(WordArea area) {
	String s;
	s = area.getText(); 
	this.mifDoc.addToStream(s);
	
	this.currentXPosition += area.getContentWidth();
	}
	
    /** render the given image area */
    public void renderImageArea(ImageArea area) {

	int x = this.currentAreaContainerXPosition + area.getXOffset();
        int y = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        this.currentYPosition -= h;

        FopImage img = area.getImage();
        if (img instanceof SVGImage) {
           /* try {
                SVGSVGElement svg =
                  ((SVGImage) img).getSVGDocument().getRootElement();
                currentStream.add("ET\nq\n" + (((float) w) / 1000f) +
                                  " 0 0 " + (((float) h) / 1000f) + " " +
                                  (((float) x) / 1000f) + " " +
                                  (((float)(y - h)) / 1000f) + " cm\n");
                //        renderSVG(svg, (int) x, (int) y);
                currentStream.add("Q\nBT\n");
            } catch (FopImageException e) {
            } */

	MessageHandler.logln("Warning: SVG images not supported in this version");
        } else {
		String url = img.getURL(); 	
		this.mifDoc.addImage(url,x,pageHeight-y,w,h);

          }
    }



    /** render the given inline area */
    public void renderInlineArea(InlineArea area) {} 

    /** render the given inline space */
    public void renderInlineSpace(InlineSpace space) {

	// I dont need the size of space! I just need to 
	// leave a blank space each time
	String s=" ";
	this.mifDoc.addToStream(s); // cool!
	this.currentXPosition += space.getSize();
    } 

    /** render the given line area */
    public void renderLineArea(LineArea area) {


	
	int rx = this.currentAreaContainerXPosition
		+ area.getStartIndent();
	int ry = this.currentYPosition;
	int w = area.getContentWidth();
	int h = area.getHeight();

	this.currentYPosition -= area.getPlacementOffset();
	this.currentXPosition = rx;

	int bl = this.currentYPosition;
          
	//The start of a new linearea corresponds to a new para in FM

	this.mifDoc.startLine();

	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	
		Box b = (Box) e.nextElement();
		this.currentYPosition = ry - area.getPlacementOffset();
		b.render(this); 

	}
	this.currentYPosition = ry-h;
	this.currentXPosition = rx;
	

    } 



    /** render the given page */
    public void renderPage(Page page) {

	AreaContainer before, after;
	BodyAreaContainer body;
	body = page.getBody();
	before = page.getBefore();
	after = page.getAfter();

	this.currentFontName = "";
	this.currentFontSize = "0";

	pageHeight=page.getHeight();
	pageWidth=page.getWidth();
	this.mifDoc.setDocumentHeightWidth(pageHeight,pageWidth);
	
	
	this.mifDoc.createPage();
	
	renderBodyAreaContainer(body);

	
	// If the area is an instance of anything other than body, it goes into the 
	// corresponding master page.
	

	if (before != null) {
		
		this.mifDoc.createTextRect(1); // Create a rect with one col
		renderAreaContainer(before);
	}

	if (after != null) {
		
		this.mifDoc.createTextRect(1); // Create a rect with one col
		renderAreaContainer(after);
	}

    } 

    /** render the given leader area */
    public void renderLeaderArea(LeaderArea area) {}


}
