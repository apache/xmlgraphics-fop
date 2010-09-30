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

import java.nio.IntBuffer;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.util.CharUtilities;

// CSOFF: InnerAssignmentCheck
// CSOFF: LineLengthCheck
// CSOFF: NoWhitespaceAfterCheck

/**
 * A GlyphSequence encapsulates a sequence of character codes, a sequence of glyph codes,
 * and a sequence of character associations, where, for each glyph in the sequence of glyph
 * codes, there is a corresponding character association. Character associations server to
 * relate the glyph codes in a glyph sequence to the specific characters in an original
 * character code sequence with which the glyph codes are associated.
 * @author Glenn Adams
 */
public class GlyphSequence implements Cloneable {

    /** default character buffer capacity in case new character buffer is created */
    private static final int DEFAULT_CHARS_CAPACITY = 8;

    /** character buffer */
    private IntBuffer characters;
    /** glyph buffer */
    private IntBuffer glyphs;
    /** association list */
    private List associations;

    /**
     * Instantiate a glyph sequence, reusing (i.e., not copying) the referenced
     * character and glyph buffers and associations. If characters is null, then
     * an empty character buffer is created. If glyphs is null, then a glyph buffer
     * is created whose capacity is that of the character buffer. If associations is
     * null, then identity associations are created.
     * @param characters a (possibly null) buffer of associated (originating) characters
     * @param glyphs a (possibly null) buffer of glyphs
     * @param associations a (possibly null) array of glyph to character associations
     */
    public GlyphSequence ( IntBuffer characters, IntBuffer glyphs, List associations ) {
        if ( characters == null ) {
            characters = IntBuffer.allocate ( DEFAULT_CHARS_CAPACITY );
        }
        if ( glyphs == null ) {
            glyphs = IntBuffer.allocate ( characters.capacity() );
        }
        if ( associations == null ) {
            associations = makeIdentityAssociations ( characters.limit(), glyphs.limit() );
        }
        this.characters = characters;
        this.glyphs = glyphs;
        this.associations = associations;
    }

    /**
     * Instantiate a glyph sequence using an existing glyph sequence, where the new glyph sequence shares
     * the character array of the existing sequence (but not the buffer object), and creates new copies
     * of glyphs buffer and association list.
     * @param gs an existing glyph sequence
     */
    public GlyphSequence ( GlyphSequence gs ) {
        this ( gs.characters.duplicate(), copyBuffer ( gs.glyphs ), copyAssociations ( gs.associations ) );
    }

    /**
     * Instantiate a glyph sequence using an existing glyph sequence, where the new glyph sequence shares
     * the character array of the existing sequence (but not the buffer object), but uses the specified
     * backtrack, input, and lookahead glyph arrays to populate the glyphs, and uses the specified
     * of glyphs buffer and association list.
     * backtrack, input, and lookahead association arrays to populate the associations.
     * @param gs an existing glyph sequence
     * @param bga backtrack glyph array
     * @param iga input glyph array
     * @param lga lookahead glyph array
     * @param bal backtrack association list
     * @param ial input association list
     * @param lal lookahead association list
     */
    public GlyphSequence ( GlyphSequence gs, int[] bga, int[] iga, int[] lga, CharAssociation[] bal, CharAssociation[] ial, CharAssociation[] lal ) {
        this ( gs.characters.duplicate(), concatGlyphs ( bga, iga, lga ), concatAssociations ( bal, ial, lal ) );
    }

    /**
     * Obtain reference to underlying character buffer.
     * @return character buffer reference
     */
    public IntBuffer getCharacters() {
        return characters;
    }

    /**
     * Obtain array of characters. If <code>copy</code> is true, then
     * a newly instantiated array is returned, otherwise a reference to
     * the underlying buffer's array is returned. N.B. in case a reference
     * to the undelying buffer's array is returned, the length
     * of the array is not necessarily the number of characters in array.
     * To determine the number of characters, use {@link #getCharacterCount}.
     * @param copy true if to return a newly instantiated array of characters
     * @return array of characters
     */
    public int[] getCharacterArray ( boolean copy ) {
        if ( copy ) {
            return toArray ( characters );
        } else {
            return characters.array();
        }
    }

    /**
     * Obtain the number of characters in character array, where
     * each character constitutes a unicode scalar value.
     * @return number of characters available in character array
     */
    public int getCharacterCount() {
        return characters.limit();
    }

