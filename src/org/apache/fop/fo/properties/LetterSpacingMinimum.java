package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class LetterSpacingMinimum extends Property  {
    public static final int dataTypes = LENGTH;
    public static final int traitMapping = DISAPPEARS;
    public static final int initialValueType = LENGTH_IT;
    public static final int NORMAL = 1;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return Length.makeLength
                    (PropNames.LETTER_SPACING_MINIMUM, 0d, Length.PT);
    }
    public static final int inherited = COMPUTED;
}

