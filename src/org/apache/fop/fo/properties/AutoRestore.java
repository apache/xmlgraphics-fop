package org.apache.fop.fo.properties;

import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Bool;

public class AutoRestore extends Property  {
    public static final int dataTypes = BOOL;
    public static final int traitMapping = ACTION;
    public static final int initialValueType = BOOL_IT;
    public PropertyValue getInitialValue(int property)
	throws PropertyException
    {
	return new Bool(PropNames.AUTO_RESTORE, true);
    }
    public static final int inherited = COMPUTED;
}

