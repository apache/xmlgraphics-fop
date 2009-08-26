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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.ConditionalElementListener;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.Keep;
import org.apache.fop.layoutmgr.KnuthBlockBox;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.RelSide;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

/**
 * LayoutManager for a list-item FO.
 * The list item contains a list item label and a list item body.
 */
public class ListItemLayoutManager extends BlockStackingLayoutManager
                    implements ConditionalElementListener {

    /**
     * logging instance
     */
    private static Log log = LogFactory.getLog(ListItemLayoutManager.class);

    private ListItemContentLayoutManager label;
    private ListItemContentLayoutManager body;

    private Block curBlockArea = null;

    private List labelList = null;
    private List bodyList = null;

    private boolean discardBorderBefore;
    private boolean discardBorderAfter;
    private boolean discardPaddingBefore;
    private boolean discardPaddingAfter;
    private MinOptMax effSpaceBefore;
    private MinOptMax effSpaceAfter;

    private Keep keepWithNextPendingOnLabel;
    private Keep keepWithNextPendingOnBody;

    private int listItemHeight;

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

        /** {@inheritDoc} */
        public boolean generatesAreas() {
            return true;
        }

        /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public void initialize() {
        foSpaceBefore = new SpaceVal(
                getListItemFO().getCommonMarginBlock().spaceBefore, this).getSpace();
        foSpaceAfter = new SpaceVal(
                getListItemFO().getCommonMarginBlock().spaceAfter, this).getSpace();
        startIndent = getListItemFO().getCommonMarginBlock().startIndent.getValue(this);
        endIndent = getListItemFO().getCommonMarginBlock().endIndent.getValue(this);
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
        referenceIPD = context.getRefIPD();
        LayoutContext childLC;

        List returnList = new LinkedList();

        if (!breakBeforeServed) {
            breakBeforeServed = true;
            if (!context.suppressBreakBefore()) {
                if (addKnuthElementsForBreakBefore(returnList, context)) {
                    return returnList;
                }
            }
        }

        addKnuthElementsForSpaceBefore(returnList, alignment);

        addKnuthElementsForBorderPaddingBefore(returnList, !firstVisibleMarkServed);
        firstVisibleMarkServed = true;

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

        context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
        this.keepWithNextPendingOnLabel = childLC.getKeepWithNextPending();

        // body
        childLC = new LayoutContext(0);
        childLC.setRefIPD(context.getRefIPD());
        body.initialize();
        bodyList = body.getNextKnuthElements(childLC, alignment);

        //Space resolution as if the contents were placed in a new reference area
        //(see 6.8.3, XSL 1.0, section on Constraints, last paragraph)
        SpaceResolver.resolveElementList(bodyList);
        ElementListObserver.observe(bodyList, "list-item-body", body.getPartFO().getId());

        context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
        this.keepWithNextPendingOnBody = childLC.getKeepWithNextPending();

        // create a combined list
        List returnedList = getCombinedKnuthElementsForListItem(labelList, bodyList, context);

        // "wrap" the Position inside each element
        wrapPositionElements(returnedList, returnList, true);

        addKnuthElementsForBorderPaddingAfter(returnList, true);
        addKnuthElementsForSpaceAfter(returnList, alignment);
        addKnuthElementsForBreakAfter(returnList, context);

        context.updateKeepWithNextPending(this.keepWithNextPendingOnLabel);
        context.updateKeepWithNextPending(this.keepWithNextPendingOnBody);
        context.updateKeepWithNextPending(getKeepWithNext());
        context.updateKeepWithPreviousPending(getKeepWithPrevious());

        setFinished(true);
        resetSpaces();
        return returnList;
    }

    private List getCombinedKnuthElementsForListItem(List labelElements,
            List bodyElements, LayoutContext context) {
        // Copy elements to array lists to improve element access performance
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
        Keep keepWithNextActive = Keep.KEEP_AUTO;

        LinkedList returnList = new LinkedList();
        while ((step = getNextStep(elementLists, start, end, partialHeights)) > 0) {

            if (end[0] + 1 == elementLists[0].size()) {
                keepWithNextActive = keepWithNextActive.compare(keepWithNextPendingOnLabel);
            }
            if (end[1] + 1 == elementLists[1].size()) {
                keepWithNextActive = keepWithNextActive.compare(keepWithNextPendingOnBody);
            }

            // compute penalty height and box height
            int penaltyHeight = step
                + getMaxRemainingHeight(fullHeights, partialHeights)
                - totalHeight;

            //Additional penalty height from penalties in the source lists
            int additionalPenaltyHeight = 0;
            int stepPenalty = 0;
            KnuthElement endEl = (KnuthElement)elementLists[0].get(end[0]);
            if (endEl instanceof KnuthPenalty) {
                additionalPenaltyHeight = endEl.getW();
                stepPenalty = Math.max(stepPenalty, endEl.getP());
            }
            endEl = (KnuthElement)elementLists[1].get(end[1]);
            if (endEl instanceof KnuthPenalty) {
                additionalPenaltyHeight = Math.max(
                        additionalPenaltyHeight, endEl.getW());
                stepPenalty = Math.max(stepPenalty, endEl.getP());
            }

            int boxHeight = step - addedBoxHeight - penaltyHeight;
            penaltyHeight += additionalPenaltyHeight; //Add AFTER calculating boxHeight!

            // collect footnote information
            // TODO this should really not be done like this. ListItemLM should remain as
            // footnote-agnostic as possible
            LinkedList footnoteList = null;
            ListElement el;
            for (int i = 0; i < elementLists.length; i++) {
                for (int j = start[i]; j <= end[i]; j++) {
                    el = (ListElement) elementLists[i].get(j);
                    if (el instanceof KnuthBlockBox && ((KnuthBlockBox) el).hasAnchors()) {
                        if (footnoteList == null) {
                            footnoteList = new LinkedList();
                        }
                        footnoteList.addAll(((KnuthBlockBox) el).getFootnoteBodyLMs());
                    }
                }
            }

            // add the new elements
            addedBoxHeight += boxHeight;
            ListItemPosition stepPosition = new ListItemPosition(this,
                    start[0], end[0], start[1], end[1]);
            if (footnoteList == null) {
                returnList.add(new KnuthBox(boxHeight, stepPosition, false));
            } else {
                returnList.add(new KnuthBlockBox(boxHeight, footnoteList, stepPosition, false));
            }

            if (addedBoxHeight < totalHeight) {
                Keep keep = keepWithNextActive.compare(getKeepTogether());
                int p = stepPenalty;
                if (p > -KnuthElement.INFINITE) {
                    p = Math.max(p, keep.getPenalty());
                }
                returnList.add(new BreakElement(stepPosition, penaltyHeight, p, keep.getContext(),
                        context));
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
     * {@inheritDoc}
     */
    public List getChangedKnuthElements(List oldList, int alignment) {
        //log.debug(" LILM.getChanged> label");
        // label
        labelList = label.getChangedKnuthElements(labelList, alignment);

        //log.debug(" LILM.getChanged> body");
        // body
        // "unwrap" the Positions stored in the elements
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement oldElement;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement)oldListIterator.next();
            Position innerPosition = oldElement.getPosition().getPosition();
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

        List returnedList = body.getChangedKnuthElements(oldList, alignment);
        // "wrap" the Position inside each element
        List tempList = returnedList;
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

        addId();

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
                positionList.add(pos.getPosition());
            }
        }

        addMarkersToPage(true, isFirst(firstPos), isLast(lastPos));

        // use the first and the last ListItemPosition to determine the
        // corresponding indexes in the original labelList and bodyList
        int labelFirstIndex = ((ListItemPosition) positionList.getFirst()).getLabelFirstIndex();
        int labelLastIndex = ((ListItemPosition) positionList.getLast()).getLabelLastIndex();
        int bodyFirstIndex = ((ListItemPosition) positionList.getFirst()).getBodyFirstIndex();
        int bodyLastIndex = ((ListItemPosition) positionList.getLast()).getBodyLastIndex();

        //Determine previous break if any
        int previousBreak = ElementListUtils.determinePreviousBreak(labelList, labelFirstIndex);
        SpaceResolver.performConditionalsNotification(labelList,
                labelFirstIndex, labelLastIndex, previousBreak);

        //Determine previous break if any
        previousBreak = ElementListUtils.determinePreviousBreak(bodyList, bodyFirstIndex);
        SpaceResolver.performConditionalsNotification(bodyList,
                bodyFirstIndex, bodyLastIndex, previousBreak);

        // add label areas
        if (labelFirstIndex <= labelLastIndex) {
            KnuthPossPosIter labelIter = new KnuthPossPosIter(labelList,
                    labelFirstIndex, labelLastIndex + 1);
            lc.setFlags(LayoutContext.FIRST_AREA, layoutContext.isFirstArea());
            lc.setFlags(LayoutContext.LAST_AREA, layoutContext.isLastArea());
            // set the space adjustment ratio
            lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
            // TO DO: use the right stack limit for the label
            lc.setStackLimitBP(layoutContext.getStackLimitBP());
            label.addAreas(labelIter, lc);
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
            lc.setStackLimitBP(layoutContext.getStackLimitBP());
            body.addAreas(bodyIter, lc);
        }

        // after adding body areas, set the maximum area bpd
        int childCount = curBlockArea.getChildAreas().size();
        assert childCount >= 1 && childCount <= 2;
        int itemBPD = ((Block)curBlockArea.getChildAreas().get(0)).getAllocBPD();
        if (childCount == 2) {
            itemBPD = Math.max(itemBPD, ((Block)curBlockArea.getChildAreas().get(1)).getAllocBPD());
        }
        curBlockArea.setBPD(itemBPD);

        addMarkersToPage(false, isFirst(firstPos), isLast(lastPos));

        // We are done with this area add the background
        TraitSetter.addBackground(curBlockArea,
                getListItemFO().getCommonBorderPaddingBackground(),
                this);
        TraitSetter.addSpaceBeforeAfter(curBlockArea, layoutContext.getSpaceAdjust(),
                effSpaceBefore, effSpaceAfter);

        flush();

        curBlockArea = null;
        resetSpaces();

        checkEndOfLayout(lastPos);
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

    /** {@inheritDoc} */
    public KeepProperty getKeepTogetherProperty() {
        return getListItemFO().getKeepTogether();
    }

    /** {@inheritDoc} */
    public KeepProperty getKeepWithPreviousProperty() {
        return getListItemFO().getKeepWithPrevious();
    }

    /** {@inheritDoc} */
    public KeepProperty getKeepWithNextProperty() {
        return getListItemFO().getKeepWithNext();
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

    /** {@inheritDoc} */
    public void reset() {
        super.reset();
        label.reset();
        body.reset();
    }


}

