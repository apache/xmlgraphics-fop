package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class ZIndex extends Property  {
    public static final int dataTypes =INTEGER | AUTO | INHERIT;
    public static final int traitMapping = VALUE_CHANGE;
    public static final int initialValueType = AUTO_IT;
    public static final int inherited = NO;
}

