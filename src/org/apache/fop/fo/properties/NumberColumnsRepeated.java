package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class NumberColumnsRepeated extends Property  {
    public static final int dataTypes = NUMBER;
    public static final int traitMapping = SPECIFICATION;
    public static final int initialValueType = NUMBER_IT;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new Numeric (PropNames.NUMBER_COLUMNS_REPEATED, 1d);
    }

    public static final int inherited = NO;
}

