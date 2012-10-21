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

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.CompareUtil;

/**
 * A number quantity in XSL which is specified as an enum, such as "no-limit".
 */
public final class EnumNumber extends Property implements Numeric {

    /** cache holding all canonical EnumNumber instances */
    private static final PropertyCache<EnumNumber> CACHE
            = new PropertyCache<EnumNumber>();

    private final EnumProperty enumProperty;

    /**
     * Constructor
     * @param enumProperty  the base EnumProperty
     */
    private EnumNumber(Property enumProperty) {
        this.enumProperty = (EnumProperty) enumProperty;
    }

    /**
     * Returns the canonical EnumNumber instance corresponding
     * to the given Property
     *
     * @param enumProperty  the base EnumProperty
     * @return  the canonical instance
     */
    public static EnumNumber getInstance(Property enumProperty) {
        return CACHE.fetch(new EnumNumber((EnumProperty) enumProperty));
    }

    /** {@inheritDoc} */
    public int getEnum() {
        return enumProperty.getEnum();
    }

    /** {@inheritDoc} */
    public String getString() {
        return enumProperty.toString();
    }

    /** {@inheritDoc} */
    public Object getObject() {
        return enumProperty.getObject();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EnumNumber)) {
            return false;
        }
        EnumNumber other = (EnumNumber) obj;
        return CompareUtil.equal(enumProperty, other.enumProperty);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return enumProperty.hashCode();
    }

    /** {@inheritDoc} */
    public int getDimension() {
        return 0;
    }

    /**
     * {@inheritDoc}
     * Always <code>true</code> for instances of this type
     */
    public boolean isAbsolute() {
        return true;
    }

    /**
     * {@inheritDoc}
     * logs an error, because it's not supposed to be called
     */
    public double getNumericValue(PercentBaseContext context) throws PropertyException {
        log.error("getNumericValue() called on " + enumProperty + " number");
        return 0;
    }

    /**
     * {@inheritDoc}
     * logs an error, because it's not supposed to be called
     */
    public int getValue(PercentBaseContext context) {
        log.error("getValue() called on " + enumProperty + " number");
        return 0;
    }

    /**
     * {@inheritDoc}
     * logs an error, because it's not supposed to be called
     */
    public int getValue() {
        log.error("getValue() called on " + enumProperty + " number");
        return 0;
    }

    /**
     * {@inheritDoc}
     * logs an error, because it's not supposed to be called
     */
    public double getNumericValue() {
        log.error("getNumericValue() called on " + enumProperty + " number");
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public Numeric getNumeric() {
        return this;
    }

}
