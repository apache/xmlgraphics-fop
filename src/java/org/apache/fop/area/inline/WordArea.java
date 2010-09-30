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

package org.apache.fop.area.inline;

import java.util.Arrays;

import org.apache.fop.util.CharUtilities;

/**
 * A string of characters without spaces
 */
public class WordArea extends InlineArea {

    /** The text for this word area */
    protected String word;

    /** An array of width for adjusting the individual letters (optional) */
    protected int[] letterAdjust;

    /**
     * An array of resolved bidirectional levels corresponding to each character
     * in word (optional)
     */
    protected int[] levels;

    /**
     * An array of glyph positioning adjustments to apply to each glyph 'char' in word (optional)
     */
    protected int[][] gposAdjustments;

    /**
     * A flag indicating whether the content of word is reversed in relation to
     * its original logical order.
     */
    protected boolean reversed;

    /**
     * Create a word area
     * @param blockProgressionOffset the offset for this area
     * @param level the bidirectional embedding level (or -1 if not defined) for word as a group
     * @param word the word string
     * @param letterAdjust the letter adjust array (may be null)
     * @param levels array of per-character (glyph) bidirectional levels,
     * in case word area is heterogenously leveled
     * @param gposAdjustments array of general position adjustments or null if none apply
     */
    public WordArea
        ( int blockProgressionOffset, int level, String word, int[] letterAdjust, int[] levels,
          int[][] gposAdjustments ) {
        super ( blockProgressionOffset, level );
        int length = ( word != null ) ? word.length() : 0;
        this.word = word;
        this.letterAdjust = maybeAdjustLength ( letterAdjust, length );
        this.levels = maybePopulateLevels ( levels, level, length );
        this.gposAdjustments = maybeAdjustLength ( gposAdjustments, length );
        this.reversed = false;
    }

    /**
     * @return Returns the word.
     */
    public String getWord() {
        return word;
    }

    /** @return the array of letter adjust widths */
    public int[] getLetterAdjustArray() {
        return this.letterAdjust;
    }

    /**
     * Obtain per-character (glyph) bidi levels.
     * @return a (possibly empty) array of levels or null (if none resolved)
     */
    public int[] getBidiLevels() {
        return levels;
    }

    /**
     * <p>Obtain per-character (glyph) bidi levels over a specified subsequence.</p>
     * <p>If word has been reversed, then the subsequence is over the reversed word.</p>
     * @param start starting (inclusive) index of subsequence
     * @param end ending (exclusive) index of subsequence
     * @return a (possibly null) array of per-character (glyph) levels over the specified
     * sequence
     */
    public int[] getBidiLevels ( int start, int end ) {
        assert start <= end;
        if ( levels != null ) {
            int n = end - start;
            int[] levels = new int [ n ];
            for ( int i = 0; i < n; i++ ) {
                levels[i] = this.levels [ start + i ];
            }
            return levels;
        } else {
            return null;
        }
    }

    /**
     * <p>Obtain per-character (glyph) level at a specified index position.</p>
     * <p>If word has been reversed, then the position is relative to the reversed word.</p>
     * @param position the index of the (possibly reversed) character from which to obtain the
     * level
     * @return a resolved bidirectional level or, if not specified, then -1
     */
    public int bidiLevelAt ( int position ) {
        if ( position > word.length() ) {
            throw new IndexOutOfBoundsException();
        } else if ( levels != null ) {
            return levels [ position ];
        } else {
            return -1;
        }
    }

    /**
     * Obtain per-character (glyph) position adjustments.
     * @return a (possibly empty) array of adjustments, each having four elements, or null
     * if no adjustments apply
     */
    public int[][] getGlyphPositionAdjustments() {
        return gposAdjustments;
    }

    /**
     * <p>Obtain per-character (glyph) position adjustments at a specified index position.</p>
     * <p>If word has been reversed, then the position is relative to the reversed word.</p>
     * @param position the index of the (possibly reversed) character from which to obtain the
     * level
     * @return an array of adjustments or null if none applies
     */
    public int[] glyphPositionAdjustmentsAt ( int position ) {
        if ( position > word.length() ) {
            throw new IndexOutOfBoundsException();
        } else if ( gposAdjustments != null ) {
            return gposAdjustments [ position ];
        } else {
            return null;
        }
    }

    /**
     * <p>Reverse characters and corresponding per-character levels and glyph position
     * adjustments.</p>
     * @param mirror if true, then perform mirroring if mirrorred characters
     */
    public void reverse ( boolean mirror ) {
        if ( word.length() > 0 ) {
            word = ( ( new StringBuffer ( word ) ) .reverse() ) .toString();
            if ( levels != null ) {
                reverse ( levels );
            }
            if ( gposAdjustments != null ) {
                reverse ( gposAdjustments );
            }
            reversed = !reversed;
            if ( mirror ) {
                word = CharUtilities.mirror ( word );
            }
        }
    }

    /**
     * <p>Perform mirroring on mirrorable characters.</p>
     */
    public void mirror() {
        if ( word.length() > 0 ) {
            word = CharUtilities.mirror ( word );
        }
    }

    /**
     * <p>Determined if word has been reversed (in relation to original logical order).</p>
     * <p>If a word is reversed, then both its characters (glyphs) and corresponding per-character
     * levels are in reverse order.</p>
     * <p>Note: this information is used in order to process non-spacing marks during rendering as
     * well as provide hints for caret direction.</p>
     * @return true if word is reversed
     */
    public boolean isReversed() {
        return reversed;
    }

    /*
     * If int[] array is not of specified length, then create
     * a new copy of the first length entries.
     */
    private static int[] maybeAdjustLength ( int[] ia, int length ) {
        if ( ia != null ) {
            if ( ia.length == length ) {
                return ia;
            } else {
                int[] iaNew = new int [ length ];
                for ( int i = 0, n = ia.length; i < n; i++ ) {
                    if ( i < length ) {
                        iaNew [ i ] = ia [ i ];
                    } else {
                        break;
                    }
                }
                return iaNew;
            }
        } else {
            return ia;
        }
    }

    /*
     * If int[][] matrix is not of specified length, then create
     * a new shallow copy of the first length entries.
     */
    private static int[][] maybeAdjustLength ( int[][] im, int length ) {
        if ( im != null ) {
            if ( im.length == length ) {
                return im;
            } else {
                int[][] imNew = new int [ length ][];
                for ( int i = 0, n = im.length; i < n; i++ ) {
                    if ( i < length ) {
                        imNew [ i ] = im [ i ];
                    } else {
                        break;
                    }
                }
                return imNew;
            }
        } else {
            return im;
        }
    }

    private static int[] maybePopulateLevels ( int[] levels, int level, int count ) {
        if ( ( levels == null ) && ( level >= 0 ) ) {
            levels = new int[count];
            Arrays.fill ( levels, level );
        }
        return maybeAdjustLength ( levels, count );
    }

    private static void reverse ( int[] a ) {
        for ( int i = 0, n = a.length, m = n / 2; i < m; i++ ) {
            int k = n - i - 1;
            int t = a [ k ];
            a [ k ] = a [ i ];
            a [ i ] = t;
        }
    }

    private static void reverse ( int[][] aa ) {
        for ( int i = 0, n = aa.length, m = n / 2; i < m; i++ ) {
            int k = n - i - 1;
            int[] t = aa [ k ];
            aa [ k ] = aa [ i ];
            aa [ i ] = t;
        }
    }

}
