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
    private final static String PATTERN = "pattern";
    private final static String BACKPATTERN = "backpattern";
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

    PDFColor color = new PDFColor(0, 0, 0);
    PDFColor backcolor = new PDFColor(255, 255, 255);
    PDFPattern pattern = null;
    PDFPattern backPattern = null;
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
        HashMap changedMap = new HashMap();

    }

    public void pop() {
        if (getStackLevel() > 0) {
            stateStack.remove(stateStack.size() - 1);
        }
    }

    public int getStackLevel() {
        return stateStack.size();
    }

    public boolean setLineDash(int[] array, int offset) {
        return false;
    }

}

