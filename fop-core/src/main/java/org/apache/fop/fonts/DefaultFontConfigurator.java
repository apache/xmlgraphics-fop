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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.DefaultFontConfig.Directory;
import org.apache.fop.fonts.autodetect.FontFileFinder;
import org.apache.fop.fonts.autodetect.FontInfoFinder;
import org.apache.fop.util.LogUtil;

/**
 * The default configurator for fonts. This configurator can configure the more generic fonts used
 * by the renderers i.e. TTF, Type1 etc...
 */
public class DefaultFontConfigurator implements FontConfigurator<EmbedFontInfo> {
    /** logger instance */
    protected static final Log log = LogFactory.getLog(DefaultFontConfigurator.class);

    private final FontManager fontManager;
    private final InternalResourceResolver resourceResolver;
    private final FontEventListener listener;
    private final boolean strict;

    /**
     * Main constructor
     * @param fontManager the font manager
     * @param listener the font event listener
     * @param strict true if an Exception should be thrown if an error is found.
     */
    public DefaultFontConfigurator(FontManager fontManager, FontEventListener listener, boolean strict) {
        this.fontManager = fontManager;
        this.resourceResolver = fontManager.getResourceResolver();
        this.listener = listener;
        this.strict = strict;
    }

    /**
     * Initializes font info settings from the user configuration
     * @throws FOPException if an exception occurs while processing the configuration
     */
    public List<EmbedFontInfo> configure(FontConfig fontInfoConfig) throws FOPException {
        List<EmbedFontInfo> fontInfoList = new ArrayList<EmbedFontInfo>();
        if (fontInfoConfig != null) {
            assert fontInfoConfig instanceof DefaultFontConfig;
            DefaultFontConfig adobeFontInfoConfig = (DefaultFontConfig) fontInfoConfig;
            long start = 0;
            if (log.isDebugEnabled()) {
                log.debug("Starting font configuration...");
                start = System.currentTimeMillis();
            }
            FontAdder fontAdder = new FontAdder(fontManager, resourceResolver, listener);
            // native o/s search (autodetect) configuration
            fontManager.autoDetectFonts(adobeFontInfoConfig.isAutoDetectFonts(), fontAdder, strict,
                    listener, fontInfoList);
            // Add configured directories to FontInfo list
            addDirectories(adobeFontInfoConfig, fontAdder, fontInfoList);
            // Add configured fonts to FontInfo
            FontCache fontCache = fontManager.getFontCache();
            try {
                addFonts(adobeFontInfoConfig, fontCache, fontInfoList);
            } catch (URISyntaxException use) {
                LogUtil.handleException(log, use, strict);
            }
            // Update referenced fonts (fonts which are not to be embedded)
            fontManager.updateReferencedFonts(fontInfoList);
            // Renderer-specific referenced fonts
            List<String> referencedFonts = adobeFontInfoConfig.getReferencedFontFamily();
            if (referencedFonts.size() > 0) {
                FontTriplet.Matcher matcher = FontManagerConfigurator.createFontsMatcher(
                        referencedFonts, strict);
                fontManager.updateReferencedFonts(fontInfoList, matcher);
            }
            // Update font cache if it has changed
            fontManager.saveCache();
            if (log.isDebugEnabled()) {
                log.debug("Finished font configuration in "
                        + (System.currentTimeMillis() - start) + "ms");
            }
        }
        return Collections.unmodifiableList(fontInfoList);
    }

    private void addDirectories(DefaultFontConfig fontInfoConfig, FontAdder fontAdder,
            List<EmbedFontInfo> fontInfoList) throws FOPException {
        // directory (multiple font) configuration
        List<Directory> directories = fontInfoConfig.getDirectories();
        for (Directory directory : directories) {
            // add fonts found in directory
            FontFileFinder fontFileFinder = new FontFileFinder(directory.isRecursive() ? -1 : 1, listener);
            List<URL> fontURLList;
            try {
                fontURLList = fontFileFinder.find(new File(directory.getDirectory()));
                fontAdder.add(fontURLList, fontInfoList);
            } catch (IOException e) {
                LogUtil.handleException(log, e, strict);
            } catch (URISyntaxException use) {
                LogUtil.handleException(log, use, strict);
            }
        }
    }

    private void addFonts(DefaultFontConfig fontInfoConfig, FontCache fontCache,
            List<EmbedFontInfo> fontInfoList) throws FOPException, URISyntaxException {
        // font file (singular) configuration
        List<DefaultFontConfig.Font> fonts = fontInfoConfig.getFonts();
        for (DefaultFontConfig.Font font : fonts) {
            EmbedFontInfo embedFontInfo = getFontInfo(font, fontCache);
            if (embedFontInfo != null) {
                fontInfoList.add(embedFontInfo);
            }
        }
    }

    private EmbedFontInfo getFontInfo(DefaultFontConfig.Font font, FontCache fontCache)
            throws FOPException, URISyntaxException {
        String embed = font.getEmbedURI();
        String metrics = font.getMetrics();
        String afm = font.getAfm();
        String pfm = font.getPfm();
        URI embedUri = InternalResourceResolver.cleanURI(embed);
        URI metricsUri = metrics == null ? null : InternalResourceResolver.cleanURI(metrics);
        URI afmUri = (afm == null) ? null : InternalResourceResolver.cleanURI(afm);
        URI pfmUri = (pfm == null) ? null : InternalResourceResolver.cleanURI(pfm);
        FontUris fontUris = (afmUri != null || pfmUri != null) ? new FontUris(embedUri, metricsUri, afmUri,
                pfmUri) : new FontUris(embedUri, metricsUri);

        String subFont = font.getSubFont();
        List<FontTriplet> tripletList = font.getTripletList();

        // no font triplet info
        if (tripletList.size() == 0) {
            URI fontUri = resourceResolver.resolveFromBase(embedUri);
            FontInfoFinder finder = new FontInfoFinder();
            finder.setEventListener(listener);
            EmbedFontInfo[] infos = finder.find(fontUri, resourceResolver, fontCache);
            return infos[0]; //When subFont is set, only one font is returned
        }
        EncodingMode encodingMode = EncodingMode.getValue(font.getEncodingMode());
        EmbeddingMode embeddingMode = EmbeddingMode.getValue(font.getEmbeddingMode());
        EmbedFontInfo embedFontInfo = new EmbedFontInfo(fontUris, font.isKerning(),
                font.isAdvanced(), tripletList, subFont, encodingMode, embeddingMode,
                font.getSimulateStyle(), font.getEmbedAsType1(), font.getUseSVG(), font.isLazyLoad());
        if (fontCache != null) {
            if (!fontCache.containsFont(embedFontInfo)) {
                fontCache.addFont(embedFontInfo, resourceResolver);
            }
        }

        if (log.isDebugEnabled()) {
            URI embedFile = embedFontInfo.getEmbedURI();
            log.debug("Adding font " + (embedFile != null ? embedFile + ", " : "")
                    + "metrics URI " + embedFontInfo.getMetricsURI());
            for (FontTriplet triplet : tripletList) {
                log.debug("  Font triplet "
                        + triplet.getName() + ", "
                        + triplet.getStyle() + ", "
                        + triplet.getWeight());
            }
        }
        return embedFontInfo;
    }
}
