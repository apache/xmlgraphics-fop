package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.datatypes.indirect.FromParent;
import org.apache.fop.datatypes.indirect.FromNearestSpecified;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.Property;

import java.util.Iterator;

public class Padding extends Property  {
    public static final int dataTypes = SHORTHAND;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = NOTYPE_IT;
    public static final int inherited = NO;

    /**
     * 'value' is a PropertyValueList or an individual PropertyValue.
     *
     * <p>If 'value' is an individual PropertyValue, it must contain
     * either
     *   a FromParent value,
     *   a FromNearestSpecified value,
     *   an Inherit value,
     *   a Numeric value which is a distance, rather than a number.
     *
     * <p>If 'value' is a PropertyValueList, it contains a list of
     * 2 to 4 length or percentage values representing padding
     * dimensions.
     *
     * <p>The value(s) provided, if valid, are converted into a list
     * containing the expansion of the shorthand.
     * The first element is a value for padding-top,
     * the second element is a value for padding-right,
     * the third element is a value for padding-bottom,
     * the fourth element is a value for padding-left.
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
        if ( ! (value instanceof PropertyValueList)) {
            if (value instanceof Inherit
                || value instanceof FromParent
                || value instanceof FromNearestSpecified
                || (value instanceof Numeric
                        && ((Numeric)value).isDistance())
                )
                return refineExpansionList(PropNames.PADDING, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));
            throw new PropertyException
                ("Invalid property value for 'padding': "
                    + value.getClass().getName());
        } else {
            PropertyValueList list =
                            spaceSeparatedList((PropertyValueList)value);
            Numeric top, left, bottom, right;
            int count = list.size();
            if (count < 2 || count > 4)
                throw new PropertyException
                    ("padding list contains " + count + " items");

            Iterator paddings = list.iterator();

            // There must be at least two
            top = (Numeric)(paddings.next());
            right = (Numeric)(paddings.next());
            try {
                bottom = (Numeric)(top.clone());
                left = (Numeric)(right.clone());
            } catch (CloneNotSupportedException cnse) {
                throw new PropertyException
                            (cnse.getMessage());
            }

            if (paddings.hasNext())
                bottom = (Numeric)(paddings.next());
            if (paddings.hasNext())
                left = (Numeric)(paddings.next());

            if ( ! (top.isDistance() & right.isDistance()
                    & bottom.isDistance() && left.isDistance()))
                throw new PropertyException
                    ("Values for 'padding' must be distances");
            list = new PropertyValueList(PropNames.PADDING);
            top.setProperty(PropNames.PADDING_TOP);
            list.add(top);
            right.setProperty(PropNames.PADDING_RIGHT);
            list.add(right);
            bottom.setProperty(PropNames.PADDING_BOTTOM);
            list.add(bottom);
            left.setProperty(PropNames.PADDING_LEFT);
            list.add(left);
            return list;
        }
    }

}

