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

package org.apache.fop.render;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;

/**
 * A base class which holds information about the current rendering state.
 */
public abstract class AbstractState {
    private AbstractData currentData = null;
    private Stack/*<AbstractData>*/ stateStack = null;

    /**
     * Instantiates a new state data object
     * @return a new state data object
     */
    protected abstract AbstractData instantiateData();
    
    /**
     * @return the currently valid state
     */
    public AbstractData getData() {
        if (currentData == null) {
            currentData = instantiateData();
        }
        return currentData;
    }

    /**
     * Set the current color.
     * Check if the new color is a change and then set the current color.
     *
     * @param col the color to set
     * @return true if the color has changed
     */
    public boolean setColor(Color col) {
        if (!col.equals(getData().color)) {
            getData().color = col;
            return true;
        }
        return false;
    }

    /**
     * Get the color.
     * @return the color
     */
    public Color getColor() {
        if (getData().color == null) {
            getData().color = Color.black;
        }
        return getData().color;
    }

    /**
     * Get the background color.
     * @return the background color
     */
    public Color getBackColor() {
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
    public boolean setBackColor(Color col) {
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
    public boolean setFontName(String internalFontName) {
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
    public String getFontName() {
        return getData().fontName;
    }
    
    /**
     * Gets the current font size
     * @return the current font size
     */
    public int getFontSize() {
        return getData().fontSize;
    }

    /**
     * Set the current font size.
     * Check if the font size is a change and then set the current font size.
     *
     * @param size the font size to set
     * @return true if the font size has changed
     */
    public boolean setFontSize(int size) {
        if (size != getData().fontSize) {
            getData().fontSize = size;
            return true;
        }
        return false;
    }

    /**
     * Set the current line width.
     * @param width the line width in points
     * @return true if the line width has changed
     */
    public boolean setLineWidth(float width) {
        if (getData().lineWidth != width) {
            getData().lineWidth = width;
            return true;
        }
        return false;
    }

    /**
     * Gets the current line width
     * @return the current line width
     */
    public float getLineWidth() {
        return getData().lineWidth;
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
     * Get the current transform.
     * This gets the combination of all transforms in the
     * current state.
     *
     * @return the calculate combined transform for the current state
     */
    public AffineTransform getTransform() {
       AffineTransform at = new AffineTransform();
       for (Iterator iter = getStateStack().iterator(); iter.hasNext();) {
           AbstractData data = (AbstractData)iter.next();
           AffineTransform stackTrans = data.getTransform();
           at.concatenate(stackTrans);
       }
       AffineTransform currentTrans = getData().getTransform();
       at.concatenate(currentTrans);
       return at;
    }

    /**
     * Check the current transform.
     * The transform for the current state is the combination of all
     * transforms in the current state. The parameter is compared
     * against this current transform.
     *
     * @param tf the transform the check against
     * @return true if the new transform is different then the current transform
     */
    public boolean checkTransform(AffineTransform tf) {
        return !tf.equals(getData().getTransform());
    }

    /**
     * Get a copy of the base transform for the page. Used to translate
     * IPP/BPP values into X,Y positions when positioning is "fixed".
     *
     * @return the base transform, or null if the state stack is empty
     */
    public AffineTransform getBaseTransform() {
       if (getStateStack().isEmpty()) {
           return null;
       } else {
           AbstractData baseData = (AbstractData)getStateStack().get(0);
           return (AffineTransform) baseData.getTransform().clone();
       }
    }
    
    /**
     * Concatenates the given AffineTransform to the current one.
     * @param tf the transform to concatenate to the current level transform
     */
    public void concatenate(AffineTransform tf) {
        getData().concatenate(tf);
    }

    /**
     * Resets the current AffineTransform.
     */
    public void resetTransform() {
        getData().resetTransform();
    }

    /**
     * Push the current state onto the stack.
     * This call should be used when the Q operator is used
     * so that the state is known when popped.
     */
    public void push() {
        AbstractData copy;
        try {
            copy = (AbstractData)getData().clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
        getStateStack().push(copy);
    }
    
    /**
     * Pop the state from the stack and set current values to popped state.
     * This should be called when a Q operator is used so
     * the state is restored to the correct values.
     * @return the restored state, null if the stack is empty
     */
    public AbstractData pop() {
        if (!getStateStack().isEmpty()) {
            this.currentData = (AbstractData)getStateStack().pop();
            return this.currentData;
        } else {
            return null;
        }
    }

    /**
     * Clears the state stack
     */    
    public void clear() {
        getStateStack().clear();
        currentData = null;
    }

    /**
     * @return the state stack
     */
    protected Stack/*<AbstractData>*/ getStateStack() {
        if (stateStack == null) {
            stateStack = new java.util.Stack/*<AbstractData>*/();
        }
        return stateStack;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "stateStack=" + stateStack
        + ", currentData=" + currentData;
    }

    /**
     * A base state data holding object 
     */
    public abstract class AbstractData implements Cloneable, Serializable {
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

        /** The current transform */
        private AffineTransform transform = null;

        /**
         * Concatenate the given AffineTransform with the current thus creating
         * a new viewport. Note that all concatenation operations are logged
         * so they can be replayed if necessary (ex. for block-containers with
         * "fixed" positioning.
         * @param at Transformation to perform
         */
        public void concatenate(AffineTransform at) {
            getTransform().concatenate(at);
        }

        /**
         * Get the current AffineTransform.
         * @return the current transform
         */
        public AffineTransform getTransform() {
            if (transform == null) {
                transform = new AffineTransform();
            }
            return transform;
        }

        /**
         * Resets the current AffineTransform.
         */
        public void resetTransform() {
            transform = new AffineTransform();
        }

        /** {@inheritDoc} */
        public Object clone() throws CloneNotSupportedException {
            AbstractData obj = instantiateData();
            obj.color = this.color;
            obj.backColor = this.backColor;
            obj.fontName = this.fontName;
            obj.fontSize = this.fontSize;
            obj.lineWidth = this.lineWidth;
            obj.dashArray = this.dashArray;
            obj.transform = new AffineTransform(this.transform);
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
                + ", transform=" + transform;
        }
    }
}
