/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class representing a Type1 or MMType1 font (not necessary for the base 14).
 *
 * Type1 fonts are specified on page 201 and onwards of the PDF 1.3 spec.
 * MMType1 fonts are specified on page 205 and onwards of the PDF 1.3 spec.
 *
 * In fact everything already done in the superclass.
 * Must only define the not default constructor.
 */
public class PDFFontType1 extends PDFFontNonBase14 {

    /**
     * create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param subtype the font's subtype (PDFFont.TYPE1 or PDFFont.MMTYPE1)
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param mapping the Unicode mapping mechanism
     */
    public PDFFontType1(int number, String fontname, byte subtype,
                        String basefont,
                        Object encoding /* , PDFToUnicode mapping */) {

        /* generic creation of PDF object */
        super(number, fontname, subtype, basefont, encoding);
    }

}
