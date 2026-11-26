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

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.complexscripts.fonts.Positionable;
import org.apache.fop.complexscripts.fonts.Substitutable;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontEventListener;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.FontUris;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;

/**
 * FontMetricsMapper that delegates most methods to an underlying
 * {@link org.apache.fop.fonts.FontMetrics} instance. This class was designed to allow
 * the underlying {@link Font} to be loaded from a
 * user-configured file not registered in the current graphics environment.
 */
public class CustomFontMetricsMapper extends Typeface implements FontMetricsMapper, Substitutable,
        Positionable {

    /**
     * Font metrics for the font this class models.
     */
    private Typeface typeface;
    private FontUris fontUris;
    private EmbedFontInfo configFontInfo;
    private InternalResourceResolver resourceResolver;
    private File fontFile;

    /**
     * The font required by the Java2D renderer.
     */
    private java.awt.Font font;

    /**
     * Maintains the most recently requested size.
     */
    private float size = 1;

    /**
     * True for a request-scoped copy whose typeface is shared between threads. The shared
     * typeface is silenced (see {@link #preload()}), so the copy reports missing glyphs to the
     * event listener of its own request.
     */
    private final boolean sharedTypeface;

    /** True if the AWT font reads the original font file, see {@link #isFileBacked()}. */
    private final boolean fileBacked;

    /** The number of glyphs of the AWT font when it was preloaded, see {@link #isReplacedByJava()}. */
    private int loadedGlyphCount;

    /** Ignores all events (set on the shared typeface, see {@link #preload()}). */
    private static final FontEventListener SILENT = new FontEventListener() {
        public void fontSubstituted(Object source, FontTriplet requested, FontTriplet effective) {
        }

        public void fontLoadingErrorAtAutoDetection(Object source, String fontURL, Exception e) {
        }

        public void glyphNotAvailable(Object source, char ch, String fontName) {
        }

        public void fontDirectoryNotFound(Object source, String dir) {
        }

        public void svgTextStrokedAsShapes(Object source, String fontFamily) {
        }
    };

    /**
     * Construction of this class results in the immediate construction
     * of the underlying {@link java.awt.Font}.
     * @param fontMetrics the metrics of the custom font
     * @throws FontFormatException if a bad font is loaded
     * @throws IOException if an I/O error occurs
     */
    public CustomFontMetricsMapper(final CustomFont fontMetrics)
            throws FontFormatException, IOException {
        this.typeface = fontMetrics;
        this.sharedTypeface = false;
        this.fileBacked = false;
        initialize(fontMetrics.getInputStream());
    }

    /**
     * Construction of this class results in the immediate construction of the underlying
     * {@link java.awt.Font}, which reads the given file directly (see {@link #isFileBacked()}).
     * @param fontMetrics the metrics of the custom font
     * @param fontFile the local font file the metrics were loaded from
     * @throws FontFormatException if a bad font is loaded
     * @throws IOException if an I/O error occurs
     */
    public CustomFontMetricsMapper(final CustomFont fontMetrics, final File fontFile)
            throws FontFormatException, IOException {
        this.typeface = fontMetrics;
        this.sharedTypeface = false;
        this.fileBacked = true;
        initialize(fontFile);
    }

    /**
     * Construction of this class results in the immediate construction
     * of the underlying {@link java.awt.Font}.
     * @param fontMetrics the font
     * @param fontSource the font source to access the font
     * @throws FontFormatException if a bad font is loaded
     * @throws IOException if an I/O error occurs
     */
    public CustomFontMetricsMapper(final LazyFont fontMetrics, final InputStream fontSource)
            throws FontFormatException, IOException {
        this.typeface = fontMetrics;
        this.sharedTypeface = false;
        this.fileBacked = false;
        initialize(fontSource);
    }

    /**
     * Construction of this class results in the immediate construction of the underlying
     * {@link java.awt.Font}, which reads the given file directly (see {@link #isFileBacked()}).
     * @param fontMetrics the font
     * @param fontFile the local font file
     * @throws FontFormatException if a bad font is loaded
     * @throws IOException if an I/O error occurs
     */
    public CustomFontMetricsMapper(final LazyFont fontMetrics, final File fontFile)
            throws FontFormatException, IOException {
        this.typeface = fontMetrics;
        this.sharedTypeface = false;
        this.fileBacked = true;
        initialize(fontFile);
    }

    public CustomFontMetricsMapper(FontUris fontUris, EmbedFontInfo configFontInfo,
                                   InternalResourceResolver resourceResolver) {
        this(fontUris, configFontInfo, resourceResolver, null);
    }

    /**
     * Like the constructor without a file, but the {@link java.awt.Font} reads the given local
     * font file directly when the font is loaded (see {@link #isFileBacked()}).
     * @param fontUris the URIs of the font and its metrics
     * @param configFontInfo the font configuration
     * @param resourceResolver the resource resolver
     * @param fontFile the local font file, or null to read the font from the resource resolver
     */
    public CustomFontMetricsMapper(FontUris fontUris, EmbedFontInfo configFontInfo,
                                   InternalResourceResolver resourceResolver, File fontFile) {
        this.fontUris = fontUris;
        this.configFontInfo = configFontInfo;
        this.resourceResolver = resourceResolver;
        this.fontFile = fontFile;
        this.sharedTypeface = false;
        this.fileBacked = fontFile != null;
    }

    /**
     * Creates a request-scoped copy that shares the (read-only) typeface and the loaded
     * {@link java.awt.Font} with the given mapper, but has its own mutable size state.
     * @param other the mapper to copy
     */
    CustomFontMetricsMapper(final CustomFontMetricsMapper other) {
        this.typeface = other.typeface;
        this.font = other.font;
        this.size = other.size;
        this.sharedTypeface = true;
        this.fileBacked = other.fileBacked;
        this.loadedGlyphCount = other.loadedGlyphCount;
    }

    /**
     * Tells whether the {@link java.awt.Font} reads the original font file. Otherwise Java reads the
     * font from a temporary copy in java.io.tmpdir; if that copy is deleted, Java silently draws
     * another font.
     * @return true if the font reads the original font file
     */
    boolean isFileBacked() {
        return fileBacked;
    }

    /**
     * Forces lazily loaded fonts and metrics to be loaded now (including fonts configured with
     * lazy-load), so that the typeface is read-only afterwards and can be shared between threads.
     * Silences its missing glyph warnings, which the request-scoped copies report instead.
     */
    void preload() {
        Typeface loaded = getRealFont();
        Typeface realFont = loaded instanceof LazyFont ? ((LazyFont) loaded).getRealFont() : loaded;
        realFont.setEventListener(SILENT);
        if (realFont instanceof CustomFont) {
            ((CustomFont) realFont).makeShareable();
        }
        loadedGlyphCount = font.getNumGlyphs();
    }

    /**
     * Tells whether Java has replaced the {@link java.awt.Font} of a preloaded mapper by its default
     * font, detected by a changed glyph count (a stored number: asking for the font name can read
     * the font file again).
     * @return true if the font no longer draws the font it was loaded from
     */
    boolean isReplacedByJava() {
        return font.getNumGlyphs() != loadedGlyphCount;
    }

    /** For testing purposes only: makes {@link #isReplacedByJava()} return true, as if Java had replaced the font. */
    void markReplacedByJava() {
        loadedGlyphCount = -1;
    }

    private void checkGlyph(char c) {
        // surrogates can't be checked one char at a time
        if (sharedTypeface && (c < 0xD800 || c > 0xDFFF) && !typeface.hasChar(c)) {
            warnMissingGlyph(c);
        }
    }

    private static final int TYPE1_FONT = 1; //Defined in Java 1.5

    /**
     * Loads the java.awt.Font
     * @param inStream
     * @throws FontFormatException
     * @throws IOException
     */
    private void initialize(final InputStream inStream)
                throws FontFormatException, IOException {
        this.font = Font.createFont(getAWTFontType(), inStream);
        inStream.close();
    }

    /**
     * Loads the java.awt.Font from the original file, without a temporary copy
     * @param file the font file
     * @throws FontFormatException
     * @throws IOException
     */
    private void initialize(final File file)
                throws FontFormatException, IOException {
        this.font = Font.createFont(getAWTFontType(), file);
    }

    private int getAWTFontType() {
        if (FontType.TYPE1.equals(getRealFont().getFontType())) {
            return TYPE1_FONT; //Font.TYPE1_FONT; only available in Java 1.5
        }
        return Font.TRUETYPE_FONT;
    }

    /** {@inheritDoc} */
    public final String getEncodingName() {
        return null; //Not applicable to Java2D rendering
    }

    /** {@inheritDoc} */
    public final boolean hasChar(final char c) {
        getRealFont();
        return font.canDisplay(c);
    }

    /** {@inheritDoc} */
    public final char mapChar(final char c) {
        checkGlyph(c);
        return getRealFont().mapChar(c);
    }

    /** {@inheritDoc} */
    public final Font getFont(final int size) {
        getRealFont();
        if (this.size == size / 1000f) {
            return font;
        }

        this.size = size / 1000f;
        font = font.deriveFont(this.size);
        return font;
    }

    /** {@inheritDoc} */
    public final int getAscender(final int size) {
        return getRealFont().getAscender(size);
    }

    /** {@inheritDoc} */
    public final int getCapHeight(final int size) {
        return getRealFont().getCapHeight(size);
    }

    /** {@inheritDoc} */
    public final int getDescender(final int size) {
        return getRealFont().getDescender(size);
    }

    /** {@inheritDoc} */
    public final String getEmbedFontName() {
        return getRealFont().getEmbedFontName();
    }

    /** {@inheritDoc} */
    public final Set<String> getFamilyNames() {
        return getRealFont().getFamilyNames();
    }

    /** {@inheritDoc} */
    public final String getFontName() {
        return getRealFont().getFontName();
    }

    /** {@inheritDoc} */
    public final URI getFontURI() {
        return getRealFont().getFontURI();
    }

    /** {@inheritDoc} */
    public final FontType getFontType() {
        return getRealFont().getFontType();
    }

    /** {@inheritDoc} */
    public final String getFullName() {
        return getRealFont().getFullName();
    }

    /** {@inheritDoc} */
    public final Map getKerningInfo() {
        return getRealFont().getKerningInfo();
    }

    /** {@inheritDoc} */
    public final int getWidth(final int i, final int size) {
        return getRealFont().getWidth(i, size);
    }

    /** {@inheritDoc} */
    public final int[] getWidths() {
        return getRealFont().getWidths();
    }

    public Rectangle getBoundingBox(int glyphIndex, int size) {
        return getRealFont().getBoundingBox(glyphIndex, size);
    }

    /** {@inheritDoc} */
    public final int getXHeight(final int size) {
        return getRealFont().getXHeight(size);
    }

    public int getUnderlinePosition(int size) {
        return getRealFont().getUnderlinePosition(size);
    }

    public int getUnderlineThickness(int size) {
        return getRealFont().getUnderlineThickness(size);
    }

    public int getStrikeoutPosition(int size) {
        return getRealFont().getStrikeoutPosition(size);
    }

    public int getStrikeoutThickness(int size) {
        return getRealFont().getStrikeoutThickness(size);
    }

    /** {@inheritDoc} */
    public final boolean hasKerningInfo() {
        return getRealFont().hasKerningInfo();
    }

    /** {@inheritDoc} */
    public boolean isMultiByte() {
        return getRealFont().isMultiByte();
    }

    /**
     * {@inheritDoc}
     */
    public boolean performsPositioning() {
        if (getRealFont() instanceof Positionable) {
            return ((Positionable) typeface).performsPositioning();
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[][] performPositioning(CharSequence cs, String script, String language, int fontSize) {
        if (getRealFont() instanceof Positionable) {
            return ((Positionable) typeface).performPositioning(cs, script, language, fontSize);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[][] performPositioning(CharSequence cs, String script, String language) {
        if (getRealFont() instanceof Positionable) {
            return ((Positionable) typeface).performPositioning(cs, script, language);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean performsSubstitution() {
        if (getRealFont() instanceof Substitutable) {
            return ((Substitutable) typeface).performsSubstitution();
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public CharSequence performSubstitution(CharSequence cs, String script, String language, List associations,
                                            boolean retainControls) {
        for (int i = 0; i < cs.length(); i++) {
            // the substituted characters no longer show which ones were missing
            checkGlyph(cs.charAt(i));
        }
        if (getRealFont() instanceof Substitutable) {
            return ((Substitutable) typeface).performSubstitution(cs, script, language, associations, retainControls);
        } else {
            return cs;
        }
    }

    /**
     * {@inheritDoc}
     */
    public CharSequence reorderCombiningMarks(CharSequence cs, int[][] gpa,
        String script, String language, List associations) {
        if (getRealFont() instanceof Substitutable) {
            return ((Substitutable) typeface).reorderCombiningMarks(cs, gpa, script, language, associations);
        } else {
            return cs;
        }
    }

    public Typeface getRealFont() {
        if (typeface == null) {
            try {
                CustomFont fontMetrics = FontLoader.loadFont(fontUris, configFontInfo.getSubFontName(), true,
                    configFontInfo.getEmbeddingMode(), configFontInfo.getEncodingMode(),
                    configFontInfo.getKerning(), configFontInfo.getAdvanced(), resourceResolver,
                    configFontInfo.getSimulateStyle(), configFontInfo.getEmbedAsType1(), configFontInfo.getUseSVG());
                typeface = fontMetrics;
                if (fontFile != null) {
                    initialize(fontFile);
                } else {
                    initialize(fontMetrics.getInputStream());
                }
            } catch (IOException | FontFormatException e) {
                throw new RuntimeException("Failed to read font file " + fontUris.getEmbed() + " " + e.getMessage(), e);
            }
        }
        return typeface;
    }
}
