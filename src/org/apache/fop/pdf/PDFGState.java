/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class representing a /ExtGState object.
 *
 */
public class PDFGState extends PDFObject {
    float alphaFill = 1;
    float alphaStroke = 1;

    /**
     * create a /ExtGState object.
     *
     * @param number the object's number
     * @param pageReference the pageReference represented by this object
     */
    public PDFGState(int number) {

        /* generic creation of object */
        super(number);

    }

    public String getName() {
        return "GS" + this.number;
    }

    public void setAlpha(float val, boolean fill) {
        if(fill) {
            alphaFill = val;
        } else {
            alphaStroke = val;
        }
    }

    /**
     * represent the object in PDF
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        StringBuffer sb = new StringBuffer(this.number + " " + this.generation
                              + " obj\n<<\n/Type /ExtGState\n");
        if(alphaFill != 1) {
            sb.append("/ca " + alphaFill + "\n");
        }
        if(alphaStroke != 1) {
            sb.append("/CA " + alphaStroke + "\n");
        }
        sb.append(">>\nendobj\n");
        return sb.toString().getBytes();
    }

    /*
     * example
     * 29 0 obj
     * <<
     * /Type /ExtGState
     * /ca 0.5
     * >>
     * endobj
     */
}
