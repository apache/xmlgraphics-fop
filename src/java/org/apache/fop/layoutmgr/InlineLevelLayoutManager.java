/*
 * Copyright 2004 The Apache Software Foundation.
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

import java.util.LinkedList;
import java.util.List;

/**
 * The interface for LayoutManagers which generate inline areas
 */
public interface InlineLevelLayoutManager extends LayoutManager {

    /**
     * Get a sequence of KnuthElements representing the content 
     * of the node assigned to the LM
     * 
     * @param context   the LayoutContext used to store layout information
     * @param alignment the desired text alignement
     * @return          the list of KnuthElements
     */
    LinkedList getNextKnuthElements(LayoutContext context, int alignment);

    /**
     * Tell the LM to modify its data, adding a letter space 
     * to the word fragment represented by the given element,
     * and returning a corrected element
     *
     * @param element the element which must be given one more letter space
     * @return        the new element replacing the old one
     */
    KnuthElement addALetterSpaceTo(KnuthElement element);

    /**
     * Get the word chars corresponding to the given position
     *
     * @param sbChars the StringBuffer used to append word chars
     * @param pos     the Position referring to the needed word chars
     */
    void getWordChars(StringBuffer sbChars, Position pos);

    /**
     * Tell the LM to hyphenate a word
     *
     * @param pos the Position referring to the word
     * @param hc  the HyphContext storing hyphenation information
     */
    void hyphenate(Position pos, HyphContext hc);

    /**
     * Tell the LM to apply the changes due to hyphenation
     *
     * @param oldList the list of the old elements the changes refer to
     * @return        true if the LM had to change its data, false otherwise
     */
    boolean applyChanges(List oldList);

    /**
     * Get a sequence of KnuthElements representing the content 
     * of the node assigned to the LM, after changes have been applied
     *
     * @param oldList        the elements to replace
     * @param flaggedPenalty the penalty value for hyphenated lines
     * @param alignment      the desired text alignment
     * @return               the updated list of KnuthElements
     */
    LinkedList getChangedKnuthElements(List oldList, int flaggedPenalty,
                                       int alignment);
}
