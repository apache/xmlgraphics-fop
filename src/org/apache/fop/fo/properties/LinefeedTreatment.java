package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class LinefeedTreatment extends Property  {
    public static final int dataTypes = ENUM | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = ENUM_IT;
    public static final int IGNORE = 1;
    public static final int PRESERVE = 2;
    public static final int TREAT_AS_SPACE = 3;
    public static final int TREAT_AS_ZERO_WIDTH_SPACE = 4;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType(PropNames.LINEFEED_TREATMENT, TREAT_AS_SPACE);
    }
    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"ignore"
        ,"preserve"
        ,"treat-as-space"
        ,"treat-as-zero-width-space"
    };
    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

