package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.ContentDimension;

public class ContentHeight extends ContentDimension  {
    public static final int dataTypes =
                            PERCENTAGE | LENGTH | AUTO | ENUM | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;
    public static final int inherited = NO;

}

