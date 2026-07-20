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

/** Enumeration class for border styles. */
public final class BorderStyle extends TraitEnum {

    private static final long serialVersionUID = 1L;

    private static final String[] BORDER_STYLE_NAMES = new String[]
            {"none", "hidden", "dotted", "dashed",
             "solid", "double", "groove", "ridge",
             "inset", "outset", "not-set", "square"};

    private static final int[] BORDER_STYLE_VALUES = new int[]
            {Constants.EN_NONE, Constants.EN_HIDDEN, Constants.EN_DOTTED, Constants.EN_DASHED,
             Constants.EN_SOLID, Constants.EN_DOUBLE, Constants.EN_GROOVE, Constants.EN_RIDGE,
             Constants.EN_INSET, Constants.EN_OUTSET, Constants.NOT_SET, Constants.EN_SQUARE};

    /** border-style: none */
    public static final BorderStyle NONE = new BorderStyle(0, 0);
    /** border-style: hidden */
    public static final BorderStyle HIDDEN = new BorderStyle(1, 0);
    /** border-style: dotted */
    public static final BorderStyle DOTTED = new BorderStyle(2, 0);
    /** border-style: dashed */
    public static final BorderStyle DASHED = new BorderStyle(3, 0);
    /** border-style: solid */
    public static final BorderStyle SOLID = new BorderStyle(4, 0);
    /** border-style: double */
    public static final BorderStyle DOUBLE = new BorderStyle(5, 0);
    /** border-style: groove */
    public static final BorderStyle GROOVE = new BorderStyle(6, 0);
    /** border-style: ridge */
    public static final BorderStyle RIDGE = new BorderStyle(7, 0);
    /** border-style: inset */
    public static final BorderStyle INSET = new BorderStyle(8, 0);
    /** border-style: outset */
    public static final BorderStyle OUTSET = new BorderStyle(9, 0);
    /** border-style: not-set */
    public static final BorderStyle NOT_SET = new BorderStyle(10, 0);
    /** border-style: square */
    public static final BorderStyle SQUARE = new BorderStyle(11, 0);


    private static final BorderStyle[] STYLES = new BorderStyle[] {
        NONE, HIDDEN, DOTTED, DASHED, SOLID, DOUBLE, GROOVE, RIDGE, INSET, OUTSET, NOT_SET, SQUARE};

    private final int spaceWidth;

    private BorderStyle(int index, int spaceWidth) {
        super(BORDER_STYLE_NAMES[index], BORDER_STYLE_VALUES[index]);
        this.spaceWidth = spaceWidth;
    }

    private BorderStyle(String name, int enumValue, int spaceWidth) {
        super(name, enumValue);
        this.spaceWidth = spaceWidth;
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param name the name of the enumeration value
     * @return the enumeration object
     */
    public static BorderStyle valueOf(String name) {
        for (BorderStyle style : STYLES) {
            if (style.getName().equalsIgnoreCase(name)) {
                return style;
            }
        }
        throw new IllegalArgumentException("Illegal border style: " + name);
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param name the name of the enumeration value
     * @param spaceWidth space width when using ruled leaders
     * @return the enumeration object
     */
    public static BorderStyle valueOf(String name, int spaceWidth) {
        for (BorderStyle style : STYLES) {
            if (style.getName().equals(name)) {
                return style.withSpaceWidth(spaceWidth);
            }
        }
        throw new IllegalArgumentException("Illegal border style: " + name);
    }

    /**
     * Returns the enumeration/singleton object based on its enumValue.
     * @param enumValue the enumeration value
     * @return the enumeration object
     */
    public static BorderStyle valueOf(int enumValue) {
        for (BorderStyle style : STYLES) {
            if (style.getEnumValue() == enumValue) {
                return style;
            }
        }
        throw new IllegalArgumentException("Illegal border style: " + enumValue);
    }

    /**
     * Returns the enumeration/singleton object based on its enumValue.
     * @param enumValue the enumeration value
     * @param spaceWidth space width when using ruled leaders
     * @return the enumeration object
     */
    public static BorderStyle valueOf(int enumValue, int spaceWidth) {
        for (BorderStyle style : STYLES) {
            if (style.getEnumValue() == enumValue) {
                return style.withSpaceWidth(spaceWidth);
            }
        }
        throw new IllegalArgumentException("Illegal border style: " + enumValue);
    }

    private Object readResolve() throws ObjectStreamException {
        return valueOf(getName());
    }

    /** {@inheritDoc} */
    public String toString() {
        return "BorderStyle:" + getName();
    }

    public int getSpaceWidth() {
        return spaceWidth;
    }

    public BorderStyle withSpaceWidth(int spaceWidth) {
        if (spaceWidth == this.spaceWidth) {
            return this;
        }

        return new BorderStyle(this.getName(), this.getEnumValue(), spaceWidth);
    }
}
