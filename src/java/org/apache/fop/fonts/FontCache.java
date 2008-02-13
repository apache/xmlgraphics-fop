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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.util.LogUtil;

/**
 * Fop cache (currently only used for font info caching)
 */
public final class FontCache implements Serializable {
    
    /** Serialization Version UID */
    private static final long serialVersionUID = 605232520271754717L;

    /** logging instance */
    private static Log log = LogFactory.getLog(FontCache.class);

    /** FOP's user directory name */
    private static final String FOP_USER_DIR = ".fop";

    /** font cache file path */
    private static final String DEFAULT_CACHE_FILENAME = "fop-fonts.cache";

    /** has this cache been changed since it was last read? */
    private transient boolean changed = false;
    
    /** change lock */
    private transient Object changeLock = new Object();
    
    /** master mapping of font url -> font info */
    private Map fontMap = new java.util.HashMap();

    /** mapping of font url -> file modified date */
    private Map failedFontMap = new java.util.HashMap();

    /**
     * Default constructor
     */
    public FontCache() {
        //nop
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.changeLock = new Object(); //Initialize transient field
    }

    private static File getUserHome() {
        String s = System.getProperty("user.home");
        if (s != null) {
            File userDir = new File(s);
            if (userDir.exists()) {
                return userDir;
            }
        }
        return null;
    }
    
    /**
     * Returns the default font cache file.
     * @param forWriting true if the user directory should be created
     * @return the default font cache file
     */
    public static File getDefaultCacheFile(boolean forWriting) {
        File userHome = getUserHome();
        if (userHome != null) {
            File fopUserDir = new File(userHome, FOP_USER_DIR);
            if (forWriting) {
                fopUserDir.mkdir();
            }
            return new File(fopUserDir, DEFAULT_CACHE_FILENAME);
        }
        return new File(FOP_USER_DIR);
    }
    
    /**
     * Reads the default font cache file and returns its contents.
     * @return the font cache deserialized from the file (or null if no cache file exists or if
     *         it could not be read)
     */
    public static FontCache load() {
        return loadFrom(getDefaultCacheFile(false));
    }
    
    /**
     * Reads a font cache file and returns its contents.
     * @param cacheFile the cache file
     * @return the font cache deserialized from the file (or null if no cache file exists or if
     *         it could not be read)
     */
    public static FontCache loadFrom(File cacheFile) {
        if (cacheFile.exists()) {
            try {
                if (log.isTraceEnabled()) {
                    log.trace("Loading font cache from " + cacheFile.getCanonicalPath());
                }
                InputStream in = new java.io.FileInputStream(cacheFile);
                in = new java.io.BufferedInputStream(in);
                ObjectInputStream oin = new ObjectInputStream(in);
                try {
                    return (FontCache)oin.readObject();
                } finally {
                    IOUtils.closeQuietly(oin);
                }
            } catch (ClassNotFoundException e) {
                //We don't really care about the exception since it's just a cache file
                log.warn("Could not read font cache. Discarding font cache file. Reason: " 
                        + e.getMessage());
            } catch (IOException ioe) {
                //We don't really care about the exception since it's just a cache file
                log.warn("I/O exception while reading font cache (" + ioe.getMessage() 
                        + "). Discarding font cache file.");
            }
        }
        return null;
    }
    
    /**
     * Writes the font cache to disk.
     * @throws FOPException fop exception
     */
    public void save() throws FOPException {
        saveTo(getDefaultCacheFile(true));
    }
    
    /**
     * Writes the font cache to disk.
     * @param cacheFile the file to write to 
     * @throws FOPException fop exception
     */
    public void saveTo(File cacheFile) throws FOPException {
        synchronized (changeLock) {
            if (changed) {
                try {
                    if (log.isTraceEnabled()) {
                        log.trace("Writing font cache to " + cacheFile.getCanonicalPath());
                    }
                    OutputStream out = new java.io.FileOutputStream(cacheFile);
                    out = new java.io.BufferedOutputStream(out);
                    ObjectOutputStream oout = new ObjectOutputStream(out);
                    try {
                        oout.writeObject(this);
                    } finally {
                        IOUtils.closeQuietly(oout);
                    }
                } catch (IOException ioe) {
                    LogUtil.handleException(log, ioe, true);
                }
                changed = false;
                log.trace("Cache file written.");
            }
        }
    }

