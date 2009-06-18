/*-- $Id: FontSetup.java --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */
package org.apache.fop.render.awt;

// FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.FontDescriptor;

// Java
import java.util.Enumeration;
import java.util.Hashtable;
import java.awt.Font;
import java.awt.Component;

/**
 * sets up the AWT fonts. It is similar to
 * org.apache.fop.render.pdf.FontSetup.
 * Assigns the font (with metrics) to internal names like "F1" and
 *  assigns family-style-weight triplets to the fonts
 */
public class FontSetup {


    /** sets up the font info object.
    *
    * adds metrics for basic fonts and useful family-style-weight
    * triplets for lookup
    *
    * @param fontInfo the font info object to set up
    * @param parent needed, since a live AWT component is needed
    *               to get a valid java.awt.FontMetrics object
    */
    public static void setup(FontInfo fontInfo, Component parent) {
        FontMetricsMapper metric;
        int normal, bold, bolditalic, italic;

        MessageHandler.logln("setting up fonts");

        /* available java fonts are:
           Serif - bold, normal, italic, bold-italic
           SansSerif - bold, normal, italic, bold-italic
           MonoSpaced - bold, normal, italic, bold-italic
        */
        normal = java.awt.Font.PLAIN;
        bold = java.awt.Font.BOLD;
        italic = java.awt.Font.ITALIC;
        bolditalic = java.awt.Font.BOLD + java.awt.Font.ITALIC;

        metric = new FontMetricsMapper("SansSerif", normal, parent);
        // --> goes to  F1
        fontInfo.addMetrics("F1", metric);
        metric = new FontMetricsMapper("SansSerif",italic, parent);
        // --> goes to  F2
        fontInfo.addMetrics("F2", metric);
        metric = new FontMetricsMapper("SansSerif", bold, parent);
        // --> goes to  F3
        fontInfo.addMetrics("F3", metric);
        metric = new FontMetricsMapper("SansSerif", bolditalic, parent);
        // --> goes to  F4
        fontInfo.addMetrics("F4", metric);


        metric = new FontMetricsMapper("Serif", normal, parent);
        // --> goes to  F5
        fontInfo.addMetrics("F5", metric);
        metric = new FontMetricsMapper("Serif", italic, parent);
        // --> goes to  F6
        fontInfo.addMetrics("F6", metric);
        metric = new FontMetricsMapper("Serif", bold, parent);
        // --> goes to  F7
        fontInfo.addMetrics("F7", metric);
        metric = new FontMetricsMapper("Serif", bolditalic, parent);
        // --> goes to  F8
        fontInfo.addMetrics("F8", metric);

        metric = new FontMetricsMapper("MonoSpaced", normal, parent);
        // --> goes to  F9
        fontInfo.addMetrics("F9", metric);
        metric = new FontMetricsMapper("MonoSpaced", italic, parent);
        // --> goes to  F10
        fontInfo.addMetrics("F10", metric);
        metric = new FontMetricsMapper("MonoSpaced", bold, parent);
        // --> goes to  F11
        fontInfo.addMetrics("F11", metric);
        metric = new FontMetricsMapper("MonoSpaced", bolditalic, parent);
        // --> goes to  F12
        fontInfo.addMetrics("F12", metric);

        metric = new FontMetricsMapper("Symbol", bolditalic, parent);
        // --> goes to  F13 and F14
        fontInfo.addMetrics("F13", metric);
        fontInfo.addMetrics("F14", metric);

        //Custom type 1 fonts step 1/2
        //    fontInfo.addMetrics("F15", new OMEP());
        //    fontInfo.addMetrics("F16", new GaramondLightCondensed());
        //    fontInfo.addMetrics("F17", new BauerBodoniBoldItalic());

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

        //Custom type 1 fonts step 2/2
        //    fontInfo.addFontProperties("F15", "OMEP", "normal", "normal");
        //    fontInfo.addFontProperties("F16", "Garamond-LightCondensed", "normal", "normal");
        //    fontInfo.addFontProperties("F17", "BauerBodoni", "italic", "bold");

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

}










