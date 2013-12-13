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

        public BestFitPosition(LayoutManager lm, List<ListElement> knuthList) {
            super(lm);
            this.knuthList = knuthList;
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
    }

    public static List<ListElement> getKnuthList(LayoutManager lm,
            List<List<ListElement>> childrenLists) {

        List<ListElement> knuthList = new LinkedList<ListElement>();

        BestFitPenalty bestFitPenalty = new BestFitPenalty(new BestFitPosition(lm));
        for (List<ListElement> childList : childrenLists) {
            SpaceResolver.resolveElementList(childList);
            int contentLength = ElementListUtils.calcContentLength(childList);
            bestFitPenalty.addVariant(childList, contentLength);
        }
        // TODO Adding the two enclosing boxes is definitely a dirty hack.
        // Let's leave it like that for now, until I find a proper fix.
        knuthList.add(new KnuthBox(0, new Position(lm), false));
        knuthList.add(bestFitPenalty);
        knuthList.add(new KnuthBox(0, new Position(lm), false));
        return knuthList;
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
