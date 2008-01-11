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
 
package org.apache.fop.render.java2d;

// Java
import java.awt.Graphics2D;
import java.util.Map;
import java.util.Set;

import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.Typeface;


/**
 * This class implements org.apache.fop.layout.FontMetrics and
 * is added to the hash table in FontInfo. It  deferes the
 * actual calculation of the metrics to
 * Java2DFontMetrics.  It only keeps the java name and
 * style as member varibles
 */

public class SystemFontMetricsMapper extends Typeface implements FontMetricsMapper {

    /**
     * This is a Java2DFontMetrics that does the real calculation.
     * It is only one class that dynamically determines the font-size.
     */
    private static Java2DFontMetrics metric = null;

    /**
     * The java name of the font.
     * #  Make the family name immutable.
     */
    private final String family;

    /**
     * The java style of the font.
     * #  Make the style immutable.
     */
    private final int style;

    /**
     * Constructs a new Font-metrics.
     * @param family the family name of the font (java value)
     * @param style the java type style value of the font
     * @param graphics a Graphics2D object - this is needed  so
     * that we can get an instance of java.awt.FontMetrics
     */
    public SystemFontMetricsMapper(String family, int style, Graphics2D graphics) {
        this.family = family;
        this.style = style;
        if (metric == null) {
            metric = new Java2DFontMetrics(graphics);
        }
    }

    /** {@inheritDoc} */
    public String getFontName() {
        return family;
    }

    /** {@inheritDoc} */
    public String getEmbedFontName() {
        return getFontName();
    }

    /** {@inheritDoc} */
    public String getFullName() {
        return getFontName();
    }

    /** {@inheritDoc} */
    public Set getFamilyNames() {
        Set s = new java.util.HashSet();
        s.add(this.family);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    public FontType getFontType() {
        return FontType.OTHER;
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxAscent(int size) {
        return metric.getMaxAscent(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public int getAscender(int size) {
        return metric.getAscender(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public int getCapHeight(int size) {
        return metric.getCapHeight(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public int getDescender(int size) {
        return metric.getDescender(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public int getXHeight(int size) {
        return metric.getXHeight(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public int getWidth(int i, int size) {
        return metric.width(i, family, style, size);
    }


    /**
     * {@inheritDoc}
     */
    public int[] getWidths() {
        return metric.getWidths(family, style, Java2DFontMetrics.FONT_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    public java.awt.Font getFont(int size) {
        return metric.getFont(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public Map getKerningInfo() {
        return java.util.Collections.EMPTY_MAP;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasKerningInfo() {
        return false;
    }

    /** {@inheritDoc} */
    public String getEncoding() {
        return null; //Not applicable to Java2D rendering
    }

    /** {@inheritDoc} */
    public char mapChar(char c) {
        return c;
    }

    /** {@inheritDoc} */
    public boolean hasChar(char c) {
        return metric.hasChar(family, style, Java2DFontMetrics.FONT_SIZE, c);
    }

}





