/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * Shorthand property parser for Box rounded corner properties
 */
public class BoxCornerPropShorthandParser extends GenericShorthandParser {

    /**
     * Default constructor.
     */
    public BoxCornerPropShorthandParser() {
    }

    /**
     * Stores 1 or 2 values of same type representing rounded corner radii.
     * If 2 value are present the first is the corner radius in the IP direction,
     * the second in the BP direction.
     * {@inheritDoc}
     */
    @Override
    protected Property convertValueForProperty(int propId,
                                               Property property,
                                               PropertyMaker maker,
                                               PropertyList propertyList)
                throws PropertyException {
        String name = FOPropertyMapping.getPropertyName(propId);
        Property p = null;
        int count = property.getList().size();

        if (name.indexOf("border-start") > -1 || name.indexOf("border-end") > -1) {
            p = getElement(property, 0);
        } else if (name.indexOf("border-before") > -1 || name.indexOf("border-after") > -1) {
            p = getElement(property, count > 1 ? 1 : 0);
        }

        // if p not null, try to convert it to a value of the correct type
        if (p != null) {
            return maker.convertShorthandProperty(propertyList, p, null);
        }
        return p;
    }

}
