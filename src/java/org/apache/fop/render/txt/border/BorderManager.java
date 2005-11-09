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

import org.apache.fop.fo.Constants;
import org.apache.fop.render.txt.TXTState;

/**
 * This keeps all information about borders for current processed page.
 */
public class BorderManager {

    /** Matrix for storing information about one border element. */
    private AbstractBorderElement[][] borderInfo;

    /** Width of current processed border. */
    private int width;
    
    /** Height of current processed border. */
    private int height;

    /** x-coordinate of upper left point of current processed border. */
    private int startX;

    /** y-coordinate of upper left point of current processed border. */
    private int startY;

    /** Stores TXTState for transforming border elements. */
    private TXTState state;

    /**
     * Constructs BorderManger, using <code>pageWidth</code> and 
     * <code>pageHeight</code> for creating <code>borderInfo</code>.
     *  
     * @param pageWidth page width
     * @param pageHeight page height
     * @param state TXTState
     */
    public BorderManager(int pageWidth, int pageHeight, TXTState state) {
        this.state = state;
        borderInfo = new AbstractBorderElement[pageHeight][pageWidth];
    }

    /**
     * Adds border element to <code>borderInfo</code>.
     * 
     * @param x x-coordinate
     * @param y y-coordinate
     * @param style border-style
     * @param type border element type, binary representation of wich gives 
     *         information about availability or absence of corresponding side. 
     */
    public void addBorderElement(int x, int y, int style, int type) {
        AbstractBorderElement be = null;

        if (style == Constants.EN_SOLID || style == Constants.EN_DOUBLE) {
            be = new SolidAndDoubleBorderElement(style, type);
        } else if (style == Constants.EN_DOTTED) {
            be = new DottedBorderElement();
        } else if (style == Constants.EN_DASHED) {
            be = new DashedBorderElement(type);
        } else {
            return;
        }
        be.transformElement(state);

        if (borderInfo[y][x] != null) {
            borderInfo[y][x] = borderInfo[y][x].merge(be);
        } else {
            borderInfo[y][x] = be;
        }
    }

    /**
     * @param x x-coordinate
     * @param y y-coordinate
     * @return if border element at point (x,y) is available, returns instance 
     * of Character, created on char, given by corresponding border element, 
     * otherwise returns null. 
     */
    public Character getCharacter(int x, int y) {
        Character c = null;
        if (borderInfo[y][x] != null) {
            c = new Character(borderInfo[y][x].convert2Char());
        }
        return c;
    }

    /**
     * @return width of current processed border.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Sets width of current processed border.
     * @param width width of border
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * @return height of current processed border.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets height of current processed border.
     * @param height height of border
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return x-coordinate of upper left point of current processed border.
     */
    public int getStartX() {
        return startX;
    }

    /**
     * Sets x-coordinate of upper left point of current processed border.
     * @param startX x-coordinate of upper left border's point.
     */
    public void setStartX(int startX) {
        this.startX = startX;
    }

    /**
     * @return y-coordinate of upper left point of current processed border.
     */
    public int getStartY() {
        return startY;
    }

    /**
     * Sets y-coordinate of upper left point of current processed border.
     * @param startY y-coordinate of upper left border's point.
     */
    public void setStartY(int startY) {
        this.startY = startY;
    }
}
