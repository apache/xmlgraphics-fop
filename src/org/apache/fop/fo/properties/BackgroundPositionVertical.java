package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.Percentage;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class BackgroundPositionVertical extends Property  {
    public static final int dataTypes =
                                    PERCENTAGE | LENGTH | ENUM | INHERIT;
    public static final int traitMapping = VALUE_CHANGE;
    public static final int initialValueType = PERCENTAGE_IT;
    public static final int TOP = 1;
    public static final int CENTER = 2;
    public static final int BOTTOM = 3;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return Percentage.makePercentage
                        (PropNames.BACKGROUND_POSITION_VERTICAL, 0.0d);
    }
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"top"
        ,"center"
        ,"bottom"
    };
    public /**/static/**/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /**/static/**/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

