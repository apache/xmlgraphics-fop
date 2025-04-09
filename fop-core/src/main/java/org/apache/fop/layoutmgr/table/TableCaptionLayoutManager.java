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

package org.apache.fop.layoutmgr.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.table.TableCaption;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.BreakOpportunity;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LMiter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for a table-caption FO.
 * A table caption consists of a table caption.
 * The caption contains blocks that are positioned next to the
 * table on the caption side.
 * The caption blocks have an implicit keep with the table.
 * TODO Implement getNextKnuthElements()
 */
public class TableCaptionLayoutManager extends BlockStackingLayoutManager implements BreakOpportunity {
    private Block curBlockArea;

    /** Iterator over the child layout managers. */
    private ProxyLMiter proxyLMiter;

    /**
     * Create a new table caption layout manager.
     * @param tableCaption the block FO object to create the layout manager for.
     */
    public TableCaptionLayoutManager(TableCaption tableCaption) {
        super(tableCaption);
        proxyLMiter = new ProxyLMiter();
    }

    public List<ListElement> getNextKnuthElements(LayoutContext context, int alignment) {
        return getNextKnuthElements(context, alignment, null, null, null);
    }

    /**
     * Proxy iterator for Block LM.
     * This iterator creates and holds the complete list
     * of child LMs.
     * It uses fobjIter as its base iterator.
     * Block LM's createNextChildLMs uses this iterator
     * as its base iterator.
     */
    protected class ProxyLMiter extends LMiter {

        /**
         * Constructs a proxy iterator for Block LM.
         */
        public ProxyLMiter() {
            super(TableCaptionLayoutManager.this);
            listLMs = new ArrayList<>(10);
        }

        /**
         * @return true if there are more child lms
         */
        public boolean hasNext() {
            return (curPos < listLMs.size()) || createNextChildLMs(curPos);
        }

        /**
         * @param pos ...
         * @return true if new child lms were added
         */
        protected boolean createNextChildLMs(int pos) {
            List<LayoutManager> newLMs = createChildLMs(pos + 1 - listLMs.size());
            if (newLMs != null) {
                listLMs.addAll(newLMs);
            }
            return pos < listLMs.size();
        }
    }

    public boolean createNextChildLMs(int pos) {
        while (proxyLMiter.hasNext()) {
            LayoutManager lm = proxyLMiter.next();
            addChildLM(lm);
            if (pos < childLMs.size()) {
                return true;
            }
        }
        return false;
    }

    public KeepProperty getKeepTogetherProperty() {
        return getTableCaptionFO().getKeepTogether();
    }

    public KeepProperty getKeepWithPreviousProperty() {
        return getTableCaptionFO().getKeepWithPrevious();
    }

    public KeepProperty getKeepWithNextProperty() {
        return getTableCaptionFO().getKeepWithNext();
    }

    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        getParentArea(null);

        // if this will create the first block area in a page
        // and display-align is after or center, add space before
        if (layoutContext.getSpaceBefore() > 0) {
            addBlockSpacing(0.0, MinOptMax.getInstance(layoutContext.getSpaceBefore()));
        }

        LayoutManager childLM;
        LayoutManager lastLM = null;
        LayoutContext lc = LayoutContext.offspringOf(layoutContext);
        lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
        // set space after in the LayoutContext for children
        if (layoutContext.getSpaceAfter() > 0) {
            lc.setSpaceAfter(layoutContext.getSpaceAfter());
        }
        PositionIterator childPosIter;

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
        LinkedList<Position> positionList = new LinkedList<>();
        Position pos;
        boolean spaceBefore = false;
        boolean spaceAfter = false;
        Position firstPos = null;
        Position lastPos = null;
        while (parentIter.hasNext()) {
            pos = parentIter.next();
            if (pos.getIndex() >= 0) {
                if (firstPos == null) {
                    firstPos = pos;
                }
                lastPos = pos;
            }
            Position innerPosition = pos;
            if (pos instanceof NonLeafPosition) {
                //Not all elements are wrapped
                innerPosition = pos.getPosition();
            }
            if (innerPosition == null) {
                // pos was created by this BlockLM and was inside an element
                // representing space before or after
                // this means the space was not discarded
                if (positionList.isEmpty()) {
                    // pos was in the element representing space-before
                    spaceBefore = true;
                } else {
                    // pos was in the element representing space-after
                    spaceAfter = true;
                }
            } else if (innerPosition.getLM() == this
                    && !(innerPosition instanceof MappingPosition)) {
                // pos was created by this BlockLM and was inside a penalty
                // allowing or forbidding a page break
                // nothing to do
            } else {
                // innerPosition was created by another LM
                positionList.add(innerPosition);
                lastLM = innerPosition.getLM();
            }
        }

        addId();

