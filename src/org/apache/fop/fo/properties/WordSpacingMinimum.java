package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.properties.WordSpacingCommon;

public class WordSpacingMinimum extends WordSpacingCommon  {
    public static final int dataTypes = LENGTH;
    public static final int traitMapping = DISAPPEARS;
    public static final int initialValueType = LENGTH_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return getMappedLength(null, NORMAL); // null implies initial value
    }
    public static final int inherited = COMPUTED;

    public Numeric getMappedLength(FONode node, int enum)
        throws PropertyException
    {
        if (enum != NORMAL)
            throw new PropertyException("Invalid MAPPED_LENGTH enum: "
                                        + enum);
        return Length.makeLength
                            (PropNames.WORD_SPACING_MINIMUM, 0d, Length.PT);
    }
}

