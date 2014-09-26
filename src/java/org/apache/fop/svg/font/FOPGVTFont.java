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

package org.apache.fop.svg.font;

import java.awt.font.FontRenderContext;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.FontTriplet;

public class FOPGVTFont implements GVTFont {

    private final Font font;

    private final GVTFontFamily fontFamily;

    public FOPGVTFont(Font font, GVTFontFamily fontFamily) {
        this.font = font;
        this.fontFamily = fontFamily;
    }

    public Font getFont() {
        return font;
    }

    public boolean canDisplay(char c) {
        return font.hasChar(c);
    }

    public int canDisplayUpTo(char[] text, int start, int limit) {
        for (int i = start; i < limit; i++) {
            if (!canDisplay(text[i])) {
                return i;
            }
        }
        return -1;
    }


    public int canDisplayUpTo(CharacterIterator iter, int start, int limit) {
        for (char c = iter.setIndex(start); iter.getIndex() < limit; c = iter.next()) {
            if (!canDisplay(c)) {
                return iter.getIndex();
            }
        }
        return -1;
    }

    public int canDisplayUpTo(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!canDisplay(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    public GVTGlyphVector createGlyphVector(FontRenderContext frc, char[] chars) {
        return createGlyphVector(frc, new String(chars));
    }

    public GVTGlyphVector createGlyphVector(FontRenderContext frc, CharacterIterator ci) {
        if (!font.performsSubstitution() && !font.performsPositioning()) {
            return new FOPGVTGlyphVector(this, ci, frc);
        } else {
            return new ComplexGlyphVector(this, ci, frc);
        }
    }

    public GVTGlyphVector createGlyphVector(FontRenderContext frc,
            int[] glyphCodes,
            CharacterIterator ci) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public GVTGlyphVector createGlyphVector(FontRenderContext frc, String text) {
        StringCharacterIterator sci = new StringCharacterIterator(text);
        return createGlyphVector(frc, sci);
    }

    public GVTGlyphVector createGlyphVector(FontRenderContext frc, String text, String script, String language) {
        if ((script != null) || (language != null)) {
            AttributedString as = new AttributedString(text);
            if (script != null) {
                as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.SCRIPT, script);
            }
            if (language != null) {
                as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.LANGUAGE, language);
            }
            return createGlyphVector(frc, as.getIterator());
        } else {
            return createGlyphVector(frc, text);
        }
    }

    public FOPGVTFont deriveFont(float size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public FontInfo getFontInfo() {
        return ((FOPGVTFontFamily) fontFamily).getFontInfo();
    }

    public String getFontKey() {
        return font.getFontName();
    }

    public FontTriplet getFontTriplet() {
        return font.getFontTriplet();
    }

    public String getFamilyName() {
        return fontFamily.getFamilyName();
    }

    public GVTLineMetrics getLineMetrics(char[] chars, int beginIndex, int limit, FontRenderContext frc) {
        return getLineMetrics(limit - beginIndex);
    }

    GVTLineMetrics getLineMetrics(int numChars) {
        numChars = numChars < 0 ? 0 : numChars;
        FontMetrics metrics = font.getFontMetrics();
        int size = font.getFontSize();
        return new GVTLineMetrics(
                metrics.getCapHeight(size) / 1000000f,
                java.awt.Font.ROMAN_BASELINE, // Not actually used by Batik
                null, // Not actually used by Batik
                -metrics.getDescender(size) / 1000000f,
                0, // Not actually used by Batik
                0, // Not actually used by Batik
                numChars,
                -metrics.getStrikeoutPosition(size) / 1000000f,
                metrics.getStrikeoutThickness(size) / 1000000f,
                -metrics.getUnderlinePosition(size) / 1000000f,
                metrics.getUnderlineThickness(size) / 1000000f,
                -metrics.getCapHeight(size) / 1000000f, // Because this is what Batik does in GVTLineMetrics
                metrics.getUnderlineThickness(size) / 1000000f);
    }

    public GVTLineMetrics getLineMetrics(CharacterIterator ci, int beginIndex, int limit,
            FontRenderContext frc) {
        return getLineMetrics(limit - beginIndex);
    }

    public GVTLineMetrics getLineMetrics(String str, FontRenderContext frc) {
        return getLineMetrics(str.length());
    }

    public GVTLineMetrics getLineMetrics(String str, int beginIndex, int limit, FontRenderContext frc) {
        return getLineMetrics(limit - beginIndex);
    }

    public float getSize() {
        return font.getFontSize() / 1000f;
    }

    public float getVKern(int glyphCode1, int glyphCode2) {
        return 0;
    }

    public float getHKern(int glyphCode1, int glyphCode2) {
        // TODO Cannot be implemented until getKernValue takes glyph indices instead of character codes
        return 0;
    }

}
