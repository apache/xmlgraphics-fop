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

public class FontStretch extends Property  {
    public static final int dataTypes = ENUM | INHERIT;
    public static final int traitMapping = FONT_SELECTION;
    public static final int initialValueType = ENUM_IT;
    public static final int NORMAL = 1;
    public static final int WIDER = 2;
    public static final int NARROWER = 3;
    public static final int ULTRA_CONDENSED = 4;
    public static final int EXTRA_CONDENSED = 5;
    public static final int CONDENSED = 6;
    public static final int SEMI_CONDENSED = 7;
    public static final int SEMI_EXPANDED = 8;
    public static final int EXPANDED = 9;
    public static final int EXTRA_EXPANDED = 10;
    public static final int ULTRA_EXPANDED = 11;

    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.FONT_STRETCH, NORMAL);
    }

    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"normal"
        ,"wider"
        ,"narrower"
        ,"ultra-condensed"
        ,"extra-condensed"
        ,"condensed"
        ,"semi-condensed"
        ,"semi-expanded"
        ,"expanded"
        ,"extra-expanded"
        ,"ultra-expanded"
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

