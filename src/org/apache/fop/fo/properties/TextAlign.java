package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class TextAlign extends Property  {
    public static final int dataTypes = LITERAL | ENUM | INHERIT;
    public static final int traitMapping = VALUE_CHANGE;
    public static final int initialValueType = ENUM_IT;
    public static final int START = 1;
    public static final int CENTER = 2;
    public static final int END = 3;
    public static final int JUSTIFY = 4;
    public static final int INSIDE = 5;
    public static final int OUTSIDE = 6;
    public static final int LEFT = 7;
    public static final int RIGHT = 8;

    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType(PropNames.TEXT_ALIGN, START);
    }

    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"start"
        ,"center"
        ,"end"
        ,"justify"
        ,"inside"
        ,"outside"
        ,"left"
        ,"right"
    };
    private static final HashMap rwEnumHash;
    static {
        rwEnumHash = new HashMap(rwEnums.length);
        for (int i = 1; i < rwEnums.length; i++ ) {
            rwEnumHash.put((Object)rwEnums[i],
                                (Object) Ints.consts.get(i));
        }
    }
    public /**/static/**/ int getEnumIndex(String enum) {
        return ((Integer)(rwEnumHash.get(enum))).intValue();
    }
    public /**/static/**/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

