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

package org.apache.fop.util;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * A base class which holds information about the current painting state.
 */
public abstract class AbstractPaintingState implements Cloneable, Serializable {

    private static final long serialVersionUID = 5998356138437094188L;

    /** current state data */
    private AbstractData data = null;

    /** the state stack */
    private StateStack/*<AbstractData>*/ stateStack = new StateStack/*<AbstractData>*/();

    /**
     * Instantiates a new state data object
     *
     * @return a new state data object
     */
    protected abstract AbstractData instantiateData();

    /**
     * Instantiates a new state object
     *
     * @return a new state object
     */
    protected abstract AbstractPaintingState instantiate();

    /**
     * Returns the currently valid state
     *
     * @return the currently valid state
     */
    public AbstractData getData() {
        if (data == null) {
            data = instantiateData();
        }
        return data;
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
     *
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
     *
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
     *
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
     *
     * @return the current font name
     */
    public String getFontName() {
        return getData().fontName;
    }

    /**
     * Gets the current font size
     *
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
     *
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
     * Returns the current line width
     *
     * @return the current line width
     */
    public float getLineWidth() {
        return getData().lineWidth;
    }

    /**
     * Sets the dash array (line type) for the current basic stroke
     *
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
       for (Iterator iter = stateStack.iterator(); iter.hasNext();) {
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
       if (stateStack.isEmpty()) {
           return null;
       } else {
           AbstractData baseData = (AbstractData)stateStack.get(0);
           return (AffineTransform) baseData.getTransform().clone();
       }
    }

    /**
     * Concatenates the given AffineTransform to the current one.
     *
     * @param at the transform to concatenate to the current level transform
     */
    public void concatenate(AffineTransform at) {
        getData().concatenate(at);
    }

    /**
     * Resets the current AffineTransform to the Base AffineTransform.
     */
    public void resetTransform() {
        getData().setTransform(getBaseTransform());
    }

    /**
     * Clears the current AffineTransform to the Identity AffineTransform
     */
    public void clearTransform() {
        getData().clearTransform();
    }


    /**
     * Save the current painting state.
     * This pushes the current painting state onto the stack.
     * This call should be used when the Q operator is used
     * so that the state is known when popped.
     */
    public void save() {
        AbstractData copy = (AbstractData)getData().clone();
        stateStack.push(copy);
    }

    /**
     * Restore the current painting state.
     * This pops the painting state from the stack and sets current values to popped state.
     *
     * @return the restored state, null if the stack is empty
     */
    public AbstractData restore() {
        if (!stateStack.isEmpty()) {
            setData((AbstractData)stateStack.pop());
            return this.data;
        } else {
            return null;
        }
    }

    /**
     * Save all painting state data.
     * This pushes all painting state data in the given list to the stack
     *
     * @param dataList a state data list
     */
    public void saveAll(List/*<AbstractData>*/ dataList) {
        Iterator it = dataList.iterator();
        while (it.hasNext()) {
            // save current data on stack
            save();
            setData((AbstractData)it.next());
        }
    }

    /**
     * Restore all painting state data.
     * This pops all painting state data from the stack
     *
     * @return a list of state data popped from the stack
     */
    public List/*<AbstractData>*/ restoreAll() {
        List/*<AbstractData>*/ dataList = new java.util.ArrayList/*<AbstractData>*/();
        AbstractData data;
        while (true) {
            data = getData();
            if (restore() == null) {
                break;
            }
            // insert because of stack-popping
            dataList.add(0, data);
        }
        return dataList;
    }

    /**
     * Sets the current state data
     *
     * @param currentData state data
     */
    protected void setData(AbstractData data) {
        this.data = data;
    }

    /**
     * Clears the state stack
     */
    public void clear() {
        stateStack.clear();
        setData(null);
    }

    /**
     * Return the state stack
     *
     * @return the state stack
     */
    protected Stack/*<AbstractData>*/ getStateStack() {
        return this.stateStack;
    }

    /** {@inheritDoc} */
    public Object clone() {
        AbstractPaintingState state = instantiate();
        state.stateStack = new StateStack(this.stateStack);
        state.data = (AbstractData)this.data.clone();
        return state;
    }

    /** {@inheritDoc} */
    public String toString() {
        return ", stateStack=" + stateStack
        + ", currentData=" + data;
    }


    /**
     * A stack implementation which holds state objects
     */
    public class StateStack extends java.util.Stack {

        private static final long serialVersionUID = 4897178211223823041L;

        /**
         * Default constructor
         */
        public StateStack() {
            super();
        }

        /**
         * Copy constructor
         *
         * @param c initial contents of stack
         */
        public StateStack(Collection c) {
            elementCount = c.size();
            // 10% for growth
            elementData = new Object[
                          (int)Math.min((elementCount * 110L) / 100, Integer.MAX_VALUE)];
            c.toArray(elementData);
        }
    }


    /**
     * A base painting state data holding object
     */
    public abstract class AbstractData implements Cloneable, Serializable {

        private static final long serialVersionUID = 5208418041189828624L;

        /** The current color */
        protected Color color = null;

        /** The current background color */
        protected Color backColor = null;

        /** The current font name */
        protected String fontName = null;

        /** The current font size */
        protected int fontSize = 0;

        /** The current line width */
        protected float lineWidth = 0;

        /** The dash array for the current basic stroke (line type) */
        protected float[] dashArray = null;

        /** The current transform */
        protected AffineTransform transform = null;

        /**
         * Returns a newly create data object
         *
         * @return a new data object
         */
        protected abstract AbstractData instantiate();

        /**
         * Concatenate the given AffineTransform with the current thus creating
         * a new viewport. Note that all concatenation operations are logged
         * so they can be replayed if necessary (ex. for block-containers with
         * "fixed" positioning.
         *
         * @param at Transformation to perform
         */
        public void concatenate(AffineTransform at) {
            getTransform().concatenate(at);
        }

        /**
         * Get the current AffineTransform.
         *
         * @return the current transform
         */
        public AffineTransform getTransform() {
            if (transform == null) {
                transform = new AffineTransform();
            }
            return transform;
        }

        /**
         * Sets the current AffineTransform.
         */
        public void setTransform(AffineTransform baseTransform) {
            this.transform = baseTransform;
        }

        /**
         * Resets the current AffineTransform.
         */
        public void clearTransform() {
            transform = new AffineTransform();
        }

        /**
         * Returns the derived rotation from the current transform
         *
         * @return the derived rotation from the current transform
         */
        public int getDerivedRotation() {
            AffineTransform at = getTransform();
            double sx = at.getScaleX();
            double sy = at.getScaleY();
            double shx = at.getShearX();
            double shy = at.getShearY();
            int rotation = 0;
            if (sx == 0 && sy == 0 && shx > 0 && shy < 0) {
                rotation = 270;
            } else if (sx < 0 && sy < 0 && shx == 0 && shy == 0) {
                rotation = 180;
            } else if (sx == 0 && sy == 0 && shx < 0 && shy > 0) {
                rotation = 90;
            } else {
                rotation = 0;
            }
            return rotation;
        }

        /** {@inheritDoc} */
        public Object clone() {
            AbstractData data = instantiate();
            data.color = this.color;
            data.backColor = this.backColor;
            data.fontName = this.fontName;
            data.fontSize = this.fontSize;
            data.lineWidth = this.lineWidth;
            data.dashArray = this.dashArray;
            data.transform = new AffineTransform(this.transform);
            return data;
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
