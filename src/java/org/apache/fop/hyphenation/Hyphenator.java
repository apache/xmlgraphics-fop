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

package org.apache.fop.hyphenation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.Map;

import org.xml.sax.InputSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.io.InternalResourceResolver;

/**
 * <p>This class is the main entry point to the hyphenation package.
 * You can use only the static methods or create an instance.</p>
 *
 * <p>This work was authored by Carlos Villegas (cav@uniscope.co.jp).</p>
 */
public final class Hyphenator {

    /** logging instance */
    protected static final Log log = LogFactory.getLog(Hyphenator.class);

    private static HyphenationTreeCache hTreeCache;

    /** Enables a dump of statistics. Note: If activated content is sent to System.out! */
    private static boolean statisticsDump;

    /**
     * Creates a new hyphenator.
     */
    private Hyphenator() { }

    /** @return the default (static) hyphenation tree cache */
    public static synchronized HyphenationTreeCache getHyphenationTreeCache() {
        if (hTreeCache == null) {
            hTreeCache = new HyphenationTreeCache();
        }
        return hTreeCache;
    }

    /**
     * Returns a hyphenation tree for a given language and country,
     * with fallback from (lang,country) to (lang).
     * The hyphenation trees are cached.
     * @param lang the language
     * @param country the country (may be null or "none")
     * @param resolver resolver to find the hyphenation files
     * @param hyphPatNames the map with user-configured hyphenation pattern file names
     * @return the hyphenation tree
     */
    public static HyphenationTree getHyphenationTree(String lang,
            String country, InternalResourceResolver resolver, Map hyphPatNames) {
        String llccKey = HyphenationTreeCache.constructLlccKey(lang, country);
        HyphenationTreeCache cache = getHyphenationTreeCache();

        // If this hyphenation tree has been registered as missing, return immediately
        if (cache.isMissing(llccKey)) {
            return null;
        }

        HyphenationTree hTree = getHyphenationTree2(lang, country, resolver, hyphPatNames);

        // fallback to lang only
        if (hTree == null && country != null && !country.equals("none")) {
            String llKey = HyphenationTreeCache.constructLlccKey(lang, null);
            if (!cache.isMissing(llKey)) {
                hTree = getHyphenationTree2(lang, null, resolver, hyphPatNames);
                if (hTree != null && log.isDebugEnabled()) {
                    log.debug("Couldn't find hyphenation pattern "
                              + "for lang=\"" + lang + "\",country=\"" + country + "\"."
                              + " Using general language pattern "
                              + "for lang=\"" + lang + "\" instead.");
                }
                if (hTree == null) {
                    // no fallback; register as missing
                    cache.noteMissing(llKey);
                } else {
                    // also register for (lang,country)
                    cache.cache(llccKey, hTree);
                }
            }
        }

        if (hTree == null) {
            // (lang,country) and (lang) tried; register as missing
            cache.noteMissing(llccKey);
            log.error("Couldn't find hyphenation pattern "
                      + "for lang=\"" + lang + "\""
                      + (country != null && !country.equals("none")
                              ? ",country=\"" + country + "\""
                              : "")
                      + ".");
        }

        return hTree;
    }

    /**
     * Returns a hyphenation tree for a given language and country
     * The hyphenation trees are cached.
     * @param lang the language
     * @param country the country (may be null or "none")
     * @param resourceResolver resolver to find the hyphenation files
     * @param hyphPatNames the map with user-configured hyphenation pattern file names
     * @return the hyphenation tree
     */
    public static HyphenationTree getHyphenationTree2(String lang,
            String country, InternalResourceResolver resourceResolver, Map hyphPatNames) {
        String llccKey = HyphenationTreeCache.constructLlccKey(lang, country);
        HyphenationTreeCache cache = getHyphenationTreeCache();

        HyphenationTree hTree;
        // first try to find it in the cache
        hTree = getHyphenationTreeCache().getHyphenationTree(lang, country);
        if (hTree != null) {
            return hTree;
        }

        String key = HyphenationTreeCache.constructUserKey(lang, country, hyphPatNames);
        if (key == null) {
            key = llccKey;
        }

        if (resourceResolver != null) {
            hTree = getUserHyphenationTree(key, resourceResolver);
        }
        if (hTree == null) {
            hTree = getFopHyphenationTree(key);
        }

        // put it into the pattern cache
        if (hTree != null) {
            cache.cache(llccKey, hTree);
        }

        return hTree;
    }

