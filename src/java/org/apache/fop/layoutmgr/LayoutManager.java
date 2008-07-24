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

import java.util.List;

import org.apache.fop.area.Area;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FObj;

/**
 * The interface for all LayoutManagers.
 */
public interface LayoutManager extends PercentBaseContext {

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
     * initialize the layout manager. Allows each layout manager
     * to calculate often used values.
     */
    void initialize();

    /**
     * Get the active PageSequenceLayoutManager instance for this
     * layout process.
     * @return the PageSequenceLayoutManager
     */
    PageSequenceLayoutManager getPSLM();

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
     * @param alignment the desired text alignment
     * @return          the list of KnuthElements
     */
    List getNextKnuthElements(LayoutContext context, int alignment);

    /**
     * Get a sequence of KnuthElements representing the content
     * of the node assigned to the LM, after changes have been applied
     *
     * In the context of line breaking, this method is called after hyphenation has
     * been performed, in order to receive the sequence of elements representing the
     * text together with all possible hyphenation points.
     * For example, if the text "representation" originates a single box element
     * when getNextKnuthElements() is called, it will be now split in syllables
     * (rep-re-sen-ta-tion) each one originating a box and divided by additional
     * elements allowing a line break.
     *
     * In the context of page breaking, this method is called only if the pages need
     * to be "vertically justified" modifying (also) the quantity of lines created by
     * the paragraphs, and after a first page breaking has been performed.
     * According to the result of the first page breaking, each paragraph now knows
     * how many lines it must create (among the existing layout possibilities) and
     * has to create a sequence of elements representing this layout; in particular,
     * each box, representing a line, will contain a LineBreakPositions that will be
     * used in the addAreas() phase.
     *
     * LMs having children look at the old list of elements in order to know which
     * ones they must get the new elements from, as break conditions of preserved
     * linefeeds can divide children into smaller groups (page sequences or
     * paragraphs).
     * LMs having no children can simply return the old elements if they have nothing
     * to change.
     *
     * Inline LMs need to know the text alignment because it affects the elements
     * representing feasible breaks between syllables.
     *
     * @param oldList        the elements to replace
     * @param alignment      the desired text alignment
     * @return               the updated list of KnuthElements
     */
    List getChangedKnuthElements(List oldList, int alignment);

    /**
     * Returns the IPD of the content area
     * @return the IPD of the content area
     */
    int getContentAreaIPD();

    /**
     * Returns the BPD of the content area
     * @return the BPD of the content area
     */
    int getContentAreaBPD();

    /**
     * Returns an indication if the layout manager generates a reference area.
     * @return True if the layout manager generates a reference area
     */
    boolean getGeneratesReferenceArea();

    /**
     * Returns an indication if the layout manager generates a block area.
     * @return True if the layout manager generates a block area
     */
    boolean getGeneratesBlockArea();

    /**
     * Returns an indication if the layout manager generates a line area.
     * @return True if the layout manager generates a line area
     */
    boolean getGeneratesLineArea();

    /**
     * Returns the fo this layout manager is associated with.
     * @return The fo for this layout manager or null.
     */
    FObj getFObj();

    /**
     * Adds a Position to the Position participating in the first|last determination by assigning
     * it a unique position index.
     * @param pos the Position
     * @return the same Position but with a position index
     */
    Position notifyPos(Position pos);
}
