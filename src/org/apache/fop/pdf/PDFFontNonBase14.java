/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java

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
     * create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param subtype the font's subtype
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param mapping the Unicode mapping mechanism
     */
    public PDFFontNonBase14(int number, String fontname, byte subtype,
                            String basefont,
                            Object encoding /* , PDFToUnicode mapping */) {

        /* generic creation of PDF object */
        super(number, fontname, subtype, basefont, encoding);

        this.descriptor = null;
    }

    /**
     * set the width metrics for the font
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
     * set the font descriptor (unused for the Type3 fonts)
     *
     * @param descriptor the descriptor for other font's metrics
     */
    public void setDescriptor(PDFFontDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * fill in the specifics for the font's subtype
     */
    protected void fillInPDF(StringBuffer p) {
        p.append("\n/FirstChar ");
        p.append(firstChar);
        p.append("\n/LastChar ");
        p.append(lastChar);
        p.append("\n/Widths ");
        p.append(this.widths.referencePDF());
        if (descriptor != null) {
            p.append("\n/FontDescriptor ");
            p.append(this.descriptor.referencePDF());
        }
    }

}
