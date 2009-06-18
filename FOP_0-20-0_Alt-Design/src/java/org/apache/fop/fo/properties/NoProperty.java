package org.apache.fop.fo.properties;


public class NoProperty extends Property  {
    // dataTypes was set to ANY_TYPE.  This meant that any property
    // type would be valid with NoProperty.  It caused problems with
    // initialization looking for complex().  I cannot now see the
    // rationale for such a setting.  Resetting to NOTYPE.
    // pbw 23/01/02
    public static final int dataTypes = NOTYPE;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = NO_TRAIT;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = NOTYPE_IT;

    public int getInitialValueType() {
        return initialValueType;
    }


    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }


    private static final String[] rwEnums = {
	null
	,"-----NoEnum-----"
    };
}

