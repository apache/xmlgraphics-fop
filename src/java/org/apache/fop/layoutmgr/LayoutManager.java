/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.fop.area.Area;

/**
 * The interface for all LayoutManagers.
 */
public interface LayoutManager {

    /**
     * Set the parent layout manager.
     * The parent layout manager is required for adding areas.
     *
     * @param lm the parent layout manager
     */
    void setParent(LayoutManager lm);

    /**
     * Get the parent layout manager.
     * @return the parent layout manager.
     */
    LayoutManager getParent();

    /**
     * Get the active PageSequenceLayoutManager instance for this
     * layout process.
     * @return the PageSequenceLayoutManager
     */
    PageSequenceLayoutManager getPSLM();

    /**
     * Generates inline areas.
     * This is used to check if the layout manager generates inline
     * areas.
     *
     * @return true if the layout manager generates inline areas
     */
    boolean generatesInlineAreas();

    /**
     * Reset to the position.
     *
     * @param position the Position to reset to
     */
    void resetPosition(Position position);

    /**
     * Get the word chars between two positions and
     * append to the string buffer. The positions could
     * span multiple layout managers.
     *
     * @param sbChars the string buffer to append the word chars
     * @param bp1 the start position
     * @param bp2 the end position
     */
    void getWordChars(StringBuffer sbChars, Position bp1,
                             Position bp2);

    /**
     * Return a value indicating whether this LayoutManager has laid out
     * all its content (or generated BreakPossibilities for all content.)
     *
     * @return true if this layout manager is finished
     */
    boolean isFinished();

    /**
     * Set a flag indicating whether the LayoutManager has laid out all
     * its content. This is generally called by the LM itself, but can
     * be called by a parentLM when backtracking.
     *
     * @param isFinished the value to set the finished flag to
     */
    void setFinished(boolean isFinished);

    /**
     * Get the parent area for an area.
     * This should get the parent depending on the class of the
     * area passed in.
     *
     * @param childArea the child area to get the parent for
     * @return the parent Area
     */
    Area getParentArea(Area childArea);

    /**
     * Add the area as a child of the current area.
     * This is called by child layout managers to add their
     * areas as children of the current area.
     *
     * @param childArea the child area to add
     */
    void addChildArea(Area childArea);

    /**
     * Tell the layout manager to add all the child areas implied
     * by Position objects which will be returned by the
     * Iterator.
     *
     * @param posIter the position iterator
     * @param context the context
     */
    void addAreas(PositionIterator posIter, LayoutContext context);

    /**
     * Create more child LMs of the parent, up to child LM index pos
     * @param pos index up to which child LMs are requested
     * @return true if requested index does exist
     */
    boolean createNextChildLMs(int pos);

    /**
     * @return the list of child LMs
     */
    List getChildLMs();

    /**
     * Add the LM in the argument to the list of child LMs;
     * set this LM as the parent;
     * initialize the LM.
     * @param lm the LM to be added
     */
    void addChildLM(LayoutManager lm);

    /**
     * Add the LMs in the argument to the list of child LMs;
     * @param newLMs the list of LMs to be added
     */
    void addChildLMs(List newLMs);

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
     * Get a sequence of KnuthElements representing the content 
     * of the node assigned to the LM, after changes have been applied
     *
     * @param oldList        the elements to replace
     * @param flaggedPenalty the penalty value for hyphenated lines
     * @param alignment      the desired text alignment
     * @return               the updated list of KnuthElements
     */
    LinkedList getChangedKnuthElements(List oldList, /*int flaggedPenalty,*/
                                       int alignment);
   
}
