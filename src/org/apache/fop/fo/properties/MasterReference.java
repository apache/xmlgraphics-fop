package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class MasterReference extends Property  {
    public static final int dataTypes = NCNAME;
    public static final int traitMapping = SPECIFICATION;
    public static final int initialValueType = NCNAME_IT;
    public /*static*/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new NCName(PropNames.MASTER_REFERENCE, "");
    }
    public static final int inherited = NO;
}

