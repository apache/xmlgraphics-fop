package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.properties.Property;

public class Size extends Property  {
    public static final int dataTypes = SHORTHAND | AUTO | ENUM | INHERIT;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = AUTO_IT;
    public static final int LANDSCAPE = 1;
    public static final int PORTRAIT = 2;
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"landscape"
        ,"portrait"
    };
    public /**/static/**/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /**/static/**/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