    /**
     * Obtain glyph id at specified index.
     * @param index to obtain glyph
     * @return the glyph identifier of glyph at specified index
     * @throws IndexOutOfBoundsException if index is less than zero
     * or exceeds last valid position
     */
    public int getGlyph ( int index ) throws IndexOutOfBoundsException {
        return glyphs.get ( index );
    }

    /**
     * Set glyph id at specified index.
     * @param index to set glyph
     * @param gi glyph index
     * @throws IndexOutOfBoundsException if index is greater or equal to
     * the limit of the underlying glyph buffer
     */
    public void setGlyph ( int index, int gi ) throws IndexOutOfBoundsException {
        if ( gi > 65535 ) {
            gi = 65535;
        }
        glyphs.put ( index, gi );
    }

    /**
     * Obtain reference to underlying glyph buffer.
     * @return glyph buffer reference
     */
    public IntBuffer getGlyphs() {
        return glyphs;
    }

    /**
     * Obtain count glyphs starting at offset. If <code>count</code> is
     * negative, then it is treated as if the number of available glyphs
     * were specified.
     * @param offset into glyph sequence
     * @param count of glyphs to obtain starting at offset, or negative,
     * indicating all avaialble glyphs starting at offset
     * @return glyph array
     */
    public int[] getGlyphs ( int offset, int count ) {
        int ng = getGlyphCount();
        if ( offset < 0 ) {
            offset = 0;
        } else if ( offset > ng ) {
            offset = ng;
        }
        if ( count < 0 ) {
            count = ng - offset;
        }
        int[] ga = new int [ count ];
        for ( int i = offset, n = offset + count, k = 0; i < n; i++ ) {
            if ( k < ga.length ) {
                ga [ k++ ] = glyphs.get ( i );
            }
        }
        return ga;
    }

    /**
     * Obtain array of glyphs. If <code>copy</code> is true, then
     * a newly instantiated array is returned, otherwise a reference to
     * the underlying buffer's array is returned. N.B. in case a reference
     * to the undelying buffer's array is returned, the length
     * of the array is not necessarily the number of glyphs in array.
     * To determine the number of glyphs, use {@link #getGlyphCount}.
     * @param copy true if to return a newly instantiated array of glyphs
     * @return array of glyphs
     */
    public int[] getGlyphArray ( boolean copy ) {
        if ( copy ) {
            return toArray ( glyphs );
        } else {
            return glyphs.array();
        }
    }

    /**
     * Obtain the number of glyphs in glyphs array, where
     * each glyph constitutes a font specific glyph index.
     * @return number of glyphs available in character array
     */
    public int getGlyphCount() {
        return glyphs.limit();
    }

    /**
     * Obtain association at specified index.
     * @param index into associations array
     * @return glyph to character associations at specified index
     * @throws IndexOutOfBoundsException if index is less than zero
     * or exceeds last valid position
     */
    public CharAssociation getAssociation ( int index ) throws IndexOutOfBoundsException {
        return (CharAssociation) associations.get ( index );
    }

    /**
     * Obtain reference to underlying associations list.
     * @return associations list
     */
    public List getAssociations() {
        return associations;
    }

    /**
     * Obtain count associations starting at offset.
     * @param offset into glyph sequence
     * @param count of associations to obtain starting at offset, or negative,
     * indicating all avaialble associations starting at offset
     * @return associations
     */
    public CharAssociation[] getAssociations ( int offset, int count ) {
        int ng = getGlyphCount();
        if ( offset < 0 ) {
            offset = 0;
        } else if ( offset > ng ) {
            offset = ng;
        }
        if ( count < 0 ) {
            count = ng - offset;
        }
        CharAssociation[] aa = new CharAssociation [ count ];
        for ( int i = offset, n = offset + count, k = 0; i < n; i++ ) {
            if ( k < aa.length ) {
                aa [ k++ ] = (CharAssociation) associations.get ( i );
            }
        }
        return aa;
    }

    /**
     * Compare glyphs.
     * @param gb buffer containing glyph indices with which this glyph sequence's glyphs are to be compared
     * @return zero if glyphs are the same, otherwise returns 1 or -1 according to whether this glyph sequence's
     * glyphs are lexicographically greater or lesser than the glyphs in the specified string buffer
     */
    public int compareGlyphs ( IntBuffer gb ) {
        int ng = getGlyphCount();
        for ( int i = 0, n = gb.limit(); i < n; i++ ) {
            if ( i < ng ) {
                int g1 = glyphs.get ( i );
                int g2 = gb.get ( i );
                if ( g1 > g2 ) {
                    return 1;
                } else if ( g1 < g2 ) {
                    return -1;
                }
            } else {
                return -1;              // this gb is a proper prefix of specified gb
            }
        }
        return 0;                       // same lengths with no difference
    }

