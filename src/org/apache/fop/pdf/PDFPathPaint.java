/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * Base class for PDF painting operations.
 *
 */
public abstract class PDFPathPaint extends PDFObject {

    /**
     * The color space for this paint
     */
    protected PDFColorSpace colorSpace;

    /**
     * Create a path paint with a PDF object number.
     *
     * @param theNumber the PDF object number
     */
    public PDFPathPaint(int theNumber) {
        super(theNumber);
    }

    /**
     * Create an emty path paint.
     */
    public PDFPathPaint() {
        // do nothing
    }

    /**
     * Get the PDF string for setting the path paint.
     *
     * @param fillNotStroke if true fill otherwise stroke
     * @return the PDF instruction string
     */
    public String getColorSpaceOut(boolean fillNotStroke) {
        return ("");
    }

    /**
     * Set the color space for this paint.
     *
     * @param theColorSpace the color space value
     */
    public void setColorSpace(int theColorSpace) {
        this.colorSpace.setColorSpace(theColorSpace);
    }

    /**
     * Get the current color space value for this paint.
     *
     * @return the color space value
     */
    public int getColorSpace() {
        return this.colorSpace.getColorSpace();
    }

}

