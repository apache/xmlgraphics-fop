/*
 * $Id: PDFGState.java,v 1.6 2003/03/07 08:25:47 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
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
        DEFAULT = new PDFGState();
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
     * Returns the name of this object
     * @return the name
     */
    public String getName() {
        return "GS" + getObjectNumber();
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
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getObjectID());
        sb.append("<<\n/Type /ExtGState\n");
        appendVal(sb, GSTATE_ALPHA_NONSTROKE);
        appendVal(sb, GSTATE_ALPHA_STROKE);

        sb.append(">>\nendobj\n");
        return sb.toString();
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

