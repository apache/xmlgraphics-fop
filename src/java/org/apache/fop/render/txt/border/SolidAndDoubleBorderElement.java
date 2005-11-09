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

package org.apache.fop.render.txt.border;

import java.util.Arrays;

/**
 * This class is responsible for solid and double border elements managing.
 */
public class SolidAndDoubleBorderElement extends AbstractBorderElement {

    private static final char LIGHT_HORIZONTAL = '\u2500';

    private static final char LIGHT_VERTICAL = '\u2502';

    private static final char LIGHT_DOWN_AND_RIGHT = '\u250C';

    private static final char LIGHT_DOWN_AND_LEFT = '\u2510';

    private static final char LIGHT_UP_AND_RIGHT = '\u2514';

    private static final char LIGHT_UP_AND_LEFT = '\u2518';

    private static final char LIGHT_VERTICAL_AND_RIGHT = '\u251C';

    private static final char LIGHT_VERTICAL_AND_LEFT = '\u2524';

    private static final char LIGHT_DOWN_AND_HORIZONTAL = '\u252C';

    private static final char LIGHT_UP_AND_HORIZONTAL = '\u2534';

    private static final char LIGHT_VERTICAL_AND_HORIZONTAL = '\u253C';
    
    private static final char DOUBLE_HORIZONTAL = '\u2550';

    private static final char DOUBLE_VERTICAL = '\u2551';
    
    private static final char DOUBLE_DOWN_AND_RIGHT = '\u2554';    

    private static final char DOUBLE_DOWN_AND_LEFT = '\u2557';

    private static final char DOUBLE_UP_AND_RIGHT = '\u255A';

    private static final char DOUBLE_UP_AND_LEFT = '\u255D';
    
    private static final char DOUBLE_VERTICAL_AND_RIGHT = '\u2560';
    
    private static final char DOUBLE_VERTICAL_AND_LEFT = '\u2563';
    
    private static final char DOUBLE_DOWN_AND_HORIZONTAL = '\u2566';
    
    private static final char DOUBLE_UP_AND_HORIZONTAL = '\u2569';
    
    private static final char DOUBLE_VERTICAL_AND_HORIZONTAL = '\u256C';
    
    private static final char DOWN_SINGLE_AND_RIGHT_DOUBLE = '\u2552';
    
    private static final char DOWN_DOUBLE_AND_RIGHT_SINGLE = '\u2553';

    private static final char DOWN_SINGLE_AND_LEFT_DOUBLE = '\u2555';

    private static final char DOWN_DOUBLE_AND_LEFT_SINGLE = '\u2556';
    
    private static final char UP_SINGLE_AND_RIGHT_DOUBLE = '\u2558';
    
    private static final char UP_DOUBLE_AND_RIGHT_SINGLE = '\u2559';
    
    private static final char UP_SINGLE_AND_LEFT_DOUBLE = '\u255B';
    
    private static final char UP_DOUBLE_AND_LEFT_SINGLE = '\u255C';
    
    private static final char VERTICAL_SINGLE_AND_RIGHT_DOUBLE = '\u255E';
    
    private static final char VERTICAL_DOUBLE_AND_RIGHT_SINGLE = '\u255F';
    
    private static final char VERTICAL_SINGLE_AND_LEFT_DOUBLE = '\u2561';
    
    private static final char VERTICAL_DOUBLE_AND_LEFT_SINGLE = '\u2562';
    
    private static final char DOWN_SINGLE_AND_HORIZONTAL_DOUBLE = '\u2564';
    
    private static final char DOWN_DOUBLE_AND_HORIZONTAL_SINGLE = '\u2565';
    
    private static final char UP_SINGLE_AND_HORIZONTAL_DOUBLE = '\u2567';
    
    private static final char UP_DOUBLE_AND_HORIZONTAL_SINGLE = '\u2568';
    
    private static final char VERTICAL_SINGLE_AND_HORIZONTAL_DOUBLE = '\u256A';
    
    private static final char VERTICAL_DOUBLE_AND_HORIZONTAL_SINGLE = '\u256B';
    
    private static final char UNDEFINED = '?';
    
    private static final int UP3 = 1;

    private static final int DOWN3 = 3;

    private static final int LEFT3 = 9;

    private static final int RIGHT3 = 27;

    private static final char[] MAP = new char[100];

