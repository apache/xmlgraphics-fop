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

package org.apache.fop.layoutmgr;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.LineArea;
import org.apache.fop.area.inline.Anchor;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineBlockParent;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.area.inline.InlineViewport;
import org.apache.fop.fo.CharIterator;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.flow.AbstractPageNumberCitation;
import org.apache.fop.fo.flow.BidiOverride;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InlineContainer;
import org.apache.fop.fo.flow.InlineLevel;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.WritingModeTraitsGetter;
import org.apache.fop.text.bidi.BidiClassUtils;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.util.BidiConstants;

// CSOFF: AvoidNestedBlocksCheck
// CSOFF: EmptyForIteratorPadCheck
// CSOFF: NoWhitespaceAfterCheck
// CSOFF: InnerAssignmentCheck
// CSOFF: SimplifyBooleanReturnCheck
// CSOFF: LineLengthCheck
// CSOFF: ParameterNumberCheck

/**
 * <p>A utility class for performing bidirectional processing.</p>
 * @author Glenn Adams
 */
public final class BidiUtil {

    /**
     * logging instance
     */
    private static final Log log = LogFactory.getLog(BidiUtil.class);                                                   // CSOK: ConstantNameCheck

    private BidiUtil() {
    }

    /**
     * Resolve inline directionality.
     * @param ps a page sequence FO instance
     */
    public static void resolveInlineDirectionality ( PageSequence ps ) {
        if (log.isDebugEnabled()) {
            log.debug ( "BD: RESOLVE: " + ps );
        }
        List ranges = pruneEmptyRanges ( collectRanges ( ps, new Stack() ) );
        resolveInlineDirectionality ( ranges );
    }

    /**
     * Reorder line area.
     * @param la a line area instance
     */
    public static void reorder ( LineArea la ) {

        // 1. collect inline levels
        List runs = collectRuns ( la.getInlineAreas(), new Vector() );
        if (log.isDebugEnabled()) {
            dumpRuns ( "BD: REORDER: INPUT:", runs );
        }

        // 2. split heterogeneous inlines
        runs = splitRuns ( runs );
        if (log.isDebugEnabled()) {
            dumpRuns ( "BD: REORDER: SPLIT INLINES:", runs );
        }

        // 3. determine minimum and maximum levels
        int[] mm = computeMinMaxLevel ( runs, null );
        if (log.isDebugEnabled()) {
            log.debug( "BD: REORDER: { min = " + mm[0] + ", max = " + mm[1] + "}" );
        }

        // 4. reorder from maximum level to minimum odd level
        int mn = mm[0];
        int mx = mm[1];
        for ( int l1 = mx, l2 = ( ( mn & 1 ) == 0 ) ? ( mn + 1 ) : mn; l1 >= l2; l1-- ) {
            runs = reorderRuns ( runs, l1 );
        }
        if (log.isDebugEnabled()) {
            dumpRuns ( "BD: REORDER: REORDERED RUNS:", runs );
        }

        // 5. reverse word consituents (characters and glyphs) while mirroring
        boolean mirror = true;
        reverseWords ( runs, mirror );
        if (log.isDebugEnabled()) {
            dumpRuns ( "BD: REORDER: REORDERED WORDS:", runs );
        }

        // 6. replace line area's inline areas with reordered runs' inline areas
        replaceInlines ( la, replicateSplitWords ( runs ) );
    }

    private static void resolveInlineDirectionality ( List ranges ) {
        for ( Iterator it = ranges.iterator(); it.hasNext(); ) {
            DelimitedTextRange r = (DelimitedTextRange) it.next();
            r.resolve();
            if (log.isDebugEnabled()) {
                log.debug ( r );
            }
        }
    }

    /**
     * Collect the sequence of delimited text ranges of node FO, where each new
     * range is pushed onto RANGES.
     */
    private static Stack collectRanges ( FONode fn, Stack ranges ) {
        // return existing ranges if passed null node
        if ( fn == null ) {
            return ranges;
        }
        // if boundary before, then push new range
        if ( isRangeBoundaryBefore ( fn ) ) {
            maybeNewRange ( ranges, fn );
        }
        // get current range, if one exists
        DelimitedTextRange r;
        if ( ranges.size() > 0 ) {
            r = (DelimitedTextRange) ranges.peek();
        } else {
            r = null;
        }
        // proceses this node
        if ( fn instanceof FOText ) {
            if ( r != null ) {
                r.append ( ( (FOText) fn ) .charIterator(), fn );
            }
        } else if ( fn instanceof Character ) {
            if ( r != null ) {
                r.append ( ( (Character) fn ) .charIterator(), fn );
            }
        } else if ( fn instanceof BidiOverride ) {
            if ( r != null ) {
                ranges = collectBidiOverrideRanges ( (BidiOverride) fn, r, ranges );
            }
        } else if ( fn instanceof PageSequence ) {
            ranges = collectRanges ( ( (PageSequence) fn ) .getMainFlow(), ranges );
        } else {
            for ( Iterator it = fn.getChildNodes(); ( it != null ) && it.hasNext(); ) {
                ranges = collectRanges ( (FONode) it.next(), ranges );
            }
        }
        // if boundary after, then push new range
        if ( isRangeBoundaryAfter ( fn ) ) {
            maybeNewRange ( ranges, fn );
        }
        return ranges;
    }

    private static Stack collectBidiOverrideRanges ( BidiOverride bo, DelimitedTextRange r, Stack ranges ) {
        char pfx = 0;
        char sfx = 0;
        int unicodeBidi = bo.getUnicodeBidi();
        int direction = bo.getDirection();
        if ( unicodeBidi == Constants.EN_BIDI_OVERRIDE ) {
            pfx = ( direction == Constants.EN_RTL ) ? CharUtilities.RLO : CharUtilities.LRO;
            sfx = CharUtilities.PDF;
        } else if ( unicodeBidi == Constants.EN_EMBED ) {
            pfx = ( direction == Constants.EN_RTL ) ? CharUtilities.RLE : CharUtilities.LRE;
            sfx = CharUtilities.PDF;
        }
        if ( pfx != 0 ) {
            r.append ( pfx, bo );
        }
        for ( Iterator it = bo.getChildNodes(); ( it != null ) && it.hasNext(); ) {
            ranges = collectRanges ( (FONode) it.next(), ranges );
        }
        if ( sfx != 0 ) {
            r.append ( sfx, bo );
        }
        return ranges;
    }

    private static List collectRuns ( List inlines, List runs ) {
        for ( Iterator it = inlines.iterator(); it.hasNext(); ) {
            InlineArea ia = (InlineArea) it.next();
            if ( ia instanceof WordArea ) {
                runs = collectRuns ( (WordArea) ia, runs );
            } else if ( ia instanceof SpaceArea ) {
                runs = collectRuns ( (SpaceArea) ia, runs );
            } else if ( ia instanceof InlineParent ) {
                runs = collectRuns ( (InlineParent) ia, runs );
            } else if ( ia instanceof InlineViewport ) {
                runs = collectRuns ( (InlineViewport) ia, runs );
            } else if ( ia instanceof Leader ) {
                runs = collectRuns ( (Leader) ia, runs );
            } else if ( ia instanceof Space ) {
                runs = collectRuns ( (Space) ia, runs );
            } else if ( ia instanceof Anchor ) {
                runs = collectRuns ( (Anchor) ia, runs );
            } else if ( ia instanceof InlineBlockParent ) {
                runs = collectRuns ( (InlineBlockParent) ia, runs );
            }
        }
        return runs;
    }

