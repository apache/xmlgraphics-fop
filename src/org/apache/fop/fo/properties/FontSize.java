package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Ems;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class FontSize extends Property  {
    public static final int dataTypes =
                        PERCENTAGE | LENGTH | MAPPED_LENGTH | INHERIT;
    public static final int traitMapping = FORMATTING| RENDERING;
    public static final int initialValueType = LENGTH_IT;
    public static final int XX_SMALL = 1;
    public static final int X_SMALL = 2;
    public static final int SMALL = 3;
    public static final int MEDIUM = 4;
    public static final int LARGE = 5;
    public static final int X_LARGE = 6;
    public static final int XX_LARGE = 7;
    public static final int LARGER = 8;
    public static final int SMALLER = 9;

    // N.B. This foundational value MUST be an absolute length
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return getMappedLength(MEDIUM);
    }

    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"xx-small"
        ,"x-small"
        ,"small"
        ,"medium"
        ,"large"
        ,"x-large"
        ,"xx-large"
        ,"larger"
        ,"smaller"
    };

    // N.B. this is a combination of points and ems
    private static final double[] mappedLengths = {
        0d
        ,7d         // xx-small
        ,8.3d       // x-small
        ,10d        // small
        ,12d        // medium
        ,14.4d      // large
        ,17.3d      // x-large
        ,20.7d      // xx-large
        ,1.2d       // larger
        ,0.83d      // smaller
    };

    public static Numeric getMappedLength(int enum)
        throws PropertyException
    {
        if (enum == LARGER || enum == SMALLER)
            return Ems.makeEms(PropNames.FONT_SIZE, mappedLengths[enum]);
        return
            Length.makeLength
                    (PropNames.FONT_SIZE, mappedLengths[enum], Length.PT);
    }

    private static final HashMap rwEnumHash;
    static {
        rwEnumHash = new HashMap(rwEnums.length);
        for (int i = 1; i < rwEnums.length; i++ ) {
            rwEnumHash.put((Object)rwEnums[i],
                                (Object) Ints.consts.get(i));
        }
    }

}

