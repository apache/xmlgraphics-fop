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

package org.apache.fop.render.afp;

import java.awt.Color;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.afp.fonts.AFPFont;
import org.apache.fop.render.afp.modca.GraphicsObject;

import org.apache.xmlgraphics.java2d.TextHandler;

/**
 * Specialized TextHandler implementation that the AFPGraphics2D class delegates to to paint text
 * using AFP GOCA text operations.
 */
public class AFPTextHandler implements TextHandler {

    /** logging instance */
    private static Log log = LogFactory.getLog(AFPTextHandler.class);

    private AFPGraphics2D g2d = null;
        
    /** Overriding FontState */
    protected Font overrideFont = null;

    /** current state */
    private AFPState afpState = null;

    /**
     * Main constructor.
     * @param g2d the PSGraphics2D instance this instances is used by
     */
    public AFPTextHandler(AFPGraphics2D g2d) {
        this.g2d = g2d;
        this.afpState = g2d.getAFPInfo().getState();
    }
    
    /**
     * Return the font information associated with this object
     * @return the FontInfo object
     */
    public FontInfo getFontInfo() {
        return g2d.getAFPInfo().getFontInfo();
    }
    
    /**
     * Add a text string to the current data object of the AFP datastream.
     * The text is painted using text operations.
     * {@inheritDoc} 
     */
    public void drawString(String str, float x, float y) throws IOException {
        log.debug("drawString() str=" + str + ", x=" + x + ", y=" + y);
        GraphicsObject graphicsObj = g2d.getGraphicsObject();
        Color col = g2d.getColor();
        if (afpState.setColor(col)) {
            graphicsObj.setColor(col);
        }
        if (overrideFont != null) {
            FontInfo fontInfo = getFontInfo();
            AFPPageFonts pageFonts = afpState.getPageFonts();
            String internalFontName = overrideFont.getFontName();
            int fontSize = overrideFont.getFontSize();
            if (afpState.setFontName(internalFontName) || afpState.setFontSize(fontSize)) {
                AFPFont font = (AFPFont)fontInfo.getFonts().get(internalFontName);
                AFPFontAttributes afpFontAttributes = pageFonts.registerFont(
                        internalFontName,
                        font,
                        fontSize
                );
                int fontReference = afpFontAttributes.getFontReference();
                graphicsObj.setCharacterSet(fontReference);                
            }
        }
        graphicsObj.addString(str, (int)Math.round(x), (int)Math.round(y));
    }
    
    /**
     * Sets the overriding font.
     * @param overrideFont Overriding Font to set
     */
    public void setOverrideFont(Font overrideFont) {
        this.overrideFont = overrideFont;       
    }    
}
