package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.datatypes.Ints;

import java.util.HashMap;

/**
 * Pseudo-property class for common border style values occurring in a
 * number of classes.
 */
public class BorderCommonStyle extends Property  {
    public static final int HIDDEN = 1;
    public static final int DOTTED = 2;
    public static final int DASHED = 3;
    public static final int SOLID = 4;
    public static final int DOUBLE = 5;
    public static final int GROOVE = 6;
    public static final int RIDGE = 7;
    public static final int INSET = 8;
    public static final int OUTSET = 9;

    private static final String[] rwEnums = {
	null
	,"hidden"
	,"dotted"
	,"dashed"
	,"solid"
	,"double"
	,"groove"
	,"ridge"
	,"inset"
	,"outset"
    };

    private static final HashMap rwEnumHash;
    static {
	rwEnumHash = new HashMap(rwEnums.length);
	for (int i = 1; i < rwEnums.length; i++ ) {
	    rwEnumHash.put((Object)rwEnums[i],
				(Object) Ints.consts.get(i));
	}
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