    private static InputStream getResourceStream(String key) {
        InputStream is = null;
        // Try to use Context Class Loader to load the properties file.
        try {
            java.lang.reflect.Method getCCL = Thread.class.getMethod(
                    "getContextClassLoader", new Class[0]);
            if (getCCL != null) {
                ClassLoader contextClassLoader = (ClassLoader)getCCL.invoke(
                        Thread.currentThread(),
                        new Object[0]);
                is = contextClassLoader.getResourceAsStream("hyph/" + key
                                                            + ".hyp");
            }
        } catch (NoSuchMethodException e) {
            //ignore, fallback further down
        } catch (IllegalAccessException e) {
            //ignore, fallback further down
        } catch (java.lang.reflect.InvocationTargetException e) {
            //ignore, fallback further down
        }

        if (is == null) {
            is = Hyphenator.class.getResourceAsStream("/hyph/" + key
                                                      + ".hyp");
        }

        return is;
    }

    private static HyphenationTree readHyphenationTree(InputStream in) {
        HyphenationTree hTree = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(in);
            hTree = (HyphenationTree)ois.readObject();
        } catch (IOException ioe) {
            log.error("I/O error while loading precompiled hyphenation pattern file", ioe);
        } catch (ClassNotFoundException cnfe) {
            log.error("Error while reading hyphenation object from file", cnfe);
        }
        return hTree;
    }

    /**
     * Returns a hyphenation tree. This method looks in the resources (getResourceStream) for
     * the hyphenation patterns.
     * @param key the language/country key
     * @return the hyphenation tree or null if it wasn't found in the resources
     */
    public static HyphenationTree getFopHyphenationTree(String key) {
        HyphenationTree hTree = null;
        ObjectInputStream ois = null;
        InputStream is = null;
        try {
            is = getResourceStream(key);
            if (is == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Couldn't find precompiled hyphenation pattern "
                              + key + " in resources");
                }
                return null;
            }
            hTree = readHyphenationTree(is);
        } finally {
            IOUtils.closeQuietly(ois);
        }
        return hTree;
    }

    /**
     * Load tree from serialized file or xml file
     * using configuration settings
     * @param key language key for the requested hyphenation file
     * @param resourceResolver resource resolver to find the hyphenation files
     * @return the requested HypenationTree or null if it is not available
     */
    public static HyphenationTree getUserHyphenationTree(String key,
            InternalResourceResolver resourceResolver) {
        HyphenationTree hTree = null;
        // I use here the following convention. The file name specified in
        // the configuration is taken as the base name. First we try
        // name + ".hyp" assuming a serialized HyphenationTree. If that fails
        // we try name + ".xml", assumming a raw hyphenation pattern file.

        // first try serialized object
        String name = key + ".hyp";
        try {
            InputStream in = getHyphenationTreeStream(name, resourceResolver);
            try {
                hTree = readHyphenationTree(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
            return hTree;
        } catch (IOException ioe) {
            if (log.isDebugEnabled()) {
                log.debug("I/O problem while trying to load " + name, ioe);
            }
        }

        // try the raw XML file
        name = key + ".xml";
        hTree = new HyphenationTree();
        try {
            InputStream in = getHyphenationTreeStream(name, resourceResolver);
            try {
                InputSource src = new InputSource(in);
                src.setSystemId(name);
                hTree.loadPatterns(src);
            } finally {
                IOUtils.closeQuietly(in);
            }
            if (statisticsDump) {
                System.out.println("Stats: ");
                hTree.printStats();
            }
            return hTree;
        } catch (HyphenationException ex) {
            log.error("Can't load user patterns from XML file " + name + ": " + ex.getMessage());
            return null;
        } catch (IOException ioe) {
            if (log.isDebugEnabled()) {
                log.debug("I/O problem while trying to load " + name, ioe);
            }
            return null;
        }
    }

    private static InputStream getHyphenationTreeStream(String name,
            InternalResourceResolver resourceResolver) throws IOException {
        try {
            return new BufferedInputStream(resourceResolver.getResource(name));
        } catch (URISyntaxException use) {
            log.debug("An exception was thrown while attempting to load " + name, use);
        }
        return null;
    }

    /**
     * Hyphenates a word.
     * @param lang the language
     * @param country the optional country code (may be null or "none")
     * @param resourceResolver resolver to find the hyphenation files
     * @param hyphPatNames the map with user-configured hyphenation pattern file names
     * @param word the word to hyphenate
     * @param leftMin the minimum number of characters before the hyphenation point
     * @param rightMin the minimum number of characters after the hyphenation point
     * @return the hyphenation result
     */
    public static Hyphenation hyphenate(String lang, String country,
            InternalResourceResolver resourceResolver, Map hyphPatNames, String word, int leftMin,
            int rightMin) {
        HyphenationTree hTree = getHyphenationTree(lang, country, resourceResolver, hyphPatNames);
        if (hTree == null) {
            return null;
        }
        return hTree.hyphenate(word, leftMin, rightMin);
    }
}
