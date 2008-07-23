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

package org.apache.fop.render;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.ClasspathResource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCache;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUtil;
import org.apache.fop.fonts.autodetect.FontFileFinder;
import org.apache.fop.fonts.autodetect.FontInfoFinder;
import org.apache.fop.util.LogUtil;

/**
 * Base Print renderer configurator (mostly handles font configuration)
 */
public class PrintRendererConfigurator extends AbstractRendererConfigurator
            implements RendererConfigurator {

    /** logger instance */
    protected static Log log = LogFactory.getLog(PrintRendererConfigurator.class);

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public PrintRendererConfigurator(FOUserAgent userAgent) {
        super(userAgent);
    }

    /**
     * Builds a list of EmbedFontInfo objects for use with the setup() method.
     *
     * @param renderer print renderer
     * @throws FOPException if something's wrong with the config data
     */
    public void configure(Renderer renderer) throws FOPException {
        Configuration cfg = getRendererConfig(renderer);
        if (cfg == null) {
            log.trace("no configuration found for " + renderer);
            return;
        }

        PrintRenderer printRenderer = (PrintRenderer)renderer;
        FontResolver fontResolver = printRenderer.getFontResolver();

        List embedFontInfoList = buildFontList(cfg, fontResolver);
        printRenderer.addFontList(embedFontInfoList);
    }

    /**
     * Builds the font list from configuration.
     * @param cfg the configuration object
     * @param fontResolver a font resolver
     * @return the list of {@code EmbedFontInfo} objects
     * @throws FOPException if an error occurs while processing the configuration
     */
    protected List buildFontList(Configuration cfg, FontResolver fontResolver) throws FOPException {
        FopFactory factory = userAgent.getFactory();
        FontManager fontManager = factory.getFontManager();
        if (fontResolver == null) {
            //Ensure that we have minimal font resolution capabilities
            fontResolver = FontManager.createMinimalFontResolver();
        }

        boolean strict = factory.validateUserConfigStrictly();
        FontCache fontCache = fontManager.getFontCache();

        List/*<EmbedFontInfo>*/ embedFontInfoList = buildFontListFromConfiguration(cfg,
                fontResolver, strict, fontManager);

        if (fontCache != null && fontCache.hasChanged()) {
            fontCache.save();
        }
        return embedFontInfoList;
    }

    /**
     * Builds a list of EmbedFontInfo objects for use with the setup() method.
     *
     * @param cfg Configuration object
     * @param fontResolver the FontResolver to use
     * @param strict true if an Exception should be thrown if an error is found.
     * @param fontManager the font manager
     * @return a List of EmbedFontInfo objects.
     * @throws FOPException If an error occurs while processing the configuration
     */
    public static List/*<EmbedFontInfo>*/ buildFontListFromConfiguration(Configuration cfg,
            FontResolver fontResolver,
            boolean strict, FontManager fontManager) throws FOPException {
        FontCache fontCache = fontManager.getFontCache();
        String fontBaseURL = fontManager.getFontBaseURL();
        List/*<EmbedFontInfo>*/ fontInfoList
                = new java.util.ArrayList/*<EmbedFontInfo>*/();

        Configuration fonts = cfg.getChild("fonts", false);
        if (fonts != null) {
            long start = 0;
            if (log.isDebugEnabled()) {
                log.debug("Starting font configuration...");
                start = System.currentTimeMillis();
            }

            // native o/s search (autodetect) configuration
            boolean autodetectFonts = (fonts.getChild("auto-detect", false) != null);
            if (autodetectFonts) {
                // search in font base if it is defined and
                // is a directory but don't recurse
                FontFileFinder fontFileFinder = new FontFileFinder();
                if (fontBaseURL != null) {
                    try {
                        File fontBase = FileUtils.toFile(new URL(fontBaseURL));
                        if (fontBase != null) {
                            //Can only use the font base URL if it's a file URL
                            addFontInfoListFromFileList(
                                    fontFileFinder.find(fontBase.getAbsolutePath()),
                                    fontInfoList,
                                    fontResolver,
                                    fontCache
                            );
                        }
                    } catch (IOException e) {
                        LogUtil.handleException(log, e, strict);
                    }
                }

                // native o/s font directory finder
                try {
                    addFontInfoListFromFileList(
                            fontFileFinder.find(),
                            fontInfoList,
                            fontResolver,
                            fontCache
                    );
                } catch (IOException e) {
                    LogUtil.handleException(log, e, strict);
                }

                // load fonts from classpath
                addFontInfoListFromFileList(ClasspathResource.getInstance()
                        .listResourcesOfMimeType("application/x-font"),
                        fontInfoList, fontResolver, fontCache);
                addFontInfoListFromFileList(
                        ClasspathResource.getInstance()
                                .listResourcesOfMimeType(
                                        "application/x-font-truetype"),
                        fontInfoList, fontResolver, fontCache);
            }

            // directory (multiple font) configuration
            Configuration[] directories = fonts.getChildren("directory");
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
                FontFileFinder fontFileFinder = new FontFileFinder(recursive ? -1 : 1);
                try {
                    addFontInfoListFromFileList(
                            fontFileFinder.find(directory),
                            fontInfoList,
                            fontResolver,
                            fontCache
                    );
                } catch (IOException e) {
                    LogUtil.handleException(log, e, strict);
                }
            }

            // font file (singular) configuration
            Configuration[] font = fonts.getChildren("font");
            for (int i = 0; i < font.length; i++) {
                EmbedFontInfo embedFontInfo = getFontInfoFromConfiguration(
                        font[i], fontResolver, strict, fontCache);
                if (embedFontInfo != null) {
                    fontInfoList.add(embedFontInfo);
                }
            }

            // Update referenced fonts (fonts which are not to be embedded)
            updateReferencedFonts(fontInfoList, fontManager.getReferencedFontsMatcher());

            if (log.isDebugEnabled()) {
                log.debug("Finished font configuration in "
                        + (System.currentTimeMillis() - start) + "ms");
            }
        }
        return fontInfoList;
    }

    private static void updateReferencedFonts(List fontInfoList, FontTriplet.Matcher matcher) {
        if (matcher == null) {
            return; //No referenced fonts
        }
        Iterator iter = fontInfoList.iterator();
        while (iter.hasNext()) {
            EmbedFontInfo fontInfo = (EmbedFontInfo)iter.next();
            Iterator triplets = fontInfo.getFontTriplets().iterator();
            while (triplets.hasNext()) {
                FontTriplet triplet = (FontTriplet)triplets.next();
                if (matcher.matches(triplet)) {
                    fontInfo.setEmbedded(false);
                    break;
                }
            }
        }
    }


    /**
     * Iterates over font file list adding font info to list
     * @param fontFileList font file list
     * @param embedFontInfoList a configured font info list
     * @param resolver font resolver
     */
    private static void addFontInfoListFromFileList(
            List fontFileList, List/*<EmbedFontInfo>*/ embedFontInfoList,
            FontResolver resolver, FontCache fontCache) {
        for (Iterator iter = fontFileList.iterator(); iter.hasNext();) {
            URL fontUrl = (URL)iter.next();
            // parse font to ascertain font info
            FontInfoFinder finder = new FontInfoFinder();
            //EmbedFontInfo fontInfo = finder.find(fontUrl, resolver, fontCache);

            //List<EmbedFontInfo> embedFontInfoList = finder.find(fontUrl, resolver, fontCache);
            EmbedFontInfo[] embedFontInfos = finder.find(fontUrl, resolver, fontCache);

            if (embedFontInfos == null) {
                continue;
            }

            for (int i = 0, c = embedFontInfos.length; i < c; i++) {
                EmbedFontInfo fontInfo = embedFontInfos[i];
                if (fontInfo != null) {
                    embedFontInfoList.add(fontInfo);
                }
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
     * Creates a new FontTriplet given a triple Configuration
     *
     * @param tripletCfg a triplet configuration
     * @param strict use strict validation
     * @return a font triplet font key
     * @throws FOPException thrown if a FOP exception occurs
     */
    private static FontTriplet getFontTripletFromConfiguration(
            Configuration tripletCfg, boolean strict) throws FOPException {
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

    /**
     * Returns a font info from a font node Configuration definition
     *
     * @param fontCfg Configuration object (font node)
     * @param fontResolver font resolver used to resolve font
     * @param strict validate configuration strictly
     * @param fontCache the font cache (or null if it is disabled)
     * @return the embedded font info
     * @throws FOPException if something's wrong with the config data
     */
    private static EmbedFontInfo getFontInfoFromConfiguration(
            Configuration fontCfg, FontResolver fontResolver, boolean strict, FontCache fontCache)
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
                EmbedFontInfo[] infos = finder.find(fontUrl, fontResolver, fontCache);
                return infos[0]; //When subFont is set, only one font is returned
            } else {
                return null;
            }
        }

        List/*<FontTriplet>*/ tripletList = new java.util.ArrayList/*<FontTriplet>*/();
        for (int j = 0; j < tripletCfg.length; j++) {
            FontTriplet fontTriplet = getFontTripletFromConfiguration(tripletCfg[j], strict);
            tripletList.add(fontTriplet);
        }

        boolean useKerning = fontCfg.getAttributeAsBoolean("kerning", true);
        EmbedFontInfo embedFontInfo
                = new EmbedFontInfo(metricsUrl, useKerning, tripletList, embedUrl, subFont);
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

}
