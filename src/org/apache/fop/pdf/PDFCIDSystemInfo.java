/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java
import java.io.UnsupportedEncodingException;

// based on work by Takayuki Takeuchi

/**
 * class representing system information for "character identifier" fonts.
 *
 * this small object is used in the CID fonts and in the CMaps.
 */
public class PDFCIDSystemInfo extends PDFObject {
    private static final StringBuffer p = new StringBuffer();
    protected String registry;
    protected String ordering;
    protected int supplement;

    public PDFCIDSystemInfo(String registry, String ordering,
                            int supplement) {
        this.registry = registry;
        this.ordering = ordering;
        this.supplement = supplement;
    }

    /**
     * produce the PDF representation for the object.
     *
     * unlike the other objects, the CIDSystemInfo is written directly inside
     * the referencing object
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        try {
            return toPDFString().getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            return toPDFString().getBytes();
        }       
    }

    public String toPDFString() {
        p.setLength(0);
        p.append("/CIDSystemInfo << /Registry (");
        p.append(registry);
        p.append(")/Ordering (");
        p.append(ordering);
        p.append(")/Supplement ");
        p.append(supplement);
        p.append(" >>");
        return p.toString();
    }

}

