package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.properties.Property;

public class LeaderAlignment extends Property  {
    public static final int dataTypes = ENUM | NONE | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = NONE_IT;
    public static final int REFERENCE_AREA = 1;
    public static final int PAGE = 2;
    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"reference-area"
        ,"page"
    };
    public /**/static/**/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /**/static/**/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

