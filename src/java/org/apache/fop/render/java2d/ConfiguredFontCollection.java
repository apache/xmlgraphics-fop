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

import java.util.List;

import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.LazyFont;

/**
 * A java2d configured font collection
 */
public class ConfiguredFontCollection implements FontCollection {

    private static Log log = LogFactory.getLog(ConfiguredFontCollection.class);

    private FontResolver fontResolver;
    private List/*<EmbedFontInfo>*/ embedFontInfoList;

    /**
     * Main constructor
     * @param fontResolver a font resolver
     * @param customFonts the list of custom fonts
     */
    public ConfiguredFontCollection(FontResolver fontResolver,
            List/*<EmbedFontInfo>*/ customFonts) {
        this.fontResolver = fontResolver;
        if (this.fontResolver == null) {
            //Ensure that we have minimal font resolution capabilities
            this.fontResolver = FontManager.createMinimalFontResolver();
        }
        this.embedFontInfoList = customFonts;
    }

    /** {@inheritDoc} */
    public int setup(int start, FontInfo fontInfo) {
        int num = start;
        if (embedFontInfoList == null || embedFontInfoList.size() < 1) {
            log.debug("No user configured fonts found.");
            return num;
        }
        String internalName = null;

        for (int i = 0; i < embedFontInfoList.size(); i++) {

            EmbedFontInfo configFontInfo = (EmbedFontInfo) embedFontInfoList.get(i);
            String fontFile = configFontInfo.getEmbedFile();
            internalName = "F" + num;
            num++;
            try {
                FontMetricsMapper font = null;
                String metricsUrl = configFontInfo.getMetricsFile();
                // If the user specified an XML-based metrics file, we'll use it
                // Otherwise, calculate metrics directly from the font file.
                if (metricsUrl != null) {
                    LazyFont fontMetrics = new LazyFont(configFontInfo, fontResolver);
                    Source fontSource = fontResolver.resolve(configFontInfo.getEmbedFile());
                    font = new CustomFontMetricsMapper(fontMetrics, fontSource);
                } else {
                    CustomFont fontMetrics = FontLoader.loadFont(
                            fontFile, null, true, EncodingMode.AUTO,
                            configFontInfo.getKerning(),
                            configFontInfo.getAdvanced(), fontResolver);
                    font = new CustomFontMetricsMapper(fontMetrics);
                }

                fontInfo.addMetrics(internalName, font);

                List triplets = configFontInfo.getFontTriplets();
                for (int c = 0; c < triplets.size(); c++) {
                    FontTriplet triplet = (FontTriplet) triplets.get(c);

                    if (log.isDebugEnabled()) {
                        log.debug("Registering: " + triplet + " under " + internalName);
                    }
                    fontInfo.addFontProperties(internalName, triplet);
                }
            } catch (Exception e) {
                log.warn("Unable to load custom font from file '" + fontFile + "'", e);
            }
        }
        return num;
    }
}
