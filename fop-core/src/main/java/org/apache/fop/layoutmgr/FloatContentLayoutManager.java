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
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockParent;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.SideFloat;
import org.apache.fop.area.Trait;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.Float;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.inline.FloatLayoutManager;
import org.apache.fop.layoutmgr.inline.KnuthInlineBox;
import org.apache.fop.layoutmgr.table.TableLayoutManager;

public class FloatContentLayoutManager extends SpacedBorderedPaddedBlockLayoutManager {

    private SideFloat floatContentArea;
    private int side;
    private int yOffset;

    /**
     * {Add info}
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
        int effectiveContentIPD = getContentAreaIPD(childLMs, childArea);
        int contentIPD = childArea.getIPD();
        int xOffset = childArea.getBorderAndPaddingWidthStart();
        // The float goes in the flow when its content is as wide as the space it could take: the width
        // available to the float, not its own area's. A block-container child's area has the container's
        // width, so comparing against that always held and every such float went in the flow (FOP-3353).
        int availableIPD = getContentAreaIPD();
        if (availableIPD > 0 && getEffectiveAllocIPD(childArea) >= availableIPD) {
            addAreaInFlow(contentIPD, xOffset);
            return;
        }
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
        if ((side == Constants.EN_START || side == Constants.EN_LEFT)
                && !(lm instanceof FlowLayoutManager)) {
            floatContentArea.setXOffset(xOffset - effectiveContentIPD);
        }
        yOffset = lm.getParentArea(floatContentArea).getBPD();
        if (!(lm instanceof FlowLayoutManager)) {
            yOffset += lm.getPSLM().getCurrentPV().getCurrentFlow().getBPD();
        }
        lm.addChildArea(floatContentArea);
        if (side == Constants.EN_END || side == Constants.EN_RIGHT) {
            lm.getPSLM().setEndIntrusionAdjustment(effectiveContentIPD);
        } else if (side == Constants.EN_START || side == Constants.EN_LEFT) {
            lm.getPSLM().setStartIntrusionAdjustment(effectiveContentIPD);
        }
    }

    /**
     * Adds the float content area in the normal flow, so the content that follows goes below it,
     * at full width, no intrusion adjustment being needed.
     *
     * @param contentIPD The IPD available in the containing block.
     * @param xOffset    The x-offset of the content, its start border and padding.
     */
    private void addAreaInFlow(int contentIPD, int xOffset) {
        floatContentArea.setPositioning(Block.STACK);
        floatContentArea.addTrait(Trait.IS_REFERENCE_AREA, Boolean.FALSE);
        floatContentArea.setIPD(contentIPD);
        floatContentArea.setXOffset(xOffset);
        LayoutManager lm = parentLayoutManager;
        while (!lm.getGeneratesReferenceArea()) {
            lm = lm.getParent();
        }
        // The float takes its own space in the flow, so the space already used on the page, that
        // PageBreaker.getOffsetDueToFloat() computes from this offset, ends below the float.
        yOffset = lm.getParentArea(floatContentArea).getBPD() + floatContentArea.getAllocBPD();
        if (!(lm instanceof FlowLayoutManager)) {
            // The closest reference area may be a block-container, see CH-1104.
            yOffset += lm.getPSLM().getCurrentPV().getCurrentFlow().getBPD();
        }
        lm.addChildArea(floatContentArea);
    }

    /**
     * Computes the IPD that the content of the float occupies, borders and padding included.
     * <p>
     * {@link Area#getEffectiveIPD()} returns the content IPD of the widest descendant of an
     * area, so it misses the borders and the padding of that descendant and of its ancestors:
     * the float looks narrower than it is. This method walks the widest of the blocks holding
     * the content and adds them.
     * </p>
     *
     * @param area An area holding float content.
     * @return The allocation IPD of the given area, computed from its widest block descendant.
     */
    private static int getEffectiveAllocIPD(Area area) {
        int ipd = 0;
        int lines = 0;
        if (area instanceof BlockParent && !(area instanceof BlockViewport)
                && !((BlockParent) area).isEmpty()) {
            for (Object child : ((BlockParent) area).getChildAreas()) {
                if (child instanceof Block) {
                    ipd = Math.max(ipd, getEffectiveAllocIPD((Area) child));
                } else if (child instanceof LineArea) {
                    lines++;
                }
            }
        }
        if (ipd == 0) {
            // Content broken on several lines asked for more width than it got, so its block
            // fills the containing block, as the shrink-to-fit width of CSS does.
            ipd = lines > 1 ? area.getIPD() : area.getEffectiveIPD();
        }
        return area.getBorderAndPaddingWidthStart() + ipd + area.getBorderAndPaddingWidthEnd();
    }

    private int getContentAreaIPD(List<LayoutManager> childLMs, Area childArea) {
        int ipd = getContentAreaIPD(childLMs);
        if (ipd == 0) {
            return childArea.getEffectiveAllocIPD();
        }
        return ipd;
    }

    private int getContentAreaIPD(List<LayoutManager> childLMs) {
        int ipd = 0;
        for (LayoutManager childLM : childLMs) {
            if (childLM instanceof TableLayoutManager) {
                ipd += childLM.getContentAreaIPD();
            } else if (childLM.getContentAreaBPD() != -1) {
                ipd += getContentAreaIPD(childLM.getChildLMs());
            }
        }
        return ipd;
    }

    /**
     * {Add info}
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
     * {Add info}
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
