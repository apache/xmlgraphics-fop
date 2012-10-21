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

import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.util.CompareUtil;

/**
 * A length quantity in XSL which is specified as an enum, such as "auto"
 */
public class EnumLength extends LengthProperty {
    private Property enumProperty;

    /**
     * Construct an enumerated length from an enum property.
     * @param enumProperty the enumeration property
     */
    public EnumLength(Property enumProperty) {
        this.enumProperty = enumProperty;
    }

    /**
     * {@inheritDoc}
     */
    public int getEnum() {
        return enumProperty.getEnum();
    }

    /** @return true if absolute */
    public boolean isAbsolute() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int getValue() {
        log.error("getValue() called on " + enumProperty + " length");
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public int getValue(PercentBaseContext context) {
        log.error("getValue() called on " + enumProperty + " length");
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public double getNumericValue() {
        log.error("getNumericValue() called on " + enumProperty + " number");
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public double getNumericValue(PercentBaseContext context) {
        log.error("getNumericValue() called on " + enumProperty + " number");
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public String getString() {
        return enumProperty.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Object getObject() {
        return enumProperty.getObject();
    }

    @Override
    public int hashCode() {
        return enumProperty.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EnumLength)) {
            return false;
        }
        EnumLength other = (EnumLength) obj;
        return CompareUtil.equal(enumProperty, other.enumProperty);
    }
}
