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

    private static final long serialVersionUID = 7053576586478548795L;

    public enum Mode {
        SEPARATE("separate") {
            @Override
            int getClippedWidth(BorderProps bp) {
                return 0;
            }
        },
        COLLAPSE_INNER("collapse-inner"), // for borders inside a table
        COLLAPSE_OUTER("collapse-outer"); // for borders at the table's outer border

        private final String value;

        Mode(String value) {
            this.value = value;
        }

        int getClippedWidth(BorderProps bp) {
            return bp.width / 2;
        };
    }

    /** Border style (one of EN_*) */
    public final int style; // Enum for border style            // CSOK: VisibilityModifier
    /** Border color */
    public final Color color;                                   // CSOK: VisibilityModifier

    /** Border width */
    public final int width;                                           // CSOK: VisibilityModifier

    private final int radiusStart;

    private final int radiusEnd;

    /** Border mode */
    private final Mode mode;                                   // CSOK: VisibilityModifier

    /**
     * Constructs a new BorderProps instance.
     * @param style border style (one of EN_*)
     * @param width border width
     * @param radiusStart radius of start corner in the direction perpendicular to border segment
     * @param radiusEnd radius of end corner in the direction perpendicular to border segment
     * @param color border color
     * @param mode border mode ((one of SEPARATE, COLLAPSE_INNER and COLLAPSE_OUTER)
     */
    public BorderProps(int style, int width, int radiusStart, int radiusEnd, Color color, Mode mode) {
        this.style = style;
        this.width = width;
        this.radiusStart = radiusStart;
        this.radiusEnd = radiusEnd;
        this.color = color;
        this.mode = mode;
    }

    /**
     * Factory method for a new BorderProps instance with rectangular corners.
     * @param style border style (one of EN_*)
     * @param width border width
     * @param color border color
     * @param mode border mode ((one of SEPARATE, COLLAPSE_INNER and COLLAPSE_OUTER)
     */
    public static BorderProps makeRectangular(int style, int width, Color color, Mode mode) {
        return new BorderProps(style, width, 0, 0, color, mode);
    }

    private BorderProps(String style, int width, int radiusStart, int radiusEnd, Color color, Mode mode) {
        this(getConstantForStyle(style), width, radiusStart, radiusEnd, color, mode);
    }

    /**
     *
     * @return the radius of the corner adjacent to the before or start border
     */
    public int getRadiusStart() {
        return radiusStart;
    }

    /**
     * @return the radius of the corner adjacent to the after or end border
     */
    public int getRadiusEnd() {
        return radiusEnd;
    }

    /**
     * @param bp the border properties or null
     * @return the effective width of the clipped part of the border
     */
    public static int getClippedWidth(BorderProps bp) {
        return bp == null ? 0 : bp.mode.getClippedWidth(bp);
    }

    private String getStyleString() {
        return BorderStyle.valueOf(style).getName();
    }

    private static int getConstantForStyle(String style) {
        return BorderStyle.valueOf(style).getEnumValue();
    }

    public boolean isCollapseOuter() {
        return mode == Mode.COLLAPSE_OUTER;
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
        return BorderPropsDeserializer.INSTANCE.valueOf(foUserAgent, s);
    }
    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append('(')
                .append(getStyleString()).append(',')
                .append(ColorUtil.colorToString(color)).append(',')
                .append(width);
        if (!mode.equals(Mode.SEPARATE)) {
            sbuf.append(",").append(mode.value);
        }

        if (radiusStart != 0 || radiusEnd != 0) {
            if (mode.equals(Mode.SEPARATE)) {
                // Because of the corner radii properties the mode must be set
                // so that the parameter index is consistent
                sbuf.append(",").append(Mode.SEPARATE.value);
            }
            sbuf.append(',').append(radiusStart)
            .append(',').append(radiusEnd);
        }
        sbuf.append(')');
        return sbuf.toString();
    }

    private static class BorderPropsDeserializer {

        private static final BorderPropsDeserializer INSTANCE = new BorderPropsDeserializer();

        private static final Pattern PATTERN = Pattern.compile("([^,\\(]+(?:\\(.*\\))?)");

        private BorderPropsDeserializer() {
        }

        public BorderProps valueOf(FOUserAgent foUserAgent, String s) {
            if (s.startsWith("(") && s.endsWith(")")) {
                s = s.substring(1, s.length() - 1);
                Matcher m = PATTERN.matcher(s);
                m.find();
                String style = m.group();
                m.find();
                String color = m.group();
                m.find();
                int width = Integer.parseInt(m.group());
                Mode mode = Mode.SEPARATE;
                if ( m.find()) {
                    String ms = m.group();
                    if (Mode.COLLAPSE_INNER.value.equalsIgnoreCase(ms)) {
                        mode = Mode.COLLAPSE_INNER;
                    } else if (Mode.COLLAPSE_OUTER.value.equalsIgnoreCase(ms)) {
                        mode = Mode.COLLAPSE_OUTER;
                    }
                }
                Color c;
                try {
                    c = ColorUtil.parseColorString(foUserAgent, color);
                } catch (PropertyException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
                int startRadius = 0;
                int endRadius = 0;
                if (m.find()) {
                    startRadius = Integer.parseInt(m.group());
                    m.find();
                    endRadius = Integer.parseInt(m.group());
                }
                return new BorderProps(style, width, startRadius, endRadius, c, mode);
            } else {
                throw new IllegalArgumentException("BorderProps must be surrounded by parentheses");
            }
        }
    }

}
