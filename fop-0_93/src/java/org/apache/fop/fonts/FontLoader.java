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

package org.apache.fop.fonts;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.fop.fonts.truetype.TTFFontLoader;
import org.apache.fop.fonts.type1.Type1FontLoader;

/**
 * Base class for font loaders.
 */
public abstract class FontLoader {

    /**
     * Loads a custom font from a URI. In the case of Type 1 fonts, the PFB file must be specified.
     * @param fontFileURI the URI to the font
     * @param resolver the font resolver to use when resolving URIs
     * @return the newly loaded font
     * @throws IOException In case of an I/O error
     */
    public static CustomFont loadFont(String fontFileURI, FontResolver resolver)
                throws IOException {
        FontLoader loader;
        fontFileURI = fontFileURI.trim();
        String name = fontFileURI.toLowerCase();
        String effURI;
        boolean type1 = false;
        if (name.endsWith(".pfb")) {
            type1 = true;
            effURI = name.substring(0, fontFileURI.length() - 4) + ".pfm";
        } else {
            effURI = fontFileURI;
        }
        
        InputStream in = openFontFile(resolver, effURI);
        try {
            if (type1) {
                loader = new Type1FontLoader(fontFileURI, in, resolver);
            } else {
                loader = new TTFFontLoader(fontFileURI, in, resolver);
            }
            return loader.getFont();
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private static InputStream openFontFile(FontResolver resolver, String uri) 
                    throws IOException, MalformedURLException {
        InputStream in = null;
        if (resolver != null) {
            Source source = resolver.resolve(uri);
            if (source == null) {
                String err = "Cannot load font: failed to create Source for font file " 
                    + uri; 
                throw new IOException(err);
            }
            if (source instanceof StreamSource) {
                in = ((StreamSource) source).getInputStream();
            }
            if (in == null && source.getSystemId() != null) {
                in = new java.net.URL(source.getSystemId()).openStream();
            }
            if (in == null) {
                String err = "Cannot load font: failed to create InputStream from"
                    + " Source for font file " + uri; 
                throw new IOException(err);
            }
        } else {
            in = new URL(uri).openStream();
        }
        return in;
    }
    
    /**
     * @return the font loaded by this loader
     */
    public abstract CustomFont getFont();

    
}
