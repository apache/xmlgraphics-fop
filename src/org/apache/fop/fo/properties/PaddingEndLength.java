package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class PaddingEndLength extends Property  {
    public static final int dataTypes = PERCENTAGE | LENGTH;
    public static final int traitMapping = FORMATTING | RENDERING;
    public static final int initialValueType = LENGTH_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return Length.makeLength(PropNames.PADDING_END_LENGTH,
                                                        0.0d, Length.PT);
    }
    public static final int inherited =  NO;
}

