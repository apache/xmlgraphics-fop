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

package org.apache.fop.fonts;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.autodetect.FontFileFinder;
import org.apache.fop.fonts.autodetect.FontInfoFinder;
import org.apache.fop.util.LogUtil;

/**
 * An abstract FontInfo configurator
 */
public class FontInfoConfigurator {
    /** logger instance */
    protected static Log log = LogFactory.getLog(FontInfoConfigurator.class);

    private Configuration cfg;
    private FontManager fontManager;
    private FontResolver fontResolver;
    private FontEventListener listener;
    private boolean strict;

    /**
     * Main constructor
     * @param cfg the configuration object
     * @param fontManager the font manager
     * @param fontResolver the font resolver
     * @param listener the font event listener
     * @param strict true if an Exception should be thrown if an error is found.
     */
    public FontInfoConfigurator(Configuration cfg, FontManager fontManager,
            FontResolver fontResolver, FontEventListener listener, boolean strict) {
        this.cfg = cfg;
        this.fontManager = fontManager;
        this.fontResolver = fontResolver;
        this.listener = listener;
        this.strict = strict;
    }

    /**
     * Initializes font info settings from the user configuration
     * @param fontInfoList a font info list
     * @throws FOPException if an exception occurs while processing the configuration
     */
    public void configure(List/*<EmbedFontInfo>*/ fontInfoList) throws FOPException {
        Configuration fonts = cfg.getChild("fonts", false);
        if (fonts != null) {
            long start = 0;
            if (log.isDebugEnabled()) {
                log.debug("Starting font configuration...");
                start = System.currentTimeMillis();
            }

            FontAdder fontAdder = new FontAdder(fontManager, fontResolver, listener);
            
            // native o/s search (autodetect) configuration
            boolean autodetectFonts = (fonts.getChild("auto-detect", false) != null);
            if (autodetectFonts) {
                FontDetector fontDetector = new FontDetector(fontManager, fontAdder, strict);
                fontDetector.detect(fontInfoList);
            }

            // Add configured directories to FontInfo
            addDirectories(fonts, fontAdder, fontInfoList);
            
            // Add configured fonts to FontInfo
            FontCache fontCache = fontManager.getFontCache();
            addFonts(fonts, fontCache, fontInfoList);

            // Update referenced fonts (fonts which are not to be embedded)
            fontManager.updateReferencedFonts(fontInfoList);
            
            if (log.isDebugEnabled()) {
                log.debug("Finished font configuration in "
                        + (System.currentTimeMillis() - start) + "ms");
            }
        }
    }
    
