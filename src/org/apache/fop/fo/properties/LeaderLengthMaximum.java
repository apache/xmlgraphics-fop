package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.Percentage;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class LeaderLengthMaximum extends Property  {
    public static final int dataTypes = LENGTH | PERCENTAGE;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = PERCENTAGE_IT;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return Percentage.makePercentage
                                (PropNames.LEADER_LENGTH_MAXIMUM, 100.0d);
    }
    public static final int inherited = COMPUTED;
}

