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

// CSOFF: LineLengthCheck

/**
 * The <code>GlyphSubstitutionSubtable</code> implements an abstract base of a glyph substitution subtable,
 * providing a default implementation of the <code>GlyphSubstitution</code> interface.
 * @author Glenn Adams
 */
public abstract class GlyphSubstitutionSubtable extends GlyphSubtable implements GlyphSubstitution {

    /**
     * Instantiate a <code>GlyphSubstitutionSubtable</code>.
     * @param id subtable identifier
     * @param sequence subtable sequence
     * @param flags subtable flags
     * @param format subtable format
     * @param coverage subtable coverage table
     */
    protected GlyphSubstitutionSubtable ( String id, int sequence, int flags, int format, GlyphCoverageTable coverage ) {
        super ( id, sequence, flags, format, coverage );
    }

    /** {@inheritDoc} */
    public int getTableType() {
        return GlyphTable.GLYPH_TABLE_TYPE_SUBSTITUTION;
    }

    /** {@inheritDoc} */
    public String getTypeName() {
        return GlyphSubstitutionTable.getLookupTypeName ( getType() );
    }

    /** {@inheritDoc} */
    public GlyphSequence substitute ( GlyphSequence gs, String script, String language ) {
        if ( gs == null ) {
            throw new IllegalArgumentException ( "invalid glyph sequence: must not be null" );
        } else {
            return gs;
        }
    }

}
