package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.properties.Property;

public class MediaUsage extends Property  {
    public static final int dataTypes = AUTO | ENUM;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;
    public static final int PAGINATE = 1;
    public static final int BOUNDED_IN_ONE_DIMENSION = 2;
    public static final int UNBOUNDED = 3;
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"paginate"
        ,"bounded-in-one-dimension"
        ,"unbounded"
    };
    public /*static*/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /*static*/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

