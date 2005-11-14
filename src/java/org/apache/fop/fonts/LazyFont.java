/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is used to defer the loading of a font until it is really used.
 */
public class LazyFont extends Typeface implements FontDescriptor {

    private static Log log = LogFactory.getLog(LazyFont.class);
    
    private String metricsFileName = null;
    private String fontEmbedPath = null;
    private boolean useKerning = false;

    private boolean isMetricsLoaded = false;
    private Typeface realFont = null;
    private FontDescriptor realFontDescriptor = null;

    /**
     * Main constructor
     * @param fontEmbedPath path to embeddable file (may be null)
     * @param metricsFileName path to the metrics XML file
     * @param useKerning True, if kerning should be enabled
     */
    public LazyFont(String fontEmbedPath, String metricsFileName, boolean useKerning) {
        this.metricsFileName = metricsFileName;
        this.fontEmbedPath = fontEmbedPath;
        this.useKerning = useKerning;
    }

    private void load() {
        if (!isMetricsLoaded) {
            isMetricsLoaded = true;
            try {
                /**@todo Possible thread problem here */

                FontReader reader = new FontReader(metricsFileName);
                reader.setKerningEnabled(useKerning);
                reader.setFontEmbedPath(fontEmbedPath);
                realFont = reader.getFont();
                if (realFont instanceof FontDescriptor) {
                    realFontDescriptor = (FontDescriptor) realFont;
                }
                // log.debug("Metrics " + metricsFileName + " loaded.");
            } catch (Exception ex) {
                log.error("Failed to read font metrics file "
                                     + metricsFileName, ex);
            }
        }
    }

    /**
     * Gets the real font.
     * @return the real font
     */
    public Typeface getRealFont() {
        load();
        return realFont;
    }

    // ---- Font ----
    /**
     * @see org.apache.fop.fonts.Typeface#getEncoding()
     */
    public String getEncoding() {
        load();
        return realFont.getEncoding();
    }

    /**
     * @see org.apache.fop.fonts.Typeface#mapChar(char)
     */
    public char mapChar(char c) {
        load();
        return realFont.mapChar(c);
    }

    /**
     * @see org.apache.fop.fonts.Typeface#hasChar(char)
     */
    public boolean hasChar(char c) {
        load();
        return realFont.hasChar(c);
    }

    /**
     * @see org.apache.fop.fonts.Typeface#isMultiByte()
     */
    public boolean isMultiByte() {
        return realFont.isMultiByte();
    }

    // ---- FontMetrics interface ----
    /**
     * @see org.apache.fop.fonts.FontMetrics#getFontName()
     */
    public String getFontName() {
        load();
        return realFont.getFontName();
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getAscender(int)
     */
    public int getAscender(int size) {
        load();
        return realFont.getAscender(size);
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getCapHeight(int)
     */
    public int getCapHeight(int size) {
        load();
        return realFont.getCapHeight(size);
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getDescender(int)
     */
    public int getDescender(int size) {
        load();
        return realFont.getDescender(size);
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getXHeight(int)
     */
    public int getXHeight(int size) {
        load();
        return realFont.getXHeight(size);
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getWidth(int, int)
     */
    public int getWidth(int i, int size) {
        load();
        return realFont.getWidth(i, size);
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getWidths()
     */
    public int[] getWidths() {
        load();
        return realFont.getWidths();
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#hasKerningInfo()
     */
    public boolean hasKerningInfo() {
        load();
        return realFont.hasKerningInfo();
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getKerningInfo()
     */
    public Map getKerningInfo() {
        load();
        return realFont.getKerningInfo();
    }

    // ---- FontDescriptor interface ----
    /**
     * @see org.apache.fop.fonts.FontDescriptor#getCapHeight()
     */
    public int getCapHeight() {
        load();
        return realFontDescriptor.getCapHeight();
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getDescender()
     */
    public int getDescender() {
        load();
        return realFontDescriptor.getDescender();
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getAscender()
     */
    public int getAscender() {
        load();
        return realFontDescriptor.getAscender();
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getFlags()
     */
    public int getFlags() {
        load();
        return realFontDescriptor.getFlags();
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getFontBBox()
     */
    public int[] getFontBBox() {
        load();
        return realFontDescriptor.getFontBBox();
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getItalicAngle()
     */
    public int getItalicAngle() {
        load();
        return realFontDescriptor.getItalicAngle();
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getStemV()
     */
    public int getStemV() {
        load();
        return realFontDescriptor.getStemV();
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getFontType()
     */
    public FontType getFontType() {
        load();
        return realFontDescriptor.getFontType();
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#isEmbeddable()
     */
    public boolean isEmbeddable() {
        load();
        return realFontDescriptor.isEmbeddable();
    }

}

