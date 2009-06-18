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

/* $Id: $ */

package org.apache.fop.afp.modca;

/**
 * MO:DCA Interchange Set
 */
public class InterchangeSet {
    /** interchange set 1 string value */
    public static final String MODCA_PRESENTATION_INTERCHANGE_SET_1 = "MO:DCA-P IS/1";
    
    /** interchange set 2 string value */
    public static final String MODCA_PRESENTATION_INTERCHANGE_SET_2 = "MO:DCA-P IS/2";
    
    /** resource interchange set string value */
    public static final String MODCA_RESOURCE_INTERCHANGE_SET = "MO:DCA-L";

    private static final String[] NAMES = {
        MODCA_PRESENTATION_INTERCHANGE_SET_1,
        MODCA_PRESENTATION_INTERCHANGE_SET_2,
        MODCA_RESOURCE_INTERCHANGE_SET
    };

    private static final int SET_1 = 0;
    private static final int SET_2 = 1;
    private static final int RESOURCE_SET = 2;

    /** the actual interchange set in use */
    private int value;

    /**
     * Returns the interchange set value of a given string
     * 
     * @param str an interchange set value
     * @return an interchange set
     */
    public static InterchangeSet valueOf(String str) {
        if (MODCA_PRESENTATION_INTERCHANGE_SET_1.equals(str)) {
            return new InterchangeSet(SET_1);
        } else if (MODCA_PRESENTATION_INTERCHANGE_SET_2.equals(str)) {
            return new InterchangeSet(SET_2);
        } else if (MODCA_RESOURCE_INTERCHANGE_SET.equals(str)) {
            return new InterchangeSet(RESOURCE_SET);
        } else {
            throw new IllegalArgumentException("Invalid MO:DCA interchange set :" + str);
        }
    }

    /**
     * Main constructor
     * 
     * @param value the interchange set value
     */
    public InterchangeSet(int value) {
        this.value = value;
    }

    /**
     * Returns true if complies with MOD:CA interchange set 1
     * 
     * @return true if complies with MOD:CA interchange set 1
     */
    protected boolean is1() {
        return value == SET_1;
    }
    
    /**
     * Returns true if complies with MOD:CA interchange set 2
     * 
     * @return true if complies with MOD:CA interchange set 2
     */
    public boolean is2() {
        return value == SET_2;
    }
    
    /**
     * Returns true if complies with MOD:CA resource set
     * 
     * @return true if complies with MOD:CA resource set
     */
    public boolean isResource() {
        return value == RESOURCE_SET;
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return NAMES[value];
    }
    
    /**
     * Returns true if MOD:CA interchange set 2 (resource groups) is supported
     * 
     * @return true if MOD:CA interchange set 2 (resource groups) is supported
     */
    public boolean supportsLevel2() {
        return is2() || isResource();
    }
}
