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

package org.apache.fop.render.afp.fonts;

import java.util.Iterator;
import java.util.List;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.afp.AFPEventProducer;

/**
 * A base collection of AFP fonts
 */
public class AFPFontCollection implements FontCollection {

    private final EventBroadcaster eventBroadcaster;

    private final List/*<AFPFontInfo>*/ fontInfoList;

    /**
     * Main constructor
     *
     * @param eventBroadcaster the event broadcaster
     * @param fontInfoList the font info list
     */
    public AFPFontCollection(EventBroadcaster eventBroadcaster,
            List/*<AFPFontInfo>*/ fontInfoList) {
        this.eventBroadcaster = eventBroadcaster;
        this.fontInfoList = fontInfoList;
    }

    /** {@inheritDoc} */
    public int setup(int start, FontInfo fontInfo) {
        int num = 1;
        AFPEventProducer eventProducer = AFPEventProducer.Provider.get(eventBroadcaster);
        if (fontInfoList != null && fontInfoList.size() > 0) {
            for (Iterator it = fontInfoList.iterator(); it.hasNext();) {
                AFPFontInfo afpFontInfo = (AFPFontInfo)it.next();
                AFPFont afpFont = afpFontInfo.getAFPFont();
                List/*<FontTriplet>*/ tripletList = afpFontInfo.getFontTriplets();
                for (Iterator it2 = tripletList.iterator(); it2.hasNext();) {
                    FontTriplet triplet = (FontTriplet)it2.next();
                    fontInfo.addFontProperties("F" + num,
                            triplet.getName(), triplet.getStyle(), triplet.getWeight());
                    fontInfo.addMetrics("F" + num, afpFont);
                    num++;
                }
            }
            if (fontInfo.fontLookup("any", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL) == null) {
                eventProducer.warnMissingDefaultFont(this, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
            }
            if (fontInfo.fontLookup("any", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL) == null) {
                eventProducer.warnMissingDefaultFont(this, Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
            }
            if (fontInfo.fontLookup("any", Font.STYLE_NORMAL, Font.WEIGHT_BOLD) == null) {
                eventProducer.warnMissingDefaultFont(this, Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
            }
            if (fontInfo.fontLookup("any", Font.STYLE_ITALIC, Font.WEIGHT_BOLD) == null) {
                eventProducer.warnMissingDefaultFont(this, Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
            }
        } else {
            eventProducer.warnDefaultFontSetup(this);

            // Go with a default base 12 configuration for AFP environments
            FontCollection base12FontCollection = new AFPBase12FontCollection();
            num = base12FontCollection.setup(num, fontInfo);
        }
        return num;
    }

}
