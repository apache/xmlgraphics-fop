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

import org.apache.fop.layoutmgr.SpaceResolver.SpaceHandlingBreakPosition;

/**
 * Utility class which provides common code for the addAreas stage.
 */
public final class AreaAdditionUtil {

    private AreaAdditionUtil() {
    }

    /**
     * Creates the child areas for the given layout manager.
     * @param bslm the BlockStackingLayoutManager instance for which "addAreas" is performed.
     * @param parentIter the position iterator
     * @param layoutContext the layout context
     */
    public static void addAreas(BlockStackingLayoutManager bslm,
            PositionIterator parentIter, LayoutContext layoutContext) {
        LayoutManager childLM;
        LayoutContext lc = LayoutContext.offspringOf(layoutContext);
        LayoutManager firstLM = null;
        LayoutManager lastLM = null;
        Position firstPos = null;
        Position lastPos = null;

        if (bslm != null) {
            bslm.addId();
        }

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
        LinkedList<Position> positionList = new LinkedList<Position>();
        Position pos;
        while (parentIter.hasNext()) {
            pos = parentIter.next();
            if (pos == null) {
                continue;
            }
            if (pos.getIndex() >= 0) {
                if (firstPos == null) {
                    firstPos = pos;
                }
                lastPos = pos;
            }
            if (pos instanceof NonLeafPosition) {
                // pos was created by a child of this FlowLM
                positionList.add(pos.getPosition());
                lastLM = (pos.getPosition().getLM());
                if (firstLM == null) {
                    firstLM = lastLM;
                }
            } else if (pos instanceof SpaceHandlingBreakPosition) {
                positionList.add(pos);
            } else {
                // pos was created by this LM, so it must be ignored
            }
        }
        if (firstPos == null) {
            return; //Nothing to do, return early
            //TODO This is a hack to avoid an NPE in the code block below.
            //If there's no firstPos/lastPos there's currently no way to
            //correctly determine first and last conditions. The Iterator
            //doesn't give us that info.
        }

        if (bslm != null) {
            bslm.addMarkersToPage(
                    true,
                    bslm.isFirst(firstPos),
                    bslm.isLast(lastPos));
        }

        PositionIterator childPosIter = new PositionIterator(positionList.listIterator());

        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // TODO vh: the test above might be problematic in some cases. See comment in
            // the TableCellLM.getNextKnuthElements method
            // Add the block areas to Area
            lc.setFlags(LayoutContext.FIRST_AREA, childLM == firstLM);
            lc.setFlags(LayoutContext.LAST_AREA, childLM == lastLM);
            // set the space adjustment ratio
            lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
            // set space before for the first LM, in order to implement
            // display-align = center or after
            lc.setSpaceBefore((childLM == firstLM ? layoutContext.getSpaceBefore() : 0));
            // set space after for each LM, in order to implement
            // display-align = distribute
            lc.setSpaceAfter(layoutContext.getSpaceAfter());
            lc.setStackLimitBP(layoutContext.getStackLimitBP());
            childLM.addAreas(childPosIter, lc);
        }

        if (bslm != null) {
            bslm.addMarkersToPage(
                    false,
                    bslm.isFirst(firstPos),
                    bslm.isLast(lastPos));
            bslm.checkEndOfLayout(lastPos);
        }


    }

}
