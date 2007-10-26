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

import java.util.Iterator;

import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Generic shorthand parser for ListProperties
 */
public class GenericShorthandParser implements ShorthandParser {

    /**
     * Constructor. 
     */
    public GenericShorthandParser() {
    }

    /**
     * @param list  the ListProperty
     * @param index the index into the List of properties
     * @return the property from the List of properties at the index parameter
     */
    protected Property getElement(Property list, int index) {
        if (list.getList().size() > index) {
            return (Property) list.getList().get(index);
        } else {
            return null;
        }
    }
    
    /**
     * {@inheritDoc} 
     */
    public Property getValueForProperty(int propId,
                                        Property property,
                                        PropertyMaker maker,
                                        PropertyList propertyList)
                    throws PropertyException {
        // Check for keyword "inherit"
        if (property.getList().size() == 1) {
            String sval = getElement(property, 0).getString();
            if (sval != null && sval.equals("inherit")) {
                return propertyList.getFromParent(propId);
            }
        }
        return convertValueForProperty(propId, property, maker, propertyList);
    }


    /**
     * Converts a property name into a Property
     * @param propId the property ID in the Constants interface
     * @param maker the Property.Maker to be used in the conversion
     * @param property ...
     * @param propertyList the PropertyList from which the Property should be
     * extracted
     * @return the Property matching the parameters, or null if not found
     * @throws PropertyException (when?)
     */
    protected Property convertValueForProperty(int propId,
                                               Property property,
                                               PropertyMaker maker,
                                               PropertyList propertyList)
                    throws PropertyException {
        Property prop = null;
        // Try each of the stored values in turn
        Iterator iprop = property.getList().iterator();
        while (iprop.hasNext() && prop == null) {
            Property p = (Property)iprop.next();
            prop = maker.convertShorthandProperty(propertyList, p, null);
        }
        return prop;
    }

}

