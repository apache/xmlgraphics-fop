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

package org.apache.fop.fonts.autodetect;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
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
import org.apache.fop.fonts.FontUtil;

/**
 * Attempts to determine correct FontInfo
 */
public class FontInfoFinder {
    
    /** logging instance */
    private Log log = LogFactory.getLog(FontInfoFinder.class);

    /**
     * Attempts to determine FontTriplets from a given CustomFont.
     * It seems to be fairly accurate but will probably require some tweaking over time
     * 
     * @param customFont CustomFont
     * @param triplet Collection that will take the generated triplets
     */
    private void generateTripletsFromFont(CustomFont customFont, Collection triplets) {
        if (log.isTraceEnabled()) {
            log.trace("Font: " + customFont.getFullName() 
                    + ", family: " + customFont.getFamilyNames() 
                    + ", PS: " + customFont.getFontName() 
                    + ", EmbedName: " + customFont.getEmbedFontName());
        }

        // default style and weight triplet vales (fallback)
        String strippedName = stripQuotes(customFont.getStrippedFontName());
        String subName = customFont.getFontSubName();
        String searchName = strippedName.toLowerCase();
        if (subName != null) {
            searchName += subName.toLowerCase();
        }
        
        String style = guessStyle(customFont, searchName);
        int weight = FontUtil.guessWeight(searchName);

        //Full Name usually includes style/weight info so don't use these traits
        //If we still want to use these traits, we have to make FontInfo.fontLookup() smarter
        String fullName = stripQuotes(customFont.getFullName());
        triplets.add(new FontTriplet(fullName, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL));
        if (!fullName.equals(strippedName)) {
            triplets.add(new FontTriplet(strippedName, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL));
        }
        Set familyNames = customFont.getFamilyNames();
        Iterator iter = familyNames.iterator();
        while (iter.hasNext()) {
            String familyName = stripQuotes((String)iter.next());
            if (!fullName.equals(familyName)) {
                triplets.add(new FontTriplet(familyName, style, weight));
            }
        }
    }
    
    private final Pattern quotePattern = Pattern.compile("'");
    
    private String stripQuotes(String name) {
        return quotePattern.matcher(name).replaceAll("");
    }

    private String guessStyle(CustomFont customFont, String fontName) {
        // style
        String style = Font.STYLE_NORMAL;
        if (customFont.getItalicAngle() > 0) {
            style = Font.STYLE_ITALIC;  
        } else {
            style = FontUtil.guessStyle(fontName);
        }
        return style;
    }
    
    /**
     * Attempts to determine FontInfo from a given custom font
     * @param fontUrl the font URL
     * @param customFont the custom font
     * @param fontCache font cache (may be null)
     * @return
     */
    private EmbedFontInfo fontInfoFromCustomFont(
            URL fontUrl, CustomFont customFont, FontCache fontCache) {
        List fontTripletList = new java.util.ArrayList();
        generateTripletsFromFont(customFont, fontTripletList);
        String embedUrl;
        embedUrl = fontUrl.toExternalForm();
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
     * @param fontUrl font URL. Assumed to be local.
     * @param resolver font resolver used to resolve font
     * @param fontCache font cache (may be null)
     * @return newly created embed font info
     */
    public EmbedFontInfo find(URL fontUrl, FontResolver resolver, FontCache fontCache) {
        String embedUrl = null;
        embedUrl = fontUrl.toExternalForm();
        
        long fileLastModified = -1;
        if (fontCache != null) {
            try {
                URLConnection conn = fontUrl.openConnection();
                try {
                    fileLastModified = conn.getLastModified();
                } finally {
                    //An InputStream is created even if it's not accessed, but we need to close it.
                    IOUtils.closeQuietly(conn.getInputStream());
                }
            } catch (IOException e) {
                // Should never happen, because URL must be local
                log.debug("IOError: " + e.getMessage());
                fileLastModified = 0;
            }
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
            customFont = FontLoader.loadFont(fontUrl, resolver);
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
        return fontInfoFromCustomFont(fontUrl, customFont, fontCache);     
    }
}
