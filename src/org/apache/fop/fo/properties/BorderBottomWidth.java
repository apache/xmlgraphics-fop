package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.BorderCommonWidth;

public class BorderBottomWidth extends BorderCommonWidth {
    public static final int dataTypes = MAPPED_LENGTH | INHERIT;
    public static final int traitMapping = DISAPPEARS;
    public static final int initialValueType = LENGTH_IT;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return getMappedLength(MEDIUM);
    }

    public static Numeric getMappedLength(int enum)
        throws PropertyException
    {
        return getMappedLength(PropNames.BORDER_BOTTOM_WIDTH, enum);
    }

    public static final int inherited = NO;

}

