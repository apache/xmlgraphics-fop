package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.BorderCommonWidth;

public class BorderAfterWidthLength extends BorderCommonWidth  {
    public static final int dataTypes = LENGTH;
    public static final int traitMapping = FORMATTING | RENDERING;
    public static final int initialValueType = LENGTH_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return getMappedLength
        (PropNames.BORDER_AFTER_WIDTH_LENGTH, MEDIUM);
    }

    public static final int inherited = NO;

}

