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
import org.apache.fop.fonts.base14.Courier;
import org.apache.fop.fonts.base14.Helvetica;
import org.apache.fop.fonts.base14.TimesRoman;
import org.apache.fop.render.afp.AFPEventProducer;

/**
 * A base collection of AFP fonts
 */
public class AFPFontCollection implements FontCollection {

    private EventBroadcaster eventBroadcaster;
    private List/*<EmbedFontInfo>*/ embedFontInfoList;
    
    /**
     * Main constructor
     * 
     * @param eventBroadcaster the event broadcaster
     * @param embedFontInfoList the embed font info list
     */
    public AFPFontCollection(EventBroadcaster eventBroadcaster, 
            List/*<EmbedFontInfo>*/ embedFontInfoList) {
        this.eventBroadcaster = eventBroadcaster;
        this.embedFontInfoList = embedFontInfoList;
    }
    
    /** {@inheritDoc} */
    public int setup(int start, FontInfo fontInfo) {
        int num = 1;
        if (embedFontInfoList != null && embedFontInfoList.size() > 0) {
            for (Iterator it = embedFontInfoList.iterator(); it.hasNext();) {
                AFPFontInfo afi = (AFPFontInfo)it.next();
                AFPFont bf = (AFPFont)afi.getAFPFont();
                for (Iterator it2 = afi.getFontTriplets().iterator(); it2.hasNext();) {
                    FontTriplet ft = (FontTriplet)it2.next();
                    fontInfo.addFontProperties("F" + num, ft.getName()
                                                    , ft.getStyle(), ft.getWeight());
                    fontInfo.addMetrics("F" + num, bf);
                    num++;
                }
            }
        } else {
            AFPEventProducer eventProducer = AFPEventProducer.Provider.get(eventBroadcaster);
            eventProducer.warnDefaultFontSetup(this);
        }
        if (fontInfo.fontLookup("sans-serif", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL) == null) {
            CharacterSet cs  = new FopCharacterSet("T1V10500", "Cp500", "CZH200  ",
                    1, new Helvetica());
            AFPFont bf = new OutlineFont("Helvetica", cs);
            fontInfo.addFontProperties(
                    "F" + num, "sans-serif", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
            fontInfo.addMetrics("F" + num, bf);
            num++;
        }
        if (fontInfo.fontLookup("serif", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL) == null) {
            CharacterSet cs  = new FopCharacterSet("T1V10500", "Cp500", "CZN200  ",
                    1, new TimesRoman());
            AFPFont bf = new OutlineFont("Helvetica", cs);
            fontInfo.addFontProperties("F" + num, "serif", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
            fontInfo.addMetrics("F" + num, bf);
            num++;
        }
        if (fontInfo.fontLookup("monospace", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL) == null) {
            CharacterSet cs  = new FopCharacterSet("T1V10500", "Cp500", "CZ4200  ",
                    1, new Courier());
            AFPFont bf = new OutlineFont("Helvetica", cs);
            fontInfo.addFontProperties(
                    "F" + num, "monospace", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
            fontInfo.addMetrics("F" + num, bf);
            num++;
        }
        if (fontInfo.fontLookup("any", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL) == null) {
            FontTriplet ft = fontInfo.fontLookup(
                    "sans-serif", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
            fontInfo.addFontProperties(
                    fontInfo.getInternalFontKey(ft), "any", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
            num++;
        }
        return num;
    }

}
