/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.pdf;

// Java
import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * class representing an /Encoding object.
 *
 * A small object expressing the base encoding name and
 * the differences from the base encoding.
 *
 * The three base encodings are given by their name.
 *
 * Encodings are specified in section 5.5.5 of the PDF 1.4 spec.
 */
public class PDFEncoding extends PDFObject {

    /**
     * the name for the standard encoding scheme
     */
    public static final String MAC_ROMAN_ENCODING = "MacRomanEncoding";

    /**
     * the name for the standard encoding scheme
     */
    public static final String MAC_EXPERT_ENCODING = "MacExpertEncoding";

    /**
     * the name for the standard encoding scheme
     */
    public static final String WIN_ANSI_ENCODING = "WinAnsiEncoding";

    /**
     * the name for the base encoding.
     * One of the three base encoding scheme names or
     * the default font's base encoding if null.
     */
    protected String basename;

    /**
     * the differences from the base encoding
     */
    protected Map differences;

    /**
     * create the /Encoding object
     *
     * @param basename the name of the character encoding schema
     */
    public PDFEncoding(String basename) {

        /* generic creation of PDF object */
        super();

        /* set fields using paramaters */
        this.basename = basename;
        this.differences = new java.util.HashMap();
    }

    /**
     * add differences to the encoding
     *
     * @param code the first index of the sequence to be changed
     * @param sequence the sequence of glyph names (as String)
     */
    public void addDifferences(int code, List sequence) {
        differences.put(new Integer(code), sequence);
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(128);
        p.append(getObjectID() 
            + "<< /Type /Encoding");
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
                List sequence = (List)differences.get(code);
                for (int i = 0; i < sequence.size(); i++) {
                    p.append(" /");
                    p.append((String)sequence.get(i));
                }
            }
            p.append(" ]");
        }
        p.append(" >>\nendobj\n");
        return p.toString();
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
