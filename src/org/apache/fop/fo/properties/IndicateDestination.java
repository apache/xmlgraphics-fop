package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Bool;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class IndicateDestination extends Property  {
    public static final int dataTypes = BOOL;
    public static final int traitMapping = ACTION;
    public static final int initialValueType = BOOL_IT;
    public /*static*/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new Bool(PropNames.INDICATE_DESTINATION, false);
    }
    public static final int inherited = NO;
}

