package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.properties.ColorCommon;

import java.util.HashMap;

/**
 * Pseudo-property class for common color values occurring in a
 * number of classes.
 */
public class ColorNonTransparent extends ColorCommon  {

    /**
     * <tt>rwColorEnums</tt> exclude "transparent"
     */
    private static final HashMap rwEnumHash;
    static {
	rwEnumHash = new HashMap(rwEnums.length);
	for (int i = 1; i < rwEnums.length - 1; i++ ) {
	    rwEnumHash.put((Object)rwEnums[i],
				(Object) Ints.consts.get(i));
	}
    }

    public /*static*/ int getEnumIndex(String enum) {
        return ((Integer)(rwEnumHash.get(enum))).intValue();
    }
    public /*static*/ String getEnumText(int index) {
        return rwEnums[index];
    }

}