    /** {@inheritDoc} */
    public Object clone() {
        try {
            GlyphSequence gs = (GlyphSequence) super.clone();
            gs.characters = copyBuffer ( characters );
            gs.glyphs = copyBuffer ( glyphs );
            gs.associations = copyAssociations ( associations );
            return gs;
        } catch ( CloneNotSupportedException e ) {
            return null;
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append ( '{' );
        sb.append ( "chars = [" );
        sb.append ( characters );
        sb.append ( "], glyphs = [" );
        sb.append ( glyphs );
        sb.append ( "], associations = [" );
        sb.append ( associations );
        sb.append ( "]" );
        sb.append ( '}' );
        return sb.toString();
    }

    /**
     * Determine if two arrays of glyphs are identical.
     * @param ga1 first glyph array
     * @param ga2 second glyph array
     * @return true if arrays are botth null or both non-null and have identical elements
     */
    public static boolean sameGlyphs ( int[] ga1, int[] ga2 ) {
        if ( ga1 == ga2 ) {
            return true;
        } else if ( ( ga1 == null ) || ( ga2 == null ) ) {
            return false;
        } else if ( ga1.length != ga2.length ) {
            return false;
        } else {
            for ( int i = 0, n = ga1.length; i < n; i++ ) {
                if ( ga1[i] != ga2[i] ) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Concatenante glyph arrays.
     * @param bga backtrack glyph array
     * @param iga input glyph array
     * @param lga lookahead glyph array
     * @return new integer buffer containing concatenated glyphs
     */
    public static IntBuffer concatGlyphs ( int[] bga, int[] iga, int[] lga ) {
        int ng = 0;
        if ( bga != null ) {
            ng += bga.length;
        }
        if ( iga != null ) {
            ng += iga.length;
        }
        if ( lga != null ) {
            ng += lga.length;
        }
        IntBuffer gb = IntBuffer.allocate ( ng );
        if ( bga != null ) {
            gb.put ( bga );
        }
        if ( iga != null ) {
            gb.put ( iga );
        }
        if ( lga != null ) {
            gb.put ( lga );
        }
        gb.flip();
        return gb;
    }

    /**
     * Concatenante association arrays.
     * @param baa backtrack association array
     * @param iaa input association array
     * @param laa lookahead association array
     * @return new list containing concatenated associations
     */
    public static List concatAssociations ( CharAssociation[] baa, CharAssociation[] iaa, CharAssociation[] laa ) {
        int na = 0;
        if ( baa != null ) {
            na += baa.length;
        }
        if ( iaa != null ) {
            na += iaa.length;
        }
        if ( laa != null ) {
            na += laa.length;
        }
        if ( na > 0 ) {
            List gl = new ArrayList ( na );
            if ( baa != null ) {
                for ( int i = 0; i < baa.length; i++ ) {
                    gl.add ( baa[i] );
                }
            }
            if ( iaa != null ) {
                for ( int i = 0; i < iaa.length; i++ ) {
                    gl.add ( iaa[i] );
                }
            }
            if ( laa != null ) {
                for ( int i = 0; i < laa.length; i++ ) {
                    gl.add ( laa[i] );
                }
            }
            return gl;
        } else {
            return null;
        }
    }

    private static int[] toArray ( IntBuffer ib ) {
        if ( ib != null ) {
            int n = ib.limit();
            int[] ia = new int[n];
            ib.get ( ia, 0, n );
            return ia;
        } else {
            return new int[0];
        }
    }

    private static List makeIdentityAssociations ( int numChars, int numGlyphs ) {
        int nc = numChars;
        int ng = numGlyphs;
        List av = new ArrayList ( ng );
        for ( int i = 0, n = ng; i < n; i++ ) {
            int k = ( i > nc ) ? nc : i;
            av.add ( new CharAssociation ( i, ( k == nc ) ? 0 : 1 ) );
        }
        return av;
    }

    private static IntBuffer copyBuffer ( IntBuffer ib ) {
        if ( ib != null ) {
            int[] ia = new int [ ib.capacity() ];
            int   p  = ib.position();
            int   l  = ib.limit();
            System.arraycopy ( ib.array(), 0, ia, 0, ia.length );
            return IntBuffer.wrap ( ia, p, l - p );
        } else {
            return null;
        }
    }

    private static List copyAssociations ( List ca ) {
        if ( ca != null ) {
            return new ArrayList ( ca );
        } else {
            return ca;
        }
    }

    /**
     * A structure class encapsulating an interval of character codes (in a CharSequence)
     * expressed as an offset and count (of code elements in a CharSequence, i.e., number of
     * UTF-16 code elements. N.B. count does not necessarily designate the number of Unicode
     * scalar values expressed by the CharSequence; in particular, it does not do so if there
     * is one or more UTF-16 surrogate pairs present in the CharSequence.)
     */
    public static class CharAssociation implements Cloneable {

        private final int offset;
        private final int count;
        private final int[] subIntervals;

        /**
         * Instantiate a character association.
         * @param offset into array of UTF-16 code elements (in associated CharSequence)
         * @param count of UTF-16 character code elements (in associated CharSequence)
         * @param subIntervals if disjoint, then array of sub-intervals, otherwise null; even
         * members of array are sub-interval starts, and odd members are sub-interval
         * ends (exclusive)
         */
        public CharAssociation ( int offset, int count, int[] subIntervals ) {
            this.offset = offset;
            this.count = count;
            this.subIntervals = ( ( subIntervals != null ) && ( subIntervals.length > 2 ) ) ? subIntervals : null;
        }

        /**
         * Instantiate a non-disjoint character association.
         * @param offset into array of UTF-16 code elements (in associated CharSequence)
         * @param count of UTF-16 character code elements (in associated CharSequence)
         */
        public CharAssociation ( int offset, int count ) {
            this ( offset, count, null );
        }

        /**
         * Instantiate a non-disjoint character association.
         * @param subIntervals if disjoint, then array of sub-intervals, otherwise null; even
         * members of array are sub-interval starts, and odd members are sub-interval
         * ends (exclusive)
         */
        public CharAssociation ( int[] subIntervals ) {
            this ( getSubIntervalsStart ( subIntervals ), getSubIntervalsLength ( subIntervals ), subIntervals );
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

        /** @return true if association is disjoint */
        public boolean isDisjoint() {
            return subIntervals != null;
        }

        /** @return subintervals of disjoint association */
        public int[] getSubIntervals() {
            return subIntervals;
        }

        /** @return count of subintervals of disjoint association */
        public int getSubIntervalCount() {
            return ( subIntervals != null ) ? ( subIntervals.length / 2 ) : 0;
        }

        /** {@inheritDoc} */
        public Object clone() {
            try {
                return super.clone();
            } catch ( CloneNotSupportedException e ) {
                return null;
            }
        }

        /**
         * Replicate association to form <code>repeat</code> new associations.
         * @param a association to replicate
         * @param repeat count
         * @return array of replicated associations
         */
        public static CharAssociation[] replicate ( CharAssociation a, int repeat ) {
            CharAssociation[] aa = new CharAssociation [ repeat ];
            for ( int i = 0, n = aa.length; i < n; i++ ) {
                aa [ i ] = (CharAssociation) a.clone();
            }
            return aa;
        }

        /**
         * Join (merge) multiple associations into a single, potentially disjoint
         * association.
         * @param aa array of associations to join
         * @return (possibly disjoint) association containing joined associations
         */
        public static CharAssociation join ( CharAssociation[] aa ) {
            // extract sorted intervals
            int[] ia = extractIntervals ( aa );
            if ( ( ia == null ) || ( ia.length == 0 ) ) {
                return new CharAssociation ( 0, 0 );
            } else if ( ia.length == 2 ) {
                int s = ia[0];
                int e = ia[1];
                return new CharAssociation ( s, e - s );
            } else {
                return new CharAssociation ( mergeIntervals ( ia ) );
            }
        }

        private static int getSubIntervalsStart ( int[] ia ) {
            int us = Integer.MAX_VALUE;
            int ue = Integer.MIN_VALUE;
            if ( ia != null ) {
                for ( int i = 0, n = ia.length; i < n; i += 2 ) {
                    int s = ia [ i + 0 ];
                    int e = ia [ i + 1 ];
                    if ( s < us ) {
                        us = s;
                    }
                    if ( e > ue ) {
                        ue = e;
                    }
                }
                if ( ue < 0 ) {
                    ue = 0;
                }
                if ( us > ue ) {
                    us = ue;
                }
            }
            return us;
        }

        private static int getSubIntervalsLength ( int[] ia ) {
            int us = Integer.MAX_VALUE;
            int ue = Integer.MIN_VALUE;
            if ( ia != null ) {
                for ( int i = 0, n = ia.length; i < n; i += 2 ) {
                    int s = ia [ i + 0 ];
                    int e = ia [ i + 1 ];
                    if ( s < us ) {
                        us = s;
                    }
                    if ( e > ue ) {
                        ue = e;
                    }
                }
                if ( ue < 0 ) {
                    ue = 0;
                }
                if ( us > ue ) {
                    us = ue;
                }
            }
            return ue - us;
        }

        /**
         * Extract sorted sub-intervals.
         */
        private static int[] extractIntervals ( CharAssociation[] aa ) {
            int ni = 0;
            for ( int i = 0, n = aa.length; i < n; i++ ) {
                CharAssociation a = aa [ i ];
                if ( a.isDisjoint() ) {
                    ni += a.getSubIntervalCount();
                } else {
                    ni += 1;
                }
            }
            int[] sa = new int [ ni ];
            int[] ea = new int [ ni ];
            for ( int i = 0, k = 0; i < aa.length; i++ ) {
                CharAssociation a = aa [ i ];
                if ( a.isDisjoint() ) {
                    int[] da = a.getSubIntervals();
                    for ( int j = 0; j < da.length; j += 2 ) {
                        sa [ k ] = da [ j + 0 ];
                        ea [ k ] = da [ j + 1 ];
                        k++;
                    }
                } else {
                    sa [ k ] = a.getStart();
                    ea [ k ] = a.getEnd();
                    k++;
                }
            }
            return sortIntervals ( sa, ea );
        }

        private static final int[] sortIncrements16                                                             // CSOK: ConstantNameCheck
            = { 1391376, 463792, 198768, 86961, 33936, 13776, 4592, 1968, 861, 336, 112, 48, 21, 7, 3, 1 };

        private static final int[] sortIncrements03                                                             // CSOK: ConstantNameCheck
            = { 7, 3, 1 };

        /**
         * Sort sub-intervals using modified Shell Sort.
         */
        private static int[] sortIntervals ( int[] sa, int[] ea ) {
            assert sa != null;
            assert ea != null;
            assert sa.length == ea.length;
            int ni = sa.length;
            int[] incr = ( ni < 21 ) ? sortIncrements03 : sortIncrements16;
            for ( int k = 0; k < incr.length; k++ ) {
                for ( int h = incr [ k ], i = h, n = ni, j; i < n; i++ ) {
                    int s1 = sa [ i ];
                    int e1 = ea [ i ];
                    for ( j = i; j >= h; j -= h) {
                        int s2 = sa [ j - h ];
                        int e2 = ea [ j - h ];
                        if ( s2 > s1 ) {
                            sa [ j ] = s2;
                            ea [ j ] = e2;
                        } else if ( ( s2 == s1 ) && ( e2 > e1 ) ) {
                            sa [ j ] = s2;
                            ea [ j ] = e2;
                        } else {
                            break;
                        }
                    }
                    sa [ j ] = s1;
                    ea [ j ] = e1;
                }
            }
            int[] ia = new int [ ni * 2 ];
            for ( int i = 0; i < ni; i++ ) {
                ia [ ( i * 2 ) + 0 ] = sa [ i ];
                ia [ ( i * 2 ) + 1 ] = ea [ i ];
            }
            return ia;
        }

        /**
         * Merge overlapping and abutting sub-intervals.
         */
        private static int[] mergeIntervals ( int[] ia ) {
            int ni = ia.length;
            int i, n, nm, is, ie;
            // count merged sub-intervals
            for ( i = 0, n = ni, nm = 0, is = ie = -1; i < n; i += 2 ) {
                int s = ia [ i + 0 ];
                int e = ia [ i + 1 ];
                if ( ( ie < 0 ) || ( s > ie ) ) {
                    is = s;
                    ie = e;
                    nm++;
                } else if ( s >= is ) {
                    if ( e > ie ) {
                        ie = e;
                    }
                }
            }
            int[] mi = new int [ nm * 2 ];
            // populate merged sub-intervals
            for ( i = 0, n = ni, nm = 0, is = ie = -1; i < n; i += 2 ) {
                int s = ia [ i + 0 ];
                int e = ia [ i + 1 ];
                int k = nm * 2;
                if ( ( ie < 0 ) || ( s > ie ) ) {
                    is = s;
                    ie = e;
                    mi [ k + 0 ] = is;
                    mi [ k + 1 ] = ie;
                    nm++;
                } else if ( s >= is ) {
                    if ( e > ie ) {
                        ie = e;
                    }
                    mi [ k - 1 ] = ie;
                }
            }
            return mi;
        }

    }

}
