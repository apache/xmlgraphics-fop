package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class RefId extends Property  {
    public static final int dataTypes = NCNAME | INHERIT;
    public static final int traitMapping = REFERENCE;
    public static final int initialValueType = NOTYPE_IT;
    public static final int inherited = NO;
}

