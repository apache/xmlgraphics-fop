package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.ColorTransparent;

public class BorderLeftColor extends ColorTransparent {
    public static final int dataTypes = ENUM | COLOR_T | INHERIT;
    public static final int traitMapping = DISAPPEARS;
    public static final int initialValueType = COLOR_IT;
    public static final int inherited = NO;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new ColorType (PropNames.BACKGROUND_COLOR, BLACK);
    }

}

