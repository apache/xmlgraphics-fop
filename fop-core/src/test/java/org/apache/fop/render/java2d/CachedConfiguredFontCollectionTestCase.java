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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;

import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.FontEventListener;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUris;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;

/**
 * Tests that the fonts shared by {@link CachedConfiguredFontCollection} stay correct when
 * several threads use them.
 */
public class CachedConfiguredFontCollectionTestCase {

    private static final File FONT_FILE = new File("test/resources/fonts/ttf/DejaVuLGCSerif.ttf");

    private static final String TEXT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            + " .,;:-+%/()&áàâãçéêíóôõúü";

    private static final int THREADS = 8;

    private static final String FAMILY = "DejaVu LGC Serif";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void checkFontFile() {
        assertTrue("Test font not found: " + FONT_FILE.getAbsolutePath()
                + " (the working directory must be the fop module, as surefire sets it)", FONT_FILE.isFile());
    }

    private static List<EmbedFontInfo> createFontList() {
        return createFontList(false);
    }

    private static List<EmbedFontInfo> createFontList(boolean lazyLoad) {
        return createFontList(FONT_FILE.toURI(), lazyLoad);
    }

    private static List<EmbedFontInfo> createFontList(URI fontURI, boolean lazyLoad) {
        // the defaults of a configured font: in embedding mode AUTO the font would be subset
        EmbedFontInfo fontInfo = new EmbedFontInfo(new FontUris(fontURI, null), true, true,
                Collections.singletonList(new FontTriplet("DejaVu", "normal", 400)), null,
                EncodingMode.AUTO, EmbeddingMode.AUTO, false, false, true, lazyLoad);
        return Collections.singletonList(fontInfo);
    }

    private static final InternalResourceResolver RESOLVER
            = FopFactory.newInstance(new File(".").toURI()).getFontManager().getResourceResolver();

    private static CachedConfiguredFontCollection.FontListProvider provide(final List<EmbedFontInfo> fontList) {
        return new CachedConfiguredFontCollection.FontListProvider() {
            public List<EmbedFontInfo> getFontList() {
                return fontList;
            }
        };
    }

    /** Loads the font through a new, empty cache. */
    private static CustomFont loadSharedFont(List<EmbedFontInfo> fontList) {
        FontInfo fontInfo = new FontInfo();
        new CachedConfiguredFontCollection(new CachedConfiguredFontCollection.Cache(), RESOLVER,
                provide(fontList), false).setup(1, fontInfo);
        CustomFontMetricsMapper mapper = (CustomFontMetricsMapper) fontInfo.getFonts().get("F1");
        Typeface font = mapper.getRealFont();
        return (CustomFont) (font instanceof LazyFont ? ((LazyFont) font).getRealFont() : font);
    }

    private static int[] getWidths(Typeface font) {
        int[] widths = new int[TEXT.length()];
        for (int i = 0; i < TEXT.length(); i++) {
            widths[i] = font.getWidth(font.mapChar(TEXT.charAt(i)), 1);
        }
        return widths;
    }

    @Test
    public void testSharedFontsAreLoadedInFullEmbeddingMode() {
        List<EmbedFontInfo> fontList = createFontList();
        CustomFont font = loadSharedFont(fontList);
        assertEquals(EmbeddingMode.FULL, font.getEmbeddingMode());
        // the configuration itself is left alone
        assertEquals(EmbeddingMode.AUTO, fontList.get(0).getEmbeddingMode());
    }

    @Test
    public void testFontIsSharedBetweenRenderings() {
        CachedConfiguredFontCollection.FontListProvider provider = provide(createFontList());
        CachedConfiguredFontCollection.Cache cache = new CachedConfiguredFontCollection.Cache();
        FontInfo first = new FontInfo();
        new CachedConfiguredFontCollection(cache, RESOLVER, provider, false).setup(1, first);
        FontInfo second = new FontInfo();
        new CachedConfiguredFontCollection(cache, RESOLVER, provider, false).setup(1, second);
        assertSame(((CustomFontMetricsMapper) first.getFonts().get("F1")).getRealFont(),
                ((CustomFontMetricsMapper) second.getFonts().get("F1")).getRealFont());
        // but each rendering has its own mapper, which holds the mutable size state
        assertNotSame(first.getFonts().get("F1"), second.getFonts().get("F1"));
    }

