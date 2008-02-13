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
import java.util.Arrays;

/**
 * This keeps information about the current state when writing to pdf.
 */
public class AFPState {
    /**
     * The current color
     */
    private Color color = null;

    /**
     * The current background color
     */
    private Color backColor = null;

    /**
     * The current font name
     */
    private String fontName = null;

    /**
     * The current font size
     */
    private int fontSize = 0;

    /**
     * The current line width
     */
    private float lineWidth = 0;

    /**
     * The dash array for the current basic stroke (line type)
     */
    private float[] dashArray = null;

    /**
     * The current fill status
     */
    private boolean filled = false;
    
    /**
     * The fonts on the current page
     */
    private AFPPageFonts pageFonts = null;

    /**
     * Set the current color.
     * Check if the new color is a change and then set the current color.
     *
     * @param col the color to set
     * @return true if the color has changed
     */
    protected boolean setColor(Color col) {
        if (!col.equals(this.color)) {
            this.color = col;
            return true;
        }
        return false;
    }

    /**
     * Sets if the current painted shape is to be filled
     * @param fill true if the current painted shape is to be filled
     * @return true if the fill value has changed
     */
    protected boolean setFill(boolean fill) {
        if (fill != this.filled) {
            this.filled = fill;
            return true;
        }
        return false;
    }

    /**
     * Get the color.
     * @return the color
     */
    protected Color getColor() {
        if (this.color == null) {
            this.color = Color.black;
        }
        return this.color;
    }

    /**
     * Set the current line width.
     * @param width the line width in points
     * @return true if the line width has changed
     */
    protected boolean setLineWidth(float width) {
        if (this.lineWidth != width) {
            this.lineWidth = width;
            return true;
        }
        return false;
    }

    /**
     * Sets the dash array (line type) for the current basic stroke
     * @param dash the line dash array
     * @return true if the dash array has changed
     */
    public boolean setDashArray(float[] dash) {
        if (!Arrays.equals(dash, this.dashArray)) {
            this.dashArray = dash;
            return true;
        }
        return false;
    }

    /**
     * Gets the current line width
     * @return the current line width
     */
    protected float getLineWidth() {
        return lineWidth;
    }

    /**
     * Get the background color.
     * @return the background color
     */
    protected Color getBackColor() {
        if (this.backColor == null) {
            this.backColor = Color.white;
        }
        return backColor;
    }

    /**
     * Set the current background color.
     * Check if the new background color is a change and then set the current background color.
     *
     * @param col the background color to set
     * @return true if the color has changed
     */
    protected boolean setBackColor(Color col) {
        if (!col.equals(this.backColor)) {
            this.backColor = col;
            return true;
        }
        return false;
    }

    /**
     * Set the current font name
     * @param internalFontName the internal font name
     * @return true if the font name has changed
     */
    protected boolean setFontName(String internalFontName) {
        if (!internalFontName.equals(this.fontName)) {
            this.fontName = internalFontName;
            return true;
        }
        return false;
    }

    /**
     * Gets the current font name
     * @return the current font name
     */
    protected String getFontName() {
        return this.fontName;
    }
    
    /**
     * Gets the current font size
     * @return the current font size
     */
    protected int getFontSize() {
        return this.fontSize;
    }

    /**
     * Set the current font size.
     * Check if the font size is a change and then set the current font size.
     *
     * @param size the font size to set
     * @return true if the font size has changed
     */
    protected boolean setFontSize(int size) {
        if (size != this.fontSize) {
            this.fontSize = size;
            return true;
        }
        return false;
    }

    /**
     * Gets the current page fonts
     * @return the current page fonts
     */
    protected AFPPageFonts getPageFonts() {
        if (this.pageFonts == null) {
            this.pageFonts = new AFPPageFonts();
        }
        return this.pageFonts;
    }

    /**
     * Resets the current state
     */    
    protected void reset() {
        this.color = null;
        this.backColor = null;
        this.fontName = null;         
        this.fontSize = 0;         
        this.lineWidth = 0;
        this.dashArray = null;
        this.filled = false;
        if (this.pageFonts != null) {
            this.pageFonts.clear();
        }
    }
}