    static {
        Arrays.fill(MAP, UNDEFINED);
        MAP[0] = ' ';
        MAP[UP3] = LIGHT_VERTICAL;
        MAP[DOWN3] = LIGHT_VERTICAL;
        MAP[RIGHT3] = LIGHT_HORIZONTAL;
        MAP[LEFT3] = LIGHT_HORIZONTAL;
        MAP[UP3 + DOWN3] = LIGHT_VERTICAL;
        MAP[LEFT3 + RIGHT3] = LIGHT_HORIZONTAL;
        MAP[UP3 + LEFT3] = LIGHT_UP_AND_LEFT;
        MAP[LEFT3 + DOWN3] = LIGHT_DOWN_AND_LEFT;
        MAP[DOWN3 + RIGHT3] = LIGHT_DOWN_AND_RIGHT;
        MAP[UP3 + RIGHT3] = LIGHT_UP_AND_RIGHT;
        MAP[UP3 + DOWN3 + RIGHT3] = LIGHT_VERTICAL_AND_RIGHT;
        MAP[UP3 + LEFT3 + DOWN3] = LIGHT_VERTICAL_AND_LEFT;
        MAP[LEFT3 + DOWN3 + RIGHT3] = LIGHT_DOWN_AND_HORIZONTAL;
        MAP[UP3 + LEFT3 + RIGHT3] = LIGHT_UP_AND_HORIZONTAL;
        MAP[UP3 + LEFT3 + DOWN3 + RIGHT3] = LIGHT_VERTICAL_AND_HORIZONTAL;
        //DOUBLE
        MAP[2 * UP3] = DOUBLE_VERTICAL;
        MAP[2 * DOWN3] = DOUBLE_VERTICAL;
        MAP[2 * RIGHT3] = DOUBLE_HORIZONTAL;
        MAP[2 * LEFT3] = DOUBLE_HORIZONTAL;
        MAP[2 * UP3 + 2 * DOWN3] = DOUBLE_VERTICAL;
        MAP[2 * LEFT3 + 2 * RIGHT3] = DOUBLE_HORIZONTAL;
        MAP[2 * UP3 + 2 * LEFT3] = DOUBLE_UP_AND_LEFT;
        MAP[2 * LEFT3 + 2 * DOWN3] = DOUBLE_DOWN_AND_LEFT;
        MAP[2 * DOWN3 + 2 * RIGHT3] = DOUBLE_DOWN_AND_RIGHT;
        MAP[2 * UP3 + 2 * RIGHT3] = DOUBLE_UP_AND_RIGHT;
        MAP[2 * UP3 + 2 * DOWN3 + 2 * RIGHT3] = DOUBLE_VERTICAL_AND_RIGHT;
        MAP[2 * UP3 + 2 * DOWN3 + 2 * LEFT3] = DOUBLE_VERTICAL_AND_LEFT;
        MAP[2 * DOWN3 + 2 * RIGHT3 + 2 * LEFT3] = DOUBLE_DOWN_AND_HORIZONTAL;
        MAP[2 * UP3 + 2 * RIGHT3 + 2 * LEFT3] = DOUBLE_UP_AND_HORIZONTAL;
        MAP[2 * UP3 + 2 * DOWN3 + 2 * RIGHT3 + 2 * LEFT3] = DOUBLE_VERTICAL_AND_HORIZONTAL;
        //DOUBLE&SINGLE
        MAP[DOWN3 + 2 * RIGHT3] = DOWN_SINGLE_AND_RIGHT_DOUBLE;
        MAP[2 * DOWN3 + RIGHT3] = DOWN_DOUBLE_AND_RIGHT_SINGLE;
        MAP[DOWN3 + 2 * LEFT3] = DOWN_SINGLE_AND_LEFT_DOUBLE;
        MAP[2 * DOWN3 + LEFT3] = DOWN_DOUBLE_AND_LEFT_SINGLE;
        MAP[UP3 + 2 * RIGHT3] = UP_SINGLE_AND_RIGHT_DOUBLE;
        MAP[2 * UP3 + RIGHT3] = UP_DOUBLE_AND_RIGHT_SINGLE;
        MAP[UP3 + 2 * LEFT3] = UP_SINGLE_AND_LEFT_DOUBLE;
        MAP[2 * UP3 + LEFT3] = UP_DOUBLE_AND_LEFT_SINGLE;
        MAP[UP3 + DOWN3 + 2 * RIGHT3] = VERTICAL_SINGLE_AND_RIGHT_DOUBLE;
        MAP[2 * UP3 + 2 * DOWN3 + RIGHT3] = VERTICAL_DOUBLE_AND_RIGHT_SINGLE;
        MAP[UP3 + DOWN3 + 2 * LEFT3] = VERTICAL_SINGLE_AND_LEFT_DOUBLE;
        MAP[2 * UP3 + 2 * DOWN3 + LEFT3] = VERTICAL_DOUBLE_AND_LEFT_SINGLE;
        MAP[DOWN3 + 2 * LEFT3 + 2 * RIGHT3] = DOWN_SINGLE_AND_HORIZONTAL_DOUBLE;
        MAP[2 * DOWN3 + LEFT3 + RIGHT3] = DOWN_DOUBLE_AND_HORIZONTAL_SINGLE;
        MAP[UP3 + 2 * LEFT3 + 2 * RIGHT3] = UP_SINGLE_AND_HORIZONTAL_DOUBLE;
        MAP[2 * UP3 + LEFT3 + RIGHT3] = UP_DOUBLE_AND_HORIZONTAL_SINGLE;
        MAP[UP3 + DOWN3 + 2 * LEFT3 + 2 * RIGHT3] = VERTICAL_SINGLE_AND_HORIZONTAL_DOUBLE;
        MAP[2 * UP3 + 2 * DOWN3 + LEFT3 + RIGHT3] = VERTICAL_DOUBLE_AND_HORIZONTAL_SINGLE;
    }

