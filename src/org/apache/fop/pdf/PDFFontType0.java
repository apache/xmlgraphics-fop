/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import org.apache.fop.fonts.FontType;

/**
 * Class representing a Type0 font.
 * <p>
 * Type0 fonts are specified on page 208 and onwards of the PDF 1.3 spec.
 */
public class PDFFontType0 extends PDFFontNonBase14 {

    /**
     * This should be an array of CIDFont but only the first one is used
     */
    protected PDFCIDFont descendantFonts;

    /**
     * The character map
     */
    protected PDFCMap cmap;

    /**
     * Create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public PDFFontType0(int number, String fontname, 
                        String basefont,
                        Object encoding) {

        /* generic creation of PDF object */
        super(number, fontname, FontType.TYPE0, basefont, encoding /* , mapping */);

        /* set fields using paramaters */
        this.descendantFonts = null;
        cmap = null;
    }

    /**
     * Create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param descendantFonts the CIDFont upon which this font is based
     */
    public PDFFontType0(int number, String fontname, 
                        String basefont,
                        Object encoding, 
                        PDFCIDFont descendantFonts) {

        /* generic creation of PDF object */
        super(number, fontname, FontType.TYPE0, basefont, encoding /* , mapping */);

        /* set fields using paramaters */
        this.descendantFonts = descendantFonts;
    }

    /**
     * Set the descendant font
     * @param descendantFonts the CIDFont upon which this font is based
     */
    public void setDescendantFonts(PDFCIDFont descendantFonts) {
        this.descendantFonts = descendantFonts;
    }

    /**
     * Sets the character map
     * @param cmap the character map
     */
    public void setCMAP(PDFCMap cmap) {
        this.cmap = cmap;
    }

    /**
     * @see org.apache.fop.pdf.PDFFont#fillInPDF(StringBuffer)
     */
    protected void fillInPDF(StringBuffer target) {
        if (descendantFonts != null) {
            target.append("\n/DescendantFonts [ "
                     + this.descendantFonts.referencePDF() + " ] ");
        }
        if (cmap != null) {
            target.append("\n/ToUnicode " + cmap.referencePDF());
        }
    }

}
