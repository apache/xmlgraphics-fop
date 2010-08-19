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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// CSOFF: NoWhitespaceAfterCheck
// CSOFF: InnerAssignmentCheck
// CSOFF: SimplifyBooleanReturnCheck
// CSOFF: LineLengthCheck

/**
 * Base class for all advanced typographic glyph tables.
 * @author Glenn Adams
 */
public class GlyphTable {

    /** substitution glyph table type */
    public static final int GLYPH_TABLE_TYPE_SUBSTITUTION = 1;
    /** positioning glyph table type */
    public static final int GLYPH_TABLE_TYPE_POSITIONING = 2;
    /** justification glyph table type */
    public static final int GLYPH_TABLE_TYPE_JUSTIFICATION = 3;
    /** baseline glyph table type */
    public static final int GLYPH_TABLE_TYPE_BASELINE = 4;
    /** definition glyph table type */
    public static final int GLYPH_TABLE_TYPE_DEFINITION = 5;

    // map from lookup specs to lists of strings, each naming a subtable
    private Map /*<LookupSpec,List>*/ lookups;

    // map from subtable names to glyph subtables
    private Map /*<String,GlyphSubtable>*/ subtables;

    /**
     * Instantiate glyph table with specified lookups.
     * @param lookups map from lookup specs to lookup tables
     */
    public GlyphTable ( Map /*<LookupSpec,List>*/ lookups ) {
        if ( ( lookups == null ) || ( lookups.size() == 0 ) ) {
            throw new IllegalArgumentException ( "lookups must be non-empty map" );
        } else {
            this.lookups = lookups;
            this.subtables = new LinkedHashMap();
        }
    }

    /**
     * Obain array of lookup specifications.
     * @return (possibly empty) array of all lookup specifications
     */
    public LookupSpec[] getLookups() {
        return matchLookupSpecs ( "*", "*", "*" );
    }

    /**
     * Obain array of lookup subtables.
     * @return (possibly empty) array of all lookup subtables
     */
    public GlyphSubtable[] getSubtables() {
        Collection values = subtables.values();
        return (GlyphSubtable[]) values.toArray ( new GlyphSubtable [ values.size() ] );
    }

    /**
     * Add a subtable.
     * @param subtable a (non-null) glyph subtable
     */
    public void addSubtable ( GlyphSubtable subtable ) {
        subtables.put ( subtable.getID(), subtable );
    }

    /**
     * Match lookup specifications according to <script,language,feature> tuple, where
     * '*' is a wildcard for a tuple component.
     * @param script a script identifier
     * @param language a language identifier
     * @param feature a feature identifier
     * @return a (possibly empty) array of matching lookup specifications
     */
    public LookupSpec[] matchLookupSpecs ( String script, String language, String feature ) {
        Set/*<LookupSpec>*/ keys = lookups.keySet();
        List matches = new ArrayList();
        for ( Iterator it = keys.iterator(); it.hasNext();) {
            LookupSpec ls = (LookupSpec) it.next();
            if ( ! "*".equals(script) ) {
                if ( ! ls.getScript().equals ( script ) ) {
                    continue;
                }
            }
            if ( ! "*".equals(language) ) {
                if ( ! ls.getLanguage().equals ( language ) ) {
                    continue;
                }
            }
            if ( ! "*".equals(feature) ) {
                if ( ! ls.getFeature().equals ( feature ) ) {
                    continue;
                }
            }
            matches.add ( ls );
        }
        return (LookupSpec[]) matches.toArray ( new LookupSpec [ matches.size() ] );
    }

    /**
     * Match lookup specifications according to <script,language,feature> tuple, where
     * '*' is a wildcard for a tuple component.
     * @param script a script identifier
     * @param language a language identifier
     * @param feature a feature identifier
     * @return a (possibly empty) map of matching lookup specifications and their corresponding subtables
     */
    public Map/*<LookupSpec,GlyphSubtable[]>*/ matchLookups ( String script, String language, String feature ) {
        LookupSpec[] lsa = matchLookupSpecs ( script, language, feature );
        Map lm = new LinkedHashMap();
        for ( int i = 0, n = lsa.length; i < n; i++ ) {
            lm.put ( lsa [ i ], findSubtables ( lsa [ i ] ) );
        }
        return lm;
    }

