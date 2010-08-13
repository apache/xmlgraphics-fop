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

package org.apache.fop.afp.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.AFPFontAttributes;
import org.apache.fop.afp.fonts.AFPPageFonts;
import org.apache.fop.afp.modca.GraphicsObject;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.FOPTextHandler;

/**
 * Specialized TextHandler implementation that the AFPGraphics2D class delegates to to paint text
 * using AFP GOCA text operations.
 */
public class AFPTextHandler implements FOPTextHandler {

    /** logging instance */
    private static Log log = LogFactory.getLog(AFPTextHandler.class);

    /** Overriding FontState */
    protected Font overrideFont = null;

    /** Font information */
    private final FontInfo fontInfo;

    /**
     * Main constructor.
     *
     * @param fontInfo the AFPGraphics2D instance
     */
    public AFPTextHandler(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
    }

    /**
     * Return the font information associated with this object
     *
     * @return the FontInfo object
     */
    public FontInfo getFontInfo() {
        return fontInfo;
    }

    /**
     * Registers a page font
     *
     * @param internalFontName the internal font name
     * @param fontSize the font size
     * @return a font reference
     */
    private int registerPageFont(AFPPageFonts pageFonts, String internalFontName, int fontSize) {
        AFPFont afpFont = (AFPFont)fontInfo.getFonts().get(internalFontName);
        // register if necessary
        AFPFontAttributes afpFontAttributes = pageFonts.registerFont(
                internalFontName,
                afpFont,
                fontSize
        );
        return afpFontAttributes.getFontReference();
    }

    /** {@inheritDoc} */
    public void drawString(String text, float x, float y) throws IOException {
        // TODO Remove me after removing the deprecated method in TextHandler.
        throw new UnsupportedOperationException("Deprecated method!");
    }

    /**
     * Add a text string to the current data object of the AFP datastream.
     * The text is painted using text operations.
     *
     * {@inheritDoc}
     */
    public void drawString(Graphics2D g, String str, float x, float y) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("drawString() str=" + str + ", x=" + x + ", y=" + y);
        }
        if (g instanceof AFPGraphics2D) {
            AFPGraphics2D g2d = (AFPGraphics2D)g;
            GraphicsObject graphicsObj = g2d.getGraphicsObject();
            Color color = g2d.getColor();

            // set the color
            AFPPaintingState paintingState = g2d.getPaintingState();
            if (paintingState.setColor(color)) {
                graphicsObj.setColor(color);
            }

            // set the character set
            int fontReference = 0;
            int fontSize;
            String internalFontName;
            AFPPageFonts pageFonts = paintingState.getPageFonts();
            if (overrideFont != null) {
                internalFontName = overrideFont.getFontName();
                fontSize = overrideFont.getFontSize();
            } else {
                java.awt.Font awtFont = g2d.getFont();
                Font fopFont = fontInfo.getFontInstanceForAWTFont(awtFont);
                internalFontName = fopFont.getFontName();
                fontSize = fopFont.getFontSize();
            }
            fontSize = (int)Math.round(
                    g2d.convertToAbsoluteLength(fontSize));
            fontReference = registerPageFont(pageFonts, internalFontName, fontSize);
            graphicsObj.setCharacterSet(fontReference);

            // add the character string
            graphicsObj.addString(str, Math.round(x), Math.round(y));
        } else {
            //Inside Batik's SVG filter operations, you won't get an AFPGraphics2D
            g.drawString(str, x, y);
        }
    }

    /**
     * Sets the overriding font.
     *
     * @param overrideFont Overriding Font to set
     */
    public void setOverrideFont(Font overrideFont) {
        this.overrideFont = overrideFont;
    }
}
