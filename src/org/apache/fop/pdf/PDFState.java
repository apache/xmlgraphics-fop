/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

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

    ArrayList stateStack = new ArrayList();

    public PDFState() {

    }

    // this call should be used when the q operator is used
    // so that the state is known when popped
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

    public int getStackLevel() {
        return stateStack.size();
    }

    public void restoreLevel(int stack) {
        int pos = stack;
        while(stateStack.size() > pos + 1) {
            stateStack.remove(stateStack.size() - 1);
        }
        if (stateStack.size() > pos) {
            pop();
        }
    }

    public boolean setLineDash(int[] array, int offset) {
        return false;
    }

    public boolean setColor(Color col) {
        if (!col.equals(color)) {
            color = col;
            return true;
        }
        return false;
    }

    public boolean setBackColor(Color col) {
        if (!col.equals(backcolor)) {
            backcolor = col;
            return true;
        }
        return false;
    }

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
     * For clips it can start a new state
     */
    public boolean checkClip(Shape cl) {
        if (clip == null) {
            if (cl != null) {
                return true;
            }
        } else if (!new Area(clip).equals(new Area(cl))) {
            return true;
        }
        return false;
    }

    public void setClip(Shape cl) {
        if (clip != null) {
            Area newClip = new Area(clip);
            newClip.intersect(new Area(cl));
            clip = new GeneralPath(newClip);
        } else {
            clip = cl;
        }
    }

    public boolean checkTransform(AffineTransform tf) {
        return !tf.equals(transform);
    }

    /**
     * Set a new transform.
     * This transform is appended to the transform of
     * the current graphic state.
     */
    public void setTransform(AffineTransform tf) {
        transform.concatenate(tf);
    }

    /**
     * Get the current transform.
     * This gets the combination of all transforms in the
     * current state.
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
     */
    public PDFGState getGState() {
        PDFGState defaultState = PDFGState.DEFAULT;

        PDFGState state;
        PDFGState newstate = new PDFGState(0);
        newstate.addValues(defaultState);
        for (Iterator iter = stateStack.iterator(); iter.hasNext(); ) {
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

