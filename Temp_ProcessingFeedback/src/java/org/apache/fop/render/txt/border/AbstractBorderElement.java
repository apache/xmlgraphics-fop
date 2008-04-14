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

package org.apache.fop.render.txt.border;

import java.awt.Point;
import java.util.Arrays;

import org.apache.fop.area.CTM;
import org.apache.fop.fo.Constants;
import org.apache.fop.render.txt.TXTState;

/**
 * This class keeps information about abstract border element, i.e. specifies
 * border element for one text position.
 */
public abstract class AbstractBorderElement implements Constants {

    /**
     * Constant for a line segment, directing from a center of symbol up 
     * the the symbol boundary.
     */
    public static final int UP = 0;

    /**
     * Constant for a line segment, directing from a center of symbol right 
     * the the symbol boundary.
     */
    public static final int RIGHT = 1;

    /**
     * Constant for a line segment, directing from a center of symbol down 
     * the the symbol boundary.
     */
    public static final int DOWN = 2;

    /**
     * Constant for a line segment, directing from a center of symbol left 
     * the the symbol boundary.
     */
    public static final int LEFT = 3;

    /**
     * I-th element of this array specify, if there line from center of symbol
     * to corresponding side (UP, RIGHT, DOWN, LEFT).
     */
    protected int[] data = {0, 0, 0, 0};

    /**
     * Initializes a newly created <code>AbstractBorderElement</code> object 
     * so that it represents an empty border element.
     */
    public AbstractBorderElement() {
    }

    /**
     * Constructs a newly allocated <code>AbstractBorderElement</code> object.
     * Fills array <code>data</code> using binary representation of 
     * <code>type</code>.
     * 
     * @param type binary representation of type gives <code>data</code>
     */
    public AbstractBorderElement(int type) {
        for (int i = 0; i < 4; i++) {
            data[i] = (type >> i) & 1;
        }
    }

    /**
     * Returns value of side's element of <code>data</code>.
     * 
     * @param side integer, representing side
     * @return value of side's element
     */
    public int getData(int side) {
        return data[side];
    }

    /**
     * Sets a value for <code>data[side]</code>.
     * 
     * @param side integer, representing side
     * @param value a new value for <code>data[side]</code>
     */
    public void setData(int side, int value) {
        data[side] = value;
    }

    /**
     * Transform border element in according with <code>state</code>.
     * @param state instance of TXTState
     */
    public void transformElement(TXTState state) {
        // here we'll get CTM^-1 without shift
        double[] da = state.getResultCTM().toArray();
        CTM ctm = new CTM(da[0], -da[1], -da[2], da[3], 0, 0);

        Point[] pa = new Point[4];
        pa[0] = new Point(0, data[UP]);
        pa[1] = new Point(data[RIGHT], 0);
        pa[2] = new Point(0, -data[DOWN]);
        pa[3] = new Point(-data[LEFT], 0);

        Arrays.fill(data, 0);
        for (int i = 0; i < 4; i++) {
            Point p = state.transformPoint(pa[i], ctm);

            int length = (int) p.distance(0, 0);
            if (p.x == 0 && p.y > 0) {
                data[UP] = length;
            } else if (p.x == 0 && p.y < 0) {
                data[DOWN] = length;
            } else if (p.x > 0 && p.y == 0) {
                data[RIGHT] = length;
            } else if (p.x < 0 && p.y == 0) {
                data[LEFT] = length;
            }
        }
    }

    /**
     * Merges with border element.
     * @param e instance of AbstractBorderElement
     * @return instance of AbstractBorderElement
     */
    public abstract AbstractBorderElement merge(AbstractBorderElement e);

    /**
     * Convert internal representation of border element to char.
     * @return corresponding char
     */
    public abstract char convert2Char();
}
