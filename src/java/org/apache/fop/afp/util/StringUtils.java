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

package org.apache.fop.afp.util;

/**
 * Library of utility methods useful in dealing with strings.
 *
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Padds the string to the left with the given character for
     * the specified length.
     * @param input The input string.
     * @param padding The char used for padding.
     * @param length The length of the new string.
     * @return The padded string.
     */
    public static String lpad(String input, char padding, int length) {

        if (input == null) {
            input = new String();
        }

        if (input.length() >= length) {
            return input;
        } else {
            StringBuffer result = new StringBuffer();
            int numChars = length - input.length();
            for (int i = 0; i < numChars; i++) {
                result.append(padding);
            }
            result.append(input);
            return result.toString();
        }
    }

    /**
     * Padds the string to the right with the given character for
     * the specified length.
     * @param input The input string.
     * @param padding The char used for padding.
     * @param length The length of the new string.
     * @return The padded string.
     */
    public static String rpad(String input, char padding, int length) {

        if (input == null) {
            input = new String();
        }

        if (input.length() >= length) {
            return input;
        } else {
            StringBuffer result = new StringBuffer(input);
            int numChars = length - input.length();
            for (int i = 0; i < numChars; i++) {
                result.append(padding);
            }
            return result.toString();
        }
    }

}
