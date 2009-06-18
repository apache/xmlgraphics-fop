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

import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.expr.PropertyException;

public class Border extends Property  {
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


    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
        throws PropertyException
    {
        int type = value.getType();
        if (type == PropertyValue.INHERIT ||
                type == PropertyValue.FROM_PARENT ||
                    type == PropertyValue.FROM_NEAREST_SPECIFIED)
            // Copy the value to each member of the shorthand expansion
            return refineExpansionList(PropNames.BORDER, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));

        PropertyValueList ssList = null;
        // Must be a space-separated list or a single value from the
        // set of choices
        if (type != PropertyValue.LIST) {
            // If it's a single value, form a list from that value
            ssList = new PropertyValueList(PropNames.BORDER);
            ssList.add(value);
        } else {
            // Must be a space-separated list
            try {
                ssList = spaceSeparatedList((PropertyValueList)value);
            } catch (PropertyException e) {
                throw new PropertyException
                    ("Space-separated list required for 'border'");
            }
        }
        // Look for appropriate values in ssList
        PropertyValue width = null;
        PropertyValue style = null;
        PropertyValue color = null;
        Iterator values = ssList.iterator();
        while (values.hasNext()) {
            PropertyValue val = (PropertyValue)(values.next());
            PropertyValue pv = null;
            try {
                pv = PropertyConsts.pconsts.refineParsing
                        (PropNames.BORDER_WIDTH, foNode, val, IS_NESTED);
                if (width != null)
                    logger.info("border: duplicate" +
                    "width overrides previous width");
                width = pv;
                continue;
            } catch (PropertyException e) {}
            try {
                pv = PropertyConsts.pconsts.refineParsing
                            (PropNames.BORDER_STYLE, foNode, val, IS_NESTED);
                if (style != null)
                    logger.info("border: duplicate" +
                    "style overrides previous style");
                style = pv;
                continue;
            } catch (PropertyException e) {}
            try {
                pv = PropertyConsts.pconsts.refineParsing
                            (PropNames.BORDER_COLOR, foNode, val, IS_NESTED);
                if (color != null)
                    logger.info("border: duplicate" +
                    "color overrides previous color");
                color = pv;
                continue;
            } catch (PropertyException e) {}

            throw new PropertyException
                ("Unrecognized value; looking for style, "
                + "width or color in border: "
                + val.getClass().getName());
        }

        // Construct the shorthand expansion list
        // Only those elements which are actually specified fint their
        // way into this list.  Other elements will take their normally
        // inherited or initial values.
        PropertyValueList borderexp =
                                new PropertyValueList(PropNames.BORDER);
        if (style != null)
            borderexp.addAll((PropertyValueList)style);
        if (color != null)
            borderexp.addAll((PropertyValueList)color);
        if (width != null)
            borderexp.addAll((PropertyValueList)width);
        return borderexp;
    }
}

