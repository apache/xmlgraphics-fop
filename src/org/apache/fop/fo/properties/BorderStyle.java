package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.Property;

import java.util.Map;
import java.util.Iterator;

public class BorderStyle extends Property  {
    public static final int dataTypes = SHORTHAND;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = NOTYPE_IT;
    public static final int inherited = NO;

    /**
     * 'value' is a PropertyValueList or an individual PropertyValue.
     *
     * <p>If 'value' is an individual PropertyValue, it must contain
     * either
     *   a NCName containing a border-style name
     *   a FromParent value,
     *   a FromNearestSpecified value,
     *   or an Inherit value.
     *
     * <p>If 'value' is a PropertyValueList, it contains a list of
     * 2 to 4 NCName enum tokens representing border-styles.
     *
     * <p>The value(s) provided, if valid, are converted into a list
     * containing the expansion of the shorthand.
     * The first element is a value for border-top-style,
     * the second element is a value for border-right-style,
     * the third element is a value for border-bottom-style,
     * the fourth element is a value for border-left-style.
     *
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
        int type = value.getType();
        if (type != PropertyValue.LIST) {
            if ( ! nested) {
                if (type == PropertyValue.INHERIT ||
                        type == PropertyValue.FROM_PARENT ||
                            type == PropertyValue.FROM_NEAREST_SPECIFIED)
                    return refineExpansionList(PropNames.BORDER_STYLE, foNode,
                            ShorthandPropSets.expandAndCopySHand(value));
            }
            if (type == PropertyValue.NCNAME) {
                // Must be a border-style
                EnumType enum;
                try {
                    enum = new EnumType(PropNames.BORDER_STYLE,
                                        ((NCName)value).getNCName());
                } catch (PropertyException e) {
                    throw new PropertyException
                        (((NCName)value).getNCName() +
                                                " not a border-style");
                }
                return refineExpansionList(PropNames.BORDER_STYLE, foNode,
                                ShorthandPropSets.expandAndCopySHand(enum));
            }
            else throw new PropertyException
                ("Invalid " + value.getClass().getName() +
                                            " value for border-style");
        } else {
            if (nested) throw new PropertyException
                    ("PropertyValueList invalid for nested border-style "
                        + "refineParsing() method");
            // List may contain only multiple style specifiers
            // i.e. NCNames specifying a standard style
            PropertyValueList list =
                            spaceSeparatedList((PropertyValueList)value);
            EnumType top, left, bottom, right;
            int count = list.size();
            if (count < 2 || count > 4)
                throw new PropertyException
                    ("border-style list contains " + count + " items");

            Iterator styles = list.iterator();

            // There must be at least two
            top = getEnum((PropertyValue)(styles.next()),
                                PropNames.BORDER_TOP_STYLE , "style");
            right = getEnum((PropertyValue)(styles.next()),
                                PropNames.BORDER_RIGHT_STYLE, "style");
            try {
                bottom = (EnumType)(top.clone());
                bottom.setProperty(PropNames.BORDER_BOTTOM_STYLE);
                left = (EnumType)(right.clone());
                left.setProperty(PropNames.BORDER_LEFT_STYLE);
            } catch (CloneNotSupportedException cnse) {
                throw new PropertyException
                            ("clone() not supported on EnumType");
            }

            if (styles.hasNext()) bottom
                    = getEnum((PropertyValue)(styles.next()),
                                PropNames.BORDER_BOTTOM_STYLE, "style");
            if (styles.hasNext()) left
                    = getEnum((PropertyValue)(styles.next()),
                                PropNames.BORDER_LEFT_STYLE, "style");

            list = new PropertyValueList(PropNames.BORDER_STYLE);
            list.add(top);
            list.add(right);
            list.add(bottom);
            list.add(left);
            // Question: if less than four styles have been specified in
            // the shorthand, what border-?-style properties, if any,
            // have been specified?
            return list;
        }
    }

}

