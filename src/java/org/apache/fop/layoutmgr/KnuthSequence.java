/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
     * TODO In principle the LayoutManager can also be retrieved from the elements in the sequence.
     * @param sequence The sequence that is to be appended.
     * @param lm The LayoutManager for the Position that may have to be created. 
     * @return whether the sequence was succesfully appended to this sequence.
     */
    public abstract boolean appendSequence(KnuthSequence sequence, LayoutManager lm);
    
    /**
     * Append sequence to this sequence if it can be appended.
     * If that is not possible, close this sequence.
     * TODO In principle the LayoutManager can also be retrieved from the elements in the sequence.
     * @param sequence The sequence that is to be appended.
     * @param lm The LayoutManager for the Position that may have to be created. 
     * @return whether the sequence was succesfully appended to this sequence.
     */
    public abstract boolean appendSequenceOrClose(KnuthSequence sequence, LayoutManager lm);
    
    /**
     * Wrap the Positions of the elements of this sequence in a Position for LayoutManager lm.
     * @param lm The LayoutManager for the Positions that will be created.
     */
    public void wrapPositions(LayoutManager lm) {
        ListIterator listIter = listIterator();
        KnuthElement element;
        while (listIter.hasNext()) {
            element = (KnuthElement) listIter.next();
            element.setPosition
            (lm.notifyPos(new NonLeafPosition(lm, element.getPosition())));
        }
    }
    
    /**
     * @return the last element of this sequence.
     */
    public KnuthElement getLast() {
        int idx = size();
        if (idx == 0) {
            return null; 
        }
        return (KnuthElement) get(idx - 1);
    }

    /**
     * Remove the last element of this sequence.
     * @return the removed element.
     */
    public KnuthElement removeLast() {
        int idx = size();
        if (idx == 0) {
            return null; 
        }
        return (KnuthElement) remove(idx - 1);
    }

    /**
     * @param index The index of the element to be returned
     * @return the element at index index.
     */
    public KnuthElement getElement(int index) {
        return (KnuthElement) get(index);
    }

    /**
     * Is this an inline or a block sequence?
     * @return true if this is an inline sequence
     */
    public abstract boolean isInlineSequence();

}
