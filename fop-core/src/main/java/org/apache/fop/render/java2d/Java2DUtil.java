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

import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.util.Arrays;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.util.CharUtilities;

/**
 * Rendering-related utilities for Java2D.
 */
public final class Java2DUtil {

    private Java2DUtil() {
    }

    /**
     * Builds a default {@link FontInfo} object for use with output formats using the Java2D
     * font setup.
     * @param fontInfo the font info object to populate
     * @param userAgent the user agent
     * @return the populated font information object
     */
    public static FontInfo buildDefaultJava2DBasedFontInfo(
            FontInfo fontInfo, FOUserAgent userAgent) {
        Java2DFontMetrics java2DFontMetrics = new Java2DFontMetrics();

        FontManager fontManager = userAgent.getFontManager();
        FontCollection[] fontCollections = new FontCollection[] {
                new org.apache.fop.render.java2d.Base14FontCollection(java2DFontMetrics),
                new InstalledFontCollection(java2DFontMetrics)
        };

        FontInfo fi = (fontInfo != null ? fontInfo : new FontInfo());
        fi.setEventListener(new FontEventAdapter(userAgent.getEventBroadcaster()));
        fontManager.setup(fi, fontCollections);
        return fi;
    }

    /**
     * Creates an instance of {@link GlyphVector} that correctly handle surrogate pairs and advanced font features such
     * as GSUB/GPOS/GDEF.
     *
     * @param text Text to render
     * @param g2d  the target Graphics2D instance
     * @param font the font instance
     * @param fontInfo the font information
     * @return an instance of {@link GlyphVector}
     */
    public static GlyphVector createGlyphVector(String text, Graphics2D g2d, Font font, FontInfo fontInfo) {
        MultiByteFont multiByteFont = getMultiByteFont(font.getFontName(), fontInfo);

        if (multiByteFont == null) {
            return createGlyphVector(text, g2d);
        }

        return createGlyphVectorMultiByteFont(text, g2d, multiByteFont);
    }

    /**
     * Creates a {@link GlyphVector} using characters. Filters out non-bmp characters.
     */
    private static GlyphVector createGlyphVector(String text, Graphics2D g2d) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int cp : CharUtilities.codepointsIter(text)) {
            // If we are here we probably do not support non-BMP codepoints
            sb.appendCodePoint(cp <= 0xFFFF ? cp : Typeface.NOT_FOUND);
        }
        return g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), sb.toString());
    }

    /**
     * Creates a {@link GlyphVector} using glyph indexes instead of characters. To correctly support the advanced font
     * features we have to build the GlyphVector passing the glyph indexes instead of the characters. This because some
     * of the chars in text might have been replaced by an internal font representation during
     * GlyphMapping.processWordMapping. Eg 'fi' replaced with the corresponding character in the font ligatures table
     * (GSUB).
     */
    private static GlyphVector createGlyphVectorMultiByteFont(String text, Graphics2D g2d,
            MultiByteFont multiByteFont) {
        int[] glyphCodes = new int[text.length()];
        int currentIdx = 0;

        for (int cp : CharUtilities.codepointsIter(text)) {
            // mapChar is not working here because MultiByteFont.mapChar replaces the glyph index with
            // CIDSet.mapChar when isEmbeddable == true.
            glyphCodes[currentIdx++] = multiByteFont.findGlyphIndex(cp);
        }

        // Trims glyphCodes
        if (currentIdx != text.length()) {
            glyphCodes = Arrays.copyOf(glyphCodes, currentIdx);
        }

        return g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), glyphCodes);
    }

    /**
     * Returns an instance of {@link MultiByteFont} for the given font name. This method will try to unwrap containers
     * such as {@link CustomFontMetricsMapper} and {@link LazyFont}
     *
     * @param fontName font key
     * @param fontInfo font information
     * @return An instance of {@link MultiByteFont} or null if it
     */
    private static MultiByteFont getMultiByteFont(String fontName, FontInfo fontInfo) {
        Typeface tf = fontInfo.getFonts().get(fontName);

        if (tf instanceof CustomFontMetricsMapper) {
            tf = ((CustomFontMetricsMapper) tf).getRealFont();
        }

        if (tf instanceof LazyFont) {
            tf = ((LazyFont) tf).getRealFont();
        }

        return (tf instanceof MultiByteFont) ? (MultiByteFont) tf : null;
    }

}
