/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// based on work by Takayuki Takeuchi

/**
 * class representing system information for "character identifier" fonts.
 *
 * this small object is used in the CID fonts and in the CMaps.
 */
public class PDFCIDSystemInfo extends PDFObject {
    private String registry;
    private String ordering;
    private int supplement;

    /**
     * Create a CID system info.
     *
     * @param registry the registry value
     * @param ordering the ordering value
     * @param supplement the supplement value
     */
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
        return toPDFString().getBytes();
    }

    /**
     * Create a string for the CIDSystemInfo dictionary.
     * The entries are placed as an inline dictionary.
     *
     * @return the string for the CIDSystemInfo entry with the inline dictionary
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer();
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

