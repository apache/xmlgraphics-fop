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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.layoutmgr.Alternative.FittingStrategy;

/*
 * Utility class used in {@link MultiSwitchLayoutManager}
 * to handle  the <i>best-fit</i> property value if specified in {@link MultiSwitch}
 */
public final class BestFitLayoutUtils {

    private BestFitLayoutUtils() { }

    static class BestFitPosition extends Position {

        public List<ListElement> knuthList;

        public BestFitPosition(LayoutManager lm) {
            super(lm);
        }

        public List<Position> getPositionList() {
            List<Position> positions = new LinkedList<Position>();
            if (knuthList != null) {

                SpaceResolver.performConditionalsNotification(knuthList, 0, knuthList.size() - 1, -1);

                for (ListElement elem : knuthList) {
                    if (elem.getPosition() != null && elem.getLayoutManager() != null) {
                        positions.add(elem.getPosition());
                    }
                }
            }
            return positions;
        }

        public void setKnuthList(List<ListElement> knuthList) {
            this.knuthList = knuthList;
        }

        public List<ListElement> getKnuthList() {
            return knuthList;
        }

    }

    public static Alternative makeAlternative(List<ListElement> childList) {
        // Add a zero penalty to make the SpaceResolver
        // transform Space elements into Knuth glues.
        childList.add(KnuthPenalty.DUMMY_ZERO_PENALTY);
        SpaceResolver.resolveElementList(childList);
        int contentLength = ElementListUtils.calcContentLength(childList);
        return new Alternative(childList, contentLength);
    }

    public static List<ListElement> getKnuthList(LayoutManager lm,
            List<List<ListElement>> childrenLists,
            FittingStrategy strategy) {
        List<ListElement> knuthList = new LinkedList<ListElement>();
        Iterator<List<ListElement>> iter = childrenLists.iterator();
        BestFitPenalty bestFitPenalty = new BestFitPenalty(strategy, new BestFitPosition(lm));
        while (iter.hasNext()) {
            List<ListElement> childList = iter.next();
            bestFitPenalty.addAlternative(makeAlternative(childList));
        }
        // A penalty must always be preceded by a box
        // to be considered as a valid breakpoint.
        addKnuthPenalty(lm, knuthList, bestFitPenalty);
        return knuthList;
    }

    public static void addKnuthPenalty(LayoutManager lm, List<ListElement> list,
            KnuthPenalty bestFitPenalty) {

        list.add(0, new KnuthBox(0, new Position(lm), false));
        list.add(bestFitPenalty);
        list.add(new KnuthBox(0, new Position(lm), false));
    }

    public static List<Position> getPositionList(LayoutManager lm, PositionIterator posIter) {

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
        LinkedList<Position> positionList = new LinkedList<Position>();
        Position pos;
        while (posIter.hasNext()) {
            pos = posIter.next();
            if (pos instanceof BestFitPosition) {
                positionList.addAll(((BestFitPosition) pos).getPositionList());
            } else {
                positionList.add(pos);
            }
        }
        return positionList;
    }

}
