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

package org.apache.fop.layoutmgr.inline;

import java.util.LinkedList;
import java.util.List;

import org.apache.fop.fo.flow.Float;
import org.apache.fop.layoutmgr.FloatContentLayoutManager;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceResolver;

public class FloatLayoutManager extends InlineStackingLayoutManager {

    private FloatContentLayoutManager floatContentLM;
    private KnuthInlineBox anchor;
    private List<KnuthElement> floatContentKnuthElements;
    private Float floatContent;
    private boolean floatContentAreaAdded;

    public FloatLayoutManager(Float node) {
        super(node);
        floatContent = node;
    }

    protected LayoutManager getChildLM() {
        return null;
    }

    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {

        if (!floatContentAreaAdded && !floatContent.isDisabled()) {
            floatContentLM = new FloatContentLayoutManager(floatContent);
            floatContentLM.setParent(this);
            floatContentLM.initialize();
            floatContentKnuthElements = floatContentLM.getNextKnuthElements(context, alignment);
            SpaceResolver.resolveElementList(floatContentKnuthElements);
        }

        // the only knuth element is a zero width and height knuth box
        LinkedList knuthElements = new LinkedList();
        KnuthSequence seq = new InlineKnuthSequence();
        anchor = new KnuthInlineBox(0, null, null, true);
        if (!floatContentAreaAdded) {
            anchor.setFloatContentLM(floatContentLM);
        }
        anchor.setPosition(notifyPos(new Position(this)));
        seq.add(anchor);
        knuthElements.add(seq);
        setFinished(true);

        return knuthElements;
    }

    public void addAreas(PositionIterator posIter, LayoutContext context) {
        // "Unwrap" the NonLeafPositions stored in posIter
        LinkedList positionList = new LinkedList();
        Position pos = null;
        while (posIter.hasNext()) {
            pos = posIter.next();
            if (pos != null && pos.getPosition() != null) {
                positionList.add(pos.getPosition());
            }
        }
    }

    public void processAreas(LayoutContext context) {
        PositionIterator contentPosIter = new KnuthPossPosIter(floatContentKnuthElements, 0,
                floatContentKnuthElements.size());
        floatContentLM.addAreas(contentPosIter, context);
        floatContentAreaAdded = true;
        anchor.setFloatContentLM(null);
    }
}
