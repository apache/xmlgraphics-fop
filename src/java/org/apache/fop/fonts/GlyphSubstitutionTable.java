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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// CSOFF: InnerAssignmentCheck
// CSOFF: LineLengthCheck

/**
 * The <code>GlyphSubstitutionTable</code> class is a glyph table that implements
 * <code>GlyphSubstitution</code> functionality.
 * @author Glenn Adams
 */
public class GlyphSubstitutionTable extends GlyphTable implements GlyphSubstitution {

    /** single substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_SINGLE = 1;
    /** multiple substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_MULTIPLE = 2;
    /** alternate substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_ALTERNATE = 3;
    /** ligature substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_LIGATURE = 4;
    /** context substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_CONTEXT = 5;
    /** chaining context substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_CHAINING_CONTEXT = 6;
    /** extension substitution substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION = 7;
    /** reverse chaining context single substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_REVERSE_CHAINING_CONTEXT_SINGLE = 8;

    /**
     * Instantiate a <code>GlyphSubstitutionTable</code> object using the specified lookups
     * and subtables.
     * @param lookups a map of lookup specifications to subtable identifier strings
     * @param subtables a list of identified subtables
     */
    public GlyphSubstitutionTable ( Map lookups, List subtables ) {
        super ( lookups );
        if ( ( subtables == null ) || ( subtables.size() == 0 ) ) {
            throw new IllegalArgumentException ( "subtables must be non-empty" );
        } else {
            for ( Iterator it = subtables.iterator(); it.hasNext();) {
                Object o = it.next();
                if ( o instanceof GlyphSubstitutionSubtable ) {
                    addSubtable ( (GlyphSubtable) o );
                } else {
                    throw new IllegalArgumentException ( "subtable must be a glyph substitution subtable" );
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
            t = GSUB_LOOKUP_TYPE_SINGLE;
        } else if ( "multiple".equals ( s ) ) {
            t = GSUB_LOOKUP_TYPE_MULTIPLE;
        } else if ( "alternate".equals ( s ) ) {
            t = GSUB_LOOKUP_TYPE_ALTERNATE;
        } else if ( "ligature".equals ( s ) ) {
            t = GSUB_LOOKUP_TYPE_LIGATURE;
        } else if ( "context".equals ( s ) ) {
            t = GSUB_LOOKUP_TYPE_CONTEXT;
        } else if ( "chainingcontext".equals ( s ) ) {
            t = GSUB_LOOKUP_TYPE_CHAINING_CONTEXT;
        } else if ( "extensionsubstitution".equals ( s ) ) {
            t = GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION;
        } else if ( "reversechainiingcontextsingle".equals ( s ) ) {
            t = GSUB_LOOKUP_TYPE_REVERSE_CHAINING_CONTEXT_SINGLE;
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
        String tn = null;
        switch ( type ) {
        case GSUB_LOOKUP_TYPE_SINGLE:
            tn = "single";
            break;
        case GSUB_LOOKUP_TYPE_MULTIPLE:
            tn = "multiple";
            break;
        case GSUB_LOOKUP_TYPE_ALTERNATE:
            tn = "alternate";
            break;
        case GSUB_LOOKUP_TYPE_LIGATURE:
            tn = "ligature";
            break;
        case GSUB_LOOKUP_TYPE_CONTEXT:
            tn = "context";
            break;
        case GSUB_LOOKUP_TYPE_CHAINING_CONTEXT:
            tn = "chainingcontext";
            break;
        case GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION:
            tn = "extensionsubstitution";
            break;
        case GSUB_LOOKUP_TYPE_REVERSE_CHAINING_CONTEXT_SINGLE:
            tn = "reversechainiingcontextsingle";
            break;
        default:
            tn = "unknown";
            break;
        }
        return tn;
    }

    /**
     * Create a substitution subtable according to the specified arguments.
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
        GlyphSubtable st = null;
        switch ( type ) {
        case GSUB_LOOKUP_TYPE_SINGLE:
            st = new SimpleSubtable ( id, sequence, flags, format, coverage, entries );
            break;
        case GSUB_LOOKUP_TYPE_MULTIPLE:
            st = new MultipleSubtable ( id, sequence, flags, format, coverage, entries );
            break;
        case GSUB_LOOKUP_TYPE_ALTERNATE:
            st = new AlternateSubtable ( id, sequence, flags, format, coverage, entries );
            break;
        case GSUB_LOOKUP_TYPE_LIGATURE:
            st = new LigatureSubtable ( id, sequence, flags, format, coverage, entries );
            break;
        case GSUB_LOOKUP_TYPE_CONTEXT:
            st = new ContextSubtable ( id, sequence, flags, format, coverage, entries );
            break;
        case GSUB_LOOKUP_TYPE_CHAINING_CONTEXT:
            st = new ChainingContextSubtable ( id, sequence, flags, format, coverage, entries );
            break;
        case GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION:
            st = new ExtensionSubtable ( id, sequence, flags, format, coverage, entries );
            break;
        case GSUB_LOOKUP_TYPE_REVERSE_CHAINING_CONTEXT_SINGLE:
            st = new ReverseChainingSingleSubtable ( id, sequence, flags, format, coverage, entries );
            break;
        default:
            break;
        }
        return st;
    }

    /** {@inheritDoc} */
    public GlyphSequence substitute ( GlyphSequence gs, String script, String language ) {
        GlyphSequence ogs;
        Map/*<LookupSpec,GlyphSubtable[]>*/ lookups = matchLookups ( script, language, "*" );
        if ( ( lookups != null ) && ( lookups.size() > 0 ) ) {
            ScriptProcessor sp = ScriptProcessor.getInstance ( script );
            ogs = sp.substitute ( gs, script, language, lookups );
        } else {
            ogs = gs;
        }
        return ogs;
    }

    static class SimpleSubtable extends GlyphSubstitutionSubtable {
        private int[] map;
        public SimpleSubtable ( String id, int sequence, int flags, int format, List coverage, List entries ) {
            super ( id, sequence, flags, format, GlyphCoverageTable.createCoverageTable ( coverage ) );
            populate ( entries );
        }
        /** {@inheritDoc} */
        public int getType() {
            return GSUB_LOOKUP_TYPE_SINGLE;
        }
        /** {@inheritDoc} */
        public List getEntries() {
            List entries = new ArrayList ( map.length );
            for ( int i = 0, n = map.length; i < n; i++ ) {
                entries.add ( Integer.valueOf ( map[i] ) );
            }
            return entries;
        }
        /** {@inheritDoc} */
        public GlyphSequence substitute ( GlyphSequence gs, String script, String language ) {
            CharBuffer cb = CharBuffer.allocate ( gs.length() );
            int ng = 0;
            for ( int i = 0; i < gs.length(); i++ ) {
                int gi = gs.charAt ( i );
                int ci, go = gi;
                if ( ( ci = getCoverageIndex ( gi ) ) >= 0 ) {
                    assert ci < map.length : "coverage index out of range";
                    if ( ci < map.length ) {
                        go = map [ ci ];
                    }
                }
                if ( ( go < 0 ) || ( go > 65535 ) ) {
                    go = 65535;
                }
                cb.put ( (char) go );
                ng++;
            }
            cb.limit(ng);
            cb.rewind();
            return new GlyphSequence ( gs.getCharacters(), (CharSequence) cb, null );
        }
        private void populate ( List entries ) {
            int i = 0, n = entries.size();
            int[] map = new int [ n ];
            for ( Iterator it = entries.iterator(); it.hasNext();) {
                Object o = it.next();
                if ( o instanceof Integer ) {
                    int gid = ( (Integer) o ) .intValue();
                    if ( ( gid >= 0 ) && ( gid < 65536 ) ) {
                        map [ i++ ] = gid;
                    } else {
                        throw new IllegalArgumentException ( "illegal glyph index: " + gid );
                    }
                } else {
                    throw new IllegalArgumentException ( "illegal entries entry, must be Integer: " + o );
                }
            }
            assert i == n;
            assert this.map == null;
            this.map = map;
        }
        /** {@inheritDoc} */
        public String toString() {
            StringBuffer sb = new StringBuffer(super.toString());
            sb.append('{');
            sb.append("coverage=");
            sb.append(getCoverage().toString());
            sb.append(",entries={");
            for ( int i = 0, n = map.length; i < n; i++ ) {
                if ( i > 0 ) {
                    sb.append(',');
                }
                sb.append(Integer.toString(map[i]));
            }
            sb.append('}');
            sb.append('}');
            return sb.toString();
        }
    }

    static class MultipleSubtable extends GlyphSubstitutionSubtable {
        public MultipleSubtable ( String id, int sequence, int flags, int format, List coverage, List entries ) {
            super ( id, sequence, flags, format, GlyphCoverageTable.createCoverageTable ( coverage ) );
        }
        /** {@inheritDoc} */
        public int getType() {
            return GSUB_LOOKUP_TYPE_MULTIPLE;
        }
        /** {@inheritDoc} */
        public List getEntries() {
            return null; // [TBD] - implement me
        }
    }

    static class AlternateSubtable extends GlyphSubstitutionSubtable {
        public AlternateSubtable ( String id, int sequence, int flags, int format, List coverage, List entries ) {
            super ( id, sequence, flags, format, GlyphCoverageTable.createCoverageTable ( coverage ) );
        }
        /** {@inheritDoc} */
        public int getType() {
            return GSUB_LOOKUP_TYPE_ALTERNATE;
        }
        /** {@inheritDoc} */
        public List getEntries() {
            return null; // [TBD] - implement me
        }
    }

    static class LigatureSubtable extends GlyphSubstitutionSubtable {
        private LigatureSet[] map;
        public LigatureSubtable ( String id, int sequence, int flags, int format, List coverage, List entries ) {
            super ( id, sequence, flags, format, GlyphCoverageTable.createCoverageTable ( coverage ) );
            populate ( entries );
        }
        /** {@inheritDoc} */
        public int getType() {
            return GSUB_LOOKUP_TYPE_LIGATURE;
        }
        /** {@inheritDoc} */
        public List getEntries() {
            List entries = new ArrayList ( map.length );
            for ( int i = 0, n = map.length; i < n; i++ ) {
                entries.add ( map[i] );
            }
            return entries;
        }
        /** {@inheritDoc} */
        public GlyphSequence substitute ( GlyphSequence gs, String script, String language ) {
            CharBuffer cb = CharBuffer.allocate ( gs.length() );
            int ng = 0;
            for ( int i = 0, n = gs.length(); i < n; i++ ) {
                int gi = gs.charAt ( i );
                int ci, go = gi;
                LigatureSet ls = null;
                if ( ( ci = getCoverageIndex ( gi ) ) >= 0 ) {
                    assert ci < map.length : "coverage index out of range";
                    if ( ci < map.length ) {
                        ls = map [ ci ];
                    }
                }
                if ( ls != null ) {
                    Ligature l;
                    if ( ( l = findLigature ( ls, gs, i ) ) != null ) {
                        go = l.getLigature();
                        i += l.getNumComponents();
                    }
                }
                if ( ( go < 0 ) || ( go > 65535 ) ) {
                    go = 65535;
                }
                cb.put ( (char) go );
                ng++;
            }
            cb.limit(ng);
            cb.rewind();
            return new GlyphSequence ( gs.getCharacters(), (CharSequence) cb, null );
        }
        private void populate ( List entries ) {
            int i = 0, n = entries.size();
            LigatureSet[] map = new LigatureSet [ n ];
            for ( Iterator it = entries.iterator(); it.hasNext();) {
                Object o = it.next();
                if ( o instanceof LigatureSet ) {
                    map [ i++ ] = (LigatureSet) o;
                } else {
                    throw new IllegalArgumentException ( "illegal ligatures entry, must be LigatureSet: " + o );
                }
            }
            assert i == n;
            assert this.map == null;
            this.map = map;
        }
        private Ligature findLigature ( LigatureSet ls, CharSequence cs, int offset ) {
            Ligature[] la = ls.getLigatures();
            int k = -1;
            int maxComponents = -1;
            for ( int i = 0, n = la.length; i < n; i++ ) {
                Ligature l = la [ i ];
                if ( l.matchesComponents ( cs, offset + 1 ) ) {
                    int nc = l.getNumComponents();
                    if ( nc > maxComponents ) {
                        maxComponents = nc;
                        k = i;
                    }
                }
            }
            if ( k >= 0 ) {
                return la [ k ];
            } else {
                return null;
            }
        }
        /** {@inheritDoc} */
        public String toString() {
            StringBuffer sb = new StringBuffer(super.toString());
            sb.append('{');
            sb.append("coverage=");
            sb.append(getCoverage().toString());
            sb.append(",entries={");
            for ( int i = 0, n = map.length; i < n; i++ ) {
                if ( i > 0 ) {
                    sb.append(',');
                }
                sb.append(map[i]);
            }
            sb.append('}');
            sb.append('}');
            return sb.toString();
        }
    }

    static class ContextSubtable extends GlyphSubstitutionSubtable {
        public ContextSubtable ( String id, int sequence, int flags, int format, List coverage, List entries ) {
            super ( id, sequence, flags, format, GlyphCoverageTable.createCoverageTable ( coverage ) );
        }
        /** {@inheritDoc} */
        public int getType() {
            return GSUB_LOOKUP_TYPE_CONTEXT;
        }
        /** {@inheritDoc} */
        public List getEntries() {
            return null; // [TBD] - implement me
        }
    }

    static class ChainingContextSubtable extends GlyphSubstitutionSubtable {
        public ChainingContextSubtable ( String id, int sequence, int flags, int format, List coverage, List entries ) {
            super ( id, sequence, flags, format, GlyphCoverageTable.createCoverageTable ( coverage ) );
        }
        /** {@inheritDoc} */
        public int getType() {
            return GSUB_LOOKUP_TYPE_CHAINING_CONTEXT;
        }
        /** {@inheritDoc} */
        public List getEntries() {
            return null; // [TBD] - implement me
        }
    }

    static class ExtensionSubtable extends GlyphSubstitutionSubtable {
        public ExtensionSubtable ( String id, int sequence, int flags, int format, List coverage, List entries ) {
            super ( id, sequence, flags, format, GlyphCoverageTable.createCoverageTable ( coverage ) );
        }
        /** {@inheritDoc} */
        public int getType() {
            return GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION;
        }
        /** {@inheritDoc} */
        public List getEntries() {
            return null; // [TBD] - implement me
        }
    }

    static class ReverseChainingSingleSubtable extends GlyphSubstitutionSubtable {
        public ReverseChainingSingleSubtable ( String id, int sequence, int flags, int format, List coverage, List entries ) {
            super ( id, sequence, flags, format, GlyphCoverageTable.createCoverageTable ( coverage ) );
        }
        /** {@inheritDoc} */
        public int getType() {
            return GSUB_LOOKUP_TYPE_REVERSE_CHAINING_CONTEXT_SINGLE;
        }
        /** {@inheritDoc} */
        public List getEntries() {
            return null; // [TBD] - implement me
        }
    }

    /**
     * The <code>Ligature</code> class implements a ligature lookup result in terms of
     * a ligature glyph (code) and the <emph>N+1...</emph> components that comprise the ligature,
     * where the <emph>Nth</emph> component was consumed in the coverage table lookup mapping to
     * this ligature instance.
     */
    public static class Ligature {

        private final int ligature;                     // (resulting) ligature glyph 
        private final int[] components;                 // component glyph codes (note that first component is implied)

        /**
         * Instantiate a ligature.
         * @param ligature glyph id
         * @param components sequence of <emph>N+1...</emph> component glyph (or character) identifiers
         */
        public Ligature ( int ligature, int[] components ) {
            if ( ( ligature < 0 ) || ( ligature > 65535 ) ) {
                throw new IllegalArgumentException ( "invalid ligature glyph index: " + ligature );
            } else if ( ( components == null ) || ( components.length == 0 ) ) {
                throw new IllegalArgumentException ( "invalid ligature components, must be non-empty array" );
            } else {
                for ( int i = 0, n = components.length; i < n; i++ ) {
                    int c = components [ i ];
                    if ( ( c < 0 ) || ( c > 65535 ) ) {
                        throw new IllegalArgumentException ( "invalid component glyph index: " + c );
                    }
                }
                this.ligature = ligature;
                this.components = components;
            }
        }

        /** @return ligature glyph id */
        public int getLigature() {
            return ligature;
        }

        /** @return array of <emph>N+1...</emph> components */
        public int[] getComponents() {
            return components;
        }

        /** @return components count */
        public int getNumComponents() {
            return components.length;
        }

        /**
         * Determine of input sequence at offset matches ligature's components.
         * @param cs glyph (or character) sequence to match this ligature against
         * @param offset index at which to start matching the components of this ligature
         * @return true if matches
         */
        public boolean matchesComponents ( CharSequence cs, int offset ) {
            if ( ( offset + components.length ) > cs.length() ) {
                return false;
            } else {
                for ( int i = 0, n = components.length; i < n; i++ ) {
                    if ( (int) cs.charAt ( offset + i ) != components [ i ] ) {
                        return false;
                    }
                }
                return true;
            }
        }

        /** {@inheritDoc} */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("{components={");
            for ( int i = 0, n = components.length; i < n; i++ ) {
                if ( i > 0 ) {
                    sb.append(',');
                }
                sb.append(Integer.toString(components[i]));
            }
            sb.append("},ligature=");
            sb.append(Integer.toString(ligature));
            sb.append("}");
            return sb.toString();
        }

    }

    /**
     * The <code>LigatureSet</code> class implements a set of  ligatures.
     */
    public static class LigatureSet {

        private final Ligature[] ligatures;                     // set of ligatures all of which share the first (implied) component

        /**
         * Instantiate a set of ligatures.
         * @param ligatures collection of ligatures
         */
        public LigatureSet ( List ligatures ) {
            this ( (Ligature[]) ligatures.toArray ( new Ligature [ ligatures.size() ] ) );
        }

        /**
         * Instantiate a set of ligatures.
         * @param ligatures array of ligatures
         */
        public LigatureSet ( Ligature[] ligatures ) {
            if ( ( ligatures == null ) || ( ligatures.length == 0 ) ) {
                throw new IllegalArgumentException ( "invalid ligatures, must be non-empty array" );
            } else {
                this.ligatures = ligatures;
            }
        }

        /** @return array of ligatures in this ligature set */
        public Ligature[] getLigatures() {
            return ligatures;
        }

        /** @return count of ligatures in this ligature set */
        public int getNumLigatures() {
            return ligatures.length;
        }

        /** {@inheritDoc} */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("{ligs={");
            for ( int i = 0, n = ligatures.length; i < n; i++ ) {
                if ( i > 0 ) {
                    sb.append(',');
                }
                sb.append(ligatures[i]);
            }
            sb.append("}}");
            return sb.toString();
        }

    }

}
