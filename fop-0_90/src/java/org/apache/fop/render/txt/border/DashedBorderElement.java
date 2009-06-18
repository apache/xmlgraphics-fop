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
 * This class is responsible for managing of dashed border elements.
 */
public class DashedBorderElement extends AbstractBorderElement {
    
    private static final char DASH_HORIZONTAL = '-';

    private static final char DASH_VERTICAL = '|';
    
    private static final char UNDEFINED = '?';
    
    private static final int UP2 = 1;
    
    private static final int RIGHT2 = 2;
    
    private static final int DOWN2 = 4;
    
    private static final int LEFT2 = 8;
    
    private static char[] map = new char[20];
    
    static {
        Arrays.fill(map, UNDEFINED);
        map[0] = ' ';
        map[UP2] = DASH_VERTICAL;
        map[DOWN2] = DASH_VERTICAL;
        map[UP2 + DOWN2] = DASH_VERTICAL;
        
        map[LEFT2] = DASH_HORIZONTAL;
        map[RIGHT2] = DASH_HORIZONTAL;
        map[LEFT2 + RIGHT2] = DASH_HORIZONTAL;
    }
    
    /**
     * Constructs a newly allocated <code>DashedBorderElement</code> object.
     * Fills <code>data</code> using superclass constructor.
     * 
     * @param type binary representation of type gives <code>data</code>
     */
    public DashedBorderElement(int type) {
        super(type);
    }

    /**
     * Merges dashed border element with instance of solid and double border
     * element, returns instance of <code>SolidAndDoubleBorderElement</code>. 
     * 
     * @param sdb instance of <code>SolidAndDoubleBorderElement</code> to merge
     * @return merged border element
     */
    private AbstractBorderElement mergeSolid(SolidAndDoubleBorderElement sdb) {
        AbstractBorderElement e = new SolidAndDoubleBorderElement(EN_SOLID, 0);
        for (int i = 0; i < 4; i++) {
            e.setData(i, Math.max(data[i], sdb.getData(i)));
        }
        return e;        
    }

    /**
     * Merges dashed border element with dashed border element and returns 
     * instance of <code>DashedBorderElement</code>. 
     * 
     * @param dbe instance of <code>DashedBorderElement</code> to merge
     * @return merged border element
     */
    private AbstractBorderElement mergeDashed(DashedBorderElement dbe) {
        for (int i = 0; i < 4; i++) {
            data[i] = Math.max(data[i], dbe.getData(i));
        }
        return this;
    }
    
    /**
     * Converts dashed border element to 
     * <code>SolidAndDoubleBorderElement</code>.
     * 
     * @return converted instance of <code>SolidAndDoubleBorderElement</code>
     */
    private AbstractBorderElement toSolidAndDouble() {
        AbstractBorderElement e = new SolidAndDoubleBorderElement(EN_SOLID, 0);
        for (int i = 0; i < 4; i++) {
            e.setData(i, data[i]);
        }
        return e;        
    }

    /**
     * Merges with border element.
     * @param e instance of AbstractBorderElement
     * @return instance of AbstractBorderElement
     */
    public AbstractBorderElement merge(AbstractBorderElement e) {
        AbstractBorderElement abe = this;
        if (e instanceof SolidAndDoubleBorderElement) {
            abe = mergeSolid((SolidAndDoubleBorderElement) e);
        } else if (e instanceof DashedBorderElement) {
            abe = mergeDashed((DashedBorderElement) e);
        } else {
            abe = e;
        }
        return abe;
    }

    /** 
     * @see org.apache.fop.render.txt.border.AbstractBorderElement#convert2Char()
     */
    public char convert2Char() {
        int key = 0;
        key += data[UP] * UP2;
        key += data[DOWN] * DOWN2;
        key += data[LEFT] * LEFT2;
        key += data[RIGHT] * RIGHT2;
        char ch = map[key];
        if (ch == UNDEFINED) {
            ch = toSolidAndDouble().convert2Char();
        }
        return ch;
    }
}
