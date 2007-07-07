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

/**
 * A number quantity in XSL which is specified as an enum, such as "no-limit".
 */
public class EnumNumber extends NumberProperty {

    /** cache holding all canonical EnumNumber instances */
    private static final PropertyCache cache = new PropertyCache();

    private final EnumProperty enumProperty;
    
    /**
     * Constructor
     * @param enumProperty  the base EnumProperty
     */
    private EnumNumber(Property enumProperty) {
        super(null);
        this.enumProperty = (EnumProperty) enumProperty;
    }

    /**
     * Returns the canonical EnumNumber instance corresponding
     * to the given Property
     * @param enumProperty  the base EnumProperty
     * @return  the canonical instance
     */
    public static EnumNumber getInstance(Property enumProperty) {
        return (EnumNumber)cache.fetch(
                new EnumNumber((EnumProperty) enumProperty));
    }

    public int getEnum() {
        return enumProperty.getEnum();
    }

    /**
     * Returns the length in 1/1000ths of a point (millipoints)
     * @return the length in millipoints
     */
    public int getValue() {
        log.error("getValue() called on " + enumProperty + " number");
        return 0;
    }

    /**
     * Returns the value as numeric.
     * @return the length in millipoints
     */
    public double getNumericValue() {
        log.error("getNumericValue() called on " + enumProperty + " number");
        return 0;
    }

    /**
     * @see org.apache.fop.fo.properties.Property#getString()
     */
    public String getString() {
        return enumProperty.toString();
    }

    /**
     * @see org.apache.fop.fo.properties.Property#getString()
     */
    public Object getObject() {
        return enumProperty.getObject();
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof EnumNumber) {
            return (((EnumNumber)obj).enumProperty == this.enumProperty);
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return enumProperty.hashCode();
    }
    
}
