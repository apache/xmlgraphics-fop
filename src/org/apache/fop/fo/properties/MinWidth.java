package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class MinWidth extends Property  {
    public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = NOTYPE_IT;
    public static final int inherited = NO;
}

