package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.BreakCommon;

public class BreakAfter extends BreakCommon {
    public static final int dataTypes = AUTO | ENUM | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = AUTO_IT;

    public static final int inherited = NO;
}

