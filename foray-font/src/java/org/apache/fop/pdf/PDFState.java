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
 
package org.apache.fop.pdf;

import java.io.Serializable;
import java.util.List;
import java.util.Iterator;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

/**
 * This keeps information about the current state when writing to pdf.
 * It allows for creating new graphics states with the q operator.
 * This class is only used to store the information about the state
 * the caller needs to handle the actual pdf operators.
 *
 * When setting the state for pdf there are three possible ways of
 * handling the situation.
 * The values can be set to override previous or default values.
 * A new state can be added and then the values set.
 * The current state can be popped and values will return to a
 * previous state then the necessary values can be overridden.
 * The current transform behaves differently to other values as the
 * matrix is combined with the current resolved value.
 * It is impossible to optimise the result without analysing the all
 * the possible combinations after completing.
 */
public class PDFState {

    private Data data = new Data();
    
    private List stateStack = new java.util.ArrayList();

    /**
     * PDF State for storing graphics state.
     */
    public PDFState() {

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
            getData().resetTransform();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
        stateStack.add(copy);
    }

    /**
     * @return the currently valid state
     */
    public Data getData() {
        return data;
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
     * Get the current stack level.
     *
     * @return the current stack level
     */
    public int getStackLevel() {
        return stateStack.size();
    }

    /**
     * Restore the state to a particular level.
     * this can be used to restore to a known level without making
     * multiple pop calls.
     *
     * @param stack the level to restore to
     */
    /*
    public void restoreLevel(int stack) {
        int pos = stack;
        while (stateStack.size() > pos + 1) {
            stateStack.remove(stateStack.size() - 1);
        }
        if (stateStack.size() > pos) {
            pop();
        }
    }*/

    /**
     * Set the current line dash.
     * Check if setting the line dash to the given values
     * will make a change and then set the state to the new values.
     *
     * @param array the line dash array
     * @param offset the line dash start offset
     * @return true if the line dash has changed
     */
    /*
    public boolean setLineDash(int[] array, int offset) {
        return false;
    }*/

    /**
     * Set the current line width.
     * @param width the line width in points
     * @return true if the line width has changed
     */
    public boolean setLineWidth(float width) {
        if (getData().lineWidth != width) {
            getData().lineWidth = width;
            return true;
        } else {
            return false;
        }
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
        } else {
            return false;
        }
    }

