/*
 * $Id$
 *
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */
package org.apache.fop.fo.properties;

import java.util.Iterator;

import org.apache.fop.datatypes.MappedNumeric;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.expr.PropertyException;

public class BorderWidth extends BorderCommonWidth {
    // Below is a special case defying the general rule that shorthands do
    // not require specific data type settings.  This one is neded for the
    // MappedNumeric generataion in checkBorderWidth().
    public static final int dataTypes = MAPPED_LENGTH | SHORTHAND;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = SHORTHAND_MAP;

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


    /** The <tt>FONode</tt> on which this property is defined. */
    private FONode foNode;

    /**
     * 'value' is a PropertyValueList or an individual PropertyValue.
     *
     * <p>If 'value' is an individual PropertyValue, it must contain
     * either
     *   a NCName containing a border-width name
     *   a FromParent value,
     *   a FromNearestSpecified value,
     *   or an Inherit value.
     *
     * <p>If 'value' is a PropertyValueList, it contains a
     * PropertyValueList which in turn contains a list of
     * 2 to 4 NCName enumval tokens or Length values representing border-widths.
     *
     * <p>The value(s) provided, if valid, are converted into a list
     * containing the expansion of the shorthand.
     * The first element is a value for border-top-width,
     * the second element is a value for border-right-width,
     * the third element is a value for border-bottom-width,
     * the fourth element is a value for border-left-width.
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
     * Do the work for the two argument refineParsing method.
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @param nested <tt>boolean</tt> indicating whether this method is
     * called normally (false), or as part of another <i>refineParsing</i>
     * method.
     * @return <tt>PropertyValue</tt> the verified value
     */
    public PropertyValue refineParsing
        (int propindex, FONode foNode, PropertyValue value, boolean nested)
                throws PropertyException
    {
        this.foNode = foNode;
        int type = value.getType();
        if (type != PropertyValue.LIST) {
            if ( ! nested) {
                if (type == PropertyValue.INHERIT ||
                        type == PropertyValue.FROM_PARENT ||
                            type == PropertyValue.FROM_NEAREST_SPECIFIED)
                    return refineExpansionList(PropNames.BORDER_WIDTH, foNode,
                            ShorthandPropSets.expandAndCopySHand(value));
            }

            return refineExpansionList
                    (PropNames.BORDER_WIDTH, foNode,
                        ShorthandPropSets.expandAndCopySHand
                         (checkBorderWidth(PropNames.BORDER_WIDTH, value)
                          )
                     );
        } else {
            if (nested) throw new PropertyException
                    ("PropertyValueList invalid for nested border-width "
                        + "refineParsing() method");
            // List may contain only multiple width specifiers
            // i.e. NCNames specifying a standard width or Numeric
            // length values
            PropertyValueList list =
                            spaceSeparatedList((PropertyValueList)value);
            Numeric top, left, bottom, right;
            int count = list.size();
            if (count < 2 || count > 4)
                throw new PropertyException
                    ("border-width list contains " + count + " items");

            Iterator widths = list.iterator();

            // There must be at least two
            top = checkBorderWidth
                            (PropNames.BORDER_TOP_WIDTH,
                                             ((PropertyValue)widths.next()));
            right = checkBorderWidth
                            (PropNames.BORDER_RIGHT_WIDTH,
                                             ((PropertyValue)widths.next()));
            try {
                bottom = (Numeric)(top.clone());
                bottom.setProperty(PropNames.BORDER_BOTTOM_WIDTH);
                left = (Numeric)(right.clone());
                left.setProperty(PropNames.BORDER_LEFT_WIDTH);
            } catch (CloneNotSupportedException cnse) {
                throw new PropertyException
                            ("clone() not supported on Numeric");
            }

            if (widths.hasNext())
                bottom = checkBorderWidth
                            (PropNames.BORDER_BOTTOM_WIDTH,
                                             ((PropertyValue)widths.next()));
            if (widths.hasNext())
                left = checkBorderWidth
                            (PropNames.BORDER_LEFT_WIDTH,
                                             ((PropertyValue)widths.next()));

            list = new PropertyValueList(PropNames.BORDER_WIDTH);
            list.add(top);
            list.add(right);
            list.add(bottom);
            list.add(left);
            // Question: if less than four widths have been specified in
            // the shorthand, what border-?-width properties, if any,
            // have been specified?
            return list;
        }
    }

    /**
     * Attempt to convert the <tt>PropertyValue</tt> into a length.
     * This may not be necessary, as it may be possible to pass
     * unconverted values directly to the <i>refineExpansionList()</i>
     * method.
     * @param property the property idex.
     * @param value the property value being converted.
     */
    private Numeric checkBorderWidth(int property, PropertyValue value)
        throws PropertyException
    {
        switch (value.getType()) {
        case PropertyValue.NUMERIC:
            Numeric length = (Numeric)value;
            if (length.isAbsOrRelLength())
                return length;
            // else fall through to throw exception
            break;
        case PropertyValue.NCNAME:
            return (new MappedNumeric
                        (foNode, property, ((NCName)value).getNCName())
                    ).getMappedNumValue();
        }
        throw new PropertyException("Invalid border-width value: " + value);
    }

    /**
     * Get mapped numeric length.  This may not be necessary.  It may be
     * feasible to simply pass the unrefined values to
     * <i>refineExpansionList()</i>.
     * @param node the node for which the mapped length is being
     * derived.
     * @param enumval the enumval value which is being mapped to a length.
     */
    public Numeric getMappedLength(FONode node, int enumval)
        throws PropertyException
    {
        return getMappedLength(node, PropNames.BORDER_WIDTH, enumval);
    }

}

