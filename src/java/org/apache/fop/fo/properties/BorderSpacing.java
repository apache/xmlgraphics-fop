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

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.expr.PropertyException;

public class BorderSpacing extends Property  {
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
     * Legal values are:
     *   an Inherit PropertyValue
     *   a FromParent PropertyValue
     *   a FromNearestSpecified PropertyValue
     *   a Length PropertyValue
     *   a list containing 2 Length PropertyValues
     *   Note: the Lengths cannot be percentages (what about relative
     *         lengths?)
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
        int type = value.getType();
        if (type != PropertyValue.LIST) {
            if (type == PropertyValue.INHERIT ||
                    type == PropertyValue.FROM_PARENT ||
                        type == PropertyValue.FROM_NEAREST_SPECIFIED)
                return refineExpansionList(PropNames.BORDER_SPACING, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));

            if (type == PropertyValue.NUMERIC &&
                                            ((Numeric)value).isLength())
                return refineExpansionList(PropNames.BORDER_SPACING, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));

            throw new PropertyException
                ("Invalid " + value.getClass().getName() +
                    " object for border-spacing");
        } else {
            // Must be a pair of Lengths
            PropertyValueList list =
                            spaceSeparatedList((PropertyValueList)value);
            if (list.size() != 2)
                throw new PropertyException
                    ("List of " + list.size() + " for border-spacing");
            PropertyValue len1 = (PropertyValue)(list.getFirst());
            int len1type = len1.getType();
            PropertyValue len2 = (PropertyValue)(list.getLast());
            int len2type = len2.getType();
            // Note that this test excludes (deliberately) ems relative
            // lengths.  I don't know whether this exclusion is valid.
            if ( !
                (len1type == PropertyValue.NUMERIC && len2type == len1type
                    && ((Numeric)len1).isLength()
                    && ((Numeric)len2).isLength()
                )
            )
                throw new PropertyException
                    ("Values to border-spacing are not both Lengths");
            // Set the individual expanded properties of the
            // border-separation compound property
            // Should I clone these values?
            len1.setProperty
                (PropNames.BORDER_SEPARATION_BLOCK_PROGRESSION_DIRECTION);
            len2.setProperty
                (PropNames.BORDER_SEPARATION_INLINE_PROGRESSION_DIRECTION);
            return value;
        }
    }
}

