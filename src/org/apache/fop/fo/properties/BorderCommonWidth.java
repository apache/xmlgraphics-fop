package org.apache.fop.fo.properties;

import org.apache.fop.fo.properties.Property;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Pseudo-property class for common border width values occurring in a
 * number of classes.
 */
public class BorderCommonWidth extends Property  {
    public static final int THIN = 1;
    public static final int MEDIUM = 2;
    public static final int THICK = 3;

    private static final String[] rwEnums = {
	null
	,"thin"
	,"medium"
	,"thick"
    };

    private static final double[] mappedPoints = {
	0d
	,0.5d
	,1d
	,2d
    };
    
    // N.B. If these values change, all initial values expressed in these
    // terms must be manually changed.

    /**
     * @param <tt>int</tt> property index
     * @param <tt>int</tt> mappedEnum enumeration value
     * @return <tt>Numeric[]</tt> containing the values corresponding
     * to the MappedNumeric enumeration constants for border width
     */
    public Numeric getMappedLength(FONode node, int property, int enum)
	throws PropertyException
    {
	return 
	    Length.makeLength(property, mappedPoints[enum], Length.PT);
    }

    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }

}

