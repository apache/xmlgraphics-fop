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

import java.util.List;

import org.apache.fop.render.PrintRenderer;

/**
 * Sets up a set of custom (embedded) fonts
 */
public class CustomFontCollection implements FontCollection {

    private PrintRenderer renderer = null;

    /**
     * A print renderer to configure
     * @param renderer a print renderer
     */
    public CustomFontCollection(PrintRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * {@inheritDoc}
     */
    public int setup(int num, FontInfo fontInfo) {
        List/*<EmbedFontInfo>*/ embedFontInfoList = renderer.getFontList();
        if (embedFontInfoList == null) {
            return num; //No fonts to process
        }

        FontResolver resolver = renderer.getFontResolver();
        if (resolver == null) {
            //Ensure that we have minimal font resolution capabilities
            resolver = FontManager.createMinimalFontResolver();
        }

        String internalName = null;
        //FontReader reader = null;

        for (int i = 0; i < embedFontInfoList.size(); i++) {
            EmbedFontInfo embedFontInfo = (EmbedFontInfo)embedFontInfoList.get(i);

            //String metricsFile = configFontInfo.getMetricsFile();
            internalName = "F" + num;
            num++;
            /*
            reader = new FontReader(metricsFile);
            reader.useKerning(configFontInfo.getKerning());
            reader.setFontEmbedPath(configFontInfo.getEmbedFile());
            fontInfo.addMetrics(internalName, reader.getFont());
            */

            LazyFont font = new LazyFont(embedFontInfo, resolver);
            fontInfo.addMetrics(internalName, font);

            List triplets = embedFontInfo.getFontTriplets();
            for (int tripletIndex = 0; tripletIndex < triplets.size(); tripletIndex++) {
                FontTriplet triplet = (FontTriplet) triplets.get(tripletIndex);
                fontInfo.addFontProperties(internalName, triplet);
            }
        }
        return num;
    }
}
