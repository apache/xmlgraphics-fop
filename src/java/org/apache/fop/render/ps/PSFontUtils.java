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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.fonts.Glyphs;
import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;
import org.apache.xmlgraphics.util.io.ASCIIHexOutputStream;

import org.apache.fop.fonts.Base14Font;
import org.apache.fop.fonts.CIDSubset;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteEncoding;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.TTFCmapEntry;

/**
 * Utility code for font handling in PostScript.
 */
public class PSFontUtils extends org.apache.xmlgraphics.ps.PSFontUtils {

    /** logging instance */
    protected static Log log = LogFactory.getLog(PSFontUtils.class);

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
        return writeFontDict(gen, fontInfo, (PSEventProducer) null);
    }

    public static Map writeFontDict(PSGenerator gen, FontInfo fontInfo,
            PSEventProducer eventProducer) throws IOException {
        return writeFontDict(gen, fontInfo, fontInfo.getFonts(), true, eventProducer);
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
    public static Map writeFontDict(PSGenerator gen, FontInfo fontInfo, Map fonts)
                throws IOException {
        return writeFontDict(gen, fontInfo, fonts, false, null);
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
    private static Map writeFontDict(PSGenerator gen, FontInfo fontInfo, Map fonts,
            boolean encodeAllCharacters, PSEventProducer eventProducer) throws IOException {
        gen.commentln("%FOPBeginFontDict");

        Map fontResources = new java.util.HashMap();
        Iterator iter = fonts.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            Typeface tf = getTypeFace(fontInfo, fonts, key);
            PSResource fontRes = new PSResource(PSResource.TYPE_FONT, tf.getFontName());
            PSFontResource fontResource = embedFont(gen, tf, fontRes, eventProducer);
            fontResources.put(key, fontResource);

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
                    fontResources.put(key + postFix,
                            PSFontResource.createFontResource(derivedFontRes));
                }
            }
        }
        gen.commentln("%FOPEndFontDict");
        reencodeFonts(gen, fonts);
        return fontResources;
    }

    private static void reencodeFonts(PSGenerator gen, Map fonts) throws IOException {
        ResourceTracker tracker = gen.getResourceTracker();

        if (!tracker.isResourceSupplied(WINANSI_ENCODING_RESOURCE)) {
            //Only out Base 14 fonts still use that
            defineWinAnsiEncoding(gen);
        }
        gen.commentln("%FOPBeginFontReencode");

        //Rewrite font encodings
        Iterator iter = fonts.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            Typeface tf = (Typeface)fonts.get(key);
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

    private static PSFontResource embedFont(PSGenerator gen, Typeface tf, PSResource fontRes,
            PSEventProducer eventProducer) throws IOException {
        boolean embeddedFont = false;
        FontType fontType = tf.getFontType();
        PSFontResource fontResource = null;
        if (fontType == FontType.TYPE1 || fontType == FontType.TRUETYPE
                || fontType == FontType.TYPE0) {
            if (tf instanceof CustomFont) {
                CustomFont cf = (CustomFont)tf;
                if (isEmbeddable(cf)) {
                    InputStream in = getInputStreamOnFont(gen, cf);
                    if (in != null) {
                        if (fontType == FontType.TYPE0) {
                            if (gen.embedIdentityH()) {
                                /*
                                 * First CID-keyed font to be embedded; add
                                 * %%IncludeResource: comment for ProcSet CIDInit.
                                 */
                                gen.includeProcsetCIDInitResource();
                                if (eventProducer != null) {
                                    eventProducer.postscriptLevel3Used(gen);
                                }
                            }
                            PSResource cidFontResource = embedCIDFont(gen, (MultiByteFont) tf, in);
                            fontResource = PSFontResource.createFontResource(fontRes,
                                    gen.getProcsetCIDInitResource(),
                                    gen.getIdentityHCMapResource(),
                                    cidFontResource);
                        }
                        gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE,
                                fontRes);
                        if (fontType == FontType.TYPE1) {
                            embedType1Font(gen, in);
                            fontResource = PSFontResource.createFontResource(fontRes);
                        } else if (fontType == FontType.TRUETYPE) {
                            embedTrueTypeFont(gen, (SingleByteFont) tf, in);
                            fontResource = PSFontResource.createFontResource(fontRes);
                        } else {
                            embedType0Font(gen, (MultiByteFont) tf, in);
                        }
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
            fontResource = PSFontResource.createFontResource(fontRes);
        }
        return fontResource;
    }

    private static void embedTrueTypeFont(PSGenerator gen,
            SingleByteFont font, InputStream fontStream) throws IOException {
        /* See Adobe Technical Note #5012, "The Type 42 Font Format Specification" */
        gen.commentln("%!PS-TrueTypeFont-65536-65536-1"); // TODO TrueType & font versions
        gen.writeln("11 dict begin");
        createType42DictionaryEntries(gen, font, fontStream, font.getCMaps());
        gen.writeln("FontName currentdict end definefont pop");
    }

    private static void createType42DictionaryEntries(PSGenerator gen, CustomFont font,
            InputStream fontStream, List cmaps) throws IOException {
        gen.write("/FontName /");
        gen.write(font.getFontName());
        gen.writeln(" def");
        gen.writeln("/PaintType 0 def");
        gen.writeln("/FontMatrix [1 0 0 1 0 0] def");
        writeFontBBox(gen, font);
        gen.writeln("/FontType 42 def");
        gen.writeln("/Encoding 256 array");
        gen.writeln("0 1 255{1 index exch/.notdef put}for");
        Set glyphs = new HashSet();
        for (int i = 0; i < Glyphs.WINANSI_ENCODING.length; i++) {
            gen.write("dup ");
            gen.write(i);
            gen.write(" /");
            String glyphName = Glyphs.charToGlyphName(Glyphs.WINANSI_ENCODING[i]);
            if (glyphName.equals("")) {
                gen.write(Glyphs.NOTDEF);
            } else {
                glyphs.add(glyphName);
                gen.write(glyphName);
            }
            gen.writeln(" put");
        }
        gen.writeln("readonly def");
        gen.write("/sfnts[");
        /*
         * Store the font file in an array of hex-encoded strings. Strings are limited to
         * 65535 characters, string will start with a newline, 2 characters are needed to
         * hex-encode each byte, one newline character will be added every 40 bytes, each
         * string should start at a 4-byte boundary
         * => buffer size = floor((65535 - 1) * 40 / 81 / 4) * 4
         * TODO this is not robust: depends on how often ASCIIHexOutputStream adds a newline
         */
        // TODO does not follow Technical Note #5012's requirements:
        // "strings must begin at TrueType table boundaries, or at individual glyph
        // boundaries within the glyf table."
        // There may be compatibility issues with older PostScript interpreters
        byte[] buffer = new byte[32360];
        int readCount;
        while ((readCount = fontStream.read(buffer)) > 0) {
            ASCIIHexOutputStream hexOut = new ASCIIHexOutputStream(gen.getOutputStream());
            gen.writeln("<");
            hexOut.write(buffer, 0, readCount);
            gen.write("> ");
        }
        gen.writeln("]def");
        gen.write("/CharStrings ");
        gen.write(glyphs.size() + 1);
        gen.writeln(" dict dup begin");
        gen.write("/");
        gen.write(Glyphs.NOTDEF);
        gen.writeln(" 0 def"); // TODO always glyph index 0?
        // TODO ugly and temporary, until CID is implemented
        for (Iterator iter = glyphs.iterator(); iter.hasNext();) {
            String glyphName = (String) iter.next();
            gen.write("/");
            gen.write(glyphName);
            gen.write(" ");
            gen.write(getGlyphIndex(glyphName, cmaps));
            gen.writeln(" def");
        }
        gen.writeln("end readonly def");
    }

    private static int getGlyphIndex(String glyphName, List cmaps) {
        char c = Glyphs.getUnicodeSequenceForGlyphName(glyphName).charAt(0);
        for (Iterator iter = cmaps.iterator(); iter.hasNext();) {
            TTFCmapEntry cmap = (TTFCmapEntry) iter.next();
            if (cmap.getUnicodeStart() <= c && c <= cmap.getUnicodeEnd()) {
                return cmap.getGlyphStartIndex() + c - cmap.getUnicodeStart();
            }
        }
        return 0;
    }

    private static void embedType0Font(PSGenerator gen, MultiByteFont font, InputStream fontStream)
            throws IOException {
        String psName = font.getFontName();
        gen.write("/");
        gen.write(psName);
        gen.write(" /Identity-H [/");
        gen.write(psName);
        gen.writeln("] composefont pop");
    }

    private static PSResource embedCIDFont(PSGenerator gen,
            MultiByteFont font, InputStream fontStream) throws IOException {
        String psName = font.getFontName();
        gen.write("%%BeginResource: CIDFont ");
        gen.writeln(psName);

        gen.write("%%Title: (");
        gen.write(psName);
        gen.writeln(" Adobe Identity 0)");

        gen.writeln("%%Version: 1"); // TODO use font revision?
        gen.writeln("/CIDInit /ProcSet findresource begin");
        gen.writeln("20 dict begin");

        gen.write("/CIDFontName /");
        gen.write(psName);
        gen.writeln(" def");

        gen.writeln("/CIDFontVersion 1 def"); // TODO same as %%Version above

        gen.write("/CIDFontType ");
        gen.write(font.getCIDType().getValue());
        gen.writeln(" def");

        gen.writeln("/CIDSystemInfo 3 dict dup begin");
        gen.writeln("  /Registry (Adobe) def");
        gen.writeln("  /Ordering (Identity) def");
        gen.writeln("  /Supplement 0 def");
        gen.writeln("end def");

        // TODO UIDBase (and UIDOffset in CMap) necessary if PostScript Level 1 & 2
        // interpreters are to be supported
        // (Level 1: with composite font extensions; Level 2: those that do not offer
        // native mode support for CID-keyed fonts)

        // TODO XUID (optional but strongly recommended)

        // TODO /FontInfo

        gen.write("/CIDCount ");
        CIDSubset cidSubset = font.getCIDSubset();
        int subsetSize = cidSubset.getSubsetSize();
        gen.write(subsetSize);
        gen.writeln(" def");
        gen.writeln("/GDBytes 2 def"); // TODO always 2?
        gen.writeln("/CIDMap [<");
        int colCount = 0;
        int lineCount = 1;
        for (int cid = 0; cid < subsetSize; cid++) {
            if (colCount++ == 20) {
                gen.newLine();
                colCount = 1;
                if (lineCount++ == 800) {
                    gen.writeln("> <");
                    lineCount = 1;
                }
            }
            String gid = HexEncoder.encode(cidSubset.getGlyphIndexForSubsetIndex(cid), 4);
            gen.write(gid);
        }
        gen.writeln(">] def");
        createType42DictionaryEntries(gen, font, fontStream, Collections.EMPTY_LIST);
        gen.writeln("CIDFontName currentdict end /CIDFont defineresource pop");
        gen.writeln("end");
        gen.writeln("%%EndResource");
        PSResource cidFontResource = new PSResource(PSResource.TYPE_CIDFONT, psName);
        gen.getResourceTracker().registerSuppliedResource(cidFontResource);
        return cidFontResource;
    }

    private static void writeFontBBox(PSGenerator gen, CustomFont font) throws IOException {
        int[] bbox = font.getFontBBox();
        gen.write("/FontBBox[");
        for (int i = 0; i < 4; i++) {
            gen.write(" ");
            gen.write(bbox[i]);
        }
        gen.writeln(" ] def");
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
    public static PSResource defineDerivedFont(PSGenerator gen, String baseFontName, String fontName,
            String encoding) throws IOException {
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