        registerMarkers(true, isFirst(firstPos), isLast(lastPos));

        if (bpUnit == 0) {
            // the Positions in positionList were inside the elements
            // created by the LineLM
            childPosIter = new StackingIter(positionList.listIterator());
        } else {
            // the Positions in positionList were inside the elements
            // created by the BlockLM in the createUnitElements() method
            LinkedList<KnuthElement> splitList = new LinkedList<>();
            int splitLength = 0;
            int iFirst = ((MappingPosition) positionList.getFirst()).getFirstIndex();
            int iLast = ((MappingPosition) positionList.getLast()).getLastIndex();
            // copy from storedList to splitList all the elements from
            // iFirst to iLast
            ListIterator<KnuthElement> storedListIterator = storedList.listIterator(iFirst);
            while (storedListIterator.nextIndex() <= iLast) {
                KnuthElement element = storedListIterator.next();
                // some elements in storedList (i.e. penalty items) were created
                // by this BlockLM, and must be ignored
                if (element.getLayoutManager() != this) {
                    splitList.add(element);
                    splitLength += element.getWidth();
                    lastLM = element.getLayoutManager();
                }
            }
            // add space before and / or after the paragraph
            // to reach a multiple of bpUnit
            if (spaceBefore && spaceAfter) {
                adjustedSpaceBefore = (neededUnits(splitLength
                        + foSpaceBefore.getMin()
                        + foSpaceAfter.getMin())
                        * bpUnit - splitLength) / 2;
                adjustedSpaceAfter = neededUnits(splitLength
                        + foSpaceBefore.getMin()
                        + foSpaceAfter.getMin())
                        * bpUnit - splitLength - adjustedSpaceBefore;
            } else if (spaceBefore) {
                adjustedSpaceBefore = neededUnits(splitLength
                        + foSpaceBefore.getMin())
                        * bpUnit - splitLength;
            } else {
                adjustedSpaceAfter = neededUnits(splitLength
                        + foSpaceAfter.getMin())
                        * bpUnit - splitLength;
            }
            childPosIter = new KnuthPossPosIter(splitList, 0, splitList
                    .size());
        }

        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // set last area flag
            lc.setFlags(LayoutContext.LAST_AREA,
                    (layoutContext.isLastArea() && childLM == lastLM));
            lc.setStackLimitBP(layoutContext.getStackLimitBP());
            // Add the line areas to Area
            childLM.addAreas(childPosIter, lc);
        }

        registerMarkers(false, isFirst(firstPos), isLast(lastPos));
        flush();

        curBlockArea = null;

        //Notify end of block layout manager to the PSLM
        checkEndOfLayout(lastPos);
    }

    private static class StackingIter extends PositionIterator {
        public StackingIter(Iterator parentIter) {
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
     * Return an Area which can contain the passed childArea. The childArea
     * may not yet have any content, but it has essential traits set.
     * In general, if the LayoutManager already has an Area it simply returns
     * it. Otherwise, it makes a new Area of the appropriate class.
     * It gets a parent area for its area by calling its parent LM.
     * Finally, based on the dimensions of the parent area, it initializes
     * its own area. This includes setting the content IPD and the maximum
     * BPD.
     * @param childArea area to get the parent area for
     * @return the parent area
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();

            curBlockArea.setIPD(super.getContentAreaIPD());

            curBlockArea.setBidiLevel(getTableCaptionFO().getBidiLevelRecursive());

            // Must get dimensions from parent area
            //Don't optimize this line away. It can have ugly side-effects.
            parentLayoutManager.getParentArea(curBlockArea);

            // set traits
            TraitSetter.setProducerID(curBlockArea, getTableCaptionFO().getId());
            TraitSetter.setLayer(curBlockArea, getTableCaptionFO().getLayer());
            curBlockArea.setLocation(FONode.getLocatorString(getTableCaptionFO().getLocator()));
            setCurrentArea(curBlockArea); // ??? for generic operations
        }
        return curBlockArea;
    }

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
     * Returns the table-caption formatting object.
     * @return the table-caption formatting object
     */
    protected TableCaption getTableCaptionFO() {
        return (TableCaption) fobj;
    }

    // --------- Property Resolution related functions --------- //

    /**
     * Returns the IPD of the content area
     * @return the IPD of the content area
     */
    public int getContentAreaIPD() {
        if (curBlockArea != null) {
            return curBlockArea.getIPD();
        }
        return super.getContentAreaIPD();
    }

    /**
     * Returns the BPD of the content area
     * @return the BPD of the content area
     */
    public int getContentAreaBPD() {
        if (curBlockArea != null) {
            return curBlockArea.getBPD();
        }
        return -1;
    }

    public boolean getGeneratesBlockArea() {
        return true;
    }

    public boolean isRestartable() {
        return true;
    }
}
