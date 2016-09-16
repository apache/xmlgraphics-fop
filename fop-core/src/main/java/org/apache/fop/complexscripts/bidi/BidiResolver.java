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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.LineArea;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.fo.pagination.PageSequence;

// CSOFF: LineLengthCheck

/**
 * <p>A utility class for performing bidirectional resolution processing.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public final class BidiResolver {

    /**
     * logging instance
     */
    private static final Log log = LogFactory.getLog(BidiResolver.class);

    private BidiResolver() {
    }

    /**
     * Resolve inline directionality.
     * @param ps a page sequence FO instance
     */
    public static void resolveInlineDirectionality(PageSequence ps) {
        if (log.isDebugEnabled()) {
            log.debug("BD: RESOLVE: " + ps);
        }
        // 1. collect delimited text ranges
        List ranges = ps.collectDelimitedTextRanges(new Stack());
        if (log.isDebugEnabled()) {
            dumpRanges("BD: RESOLVE: RANGES:", ranges);
        }
        // 2. prune empty ranges
        ranges = pruneEmptyRanges(ranges);
        if (log.isDebugEnabled()) {
            dumpRanges("BD: RESOLVE: PRUNED RANGES:", ranges);
        }
        // 3. resolve inline directionaly of unpruned ranges
        resolveInlineDirectionality(ranges);
    }

    /**
     * Reorder line area.
     * @param la a line area instance
     */
    public static void reorder(LineArea la) {

        // 1. collect inline levels
        List runs = collectRuns(la.getInlineAreas(), new Vector());
        if (log.isDebugEnabled()) {
            dumpRuns("BD: REORDER: INPUT:", runs);
        }

        // 2. split heterogeneous inlines
        runs = splitRuns(runs);
        if (log.isDebugEnabled()) {
            dumpRuns("BD: REORDER: SPLIT INLINES:", runs);
        }

        // 3. determine minimum and maximum levels
        int[] mm = computeMinMaxLevel(runs, null);
        if (log.isDebugEnabled()) {
            log.debug("BD: REORDER: { min = " + mm[0] + ", max = " + mm[1] + "}");
        }

        // 4. reorder from maximum level to minimum odd level
        int mn = mm[0];
        int mx = mm[1];
        if (mx > 0) {
            for (int l1 = mx, l2 = ((mn & 1) == 0) ? (mn + 1) : mn; l1 >= l2; l1--) {
                runs = reorderRuns(runs, l1);
            }
        }
        if (log.isDebugEnabled()) {
            dumpRuns("BD: REORDER: REORDERED RUNS:", runs);
        }

        // 5. reverse word consituents (characters and glyphs) while mirroring
        boolean mirror = true;
        reverseWords(runs, mirror);
        if (log.isDebugEnabled()) {
            dumpRuns("BD: REORDER: REORDERED WORDS:", runs);
        }

        // 6. replace line area's inline areas with reordered runs' inline areas
        replaceInlines(la, replicateSplitWords(runs));
    }

    private static void resolveInlineDirectionality(List ranges) {
        for (Object range : ranges) {
            DelimitedTextRange r = (DelimitedTextRange) range;
            r.resolve();
            if (log.isDebugEnabled()) {
                log.debug(r);
            }
        }
    }

    private static List collectRuns(List inlines, List runs) {
        for (Object inline : inlines) {
            InlineArea ia = (InlineArea) inline;
            runs = ia.collectInlineRuns(runs);
        }
        return runs;
    }

    private static List splitRuns(List runs) {
        List runsNew = new Vector();
        for (Object run : runs) {
            InlineRun ir = (InlineRun) run;
            if (ir.isHomogenous()) {
                runsNew.add(ir);
            } else {
                runsNew.addAll(ir.split());
            }
        }
        if (!runsNew.equals(runs)) {
            runs = runsNew;
        }
        return runs;
    }

    private static int[] computeMinMaxLevel(List runs, int[] mm) {
        if (mm == null) {
            mm = new int[] {Integer.MAX_VALUE, Integer.MIN_VALUE};
        }
        for (Object run : runs) {
            InlineRun ir = (InlineRun) run;
            ir.updateMinMax(mm);
        }
        return mm;
    }
    private static List reorderRuns(List runs, int level) {
        assert level >= 0;
        List runsNew = new Vector();
        for (int i = 0, n = runs.size(); i < n; i++) {
            InlineRun iri = (InlineRun) runs.get(i);
            if (iri.getMinLevel() < level) {
                runsNew.add(iri);
            } else {
                int s = i;
                int e = s;
                while (e < n) {
                    InlineRun ire = (InlineRun) runs.get(e);
                    if (ire.getMinLevel() < level) {
                        break;
                    } else {
                        e++;
                    }
                }
                if (s < e) {
                    runsNew.addAll(reverseRuns(runs, s, e));
                }
                i = e - 1;
            }
        }
        if (!runsNew.equals(runs)) {
            runs = runsNew;
        }
        return runs;
    }
    private static List reverseRuns(List runs, int s, int e) {
        int n = e - s;
        Vector runsNew = new Vector(n);
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                int k = (n - i - 1);
                InlineRun ir = (InlineRun) runs.get(s + k);
                ir.reverse();
                runsNew.add(ir);
            }
        }
        return runsNew;
    }
    private static void reverseWords(List runs, boolean mirror) {
        for (Object run : runs) {
            InlineRun ir = (InlineRun) run;
            ir.maybeReverseWord(mirror);
        }
    }
    private static List replicateSplitWords(List runs) {
        // [TBD] for each run which inline word area appears multiple times in
        // runs, replicate that word
        return runs;
    }
    private static void replaceInlines(LineArea la, List runs) {
        List<InlineArea> inlines = new ArrayList<InlineArea>();
        for (Object run : runs) {
            InlineRun ir = (InlineRun) run;
            inlines.add(ir.getInline());
        }
        la.setInlineAreas(unflattenInlines(inlines));
    }
    private static List unflattenInlines(List<InlineArea> inlines) {
        return new UnflattenProcessor(inlines) .unflatten();
    }
    private static void dumpRuns(String header, List runs) {
        log.debug(header);
        for (Object run : runs) {
            InlineRun ir = (InlineRun) run;
            log.debug(ir);
        }
    }
    private static void dumpRanges(String header, List ranges) {
        log.debug(header);
        for (Object range : ranges) {
            DelimitedTextRange r = (DelimitedTextRange) range;
            log.debug(r);
        }
    }
    private static List pruneEmptyRanges(List ranges) {
        Vector rv = new Vector();
        for (Object range : ranges) {
            DelimitedTextRange r = (DelimitedTextRange) range;
            if (!r.isEmpty()) {
                rv.add(r);
            }
        }
        return rv;
    }

}
