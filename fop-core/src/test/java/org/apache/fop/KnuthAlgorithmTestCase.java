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

package org.apache.fop;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.fop.layoutmgr.BlockKnuthSequence;
import org.apache.fop.layoutmgr.BreakingAlgorithm;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthSequence;

/**
 * Tests the Knuth algorithm implementation.
 */
public class KnuthAlgorithmTestCase {

    @Before
    public void setUp() {
        DebugHelper.registerStandardElementListObservers();
    }

    private KnuthSequence getKnuthSequence1() {
        KnuthSequence seq = new BlockKnuthSequence();
        for (int i = 0; i < 5; i++) {
            seq.add(new KnuthBox(0, null, true));
            seq.add(new KnuthPenalty(0, KnuthPenalty.INFINITE, false, null, true));
            seq.add(new KnuthGlue(5000, 0, 0, null, true));
            seq.add(new KnuthBox(10000, null, false));
            if (i < 4) {
                seq.add(new KnuthPenalty(0, 0, false, null, false));
                seq.add(new KnuthGlue(-5000, 0, 0, null, true));
            }
        }

        seq.add(new KnuthPenalty(0, KnuthPenalty.INFINITE, false, null, false));
        seq.add(new KnuthGlue(0, Integer.MAX_VALUE, 0, null, false));
        seq.add(new KnuthPenalty(0, -KnuthPenalty.INFINITE, false, null, false));
        ElementListObserver.observe(seq, "test", null);
        return seq;
    }

    /**
     * Tests a special condition where a negative-length glue occurs directly after a break
     * possibility.
     * @throws Exception if an error occurs
     */
    @Test
    public void test1() throws Exception {
        MyBreakingAlgorithm algo = new MyBreakingAlgorithm(0, 0, true, true, 0);
        algo.setConstantLineWidth(30000);
        KnuthSequence seq = getKnuthSequence1();
        algo.findBreakingPoints(seq, 1, true, BreakingAlgorithm.ALL_BREAKS);
        Part[] parts = algo.getParts();
        assertEquals("Sequence must produce 3 parts", 3, parts.length);
        assertEquals(5000, parts[0].difference);
        assertEquals(5000, parts[1].difference);
    }

    private class Part {
        private int difference;
        private double ratio;
        private int position;
    }

    private class MyBreakingAlgorithm extends BreakingAlgorithm {

        private final List<Part> parts = new java.util.ArrayList<Part>();

        public MyBreakingAlgorithm(int align, int alignLast, boolean first,
                    boolean partOverflowRecovery, int maxFlagCount) {
            super(align, alignLast, first, partOverflowRecovery, maxFlagCount);
        }

        public Part[] getParts() {
            return parts.toArray(new Part[parts.size()]);
        }

        @Override
        public void updateData1(int total, double demerits) {
            //nop
        }

        @Override
        public void updateData2(KnuthNode bestActiveNode, KnuthSequence sequence, int total) {
            int difference = bestActiveNode.difference;
            // it is always allowed to adjust space, so the ratio must be set regardless of
            // the value of the property display-align; the ratio must be <= 1
            double ratio = bestActiveNode.adjustRatio;
            if (ratio < 0) {
                // page break with a negative difference:
                // spaces always have enough shrink
                difference = 0;
            } else if (ratio <= 1 && bestActiveNode.line < total) {
                // not-last page break with a positive difference smaller than the available
                // stretch: spaces can stretch to fill the whole difference
                difference = 0;
            } else if (ratio > 1) {
                // not-last page with a positive difference greater than the available stretch
                // spaces can stretch to fill the difference only partially
                ratio = 1;
                difference -= bestActiveNode.availableStretch;
            } else {
                // last page with a positive difference:
                // spaces do not need to stretch
                ratio = 0;
            }

            // add nodes at the beginning of the list, as they are found
            // backwards, from the last one to the first one
            Part part = new Part();
            part.difference = difference;
            part.ratio = ratio;
            part.position = bestActiveNode.position;
            parts.add(0, part);
        }

        @Override
        protected int filterActiveNodes() {
            //nop
            return 0;
        }

    }

}
