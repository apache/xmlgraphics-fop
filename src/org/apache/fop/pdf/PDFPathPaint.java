/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import org.apache.fop.datatypes.ColorSpace;

public abstract class PDFPathPaint extends PDFObject {

    // protected int colorspace = 0; //default is 0:RGB, not 1:CMYK
    protected ColorSpace colorSpace;

    public PDFPathPaint(int theNumber) {
        super(theNumber);

    }

    public PDFPathPaint() {
        // do nothing
    }

    public String getColorSpaceOut(boolean fillNotStroke) {
        return ("");
    }

    public void setColorSpace(int theColorSpace) {
        this.colorSpace.setColorSpace(theColorSpace);
    }

    public int getColorSpace() {
        return (this.colorSpace.getColorSpace());
    }

}

