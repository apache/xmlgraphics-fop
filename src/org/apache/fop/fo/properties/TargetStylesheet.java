package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class TargetStylesheet extends Property  {
    public static final int dataTypes = URI_SPECIFICATION | ENUM;
    public static final int traitMapping = ACTION;
    public static final int initialValueType = ENUM_IT;
    public static final int USE_NORMAL_STYLESHEET = 1;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType(PropNames.TARGET_STYLESHEET,
                                                USE_NORMAL_STYLESHEET);
    }
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"use-normal-stylesheet"
    };
    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

