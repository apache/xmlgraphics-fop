package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.properties.Property;

public class LetterValue extends Property  {
    public static final int dataTypes = AUTO | ENUM;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;
    public static final int ALPHABETIC = 1;
    public static final int TRADITIONAL = 2;
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"alphabetic"
        ,"traditional"
    };
    public /**/static/**/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /**/static/**/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

