package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.PropNames;

public class BackgroundColor extends ColorTransparent {
    public static final int dataTypes = COLOR_TRANS | INHERIT;
    public static final int traitMapping = RENDERING;
    public static final int initialValueType = COLOR_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new ColorType (PropNames.BACKGROUND_COLOR, "transparent");
    }

    public static final int inherited = NO;

}

