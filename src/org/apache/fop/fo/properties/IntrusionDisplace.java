package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.properties.Property;

public class IntrusionDisplace extends Property  {
    public static final int dataTypes = AUTO | ENUM | NONE | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;
    public static final int LINE = 1;
    public static final int INDENT = 2;
    public static final int BLOCK = 3;
    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"line"
        ,"indent"
        ,"block"
    };
    public /*static*/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /*static*/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

