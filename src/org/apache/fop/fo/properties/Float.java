package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.properties.Property;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class Float extends Property  {
    public static final int dataTypes = ENUM | NONE | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = NONE_IT;
    public static final int BEFORE = 1;
    public static final int START = 2;
    public static final int END = 3;
    public static final int LEFT = 4;
    public static final int RIGHT = 5;

    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"before"
        ,"start"
        ,"end"
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
    public int getEnumIndex(String enum) {
        return ((Integer)(rwEnumHash.get(enum))).intValue();
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

