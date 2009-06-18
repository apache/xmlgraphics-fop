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

import org.apache.fop.datatypes.Auto;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.indirect.FromNearestSpecified;
import org.apache.fop.datatypes.indirect.FromParent;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.expr.PropertyException;

public class Margin extends Property  {
    public static final int dataTypes = SHORTHAND;

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


    /**
     * 'value' is a PropertyValueList or an individual PropertyValue.
     *
     * <p>If 'value' is an individual PropertyValue, it must contain
     * either
     *   a FromParent value,
     *   a FromNearestSpecified value,
     *   an Inherit value,
     *   an Auto value,
     *   a Numeric value which is a distance, rather than a number.
     *
     * <p>If 'value' is a PropertyValueList, it contains a list of
     * 2 to 4 length, percentage or auto values representing margin
     * dimensions.
     *
     * <p>The value(s) provided, if valid, are converted into a list
     * containing the expansion of the shorthand.
     * The first element is a value for margin-top,
     * the second element is a value for margin-right,
     * the third element is a value for margin-bottom,
     * the fourth element is a value for margin-left.
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
        if ( ! (value instanceof PropertyValueList)) {
            if (value instanceof Inherit
                || value instanceof FromParent
                || value instanceof FromNearestSpecified
                )
                return refineExpansionList(PropNames.MARGIN, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));
            // N.B.  Does this require further refinement?
            // Where is Auto expanded?
            return refineExpansionList(PropNames.MARGIN, foNode,
                        ShorthandPropSets.expandAndCopySHand
                                                (autoOrDistance(value)));
        } else {
            PropertyValueList list =
                            spaceSeparatedList((PropertyValueList)value);
            PropertyValue top, left, bottom, right;
            int count = list.size();
            if (count < 2 || count > 4)
                throw new PropertyException
                    ("margin list contains " + count + " items");

            Iterator margins = list.iterator();

            // There must be at least two
            top = autoOrDistance
                ((PropertyValue)(margins.next()), PropNames.MARGIN_TOP);
            right = autoOrDistance
                ((PropertyValue)(margins.next()), PropNames.MARGIN_RIGHT);
            try {
                bottom = (PropertyValue)(top.clone());
                bottom.setProperty(PropNames.MARGIN_BOTTOM);
                left = (PropertyValue)(right.clone());
                left.setProperty(PropNames.MARGIN_LEFT);
            } catch (CloneNotSupportedException cnse) {
                throw new PropertyException
                            (cnse.getMessage());
            }

            if (margins.hasNext())
                bottom = autoOrDistance((PropertyValue)(margins.next()),
                                                PropNames.MARGIN_BOTTOM);
            if (margins.hasNext())
                left = autoOrDistance((PropertyValue)(margins.next()),
                                                PropNames.MARGIN_LEFT);

            list = new PropertyValueList(PropNames.MARGIN);
            list.add(top);
            list.add(right);
            list.add(bottom);
            list.add(left);
            return list;
        }
    }

    /**
     * @param value <tt>PropertyValue</tt> the value being tested
     * @param property <tt>int</tt> property index of returned value
     * @return <tt>PropertyValue</t> the same value, with its property set
     *  to the <i>property</i> argument, if it is an Auto or a
     * <tt>Numeric</tt> distance
     * @exception PropertyException if the conditions are not met
     */
    private static PropertyValue autoOrDistance
                                    (PropertyValue value, int property)
        throws PropertyException
    {
        if (value instanceof Auto ||
            value instanceof Numeric && ((Numeric)value).isDistance()) {
            value.setProperty(property);
            return value;
        }
        else throw new PropertyException
            ("Value not 'Auto' or a distance for "
                + PropNames.getPropertyName(value.getProperty()));
    }

    /**
     * @param value <tt>PropertyValue</tt> the value being tested
     * @return <tt>PropertyValue</t> the same value if it is an Auto or a
     * <tt>Numeric</tt> distance
     * @exception PropertyException if the conditions are not met
     */
    private static PropertyValue autoOrDistance(PropertyValue value)
        throws PropertyException
    {
        return autoOrDistance(value, value.getProperty());
    }
}

