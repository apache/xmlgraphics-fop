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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.ColorUtil;

/**
 * Border properties.
 * Class to store border trait properties for the area tree.
 */
public class BorderProps implements Serializable {

    private static final long serialVersionUID = -886871454032189183L;

    /** Separate border model */
    public static final int SEPARATE = 0;
    /** Collapsing border model, for borders inside a table */
    public static final int COLLAPSE_INNER = 1;
    /** Collapsing border model, for borders at the table's outer border */
    public static final int COLLAPSE_OUTER = 2;

    /** Border style (one of EN_*) */
    public int style; // Enum for border style                  // CSOK: VisibilityModifier
    /** Border color */
    public Color color;                                         // CSOK: VisibilityModifier
    /** Border width */
    public int width;                                           // CSOK: VisibilityModifier

    private int radiusStart = 0;
    
    private int radiusEnd = 0;
    
    /** Border mode (one of SEPARATE, COLLAPSE_INNER and COLLAPSE_OUTER) */
    public int mode;                                            // CSOK: VisibilityModifier

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
     *
     * @return the radius of the corner adjacent to the before or start border
     */
    public int getRadiusStart() {
        return radiusStart;
    }

    /**
     *
     * @param radiusStart the radius of the corner adjacent to the before or start border
     */
    public void setRadiusStart(int radiusStart) {
        this.radiusStart = radiusStart;
    }

    /**
     * @return the radius of the corner adjacent to the after or end border
     */
    public int getRadiusEnd() {
        return radiusEnd;
    }

    /**
     *
     * @param radiusEnd the radius of the corner adjacent to the  after or end border
     */
    public void setRadiusEnd(int radiusEnd) {
        this.radiusEnd = radiusEnd;
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
        return BorderStyle.valueOf(style).getName();
    }

    private static int getConstantForStyle(String style) {
        return BorderStyle.valueOf(style).getEnumValue();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            if (obj instanceof BorderProps) {
                BorderProps other = (BorderProps)obj;
                return (style == other.style)
                        && org.apache.xmlgraphics.java2d.color.ColorUtil.isSameColor(
                                color, other.color)
                        && width == other.width
                        && mode == other.mode
                        && radiusStart == other.radiusStart
                        && radiusEnd == other.radiusEnd;
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
            Pattern pattern = Pattern.compile("([^,\\(]+(?:\\(.*\\))?)");
            Matcher m = pattern.matcher(s);
            boolean found;
            found = m.find();
            String style = m.group();
            found = m.find();
            String color = m.group();
            found = m.find();
            int width = Integer.parseInt(m.group());
            int mode = SEPARATE;
            found = m.find();
            if (found) {
                String ms = m.group();
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

            BorderProps bp = new BorderProps(style, width, c, mode);

            found = m.find();
            if (found) {
                int startRadius = Integer.parseInt(m.group());
                m.find();
                int endRadius = Integer.parseInt(m.group());
                bp.setRadiusStart(startRadius);
                bp.setRadiusEnd(endRadius);
            }

            return bp;
        } else {
            throw new IllegalArgumentException("BorderProps must be surrounded by parentheses");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append('(');
        sbuf.append(getStyleString());
        sbuf.append(',');
        sbuf.append(ColorUtil.colorToString(color));
        sbuf.append(',');
        sbuf.append(width);
        if (mode != SEPARATE) {
            if (mode == COLLAPSE_INNER) {
                sbuf.append(",collapse-inner");
            } else {
                sbuf.append(",collapse-outer");
            }
        }

        if (radiusStart != 0 || radiusEnd != 0) {
            if (mode == SEPARATE) {
                // Because of the corner radii properties the mode must be set
                // so that the parameter index is consistent
                sbuf.append(",separate");
            }
            sbuf.append(',');
            sbuf.append(radiusStart);

            sbuf.append(',');
            sbuf.append(radiusEnd);
        }
        sbuf.append(')');
        return sbuf.toString();
    }

}
