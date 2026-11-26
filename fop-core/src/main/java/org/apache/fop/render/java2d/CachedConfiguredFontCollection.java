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

package org.apache.fop.render.java2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.Typeface;

/**
 * A {@link ConfiguredFontCollection} whose fonts, including the AWT fonts, are loaded once per
 * {@link Cache} and then shared read-only between renderings. The cache belongs to the renderer
 * configuration, so it lives once per FopFactory and output format (cache-fonts attribute of the
 * fonts element). Font files must not change on disk while the FopFactory lives: the metrics stay
 * cached, but Java reads the glyphs from the file again.
 * <p>
 * The fonts are loaded in full embedding mode, whatever the configuration says: subsetting records
 * every first use of a glyph in maps that are not thread-safe. Bitmap output does not subset anyway.
 * <p>
 * The internal font names contain the start number, so the fonts are loaded again if it changes
 * (see {@link InstalledFontCollection}). They are also loaded again, at most every
 * {@link #RELOAD_INTERVAL_SECONDS} seconds, if Java has replaced one by its default font (see
 * {@link CustomFontMetricsMapper#isReplacedByJava()} and {@link CustomFontMetricsMapper#isFileBacked()}).
 */
public class CachedConfiguredFontCollection implements FontCollection {

    private static final Log log = LogFactory.getLog(CachedConfiguredFontCollection.class);

    /** fonts replaced by Java are loaded again at most this often */
    private static final int RELOAD_INTERVAL_SECONDS = 30;

    /** Supplies the configured font list; only called when the fonts are not cached yet. */
    public interface FontListProvider {
        /**
         * @return the configured fonts
         * @throws FOPException if the font configuration is invalid
         */
        List<EmbedFontInfo> getFontList() throws FOPException;
    }

    /** Holds the fonts loaded with one renderer configuration. */
    public static final class Cache {
        private volatile Snapshot snapshot;
        /** when fonts replaced by Java were last loaded again, null if never; guarded by this */
        private Long lastReloadNanos;
        /** true once the renderings during the current reload interval were warned about; guarded by this */
        private boolean warnedDuringInterval;

        /** @return true once the fonts have been loaded into this cache */
        public boolean isLoaded() {
            return snapshot != null;
        }

        /** For testing purposes only: makes the cached fonts look as if Java had replaced them by its default font. */
        void markFontsReplacedByJava() {
            for (CustomFontMetricsMapper mapper : snapshot.mappers) {
                mapper.markReplacedByJava();
            }
        }
    }

    /** Fully loaded fonts, published through the volatile field of the cache. */
    private static final class Snapshot {
        /** the start number the fonts are registered with (the internal font names contain it) */
        private final int startNum;
        private final int nextNum;
        private final FontInfo fonts;
        private final CustomFontMetricsMapper[] mappers;

        Snapshot(int startNum, int nextNum, FontInfo fonts, List<CustomFontMetricsMapper> mappers) {
            this.startNum = startNum;
            this.nextNum = nextNum;
            this.fonts = fonts;
            this.mappers = mappers.toArray(new CustomFontMetricsMapper[mappers.size()]);
        }

        /** @return the fonts Java has replaced by its default font, normally none */
        List<String> getFontsReplacedByJava() {
            List<String> replaced = new ArrayList<String>();
            for (CustomFontMetricsMapper mapper : mappers) {
                if (mapper.isReplacedByJava()) {
                    replaced.add(mapper.getFullName() + " (" + mapper.getFontURI() + ")");
                }
            }
            return replaced;
        }
    }

    private final Cache cache;
    private final InternalResourceResolver resourceResolver;
    private final FontListProvider fontListProvider;
    private final boolean useComplexScripts;

    /**
     * Main constructor.
     * @param cache the cache of the renderer configuration
     * @param resourceResolver the resource resolver
     * @param fontListProvider supplies the configured fonts if they are not cached yet
     * @param useComplexScripts true if complex script features are enabled
     */
    public CachedConfiguredFontCollection(Cache cache, InternalResourceResolver resourceResolver,
            FontListProvider fontListProvider, boolean useComplexScripts) {
        this.cache = cache;
        this.resourceResolver = resourceResolver;
        this.fontListProvider = fontListProvider;
        this.useComplexScripts = useComplexScripts;
    }