    private static List collectRuns ( Anchor a, List runs ) {
        return runs;
    }

    private static List collectRuns ( InlineBlockParent a, List runs ) {
        return runs;
    }

    private static List collectRuns ( InlineParent a, List runs ) {
        return collectRuns ( a.getChildAreas(), runs );
    }

    private static List collectRuns ( Leader a, List runs ) {
        return runs;
    }

    private static List collectRuns ( Space a, List runs ) {
        return runs;
    }

    private static List collectRuns ( SpaceArea a, List runs ) {
        runs.add ( new InlineRun ( a, new int[] {a.getBidiLevel()}) );
        return runs;
    }

    private static List collectRuns ( InlineViewport a, List runs ) {
        return runs;
    }

    private static List collectRuns ( WordArea a, List runs ) {
        runs.add ( new InlineRun ( a, a.getBidiLevels() ) );
        return runs;
    }

    private static List splitRuns ( List runs ) {
        List runsNew = new Vector();
        for ( Iterator it = runs.iterator(); it.hasNext(); ) {
            InlineRun ir = (InlineRun) it.next();
            if ( ir.isHomogenous() ) {
                runsNew.add ( ir );
            } else {
                runsNew.addAll ( ir.split() );
            }
        }
        if ( ! runsNew.equals ( runs ) ) {
            runs = runsNew;
        }
        return runs;
    }

    private static int[] computeMinMaxLevel ( List runs, int[] mm ) {
        if ( mm == null ) {
            mm = new int[] {Integer.MAX_VALUE, Integer.MIN_VALUE};
        }
        for ( Iterator it = runs.iterator(); it.hasNext(); ) {
            InlineRun ir = (InlineRun) it.next();
            ir.updateMinMax ( mm );
        }
        return mm;
    }
    private static List reorderRuns ( List runs, int level ) {
        List runsNew = new Vector();
        for ( int i = 0, n = runs.size(); i < n; i++ ) {
            InlineRun iri = (InlineRun) runs.get(i);
            if ( iri.getMinLevel() < level ) {
                runsNew.add ( iri );
            } else {
                int s = i;
                int e = s;
                while ( e < n ) {
                    InlineRun ire = (InlineRun) runs.get(e);
                    if ( ire.getMinLevel() < level ) {
                        break;
                    } else {
                        e++;
                    }
                }
                if ( s < e ) {
                    runsNew.addAll ( reverseRuns ( runs, s, e ) );
                }
                i = e - 1;
            }
        }
        if ( ! runsNew.equals ( runs ) ) {
            runs = runsNew;
        }
        return runs;
    }
    private static List reverseRuns ( List runs, int s, int e ) {
        int n = e - s;
        Vector runsNew = new Vector ( n );
        if ( n > 0 ) {
            for ( int i = 0; i < n; i++ ) {
                int k = ( n - i - 1 );
                InlineRun ir = (InlineRun) runs.get(s + k);
                ir.reverse();
                runsNew.add ( ir );
            }
        }
        return runsNew;
    }
    private static void reverseWords ( List runs, boolean mirror ) {
        for ( Iterator it = runs.iterator(); it.hasNext(); ) {
            InlineRun ir = (InlineRun) it.next();
            ir.maybeReverseWord ( mirror );
        }
    }
    private static List replicateSplitWords ( List runs ) {
        // [TBD] for each run which inline word area appears multiple times in
        // runs, replicate that word
        return runs;
    }
    private static void replaceInlines ( LineArea la, List runs ) {
        List inlines = new ArrayList();
        for ( Iterator it = runs.iterator(); it.hasNext(); ) {
            InlineRun ir = (InlineRun) it.next();
            inlines.add ( ir.getInline() );
        }
        inlines = unflattenInlines ( inlines );
        la.setInlineAreas ( inlines );
    }
    private static List unflattenInlines ( List inlines ) {
        List inlinesNew = new ArrayList();                              // unflattened inlines being consed
        TextArea tLast = null;                                          // last text area parent
        TextArea tNew = null;                                           // new text area being consed
        int lLast = -1;                                                 // last bidi level
        for ( Iterator it = inlines.iterator(); it.hasNext(); ) {
            InlineArea ia = (InlineArea) it.next();
            if ( ( ia instanceof WordArea ) || ( ia instanceof SpaceArea ) ) {
                TextArea t = (TextArea) ia.getParentArea();
                int l = ia.getBidiLevel();
                if ( isEndOfTextArea ( t, tLast, l, lLast ) ) {
                    if ( tNew != null ) {
                        inlinesNew.add ( tNew );
                        tNew = null;
                    }
                }
                if ( tNew == null ) {
                    tNew = createUnflattenedText ( t );
                }
                tNew.addChildArea ( ia );
                tLast = t;
                lLast = l;
            } else {
                inlinesNew.add ( ia );
            }
        }
        if ( tNew != null ) {
            inlinesNew.add ( tNew );
        }
        return inlinesNew;
    }
    private static boolean isEndOfTextArea ( TextArea t, TextArea tLast, int level, int levelLast ) {
        if ( ( tLast != null ) && ( t != tLast ) ) {
            return true;
        } else if ( ( levelLast != -1 ) && ( level != levelLast ) ) {
            return true;
        } else {
            return false;
        }
    }
    private static TextArea createUnflattenedText ( TextArea t ) {
        TextArea tNew = new TextArea();
        if ( t != null ) {
            tNew.setBPD ( t.getBPD() );
            tNew.setTraits ( t.getTraits() );
            tNew.setBlockProgressionOffset ( t.getBlockProgressionOffset() );
            tNew.setBaselineOffset ( t.getBaselineOffset() );
        }
        return tNew;
    }
    private static void dumpRuns ( String header, List runs ) {
        log.debug ( header );
        for ( Iterator it = runs.iterator(); it.hasNext(); ) {
            InlineRun ir = (InlineRun) it.next();
            log.debug ( ir );
        }
    }

    /**
     * <p>Conditionally add a new delimited text range to RANGES, where new range is
     * associated with node FN. A new text range is added unless all of the following are true:</p>
     * <ul>
     * <li>there exists a current range RCUR in RANGES</li>
     * <li>RCUR is empty</li>
     * <li>the node of the RCUR is the same node as FN or a descendent node of FN</li>
     * </ul>
     */
    private static DelimitedTextRange maybeNewRange ( Stack ranges, FONode fn ) {
        DelimitedTextRange rCur = null;
        DelimitedTextRange rNew = null;
        if ( ranges.empty() ) {
            if ( fn instanceof Block ) {
                rNew = new DelimitedTextRange(fn);
            }
        } else if ( ( rCur = (DelimitedTextRange) ranges.peek() ) != null ) {
            if ( ! rCur.isEmpty() || ! isSelfOrDescendent ( rCur.getNode(), fn ) ) {
                rNew = new DelimitedTextRange(fn);
            }
        }
        if ( rNew != null ) {
            ranges.push ( rNew );
        } else {
            rNew = rCur;
        }
        return rNew;
    }

