package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class WhiteSpaceTreatment extends Property  {
    public static final int dataTypes = ENUM | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = ENUM_IT;
    public static final int IGNORE = 1;
    public static final int PRESERVE = 2;
    public static final int IGNORE_IF_BEFORE_LINEFEED = 3;
    public static final int IGNORE_IF_AFTER_LINEFEED = 4;
    public static final int IGNORE_IF_SURROUNDING_LINEFEED = 5;
    public /*static*/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.WHITE_SPACE_TREATMENT, PRESERVE);
    }
    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"ignore"
        ,"preserve"
        ,"ignore-if-before-linefeed"
        ,"ignore-if-after-linefeed"
        ,"ignore-if-surrounding-linefeed"
    };
    private static final HashMap rwEnumHash;
    static {
        rwEnumHash = new HashMap(rwEnums.length);
        for (int i = 1; i < rwEnums.length; i++ ) {
            rwEnumHash.put((Object)rwEnums[i],
                                (Object) Ints.consts.get(i));
        }
    }
    public /*static*/ int getEnumIndex(String enum) {
        return ((Integer)(rwEnumHash.get(enum))).intValue();
    }
    public /*static*/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

