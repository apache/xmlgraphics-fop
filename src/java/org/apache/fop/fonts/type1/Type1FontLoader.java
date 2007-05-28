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

import java.io.IOException;
import java.io.InputStream;

import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.SingleByteFont;

/**
 * Loads a Type 1 font into memory directly from the original font file.
 */
public class Type1FontLoader extends FontLoader {

    private PFMFile pfm;
    private SingleByteFont singleFont;
    
    /**
     * Constructs a new Type 1 font loader.
     * @param fontFileURI the URI to the PFB file of a Type 1 font
     * @param in the InputStream reading the PFM file of a Type 1 font
     * @param resolver the font resolver used to resolve URIs
     * @throws IOException In case of an I/O error
     */
    public Type1FontLoader(String fontFileURI, InputStream in, FontResolver resolver) 
                throws IOException {
        super(fontFileURI, in, resolver);
    }

    /**
     * @see FontLoader#read()
     */
    protected void read() throws IOException {
        pfm = new PFMFile();
        pfm.load(in);
        singleFont = new SingleByteFont();
        singleFont.setFontType(FontType.TYPE1);
        singleFont.setResolver(this.resolver);
        returnFont = singleFont;
        returnFont.setFontName(pfm.getPostscriptName());
        returnFont.setCapHeight(pfm.getCapHeight());
        returnFont.setXHeight(pfm.getXHeight());
        returnFont.setAscender(pfm.getLowerCaseAscent());
        returnFont.setDescender(pfm.getLowerCaseDescent());
        returnFont.setFontBBox(pfm.getFontBBox());
        returnFont.setFirstChar(pfm.getFirstChar());
        returnFont.setLastChar(pfm.getFirstChar());
        returnFont.setFlags(pfm.getFlags());
        returnFont.setStemV(pfm.getStemV());
        returnFont.setItalicAngle(pfm.getItalicAngle());
        returnFont.setMissingWidth(0);
        for (short i = pfm.getFirstChar(); i <= pfm.getLastChar(); i++) {
            singleFont.setWidth(i, pfm.getCharWidth(i));
        }
        singleFont.setEmbedFileName(this.fontFileURI);
    }
}
