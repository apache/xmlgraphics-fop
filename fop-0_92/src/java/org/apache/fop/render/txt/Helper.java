/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.fop.render.txt;

/**
 * This class has a few convenient static methods for number quantization.
 */
public final class Helper {

    /**
     * Don't let anyone instantiate this class.
     */
    private Helper() { }

    /**
     * Returns nearest integer to <code>x</code>, divisible by 
     * <code>quantum</code>. 
     * 
     * @param x integer for quantization
     * @param quantum integer, representing quantization
     * @return computed nearest integer
     */
    public static int round(int x, int quantum) {
        int ceil = ceil(x, quantum);
        int floor = floor(x, quantum);
        return (ceil - x < x - floor) ? ceil : floor;
    }

    /**
     * Returns minimal possible integer, greater or equal than 
     * <code>x</code>, divisible by <code>quantum</code>.
     *         
     * @param x integer for quantization
     * @param quantum integer, representing quantization
     * @return computed nearest integer
     */
    public static int ceil(int x, int quantum) {
        int dx = (x < 0) || (x % quantum == 0) ? 0 : 1;
        return (x / quantum + dx) * quantum;
    }

    /**
     * Returns maximum possible integer, less or equal than
     * <code>oldValue</code>, divisible by <code>quantum</code>.
     * 
     * @param x integer for quantization
     * @param quantum integer, representing quantization
     * @return computed nearest integer
     */
    public static int floor(int x, int quantum) {
        int dx = (x > 0) || (x % quantum == 0) ? 0 : -1;
        return (x / quantum + dx) * quantum;
    }

    /**
     * Returns the closest integer to <code>x/y</code> fraction.
     * It's possible to consider this methos as a analog of Math.round(x/y), 
     * without having deal with non-integer.
     * 
     * @param x integer, fraction numerator
     * @param y  integer, fraction denominator
     * @return the value of the fraction rounded to the nearest
     * @see java.lang.Math#round(double)
     */
    public static int roundPosition(int x, int y) {
        return round(x, y) / y;
    }

    /**
     * Returns the smallest integer that is greater than or equal to the 
     * <code>x/y</code> fraction.
     * It's possible to consider this function as a analog of Math.ceil(x/y), 
     * without having deal with non-integer.
     * 
     * @param x integer, fraction numerator
     * @param y  integer, fraction denominator
     * @return the smallest integer that is greater than or equal to 
     *         <code>x/y</code> fraction
     * @see java.lang.Math#ceil(double)
     */
    public static int ceilPosition(int x, int y) {
        return ceil(x, y) / y;
    }
    
    
    /**
     * Returns the largest integer that is less than or equal to the
     * argument and is equal to <code>x/y</code> fraction.
     * It's possible to consider this function as a analog of Math.floor(x/y), 
     * without having deal with non-integer.
     * 
     * @param x integer, fraction numerator
     * @param y integer, fraction denominator
     * @return the largest integer that is less than or equal to 
     *            the argument and is equal to <code>x/y</code> fraction
     * @see java.lang.Math#ceil(double)
     */
    public static int floorPosition(int x, int y) {
        return floor(x, y) / y;
    }
}
