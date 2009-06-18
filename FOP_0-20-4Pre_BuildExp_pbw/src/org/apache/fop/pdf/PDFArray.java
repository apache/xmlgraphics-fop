/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class representing an array object
 */
public class PDFArray extends PDFObject {

    protected int[] values;

    /**
     * create the array object
     *
     * @param number the object's number
     * @param values the actual array wrapped by this object
     */
    public PDFArray(int number, int[] values) {

        /* generic creation of PDF object */
        super(number);

        /* set fields using paramaters */
        this.values = values;
    }

    /**
     * produce the PDF representation for the object
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        StringBuffer p = new StringBuffer();
        p.append(this.number + " " + this.generation + " obj\n[");
        for (int i = 0; i < values.length; i++) {
            p.append(" ");
            p.append(values[i]);
        }
        p.append("]\nendobj\n");
        return p.toString().getBytes();
    }

}