    /**
     * Determine if node N2 is the same or a descendent of node N1.
     */
    private static boolean isSelfOrDescendent ( FONode n1, FONode n2 ) {
        for ( FONode n = n2; n != null; n = n.getParent() ) {
            if ( n == n1 ) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRangeBoundary ( FONode fn ) {
        if ( fn instanceof Block ) {                                    // fo:block
            return true;
        } else if ( fn instanceof InlineLevel ) {                       // fo:inline, fo:leader, fo:bidi-override, fo:title
            return false;
        } else if ( fn instanceof InlineContainer ) {                   // fo:inline-container
            return false;
        } else if ( fn instanceof BlockContainer ) {                    // fo:block-container
            return true;
        } else if ( fn instanceof AbstractPageNumberCitation ) {        // fo:page-number-citation, fo:page-number-citation-last
            return false;
        } else if ( fn instanceof PageNumber ) {                        // fo:page-number
            return false;
        } else if ( fn instanceof ExternalGraphic ) {                   // fo:external-graphic
            return false;
        } else if ( fn instanceof FOText ) {                            // #PCDATA
            return false;
        } else {
            return true;
        }
    }

    private static boolean isRangeBoundaryBefore ( FONode fn ) {
        return isRangeBoundary ( fn );
    }

    private static boolean isRangeBoundaryAfter ( FONode fn ) {
        return isRangeBoundary ( fn );
    }

    private static List pruneEmptyRanges ( Stack ranges ) {
        Vector rv = new Vector();
        for ( Iterator it = ranges.iterator(); it.hasNext(); ) {
            DelimitedTextRange r = (DelimitedTextRange) it.next();
            if ( ! r.isEmpty() ) {
                rv.add ( r );
            }
        }
        return rv;
    }

    private static String padLeft ( int n, int width ) {
        return padLeft ( Integer.toString ( n ), width );
    }

    private static String padLeft ( String s, int width ) {
        StringBuffer sb = new StringBuffer();
        for ( int i = s.length(); i < width; i++ ) {
            sb.append(' ');
        }
        sb.append ( s );
        return sb.toString();
    }

    /* not used yet
    private static String padRight ( int n, int width ) {
        return padRight ( Integer.toString ( n ), width );
    }
    */

    private static String padRight ( String s, int width ) {
        StringBuffer sb = new StringBuffer ( s );
        for ( int i = sb.length(); i < width; i++ ) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static class DelimitedTextRange {
        private FONode fn;                              // node that generates this text range
        private StringBuffer buffer;                    // flattened character sequence of generating FO nodes
        private List intervals;                         // list of intervals over buffer of generating FO nodes
        DelimitedTextRange ( FONode fn ) {
            this.fn = fn;
            this.buffer = new StringBuffer();
            this.intervals = new Vector();
        }
        FONode getNode() {
            return fn;
        }
        void append ( CharIterator it, FONode fn ) {
            if ( it != null ) {
                int s = buffer.length();
                int e = s;
                while ( it.hasNext() ) {
                    char c = it.nextChar();
                    buffer.append ( c );
                    e++;
                }
                intervals.add ( new TextInterval ( fn, s, e ) );
            }
        }
        void append ( char c, FONode fn ) {
            if ( c != 0 ) {
                int s = buffer.length();
                int e = s + 1;
                buffer.append ( c );
                intervals.add ( new TextInterval ( fn, s, e ) );
            }
        }
        boolean isEmpty() {
            return buffer.length() == 0;
        }
        void resolve() {
            WritingModeTraitsGetter tg;
            if ( ( tg = getWritingModeTraitsGetter ( getNode() ) ) != null ) {
                resolve ( tg.getInlineProgressionDirection() );
            }
        }
        public String toString() {
            StringBuffer sb = new StringBuffer ( "DR: " + fn.getLocalName() + "{ <" + CharUtilities.toNCRefs ( buffer.toString() ) + ">" );
            sb.append ( ", intervals <" );
            boolean first = true;
            for ( Iterator it = intervals.iterator(); it.hasNext(); ) {
                TextInterval ti = (TextInterval) it.next();
                if ( first ) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append ( ti.toString() );
            }
            sb.append(">}");
            return sb.toString();
        }
        private void resolve ( Direction paragraphEmbeddingLevel ) {
            int [] levels;
            if ( ( levels = UnicodeBidiAlgorithm.resolveLevels ( buffer, paragraphEmbeddingLevel ) ) != null ) {
                assignLevels ( levels );
                assignTextLevels();
                assignBlockLevel(paragraphEmbeddingLevel);
            }
        }
        /**
         * <p>Assign resolved levels to all text intervals of this delimited text range.</p>
         * <p>Has a possible side effect of replacing the intervals array with a new array
         * containing new text intervals, such that each text interval is associated with
         * a single level run.</p>
         * @param levels array of levels each corresponding to each index of the delimited
         * text range
         */
        private void assignLevels ( int[] levels ) {
            Vector intervalsNew = new Vector ( intervals.size() );
            for ( Iterator it = intervals.iterator(); it.hasNext(); ) {
                TextInterval ti = (TextInterval) it.next();
                intervalsNew.addAll ( assignLevels ( ti, levels ) );
            }
            if ( ! intervalsNew.equals ( intervals ) ) {
                intervals = intervalsNew;
            }
        }
        /**
         * <p>Assign resolved levels to a specified text interval over this delimited text
         * range.</p>
         * <p>Returns a list of text intervals containing either (1) the single, input text
         * interval or (2) two or more new text intervals obtained from sub-dividing the input
         * text range into level runs, i.e., runs of text assigned to a single level.</p>
         * @param ti a text interval to which levels are to be assigned
         * @param levels array of levels each corresponding to each index of the delimited
         * text range
         * @return a list of text intervals as described above
         */
        private List assignLevels ( TextInterval ti, int[] levels ) {
            Vector tiv = new Vector();
            FONode fn = ti.getNode();
            int fnStart = ti.getStart();                                     // start of node's text in delimited text range
            for ( int i = fnStart, n = ti.getEnd(); i < n; ) {
                int s = i;                                              // inclusive start index of interval in delimited text range
                int e = s;                                              // exclusive end index of interval in delimited text range
                int l = levels [ s ];                                   // current run level
                while ( e < n ) {                                       // skip to end of run level or end of interval
                    if ( levels [ e ] != l ) {
                        break;
                    } else {
                        e++;
                    }
                }
                if ( ( ti.getStart() == s ) && ( ti.getEnd() == e ) ) {
                    ti.setLevel ( l );                                       // reuse interval, assigning it single level
                } else {
                    ti = new TextInterval ( fn, fnStart, s, e, l );     // subdivide interval
                }
                if (log.isDebugEnabled()) {
                    log.debug ( "AL(" + l + "): " + ti );
                }
                tiv.add ( ti );
                i = e;
            }
            return tiv;
        }
        /**
         * <p>Assign resolved levels for each interval to source #PCDATA in the associated FOText.</p>
         */
        private void assignTextLevels() {
            for ( Iterator it = intervals.iterator(); it.hasNext(); ) {
                TextInterval ti = (TextInterval) it.next();
                ti.assignTextLevels();
            }
        }
        private void assignBlockLevel ( Direction paragraphEmbeddingLevel ) {
            int defaultLevel = ( paragraphEmbeddingLevel == Direction.RL ) ? 1 : 0;
            for ( Iterator it = intervals.iterator(); it.hasNext(); ) {
                TextInterval ti = (TextInterval) it.next();
                assignBlockLevel ( ti.getNode(), defaultLevel );
            }
        }
        private void assignBlockLevel ( FONode node, int defaultLevel ) {
            for ( FONode fn = node; fn != null; fn = fn.getParent() ) {
                if ( fn instanceof Block ) {
                    Block bn = (Block) fn;
                    if ( bn.getBidiLevel() < 0 ) {
                        bn.setBidiLevel ( defaultLevel );
                    }
                    break;
                }
            }
        }
        private WritingModeTraitsGetter getWritingModeTraitsGetter ( FONode fn ) {
            for ( FONode n = fn; n != null; n = n.getParent() ) {
                if ( n instanceof WritingModeTraitsGetter ) {
                    return (WritingModeTraitsGetter) n;
                }
            }
            return null;
        }
    }

    private static class TextInterval {
        private FONode fn;              // associated node
        private int textStart;          // starting index within delimited text range of associated node's text
        private int start;              // starting index within delimited text range
        private int end;                // ending index within delimited text range
        private int level;              // resolved level or default (-1)
        TextInterval ( FONode fn, int start, int end ) {
            this ( fn, start, start, end, -1 );
        }
        TextInterval ( FONode fn, int textStart, int start, int end, int level ) {
            this.fn = fn;
            this.textStart = textStart;
            this.start = start;
            this.end = end;
            this.level = level;
        }
        FONode getNode() {
            return fn;
        }
        int getTextStart() {
            return textStart;
        }
        int getStart() {
            return start;
        }
        int getEnd() {
            return end;
        }
        int getLevel() {
            return level;
        }
        void setLevel ( int level ) {
            this.level = level;
        }
        public int length() {
            return end - start;
        }
        public String getText() {
            if ( fn instanceof FOText ) {
                return new String ( ( (FOText) fn ) .getCharArray() );
            } else if ( fn instanceof Character ) {
                return new String ( new char[] {( (Character) fn ) .getCharacter()} );
            } else {
                return null;
            }
        }
        public void assignTextLevels() {
            if ( fn instanceof FOText ) {
                ( (FOText) fn ) .setBidiLevel ( level, start - textStart, end - textStart );
            } else if ( fn instanceof Character ) {
                ( (Character) fn ) .setBidiLevel ( level );
            }
        }
        public boolean equals ( Object o ) {
            if ( o instanceof TextInterval ) {
                TextInterval ti = (TextInterval) o;
                if ( ti.getNode() != fn ) {
                    return false;
                } else if ( ti.getStart() != start ) {
                    return false;
                } else if ( ti.getEnd() != end ) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
        public int hashCode() {
            int l = ( fn != null ) ? fn.hashCode() : 0;
            l = ( l ^ start ) + ( l << 19 );
            l = ( l ^ end )   + ( l << 11 );
            return l;
        }
        public String toString() {
            StringBuffer sb = new StringBuffer();
            char c;
            if ( fn instanceof FOText ) {
                c = 'T';
            } else if ( fn instanceof Character ) {
                c = 'C';
            } else if ( fn instanceof BidiOverride ) {
                c = 'B';
            } else {
                c = '?';
            }
            sb.append ( c );
            sb.append ( "[" + start + "," + end + "][" + textStart + "](" + level + ")" );
            return sb.toString();
        }
    }

    /**
     * The <code>InlineRun</code> class is a utility class used to capture a sequence of
     * reordering levels associated with an inline area.
     */
    private static class InlineRun {
        private InlineArea inline;
        private int[] levels;
        private int minLevel;
        private int maxLevel;
        private int reversals;
        InlineRun ( InlineArea inline, int[] levels ) {
            this.inline = inline;
            this.levels = levels;
            setMinMax ( levels );
        }
        private InlineRun ( InlineArea inline, int level, int count ) {
            this ( inline, makeLevels ( level, count ) );
        }
        InlineArea getInline() {
            return inline;
        }
        int getMinLevel() {
            return minLevel;
        }
        private void setMinMax ( int[] levels ) {
            int mn = Integer.MAX_VALUE;
            int mx = Integer.MIN_VALUE;
            if ( ( levels != null ) && ( levels.length > 0 ) ) {
                for ( int i = 0, n = levels.length; i < n; i++ ) {
                    int l = levels [ i ];
                    if ( l < mn ) {
                        mn = l;
                    }
                    if ( l > mx ) {
                        mx = l;
                    }
                }
            } else {
                mn = mx = -1;
            }
            this.minLevel = mn;
            this.maxLevel = mx;
        }
        public boolean isHomogenous() {
            return minLevel == maxLevel;
        }
        public List split() {
            List runs = new Vector();
            for ( int i = 0, n = levels.length; i < n; ) {
                int l = levels [ i ];
                int s = i;
                int e = s;
                while ( e < n ) {
                    if ( levels [ e ] != l ) {
                        break;
                    } else {
                        e++;
                    }
                }
                if ( s < e ) {
                    runs.add ( new InlineRun ( inline, l, e - s ) );
                }
                i = e;
            }
            assert runs.size() < 2 : "heterogeneous inlines not yet supported!!";
            return runs;
        }
        public void updateMinMax ( int[] mm ) {
            if ( minLevel < mm[0] ) {
                mm[0] = minLevel;
            }
            if ( maxLevel > mm[1] ) {
                mm[1] = maxLevel;
            }
        }
        public boolean maybeNeedsMirroring() {
            return ( minLevel == maxLevel ) && ( ( minLevel & 1 ) != 0 );
        }
        public void reverse() {
            reversals++;
        }
        public void maybeReverseWord ( boolean mirror ) {
            if ( inline instanceof WordArea ) {
                WordArea w = (WordArea) inline;
                if ( ( reversals & 1 ) != 0 ) {
                    w.reverse ( mirror );
                } else if ( mirror && maybeNeedsMirroring() ) {
                    w.mirror();
                }
            }
        }
        public boolean equals ( Object o ) {
            if ( o instanceof InlineRun ) {
                InlineRun ir = (InlineRun) o;
                if ( ir.inline != inline ) {
                    return false;
                } else if ( ir.minLevel != minLevel ) {
                    return false;
                } else if ( ir.maxLevel != maxLevel ) {
                    return false;
                } else if ( ( ir.levels != null ) && ( levels != null ) ) {
                    if ( ir.levels.length != levels.length ) {
                        return false;
                    } else {
                        for ( int i = 0, n = levels.length; i < n; i++ ) {
                            if ( ir.levels[i] != levels[i] ) {
                                return false;
                            }
                        }
                        return true;
                    }
                } else if ( ( ir.levels == null ) && ( levels == null ) ) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        public int hashCode() {
            int l = ( inline != null ) ? inline.hashCode() : 0;
            l = ( l ^ minLevel ) + ( l << 19 );
            l = ( l ^ maxLevel )   + ( l << 11 );
            return l;
        }
        public String toString() {
            StringBuffer sb = new StringBuffer( "RR: { type = \'" );
            char c;
            String content = null;
            if ( inline instanceof WordArea ) {
                c = 'W';
                content = ( (WordArea) inline ) .getWord();
            } else if ( inline instanceof SpaceArea ) {
                c = 'S';
                content = ( (SpaceArea) inline ) .getSpace();
            } else if ( inline instanceof InlineParent ) {
                c = 'I';
            } else if ( inline instanceof InlineBlockParent ) {
                c = 'B';
            } else if ( inline instanceof InlineViewport ) {
                c = 'V';
            } else if ( inline instanceof Leader ) {
                c = 'L';
            } else if ( inline instanceof Anchor ) {
                c = 'A';
            } else if ( inline instanceof Space ) {
                c = 'G'; // 'G' => glue
            } else {
                c = '?';
            }
            sb.append ( c );
            sb.append ( "\', levels = \'" );
            sb.append ( generateLevels ( levels ) );
            sb.append ( "\', min = " );
            sb.append ( minLevel );
            sb.append ( ", max = " );
            sb.append ( maxLevel );
            sb.append ( ", reversals = " );
            sb.append ( reversals );
            sb.append ( ", content = <" );
            sb.append ( CharUtilities.toNCRefs ( content ) );
            sb.append ( "> }" );
            return sb.toString();
        }
        private String generateLevels ( int[] levels ) {
            StringBuffer lb = new StringBuffer();
            int maxLevel = -1;
            int numLevels = levels.length;
            for ( int i = 0; i < numLevels; i++ ) {
                int l = levels [ i ];
                if ( l > maxLevel ) {
                    maxLevel = l;
                }
            }
            if ( maxLevel < 0 ) {
                // leave level buffer empty
            } else if ( maxLevel < 10 ) {
                // use string of decimal digits
                for ( int i = 0; i < numLevels; i++ ) {
                    lb.append ( (char) ( '0' + levels [ i ] ) );
                }
            } else {
                // use comma separated list
                boolean first = true;
                for ( int i = 0; i < numLevels; i++ ) {
                    if ( first ) {
                        first = false;
                    } else {
                        lb.append(',');
                    }
                    lb.append ( levels [ i ] );
                }
            }
            return lb.toString();
        }
        private static int[] makeLevels ( int level, int count ) {
            int[] levels = new int [ count ];
            Arrays.fill ( levels, level );
            return levels;
        }
    }

    /**
     * The <code>UnicodeBidiAlgorithm</code> class implements functionality prescribed by
     * the Unicode Bidirectional Algorithm, Unicode Standard Annex #9.
     */
    public static final class UnicodeBidiAlgorithm implements BidiConstants {

        private UnicodeBidiAlgorithm() {
        }

        /**
         * Resolve the directionality levels of each character in a character seqeunce.
         * If some character is encoded in the character sequence as a Unicode Surrogate Pair,
         * then the directionality level of each of the two members of the  pair will be identical.
         * @return null if bidirectional processing is not required; otherwise, returns an array
         * of integers, where each integer corresponds to exactly one UTF-16
         * encoding element present in the input character sequence, and where each integer denotes
         * the directionality level of the corresponding encoding element
         * @param cs input character sequence representing a UTF-16 encoded string
         * @param defaultLevel the default paragraph level, which must be zero (LR) or one (RL)
         */
        public static int[] resolveLevels ( CharSequence cs, Direction defaultLevel ) {
            int[] chars = new int [ cs.length() ];
            if ( convertToScalar ( cs, chars ) || ( defaultLevel == Direction.RL ) ) {
                return resolveLevels ( chars, ( defaultLevel == Direction.RL ) ? 1 : 0, new int [ chars.length ] );
            } else {
                return null;
            }
        }

        /**
         * Resolve the directionality levels of each character in a character seqeunce.
         * @return null if bidirectional processing is not required; otherwise, returns an array
         * of integers, where each integer corresponds to exactly one UTF-16
         * encoding element present in the input character sequence, and where each integer denotes
         * the directionality level of the corresponding encoding element
         * @param chars array of input characters represented as unicode scalar values
         * @param defaultLevel the default paragraph level, which must be zero (LR) or one (RL)
         * @param levels array to receive levels, one for each character in chars array
         */
        public static int[] resolveLevels ( int[] chars, int defaultLevel, int[] levels ) {
            return resolveLevels ( chars, getClasses ( chars ), defaultLevel, levels, false );
        }

        /**
         * Resolve the directionality levels of each character in a character seqeunce.
         * @return null if bidirectional processing is not required; otherwise, returns an array
         * of integers, where each integer corresponds to exactly one UTF-16
         * encoding element present in the input character sequence, and where each integer denotes
         * the directionality level of the corresponding encoding element
         * @param chars array of input characters represented as unicode scalar values
         * @param classes array containing one bidi class per character in chars array
         * @param defaultLevel the default paragraph level, which must be zero (LR) or one (RL)
         * @param levels array to receive levels, one for each character in chars array
         * @param useRuleL1 true if rule L1 should be used
         */
        public static int[] resolveLevels ( int[] chars, int[] classes, int defaultLevel, int[] levels, boolean useRuleL1 ) {
            int[] ica = classes;
            int[] wca = copySequence ( ica );
            int[] ea  = new int [ levels.length ];
            resolveExplicit ( wca, defaultLevel, ea );
            resolveRuns ( wca, defaultLevel, ea, levelsFromEmbeddings ( ea, levels ) );
            if ( useRuleL1 ) {
                resolveSeparators ( ica, wca, defaultLevel, levels );
            }
            dump ( "RL: CC(" + ( ( chars != null ) ? chars.length : -1 ) + ")", chars, classes, defaultLevel, levels );
            return levels;
        }

        private static int[] copySequence ( int[] ta ) {
            int[] na = new int [ ta.length ];
            System.arraycopy ( ta, 0, na, 0, na.length );
            return na;
        }

        private static void resolveExplicit ( int[] wca, int defaultLevel, int[] ea ) {
            int[] es = new int [ MAX_LEVELS ];          /* embeddings stack */
            int ei = 0;                                 /* embeddings stack index */
            int ec = defaultLevel;                      /* current embedding level */
            for ( int i = 0, n = wca.length; i < n; i++ ) {
                int bc = wca [ i ];                     /* bidi class of current char */
                int el;                                 /* embedding level to assign to current char */
                switch ( bc ) {
                case LRE:                               // start left-to-right embedding
                case RLE:                               // start right-to-left embedding
                case LRO:                               // start left-to-right override
                case RLO:                               // start right-to-left override
                    {
                        int en;                         /* new embedding level */
                        if ( ( bc == RLE ) || ( bc == RLO ) ) {
                            en = ( ( ec & ~OVERRIDE ) + 1 ) | 1;
                        } else {
                            en = ( ( ec & ~OVERRIDE ) + 2 ) & ~1;
                        }
                        if ( en < ( MAX_LEVELS + 1 ) ) {
                            es [ ei++ ] = ec;
                            if ( ( bc == LRO ) || ( bc == RLO ) ) {
                                ec = en | OVERRIDE;
                            } else {
                                ec = en & ~OVERRIDE;
                            }
                        } else {
                            // max levels exceeded, so don't change level or override
                        }
                        el = ec;
                        break;
                    }
                case PDF:                               // pop directional formatting
                    {
                        el = ec;
                        if ( ei > 0 ) {
                            ec = es [ --ei ];
                        } else {
                            // ignore isolated PDF
                        }
                        break;
                    }
                case B:                                 // paragraph separator
                    {
                        el = ec = defaultLevel;
                        ei = 0;
                        break;
                    }
                default:
                    {
                        el = ec;
                        break;
                    }
                }
                switch ( bc ) {
                case BN:
                    break;
                case LRE: case RLE: case LRO: case RLO: case PDF:
                    wca [ i ] = BN;
                    break;
                default:
                    if ( ( el & OVERRIDE ) != 0 ) {
                        wca [ i ] = directionOfLevel ( el );
                    }
                    break;
                }
                ea [ i ] = el;
            }
        }

        private static int directionOfLevel ( int level ) {
            return ( ( level & 1 ) != 0 ) ? R : L;
        }

        private static int levelOfEmbedding ( int embedding ) {
            return embedding & ~OVERRIDE;
        }

        private static int[] levelsFromEmbeddings ( int[] ea, int[] la ) {
            assert ea != null;
            assert la != null;
            assert la.length == ea.length;
            for ( int i = 0, n = la.length; i < n; i++ ) {
                la [ i ] = levelOfEmbedding ( ea [ i ] );
            }
            return la;
        }

        private static void resolveRuns ( int[] wca, int defaultLevel, int[] ea, int[] la ) {
            if ( la.length != wca.length ) {
                throw new IllegalArgumentException ( "levels sequence length must match classes sequence length" );
            } else if ( la.length != ea.length ) {
                throw new IllegalArgumentException ( "levels sequence length must match embeddings sequence length" );
            } else {
                for ( int i = 0, n = ea.length, lPrev = defaultLevel; i < n; ) {
                    int s = i;
                    int e = s;
                    int l = findNextNonRetainedFormattingLevel ( wca, ea, s, lPrev );
                    while ( e < n ) {
                        if ( la [ e ] != l ) {
                            if ( startsWithRetainedFormattingRun ( wca, ea, e ) ) {
                                e += getLevelRunLength ( ea, e );
                            } else {
                                break;
                            }
                        } else {
                            e++;
                        }
                    }
                    lPrev = resolveRun ( wca, defaultLevel, ea, la, s, e, l, lPrev );
                    i = e;
                }
            }
        }

        private static int findNextNonRetainedFormattingLevel ( int[] wca, int[] ea, int start, int lPrev ) {
            int s = start;
            int e = wca.length;
            while ( s < e ) {
                if ( startsWithRetainedFormattingRun ( wca, ea, s ) ) {
                    s += getLevelRunLength ( ea, s );
                } else {
                    break;
                }
            }
            if ( s < e ) {
                return levelOfEmbedding ( ea [ s ] );
            } else {
                return lPrev;
            }
        }

        private static int getLevelRunLength ( int[] ea, int start ) {
            assert start < ea.length;
            int nl = 0;
            for ( int s = start, e = ea.length, l0 = levelOfEmbedding ( ea [ start ] ); s < e; s++ ) {
                if ( levelOfEmbedding ( ea [ s ] ) == l0 ) {
                    nl++;
                } else {
                    break;
                }
            }
            return nl;
        }

        private static boolean startsWithRetainedFormattingRun ( int[] wca, int[] ea, int start ) {
            int nl = getLevelRunLength ( ea, start );
            if ( nl > 0 ) {
                int nc = getRetainedFormattingRunLength ( wca, start );
                return ( nc >= nl );
            } else {
                return false;
            }
        }

        private static int getRetainedFormattingRunLength ( int[] wca, int start ) {
            assert start < wca.length;
            int nc = 0;
            for ( int s = start, e = wca.length; s < e; s++ ) {
                if ( wca [ s ] == BidiConstants.BN ) {
                    nc++;
                } else {
                    break;
                }
            }
            return nc;
        }

        private static int resolveRun ( int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int levelPrev ) {

            // determine start of run direction
            int sor = directionOfLevel ( max ( levelPrev, level ) );

            // determine end of run direction
            int le = -1;
            if ( end == wca.length ) {
                le = max ( level, defaultLevel );
            } else {
                for ( int i = end; i < wca.length; i++ ) {
                    if ( wca [ i ] != BidiConstants.BN ) {
                        le = max ( level, la [ i ] );
                        break;
                    }
                }
                if ( le < 0 ) {
                    le = max ( level, defaultLevel );
                }
            }
            int eor = directionOfLevel ( le );

            if (log.isDebugEnabled()) {
                log.debug ( "BR[" + padLeft ( start, 3 ) + "," + padLeft ( end, 3 ) + "] :" + padLeft ( level, 2 ) + ": SOR(" + getClassName(sor) + "), EOR(" + getClassName(eor) + ")" );
            }

            resolveWeak ( wca, defaultLevel, ea, la, start, end, level, sor, eor );
            resolveNeutrals ( wca, defaultLevel, ea, la, start, end, level, sor, eor );
            resolveImplicit ( wca, defaultLevel, ea, la, start, end, level, sor, eor );

            // if this run is all retained formatting, then return prior level, otherwise this run's level
            return isRetainedFormatting ( wca, start, end ) ? levelPrev : level;
        }

        private static void resolveWeak ( int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int sor, int eor ) {

            // W1 - X BN* NSM -> X BN* X
            for ( int i = start, n = end, bcPrev = sor; i < n; i++ ) {
                int bc = wca [ i ];
                if ( bc == NSM ) {
                    wca [ i ] = bcPrev;
                } else if ( bc != BN ) {
                    bcPrev = bc;
                }
            }

            // W2 - AL ... EN -> AL ... AN
            for ( int i = start, n = end, bcPrev = sor; i < n; i++ ) {
                int bc = wca [ i ];
                if ( bc == EN ) {
                    if ( bcPrev == AL ) {
                        wca [ i ] = AN;
                    }
                } else if ( isStrong ( bc ) ) {
                    bcPrev = bc;
                }
            }

            // W3 - AL -> R
            for ( int i = start, n = end; i < n; i++ ) {
                int bc = wca [ i ];
                if ( bc == AL ) {
                    wca [ i ] = R;
                }
            }

            // W4 - EN BN* ES BN* EN -> EN BN* EN BN* EN; XN BN* CS BN* XN -> XN BN* XN BN* XN
            for ( int i = start, n = end, bcPrev = sor; i < n; i++ ) {
                int bc = wca [ i ];
                if ( bc == ES ) {
                    int bcNext = eor;
                    for ( int j = i + 1; j < n; j++ ) {
                        if ( ( bc = wca [ j ] ) != BN ) {
                            bcNext = bc;
                            break;
                        }
                    }
                    if ( ( bcPrev == EN ) && ( bcNext == EN ) ) {
                        wca [ i ] = EN;
                    }
                } else if ( bc == CS ) {
                    int bcNext = eor;
                    for ( int j = i + 1; j < n; j++ ) {
                        if ( ( bc = wca [ j ] ) != BN ) {
                            bcNext = bc;
                            break;
                        }
                    }
                    if ( ( bcPrev == EN ) && ( bcNext == EN ) ) {
                        wca [ i ] = EN;
                    } else if ( ( bcPrev == AN ) && ( bcNext == AN ) ) {
                        wca [ i ] = AN;
                    }
                }
                if ( bc != BN ) {
                    bcPrev = bc;
                }
            }

            // W5 - EN (ET|BN)* -> EN (EN|BN)*; (ET|BN)* EN -> (EN|BN)* EN
            for ( int i = start, n = end, bcPrev = sor; i < n; i++ ) {
                int bc = wca [ i ];
                if ( bc == ET ) {
                    int bcNext = eor;
                    for ( int j = i + 1; j < n; j++ ) {
                        bc = wca [ j ];
                        if ( ( bc != BN ) && ( bc != ET ) ) {
                            bcNext = bc;
                            break;
                        }
                    }
                    if ( ( bcPrev == EN ) || ( bcNext == EN ) ) {
                        wca [ i ] = EN;
                    }
                } else if ( ( bc != BN ) && ( bc != ET ) ) {
                    bcPrev = bc;
                }
            }

            // W6 - BN* (ET|ES|CS) BN* -> ON* ON ON*
            for ( int i = start, n = end; i < n; i++ ) {
                int bc = wca [ i ];
                if ( ( bc == ET ) || ( bc == ES ) || ( bc == CS ) ) {
                    wca [ i ] = ON;
                    resolveAdjacentBoundaryNeutrals ( wca, start, end, i, ON );
                }
            }

            // W7 - L ... EN -> L ... L
            for ( int i = start, n = end, bcPrev = sor; i < n; i++ ) {
                int bc = wca [ i ];
                if ( bc == EN ) {
                    if ( bcPrev == L ) {
                        wca [ i ] = L;
                    }
                } else if ( ( bc == L ) || ( bc == R ) ) {
                    bcPrev = bc;
                }
            }

        }

        private static void resolveNeutrals ( int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int sor, int eor ) {

            // N1 - (L|R) N+ (L|R) -> L L+ L | R R+ R; (AN|EN) N+ R -> (AN|EN) R+ R; R N+ (AN|EN) -> R R+ (AN|EN)
            for ( int i = start, n = end, bcPrev = sor; i < n; i++ ) {
                int bc = wca [ i ];
                if ( isNeutral ( bc ) ) {
                    int bcNext = eor;
                    for ( int j = i + 1; j < n; j++ ) {
                        bc = wca [ j ];
                        if ( ( bc == L ) || ( bc == R ) ) {
                            bcNext = bc;
                            break;
                        } else if ( ( bc == AN ) || ( bc == EN ) ) {
                            bcNext = R;
                            break;
                        } else if ( isNeutral ( bc ) ) {
                            continue;
                        } else if ( isRetainedFormatting ( bc ) ) {
                            continue;
                        } else {
                            break;
                        }
                    }
                    if ( bcPrev == bcNext ) {
                        wca [ i ] = bcPrev;
                        resolveAdjacentBoundaryNeutrals ( wca, start, end, i, bcPrev );
                    }
                } else if ( ( bc == L ) || ( bc == R ) ) {
                    bcPrev = bc;
                } else if ( ( bc == AN ) || ( bc == EN ) ) {
                    bcPrev = R;
                }
            }

            // N2 - N -> embedding level
            for ( int i = start, n = end; i < n; i++ ) {
                int bc = wca [ i ];
                if ( isNeutral ( bc ) ) {
                    int bcEmbedding = directionOfLevel ( levelOfEmbedding ( ea [ i ] ) );
                    wca [ i ] = bcEmbedding;
                    resolveAdjacentBoundaryNeutrals ( wca, start, end, i, bcEmbedding );
                }
            }

        }

        private static void resolveAdjacentBoundaryNeutrals ( int[] wca, int start, int end, int index, int bcNew ) {
            if ( ( index < start ) || ( index >= end ) ) {
                throw new IllegalArgumentException();
            } else {
                for ( int i = index - 1; i >= start; i-- ) {
                    int bc = wca [ i ];
                    if ( bc == BN ) {
                        wca [ i ] = bcNew;
                    } else {
                        break;
                    }
                }
                for ( int i = index + 1; i < end; i++ ) {
                    int bc = wca [ i ];
                    if ( bc == BN ) {
                        wca [ i ] = bcNew;
                    } else {
                        break;
                    }
                }
            }
        }

        private static void resolveImplicit ( int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int sor, int eor ) {
            for ( int i = start, n = end; i < n; i++ ) {
                int bc = wca [ i ];                     // bidi class
                int el = la [ i ];                      // embedding level
                int ed = 0;                             // embedding level delta
                if ( ( el & 1 ) == 0 ) {                // even
                    if ( bc == R ) {
                        ed = 1;
                    } else if ( bc == AN ) {
                        ed = 2;
                    } else if ( bc == EN ) {
                        ed = 2;
                    }
                } else {                                // odd
                    if ( bc == L ) {
                        ed = 1;
                    } else if ( bc == EN ) {
                        ed = 1;
                    } else if ( bc == AN ) {
                        ed = 1;
                    }
                }
                la [ i ] = el + ed;
            }
        }

        /**
         * Resolve separators and boundary neutral levels to account for UAX#9 3.4 L1 while taking into
         * account retention of formatting codes (5.2).
         * @param ica original input class array (sequence)
         * @param wca working copy of original intput class array (sequence), as modified by prior steps
         * @param dl default paragraph level
         * @param la array of output levels to be adjusted, as produced by bidi algorithm
         */
        private static void resolveSeparators ( int[] ica, int[] wca, int dl, int[] la ) {
            // steps (1) through (3)
            for ( int i = 0, n = ica.length; i < n; i++ ) {
                int ic = ica[i];
                if ( ( ic == BidiConstants.S ) || ( ic == BidiConstants.B ) ) {
                    la[i] = dl;
                    for ( int k = i - 1; k >= 0; k-- ) {
                        int pc = ica[k];
                        if ( isRetainedFormatting ( pc ) ) {
                            continue;
                        } if ( pc == BidiConstants.WS ) {
                            la[k] = dl;
                        } else {
                            break;
                        }
                    }
                }
            }
            // step (4) - consider end of input sequence to be end of line, but skip any trailing boundary neutrals and retained formatting codes
            for ( int i = ica.length; i > 0; i-- ) {
                int k = i - 1;
                int ic = ica[k];
                if ( isRetainedFormatting ( ic ) ) {
                    continue;
                } else if ( ic == BidiConstants.WS ) {
                    la[k] = dl;
                } else {
                    break;
                }
            }
            // step (5) - per section 5.2
            for ( int i = 0, n = ica.length; i < n; i++ ) {
                int ic = ica[i];
                if ( isRetainedFormatting ( ic ) ) {
                    if ( i == 0 ) {
                        la[i] = dl;
                    } else {
                        la[i] = la [ i - 1 ];
                    }
                }
            }
        }

        private static boolean isStrong ( int bc ) {
            switch ( bc ) {
            case L:
            case R:
            case AL:
                return true;
            default:
                return false;
            }
        }

        private static boolean isNeutral ( int bc ) {
            switch ( bc ) {
            case WS:
            case ON:
            case S:
            case B:
                return true;
            default:
                return false;
            }
        }

        private static boolean isRetainedFormatting ( int bc ) {
            switch ( bc ) {
            case LRE:
            case LRO:
            case RLE:
            case RLO:
            case PDF:
            case BN:
                return true;
            default:
                return false;
            }
        }

        private static boolean isRetainedFormatting ( int[] ca, int s, int e ) {
            for ( int i = s; i < e; i++ ) {
                if ( ! isRetainedFormatting ( ca[i] ) ) {
                    return false;
                }
            }
            return true;
        }

        private static int max ( int x, int y ) {
            if ( x > y ) {
                return x;
            } else {
                return y;
            }
        }

        private static int[] getClasses ( int[] chars ) {
            int[] classes = new int [ chars.length ];
            int bc;
            for ( int i = 0, n = chars.length; i < n; i++ ) {
                int ch = chars [ i ];
                if ( ch >= 0 ) {
                    bc = BidiClassUtils.getBidiClass ( chars [ i ] );
                } else {
                    bc = SURROGATE;
                }
                classes [ i ] = bc;
            }
            return classes;
        }

        /**
         * Convert character sequence (a UTF-16 encoded string) to an array of unicode scalar values
         * expressed as integers. If a valid UTF-16 surrogate pair is encountered, it is converted to
         * two integers, the first being the equivalent unicode scalar  value, and the second being
         * negative one (-1). This special mechanism is used to track the use of surrogate pairs while
         * working with unicode scalar values, and permits maintaining indices that apply both to the
         * input UTF-16 and out scalar value sequences.
         * @return a boolean indicating that content is present that triggers bidirectional processing
         * @param cs a UTF-16 encoded character sequence
         * @param chars an integer array to accept the converted scalar values, where the length of the
         * array must be the same as the length of the input character sequence
         * @throws IllegalArgumentException if the input sequence is not a valid UTF-16 string, e.g.,
         * if it contains an isolated UTF-16 surrogate
         */
        private static boolean convertToScalar ( CharSequence cs, int[] chars ) throws IllegalArgumentException {
            boolean triggered = false;
            if ( chars.length != cs.length() ) {
                throw new IllegalArgumentException ( "characters array length must match input sequence length" );
            }
            for ( int i = 0, n = chars.length; i < n; ) {
                int chIn = cs.charAt ( i );
                int chOut;
                if ( chIn < 0xD800 ) {
                    chOut = chIn;
                } else if ( chIn < 0xDC00 ) {
                    int chHi = chIn;
                    int chLo;
                    if ( ( i + 1 ) < n ) {
                        chLo = cs.charAt ( i + 1 );
                        if ( ( chLo >= 0xDC00 ) && ( chLo <= 0xDFFF ) ) {
                            chOut = convertToScalar ( chHi, chLo );
                        } else {
                            throw new IllegalArgumentException ( "isolated high surrogate" );
                        }
                    } else {
                        throw new IllegalArgumentException ( "truncated surrogate pair" );
                    }
                } else if ( chIn < 0xE000 ) {
                    throw new IllegalArgumentException ( "isolated low surrogate" );
                } else {
                    chOut = chIn;
                }
                if ( ! triggered && triggersBidi ( chOut ) ) {
                    triggered = true;
                }
                if ( ( chOut & 0xFF0000 ) == 0 ) {
                    chars [ i++ ] = chOut;
                } else {
                    chars [ i++ ] = chOut;
                    chars [ i++ ] = -1;
                }
            }
            return triggered;
        }

        /**
         * Convert UTF-16 surrogate pair to unicode scalar valuee.
         * @return a unicode scalar value
         * @param chHi high (most significant or first) surrogate
         * @param chLo low (least significant or second) surrogate
         * @throws IllegalArgumentException if one of the input surrogates is not valid
         */
        private static int convertToScalar ( int chHi, int chLo ) {
            if ( ( chHi < 0xD800 ) || ( chHi > 0xDBFF ) ) {
                throw new IllegalArgumentException ( "bad high surrogate" );
            } else if ( ( chLo < 0xDC00 ) || ( chLo > 0xDFFF ) ) {
                throw new IllegalArgumentException ( "bad low surrogate" );
            } else {
                return ( ( ( chHi & 0x03FF ) << 10 ) | ( chLo & 0x03FF ) ) + 0x10000;
            }
        }

        /**
         * Determine of character CH triggers bidirectional processing. Bidirectional
         * processing is deemed triggerable if CH is a strong right-to-left character,
         * an arabic letter or number, or is a right-to-left embedding or override
         * character.
         * @return true if character triggers bidirectional processing
         * @param ch a unicode scalar value
         */
        private static boolean triggersBidi ( int ch ) {
            switch ( BidiClassUtils.getBidiClass ( ch ) ) {
            case R:
            case AL:
            case AN:
            case RLE:
            case RLO:
                return true;
            default:
                return false;
            }
        }

        private static void dump ( String header, int[] chars, int[] classes, int defaultLevel, int[] levels ) {
            log.debug ( header );
            log.debug ( "BD: default level(" + defaultLevel + ")" );
            StringBuffer sb = new StringBuffer();
            if ( chars != null ) {
                for ( int i = 0, n = chars.length; i < n; i++ ) {
                    int ch = chars [ i ];
                    sb.setLength(0);
                    if ( ( ch > 0x20 ) && ( ch < 0x7F ) ) {
                        sb.append ( (char) ch );
                    } else {
                        sb.append ( CharUtilities.charToNCRef ( ch ) );
                    }
                    for ( int k = sb.length(); k < 12; k++ ) {
                        sb.append ( ' ' );
                    }
                    sb.append ( ": " + padRight ( getClassName ( classes[i] ), 4 ) + " " + levels[i] );
                    log.debug ( sb );
                }
            } else {
                for ( int i = 0, n = classes.length; i < n; i++ ) {
                    sb.setLength(0);
                    for ( int k = sb.length(); k < 12; k++ ) {
                        sb.append ( ' ' );
                    }
                    sb.append ( ": " + padRight ( getClassName ( classes[i] ), 4 ) + " " + levels[i] );
                    log.debug ( sb );
                }
            }
        }

        private static String getClassName ( int bc ) {
            switch ( bc ) {
            case L:                                     // left-to-right
                return "L";
            case LRE:                                   // left-to-right embedding
                return "LRE";
            case LRO:                                   // left-to-right override
                return "LRO";
            case R:                                     // right-to-left
                return "R";
            case AL:                                    // right-to-left arabic
                return "AL";
            case RLE:                                   // right-to-left embedding
                return "RLE";
            case RLO:                                   // right-to-left override
                return "RLO";
            case PDF:                                   // pop directional formatting
                return "PDF";
            case EN:                                    // european number
                return "EN";
            case ES:                                    // european number separator
                return "ES";
            case ET:                                    // european number terminator
                return "ET";
            case AN:                                    // arabic number
                return "AN";
            case CS:                                    // common number separator
                return "CS";
            case NSM:                                   // non-spacing mark
                return "NSM";
            case BN:                                    // boundary neutral
                return "BN";
            case B:                                     // paragraph separator
                return "B";
            case S:                                     // segment separator
                return "S";
            case WS:                                    // whitespace
                return "WS";
            case ON:                                    // other neutrals
                return "ON";
            case SURROGATE:                             // placeholder for low surrogate
                return "SUR";
            default:
                return "?";
            }
        }

    }

}
