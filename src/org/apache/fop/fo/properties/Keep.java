package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class Keep extends Property  {

    private static final String[] rwEnums = {
        null
        ,"always"
    };

    public /*static*/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /*static*/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

