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
import java.io.Serializable;
import java.util.List;

/**
 * FontInfo contains meta information on fonts (where is the metrics file etc.)
 * TODO: We need to remove this class and think about more intelligent design patterns
 * (Data classes => Procedural code)
 */
public class EmbedFontInfo implements Serializable {

    /** Serialization Version UID */
    private static final long serialVersionUID = 8755432068669997368L;

    /** filename of the metrics file */
    protected String metricsFile;
    /** filename of the main font file */
    protected String embedFile;
    /** false, to disable kerning */
    protected boolean kerning;
    /** the requested encoding mode for the font */
    protected EncodingMode encodingMode = EncodingMode.AUTO;
    /** the requested embedding mode for this font */
    protected EmbeddingMode embeddingMode = EmbeddingMode.AUTO;

    /** the PostScript name of the font */
    protected String postScriptName = null;
    /** the sub-fontname of the font (used for TrueType Collections, null otherwise) */
    protected String subFontName = null;

    /** the list of associated font triplets */
    private List<FontTriplet> fontTriplets = null;

    private transient boolean embedded = true;

    /**
     * Main constructor
     * @param metricsFile Path to the xml file containing font metrics
     * @param kerning True if kerning should be enabled
     * @param fontTriplets List of font triplets to associate with this font
     * @param embedFile Path to the embeddable font file (may be null)
     * @param subFontName the sub-fontname used for TrueType Collections (null otherwise)
     */
    public EmbedFontInfo(String metricsFile, boolean kerning,
                    List<FontTriplet> fontTriplets, String embedFile, String subFontName) {
        this.metricsFile = metricsFile;
        this.embedFile = embedFile;
        this.kerning = kerning;
        this.fontTriplets = fontTriplets;
        this.subFontName = subFontName;
    }

    /**
     * Returns the path to the metrics file
     * @return the metrics file path
     */
    public String getMetricsFile() {
        return metricsFile;
    }

    /**
     * Returns the path to the embeddable font file
     * @return the font file path
     */
    public String getEmbedFile() {
        return embedFile;
    }

    /**
     * Determines if kerning is enabled
     * @return True if enabled
     */
    public boolean getKerning() {
        return kerning;
    }

    /**
     * Returns the sub-font name of the font. This is primarily used for TrueType Collections
     * to select one of the sub-fonts. For all other fonts, this is always null.
     * @return the sub-font name (or null)
     */
    public String getSubFontName() {
        return this.subFontName;
    }

    /**
     * Returns the PostScript name of the font.
     * @return the PostScript name
     */
    public String getPostScriptName() {
        return postScriptName;
    }

    /**
     * Sets the PostScript name of the font
     * @param postScriptName the PostScript name
     */
    public void setPostScriptName(String postScriptName) {
        this.postScriptName = postScriptName;
    }

    /**
     * Returns the list of font triplets associated with this font.
     * @return List of font triplets
     */
    public List<FontTriplet> getFontTriplets() {
        return fontTriplets;
    }

    /**
     * Indicates whether the font is only referenced rather than embedded.
     * @return true if the font is embedded, false if it is referenced.
     */
    public boolean isEmbedded() {
        if (metricsFile != null && embedFile == null) {
            return false;
        } else {
            return this.embedded;
        }
    }

    /**
     * Returns the embedding mode for this font.
     * @return the embedding mode.
     */
    public EmbeddingMode getEmbeddingMode() {
       return embeddingMode;
    }

    /**
     * Defines whether the font is embedded or not.
     * @param value true to embed the font, false to reference it
     */
    public void setEmbedded(boolean value) {
        this.embedded = value;
    }

    /**
     * Returns the requested encoding mode for this font.
     * @return the encoding mode
     */
    public EncodingMode getEncodingMode() {
        return this.encodingMode;
    }

    /**
     * Sets the requested encoding mode for this font.
     * @param mode the new encoding mode
     */
    public void setEncodingMode(EncodingMode mode) {
        if (mode == null) {
            throw new NullPointerException("mode must not be null");
        }
        this.encodingMode = mode;
    }

    /**
     * Sets the embedding mode for this font, currently not supported for type1 fonts.
     * @param embeddingMode the new embedding mode.
     */
    public void setEmbeddingMode(EmbeddingMode embeddingMode) {
        if (embeddingMode == null) {
            throw new NullPointerException("embeddingMode must not be null");
        }
        this.embeddingMode = embeddingMode;
    }

    private void readObject(java.io.ObjectInputStream in)
                throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.embedded = true;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "metrics-url=" + metricsFile + ", embed-url=" + embedFile
            + ", kerning=" + kerning
            + ", enc-mode=" + encodingMode
            + ", font-triplet=" + fontTriplets
            + (getSubFontName() != null ? ", sub-font=" + getSubFontName() : "")
            + (isEmbedded() ? "" : ", NOT embedded");
    }

}
