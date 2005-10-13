/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

    private final CodePointMapping mapping
        = CodePointMapping.getMapping("WinAnsiEncoding");

    private String encoding = "WinAnsiEncoding";

    private int width[] = null;


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
        /*
        for (int i = 0; i < arr.length; i++)
            arr[i] *= size;
        */
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

