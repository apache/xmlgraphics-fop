/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class representing a /GoToR object.
 */
public class PDFGoToRemote extends PDFAction {

    /**
     * the file specification
     */
    protected PDFFileSpec pdfFileSpec;

    /**
     * create an GoToR object.
     *
     * @param number the object's number
     * @param fileSpec the fileSpec associated with the action
     */
    public PDFGoToRemote(int number, PDFFileSpec pdfFileSpec) {

        /* generic creation of object */
        super(number);

        this.pdfFileSpec = pdfFileSpec;
    }

    /**
     * return the action string which will reference this object
     *
     * @return the action String
     */
    public String getAction() {
        return this.referencePDF();
    }

    /**
     * represent the object in PDF
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        String p = new String(this.number + " " + this.generation + " obj\n"
                              + "<<\n/S /GoToR\n" + "/F "
                              + pdfFileSpec.referencePDF() + "\n"
                              + "/D [ 0 /XYZ null null null ]"
                              + " \n>>\nendobj\n");
        return p.getBytes();
    }


    /*
     * example
     * 28 0 obj
     * <<
     * /S /GoToR
     * /F 29 0 R
     * /D [ 0 /XYZ -6 797 null ]
     * >>
     * endobj
     */
}
