package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.PageHeightWidth;

public class PageWidth extends PageHeightWidth  {
    public static final int dataTypes = LENGTH | ENUM | INHERIT;
    public static final int traitMapping = SPECIFICATION;
    public static final int initialValueType = AUTO_IT;
    public static final int inherited = NO;

}

