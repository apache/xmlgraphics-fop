package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class WordSpacing extends Property  {
    public static final int dataTypes =
                            COMPOUND | LENGTH | MAPPED_LENGTH | INHERIT;
    public static final int traitMapping = DISAPPEARS;
    public static final int initialValueType = LENGTH_IT;
    public static final int NORMAL = 1;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return getMappedLength(NORMAL); //normal
    }
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"normal"
    };

    public static Numeric getMappedLength(int enum)
        throws PropertyException
    {
        if (enum != NORMAL)
            throw new PropertyException("Invalid MAPPED_LENGTH enum: "
                                        + enum);
        return Length.makeLength(PropNames.WORD_SPACING, 0d, Length.PT);
    }
}

