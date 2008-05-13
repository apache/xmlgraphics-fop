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
import org.apache.fop.fo.FOValidationEventProducer;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.expr.PropertyException;

import java.util.Set;

/**
 * Exists primarily as a container for its Maker inner class, which is
 * extended by many string-based FO property classes.
 */
public final class StringProperty extends Property {

    /**
     * Inner class for making instances of StringProperty
     */
    public static class Maker extends PropertyMaker {

        /**
         * @param propId the id of the property for which a Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * Make a new StringProperty object
         * @param propertyList not used
         * @param value String value of the new object
         * @param fo not used
         * @return the StringProperty object
         */
        public Property make(PropertyList propertyList, String value,
                             FObj fo) {
            // Work around the fact that most String properties are not
            // specified as actual String literals (with "" or '') since
            // the attribute values themselves are Strings!
            // If the value starts with ' or ", make sure it also ends with
            // this character
            // Otherwise, just take the whole value as the String
            int vlen = value.length() - 1;
            if (vlen > 0) {
                char q1 = value.charAt(0);
                if (q1 == '"' || q1 == '\'') {
                    if (value.charAt(vlen) == q1) {
                        return new StringProperty(value.substring(1, vlen));
                    }
                    log.warn("String-valued property starts with quote"
                                       + " but doesn't end with quote: "
                                       + value);
                    // fall through and use the entire value, including first quote
                }
                String str = checkValueKeywords(value);
                if (str != null) {
                    value = str;
                }
            }
            return StringProperty.getInstance(value);
        }

    }

    /**
     * Inner class dedicated to the "id" property, which should provide a random
     * unique identifier as an initial value.
     * The values for "id" are never cached, as they're typically valid for one
     * document.
     */
    public static class IdMaker extends PropertyMaker {

        /**
         * @param propId    the id of the property for which the maker should be created
         */
        public IdMaker(int propId) {
            super(propId);
        }

        /** {@inheritDoc} */
        public Property make(PropertyList propertyList) throws PropertyException {
            String newId = "FO_";
            newId += propertyList.getFObj().getFOEventHandler().getNextId();
            return new StringProperty(newId);
        }
        
        /** {@inheritDoc} */
        public Property make(PropertyList propertyList, 
                             String value,
                             FObj fo) throws PropertyException {
            
            Property idProp;
            
            //no parsing necessary; just return a new StringProperty
            //TODO: Should we move validation here? (see FObj#checkId())
            if ("".equals(value)) {
                //if an empty string was specified, return the default
                idProp = this.make(propertyList);
            } else {
                idProp = new StringProperty(value);
            }
            
            return idProp;
        }
    }

    /** cache containing all canonical StringProperty instances */
    private static final PropertyCache cache = new PropertyCache(StringProperty.class);
    
    /** canonical instance for empty strings */
    public static final StringProperty EMPTY_STRING_PROPERTY = new StringProperty("");
    
    private final String str;

    /**
     * Constructor
     * @param str String value to place in this object
     */
    private StringProperty(String str) {
        this.str = str;
    }

    /**
     * Return the canonical StringProperty instance 
     * corresponding to the given string value
     * @param str   the base String
     * @return  the canonical instance
     */
    public static StringProperty getInstance(String str) {
        if ("".equals(str) || str == null) {
            return EMPTY_STRING_PROPERTY;
        } else {
            return (StringProperty)cache.fetch(
                       new StringProperty(str));
        }
    }
    
    /** @return the Object equivalent of this property */
    public Object getObject() {
        return this.str;
    }

    /** @return the String equivalent of this property */
    public String getString() {
        return this.str;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof StringProperty) {
            StringProperty sp = (StringProperty)obj;
            return (sp.str == this.str
                    || sp.str.equals(this.str));
        }
        return false;
    }
    
    /** {@inheritDoc} */
    public int hashCode() {
        return str.hashCode();
    }
}
