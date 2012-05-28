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
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.fonts.Glyphs;
import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;

import org.apache.fop.fonts.Base14Font;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.SingleByteEncoding;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;

/**
 * Utility code for font handling in PostScript.
 */
public class PSFontUtils extends org.apache.xmlgraphics.ps.PSFontUtils {

    /** logging instance */
    protected static final Log log = LogFactory.getLog(PSFontUtils.class);

    /**
     * Generates the PostScript code for the font dictionary. This method should only be
     * used if no "resource optimization" is performed, i.e. when the fonts are not embedded
     * in a second pass.
     * @param gen PostScript generator to use for output
     * @param fontInfo available fonts
     * @return a Map of PSResource instances representing all defined fonts (key: font key)
     * @throws IOException in case of an I/O problem
     */
    public static Map writeFontDict(PSGenerator gen, FontInfo fontInfo)
                throws IOException {
        return writeFontDict(gen, fontInfo, fontInfo.getFonts(), true);
    }

    /**
     * Generates the PostScript code for the font dictionary. This method assumes all used
     * fonts and characters are known, i.e. when PostScript is generated with resource
     * optimization turned on.
     * @param gen PostScript generator to use for output
     * @param fontInfo available fonts
     * @param fonts the set of fonts to work with
     * @return a Map of PSResource instances representing all defined fonts (key: font key)
     * @throws IOException in case of an I/O problem
     */
    public static Map writeFontDict(PSGenerator gen, FontInfo fontInfo,
            Map<String, Typeface> fonts)
                throws IOException {
        return writeFontDict(gen, fontInfo, fonts, false);
    }

    /**
     * Generates the PostScript code for the font dictionary.
     * @param gen PostScript generator to use for output
     * @param fontInfo available fonts
     * @param fonts the set of fonts to work with
     * @param encodeAllCharacters true if all characters shall be encoded using additional,
     *           generated encodings.
     * @return a Map of PSResource instances representing all defined fonts (key: font key)
     * @throws IOException in case of an I/O problem
     */
    private static Map writeFontDict(PSGenerator gen, FontInfo fontInfo,
            Map<String, Typeface> fonts, boolean encodeAllCharacters) throws IOException {
        gen.commentln("%FOPBeginFontDict");

        Map fontResources = new java.util.HashMap();
        for (String key : fonts.keySet()) {
            Typeface tf = getTypeFace(fontInfo, fonts, key);
            PSResource fontRes = new PSResource(PSResource.TYPE_FONT, tf.getFontName());
            fontResources.put(key, fontRes);
            embedFont(gen, tf, fontRes);

            if (tf instanceof SingleByteFont) {
                SingleByteFont sbf = (SingleByteFont)tf;

                if (encodeAllCharacters) {
                    sbf.encodeAllUnencodedCharacters();
                }

                for (int i = 0, c = sbf.getAdditionalEncodingCount(); i < c; i++) {
                    SingleByteEncoding encoding = sbf.getAdditionalEncoding(i);
                    defineEncoding(gen, encoding);
                    String postFix = "_" + (i + 1);
                    PSResource derivedFontRes = defineDerivedFont(gen, tf.getFontName(),
                            tf.getFontName() + postFix, encoding.getName());
                    fontResources.put(key + postFix, derivedFontRes);
                }
            }
        }
        gen.commentln("%FOPEndFontDict");
        reencodeFonts(gen, fonts);
        return fontResources;
    }

    private static void reencodeFonts(PSGenerator gen, Map<String, Typeface> fonts)
            throws IOException {
        ResourceTracker tracker = gen.getResourceTracker();

        if (!tracker.isResourceSupplied(WINANSI_ENCODING_RESOURCE)) {
            //Only out Base 14 fonts still use that
            defineWinAnsiEncoding(gen);
        }
        gen.commentln("%FOPBeginFontReencode");

        //Rewrite font encodings
        for (String key : fonts.keySet()) {
            Typeface tf = fonts.get(key);
            if (tf instanceof LazyFont) {
                tf = ((LazyFont)tf).getRealFont();
                if (tf == null) {
                    continue;
                }
            }
            if (null == tf.getEncodingName()) {
                //ignore (ZapfDingbats and Symbol used to run through here, kept for safety reasons)
            } else if ("SymbolEncoding".equals(tf.getEncodingName())) {
                //ignore (no encoding redefinition)
            } else if ("ZapfDingbatsEncoding".equals(tf.getEncodingName())) {
                //ignore (no encoding redefinition)
            } else {
                if (tf instanceof Base14Font) {
                    //Our Base 14 fonts don't use the default encoding
                    redefineFontEncoding(gen, tf.getFontName(), tf.getEncodingName());
                } else if (tf instanceof SingleByteFont) {
                    SingleByteFont sbf = (SingleByteFont)tf;
                    if (!sbf.isUsingNativeEncoding()) {
                        //Font has been configured to use an encoding other than the default one
                        redefineFontEncoding(gen, tf.getFontName(), tf.getEncodingName());
                    }
                }
            }
        }
        gen.commentln("%FOPEndFontReencode");
    }

