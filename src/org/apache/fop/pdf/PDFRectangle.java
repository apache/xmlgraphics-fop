/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class representing a rectangle
 *
 * Rectangles are specified on page 183 of the PDF 1.3 spec.
 */
public class PDFRectangle {

    /**
     * lower left x coordinate
     */
    protected int llx;

    /**
     * lower left y coordinate
     */
    protected int lly;

    /**
     * upper right x coordinate
     */
    protected int urx;

    /**
     * upper right y coordinate
     */
    protected int ury;

    /**
     * create a rectangle giving the four separate values
     *
     * @param llx  lower left x coordinate
     * @param lly  lower left y coordinate
     * @param urx  upper right x coordinate
     * @param ury  upper right y coordinate
     */
    public PDFRectangle(int llx, int lly, int urx, int ury) {
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
    }

    /**
     * create a rectangle giving an array of four values
     *
     * @param array values in the order llx, lly, urx, ury
     */
    public PDFRectangle(int[] array) {
        this.llx = array[0];
        this.lly = array[1];
        this.urx = array[2];
        this.ury = array[3];
    }

    /**
     * produce the PDF representation for the object
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        return toPDFString().getBytes();
    }

    public String toPDFString() {
        return new String(" [" + llx + " " + lly + " " + urx + " " + ury
                          + "] ");
    }

}
