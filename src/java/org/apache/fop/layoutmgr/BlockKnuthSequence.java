/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.List;


/**
 * Represents a list of block level Knuth elements.
 */
public class BlockKnuthSequence extends KnuthSequence {
    
    private boolean isClosed = false;
    
    /**
     * Creates a new and empty list.
     */
    public BlockKnuthSequence() {
        super();
    }
    
    /**
     * Creates a new list from an existing list.
     * @param list The list from which to create the new list.
     */
    public BlockKnuthSequence(List list) {
        super(list);
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthList#isInlineSequence()
     */
    public boolean isInlineSequence() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthList#canAppendSequence(org.apache.fop.layoutmgr.KnuthSequence)
     */
    public boolean canAppendSequence(KnuthSequence sequence) {
        return !sequence.isInlineSequence() && !isClosed;
    }

    /**
     * this and sequence are supposed to belong to the same LayoutManager,
     * which is stored in the positions of the elements in the sequences
     * @param sequence The sequence following this
     * @return whether this and the following sequence must be kept together
     */
    private boolean mustKeepWithNext(BlockKnuthSequence sequence) {
/*        // TODO read keep together correctly
        // for now, return false
        return BlockKnuthSequence.mustKeepTogether(this, sequence);
*/        return false;
    }

    /**
     * the two sequences are supposed to belong to the same LayoutManager,
     * which is stored in the positions of the elements in the sequences
     * @param sequence1 The leading sequence
     * @param sequence2 The following sequence
     * @return whether the two sequences must be kept together
     */
    public static boolean mustKeepTogether(BlockKnuthSequence sequence1,
                                           BlockKnuthSequence sequence2) {
        ListElement element1 = (ListElement) sequence1.get(sequence1.size() - 1);
        LayoutManager lm1 = (LayoutManager) element1.getLayoutManager();
        ListElement element2 = (ListElement) sequence2.get(0);
        LayoutManager lm2 = (LayoutManager) element2.getLayoutManager();
        if (!lm1.equals(lm2)) {
            throw new IllegalStateException
              ("The two sequences must belong to the same LayoutManager");
        }
        if (lm1 instanceof BlockLevelLayoutManager
                && ((BlockLevelLayoutManager) lm1).mustKeepTogether()) {
            return true;
        }
        Position pos1 = element1.getPosition();
        if (pos1 != null) {
            pos1 = pos1.getPosition();
            if (pos1 != null) {
                lm1 = pos1.getLM();
                if (lm1 instanceof BlockLevelLayoutManager
                    && ((BlockLevelLayoutManager) lm1).mustKeepWithNext()) {
                        return true;
                } 
            }
        }
        Position pos2 = element1.getPosition();
        if (pos2 != null) {
            pos2 = pos2.getPosition();
            if (pos2 != null) {
                lm2 = pos2.getLM();
                if (lm2 instanceof BlockLevelLayoutManager
                    && ((BlockLevelLayoutManager) lm2).mustKeepWithPrevious()) {
                        return true;
                } 
            }
        }
        return false;
/*      From BlockStackingLM.getChangedKnuthElements
        // there is another block after this one
        if (bSomethingAdded
            && (this.mustKeepTogether()
                || prevLM.mustKeepWithNext()
                || currLM.mustKeepWithPrevious())) {
            // add an infinite penalty to forbid a break between blocks
            returnedList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, 
                    new Position(this), false));
        } else if (bSomethingAdded && !((KnuthElement) returnedList.getLast()).isGlue()) {
            // add a null penalty to allow a break between blocks
            returnedList.add(new KnuthPenalty(0, 0, false, new Position(this), false));
        }
*/    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthList#appendSequence(org.apache.fop.layoutmgr.KnuthSequence, org.apache.fop.layoutmgr.LayoutManager)
     */
    /**
     * this and sequence are supposed to belong to the same LayoutManager,
     * which is stored in the positions of the elements in the sequences
     */
    public boolean appendSequence(KnuthSequence sequence, LayoutManager lm) {
        if (!canAppendSequence(sequence)) {
            return false;
        }
/*        // TODO disable because InlineLM.addAreas expects only NonLeafPostions; why?
        if (!mustKeepWithNext((BlockKnuthSequence) sequence)) {
            add(new KnuthPenalty(0, 0, false, new Position(lm), false));
        }
*/        addAll(sequence);
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthList#appendSequenceOrClose(org.apache.fop.layoutmgr.KnuthSequence, org.apache.fop.layoutmgr.LayoutManager)
     */
    public boolean appendSequenceOrClose(KnuthSequence sequence, LayoutManager lm) {
        if (!appendSequence(sequence, lm)) {
            endSequence();
            return false;
        } else {
            return true;
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthSequence#endSequence()
     */
    public KnuthSequence endSequence() {
        isClosed = true;
        return this;
    }

}
