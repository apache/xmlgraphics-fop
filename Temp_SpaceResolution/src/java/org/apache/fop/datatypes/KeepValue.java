/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.fop.datatypes;

/**
 * Keep Value
 * Stores the different types of keeps in a single convenient format.
 */
public class KeepValue {
    /** constant for keep-with-always */
    public static final String KEEP_WITH_ALWAYS = "KEEP_WITH_ALWAYS";
    /** constant for automatic keep-with computation */
    public static final String KEEP_WITH_AUTO = "KEEP_WITH_AUTO";
    /** constant for a user-settable keep-with value (??) */
    public static final String KEEP_WITH_VALUE = "KEEP_WITH_VALUE";
    private String type = KEEP_WITH_AUTO;
    private int value = 0;

    /**
     * Constructor
     * @param type one of "KEEP_WITH_ALWAYS", "KEEP_WITH_AUTO", or
     * "KEEP_WITH_VALUE"
     * @param val keep-with value to use (used only by KEEP_WITH_VALUE ??).
     */
    public KeepValue(String type, int val) {
        this.type = type;
        this.value = val;
    }

    /**
     * @return the keep-with value
     */
    public int getValue() {
        return value;
    }

    /**
     * @return the descriptive type
     */
    public String getType() {
        return type;
    }

    /**
     * @return string representation of this
     */
    public String toString() {
        return type;
    }

}
