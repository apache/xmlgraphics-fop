package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.properties.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class Clear extends Property  {
    public static final int dataTypes = ENUM | NONE | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = NONE_IT;
    public static final int START = 1;
    public static final int END = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;
    public static final int BOTH = 5;

    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"start"
        ,"end"
        ,"left"
        ,"right"
        ,"both"
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

