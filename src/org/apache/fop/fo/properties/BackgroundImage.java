package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class BackgroundImage extends Property  {
    public static final int dataTypes = URI_SPECIFICATION | NONE | INHERIT;
    public static final int traitMapping = RENDERING;
    public static final int initialValueType = NONE_IT;

    public static final int inherited = NO;
}

