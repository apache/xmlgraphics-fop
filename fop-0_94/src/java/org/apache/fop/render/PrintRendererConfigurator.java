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

package org.apache.fop.render;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.CachedFontInfo;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCache;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontSetup;
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

    /** have we already autodetected system fonts? */
    private static boolean autodetectedFonts = false;

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
            return;
        }

        PrintRenderer printRenderer = (PrintRenderer)renderer;
        FontResolver fontResolver = printRenderer.getFontResolver();
        if (fontResolver == null) {
            //Ensure that we have minimal font resolution capabilities
            fontResolver = FontSetup.createMinimalFontResolver();
        }

        FopFactory factory = userAgent.getFactory();
        boolean strict = factory.validateUserConfigStrictly();
        FontCache fontCache = factory.getFontCache();

        List fontInfoList = buildFontListFromConfiguration(cfg, 
                userAgent.getFontBaseURL(), fontResolver, strict, 
                fontCache);
        
        if (fontCache != null && fontCache.hasChanged()) {
            fontCache.save();
        }
        printRenderer.addFontList(fontInfoList);
    }

    /**
     * Builds a list of EmbedFontInfo objects for use with the setup() method.
     * 
     * @param cfg Configuration object
     * @param fontBaseURL the base URL to resolve relative font URLs with
     * @param fontResolver the FontResolver to use
     * @param strict true if an Exception should be thrown if an error is found.
     * @param fontCache the font cache (or null if it is disabled)
     * @return a List of EmbedFontInfo objects.
     * @throws FOPException If an error occurs while processing the configuration
     */
    public static List buildFontListFromConfiguration(Configuration cfg, 
            String fontBaseURL, FontResolver fontResolver, 
            boolean strict, FontCache fontCache) throws FOPException {
        List fontInfoList = new java.util.ArrayList();

        Configuration fonts = cfg.getChild("fonts");
        if (fonts != null) {
            long start = 0;
            if (log.isDebugEnabled()) {
                log.debug("Starting font configuration...");
                start = System.currentTimeMillis();
            }
            
            // native o/s search (autodetect) configuration
            boolean autodetectFonts = (fonts.getChild("auto-detect", false) != null);
            if (!autodetectedFonts && autodetectFonts) {
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
                autodetectedFonts = true;
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
                EmbedFontInfo fontInfo = getFontInfoFromConfiguration(
                        font[i], fontResolver, strict, fontCache);
                if (fontInfo != null) {
                    fontInfoList.add(fontInfo);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Finished font configuration in " 
                        + (System.currentTimeMillis() - start) + "ms");
            }
        }
        return fontInfoList;
    }

    /**
     * Iterates over font file list adding font info to list
     * @param fontFileList font file list
     * @param fontInfoList font info list
     * @param resolver font resolver
     */
    private static void addFontInfoListFromFileList(
            List fontFileList, List fontInfoList, FontResolver resolver, FontCache fontCache) {
        for (Iterator iter = fontFileList.iterator(); iter.hasNext();) {
            File fontFile = (File)iter.next();
            // parse font to ascertain font info
            FontInfoFinder finder = new FontInfoFinder();
            EmbedFontInfo fontInfo = finder.find(fontFile, resolver, fontCache);
            if (fontInfo != null) {
                fontInfoList.add(fontInfo);                
            }
        }
    }
        
    /**
     * Returns a font info from a font node Configuration definition
     * 
     * @param fontCfg Configuration object (font node)
     * @param fontResolver font resolver used to resolve font
     * @param strict validate configuration strictly
     * @param fontCache the font cache (or null if it is disabled)
     * @return font info
     * @throws FOPException if something's wrong with the config data
     */
    public static EmbedFontInfo getFontInfoFromConfiguration(
            Configuration fontCfg, FontResolver fontResolver, boolean strict, FontCache fontCache)
    throws FOPException {
        String metricsUrl = fontCfg.getAttribute("metrics-url", null);
        String embedUrl = fontCfg.getAttribute("embed-url", null);

        if (metricsUrl == null && embedUrl == null) {
            LogUtil.handleError(log, "Font configuration without metric-url or embed-url", strict);
            return null;
        }
        if (embedUrl != null) {
            StreamSource source = (StreamSource)fontResolver.resolve(embedUrl);
            if (source == null) {
                LogUtil.handleError(log,
                        "Failed to resolve font with embed-url '" + embedUrl + "'", strict);
                return null;
            }
            embedUrl = source.getSystemId(); // absolute path/url
        }
        if (metricsUrl != null) {
            StreamSource source = (StreamSource)fontResolver.resolve(metricsUrl);
            if (source == null) {
                LogUtil.handleError(log,
                        "Failed to resolve font with metric-url '" + metricsUrl + "'", strict);
                return null;
            }
            metricsUrl = source.getSystemId(); // absolute path/url
        }
        boolean useKerning = fontCfg.getAttributeAsBoolean("kerning", true);
                        
        EmbedFontInfo fontInfo = null;
        Configuration[] tripletCfg = fontCfg.getChildren("font-triplet");
        // no font triplet info
        if (tripletCfg.length == 0) {
            LogUtil.handleError(log, "font without font-triplet", strict);

            // if not strict try to determine font info from the embed/metrics url
            File fontFile = CachedFontInfo.getFileFromUrls(new String[] {embedUrl, metricsUrl});
            if (fontFile != null) {
                FontInfoFinder finder = new FontInfoFinder();
                return finder.find(fontFile, fontResolver, fontCache);
            } else {
                return null;
            }
        } else {
            List tripleList = new java.util.ArrayList();
            for (int j = 0; j < tripletCfg.length; j++) {
                try {
                    String name = tripletCfg[j].getAttribute("name");
                    if (name == null) {
                        LogUtil.handleError(log, "font-triplet without name", strict);
                        continue;
                    }
                    String weightStr = tripletCfg[j].getAttribute("weight");
                    if (weightStr == null) {
                        LogUtil.handleError(log, "font-triplet without weight", strict);
                        continue;
                    }
                    int weight = FontUtil.parseCSS2FontWeight(weightStr);
                    String style = tripletCfg[j].getAttribute("style");
                    if (style == null) {
                        LogUtil.handleError(log, "font-triplet without style", strict);
                        continue;
                    }
                    tripleList.add(FontInfo.createFontKey(name, style, weight));
                } catch (ConfigurationException e) {
                    LogUtil.handleException(log, e, strict);
                }
            }
            
            fontInfo = new EmbedFontInfo(metricsUrl, useKerning, tripleList, embedUrl);
            
            if (fontCache != null) {
                if (!fontCache.containsFont(fontInfo)) {
                    fontCache.addFont(fontInfo);                    
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Adding font " + fontInfo.getEmbedFile()
                        + ", metric file " + fontInfo.getMetricsFile());
                for (int j = 0; j < tripleList.size(); ++j) {
                    FontTriplet triplet = (FontTriplet) tripleList.get(j);
                    log.debug("  Font triplet "
                            + triplet.getName() + ", "
                            + triplet.getStyle() + ", "
                            + triplet.getWeight());
                }
            }            
        }
        return fontInfo;
    }
    
}
