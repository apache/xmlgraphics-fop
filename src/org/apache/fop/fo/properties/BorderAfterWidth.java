package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.BorderCommonWidth;

public class BorderAfterWidth extends BorderCommonWidth {
    public static final int dataTypes =
                            COMPOUND | MAPPED_LENGTH | LENGTH | INHERIT;
    public static final int traitMapping = FORMATTING | RENDERING;
    public static final int initialValueType = LENGTH_IT;

    // Initial value for BorderAfterWidth is tne mapped enumerated value
    // "medium".  This maps to 1pt.  There is no way at present to
    // automatically update the following initial Length PropertyValue
    // if the mapping changes.

    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return getMappedLength(MEDIUM);
    }

    public static Numeric getMappedLength(int enum)
        throws PropertyException
    {
        return getMappedLength (PropNames.BORDER_AFTER_WIDTH, enum);
    }

    public static final int inherited = NO;
    

}

