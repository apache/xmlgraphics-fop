package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class EmptyCells extends Property  {
    public static final int dataTypes = ENUM | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = ENUM_IT;
    public static final int SHOW = 1;
    public static final int HIDE = 2;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.EMPTY_CELLS, SHOW);
    }
    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"show"
        ,"hide"
    };
    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

