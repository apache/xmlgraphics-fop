/*
 * $Id$
 * Copyright (C) 2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

import org.apache.avalon.framework.ValuedEnum;

/**
 * This class enumerates all supported CID font types.
 */
public class CIDFontType extends ValuedEnum {

    /**
     * CID Font Type 0
     */
    public static final CIDFontType CIDTYPE0 = new CIDFontType("CIDFontType0", 0);

    /**
     * CID Font Type 2
     */
    public static final CIDFontType CIDTYPE2 = new CIDFontType("CIDFontType2", 1);


    /**
     * @see org.apache.avalon.framework.Enum#Enum(String)
     */
    protected CIDFontType(String name, int value) {
        super(name, value);
    }


    /**
     * Returns the CIDFontType by name.
     * @param name Name of the CID font type to look up
     * @return FontType the CID font type
     */
    public static CIDFontType byName(String name) {
        if (name.equalsIgnoreCase(CIDFontType.CIDTYPE0.getName())) {
            return CIDFontType.CIDTYPE0;
        } else if (name.equalsIgnoreCase(CIDFontType.CIDTYPE2.getName())) {
            return CIDFontType.CIDTYPE2;
        } else {
            throw new IllegalArgumentException("Invalid CID font type: " + name);
        }
    }
    
    
    /**
     * Returns the CID FontType by value.
     * @param value Value of the CID font type to look up
     * @return FontType the CID font type
     */
    public static CIDFontType byValue(int value) {
        if (value == CIDFontType.CIDTYPE0.getValue()) {
            return CIDFontType.CIDTYPE0;
        } else if (value == CIDFontType.CIDTYPE2.getValue()) {
            return CIDFontType.CIDTYPE2;
        } else {
            throw new IllegalArgumentException("Invalid CID font type: " + value);
        }
    }
    
}
