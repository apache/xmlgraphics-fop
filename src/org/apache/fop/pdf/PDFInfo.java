/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java
import java.io.IOException;
import java.io.PrintWriter;

/**
 * class representing an /Info object
 */
public class PDFInfo extends PDFObject {

    /**
     * the application producing the PDF
     */
    protected String producer;

    /**
     * create an Info object
     *
     * @param number the object's number
     */
    public PDFInfo(int number) {
        super(number);
    }

    /**
     * set the producer string
     *
     * @param producer the producer string
     */
    public void setProducer(String producer) {
        this.producer = producer;
    }

    /**
     * produce the PDF representation of the object
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        String p = this.number + " " + this.generation
                   + " obj\n<< /Type /Info\n/Producer (" + this.producer
                   + ") >>\nendobj\n";
        return p.getBytes();
    }

}
