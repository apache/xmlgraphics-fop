package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class WhiteSpace extends Property  {
    public static final int dataTypes = SHORTHAND | ENUM | INHERIT;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = ENUM_IT;
    public static final int NORMAL = 1;
    public static final int PRE = 2;
    public static final int NOWRAP = 3;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.WHITE_SPACE, NORMAL);
    }
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"normal"
        ,"pre"
        ,"nowrap"
    };
    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

