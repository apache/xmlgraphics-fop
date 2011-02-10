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

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontCache;
import org.apache.fop.fonts.FontEventListener;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUtil;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.fop.fonts.truetype.TTFFontLoader;

/**
 * Attempts to determine correct FontInfo
 */
public class FontInfoFinder {

    /** logging instance */
    private final Log log = LogFactory.getLog(FontInfoFinder.class);

    private FontEventListener eventListener;

    /**
     * Sets the font event listener that can be used to receive events about particular events
     * in this class.
     * @param listener the font event listener
     */
    public void setEventListener(FontEventListener listener) {
        this.eventListener = listener;
    }

    /**
     * Attempts to determine FontTriplets from a given CustomFont.
     * It seems to be fairly accurate but will probably require some tweaking over time
     *
     * @param customFont CustomFont
     * @param triplets Collection that will take the generated triplets
     */
    private void generateTripletsFromFont(CustomFont customFont, Collection<FontTriplet> triplets) {
        if (log.isTraceEnabled()) {
            log.trace("Font: " + customFont.getFullName()
                    + ", family: " + customFont.getFamilyNames()
                    + ", PS: " + customFont.getFontName()
                    + ", EmbedName: " + customFont.getEmbedFontName());
        }

        // default style and weight triplet vales (fallback)
        String strippedName = stripQuotes(customFont.getStrippedFontName());
        //String subName = customFont.getFontSubName();
        String fullName = stripQuotes(customFont.getFullName());
        String searchName = fullName.toLowerCase();

        String style = guessStyle(customFont, searchName);
        int weight; //= customFont.getWeight();
        int guessedWeight = FontUtil.guessWeight(searchName);
        //We always take the guessed weight for now since it yield much better results.
        //OpenType's OS/2 usWeightClass value proves to be unreliable.
        weight = guessedWeight;

        //Full Name usually includes style/weight info so don't use these traits
        //If we still want to use these traits, we have to make FontInfo.fontLookup() smarter
        triplets.add(new FontTriplet(fullName, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL));
        if (!fullName.equals(strippedName)) {
            triplets.add(new FontTriplet(strippedName, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL));
        }
        Set<String> familyNames = customFont.getFamilyNames();
        for (String familyName : familyNames) {
            familyName = stripQuotes(familyName);
            if (!fullName.equals(familyName)) {
                /* Heuristic:
                 *   The more similar the family name to the full font name,
                 *   the higher the priority of its triplet.
                 * (Lower values indicate higher priorities.) */
                int priority = fullName.startsWith(familyName)
                    ? fullName.length() - familyName.length()
                    : fullName.length();
                triplets.add(new FontTriplet(familyName, style, weight, priority));
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
     * @param fontURL the font URL
     * @param customFont the custom font
     * @param fontCache font cache (may be null)
     * @return FontInfo from the given custom font
     */
    private EmbedFontInfo getFontInfoFromCustomFont(
            URL fontURL, CustomFont customFont, FontCache fontCache) {
        List<FontTriplet> fontTripletList = new java.util.ArrayList<FontTriplet>();
        generateTripletsFromFont(customFont, fontTripletList);
        String embedUrl;
        embedUrl = fontURL.toExternalForm();
        String subFontName = null;
        if (customFont instanceof MultiByteFont) {
            subFontName = ((MultiByteFont)customFont).getTTCName();
        }
        EmbedFontInfo fontInfo = new EmbedFontInfo(null, customFont.isKerningEnabled(),
                fontTripletList, embedUrl, subFontName);
        fontInfo.setPostScriptName(customFont.getFontName());
        if (fontCache != null) {
            fontCache.addFont(fontInfo);
        }
        return fontInfo;
    }

    /**
     * Attempts to determine EmbedFontInfo from a given font file.
     *
     * @param fontURL font URL. Assumed to be local.
     * @param resolver font resolver used to resolve font
     * @param fontCache font cache (may be null)
     * @return an array of newly created embed font info. Generally, this array
     *         will have only one entry, unless the fontUrl is a TrueType Collection
     */
    public EmbedFontInfo[] find(URL fontURL, FontResolver resolver, FontCache fontCache) {
        String embedURL = null;
        embedURL = fontURL.toExternalForm();

        long fileLastModified = -1;
        if (fontCache != null) {
            fileLastModified = FontCache.getLastModified(fontURL);
            // firstly try and fetch it from cache before loading/parsing the font file
            if (fontCache.containsFont(embedURL)) {
                EmbedFontInfo[] fontInfos = fontCache.getFontInfos(embedURL, fileLastModified);
                if (fontInfos != null) {
                    return fontInfos;
                }
            // is this a previously failed parsed font?
            } else if (fontCache.isFailedFont(embedURL, fileLastModified)) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping font file that failed to load previously: " + embedURL);
                }
                return null;
            }
        }


        // try to determine triplet information from font file
        CustomFont customFont = null;
        if (fontURL.toExternalForm().endsWith(".ttc")) {
            // Get a list of the TTC Font names
            List<String> ttcNames = null;
            String fontFileURL = fontURL.toExternalForm().trim();
            InputStream in = null;
            try {
                in = FontLoader.openFontUri(resolver, fontFileURL);
                TTFFile ttf = new TTFFile();
                FontFileReader reader = new FontFileReader(in);
                ttcNames = ttf.getTTCnames(reader);
            } catch (Exception e) {
                if (this.eventListener != null) {
                    this.eventListener.fontLoadingErrorAtAutoDetection(this, fontFileURL, e);
                }
                return null;
            } finally {
                IOUtils.closeQuietly(in);
            }

            List<EmbedFontInfo> embedFontInfoList = new java.util.ArrayList<EmbedFontInfo>();

            // For each font name ...
            for (String fontName : ttcNames) {
                if (log.isDebugEnabled()) {
                    log.debug("Loading " + fontName);
                }
                try {
                    TTFFontLoader ttfLoader = new TTFFontLoader(
                            fontFileURL, fontName, true, EncodingMode.AUTO, true, resolver);
                    customFont = ttfLoader.getFont();
                    if (this.eventListener != null) {
                        customFont.setEventListener(this.eventListener);
                    }
                } catch (Exception e) {
                    if (fontCache != null) {
                        fontCache.registerFailedFont(embedURL, fileLastModified);
                    }
                    if (this.eventListener != null) {
                        this.eventListener.fontLoadingErrorAtAutoDetection(this, embedURL, e);
                    }
                    continue;
                }
                EmbedFontInfo fi = getFontInfoFromCustomFont(fontURL, customFont, fontCache);
                if (fi != null) {
                    embedFontInfoList.add(fi);
                }
            }
            return embedFontInfoList.toArray(
                    new EmbedFontInfo[embedFontInfoList.size()]);
        } else {
            // The normal case
            try {
                customFont = FontLoader.loadFont(fontURL, null, true, EncodingMode.AUTO, resolver);
                if (this.eventListener != null) {
                    customFont.setEventListener(this.eventListener);
                }
            } catch (Exception e) {
                if (fontCache != null) {
                    fontCache.registerFailedFont(embedURL, fileLastModified);
                }
                if (this.eventListener != null) {
                    this.eventListener.fontLoadingErrorAtAutoDetection(this, embedURL, e);
                }
                return null;
            }
            EmbedFontInfo fi = getFontInfoFromCustomFont(fontURL, customFont, fontCache);
            if (fi != null) {
                return new EmbedFontInfo[] {fi};
            } else {
                return null;
            }
        }

    }

}
