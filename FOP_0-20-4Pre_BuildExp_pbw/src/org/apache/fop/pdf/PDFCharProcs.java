/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.util.Hashtable;

/**
 * class representing a /CharProcs dictionary for Type3 fonts.
 *
 * <p><b>CAUTION: this is not yet fully implemented!!!!!!!</b>
 * I miss an exemple of <i>how</i> to output this dictionary.
 * </p>
 *
 * Type3 fonts are specified on page 206 and onwards of the PDF 1.3 spec.
 */
public class PDFCharProcs extends PDFObject {

    /**
     * the (character name, drawing stream) pairs for a Type3 font
     */
    protected Hashtable keys;

    public PDFCharProcs() {
        keys = new Hashtable();
    }

    /**
     * add a character definition in the dictionary
     *
     * @param name the character name
     * @param stream the stream that draws the character
     */
    public void addCharacter(String name, PDFStream stream) {
        keys.put(name, stream);
    }

    /**
     * not done yet
     */
    public byte[] toPDF() {
        // TODO: implement this org.apache.fop.pdf.PDFObject abstract method
        return new byte[0];
    }

}
