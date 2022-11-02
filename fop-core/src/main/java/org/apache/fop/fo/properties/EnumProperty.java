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
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.CompareUtil;

/**
 * Superclass for properties that wrap an enumeration value
 */
public final class EnumProperty extends Property {

    /** cache holding all canonical EnumProperty instances */
    private static final PropertyCache<EnumProperty> CACHE
            = new PropertyCache<EnumProperty>();

    /**
     * Inner class for creating EnumProperty instances
     */
    public static class Maker extends PropertyMaker {

        /**
         * @param propId the id of the property for which a Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * Called by subclass if no match found.
         * @param value string containing the value to be checked
         * @return null (indicates that an appropriate match was not found)
         */
        public Property checkEnumValues(String value) {
            //log.error("Unknown enumerated value for property '"
            //                       + getPropName() + "': " + value);
            return super.checkEnumValues(value);
        }

        /**
         * Convert a property.
         * @param p the property to convert
         * @param propertyList the property list to use in conversion
         * @param fo the FO to use in conversion
         * @return the converted property
         * @throws PropertyException if a property conversion exception occurs
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList,
                                        FObj fo) throws PropertyException {
            if (p instanceof EnumProperty) {
                return p;
            } else {
                return super.convertProperty(p, propertyList, fo);
            }
        }
    }

    private final int value;
    private final String text;

    /**
     * @param explicitValue enumerated value to be set for this property
     * @param text the string value of the enum.
     */
    private EnumProperty(int explicitValue, String text) {
        this.value = explicitValue;
        this.text = text;
    }

    /**
     * Construct an enumeration property.
     * @param explicitValue the value
     * @param text the text
     * @return an enumeration property
     */
    public static EnumProperty getInstance(int explicitValue, String text) {
        return CACHE.fetch(new EnumProperty(explicitValue, text));
    }

    /**
     * @return this.value
     */
    public int getEnum() {
        return this.value;
    }

    /**
     * @return this.value cast as an Object
     */
    public Object getObject() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnumProperty) {
            EnumProperty ep = (EnumProperty)obj;
            return this.value == ep.value
                    && CompareUtil.equal(text, ep.text);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return value + text.hashCode();
    }
}

