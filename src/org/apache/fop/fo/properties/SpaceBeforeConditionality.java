package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Conditionality;

public class SpaceBeforeConditionality extends Conditionality {
    public static final int dataTypes = ENUM;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = ENUM_IT;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType(PropNames.SPACE_BEFORE_CONDITIONALITY, DISCARD);
    }
    public static final int inherited = NO;

}

