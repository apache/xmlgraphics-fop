/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: BlockLayoutManager.java,v 1.19 2004/05/26 04:22:39 gmazza Exp $ */

package org.apache.fop.layoutmgr;

import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.TextInfo;
import org.apache.fop.fo.PropertyManager;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockParent;
import org.apache.fop.area.LineArea;
import org.apache.fop.traits.LayoutProps;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for a block FO.
 */
public class BlockLayoutManager extends BlockStackingLayoutManager {

    private Block curBlockArea;

    private LayoutProps layoutProps;
    private CommonBorderAndPadding borderProps;
    private CommonBackground backgroundProps;
    private CommonMarginBlock marginProps;

    /* holds the (one-time use) fo:block space-before
       and -after properties.  Large fo:blocks are split
       into multiple Area.Blocks to accomodate the subsequent
       regions (pages) they are placed on.  space-before
       is applied at the beginning of the first
       Block and space-after at the end of the last Block
       used in rendering the fo:block.
    */
    private MinOptMax foBlockSpaceBefore = null;
    // need to retain foBlockSpaceAfter from previous instantiation
    private static MinOptMax foBlockSpaceAfter = null;
    private MinOptMax prevFoBlockSpaceAfter = null;

    private int lead = 12000;
    private int lineHeight = 14000;
    private int follow = 2000;

    private int iStartPos = 0;

    protected List childBreaks = new java.util.ArrayList();

    public BlockLayoutManager(org.apache.fop.fo.flow.Block inBlock) {
        super.setFObj(inBlock);
        childLMiter = new BlockLMiter(this, childLMiter);
        userAgent = inBlock.getUserAgent();
        setBlockTextInfo(inBlock.getPropertyManager().getTextLayoutProps(
            inBlock.getFOInputHandler().getFontInfo()));
    }

    private void setBlockTextInfo(TextInfo ti) {
        lead = ti.fs.getAscender();
        follow = -ti.fs.getDescender();
        lineHeight = ti.lineHeight;
    }

    /**
     * @see org.apache.fop.layoutmgr.AbstractLayoutManager#initProperties()
     * @todo need to take into account somewhere the effects of fo:initial-property-set,
     *      if defined for the block.
     */
    protected void initProperties() {
        PropertyManager pm = fobj.getPropertyManager();
        layoutProps = pm.getLayoutProps();
        borderProps = pm.getBorderAndPadding();
        backgroundProps = pm.getBackgroundProps();
        marginProps = pm.getMarginProps();
        foBlockSpaceBefore = layoutProps.spaceBefore.getSpace();
        prevFoBlockSpaceAfter = foBlockSpaceAfter;
    }

    /**
     * Iterator for Block layout.
     * This iterator combines consecutive inline areas and
     * creates a line layout manager.
     * The use of this iterator means that it can be reset properly.
     */
    protected class BlockLMiter extends LMiter {

        private ListIterator proxy;

        public BlockLMiter(LayoutManager lp, ListIterator pr) {
            super(lp, null);
            proxy = pr;
        }

        protected boolean preLoadNext() {
            while (proxy.hasNext()) {
                LayoutManager lm = (LayoutManager) proxy.next();
                lm.setParent(BlockLayoutManager.this);
                if (lm.generatesInlineAreas()) {
                    LineLayoutManager lineLM = createLineManager(lm);
                    listLMs.add(lineLM);
                } else {
                    listLMs.add(lm);
                }
                if (curPos < listLMs.size()) {
                    return true;
                }
            }
            return false;
        }

        protected LineLayoutManager createLineManager(
          LayoutManager firstlm) {
            LayoutManager lm;
            List inlines = new ArrayList();
            inlines.add(firstlm);
            while (proxy.hasNext()) {
                lm = (LayoutManager) proxy.next();
                lm.setParent(BlockLayoutManager.this);
                if (lm.generatesInlineAreas()) {
                    inlines.add(lm);
                } else {
                    proxy.previous();
                    break;
                }
            }
            LineLayoutManager child;
            child = new LineLayoutManager(fobj, lineHeight,
                                            lead, follow);
            child.setLMiter(inlines.listIterator());
            return child;

        }
    }

