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

import java.util.Iterator;
import java.awt.Paint;
import java.awt.Shape;
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
public class PDFState extends org.apache.fop.render.AbstractState {

    /**
     * PDF State for storing graphics state.
     */
    public PDFState() {
    }

    /**
     * Set the current paint.
     * This checks if the paint will change and then sets the current paint.
     *
     * @param p the new paint
     * @return true if the new paint changes the current paint
     */
    public boolean setPaint(Paint p) {
        Paint paint = ((PDFData)getData()).paint; 
        if (paint == null) {
            if (p != null) {
                ((PDFData)getData()).paint = p;
                return true;
            }
        } else if (!paint.equals(p)) {
            ((PDFData)getData()).paint = p;
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
        Shape clip = ((PDFData)getData()).clip; 
        if (clip == null) {
            if (cl != null) {
                return true;
            }
        } else if (!new Area(clip).equals(new Area(cl))) {
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
        Shape clip = ((PDFData)getData()).clip; 
        if (clip != null) {
            Area newClip = new Area(clip);
            newClip.intersect(new Area(cl));
            ((PDFData)getData()).clip = new GeneralPath(newClip);
        } else {
            ((PDFData)getData()).clip = cl;
        }
    }

    /**
     * Get the current stack level.
     *
     * @return the current stack level
     */
    public int getStackLevel() {
        return getStateStack().size();
    }

    /**
     * Get the graphics state.
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
        PDFGState newState = new PDFGState();
        newState.addValues(defaultState);
        for (Iterator iter = getStateStack().iterator(); iter.hasNext();) {
            PDFData d = (PDFData)iter.next();
            state = d.gstate;
            if (state != null) {
                newState.addValues(state);
            }
        }
        if (((PDFData)getData()).gstate != null) {
            newState.addValues(((PDFData)getData()).gstate);
        }
        return newState;
    }

    private class PDFData extends org.apache.fop.render.AbstractState.AbstractData {

        private static final long serialVersionUID = 3527950647293177764L;

        private Paint paint = null;
        private Paint backPaint = null;
        private int lineCap = 0;
        private int lineJoin = 0;
        private float miterLimit = 0;
        private boolean text = false;
        private int dashOffset = 0;
        private Shape clip = null;
        private PDFGState gstate = null;
        
        /** {@inheritDoc} */
        public Object clone() throws CloneNotSupportedException {
            PDFData obj = (PDFData)super.clone();
            obj.paint = this.paint;
            obj.backPaint = this.paint;
            obj.lineCap = this.lineCap;
            obj.lineJoin = this.lineJoin;
            obj.miterLimit = this.miterLimit;
            obj.text = this.text;
            obj.dashOffset = this.dashOffset;
            obj.clip = this.clip;
            obj.gstate = this.gstate;
            return obj;
        }

        /** {@inheritDoc} */
        public String toString() {
            return super.toString()
                + ", paint=" + paint
                + ", backPaint=" + backPaint
                + ", lineCap=" + lineCap
                + ", miterLimit=" + miterLimit
                + ", text=" + text
                + ", dashOffset=" + dashOffset
                + ", clip=" + clip
                + ", gstate=" + gstate;
        }
    }

    /** {@inheritDoc} */
    protected AbstractData instantiateData() {
        return new PDFData();
    }
}

