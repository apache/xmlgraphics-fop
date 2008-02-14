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
    /** the FontResolver to use for font URI resolution */
    protected FontResolver resolver = null;
    /** the loaded font */
    protected CustomFont returnFont = null;

    /** true if the font has been loaded */
    protected boolean loaded = false;

    /**
     * Default constructor.
     * @param fontFileURI the URI to the PFB file of a Type 1 font
     * @param resolver the font resolver used to resolve URIs
     */
    public FontLoader(String fontFileURI, FontResolver resolver) {
        this.fontFileURI = fontFileURI;
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
     * Loads a custom font from an URL. In the case of Type 1 fonts, the PFB file must be specified.
     * @param fontUrl the URL representation of the font
     * @param resolver the font resolver to use when resolving URIs
     * @return the newly loaded font
     * @throws IOException In case of an I/O error
     */
    public static CustomFont loadFont(URL fontUrl, FontResolver resolver)
                throws IOException {
        return loadFont(fontUrl.toExternalForm(), resolver);
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
        boolean type1 = isType1(fontFileURI);
        FontLoader loader;
        if (type1) {
            loader = new Type1FontLoader(fontFileURI, resolver);
        } else {
            loader = new TTFFontLoader(fontFileURI, resolver);
        }
        return loader.getFont();
    }

    /**
     * Opens a font URI and returns an input stream.
     * @param resolver the FontResolver to use for font URI resolution
     * @param uri the URI representing the font
     * @return the InputStream to read the font from.
     * @throws IOException In case of an I/O error
     * @throws MalformedURLException If an invalid URL is built
     */
    protected static InputStream openFontUri(FontResolver resolver, String uri) 
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

    /**
     * Returns the custom font that was read using this instance of FontLoader.
     * @return the newly loaded font
     * @throws IOException if an I/O error occurs
     */
    public CustomFont getFont() throws IOException {
        if (!loaded) {
            read();
        }
        return this.returnFont;
    }
}
