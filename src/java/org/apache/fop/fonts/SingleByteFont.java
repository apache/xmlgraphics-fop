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

/**
 * Generic SingleByte font
 */
public class SingleByteFont extends CustomFont {

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
     * @see org.apache.fop.fonts.FontDescriptor#isEmbeddable()
     */
    public boolean isEmbeddable() {
        return (getEmbedFileName() == null && getEmbedResourceName() == null) ? false
               : true;
    }

    /**
     * @see org.apache.fop.fonts.Typeface#getEncoding()
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
     * @see org.apache.fop.fonts.FontMetrics#getWidth(int, int)
     */
    public int getWidth(int i, int size) {
        return size * width[i];
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getWidths()
     */
    public int[] getWidths() {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length - 1);
        return arr;
    }

    /**
     * @see org.apache.fop.fonts.Typeface#mapChar(char)
     */
    public char mapChar(char c) {
        char d = mapping.mapChar(c);
        if (d != 0) {
            return d;
        } else {
            return '#';
        }
    }
    
    /**
     * @see org.apache.fop.fonts.Typeface#hasChar(char)
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

}

