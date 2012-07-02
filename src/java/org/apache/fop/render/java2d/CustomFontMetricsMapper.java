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

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;

/**
 * FontMetricsMapper that delegates most methods to an underlying
 * {@link org.apache.fop.fonts.FontMetrics} instance. This class was designed to allow
 * the underlying {@link Font} to be loaded from a
 * user-configured file not registered in the current graphics environment.
 */
public class CustomFontMetricsMapper extends Typeface implements FontMetricsMapper {

    /**
     * Font metrics for the font this class models.
     */
    private Typeface typeface;

    /**
     * The font required by the Java2D renderer.
     */
    private java.awt.Font font;

    /**
     * Maintains the most recently requested size.
     */
    private float size = 1;

    /**
     * Construction of this class results in the immediate construction
     * of the underlying {@link java.awt.Font}.
     * @param fontMetrics the metrics of the custom font
     * @throws FontFormatException if a bad font is loaded
     * @throws IOException if an I/O error occurs
     */
    public CustomFontMetricsMapper(final CustomFont fontMetrics)
            throws FontFormatException, IOException {
        this.typeface = fontMetrics;
        initialize(fontMetrics.getEmbedFileSource());
    }

    /**
     * Construction of this class results in the immediate construction
     * of the underlying {@link java.awt.Font}.
     * @param fontMetrics the font
     * @param fontSource the font source to access the font
     * @throws FontFormatException if a bad font is loaded
     * @throws IOException if an I/O error occurs
     */
    public CustomFontMetricsMapper(final LazyFont fontMetrics, final Source fontSource)
            throws FontFormatException, IOException {
        this.typeface = fontMetrics;
        initialize(fontSource);
    }

    private static final int TYPE1_FONT = 1; //Defined in Java 1.5

    /**
     * Loads the java.awt.Font
     * @param source
     * @throws FontFormatException
     * @throws IOException
     */
    private void initialize(final Source source)
                throws FontFormatException, IOException {
        int type = Font.TRUETYPE_FONT;
        if (FontType.TYPE1.equals(typeface.getFontType())) {
            type = TYPE1_FONT; //Font.TYPE1_FONT; only available in Java 1.5
        }

        InputStream is = null;
        if (source instanceof StreamSource) {
            is = ((StreamSource) source).getInputStream();
        } else if (source.getSystemId() != null) {
            is = new java.net.URL(source.getSystemId()).openStream();
        } else {
            throw new IllegalArgumentException("No font source provided.");
        }

        this.font = Font.createFont(type, is);
        is.close();

    }

    /** {@inheritDoc} */
    public final String getEncodingName() {
        return null; //Not applicable to Java2D rendering
    }

    /** {@inheritDoc} */
    public final boolean hasChar(final char c) {
        return font.canDisplay(c);
    }

    /** {@inheritDoc} */
    public final char mapChar(final char c) {
        return typeface.mapChar(c);
    }

    /** {@inheritDoc} */
    public final Font getFont(final int size) {
        if (this.size == size) {
            return font;
        }

        this.size = size / 1000f;
        font = font.deriveFont(this.size);
        return font;
    }

    /** {@inheritDoc} */
    public final int getAscender(final int size) {
        return typeface.getAscender(size);
    }

    /** {@inheritDoc} */
    public final int getCapHeight(final int size) {
        return typeface.getCapHeight(size);
    }

    /** {@inheritDoc} */
    public final int getDescender(final int size) {
        return typeface.getDescender(size);
    }

    /** {@inheritDoc} */
    public final String getEmbedFontName() {
        return typeface.getEmbedFontName();
    }

    /** {@inheritDoc} */
    public final Set<String> getFamilyNames() {
        return typeface.getFamilyNames();
    }

    /** {@inheritDoc} */
    public final String getFontName() {
        return typeface.getFontName();
    }

    /** {@inheritDoc} */
    public final FontType getFontType() {
        return typeface.getFontType();
    }

    /** {@inheritDoc} */
    public final String getFullName() {
        return typeface.getFullName();
    }

    /** {@inheritDoc} */
    public final Map getKerningInfo() {
        return typeface.getKerningInfo();
    }

    /** {@inheritDoc} */
    public final int getWidth(final int i, final int size) {
        return typeface.getWidth(i, size);
    }

    /** {@inheritDoc} */
    public final int[] getWidths() {
        return typeface.getWidths();
    }

    /** {@inheritDoc} */
    public final int getXHeight(final int size) {
        return typeface.getXHeight(size);
    }

    /** {@inheritDoc} */
    public final boolean hasKerningInfo() {
        return typeface.hasKerningInfo();
    }

}
