package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.UriType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class ExternalDestination extends Property  {
    public static final int dataTypes = URI_SPECIFICATION;
    public static final int traitMapping = ACTION;
    public static final int initialValueType = URI_SPECIFICATION_IT;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new UriType(PropNames.EXTERNAL_DESTINATION, "");
    }
    public static final int inherited = NO;
}

