package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.properties.Property;

import java.util.Map;
import java.util.Iterator;

public class BorderColor extends Property {
    public static final int dataTypes = SHORTHAND;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = NOTYPE_IT;
    public static final int inherited = NO;


    /**
     * 'value' is a PropertyValueList or an individual PropertyValue.
     *
     * <p>If 'value' is an individual PropertyValue, it must contain
     * either
     *   a ColorType value
     *   a NCName containing a standard color name or 'transparent'
     *   a FromParent value,
     *   a FromNearestSpecified value,
     *   or an Inherit value.
     *
     * <p>If 'value' is a PropertyValueList, it contains a list of
     * 2 to 4 ColorType values or NCName enum tokens representing colors.
     *
     * <p>The value(s) provided, if valid, are converted into a list
     * containing the expansion of the shorthand.
     * The first element is a value for border-top-color,
     * the second element is a value for border-right-color,
     * the third element is a value for border-bottom-color,
     * the fourth element is a value for border-left-color.
     *
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @return <tt>PropertyValue</tt> the verified value
     */
    public /*static*/ PropertyValue refineParsing
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
    public /*static*/ PropertyValue refineParsing
        (int propindex, FONode foNode, PropertyValue value, boolean nested)
                throws PropertyException
    {
        int type = value.getType();
        if ( ! (value instanceof PropertyValueList)) {
            if ( ! nested) {
                if (type == PropertyValue.INHERIT ||
                        type == PropertyValue.FROM_PARENT ||
                            type == PropertyValue.FROM_NEAREST_SPECIFIED)
                    return refineExpansionList(PropNames.BORDER_COLOR, foNode,
                            ShorthandPropSets.expandAndCopySHand(value));
            }
            if (type == PropertyValue.COLOR_TYPE)
                return refineExpansionList(PropNames.BORDER_COLOR, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));
            if (type == PropertyValue.NCNAME) {
                // Must be a standard color
                ColorType color;
                try {
                    color = new ColorType(PropNames.BORDER_COLOR,
                                        ((NCName)value).getNCName());
                } catch (PropertyException e) {
                    throw new PropertyException
                        (((NCName)value).getNCName() +
                            " not a standard color for border-color");
                }
                return refineExpansionList(PropNames.BORDER_COLOR, foNode,
                                ShorthandPropSets.expandAndCopySHand(color));
            }
            else throw new PropertyException
                ("Invalid " + value.getClass().getName() +
                                            " value for border-color");
        } else {
            if (nested) throw new PropertyException
                    ("PropertyValueList invalid for nested border-color "
                        + "refineParsing() method");
            // List may contain only multiple color specifiers
            // i.e. ColorTypes or NCNames specifying a standard color or
            // 'transparent'.
            PropertyValueList list =
                            spaceSeparatedList((PropertyValueList)value);
            ColorType top, left, bottom, right;
            int count = list.size();
            if (count < 2 || count > 4)
                throw new PropertyException
                    ("border-color list contains " + count + " items");

            Iterator colors = list.iterator();

            // There must be at least two
            top = getColor((PropertyValue)(colors.next()));
            right = getColor((PropertyValue)(colors.next()));
            try {
                bottom = (ColorType)(top.clone());
                left = (ColorType)(right.clone());
            } catch (CloneNotSupportedException cnse) {
                throw new PropertyException
                                ("clone() not supported on ColorType");
            }

            if (colors.hasNext()) bottom
                            = getColor((PropertyValue)(colors.next()));
            if (colors.hasNext()) left
                            = getColor((PropertyValue)(colors.next()));

            // Set the properties for each
            top.setProperty(PropNames.BORDER_TOP_COLOR);
            right.setProperty(PropNames.BORDER_RIGHT_COLOR);
            bottom.setProperty(PropNames.BORDER_BOTTOM_COLOR);
            left.setProperty(PropNames.BORDER_LEFT_COLOR);

            list = new PropertyValueList(PropNames.BORDER_COLOR);
            list.add(top);
            list.add(right);
            list.add(bottom);
            list.add(left);
            // Question: if less than four colors have been specified in
            // the shorthand, what border-?-color properties, if any,
            // have been specified?
            return list;
        }
    }

    /**
     * Return the ColorType derived from the argument.
     * The argument must be either a ColorType already, in which case
     * it is returned unchanged, or an NCName whose string value is a
     * standard color or 'transparent'.
     * @param value <tt>PropertyValue</tt>
     * @return <tt>ColorValue</tt> equivalent of the argument
     * @exception <tt>PropertyException</tt>
     */
    private static ColorType getColor(PropertyValue value)
            throws PropertyException
    {
        int property = value.getProperty();
        int type = value.getType();
        if (type == PropertyValue.COLOR_TYPE) return (ColorType)value;
        // Must be a color enum
        if (type != PropertyValue.NCNAME)
            throw new PropertyException
                (value.getClass().getName() + " instead of color for "
                                + PropNames.getPropertyName(property));
        // We have an NCName - hope it''s a color
        NCName ncname = (NCName)value;
        try {
            return new ColorType(property, ncname.getNCName());
        } catch (PropertyException e) {
            throw new PropertyException
                        (ncname.getNCName() + " instead of color for "
                                + PropNames.getPropertyName(property));
        }
    }
}

