package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.properties.Property;

public class Conditionality extends Property  {
    public static final int DISCARD = 1;
    public static final int RETAIN = 2;

    private static final String[] rwEnums = {
        null
        ,"discard"
        ,"retain"
    };

    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }

}