    private void addDirectories(Configuration fontsCfg,
            FontAdder fontAdder, List/*<URL>*/ fontInfoList) throws FOPException {
        // directory (multiple font) configuration
        Configuration[] directories = fontsCfg.getChildren("directory");
        for (int i = 0; i < directories.length; i++) {
            boolean recursive = directories[i].getAttributeAsBoolean("recursive", false);
            String directory = null;
            try {
                directory = directories[i].getValue();
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, strict);
                continue;
            }
            if (directory == null) {
                LogUtil.handleException(log,
                        new FOPException("directory defined without value"), strict);
                continue;
            }
            
            // add fonts found in directory
            FontFileFinder fontFileFinder = new FontFileFinder(recursive ? -1 : 1);
            List/*<URL>*/ fontURLList;
            try {
                fontURLList = fontFileFinder.find(directory);
                fontAdder.add(fontURLList, fontInfoList);
            } catch (IOException e) {
                LogUtil.handleException(log, e, strict);
            }
        }
    }

    /**
     * Populates the font info list from the fonts configuration 
     * @param fontsCfg a fonts configuration
     * @param fontCache a font cache
     * @param fontInfoList a font info list
     * @throws FOPException if an exception occurs while processing the configuration
     */
    protected void addFonts(Configuration fontsCfg, FontCache fontCache,
            List/*<EmbedFontInfo>*/ fontInfoList) throws FOPException {
        // font file (singular) configuration
        Configuration[] font = fontsCfg.getChildren("font");
        for (int i = 0; i < font.length; i++) {
            EmbedFontInfo embedFontInfo = getFontInfo(
                    font[i], fontCache);
            if (embedFontInfo != null) {
                fontInfoList.add(embedFontInfo);
            }
        }
    }
    
    private static void closeSource(Source src) {
        if (src instanceof StreamSource) {
            StreamSource streamSource = (StreamSource)src;
            IOUtils.closeQuietly(streamSource.getInputStream());
            IOUtils.closeQuietly(streamSource.getReader());
        }
    }

    /**
     * Returns a font info from a font node Configuration definition
     *
     * @param fontCfg Configuration object (font node)
     * @param fontCache the font cache (or null if it is disabled)
     * @return the embedded font info
     * @throws FOPException if something's wrong with the config data
     */
    protected EmbedFontInfo getFontInfo(
            Configuration fontCfg, FontCache fontCache)
                    throws FOPException {
        String metricsUrl = fontCfg.getAttribute("metrics-url", null);
        String embedUrl = fontCfg.getAttribute("embed-url", null);
        String subFont = fontCfg.getAttribute("sub-font", null);

        if (metricsUrl == null && embedUrl == null) {
            LogUtil.handleError(log,
                    "Font configuration without metric-url or embed-url attribute",
                    strict);
            return null;
        }
        if (strict) {
            //This section just checks early whether the URIs can be resolved
            //Stream are immediately closed again since they will never be used anyway
            if (embedUrl != null) {
                Source source = fontResolver.resolve(embedUrl);
                closeSource(source);
                if (source == null) {
                    LogUtil.handleError(log,
                            "Failed to resolve font with embed-url '" + embedUrl + "'", strict);
                    return null;
                }
            }
            if (metricsUrl != null) {
                Source source = fontResolver.resolve(metricsUrl);
                closeSource(source);
                if (source == null) {
                    LogUtil.handleError(log,
                            "Failed to resolve font with metric-url '" + metricsUrl + "'", strict);
                    return null;
                }
            }
        }

        Configuration[] tripletCfg = fontCfg.getChildren("font-triplet");

        // no font triplet info
        if (tripletCfg.length == 0) {
            LogUtil.handleError(log, "font without font-triplet", strict);

            File fontFile = FontCache.getFileFromUrls(new String[] {embedUrl, metricsUrl});
            URL fontUrl;
            try {
                fontUrl = fontFile.toURI().toURL();
            } catch (MalformedURLException e) {
                // Should never happen
                log.debug("Malformed Url: " + e.getMessage());
                return null;
            }
            if (fontFile != null) {
                FontInfoFinder finder = new FontInfoFinder();
                finder.setEventListener(listener);
                EmbedFontInfo[] infos = finder.find(fontUrl, fontResolver, fontCache);
                return infos[0]; //When subFont is set, only one font is returned
            } else {
                return null;
            }
        }

        List/*<FontTriplet>*/ tripletList = new java.util.ArrayList/*<FontTriplet>*/();
        for (int j = 0; j < tripletCfg.length; j++) {
            FontTriplet fontTriplet = getFontTriplet(tripletCfg[j]);
            tripletList.add(fontTriplet);
        }

        boolean useKerning = fontCfg.getAttributeAsBoolean("kerning", true);
        EncodingMode encodingMode = EncodingMode.valueOf(
                fontCfg.getAttribute("encoding-mode", EncodingMode.AUTO.getName()));
        EmbedFontInfo embedFontInfo
                = new EmbedFontInfo(metricsUrl, useKerning, tripletList, embedUrl, subFont);
        embedFontInfo.setEncodingMode(encodingMode);
        if (fontCache != null) {
            if (!fontCache.containsFont(embedFontInfo)) {
                fontCache.addFont(embedFontInfo);
            }
        }

        if (log.isDebugEnabled()) {
            String embedFile = embedFontInfo.getEmbedFile();
            log.debug("Adding font " + (embedFile != null ? embedFile + ", " : "")
                    + "metric file " + embedFontInfo.getMetricsFile());
            for (int j = 0; j < tripletList.size(); ++j) {
                FontTriplet triplet = (FontTriplet) tripletList.get(j);
                log.debug("  Font triplet "
                        + triplet.getName() + ", "
                        + triplet.getStyle() + ", "
                        + triplet.getWeight());
            }
        }
        return embedFontInfo;
    }

    /**
     * Creates a new FontTriplet given a triple Configuration
     *
     * @param tripletCfg a triplet configuration
     * @return a font triplet font key
     * @throws FOPException thrown if a FOP exception occurs
     */
    private FontTriplet getFontTriplet(Configuration tripletCfg) throws FOPException {
        try {
            String name = tripletCfg.getAttribute("name");
            if (name == null) {
                LogUtil.handleError(log, "font-triplet without name", strict);
                return null;
            }

            String weightStr = tripletCfg.getAttribute("weight");
            if (weightStr == null) {
                LogUtil.handleError(log, "font-triplet without weight", strict);
                return null;
            }
            int weight = FontUtil.parseCSS2FontWeight(FontUtil.stripWhiteSpace(weightStr));

            String style = tripletCfg.getAttribute("style");
            if (style == null) {
                LogUtil.handleError(log, "font-triplet without style", strict);
                return null;
            } else {
                style = FontUtil.stripWhiteSpace(style);
            }
            return FontInfo.createFontKey(name, style, weight);
        } catch (ConfigurationException e) {
            LogUtil.handleException(log, e, strict);
        }
        return null;
    }

}
