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

package org.apache.fop.render.java2d;

// FOP
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUtil;
import org.apache.fop.fonts.LazyFont;

/**
 * Sets up the Java2D/AWT fonts. It is similar to
 * org.apache.fop.render.fonts.FontSetup.
 * Assigns the font (with metrics) to internal names like "F1" and
 * assigns family-style-weight triplets to the fonts.
 */
public class FontSetup {

    /** logging instance */
    protected static Log log = LogFactory.getLog(FontSetup.class);

    private static final int LAST_PREDEFINED_FONT_NUMBER = 14;

    private static final Set HARDCODED_FONT_NAMES;

    static {
        HARDCODED_FONT_NAMES = new java.util.HashSet();
        HARDCODED_FONT_NAMES.add("any");
        HARDCODED_FONT_NAMES.add("sans-serif");
        HARDCODED_FONT_NAMES.add("serif");
        HARDCODED_FONT_NAMES.add("monospace");

        HARDCODED_FONT_NAMES.add("Helvetica");
        HARDCODED_FONT_NAMES.add("Times");
        HARDCODED_FONT_NAMES.add("Courier");
        HARDCODED_FONT_NAMES.add("Symbol");
        HARDCODED_FONT_NAMES.add("ZapfDingbats");
        HARDCODED_FONT_NAMES.add("Times Roman");
        HARDCODED_FONT_NAMES.add("Times-Roman");
        HARDCODED_FONT_NAMES.add("Computer-Modern-Typewriter");
    }

