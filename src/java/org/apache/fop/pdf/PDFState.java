/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.awt.Shape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Area;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;

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
    private static final String COLOR = "color";
    private static final String BACKCOLOR = "backcolor";
    private static final String PAINT = "paint";
    private static final String BACKPAINT = "backpaint";
    private static final String LINECAP = "lineCap";
    private static final String LINEJOIN = "lineJoin";
    private static final String LINEWIDTH = "lineWidth";
    private static final String MITERLIMIT = "miterLimit";
    private static final String TEXT = "text";
    private static final String DASHOFFSET = "dashOffset";
    private static final String DASHARRAY = "dashArray";
    private static final String TRANSFORM = "transform";
    private static final String FONTSIZE = "fontSize";
    private static final String FONTNAME = "fontName";
    private static final String CLIP = "clip";
    private static final String GSTATE = "gstate";

    private Color color = Color.black;
    private Color backcolor = Color.white;
    private Paint paint = null;
    private Paint backPaint = null;
    private int lineCap = 0;
    private int lineJoin = 0;
    private float lineWidth = 1;
    private float miterLimit = 0;
    private boolean text = false;
    private int dashOffset = 0;
    private int[] dashArray = new int[0];
    private AffineTransform transform = new AffineTransform();
    private float fontSize = 0;
    private String fontName = "";
    private Shape clip = null;
    private PDFGState gstate = null;

    private ArrayList stateStack = new ArrayList();

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
        HashMap saveMap = new HashMap();
        saveMap.put(COLOR, color);
        saveMap.put(BACKCOLOR, backcolor);
        saveMap.put(PAINT, paint);
        saveMap.put(BACKPAINT, backPaint);
        saveMap.put(LINECAP, new Integer(lineCap));
        saveMap.put(LINEJOIN, new Integer(lineJoin));
        saveMap.put(LINEWIDTH, new Float(lineWidth));
        saveMap.put(MITERLIMIT, new Float(miterLimit));
        saveMap.put(TEXT, new Boolean(text));
        saveMap.put(DASHOFFSET, new Integer(dashOffset));
        saveMap.put(DASHARRAY, dashArray);
        saveMap.put(TRANSFORM, transform);
        saveMap.put(FONTSIZE, new Float(fontSize));
        saveMap.put(FONTNAME, fontName);
        saveMap.put(CLIP, clip);
        saveMap.put(GSTATE, gstate);

        stateStack.add(saveMap);

        transform = new AffineTransform();
    }

    /**
     * Pop the state from the stack and set current values to popped state.
     * This should be called when a Q operator is used so
     * the state is restored to the correct values.
     */
    public void pop() {
        if (getStackLevel() > 0) {
            HashMap saveMap = (HashMap)stateStack.get(stateStack.size() - 1);
            stateStack.remove(stateStack.size() - 1);
            color = (Color)saveMap.get(COLOR);
            backcolor = (Color)saveMap.get(BACKCOLOR);
            paint = (Paint)saveMap.get(PAINT);
            backPaint = (Paint)saveMap.get(BACKPAINT);
            lineCap = ((Integer)saveMap.get(LINECAP)).intValue();
            lineJoin = ((Integer)saveMap.get(LINEJOIN)).intValue();
            lineWidth = ((Float)saveMap.get(LINEWIDTH)).floatValue();
            miterLimit = ((Float)saveMap.get(MITERLIMIT)).floatValue();
            text = ((Boolean)saveMap.get(TEXT)).booleanValue();
            dashOffset = ((Integer)saveMap.get(DASHOFFSET)).intValue();
            dashArray = (int[])saveMap.get(DASHARRAY);
            transform = (AffineTransform)saveMap.get(TRANSFORM);
            fontSize = ((Float)saveMap.get(FONTSIZE)).floatValue();
            fontName = (String)saveMap.get(FONTNAME);
            clip = (Shape)saveMap.get(CLIP);
            gstate = (PDFGState)saveMap.get(GSTATE);
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
    public void restoreLevel(int stack) {
        int pos = stack;
        while (stateStack.size() > pos + 1) {
            stateStack.remove(stateStack.size() - 1);
        }
        if (stateStack.size() > pos) {
            pop();
        }
    }

    /**
     * Set the current line dash.
     * Check if setting the line dash to the given values
     * will make a change and then set the state to the new values.
     *
     * @param array the line dash array
     * @param offset the line dash start offset
     * @return true if the line dash has changed
     */
    public boolean setLineDash(int[] array, int offset) {
        return false;
    }

    /**
     * Set the current line width.
     * @param width the line width in points
     * @return true if the line width has changed
     */
    public boolean setLineWidth(float width) {
        if (lineWidth != width) {
            lineWidth = width;
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
        if (!col.equals(color)) {
            color = col;
            return true;
        }
        return false;
    }

    /**
     * Set the current background color.
     * Check if the background color will change and then set the new color.
     *
     * @param col the new background color
     * @return true if the background color has changed
     */
    public boolean setBackColor(Color col) {
        if (!col.equals(backcolor)) {
            backcolor = col;
            return true;
        }
        return false;
    }

    /**
     * Set the current paint.
     * This checks if the paint will change and then sets the current paint.
     *
     * @param p the new paint
     * @return true if the new paint changes the current paint
     */
    public boolean setPaint(Paint p) {
        if (paint == null) {
            if (p != null) {
                paint = p;
                return true;
            }
        } else if (!paint.equals(p)) {
            paint = p;
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
        if (clip == null) {
            if (cl != null) {
                return true;
            }
        } else if (!new Area(clip).equals(new Area(cl))) {
            return true;
        }
        // todo check for clips that are larger than the current
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
        if (clip != null) {
            Area newClip = new Area(clip);
            newClip.intersect(new Area(cl));
            clip = new GeneralPath(newClip);
        } else {
            clip = cl;
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
        return !tf.equals(transform);
    }

    /**
     * Set a new transform.
     * This transform is appended to the transform of
     * the current graphic state.
     *
     * @param tf the transform to concatonate to the current level transform
     */
    public void setTransform(AffineTransform tf) {
        transform.concatenate(tf);
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
            HashMap map = (HashMap)iter.next();
            tf = (AffineTransform)map.get(TRANSFORM);
            at.concatenate(tf);
        }
        at.concatenate(transform);

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
            HashMap map = (HashMap)iter.next();
            state = (PDFGState)map.get(GSTATE);
            if (state != null) {
                newstate.addValues(state);
            }
        }
        if (gstate != null) {
            newstate.addValues(gstate);
        }

        return newstate;
    }
}

