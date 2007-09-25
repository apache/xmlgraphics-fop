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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generic SingleByte font
 */
public class SingleByteFont extends CustomFont {

    /** logger */
    private  static Log log = LogFactory.getLog(SingleByteFont.class);

    private CodePointMapping mapping;

    private String encoding = "WinAnsiEncoding";

    private int[] width = null;

    /**
     * Main constructor.
     */
    public SingleByteFont() {
        updateMapping();
    }
    
    /**
     * Updates the mapping variable based on the encoding.
     */
    protected void updateMapping() {
        mapping = CodePointMapping.getMapping(getEncoding()); 
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isEmbeddable() {
        return (getEmbedFileName() == null && getEmbedResourceName() == null) ? false
               : true;
    }

    /**
     * {@inheritDoc}
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the encoding of the font.
     * @param encoding the encoding (ex. "WinAnsiEncoding" or "SymbolEncoding")
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
        updateMapping();
    }

    /**
     * {@inheritDoc} 
     */
    public int getWidth(int i, int size) {
        return size * width[i];
    }

    /**
     * {@inheritDoc}
     */
    public int[] getWidths() {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length - 1);
        return arr;
    }

    /**
     * {@inheritDoc}
     */
    public char mapChar(char c) {
        char d = mapping.mapChar(c);
        if (d != 0) {
            return d;
        } else {
            log.warn("Glyph " + (int) c + " not available in font "
                    + getFontName());
            return '#';
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChar(char c) {
        return (mapping.mapChar(c) > 0);
    }

    /* ---- single byte font specific setters --- */

    /**
     * Sets a width for a character.
     * @param index index of the character
     * @param width the width of the character
     */
    public void setWidth(int index, int width) {
        if (this.width == null) {
            this.width = new int[256];
        }
        this.width[index] = width;
    }

    public char[] getCharsUsed() {
        return null;
    }
}

