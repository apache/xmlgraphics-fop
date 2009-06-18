/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

// FOP (base 14 fonts)
import org.apache.fop.fonts.base14.Helvetica;
import org.apache.fop.fonts.base14.HelveticaBold;
import org.apache.fop.fonts.base14.HelveticaOblique;
import org.apache.fop.fonts.base14.HelveticaBoldOblique;
import org.apache.fop.fonts.base14.TimesRoman;
import org.apache.fop.fonts.base14.TimesBold;
import org.apache.fop.fonts.base14.TimesItalic;
import org.apache.fop.fonts.base14.TimesBoldItalic;
import org.apache.fop.fonts.base14.Courier;
import org.apache.fop.fonts.base14.CourierBold;
import org.apache.fop.fonts.base14.CourierOblique;
import org.apache.fop.fonts.base14.CourierBoldOblique;
import org.apache.fop.fonts.base14.Symbol;
import org.apache.fop.fonts.base14.ZapfDingbats;

// commons logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Avalon
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

// Java
import java.util.List;

/**
 * Default fonts for FOP application; currently this uses PDF's fonts
 * by default.
 *
 * Assigns the font (with metrics) to internal names like "F1" and
 * assigns family-style-weight triplets to the fonts
 */
public class FontSetup {

    /**
     * logging instance
     */
    protected static Log log = LogFactory.getLog("org.apache.fop.fonts");
    