    /**
     * Initializes a newly created <code>SolidAndDoubleBorderElement</code> 
     * object so that it represents an empty border element.
     */
    public SolidAndDoubleBorderElement() {
    }

    /**
     * Constructs a newly allocated <code>SolidAndDoubleBorderElement</code> 
     * object. Fills <code>data</code> using binary representation of 
     * <code>type</code>. If border style is EN_DOUBLE, multiplies 
     * <code>data[side]</code> by 2 for every side to distinguish EN_SOLID and 
     * EN_DOUBLE.
     * 
     * @param style integer, representing border style.
     * @param type binary representation of type gives <code>data</code>
     */
    public SolidAndDoubleBorderElement(int style, int type) {
        super(type);
        if (style == EN_DOUBLE) {
            for (int i = 0; i < 4; i++) {
                data[i] *= 2;
            }
        }
    }
    
    /**
     * Merges with <code>sde</code>.
     * @param sde instance of SolidAndDoubleBorderElement
     * @return instance of AbstractBorderElement
     */
    public AbstractBorderElement mergeSolid(SolidAndDoubleBorderElement sde) {
        AbstractBorderElement e = new SolidAndDoubleBorderElement(EN_SOLID, 0);
        for (int i = 0; i < 4; i++) {
            if (sde.getData(i) != 0) {
                e.setData(i, sde.getData(i));
            } else {
                e.setData(i, data[i]);
            }
        }
        return e;
    }

    /**
     * Merges with e.
     * @param e instance of AbstractBorderElement
     * @return instance of AbstractBorderElement
     */
    public AbstractBorderElement merge(AbstractBorderElement e) {
        AbstractBorderElement abe = this;
        if (e instanceof SolidAndDoubleBorderElement) {
            abe = mergeSolid((SolidAndDoubleBorderElement) e);
        } else if (e instanceof DottedBorderElement) {
            abe = e;
        } else if (e instanceof DashedBorderElement) {
            abe = e.merge(this);
        }
        return abe;
    }
    
    /**
     * Maps to char.
     * @return resulting mapping char 
     */
    private char map2Char() {
        int key = 0;
        key += data[UP] * UP3;
        key += data[LEFT] * LEFT3;
        key += data[DOWN] * DOWN3;
        key += data[RIGHT] * RIGHT3;
        return MAP[key];
    }

    /**
     * Modifies data to nearest normal internal representation.
     */
    private void modifyData() {
        int c1 = 0;
        int c2 = 0;
        for (int i = 0; i < 4; i++) {
            c1 += (data[i] == 1) ? 1 : 0;
            c2 += (data[i] == 2) ? 1 : 0;
        }
        int m = c1 > c2 ? 1 : 0;
        int[] p = {0, m, 2 * (1 - m)};
        for (int i = 0; i < 4; i++) {
            data[i] = p[data[i]];
        }
    }

    /**
     * @see org.apache.fop.render.txt.border.AbstractBorderElement#convert2Char()
     */
    public char convert2Char() {
        char ch = map2Char();
        if (ch == UNDEFINED) {
            modifyData();
            ch = map2Char();
        }
        return ch;
    }
}
