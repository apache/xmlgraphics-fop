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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

// CSOFF: LineLengthCheck

/**
 * The <code>GlyphPositioningTable</code> class is a glyph table that implements
 * <code>GlyphPositioning</code> functionality.
 * @author Glenn Adams
 */
public class GlyphPositioningTable extends GlyphTable implements GlyphPositioning {

    /** single positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_SINGLE = 1;
    /** multiple positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_PAIR = 2;
    /** cursive positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_CURSIVE = 3;
    /** mark to base positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_MARK_TO_BASE = 4;
    /** mark to ligature positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE = 5;
    /** mark to mark positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_MARK_TO_MARK = 6;
    /** context positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_CONTEXT = 7;
    /** chained context positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_CHAINED_CONTEXT = 8;
    /** extension positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_EXTENSION_POSITIONING = 9;

    /**
     * Instantiate a <code>GlyphPositioningTable</code> object using the specified lookups
     * and subtables.
     * @param lookups a map of lookup specifications to subtable identifier strings
     * @param subtables a list of identified subtables
     */
    public GlyphPositioningTable ( Map lookups, List subtables ) {
        super ( lookups );
        if ( ( subtables == null ) || ( subtables.size() == 0 ) ) {
            throw new IllegalArgumentException ( "subtables must be non-empty" );
        } else {
            for ( Iterator it = subtables.iterator(); it.hasNext();) {
                Object o = it.next();
                if ( o instanceof GlyphPositioningSubtable ) {
                    addSubtable ( (GlyphSubtable) o );
                } else {
                    throw new IllegalArgumentException ( "subtable must be a glyph positioning subtable" );
                }
            }
        }
    }

    /**
     * Map a lookup type name to its constant (integer) value.
     * @param name lookup type name
     * @return lookup type
     */
    public static int getLookupTypeFromName ( String name ) {
        int t;
        String s = name.toLowerCase();
        if ( "single".equals ( s ) ) {
            t = GPOS_LOOKUP_TYPE_SINGLE;
        } else if ( "pair".equals ( s ) ) {
            t = GPOS_LOOKUP_TYPE_PAIR;
        } else if ( "cursive".equals ( s ) ) {
            t = GPOS_LOOKUP_TYPE_CURSIVE;
        } else if ( "marktobase".equals ( s ) ) {
            t = GPOS_LOOKUP_TYPE_MARK_TO_BASE;
        } else if ( "marktoligature".equals ( s ) ) {
            t = GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE;
        } else if ( "marktomark".equals ( s ) ) {
            t = GPOS_LOOKUP_TYPE_MARK_TO_MARK;
        } else if ( "context".equals ( s ) ) {
            t = GPOS_LOOKUP_TYPE_CONTEXT;
        } else if ( "chainedcontext".equals ( s ) ) {
            t = GPOS_LOOKUP_TYPE_CHAINED_CONTEXT;
        } else if ( "extensionpositioning".equals ( s ) ) {
            t = GPOS_LOOKUP_TYPE_EXTENSION_POSITIONING;
        } else {
            t = -1;
        }
        return t;
    }

    /**
     * Map a lookup type constant (integer) value to its name.
     * @param type lookup type
     * @return lookup type name
     */
    public static String getLookupTypeName ( int type ) {
        String tn;
        switch ( type ) {
        case GPOS_LOOKUP_TYPE_SINGLE:
            tn = "single";
            break;
        case GPOS_LOOKUP_TYPE_PAIR:
            tn = "pair";
            break;
        case GPOS_LOOKUP_TYPE_CURSIVE:
            tn = "cursive";
            break;
        case GPOS_LOOKUP_TYPE_MARK_TO_BASE:
            tn = "marktobase";
            break;
        case GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE:
            tn = "marktoligature";
            break;
        case GPOS_LOOKUP_TYPE_MARK_TO_MARK:
            tn = "marktomark";
            break;
        case GPOS_LOOKUP_TYPE_CONTEXT:
            tn = "context";
            break;
        case GPOS_LOOKUP_TYPE_CHAINED_CONTEXT:
            tn = "chainedcontext";
            break;
        case GPOS_LOOKUP_TYPE_EXTENSION_POSITIONING:
            tn = "extensionpositioning";
            break;
        default:
            tn = "unknown";
            break;
        }
        return tn;
    }

    /**
     * Create a positioning subtable according to the specified arguments.
     * @param type subtable type
     * @param id subtable identifier
     * @param sequence subtable sequence
     * @param flags subtable flags
     * @param format subtable format
     * @param coverage subtable coverage table
     * @param entries subtable entries
     * @return a glyph subtable instance
     */
    public static GlyphSubtable createSubtable ( int type, String id, int sequence, int flags, int format, List coverage, List entries ) {
        return null;
    }

    /** {@inheritDoc} */
    public int[] position ( GlyphSequence gs, String script, String language ) {
        return null;
    }

}