    /**
     * creates a key given a font info for the font mapping
     * @param fontInfo font info
     * @return font cache key
     */
    protected static String getCacheKey(EmbedFontInfo fontInfo) {
        if (fontInfo != null) {
            String embedFile = fontInfo.getEmbedFile();
            String metricsFile = fontInfo.getMetricsFile();
            return (embedFile != null) ? embedFile : metricsFile;
        }
        return null;
    }

    /**
     * cache has been updated since it was read
     * @return if this cache has changed
     */
    public boolean hasChanged() {
        return this.changed;
    }
    
    /**
     * is this font in the cache?
     * @param embedUrl font info
     * @return boolean
     */
    public boolean containsFont(String embedUrl) {
        if (embedUrl != null) {
            return fontMap.containsKey(embedUrl);
        }
        return false;
    }

    /**
     * is this font info in the cache?
     * @param fontInfo font info
     * @return font
     */
    public boolean containsFont(EmbedFontInfo fontInfo) {
        if (fontInfo != null) {
            return fontMap.containsKey(getCacheKey(fontInfo));
        }
        return false;
    }

    /**
     * adds a font info to cache
     * @param fontInfo font info
     */
    public void addFont(EmbedFontInfo fontInfo) {
        String cacheKey = getCacheKey(fontInfo);
        synchronized (changeLock) {
            if (!containsFont(cacheKey)) {
                if (log.isTraceEnabled()) {
                    log.trace("Font added to cache: " + cacheKey);
                }
                if (fontInfo instanceof CachedFontInfo) {
                    fontMap.put(cacheKey, fontInfo);
                } else {
                    fontMap.put(cacheKey, new CachedFontInfo(fontInfo));
                }
                changed = true;
            }
        }
    }

    /**
     * returns a font from the cache
     * @param embedUrl font info
     * @return boolean
     */
    public CachedFontInfo getFont(String embedUrl) {
        if (containsFont(embedUrl)) {
            return (CachedFontInfo)fontMap.get(embedUrl);
        }
        return null;
    }
    
    /**
     * removes font from cache
     * @param embedUrl embed url
     */
    public void removeFont(String embedUrl) {
        synchronized (changeLock) {
            if (containsFont(embedUrl)) {
                if (log.isTraceEnabled()) {
                    log.trace("Font removed from cache: " + embedUrl);
                }
                fontMap.remove(embedUrl);
                changed = true;
            }
        }
    }
    
    /**
     * has this font previously failed to load?
     * @param embedUrl embed url
     * @param lastModified last modified
     * @return whether this is a failed font
     */
    public boolean isFailedFont(String embedUrl, long lastModified) {
        if (failedFontMap.containsKey(embedUrl)) {
            synchronized (changeLock) {
                long failedLastModified = ((Long)failedFontMap.get(embedUrl)).longValue();
                if (lastModified != failedLastModified) {
                    // this font has been changed so lets remove it
                    // from failed font map for now
                    failedFontMap.remove(embedUrl);
                    changed = true;
                }                
            }
            return true;
        }
        return false;
    }

    /**
     * registers a failed font with the cache
     * @param embedUrl embed url
     * @param lastModified time last modified
     */
    public void registerFailedFont(String embedUrl, long lastModified) {
        synchronized (changeLock) {
            if (!failedFontMap.containsKey(embedUrl)) {
                failedFontMap.put(embedUrl, new Long(lastModified));
                changed = true;
            }
        }
    }

    /**
     * Clears font cache
     */
    public void clear() {
        synchronized (changeLock) {
            if (log.isTraceEnabled()) {
                log.trace("Font cache cleared.");
            }
            fontMap.clear();
            failedFontMap.clear();
            changed = true;
        }
    }
}
