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

/** Enumeration class for writing mode trait. */
public final class WritingMode extends TraitEnum {

    private static final long serialVersionUID = 1L;

    private static final String[] WRITING_MODE_NAMES = new String[]
        {"lr-tb", "rl-tb", "tb-lr", "tb-rl"};

    private static final int[] WRITING_MODE_VALUES = new int[]
        {Constants.EN_LR_TB, Constants.EN_RL_TB, Constants.EN_TB_LR, Constants.EN_TB_RL};

    /** writing mode: lr-tb */
    public static final WritingMode LR_TB = new WritingMode(0);
    /** writing mode: rl-tb */
    public static final WritingMode RL_TB = new WritingMode(1);
    /** writing mode: tb-lr */
    public static final WritingMode TB_LR = new WritingMode(2);
    /** writing mode: tb-rl */
    public static final WritingMode TB_RL = new WritingMode(3);

    private static final WritingMode[] WRITING_MODES
        = new WritingMode[] {LR_TB, RL_TB, TB_LR, TB_RL};

    private WritingMode(int index) {
        super(WRITING_MODE_NAMES[index], WRITING_MODE_VALUES[index]);
    }

    /**
     * Assign writing mode traits from this trait to the specified
     * writing mode traits setter.
     * @param wms a writing mode traits setter
     */
    public void assignWritingModeTraits ( WritingModeTraitsSetter wms ) {
        Direction inlineProgressionDirection;
        Direction blockProgressionDirection;
        Direction columnProgressionDirection;
        Direction rowProgressionDirection;
        Direction shiftDirection;
        switch ( getEnumValue() ) {
        default:
        case Constants.EN_LR_TB:
            inlineProgressionDirection = Direction.LR;
            blockProgressionDirection = Direction.TB;
            columnProgressionDirection = Direction.LR;
            rowProgressionDirection = Direction.TB;
            shiftDirection = Direction.BT;
            break;
        case Constants.EN_RL_TB:
            inlineProgressionDirection = Direction.RL;
            blockProgressionDirection = Direction.TB;
            columnProgressionDirection = Direction.RL;
            rowProgressionDirection = Direction.TB;
            shiftDirection = Direction.BT;
            break;
        case Constants.EN_TB_LR:
            inlineProgressionDirection = Direction.TB;
            blockProgressionDirection = Direction.LR;
            columnProgressionDirection = Direction.TB;
            rowProgressionDirection = Direction.LR;
            shiftDirection = Direction.RL;
            break;
        case Constants.EN_TB_RL:
            inlineProgressionDirection = Direction.TB;
            blockProgressionDirection = Direction.RL;
            columnProgressionDirection = Direction.TB;
            rowProgressionDirection = Direction.RL;
            shiftDirection = Direction.LR;
            break;
        }
        wms.setInlineProgressionDirection ( inlineProgressionDirection );
        wms.setBlockProgressionDirection ( blockProgressionDirection );
        wms.setColumnProgressionDirection ( columnProgressionDirection );
        wms.setRowProgressionDirection ( rowProgressionDirection );
        wms.setShiftDirection ( shiftDirection );
        wms.setWritingMode ( this );
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param name the name of the enumeration value
     * @return the enumeration object
     */
    public static WritingMode valueOf(String name) {
        for (int i = 0; i < WRITING_MODES.length; i++) {
            if (WRITING_MODES[i].getName().equalsIgnoreCase(name)) {
                return WRITING_MODES[i];
            }
        }
        throw new IllegalArgumentException("Illegal writing mode: " + name);
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param enumValue the enumeration value
     * @return the enumeration object
     */
    public static WritingMode valueOf(int enumValue) {
        for (int i = 0; i < WRITING_MODES.length; i++) {
            if (WRITING_MODES[i].getEnumValue() == enumValue) {
                return WRITING_MODES[i];
            }
        }
        throw new IllegalArgumentException("Illegal writing mode: " + enumValue);
    }

    private Object readResolve() throws ObjectStreamException {
        return valueOf(getName());
    }

    /** {@inheritDoc} */
    public String toString() {
        return getName();
    }

}
