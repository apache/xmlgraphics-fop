/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.pdf;

// Java
import java.io.UnsupportedEncodingException;
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

        try {
            return p.toString().getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            return p.toString().getBytes();
        }       
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
