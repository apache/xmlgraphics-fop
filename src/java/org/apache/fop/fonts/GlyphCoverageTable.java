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

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

// CSOFF: NoWhitespaceAfterCheck
// CSOFF: InnerAssignmentCheck
// CSOFF: LineLengthCheck

/**
 * Abstract base class implementation of glyph coverage table.
 * @author Glenn Adams
 */
public abstract class GlyphCoverageTable {

    /** empty coverage table */
    public static final int GLYPH_COVERAGE_TYPE_EMPTY = 0;

    /** mapped coverage table */
    public static final int GLYPH_COVERAGE_TYPE_MAPPED = 1;

    /** range based coverage table */
    public static final int GLYPH_COVERAGE_TYPE_RANGE = 2;

    /**
     * Obtain coverage type.
     * @return coverage format type
     */
    public abstract int getType();

    /**
     * Obtain coverage entries.
     * @return list of coverage entries
     */
    public abstract List getEntries();

    /**
     * Map glyph identifier (code) to coverge index. Returns -1 if glyph identifier is not in the domain of
     * the coverage table.
     * @param gid glyph identifier (code)
     * @return non-negative glyph coverage index or -1 if glyph identifiers is not mapped by table
     */
    public abstract int getCoverageIndex ( int gid );

    /**
     * Create glyph coverage table.
     * @param coverage list of mapped or ranged coverage entries, or null or empty list
     * @return a new covera table instance
     */
    public static GlyphCoverageTable createCoverageTable ( List coverage ) {
        GlyphCoverageTable ct;
        if ( ( coverage == null ) || ( coverage.size() == 0 ) ) {
            ct = new EmptyCoverageTable ( coverage );
        } else if ( isMappedCoverage ( coverage ) ) {
            ct = new MappedCoverageTable ( coverage );
        } else if ( isRangeCoverage ( coverage ) ) {
            ct = new RangeCoverageTable ( coverage );
        } else {
            ct = null;
        }
        assert ct != null : "unknown coverage type";
        return ct;
    }

