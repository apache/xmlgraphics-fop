package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.IntegerType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class ReferenceOrientation extends Property  {
    public static final int dataTypes = INTEGER | INHERIT;
    public static final int traitMapping = NEW_TRAIT;
    public static final int initialValueType = INTEGER_IT;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new IntegerType(PropNames.REFERENCE_ORIENTATION, 0);
    }
    public static final int inherited = COMPUTED;
}

