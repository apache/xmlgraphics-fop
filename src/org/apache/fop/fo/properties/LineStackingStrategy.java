package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class LineStackingStrategy extends Property  {
    public static final int dataTypes = ENUM | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = ENUM_IT;
    public static final int LINE_HEIGHT = 1;
    public static final int FONT_HEIGHT = 2;
    public static final int MAX_HEIGHT = 3;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType
                        (PropNames.LINE_STACKING_STRATEGY, LINE_HEIGHT);
    }
    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"line-height"
        ,"font-height"
        ,"max-height"
    };
    public /**/static/**/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /**/static/**/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

