/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// based on work by Takayuki Takeuchi

/**
 * class representing a font descriptor for CID fonts.
 *
 * Font descriptors for CID fonts are specified on page 227 and onwards of the PDF 1.3 spec.
 */
public class PDFCIDFontDescriptor extends PDFFontDescriptor {

    /**
     * The language for the font
     */
    protected String lang;

    /**
     * The cid set stream
     */
    protected PDFStream cidSet;

    /**
     * create the /FontDescriptor object
     *
     * @param number the object's number
     * @param basefont the base font name
     * @param fontBBox the bounding box for the described font
     * @param flags various characteristics of the font
     * @param capHeight height of the capital letters
     * @param stemV the width of the dominant vertical stems of glyphs
     * @param italicAngle the angle of the vertical dominant strokes
     * @param lang the language
     */
    public PDFCIDFontDescriptor(int number, String basefont, int[] fontBBox,
                                int capHeight, int flags, int italicAngle,
                                int stemV, String lang) {

        super(number, basefont, fontBBox[3], fontBBox[1], capHeight, flags,
              new PDFRectangle(fontBBox), italicAngle, stemV);

        this.lang = lang;
    }

    /**
     * Set the CID set stream.
     * @param cidSet the pdf stream cotnaining the CID set
     */
    public void setCIDSet(PDFStream cidSet) {
        this.cidSet = cidSet;
    }

    /**
     * Fill in the pdf data for this font descriptor.
     * The charset specific dictionary entries are output.
     * @param p the string buffer to append the data
     */
    protected void fillInPDF(StringBuffer p) {
        p.append("\n/MissingWidth 500\n");
        if (lang != null) {
            p.append("\n/Lang /");
            p.append(lang);
        }
        if (cidSet != null) {
            p.append("\n/CIDSet /");
            p.append(this.cidSet.referencePDF());
        }
    }

}