    public BreakPoss getNextBreakPoss(LayoutContext context) {
        LayoutManager curLM; // currently active LM

        int ipd = context.getRefIPD();
        int iIndents = marginProps.startIndent + marginProps.endIndent;
        int bIndents = borderProps.getBPPaddingAndBorder(false);
        ipd -= iIndents;

        MinOptMax stackSize = new MinOptMax();

        if (prevFoBlockSpaceAfter != null) {
            stackSize.add(prevFoBlockSpaceAfter);
            prevFoBlockSpaceAfter = null;
        }

        if (foBlockSpaceBefore != null) {
            // this function called before addAreas(), so
            // resetting foBlockSpaceBefore = null in addAreas()
            stackSize.add(foBlockSpaceBefore);
        }

        BreakPoss lastPos = null;

        // Set context for percentage property values.
        fobj.setLayoutDimension(PercentBase.BLOCK_IPD, ipd);
        fobj.setLayoutDimension(PercentBase.BLOCK_BPD, -1);

        while ((curLM = getChildLM()) != null) {
            // Make break positions and return blocks!
            // Set up a LayoutContext
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            // if line layout manager then set stack limit to ipd
            // line LM actually generates a LineArea which is a block
            if (curLM.generatesInlineAreas()) {
                // set stackLimit for lines
                childLC.setStackLimit(new MinOptMax(ipd/* - iIndents - iTextIndent*/));
                childLC.setRefIPD(ipd);
            } else {
                childLC.setStackLimit(
                  MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));
                childLC.setRefIPD(ipd);
            }
            boolean over = false;
            while (!curLM.isFinished()) {
                if ((bp = curLM.getNextBreakPoss(childLC)) != null) {
                    if (stackSize.opt + bp.getStackingSize().opt > context.getStackLimit().max) {
                        // reset to last break
                        if (lastPos != null) {
                            LayoutManager lm = lastPos.getLayoutManager();
                            lm.resetPosition(lastPos.getPosition());
                            if (lm != curLM) {
                                curLM.resetPosition(null);
                            }
                        } else {
                            curLM.resetPosition(null);
                        }
                        over = true;
                        break;
                    }
                    stackSize.add(bp.getStackingSize());
                    lastPos = bp;
                    childBreaks.add(bp);

                    if (bp.nextBreakOverflows()) {
                        over = true;
                        break;
                    }

                    if (curLM.generatesInlineAreas()) {
                        // Reset stackLimit for non-first lines
                        childLC.setStackLimit(new MinOptMax(ipd/* - iIndents*/));
                    } else {
                        childLC.setStackLimit(MinOptMax.subtract(
                                                 context.getStackLimit(), stackSize));
                    }
                }
            }
            if (getChildLM() == null || over) {
                if (getChildLM() == null) {
                    setFinished(true);
                    stackSize.add(layoutProps.spaceAfter.getSpace());
                }
                BreakPoss breakPoss = new BreakPoss(
                                    new LeafPosition(this, childBreaks.size() - 1));
                if (over) {
                    breakPoss.setFlag(BreakPoss.NEXT_OVERFLOWS, true);
                }
                breakPoss.setStackingSize(stackSize);
                return breakPoss;
            }
        }
        setFinished(true);
        BreakPoss breakPoss = new BreakPoss(new LeafPosition(this, -2));
        breakPoss.setStackingSize(stackSize);
        return breakPoss;
    }

    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);

        // if adjusted space before
        double adjust = layoutContext.getSpaceAdjust();
        addBlockSpacing(adjust, foBlockSpaceBefore);
        foBlockSpaceBefore = null;

        addID();
        addMarkers(true, true);

        LayoutManager childLM;
        LayoutContext lc = new LayoutContext(0);
        while (parentIter.hasNext()) {
            LeafPosition lfp = (LeafPosition) parentIter.next();
            if (lfp.getLeafPos() == -2) {
                curBlockArea = null;
                flush();
                return;
            }
            // Add the block areas to Area
            PositionIterator breakPosIter =
              new BreakPossPosIter(childBreaks, iStartPos,
                                   lfp.getLeafPos() + 1);
            iStartPos = lfp.getLeafPos() + 1;
            while ((childLM = breakPosIter.getNextChildLM()) != null) {
                childLM.addAreas(breakPosIter, lc);
            }
        }

        int bIndents = borderProps.getBPPaddingAndBorder(false);
        curBlockArea.setHeight(curBlockArea.getHeight() + bIndents);

        addMarkers(false, true);

        flush();

        // if adjusted space after
        foBlockSpaceAfter = layoutProps.spaceAfter.getSpace();
        addBlockSpacing(adjust, foBlockSpaceAfter);

        curBlockArea = null;
    }

    /**
     * Return an Area which can contain the passed childArea. The childArea
     * may not yet have any content, but it has essential traits set.
     * In general, if the LayoutManager already has an Area it simply returns
     * it. Otherwise, it makes a new Area of the appropriate class.
     * It gets a parent area for its area by calling its parent LM.
     * Finally, based on the dimensions of the parent area, it initializes
     * its own area. This includes setting the content IPD and the maximum
     * BPD.
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();

            // set traits
            TraitSetter.addBorders(curBlockArea, borderProps);
            TraitSetter.addBackground(curBlockArea, backgroundProps);
            TraitSetter.addMargins(curBlockArea, borderProps, marginProps);
            TraitSetter.addBreaks(curBlockArea, layoutProps);

            // Set up dimensions
            // Must get dimensions from parent area
            Area parentArea = parentLM.getParentArea(curBlockArea);

            // Get reference IPD from parentArea
            int referenceIPD = parentArea.getIPD();
            curBlockArea.setIPD(referenceIPD);

            // Set the width of the block based on the parent block
            // Need to be careful though, if parent is BC then width may not be set
            int parentwidth = 0;
            if (parentArea instanceof BlockParent) {
                parentwidth = ((BlockParent) parentArea).getWidth();
            }
            if (parentwidth == 0) {
                parentwidth = referenceIPD;
            }
            curBlockArea.setWidth(parentwidth);
            setCurrentArea(curBlockArea); // ??? for generic operations
        }
        return curBlockArea;
    }

    public void addChild(Area childArea) {
        if (curBlockArea != null) {
            if (childArea instanceof LineArea) {
                curBlockArea.addLineArea((LineArea) childArea);
            } else {
                curBlockArea.addBlock((Block) childArea);
            }
        }
    }

    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
            childBreaks.clear();
            iStartPos = 0;
        } else {
            //reset(resetPos);
            LayoutManager lm = resetPos.getLM();
        }
    }
}

