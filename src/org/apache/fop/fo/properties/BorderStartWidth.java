package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.BorderCommonWidth;

public class BorderStartWidth extends BorderCommonWidth {
    public static final int dataTypes =
                            COMPOUND | MAPPED_LENGTH | LENGTH | INHERIT;
    public static final int traitMapping = FORMATTING | RENDERING;
    public static final int initialValueType = LENGTH_IT;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return getMappedLength(MEDIUM);
    }

    public static Numeric getMappedLength(int enum)
        throws PropertyException
    {
        return getMappedLength(PropNames.BORDER_START_WIDTH, enum);
    }

    public static final int inherited = NO;

}

