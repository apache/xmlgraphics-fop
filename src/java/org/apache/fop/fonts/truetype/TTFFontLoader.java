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

package org.apache.fop.fonts.truetype;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.fop.fonts.BFEntry;
import org.apache.fop.fonts.CIDFontType;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.NamedCharacter;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.xmlgraphics.fonts.Glyphs;

/**
 * Loads a TrueType font into memory directly from the original font file.
 */
public class TTFFontLoader extends FontLoader {

    private MultiByteFont multiFont;
    private SingleByteFont singleFont;
    private final String subFontName;
    private EncodingMode encodingMode;

    /**
     * Default constructor
     * @param fontFileURI the URI representing the font file
     * @param resolver the FontResolver for font URI resolution
     */
    public TTFFontLoader(String fontFileURI, FontResolver resolver) {
        this(fontFileURI, null, true, EncodingMode.AUTO, true, true, resolver);
    }

    /**
     * Additional constructor for TrueType Collections.
     * @param fontFileURI the URI representing the font file
     * @param subFontName the sub-fontname of a font in a TrueType Collection (or null for normal
     *          TrueType fonts)
     * @param embedded indicates whether the font is embedded or referenced
     * @param encodingMode the requested encoding mode
     * @param useKerning true to enable loading kerning info if available, false to disable
     * @param useAdvanced true to enable loading advanced info if available, false to disable
     * @param resolver the FontResolver for font URI resolution
     */
    public TTFFontLoader(String fontFileURI, String subFontName,
                boolean embedded, EncodingMode encodingMode, boolean useKerning,
                boolean useAdvanced, FontResolver resolver) {
        super(fontFileURI, embedded, useKerning, useAdvanced, resolver);
        this.subFontName = subFontName;
        this.encodingMode = encodingMode;
        if (this.encodingMode == EncodingMode.AUTO) {
            this.encodingMode = EncodingMode.CID; //Default to CID mode for TrueType
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void read() throws IOException {
        read(this.subFontName);
    }

    /**
     * Reads a TrueType font.
     * @param ttcFontName the TrueType sub-font name of TrueType Collection (may be null for
     *    normal TrueType fonts)
     * @throws IOException if an I/O error occurs
     */
    private void read(String ttcFontName) throws IOException {
        InputStream in = openFontUri(resolver, this.fontFileURI);
        try {
            TTFFile ttf = new TTFFile(useKerning, useAdvanced);
            FontFileReader reader = new FontFileReader(in);
            boolean supported = ttf.readFont(reader, ttcFontName);
            if (!supported) {
                throw new IOException("TrueType font is not supported: " + fontFileURI);
            }
            buildFont(ttf, ttcFontName);
            loaded = true;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }


    private void buildFont(TTFFile ttf, String ttcFontName) {
        if (ttf.isCFF()) {
            throw new UnsupportedOperationException(
                    "OpenType fonts with CFF data are not supported, yet");
        }

        boolean isCid = this.embedded;
        if (this.encodingMode == EncodingMode.SINGLE_BYTE) {
            isCid = false;
        }

        if (isCid) {
            multiFont = new MultiByteFont();
            returnFont = multiFont;
            multiFont.setTTCName(ttcFontName);
        } else {
            singleFont = new SingleByteFont();
            returnFont = singleFont;
        }
        returnFont.setResolver(resolver);

        returnFont.setFontName(ttf.getPostScriptName());
        returnFont.setFullName(ttf.getFullName());
        returnFont.setFamilyNames(ttf.getFamilyNames());
        returnFont.setFontSubFamilyName(ttf.getSubFamilyName());
        returnFont.setCapHeight(ttf.getCapHeight());
        returnFont.setXHeight(ttf.getXHeight());
        returnFont.setAscender(ttf.getLowerCaseAscent());
        returnFont.setDescender(ttf.getLowerCaseDescent());
        returnFont.setFontBBox(ttf.getFontBBox());
        returnFont.setFlags(ttf.getFlags());
        returnFont.setStemV(Integer.parseInt(ttf.getStemV())); //not used for TTF
        returnFont.setItalicAngle(Integer.parseInt(ttf.getItalicAngle()));
        returnFont.setMissingWidth(0);
        returnFont.setWeight(ttf.getWeightClass());

        if (isCid) {
            multiFont.setCIDType(CIDFontType.CIDTYPE2);
            int[] wx = ttf.getWidths();
            multiFont.setWidthArray(wx);
            List entries = ttf.getCMaps();
            BFEntry[] bfentries = new BFEntry[entries.size()];
            int pos = 0;
            Iterator iter = ttf.getCMaps().listIterator();
            while (iter.hasNext()) {
                TTFCmapEntry ce = (TTFCmapEntry)iter.next();
                bfentries[pos] = new BFEntry(ce.getUnicodeStart(), ce.getUnicodeEnd(),
                        ce.getGlyphStartIndex());
                pos++;
            }
            multiFont.setBFEntries(bfentries);
        } else {
            singleFont.setFontType(FontType.TRUETYPE);
            singleFont.setEncoding(ttf.getCharSetName());
            returnFont.setFirstChar(ttf.getFirstChar());
            returnFont.setLastChar(ttf.getLastChar());
            copyWidthsSingleByte(ttf);
        }

        if (useKerning) {
            copyKerning(ttf, isCid);
        }
        if (useAdvanced) {
            copyAdvanced(ttf);
        }
        if (this.embedded && ttf.isEmbeddable()) {
            returnFont.setEmbedFileName(this.fontFileURI);
        }
    }

    private void copyWidthsSingleByte(TTFFile ttf) {
        int[] wx = ttf.getWidths();
        for (int i = singleFont.getFirstChar(); i <= singleFont.getLastChar(); i++) {
            singleFont.setWidth(i, ttf.getCharWidth(i));
        }
        Iterator iter = ttf.getCMaps().listIterator();
        while (iter.hasNext()) {
            TTFCmapEntry ce = (TTFCmapEntry)iter.next();
            if (ce.getUnicodeStart() < 0xFFFE) {
                for (char u = (char)ce.getUnicodeStart(); u <= ce.getUnicodeEnd(); u++) {
                    int codePoint = singleFont.getEncoding().mapChar(u);
                    if (codePoint <= 0) {
                        String unicode = Character.toString(u);
                        String charName = Glyphs.stringToGlyph(unicode);
                        if (charName.length() > 0) {
                            NamedCharacter nc = new NamedCharacter(charName, unicode);
                            int glyphIndex = ce.getGlyphStartIndex() + u - ce.getUnicodeStart();
                            singleFont.addUnencodedCharacter(nc, wx[glyphIndex]);
                        }
                    }
                }
            }
        }
    }

    /**
     * Copy kerning information.
     */
    private void copyKerning(TTFFile ttf, boolean isCid) {

        // Get kerning
        Set<Integer> kerningSet;
        if (isCid) {
            kerningSet = ttf.getKerning().keySet();
        } else {
            kerningSet = ttf.getAnsiKerning().keySet();
        }

        for (Integer kpx1 : kerningSet) {

            Map<Integer, Integer> h2;
            if (isCid) {
                h2 = ttf.getKerning().get(kpx1);
            } else {
                h2 = ttf.getAnsiKerning().get(kpx1);
            }
            returnFont.putKerningEntry(kpx1, h2);
        }
    }

    /**
     * Copy advanced typographic information.
     */
    private void copyAdvanced ( TTFFile ttf ) {
        if ( returnFont instanceof MultiByteFont ) {
            MultiByteFont mbf = (MultiByteFont) returnFont;
            mbf.setGDEF ( ttf.getGDEF() );
            mbf.setGSUB ( ttf.getGSUB() );
            mbf.setGPOS ( ttf.getGPOS() );
        }
    }

}