    @Test
    public void testLazyLoadFontIsSharedAndRenders() {
        CachedConfiguredFontCollection.FontListProvider provider = provide(createFontList(true));
        CachedConfiguredFontCollection.Cache cache = new CachedConfiguredFontCollection.Cache();
        FontInfo first = new FontInfo();
        new CachedConfiguredFontCollection(cache, RESOLVER, provider, false).setup(1, first);
        FontInfo second = new FontInfo();
        new CachedConfiguredFontCollection(cache, RESOLVER, provider, false).setup(1, second);
        CustomFontMetricsMapper mapper = (CustomFontMetricsMapper) first.getFonts().get("F1");
        assertEquals(EmbeddingMode.FULL, ((CustomFont) mapper.getRealFont()).getEmbeddingMode());
        assertSame(mapper.getRealFont(),
                ((CustomFontMetricsMapper) second.getFonts().get("F1")).getRealFont());
        // the copy can render although it was made from a lazily configured font
        java.awt.Font awtFont = mapper.getFont(12000);
        assertEquals(12f, awtFont.getSize2D(), 0f);
        assertEquals(FAMILY, awtFont.getFamily());
    }

    /** Sets up a rendering with the given cache and returns its font F&lt;start&gt;. */
    private static CustomFontMetricsMapper setup(CachedConfiguredFontCollection.Cache cache,
            CachedConfiguredFontCollection.FontListProvider provider, int start) {
        FontInfo fontInfo = new FontInfo();
        new CachedConfiguredFontCollection(cache, RESOLVER, provider, false).setup(start, fontInfo);
        return (CustomFontMetricsMapper) fontInfo.getFonts().get("F" + start);
    }

    @Test
    public void testFontsReplacedByJavaAreLoadedAgainAtMostEveryInterval() {
        CachedConfiguredFontCollection.FontListProvider provider = provide(createFontList());
        CachedConfiguredFontCollection.Cache cache = new CachedConfiguredFontCollection.Cache();
        Typeface original = setup(cache, provider, 1).getRealFont();
        final List<String> errors = new ArrayList<String>();
        Handler handler = new Handler() {
            public void publish(LogRecord record) {
                if (record.getLevel() == Level.SEVERE) {
                    errors.add(record.getMessage());
                }
            }

            public void flush() {
            }

            public void close() {
            }
        };
        Logger logger = Logger.getLogger(CachedConfiguredFontCollection.class.getName());
        logger.addHandler(handler);
        try {
            cache.markFontsReplacedByJava();
            Typeface reloaded = setup(cache, provider, 1).getRealFont();
            assertNotSame("replaced fonts are loaded again", original, reloaded);

            cache.markFontsReplacedByJava();
            assertSame("not loaded again within the interval", reloaded, setup(cache, provider, 1).getRealFont());
            assertSame(reloaded, setup(cache, provider, 1).getRealFont());
        } finally {
            logger.removeHandler(handler);
        }
        assertEquals("one error for the reload, one for the interval: " + errors, 2, errors.size());
        assertTrue(errors.get(0), errors.get(0).endsWith("Loading the fonts again."));
        assertTrue(errors.get(1), errors.get(1).contains("again, less than 30 seconds"));
    }

    @Test
    public void testPreviousFontsAreKeptIfFewerCanBeLoadedAgain() {
        final List<List<EmbedFontInfo>> fontList = new ArrayList<List<EmbedFontInfo>>();
        fontList.add(createFontList());
        CachedConfiguredFontCollection.FontListProvider provider
                = new CachedConfiguredFontCollection.FontListProvider() {
            public List<EmbedFontInfo> getFontList() {
                return fontList.get(0);
            }
        };
        CachedConfiguredFontCollection.Cache cache = new CachedConfiguredFontCollection.Cache();
        Typeface original = setup(cache, provider, 1).getRealFont();

        cache.markFontsReplacedByJava();
        fontList.set(0, Collections.<EmbedFontInfo>emptyList()); // the font can no longer be loaded
        assertSame(original, setup(cache, provider, 1).getRealFont());
    }

    @Test
    public void testFontsAreLoadedAgainWhenStartNumberChanges() {
        CachedConfiguredFontCollection.FontListProvider provider = provide(createFontList());
        CachedConfiguredFontCollection.Cache cache = new CachedConfiguredFontCollection.Cache();
        Typeface first = setup(cache, provider, 1).getRealFont();

        CustomFontMetricsMapper moved = setup(cache, provider, 5);
        assertNotSame(first, moved.getRealFont());

        FontInfo fontInfo = new FontInfo();
        int next = new CachedConfiguredFontCollection(cache, RESOLVER, provider, false).setup(5, fontInfo);
        assertNull(fontInfo.getFonts().get("F1"));
        assertEquals(6, next);
        assertSame("cached for the new start number", moved.getRealFont(),
                ((CustomFontMetricsMapper) fontInfo.getFonts().get("F5")).getRealFont());
    }

