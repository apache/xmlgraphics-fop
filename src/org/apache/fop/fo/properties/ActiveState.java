package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datatypes.Ints;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class ActiveState extends Property  {
    public static final int dataTypes = ENUM;
    public static final int traitMapping = ACTION;
    public static final int initialValueType = NOTYPE_IT;
    public static final int LINK = 1;
    public static final int VISITED = 2;
    public static final int ACTIVE = 3;
    public static final int HOVER = 4;
    public static final int FOCUS = 5;

    public static final int inherited = NO;

    private static final String[] rwEnums = {
	null
	,"link"
	,"visited"
	,"active"
	,"hover"
	,"focus"
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

