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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

// CSOFF: NoWhitespaceAfterCheck
// CSOFF: LineLengthCheck

/**
 * A GlyphSequence encapsulates a sequence of character codes, a sequence of glyph codes,
 * and a sequence of character associations, where, for each glyph in the sequence of glyph
 * codes, there is a corresponding character association. Character associations server to
 * relate the glyph codes in a glyph sequence to the specific characters in an original
 * character code sequence with which the glyph codes are associated.
 * @author Glenn Adams
 */
public class GlyphSequence implements CharSequence {

    private CharSequence characters;
    private CharSequence glyphs;
    private CharAssociation[] associations;

    /**
     * Instantiate a glyph sequence.
     * @param characters a (possibly empty) sequence of associated (originating) characters
     * @param sequences a (possibly empty) list of glyph sequences
     * @param associations a (possibly empty) list of glyph to character associations, one for each glyph in the concatenated glyph sequences
     * @param reverse a boolean indicating if the glyphs are in reverse order with respect to the nominal inline progression direction
     */
    public GlyphSequence ( CharSequence characters, List/*<GlyphSequence>*/ sequences, List/*<CharAssociation>*/ associations, boolean reverse ) {
        this ( characters, concatenateSequences ( sequences, reverse ), concatenateAssociations ( associations, reverse ) );
    }

    /**
     * Instantiate a glyph sequence.
     * @param characters a (possibly empty) sequence of associated (originating) characters
     * @param glyphs a (possibly empty) list of glyphs
     * @param associations a (possibly empty) list of glyph to character associations, one for each glyph in the concatenated glyph sequences
     */
    public GlyphSequence ( CharSequence characters, CharSequence glyphs, CharAssociation[] associations ) {
        if ( ( characters == null ) || ( glyphs == null ) ) {
            throw new IllegalArgumentException ( "characters and glyphs must be non-null" );
        } else if ( ( associations != null ) && ( associations.length != glyphs.length() ) ) {
            throw new IllegalArgumentException ( "number of associations must match number of glyphs" );
        } else {
            this.characters = characters;
            this.glyphs = glyphs;
            if ( associations == null ) {
                associations = makeIdentityAssociations ( characters, glyphs );
            }
            this.associations = associations;
        }
    }

    /** @return sequence of corresponding (originating) characters */
    public CharSequence getCharacters() {
        return characters;
    }

    /** @return sequence of glyphs in glyph sequence */
    public CharSequence getGlyphs() {
        return glyphs;
    }

    /** @return glyph to character associations, one for each glyph */
    public CharAssociation[] getAssociations() {
        return associations;
    }

    /**
     * Obtain the sequence of characters that corresponds to the glyph sequence at interval
     * [offset,offset+count).
     * @param offset to first glyph
     * @param count of glyphs
     * @return corresponding character sequence
     */
    public CharSequence getCharsForGlyphs ( int offset, int count ) throws DiscontinuousAssociationException {          // CSOK: JavadocMethodCheck
        int sFirst = -1, eLast = -1;
        for ( int i = 0, n = count; i < n; i++ ) {
            CharAssociation ca = associations [ offset + i ];
            int s = ca.getStart();
            int e = ca.getEnd();
            if ( sFirst < 0 ) {
                sFirst = s;
            }
            if ( eLast < 0 ) {
                eLast = e;
            } else if ( s == eLast ) {
                eLast = e;
            } else {
                throw new DiscontinuousAssociationException();
            }
        }
        return characters.subSequence ( sFirst, eLast );
    }

    /**
     * Obtain the glyph subsequence corresponding to the half-open interval [start,end).
     * @param start of subsequence
     * @param end of subsequence
     * @return a subsequence of this sequence
     */
    public GlyphSequence getGlyphSubsequence ( int start, int end ) {
        CharAssociation[] subset = new CharAssociation[end - start];
        System.arraycopy(associations, start, subset, 0, end - start);
        return new GlyphSequence ( characters, glyphs.subSequence ( start, end ), subset );
    }

