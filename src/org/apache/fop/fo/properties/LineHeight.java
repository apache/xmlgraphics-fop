package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Ems;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.Property;

public class LineHeight extends Property  {
    public static final int dataTypes =
        COMPOUND| PERCENTAGE | LENGTH | NUMBER | MAPPED_LENGTH | INHERIT;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = NOTYPE_IT;
    public static final int NORMAL = 1;
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"normal"
    };

    public Numeric getMappedLength(FONode node, int enum)
        throws PropertyException
    {
        if (enum != NORMAL)
            throw new PropertyException("Invalid MAPPED_LENGTH enum: "
                                        + enum);
        return Ems.makeEms(node, PropNames.LINE_HEIGHT, 1.2d); // normal
    }
}

