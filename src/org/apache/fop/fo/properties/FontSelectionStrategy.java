package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.properties.Property;

public class FontSelectionStrategy extends Property  {
    public static final int dataTypes = AUTO | ENUM | INHERIT;
    public static final int traitMapping = FONT_SELECTION;
    public static final int initialValueType = AUTO_IT;
    public static final int CHARACTER_BY_CHARACTER = 1;
    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"character-by-character"
    };
    public /*static*/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /*static*/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

