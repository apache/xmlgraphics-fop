package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.properties.ColorCommon;

import java.util.HashMap;

/**
 * Pseudo-property class for common color values, and the special value
 * "transparent", occurring in a number of classes.
 */
public class ColorTransparent extends ColorCommon  {

    /**
     * include "transparent"
     */
    private static final HashMap rwEnumHash;
    static {
	rwEnumHash = new HashMap(rwEnums.length + 1);
	for (int i = 1; i < rwEnums.length; i++ ) {
	    rwEnumHash.put(rwEnums[i], Ints.consts.get(i));
	}
        rwEnumHash.put("grey", Ints.consts.get(ColorCommon.GRAY));
    }

    public int getEnumIndex(String enum)
        throws PropertyException
    {
        Integer ii = (Integer)(rwEnumHash.get(enum));
        if (ii == null)
            throw new PropertyException("Unknown enum value: " + enum);
        return ii.intValue();
    }

    public String getEnumText(int index)
        throws PropertyException
    {
        if (index < 1 || index >= rwEnums.length)
            throw new PropertyException("index out of range: " + index);
        return rwEnums[index];
    }

}