    /**
     * Find glyph subtables that match a secific lookup specification.
     * @param ls a (non-null) lookup specification
     * @return a (possibly empty) array of subtables whose lookup specification matches the specified lookup spec
     */
    public GlyphSubtable[] findSubtables ( LookupSpec ls ) {
        GlyphSubtable[] staEmpty = new GlyphSubtable [ 0 ];
        List ids;
        if ( ( ids = (List) lookups.get ( ls ) ) != null ) {
            List stl = new ArrayList();
            for ( Iterator it = ids.iterator(); it.hasNext();) {
                String id = (String) it.next();
                GlyphSubtable st;
                if ( ( st = (GlyphSubtable) subtables.get ( id ) ) != null ) {
                    stl.add ( st );
                }
            }
            return (GlyphSubtable[]) stl.toArray ( staEmpty );
        } else {
            return staEmpty;
        }
    }

    /**
     * Obtain glyph table type from name.
     * @param name of table type to map to type value
     * @return glyph table type (as an integer constant)
     */
    public static int getTableTypeFromName ( String name ) {
        int t;
        String s = name.toLowerCase();
        if ( "gsub".equals ( s ) ) {
            t = GLYPH_TABLE_TYPE_SUBSTITUTION;
        } else if ( "gpos".equals ( s ) ) {
            t = GLYPH_TABLE_TYPE_POSITIONING;
        } else if ( "jstf".equals ( s ) ) {
            t = GLYPH_TABLE_TYPE_JUSTIFICATION;
        } else if ( "base".equals ( s ) ) {
            t = GLYPH_TABLE_TYPE_BASELINE;
        } else if ( "gdef".equals ( s ) ) {
            t = GLYPH_TABLE_TYPE_DEFINITION;
        } else {
            t = -1;
        }
        return t;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append("{");
        sb.append("lookups={");
        sb.append(lookups.toString());
        sb.append("},subtables={");
        sb.append(subtables.toString());
        sb.append("}}");
        return sb.toString();
    }

    /**
     * A structure class encapsulating a lookup specification as a <script,language,feature> tuple.
     */
    public static class LookupSpec {

        private final String script;
        private final String language;
        private final String feature;

        /**
         * Instantiate lookup spec.
         * @param script a script identifier
         * @param language a language identifier
         * @param feature a feature identifier
         */
        public LookupSpec ( String script, String language, String feature ) {
            if ( ( script == null ) || ( script.length() == 0 ) ) {
                throw new IllegalArgumentException ( "script must be non-empty string" );
            } else if ( ( language == null ) || ( language.length() == 0 ) ) {
                throw new IllegalArgumentException ( "language must be non-empty string" );
            } else if ( ( feature == null ) || ( feature.length() == 0 ) ) {
                throw new IllegalArgumentException ( "feature must be non-empty string" );
            } else {
                this.script = script;
                this.language = language;
                this.feature = feature;
            }
        }

        /** @return script identifier */
        public String getScript() {
            return script;
        }

        /** @return language identifier */
        public String getLanguage() {
            return language;
        }

        /** @return feature identifier  */
        public String getFeature() {
            return feature;
        }

        /** {@inheritDoc} */
        public int hashCode() {
            int h = 0;
            h = 31 * h + script.hashCode();
            h = 31 * h + language.hashCode();
            h = 31 * h + feature.hashCode();
            return h;
        }

        /** {@inheritDoc} */
        public boolean equals ( Object o ) {
            if ( o instanceof LookupSpec ) {
                LookupSpec l = (LookupSpec) o;
                if ( ! l.script.equals ( script ) ) {
                    return false;
                } else if ( ! l.language.equals ( language ) ) {
                    return false;
                } else if ( ! l.feature.equals ( feature ) ) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }

        /** {@inheritDoc} */
        public String toString() {
            StringBuffer sb = new StringBuffer(super.toString());
            sb.append("{");
            sb.append("<'" + script + "'");
            sb.append(",'" + language + "'");
            sb.append(",'" + feature + "'");
            sb.append(">}");
            return sb.toString();
        }

    }

}
