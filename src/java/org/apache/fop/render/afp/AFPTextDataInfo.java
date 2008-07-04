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

package org.apache.fop.render.afp;

import java.awt.Color;

/**
 * Contains text data information
 */
public class AFPTextDataInfo {
    private int fontReference;
    private int x;
    private int y;
    private Color color;
    private int variableSpaceCharacterIncrement;
    private int interCharacterAdjustment;
    private byte[] data;
    private int orientation;
    
    /**
     * @return the font reference
     */
    public int getFontReference() {
        return fontReference;
    }
    
    /**
     * Sets the font reference
     * @param fontReference the font reference
     */
    protected void setFontReference(int fontReference) {
        this.fontReference = fontReference;
    }
    
    /**
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }
    
    /**
     * Sets the X coordinate
     * @param x the X coordinate
     */
    protected void setX(int x) {
        this.x = x;
    }
    
    /**
     * @return the y coordinate
     */
    public int getY() {
        return y;
    }
    
    /**
     * Sets the Y coordinate
     * @param y the Y coordinate
     */
    protected void setY(int y) {
        this.y = y;
    }
    
    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Sets the color
     * @param color the color
     */
    protected void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * @return the variable space character increment
     */
    public int getVariableSpaceCharacterIncrement() {
        return variableSpaceCharacterIncrement;
    }
    
    /**
     * Sets the variable space character increment
     * @param variableSpaceCharacterIncrement the variable space character increment
     */
    protected void setVariableSpaceCharacterIncrement(
            int variableSpaceCharacterIncrement) {
        this.variableSpaceCharacterIncrement = variableSpaceCharacterIncrement;
    }
    
    /**
     * @return the inter character adjustment
     */
    public int getInterCharacterAdjustment() {
        return interCharacterAdjustment;
    }
    
    /**
     * Sets the inter character adjustment
     * @param interCharacterAdjustment the inter character adjustment
     */
    protected void setInterCharacterAdjustment(int interCharacterAdjustment) {
        this.interCharacterAdjustment = interCharacterAdjustment;
    }
    
    /**
     * @return the text data
     */
    public byte[] getData() {
        return data;
    }
    
    /**
     * Sets the text data
     * @param data the text orientation
     */
    protected void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Sets the text orientation
     * @param orientation the text orientation
     */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
    
    /**
     * @return the text orientation
     */
    public int getOrientation() {
        return this.orientation;
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return "AFPTextDataInfo{fontReference=" + fontReference
        + ", x=" + x
        + ", y=" + y
        + ", color=" + color
        + ", vsci=" + variableSpaceCharacterIncrement
        + ", ica=" + interCharacterAdjustment
        + ", orientation=" + orientation
        + ", data=" + data
        + "}";
    }
}