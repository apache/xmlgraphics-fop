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

import org.apache.fop.area.Area;
import org.apache.fop.fo.flow.FootnoteBody;

/**
 * Layout manager for footnote bodies.
 */
public class FootnoteBodyLayoutManager extends BlockStackingLayoutManager {

    /**
     * Creates a new FootnoteBodyLayoutManager.
     * @param body the footnote-body element
     */
    public FootnoteBodyLayoutManager(FootnoteBody body) {
        super(body);
    }

    /** {@inheritDoc} */
    @Override
    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        LayoutManager childLM;
        LayoutManager lastLM = null;
        LayoutContext lc = new LayoutContext(0);

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
        LinkedList<Position> positionList = new LinkedList<Position>();
        Position pos;
        while (parentIter.hasNext()) {
            pos = parentIter.next();
            Position innerPosition;
            if (pos instanceof NonLeafPosition) {
                innerPosition = pos.getPosition();
                if (innerPosition.getLM() == this) {
                    // pos was created by this LM and was inside a penalty
                    // allowing or forbidding a page break
                    // nothing to do
                } else {
                    // innerPosition was created by another LM
                    positionList.add(innerPosition);
                    lastLM = innerPosition.getLM();
                }
            }
        }

        // the Positions in positionList were inside the elements
        // created by the LineLM
        PositionIterator childPosIter = new PositionIterator(positionList.listIterator());

        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // set last area flag
            lc.setFlags(LayoutContext.LAST_AREA,
                    (layoutContext.isLastArea() && childLM == lastLM));
            // Add the line areas to Area
            childLM.addAreas(childPosIter, lc);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addChildArea(Area childArea) {
        childArea.setAreaClass(Area.CLASS_FOOTNOTE);
        parentLayoutManager.addChildArea(childArea);
    }

    /** @return the FootnoteBody node */
    protected FootnoteBody getFootnodeBodyFO() {
        return (FootnoteBody) fobj;
    }

    /** {@inheritDoc} */
    @Override
    public Keep getKeepTogether() {
        return getParentKeepTogether();
    }

    /** {@inheritDoc} */
    @Override
    public Keep getKeepWithNext() {
        return Keep.KEEP_AUTO;
    }

    /** {@inheritDoc} */
    @Override
    public Keep getKeepWithPrevious() {
        return Keep.KEEP_AUTO;
    }

}
