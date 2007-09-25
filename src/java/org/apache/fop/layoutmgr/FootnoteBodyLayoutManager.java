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

import org.apache.fop.area.Area;
import org.apache.fop.fo.flow.FootnoteBody;

import java.util.LinkedList;

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
    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        LayoutManager childLM = null;
        LayoutManager lastLM = null;
        LayoutContext lc = new LayoutContext(0);

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
        LinkedList positionList = new LinkedList();
        Position pos;
        while (parentIter.hasNext()) {
            pos = (Position) parentIter.next();
            //log.trace("pos = " + pos.getClass().getName() + "; " + pos);
            Position innerPosition = pos;
            if (pos instanceof NonLeafPosition) {
                innerPosition = ((NonLeafPosition) pos).getPosition();
                if (innerPosition.getLM() == this) {
                    // pos was created by this LM and was inside a penalty
                    // allowing or forbidding a page break
                    // nothing to do
                    //log.trace(" penalty");
                } else {
                    // innerPosition was created by another LM
                    positionList.add(innerPosition);
                    lastLM = innerPosition.getLM();
                    //log.trace(" " + innerPosition.getClass().getName());
                }
            }
        }

        // the Positions in positionList were inside the elements
        // created by the LineLM
        StackingIter childPosIter = new StackingIter(positionList.listIterator());

        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // set last area flag
            lc.setFlags(LayoutContext.LAST_AREA,
                    (layoutContext.isLastArea() && childLM == lastLM));
            // Add the line areas to Area
            childLM.addAreas(childPosIter, lc);
        }
    }

    /** {@inheritDoc} */
    public void addChildArea(Area childArea) {
        childArea.setAreaClass(Area.CLASS_FOOTNOTE);
        parentLM.addChildArea(childArea);
    }

    /** @return the FootnoteBody node */
    protected FootnoteBody getFootnodeBodyFO() {
        return (FootnoteBody) fobj;
    }

}