    /**
     * Sets up the font info object.
     *
     * Adds metrics for basic fonts and useful family-style-weight
     * triplets for lookup.
     *
     * @param fontInfo the font info object to set up
     * @param configuredFontList of fop config fonts
     * @param resolver for resolving the font file URI
     * @param graphics needed for access to font metrics
     */
    public static void setup(FontInfo fontInfo, List configuredFontList,
            FontResolver resolver, Graphics2D graphics) {
        FontMetricsMapper metric;
        int normal, bold, bolditalic, italic;

        /*
         * available java fonts are:
         * Serif - bold, normal, italic, bold-italic
         * SansSerif - bold, normal, italic, bold-italic
         * MonoSpaced - bold, normal, italic, bold-italic
         */
        normal = java.awt.Font.PLAIN;
        bold = java.awt.Font.BOLD;
        italic = java.awt.Font.ITALIC;
        bolditalic = java.awt.Font.BOLD + java.awt.Font.ITALIC;

        metric = new SystemFontMetricsMapper("SansSerif", normal, graphics);
        // --> goes to  F1
        fontInfo.addMetrics("F1", metric);
        metric = new SystemFontMetricsMapper("SansSerif", italic, graphics);
        // --> goes to  F2
        fontInfo.addMetrics("F2", metric);
        metric = new SystemFontMetricsMapper("SansSerif", bold, graphics);
        // --> goes to  F3
        fontInfo.addMetrics("F3", metric);
        metric = new SystemFontMetricsMapper("SansSerif", bolditalic, graphics);
        // --> goes to  F4
        fontInfo.addMetrics("F4", metric);


        metric = new SystemFontMetricsMapper("Serif", normal, graphics);
        // --> goes to  F5
        fontInfo.addMetrics("F5", metric);
        metric = new SystemFontMetricsMapper("Serif", italic, graphics);
        // --> goes to  F6
        fontInfo.addMetrics("F6", metric);
        metric = new SystemFontMetricsMapper("Serif", bold, graphics);
        // --> goes to  F7
        fontInfo.addMetrics("F7", metric);
        metric = new SystemFontMetricsMapper("Serif", bolditalic, graphics);
        // --> goes to  F8
        fontInfo.addMetrics("F8", metric);

        metric = new SystemFontMetricsMapper("MonoSpaced", normal, graphics);
        // --> goes to  F9
        fontInfo.addMetrics("F9", metric);
        metric = new SystemFontMetricsMapper("MonoSpaced", italic, graphics);
        // --> goes to  F10
        fontInfo.addMetrics("F10", metric);
        metric = new SystemFontMetricsMapper("MonoSpaced", bold, graphics);
        // --> goes to  F11
        fontInfo.addMetrics("F11", metric);
        metric = new SystemFontMetricsMapper("MonoSpaced", bolditalic, graphics);
        // --> goes to  F12
        fontInfo.addMetrics("F12", metric);

        metric = new SystemFontMetricsMapper("Serif", normal, graphics);
        //"Symbol" doesn't seem to work here, but "Serif" does the job just fine. *shrug*
        // --> goes to  F13 and F14
        fontInfo.addMetrics("F13", metric);
        fontInfo.addMetrics("F14", metric);

        // Custom type 1 fonts step 1/2
        // fontInfo.addMetrics("F15", new OMEP());
        // fontInfo.addMetrics("F16", new GaramondLightCondensed());
        // fontInfo.addMetrics("F17", new BauerBodoniBoldItalic());

        /* any is treated as serif */
        fontInfo.addFontProperties("F5", "any", "normal", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "any", "italic", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "any", "oblique", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F7", "any", "normal", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "any", "italic", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "any", "oblique", Font.WEIGHT_BOLD);

        fontInfo.addFontProperties("F1", "sans-serif", "normal", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F2", "sans-serif", "oblique", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F2", "sans-serif", "italic", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F3", "sans-serif", "normal", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F4", "sans-serif", "oblique", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F4", "sans-serif", "italic", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F5", "serif", "normal", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "serif", "oblique", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "serif", "italic", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F7", "serif", "normal", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "serif", "oblique", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "serif", "italic", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F9", "monospace", "normal", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F10", "monospace", "oblique", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F10", "monospace", "italic", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F11", "monospace", "normal", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F12", "monospace", "oblique", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F12", "monospace", "italic", Font.WEIGHT_BOLD);

        fontInfo.addFontProperties("F1", "Helvetica", "normal", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F2", "Helvetica", "oblique", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F2", "Helvetica", "italic", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F3", "Helvetica", "normal", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F4", "Helvetica", "oblique", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F4", "Helvetica", "italic", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F5", "Times", "normal", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times", "oblique", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times", "italic", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F7", "Times", "normal", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times", "oblique", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times", "italic", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F9", "Courier", "normal", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F10", "Courier", "oblique", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F10", "Courier", "italic", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F11", "Courier", "normal", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F12", "Courier", "oblique", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F12", "Courier", "italic", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F13", "Symbol", "normal", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F14", "ZapfDingbats", "normal", Font.WEIGHT_NORMAL);

        // Custom type 1 fonts step 2/2
        // fontInfo.addFontProperties("F15", "OMEP", "normal", FontInfo.NORMAL);
        // fontInfo.addFontProperties("F16", "Garamond-LightCondensed", "normal", FontInfo.NORMAL);
        // fontInfo.addFontProperties("F17", "BauerBodoni", "italic", FontInfo.BOLD);

        /* for compatibility with PassiveTex */
        fontInfo.addFontProperties("F5", "Times-Roman", "normal", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times-Roman", "oblique", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times-Roman", "italic", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F7", "Times-Roman", "normal", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times-Roman", "oblique", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times-Roman", "italic", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F5", "Times Roman", "normal", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times Roman", "oblique", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times Roman", "italic", Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F7", "Times Roman", "normal", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times Roman", "oblique", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times Roman", "italic", Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F9", "Computer-Modern-Typewriter",
                                   "normal", Font.WEIGHT_NORMAL);

        int lastNum = configureInstalledAWTFonts(fontInfo, graphics, LAST_PREDEFINED_FONT_NUMBER + 1);
        addConfiguredFonts(fontInfo, configuredFontList, resolver, lastNum++);
    }

    private static int configureInstalledAWTFonts(FontInfo fontInfo, Graphics2D graphics,
            int startNumber) {
        int num = startNumber;
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

        java.awt.Font[] fonts = env.getAllFonts();
        for (int i = 0; i < fonts.length; i++) {
            java.awt.Font f = fonts[i];
            if (HARDCODED_FONT_NAMES.contains(f.getName())) {
                continue; //skip
            }

            if (log.isTraceEnabled()) {
                log.trace("AWT Font: " + f.getFontName()
                        + ", family: " + f.getFamily()
                        + ", PS: " + f.getPSName()
                        + ", Name: " + f.getName()
                        + ", Angle: " + f.getItalicAngle()
                        + ", Style: " + f.getStyle());
            }

            String searchName = FontUtil.stripWhiteSpace(f.getName()).toLowerCase();
            String guessedStyle = FontUtil.guessStyle(searchName);
            int guessedWeight = FontUtil.guessWeight(searchName);

            num++;
            String fontKey = "F" + num;
            int style = convertToAWTFontStyle(guessedStyle, guessedWeight);
            addFontMetricsMapper(fontInfo, f.getName(), fontKey, graphics, style);

            //Register appropriate font triplets matching the font. Two different strategies:
            //Example: "Arial Bold", normal, normal
            addFontTriplet(fontInfo, f.getName(),
                    Font.STYLE_NORMAL, Font.WEIGHT_NORMAL, fontKey);
            if (!f.getName().equals(f.getFamily())) {
                //Example: "Arial", bold, normal
                addFontTriplet(fontInfo, f.getFamily(),
                        guessedStyle, guessedWeight, fontKey);
            }
        }
        return num;

    }

    /**
     * Add fonts from configuration file starting with internal name F<num>.
     *
     * @param fontInfo the font info object to set up
     * @param fontList a list of EmbedFontInfo objects
     * @param num starting index for internal font numbering
     * @param resolver the font resolver
     */
    private static void addConfiguredFonts(FontInfo fontInfo, List fontList, FontResolver resolver, int num) {

        if (fontList == null || fontList.size() < 1) {
            log.debug("No user configured fonts found.");
            return;
        }
        if (resolver == null) {
            // Ensure that we have minimal font resolution capabilities
            resolver = org.apache.fop.fonts.FontSetup
                .createMinimalFontResolver();
        }
        String internalName = null;

        for (int i = 0; i < fontList.size(); i++) {

            EmbedFontInfo configFontInfo = (EmbedFontInfo) fontList.get(i);
            String fontFile = configFontInfo.getEmbedFile();
            internalName = "F" + num;
            num++;
            try {
                FontMetricsMapper font = null;
                String metricsUrl = configFontInfo.getMetricsFile();
                // If the user specified an XML-based metrics file, we'll use it
                // Otherwise, calculate metrics directly from the font file.
                if (metricsUrl != null) {
                    LazyFont fontMetrics = new LazyFont(configFontInfo, resolver);
                    Source fontSource = resolver.resolve(configFontInfo.getEmbedFile());
                    font = new CustomFontMetricsMapper(fontMetrics, fontSource);
                } else {
                    CustomFont fontMetrics = FontLoader.loadFont(fontFile, null, resolver);
                    font = new CustomFontMetricsMapper(fontMetrics);
                }

                fontInfo.addMetrics(internalName, font);

                List triplets = configFontInfo.getFontTriplets();
                for (int c = 0; c < triplets.size(); c++) {
                    FontTriplet triplet = (FontTriplet) triplets.get(c);

                    if (log.isDebugEnabled()) {
                        log.debug("Registering: " + triplet + " under " + internalName);
                    }
                    fontInfo.addFontProperties(internalName, triplet);
                }
            } catch (Exception e) {
                log.warn("Unable to load custom font from file '" + fontFile + "'", e);
            }
        }
    }


    private static void addFontTriplet(FontInfo fontInfo, String fontName, String fontStyle,
            int fontWeight, String fontKey) {
        FontTriplet triplet = FontInfo.createFontKey(fontName, fontStyle, fontWeight);
        fontInfo.addFontProperties(fontKey, triplet);
    }

    private static void addFontMetricsMapper(FontInfo fontInfo, String family, String fontKey,
            Graphics2D graphics, int style) {
        FontMetricsMapper metric = new SystemFontMetricsMapper(family, style, graphics);
        fontInfo.addMetrics(fontKey, metric);
    }

    private static int convertToAWTFontStyle(String fontStyle, int fontWeight) {
        int style = java.awt.Font.PLAIN;
        if (fontWeight >= Font.WEIGHT_BOLD) {
            style |= java.awt.Font.BOLD;
        }
        if (!"normal".equals(fontStyle)) {
            style |= java.awt.Font.ITALIC;
        }
        return style;
    }

}

