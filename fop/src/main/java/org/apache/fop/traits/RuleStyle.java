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

package org.apache.fop.traits;

import java.io.ObjectStreamException;

import org.apache.fop.fo.Constants;

/** Enumeration class for rule styles. */
public final class RuleStyle extends TraitEnum {

    private static final long serialVersionUID = 1L;

    private static final String[] RULE_STYLE_NAMES = new String[]
            {"none", "dotted", "dashed",
             "solid", "double", "groove", "ridge"};

    private static final int[] RULE_STYLE_VALUES = new int[]
            {Constants.EN_NONE, Constants.EN_DOTTED, Constants.EN_DASHED,
             Constants.EN_SOLID, Constants.EN_DOUBLE, Constants.EN_GROOVE, Constants.EN_RIDGE};

    /** rule-style: none */
    public static final RuleStyle NONE = new RuleStyle(0);
    /** rule-style: dotted */
    public static final RuleStyle DOTTED = new RuleStyle(1);
    /** rule-style: dashed */
    public static final RuleStyle DASHED = new RuleStyle(2);
    /** rule-style: solid */
    public static final RuleStyle SOLID = new RuleStyle(3);
    /** rule-style: double */
    public static final RuleStyle DOUBLE = new RuleStyle(4);
    /** rule-style: groove */
    public static final RuleStyle GROOVE = new RuleStyle(5);
    /** rule-style: ridge */
    public static final RuleStyle RIDGE = new RuleStyle(6);

    private static final RuleStyle[] STYLES = new RuleStyle[] {
        NONE, DOTTED, DASHED, SOLID, DOUBLE, GROOVE, RIDGE};

    private RuleStyle(int index) {
        super(RULE_STYLE_NAMES[index], RULE_STYLE_VALUES[index]);
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param name the name of the enumeration value
     * @return the enumeration object
     */
    public static RuleStyle valueOf(String name) {
        for (int i = 0; i < STYLES.length; i++) {
            if (STYLES[i].getName().equalsIgnoreCase(name)) {
                return STYLES[i];
            }
        }
        throw new IllegalArgumentException("Illegal rule style: " + name);
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param enumValue the enumeration value
     * @return the enumeration object
     */
    public static RuleStyle valueOf(int enumValue) {
        for (int i = 0; i < STYLES.length; i++) {
            if (STYLES[i].getEnumValue() == enumValue) {
                return STYLES[i];
            }
        }
        throw new IllegalArgumentException("Illegal rule style: " + enumValue);
    }

    private Object readResolve() throws ObjectStreamException {
        return valueOf(getName());
    }

    /** {@inheritDoc} */
    public String toString() {
        return "RuleStyle:" + getName();
    }

}
