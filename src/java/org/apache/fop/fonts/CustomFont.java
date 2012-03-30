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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;


/**
 * Abstract base class for custom fonts loaded from files, for example.
 */
public abstract class CustomFont extends Typeface
            implements FontDescriptor, MutableFont {

    private String fontName = null;
    private String fullName = null;
    private Set<String> familyNames = null;
    private String fontSubName = null;
    private String embedFileName = null;
    private String embedResourceName = null;
    private FontResolver resolver = null;
    private EmbeddingMode embeddingMode = EmbeddingMode.AUTO;

    private int capHeight = 0;
    private int xHeight = 0;
    private int ascender = 0;
    private int descender = 0;
    private int[] fontBBox = {0, 0, 0, 0};
    private int flags = 4;
    private int weight = 0; //0 means unknown weight
    private int stemV = 0;
    private int italicAngle = 0;
    private int missingWidth = 0;
    private FontType fontType = FontType.TYPE1;
    private int firstChar = 0;
    private int lastChar = 255;

    private Map<Integer, Map<Integer, Integer>> kerning;

    private boolean useKerning = true;

    /** the character map, mapping Unicode ranges to glyph indices. */
    protected BFEntry[] cmap;

    /** {@inheritDoc} */
    public String getFontName() {
        return fontName;
    }

    /** {@inheritDoc} */
    public String getEmbedFontName() {
        return getFontName();
    }

    /** {@inheritDoc} */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the font family names.
     * @return the font family names (a Set of Strings)
     */
    public Set<String> getFamilyNames() {
        return Collections.unmodifiableSet(this.familyNames);
    }

    /**
     * Returns the font family name stripped of whitespace.
     * @return the stripped font family
     * @see FontUtil#stripWhiteSpace(String)
     */
    public String getStrippedFontName() {
        return FontUtil.stripWhiteSpace(getFontName());
    }

    /**
     * Returns font's subfamily name.
     * @return the font's subfamily name
     */
    public String getFontSubName() {
        return fontSubName;
    }

    /**
     * Returns an URI representing an embeddable font file. The URI will often
     * be a filename or an URL.
     * @return URI to an embeddable font file or null if not available.
     */
    public String getEmbedFileName() {
        return embedFileName;
    }

    /**
     * Returns the embedding mode for this font.
     * @return embedding mode
     */
    public EmbeddingMode getEmbeddingMode() {
        return embeddingMode;
    }

    /**
     * Returns a Source representing an embeddable font file.
     * @return Source for an embeddable font file
     * @throws IOException if embedFileName is not null but Source is not found
     */
    public Source getEmbedFileSource() throws IOException {
        Source result = null;
        if (resolver != null && embedFileName != null) {
            result = resolver.resolve(embedFileName);
            if (result == null) {
                throw new IOException("Unable to resolve Source '"
                        + embedFileName + "' for embedded font");
            }
        }
        return result;
    }

    /**
     * Returns the lookup name to an embeddable font file available as a
     * resource.
     * (todo) Remove this method, this should be done using a resource: URI.
     * @return the lookup name
     */
    public String getEmbedResourceName() {
        return embedResourceName;
    }

    /**
     * {@inheritDoc}
     */
    public int getAscender() {
        return ascender;
    }

    /**
     * {@inheritDoc}
     */
    public int getDescender() {
        return descender;
    }

    /**
     * {@inheritDoc}
     */
    public int getCapHeight() {
        return capHeight;
    }

    /**
     * {@inheritDoc}
     */
    public int getAscender(int size) {
        return size * ascender;
    }

    /**
     * {@inheritDoc}
     */
    public int getDescender(int size) {
        return size * descender;
    }

    /**
     * {@inheritDoc}
     */
    public int getCapHeight(int size) {
        return size * capHeight;
    }

    /**
     * {@inheritDoc}
     */
    public int getXHeight(int size) {
        return size * xHeight;
    }

    /**
     * {@inheritDoc}
     */
    public int[] getFontBBox() {
        return fontBBox;
    }

    /** {@inheritDoc} */
    public int getFlags() {
        return flags;
    }

    /** {@inheritDoc} */
    public boolean isSymbolicFont() {
        return ((getFlags() & 4) != 0) || "ZapfDingbatsEncoding".equals(getEncodingName());
        //Note: The check for ZapfDingbats is necessary as the PFM does not reliably indicate
        //if a font is symbolic.
    }

    /**
     * Returns the font weight (100, 200...800, 900). This value may be different from the
     * one that was actually used to register the font.
     * @return the font weight (or 0 if the font weight is unknown)
     */
    public int getWeight() {
        return this.weight;
    }

    /**
     * {@inheritDoc}
     */
    public int getStemV() {
        return stemV;
    }

    /**
     * {@inheritDoc}
     */
    public int getItalicAngle() {
        return italicAngle;
    }

    /**
     * Returns the width to be used when no width is available.
     * @return a character width
     */
    public int getMissingWidth() {
        return missingWidth;
    }

    /**
     * {@inheritDoc}
     */
    public FontType getFontType() {
        return fontType;
    }

    /**
     * Returns the index of the first character defined in this font.
     * @return the index of the first character
     */
    public int getFirstChar() {
        return firstChar;
    }

    /**
     * Returns the index of the last character defined in this font.
     * @return the index of the last character
     */
    public int getLastChar() {
        return lastChar;
    }

    /**MutableFont
     * Used to determine if kerning is enabled.
     * @return True if kerning is enabled.
     */
    public boolean isKerningEnabled() {
        return useKerning;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean hasKerningInfo() {
        return (isKerningEnabled() && (kerning != null) && !kerning.isEmpty());
    }

    /**
     * {@inheritDoc}
     */
    public final Map<Integer, Map<Integer, Integer>> getKerningInfo() {
        if (hasKerningInfo()) {
            return kerning;
        } else {
            return Collections.emptyMap();
        }
    }

    /* ---- MutableFont interface ---- */

    /** {@inheritDoc} */
    public void setFontName(String name) {
        this.fontName = name;
    }

    /** {@inheritDoc} */
    public void setFullName(String name) {
        this.fullName = name;
    }

    /** {@inheritDoc} */
    public void setFamilyNames(Set<String> names) {
        this.familyNames = new HashSet<String>(names);
    }

    /**
     * Sets the font's subfamily name.
     * @param subFamilyName the subfamily name of the font
     */
    public void setFontSubFamilyName(String subFamilyName) {
        this.fontSubName = subFamilyName;
    }

    /**
     * {@inheritDoc}
     */
    public void setEmbedFileName(String path) {
        this.embedFileName = path;
    }

    /**
     * {@inheritDoc}
     */
    public void setEmbedResourceName(String name) {
        this.embedResourceName = name;
    }

    /**
     * {@inheritDoc}
     */
    public void setEmbeddingMode(EmbeddingMode embeddingMode) {
        this.embeddingMode = embeddingMode;
    }

    /**
     * {@inheritDoc}
     */
    public void setCapHeight(int capHeight) {
        this.capHeight = capHeight;
    }

    /**
     * Returns the XHeight value of the font.
     * @param xHeight the XHeight value
     */
    public void setXHeight(int xHeight) {
        this.xHeight = xHeight;
    }

    /**
     * {@inheritDoc}
     */
    public void setAscender(int ascender) {
        this.ascender = ascender;
    }

    /**
     * {@inheritDoc}
     */
    public void setDescender(int descender) {
        this.descender = descender;
    }

    /**
     * {@inheritDoc}
     */
    public void setFontBBox(int[] bbox) {
        this.fontBBox = bbox;
    }

    /**
     * {@inheritDoc}
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Sets the font weight. Valid values are 100, 200...800, 900.
     * @param weight the font weight
     */
    public void setWeight(int weight) {
        weight = (weight / 100) * 100;
        weight = Math.max(100, weight);
        weight = Math.min(900, weight);
        this.weight = weight;
    }

    /**
     * {@inheritDoc}
     */
    public void setStemV(int stemV) {
        this.stemV = stemV;
    }

    /**
     * {@inheritDoc}
     */
    public void setItalicAngle(int italicAngle) {
        this.italicAngle = italicAngle;
    }

    /**
     * {@inheritDoc}
     */
    public void setMissingWidth(int width) {
        this.missingWidth = width;
    }

    /**
     * {@inheritDoc}
     */
    public void setFontType(FontType fontType) {
        this.fontType = fontType;
    }

    /**
     * {@inheritDoc}
     */
    public void setFirstChar(int index) {
        this.firstChar = index;
    }

    /**
     * {@inheritDoc}
     */
    public void setLastChar(int index) {
        this.lastChar = index;
    }

    /**
     * {@inheritDoc}
     */
    public void setKerningEnabled(boolean enabled) {
        this.useKerning = enabled;
    }

    /**
     * Sets the font resolver. Needed for URI resolution.
     * @param resolver the font resolver
     */
    public void setResolver(FontResolver resolver) {
        this.resolver = resolver;
    }

    /** {@inheritDoc} */
    public void putKerningEntry(Integer key, Map<Integer, Integer> value) {
        if (kerning == null) {
            kerning = new HashMap<Integer, Map<Integer, Integer>>();
        }
        this.kerning.put(key, value);
    }

    /**
     * Replaces the existing kerning map with a new one.
     * @param kerningMap the kerning map (Map<Integer, Map<Integer, Integer>, the integers are
     *                          character codes)
     */
    public void replaceKerningMap(Map<Integer, Map<Integer, Integer>> kerningMap) {
        if (kerningMap == null) {
            this.kerning = Collections.emptyMap();
        } else {
            this.kerning = kerningMap;
        }
    }

    /**
     * Sets the identity character map for this font. It maps all available Unicode characters
     * to their glyph indices inside the font.
     * @param cmap the identity character map
     */
    public void setCMap(BFEntry[] cmap) {
        this.cmap = new BFEntry[cmap.length];
        System.arraycopy(cmap, 0, this.cmap, 0, cmap.length);
    }

    /**
     * Returns the identity character map for this font. It maps all available Unicode characters
     * to their glyph indices inside the font.
     * @return the identity character map
     */
    public BFEntry[] getCMap() {
        BFEntry[] copy = new BFEntry[cmap.length];
        System.arraycopy(this.cmap, 0, copy, 0, this.cmap.length);
        return copy;
    }

}
