package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class CaptionSide extends Property  {
    public static final int dataTypes = ENUM | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = ENUM_IT;
    public static final int BEFORE = 1;
    public static final int AFTER = 2;
    public static final int START = 3;
    public static final int END = 4;
    public static final int TOP = 5;
    public static final int BOTTOM = 6;
    public static final int LEFT = 7;
    public static final int RIGHT = 8;

    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.CAPTION_SIDE, BEFORE);
    }

    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"before"
        ,"after"
        ,"start"
        ,"end"
        ,"top"
        ,"bottom"
        ,"left"
        ,"right"
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

