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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.layoutmgr.inline.KnuthParagraph;
import org.apache.fop.layoutmgr.list.LineBreakingListElement;

/**
 * Represents a list of block level Knuth elements.
 */
public class BlockKnuthSequence extends KnuthSequence {

    /** the logger for the class */
    private static Log log = LogFactory.getLog(BlockKnuthSequence.class);
    
    private boolean isClosed = false;
    
    private Stack subSequences;
    
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

    /** {@inheritDoc} */
    public boolean isInlineSequence() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean canAppendSequence(KnuthSequence sequence) {
        return !sequence.isInlineSequence() && !isClosed;
    }

    /** {@inheritDoc} */
    public boolean appendSequence(KnuthSequence sequence) {
        // log.debug("Cannot append a sequence without a BreakElement");
        return false;
    }
    
    /** {@inheritDoc} */
    public boolean appendSequence(KnuthSequence sequence, boolean keepTogether,
                                  BreakElement breakElement) {
        if (!canAppendSequence(sequence)) {
            return false;
        }
        if (keepTogether) {
            breakElement.setPenaltyValue(KnuthElement.INFINITE);
            add(breakElement);
        } else if (!((ListElement) getLast()).isGlue()) {
            breakElement.setPenaltyValue(0);
            add(breakElement);
        }
        addAll(sequence);
        return true;
    }

