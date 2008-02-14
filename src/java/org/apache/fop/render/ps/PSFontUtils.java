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

package org.apache.fop.render.ps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;

/**
 * Utility code for font handling in PostScript.
 */
public class PSFontUtils extends org.apache.xmlgraphics.ps.PSFontUtils {

    /** logging instance */
    protected static Log log = LogFactory.getLog(PSFontUtils.class);
    
    /**
     * Generates the PostScript code for the font dictionary.
     * @param gen PostScript generator to use for output
     * @param fontInfo available fonts
     * @return a Map of PSResource instances representing all defined fonts (key: font key)
     * @throws IOException in case of an I/O problem
     */
    public static Map writeFontDict(PSGenerator gen, FontInfo fontInfo) 
                throws IOException {
        return writeFontDict(gen, fontInfo, fontInfo.getFonts());
    }
    
    /**
     * Generates the PostScript code for the font dictionary.
     * @param gen PostScript generator to use for output
     * @param fontInfo available fonts
     * @param fonts the set of fonts to work with
     * @return a Map of PSResource instances representing all defined fonts (key: font key)
     * @throws IOException in case of an I/O problem
     */
    public static Map writeFontDict(PSGenerator gen, FontInfo fontInfo, Map fonts) 
                throws IOException {
        gen.commentln("%FOPBeginFontDict");

        Map fontResources = new java.util.HashMap();
        Iterator iter = fonts.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            Typeface tf = getTypeFace(fontInfo, fonts, key);
            PSResource fontRes = new PSResource("font", tf.getFontName());
            fontResources.put(key, fontRes);
            embedFont(gen, tf, fontRes);
        }
        gen.commentln("%FOPEndFontDict");
        reencodeFonts(gen, fonts);
        return fontResources;
    }

    private static void reencodeFonts(PSGenerator gen, Map fonts) throws IOException {
        gen.commentln("%FOPBeginFontReencode");
        defineWinAnsiEncoding(gen);
        
        //Rewrite font encodings
        Iterator iter = fonts.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            Typeface fm = (Typeface)fonts.get(key);
            if (fm instanceof LazyFont && ((LazyFont)fm).getRealFont() == null) {
                continue;
            } else if (null == fm.getEncoding()) {
                //ignore (ZapfDingbats and Symbol used to run through here, kept for safety reasons)
            } else if ("SymbolEncoding".equals(fm.getEncoding())) {
                //ignore (no encoding redefinition)
            } else if ("ZapfDingbatsEncoding".equals(fm.getEncoding())) {
                //ignore (no encoding redefinition)
            } else if ("WinAnsiEncoding".equals(fm.getEncoding())) {
                redefineFontEncoding(gen, fm.getFontName(), fm.getEncoding());
            } else {
                /* Don't complain anymore, just use the font's default encoding.
                gen.commentln("%WARNING: Only WinAnsiEncoding is supported. Font '" 
                    + fm.getFontName() + "' asks for: " + fm.getEncoding());
                */
            }
        }
        gen.commentln("%FOPEndFontReencode");
    }

    private static Typeface getTypeFace(FontInfo fontInfo, Map fonts, String key) {
        Typeface tf = (Typeface)fonts.get(key);
        if (tf instanceof LazyFont) {
            tf = ((LazyFont)tf).getRealFont();
        }
        if (tf == null) {
            //This is to avoid an NPE if a malconfigured font is in the configuration but not
            //used in the document. If it were used, we wouldn't get this far.
            String fallbackKey = fontInfo.getInternalFontKey(Font.DEFAULT_FONT); 
            tf = (Typeface)fonts.get(fallbackKey);
        }
        return tf;
    }

    /**
     * Embeds a font in the PostScript file.
     * @param gen the PostScript generator
     * @param tf the font
     * @param fontRes the PSResource associated with the font
     * @throws IOException In case of an I/O error
     */
    public static void embedFont(PSGenerator gen, Typeface tf, PSResource fontRes) 
                throws IOException {
        boolean embeddedFont = false;
        if (FontType.TYPE1 == tf.getFontType()) {
            if (tf instanceof CustomFont) {
                CustomFont cf = (CustomFont)tf;
                if (isEmbeddable(cf)) {
                    InputStream in = getInputStreamOnFont(gen, cf);
                    if (in != null) {
                        gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, 
                                fontRes);
                        embedType1Font(gen, in);
                        gen.writeDSCComment(DSCConstants.END_RESOURCE);
                        gen.getResourceTracker().registerSuppliedResource(fontRes);
                        embeddedFont = true;
                    } else {
                        gen.commentln("%WARNING: Could not embed font: " + cf.getFontName());
                        log.warn("Font " + cf.getFontName() + " is marked as supplied in the"
                                + " PostScript file but could not be embedded!");
                    }
                }
            }
        }
        if (!embeddedFont) {
            gen.writeDSCComment(DSCConstants.INCLUDE_RESOURCE, fontRes);
        }
    }

    private static boolean isEmbeddable(CustomFont font) {
        return font.isEmbeddable() 
                && (font.getEmbedFileName() != null || font.getEmbedResourceName() != null);
    }
    
    private static InputStream getInputStreamOnFont(PSGenerator gen, CustomFont font) 
                throws IOException {
        if (isEmbeddable(font)) {
            Source source = font.getEmbedFileSource();
            if (source == null && font.getEmbedResourceName() != null) {
                source = new StreamSource(PSFontUtils.class
                        .getResourceAsStream(font.getEmbedResourceName()));
            }
            if (source == null) {
                return null;
            }
            InputStream in = null;
            if (source instanceof StreamSource) {
                in = ((StreamSource) source).getInputStream();
            }
            if (in == null && source.getSystemId() != null) {
                try {
                    in = new java.net.URL(source.getSystemId()).openStream();
                } catch (MalformedURLException e) {
                    new FileNotFoundException(
                            "File not found. URL could not be resolved: "
                                    + e.getMessage());
                }
            }
            if (in == null) {
                return null;
            }
            //Make sure the InputStream is decorated with a BufferedInputStream
            if (!(in instanceof java.io.BufferedInputStream)) {
                in = new java.io.BufferedInputStream(in);
            }
            return in;
        } else {
            return null;
        }
    }

    /**
     * Determines the set of fonts that will be supplied with the PS file and registers them
     * with the resource tracker. All the fonts that are being processed are returned as a Map.
     * @param resTracker the resource tracker
     * @param fontInfo available fonts
     * @param fonts the set of fonts to work with
     * @return a Map of PSResource instances representing all defined fonts (key: font key)
     */
    public static Map determineSuppliedFonts(ResourceTracker resTracker, 
            FontInfo fontInfo, Map fonts) {
        Map fontResources = new java.util.HashMap();
        Iterator iter = fonts.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            Typeface tf = getTypeFace(fontInfo, fonts, key);
            PSResource fontRes = new PSResource("font", tf.getFontName());
            fontResources.put(key, fontRes);
            if (FontType.TYPE1 == tf.getFontType()) {
                if (tf instanceof CustomFont) {
                    CustomFont cf = (CustomFont)tf;
                    if (isEmbeddable(cf)) {
                        resTracker.registerSuppliedResource(fontRes);
                    }
                }
            }
        }
        return fontResources;
    }

}
