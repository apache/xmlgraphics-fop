package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class BaselineShift extends Property  {
    public static final int dataTypes =
                                    PERCENTAGE | LENGTH | ENUM | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = ENUM_IT;
    public static final int BASELINE = 1;
    public static final int SUB = 2;
    public static final int SUPER = 3;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.BASELINE_SHIFT, BASELINE);
    }

    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"baseline"
        ,"sub"
        ,"super"
    };
    public /**/static/**/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /**/static/**/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

