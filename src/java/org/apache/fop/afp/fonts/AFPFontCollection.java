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

package org.apache.fop.afp.fonts;

import java.util.List;

import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;

/**
 * A base collection of AFP fonts
 */
public class AFPFontCollection implements FontCollection {

    private final AFPEventProducer eventProducer;

    private final List<AFPFontInfo> fontInfoList;

    /**
     * Main constructor
     *
     * @param eventBroadcaster the event broadcaster
     * @param fontInfoList the font info list
     */
    public AFPFontCollection(EventBroadcaster eventBroadcaster, List<AFPFontInfo> fontInfoList) {
        this.eventProducer = AFPEventProducer.Provider.get(eventBroadcaster);
        this.fontInfoList = fontInfoList;
    }

    /** {@inheritDoc} */
    public int setup(int start, FontInfo fontInfo) {
        int num = 1;
        if (fontInfoList != null && fontInfoList.size() > 0) {
            for (AFPFontInfo afpFontInfo : fontInfoList) {
                AFPFont afpFont = afpFontInfo.getAFPFont();
                List<FontTriplet> tripletList = afpFontInfo.getFontTriplets();
                for (FontTriplet triplet : tripletList) {
                    fontInfo.addMetrics("F" + num, afpFont);
                    fontInfo.addFontProperties("F" + num,
                            triplet.getName(), triplet.getStyle(), triplet.getWeight());
                    num++;
                }
            }
            checkDefaultFontAvailable(fontInfo, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
            checkDefaultFontAvailable(fontInfo, Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
            checkDefaultFontAvailable(fontInfo, Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
            checkDefaultFontAvailable(fontInfo, Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
        } else {
            eventProducer.warnDefaultFontSetup(this);

            // Go with a default base 12 configuration for AFP environments
            FontCollection base12FontCollection = new AFPBase12FontCollection(eventProducer);
            num = base12FontCollection.setup(num, fontInfo);
        }
        return num;
    }

    private void checkDefaultFontAvailable(FontInfo fontInfo, String style, int weight) {
        if (!fontInfo.hasFont("any", style, weight)) {
            eventProducer.warnMissingDefaultFont(this, style, weight);
        }
    }

}
