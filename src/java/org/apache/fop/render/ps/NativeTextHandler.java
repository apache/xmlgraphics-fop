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

package org.apache.fop.render.ps;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.java2d.ps.PSTextHandler;
import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.fonts.FontTriplet;

/**
 * Specialized TextHandler implementation that the PSGraphics2D class delegates to to paint text
 * using PostScript text operations.
 */
public class NativeTextHandler implements PSTextHandler {

    private PSGraphics2D rootG2D;

    /** FontInfo containing all available fonts */
    protected FontInfo fontInfo;

    /** Currently valid Font */
    protected Font font;

    /** Overriding FontState */
    protected Font overrideFont = null;

    /** the current (internal) font name */
    protected String currentFontName;

    /** the current font size in millipoints */
    protected int currentFontSize;

    /**
     * Main constructor.
     * @param g2d the PSGraphics2D instance this instances is used by
     * @param fontInfo the FontInfo object with all available fonts
     */
    public NativeTextHandler(PSGraphics2D g2d, FontInfo fontInfo) {
        this.rootG2D = g2d;
        if (fontInfo != null) {
            this.fontInfo = fontInfo;
        } else {
            setupFontInfo();
        }
    }

    private void setupFontInfo() {
        //Sets up a FontInfo with default fonts
        fontInfo = new FontInfo();
        boolean base14Kerning = false;
        FontSetup.setup(fontInfo, base14Kerning);
    }

    /**
     * Return the font information associated with this object
     * @return the FontInfo object
     */
    public FontInfo getFontInfo() {
        return fontInfo;
    }

    private PSGenerator getPSGenerator() {
        return this.rootG2D.getPSGenerator();
    }

    /** {@inheritDoc} */
    public void writeSetup() throws IOException {
        if (fontInfo != null) {
            PSFontUtils.writeFontDict(getPSGenerator(), fontInfo);
        }
    }

    /** {@inheritDoc} */
    public void writePageSetup() throws IOException {
        //nop
    }

    /** {@inheritDoc} */
    public void drawString(String text, float x, float y) throws IOException {
        // TODO Remove me after removing the deprecated method in TextHandler.
        throw new UnsupportedOperationException("Deprecated method!");
    }

    /**
     * Draw a string to the PostScript document. The text is painted using
     * text operations.
     * {@inheritDoc}
     */
    public void drawString(Graphics2D g, String s, float x, float y) throws IOException {
        PSGraphics2D g2d = (PSGraphics2D)g;
        g2d.preparePainting();
        if (this.overrideFont == null) {
            java.awt.Font awtFont = g2d.getFont();
            this.font = createFont(awtFont);
        } else {
            this.font = this.overrideFont;
            this.overrideFont = null;
        }

        //Color and Font state
        g2d.establishColor(g2d.getColor());
        establishCurrentFont();

        PSGenerator gen = getPSGenerator();
        gen.saveGraphicsState();

        //Clip
        Shape imclip = g2d.getClip();
        g2d.writeClip(imclip);

        //Prepare correct transformation
        AffineTransform trans = g2d.getTransform();
        gen.concatMatrix(trans);
        gen.writeln(gen.formatDouble(x) + " "
                  + gen.formatDouble(y) + " moveto ");
        gen.writeln("1 -1 scale");

        StringBuffer sb = new StringBuffer("(");
        escapeText(s, sb);
        sb.append(") t ");

        gen.writeln(sb.toString());

        gen.restoreGraphicsState();
    }

    private void escapeText(final String text, StringBuffer target) {
        final int l = text.length();
        for (int i = 0; i < l; i++) {
            final char ch = text.charAt(i);
            final char mch = this.font.mapChar(ch);
            PSGenerator.escapeChar(mch, target);
        }
    }

    private Font createFont(java.awt.Font f) {
        return fontInfo.getFontInstanceForAWTFont(f);
    }

    private void establishCurrentFont() throws IOException {
        if ((currentFontName != this.font.getFontName())
                || (currentFontSize != this.font.getFontSize())) {
            PSGenerator gen = getPSGenerator();
            gen.writeln(this.font.getFontName() + " "
                    + gen.formatDouble(font.getFontSize() / 1000f) + " F");
            currentFontName = this.font.getFontName();
            currentFontSize = this.font.getFontSize();
        }
    }

    /**
     * Sets the overriding font.
     * @param override Overriding Font to set
     */
    public void setOverrideFont(Font override) {
        this.overrideFont = override;
    }


}
