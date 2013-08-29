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

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.fo.extensions.AlternativeBlock;

/**
 * Layout manager for {@link AlternativeBlock}
 */

public class AlternativeBlockLayoutManager extends AbstractLayoutManager {

    private Block curBlockArea;

    public AlternativeBlockLayoutManager(AlternativeBlock node) {
        super(node);
        setGeneratesBlockArea(true);
    }

    /**
     * Creates and initializes a {@link LayoutContext} to pass to the child LM
     *
     * @param context the parent {@link LayoutContext}
     * @return a new child layout context
     */
    protected LayoutContext makeChildLayoutContext(LayoutContext context) {
        LayoutContext childLC = LayoutContext.newInstance();
        childLC.copyPendingMarksFrom(context);
        childLC.setStackLimitBP(context.getStackLimitBP());
        childLC.setRefIPD(context.getRefIPD());
        childLC.setWritingMode(context.getWritingMode());
        return childLC;
    }

    /** {@inheritDoc} */
    @Override
    public List getNextKnuthElements(LayoutContext context, int alignment) {
        return getNextKnuthElements(context, alignment, null, null, null);
    }

    /** {@inheritDoc} */
    public List getNextKnuthElements(LayoutContext context, int alignment,
            Stack lmStack, Position positionAtIPDChange,
            LayoutManager restartAtLM) {
        LayoutManager curLM;
        List<ListElement> returnList = new LinkedList<ListElement>();
        while ((curLM = getChildLM()) != null) {
            LayoutContext childLC = makeChildLayoutContext(context);

            List returnedList = null;
            if (!curLM.isFinished()) {
                returnedList = curLM.getNextKnuthElements(childLC, alignment);
            }
            if (returnedList != null) {
                wrapPositionElements(returnedList, returnList);
            }
        }
        setFinished(true);
        return returnList;
    }

    /**
     * "wrap" the Position inside each element moving the elements from SourceList to targetList
     *
     * @param sourceList source list
     * @param targetList target list receiving the wrapped position elements
     */
    protected void wrapPositionElements(List sourceList, List targetList) {
        wrapPositionElements(sourceList, targetList, false);
    }

    /**
     * "wrap" the Position inside each element moving the elements from
     * SourceList to targetList
     *
     * @param sourceList source list
     * @param targetList target list receiving the wrapped position elements
     * @param force if true, every Position is wrapped regardless of its LM of origin
     */
    protected void wrapPositionElements(List sourceList, List targetList,
            boolean force) {

        ListIterator listIter = sourceList.listIterator();
        Object tempElement;
        while (listIter.hasNext()) {
            tempElement = listIter.next();
            if (tempElement instanceof ListElement) {
                wrapPositionElement((ListElement) tempElement, targetList,
                        force);
            } else if (tempElement instanceof List) {
                wrapPositionElements((List) tempElement, targetList, force);
            }
        }
    }

    /**
     * "wrap" the Position inside the given element and add it to the target list.
     *
     * @param el the list element
     * @param targetList target list receiving the wrapped position elements
     * @param force if true, every Position is wrapped regardless of its LM of origin
     */
    protected void wrapPositionElement(ListElement el, List targetList,
            boolean force) {
        if (force || el.getLayoutManager() != this) {
            el.setPosition(notifyPos(new NonLeafPosition(this, el.getPosition())));
        }
        targetList.add(el);
    }

    @Override
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();
            // Set up dimensions
            // Must get dimensions from parent area
            /*Area parentArea = */parentLayoutManager.getParentArea(curBlockArea);
        }
        return curBlockArea;
    }

    /** {@inheritDoc} */
    @Override
    public void addChildArea(Area childArea) {
        if (curBlockArea != null) {
            if (childArea instanceof LineArea) {
                curBlockArea.addLineArea((LineArea) childArea);
            } else {
                curBlockArea.addBlock((Block) childArea);
            }
        }
    }

    /**
     * Force current area to be added to parent area.
     */
    protected void flush() {
        if (getCurrentArea() != null) {
            parentLayoutManager.addChildArea(getCurrentArea());
        }
    }

    private Area getCurrentArea() {
        return curBlockArea;
    }

    /** {@inheritDoc} */
    @Override
    public void addAreas(PositionIterator parentIter,
            LayoutContext layoutContext) {

        getParentArea(null);

        addId();

        LayoutManager childLM;
        LayoutContext lc = LayoutContext.offspringOf(layoutContext);
        LayoutManager firstLM = null;
        LayoutManager lastLM = null;
        Position firstPos = null;
        Position lastPos = null;

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
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
            if (pos instanceof NonLeafPosition && (pos.getPosition() != null)
                    && pos.getPosition().getLM() != this) {
                // pos was created by a child of this BestFitBlockLM
                positionList.add(pos.getPosition());
                lastLM = pos.getPosition().getLM();
                if (firstLM == null) {
                    firstLM = lastLM;
                }
            }
        }

        registerMarkers(true, isFirst(firstPos), isLast(lastPos));

        PositionIterator childPosIter = new PositionIterator(
                positionList.listIterator());
        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // Add the block areas to Area
            // set the space adjustment ratio
            lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
            lc.setFlags(LayoutContext.FIRST_AREA, childLM == firstLM);
            lc.setFlags(LayoutContext.LAST_AREA, childLM == lastLM);
            lc.setStackLimitBP(layoutContext.getStackLimitBP());
            childLM.addAreas(childPosIter, lc);
        }

        registerMarkers(false, isFirst(firstPos), isLast(lastPos));

        flush();

        curBlockArea = null;

        checkEndOfLayout(lastPos);
    }

    /** {@inheritDoc} */
    public boolean isRestartable() {
        return true;
    }

}
