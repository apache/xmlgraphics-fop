package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class MaxWidth extends Property  {
    public static final int dataTypes =
                                    PERCENTAGE | LENGTH | NONE | INHERIT;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = NONE_IT;
    public static final int inherited = NO;
}

