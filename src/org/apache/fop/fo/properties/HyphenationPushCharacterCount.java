package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class HyphenationPushCharacterCount extends Property  {
    public static final int dataTypes = NUMBER | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = NUMBER_IT;
    public /*static*/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new Numeric (PropNames.HYPHENATION_PUSH_CHARACTER_COUNT, 2d);
    }

    public static final int inherited = COMPUTED;
}

