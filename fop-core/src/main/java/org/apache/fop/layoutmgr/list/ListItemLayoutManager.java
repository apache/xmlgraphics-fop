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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.layoutmgr.BlockLayoutManager;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.BreakOpportunity;
import org.apache.fop.layoutmgr.BreakOpportunityHelper;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.FloatContentLayoutManager;
import org.apache.fop.layoutmgr.FootenoteUtil;
import org.apache.fop.layoutmgr.FootnoteBodyLayoutManager;
import org.apache.fop.layoutmgr.Keep;
import org.apache.fop.layoutmgr.KnuthBlockBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.PageProvider;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.layoutmgr.SpacedBorderedPaddedBlockLayoutManager;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.util.BreakUtil;

/**
 * LayoutManager for a list-item FO.
 * The list item contains a list item label and a list item body.
 */
public class ListItemLayoutManager extends SpacedBorderedPaddedBlockLayoutManager
        implements BreakOpportunity {

    /** logging instance */
    private static Log log = LogFactory.getLog(ListItemLayoutManager.class);

    private ListItemContentLayoutManager label;
    private ListItemContentLayoutManager body;

    private Block curBlockArea;

    private List<ListElement> labelList;
    private List<ListElement> bodyList;

    private Keep keepWithNextPendingOnLabel;
    private Keep keepWithNextPendingOnBody;

    public static class ListItemPosition extends Position {
        private int labelFirstIndex;
        private int labelLastIndex;
        private int bodyFirstIndex;
        private int bodyLastIndex;
        private Position originalLabelPosition;
        private Position originalBodyPosition;

        public ListItemPosition(LayoutManager lm, int labelFirst, int labelLast,
                int bodyFirst, int bodyLast) {
            super(lm);
            labelFirstIndex = labelFirst;
            labelLastIndex = labelLast;
            bodyFirstIndex = bodyFirst;
            bodyLastIndex = bodyLast;
        }

        public int getLabelFirstIndex() {
            return labelFirstIndex;
        }

        public int getLabelLastIndex() {
            return labelLastIndex;
        }

        public int getBodyFirstIndex() {
            return bodyFirstIndex;
        }

        public int getBodyLastIndex() {
            return bodyLastIndex;
        }

        /** {@inheritDoc} */
        public boolean generatesAreas() {
            return true;
        }

        /** {@inheritDoc} */
        public String toString() {
            StringBuffer sb = new StringBuffer("ListItemPosition:");
            sb.append(getIndex()).append("(");
            sb.append("label:").append(labelFirstIndex).append("-").append(labelLastIndex);
            sb.append(" body:").append(bodyFirstIndex).append("-").append(bodyLastIndex);
            sb.append(")");
            return sb.toString();
        }

        public Position getOriginalLabelPosition() {
            return originalLabelPosition;
        }

        public void setOriginalLabelPosition(Position originalLabelPosition) {
            this.originalLabelPosition = originalLabelPosition;
        }

        public Position getOriginalBodyPosition() {
            return originalBodyPosition;
        }

        public void setOriginalBodyPosition(Position originalBodyPosition) {
            this.originalBodyPosition = originalBodyPosition;
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

    @Override
    protected CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return getListItemFO().getCommonBorderPaddingBackground();
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
    @Override
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
    public List<ListElement> getNextKnuthElements(LayoutContext context, int alignment, Stack lmStack,
            Position restartPosition, LayoutManager restartAtLM) {
        referenceIPD = context.getRefIPD();
        LayoutContext childLC;

        List<ListElement> returnList = new LinkedList<ListElement>();

        if (!breakBeforeServed(context, returnList)) {
            return returnList;
        }

        addFirstVisibleMarks(returnList, context, alignment);

        // label
        childLC = makeChildLayoutContext(context);
        childLC.setFlags(LayoutContext.SUPPRESS_BREAK_BEFORE);
        label.initialize();
        boolean labelDone = false;
        Stack labelLMStack = null;
        Position labelRestartPosition = null;
        LayoutManager labelRestartLM = null;
        if (restartPosition != null && restartPosition instanceof ListItemPosition) {
            ListItemPosition lip = (ListItemPosition) restartPosition;
            if (lip.labelLastIndex <= lip.labelFirstIndex) {
                labelDone = true;
            } else {
                labelRestartPosition = lip.getOriginalLabelPosition();
                labelRestartLM = labelRestartPosition.getLM();
                LayoutManager lm = labelRestartLM;
                labelLMStack = new Stack();
                while (lm != this) {
                    labelLMStack.push(lm);
                    lm = lm.getParent();
                    if (lm instanceof ListItemContentLayoutManager) {
                        lm = lm.getParent();
                    }
                }
            }
        }
        labelList = !labelDone ? label.getNextKnuthElements(childLC, alignment, labelLMStack,
                labelRestartPosition, labelRestartLM) : (List) new LinkedList<KnuthElement>();

        //Space resolution as if the contents were placed in a new reference area
        //(see 6.8.3, XSL 1.0, section on Constraints, last paragraph)
        SpaceResolver.resolveElementList(labelList);
        ElementListObserver.observe(labelList, "list-item-label", label.getPartFO().getId());

        context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
        this.keepWithNextPendingOnLabel = childLC.getKeepWithNextPending();

        // body
        childLC = makeChildLayoutContext(context);
        childLC.setFlags(LayoutContext.SUPPRESS_BREAK_BEFORE);
        body.initialize();
        boolean bodyDone = false;
        Stack bodyLMStack = null;
        Position bodyRestartPosition = null;
        LayoutManager bodyRestartLM = null;
        if (restartPosition != null && restartPosition instanceof ListItemPosition) {
            ListItemPosition lip = (ListItemPosition) restartPosition;
            if (lip.bodyLastIndex <= lip.bodyFirstIndex) {
                bodyDone = true;
            } else {
                bodyRestartPosition = lip.getOriginalBodyPosition();
                bodyRestartLM = bodyRestartPosition.getLM();
                LayoutManager lm = bodyRestartLM;
                bodyLMStack = new Stack();
                while (lm != this) {
                    bodyLMStack.push(lm);
                    lm = lm.getParent();
                    if (lm instanceof ListItemContentLayoutManager) {
                        lm = lm.getParent();
                    }
                }
            }
        }
        bodyList = !bodyDone ? body.getNextKnuthElements(childLC, alignment, bodyLMStack,
                bodyRestartPosition, bodyRestartLM) : (List) new LinkedList<KnuthElement>();

        //Space resolution as if the contents were placed in a new reference area
        //(see 6.8.3, XSL 1.0, section on Constraints, last paragraph)
        SpaceResolver.resolveElementList(bodyList);
        ElementListObserver.observe(bodyList, "list-item-body", body.getPartFO().getId());

        context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
        this.keepWithNextPendingOnBody = childLC.getKeepWithNextPending();

        List<ListElement> returnedList = new LinkedList<ListElement>();
        if (!labelList.isEmpty() && labelList.get(0) instanceof KnuthBlockBox) {
            KnuthBlockBox kbb = (KnuthBlockBox) labelList.get(0);
            if (kbb.getWidth() == 0 && kbb.hasFloatAnchors()) {
                List<FloatContentLayoutManager> floats = kbb.getFloatContentLMs();
                returnedList.add(new KnuthBlockBox(0, Collections.emptyList(), null, false, floats));
                Keep keep = getKeepTogether();
                returnedList.add(new BreakElement(new LeafPosition(this, 0), keep.getPenalty(), keep
                        .getContext(), context));
                labelList.remove(0);
                labelList.remove(0);
            }
        }
        if (!bodyList.isEmpty() && bodyList.get(0) instanceof KnuthBlockBox) {
            KnuthBlockBox kbb = (KnuthBlockBox) bodyList.get(0);
            if (kbb.getWidth() == 0 && kbb.hasFloatAnchors()) {
                List<FloatContentLayoutManager> floats = kbb.getFloatContentLMs();
                returnedList.add(new KnuthBlockBox(0, Collections.emptyList(), null, false, floats));
                Keep keep = getKeepTogether();
                returnedList.add(new BreakElement(new LeafPosition(this, 0), keep.getPenalty(), keep
                        .getContext(), context));
                bodyList.remove(0);
                bodyList.remove(0);
            }
        }

        // create a combined list
        returnedList.addAll(getCombinedKnuthElementsForListItem(labelList, bodyList, context));

        // "wrap" the Position inside each element
        wrapPositionElements(returnedList, returnList, true);

        addLastVisibleMarks(returnList, context, alignment);

        addKnuthElementsForBreakAfter(returnList, context);

        context.updateKeepWithNextPending(this.keepWithNextPendingOnLabel);
        context.updateKeepWithNextPending(this.keepWithNextPendingOnBody);
        context.updateKeepWithNextPending(getKeepWithNext());
        context.updateKeepWithPreviousPending(getKeepWithPrevious());

        setFinished(true);
        resetSpaces();
        return returnList;
    }

    /**
     * Overridden to unconditionally add elements for space-before.
     * {@inheritDoc}
     */
    @Override
    protected void addFirstVisibleMarks(List<ListElement> elements,
                                        LayoutContext context, int alignment) {
        addKnuthElementsForSpaceBefore(elements, alignment);
        addKnuthElementsForBorderPaddingBefore(elements, !firstVisibleMarkServed);
        firstVisibleMarkServed = true;
        //Spaces, border and padding to be repeated at each break
        addPendingMarks(context);
    }

    private List getCombinedKnuthElementsForListItem(List<ListElement> labelElements,
            List<ListElement> bodyElements, LayoutContext context) {
        // Copy elements to array lists to improve element access performance
        List[] elementLists = {new ArrayList<ListElement>(labelElements),
                               new ArrayList<ListElement>(bodyElements)};
        int[] fullHeights = {ElementListUtils.calcContentLength(elementLists[0]),
                ElementListUtils.calcContentLength(elementLists[1])};
        int[] partialHeights = {0, 0};
        int[] start = {-1, -1};
        int[] end = {-1, -1};

        int totalHeight = Math.max(fullHeights[0], fullHeights[1]);
        int step;
        int addedBoxHeight = 0;
        Keep keepWithNextActive = Keep.KEEP_AUTO;

        LinkedList<ListElement> returnList = new LinkedList<ListElement>();
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
            int breakClass = EN_AUTO;
            KnuthElement endEl = elementLists[0].size() > 0 ? (KnuthElement) elementLists[0].get(end[0])
                    : null;
            Position originalLabelPosition =
                    (endEl != null && endEl.getPosition() != null) ? endEl.getPosition().getPosition() : null;
            if (endEl instanceof KnuthPenalty) {
                additionalPenaltyHeight = endEl.getWidth();
                stepPenalty = endEl.getPenalty() == -KnuthElement.INFINITE ? -KnuthElement.INFINITE : Math
                        .max(stepPenalty, endEl.getPenalty());
                breakClass = BreakUtil.compareBreakClasses(breakClass,
                        ((KnuthPenalty) endEl).getBreakClass());
            }
            endEl = elementLists[1].size() > 0 ? (KnuthElement) elementLists[1].get(end[1]) : null;
            Position originalBodyPosition =
                    (endEl != null && endEl.getPosition() != null) ? endEl.getPosition().getPosition() : null;
            if (endEl instanceof KnuthPenalty) {
                additionalPenaltyHeight = Math.max(
                        additionalPenaltyHeight, endEl.getWidth());
                stepPenalty = endEl.getPenalty() == -KnuthElement.INFINITE ? -KnuthElement.INFINITE : Math
                        .max(stepPenalty, endEl.getPenalty());
                breakClass = BreakUtil.compareBreakClasses(breakClass,
                        ((KnuthPenalty) endEl).getBreakClass());
            }

            int boxHeight = step - addedBoxHeight - penaltyHeight;
            penaltyHeight += additionalPenaltyHeight; //Add AFTER calculating boxHeight!

            // collect footnote information
            // TODO this should really not be done like this. ListItemLM should remain as
            // footnote-agnostic as possible
            LinkedList<FootnoteBodyLayoutManager> footnoteList = new LinkedList<>();
            for (int i = 0; i < elementLists.length; i++) {
                footnoteList.addAll(FootenoteUtil.getFootnotes(elementLists[i], start[i], end[i]));
            }

            LinkedList<FloatContentLayoutManager> floats = new LinkedList<FloatContentLayoutManager>();
            for (int i = 0; i < elementLists.length; i++) {
                floats.addAll(FloatContentLayoutManager.checkForFloats(elementLists[i], start[i], end[i]));
            }

            // add the new elements
            addedBoxHeight += boxHeight;
            ListItemPosition stepPosition = new ListItemPosition(this, start[0], end[0], start[1], end[1]);
            stepPosition.setOriginalLabelPosition(originalLabelPosition);
            if (originalBodyPosition == null || originalBodyPosition.getLM() instanceof ListItemContentLayoutManager) {
                // Happens when ListItem has multiple blocks and a block (that's not the last block) ends at the same
                // page height as a IPD change (e.g. FOP-3098). originalBodyPosition (reset) position needs to be a
                // Block so that BlockStackingLayoutManager can stack it. Lookahead to find next Block.
                Position block = extractBlock(elementLists[1], end[1] + 1);
                if (block != null) {
                    originalBodyPosition = block;
                }
            }
            stepPosition.setOriginalBodyPosition(originalBodyPosition);

            if (floats.isEmpty()) {
                returnList.add(new KnuthBlockBox(boxHeight, footnoteList, stepPosition, false));
            } else {
                // add a line with height zero and no content and attach float to it
                returnList.add(new KnuthBlockBox(0, Collections.emptyList(), stepPosition, false, floats));
                // add a break element to signal that we should restart LB at this break
                Keep keep = getKeepTogether();
                returnList.add(new BreakElement(stepPosition, keep.getPenalty(), keep.getContext(), context));
                // add the original line where the float was but without the float now
                returnList.add(new KnuthBlockBox(boxHeight, footnoteList, stepPosition, false));
            }
            if (originalBodyPosition != null && getKeepWithPrevious().isAuto()
                    && shouldWeAvoidBreak(returnList, originalBodyPosition.getLM())) {
                stepPenalty++;
            }
            if (addedBoxHeight < totalHeight) {
                Keep keep = keepWithNextActive.compare(getKeepTogether());
                int p = stepPenalty;
                if (p > -KnuthElement.INFINITE) {
                    p = Math.max(p, keep.getPenalty());
                    breakClass = keep.getContext();
                }
                returnList.add(new BreakElement(stepPosition, penaltyHeight, p, breakClass, context));
            }
        }

        return returnList;
    }

    /**
     * Extracts a block Position from a ListElement at a given index in a list of ListItem body elements.
     * @param bodyElements The ListItem body elements.
     * @param index        The index of the ListElement containing the block.
     * @return             The required block Position as a LeafPosition or null on failure.
     */
    private Position extractBlock(List<ListElement> bodyElements, int index) {
        ListElement listElement;
        Position position = null;
        Position retval = null;
        do {
            if (bodyElements == null || index >= bodyElements.size()) {
                break;
            }
            listElement = bodyElements.get(index);
            if (listElement != null
                    && listElement.getLayoutManager() instanceof ListItemContentLayoutManager) {
                position = listElement.getPosition();
                if (position != null
                        && position.getLM() instanceof ListItemContentLayoutManager) {
                    position = position.getPosition();
                    if (position != null
                            && position.getPosition() != null
                            && position.getLM() instanceof BlockLayoutManager) {
                        retval = new LeafPosition(position.getPosition().getLM(), 0);
                    }
                }
            }
        } while (false);
        return retval;
    }

    private boolean shouldWeAvoidBreak(List<ListElement> returnList, LayoutManager lm) {
        if (isChangingIPD(lm)) {
            if (lm instanceof BlockLayoutManager) {
                return true;
            }
            if (lm instanceof ListBlockLayoutManager) {
                int penaltyShootout = 0;
                for (Object o : returnList) {
                    if (o instanceof BreakElement) {
                        if (((BreakElement) o).getPenaltyValue() > 0) {
                            penaltyShootout++;
                        } else {
                            penaltyShootout--;
                        }
                    }
                }
                return penaltyShootout > 0;
            }
        }
        return false;
    }

    private boolean isChangingIPD(LayoutManager lm) {
        PageProvider pageProvider = lm.getPSLM().getPageProvider();
        int currentIPD = pageProvider.getCurrentIPD();
        if (currentIPD == -1) {
            return false;
        }
        int nextIPD = pageProvider.getNextIPD();
        return nextIPD != currentIPD;
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
                    if (el.getPenalty() < KnuthElement.INFINITE) {
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
                    partialHeights[i] += el.getWidth();
                } else {
                    partialHeights[i] += el.getWidth();
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

    /** {@inheritDoc} */
    @Override
    public List<ListElement> getChangedKnuthElements(List oldList, int alignment) {
        // label
        labelList = label.getChangedKnuthElements(labelList, alignment);

        // body
        // "unwrap" the Positions stored in the elements
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement oldElement;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement)oldListIterator.next();
            Position innerPosition = oldElement.getPosition().getPosition();
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

        List<ListElement> returnedList = body.getChangedKnuthElements(oldList, alignment);
        // "wrap" the Position inside each element
        List<ListElement> tempList = returnedList;
        KnuthElement tempElement;
        returnedList = new LinkedList<>();
        for (Object aTempList : tempList) {
            tempElement = (KnuthElement) aTempList;
            tempElement.setPosition(new NonLeafPosition(this, tempElement.getPosition()));
            returnedList.add(tempElement);
        }

        return returnedList;
    }


    @Override
    public boolean hasLineAreaDescendant() {
        return label.hasLineAreaDescendant() || body.hasLineAreaDescendant();
    }

    @Override
    public int getBaselineOffset() {
        if (label.hasLineAreaDescendant()) {
            return label.getBaselineOffset();
        } else if (body.hasLineAreaDescendant()) {
            return body.getBaselineOffset();
        } else {
            throw newNoLineAreaDescendantException();
        }
    }

    /**
     * Add the areas for the break points.
     *
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    @Override
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);

        addId();

        LayoutContext lc = LayoutContext.offspringOf(layoutContext);
        Position firstPos = null;
        Position lastPos = null;

        // "unwrap" the NonLeafPositions stored in parentIter
        LinkedList<Position> positionList = new LinkedList<Position>();
        Position pos;
        while (parentIter.hasNext()) {
            pos = parentIter.next();
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
        if (positionList.isEmpty()) {
            reset();
            return;
        }

        registerMarkers(true, isFirst(firstPos), isLast(lastPos));

        // use the first and the last ListItemPosition to determine the
        // corresponding indexes in the original labelList and bodyList
        int labelFirstIndex = ((ListItemPosition) positionList.getFirst()).getLabelFirstIndex();
        int labelLastIndex = ((ListItemPosition) positionList.getLast()).getLabelLastIndex();
        int bodyFirstIndex = ((ListItemPosition) positionList.getFirst()).getBodyFirstIndex();
        int bodyLastIndex = ((ListItemPosition) positionList.getLast()).getBodyLastIndex();

        //Determine previous break if any (in item label list)
        int previousBreak = ElementListUtils.determinePreviousBreak(labelList, labelFirstIndex);
        SpaceResolver.performConditionalsNotification(labelList,
                labelFirstIndex, labelLastIndex, previousBreak);

        //Determine previous break if any (in item body list)
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

        registerMarkers(false, isFirst(firstPos), isLast(lastPos));

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
    @Override
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();
            curBlockArea.setChangeBarList(getChangeBarList());

            // Set up dimensions
            /*Area parentArea =*/ parentLayoutManager.getParentArea(curBlockArea);

            // set traits
            ListItem fo = getListItemFO();
            TraitSetter.setProducerID(curBlockArea, fo.getId());
            TraitSetter.addBorders(curBlockArea, fo.getCommonBorderPaddingBackground(),
                    discardBorderBefore, discardBorderAfter, false, false, this);
            TraitSetter.addPadding(curBlockArea, fo.getCommonBorderPaddingBackground(),
                    discardPaddingBefore, discardPaddingAfter, false, false, this);
            TraitSetter.addMargins(curBlockArea, fo.getCommonBorderPaddingBackground(),
                    fo.getCommonMarginBlock(), this);
            TraitSetter.addBreaks(curBlockArea, fo.getBreakBefore(), fo.getBreakAfter());

            int contentIPD = referenceIPD - getIPIndents();
            curBlockArea.setIPD(contentIPD);

            curBlockArea.setBidiLevel(fo.getBidiLevel());

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
    @Override
    public void addChildArea(Area childArea) {
        if (curBlockArea != null) {
            curBlockArea.addBlock((Block) childArea);
        }
    }

    /** {@inheritDoc} */
    @Override
    public KeepProperty getKeepTogetherProperty() {
        return getListItemFO().getKeepTogether();
    }

    /** {@inheritDoc} */
    @Override
    public KeepProperty getKeepWithPreviousProperty() {
        return getListItemFO().getKeepWithPrevious();
    }

    /** {@inheritDoc} */
    @Override
    public KeepProperty getKeepWithNextProperty() {
        return getListItemFO().getKeepWithNext();
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        super.reset();
        label.reset();
        body.reset();
    }

    @Override
    public int getBreakBefore() {
        int breakBefore = BreakOpportunityHelper.getBreakBefore(this);
        breakBefore = BreakUtil.compareBreakClasses(breakBefore, label.getBreakBefore());
        breakBefore = BreakUtil.compareBreakClasses(breakBefore, body.getBreakBefore());
        return breakBefore;
    }

    /** {@inheritDoc} */
    public boolean isRestartable() {
        return true;
    }
}

