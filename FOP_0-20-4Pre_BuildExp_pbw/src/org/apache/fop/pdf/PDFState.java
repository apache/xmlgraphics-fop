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

import java.awt.Color;
import java.awt.Paint;

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
    private final static String COLOR = "color";
    private final static String BACKCOLOR = "backcolor";
    private final static String PAINT = "paint";
    private final static String BACKPAINT = "backpaint";
    private final static String LINECAP = "lineCap";
    private final static String LINEJOIN = "lineJoin";
    private final static String LINEWIDTH = "lineWidth";
    private final static String MITERLIMIT = "miterLimit";
    private final static String TEXT = "text";
    private final static String DASHOFFSET = "dashOffset";
    private final static String DASHARRAY = "dashArray";
    private final static String TRANSFORM = "transform";
    private final static String FONTSIZE = "fontSize";
    private final static String FONTNAME = "fontName";
    private final static String CLIP = "clip";

    Color color = Color.black;
    Color backcolor = Color.white;
    Paint paint = null;
    Paint backPaint = null;
    int lineCap = 0;
    int lineJoin = 0;
    float lineWidth = 1;
    float miterLimit = 0;
    boolean text = false;
    int dashOffset = 0;
    int[] dashArray = new int[0];
    double[] transform = new double[]{1, 0, 0, 1, 0, 0};
    float fontSize = 0;
    String fontName = "";
    Shape clip = null;

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

        stateStack.add(saveMap);
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
            transform = (double[])saveMap.get(TRANSFORM);
            fontSize = ((Float)saveMap.get(FONTSIZE)).floatValue();
            fontName = (String)saveMap.get(FONTNAME);
            clip = (Shape)saveMap.get(CLIP);
        }
    }

    public int getStackLevel() {
        return stateStack.size();
    }

    public boolean setLineDash(int[] array, int offset) {
        return false;
    }

    public boolean setColor(Color col) {
        if(!col.equals(color)) {
            color = col;
            return true;
        }
        return false;
    }

    public boolean setBackColor(Color col) {
        if(!col.equals(backcolor)) {
            backcolor = col;
            return true;
        }
        return false;
    }

    public boolean setPaint(Paint p) {
        if(paint == null) {
            if(p != null) {
                paint = p;
                return true;
            }
        } else if(!paint.equals(p)) {
            paint = p;
            return true;
        }
        return false;
    }

    public boolean checkClip(Shape cl) {
        if(clip == null) {
            if(cl != null) {
                return true;
            }
        } else if(!clip.equals(cl)) {
            return true;
        }
        return false;
    }

    public void setClip(Shape cl) {
        clip = cl;
    }

    public boolean checkTransform(double[] vals) {
        for(int count = 0; count < transform.length; count++) {
            if(transform[count] != vals[count]) {
                return true;
            }
        }
        return false;
    }

    public void setTransform(double[] vals) {
        transform = vals;
    }
}

