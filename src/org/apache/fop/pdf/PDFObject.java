/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java
import java.io.IOException;
import java.io.OutputStream;

/**
 * generic PDF object.
 *
 * A PDF Document is essentially a collection of these objects. A PDF
 * Object has a number and a generation (although the generation will always
 * be 0 in new documents).
 */
public abstract class PDFObject {

    /**
     * the object's number
     */
    protected int number;

    /**
     * the object's generation (0 in new documents)
     */
    protected int generation = 0;

    /**
     * create an empty object
     *
     * @param number the object's number
     */
    public PDFObject(int number) {
        this.number = number;
    }

    public PDFObject() {
        // do nothing
    }

    /**
     * @return the PDF Object number
     */
    public int getNumber() {
        return this.number;
    }

    /**
     * write the PDF represention of this object
     *
     * @param stream the stream to write the PDF to
     * @return the number of bytes written
     */
    protected int output(OutputStream stream) throws IOException {
        byte[] pdf = this.toPDF();
        stream.write(pdf);
        return pdf.length;
    }

    /**
     * the PDF representation of a reference to this object
     *
     * @return the reference string
     */
    public String referencePDF() {
        String p = this.number + " " + this.generation + " R";
        return p;
    }

    /**
     * represent object as PDF
     *
     * @return PDF string
     */
    abstract byte[] toPDF();
}