    /**
     * Sets up the font info object.
     *
     * Adds metrics for basic fonts and useful family-style-weight
     * triplets for lookup.
     *
     * @param fontInfo the font info object to set up
     * @param embedList ???
     */
    public static void setup(FontInfo fontInfo, List embedList) {

        fontInfo.addMetrics("F1", new Helvetica());
        fontInfo.addMetrics("F2", new HelveticaOblique());
        fontInfo.addMetrics("F3", new HelveticaBold());
        fontInfo.addMetrics("F4", new HelveticaBoldOblique());
        fontInfo.addMetrics("F5", new TimesRoman());
        fontInfo.addMetrics("F6", new TimesItalic());
        fontInfo.addMetrics("F7", new TimesBold());
        fontInfo.addMetrics("F8", new TimesBoldItalic());
        fontInfo.addMetrics("F9", new Courier());
        fontInfo.addMetrics("F10", new CourierOblique());
        fontInfo.addMetrics("F11", new CourierBold());
        fontInfo.addMetrics("F12", new CourierBoldOblique());
        fontInfo.addMetrics("F13", new Symbol());
        fontInfo.addMetrics("F14", new ZapfDingbats());

        // Custom type 1 fonts step 1/2
        // fontInfo.addMetrics("F15", new OMEP());
        // fontInfo.addMetrics("F16", new GaramondLightCondensed());
        // fontInfo.addMetrics("F17", new BauerBodoniBoldItalic());

        /* any is treated as serif */
        fontInfo.addFontProperties("F5", "any", "normal", Font.NORMAL);
        fontInfo.addFontProperties("F6", "any", "italic", Font.NORMAL);
        fontInfo.addFontProperties("F6", "any", "oblique", Font.NORMAL);
        fontInfo.addFontProperties("F7", "any", "normal", Font.BOLD);
        fontInfo.addFontProperties("F8", "any", "italic", Font.BOLD);
        fontInfo.addFontProperties("F8", "any", "oblique", Font.BOLD);

        fontInfo.addFontProperties("F1", "sans-serif", "normal", Font.NORMAL);
        fontInfo.addFontProperties("F2", "sans-serif", "oblique", Font.NORMAL);
        fontInfo.addFontProperties("F2", "sans-serif", "italic", Font.NORMAL);
        fontInfo.addFontProperties("F3", "sans-serif", "normal", Font.BOLD);
        fontInfo.addFontProperties("F4", "sans-serif", "oblique", Font.BOLD);
        fontInfo.addFontProperties("F4", "sans-serif", "italic", Font.BOLD);
        fontInfo.addFontProperties("F5", "serif", "normal", Font.NORMAL);
        fontInfo.addFontProperties("F6", "serif", "oblique", Font.NORMAL);
        fontInfo.addFontProperties("F6", "serif", "italic", Font.NORMAL);
        fontInfo.addFontProperties("F7", "serif", "normal", Font.BOLD);
        fontInfo.addFontProperties("F8", "serif", "oblique", Font.BOLD);
        fontInfo.addFontProperties("F8", "serif", "italic", Font.BOLD);
        fontInfo.addFontProperties("F9", "monospace", "normal", Font.NORMAL);
        fontInfo.addFontProperties("F10", "monospace", "oblique", Font.NORMAL);
        fontInfo.addFontProperties("F10", "monospace", "italic", Font.NORMAL);
        fontInfo.addFontProperties("F11", "monospace", "normal", Font.BOLD);
        fontInfo.addFontProperties("F12", "monospace", "oblique", Font.BOLD);
        fontInfo.addFontProperties("F12", "monospace", "italic", Font.BOLD);

        fontInfo.addFontProperties("F1", "Helvetica", "normal", Font.NORMAL);
        fontInfo.addFontProperties("F2", "Helvetica", "oblique", Font.NORMAL);
        fontInfo.addFontProperties("F2", "Helvetica", "italic", Font.NORMAL);
        fontInfo.addFontProperties("F3", "Helvetica", "normal", Font.BOLD);
        fontInfo.addFontProperties("F4", "Helvetica", "oblique", Font.BOLD);
        fontInfo.addFontProperties("F4", "Helvetica", "italic", Font.BOLD);
        fontInfo.addFontProperties("F5", "Times", "normal", Font.NORMAL);
        fontInfo.addFontProperties("F6", "Times", "oblique", Font.NORMAL);
        fontInfo.addFontProperties("F6", "Times", "italic", Font.NORMAL);
        fontInfo.addFontProperties("F7", "Times", "normal", Font.BOLD);
        fontInfo.addFontProperties("F8", "Times", "oblique", Font.BOLD);
        fontInfo.addFontProperties("F8", "Times", "italic", Font.BOLD);
        fontInfo.addFontProperties("F9", "Courier", "normal", Font.NORMAL);
        fontInfo.addFontProperties("F10", "Courier", "oblique", Font.NORMAL);
        fontInfo.addFontProperties("F10", "Courier", "italic", Font.NORMAL);
        fontInfo.addFontProperties("F11", "Courier", "normal", Font.BOLD);
        fontInfo.addFontProperties("F12", "Courier", "oblique", Font.BOLD);
        fontInfo.addFontProperties("F12", "Courier", "italic", Font.BOLD);
        fontInfo.addFontProperties("F13", "Symbol", "normal", Font.NORMAL);
        fontInfo.addFontProperties("F14", "ZapfDingbats", "normal", Font.NORMAL);

        // Custom type 1 fonts step 2/2
        // fontInfo.addFontProperties("F15", "OMEP", "normal", FontInfo.NORMAL);
        // fontInfo.addFontProperties("F16", "Garamond-LightCondensed", "normal", FontInfo.NORMAL);
        // fontInfo.addFontProperties("F17", "BauerBodoni", "italic", FontInfo.BOLD);

        /* for compatibility with PassiveTex */
        fontInfo.addFontProperties("F5", "Times-Roman", "normal", Font.NORMAL);
        fontInfo.addFontProperties("F6", "Times-Roman", "oblique", Font.NORMAL);
        fontInfo.addFontProperties("F6", "Times-Roman", "italic", Font.NORMAL);
        fontInfo.addFontProperties("F7", "Times-Roman", "normal", Font.BOLD);
        fontInfo.addFontProperties("F8", "Times-Roman", "oblique", Font.BOLD);
        fontInfo.addFontProperties("F8", "Times-Roman", "italic", Font.BOLD);
        fontInfo.addFontProperties("F5", "Times Roman", "normal", Font.NORMAL);
        fontInfo.addFontProperties("F6", "Times Roman", "oblique", Font.NORMAL);
        fontInfo.addFontProperties("F6", "Times Roman", "italic", Font.NORMAL);
        fontInfo.addFontProperties("F7", "Times Roman", "normal", Font.BOLD);
        fontInfo.addFontProperties("F8", "Times Roman", "oblique", Font.BOLD);
        fontInfo.addFontProperties("F8", "Times Roman", "italic", Font.BOLD);
        fontInfo.addFontProperties("F9", "Computer-Modern-Typewriter",
                                   "normal", Font.NORMAL);

        /* Add configured fonts */
        addConfiguredFonts(fontInfo, embedList, 15);
    }