    @Test
    public void testLocalFontFileIsGivenToJava() {
        assertTrue(setup(new CachedConfiguredFontCollection.Cache(), provide(createFontList()), 1)
                .isFileBacked());
    }

    @Test
    public void testFontOfApplicationResolverIsReadFromStream() {
        final ResourceResolver defaultResolver = ResourceResolverFactory.createDefaultResourceResolver();
        ResourceResolver ownResolver = new ResourceResolver() {
            public Resource getResource(URI uri) throws IOException {
                return defaultResolver.getResource(uri);
            }

            public OutputStream getOutputStream(URI uri) throws IOException {
                return defaultResolver.getOutputStream(uri);
            }
        };
        InternalResourceResolver resolver = ResourceResolverFactory.createInternalResourceResolver(
                new File(".").toURI(), ownResolver);
        FontInfo fontInfo = new FontInfo();
        new CachedConfiguredFontCollection(new CachedConfiguredFontCollection.Cache(), resolver,
                provide(createFontList()), false).setup(1, fontInfo);
        CustomFontMetricsMapper mapper = (CustomFontMetricsMapper) fontInfo.getFonts().get("F1");
        assertFalse(mapper.isFileBacked());
        assertEquals(FAMILY, mapper.getFont(12000).getFamily());
    }

    @Test
    public void testFontInJarIsReadFromStream() throws IOException {
        File jar = tempFolder.newFile("fonts.jar");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(jar));
        try {
            out.putNextEntry(new ZipEntry("DejaVuLGCSerif.ttf"));
            Files.copy(FONT_FILE.toPath(), out);
            out.closeEntry();
        } finally {
            out.close();
        }
        URI fontURI = URI.create("jar:" + jar.toURI() + "!/DejaVuLGCSerif.ttf");
        CustomFontMetricsMapper mapper = setup(new CachedConfiguredFontCollection.Cache(),
                provide(createFontList(fontURI, false)), 1);
        assertFalse(mapper.isFileBacked());
        assertEquals(FAMILY, mapper.getFont(12000).getFamily());
    }

    @Test
    public void testMissingGlyphIsReportedToListenerOfRendering() {
        final List<Character> missing = new ArrayList<Character>();
        FontInfo fontInfo = new FontInfo();
        fontInfo.setEventListener(new FontEventListener() {
            public void fontSubstituted(Object source, FontTriplet requested, FontTriplet effective) {
            }

            public void fontLoadingErrorAtAutoDetection(Object source, String fontURL, Exception e) {
            }

            public void glyphNotAvailable(Object source, char ch, String fontName) {
                missing.add(ch);
            }

            public void fontDirectoryNotFound(Object source, String dir) {
            }

            public void svgTextStrokedAsShapes(Object source, String fontFamily) {
            }
        });
        new CachedConfiguredFontCollection(new CachedConfiguredFontCollection.Cache(), RESOLVER,
                provide(createFontList()), false).setup(1, fontInfo);
        Typeface font = fontInfo.getFonts().get("F1");
        font.mapChar('A');
        font.mapChar('\u4E00'); // CJK, not in DejaVu LGC
        assertEquals(Collections.singletonList('\u4E00'), missing);
    }

    /**
     * Several threads use a freshly loaded shared font at the same time, as rendering threads do
     * right after startup. Before the fix, glyph widths got mixed up in about every fifth round.
     */
    @Test
    public void testConcurrentFirstUseKeepsWidthsCorrect() throws Exception {
        List<EmbedFontInfo> fontList = createFontList();
        int[] expected = getWidths(loadSharedFont(fontList));
        int failedRounds = 0;
        for (int round = 0; round < 50; round++) {
            final CustomFont font = loadSharedFont(fontList);
            final CyclicBarrier start = new CyclicBarrier(THREADS);
            final AtomicInteger errors = new AtomicInteger();
            List<Thread> threads = new ArrayList<Thread>();
            for (int t = 0; t < THREADS; t++) {
                final int offset = t * 7;
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            start.await();
                            // every thread starts at a different character
                            for (int i = 0; i < TEXT.length(); i++) {
                                int c = (i + offset) % TEXT.length();
                                font.getWidth(font.mapChar(TEXT.charAt(c)), 1);
                            }
                        } catch (Exception e) {
                            errors.incrementAndGet();
                        }
                    }
                };
                threads.add(thread);
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
            int[] actual;
            try {
                actual = getWidths(font);
            } catch (RuntimeException e) {
                actual = null;
            }
            if (errors.get() > 0 || actual == null || !java.util.Arrays.equals(expected, actual)) {
                failedRounds++;
            }
        }
        assertEquals("rounds in which the shared font was corrupted", 0, failedRounds);
    }
}
