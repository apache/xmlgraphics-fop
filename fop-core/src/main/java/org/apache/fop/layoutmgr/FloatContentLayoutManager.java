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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.fop.area.Area;
import org.apache.fop.area.SideFloat;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.Float;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.inline.FloatLayoutManager;
import org.apache.fop.layoutmgr.inline.KnuthInlineBox;

public class FloatContentLayoutManager extends SpacedBorderedPaddedBlockLayoutManager {

    private SideFloat floatContentArea;
    private int side;
    private int yOffset;

    /**
     * {@asf.todo - Add info}
     *
     * @param node the {@link Float} associated with this instance
     */
    public FloatContentLayoutManager(Float node) {
        super(node);
        generatesReferenceArea = true;
        side = node.getFloat();
    }

    @Override
    public Keep getKeepTogether() {
        return getParentKeepTogether();
    }

    @Override
    public Keep getKeepWithNext() {
        return Keep.KEEP_AUTO;
    }

    @Override
    public Keep getKeepWithPrevious() {
        return Keep.KEEP_ALWAYS;
    }

    @Override
    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        floatContentArea = new SideFloat();
        AreaAdditionUtil.addAreas(this, parentIter, layoutContext);
        flush();
    }

    @Override
    public void addChildArea(Area childArea) {
        floatContentArea.addChildArea(childArea);
        floatContentArea.setBPD(childArea.getAllocBPD());
        int effectiveContentIPD = childArea.getEffectiveAllocIPD();
        int contentIPD = childArea.getIPD();
        int xOffset = childArea.getBorderAndPaddingWidthStart();
        floatContentArea.setIPD(effectiveContentIPD);
        childArea.activateEffectiveIPD();
        if (side == Constants.EN_END || side == Constants.EN_RIGHT) {
            xOffset += getStartIndent();
            floatContentArea.setXOffset(xOffset + contentIPD - effectiveContentIPD);
        } else if (side == Constants.EN_START || side == Constants.EN_LEFT) {
            floatContentArea.setXOffset(xOffset);
        }
        LayoutManager lm = parentLayoutManager;
        while (!lm.getGeneratesReferenceArea()) {
            lm = lm.getParent();
        }
        yOffset = lm.getParentArea(floatContentArea).getBPD();
        lm.addChildArea(floatContentArea);
        if (side == Constants.EN_END || side == Constants.EN_RIGHT) {
            lm.getPSLM().setEndIntrusionAdjustment(effectiveContentIPD);
        } else if (side == Constants.EN_START || side == Constants.EN_LEFT) {
            lm.getPSLM().setStartIntrusionAdjustment(effectiveContentIPD);
        }
    }

    /**
     * {@asf.todo - Add info}
     *
     * @param elemenList
     * @param startIndex
     * @param endIndex
     * @return
     */
    public static List<FloatContentLayoutManager> checkForFloats(List<ListElement> elemenList,
            int startIndex, int endIndex) {
        ListIterator<ListElement> iter = elemenList.listIterator(startIndex);
        List<FloatContentLayoutManager> floats = new ArrayList<FloatContentLayoutManager>();
        while (iter.nextIndex() <= endIndex) {
            ListElement element = iter.next();
            if (element instanceof KnuthInlineBox && ((KnuthInlineBox) element).isFloatAnchor()) {
                floats.add(((KnuthInlineBox) element).getFloatContentLM());
            } else if (element instanceof KnuthBlockBox && ((KnuthBlockBox) element).hasFloatAnchors()) {
                floats.addAll(((KnuthBlockBox) element).getFloatContentLMs());
            }
        }
        if (floats.isEmpty()) {
            return Collections.emptyList();
        } else {
            return floats;
        }
    }

    @Override
    protected CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return null;
    }

    /**
     * {@asf.todo - Add info}
     *
     * @param layoutContext
     */
    public void processAreas(LayoutContext layoutContext) {
        if (getParent() instanceof FloatLayoutManager) {
            FloatLayoutManager flm = (FloatLayoutManager) getParent();
            flm.processAreas(layoutContext);
        }
    }

    /**
     * @return the height of the float content area
     */
    public int getFloatHeight() {
        return floatContentArea.getAllocBPD();
    }

    /**
     * @return the y-offset of the float content
     */
    public int getFloatYOffset() {
        return yOffset;
    }

    private int getStartIndent() {
        int startIndent;
        LayoutManager lm = getParent();
        while (!(lm instanceof BlockLayoutManager)) {
            lm = lm.getParent();
        }
        startIndent = ((BlockLayoutManager) lm).startIndent;
        return startIndent;
    }
}
