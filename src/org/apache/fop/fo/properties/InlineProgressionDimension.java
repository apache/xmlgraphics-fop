package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class InlineProgressionDimension extends Property  {
    public static final int dataTypes =
                        COMPOUND | PERCENTAGE | LENGTH | AUTO | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;
    public static final int inherited = NO;
}

