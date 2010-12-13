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

package org.apache.fop.render.intermediate;

import java.awt.Color;

import org.apache.xmlgraphics.java2d.color.ColorUtil;

public class IFState {

    private IFState parent;

    private String fontFamily;
    private int fontSize;
    private String fontStyle;
    private int fontWeight;
    private String fontVariant;
    private boolean fontChanged = true;

    private Color textColor;

    private IFState() {
        //nop
    }

    private IFState(IFState parent) {
        this.parent = parent;

        this.fontFamily = parent.fontFamily;
        this.fontSize = parent.fontSize;
        this.fontStyle = parent.fontStyle;
        this.fontWeight = parent.fontWeight;
        this.fontVariant = parent.fontVariant;

        this.textColor = parent.textColor;
    }

    public static IFState create() {
        return new IFState();
    }

    public IFState push() {
        return new IFState(this);
    }

    public IFState pop() {
        return this.parent;
    }

    public boolean isFontChanged() {
        return this.fontChanged;
    }

    public void resetFontChanged() {
        this.fontChanged = false;
    }

    /**
     * Returns the font family.
     * @return the font family
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * Sets the font family.
     * @param family the new font family
     */
    public void setFontFamily(String family) {
        if (!family.equals(this.fontFamily)) {
            this.fontChanged = true;
        }
        this.fontFamily = family;
    }

    /**
     * Returns the font size.
     * @return the font size (in mpt)
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Sets the font size.
     * @param size the new font size (in mpt)
     */
    public void setFontSize(int size) {
        if (size != this.fontSize) {
            this.fontChanged = true;
        }
        this.fontSize = size;
    }

    /**
     * Returns the font style.
     * @return the font style
     */
    public String getFontStyle() {
        return fontStyle;
    }

    /**
     * Set the font style
     * @param style the new font style
     */
    public void setFontStyle(String style) {
        if (!style.equals(this.fontStyle)) {
            this.fontChanged = true;
        }
        this.fontStyle = style;
    }

    /**
     * Returns the font weight.
     * @return the font weight
     */
    public int getFontWeight() {
        return fontWeight;
    }

    /**
     * Sets the font weight
     * @param weight the new font weight
     */
    public void setFontWeight(int weight) {
        if (weight != this.fontWeight) {
            this.fontChanged = true;
        }
        this.fontWeight = weight;
    }

    /**
     * Returns the font variant.
     * @return the font variant
     */
    public String getFontVariant() {
        return fontVariant;
    }

    /**
     * Sets the font variant.
     * @param variant the new font variant
     */
    public void setFontVariant(String variant) {
        if (!variant.equals(this.fontVariant)) {
            this.fontChanged = true;
        }
        this.fontVariant = variant;
    }

    /**
     * Returns the text color.
     * @return the text color
     */
    public Color getTextColor() {
        return textColor;
    }

    /**
     * Sets the text color.
     * @param color the new text color
     */
    public void setTextColor(Color color) {
        if (!ColorUtil.isSameColor(color, this.textColor)) {
            this.fontChanged = true;
        }
        this.textColor = color;
    }


}
