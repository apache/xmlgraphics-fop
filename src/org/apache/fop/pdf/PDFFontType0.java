/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class representing a Type0 font.
 *
 * Type0 fonts are specified on page 208 and onwards of the PDF 1.3 spec.
 */
public class PDFFontType0 extends PDFFontNonBase14 {

    /**
     * this should be an array of CIDFont but only the first one is used
     */
    protected PDFCIDFont descendantFonts;

    protected PDFCMap cmap;

    /**
     * create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param subtype the font's subtype (PDFFont.TYPE0)
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param mapping the Unicode mapping mechanism
     */
    public PDFFontType0(int number, String fontname, byte subtype,
                        String basefont,
                        Object encoding /* , PDFToUnicode mapping */) {

        /* generic creation of PDF object */
        super(number, fontname, subtype, basefont, encoding /* , mapping */);

        /* set fields using paramaters */
        this.descendantFonts = null;
        cmap = null;
    }

    /**
     * create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param subtype the font's subtype (PDFFont.TYPE0)
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param mapping the Unicode mapping mechanism
     * @param descendantFonts the CIDFont upon which this font is based
     */
    public PDFFontType0(int number, String fontname, byte subtype,
                        String basefont,
                        Object encoding /* , PDFToUnicode mapping */,
                        PDFCIDFont descendantFonts) {

        /* generic creation of PDF object */
        super(number, fontname, subtype, basefont, encoding /* , mapping */);

        /* set fields using paramaters */
        this.descendantFonts = descendantFonts;
    }

    /**
     * set the descendant font
     *
     * @param descendantFonts the CIDFont upon which this font is based
     */
    public void setDescendantFonts(PDFCIDFont descendantFonts) {
        this.descendantFonts = descendantFonts;
    }

    public void setCMAP(PDFCMap cmap) {
        this.cmap = cmap;
    }

    /**
     * fill in the specifics for the font's subtype
     */
    protected void fillInPDF(StringBuffer p) {
        if (descendantFonts != null) {
            p.append("\n/DescendantFonts [ "
                     + this.descendantFonts.referencePDF() + " ] ");
        }
        if (cmap != null) {
            p.append("\n/ToUnicode " + cmap.referencePDF());
        }
    }

}
