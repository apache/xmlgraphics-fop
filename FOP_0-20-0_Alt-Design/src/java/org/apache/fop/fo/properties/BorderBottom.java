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

import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

public class BorderBottom extends BorderAbsoluteShorthand  {
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
     *
     * @param propindex index of the property
     * @param foNode on which this property value is expressed
     * @param value of the property expression parsed in the previous stages
     * of property expression evaluation
     * @return the refined and expanded value
     */
    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
                throws PropertyException
    {
        int property = PropNames.BORDER_BOTTOM;
        if (property != propindex) // DEBUG
            throw new PropertyException
                ("Mismatch between propindex and BORDER_BOTTOM.");
        return borderEdge(property, foNode, value,
                                PropNames.BORDER_BOTTOM_STYLE,
                                PropNames.BORDER_BOTTOM_COLOR,
                                PropNames.BORDER_BOTTOM_WIDTH
                                );
    }
}

