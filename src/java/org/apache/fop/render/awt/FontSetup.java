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

package org.apache.fop.render.awt;

// FOP
import org.apache.fop.apps.Document;
import org.apache.fop.fonts.Font;

// Java
import java.awt.Graphics2D;

/**
 * Sets up the AWT fonts. It is similar to
 * org.apache.fop.render.pdf.FontSetup.
 * Assigns the font (with metrics) to internal names like "F1" and
 * assigns family-style-weight triplets to the fonts.
 */
public class FontSetup {


    /**
     * Sets up the font info object.
     *
     * Adds metrics for basic fonts and useful family-style-weight
     * triplets for lookup.
     *
     * @param fontInfo the font info object to set up
     * @param graphics needed for acces to font metrics
     */
    public static void setup(Document fontInfo, Graphics2D graphics) {
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

        metric = new FontMetricsMapper("SansSerif", normal, graphics);
        // --> goes to  F1
        fontInfo.addMetrics("F1", metric);
        metric = new FontMetricsMapper("SansSerif", italic, graphics);
        // --> goes to  F2
        fontInfo.addMetrics("F2", metric);
        metric = new FontMetricsMapper("SansSerif", bold, graphics);
        // --> goes to  F3
        fontInfo.addMetrics("F3", metric);
        metric = new FontMetricsMapper("SansSerif", bolditalic, graphics);
        // --> goes to  F4
        fontInfo.addMetrics("F4", metric);


        metric = new FontMetricsMapper("Serif", normal, graphics);
        // --> goes to  F5
        fontInfo.addMetrics("F5", metric);
        metric = new FontMetricsMapper("Serif", italic, graphics);
        // --> goes to  F6
        fontInfo.addMetrics("F6", metric);
        metric = new FontMetricsMapper("Serif", bold, graphics);
        // --> goes to  F7
        fontInfo.addMetrics("F7", metric);
        metric = new FontMetricsMapper("Serif", bolditalic, graphics);
        // --> goes to  F8
        fontInfo.addMetrics("F8", metric);

        metric = new FontMetricsMapper("MonoSpaced", normal, graphics);
        // --> goes to  F9
        fontInfo.addMetrics("F9", metric);
        metric = new FontMetricsMapper("MonoSpaced", italic, graphics);
        // --> goes to  F10
        fontInfo.addMetrics("F10", metric);
        metric = new FontMetricsMapper("MonoSpaced", bold, graphics);
        // --> goes to  F11
        fontInfo.addMetrics("F11", metric);
        metric = new FontMetricsMapper("MonoSpaced", bolditalic, graphics);
        // --> goes to  F12
        fontInfo.addMetrics("F12", metric);

        metric = new FontMetricsMapper("Symbol", bolditalic, graphics);
        // --> goes to  F13 and F14
        fontInfo.addMetrics("F13", metric);
        fontInfo.addMetrics("F14", metric);

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
    }

}

