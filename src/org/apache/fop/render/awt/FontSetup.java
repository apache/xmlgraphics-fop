/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.awt;

// FOP
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.FontDescriptor;

// Java
import java.util.Enumeration;
import java.util.Hashtable;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * sets up the AWT fonts. It is similar to
 * org.apache.fop.render.pdf.FontSetup.
 * Assigns the font (with metrics) to internal names like "F1" and
 * assigns family-style-weight triplets to the fonts
 */
public class FontSetup {


    /**
     * sets up the font info object.
     *
     * adds metrics for basic fonts and useful family-style-weight
     * triplets for lookup
     *
     * @param fontInfo the font info object to set up
     * @param parent needed, since a live AWT component is needed
     * to get a valid java.awt.FontMetrics object
     */
    public static void setup(FontInfo fontInfo, Graphics2D graphics) {
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
        fontInfo.addFontProperties("F5", "any", "normal", FontInfo.NORMAL);
        fontInfo.addFontProperties("F6", "any", "italic", FontInfo.NORMAL);
        fontInfo.addFontProperties("F6", "any", "oblique", FontInfo.NORMAL);
        fontInfo.addFontProperties("F7", "any", "normal", FontInfo.BOLD);
        fontInfo.addFontProperties("F8", "any", "italic", FontInfo.BOLD);
        fontInfo.addFontProperties("F8", "any", "oblique", FontInfo.BOLD);

        fontInfo.addFontProperties("F1", "sans-serif", "normal", FontInfo.NORMAL);
        fontInfo.addFontProperties("F2", "sans-serif", "oblique", FontInfo.NORMAL);
        fontInfo.addFontProperties("F2", "sans-serif", "italic", FontInfo.NORMAL);
        fontInfo.addFontProperties("F3", "sans-serif", "normal", FontInfo.BOLD);
        fontInfo.addFontProperties("F4", "sans-serif", "oblique", FontInfo.BOLD);
        fontInfo.addFontProperties("F4", "sans-serif", "italic", FontInfo.BOLD);
        fontInfo.addFontProperties("F5", "serif", "normal", FontInfo.NORMAL);
        fontInfo.addFontProperties("F6", "serif", "oblique", FontInfo.NORMAL);
        fontInfo.addFontProperties("F6", "serif", "italic", FontInfo.NORMAL);
        fontInfo.addFontProperties("F7", "serif", "normal", FontInfo.BOLD);
        fontInfo.addFontProperties("F8", "serif", "oblique", FontInfo.BOLD);
        fontInfo.addFontProperties("F8", "serif", "italic", FontInfo.BOLD);
        fontInfo.addFontProperties("F9", "monospace", "normal", FontInfo.NORMAL);
        fontInfo.addFontProperties("F10", "monospace", "oblique", FontInfo.NORMAL);
        fontInfo.addFontProperties("F10", "monospace", "italic", FontInfo.NORMAL);
        fontInfo.addFontProperties("F11", "monospace", "normal", FontInfo.BOLD);
        fontInfo.addFontProperties("F12", "monospace", "oblique", FontInfo.BOLD);
        fontInfo.addFontProperties("F12", "monospace", "italic", FontInfo.BOLD);

        fontInfo.addFontProperties("F1", "Helvetica", "normal", FontInfo.NORMAL);
        fontInfo.addFontProperties("F2", "Helvetica", "oblique", FontInfo.NORMAL);
        fontInfo.addFontProperties("F2", "Helvetica", "italic", FontInfo.NORMAL);
        fontInfo.addFontProperties("F3", "Helvetica", "normal", FontInfo.BOLD);
        fontInfo.addFontProperties("F4", "Helvetica", "oblique", FontInfo.BOLD);
        fontInfo.addFontProperties("F4", "Helvetica", "italic", FontInfo.BOLD);
        fontInfo.addFontProperties("F5", "Times", "normal", FontInfo.NORMAL);
        fontInfo.addFontProperties("F6", "Times", "oblique", FontInfo.NORMAL);
        fontInfo.addFontProperties("F6", "Times", "italic", FontInfo.NORMAL);
        fontInfo.addFontProperties("F7", "Times", "normal", FontInfo.BOLD);
        fontInfo.addFontProperties("F8", "Times", "oblique", FontInfo.BOLD);
        fontInfo.addFontProperties("F8", "Times", "italic", FontInfo.BOLD);
        fontInfo.addFontProperties("F9", "Courier", "normal", FontInfo.NORMAL);
        fontInfo.addFontProperties("F10", "Courier", "oblique", FontInfo.NORMAL);
        fontInfo.addFontProperties("F10", "Courier", "italic", FontInfo.NORMAL);
        fontInfo.addFontProperties("F11", "Courier", "normal", FontInfo.BOLD);
        fontInfo.addFontProperties("F12", "Courier", "oblique", FontInfo.BOLD);
        fontInfo.addFontProperties("F12", "Courier", "italic", FontInfo.BOLD);
        fontInfo.addFontProperties("F13", "Symbol", "normal", FontInfo.NORMAL);
        fontInfo.addFontProperties("F14", "ZapfDingbats", "normal", FontInfo.NORMAL);

        // Custom type 1 fonts step 2/2
        // fontInfo.addFontProperties("F15", "OMEP", "normal", FontInfo.NORMAL);
        // fontInfo.addFontProperties("F16", "Garamond-LightCondensed", "normal", FontInfo.NORMAL);
        // fontInfo.addFontProperties("F17", "BauerBodoni", "italic", FontInfo.BOLD);

        /* for compatibility with PassiveTex */
        fontInfo.addFontProperties("F5", "Times-Roman", "normal", FontInfo.NORMAL);
        fontInfo.addFontProperties("F6", "Times-Roman", "oblique", FontInfo.NORMAL);
        fontInfo.addFontProperties("F6", "Times-Roman", "italic", FontInfo.NORMAL);
        fontInfo.addFontProperties("F7", "Times-Roman", "normal", FontInfo.BOLD);
        fontInfo.addFontProperties("F8", "Times-Roman", "oblique", FontInfo.BOLD);
        fontInfo.addFontProperties("F8", "Times-Roman", "italic", FontInfo.BOLD);
        fontInfo.addFontProperties("F5", "Times Roman", "normal", FontInfo.NORMAL);
        fontInfo.addFontProperties("F6", "Times Roman", "oblique", FontInfo.NORMAL);
        fontInfo.addFontProperties("F6", "Times Roman", "italic", FontInfo.NORMAL);
        fontInfo.addFontProperties("F7", "Times Roman", "normal", FontInfo.BOLD);
        fontInfo.addFontProperties("F8", "Times Roman", "oblique", FontInfo.BOLD);
        fontInfo.addFontProperties("F8", "Times Roman", "italic", FontInfo.BOLD);
        fontInfo.addFontProperties("F9", "Computer-Modern-Typewriter",
                                   "normal", FontInfo.NORMAL);
    }

}

