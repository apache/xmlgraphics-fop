package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class RetrievePosition extends Property  {
    public static final int dataTypes = ENUM;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = ENUM_IT;
    public static final int FIRST_STARTING_WITHIN_PAGE = 1;
    public static final int FIRST_INCLUDING_CARRYOVER = 2;
    public static final int LAST_STARTING_WITHIN_PAGE = 3;
    public static final int LAST_ENDING_WITHIN_PAGE = 4;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType
                (PropNames.RETRIEVE_POSITION, FIRST_STARTING_WITHIN_PAGE);
    }
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"first-starting-within-page"
        ,"first-including-carryover"
        ,"last-starting-within-page"
        ,"last-ending-within-page"
    };
    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

