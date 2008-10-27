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

package org.apache.fop.afp;

import java.awt.Color;

/**
 * Text data information
 */
public class AFPTextDataInfo {

    private int fontReference;

    private int x;

    private int y;

    private Color color;

    private int variableSpaceCharacterIncrement;

    private int interCharacterAdjustment;

    private byte[] data;

    private int rotation;

    /**
     * Returns the font reference
     *
     * @return the font reference
     */
    public int getFontReference() {
        return fontReference;
    }

    /**
     * Sets the font reference
     *
     * @param fontReference the font reference
     */
    public void setFontReference(int fontReference) {
        this.fontReference = fontReference;
    }

    /**
     * Returns the x coordinate
     *
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the X coordinate
     *
     * @param x the X coordinate
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Returns the y coordinate
     *
     * @return the y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the Y coordinate
     *
     * @param y the Y coordinate
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Returns the color
     *
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color
     *
     * @param color the color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Return the variable space character increment
     *
     * @return the variable space character increment
     */
    public int getVariableSpaceCharacterIncrement() {
        return variableSpaceCharacterIncrement;
    }

    /**
     * Sets the variable space character increment
     *
     * @param variableSpaceCharacterIncrement the variable space character increment
     */
    public void setVariableSpaceCharacterIncrement(
            int variableSpaceCharacterIncrement) {
        this.variableSpaceCharacterIncrement = variableSpaceCharacterIncrement;
    }

    /**
     * Return the inter character adjustment
     *
     * @return the inter character adjustment
     */
    public int getInterCharacterAdjustment() {
        return interCharacterAdjustment;
    }

    /**
     * Sets the inter character adjustment
     *
     * @param interCharacterAdjustment the inter character adjustment
     */
    public void setInterCharacterAdjustment(int interCharacterAdjustment) {
        this.interCharacterAdjustment = interCharacterAdjustment;
    }

    /**
     * Return the text data
     *
     * @return the text data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the text data
     *
     * @param data the text orientation
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Sets the text orientation
     *
     * @param rotation the text rotation
     */
    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    /**
     * Returns the text rotation
     *
     * @return the text rotation
     */
    public int getRotation() {
        return this.rotation;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "TextDataInfo{fontReference=" + fontReference
        + ", x=" + x
        + ", y=" + y
        + ", color=" + color
        + ", vsci=" + variableSpaceCharacterIncrement
        + ", ica=" + interCharacterAdjustment
        + ", orientation=" + rotation
        + ", data=" + data
        + "}";
    }
}