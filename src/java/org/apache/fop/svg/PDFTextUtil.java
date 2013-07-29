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

package org.apache.fop.svg;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.Typeface;

/**
 * Utility class for generating PDF text objects. It needs to be subclassed to add writing
 * functionality (see {@link #write(String)}).
 */
public abstract class PDFTextUtil extends org.apache.fop.pdf.PDFTextUtil {

    private FontInfo fontInfo;
    private Font font;
    private int encoding;

    /**
     * Main constructor.
     * @param fontInfo the font catalog
     */
    public PDFTextUtil(FontInfo fontInfo) {
        super();
        this.fontInfo = fontInfo;
    }

    /** {@inheritDoc} */
    protected void initValues() {
        super.initValues();
        this.font = null;
    }

    /**
     * Returns the current font in use.
     * @return the current font or null if no font is currently active.
     */
    public Font getCurrentFont() {
        return this.font;
    }

    /**
     * Returns the current encoding.
     * @return the current encoding
     */
    public int getCurrentEncoding() {
        return this.encoding;
    }

    /**
     * Sets the current font.
     * @param f the new font to use
     */
    public void setCurrentFont(Font f) {
        this.font = f;
    }

    /**
     * Sets the current encoding.
     * @param encoding the new encoding
     */
    public void setCurrentEncoding(int encoding) {
        this.encoding = encoding;
    }

    /**
     * Determines whether the font with the given name is a multi-byte font.
     * @param name the name of the font
     * @return true if it's a multi-byte font
     */
    protected boolean isMultiByteFont(String name) {
        Typeface f = fontInfo.getFonts().get(name);
        return f.isMultiByte();
    }

    /**
     * Writes a "Tf" command, setting a new current font.
     * @param f the font to select
     */
    public void writeTf(Font f) {
        String fontName = f.getFontName();
        float fontSize = (float)f.getFontSize() / 1000f;
        boolean isMultiByte = isMultiByteFont(fontName);
        if (!isMultiByte && encoding != 0) {
            updateTf(fontName + "_" + Integer.toString(encoding), fontSize, isMultiByte);
        } else {
            updateTf(fontName, fontSize, isMultiByte);
        }
    }

}
