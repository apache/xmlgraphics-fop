/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java
import java.awt.geom.Rectangle2D;

/**
 * class representing an /Annot object of /Subtype /Link
 */
public class PDFLink extends PDFObject {
    /**
     * Used to represent an external link.
     */
    public static final int EXTERNAL = 0;

    /**
     * Used to represent an internal link.
     */
    public static final int INTERNAL = 1;

    private float ulx;
    private float uly;
    private float brx;
    private float bry;
    private String color;
    private PDFAction action;

    /**
     * create objects associated with a link annotation (GoToR)
     *
     * @param number the object's number
     * @param r the rectangle of the link hotspot in absolute coordinates
     */
    public PDFLink(int number, Rectangle2D r) {
        /* generic creation of PDF object */
        super(number);

        this.ulx = (float)r.getX();
        this.uly = (float)r.getY();
        this.brx = (float)(r.getX() + r.getWidth());
        this.bry = (float)(r.getY() + r.getHeight());
        this.color = "0 0 0";    // just for now

    }

    /**
     * Set the pdf action for this link.
     * @param action the pdf action that is activated for this link
     */
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
                   + (ulx) + " " + (uly) + " "
                   + (brx) + " " + (bry) + " ]\n" + "/C [ "
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

    /**
     * Check if this equals another object.
     *
     * @param obj the object to compare
     * @return true if this equals other object
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof PDFLink)) {
            return false;
        }

        PDFLink link = (PDFLink)obj;

        if (!((link.ulx == ulx) && (link.uly == uly)
              && (link.brx == brx) && (link.bry == bry))) {
            return false;
        }

        if (!(link.color.equals(color)
             && link.action.getAction().equals(action.getAction()))) {
            return false;
        }

        return true;
    }
}

