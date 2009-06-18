/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * class representing an /Encoding object.
 *
 * A small object expressing the base encoding name and
 * the differences from the base encoding.
 *
 * The three base encodings are given by their name.
 *
 * Encodings are specified on page 213 and onwards of the PDF 1.3 spec.
 */
public class PDFEncoding extends PDFObject {

    /**
     * the name for the standard encoding scheme
     */
    public static final String MacRomanEncoding = "MacRomanEncoding";

    /**
     * the name for the standard encoding scheme
     */
    public static final String MacExpertEncoding = "MacExpertEncoding";

    /**
     * the name for the standard encoding scheme
     */
    public static final String WinAnsiEncoding = "WinAnsiEncoding";

    /**
     * the name for the base encoding.
     * One of the three base encoding scheme names or
     * the default font's base encoding if null.
     */
    protected String basename;

    /**
     * the differences from the base encoding
     */
    protected HashMap differences;

    /**
     * create the /Encoding object
     *
     * @param number the object's number
     * @param basename the name of the character encoding schema
     */
    public PDFEncoding(int number, String basename) {

        /* generic creation of PDF object */
        super(number);

        /* set fields using paramaters */
        this.basename = basename;
        this.differences = new HashMap();
    }

    /**
     * add differences to the encoding
     *
     * @param code the first index of the sequence to be changed
     * @param sequence the sequence of glyph names (as String)
     */
    public void addDifferences(int code, ArrayList sequence) {
        differences.put(new Integer(code), sequence);
    }

    /**
     * produce the PDF representation for the object
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        StringBuffer p = new StringBuffer();
        p.append(this.number + " " + this.generation
                 + " obj\n<< /Type /Encoding");
        if ((basename != null) && (!basename.equals(""))) {
            p.append("\n/BaseEncoding /" + this.basename);
        }
        if (!differences.isEmpty()) {
            p.append("\n/Differences [ ");
            Object code;
            Iterator codes = differences.keySet().iterator();
            while (codes.hasNext()) {
                code = codes.next();
                p.append(" ");
                p.append(code);
                ArrayList sequence = (ArrayList)differences.get(code);
                for (int i = 0; i < sequence.size(); i++) {
                    p.append(" /");
                    p.append((String)sequence.get(i));
                }
            }
            p.append(" ]");
        }
        p.append(" >>\nendobj\n");
        return p.toString().getBytes();
    }

    /*
     * example (p. 214)
     * 25 0 obj
     * <<
     * /Type /Encoding
     * /Differences [39 /quotesingle 96 /grave 128
     * /Adieresis /Aring /Ccedilla /Eacute /Ntilde
     * /Odieresis /Udieresis /aacute /agrave]
     * >>
     * endobj
     */
}
