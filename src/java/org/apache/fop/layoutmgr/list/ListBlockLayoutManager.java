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
 
package org.apache.fop.layoutmgr.list;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.ConditionalElementListener;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KeepUtil;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.RelSide;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

/**
 * LayoutManager for a list-block FO.
 * A list block contains list items which are stacked within
 * the list block area..
 */
public class ListBlockLayoutManager extends BlockStackingLayoutManager 
                implements ConditionalElementListener {

    /**
     * logging instance
     */
    private static Log log = LogFactory.getLog(ListBlockLayoutManager.class);

    private Block curBlockArea;

    private boolean discardBorderBefore;
    private boolean discardBorderAfter;
    private boolean discardPaddingBefore;
    private boolean discardPaddingAfter;
    private MinOptMax effSpaceBefore;
    private MinOptMax effSpaceAfter;

    private static class StackingIter extends PositionIterator {
        StackingIter(Iterator parentIter) {
            super(parentIter);
        }

        protected LayoutManager getLM(Object nextObj) {
            return ((Position) nextObj).getLM();
        }

        protected Position getPos(Object nextObj) {
            return ((Position) nextObj);
        }
    }

    /**
     * Create a new list block layout manager.
     * @param node list-block to create the layout manager for
     */
    public ListBlockLayoutManager(ListBlock node) {
        super(node);
    }

    /**
     * Convenience method.
     * @return the ListBlock node
     */
    protected ListBlock getListBlockFO() {
        return (ListBlock)fobj;
    }

    /** {@inheritDoc} */
    public void initialize() {
        foSpaceBefore = new SpaceVal(
                getListBlockFO().getCommonMarginBlock().spaceBefore, this).getSpace();
        foSpaceAfter = new SpaceVal(
                getListBlockFO().getCommonMarginBlock().spaceAfter, this).getSpace();
        startIndent = getListBlockFO().getCommonMarginBlock().startIndent.getValue(this);
        endIndent = getListBlockFO().getCommonMarginBlock().endIndent.getValue(this); 
    }

    private void resetSpaces() {
        this.discardBorderBefore = false;        
        this.discardBorderAfter = false;        
        this.discardPaddingBefore = false;        
        this.discardPaddingAfter = false;
        this.effSpaceBefore = null;
        this.effSpaceAfter = null;
    }
    
    /** {@inheritDoc} */
    public List getNextKnuthElements(LayoutContext context, int alignment) {
        resetSpaces(); 
        List returnList = super.getNextKnuthElements(context, alignment);

        //fox:widow-content-limit
        int widowRowLimit = getListBlockFO().getWidowContentLimit().getValue(); 
        if (widowRowLimit != 0) {
            ElementListUtils.removeLegalBreaks(returnList, widowRowLimit);
        }

        //fox:orphan-content-limit
        int orphanRowLimit = getListBlockFO().getOrphanContentLimit().getValue(); 
        if (orphanRowLimit != 0) {
            ElementListUtils.removeLegalBreaksFromEnd(returnList, orphanRowLimit);
        }

        return returnList;
    }
   
    /** {@inheritDoc} */
    public List getChangedKnuthElements(List oldList, int alignment) {
        //log.debug("LBLM.getChangedKnuthElements>");
        return super.getChangedKnuthElements(oldList, alignment);
    }

    /**
     * The table area is a reference area that contains areas for
     * columns, bodies, rows and the contents are in cells.
     *
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);

        // if this will create the first block area in a page
        // and display-align is after or center, add space before
        if (layoutContext.getSpaceBefore() > 0) {
            addBlockSpacing(0.0, new MinOptMax(layoutContext.getSpaceBefore()));
        }

        addId();

        // the list block contains areas stacked from each list item

        LayoutManager childLM;
        LayoutContext lc = new LayoutContext(0);
        LayoutManager firstLM = null;
        LayoutManager lastLM = null;
        Position firstPos = null;
        Position lastPos = null;

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list; 
        LinkedList positionList = new LinkedList();
        Position pos;
        while (parentIter.hasNext()) {
            pos = (Position)parentIter.next();
            if (pos.getIndex() >= 0) {
                if (firstPos == null) {
                    firstPos = pos;
                }
                lastPos = pos;
            }
            if (pos instanceof NonLeafPosition
                    && (pos.getPosition() != null)
                    && pos.getPosition().getLM() != this) {
                // pos was created by a child of this ListBlockLM
                positionList.add(pos.getPosition());
                lastLM = pos.getPosition().getLM();
                if (firstLM == null) {
                    firstLM = lastLM;
                }
            }
        }

        addMarkersToPage(true, isFirst(firstPos), isLast(lastPos));

        StackingIter childPosIter = new StackingIter(positionList.listIterator());
        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // Add the block areas to Area
            // set the space adjustment ratio
            lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
            lc.setFlags(LayoutContext.FIRST_AREA, childLM == firstLM);
            lc.setFlags(LayoutContext.LAST_AREA, childLM == lastLM);
            lc.setStackLimitBP(layoutContext.getStackLimitBP());
            childLM.addAreas(childPosIter, lc);
        }

        addMarkersToPage(false, isFirst(firstPos), isLast(lastPos));

        // We are done with this area add the background
        TraitSetter.addBackground(curBlockArea, 
                getListBlockFO().getCommonBorderPaddingBackground(),
                this);
        TraitSetter.addSpaceBeforeAfter(curBlockArea, layoutContext.getSpaceAdjust(), 
                effSpaceBefore, effSpaceAfter);

        flush();

        curBlockArea = null;
        resetSpaces();
        
        checkEndOfLayout(lastPos);
    }

    /**
     * Return an Area which can contain the passed childArea. The childArea
     * may not yet have any content, but it has essential traits set.
     * In general, if the LayoutManager already has an Area it simply returns
     * it. Otherwise, it makes a new Area of the appropriate class.
     * It gets a parent area for its area by calling its parent LM.
     * Finally, based on the dimensions of the parent area, it initializes
     * its own area. This includes setting the content IPD and the maximum
     * BPD.
     *
     * @param childArea the child area
     * @return the parent area of the child
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();
            
            // Set up dimensions
            // Must get dimensions from parent area
            /*Area parentArea =*/ parentLM.getParentArea(curBlockArea);

            // set traits
            TraitSetter.setProducerID(curBlockArea, getListBlockFO().getId());
            TraitSetter.addBorders(curBlockArea, 
                    getListBlockFO().getCommonBorderPaddingBackground(), 
                    discardBorderBefore, discardBorderAfter, false, false, this);
            TraitSetter.addPadding(curBlockArea, 
                    getListBlockFO().getCommonBorderPaddingBackground(), 
                    discardPaddingBefore, discardPaddingAfter, false, false, this);
            TraitSetter.addMargins(curBlockArea,
                    getListBlockFO().getCommonBorderPaddingBackground(), 
                    getListBlockFO().getCommonMarginBlock(),
                    this);
            TraitSetter.addBreaks(curBlockArea, 
                    getListBlockFO().getBreakBefore(), 
                    getListBlockFO().getBreakAfter());
            
            int contentIPD = referenceIPD - getIPIndents();
            curBlockArea.setIPD(contentIPD);
            
            setCurrentArea(curBlockArea);
        }
        return curBlockArea;
    }

    /**
     * Add the child area to this layout manager.
     *
     * @param childArea the child area to add
     */
    public void addChildArea(Area childArea) {
        if (curBlockArea != null) {
            curBlockArea.addBlock((Block) childArea);
        }
    }

    /** {@inheritDoc} */
    public int getKeepTogetherStrength() {
        int strength = KeepUtil.getCombinedBlockLevelKeepStrength(
                getListBlockFO().getKeepTogether());
        strength = Math.max(strength, getParentKeepTogetherStrength());
        return strength;
    }
    
    /** {@inheritDoc} */
    public int getKeepWithNextStrength() {
        return KeepUtil.getCombinedBlockLevelKeepStrength(getListBlockFO().getKeepWithNext());
    }

    /** {@inheritDoc} */
    public int getKeepWithPreviousStrength() {
        return KeepUtil.getCombinedBlockLevelKeepStrength(getListBlockFO().getKeepWithPrevious());
    }

    /** {@inheritDoc} */
    public void notifySpace(RelSide side, MinOptMax effectiveLength) {
        if (RelSide.BEFORE == side) {
            if (log.isDebugEnabled()) {
                log.debug(this + ": Space " + side + ", " 
                        + this.effSpaceBefore + "-> " + effectiveLength);
            }
            this.effSpaceBefore = effectiveLength;
        } else {
            if (log.isDebugEnabled()) {
                log.debug(this + ": Space " + side + ", " 
                        + this.effSpaceAfter + "-> " + effectiveLength);
            }
            this.effSpaceAfter = effectiveLength;
        }
    }

    /** {@inheritDoc} */
    public void notifyBorder(RelSide side, MinOptMax effectiveLength) {
        if (effectiveLength == null) {
            if (RelSide.BEFORE == side) {
                this.discardBorderBefore = true;
            } else {
                this.discardBorderAfter = true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(this + ": Border " + side + " -> " + effectiveLength);
        }
    }

    /** {@inheritDoc} */
    public void notifyPadding(RelSide side, MinOptMax effectiveLength) {
        if (effectiveLength == null) {
            if (RelSide.BEFORE == side) {
                this.discardPaddingBefore = true;
            } else {
                this.discardPaddingAfter = true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(this + ": Padding " + side + " -> " + effectiveLength);
        }
    }

}

