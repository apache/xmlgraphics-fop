package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.BorderCommonWidth;

public class BorderRightWidth extends BorderCommonWidth {
    public static final int dataTypes = LENGTH | MAPPED_LENGTH | INHERIT;
    public static final int traitMapping = DISAPPEARS;
    public static final int initialValueType = LENGTH_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return getMappedLength(null, MEDIUM);
    }

    public Numeric getMappedLength(FONode node, int enum)
        throws PropertyException
    {
        return getMappedLength(node, PropNames.BORDER_RIGHT_WIDTH, enum);
    }

    public static final int inherited = NO;

}

