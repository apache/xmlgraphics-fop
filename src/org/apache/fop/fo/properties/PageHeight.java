package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.properties.PageHeightWidth;

public class PageHeight extends PageHeightWidth  {
    public static final int dataTypes = LENGTH | AUTO | ENUM | INHERIT;
    public static final int traitMapping = SPECIFICATION;
    public static final int initialValueType = AUTO_IT;
    public static final int INDEFINITE = 1;
    public static final int inherited = NO;
}

