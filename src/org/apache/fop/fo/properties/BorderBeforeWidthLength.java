package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class BorderBeforeWidthLength extends Property  {
    public static final int dataTypes = LENGTH;
    public static final int traitMapping = FORMATTING | RENDERING;
    public static final int initialValueType = LENGTH_IT;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return BorderCommonWidth.getMappedLength
        (PropNames.BORDER_BEFORE_WIDTH_LENGTH, BorderCommonWidth.MEDIUM);
    }

    public static final int inherited = NO;

}

