package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.properties.Property;

import java.util.HashMap;

public class BreakCommon extends Property  {
    public static final int COLUMN = 1;
    public static final int PAGE = 2;
    public static final int EVEN_PAGE = 3;
    public static final int ODD_PAGE = 4;

    private static final String[] rwEnums = {
        null
        ,"column"
        ,"page"
        ,"even-page"
        ,"odd-page"
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

