package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Ems;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class LineHeightMaximum extends Property  {
    public static final int dataTypes = LENGTH | PERCENTAGE;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = LENGTH_IT;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return Ems.makeEms(PropNames.LINE_HEIGHT_MAXIMUM, 1.2d);
    }
    public static final int inherited = COMPUTED;
}

