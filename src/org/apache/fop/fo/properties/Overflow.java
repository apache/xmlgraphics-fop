package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.properties.Property;

public class Overflow extends Property  {
    public static final int dataTypes = AUTO | ENUM | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;
    public static final int VISIBLE = 1;
    public static final int HIDDEN = 2;
    public static final int SCROLL = 3;
    public static final int ERROR_IF_OVERFLOW = 4;
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"visible"
        ,"hidden"
        ,"scroll"
        ,"error-if-overflow"
    };
    public /*static*/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /*static*/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

