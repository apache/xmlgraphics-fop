package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class Language extends Property  {
    public static final int dataTypes = LANGUAGE_T | NONE | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = NONE_IT;
    public static final int inherited = COMPUTED;
}

