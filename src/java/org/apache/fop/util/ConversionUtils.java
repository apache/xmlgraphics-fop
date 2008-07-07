/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 
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
 * This class contains utility methods for conversions, like
 * a java.lang.String to an array of int or double.
 */
public final class ConversionUtils {

    /**
     * Converts the given base <code>String</code> into
     * an array of <code>int</code>, splitting the base along the
     * given separator pattern.
     * <em>Note: this method assumes the input is a string containing 
     * only decimal integers, signed or unsigned, that are parsable
     * by <code>java.lang.Integer.parseInt(String)</code>. If this
     * is not the case, the resulting <code>NumberFormatException</code>
     * will have to be handled by the caller.</em>
     * 
     * @param baseString    the base string
     * @param separatorPattern  the pattern separating the integer values
     *        (if this is <code>null</code>, the baseString is parsed as one
     *         integer value) 
     * @return  an array of <code>int</code> whose size is equal to the number
     *          values in the input string; <code>null</code> if this number
     *          is equal to zero.
     */
    public static int[] toIntArray(String baseString, String separatorPattern) {
        
        if (baseString == null || "".equals(baseString)) {
            return null;
        }
        
        if (separatorPattern == null || "".equals(separatorPattern)) {
            return new int[] { Integer.parseInt(baseString) };
        }
        
        String[] values = baseString.split(separatorPattern);
        int numValues = values.length;
        if (numValues == 0) {
            return null;
        }
        
        int[] returnArray = new int[numValues];
        for (int i = 0; i < numValues; ++i) {
            returnArray[i] = Integer.parseInt(values[i]);
        }
        return returnArray;
        
    }

    /**
     * Converts the given base <code>String</code> into
     * an array of <code>double</code>, splitting the base along the
     * given separator pattern.
     * <em>Note: this method assumes the input is a string containing 
     * only decimal doubles, signed or unsigned, that are parsable
     * by <code>java.lang.Double.parseDouble(String)</code>. If this
     * is not the case, the resulting <code>NumberFormatException</code>
     * will have to be handled by the caller.</em>
     * 
     * @param baseString    the base string
     * @param separatorPattern  the pattern separating the integer values
     *        (if this is <code>null</code>, the baseString is parsed as one
     *         double value) 
     * @return  an array of <code>double</code> whose size is equal to the number
     *          values in the input string; <code>null</code> if this number
     *          is equal to zero.
     */
    public static double[] toDoubleArray(String baseString, String separatorPattern) {
        
        if (baseString == null || "".equals(baseString)) {
            return null;
        }
        
        if (separatorPattern == null || "".equals(separatorPattern)) {
            return new double[] { Double.parseDouble(baseString) };
        }
        
        String[] values = baseString.split(separatorPattern);
        int numValues = values.length;
        if (numValues == 0) {
            return null;
        }
        
        double[] returnArray = new double[numValues];
        for (int i = 0; i < numValues; ++i) {
            returnArray[i] = Double.parseDouble(values[i]);
        }
        return returnArray;
        
    }
    
}
