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

import org.apache.commons.io.IOUtils;

import org.apache.fop.fonts.BFEntry;
import org.apache.fop.fonts.CIDFontType;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.MultiByteFont;

/**
 * Loads a font into memory directly from the original font file.
 */
public class TTFFontLoader extends FontLoader {

    private MultiByteFont multiFont;
    
    /**
     * Default constructor
     * @param fontFileURI the URI representing the font file
     * @param resolver the FontResolver for font URI resolution
     */
    public TTFFontLoader(String fontFileURI, FontResolver resolver) {
        super(fontFileURI, resolver);
    }
    
    /** {@inheritDoc} */
    protected void read() throws IOException {
        InputStream in = openFontUri(resolver, this.fontFileURI);
        try {
            TTFFile ttf = new TTFFile();
            FontFileReader reader = new FontFileReader(in);
            boolean supported = ttf.readFont(reader, null);
            if (!supported) {
                throw new IOException("TrueType font is not supported: " + fontFileURI);
            }
            buildFont(ttf);
            loaded = true;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private void buildFont(TTFFile ttf) {
        if (ttf.isCFF()) {
            throw new UnsupportedOperationException(
                    "OpenType fonts with CFF data are not supported, yet");
        }
        multiFont = new MultiByteFont();
        multiFont.setResolver(this.resolver);
        returnFont = multiFont;

        returnFont.setFontName(ttf.getPostScriptName());
        returnFont.setFullName(ttf.getFullName());
        returnFont.setFamilyNames(ttf.getFamilyNames());
        returnFont.setFontSubFamilyName(ttf.getSubFamilyName());
        //multiFont.setTTCName(ttcName)
        returnFont.setCapHeight(ttf.getCapHeight());
        returnFont.setXHeight(ttf.getXHeight());
        returnFont.setAscender(ttf.getLowerCaseAscent());
        returnFont.setDescender(ttf.getLowerCaseDescent());
        returnFont.setFontBBox(ttf.getFontBBox());
        //returnFont.setFirstChar(ttf.getFirstChar();)
        returnFont.setFlags(ttf.getFlags());
        returnFont.setStemV(Integer.parseInt(ttf.getStemV())); //not used for TTF
        returnFont.setItalicAngle(Integer.parseInt(ttf.getItalicAngle()));
        returnFont.setMissingWidth(0);
        returnFont.setWeight(ttf.getWeightClass());
        
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
        copyKerning(ttf, true);
        multiFont.setEmbedFileName(this.fontFileURI);
    }
    
    /**
     * Copy kerning information.
     */
    private void copyKerning(TTFFile ttf, boolean isCid) {
        
        // Get kerning
        Iterator iter;
        if (isCid) {
            iter = ttf.getKerning().keySet().iterator();
        } else {
            iter = ttf.getAnsiKerning().keySet().iterator();
        }

        while (iter.hasNext()) {
            Integer kpx1 = (Integer)iter.next();

            Map h2;
            if (isCid) {
                h2 = (Map)ttf.getKerning().get(kpx1);
            } else {
                h2 = (Map)ttf.getAnsiKerning().get(kpx1);
            }
            returnFont.putKerningEntry(kpx1, h2);
        }
    }    
}
