/*
 * Copyright 2004 The Apache Software Foundation.
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

/**
 * A number quantity in XSL which is specified as an enum, such as "no-limit".
 */
public class EnumNumber extends NumberProperty {
    private Property enumProperty;
    
    public EnumNumber(Property enumProperty) {
        super(null);
        this.enumProperty = enumProperty;
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


}