    /** {@inheritDoc} */
    public KnuthSequence endSequence() {
        isClosed = true;
        return this;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthSequence#addKnuthElementForBorderPaddingStart(org.apache.fop.layoutmgr.KnuthElement)
     */
    public void addKnuthElementForBorderPaddingStart(KnuthBox bap) {
        ListIterator iter = listIterator();
        ParagraphListElement parale = null;
        while (iter.hasNext()) {
            ListElement le = (ListElement) iter.next();
            if (le instanceof ParagraphListElement) {
                parale = (ParagraphListElement) le;
                break;
            }
        }
        if (parale == null) {
            log.debug("Failed to add border and padding: block sequence contains no paragraph");
            return;
        }

        KnuthParagraph par = parale.getPara();
        Position newPos;
        KnuthElement firstElt = (KnuthElement) par.get(par.getIgnoreAtStart());
        Position firstPos = firstElt.getPosition();
        LayoutManager firstLM = firstPos.getLM();
        if (firstPos instanceof NonLeafPosition) {
            Position firstSubPos = firstPos.getPosition();
            newPos = new NonLeafPosition(firstLM, firstSubPos);
        } else {
            newPos = new LeafPosition(firstLM, -1);
        }
        bap.setPosition(newPos);
        par.addKnuthElementForBorderPaddingStart(bap);
        ElementListObserver.observe(par, "line", "added-bap-start");
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthSequence#addKnuthElementForBorderPaddingEnd(org.apache.fop.layoutmgr.KnuthElement)
     */
    public void addKnuthElementForBorderPaddingEnd(KnuthBox bap) {
        ListIterator iter = listIterator(size());
        ParagraphListElement parale = null;
        while (iter.hasPrevious()) {
            ListElement le = (ListElement) iter.previous();
            if (le instanceof ParagraphListElement) {
                parale = (ParagraphListElement) le;
                break;
            }
        }
        if (parale == null) {
            log.debug("Failed to add border and padding: block sequence contains no paragraph");
            return;
        }
        
        KnuthParagraph par = parale.getPara();
        Position newPos;
        KnuthElement lastElt = (KnuthElement) par.get(par.size() - 1 - par.getIgnoreAtEnd());
        Position lastPos = lastElt.getPosition();
        LayoutManager lastLM = lastPos.getLM();
        if (lastPos instanceof NonLeafPosition) {
            Position lastSubPos = lastPos.getPosition();
            newPos = new NonLeafPosition(lastLM, lastSubPos);
        } else {
            newPos = new LeafPosition(lastLM, -1);
        }
        bap.setPosition(newPos);
        par.addKnuthElementForBorderPaddingEnd(bap);
        ElementListObserver.observe(par, "line", "added-bap-end");
    }

    public class SubSequence {
        private KnuthBox firstBox = null;
        private int widowRowLimit = 0;
        private SubSequence(KnuthBox firstBox) {
            this.firstBox = firstBox;
        }
        private SubSequence(KnuthBox firstBox, int widowRowLimit) {
            this.firstBox = firstBox;
            this.widowRowLimit = widowRowLimit;
        }
        
        /**
         * @return the firstBox
         */
        public KnuthBox getFirstBox() {
            return firstBox;
        }
        
        /**
         * @return the widowRowLimit
         */
        public int getWidowRowLimit() {
            return widowRowLimit;
        }

    }
    
    public void addSubSequence(KnuthBox firstBox) {
        if (subSequences == null) {
            subSequences = new Stack();
        }
        subSequences.push(new SubSequence(firstBox));
    }

    public void addSubSequence(KnuthBox firstBox, int orphanRowLimit) {
        if (subSequences == null) {
            subSequences = new Stack();
        }
        subSequences.push(new SubSequence(firstBox, orphanRowLimit));
    }
    
    public boolean hasSubSequence() {
        return !(subSequences == null || subSequences.isEmpty());
    }
    
    public SubSequence getSubSequence() {
        return (SubSequence) subSequences.peek();
    }

    public SubSequence removeSubSequence() {
        return (SubSequence) subSequences.pop();
    }

    /**
     * The iteration stops at the first resolved element (after line breaking).
     * After space resolution it is guaranteed that seq does not to contain
     * Paragraph or ListItemListElements until the first resolved element.
     * @param seq the Knuth Sequence
     * @param startIndex the start index
     */
    public void resolveElements(int startIndex) {
        resolveElements(startIndex, false);
    }
    
    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.KnuthSequence#resolveAndGetElement(int)
     */
    public KnuthElement resolveAndGetKnuthElement(int index) {
        resolveElements(index);
        if (index < size()) {
            return (KnuthElement) get(index);
        } else {
            return null;
        }
    }

    /**
     * Resolve all elements in seq
     * @param seq the Knuth Sequence
     */
    public void resolveElements() {
        resolveElements(0, true);
    }
    
    /**
     * This method iterates over seq starting at startIndex.
     * If it finds a ParagraphListElement, the paragraph is broken into lines,
     * and the ParagraphListElement is replaced by the resulting elements.
     * If it finds a ListItemListElement, the paragraphs in the next step
     * of the list item are broken into lines,
     * the elements of the next step are added to the sequence before the ListItemListElement,
     * and the ListItemListElement is removed from the sequence if all steps have been returned.  
     * Then space resolution is done on seq starting at startIndex.
     * @param seq the Knuth Sequence
     * @param startIndex the start index
     * @param doall resolve all elements or not
     */
    private void resolveElements(int startIndex, boolean doall) {
        for (int i = startIndex; i < size(); ++i) {
            ListElement elt = (ListElement) get(i);
            if (!doall && !elt.isUnresolvedElement()
                    && !(elt instanceof LineBreakingListElement)
                    && !hasSubSequence()) {
                break;
            }
            if (elt instanceof LineBreakingListElement) {
                LineBreakingListElement lbelt = (LineBreakingListElement) elt;
                boolean startOfSubsequence =
                    lbelt.lineBreakingIsStarting() && lbelt.isStartOfSubsequence();
                LinkedList lineElts = lbelt.doLineBreaking();
                
                if (startOfSubsequence) {
                    KnuthBox box = ElementListUtils.firstKnuthBox(lineElts);
                    if (box == null) {
                        log.debug("Could not find a KnuthBox in step");
                    } else {
                        addSubSequence(box, lbelt.getWidowRowLimit());
                    }
                }
                
                boolean endOfSubsequence = false;
                if (lbelt.lineBreakingIsFinished()) {
                    remove(i);
                    endOfSubsequence = lbelt.isEndOfSubsequence();
                }
                addAll(i, lineElts);
                
                if (endOfSubsequence) {
                    SubSequence sseq;
                    // may throw EmptyStackException
                    sseq = removeSubSequence();
                    int widowRowLimit = sseq.getWidowRowLimit();
                    int orphanRowLimit = lbelt.getOrphanRowLimit();
                    Object nextElt = get(i);
                    KnuthBox box = ElementListUtils.lastKnuthBox(lineElts);
                    if (box == null) {
                        log.debug("Could not find a KnuthBox in step");
                    } else {
                        int fromIndex = indexOf(sseq.getFirstBox());
                        int toIndex = indexOf(box);
                        List subList = subList(fromIndex, toIndex+1);
                        SpaceResolver.resolveElementList(subList, 0, true);
                        if (widowRowLimit != 0) {
                            ElementListUtils.removeLegalBreaks(subList, widowRowLimit);
                        }
                        if (orphanRowLimit != 0) {
                            ElementListUtils.removeLegalBreaksFromEnd(subList, orphanRowLimit);
                        }
                        i = indexOf(nextElt);
                    }
                }
                
                // consider the new element at i
                --i;
            }
        }
        SpaceResolver.resolveElementList(this, startIndex, doall);
        // resolveElements may have removed element startIndex
        // without adding any element, so that startIndex == par.size()
        if (startIndex >= size() - 1) {
            endBlockSequence();
        }
    }
    
}
