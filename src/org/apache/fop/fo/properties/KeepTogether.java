package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Keep;

public class KeepTogether extends Keep  {
    public static final int dataTypes =
                            COMPOUND | AUTO | ENUM | INTEGER | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;
    public static final int inherited = NO;
}

