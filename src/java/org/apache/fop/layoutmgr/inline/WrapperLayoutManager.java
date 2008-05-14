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

import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.layoutmgr.BlockLayoutManager;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;

/**
 * This is the layout manager for the fo:wrapper formatting object.
 */
public class WrapperLayoutManager extends LeafNodeLayoutManager {
    
    private Wrapper fobj;

    /**
     * Creates a new LM for fo:wrapper.
     * @param node the fo:wrapper
     */
    public WrapperLayoutManager(Wrapper node) {
        super(node);
        fobj = node;
    }

    /** {@inheritDoc} */
    public InlineArea get(LayoutContext context) {
        // Create a zero-width, zero-height dummy area so this node can
        // participate in the ID handling. Otherwise, addId() wouldn't
        // be called. The area must also be added to the tree, because
        // determination of the X,Y position is done in the renderer.
        InlineArea area = new InlineArea();
        if (fobj.hasId()) {
            TraitSetter.setProducerID(area, fobj.getId());
        }
        return area;
    }

    /**
     * Add the area for this layout manager.
     * This adds the dummy area to the parent, *if* it has an id
     * - otherwise it serves no purpose.
     *
     * @param posIter the position iterator
     * @param context the layout context for adding the area
     */
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        if (fobj.hasId()) {
            addId();
            InlineArea area = getEffectiveArea();
            if (parentLM instanceof BlockStackingLayoutManager
                    && !(parentLM instanceof BlockLayoutManager)) {
                Block helperBlock = new Block();
                TraitSetter.setProducerID(helperBlock, fobj.getId());
                parentLM.addChildArea(helperBlock);
            } else {
                parentLM.addChildArea(area);
            }
        }
        while (posIter.hasNext()) {
            posIter.next();
        }
    }

    /** {@inheritDoc} */
    protected void addId() {
        getPSLM().addIDToPage(fobj.getId());
    }

}
