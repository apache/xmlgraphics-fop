/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class representing a /GoTo object.
 *
 */
public class PDFGoTo extends PDFAction {

    /**
     * the pageReference
     */
    protected String pageReference;
    protected String destination = null;
    protected float xPosition = 0, yPosition = 0;

    /**
     * create a /GoTo object.
     *
     * @param number the object's number
     * @param pageReference the pageReference represented by this object
     */
    public PDFGoTo(int number, String pageReference) {

        /* generic creation of object */
        super(number);

        this.pageReference = pageReference;
    }


    /**
     * Sets page reference after object has been created
     *
     * @param pageReference
     * the new page reference to use
     */
    public void setPageReference(String pageReference) {
        this.pageReference = pageReference;
    }



    /**
     * Sets the Y position to jump to
     *
     * @param yPosition y position
     */
    public void setYPosition(float yPosition) {
        this.yPosition = yPosition;
    }

    public void setDestination(String dest) {
        destination = dest;
    }

    /**
     * Sets the x Position to jump to
     *
     * @param xPosition x position
     */
    public void setXPosition(int xPosition) {
        this.xPosition = (xPosition / 1000f);
    }

    public String getAction() {
        return referencePDF();
    }


    /**
     * represent the object in PDF
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        String dest;
        if(destination == null) {
            dest = "/D [" + this.pageReference + " /XYZ " + xPosition
                          + " " + yPosition + " null]\n";
        } else {
            dest = "/D [" + this.pageReference + " " + destination + "]\n";
        }
        String p = new String(this.number + " " + this.generation
                              + " obj\n<<\n/S /GoTo\n" + dest
                              + ">>\nendobj\n");
        return p.getBytes();
    }

    /*
     * example
     * 29 0 obj
     * <<
     * /S /GoTo
     * /D [23 0 R /FitH 600]
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

        if (obj == null || !(obj instanceof PDFGoTo)) {
            return false;
        }

        PDFGoTo gt = (PDFGoTo)obj;

        if (gt.pageReference == null) {
            if (pageReference != null) {
                return false;
            }
        } else {
            if (!gt.pageReference.equals(pageReference)) {
                return false;
            }
        }

        if(destination == null) {
            if (!(gt.destination == null && gt.xPosition == xPosition
                && gt.yPosition == yPosition)) {
                return false;
            }
        } else {
            if (!destination.equals(gt.destination)) {
                return false;
            }
        }

        return true;
    }
}

