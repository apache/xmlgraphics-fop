package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.properties.Property;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class DominantBaseline extends Property  {
    public static final int dataTypes = AUTO | ENUM | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;
    public static final int USE_SCRIPT = 1;
    public static final int NO_CHANGE = 2;
    public static final int RESET_SIZE = 3;
    public static final int IDEOGRAPHIC = 4;
    public static final int ALPHABETIC = 5;
    public static final int HANGING = 6;
    public static final int MATHEMATICAL = 7;
    public static final int CENTRAL = 8;
    public static final int MIDDLE = 9;
    public static final int TEXT_AFTER_EDGE = 10;
    public static final int TEXT_BEFORE_EDGE = 11;
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"use-script"
        ,"no-change"
        ,"reset-size"
        ,"ideographic"
        ,"alphabetic"
        ,"hanging"
        ,"mathematical"
        ,"central"
        ,"middle"
        ,"test-after-edge"
        ,"text-before-edge"
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

