/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java
import java.io.UnsupportedEncodingException;

/**
 * class representing a /FileSpec object.
 *
 */
public class PDFFileSpec extends PDFObject {

    /**
     * the filename
     */
    protected String filename;

    /**
     * create a /FileSpec object.
     *
     * @param number the object's number
     * @param filename the filename represented by this object
     */
    public PDFFileSpec(int number, String filename) {

        /* generic creation of object */
        super(number);

        this.filename = filename;
    }

    /**
     * represent the object in PDF
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        String p = new String(this.number + " " + this.generation
                              + " obj\n<<\n/Type /FileSpec\n" + "/F ("
                              + this.filename + ")\n" + ">>\nendobj\n");

        try {
            return p.getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            return p.getBytes();
        }       
    }

    /*
     * example
     * 29 0 obj
     * <<
     * /Type /FileSpec
     * /F (table1.pdf)
     * >>
     * endobj
     */
}
