/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.fop.svg;

import org.apache.fop.pdf.*;
import org.apache.fop.layout.*;
import org.apache.fop.fonts.*;
import org.apache.fop.render.pdf.*;
import org.apache.fop.image.*;
import org.apache.fop.datatypes.ColorSpace;

import org.apache.batik.ext.awt.g2d.*;

import java.text.AttributedCharacterIterator;
import java.awt.*;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.renderable.*;
import java.io.*;

import java.util.Map;

/**
 * This concrete implementation of <tt>AbstractGraphics2D</tt> is a 
 * simple help to programmers to get started with their own 
 * implementation of <tt>Graphics2D</tt>. 
 * <tt>DefaultGraphics2D</tt> implements all the abstract methods
 * is <tt>AbstractGraphics2D</tt> and makes it easy to start 
 * implementing a <tt>Graphic2D</tt> piece-meal.
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id$
 * @see org.apache.batik.ext.awt.g2d.AbstractGraphics2D
 */
public class PDFDocumentGraphics2D extends PDFGraphics2D {
	OutputStream stream;

	PDFStream pdfStream;
	int width;
	int height;

    /**
     * Create a new PDFGraphics2D with the given pdf document info.
     * This is used to create a Graphics object for use inside an already
     * existing document.
     * Maybe this could be handled as a subclass (PDFDocumentGraphics2d)
     */
    public PDFDocumentGraphics2D(boolean textAsShapes, OutputStream stream, int width, int height)
	{
        super(textAsShapes);
        standalone = true;
		this.stream = stream;
        this.pdfDoc = new PDFDocument();
        this.pdfDoc.setProducer("FOP SVG Renderer");
        pdfStream = this.pdfDoc.makeStream();
		this.width = width;
		this.height = height;

				currentFontName = "";
				currentFontSize = 0;
				currentYPosition = 0;
				currentXPosition = 0;
//				fontState = fs;

        currentStream.write("1 0 0 -1 0 " + height + " cm\n");

        // end part
        /*
        FontSetup.addToResources(this.pdfDoc, fontInfo);
		*/

    }

    public void finish() throws IOException
	{
        pdfStream.add(getString());
        PDFResources pdfResources = this.pdfDoc.getResources();
        PDFPage currentPage = this.pdfDoc.makePage(pdfResources, pdfStream,
                                           width,
					   height, null);
        this.pdfDoc.output(stream);

	}

		public String getString() {
				return currentStream.toString();
		}

    public void setGraphicContext(GraphicContext c)
    {
        gc = c;
    }

    /**
     * This constructor supports the create method
     */
    public PDFDocumentGraphics2D(PDFDocumentGraphics2D g){
        super(g);
    }

    /**
     * Creates a new <code>Graphics</code> object that is
     * a copy of this <code>Graphics</code> object.
     * @return     a new graphics context that is a copy of
     *             this graphics context.
     */
    public Graphics create(){
        return new PDFDocumentGraphics2D(this);
    }

}
