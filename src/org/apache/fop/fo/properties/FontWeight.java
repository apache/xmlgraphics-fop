package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.IntegerType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.Property;

public class FontWeight extends Property  {
    public static final int dataTypes = INTEGER | ENUM | INHERIT;
    public static final int traitMapping = FONT_SELECTION;
    public static final int initialValueType = INTEGER_IT;
    public static final int NORMAL = 1;
    public static final int BOLD = 2;
    public static final int BOLDER = 3;
    public static final int LIGHTER = 4;

    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new IntegerType(PropNames.FONT_WEIGHT, 400);
    }

    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"normal"
        ,"bold"
        ,"bolder"
        ,"lighter"
    };

    /*
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @return <tt>PropertyValue</tt> the verified value
     */
    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
                    throws PropertyException
    {
        return refineParsing(propindex, foNode, value, NOT_NESTED);
    }

    /**
     * Do the work for the three argument refineParsing method.
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @param nested <tt>boolean</tt> indicating whether this method is
     * called normally (false), or as part of another <i>refineParsing</i>
     * method.
     * @return <tt>PropertyValue</tt> the verified value
     * @see #refineParsing(FONode,PropertyValue)
     */
    public PropertyValue refineParsing
        (int propindex, FONode foNode, PropertyValue value, boolean nested)
                    throws PropertyException
    {
        // Override the shadowed method to ensure that Integer values
        // are limited to the valid numbers
        PropertyValue fw =
                    super.refineParsing(propindex, foNode, value, nested);
        // If the result is an IntegerType, restrict the values
        if (fw instanceof IntegerType) {
            int weight = ((IntegerType)fw).getInt();
            if (weight % 100 != 0 || weight < 100 || weight > 900)
                throw new PropertyException
                    ("Invalid integer font-weight value: " + weight);
        }
        return fw;
    }
    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

