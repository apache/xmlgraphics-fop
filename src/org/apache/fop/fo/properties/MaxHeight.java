package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class MaxHeight extends Property  {
    public static final int dataTypes =
                                    PERCENTAGE | LENGTH | NONE | INHERIT;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = LENGTH_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return Length.makeLength (PropNames.MAX_HEIGHT, 0.0d, Length.PT);
    }
    public static final int inherited = NO;
}

