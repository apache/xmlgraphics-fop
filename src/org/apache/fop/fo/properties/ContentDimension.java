package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class ContentDimension extends Property  {
    public static final int SCALE_TO_FIT = 1;

    private static final String[] rwEnums = {
        null
        ,"scale-to-fit"
    };

    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }

}

