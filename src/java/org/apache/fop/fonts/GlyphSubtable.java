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

import java.util.List;

// CSOFF: LineLengthCheck

/**
 * The <code>GlyphSubtable</code> implements an abstract glyph subtable that
 * encapsulates identification, type, format, and coverage information.
 * @author Glenn Adams
 */
public abstract class GlyphSubtable {

    private String id;
    private int sequence;
    private int flags;
    private int format;
    private GlyphCoverageTable coverage;

    /**
     * Instantiate this glyph subtable.
     * @param id subtable identifier
     * @param sequence subtable sequence
     * @param flags subtable flags
     * @param format subtable format
     * @param coverage subtable coverage table
     */
    protected GlyphSubtable ( String id, int sequence, int flags, int format, GlyphCoverageTable coverage )
    {
        if ( ( id == null ) || ( id.length() == 0 ) ) {
            throw new IllegalArgumentException ( "invalid lookup identifier, must be non-empty string" );
        } else if ( coverage == null ) {
            throw new IllegalArgumentException ( "invalid coverage table, must not be null" );
        } else {
            this.id = id;
            this.sequence = sequence;
            this.flags = flags;
            this.format = format;
            this.coverage = coverage;
        }
    }

    /** @return this subtable's identifer */
    public String getID() {
        return id;
    }

    /** @return this subtable's table type */
    public abstract int getTableType();

    /** @return this subtable's type */
    public abstract int getType();

    /** @return this subtable's type name */
    public abstract String getTypeName();

    /** @return this subtable's sequence */
    public int getSequence() {
        return sequence;
    }

    /** @return this subtable's flags */
    public int getFlags() {
        return flags;
    }

    /** @return this subtable's format */
    public int getFormat() {
        return format;
    }

    /** @return this subtable's coverage table */
    public GlyphCoverageTable getCoverage() {
        return coverage;
    }

    /** @return this subtable's lookup entries */
    public abstract List getEntries();

    /**
     * Map glyph id to coverage index.
     * @param gid glyph id
     * @return the corresponding coverage index of the specified glyph id
     */
    public int getCoverageIndex ( int gid ) {
        return coverage.getCoverageIndex ( gid );
    }

}