    /**
     * Add fonts from configuration file starting with internal name F<num>.
     * @param fontInfo the font info object to set up
     * @param fontInfoList
     * @param num starting index for internal font numbering
     */
    public static void addConfiguredFonts(FontInfo fontInfo, List fontInfoList, int num) {
        if (fontInfoList == null) {
            return; //No fonts to process
        }

        String internalName = null;
        //FontReader reader = null;

        for (int i = 0; i < fontInfoList.size(); i++) {
            EmbedFontInfo configFontInfo = (EmbedFontInfo) fontInfoList.get(i);

            String metricsFile = configFontInfo.getMetricsFile();
            if (metricsFile != null) {
                internalName = "F" + num;
                num++;
                /*
                reader = new FontReader(metricsFile);
                reader.useKerning(configFontInfo.getKerning());
                reader.setFontEmbedPath(configFontInfo.getEmbedFile());
                fontInfo.addMetrics(internalName, reader.getFont());
                */
                LazyFont font = new LazyFont(configFontInfo.getEmbedFile(),
                                             metricsFile,
                                             configFontInfo.getKerning());
                fontInfo.addMetrics(internalName, font);

                List triplets = configFontInfo.getFontTriplets();
                for (int c = 0; c < triplets.size(); c++) {
                    FontTriplet triplet = (FontTriplet) triplets.get(c);

                    int weight = FontUtil.parseCSS2FontWeight(triplet.getWeight());
                    if (log.isDebugEnabled()) {
                        log.debug("Registering: " + triplet + " weight=" + weight);
                    }
                    fontInfo.addFontProperties(internalName,
                                               triplet.getName(),
                                               triplet.getStyle(),
                                               weight);
                }
            }
        }
    }

    /**
     * Builds a list of EmbedFontInfo objects for use with the setup() method.
     * @param cfg Configuration object
     * @return List the newly created list of fonts
     * @throws ConfigurationException if something's wrong with the config data
     */
    public static List buildFontListFromConfiguration(Configuration cfg)
            throws ConfigurationException {
        List fontList = new java.util.ArrayList();
        Configuration[] font = cfg.getChild("fonts").getChildren("font");
        for (int i = 0; i < font.length; i++) {
            Configuration[] triple = font[i].getChildren("font-triplet");
            List tripleList = new java.util.ArrayList();
            for (int j = 0; j < triple.length; j++) {
                tripleList.add(new FontTriplet(triple[j].getAttribute("name"),
                                               triple[j].getAttribute("weight"),
                                               triple[j].getAttribute("style")));
            }

            EmbedFontInfo efi;
            efi = new EmbedFontInfo(font[i].getAttribute("metrics-url"),
                                    font[i].getAttributeAsBoolean("kerning", false),
                                    tripleList, font[i].getAttribute("embed-url", null));

            if (log.isDebugEnabled()) {
                log.debug("Adding font " + efi.getEmbedFile()
                          + ", metric file " + efi.getMetricsFile());
                for (int j = 0; j < tripleList.size(); ++j) {
                    FontTriplet triplet = (FontTriplet) tripleList.get(j);
                    log.debug("Font triplet "
                              + triplet.getName() + ", "
                              + triplet.getWeight() + ", "
                              + triplet.getStyle());
                }
            }

            fontList.add(efi);
        }
        return fontList;
    }
}

