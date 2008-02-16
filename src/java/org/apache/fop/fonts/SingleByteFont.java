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

package org.apache.fop.fonts;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.fonts.Glyphs;

/**
 * Generic SingleByte font
 */
public class SingleByteFont extends CustomFont {

    /** logger */
    private  static Log log = LogFactory.getLog(SingleByteFont.class);

    private CodePointMapping mapping;

    private int[] width = null;

    private Set warnedChars;
    
    /**
     * Main constructor.
     */
    public SingleByteFont() {
        setEncoding(CodePointMapping.WIN_ANSI_ENCODING);
    }
    
    /** {@inheritDoc} */
    public boolean isEmbeddable() {
        return (getEmbedFileName() == null && getEmbedResourceName() == null) ? false
               : true;
    }

    /** {@inheritDoc} */
    public String getEncoding() {
        return this.mapping.getName();
    }

    /**
     * Returns the code point mapping (encoding) of this font.
     * @return the code point mapping
     */
    public CodePointMapping getCodePointMapping() {
        return this.mapping;
    }
    
    /** {@inheritDoc} */
    public int getWidth(int i, int size) {
        int idx = i - getFirstChar();
        if (idx >= 0 && idx < width.length) {
            return size * width[i - getFirstChar()];
        } else {
            return 0;
        }
    }

    /** {@inheritDoc} */
    public int[] getWidths() {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length - 1);
        return arr;
    }

    /** {@inheritDoc} */
    public char mapChar(char c) {
        notifyMapOperation();
        char d = mapping.mapChar(c);
        if (d != 0) {
            return d;
        } else {
            Character ch = new Character(c);
            if (warnedChars == null) {
                warnedChars = new java.util.HashSet();
            }
            if (warnedChars.size() < 8 && !warnedChars.contains(ch)) {
                warnedChars.add(ch);
                if (warnedChars.size() == 8) {
                    log.warn("Many requested glyphs are not available in font " + getFontName());
                } else {
                    log.warn("Glyph " + (int)c + " (0x" + Integer.toHexString(c) 
                            + ", " + Glyphs.charToGlyphName(c)
                            + ") not available in font " + getFontName());
                }
            }
            return '#';
        }
    }

    /** {@inheritDoc} */
    public boolean hasChar(char c) {
        return (mapping.mapChar(c) > 0);
    }

    /* ---- single byte font specific setters --- */

    /**
     * Updates the mapping variable based on the encoding.
     * @param encoding the name of the encoding
     */
    protected void updateMapping(String encoding) {
        try {
            this.mapping = CodePointMapping.getMapping(encoding);
        } catch (UnsupportedOperationException e) {
            log.error("Font '" + super.getFontName() + "': " + e.getMessage());
        }
    }
    
    /**
     * Sets the encoding of the font.
     * @param encoding the encoding (ex. "WinAnsiEncoding" or "SymbolEncoding")
     */
    public void setEncoding(String encoding) {
        updateMapping(encoding);
    }
    
    /**
     * Sets the encoding of the font.
     * @param encoding the encoding information
     */
    public void setEncoding(CodePointMapping encoding) {
        this.mapping = encoding;
    }

    /**
     * Sets a width for a character.
     * @param index index of the character
     * @param width the width of the character
     */
    public void setWidth(int index, int width) {
        if (this.width == null) {
            this.width = new int[getLastChar() - getFirstChar() + 1];
        }
        this.width[index - getFirstChar()] = width;
    }

}

