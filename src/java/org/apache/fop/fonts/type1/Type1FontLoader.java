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

package org.apache.fop.fonts.type1;

import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import org.apache.fop.fonts.CodePointMapping;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.SingleByteFont;

/**
 * Loads a Type 1 font into memory directly from the original font file.
 */
public class Type1FontLoader extends FontLoader {

    private SingleByteFont singleFont;
    
    /**
     * Constructs a new Type 1 font loader.
     * @param fontFileURI the URI to the PFB file of a Type 1 font
     * @param resolver the font resolver used to resolve URIs
     * @throws IOException In case of an I/O error
     */
    public Type1FontLoader(String fontFileURI, FontResolver resolver) 
                throws IOException {
        super(fontFileURI, resolver);
    }

    private String getPFMURI(String pfbURI) {
        String pfbExt = pfbURI.substring(pfbURI.length() - 3, pfbURI.length());
        String pfmExt = pfbExt.substring(0, 2)
                + (Character.isUpperCase(pfbExt.charAt(2)) ? "M" : "m");
        return pfbURI.substring(0, pfbURI.length() - 4) + "." + pfmExt;
    }
    
    private static final String[] AFM_EXTENSIONS = new String[] {".AFM", ".afm", ".Afm"};
    
    /** {@inheritDoc} */
    protected void read() throws IOException {
        AFMFile afm = null;
        PFMFile pfm = null;
        
        InputStream afmIn = null;
        for (int i = 0; i < AFM_EXTENSIONS.length; i++) {
            try {
                String afmUri = this.fontFileURI.substring(0, this.fontFileURI.length() - 4)
                        + AFM_EXTENSIONS[i];
                afmIn = openFontUri(resolver, afmUri);
                if (afmIn != null) {
                    break;
                }
            } catch (IOException ioe) {
                //Ignore, AFM probably not available under the URI
            }
        }
        if (afmIn != null) {
            try {
                AFMParser afmParser = new AFMParser();
                afm = afmParser.parse(afmIn);
            } finally {
                IOUtils.closeQuietly(afmIn);
            }
        }
        
        String pfmUri = getPFMURI(this.fontFileURI);
        InputStream pfmIn = null;
        try {
            pfmIn = openFontUri(resolver, pfmUri);
        } catch (IOException ioe) {
            //Ignore, PFM probably not available under the URI
        }
        if (pfmIn != null) {
            try {
                pfm = new PFMFile();
                pfm.load(pfmIn);
            } finally {
                IOUtils.closeQuietly(pfmIn);
            }
        }
        
        if (afm == null && pfm == null) {
            throw new java.io.FileNotFoundException(
                    "Neither an AFM nor a PFM file was found for " + this.fontFileURI);
        }
        buildFont(afm, pfm);
        this.loaded = true;
    }

    private void buildFont(AFMFile afm, PFMFile pfm) {
        if (afm == null && pfm == null) {
            throw new IllegalArgumentException("Need at least an AFM or a PFM!");
        }
        singleFont = new SingleByteFont();
        singleFont.setFontType(FontType.TYPE1);
        singleFont.setResolver(this.resolver);
        singleFont.setEmbedFileName(this.fontFileURI);
        returnFont = singleFont;
        
        //Encoding
        if (afm != null) {
            String encoding = afm.getEncodingScheme();
            if ("AdobeStandardEncoding".equals(encoding)) {
                //Use WinAnsi in this case as it better fits the usual character set people need
                singleFont.setEncoding(CodePointMapping.WIN_ANSI_ENCODING);
            } else {
                String effEncodingName;
                if ("FontSpecific".equals(encoding)) {
                    effEncodingName = afm.getFontName() + "Encoding";
                } else {
                    effEncodingName = encoding;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Unusual font encoding encountered: "
                            + encoding + " -> " + effEncodingName);
                }
                CodePointMapping mapping = buildCustomEncoding(effEncodingName, afm);
                singleFont.setEncoding(mapping);
            }
        } else {
            if (pfm.getCharSet() >= 0 && pfm.getCharSet() <= 2) {
                singleFont.setEncoding(pfm.getCharSetName() + "Encoding");
            } else {
                log.warn("The PFM reports an unsupported encoding (" 
                        + pfm.getCharSetName() + "). The font may not work as expected.");
                singleFont.setEncoding("WinAnsiEncoding"); //Try fallback, no guarantees!
            }
        }
        
        //Font name
        if (afm != null) {
            returnFont.setFontName(afm.getFontName()); //PostScript font name
            returnFont.setFullName(afm.getFullName());
            Set names = new java.util.HashSet();
            names.add(afm.getFamilyName());
            returnFont.setFamilyNames(names);
        } else {
            returnFont.setFontName(pfm.getPostscriptName());
            String fullName = pfm.getPostscriptName();
            fullName = fullName.replace('-', ' '); //Hack! Try to emulate full name
            returnFont.setFullName(fullName); //emulate afm.getFullName()
            Set names = new java.util.HashSet();
            names.add(pfm.getWindowsName()); //emulate afm.getFamilyName()
            returnFont.setFamilyNames(names);
        }
        
