package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.datastructs.ROStringArray;

public class NoProperty extends Property  {
    // dataTypes was set to ANY_TYPE.  This meant that any property
    // type would be valid with NoProperty.  It caused problems with
    // initialization looking for complex().  I cannot now see the
    // rationale for such a setting.  Resetting to NOTYPE.
    // pbw 23/01/02
    public static final int dataTypes = NOTYPE;
    public static final int traitMapping = NO_TRAIT;
    public static final int initialValueType = NOTYPE_IT;

    public static final int inherited = NO;

    private static final String[] rwEnums = {
	null
	,"-----NoEnum-----"
    };
}

