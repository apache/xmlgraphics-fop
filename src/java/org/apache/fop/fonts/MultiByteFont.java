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

import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.util.CharUtilities;

/**
 * Generic MultiByte (CID) font
 */
public class MultiByteFont extends CIDFont implements Substitutable, Positionable {

    /** logging instance */
    private static final Log log // CSOK: ConstantNameCheck
        = LogFactory.getLog(MultiByteFont.class);

    private static int uniqueCounter = -1;

    private String ttcName = null;
    private String encoding = "Identity-H";

    private int defaultWidth = 0;
    private CIDFontType cidType = CIDFontType.CIDTYPE2;

    private String namePrefix = null;    // Quasi unique prefix

    private CIDSubset subset = new CIDSubset();

    /** A map from Unicode indices to glyph indices */
    private BFEntry[] bfentries = null;

    /* advanced typographic support */
    private GlyphDefinitionTable gdef;
    private GlyphSubstitutionTable gsub;
    private GlyphPositioningTable gpos;

    /**
     * Default constructor
     */
    public MultiByteFont() {
        // Make sure that the 3 first glyphs are included
        subset.setupFirstThreeGlyphs();

        // Create a quasiunique prefix for fontname
        synchronized (this.getClass()) {
            uniqueCounter++;
            if (uniqueCounter > 99999 || uniqueCounter < 0) {
                uniqueCounter = 0; //We need maximum 5 character then we start again
            }
        }
        DecimalFormat counterFormat = new DecimalFormat("00000");
        String cntString = counterFormat.format(uniqueCounter);

        //Subset prefix as described in chapter 5.5.3 of PDF 1.4
        StringBuffer sb = new StringBuffer("E");
        for (int i = 0, c = cntString.length(); i < c; i++) {
            //translate numbers to uppercase characters
            sb.append((char)(cntString.charAt(i) + (65 - 48)));
        }
        sb.append("+");
        namePrefix = sb.toString();

        setFontType(FontType.TYPE0);
    }

    /** {@inheritDoc} */
    public int getDefaultWidth() {
        return defaultWidth;
    }

    /** {@inheritDoc} */
    public String getRegistry() {
        return "Adobe";
    }

    /** {@inheritDoc} */
    public String getOrdering() {
        return "UCS";
    }

    /** {@inheritDoc} */
    public int getSupplement() {
        return 0;
    }

    /** {@inheritDoc} */
    public CIDFontType getCIDType() {
        return cidType;
    }

    /**
     * Sets the CIDType.
     * @param cidType The cidType to set
     */
    public void setCIDType(CIDFontType cidType) {
        this.cidType = cidType;
    }

    private String getPrefixedFontName() {
        return namePrefix + FontUtil.stripWhiteSpace(super.getFontName());
    }

    /** {@inheritDoc} */
    public String getEmbedFontName() {
        if (isEmbeddable()) {
            return getPrefixedFontName();
        } else {
            return super.getFontName();
        }
    }

    /** {@inheritDoc} */
    public boolean isEmbeddable() {
        return !(getEmbedFileName() == null && getEmbedResourceName() == null);
    }

    /** {@inheritDoc} */
    public CIDSubset getCIDSubset() {
        return this.subset;
    }

    /** {@inheritDoc} */
    public String getEncodingName() {
        return encoding;
    }

    /** {@inheritDoc} */
    public int getWidth(int i, int size) {
        if (isEmbeddable()) {
            int glyphIndex = subset.getGlyphIndexForSubsetIndex(i);
            return size * width[glyphIndex];
        } else {
            return size * width[i];
        }
    }

