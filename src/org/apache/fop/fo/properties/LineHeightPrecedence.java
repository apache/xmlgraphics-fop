package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.PrecedenceCommon;

public class LineHeightPrecedence extends PrecedenceCommon {
    public static final int dataTypes = INTEGER | ENUM;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = ENUM_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType
            (PropNames.LINE_HEIGHT_PRECEDENCE, FORCE);
    }

    public static final int inherited = COMPUTED;

}

