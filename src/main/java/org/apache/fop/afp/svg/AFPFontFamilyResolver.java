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

package org.apache.fop.afp.svg;

import java.util.HashMap;
import java.util.Map;

import org.apache.batik.gvt.font.GVTFontFace;

import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.fonts.DoubleByteFont;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.svg.font.FOPFontFamilyResolverImpl;
import org.apache.fop.svg.font.FOPGVTFontFamily;
import org.apache.fop.svg.font.FilteringFontFamilyResolver;

public class AFPFontFamilyResolver extends FilteringFontFamilyResolver {

    private final FontInfo fontInfo;

    private final AFPEventProducer eventProducer;


    public AFPFontFamilyResolver(FontInfo fontInfo, EventBroadcaster eventBroadCaster) {
        super(new FOPFontFamilyResolverImpl(fontInfo));
        this.fontInfo = fontInfo;
        this.eventProducer = AFPEventProducer.Provider.get(eventBroadCaster);
    }

    @Override
    public FOPGVTFontFamily resolve(String familyName) {
        FOPGVTFontFamily fopGVTFontFamily = super.resolve(familyName);
        // TODO why don't DB fonts work with GOCA?!?
        if (fopGVTFontFamily != null && fopGVTFontFamily.deriveFont(1, new HashMap())
                .getFont().getFontMetrics() instanceof DoubleByteFont) {
            notifyDBFontRejection(fopGVTFontFamily.getFamilyName());
            fopGVTFontFamily = null;
        }
        return fopGVTFontFamily;
    }

    @Override
    public FOPGVTFontFamily getFamilyThatCanDisplay(char c) {
        Map<String, Typeface> fonts = fontInfo.getFonts();
        for (Typeface font : fonts.values()) {
            // TODO why don't DB fonts work with GOCA?!?
            if (font.hasChar(c) && !(font instanceof DoubleByteFont)) {
                String fontFamily = font.getFamilyNames().iterator().next();
                if (font instanceof DoubleByteFont) {
                    notifyDBFontRejection(font.getFontName());
                } else {
                    return new FOPGVTFontFamily(fontInfo, fontFamily,
                            new FontTriplet(fontFamily, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL),
                            new GVTFontFace(fontFamily));
                }

            }
        }
        return null;
    }

    private void notifyDBFontRejection(String fontFamily) {
        eventProducer.invalidDBFontInSVG(this, fontFamily);
    }

}
