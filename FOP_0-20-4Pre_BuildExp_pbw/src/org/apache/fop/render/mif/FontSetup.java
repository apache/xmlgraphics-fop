/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.mif;

// FOP
import org.apache.fop.render.mif.fonts.*;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.FontDescriptor;
import org.apache.fop.mif.MIFDocument;
// import org.apache.fop.pdf.PDFResources;

// Java
import java.util.Iterator;
import java.util.HashMap;

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
    public static void setup(FontInfo fontInfo) {

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
        fontInfo.addFontProperties("F5", "any", "normal", "normal");
        fontInfo.addFontProperties("F6", "any", "italic", "normal");
        fontInfo.addFontProperties("F6", "any", "oblique", "normal");
        fontInfo.addFontProperties("F7", "any", "normal", "bold");
        fontInfo.addFontProperties("F8", "any", "italic", "bold");
        fontInfo.addFontProperties("F8", "any", "oblique", "bold");

        fontInfo.addFontProperties("F1", "sans-serif", "normal", "normal");
        fontInfo.addFontProperties("F2", "sans-serif", "oblique", "normal");
        fontInfo.addFontProperties("F2", "sans-serif", "italic", "normal");
        fontInfo.addFontProperties("F3", "sans-serif", "normal", "bold");
        fontInfo.addFontProperties("F4", "sans-serif", "oblique", "bold");
        fontInfo.addFontProperties("F4", "sans-serif", "italic", "bold");
        fontInfo.addFontProperties("F5", "serif", "normal", "normal");
        fontInfo.addFontProperties("F6", "serif", "oblique", "normal");
        fontInfo.addFontProperties("F6", "serif", "italic", "normal");
        fontInfo.addFontProperties("F7", "serif", "normal", "bold");
        fontInfo.addFontProperties("F8", "serif", "oblique", "bold");
        fontInfo.addFontProperties("F8", "serif", "italic", "bold");
        fontInfo.addFontProperties("F9", "monospace", "normal", "normal");
        fontInfo.addFontProperties("F10", "monospace", "oblique", "normal");
        fontInfo.addFontProperties("F10", "monospace", "italic", "normal");
        fontInfo.addFontProperties("F11", "monospace", "normal", "bold");
        fontInfo.addFontProperties("F12", "monospace", "oblique", "bold");
        fontInfo.addFontProperties("F12", "monospace", "italic", "bold");

        fontInfo.addFontProperties("F1", "Helvetica", "normal", "normal");
        fontInfo.addFontProperties("F2", "Helvetica", "oblique", "normal");
        fontInfo.addFontProperties("F2", "Helvetica", "italic", "normal");
        fontInfo.addFontProperties("F3", "Helvetica", "normal", "bold");
        fontInfo.addFontProperties("F4", "Helvetica", "oblique", "bold");
        fontInfo.addFontProperties("F4", "Helvetica", "italic", "bold");
        fontInfo.addFontProperties("F5", "Times", "normal", "normal");
        fontInfo.addFontProperties("F6", "Times", "oblique", "normal");
        fontInfo.addFontProperties("F6", "Times", "italic", "normal");
        fontInfo.addFontProperties("F7", "Times", "normal", "bold");
        fontInfo.addFontProperties("F8", "Times", "oblique", "bold");
        fontInfo.addFontProperties("F8", "Times", "italic", "bold");
        fontInfo.addFontProperties("F9", "Courier", "normal", "normal");
        fontInfo.addFontProperties("F10", "Courier", "oblique", "normal");
        fontInfo.addFontProperties("F10", "Courier", "italic", "normal");
        fontInfo.addFontProperties("F11", "Courier", "normal", "bold");
        fontInfo.addFontProperties("F12", "Courier", "oblique", "bold");
        fontInfo.addFontProperties("F12", "Courier", "italic", "bold");
        fontInfo.addFontProperties("F13", "Symbol", "normal", "normal");
        fontInfo.addFontProperties("F14", "ZapfDingbats", "normal", "normal");

        // Custom type 1 fonts step 2/2
        // fontInfo.addFontProperties("F15", "OMEP", "normal", "normal");
        // fontInfo.addFontProperties("F16", "Garamond-LightCondensed", "normal", "normal");
        // fontInfo.addFontProperties("F17", "BauerBodoni", "italic", "bold");

        /* for compatibility with PassiveTex */
        fontInfo.addFontProperties("F5", "Times-Roman", "normal", "normal");
        fontInfo.addFontProperties("F6", "Times-Roman", "oblique", "normal");
        fontInfo.addFontProperties("F6", "Times-Roman", "italic", "normal");
        fontInfo.addFontProperties("F7", "Times-Roman", "normal", "bold");
        fontInfo.addFontProperties("F8", "Times-Roman", "oblique", "bold");
        fontInfo.addFontProperties("F8", "Times-Roman", "italic", "bold");
        fontInfo.addFontProperties("F5", "Times Roman", "normal", "normal");
        fontInfo.addFontProperties("F6", "Times Roman", "oblique", "normal");
        fontInfo.addFontProperties("F6", "Times Roman", "italic", "normal");
        fontInfo.addFontProperties("F7", "Times Roman", "normal", "bold");
        fontInfo.addFontProperties("F8", "Times Roman", "oblique", "bold");
        fontInfo.addFontProperties("F8", "Times Roman", "italic", "bold");
        fontInfo.addFontProperties("F9", "Computer-Modern-Typewriter",
                                   "normal", "normal");
    }

    /**
     * add the fonts in the font info to the PDF document
     *
     * @param doc PDF document to add fonts to
     * @param fontInfo font info object to get font information from
     */
    public static void addToFontFormat(MIFDocument mifDoc,
                                       FontInfo fontInfo) {

        HashMap fonts = fontInfo.getFonts();
        Iterator e = fonts.keySet().iterator();
        while (e.hasNext()) {
            String f = (String)e.next();
            Font font = (Font)fonts.get(f);
            FontDescriptor desc = null;
            if (font instanceof FontDescriptor) {
                desc = (FontDescriptor)font;
            }
            /*
             * mifDoc.makeFont(f,font.fontName(),
             * font.encoding(),
             * font,
             * desc
             * );
             */

        }
    }

}
