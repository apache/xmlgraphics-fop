package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Angle;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class GlyphOrientationHorizontal extends Property  {
    public static final int dataTypes = ANGLE | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = ANGLE_IT;
    public /*static*/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new Angle
		    (PropNames.GLYPH_ORIENTATION_HORIZONTAL, Angle.DEG, 0d);
    }
    public static final int inherited = COMPUTED;
}

