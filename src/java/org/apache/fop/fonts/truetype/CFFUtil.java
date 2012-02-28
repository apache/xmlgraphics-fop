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

package org.apache.fop.fonts.truetype;

import java.io.IOException;
import java.util.Map;

/**
 * A utility class for working with Adobe Compact Font Format (CFF) data. See
 * Adobe Technical Note #5176 "The Compact Font Format Specification" for more
 * information.
 */
final class CFFUtil {

    private CFFUtil() {
    }

    /**
     * Extract glyph subset of CFF table ENTRY from input font IN according to
     * the specified GLYPHS.
     * @param in input font file reader
     * @param entry directory entry describing CFF table in input file
     * @param glyphs map of original glyph indices to subset indices
     * @returns an array of bytes representing a well formed CFF table containing
     * the specified glyph subset
     * @throws IOException in case of an I/O exception when reading from input font
     */
    public static byte[] extractGlyphSubset
        ( FontFileReader in, TTFDirTabEntry entry, Map<Integer, Integer> glyphs )
        throws IOException {

        // 1. read CFF data from IN, where ENTRY points at start of CFF table
        // 2. while reading CFF data, accumulate necessary information to output subset
        //    of glyphs, where GLYPHS.keySet() enumerates the desired glyphs, and
        //    GLYPHS.values() (for the keys) enumerates the new (output) glyph indices
        //    for the desired glyph subset
        // 3. return a BLOB containing a well-formed CFF font according to the Adobe
        //    spec and constrained as needed by http://www.microsoft.com/typography/otspec/cff.htm

        long cffOffset = entry.getOffset();

        // HEADER

        in.seekSet ( cffOffset );
        int major = in.readTTFUByte();
        int minor = in.readTTFUByte();
        int hdrSize = in.readTTFUByte();
        int hdrOffSize = in.readTTFUByte();

        // Name INDEX

        in.seekSet ( cffOffset + hdrSize );
        int nameIndexCount = in.readTTFUShort();
        if ( nameIndexCount > 0 ) {
            int nameIndexOffsetSize = in.readTTFUByte();
            long nameIndexOffsets[] = new long [ nameIndexCount + 1 ];
            if ( nameIndexOffsetSize == 1 ) {
                for ( int i = 0, n = nameIndexCount + 1; i < n; i++ ) {
                    nameIndexOffsets [ i ] = in.readTTFUByte();
                }
            } else if ( nameIndexOffsetSize == 2 ) {
                for ( int i = 0, n = nameIndexCount + 1; i < n; i++ ) {
                    nameIndexOffsets [ i ] = in.readTTFUShort();
                }
            } else if ( nameIndexOffsetSize == 4 ) {
                for ( int i = 0, n = nameIndexCount + 1; i < n; i++ ) {
                    nameIndexOffsets [ i ] = in.readTTFULong();
                }
            } else {
                throw new RuntimeException ( "invalid offset size, got " + nameIndexOffsetSize + ", expected 1, 2, or 4" );
            }
            int nameIndexDataOffset = in.getCurrentPos() - 1;
            String[] names = new String [ nameIndexCount ];
            for ( int i = 0, n = names.length, nOffsets = nameIndexOffsets.length; i < n; i++ ) {
                assert ( i + 1 ) < nOffsets;
                long offCurrent = nameIndexOffsets [ i ];
                long offNext = nameIndexOffsets [ i + 1 ];
                long numBytes = offNext - offCurrent;
                String name;
                if ( numBytes > 0 ) {
                    if ( numBytes < Integer.MAX_VALUE ) {
                        long nameOffset = nameIndexDataOffset + offCurrent;
                        if ( nameOffset < Integer.MAX_VALUE ) {
                            byte[] nameBytes = in.getBytes ( (int) nameOffset, (int) numBytes );
                            name = new String ( nameBytes, 0, (int) numBytes, "US-ASCII" );
                        } else {
                            throw new UnsupportedOperationException ( "unsupported index offset value, got " + nameOffset + ", expected less than " + Integer.MAX_VALUE );
                        }
                    } else {
                        throw new UnsupportedOperationException ( "unsupported indexed data length, got " + numBytes + ", expected less than " + Integer.MAX_VALUE );
                    }
                } else {
                    name = "";
                }
                names [ i ] = name;
            }
            in.seekSet ( nameIndexDataOffset + nameIndexOffsets [ nameIndexCount ] );
        }

        // Top Dict INDEX

        int topDictIndexCount = in.readTTFUShort();
        if ( topDictIndexCount > 0 ) {
            int topDictIndexOffsetSize = in.readTTFUByte();
            long topDictIndexOffsets[] = new long [ topDictIndexCount + 1 ];
            if ( topDictIndexOffsetSize == 1 ) {
                for ( int i = 0, n = topDictIndexCount + 1; i < n; i++ ) {
                    topDictIndexOffsets [ i ] = in.readTTFUByte();
                }
            } else if ( topDictIndexOffsetSize == 2 ) {
                for ( int i = 0, n = topDictIndexCount + 1; i < n; i++ ) {
                    topDictIndexOffsets [ i ] = in.readTTFUShort();
                }
            } else if ( topDictIndexOffsetSize == 4 ) {
                for ( int i = 0, n = topDictIndexCount + 1; i < n; i++ ) {
                    topDictIndexOffsets [ i ] = in.readTTFULong();
                }
            } else {
                throw new RuntimeException ( "invalid offset size, got " + topDictIndexOffsetSize + ", expected 1, 2, or 4" );
            }
            int topDictIndexDataOffset = in.getCurrentPos() - 1;
            byte[][] topDicts = new byte [ topDictIndexCount ][];
            for ( int i = 0, n = topDicts.length, nOffsets = topDictIndexOffsets.length; i < n; i++ ) {
                assert ( i + 1 ) < nOffsets;
                long offCurrent = topDictIndexOffsets [ i ];
                long offNext = topDictIndexOffsets [ i + 1 ];
                long numBytes = offNext - offCurrent;
                byte[] topDict;
                if ( numBytes > 0 ) {
                    if ( numBytes < Integer.MAX_VALUE ) {
                        long topDictOffset = topDictIndexDataOffset + offCurrent;
                        if ( topDictOffset < Integer.MAX_VALUE ) {
                            byte[] topDictBytes = in.getBytes ( (int) topDictOffset, (int) numBytes );
                            topDict = topDictBytes;
                        } else {
                            throw new UnsupportedOperationException ( "unsupported index offset value, got " + topDictOffset + ", expected less than " + Integer.MAX_VALUE );
                        }
                    } else {
                        throw new UnsupportedOperationException ( "unsupported indexed data length, got " + numBytes + ", expected less than " + Integer.MAX_VALUE );
                    }
                } else {
                    topDict = new byte [ 0 ];
                }
                topDicts [ i ] = topDict;
            }
            in.seekSet ( topDictIndexDataOffset + topDictIndexOffsets [ topDictIndexCount ] );
        }

        return new byte[] {};
    }

}
