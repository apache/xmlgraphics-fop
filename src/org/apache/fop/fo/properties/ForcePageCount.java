package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.properties.Property;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class ForcePageCount extends Property  {
    public static final int dataTypes = AUTO | ENUM | INHERIT;
    public static final int traitMapping = SPECIFICATION;
    public static final int initialValueType = AUTO_IT;
    public static final int EVEN = 1;
    public static final int ODD = 2;
    public static final int END_ON_EVEN = 3;
    public static final int END_ON_ODD = 4;
    public static final int NO_FORCE = 5;

    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"even"
        ,"odd"
        ,"end-on-even"
        ,"end-on-odd"
        ,"no-force"
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

