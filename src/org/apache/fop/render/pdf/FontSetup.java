/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.render.pdf;

// FOP
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontDescriptor;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFResources;
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

// Java
import java.util.Map;
import java.util.Iterator;
import java.util.List;

/**
 * sets up the PDF fonts.
 *
 * Assigns the font (with metrics) to internal names like "F1" and
 * assigns family-style-weight triplets to the fonts
 */
public class FontSetup {

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
     * @param fontInfo the font info object to set up
     * @param fontInfos ???
     * @param num starting index for internal font numbering
     */
    public static void addConfiguredFonts(FontInfo fontInfo, List fontInfos, int num) {
        if (fontInfos == null) {
            return; //No fonts to process
        }

        String internalName = null;
        //FontReader reader = null;

        for (int i = 0; i < fontInfos.size(); i++) {
            EmbedFontInfo configFontInfo = (EmbedFontInfo)fontInfos.get(i);

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
                    FontTriplet triplet = (FontTriplet)triplets.get(c);

                    int weight = 400;
                    try {
                        weight = Integer.parseInt(triplet.getWeight());
                        weight = ((int)weight / 100) * 100;
                        weight = Math.min(weight, 100);
                        weight = Math.max(weight, 900);
                    } catch (NumberFormatException nfe) {
                        /**@todo log this exception */
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
     * Add the fonts in the font info to the PDF document
     *
     * @param doc PDF document to add fonts to
     * @param resources PDFResources object to attach the font to
     * @param fontInfo font info object to get font information from
     */
    public static void addToResources(PDFDocument doc, PDFResources resources, FontInfo fontInfo) {
        Map fonts = fontInfo.getUsedFonts();
        Iterator e = fonts.keySet().iterator();
        while (e.hasNext()) {
            String f = (String)e.next();
            Font font = (Font)fonts.get(f);
            FontDescriptor desc = null;
            if (font instanceof FontDescriptor) {
                desc = (FontDescriptor)font;
            }
            resources.addFont(doc.makeFont(f, font.getFontName(),
                                           font.getEncoding(), font, desc));
        }
    }
}

