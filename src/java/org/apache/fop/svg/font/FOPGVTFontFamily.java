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

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTFontFamily;

import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.svg.ACIUtils;

public class FOPGVTFontFamily implements GVTFontFamily {

    private final FontInfo fontInfo;

    private final FontTriplet fontTriplet;

    private final String familyName;

    private GVTFontFace fontFace;

    public FOPGVTFontFamily(FontInfo fontInfo, String familyName, FontTriplet triplet, GVTFontFace fontFace) {
        this.fontInfo = fontInfo;
        this.fontTriplet = triplet;
        this.familyName = familyName;
        this.fontFace = fontFace;
    }

    public String getFamilyName() {
        return familyName;
    }

    public GVTFontFace getFontFace() {
        return fontFace;
    }

    public FOPGVTFont deriveFont(float size, AttributedCharacterIterator aci) {
        return deriveFont(size, aci.getAttributes());
    }

    public FOPGVTFont deriveFont(float size, @SuppressWarnings("rawtypes") Map attrs) {
        Float fontWeight = (Float) attrs.get(TextAttribute.WEIGHT);
        int weight = fontWeight == null ? fontTriplet.getWeight() : ACIUtils.toCSSWeight(fontWeight);
        Float fontStyle = (Float) attrs.get(TextAttribute.POSTURE);
        String style = fontStyle == null ? fontTriplet.getStyle() : ACIUtils.toStyle(fontStyle);
        FontTriplet triplet = fontInfo.fontLookup(fontTriplet.getName(), style, weight);
        return new FOPGVTFont(fontInfo.getFontInstance(triplet, (int) (size * 1000)), this);
    }

    public boolean isComplex() {
        return false;
    }

}
