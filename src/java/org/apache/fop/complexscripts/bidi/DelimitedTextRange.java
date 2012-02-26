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

package org.apache.fop.complexscripts.bidi;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.CharIterator;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.WritingModeTraits;
import org.apache.fop.traits.WritingModeTraitsGetter;
import org.apache.fop.util.CharUtilities;

// CSOFF: EmptyForIteratorPadCheck
// CSOFF: InnerAssignmentCheck
// CSOFF: LineLengthCheck
// CSOFF: NoWhitespaceAfterCheck

/**
 * The <code>DelimitedTextRange</code> class implements the "delimited text range" as described
 * by XML-FO 1.1 §5.8, which contains a flattened sequence of characters. Any FO that generates
 * block areas serves as a delimiter.
 *
 * @author Glenn Adams
 */
public class DelimitedTextRange {
    private FONode fn;                              // node that generates this text range
    private StringBuffer buffer;                    // flattened character sequence of generating FO nodes
    private List intervals;                         // list of intervals over buffer of generating FO nodes
    /**
     * Primary constructor.
     * @param fn node that generates this text range
     */
    public DelimitedTextRange ( FONode fn ) {
        this.fn = fn;
        this.buffer = new StringBuffer();
        this.intervals = new Vector();
    }
    /**
     * Obtain node that generated this text range.
     * @return node that generated this text range
     */
    public FONode getNode() {
        return fn;
    }
    /**
     * Append interval using characters from character iterator IT.
     * @param it character iterator
     * @param fn node that generates interval being appended
     */
    public void append ( CharIterator it, FONode fn ) {
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
    /**
     * Append interval using character C.
     * @param c character
     * @param fn node that generates interval being appended
     */
    public void append ( char c, FONode fn ) {
        if ( c != 0 ) {
            int s = buffer.length();
            int e = s + 1;
            buffer.append ( c );
            intervals.add ( new TextInterval ( fn, s, e ) );
        }
    }
    /**
     * Determine if range is empty.
     * @return true if range is empty
     */
    public boolean isEmpty() {
        return buffer.length() == 0;
    }
    /**
     * Resolve bidirectional levels for this range.
     */
    public void resolve() {
        WritingModeTraitsGetter tg;
        if ( ( tg = WritingModeTraits.getWritingModeTraitsGetter ( getNode() ) ) != null ) {
            resolve ( tg.getInlineProgressionDirection() );
        }
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer ( "DR: " + fn.getLocalName() + " { <" + CharUtilities.toNCRefs ( buffer.toString() ) + ">" );
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
        sb.append("> }");
        return sb.toString();
    }
    private void resolve ( Direction paragraphEmbeddingLevel ) {
        int [] levels;
        if ( ( levels = UnicodeBidiAlgorithm.resolveLevels ( buffer, paragraphEmbeddingLevel ) ) != null ) {
            assignLevels ( levels );
            assignBlockLevel ( paragraphEmbeddingLevel );
            assignTextLevels();
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
    private static final Log log = LogFactory.getLog(BidiResolver.class); // CSOK: ConstantNameCheck
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
            if ( fn instanceof FObj ) {
                FObj fo = (FObj) fn;
                if ( fo.isBidiRangeBlockItem() ) {
                    if ( fo.getBidiLevel() < 0 ) {
                        fo.setBidiLevel ( defaultLevel );
                    }
                    break;
                }
            }
        }
    }
}

