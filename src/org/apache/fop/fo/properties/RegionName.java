package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.properties.Property;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class RegionName extends Property  {
    public static final int dataTypes = NCNAME | ENUM;
    public static final int traitMapping = SPECIFICATION;
    public static final int initialValueType = NOTYPE_IT;
    public static final int XSL_REGION_BODY = 1;
    public static final int XSL_REGION_START = 2;
    public static final int XSL_REGION_END = 3;
    public static final int XSL_REGION_BEFORE = 4;
    public static final int XSL_REGION_AFTER = 5;
    public static final int XSL_BEFORE_FLOAT_SEPARATOR = 6;
    public static final int XSL_FOOTNOTE_SEPARATOR = 7;
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"xsl-region-body"
        ,"xsl-region-start"
        ,"xsl-region-end"
        ,"xsl-region-before"
        ,"xsl-region-after"
        ,"xsl-before-float-separator"
        ,"xsl-footnote-separator"
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

