package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class PaddingBottom extends Property  {
    public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
    public static final int traitMapping = DISAPPEARS;
    public static final int initialValueType = LENGTH_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return Length.makeLength
                            (PropNames.PADDING_BOTTOM, 0.0d, Length.PT);
    }
    public static final int inherited = NO;
}