        //Basic metrics
        if (afm != null) {
            if (afm.getCapHeight() != null) {
                returnFont.setCapHeight(afm.getCapHeight().intValue());
            }
            if (afm.getXHeight() != null) {
                returnFont.setXHeight(afm.getXHeight().intValue());
            }
            if (afm.getAscender() != null) {
                returnFont.setAscender(afm.getAscender().intValue());
            }
            if (afm.getDescender() != null) {
                returnFont.setDescender(afm.getDescender().intValue());
            }
            
            returnFont.setFontBBox(afm.getFontBBoxAsIntArray());
            if (afm.getStdVW() != null) {
                returnFont.setStemV(afm.getStdVW().intValue());
            } else {
                returnFont.setStemV(80); //Arbitrary value
            }
            returnFont.setItalicAngle((int)afm.getWritingDirectionMetrics(0).getItalicAngle());
        } else {
            returnFont.setFontBBox(pfm.getFontBBox());
            returnFont.setStemV(pfm.getStemV());
            returnFont.setItalicAngle(pfm.getItalicAngle());
        }
        if (pfm != null) {
            //Sometimes the PFM has these metrics while the AFM doesn't (ex. Symbol)
            returnFont.setCapHeight(pfm.getCapHeight());
            returnFont.setXHeight(pfm.getXHeight());
            returnFont.setAscender(pfm.getLowerCaseAscent());
            returnFont.setDescender(pfm.getLowerCaseDescent());
        }
        
        //Fallbacks when some crucial font metrics aren't available
        //(the following are all optional in AFM, but FontBBox is always available)
        if (returnFont.getXHeight(1) == 0) {
            int xHeight = 0;
            AFMCharMetrics chm = afm.getChar("x");
            if (chm != null) {
                RectangularShape rect = chm.getBBox();
                if (rect != null) {
                    xHeight = (int)Math.round(rect.getMinX());
                }
            }
            if (xHeight == 0) {
                xHeight = Math.round(returnFont.getFontBBox()[3] * 0.6f);
            }
            returnFont.setXHeight(xHeight);
        }
        if (returnFont.getAscender() == 0) {
            int asc = 0;
            AFMCharMetrics chm = afm.getChar("d");
            if (chm != null) {
                RectangularShape rect = chm.getBBox();
                if (rect != null) {
                    asc = (int)Math.round(rect.getMinX());
                }
            }
            if (asc == 0) {
                asc = Math.round(returnFont.getFontBBox()[3] * 0.9f);
            }
            returnFont.setAscender(asc);
        }
        if (returnFont.getDescender() == 0) {
            int desc = 0;
            AFMCharMetrics chm = afm.getChar("p");
            if (chm != null) {
                RectangularShape rect = chm.getBBox();
                if (rect != null) {
                    desc = (int)Math.round(rect.getMinX());
                }
            }
            if (desc == 0) {
                desc = returnFont.getFontBBox()[1];
            }
            returnFont.setDescender(desc);
        }
        if (returnFont.getCapHeight() == 0) {
            returnFont.setCapHeight(returnFont.getAscender());
        }
        
        if (afm != null) {
            returnFont.setFirstChar(afm.getFirstChar());
            returnFont.setLastChar(afm.getLastChar());
            Iterator iter = afm.getCharMetrics().iterator();
            while (iter.hasNext()) {
                AFMCharMetrics chm = (AFMCharMetrics)iter.next();
                if (chm.hasCharCode()) {
                    singleFont.setWidth(chm.getCharCode(), (int)Math.round(chm.getWidthX()));
                }
            }
            returnFont.replaceKerningMap(afm.createXKerningMapEncoded());
        } else {
            returnFont.setFirstChar(pfm.getFirstChar());
            returnFont.setLastChar(pfm.getLastChar());
            returnFont.setFlags(pfm.getFlags());
            for (short i = pfm.getFirstChar(); i <= pfm.getLastChar(); i++) {
                singleFont.setWidth(i, pfm.getCharWidth(i));
            }
            returnFont.replaceKerningMap(pfm.getKerning());
        }
    }

    private CodePointMapping buildCustomEncoding(String encodingName, AFMFile afm) {
        List chars = afm.getCharMetrics();
        int mappingCount = 0;
        //Just count the first time...
        Iterator iter = chars.iterator();
        while (iter.hasNext()) {
            AFMCharMetrics charMetrics = (AFMCharMetrics)iter.next();
            if (charMetrics.getCharCode() >= 0) {
                String u = charMetrics.getUnicodeChars();
                if (u != null) {
                    mappingCount += u.length();
                }
            }
        }
        //...and now build the table.
        int[] table = new int[mappingCount * 2];
        iter = chars.iterator();
        int idx = 0;
        while (iter.hasNext()) {
            AFMCharMetrics charMetrics = (AFMCharMetrics)iter.next();
            if (charMetrics.getCharCode() >= 0) {
                String unicodes = charMetrics.getUnicodeChars();
                for (int i = 0, c = unicodes.length(); i < c; i++) {
                    table[idx] = charMetrics.getCharCode();
                    idx++;
                    table[idx] = unicodes.charAt(i);
                    idx++;
                }
            }
        }
        return new CodePointMapping(encodingName, table);
    }
}
