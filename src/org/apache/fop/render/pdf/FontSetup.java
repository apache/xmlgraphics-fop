/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pdf;

// FOP
import org.apache.fop.render.pdf.fonts.*;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.FontDescriptor;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFResources;

// Java
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * sets up the PDF fonts.
 *
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
     */
    public static void setup(FontInfo fontInfo, ArrayList embedList) {

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

        /* Add configured fonts */
        addConfiguredFonts(fontInfo, embedList, 15);
    }

    /**
     * Add fonts from configuration file starting with
     * internalnames F<num>
     */
    public static void addConfiguredFonts(FontInfo fontInfo, ArrayList fontInfos, int num) {
        if (fontInfos == null)
            return;

        String internalName = null;
        FontReader reader = null;

        for (int count = 0; count < fontInfos.size(); count++) {
            EmbedFontInfo configFontInfo =
                (EmbedFontInfo)fontInfos.get(count);

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
                    
                    ArrayList triplets = configFontInfo.getFontTriplets();
                    for (int c = 0; c < triplets.size(); c++) {
                        FontTriplet triplet = (FontTriplet)triplets.get(c);

                        int weight = 400;
                        try {
                            weight = Integer.parseInt(triplet.getWeight());
                            weight = ((int)weight/100) * 100;
                            if(weight < 100) weight = 100;
                            if(weight > 900) weight = 900;
                        } catch(NumberFormatException nfe) {

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
     * add the fonts in the font info to the PDF document
     *
     * @param doc PDF document to add fonts to
     * @param fontInfo font info object to get font information from
     */
    public static void addToResources(PDFDocument doc, PDFResources resources, FontInfo fontInfo) {
        HashMap fonts = fontInfo.getUsedFonts();
        Iterator e = fonts.keySet().iterator();
        while (e.hasNext()) {
            String f = (String)e.next();
            Font font = (Font)fonts.get(f);
            FontDescriptor desc = null;
            if (font instanceof FontDescriptor) {
                desc = (FontDescriptor)font;
            }
            resources.addFont(doc.makeFont(f, font.fontName(),
                                           font.encoding(), font, desc));
        }
    }
}

