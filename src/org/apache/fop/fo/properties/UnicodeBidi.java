package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class UnicodeBidi extends Property  {
    public static final int dataTypes = ENUM | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = ENUM_IT;
    public static final int NORMAL = 1;
    public static final int EMBED = 2;
    public static final int BIDI_OVERRIDE = 3;
    public /*static*/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.UNICODE_BIDI, NORMAL);
    }
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"normal"
        ,"embed"
        ,"bidi-override"
    };
    public /*static*/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /*static*/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

