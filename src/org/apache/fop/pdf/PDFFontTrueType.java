/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class representing a TrueType font.
 *
 * In fact everything already done in the superclass.
 * Must only define the not default constructor.
 */
public class PDFFontTrueType extends PDFFontNonBase14 {

    /**
     * create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param subtype the font's subtype (PDFFont.TRUETYPE)
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param mapping the Unicode mapping mechanism
     */
    public PDFFontTrueType(int number, String fontname, byte subtype,
                           String basefont,
                           Object encoding /* , PDFToUnicode mapping */) {

        /* generic creation of PDF object */
        super(number, fontname, subtype, basefont, encoding /* , mapping */);
    }

}
