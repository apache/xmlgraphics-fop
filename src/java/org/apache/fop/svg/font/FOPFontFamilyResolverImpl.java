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

import java.io.InputStream;
import java.util.Map;

import org.apache.batik.bridge.FontFace;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTFontFamily;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.Typeface;

public class FOPFontFamilyResolverImpl implements FOPFontFamilyResolver {

    private final FontInfo fontInfo;

    public FOPFontFamilyResolverImpl(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
    }

    public FOPGVTFontFamily resolve(String familyName) {
        return resolve(familyName, new GVTFontFace(familyName));
    }

    public FOPGVTFontFamily resolve(String familyName, FontFace fontFace) {
        return resolve(familyName, (GVTFontFace) FontFace.createFontFace(familyName, fontFace));
    }

    private FOPGVTFontFamily resolve(String familyName, GVTFontFace fontFace) {
        FOPGVTFontFamily gvtFontFamily = null;
        FontTriplet triplet = fontInfo.fontLookup(familyName, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        if (fontInfo.hasFont(familyName, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL)) {
            gvtFontFamily = new FOPGVTFontFamily(fontInfo, familyName, triplet, fontFace);
        }
        return gvtFontFamily;
    }

    public GVTFontFamily loadFont(InputStream in, FontFace fontFace) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    public FOPGVTFontFamily getDefault() {
        return resolve("any");
    }

    public FOPGVTFontFamily getFamilyThatCanDisplay(char c) {
        Map<String, Typeface> fonts = fontInfo.getFonts();
        for (Typeface font : fonts.values()) {
            if (font.hasChar(c)) {
                String fontFamily = font.getFamilyNames().iterator().next();
                return new FOPGVTFontFamily(fontInfo, fontFamily,
                        new FontTriplet(fontFamily, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL),
                        new GVTFontFace(fontFamily));
            }
        }
        return null;
    }

}
