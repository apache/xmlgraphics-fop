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

package org.apache.fop.render.java2d;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUris;
import org.apache.fop.fonts.LazyFont;

/**
 * A java2d configured font collection
 */
public class ConfiguredFontCollection implements FontCollection {

    private static Log log = LogFactory.getLog(ConfiguredFontCollection.class);

    private final InternalResourceResolver resourceResolver;
    private final List<EmbedFontInfo> embedFontInfoList;
    private final boolean useComplexScripts;

    /**
     * Main constructor
     * @param resourceResolver a font resolver
     * @param customFonts the list of custom fonts
     * @param useComplexScriptFeatures true if complex script features enabled
     */
    public ConfiguredFontCollection(InternalResourceResolver resourceResolver,
            List<EmbedFontInfo> customFonts, boolean useComplexScriptFeatures) {
        this.resourceResolver = resourceResolver;
        this.embedFontInfoList = customFonts;
        this.useComplexScripts = useComplexScriptFeatures;
    }

    /** {@inheritDoc} */
    public int setup(int start, FontInfo fontInfo) {
        int num = start;
        if (embedFontInfoList == null || embedFontInfoList.size() < 1) {
            log.debug("No user configured fonts found.");
            return num;
        }
        String internalName = null;

        for (EmbedFontInfo configFontInfo : embedFontInfoList) {
            internalName = "F" + num++;
            try {
                URI fontURI = configFontInfo.getEmbedURI();
                CustomFontMetricsMapper font;
                URI metricsURI = configFontInfo.getMetricsURI();
                // a local font file is given to Java directly, see CustomFontMetricsMapper.isFileBacked()
                File fontFile = toLocalFile(fontURI);
                // If the user specified an XML-based metrics file, we'll use it
                // Otherwise, calculate metrics directly from the font file.
                if (metricsURI != null) {
                    LazyFont fontMetrics = new LazyFont(configFontInfo, resourceResolver, useComplexScripts);
                    if (fontFile != null) {
                        font = new CustomFontMetricsMapper(fontMetrics, fontFile);
                    } else {
                        InputStream fontSource = resourceResolver.getResource(fontURI);
                        font = new CustomFontMetricsMapper(fontMetrics, fontSource);
                    }
                } else {
                    FontUris fontUris = configFontInfo.getFontUris();
                    font = new CustomFontMetricsMapper(fontUris, configFontInfo, resourceResolver, fontFile);
                    if (!configFontInfo.isLazyLoad()) {
                        font.getRealFont();
                    }
                }

                fontInfo.addMetrics(internalName, font);

                for (FontTriplet triplet : configFontInfo.getFontTriplets()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Registering: " + triplet + " under " + internalName);
                    }
                    fontInfo.addFontProperties(internalName, triplet);
                }
            } catch (Exception e) {
                log.warn("Unable to load custom font from file '" + configFontInfo.getEmbedURI()
                        + "'", e);
            }
        }
        return num;
    }

    /**
     * Returns the local file of a font, resolved against the font base like any other font URI.
     * @param fontURI the configured font URI
     * @return the readable local file, or null if the font is not a local file (jar, http, data
     * URI, missing file) or is read by a resolver of the application, which may serve other
     * content for the URI than the file of that path
     */
    private File toLocalFile(URI fontURI) {
        if (!resourceResolver.usesDefaultResourceResolver()) {
            return null;
        }
        URI resolved = resourceResolver.resolveFromBase(fontURI);
        if (!"file".equalsIgnoreCase(resolved.getScheme())) {
            return null;
        }
        try {
            File file = new File(resolved);
            return file.isFile() && file.canRead() ? file : null;
        } catch (IllegalArgumentException e) {
            // not a plain file URI, e.g. with an authority (UNC path) or a query
            return null;
        }
    }
}
