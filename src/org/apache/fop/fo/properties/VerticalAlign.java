package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class VerticalAlign extends Property  {
    public static final int dataTypes =
                        SHORTHAND | PERCENTAGE | LENGTH | ENUM | INHERIT;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = ENUM_IT;
    public static final int BASELINE = 1;
    public static final int MIDDLE = 2;
    public static final int SUB = 3;
    public static final int SUPER = 4;
    public static final int TEXT_TOP = 5;
    public static final int TEXT_BOTTOM = 6;
    public static final int TOP = 7;
    public static final int BOTTOM = 8;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.VERTICAL_ALIGN, BASELINE);
    }
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"baseline"
        ,"middle"
        ,"sub"
        ,"super"
        ,"text-top"
        ,"text-bottom"
        ,"top"
        ,"bottom"
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

