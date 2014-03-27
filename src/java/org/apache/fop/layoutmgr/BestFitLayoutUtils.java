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

package org.apache.fop.layoutmgr;

import java.util.LinkedList;
import java.util.List;

import org.apache.fop.layoutmgr.BestFitPenalty.Variant;

/**
 * Utility class used in {@link MultiSwitchLayoutManager}
 * to handle  the <i>best-fit</i> property value if specified in {@link org.apache.fop.fo.flow.MultiSwitch}
 */
public final class BestFitLayoutUtils {

    private BestFitLayoutUtils() { }

    static class BestFitPosition extends Position {

        private List<ListElement> knuthList;

        public BestFitPosition(LayoutManager lm) {
            super(lm);
        }

        public List<Position> getPositionList() {
            List<Position> positions = new LinkedList<Position>();
            if (knuthList != null && !knuthList.isEmpty()) {
                SpaceResolver.performConditionalsNotification(knuthList, 0, knuthList.size() - 1, -1);
                for (ListElement el : knuthList) {
                    if (el.getPosition() != null) {
                        positions.add(el.getPosition());
                    }
                }
            }
            return positions;
        }

        public void setKnuthList(List<ListElement> knuthList) {
            this.knuthList = knuthList;
        }

    }

    public static List<ListElement> getKnuthList(LayoutManager lm,
            List<List<ListElement>> childrenLists) {

        List<ListElement> knuthList = new LinkedList<ListElement>();

        BestFitPenalty bestFitPenalty = new BestFitPenalty(new BestFitPosition(lm));
        for (List<ListElement> childList : childrenLists) {
            // TODO Doing space resolution here is not correct.
            // Space elements will simply be nulled.
            SpaceResolver.resolveElementList(childList);
            int contentLength = ElementListUtils.calcContentLength(childList);
            bestFitPenalty.addVariant(new Variant(childList, contentLength));
        }
        // TODO Adding two enclosing boxes is definitely a dirty hack.
        // The first box forces the breaking algorithm to consider the penalty
        // in case there are no elements preceding it
        // and the last box prevents the glue and penalty from getting deleted
        // when they are at the end of the Knuth list.
        knuthList.add(new KnuthBox(0, new Position(lm), false));
        knuthList.add(bestFitPenalty);
        knuthList.add(new KnuthBox(0, new Position(lm), false));
        return knuthList;
    }

    public static List<Position> getPositionList(LayoutManager lm, PositionIterator posIter) {

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
        LinkedList<Position> positionList = new LinkedList<Position>();
        while (posIter.hasNext()) {
            Position pos = posIter.next();
            if (pos instanceof BestFitPosition) {
                positionList.addAll(((BestFitPosition) pos).getPositionList());
            } else {
                positionList.add(pos);
            }
        }
        return positionList;
    }

}
