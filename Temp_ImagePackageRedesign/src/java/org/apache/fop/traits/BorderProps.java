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
 
package org.apache.fop.traits;

import java.awt.Color;
import java.io.Serializable;
import java.util.StringTokenizer;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.ColorUtil;

/**
 * Border properties.
 * Class to store border trait propties for the area tree.
 */
public class BorderProps implements Serializable {
    
    /** Separate border model */
    public static final int SEPARATE = 0;
    /** Collapsing border model, for borders inside a table */
    public static final int COLLAPSE_INNER = 1;
    /** Collapsing border model, for borders at the table's outer border */
    public static final int COLLAPSE_OUTER = 2;
    
    /** Border style (one of EN_*) */
    public int style; // Enum for border style
    /** Border color */
    public Color color;
    /** Border width */
    public int width;
    /** Border mode (one of SEPARATE, COLLAPSE_INNER and COLLAPSE_OUTER) */
    public int mode;

    /**
     * Constructs a new BorderProps instance.
     * @param style border style (one of EN_*)
     * @param width border width
     * @param color border color
     * @param mode border mode ((one of SEPARATE, COLLAPSE_INNER and COLLAPSE_OUTER)
     */
    public BorderProps(int style, int width, Color color, int mode) {
        this.style = style;
        this.width = width;
        this.color = color;
        this.mode = mode;
    }

    /**
     * Constructs a new BorderProps instance.
     * @param style border style (one of the XSL enum values for border style)
     * @param width border width
     * @param color border color
     * @param mode border mode ((one of SEPARATE, COLLAPSE_INNER and COLLAPSE_OUTER)
     */
    public BorderProps(String style, int width, Color color, int mode) {
        this(getConstantForStyle(style), width, color, mode);
    }

    /**
     * @param bp the border properties or null
     * @return the effective width of the clipped part of the border
     */
    public static int getClippedWidth(BorderProps bp) {
        if ((bp != null) && (bp.mode != SEPARATE)) {
            return bp.width / 2;
        } else {
            return 0;
        }
    }
    
    private String getStyleString() {
        switch (style) {
        case Constants.EN_NONE: return "none";
        case Constants.EN_HIDDEN: return "hidden";
        case Constants.EN_DOTTED: return "dotted";
        case Constants.EN_DASHED: return "dashed";
        case Constants.EN_SOLID: return "solid";
        case Constants.EN_DOUBLE: return "double";
        case Constants.EN_GROOVE: return "groove";
        case Constants.EN_RIDGE: return "ridge";
        case Constants.EN_INSET: return "inset";
        case Constants.EN_OUTSET: return "outset";
        default: throw new IllegalStateException("Illegal border style: " + style);
        }
    }
    
    private static int getConstantForStyle(String style) {
        if ("none".equalsIgnoreCase(style)) {
            return Constants.EN_NONE;
        } else if ("hidden".equalsIgnoreCase(style)) {
            return Constants.EN_HIDDEN;
        } else if ("dotted".equalsIgnoreCase(style)) {
            return Constants.EN_DOTTED;
        } else if ("dashed".equalsIgnoreCase(style)) {
            return Constants.EN_DASHED;
        } else if ("solid".equalsIgnoreCase(style)) {
            return Constants.EN_SOLID;
        } else if ("double".equalsIgnoreCase(style)) {
            return Constants.EN_DOUBLE;
        } else if ("groove".equalsIgnoreCase(style)) {
            return Constants.EN_GROOVE;
        } else if ("ridge".equalsIgnoreCase(style)) {
            return Constants.EN_RIDGE;
        } else if ("inset".equalsIgnoreCase(style)) {
            return Constants.EN_INSET;
        } else if ("outset".equalsIgnoreCase(style)) {
            return Constants.EN_OUTSET;
        } else {
            throw new IllegalStateException("Illegal border style: " + style);
        }
    }
    
    /** {@inheritDoc} */
    public int hashCode() {
        return toString().hashCode();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            if (obj instanceof BorderProps) {
                BorderProps other = (BorderProps)obj;
                return (style == other.style)
                        && color.equals(other.color) 
                        && width == other.width
                        && mode == other.mode;
            }
        }
        return false;
    }

    /**
     * Returns a BorderProps represtation of a string of the format as written by 
     * BorderProps.toString().
     * @param foUserAgent FOP user agent caching ICC profiles
     * @param s the string
     * @return a BorderProps instance
     */
    public static BorderProps valueOf(FOUserAgent foUserAgent, String s) {
        if (s.startsWith("(") && s.endsWith(")")) {
            s = s.substring(1, s.length() - 1);
            StringTokenizer st = new StringTokenizer(s, ",");
            String style = st.nextToken();
            String color = st.nextToken();
            int width = Integer.parseInt(st.nextToken());
            int mode = SEPARATE;
            if (st.hasMoreTokens()) {
                String ms = st.nextToken();
                if ("collapse-inner".equalsIgnoreCase(ms)) {
                    mode = COLLAPSE_INNER;
                } else if ("collapse-outer".equalsIgnoreCase(ms)) {
                    mode = COLLAPSE_OUTER;
                }
            }
            Color c;
            try {
                c = ColorUtil.parseColorString(foUserAgent, color);
            } catch (PropertyException e) {
                throw new IllegalArgumentException(e.getMessage());
            } 
            
            return new BorderProps(style, width, c, mode);
        } else {
            throw new IllegalArgumentException("BorderProps must be surrounded by parentheses");
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append('(');
        sbuf.append(getStyleString());
        sbuf.append(',');
        sbuf.append(ColorUtil.colorToString(color));
        sbuf.append(',');
        sbuf.append(width);
        if (mode != SEPARATE) {
            sbuf.append(',');
            if (mode == COLLAPSE_INNER) {
                sbuf.append("collapse-inner");
            } else {
                sbuf.append("collapse-outer");
            }
        }
        sbuf.append(')');
        return sbuf.toString();
    }

}
