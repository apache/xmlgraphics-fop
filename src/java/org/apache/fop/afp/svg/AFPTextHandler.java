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
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.AFPFontAttributes;
import org.apache.fop.afp.fonts.AFPPageFonts;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.modca.GraphicsObject;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.FOPTextHandlerAdapter;

/**
 * Specialized TextHandler implementation that the AFPGraphics2D class delegates to to paint text
 * using AFP GOCA text operations.
 */
public class AFPTextHandler extends FOPTextHandlerAdapter {

    /** logging instance */
    private static Log log = LogFactory.getLog(AFPTextHandler.class);

    /** Overriding FontState */
    protected Font overrideFont = null;

    /** Font information */
    private final FontInfo fontInfo;

    /** the resource manager */
    private AFPResourceManager resourceManager;

    /**
     * Main constructor.
     *
     * @param fontInfo the AFPGraphics2D instance
     * @param resourceManager the AFPResourceManager instance
     */
    public AFPTextHandler(FontInfo fontInfo, AFPResourceManager resourceManager) {
        this.fontInfo = fontInfo;
        this.resourceManager = resourceManager;
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
        if (afpFont.isEmbeddable()) {
            try {
                final CharacterSet charSet = afpFont.getCharacterSet(fontSize);
                this.resourceManager.embedFont(afpFont, charSet);
            } catch (IOException ioe) {
                throw new RuntimeException("Error while embedding font resources", ioe);
            }
        }
        return afpFontAttributes.getFontReference();
    }

    /**
     * Add a text string to the current data object of the AFP datastream.
     * The text is painted using text operations.
     *
     * {@inheritDoc}
     */
    @Override
    public void drawString(Graphics2D g, String str, float x, float y) {
        if (log.isDebugEnabled()) {
            log.debug("drawString() str=" + str + ", x=" + x + ", y=" + y);
        }
        if (g instanceof AFPGraphics2D) {
            AFPGraphics2D g2d = (AFPGraphics2D)g;
            GraphicsObject graphicsObj = g2d.getGraphicsObject();
            Color color = g2d.getColor();

            // set the color
            AFPPaintingState paintingState = g2d.getPaintingState();
            paintingState.setColor(color);
            graphicsObj.setColor(color);

            // set the character set
            int fontReference = 0;
            int fontSize;
            String internalFontName;
            AFPPageFonts pageFonts = paintingState.getPageFonts();
            if (overrideFont != null) {
                internalFontName = overrideFont.getFontName();
                fontSize = overrideFont.getFontSize();
                if (log.isDebugEnabled()) {
                    log.debug("  with overriding font: " + internalFontName + ", " + fontSize);
                }
                fontSize = (int) Math.round(g2d.convertToAbsoluteLength(fontSize));
                fontReference = registerPageFont(pageFonts, internalFontName, fontSize);
                // TODO: re-think above registerPageFont code...
                AFPFont afpFont = (AFPFont) fontInfo.getFonts().get(internalFontName);
                final CharacterSet charSet = afpFont.getCharacterSet(fontSize);
                // Work-around for InfoPrint's AFP which loses character set state
                // over Graphics Data
                // boundaries.
                graphicsObj.setCharacterSet(fontReference);
                // add the character string
                graphicsObj.addString(str, Math.round(x), Math.round(y), charSet);
            }
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
