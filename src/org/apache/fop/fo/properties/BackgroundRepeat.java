package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class BackgroundRepeat extends Property  {
    public static final int dataTypes = ENUM | INHERIT;
    public static final int traitMapping = RENDERING;
    public static final int initialValueType = ENUM_IT;
    public static final int REPEAT = 1;
    public static final int REPEAT_X = 2;
    public static final int REPEAT_Y = 3;
    public static final int NO_REPEAT = 4;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.BACKGROUND_REPEAT, REPEAT);
    }

    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"repeat"
        ,"repeat-x"
        ,"repeat-y"
        ,"no-repeat"
    };
    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}


