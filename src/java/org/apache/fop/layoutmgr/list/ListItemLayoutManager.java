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

package org.apache.fop.layoutmgr.list;

import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.layoutmgr.BlockLevelLayoutManager;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.ConditionalElementListener;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.RelSide;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * LayoutManager for a list-item FO.
 * The list item contains a list item label and a list item body.
 */
public class ListItemLayoutManager extends BlockStackingLayoutManager 
                    implements ConditionalElementListener {
    private ListItemContentLayoutManager label;
    private ListItemContentLayoutManager body;

    private Block curBlockArea = null;

    private LinkedList labelList = null;
    private LinkedList bodyList = null;

    private int listItemHeight;
    
    private boolean discardBorderBefore;
    private boolean discardBorderAfter;
    private boolean discardPaddingBefore;
    private boolean discardPaddingAfter;
    private MinOptMax effSpaceBefore;
    private MinOptMax effSpaceAfter;
    
    private boolean keepWithNextPendingOnLabel;
    private boolean keepWithNextPendingOnBody;
  
    private class ListItemPosition extends Position {
        private int iLabelFirstIndex;
        private int iLabelLastIndex;
        private int iBodyFirstIndex;
        private int iBodyLastIndex;

        public ListItemPosition(LayoutManager lm, int labelFirst, int labelLast, 
                int bodyFirst, int bodyLast) {
            super(lm);
            iLabelFirstIndex = labelFirst;
            iLabelLastIndex = labelLast;
            iBodyFirstIndex = bodyFirst;
            iBodyLastIndex = bodyLast;
        }
        
        public int getLabelFirstIndex() {
            return iLabelFirstIndex;
        }
        
        public int getLabelLastIndex() {
            return iLabelLastIndex;
        }

        public int getBodyFirstIndex() {
            return iBodyFirstIndex;
        }
        
        public int getBodyLastIndex() {
            return iBodyLastIndex;
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            StringBuffer sb = new StringBuffer("ListItemPosition:");
            sb.append(getIndex()).append("(");
            sb.append("label:").append(iLabelFirstIndex).append("-").append(iLabelLastIndex);
            sb.append(" body:").append(iBodyFirstIndex).append("-").append(iBodyLastIndex);
            sb.append(")");
            return sb.toString();
        }
    }

    /**
     * Create a new list item layout manager.
     * @param node list-item to create the layout manager for
     */
    public ListItemLayoutManager(ListItem node) {
        super(node);
        setLabel(node.getLabel());
        setBody(node.getBody());
    }

    /**
     * Convenience method.
     * @return the ListBlock node
     */
    protected ListItem getListItemFO() {
        return (ListItem)fobj;
    }

    /**
     * Create a LM for the fo:list-item-label object
     * @param node the fo:list-item-label FO
     */
    public void setLabel(ListItemLabel node) {
        label = new ListItemContentLayoutManager(node);
        label.setParent(this);
    }

    /**
     * Create a LM for the fo:list-item-body object
     * @param node the fo:list-item-body FO
     */
    public void setBody(ListItemBody node) {
        body = new ListItemContentLayoutManager(node); 
        body.setParent(this);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager#initialize() */
    public void initialize() {
        foSpaceBefore = new SpaceVal(
                getListItemFO().getCommonMarginBlock().spaceBefore, this).getSpace();
        foSpaceAfter = new SpaceVal(
                getListItemFO().getCommonMarginBlock().spaceAfter, this).getSpace();
    }

    private void resetSpaces() {
        this.discardBorderBefore = false;        
        this.discardBorderAfter = false;        
        this.discardPaddingBefore = false;        
        this.discardPaddingAfter = false;
        this.effSpaceBefore = foSpaceBefore;
        this.effSpaceAfter = foSpaceAfter;
    }
    
    private int getIPIndents() {
        int iIndents = 0;
        iIndents += getListItemFO().getCommonMarginBlock().startIndent.getValue(this);
        iIndents += getListItemFO().getCommonMarginBlock().endIndent.getValue(this);
        return iIndents;
    }
    
    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        referenceIPD = context.getRefIPD();
        LayoutContext childLC;
        
        LinkedList returnList = new LinkedList();
        
        addKnuthElementsForSpaceBefore(returnList, alignment);
        
        //Spaces, border and padding to be repeated at each break
        addPendingMarks(context);

        // label
        childLC = new LayoutContext(0);
        childLC.setRefIPD(context.getRefIPD());
        label.initialize();
        labelList = label.getNextKnuthElements(childLC, alignment);
        
        //Space resolution as if the contents were placed in a new reference area
        //(see 6.8.3, XSL 1.0, section on Constraints, last paragraph)
        SpaceResolver.resolveElementList(labelList);
        ElementListObserver.observe(labelList, "list-item-label", label.getPartFO().getId());
        
        if (childLC.isKeepWithPreviousPending()) {
            context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
        }
        this.keepWithNextPendingOnLabel = childLC.isKeepWithNextPending();

        // body
        childLC = new LayoutContext(0);
        childLC.setRefIPD(context.getRefIPD());
        body.initialize();
        bodyList = body.getNextKnuthElements(childLC, alignment);

        //Space resolution as if the contents were placed in a new reference area
        //(see 6.8.3, XSL 1.0, section on Constraints, last paragraph)
        SpaceResolver.resolveElementList(bodyList);
        ElementListObserver.observe(bodyList, "list-item-body", body.getPartFO().getId());
        
        if (childLC.isKeepWithPreviousPending()) {
            context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
        }
        this.keepWithNextPendingOnBody = childLC.isKeepWithNextPending();

        // create a combined list
        LinkedList returnedList = getCombinedKnuthElementsForListItem(labelList, bodyList, context);

        // "wrap" the Position inside each element
        wrapPositionElements(returnedList, returnList, true);
        
        addKnuthElementsForSpaceAfter(returnList, alignment);

        if (keepWithNextPendingOnLabel || keepWithNextPendingOnBody || mustKeepWithNext()) {
            context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING);
        }
        if (mustKeepWithPrevious()) {
            context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
        }

        setFinished(true);
        resetSpaces();
        return returnList;
    }

    private LinkedList getCombinedKnuthElementsForListItem(LinkedList labelElements,
                                                           LinkedList bodyElements,
                                                           LayoutContext context) {
        //Copy elements to array lists to improve element access performance
        List[] elementLists = {new ArrayList(labelElements),
                               new ArrayList(bodyElements)};
        int[] fullHeights = {ElementListUtils.calcContentLength(elementLists[0]),
                ElementListUtils.calcContentLength(elementLists[1])};
        int[] partialHeights = {0, 0};
        int[] start = {-1, -1};
        int[] end = {-1, -1};

        int totalHeight = Math.max(fullHeights[0], fullHeights[1]);
        int step;
        int addedBoxHeight = 0;
        boolean keepWithNextActive = false;

        LinkedList returnList = new LinkedList();
        while ((step = getNextStep(elementLists, start, end, partialHeights))
               > 0) {
            
            if (end[0] + 1 == elementLists[0].size()) {
                if (keepWithNextPendingOnLabel) {
                    keepWithNextActive = true;
                }
            }
            if (end[1] + 1 == elementLists[1].size()) {
                if (keepWithNextPendingOnBody) {
                    keepWithNextActive = true;
                }
            }
            
            // compute penalty height and box height
            int penaltyHeight = step 
                + getMaxRemainingHeight(fullHeights, partialHeights) 
                - totalHeight;
            int boxHeight = step - addedBoxHeight - penaltyHeight;

            // add the new elements
            addedBoxHeight += boxHeight;
            ListItemPosition stepPosition = new ListItemPosition(this, 
                    start[0], end[0], start[1], end[1]);
            returnList.add(new KnuthBox(boxHeight, stepPosition, false));
            if (addedBoxHeight < totalHeight) {
                int p = 0;
                if (keepWithNextActive || mustKeepTogether()) {
                    p = KnuthPenalty.INFINITE;
                }
                //returnList.add(new KnuthPenalty(penaltyHeight, p, false, stepPosition, false));
                returnList.add(new BreakElement(stepPosition, penaltyHeight, p, 0, context));
            }
        }

        return returnList;
    }

    private int getNextStep(List[] elementLists, int[] start, int[] end, int[] partialHeights) {
        // backup of partial heights
        int[] backupHeights = {partialHeights[0], partialHeights[1]};

        // set starting points
        start[0] = end[0] + 1;
        start[1] = end[1] + 1;

        // get next possible sequence for label and body
        int seqCount = 0;
        for (int i = 0; i < start.length; i++) {
            while (end[i] + 1 < elementLists[i].size()) {
                end[i]++;
                KnuthElement el = (KnuthElement)elementLists[i].get(end[i]);
                if (el.isPenalty()) {
                    if (el.getP() < KnuthElement.INFINITE) {
                        //First legal break point
                        break;
                    }
                } else if (el.isGlue()) {
                    if (end[i] > 0) {
                        KnuthElement prev = (KnuthElement)elementLists[i].get(end[i] - 1);
                        if (prev.isBox()) {
                            //Second legal break point
                            break;
                        }
                    }
                    partialHeights[i] += el.getW();
                } else {
                    partialHeights[i] += el.getW();
                }
            }
            if (end[i] < start[i]) {
                partialHeights[i] = backupHeights[i];
            } else {
                seqCount++;
            }
        }
        if (seqCount == 0) {
            return 0;
        }
        
        // determine next step
        int step;
        if (backupHeights[0] == 0 && backupHeights[1] == 0) {
            // this is the first step: choose the maximum increase, so that
            // the smallest area in the first page will contain at least
            // a label area and a body area
            step = Math.max((end[0] >= start[0] ? partialHeights[0] : Integer.MIN_VALUE),
                            (end[1] >= start[1] ? partialHeights[1] : Integer.MIN_VALUE));
        } else {
            // this is not the first step: choose the minimum increase
            step = Math.min((end[0] >= start[0] ? partialHeights[0] : Integer.MAX_VALUE),
                            (end[1] >= start[1] ? partialHeights[1] : Integer.MAX_VALUE));
        }

        // reset bigger-than-step sequences
        for (int i = 0; i < partialHeights.length; i++) {
            if (partialHeights[i] > step) {
                partialHeights[i] = backupHeights[i];
                end[i] = start[i] - 1;
            }
        }

        return step;
    }

    private int getMaxRemainingHeight(int[] fullHeights, int[] partialHeights) {
        return Math.max(fullHeights[0] - partialHeights[0],
                        fullHeights[1] - partialHeights[1]);
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getChangedKnuthElements(java.util.List, int)
     */
    public LinkedList getChangedKnuthElements(List oldList, int alignment) {
        //log.debug(" LILM.getChanged> label");
        // label
        labelList = label.getChangedKnuthElements(labelList, alignment);

        //log.debug(" LILM.getChanged> body");
        // body
        // "unwrap" the Positions stored in the elements
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement oldElement = null;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement)oldListIterator.next();
            Position innerPosition = ((NonLeafPosition) oldElement.getPosition()).getPosition();
            //log.debug(" BLM> unwrapping: " + (oldElement.isBox() 
            //  ? "box    " : (oldElement.isGlue() ? "glue   " : "penalty")) 
            //  + " creato da " + oldElement.getLayoutManager().getClass().getName());
            //log.debug(" BLM> unwrapping:         " 
            //  + oldElement.getPosition().getClass().getName());
            if (innerPosition != null) {
                // oldElement was created by a descendant of this BlockLM
                oldElement.setPosition(innerPosition);
            } else {
                // thisElement was created by this BlockLM
                // modify its position in order to recognize it was not created
                // by a child
                oldElement.setPosition(new Position(this));
            }
        }

        LinkedList returnedList = body.getChangedKnuthElements(oldList, alignment);
        // "wrap" the Position inside each element
        LinkedList tempList = returnedList;
        KnuthElement tempElement;
        returnedList = new LinkedList();
        ListIterator listIter = tempList.listIterator();
        while (listIter.hasNext()) {
            tempElement = (KnuthElement)listIter.next();
            tempElement.setPosition(new NonLeafPosition(this, tempElement.getPosition()));
            returnedList.add(tempElement);
        }

        return returnedList;
    }

    /**
     * Add the areas for the break points.
     * This sets the offset of each cell as it is added.
     *
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);

        getPSLM().addIDToPage(getListItemFO().getId());

        LayoutContext lc = new LayoutContext(0);
        Position firstPos = null;
        Position lastPos = null;

        // "unwrap" the NonLeafPositions stored in parentIter
        LinkedList positionList = new LinkedList();
        Position pos;
        while (parentIter.hasNext()) {
            pos = (Position) parentIter.next();
            if (pos.getIndex() >= 0) {
                if (firstPos == null) {
                    firstPos = pos;
                }
                lastPos = pos;
            }
            if (pos instanceof NonLeafPosition && pos.getPosition() != null) {
                // pos contains a ListItemPosition created by this ListBlockLM
                positionList.add(((NonLeafPosition) pos).getPosition());
            }
        }

        if (markers != null) {
            getCurrentPV().addMarkers(markers, true, isFirst(firstPos), isLast(lastPos));
        }

        // use the first and the last ListItemPosition to determine the 
        // corresponding indexes in the original labelList and bodyList
        int labelFirstIndex = ((ListItemPosition) positionList.getFirst()).getLabelFirstIndex();
        int labelLastIndex = ((ListItemPosition) positionList.getLast()).getLabelLastIndex();
        int bodyFirstIndex = ((ListItemPosition) positionList.getFirst()).getBodyFirstIndex();
        int bodyLastIndex = ((ListItemPosition) positionList.getLast()).getBodyLastIndex();

        int lastBreak;
        //Determine last break if any
        lastBreak = labelFirstIndex - 1;
        while (lastBreak >= 0) {
            KnuthElement el = (KnuthElement)labelList.get(lastBreak);
            if (el.isPenalty() && el.getP() < KnuthElement.INFINITE) {
                break;
            }
            lastBreak--;
        }
        SpaceResolver.performConditionalsNotification(labelList, 
                labelFirstIndex, labelLastIndex, lastBreak);

        //Determine last break if any
        lastBreak = bodyFirstIndex - 1;
        while (lastBreak >= 0) {
            KnuthElement el = (KnuthElement)bodyList.get(lastBreak);
            if (el.isPenalty() && el.getP() < KnuthElement.INFINITE) {
                break;
            }
            lastBreak--;
        }
        SpaceResolver.performConditionalsNotification(bodyList, 
                bodyFirstIndex, bodyLastIndex, lastBreak);
        
        // add label areas
        if (labelFirstIndex <= labelLastIndex) {
            KnuthPossPosIter labelIter = new KnuthPossPosIter(labelList, 
                    labelFirstIndex, labelLastIndex + 1);
            lc.setFlags(LayoutContext.FIRST_AREA, layoutContext.isFirstArea());
            lc.setFlags(LayoutContext.LAST_AREA, layoutContext.isLastArea());
            // set the space adjustment ratio
            lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
            // TO DO: use the right stack limit for the label
            lc.setStackLimit(layoutContext.getStackLimit());
            label.addAreas(labelIter, lc);
        }

        // reset the area bpd after adding the label areas and before adding the body areas
        int savedBPD = 0;
        if (labelFirstIndex <= labelLastIndex
            && bodyFirstIndex <= bodyLastIndex) {
            savedBPD = curBlockArea.getBPD();
            curBlockArea.setBPD(0);
        }

        // add body areas
        if (bodyFirstIndex <= bodyLastIndex) {
            KnuthPossPosIter bodyIter = new KnuthPossPosIter(bodyList, 
                    bodyFirstIndex, bodyLastIndex + 1);
            lc.setFlags(LayoutContext.FIRST_AREA, layoutContext.isFirstArea());
            lc.setFlags(LayoutContext.LAST_AREA, layoutContext.isLastArea());
            // set the space adjustment ratio
            lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
            // TO DO: use the right stack limit for the body
            lc.setStackLimit(layoutContext.getStackLimit());
            body.addAreas(bodyIter, lc);
        }

        // after adding body areas, set the maximum area bpd
        if (curBlockArea.getBPD() < savedBPD) {
            curBlockArea.setBPD(savedBPD);
        }

        if (markers != null) {
            getCurrentPV().addMarkers(markers, false, isFirst(firstPos), isLast(lastPos));
        }

        // We are done with this area add the background
        TraitSetter.addBackground(curBlockArea, 
                getListItemFO().getCommonBorderPaddingBackground(),
                this);
        TraitSetter.addSpaceBeforeAfter(curBlockArea, layoutContext.getSpaceAdjust(), 
                effSpaceBefore, effSpaceAfter);

        flush();

        curBlockArea = null;
        resetSpaces();
    }

    /**
     * Get the height of the list item after adjusting.
     * Should only be called after adding the list item areas.
     *
     * @return the height of this list item after adjustment
     */
    public int getListItemHeight() {
        return listItemHeight;
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
     * @return the parent are for the child
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();

            // Set up dimensions
            /*Area parentArea =*/ parentLM.getParentArea(curBlockArea);
            
            // set traits
            TraitSetter.setProducerID(curBlockArea, getListItemFO().getId());
            TraitSetter.addBorders(curBlockArea, 
                    getListItemFO().getCommonBorderPaddingBackground(), 
                    discardBorderBefore, discardBorderAfter, false, false, this);
            TraitSetter.addPadding(curBlockArea, 
                    getListItemFO().getCommonBorderPaddingBackground(), 
                    discardPaddingBefore, discardPaddingAfter, false, false, this);
            TraitSetter.addMargins(curBlockArea,
                    getListItemFO().getCommonBorderPaddingBackground(), 
                    getListItemFO().getCommonMarginBlock(), this);
            TraitSetter.addBreaks(curBlockArea, 
                    getListItemFO().getBreakBefore(), 
                    getListItemFO().getBreakAfter());
            
            int contentIPD = referenceIPD - getIPIndents();
            curBlockArea.setIPD(contentIPD);

            setCurrentArea(curBlockArea);
        }
        return curBlockArea;
    }

    /**
     * Add the child.
     * Rows return the areas returned by the child elements.
     * This simply adds the area to the parent layout manager.
     *
     * @param childArea the child area
     */
    public void addChildArea(Area childArea) {
        if (curBlockArea != null) {
            curBlockArea.addBlock((Block) childArea);
        }
    }

    /**
     * Reset the position of this layout manager.
     *
     * @param resetPos the position to reset to
     */
    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
        }
    }
    
    /** @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepTogether() */
    public boolean mustKeepTogether() {
        //TODO Keeps will have to be more sophisticated sooner or later
        return ((BlockLevelLayoutManager)getParent()).mustKeepTogether() 
                || !getListItemFO().getKeepTogether().getWithinPage().isAuto()
                || !getListItemFO().getKeepTogether().getWithinColumn().isAuto();
    }

    /** @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithPrevious() */
    public boolean mustKeepWithPrevious() {
        return !getListItemFO().getKeepWithPrevious().getWithinPage().isAuto()
            || !getListItemFO().getKeepWithPrevious().getWithinColumn().isAuto();
    }

    /** @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithNext() */
    public boolean mustKeepWithNext() {
        return !getListItemFO().getKeepWithNext().getWithinPage().isAuto()
                || !getListItemFO().getKeepWithNext().getWithinColumn().isAuto();
    }

    /** @see org.apache.fop.layoutmgr.ConditionalElementListener */
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

    /** @see org.apache.fop.layoutmgr.ConditionalElementListener */
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

    /** @see org.apache.fop.layoutmgr.ConditionalElementListener */
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

