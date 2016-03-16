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

package org.apache.fop.svg.font;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.truetype.OFFontLoader;

class FontInfoBuilder {

    public static final String DEJAVU_LGC_SERIF = "DejaVu LGC Serif";

    public static final String DROID_SANS_MONO = "Droid Sans Mono";

    private static final boolean USE_ADVANCED_BY_DEFAULT = true;

    private FontInfo fontInfo;

    private int fontKey;

    public FontInfoBuilder() {
        reset();
    }

    private void reset() {
        fontInfo = new FontInfo();
        fontKey = 1;
    }

    public FontInfoBuilder useDejaVuLGCSerif() {
        return useDejaVuLGCSerif(USE_ADVANCED_BY_DEFAULT);
    }

    public FontInfoBuilder useDejaVuLGCSerif(boolean useAdvanced) {
        try {
            return useFont(DEJAVU_LGC_SERIF, "DejaVuLGCSerif.ttf", useAdvanced);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FontInfoBuilder useDroidSansMono() {
        return useDroidSansMono(USE_ADVANCED_BY_DEFAULT);
    }

    public FontInfoBuilder useDroidSansMono(boolean useAdvanced) {
        try {
            return useFont(DROID_SANS_MONO, "DroidSansMono.ttf", useAdvanced);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FontInfoBuilder useFont(String fontName, String filename, boolean useAdvanced)
            throws IOException, URISyntaxException {
        URI baseURI = new File("test/resources/fonts/ttf").toURI();
        InternalResourceResolver resolver = ResourceResolverFactory.createDefaultInternalResourceResolver(baseURI);
        OFFontLoader fontLoader = new OFFontLoader(new URI(filename), null, true,
                EmbeddingMode.AUTO, EncodingMode.AUTO, true, useAdvanced, resolver);
        FontMetrics font = fontLoader.getFont();
        registerFont(font, "F" + fontKey++, fontName);
        return this;
    }

    private void registerFont(FontMetrics font, String key, String familyName) {
        fontInfo.addMetrics(key, font);
        fontInfo.addFontProperties(key, familyName, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
    }

    public FontInfo build() {
        FontInfo fontInfo = this.fontInfo;
        reset();
        return fontInfo;
    }
}
