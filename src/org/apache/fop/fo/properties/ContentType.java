package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class ContentType extends Property  {
    public static final int dataTypes = NCNAME | MIMETYPE | AUTO;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;
    public static final int inherited = NO;
}

