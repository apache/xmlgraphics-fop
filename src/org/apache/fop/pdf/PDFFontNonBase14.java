/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import org.apache.fop.fonts.FontType;

/**
 * A common ancestor for Type1, TrueType, MMType1 and Type3 fonts
 * (all except base 14 fonts).
 */
public abstract class PDFFontNonBase14 extends PDFFont {

    /**
     * first character code in the font
     */
    protected int firstChar;

    /**
     * last character code in the font
     */
    protected int lastChar;

    /**
     * widths of characters from firstChar to lastChar
     */
    protected PDFArray widths;

    /**
     * descriptor of font metrics
     */
    protected PDFFontDescriptor descriptor;

    /**
     * Create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param subtype the font's subtype
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public PDFFontNonBase14(int number, String fontname, FontType subtype,
                            String basefont,
                            Object encoding) {

        /* generic creation of PDF object */
        super(number, fontname, subtype, basefont, encoding);

        this.descriptor = null;
    }

    /**
     * Set the width metrics for the font
     *
     * @param firstChar the first character code in the font
     * @param lastChar the last character code in the font
     * @param widths an array of size (lastChar - firstChar +1)
     */
    public void setWidthMetrics(int firstChar, int lastChar,
                                PDFArray widths) {
        /* set fields using paramaters */
        this.firstChar = firstChar;
        this.lastChar = lastChar;
        this.widths = widths;
    }

    /**
     * Set the font descriptor (unused for the Type3 fonts)
     *
     * @param descriptor the descriptor for other font's metrics
     */
    public void setDescriptor(PDFFontDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * @see org.apache.fop.pdf.PDFFont#fillInPDF(StringBuffer)
     */
    protected void fillInPDF(StringBuffer target) {
        target.append("\n/FirstChar ");
        target.append(firstChar);
        target.append("\n/LastChar ");
        target.append(lastChar);
        target.append("\n/Widths ");
        target.append(this.widths.referencePDF());
        if (descriptor != null) {
            target.append("\n/FontDescriptor ");
            target.append(this.descriptor.referencePDF());
        }
    }

}
