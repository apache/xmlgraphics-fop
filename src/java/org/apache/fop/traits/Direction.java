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

/**
 * Enumeration class for direction traits, namely {inline,block}-progression-direction
 * and shift-direction.
 */
public final class Direction extends TraitEnum {

    private static final long serialVersionUID = 1L;

    private static final String[] DIRECTION_NAMES = new String[]
        {"lr", "rl", "tb", "bt"};

    private static final int[] DIRECTION_VALUES = new int[]
        {Constants.EN_LR, Constants.EN_RL, Constants.EN_TB, Constants.EN_BT};

    /** direction: left-to-right */
    public static final Direction LR = new Direction(0);
    /** direction: right-to-left */
    public static final Direction RL = new Direction(1);
    /** direction: top-to-bottom */
    public static final Direction TB = new Direction(2);
    /** direction: bottom-to-top */
    public static final Direction BT = new Direction(3);

    private static final Direction[] DIRECTIONS = new Direction[] {LR, RL, TB, BT};

    private Direction(int index) {
        super(DIRECTION_NAMES[index], DIRECTION_VALUES[index]);
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param name the name of the enumeration value
     * @return the enumeration object
     */
    public static Direction valueOf(String name) {
        for (int i = 0; i < DIRECTIONS.length; i++) {
            if (DIRECTIONS[i].getName().equalsIgnoreCase(name)) {
                return DIRECTIONS[i];
            }
        }
        throw new IllegalArgumentException("Illegal direction: " + name);
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param enumValue the enumeration value
     * @return the enumeration object
     */
    public static Direction valueOf(int enumValue) {
        for (int i = 0; i < DIRECTIONS.length; i++) {
            if (DIRECTIONS[i].getEnumValue() == enumValue) {
                return DIRECTIONS[i];
            }
        }
        throw new IllegalArgumentException("Illegal direction: " + enumValue);
    }

    private Object readResolve() throws ObjectStreamException {
        return valueOf(getName());
    }

    /** {@inheritDoc} */
    public String toString() {
        return getName();
    }

}
