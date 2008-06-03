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
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * This keeps information about the current state when writing to pdf.
 */
public class AFPState {
    private Data data = new Data();
    
    private List stateStack = new java.util.ArrayList();

    /**
     * Set the current color.
     * Check if the new color is a change and then set the current color.
     *
     * @param col the color to set
     * @return true if the color has changed
     */
    protected boolean setColor(Color col) {
        if (!col.equals(getData().color)) {
            getData().color = col;
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
        if (fill != getData().filled) {
            getData().filled = fill;
            return true;
        }
        return false;
    }

    /**
     * Get the color.
     * @return the color
     */
    protected Color getColor() {
        if (getData().color == null) {
            getData().color = Color.black;
        }
        return getData().color;
    }

    /**
     * Set the current line width.
     * @param width the line width in points
     * @return true if the line width has changed
     */
    protected boolean setLineWidth(float width) {
        if (getData().lineWidth != width) {
            getData().lineWidth = width;
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
        if (!Arrays.equals(dash, getData().dashArray)) {
            getData().dashArray = dash;
            return true;
        }
        return false;
    }

    /**
     * Gets the current line width
     * @return the current line width
     */
    protected float getLineWidth() {
        return getData().lineWidth;
    }

    /**
     * Get the background color.
     * @return the background color
     */
    protected Color getBackColor() {
        if (getData().backColor == null) {
            getData().backColor = Color.white;
        }
        return getData().backColor;
    }

    /**
     * Set the current background color.
     * Check if the new background color is a change and then set the current background color.
     *
     * @param col the background color to set
     * @return true if the color has changed
     */
    protected boolean setBackColor(Color col) {
        if (!col.equals(getData().backColor)) {
            getData().backColor = col;
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
        if (!internalFontName.equals(getData().fontName)) {
            getData().fontName = internalFontName;
            return true;
        }
        return false;
    }

    /**
     * Gets the current font name
     * @return the current font name
     */
    protected String getFontName() {
        return getData().fontName;
    }
    
    /**
     * Gets the current font size
     * @return the current font size
     */
    protected int getFontSize() {
        return getData().fontSize;
    }

    /**
     * Set the current font size.
     * Check if the font size is a change and then set the current font size.
     *
     * @param size the font size to set
     * @return true if the font size has changed
     */
    protected boolean setFontSize(int size) {
        if (size != getData().fontSize) {
            getData().fontSize = size;
            return true;
        }
        return false;
    }

    /**
     * Gets the current page fonts
     * @return the current page fonts
     */
    protected AFPPageFonts getPageFonts() {
        if (getData().pageFonts == null) {
            getData().pageFonts = new AFPPageFonts();
        }
        return getData().pageFonts;
    }

    /**
     * Sets the image uri of the current image being processed
     * @param uri the image uri of the current image being processed
     * @return true if the image uri has changed
     */
    public boolean setImageUri(String uri) {
        if (!uri.equals(getData().imageUri)) {
            getData().imageUri = uri;
            return true;
        }
        return false;
    }

    /**
     * Returns the image uri of the current image being processed
     * @return the image uri of the current image being processed
     */
    protected String getImageUri() {
        return getData().imageUri;
    }
    
    /**
     * Push the current state onto the stack.
     * This call should be used when the q operator is used
     * so that the state is known when popped.
     */
    public void push() {
        Data copy;
        try {
            copy = (Data)getData().clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
        stateStack.add(copy);
    }

    /**
     * Get the current stack level.
     *
     * @return the current stack level
     */
    public int getStackLevel() {
        return stateStack.size();
    }

    /**
     * Pop the state from the stack and set current values to popped state.
     * This should be called when a Q operator is used so
     * the state is restored to the correct values.
     * @return the restored state, null if the stack is empty
     */
    public Data pop() {
        if (getStackLevel() > 0) {
            Data popped = (Data)stateStack.remove(stateStack.size() - 1);
            data = popped;
            return popped;
        } else {
            return null;
        }
    }

    /**
     * @return the currently valid state
     */
    public Data getData() {
        return data;
    }
    
    /** the state data instance */
    public class Data implements Cloneable, Serializable {
        private static final long serialVersionUID = -1789481244175275686L;

        /** The current color */
        private Color color = null;

        /** The current background color */
        private Color backColor = null;

        /** The current font name */
        private String fontName = null;

        /** The current font size */
        private int fontSize = 0;

        /** The current line width */
        private float lineWidth = 0;

        /** The dash array for the current basic stroke (line type) */
        private float[] dashArray = null;

        /** The current fill status */
        private boolean filled = false;
        
        /** The fonts on the current page */
        private AFPPageFonts pageFonts = null;

        /** The current image uri */
        private String imageUri = null;
        
        /** {@inheritDoc} */
        public Object clone() throws CloneNotSupportedException {
            Data obj = new Data();
            obj.color = this.color;
            obj.backColor = this.backColor;
            obj.fontName = this.fontName;
            obj.fontSize = this.fontSize;
            obj.lineWidth = this.lineWidth;
            obj.dashArray = this.dashArray;
            obj.filled = this.filled;
            obj.pageFonts = this.pageFonts;
            obj.imageUri = this.imageUri;
            return obj;
        }
        
        /** {@inheritDoc} */        
        public String toString() {
            return "color=" + color
            + ", backColor=" + backColor
            + ", fontName=" + fontName
            + ", fontSize=" + fontSize
            + ", lineWidth=" + lineWidth
            + ", dashArray=" + dashArray
            + ", filled=" + filled
            + ", pageFonts=" + pageFonts
            + ", imageUri=" + imageUri;
        }
    }
}