    /** @return the number of glyphs in this glyph sequence */
    public int length() {
        return glyphs.length();
    }

    /**
     * Obtain glyph id at specified index.
     * @param index to obtain glyph
     * @return the glyph identifier of glyph at specified index
     */
    public char charAt ( int index ) {
        return glyphs.charAt ( index );
    }

    /**
     * Obtain glyph code subsequence over interval [start,end).
     * @param start of subsequence
     * @param end of subsequence
     * @return the glyph code subsequence
     */
    public CharSequence subSequence ( int start, int end ) {
        return glyphs.subSequence ( start, end );
    }

    /** {@inheritDoc} */
    public String toString() {
        return glyphs.toString();
    }

    private CharAssociation[] makeIdentityAssociations ( CharSequence characters, CharSequence glyphs ) {
        int nc = characters.length();
        int ng = glyphs.length();
        CharAssociation[] ca = new CharAssociation [ ng ];
        for ( int i = 0, n = ng; i < n; i++ ) {
            int k = ( i > nc ) ? nc : i;
            ca [ i ] = new CharAssociation ( i, ( k == nc ) ? 0 : 1 );
        }
        return ca;
    }

    private static CharSequence concatenateSequences ( List/*<GlyphSequence>*/ sequences, boolean reverse ) {
        int ng = 0;
        for ( Iterator it = sequences.iterator(); it.hasNext();) {
            GlyphSequence gs = (GlyphSequence) it.next();
            ng += gs.length();
        }
        CharBuffer cb = CharBuffer.allocate ( ng );
        if ( ! reverse ) {
            for ( ListIterator it = sequences.listIterator(); it.hasNext();) {
                GlyphSequence gs = (GlyphSequence) it.next();
                cb.append ( (CharSequence) gs );
            }
        } else {
            for ( ListIterator it = sequences.listIterator ( sequences.size() ); it.hasPrevious();) {
                GlyphSequence gs = (GlyphSequence) it.previous();
                cb.append ( (CharSequence) gs );
            }
        }
        cb.rewind();
        return cb;
    }

    private static CharAssociation[] concatenateAssociations ( List/*<CharAssociation>*/ associations, boolean reverse ) {
        int na = 0;
        CharAssociation[] ca = new CharAssociation [ associations.size() ];
        if ( ! reverse ) {
            for ( ListIterator it = associations.listIterator(); it.hasNext();) {
                CharAssociation a = (CharAssociation) it.next();
                ca [ na++ ] = a;
            }
        } else {
            for ( ListIterator it = associations.listIterator ( associations.size() ); it.hasPrevious();) {
                CharAssociation a = (CharAssociation) it.previous();
                ca [ na++ ] = a;
            }
        }
        return ca;
    }

    /**
     * A structure class encapsulating an interval of character codes (in a CharSequence)
     * expressed as an offset and count (of code elements in a CharSequence, i.e., numbere of
     * UTF-16 code elements. N.B. count does not necessarily designate the number of Unicode
     * scalar values expressed by the CharSequence; in particular, it does not do so if there
     * is one or more UTF-16 surrogate pairs present in the CharSequence.)
     */
    public static class CharAssociation {

        private final int offset;
        private final int count;

        /**
         * Instantiate a character association.
         * @param offset into array of UTF-16 code elements (in associated CharSequence)
         * @param count of UTF-16 character code elements (in associated CharSequence)
         */
        public CharAssociation ( int offset, int count ) {
            this.offset = offset;
            this.count = count;
        }

        /** @return offset (start of association interval) */
        public int getOffset() {
            return offset;
        }

        /** @return count (number of characer codes in association) */
        public int getCount() {
            return count;
        }

        /** @return start of association interval */
        public int getStart() {
            return getOffset();
        }

        /** @return end of association interval */
        public int getEnd() {
            return getOffset() + getCount();
        }

    }
}
