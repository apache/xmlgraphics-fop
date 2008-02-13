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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * Font info stored in the cache 
 */
public class CachedFontInfo extends EmbedFontInfo {

    /** Serialization Version UID */
    private static final long serialVersionUID = 240028291961081894L;
    
    /** file modify date (if available) */
    private long lastModified = -1;

    /**
     * Returns a file given a list of urls
     * @param urls array of possible font urls
     * @return file font file 
     */
    public static File getFileFromUrls(String[] urls) {
        for (int i = 0; i < urls.length; i++) {
            String urlStr = urls[i]; 
            if (urlStr != null) {
                File fontFile = null;
                if (urlStr.startsWith("file:")) {
                    try {
                        URL url = new URL(urlStr);
                        fontFile = FileUtils.toFile(url);
                    } catch (MalformedURLException mfue) {
                        // do nothing
                    }
                }
                if (fontFile == null) {
                    fontFile = new File(urlStr);
                }
                if (fontFile.exists() && fontFile.canRead()) {
                    return fontFile;
                }
            }
        } 
        return null;
    }

    /**
     * Default constructor
     * @param metricsFile metrics file
     * @param kerning kerning
     * @param fontTriplets font triplets
     * @param embedFile embed file
     * @param lastModified timestamp that this font was last modified 
     */
    public CachedFontInfo(String metricsFile, boolean kerning, List fontTriplets,
            String embedFile, long lastModified) {
        super(metricsFile, kerning, fontTriplets, embedFile);
        this.lastModified = lastModified;
    }

    /**
     * Constructor
     * @param fontInfo an existing embed font info
     */
    public CachedFontInfo(EmbedFontInfo fontInfo) {
        super(fontInfo.metricsFile, fontInfo.kerning, fontInfo.fontTriplets, fontInfo.embedFile);
        // try and determine modified date
        File fontFile = getFileFromUrls(new String[] {embedFile, metricsFile});
        if (fontFile != null ) {
            this.lastModified = fontFile.lastModified();
        }
    }

    /**
     * Gets the modified timestamp for font file (not always available)
     * @return modified timestamp
     */
    public long lastModified() {
        return this.lastModified;
    }

    /**
     * Gets the modified timestamp for font file
     * (used for the purposes of font info caching) 
     * @param lastModified modified font file timestamp
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
    /**
     * @return string representation of this object 
     * {@inheritDoc}
     */
    public String toString() {
        return super.toString() + ", lastModified=" + lastModified;
    }
}