    /** {@inheritDoc} */
    public int setup(int start, FontInfo target) {
        Snapshot snapshot = cache.snapshot;
        if (snapshot == null || snapshot.startNum != start || !snapshot.getFontsReplacedByJava().isEmpty()) {
            snapshot = loadAndCache(start);
        }
        for (Map.Entry<String, Typeface> entry : snapshot.fonts.getFonts().entrySet()) {
            Typeface typeface = entry.getValue();
            if (typeface instanceof CustomFontMetricsMapper) {
                // the mapper holds mutable size state: every request gets its own copy
                typeface = new CustomFontMetricsMapper((CustomFontMetricsMapper) typeface);
            }
            target.addMetrics(entry.getKey(), typeface);
        }
        for (Map.Entry<FontTriplet, String> entry : snapshot.fonts.getFontTriplets().entrySet()) {
            target.addFontProperties(entry.getValue(), entry.getKey());
        }
        return snapshot.nextNum;
    }

    /**
     * Loads the fonts again if they are missing, for another start number, or replaced by Java.
     * Renderings in progress keep their fonts.
     */
    private Snapshot loadAndCache(int start) {
        synchronized (cache) {
            Snapshot current = cache.snapshot;
            boolean sameStart = current != null && current.startNum == start;
            if (sameStart) {
                List<String> replaced = current.getFontsReplacedByJava();
                if (replaced.isEmpty()) {
                    return current; // loaded by another rendering in the meantime
                }
                long now = System.nanoTime();
                if (cache.lastReloadNanos != null
                        && now - cache.lastReloadNanos < TimeUnit.SECONDS.toNanos(RELOAD_INTERVAL_SECONDS)) {
                    if (!cache.warnedDuringInterval) {
                        cache.warnedDuringInterval = true;
                        log.error("Java replaced " + replaced.size() + " cached font(s) by its default font again,"
                                + " less than " + RELOAD_INTERVAL_SECONDS
                                + " seconds after the last attempt to load them again: "
                                + replaced + ". Text in these fonts is drawn in the wrong font until they are"
                                + " loaded again.");
                    }
                    // reloaded less than RELOAD_INTERVAL_SECONDS ago: keep the replaced fonts for now
                    return current;
                }
                cache.lastReloadNanos = now;
                cache.warnedDuringInterval = false;
                log.error("Java replaced " + replaced.size() + " cached font(s) by its default font,"
                        + " because it could not open or read the font file: " + replaced
                        + ". Text in these fonts was drawn in the wrong font. Loading the fonts again.");
            } else if (current != null) {
                log.warn("Start number of the configured fonts changed from " + current.startNum + " to "
                        + start + " (the number of installed system fonts changed), loading the fonts again");
            }
            Snapshot fresh = load(start);
            if (sameStart && fresh.fonts.getFonts().size() < current.fonts.getFonts().size()) {
                // a font drawn in the wrong typeface is better than a font that is missing
                log.error("Not all fonts could be loaded again, keeping the previous fonts");
                return current;
            }
            cache.snapshot = fresh;
            return fresh;
        }
    }

    private Snapshot load(int start) {
        List<EmbedFontInfo> fontList;
        try {
            fontList = fontListProvider.getFontList();
        } catch (FOPException e) {
            throw new IllegalStateException("Unable to read the font configuration: " + e.getMessage(), e);
        }
        List<EmbedFontInfo> fullFonts = new ArrayList<EmbedFontInfo>(fontList.size());
        for (EmbedFontInfo fontInfo : fontList) {
            fullFonts.add(fontInfo.withEmbeddingMode(EmbeddingMode.FULL));
        }
        FontInfo master = new FontInfo();
        int next = new ConfiguredFontCollection(resourceResolver, fullFonts, useComplexScripts)
                .setup(start, master);
        List<CustomFontMetricsMapper> mappers = new ArrayList<CustomFontMetricsMapper>();
        for (Typeface typeface : master.getFonts().values()) {
            if (typeface instanceof CustomFontMetricsMapper) {
                CustomFontMetricsMapper mapper = (CustomFontMetricsMapper) typeface;
                mapper.preload();
                mappers.add(mapper);
                if (!mapper.isFileBacked()) {
                    log.warn("Font '" + mapper.getFullName() + "' (" + mapper.getFontURI() + ") is not read"
                            + " from a local file (no file: URL, or the application uses its own resource"
                            + " resolver): Java renders it from a temporary copy in "
                            + System.getProperty("java.io.tmpdir") + ". If that copy is deleted, the font"
                            + " has to be loaded again and text can be drawn in another font meanwhile.");
                }
            }
        }
        return new Snapshot(start, next, master, mappers);
    }
}
