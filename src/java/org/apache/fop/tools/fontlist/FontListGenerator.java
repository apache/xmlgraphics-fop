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

package org.apache.fop.tools.fontlist;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.FontEventListener;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;

/**
 * Generates a list of available fonts.
 */
public class FontListGenerator {

    /**
     * List all fonts configured for a particular output format (identified by MIME type).
     * The sorted map returned looks like this:
     * <code>SortedMap&lt;String/font-family, List&lt;{@link FontSpec}&gt;&gt;</code>
     * @param fopFactory the FOP factory (already configured)
     * @param mime the MIME type identified the selected output format
     * @param listener a font event listener to catch any font-related errors while listing fonts
     * @return the map of font families
     * @throws FOPException if an error occurs setting up the fonts
     */
    public SortedMap listFonts(FopFactory fopFactory, String mime, FontEventListener listener)
            throws FOPException {
        FontInfo fontInfo = setupFonts(fopFactory, mime, listener);
        SortedMap fontFamilies = buildFamilyMap(fontInfo);
        return fontFamilies;
    }

    private FontInfo setupFonts(FopFactory fopFactory, String mime, FontEventListener listener)
                throws FOPException {
        FOUserAgent userAgent = fopFactory.newFOUserAgent();

        //The document handler is only instantiated to get access to its configurator!
        IFDocumentHandler documentHandler
            = fopFactory.getRendererFactory().createDocumentHandler(userAgent, mime);
        IFDocumentHandlerConfigurator configurator = documentHandler.getConfigurator();

        FontInfo fontInfo = new FontInfo();
        configurator.setupFontInfo(documentHandler, fontInfo);
        return fontInfo;
    }

    private SortedMap buildFamilyMap(FontInfo fontInfo) {
        Map fonts = fontInfo.getFonts();
        Set keyBag = new java.util.HashSet(fonts.keySet());

        Map keys = new java.util.HashMap();
        SortedMap fontFamilies = new java.util.TreeMap();
        //SortedMap<String/font-family, List<FontSpec>>

        Iterator iter = fontInfo.getFontTriplets().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            FontTriplet triplet = (FontTriplet)entry.getKey();
            String key = (String)entry.getValue();
            FontSpec container;
            if (keyBag.contains(key)) {
                keyBag.remove(key);

                FontMetrics metrics = (FontMetrics)fonts.get(key);

                container = new FontSpec(key, metrics);
                container.addFamilyNames(metrics.getFamilyNames());
                keys.put(key, container);
                String firstFamilyName = (String)container.getFamilyNames().first();
                List containers = (List)fontFamilies.get(firstFamilyName);
                if (containers == null) {
                    containers = new java.util.ArrayList();
                    fontFamilies.put(firstFamilyName, containers);
                }
                containers.add(container);
                Collections.sort(containers);

            } else {
                container = (FontSpec)keys.get(key);
            }
            container.addTriplet(triplet);
        }

        return fontFamilies;
    }

}
