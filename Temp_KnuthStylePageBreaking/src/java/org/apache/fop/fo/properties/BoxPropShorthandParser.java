/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.properties;

import org.apache.fop.fo.FOPropertyMapping;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Shorthand property parser for Box properties
 */
public class BoxPropShorthandParser extends GenericShorthandParser {

    /**
     * @see org.apache.fop.fo.GenericShorthandParser#GenericShorthandParser()
     */
    public BoxPropShorthandParser() {
    }

    /**
     * Stores 1 to 4 values of same type.
     * Set the given property based on the number of values set.
     * Example: padding, border-width, border-color, border-style, margin
     * @see org.apache.fop.fo.GenericShorthandParser#convertValueForProperty(
     * int, ListProperty, PropertyMaker, PropertyList)
     */
    protected Property convertValueForProperty(int propId,
                                               Property property,
                                               PropertyMaker maker,
                                               PropertyList propertyList)
                throws PropertyException {
        String name = FOPropertyMapping.getPropertyName(propId);
        Property p = null;
        int count = property.getList().size();
        if (name.indexOf("-top") >= 0) {
            p = getElement(property, 0);
        } else if (name.indexOf("-right") >= 0) {
            p = getElement(property, count > 1 ? 1 : 0);
        } else if (name.indexOf("-bottom") >= 0) {
            p = getElement(property, count > 2 ? 2 : 0);
        } else if (name.indexOf("-left") >= 0) {
            p = getElement(property, count > 3 ? 3 : (count > 1 ? 1 : 0));
        }
        // if p not null, try to convert it to a value of the correct type
        if (p != null) {
            return maker.convertShorthandProperty(propertyList, p, null);
        }
        return p;
    }

}
