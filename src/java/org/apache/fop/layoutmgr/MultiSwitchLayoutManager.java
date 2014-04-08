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

import org.apache.fop.area.Area;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.MultiSwitch;

public class MultiSwitchLayoutManager extends BlockStackingLayoutManager {

    static class WhitespaceManagementPosition extends Position {

        private List<ListElement> knuthList;

        public WhitespaceManagementPosition(LayoutManager lm) {
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

        public List<ListElement> getKnuthList() {
            return knuthList;
        }

    }

    private interface KnuthElementsGenerator {
        List<ListElement> getKnuthElement(LayoutContext context, int alignment);
    }

    private class DefaultKnuthListGenerator implements KnuthElementsGenerator {

        public List<ListElement> getKnuthElement(LayoutContext context, int alignment) {

            List<ListElement> knuthList = new LinkedList<ListElement>();
            LayoutManager childLM;
            while ((childLM = getChildLM()) != null) {
                if (!childLM.isFinished()) {
                    LayoutContext childLC = makeChildLayoutContext(context);
                    List childElements = childLM.getNextKnuthElements(childLC, alignment);
                    if (childElements != null) {
                        List<ListElement> newList = new LinkedList<ListElement>();
                        wrapPositionElements(childElements, newList);
                        knuthList.addAll(newList);
                    }
                }
            }
            return knuthList;
        }

    }

    private class WhitespaceManagement implements KnuthElementsGenerator {

        public List<ListElement> getKnuthElement(LayoutContext context, int alignment) {

            MultiSwitchLayoutManager mslm = MultiSwitchLayoutManager.this;
            List<ListElement> knuthList = new LinkedList<ListElement>();
            WhitespaceManagementPenalty penalty = new WhitespaceManagementPenalty(
                    new WhitespaceManagementPosition(mslm));
            LayoutManager childLM;
            while ((childLM = getChildLM()) != null) {
                if (!childLM.isFinished()) {
                    LayoutContext childLC = makeChildLayoutContext(context);
                    List childElements = childLM.getNextKnuthElements(childLC, alignment);
                    if (childElements != null) {
                        List<ListElement> newList = new LinkedList<ListElement>();
                        wrapPositionElements(childElements, newList);
                        // TODO Doing space resolution here is wrong.
                        SpaceResolver.resolveElementList(newList);
                        int contentLength = ElementListUtils.calcContentLength(newList);
                        penalty.addVariant(penalty.new Variant(newList, contentLength));
                    }
                }
            }
            // Prevent the penalty from being ignored if it is at the beginning of the content
            knuthList.add(new KnuthBox(0, new Position(mslm), false));
            knuthList.add(penalty);
            // Prevent the penalty from being ignored if it is at the end of the content
            knuthList.add(new KnuthBox(0, new Position(mslm), false));
            return knuthList;
        }

    }

    private KnuthElementsGenerator knuthGen;

    public MultiSwitchLayoutManager(FObj node) {
        super(node);
        MultiSwitch multiSwitchNode = (MultiSwitch) node;
        if (multiSwitchNode.getAutoToggle().equals("best-fit")) {
            knuthGen = new WhitespaceManagement();
        } else {
            knuthGen = new DefaultKnuthListGenerator();
        }
    }

    @Override
    public List<ListElement> getNextKnuthElements(LayoutContext context, int alignment) {
        referenceIPD = context.getRefIPD();
        List<ListElement> knuthList = knuthGen.getKnuthElement(context, alignment);
        setFinished(true);
        return knuthList;
    }

    @Override
    public Area getParentArea(Area childArea) {
        return parentLayoutManager.getParentArea(childArea);
    }

    @Override
    public void addChildArea(Area childArea) {
        parentLayoutManager.addChildArea(childArea);
    }

    @Override
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        LinkedList<Position> positionList = new LinkedList<Position>();
        while (posIter.hasNext()) {
            Position pos = posIter.next();
            if (pos instanceof WhitespaceManagementPosition) {
                positionList.addAll(((WhitespaceManagementPosition) pos).getPositionList());
            } else {
                positionList.add(pos);
            }
        }
        PositionIterator newPosIter = new PositionIterator(positionList.listIterator());
        AreaAdditionUtil.addAreas(this, newPosIter, context);
        flush();
    }

}