    private static boolean isMappedCoverage ( List coverage ) {
        if ( ( coverage == null ) || ( coverage.size() == 0 ) ) {
            return false;
        } else {
            for ( Iterator it = coverage.iterator(); it.hasNext();) {
                Object o = it.next();
                if ( ! ( o instanceof Integer ) ) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean isRangeCoverage ( List coverage ) {
        if ( ( coverage == null ) || ( coverage.size() == 0 ) ) {
            return false;
        } else {
            for ( Iterator it = coverage.iterator(); it.hasNext();) {
                Object o = it.next();
                if ( ! ( o instanceof CoverageRange ) ) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class EmptyCoverageTable extends GlyphCoverageTable {
        public EmptyCoverageTable ( List coverage ) {
        }
        public int getType() {
            return GLYPH_COVERAGE_TYPE_EMPTY;
        }
        public List getEntries() {
            return new java.util.ArrayList();
        }
        public int getCoverageIndex ( int gid ) {
            return -1;
        }
    }

    private static class MappedCoverageTable extends GlyphCoverageTable {
        private int[] map = null;
        public MappedCoverageTable ( List coverage ) {
            populate ( coverage );
        }
        public int getType() {
            return GLYPH_COVERAGE_TYPE_MAPPED;
        }
        public List getEntries() {
            List entries = new java.util.ArrayList();
            if ( map != null ) {
                for ( int i = 0, n = map.length; i < n; i++ ) {
                    entries.add ( Integer.valueOf ( map [ i ] ) );
                }
            }
            return entries;
        }
        public int getCoverageIndex ( int gid ) {
            int i;
            if ( ( i = Arrays.binarySearch ( map, gid ) ) >= 0 ) {
                return i;
            } else {
                return -1;
            }
        }
        private void populate ( List coverage ) {
            int i = 0, n = coverage.size(), gidMax = -1;
            int[] map = new int [ n ];
            for ( Iterator it = coverage.iterator(); it.hasNext();) {
                Object o = it.next();
                if ( o instanceof Integer ) {
                    int gid = ( (Integer) o ) . intValue();
                    if ( ( gid >= 0 ) && ( gid < 65536 ) ) {
                        if ( gid > gidMax ) {
                            map [ i++ ] = gidMax = gid;
                        } else {
                            throw new IllegalArgumentException ( "out of order or duplicate glyph index: " + gid );
                        }
                    } else {
                        throw new IllegalArgumentException ( "illegal glyph index: " + gid );
                    }
                } else {
                    throw new IllegalArgumentException ( "illegal coverage entry, must be Integer: " + o );
                }
            }
            assert i == n;
            assert this.map == null;
            this.map = map;
        }
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append('{');
            for ( int i = 0, n = map.length; i < n; i++ ) {
                if ( i > 0 ) {
                    sb.append(',');
                }
                sb.append ( Integer.toString ( map [ i ] ) );
            }
            sb.append('}');
            return sb.toString();
        }
    }

    private static class RangeCoverageTable extends GlyphCoverageTable {
        private int[] sa = null;                                                // array of ranges starts
        private int[] ea = null;                                                // array of range ends
        private int[] ca = null;                                                // array of range coverage (start) indices
        public RangeCoverageTable ( List coverage ) {
            populate ( coverage );
        }
        public int getType() {
            return GLYPH_COVERAGE_TYPE_RANGE;
        }
        public List getEntries() {
            List entries = new java.util.ArrayList();
            if ( sa != null ) {
                for ( int i = 0, n = sa.length; i < n; i++ ) {
                    entries.add ( new CoverageRange ( sa [ i ], ea [ i ], ca [ i ] ) );
                }
            }
            return entries;
        }
        public int getCoverageIndex ( int gid ) {
            int i, ci;
            if ( ( i = Arrays.binarySearch ( sa, gid ) ) >= 0 ) {
                ci = ca [ i ] + gid - sa [ i ];                         // matches start of (some) range
            } else if ( ( i = - ( i + 1 ) ) == 0 ) {
                ci = -1;                                                // precedes first range 
            } else if ( gid > ea [ --i ] ) {
                ci = -1;                                                // follows preceding (or last) range
            } else {
                ci = ca [ i ] + gid - sa [ i ];                         // intersects (some) range
            }
            return ci;
        }
        private void populate ( List coverage ) {
            int i = 0, n = coverage.size(), gidMax = -1;
            int[] sa = new int [ n ];
            int[] ea = new int [ n ];
            int[] ca = new int [ n ];
            for ( Iterator it = coverage.iterator(); it.hasNext();) {
                Object o = it.next();
                if ( o instanceof CoverageRange ) {
                    CoverageRange r = (CoverageRange) o;
                    int gs = r.getStart();
                    int ge = r.getEnd();
                    int ci = r.getIndex();
                    if ( ( gs < 0 ) || ( gs > 65535 ) ) {
                        throw new IllegalArgumentException ( "illegal glyph range: [" + gs + "," + ge + "]: bad start index" );
                    } else if ( ( ge < 0 ) || ( ge > 65535 ) ) {
                        throw new IllegalArgumentException ( "illegal glyph range: [" + gs + "," + ge + "]: bad end index" );
                    } else if ( gs > ge ) {
                        throw new IllegalArgumentException ( "illegal glyph range: [" + gs + "," + ge + "]: start index exceeds end index" );
                    } else if ( gs < gidMax ) {
                        throw new IllegalArgumentException ( "out of order glyph range: [" + gs + "," + ge + "]" );
                    } else if ( ci < 0 ) {
                        throw new IllegalArgumentException ( "illegal coverage index: " + ci );
                    } else {
                        sa [ i ] = gs;
                        ea [ i ] = gidMax = ge;
                        ca [ i ] = ci;
                        i++;
                    }
                } else {
                    throw new IllegalArgumentException ( "illegal coverage entry, must be Integer: " + o );
                }
            }
            assert i == n;
            assert this.sa == null;
            assert this.ea == null;
            assert this.ca == null;
            this.sa = sa;
            this.ea = ea;
            this.ca = ca;
        }
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append('{');
            for ( int i = 0, n = sa.length; i < n; i++ ) {
                if ( i > 0 ) {
                    sb.append(',');
                }
                sb.append ( '[' );
                sb.append ( Integer.toString ( sa [ i ] ) );
                sb.append ( Integer.toString ( ea [ i ] ) );
                sb.append ( "]:" );
                sb.append ( Integer.toString ( ca [ i ] ) );
            }
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * The <code>CoverageRange</code> class encapsulates a glyph [start,end] range and
     * a coverage index.
     */
    public static class CoverageRange {

        private final int gidStart;                     // first glyph in range (inclusive)
        private final int gidEnd;                       // last glyph in range (inclusive)
        private final int index;                        // coverage index;

        /**
         * Instantiate a coverage range.
         */
        public CoverageRange() {
            this ( 0, 0, 0 );
        }

        /**
         * Instantiate a specific coverage range.
         * @param gidStart start of range
         * @param gidEnd end of range
         * @param index coverage index
         */
        public CoverageRange ( int gidStart, int gidEnd, int index ) {
            if ( ( gidStart < 0 ) || ( gidEnd < 0 ) || ( index < 0 ) ) {
                throw new IllegalArgumentException();
            } else if ( gidStart > gidEnd ) {
                throw new IllegalArgumentException();
            } else {
                this.gidStart = gidStart;
                this.gidEnd = gidEnd;
                this.index = index;
            }
        }

        /** @return start of range */
        public int getStart() {
            return gidStart;
        }

        /** @return end of range */
        public int getEnd() {
            return gidEnd;
        }

        /** @return coverage index */
        public int getIndex() {
            return index;
        }

        /** @return interval as a pair of integers */
        public int[] getInterval() {
            return new int[] { gidStart, gidEnd };
        }

        /**
         * Obtain interval, filled into first two elements of specified array, or returning new array.
         * @param interval an array of length two or greater or null
         * @return interval as a pair of integers, filled into specified array
         */
        public int[] getInterval ( int[] interval ) {
            if ( ( interval == null ) || ( interval.length != 2 ) ) {
                throw new IllegalArgumentException();
            } else {
                interval[0] = gidStart;
                interval[1] = gidEnd;
            }
            return interval;
        }

        /** @return length of interval */
        public int getLength() {
            return gidStart - gidEnd;
        }

    }

}
