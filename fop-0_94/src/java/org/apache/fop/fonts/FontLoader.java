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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.truetype.TTFFontLoader;
import org.apache.fop.fonts.type1.Type1FontLoader;

/**
 * Base class for font loaders.
 */
public abstract class FontLoader {

    /**
     * logging instance
     */
    protected static Log log = LogFactory.getLog(FontLoader.class);

    /** URI representing the font file */
    protected String fontFileURI = null;
    /** the InputStream to load the font from */
    protected InputStream in = null;
    /** the FontResolver to use for font URI resolution */
    protected FontResolver resolver = null;
    /** the loaded font */
    protected CustomFont returnFont = null;

    /** true if the font has been loaded */
    protected boolean loaded = false;

    /**
     * Default constructor.
     * @param fontFileURI the URI to the PFB file of a Type 1 font
     * @param in the InputStream reading the PFM file of a Type 1 font
     * @param resolver the font resolver used to resolve URIs
     */
    public FontLoader(String fontFileURI, InputStream in, FontResolver resolver) {
        this.fontFileURI = fontFileURI;
        this.in = in;
        this.resolver = resolver;
    }

    private static boolean isType1(String fontURI) {
        return fontURI.toLowerCase().endsWith(".pfb");
    }

    /**
     * Loads a custom font from a File. In the case of Type 1 fonts, the PFB file must be specified.
     * @param fontFile the File representation of the font
     * @param resolver the font resolver to use when resolving URIs
     * @return the newly loaded font
     * @throws IOException In case of an I/O error
     */
    public static CustomFont loadFont(File fontFile, FontResolver resolver)
                throws IOException {
        return loadFont(fontFile.getAbsolutePath(), resolver);
    }
        
    /**
     * Loads a custom font from a URI. In the case of Type 1 fonts, the PFB file must be specified.
     * @param fontFileURI the URI to the font
     * @param resolver the font resolver to use when resolving URIs
     * @return the newly loaded font
     * @throws IOException In case of an I/O error
     */
    public static CustomFont loadFont(String fontFileURI, FontResolver resolver)
                throws IOException {
        fontFileURI = fontFileURI.trim();
        String name = fontFileURI.toLowerCase();
        String effURI;
        boolean type1 = isType1(fontFileURI);
        if (type1) {
            effURI = name.substring(0, fontFileURI.length() - 4) + ".pfm";
        } else {
            effURI = fontFileURI;
        }
        if (log.isDebugEnabled()) {
            log.debug("opening " + effURI);
        }
        InputStream in = openFontFile(resolver, effURI);
        return loadFontFromInputStream(fontFileURI, resolver, type1, in);
    }

    /**
     * Loads and returns a font given an input stream.
     * @param fontFileURI font file uri
     * @param resolver font resolver
     * @param isType1 is it a type1 font?
     * @param in input stream
     * @return the loaded font.
     * @throws IOException In case of an I/O error
     */
    protected static CustomFont loadFontFromInputStream(
            String fontFileURI, FontResolver resolver, boolean isType1,
            InputStream in)
                throws IOException {
        FontLoader loader;
        try {
            if (isType1) {
                loader = new Type1FontLoader(fontFileURI, in, resolver);
            } else {
                loader = new TTFFontLoader(fontFileURI, in, resolver);
            }
            return loader.getFont();
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Opens a font file and returns an input stream.
     * @param resolver the FontResolver to use for font URI resolution
     * @param uri the URI representing the font
     * @return the InputStream to read the font from.
     * @throws IOException In case of an I/O error
     * @throws MalformedURLException If an invalid URL is built
     */
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
     * Reads/parses the font data.
     * @throws IOException In case of an I/O error
     */
    protected abstract void read() throws IOException;

    /** @see org.apache.fop.fonts.FontLoader#getFont() */
    public CustomFont getFont() throws IOException {
        if (!loaded) {
            read();
        }
        return this.returnFont;
    }
}
