/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import org.apache.fop.fonts.FontType;

/**
 * Class representing a Type1 or MMType1 font (not necessary for the base 14).
 * <p>
 * Type1 fonts are specified on page 201 and onwards of the PDF 1.3 spec.
 * <br>
 * MMType1 fonts are specified on page 205 and onwards of the PDF 1.3 spec.
 * <p>
 * In fact everything already done in the superclass.
 * Must only define the not default constructor.
 */
public class PDFFontType1 extends PDFFontNonBase14 {

    /**
     * Create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public PDFFontType1(int number, String fontname, 
                        String basefont,
                        Object encoding) {

        /* generic creation of PDF object */
        super(number, fontname, FontType.TYPE1, basefont, encoding);
    }

}
