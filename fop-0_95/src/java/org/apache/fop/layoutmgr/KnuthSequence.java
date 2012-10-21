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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a list of Knuth elements.
 */
/**
 * 
 */
public abstract class KnuthSequence extends ArrayList {
    /**
     * Creates a new and empty list.
     */
    public KnuthSequence() {
        super();
    }

    /**
     * Creates a new list from an existing list.
     * @param list The list from which to create the new list.
     */
    public KnuthSequence(List list) {
        super(list);
    }

    /**
     * Marks the start of the sequence.
     */
    public void startSequence() {
    }

    /**
     * Finalizes a Knuth sequence.
     * @return a finalized sequence.
     */
    public abstract KnuthSequence endSequence();

    /**
     * Can sequence be appended to this sequence?
     * @param sequence The sequence that may be appended.
     * @return whether the sequence can be appended to this sequence.
     */
    public abstract boolean canAppendSequence(KnuthSequence sequence);

    /**
     * Append sequence to this sequence if it can be appended.
     * @param sequence The sequence that is to be appended.
     * @param keepTogether Whether the two sequences must be kept together.
     * @param breakElement The BreakElement that may be inserted between the two sequences. 
     * @return whether the sequence was succesfully appended to this sequence.
     */
    public abstract boolean appendSequence(KnuthSequence sequence, boolean keepTogether,
                                           BreakElement breakElement);
    
    /**
     * Append sequence to this sequence if it can be appended.
     * @param sequence The sequence that is to be appended.
     * @return whether the sequence was succesfully appended to this sequence.
     */
    public abstract boolean appendSequence(KnuthSequence sequence);
    
    /**
     * Append sequence to this sequence if it can be appended.
     * If that is not possible, close this sequence.
     * @param sequence The sequence that is to be appended.
     * @return whether the sequence was succesfully appended to this sequence.
     */
    public boolean appendSequenceOrClose(KnuthSequence sequence) {
        if (!appendSequence(sequence)) {
            endSequence();
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Append sequence to this sequence if it can be appended.
     * If that is not possible, close this sequence.
     * @param sequence The sequence that is to be appended.
     * @param keepTogether Whether the two sequences must be kept together.
     * @param breakElement The BreakElement that may be inserted between the two sequences. 
     * @return whether the sequence was succesfully appended to this sequence.
     */
    public boolean appendSequenceOrClose(KnuthSequence sequence, boolean keepTogether,
                                         BreakElement breakElement) {
        if (!appendSequence(sequence, keepTogether, breakElement)) {
            endSequence();
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Wrap the Positions of the elements of this sequence in a Position for LayoutManager lm.
     * @param lm The LayoutManager for the Positions that will be created.
     */
    public void wrapPositions(LayoutManager lm) {
        ListIterator listIter = listIterator();
        ListElement element;
        while (listIter.hasNext()) {
            element = (ListElement) listIter.next();
            element.setPosition
            (lm.notifyPos(new NonLeafPosition(lm, element.getPosition())));
        }
    }
    
    /**
     * @return the last element of this sequence.
     */
    public ListElement getLast() {
        int idx = size();
        if (idx == 0) {
            return null; 
        }
        return (ListElement) get(idx - 1);
    }

    /**
     * Remove the last element of this sequence.
     * @return the removed element.
     */
    public ListElement removeLast() {
        int idx = size();
        if (idx == 0) {
            return null; 
        }
        return (ListElement) remove(idx - 1);
    }

    /**
     * @param index The index of the element to be returned
     * @return the element at index index.
     */
    public ListElement getElement(int index) {
        return (ListElement) get(index);
    }

    /**
     * Is this an inline or a block sequence?
     * @return true if this is an inline sequence
     */
    public abstract boolean isInlineSequence();

}
