package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Auto;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.datatypes.indirect.FromParent;
import org.apache.fop.datatypes.indirect.FromNearestSpecified;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.Property;

public class Position extends Property  {
    public static final int dataTypes = SHORTHAND | ENUM | INHERIT;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = ENUM_IT;
    public static final int STATIC = 1;
    public static final int RELATIVE = 2;
    public static final int ABSOLUTE = 3;
    public static final int FIXED = 4;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType(PropNames.POSITION, STATIC);
    }
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"static"
        ,"relative"
        ,"absolute"
        ,"fixed"
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
        if (value instanceof Inherit |
                value instanceof FromParent |
                    value instanceof FromNearestSpecified)
        {
            return refineExpansionList(PropNames.POSITION, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));
        }
        if (value instanceof NCName) {
            EnumType enum = null;
            String ncname = ((NCName)value).getNCName();
            try {
                enum = new EnumType(value.getProperty(), ncname);
            } catch (PropertyException e) {
                throw new PropertyException
                ("Unrecognized NCName in position: " + ncname);
            }
            PropertyValueList list =
                        new PropertyValueList(PropNames.POSITION);
            switch (enum.getEnumValue()) {
            case STATIC:
                list.add(new EnumType
                            (PropNames.RELATIVE_POSITION, "static"));
                list.add(new Auto(PropNames.ABSOLUTE_POSITION));
                return list;
            case RELATIVE:
                list.add(new EnumType
                            (PropNames.RELATIVE_POSITION, "relative"));
                list.add(new Auto(PropNames.ABSOLUTE_POSITION));
                return list;
            case ABSOLUTE:
                list.add(new EnumType
                            (PropNames.RELATIVE_POSITION, "static"));
                list.add(new EnumType
                            (PropNames.ABSOLUTE_POSITION, "absolute"));
                return list;
            case FIXED:
                list.add(new EnumType
                            (PropNames.RELATIVE_POSITION, "static"));
                list.add(new EnumType
                            (PropNames.ABSOLUTE_POSITION, "fixed"));
                return list;
            }
        }

        throw new PropertyException
            ("Invalid value for 'position': "
                + value.getClass().getName());
    }
    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

