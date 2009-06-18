/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java
import java.awt.Rectangle;

/**
 * class representing an /Annot object of /Subtype /Link
 */
public class PDFLink extends PDFObject {

    float ulx;
    float uly;
    float brx;
    float bry;
    String color;
    PDFAction action;

    /**
     * create objects associated with a link annotation (GoToR)
     *
     * @param number the object's number
     * @param producer the application producing the PDF
     */
    public PDFLink(int number, Rectangle r) {
        /* generic creation of PDF object */
        super(number);

        this.ulx = r.x;
        this.uly = r.y;
        this.brx = r.x + r.width;
        this.bry = r.y - r.height;
        this.color = "0 0 0";    // just for now

    }

    public void setAction(PDFAction action) {
        this.action = action;
    }

    /**
     * produce the PDF representation of the object
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        String p = this.number + " " + this.generation + " obj\n"
                   + "<< /Type /Annot\n" + "/Subtype /Link\n" + "/Rect [ "
                   + (ulx / 1000f) + " " + (uly / 1000f) + " "
                   + (brx / 1000f) + " " + (bry / 1000f) + " ]\n" + "/C [ "
                   + this.color + " ]\n" + "/Border [ 0 0 0 ]\n" + "/A "
                   + this.action.getAction() + "\n" + "/H /I\n>>\nendobj\n";
        return p.getBytes();
    }

    /*
     * example
     * 19 0 obj
     * <<
     * /Type /Annot
     * /Subtype /Link
     * /Rect [ 176.032 678.48412 228.73579 692.356 ]
     * /C [ 0.86491 0.03421 0.02591 ]
     * /Border [ 0 0 1 ]
     * /A 28 0 R
     * /H /I
     * >>
     * endobj
     */
}