    private static Typeface getTypeFace(FontInfo fontInfo, Map<String, Typeface> fonts,
            String key) {
        Typeface tf = fonts.get(key);
        if (tf instanceof LazyFont) {
            tf = ((LazyFont)tf).getRealFont();
        }
        if (tf == null) {
            //This is to avoid an NPE if a malconfigured font is in the configuration but not
            //used in the document. If it were used, we wouldn't get this far.
            String fallbackKey = fontInfo.getInternalFontKey(Font.DEFAULT_FONT);
            tf = fonts.get(fallbackKey);
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
        return font.isEmbeddable();
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
            FontInfo fontInfo, Map<String, Typeface> fonts) {
        Map fontResources = new java.util.HashMap();
        for (String key : fonts.keySet()) {
            Typeface tf = getTypeFace(fontInfo, fonts, key);
            PSResource fontRes = new PSResource("font", tf.getFontName());
            fontResources.put(key, fontRes);
            if (FontType.TYPE1 == tf.getFontType()) {
                if (tf instanceof CustomFont) {
                    CustomFont cf = (CustomFont)tf;
                    if (isEmbeddable(cf)) {
                        resTracker.registerSuppliedResource(fontRes);
                    }
                    if (tf instanceof SingleByteFont) {
                        SingleByteFont sbf = (SingleByteFont)tf;
                        for (int i = 0, c = sbf.getAdditionalEncodingCount(); i < c; i++) {
                            SingleByteEncoding encoding = sbf.getAdditionalEncoding(i);
                            PSResource encodingRes = new PSResource(
                                    PSResource.TYPE_ENCODING, encoding.getName());
                            resTracker.registerSuppliedResource(encodingRes);
                            PSResource derivedFontRes = new PSResource(
                                    PSResource.TYPE_FONT, tf.getFontName() + "_" + (i + 1));
                            resTracker.registerSuppliedResource(derivedFontRes);
                        }
                    }
                }
            }
        }
        return fontResources;
    }

    /**
     * Defines the single-byte encoding for use in PostScript files.
     * @param gen the PostScript generator
     * @param encoding the single-byte encoding
     * @return the PSResource instance that represents the encoding
     * @throws IOException In case of an I/O problem
     */
    public static PSResource defineEncoding(PSGenerator gen, SingleByteEncoding encoding)
            throws IOException {
        PSResource res = new PSResource(PSResource.TYPE_ENCODING, encoding.getName());
        gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, res);
        gen.writeln("/" + encoding.getName() + " [");
        String[] charNames = encoding.getCharNameMap();
        for (int i = 0; i < 256; i++) {
            if (i > 0) {
                if ((i % 5) == 0) {
                    gen.newLine();
                } else {
                    gen.write(" ");
                }
            }
            String glyphname = null;
            if (i < charNames.length) {
                glyphname = charNames[i];
            }
            if (glyphname == null || "".equals(glyphname)) {
                glyphname = Glyphs.NOTDEF;
            }
            gen.write("/");
            gen.write(glyphname);
        }
        gen.newLine();
        gen.writeln("] def");
        gen.writeDSCComment(DSCConstants.END_RESOURCE);
        gen.getResourceTracker().registerSuppliedResource(res);
        return res;
    }

    /**
     * Derives a new font based on an existing font with a given encoding. The encoding must
     * have been registered before.
     * @param gen the PostScript generator
     * @param baseFontName the font name of the font to derive from
     * @param fontName the font name of the new font to be define
     * @param encoding the new encoding (must be predefined in the PS file)
     * @return the PSResource representing the derived font
     * @throws IOException In case of an I/O problem
     */
    public static PSResource defineDerivedFont
        (PSGenerator gen, String baseFontName, String fontName, String encoding)
        throws IOException {
        PSResource res = new PSResource(PSResource.TYPE_FONT, fontName);
        gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, res);
        gen.commentln("%XGCDependencies: font " + baseFontName);
        gen.commentln("%XGC+ encoding " + encoding);
        gen.writeln("/" + baseFontName + " findfont");
        gen.writeln("dup length dict begin");
        gen.writeln("  {1 index /FID ne {def} {pop pop} ifelse} forall");
        gen.writeln("  /Encoding " + encoding + " def");
        gen.writeln("  currentdict");
        gen.writeln("end");
        gen.writeln("/" + fontName + " exch definefont pop");
        gen.writeDSCComment(DSCConstants.END_RESOURCE);
        gen.getResourceTracker().registerSuppliedResource(res);
        return res;
    }

}
