package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class PagePosition extends Property  {
    public static final int dataTypes = ENUM | INHERIT;
    public static final int traitMapping = SPECIFICATION;
    public static final int initialValueType = ENUM_IT;
    public static final int FIRST = 1;
    public static final int LAST = 2;
    public static final int REST = 3;
    public static final int ANY = 4;
    public /*static*/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.PAGE_POSITION, ANY);
    }
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"first"
        ,"last"
        ,"rest"
        ,"any"
    };
    public /*static*/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /*static*/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

