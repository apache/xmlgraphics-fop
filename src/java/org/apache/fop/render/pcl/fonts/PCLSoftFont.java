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

package org.apache.fop.render.pcl.fonts;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OpenFont;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;

public class PCLSoftFont {
    private int fontID;
    private Typeface font;
    private Map<Integer, int[]> charOffsets;
    private OpenFont openFont;
    private InputStream fontStream;
    private FontFileReader reader;
    /** Map containing unicode character and it's soft font codepoint **/
    private Map<Integer, Integer> charsWritten;
    private Map<Character, Integer> mappedChars;
    private Map<Integer, Integer> charMtxPositions;
    private boolean multiByteFont;
    private int charCount = 32;

    public PCLSoftFont(int fontID, Typeface font, boolean multiByteFont) {
        this.fontID = fontID;
        this.font = font;
        charsWritten = new HashMap<Integer, Integer>();
        mappedChars = new HashMap<Character, Integer>();
        this.multiByteFont = multiByteFont;
    }

    public Typeface getTypeface() {
        return font;
    }

    public int getFontID() {
        return fontID;
    }

    public void setCharacterOffsets(Map<Integer, int[]> charOffsets) {
        this.charOffsets = charOffsets;
    }

    public Map<Integer, int[]> getCharacterOffsets() {
        return charOffsets;
    }

    public OpenFont getOpenFont() {
        return openFont;
    }

    public void setOpenFont(OpenFont openFont) {
        this.openFont = openFont;
    }

    public InputStream getFontStream() {
        return fontStream;
    }

    public void setFontStream(InputStream fontStream) {
        this.fontStream = fontStream;
    }

    public FontFileReader getReader() {
        return reader;
    }

    public void setReader(FontFileReader reader) {
        this.reader = reader;
    }

    public void writeCharacter(int unicode) {
        charsWritten.put(unicode, charCount++);
    }

    public int getUnicodeCodePoint(int unicode) {
        if (charsWritten.containsKey(unicode)) {
            return charsWritten.get(unicode);
        } else {
            return -1;
        }
    }

    public boolean hasPreviouslyWritten(int unicode) {
        return charsWritten.containsKey(unicode);
    }

    public int getMtxCharIndex(int unicode) {
        if (charMtxPositions.get(unicode) != null) {
            return charMtxPositions.get(unicode);
        }
        return 0;
    }

    public int getCmapGlyphIndex(int unicode) {
        if (font instanceof CustomFontMetricsMapper) {
            CustomFontMetricsMapper customFont = (CustomFontMetricsMapper) font;
            Typeface realFont = customFont.getRealFont();
            if (realFont instanceof MultiByteFont) {
                MultiByteFont mbFont = (MultiByteFont) realFont;
                return mbFont.findGlyphIndex(unicode);
            }
        }
        return 0;
    }

    public void setMtxCharIndexes(Map<Integer, Integer> charMtxPositions) {
        this.charMtxPositions = charMtxPositions;
    }

    public int getCharCount() {
        return charCount;
    }

    public void setMappedChars(Map<Character, Integer> mappedChars) {
        this.mappedChars = mappedChars;
    }

    public Map<Character, Integer> getMappedChars() {
        return mappedChars;
    }

    public int getCharIndex(char ch) {
        if (mappedChars.containsKey(ch)) {
            return mappedChars.get(ch);
        } else {
            return -1;
        }
    }

    public int getCharCode(char ch) {
        if (multiByteFont) {
            return getCharIndex(ch);
        } else {
            return getUnicodeCodePoint(ch);
        }
    }
}
