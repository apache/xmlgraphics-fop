package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Literal;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class HyphenationCharacter extends Property  {
    public static final int dataTypes = CHARACTER_T | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = LITERAL_IT;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new Literal(PropNames.HYPHENATION_CHARACTER, "\u2010");
    }
    public static final int inherited = COMPUTED;
}

