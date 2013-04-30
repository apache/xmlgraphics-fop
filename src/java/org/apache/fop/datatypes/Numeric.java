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

package org.apache.fop.datatypes;

/**
 * An interface for classes that can participate in numeric operations.
 * All the numeric operation (+, -, *, ...) are expressed in terms of
 * this Numeric interface.
 * Numerics has a value (getNumericValue) and a dimension (getDimension).
 * Numerics can be either absolute or relative. Relative numerics
 * must be resolved against base value before the value can be used.
 * <p>
 * To support relative numerics internally in the expression parser and
 * during evaluation one additional methods exists: isAbsolute() which
 * return true for absolute numerics and false for relative numerics.
 */
public interface Numeric {
    /**
     * Return the value of this Numeric
     * @return the computed value.
     * @throws PropertyException if a property exception occurs
     */
    double getNumericValue();

    /**
     * Return the value of this Numeric
     * @param context The context for the length calculation (for percentage based lengths)
     * @return the computed value.
     * @throws PropertyException if a property exception occurs
     */
    double getNumericValue(PercentBaseContext context);

    /**
     * Return the dimension of this numeric. Plain numbers has a dimension of
     * 0 and length has a dimension of 1. Other dimension can occur as a result
     * of multiplications and divisions.
     * @return the dimension.
     */
    int getDimension();

    /**
     * Return true if the numeric is an absolute value. Relative values are
     * percentages and table-column-units. All other numerics are absolute.
     * @return true when the numeric is absolute.
     */
    boolean isAbsolute();

    /**
     * Returns the value of this numeric as an int.
     * @return the value as an integer.
     */
    int getValue();

    /**
     * Returns the value of this numeric as an int.
     * @param context the context for the length calculation (for percentage based lengths)
     * @return the value as an integer.
     */
    int getValue(PercentBaseContext context);

    /**
     * Return the resolved value. This method will be called during evaluation
     * of the expression tree and relative numerics can then return a
     * resolved absolute Numeric. Absolute numerics can just return themselves.
     *
     * @return A resolved value.
     * @throws PropertyException
     */
    //Numeric getResolved() throws PropertyException;

    /**
     * Return the enum value that is stored in this numeric.
     * @return the enum value
     */
    int getEnum();
}
