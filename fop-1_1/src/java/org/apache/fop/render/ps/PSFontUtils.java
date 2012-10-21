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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.fonts.Glyphs;
import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;

import org.apache.fop.fonts.Base14Font;
import org.apache.fop.fonts.CIDFontType;
import org.apache.fop.fonts.CIDSet;
import org.apache.fop.fonts.CMapSegment;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteEncoding;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.fop.fonts.truetype.TTFFile.PostScriptVersion;
import org.apache.fop.fonts.truetype.TTFOutputStream;
import org.apache.fop.fonts.truetype.TTFSubSetFile;
import org.apache.fop.render.ps.fonts.PSTTFOutputStream;
import org.apache.fop.util.HexEncoder;

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
        return writeFontDict(gen, fontInfo, null);
    }

    /**
     * Generates the PostScript code for the font dictionary. This method should only be
     * used if no "resource optimization" is performed, i.e. when the fonts are not embedded
     * in a second pass.
     * @param gen PostScript generator to use for output
     * @param fontInfo available fonts
     * @param eventProducer to report events
     * @return a Map of PSResource instances representing all defined fonts (key: font key)
     * @throws IOException in case of an I/O problem
     */
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
     * @param eventProducer the event producer
     * @return a Map of PSResource instances representing all defined fonts (key: font key)
     * @throws IOException in case of an I/O problem
     */
    public static Map writeFontDict(PSGenerator gen, FontInfo fontInfo, Map<String, Typeface> fonts,
            PSEventProducer eventProducer) throws IOException {
        return writeFontDict(gen, fontInfo, fonts, false, eventProducer);
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
            Map<String, Typeface> fonts, boolean encodeAllCharacters, PSEventProducer eventProducer)
            throws IOException {
        gen.commentln("%FOPBeginFontDict");

        Map fontResources = new HashMap();
        for (String key : fonts.keySet()) {
            Typeface tf = getTypeFace(fontInfo, fonts, key);
            PSResource fontRes = new PSResource(PSResource.TYPE_FONT, tf.getEmbedFontName());
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
                    PSResource derivedFontRes;
                    if (tf.getFontType() == FontType.TRUETYPE
                            && sbf.getTrueTypePostScriptVersion() != PostScriptVersion.V2) {
                        derivedFontRes = defineDerivedTrueTypeFont(gen, eventProducer,
                                tf.getEmbedFontName(), tf.getEmbedFontName() + postFix, encoding,
                                sbf.getCMap());
                    } else {
                        derivedFontRes = defineDerivedFont(gen, tf.getEmbedFontName(),
                                tf.getEmbedFontName() + postFix, encoding.getName());
                    }
                    fontResources.put(key + postFix,
                            PSFontResource.createFontResource(derivedFontRes));
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
                    redefineFontEncoding(gen, tf.getEmbedFontName(), tf.getEncodingName());
                } else if (tf instanceof SingleByteFont) {
                    SingleByteFont sbf = (SingleByteFont)tf;
                    if (!sbf.isUsingNativeEncoding()) {
                        //Font has been configured to use an encoding other than the default one
                        redefineFontEncoding(gen, tf.getEmbedFontName(), tf.getEncodingName());
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

    private static PSFontResource embedFont(PSGenerator gen, Typeface tf, PSResource fontRes,
            PSEventProducer eventProducer) throws IOException {
        FontType fontType = tf.getFontType();
        PSFontResource fontResource = null;
        if (!(fontType == FontType.TYPE1 || fontType == FontType.TRUETYPE
                || fontType == FontType.TYPE0) || !(tf instanceof CustomFont)) {
            gen.writeDSCComment(DSCConstants.INCLUDE_RESOURCE, fontRes);
            fontResource = PSFontResource.createFontResource(fontRes);
            return fontResource;
        }
        CustomFont cf = (CustomFont)tf;
        if (isEmbeddable(cf)) {
            InputStream in = getInputStreamOnFont(gen, cf);
            if (in == null) {
                gen.commentln("%WARNING: Could not embed font: " + cf.getEmbedFontName());
                log.warn("Font " + cf.getEmbedFontName() + " is marked as supplied in the"
                        + " PostScript file but could not be embedded!");
                gen.writeDSCComment(DSCConstants.INCLUDE_RESOURCE, fontRes);
                fontResource = PSFontResource.createFontResource(fontRes);
                return fontResource;
            }
            if (fontType == FontType.TYPE0) {
                if (gen.embedIdentityH()) {
                    checkPostScriptLevel3(gen, eventProducer);
                    /*
                     * First CID-keyed font to be embedded; add
                     * %%IncludeResource: comment for ProcSet CIDInit.
                     */
                    gen.includeProcsetCIDInitResource();
                }
                PSResource cidFontResource = embedType2CIDFont(gen,
                        (MultiByteFont) tf, in);
                fontResource = PSFontResource.createFontResource(fontRes,
                        gen.getProcsetCIDInitResource(), gen.getIdentityHCMapResource(),
                        cidFontResource);
            }
            gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, fontRes);
            if (fontType == FontType.TYPE1) {
                embedType1Font(gen, in);
                fontResource = PSFontResource.createFontResource(fontRes);
            } else if (fontType == FontType.TRUETYPE) {
                embedTrueTypeFont(gen, (SingleByteFont) tf, in);
                fontResource = PSFontResource.createFontResource(fontRes);
            } else {
                composeType0Font(gen, (MultiByteFont) tf, in);
            }
            gen.writeDSCComment(DSCConstants.END_RESOURCE);
            gen.getResourceTracker().registerSuppliedResource(fontRes);
        }
        return fontResource;
    }

    private static void checkPostScriptLevel3(PSGenerator gen, PSEventProducer eventProducer) {
        if (gen.getPSLevel() < 3) {
            if (eventProducer != null) {
                eventProducer.postscriptLevel3Needed(gen);
            } else {
                throw new IllegalStateException("PostScript Level 3 is"
                        + " required to use TrueType fonts,"
                        + " configured level is "
                        + gen.getPSLevel());
            }
        }
    }

    private static void embedTrueTypeFont(PSGenerator gen,
            SingleByteFont font, InputStream fontStream) throws IOException {
        /* See Adobe Technical Note #5012, "The Type 42 Font Format Specification" */
        gen.commentln("%!PS-TrueTypeFont-65536-65536-1"); // TODO TrueType & font versions
        gen.writeln("11 dict begin");
        if (font.getEmbeddingMode() == EmbeddingMode.AUTO) {
            font.setEmbeddingMode(EmbeddingMode.SUBSET);
        }
        FontFileReader reader = new FontFileReader(fontStream);
        TTFFile ttfFile = new TTFFile();
        ttfFile.readFont(reader, font.getFullName());
        createType42DictionaryEntries(gen, font, font.getCMap(), ttfFile);
        gen.writeln("FontName currentdict end definefont pop");
    }

    private static void createType42DictionaryEntries(PSGenerator gen, CustomFont font,
            CMapSegment[] cmap, TTFFile ttfFile) throws IOException {
        gen.write("/FontName /");
        gen.write(font.getEmbedFontName());
        gen.writeln(" def");
        gen.writeln("/PaintType 0 def");
        gen.writeln("/FontMatrix [1 0 0 1 0 0] def");
        writeFontBBox(gen, font);
        gen.writeln("/FontType 42 def");
        gen.writeln("/Encoding 256 array");
        gen.writeln("0 1 255{1 index exch/.notdef put}for");
        boolean buildCharStrings;
        Set<String> glyphNames = new HashSet<String>();
        if (font.getFontType() == FontType.TYPE0 && font.getEmbeddingMode() != EmbeddingMode.FULL) {
            //"/Encoding" is required but ignored for CID fonts
            //so we keep it minimal to save space
            buildCharStrings = false;
        } else {
            buildCharStrings = true;
            for (int i = 0; i < Glyphs.WINANSI_ENCODING.length; i++) {
                gen.write("dup ");
                gen.write(i);
                gen.write(" /");
                String glyphName = Glyphs.charToGlyphName(Glyphs.WINANSI_ENCODING[i]);
                if (glyphName.equals("")) {
                    gen.write(Glyphs.NOTDEF);
                } else {
                    gen.write(glyphName);
                    glyphNames.add(glyphName);
                }
                gen.writeln(" put");
            }
        }
        gen.writeln("readonly def");
        TTFOutputStream ttfOut = new PSTTFOutputStream(gen);
        ttfFile.stream(ttfOut);

        buildCharStrings(gen, buildCharStrings, cmap, glyphNames, font);
    }

    private static void buildCharStrings(PSGenerator gen, boolean buildCharStrings,
            CMapSegment[] cmap, Set<String> glyphNames, CustomFont font) throws IOException {
        gen.write("/CharStrings ");
        if (!buildCharStrings) {
            gen.write(1);
        } else if (font.getEmbeddingMode() != EmbeddingMode.FULL) {
            int charCount = 1; //1 for .notdef
            for (CMapSegment segment : cmap) {
                charCount += segment.getUnicodeEnd() - segment.getUnicodeStart() + 1;
            }
            gen.write(charCount);
        } else {
            gen.write(font.getCMap().length);
        }
        gen.writeln(" dict dup begin");
        gen.write("/");
        gen.write(Glyphs.NOTDEF);
        gen.writeln(" 0 def"); // .notdef always has to be at index 0
        if (!buildCharStrings) {
            // If we're not building the full CharStrings we can end here
            gen.writeln("end readonly def");
            return;
        }
        if (font.getEmbeddingMode() != EmbeddingMode.FULL) {
          //Only performed in singly-byte mode, ignored for CID fonts
            for (CMapSegment segment : cmap) {
                int glyphIndex = segment.getGlyphStartIndex();
                for (int ch = segment.getUnicodeStart(); ch <= segment.getUnicodeEnd(); ch++) {
                    char ch16 = (char)ch; //TODO Handle Unicode characters beyond 16bit
                    String glyphName = Glyphs.charToGlyphName(ch16);
                    if ("".equals(glyphName)) {
                        glyphName = "u" + Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
                    }
                    writeGlyphDefs(gen, glyphName, glyphIndex);

                    glyphIndex++;
                }
            }
        } else {
            for (String name : glyphNames) {
                writeGlyphDefs(gen, name,
                        getGlyphIndex(Glyphs.getUnicodeSequenceForGlyphName(name).charAt(0),
                                font.getCMap()));
            }
        }
        gen.writeln("end readonly def");
    }

    private static void writeGlyphDefs(PSGenerator gen, String glyphName, int glyphIndex)
                throws IOException {
        gen.write("/");
        gen.write(glyphName);
        gen.write(" ");
        gen.write(glyphIndex);
        gen.writeln(" def");
    }

    private static int getGlyphIndex(char c, CMapSegment[] cmap) {
        for (CMapSegment segment : cmap) {
            if (segment.getUnicodeStart() <= c && c <= segment.getUnicodeEnd()) {
                return segment.getGlyphStartIndex() + c - segment.getUnicodeStart();
            }
        }
        return 0;
    }

    private static void composeType0Font(PSGenerator gen, MultiByteFont font,
            InputStream fontStream) throws IOException {
        String psName = font.getEmbedFontName();
        gen.write("/");
        gen.write(psName);
        gen.write(" /Identity-H [/");
        gen.write(psName);
        gen.writeln("] composefont pop");
    }

    private static PSResource embedType2CIDFont(PSGenerator gen,
            MultiByteFont font, InputStream fontStream) throws IOException {
        assert font.getCIDType() == CIDFontType.CIDTYPE2;

        String psName = font.getEmbedFontName();
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
        CIDSet cidSet = font.getCIDSet();
        int numberOfGlyphs = cidSet.getNumberOfGlyphs();
        gen.write(numberOfGlyphs);
        gen.writeln(" def");
        gen.writeln("/GDBytes 2 def"); // TODO always 2?
        gen.writeln("/CIDMap [<");
        int colCount = 0;
        int lineCount = 1;
        int nextBitSet = 0;
        int previousBitSet = 0;
        for (int cid = 0; cid < numberOfGlyphs; cid++) {
            if (colCount++ == 20) {
                gen.newLine();
                colCount = 1;
                if (lineCount++ == 800) {
                    gen.writeln("> <");
                    lineCount = 1;
                }
            }
            String gid;
            if (font.getEmbeddingMode() != EmbeddingMode.FULL) {
                gid = HexEncoder.encode(cid, 4);
            } else {
                previousBitSet = nextBitSet;
                nextBitSet = cidSet.getGlyphIndices().nextSetBit(nextBitSet);
                while (previousBitSet++ < nextBitSet) {
                    // if there are gaps in the indices we pad them with zeros
                    gen.write("0000");
                    cid++;
                    if (colCount++ == 20) {
                        gen.newLine();
                        colCount = 1;
                        if (lineCount++ == 800) {
                            gen.writeln("> <");
                            lineCount = 1;
                        }
                    }
                }
                gid = HexEncoder.encode(nextBitSet, 4);
                nextBitSet++;
            }
            gen.write(gid);
        }
        gen.writeln(">] def");
        FontFileReader reader = new FontFileReader(fontStream);

        TTFFile ttfFile;
        if (font.getEmbeddingMode() != EmbeddingMode.FULL) {
            ttfFile = new TTFSubSetFile();
            ttfFile.readFont(reader, font.getTTCName(), font.getUsedGlyphs());
        } else {
            ttfFile = new TTFFile();
            ttfFile.readFont(reader, font.getTTCName());
        }


        createType42DictionaryEntries(gen, font, new CMapSegment[0], ttfFile);
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
            InputStream in = font.getInputStream();
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
            PSResource fontRes = new PSResource("font", tf.getEmbedFontName());
            fontResources.put(key, fontRes);
            FontType fontType = tf.getFontType();
            if (fontType == FontType.TYPE1 || fontType == FontType.TRUETYPE
                    || fontType == FontType.TYPE0) {
                if (tf instanceof CustomFont) {
                    CustomFont cf = (CustomFont)tf;
                    if (isEmbeddable(cf)) {
                        if (fontType == FontType.TYPE0) {
                            resTracker.registerSuppliedResource(
                                    new PSResource(PSResource.TYPE_CIDFONT, tf.getEmbedFontName()));
                            resTracker.registerSuppliedResource(
                                    new PSResource(PSResource.TYPE_CMAP, "Identity-H"));
                        }
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
                                    PSResource.TYPE_FONT, tf.getEmbedFontName() + "_" + (i + 1));
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

    private static PSResource defineDerivedTrueTypeFont(PSGenerator gen,
            PSEventProducer eventProducer, String baseFontName, String fontName,
            SingleByteEncoding encoding, CMapSegment[] cmap) throws IOException {
        checkPostScriptLevel3(gen, eventProducer);
        PSResource res = new PSResource(PSResource.TYPE_FONT, fontName);
        gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, res);
        gen.commentln("%XGCDependencies: font " + baseFontName);
        gen.commentln("%XGC+ encoding " + encoding.getName());
        gen.writeln("/" + baseFontName + " findfont");
        gen.writeln("dup length dict begin");
        gen.writeln("  {1 index /FID ne {def} {pop pop} ifelse} forall");
        gen.writeln("  /Encoding " + encoding.getName() + " def");

        gen.writeln("  /CharStrings 256 dict dup begin");
        String[] charNameMap = encoding.getCharNameMap();
        char[] unicodeCharMap = encoding.getUnicodeCharMap();
        assert charNameMap.length == unicodeCharMap.length;
        for (int i = 0; i < charNameMap.length; i++) {
            String glyphName = charNameMap[i];
            gen.write("    /");
            gen.write(glyphName);
            gen.write(" ");
            if (glyphName.equals(".notdef")) {
                gen.write(0);
            } else {
                gen.write(getGlyphIndex(unicodeCharMap[i], cmap));
            }
            gen.writeln(" def");
        }
        gen.writeln("  end readonly def");

        gen.writeln("  currentdict");
        gen.writeln("end");
        gen.writeln("/" + fontName + " exch definefont pop");
        gen.writeDSCComment(DSCConstants.END_RESOURCE);
        gen.getResourceTracker().registerSuppliedResource(res);
        return res;
    }
}
