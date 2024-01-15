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

package org.apache.fop.area.inline;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InlineParentTestCase {

    private static final int BLOCK_PROG_OFFSET = -12;

    private static final int BPD = 25;

    @Test
    public void testAddChildAreaMixedFromFootnote() {
        InlineParent parent = new InlineParent();
        InlineParent firstChild = createChildInlineParent(BLOCK_PROG_OFFSET, BPD, false);
        InlineParent secondChild = createChildInlineParent(3 * BLOCK_PROG_OFFSET, 3 * BPD, false);
        InlineParent thirdChild = createChildInlineParent(2 * BLOCK_PROG_OFFSET, 2 * BPD, true);
        InlineParent forthChild = createChildInlineParent(0, 0, true);

        assertEquals("Default Values must be zero", 0, parent.minChildOffset);
        assertEquals("Default Values must be zero", 0, parent.getVirtualBPD());

        assertAddChildArea(parent, firstChild, -12, 0);
        assertAddChildArea(parent, secondChild, -36, 0);
        assertAddChildArea(parent, thirdChild, -36, -36);
        assertAddChildArea(parent, forthChild, -36, -36);
    }

    @Test
    public void testAddChildAreaNotFromFootnote() {
        InlineParent parent = new InlineParent();
        InlineParent firstChild = createChildInlineParent(BLOCK_PROG_OFFSET, BPD, false);
        InlineParent secondChild = createChildInlineParent(3 * BLOCK_PROG_OFFSET, 3 * BPD, false);
        InlineParent thirdChild = createChildInlineParent(2 * BLOCK_PROG_OFFSET, 2 * BPD, false);
        InlineParent forthChild = createChildInlineParent(0, 0, false);

        assertEquals("Default Values must be zero", 0, parent.minChildOffset);
        assertEquals("Default Values must be zero", 0, parent.getVirtualBPD());

        assertAddChildArea(parent, firstChild, -12, 0);
        assertAddChildArea(parent, secondChild, -36, 0);
        assertAddChildArea(parent, thirdChild, -36, 0);
        assertAddChildArea(parent, forthChild, -36, 0);
    }

    @Test
    public void testAddChildAreaFromFootnote() {
        InlineParent parent = new InlineParent();
        InlineParent firstChild = createChildInlineParent(BLOCK_PROG_OFFSET, BPD, true);
        InlineParent secondChild = createChildInlineParent(3 * BLOCK_PROG_OFFSET, 3 * BPD, true);
        InlineParent thirdChild = createChildInlineParent(2 * BLOCK_PROG_OFFSET, 2 * BPD, true);
        InlineParent forthChild = createChildInlineParent(0, 0, true);

        assertEquals("Default Values must be zero", 0, parent.minChildOffset);
        assertEquals("Default Values must be zero", 0, parent.getVirtualBPD());

        assertAddChildArea(parent, firstChild, -12, -12);
        assertAddChildArea(parent, secondChild, -36, -36);
        assertAddChildArea(parent, thirdChild, -36, -36);
        assertAddChildArea(parent, forthChild, -36, -36);
    }

    private void assertAddChildArea(InlineParent parent, InlineParent child,
                                    int minChildOffset, int maxAfterEdge) {
        parent.addChildArea(child);

        // the virtualBPD is the subtraction of the maxAfterEdge with the minChildOffset
        // by adding the minChildOffset to the virtualBPD we get the maxAfterEdge alone
        int parentMaxAfterEdge = parent.getVirtualBPD() + parent.minChildOffset;

        if (!child.isFromFootnote()) {
            assertEquals("Must be set to the min of the current minChildOffset and the "
                            + "sum of the child's virtualOffset with the current value of the minChildOffset",
                    minChildOffset, parent.minChildOffset);
            assertEquals("Must be set to the max of the current maxAfterEdge and the "
                            + "result of the sum of the child's virtualOffset with child's the virtualBPD",
                    maxAfterEdge, parentMaxAfterEdge);
        } else {
            assertEquals("Must be set to the min of the current minChildOffset and the "
                            + "sum of the child's blockProgressionOffset with the current value "
                            + "of the minChildOffset",
                    minChildOffset, parent.minChildOffset);
            assertEquals("Must be the result of the sum of the maxAfterEdge "
                    + "with the child's virtualBPD", maxAfterEdge, parentMaxAfterEdge);
        }
    }

    private InlineParent createChildInlineParent(int bpo, int bpd, boolean footnote) {
        InlineParent child = new InlineParent();
        child.setBlockProgressionOffset(bpo);
        child.setBPD(bpd);
        child.setFromFootnote(footnote);

        return child;
    }
}