    /**
     * Set the current background color.
     * Check if the background color will change and then set the new color.
     *
     * @param col the new background color
     * @return true if the background color has changed
     */
    public boolean setBackColor(Color col) {
        if (!col.equals(getData().backcolor)) {
            getData().backcolor = col;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the current paint.
     * This checks if the paint will change and then sets the current paint.
     *
     * @param p the new paint
     * @return true if the new paint changes the current paint
     */
    public boolean setPaint(Paint p) {
        if (getData().paint == null) {
            if (p != null) {
                getData().paint = p;
                return true;
            }
        } else if (!data.paint.equals(p)) {
            getData().paint = p;
            return true;
        }
        return false;
    }

    /**
     * Check if the clip will change the current state.
     * A clip is assumed to be used in a situation where it will add
     * to any clip in the current or parent states.
     * A clip cannot be cleared, this can only be achieved by going to
     * a parent level with the correct clip.
     * If the clip is different then it may start a new state so that
     * it can return to the previous clip.
     *
     * @param cl the clip shape to check
     * @return true if the clip will change the current clip.
     */
    public boolean checkClip(Shape cl) {
        if (getData().clip == null) {
            if (cl != null) {
                return true;
            }
        } else if (!new Area(getData().clip).equals(new Area(cl))) {
            return true;
        }
        //TODO check for clips that are larger than the current
        return false;
    }

    /**
     * Set the current clip.
     * This either sets a new clip or sets the clip to the intersect of
     * the old clip and the new clip.
     *
     * @param cl the new clip in the current state
     */
    public void setClip(Shape cl) {
        if (getData().clip != null) {
            Area newClip = new Area(getData().clip);
            newClip.intersect(new Area(cl));
            getData().clip = new GeneralPath(newClip);
        } else {
            getData().clip = cl;
        }
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
        return !tf.equals(getData().transform);
    }

    /**
     * Set a new transform.
     * This transform is appended to the transform of
     * the current graphic state.
     *
     * @param tf the transform to concatonate to the current level transform
     * @deprecated This method name is misleading. Use concatenate(AffineTransform) instead!
     */
    public void setTransform(AffineTransform tf) {
        concatenate(tf);
    }
    
    /**
     * Concatenates the given AffineTransform to the current one.
     * @param tf the transform to concatenate to the current level transform
     */
    public void concatenate(AffineTransform tf) {
        getData().concatenate(tf);
    }

    /**
     * Get the current transform.
     * This gets the combination of all transforms in the
     * current state.
     *
     * @return the calculate combined transform for the current state
     */
    public AffineTransform getTransform() {
       AffineTransform tf;
       AffineTransform at = new AffineTransform();
       for (Iterator iter = stateStack.iterator(); iter.hasNext();) {
           Data d = (Data)iter.next();
           tf = d.transform;
           at.concatenate(tf);
       }
       at.concatenate(getData().transform);
       return at;
    }

    /**
     * Get the grapics state.
     * This gets the combination of all graphic states for
     * the current context.
     * This is the graphic state set with the gs operator not
     * the other graphic state changes.
     *
     * @return the calculated ExtGState in the current context
     */
    public PDFGState getGState() {
        PDFGState defaultState = PDFGState.DEFAULT;

        PDFGState state;
        PDFGState newstate = new PDFGState();
        newstate.addValues(defaultState);
        for (Iterator iter = stateStack.iterator(); iter.hasNext();) {
            Data d = (Data)iter.next();
            state = d.gstate;
            if (state != null) {
                newstate.addValues(state);
            }
        }
        if (getData().gstate != null) {
            newstate.addValues(getData().gstate);
        }

        return newstate;
    }
    
    public class Data implements Cloneable, Serializable {
        
        public Color color = Color.black;
        public Color backcolor = Color.black;
        public Paint paint = null;
        public Paint backPaint = null;
        public int lineCap = 0;
        public int lineJoin = 0;
        public float lineWidth = 1;
        public float miterLimit = 0;
        public boolean text = false;
        public int dashOffset = 0;
        public int[] dashArray = new int[0];
        public AffineTransform transform = new AffineTransform();
        public float fontSize = 0;
        public String fontName = "";
        public Shape clip = null;
        public PDFGState gstate = null;

        
        /** @see java.lang.Object#clone() */
        public Object clone() throws CloneNotSupportedException {
            Data obj = new Data();
            obj.color = this.color;
            obj.backcolor = this.backcolor;
            obj.paint = this.paint;
            obj.backPaint = this.paint;
            obj.lineCap = this.lineCap;
            obj.lineJoin = this.lineJoin;
            obj.lineWidth = this.lineWidth;
            obj.miterLimit = this.miterLimit;
            obj.text = this.text;
            obj.dashOffset = this.dashOffset;
            obj.dashArray = this.dashArray;
            obj.transform = new AffineTransform(this.transform);
            obj.fontSize = this.fontSize;
            obj.fontName = this.fontName;
            obj.clip = this.clip;
            obj.gstate = this.gstate;
            return obj;
        }
        
        /**
         * Get the current Transform.
         */
        public AffineTransform getTransform() {
            return transform;
        }

        public void resetTransform() {
            transform = new AffineTransform();
        }

        /**
         * Concatenate the given AffineTransform with the current thus creating
         * a new viewport. Note that all concatenation operations are logged
         * so they can be replayed if necessary (ex. for block-containers with
         * "fixed" positioning.
         * @param at Transformation to perform
         */
        public void concatenate(AffineTransform at) {
            transform.concatenate(at);
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            return super.toString() + ", " + this.transform;
        }
    }
}

