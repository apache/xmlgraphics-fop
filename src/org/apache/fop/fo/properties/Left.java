package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class Left extends Property  {
    public static final int dataTypes =
                                    PERCENTAGE | LENGTH | AUTO | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;
    public static final int inherited = NO;
}

