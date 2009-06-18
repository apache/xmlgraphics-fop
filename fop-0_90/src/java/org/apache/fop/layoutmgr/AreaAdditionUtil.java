/*
 * Copyright 2005 The Apache Software Foundation.
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

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.fop.layoutmgr.SpaceResolver.SpaceHandlingBreakPosition;

/**
 * Utility class which provides common code for the addAreas stage.
 */
public class AreaAdditionUtil {

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
     * Creates the child areas for the given layout manager.
     * @param bslm the BlockStackingLayoutManager instance for which "addAreas" is performed.
     * @param parentIter the position iterator
     * @param layoutContext the layout context
     */
    public static void addAreas(BlockStackingLayoutManager bslm, 
            PositionIterator parentIter, LayoutContext layoutContext) {
        LayoutManager childLM = null;
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
                positionList.add(((NonLeafPosition) pos).getPosition());
                lastLM = ((NonLeafPosition) pos).getPosition().getLM();
                if (firstLM == null) {
                    firstLM = lastLM;
                }
            } else if (pos instanceof SpaceHandlingBreakPosition) {
                positionList.add(pos);
            } else {
                // pos was created by this LM, so it must be ignored
            }
        }
        
        if (bslm != null && bslm.markers != null) {
            bslm.getCurrentPV().addMarkers(bslm.markers, true, 
                    bslm.isFirst(firstPos), bslm.isLast(lastPos));
        }

        StackingIter childPosIter = new StackingIter(positionList.listIterator());
        while ((childLM = childPosIter.getNextChildLM()) != null) {
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
            lc.setStackLimit(layoutContext.getStackLimit());
            childLM.addAreas(childPosIter, lc);
        }
        if (bslm != null && bslm.markers != null) {
            bslm.getCurrentPV().addMarkers(bslm.markers, false, 
                    bslm.isFirst(firstPos), bslm.isLast(lastPos));
        }
        
    }
    
}
