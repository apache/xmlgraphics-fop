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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FONode.FONodeIterator;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.MultiSwitch;

public class MultiSwitchLayoutManager extends BlockStackingLayoutManager {

    private Block curBlockArea;

    public MultiSwitchLayoutManager(FObj node) {
        super(node);
    }

    @Override
    public List<ListElement> getNextKnuthElements(LayoutContext context, int alignment) {

        referenceIPD = context.getRefIPD();
        List<List<ListElement>> childrenLists = new LinkedList<List<ListElement>>();
        LayoutManager childLM;
        while ((childLM = getChildLM()) != null) {
            if (!childLM.isFinished()) {
                LayoutContext childLC = makeChildLayoutContext(context);
                List childElements = childLM.getNextKnuthElements(childLC, alignment);
                if (childElements != null) {
                    List<ListElement> newList = new LinkedList<ListElement>();
                    wrapPositionElements(childElements, newList);
                    childrenLists.add(newList);
                }
            }
        }
        setFinished(true);
        return BestFitLayoutUtils.getKnuthList(this, childrenLists);
    }

    @Override
    protected List<LayoutManager> createChildLMs(int size) {
        MultiSwitch multiSwitch = (MultiSwitch) getFObj();
        if (multiSwitch.getCurrentlyVisibleNode() != null) {
            List<LayoutManager> newLMs = new ArrayList<LayoutManager>(size);
            if (childLMs.size() == 0) {
                createMultiCaseLM(multiSwitch.getCurrentlyVisibleNode());
                return new ArrayList<LayoutManager>(size);
            }
            return newLMs;
        } else {
            return super.createChildLMs(size);
        }
    }

    private void createMultiCaseLM(FONode multiCase) {
        FONodeIterator childIter = multiCase.getChildNodes();
        while (childIter.hasNext()) {
            List<LayoutManager> newLMs = new ArrayList<LayoutManager>(1);
            getPSLM().getLayoutManagerMaker()
            .makeLayoutManagers((childIter.nextNode()), newLMs);
            if (!newLMs.isEmpty()) {
                this.getParent().addChildLM(newLMs.get(0));
            }
        }
    }

    @Override
    public Keep getKeepTogether() {
        return Keep.KEEP_AUTO;
    }

    @Override
    public Keep getKeepWithNext() {
        return Keep.KEEP_AUTO;
    }

    @Override
    public Keep getKeepWithPrevious() {
        return Keep.KEEP_AUTO;
    }

    @Override
    public int getContentAreaIPD() {
        if (curBlockArea != null) {
            return curBlockArea.getIPD();
        }
        return super.getContentAreaIPD();
    }

    @Override
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();
            curBlockArea.setIPD(super.getContentAreaIPD());
            setCurrentArea(curBlockArea);
            // Set up dimensions
            // Must get dimensions from parent area
            /*Area parentArea = */parentLayoutManager.getParentArea(curBlockArea);
        }
        return curBlockArea;
    }

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
    @Override
    protected void flush() {
        if (getCurrentArea() != null) {
            parentLayoutManager.addChildArea(getCurrentArea());
        }
    }

    @Override
    public void addAreas(PositionIterator posIter, LayoutContext context) {

        List<Position> positionList = BestFitLayoutUtils.getPositionList(this, posIter);
        PositionIterator newPosIter = new PositionIterator(
                positionList.listIterator());

        AreaAdditionUtil.addAreas(this, newPosIter, context);
        flush();
    }

}
