package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.properties.Property;

public class TextTransform extends Property  {
    public static final int dataTypes = ENUM | NONE | INHERIT;
    public static final int traitMapping = REFINE;
    public static final int initialValueType = NONE_IT;
    public static final int CAPITALIZE = 1;
    public static final int UPPERCASE = 2;
    public static final int LOWERCASE = 3;
    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"capitalize"
        ,"uppercase"
        ,"lowercase"
    };
    public /*static*/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /*static*/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

