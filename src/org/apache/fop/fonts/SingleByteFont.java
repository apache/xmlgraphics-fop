/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

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
     * @see org.apache.fop.fonts.Font#getEncoding()
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
     * @see org.apache.fop.fonts.Font#mapChar(char)
     */
    public char mapChar(char c) {
        char d = mapping.mapChar(c);
        if (d != 0) {
            return d;
        } else {
            return '#';
        }
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

