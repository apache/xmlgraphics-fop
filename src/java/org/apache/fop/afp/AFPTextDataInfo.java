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

    /** the text font reference */
    private int fontReference;

    /** the text x coordinate position */
    private int x;

    /** the text y coordinate position */
    private int y;

    /** the text color */
    private Color color;

    /** the text variable space adjustment */
    private int variableSpaceCharacterIncrement;

    /** the text inter character adjustment */
    private int interCharacterAdjustment;

    /** the text orientation */
    private int rotation;

    /** the text encoding */
    private String textEncoding;

    /** the text string */
    private String textString;

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

    /**
     * Sets the text encoding
     *
     * @param textEncoding the text encoding
     */
    public void setEncoding(String textEncoding) {
        this.textEncoding = textEncoding;
    }

    /**
     * Returns the text encoding
     *
     * @return the text encoding
     */
    public String getEncoding() {
        return this.textEncoding;
    }

    /**
     * Sets the text string
     *
     * @param textString the text string
     */
    public void setString(String textString) {
        this.textString = textString;
    }

    /**
     * Returns the text string
     *
     * @return the text string
     */
    public String getString() {
        return this.textString;
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
        + ", textString=" + textString
        + ", textEncoding=" + textEncoding
        + "}";
    }
}