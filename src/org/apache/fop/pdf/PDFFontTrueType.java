/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import org.apache.fop.fonts.FontType;

/**
 * Class representing a TrueType font.
 * <p>
 * In fact everything already done in the superclass.
 * Must only define the not default constructor.
 */
public class PDFFontTrueType extends PDFFontNonBase14 {

    /**
     * create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public PDFFontTrueType(int number, String fontname, 
                           String basefont,
                           Object encoding) {

        /* generic creation of PDF object */
        super(number, fontname, FontType.TRUETYPE, basefont, encoding /* , mapping */);
    }

}
