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
	rwEnumHash = new HashMap(rwEnums.length);
	for (int i = 1; i < rwEnums.length; i++ ) {
	    rwEnumHash.put((Object)rwEnums[i],
				(Object) Ints.consts.get(i));
	}
    }

    public int getEnumIndex(String enum) {
        return ((Integer)(rwEnumHash.get(enum))).intValue();
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }

}

