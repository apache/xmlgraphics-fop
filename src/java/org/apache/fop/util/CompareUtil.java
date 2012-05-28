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

package org.apache.fop.util;

/**
 * A utility class that provides helper methods for implementing equals and hashCode.
 */
public final class CompareUtil {

    private CompareUtil() {
    }

    /**
     * Compares two objects for equality.
     *
     * @param o1 an object
     * @param o2 another object
     * @return true if either o1 and o2 are null or if o1.equals(o2)
     */
    public static boolean equal(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1 == o2 || o1.equals(o2);
    }

    /**
     * Returns the hash code of the given object.
     *
     * @param object an object
     * @return object.hashCode(), or 0 if object is null
     */
    public static int getHashCode(Object object) {
        return object == null ? 0 : object.hashCode();
    }

    /**
     * Compares two numbers for equality. Uses the same comparison algorithm as
     * the {@link Double#equals(Object)} method.
     *
     * @param n1 a number
     * @param n2 another number
     * @return true if the two numbers are equal, false otherwise
     */
    public static boolean equal(double n1, double n2) {
        return Double.doubleToLongBits(n1) == Double.doubleToLongBits(n2);
    }

    /**
     * Returns a hash code for the given number. Applies the same algorithm as
     * the {@link Double#hashCode()} method.
     *
     * @param number a number
     * @return a hash code for that number
     */
    public static int getHashCode(double number) {
        long bits = Double.doubleToLongBits(number);
        return (int) (bits ^ (bits >>> 32));
    }

}
