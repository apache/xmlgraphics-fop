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

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

/**
 * Property class for the font-family property.
 */
public class FontFamilyProperty extends ListProperty {

    /**
     * Inner class for creating instances of ListProperty
     */
    public static class Maker extends PropertyMaker {

        /**
         * @param propId ID of the property for which Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * @see PropertyMaker#convertProperty
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo) {
            if (p instanceof FontFamilyProperty) {
                return p;
            } else {
                return new FontFamilyProperty(p);
            }
        }

    }

    /**
     * @param prop the first Property to be added to the list
     */
    public FontFamilyProperty(Property prop) {
        super();
        addProperty(prop);
    }

    /**
     * Add a new property to the list
     * @param prop Property to be added to the list
     */
    public void addProperty(Property prop) {
        if (prop.getList() != null) {
            list.addAll(prop.getList());
        } else {
            super.addProperty(prop);
        }
    }

    /** @see org.apache.fop.fo.properties.Property#getString() */
    public String getString() {
        if (list.size() > 0) {
            Property first = (Property)list.get(0);
            return first.getString();
        } else {
            return super.getString();
        }
    }

}
