/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.util.Map;
import java.util.Iterator;

/**
 * Class representing a /ExtGState object.
 */
public class PDFGState extends PDFObject {

    /** Line width (LW) */
    public static final String GSTATE_LINE_WIDTH          = "LW";
    /** Line cap (LC) */
    public static final String GSTATE_LINE_CAP            = "LC";
    /** Line join (LJ) */
    public static final String GSTATE_LINE_JOIN           = "LJ";
    /** Miter limit (ML) */
    public static final String GSTATE_MITER_LIMIT         = "ML";
    /** Dash pattern (D) */
    public static final String GSTATE_DASH_PATTERN        = "D";
    /** Rendering intent (RI) */
    public static final String GSTATE_RENDERING_INTENT    = "RI";
    /** Overprint for stroke (OP) */
    public static final String GSTATE_OVERPRINT_STROKE    = "OP";
    /** Overprint for fill (op) */
    public static final String GSTATE_OVERPRINT_FILL      = "op";
    /** Overprint mode (OPM) */
    public static final String GSTATE_OVERPRINT_MODE      = "OPM";
    /** Font (Font) */
    public static final String GSTATE_FONT                = "Font";
    /** Black generation (BG) */
    public static final String GSTATE_BLACK_GENERATION    = "BG";
    /** Black generation with default (BG2) */
    public static final String GSTATE_BLACK_GENERATION2   = "BG2";
    /** Undercolor removal function (UCR) */
    public static final String GSTATE_UNDERCOLOR_REMOVAL  = "UCR";
    /** Undercolor removal function with default (UCR2) */
    public static final String GSTATE_UNDERCOLOR_REMOVAL2 = "UCR2";
    /** Transfer function (TR) */
    public static final String GSTATE_TRANSFER_FUNCTION   = "TR";
    /** Transfer function with default (TR2) */
    public static final String GSTATE_TRANSFER_FUNCTION2  = "TR2";
    /** Halftone dictionary or stream (HT) */
    public static final String GSTATE_HALFTONE_DICT       = "HT";
    /** Halftone phase (HTP, does not show up anymore in PDF 1.4)*/
    public static final String GSTATE_HALFTONE_PHASE      = "HTP";
    /** Flatness (FL) */
    public static final String GSTATE_FLATNESS            = "FL";
    /** Smoothness (SM) */
    public static final String GSTATE_SMOOTHNESS          = "SM";
    /** Strike adjustment (SA) */
    public static final String GSTATE_STRIKE_ADJ          = "SA";
    /** Blend mode (BM, PDF 1.4) */
    public static final String GSTATE_BLEND_MODE          = "BM";
    /** Soft mask (SMask, PDF 1.4) */
    public static final String GSTATE_SOFT_MASK           = "SMask";
    /** Stroking Alpha (CA, PDF 1.4) */
    public static final String GSTATE_ALPHA_STROKE        = "CA";
    /** Nonstroking Alpha (ca, PDF 1.4) */
    public static final String GSTATE_ALPHA_NONSTROKE     = "ca";
    /** Alpha Source Flag (AIS, PDF 1.4) */
    public static final String GSTATE_ALPHA_SOURCE_FLAG   = "AIS";
    /** Text Knockout Flag (TK, PDF 1.4) */
    public static final String GSTATE_TEXT_KNOCKOUT       = "TK";
    

    /** Default GState object */
    public static final PDFGState DEFAULT;

    static {
        DEFAULT = new PDFGState(0);
        Map vals = DEFAULT.values;
        /*vals.put(LW, new Float(1.0));
        vals.put(LC, new Integer(0));
        vals.put(LJ, new Integer(0));
        vals.put(ML, new Float(10.0));
        vals.put(D, "0 []");
        vals.put(RI, "RelativeColorimetric");
        vals.put(OP, Boolean.FALSE);
        vals.put(op, Boolean.FALSE);
        vals.put(OPM, new Integer(1));
        vals.put(Font, "");*/
        
        vals.put(GSTATE_ALPHA_STROKE, new Float(1.0));
        vals.put(GSTATE_ALPHA_NONSTROKE, new Float(1.0));
    }

    private Map values = new java.util.HashMap();

    /**
     * Create a /ExtGState object.
     *
     * @param number the object's number
     */
    public PDFGState(int number) {
        /* generic creation of object */
        super(number);
    }

    /**
     * Returns the name of this object
     * @return the name
     */
    public String getName() {
        return "GS" + this.number;
    }

    /**
     * Sets the alpha value.
     * @param val alpha value (0.0 - 1.0)
     * @param fill True if alpha should be set for non-stroking operations, 
     * False if for stroking operations
     */
    public void setAlpha(float val, boolean fill) {
        if (fill) {
            values.put(GSTATE_ALPHA_NONSTROKE, new Float(val));
        } else {
            values.put(GSTATE_ALPHA_STROKE, new Float(val));
        }
    }

    /**
     * Adds all values from another GState object to this one.
     * @param state source object to copy from
     */
    public void addValues(PDFGState state) {
        values.putAll(state.values);
    }

    /**
     * Adds all values from a Map to this object.
     * @param vals source object to copy from
     */
    public void addValues(Map vals) {
        values.putAll(vals);
    }

    /**
     * Represent the object in PDF.
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        StringBuffer sb = new StringBuffer(this.number + " " + this.generation
                              + " obj\n<<\n/Type /ExtGState\n");
        appendVal(sb, GSTATE_ALPHA_NONSTROKE);
        appendVal(sb, GSTATE_ALPHA_STROKE);

        sb.append(">>\nendobj\n");
        return sb.toString().getBytes();
    }

    private void appendVal(StringBuffer sb, String name) {
        Object val = values.get(name);
        if (val != null) {
            sb.append("/" + name + " " + val + "\n");
        }
    }

    /*
     * example
     * 29 0 obj
     * <<
     * /Type /ExtGState
     * /ca 0.5
     * >>
     * endobj
     */

    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PDFGState)) {
            return false;
        }
        Map vals1 = values;
        Map vals2 = ((PDFGState)obj).values;
        if (vals1.size() != vals2.size()) {
            return false;
        }
        for (Iterator iter = vals1.keySet().iterator(); iter.hasNext();) {
            Object str = iter.next();
            Object obj1 = vals1.get(str);
            if (!obj1.equals(vals2.get(str))) {
                return false;
            }
        }
        return true;
    }
}

