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

package org.apache.fop.fonts;

import java.io.File;
import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.FontTriplet.Matcher;
import org.apache.fop.fonts.substitute.FontSubstitutions;

// TODO: Refactor fonts package so major font activities (autodetection etc)
// are all centrally managed and delegated from this class

/**
 * The manager of fonts. The class holds a reference to the font cache and information about
 * font substitution, referenced fonts and similar.
 */
public class FontManager {

    /** The resource resolver */
    private InternalResourceResolver resourceResolver;

    private final FontDetector fontDetector;

    private FontCacheManager fontCacheManager;

    /** Font substitutions */
    private FontSubstitutions fontSubstitutions = null;

    /** Allows enabling kerning on the base 14 fonts, default is false */
    private boolean enableBase14Kerning = false;

    /** FontTriplet matcher for fonts that shall be referenced rather than embedded. */
    private FontTriplet.Matcher referencedFontsMatcher;

    /** Provides a font cache file path **/
    private File cacheFile;

    /**
     * Main constructor
     *
     * @param resourceResolver the URI resolver
     * @param fontDetector the font detector
     * @param fontCacheManager the font cache manager
     */
    public FontManager(InternalResourceResolver resourceResolver, FontDetector fontDetector,
            FontCacheManager fontCacheManager) {
        this.resourceResolver = resourceResolver;
        this.fontDetector = fontDetector;
        this.fontCacheManager = fontCacheManager;
    }

    /**
     * Sets the font resource resolver
     * @param resourceResolver resource resolver
     */
    public void setResourceResolver(InternalResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    public InternalResourceResolver getResourceResolver() {
        return this.resourceResolver;
    }

    /** @return true if kerning on base 14 fonts is enabled */
    public boolean isBase14KerningEnabled() {
        return this.enableBase14Kerning;
    }

    /**
     * Controls whether kerning is activated on base 14 fonts.
     * @param value true if kerning should be activated
     */
    public void setBase14KerningEnabled(boolean value) {
        this.enableBase14Kerning = value;
    }

    /**
     * Sets the font substitutions
     * @param substitutions font substitutions
     */
    public void setFontSubstitutions(FontSubstitutions substitutions) {
        this.fontSubstitutions = substitutions;
    }

    /**
     * Returns the font substitution catalog
     * @return the font substitution catalog
     */
    protected FontSubstitutions getFontSubstitutions() {
        if (fontSubstitutions == null) {
            this.fontSubstitutions = new FontSubstitutions();
        }
        return fontSubstitutions;
    }

    /**
     * Sets the font cache file
     * @param cacheFile the font cache file
     */
    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    /**
     * Returns the font cache file
     * @return the font cache file
     */
    public File getCacheFile() {
        return getCacheFile(false);
    }

    private File getCacheFile(boolean writable) {
        if (cacheFile != null) {
            return cacheFile;
        }
        return FontCache.getDefaultCacheFile(writable);
    }

    /**
     * Whether or not to cache results of font triplet detection/auto-config
     * @param useCache use cache or not
     */
    public void disableFontCache() {
        fontCacheManager = FontCacheManagerFactory.createDisabled();
    }

    /**
     * Returns the font cache instance used by this font manager.
     * @return the font cache
     */
    public FontCache getFontCache() {
        return fontCacheManager.load(getCacheFile());
    }

    /**
     * Saves the FontCache as necessary
     *
     * @throws FOPException fop exception
     */
    public void saveCache() throws FOPException {
        fontCacheManager.save(getCacheFile());
    }

    /**
     * Deletes the current FontCache file
     * @return Returns true if the font cache file was successfully deleted.
     * @throws FOPException -
     */
    public void deleteCache() throws FOPException {
        fontCacheManager.delete(getCacheFile(true));
    }

    /**
     * Sets up the fonts on a given FontInfo object. The fonts to setup are defined by an
     * array of {@link FontCollection} objects.
     * @param fontInfo the FontInfo object to set up
     * @param fontCollections the array of font collections/sources
     */
    public void setup(FontInfo fontInfo, FontCollection[] fontCollections) {
        int startNum = 1;

        for (int i = 0, c = fontCollections.length; i < c; i++) {
            startNum = fontCollections[i].setup(startNum, fontInfo);
        }
        // Make any defined substitutions in the font info
        getFontSubstitutions().adjustFontInfo(fontInfo);
    }

    /**
     * Sets the {@link FontTriplet.Matcher} that can be used to identify the fonts that shall
     * be referenced rather than embedded.
     * @param matcher the font triplet matcher
     */
    public void setReferencedFontsMatcher(FontTriplet.Matcher matcher) {
        this.referencedFontsMatcher = matcher;
    }

    /**
     * Gets the {@link FontTriplet.Matcher} that can be used to identify the fonts that shall
     * be referenced rather than embedded.
     * @return the font triplet matcher (or null if none is set)
     */
    public Matcher getReferencedFontsMatcher() {
        return this.referencedFontsMatcher;
    }

    /**
     * Updates the referenced font list using the FontManager's referenced fonts matcher
     * ({@link #getReferencedFontsMatcher()}).
     * @param fontInfoList a font info list
     */
    public void updateReferencedFonts(List<EmbedFontInfo> fontInfoList) {
        Matcher matcher = getReferencedFontsMatcher();
        updateReferencedFonts(fontInfoList, matcher);
    }

    /**
     * Updates the referenced font list.
     * @param fontInfoList a font info list
     * @param matcher the font triplet matcher to use
     */
    public void updateReferencedFonts(List<EmbedFontInfo> fontInfoList, Matcher matcher) {
        if (matcher == null) {
            return; //No referenced fonts
        }
        for (EmbedFontInfo fontInfo : fontInfoList) {
            for (FontTriplet triplet : fontInfo.getFontTriplets()) {
                if (matcher.matches(triplet)) {
                    fontInfo.setEmbedded(false);
                    break;
                }
            }
        }
    }

    /**
     * Detect fonts from the operating system via FOPs autodetect mechanism.
     *
     * @param autoDetectFonts if autodetect has been enabled
     * @param fontAdder the font adding mechanism
     * @param strict whether to enforce strict validation
     * @param listener the listener for font related events
     * @param fontInfoList a list of font info objects
     * @throws FOPException if an exception was thrown auto-detecting fonts
     */
    public void autoDetectFonts(boolean autoDetectFonts, FontAdder fontAdder, boolean strict,
            FontEventListener  listener, List<EmbedFontInfo> fontInfoList) throws FOPException {
        if (autoDetectFonts) {
            fontDetector.detect(this, fontAdder, strict, listener, fontInfoList);
        }
    }
}
