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

public class Padding extends Property  {
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
    public PropertyValue refineParsing
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

