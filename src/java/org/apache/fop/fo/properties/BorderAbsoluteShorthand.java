/*
 *
 * Copyright 2004 The Apache Software Foundation.
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
 * Created on 20/04/2004
 * $Id$
 */
package org.apache.fop.fo.properties;

import java.util.Iterator;

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.MappedNumeric;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.expr.PropertyException;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class BorderAbsoluteShorthand extends Property {
    /**
     * Superclass for Border{AbsoluteEdge} shorthands.
     */
    public BorderAbsoluteShorthand() {}

    /**
     * Border shorthand expression parsing.
     * 
     * <p>'value' is a <code>PropertyValueList</code> or an individual
     * <code>PropertyValue</code>.
     * If 'value' is a <code>PropertyValueList</code>, it must contain a single
     * <code>PropertyValueList</code>, which in turn contains the individual
     * elements.
     *
     * <p>'value' can contain a parsed <code>Inherit</code> value,
     *  parsed <code>FromParent value</code>, parsed
     * <code>FromNearestSpecified</code> value,  or, in any order;
     * 
     * <dl>
     * <dt>border-width</dt>
     *     <dd>a parsed NCName value containing a standard border width
     *     or a Numeric length value (including a percentage)</dd>
     * <dt>border-style</dt>
     *     <dl>a parsed NCName value containing a standard border style</dl>
     * <dt>border-color</dt>
     *     <dl>a parsed ColorType value, or an NCName containing one of
     *     the standard colors</dl>
     * </dl>
     * <p>The value(s) provided, if valid, are converted into a list
     * containing the expansion of the shorthand.  The elements may
     * be in any order.  A minimum of one value will be present.
     * <ul>
     * <li>a border-EDGE-color <code>ColorType</code> or inheritance value</li>
     * <li>a border-EDGE-style <code>EnumType</code> or inheritance value</li>
     * <li>a border-EDGE-width <code>MappedNumeric</code> or inheritance
     * value</li>
     * </ul>
     *  <p>N.B. this is the order of elements defined in
     *       <code>ShorthandPropSets.borderRightExpansion</code>
     * @param propindex index of the property
     * @param foNode on which this property value is expressed
     * @param value of the property expression parsed in the previous stages
     * of property expression evaluation
     * @param styleProp index of the associated style property
     * @param colorProp index of the associated color property
     * @param widthProp index of the associated width property
     * @return the refined and expanded value
     * @throws PropertyException
     */
    protected PropertyValue borderEdge
                        (int propindex, FONode foNode, PropertyValue value,
                                int styleProp, int colorProp, int widthProp)
                throws PropertyException
    {
        return borderEdge(propindex, foNode, value, styleProp,
                                            colorProp, widthProp, NOT_NESTED);
    }

    protected PropertyValue borderEdge
            (int propindex, FONode foNode, PropertyValue value, int styleProp,
                                int colorProp, int widthProp, boolean nested)
                throws PropertyException
    {
        if (value.getType() != PropertyValue.LIST) {
            return processEdgeValue(propindex, foNode, value,
                                    styleProp, colorProp, widthProp, nested);
        } else {
            return processEdgeList(propindex, foNode,
                        spaceSeparatedList((PropertyValueList)value),
                                            styleProp, colorProp, widthProp);
        }
    }

    private PropertyValueList processEdgeValue
            (int propindex, FONode foNode, PropertyValue value, int styleProp,
                int colorProp, int widthProp, boolean nested)
            throws PropertyException
    {
        if ( ! nested) {
            int type = value.getType();
            if (type == PropertyValue.INHERIT ||
                    type == PropertyValue.FROM_PARENT ||
                        type == PropertyValue.FROM_NEAREST_SPECIFIED)
            {
                // Copy the value to each member of the shorthand expansion
                return refineExpansionList(propindex, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));
            }
        }
        // Make a list and pass to processList
        PropertyValueList tmpList = new PropertyValueList(propindex);
        tmpList.add(value);
        return processEdgeList
                (propindex, foNode, tmpList, styleProp, colorProp, widthProp);
    }

    private PropertyValueList processEdgeList
            (int property, FONode foNode, PropertyValueList value,
                                int styleProp, int colorProp, int widthProp)
                    throws PropertyException
    {
        //int property = value.getProperty();
        String propName = PropNames.getPropertyName(property);
        PropertyValue   color= null,
                        style = null,
                        width = null;

        PropertyValueList newlist = new PropertyValueList(property);
        // This is a list
        if (value.size() == 0)
            throw new PropertyException
                            ("Empty list for " + propName);
        Iterator elements = (value).iterator();

        scanning_elements: while (elements.hasNext()) {
            PropertyValue pval = (PropertyValue)(elements.next());
            int type = pval.getType();
            switch (type) {
            case PropertyValue.COLOR_TYPE:
                if (color != null) logger.info(propName +
                            ": duplicate color overrides previous color");
                color = pval;
                color.setProperty(colorProp);
                continue scanning_elements;
            case PropertyValue.NUMERIC:
                if (width != null) logger.info(propName +
                            ": duplicate width overrides previous width");
                width = pval;
                width.setProperty(widthProp);
                continue scanning_elements;
            case PropertyValue.NCNAME:
                // Could be standard color, style Enum or width MappedNumeric
                PropertyValue colorFound = null;
                PropertyValue styleFound = null;
                PropertyValue widthFound = null;

                String ncname = ((NCName)pval).getNCName();
                try {
                    styleFound = new EnumType(styleProp, ncname);
                } catch (PropertyException e) {}
                if (styleFound != null) {
                    if (style != null) logger.info(propName +
                            ": duplicate style overrides previous style");
                    style = styleFound;
                    continue scanning_elements;
                }

                try {
                    widthFound =
                        (new MappedNumeric
                            (foNode, widthProp, ncname)).getMappedNumValue();
                } catch (PropertyException e) {}
                if (widthFound != null) {
                    if (width != null) logger.info(propName +
                            ": duplicate width overrides previous width");
                    width = widthFound;
                    continue scanning_elements;
                }

                try {
                    colorFound = new ColorType(colorProp, ncname);
                } catch (PropertyException e) {}
                if (colorFound != null) {
                    if (color != null) logger.info(propName +
                            ": duplicate color overrides previous color");
                    color = colorFound;
                    continue scanning_elements;
                }

                throw new PropertyException
                    ("Unknown NCName value for " + propName + ": " + ncname);
            default:
                throw new PropertyException
                    ("Invalid " + pval.getClass().getName() +
                        " property value for " + propName);
            } // end of switch
        }

        // Now construct the list of PropertyValues with their
        // associated property indices, as expanded from the
        // border-right shorthand.
        if (style != null) newlist.add(style);
        if (color != null) newlist.add(color);
        if (width != null) newlist.add(width);
        return newlist;
    }
    
    
}
