package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datatypes.Ints;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class AlignmentAdjust extends Property  {
    public static final int dataTypes =
			    AUTO | ENUM | PERCENTAGE | LENGTH | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;
    public static final int BASELINE = 1;
    public static final int BEFORE_EDGE = 2;
    public static final int TEXT_BEFORE_EDGE = 3;
    public static final int MIDDLE = 4;
    public static final int CENTRAL = 5;
    public static final int AFTER_EDGE = 6;
    public static final int TEXT_AFTER_EDGE = 7;
    public static final int IDEOGRAPHIC = 8;
    public static final int ALPHABETIC = 9;
    public static final int HANGING = 10;
    public static final int MATHEMATICAL = 11;

    public static final int inherited = NO;

    private static final String[] rwEnums = {
	null
	,"baseline"
	,"before-edge"
	,"text-before-edge"
	,"middle"
	,"central"
	,"after-edge"
	,"text-after-edge"
	,"ideographic"
	,"alphabetic"
	,"hanging"
	,"mathematical"
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

