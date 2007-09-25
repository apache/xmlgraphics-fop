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

/* $Id: $ */

package org.apache.fop.fonts.autodetect;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.CachedFontInfo;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontCache;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontTriplet;

/**
 * Attempts to determine correct FontInfo
 */
public class FontInfoFinder {
    
    /** logging instance */
    private Log log = LogFactory.getLog(FontInfoFinder.class);

    /** font constituent names which identify a font as being of "italic" style */
    private static final String[] ITALIC_WORDS = {"italic", "oblique"};

    /** font constituent names which identify a font as being of "bold" weight */
    private static final String[] BOLD_WORDS = {"bold", "black", "heavy", "ultra", "super"};

    /**
     * Attempts to determine FontTriplet from a given CustomFont.
     * It seems to be fairly accurate but will probably require some tweaking over time
     * 
     * @param customFont CustomFont
     * @return newly created font triplet
     */
    private FontTriplet tripletFromFont(CustomFont customFont) {
        // default style and weight triplet vales (fallback)
        String name = customFont.getStrippedFontName();
        String subName = customFont.getFontSubName();
        String searchName = name.toLowerCase();
        if (subName != null) {
            searchName += subName.toLowerCase();
        }
        
        // style
        String style = Font.STYLE_NORMAL;
        if (customFont.getItalicAngle() > 0) {
            style = Font.STYLE_ITALIC;  
        } else {
            for (int i = 0; i < ITALIC_WORDS.length; i++) {
                if (searchName.indexOf(ITALIC_WORDS[i]) != -1) {
                    style = Font.STYLE_ITALIC;          
                    break;
                }
            }
        }
        
        // weight
        int weight = Font.WEIGHT_NORMAL;
        for (int i = 0; i < BOLD_WORDS.length; i++) {
            if (searchName.indexOf(BOLD_WORDS[i]) != -1) {
                weight = Font.WEIGHT_BOLD;
                break;
            }            
        }
        return new FontTriplet(name, style, weight);
    }
    
    /**
     * Attempts to determine FontInfo from a given custom font
     * @param fontFile the font file
     * @param customFont the custom font
     * @param fontCache font cache (may be null)
     * @return
     */
    private EmbedFontInfo fontInfoFromCustomFont(
            File fontFile, CustomFont customFont, FontCache fontCache) {
        FontTriplet fontTriplet = tripletFromFont(customFont);
        List fontTripletList = new java.util.ArrayList();
        fontTripletList.add(fontTriplet);
        String embedUrl;
        try {
            embedUrl = fontFile.toURL().toExternalForm();
        } catch (MalformedURLException e) {
            embedUrl = fontFile.getAbsolutePath();
        }
        EmbedFontInfo fontInfo = new EmbedFontInfo(null, customFont.isKerningEnabled(),
                fontTripletList, embedUrl);
        if (fontCache != null) {
            fontCache.addFont(fontInfo);
        }
        return fontInfo;
    }
        
    /**
     * Attempts to determine EmbedFontInfo from a given font file.
     * 
     * @param fontFile font file
     * @param resolver font resolver used to resolve font
     * @param fontCache font cache (may be null)
     * @return newly created embed font info
     */
    public EmbedFontInfo find(File fontFile, FontResolver resolver, FontCache fontCache) {
        String embedUrl = null;
        try {
            embedUrl = fontFile.toURL().toExternalForm();
        } catch (MalformedURLException mfue) {
            // should never happen
            log.error("Failed to convert '" + fontFile + "' to URL: " + mfue.getMessage() );
        }
        
        long fileLastModified = -1;
        if (fontCache != null) {
            fileLastModified = fontFile.lastModified();
            // firstly try and fetch it from cache before loading/parsing the font file
            if (fontCache.containsFont(embedUrl)) {
                CachedFontInfo fontInfo = fontCache.getFont(embedUrl);
                if (fontInfo.lastModified() == fileLastModified) {
                    return fontInfo;
                } else {
                    // out of date cache item
                    fontCache.removeFont(embedUrl);
                }
            // is this a previously failed parsed font?
            } else if (fontCache.isFailedFont(embedUrl, fileLastModified)) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping font file that failed to load previously: " + embedUrl);
                }
                return null;
            }
        }
        
        // try to determine triplet information from font file
        CustomFont customFont = null;
        try {
            customFont = FontLoader.loadFont(fontFile, resolver);
        } catch (Exception e) {
            //TODO Too verbose (it's an error but we don't care if some fonts can't be loaded)
            if (log.isErrorEnabled()) {
                log.error("Unable to load font file: " + embedUrl + ". Reason: " + e.getMessage());
            }
            if (fontCache != null) {
                fontCache.registerFailedFont(embedUrl, fileLastModified);
            }
            return null;
        }
        return fontInfoFromCustomFont(fontFile, customFont, fontCache);     
    }
}