    /** {@inheritDoc} */
    public int[] getWidths() {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length);
        return arr;
    }

    /**
     * Returns the glyph index for a Unicode character. The method returns 0 if there's no
     * such glyph in the character map.
     * @param c the Unicode character index
     * @return the glyph index (or 0 if the glyph is not available)
     */
    // [TBD] - needs optimization
    private int findGlyphIndex(int c) {
        int idx = c;
        int retIdx = SingleByteEncoding.NOT_FOUND_CODE_POINT;

        for (int i = 0; (i < bfentries.length) && retIdx == 0; i++) {
            if (bfentries[i].getUnicodeStart() <= idx
                    && bfentries[i].getUnicodeEnd() >= idx) {

                retIdx = bfentries[i].getGlyphStartIndex()
                    + idx
                    - bfentries[i].getUnicodeStart();
            }
        }
        return retIdx;
    }

    /**
     * Returns the Unicode scalar value that corresponds to the glyph index. If more than
     * one correspondence exists, then the first one is returned (ordered by bfentries[]).
     * If the glyph index is Typeface.NOT_FOUND, then returns the Unicode replacement
     * character (0x00FFFD).
     * @param gi glyph index
     * @returns unicode scalar value
     */
    // [TBD] - needs optimization
    private int findCharacterFromGlyphIndex ( int gi ) {
        int cc = 0;
        for ( int i = 0, n = bfentries.length; i < n; i++ ) {
            BFEntry be = bfentries [ i ];
            int s = be.getGlyphStartIndex();
            int e = s + ( be.getUnicodeEnd() - be.getUnicodeStart() );
            if ( ( gi >= s ) && ( gi <= e ) ) {
                cc = be.getUnicodeStart() + ( gi - s );
                break;
            }
        }
        return cc;
    }

    /** {@inheritDoc} */
    public char mapChar(char c) {
        notifyMapOperation();
        int glyphIndex = findGlyphIndex(c);
        if (glyphIndex == SingleByteEncoding.NOT_FOUND_CODE_POINT) {
            warnMissingGlyph(c);
            glyphIndex = findGlyphIndex(Typeface.NOT_FOUND);
        }
        if (isEmbeddable()) {
            glyphIndex = subset.mapSubsetChar(glyphIndex, c);
        }
        return (char)glyphIndex;
    }

    /** {@inheritDoc} */
    public boolean hasChar(char c) {
        return (findGlyphIndex(c) != SingleByteEncoding.NOT_FOUND_CODE_POINT);
    }

    /**
     * Sets the array of BFEntry instances which constitutes the Unicode to glyph index map for
     * a font. ("BF" means "base font")
     * @param entries the Unicode to glyph index map
     */
    public void setBFEntries(BFEntry[] entries) {
        this.bfentries = entries;
    }

    /**
     * Sets the defaultWidth.
     * @param defaultWidth The defaultWidth to set
     */
    public void setDefaultWidth(int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    /**
     * Returns the TrueType Collection Name.
     * @return the TrueType Collection Name
     */
    public String getTTCName() {
        return ttcName;
    }

    /**
     * Sets the the TrueType Collection Name.
     * @param ttcName the TrueType Collection Name
     */
    public void setTTCName(String ttcName) {
        this.ttcName = ttcName;
    }

    /**
     * Sets the width array.
     * @param wds array of widths.
     */
    public void setWidthArray(int[] wds) {
        this.width = wds;
    }

    /**
     * Returns a Map of used Glyphs.
     * @return Map Map of used Glyphs
     */
    public Map getUsedGlyphs() {
        return subset.getSubsetGlyphs();
    }

    /** @return an array of the chars used */
    public char[] getCharsUsed() {
        if (!isEmbeddable()) {
            return null;
        }
        return subset.getSubsetChars();
    }

    /**
     * Establishes the glyph definition table.
     * @param gdef the glyph definition table to be used by this font
     */
    public void setGDEF ( GlyphDefinitionTable gdef ) {
        if ( ( this.gdef == null ) || ( gdef == null ) ) {
            this.gdef = gdef;
        } else {
            throw new IllegalStateException ( "font already associated with GDEF table" );
        }
    }

    /**
     * Obtain glyph definition table.
     * @return glyph definition table or null if none is associated with font
     */
    public GlyphDefinitionTable getGDEF() {
        return gdef;
    }

    /**
     * Establishes the glyph substitution table.
     * @param gsub the glyph substitution table to be used by this font
     */
    public void setGSUB ( GlyphSubstitutionTable gsub ) {
        if ( ( this.gsub == null ) || ( gsub == null ) ) {
            this.gsub = gsub;
        } else {
            throw new IllegalStateException ( "font already associated with GSUB table" );
        }
    }

    /**
     * Obtain glyph substitution table.
     * @return glyph substitution table or null if none is associated with font
     */
    public GlyphSubstitutionTable getGSUB() {
        return gsub;
    }

    /**
     * Establishes the glyph positioning table.
     * @param gpos the glyph positioning table to be used by this font
     */
    public void setGPOS ( GlyphPositioningTable gpos ) {
        if ( ( this.gpos == null ) || ( gpos == null ) ) {
            this.gpos = gpos;
        } else {
            throw new IllegalStateException ( "font already associated with GPOS table" );
        }
    }

    /**
     * Obtain glyph positioning table.
     * @return glyph positioning table or null if none is associated with font
     */
    public GlyphPositioningTable getGPOS() {
        return gpos;
    }

    /** {@inheritDoc} */
    public boolean performsSubstitution() {
        return gsub != null;
    }

    /** {@inheritDoc} */
    public CharSequence performSubstitution ( CharSequence cs, String script, String language ) {
        if ( gsub != null ) {
            GlyphSequence igs = mapCharsToGlyphs ( cs );
            GlyphSequence ogs = gsub.substitute ( igs, script, language );
            CharSequence ocs = mapGlyphsToChars ( ogs );
            return ocs;
        } else {
            return cs;
        }
    }

    /** {@inheritDoc} */
    public boolean performsPositioning() {
        return gpos != null;
    }

    /** {@inheritDoc} */
    public int[][]
        performPositioning ( CharSequence cs, String script, String language, int fontSize ) {
        if ( gpos != null ) {
            GlyphSequence gs = mapCharsToGlyphs ( cs );
            int[][] adjustments = new int [ gs.getGlyphCount() ] [ 4 ];
            if ( gpos.position ( gs, script, language, fontSize, this.width, adjustments ) ) {
                return scaleAdjustments ( adjustments, fontSize );
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public int[][] performPositioning ( CharSequence cs, String script, String language ) {
        throw new UnsupportedOperationException();
    }


    private int[][] scaleAdjustments ( int[][] adjustments, int fontSize ) {
        if ( adjustments != null ) {
            for ( int i = 0, n = adjustments.length; i < n; i++ ) {
                int[] gpa = adjustments [ i ];
                for ( int k = 0; k < 4; k++ ) {
                    gpa [ k ] = ( gpa [ k ] * fontSize ) / 1000;
                }
            }
            return adjustments;
        } else {
            return null;
        }
    }

    /**
     * Map sequence CS, comprising a sequence of UTF-16 encoded Unicode Code Points, to
     * an output character sequence GS, comprising a sequence of Glyph Indices. N.B. Unlike
     * mapChar(), this method does not make use of embedded subset encodings.
     * @param cs a CharSequence containing UTF-16 encoded Unicode characters
     * @returns a CharSequence containing glyph indices
     */
    private GlyphSequence mapCharsToGlyphs ( CharSequence cs ) {
        IntBuffer cb = IntBuffer.allocate ( cs.length() );
        IntBuffer gb = IntBuffer.allocate ( cs.length() );
        int gi, giMissing = findGlyphIndex ( Typeface.NOT_FOUND );
        for ( int i = 0, n = cs.length(); i < n; i++ ) {
            int cc = cs.charAt ( i );
            if ( ( cc >= 0xD800 ) && ( cc < 0xDC00 ) ) {
                if ( ( i + 1 ) < n ) {
                    int sh = cc;
                    int sl = cs.charAt ( ++i );
                    if ( ( sl >= 0xDC00 ) && ( sl < 0xE000 ) ) {
                        cc = 0x10000 + ( ( sh - 0xD800 ) << 10 ) + ( ( sl - 0xDC00 ) << 0 );
                    } else {
                        throw new IllegalArgumentException
                            (  "ill-formed UTF-16 sequence, "
                               + "contains isolated high surrogate at index " + i );
                    }
                } else {
                    throw new IllegalArgumentException
                        ( "ill-formed UTF-16 sequence, "
                          + "contains isolated high surrogate at end of sequence" );
                }
            } else if ( ( cc >= 0xDC00 ) && ( cc < 0xE000 ) ) {
                throw new IllegalArgumentException
                    ( "ill-formed UTF-16 sequence, "
                      + "contains isolated low surrogate at index " + i );
            }
            gi = findGlyphIndex ( cc );
            if ( gi == SingleByteEncoding.NOT_FOUND_CODE_POINT ) {
                gi = giMissing;
            }
            cb.put ( cc );
            gb.put ( gi );
        }
        cb.flip();
        gb.flip();
        return new GlyphSequence ( cb, gb, null );
    }

    /**
     * Map sequence GS, comprising a sequence of Glyph Indices, to output sequence CS,
     * comprising a sequence of UTF-16 encoded Unicode Code Points.
     * @param gs a GlyphSequence containing glyph indices
     * @returns a CharSequence containing UTF-16 encoded Unicode characters
     */
    private CharSequence mapGlyphsToChars ( GlyphSequence gs ) {
        int ng = gs.getGlyphCount();
        CharBuffer cb = CharBuffer.allocate ( ng );
        int ccMissing = Typeface.NOT_FOUND;
        for ( int i = 0, n = ng; i < n; i++ ) {
            int gi = gs.getGlyph ( i );
            int cc = findCharacterFromGlyphIndex ( gi );
            if ( ( cc == 0 ) || ( cc > 0x10FFFF ) ) {
                cc = ccMissing;
                log.warn("Unable to map glyph index " + gi
                         + " to Unicode scalar in font '"
                         + getFullName() + "', substituting missing character '"
                         + (char) cc + "'");
            }
            if ( cc > 0x00FFFF ) {
                int sh, sl;
                cc -= 0x10000;
                sh = ( ( cc >> 10 ) & 0x3FF ) + 0xD800;
                sl = ( ( cc >>  0 ) & 0x3FF ) + 0xDC00;
                cb.put ( (char) sh );
                cb.put ( (char) sl );
            } else {
                cb.put ( (char) cc );
            }
        }
        cb.flip();
        return (CharSequence) cb;
    }

}

