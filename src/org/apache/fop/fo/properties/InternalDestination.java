package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Literal;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class InternalDestination extends Property  {
    public static final int dataTypes = LITERAL | NCNAME;
    public static final int traitMapping = ACTION;
    public static final int initialValueType = LITERAL_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new Literal(PropNames.INTERNAL_DESTINATION, "");
    }
    public static final int inherited = NO;
}

