/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.TextInfo;
import org.apache.fop.fo.PropertyManager;
import org.apache.fop.area.Area;
import org.apache.fop.area.BlockParent;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.area.Trait;
import org.apache.fop.traits.LayoutProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.traits.BorderProps;

import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * LayoutManager for a block FO.
 */
public class BlockLayoutManager extends BlockStackingLayoutManager {

    private Block curBlockArea;

    LayoutProps layoutProps;
    BorderAndPadding borderProps;
    BackgroundProps backgroundProps;

    int lead = 12000;
    int lineHeight = 14000;
    int follow = 2000;

    protected List childBreaks = new ArrayList();

    /**
     * Iterator for Block layout.
     * This iterator combines consecutive inline areas and
     * creates a line layout manager.
     * The use of this iterator means that it can be reset properly.
     */
    protected class BlockLMiter extends LMiter {

        private ListIterator proxy;

        public BlockLMiter(ListIterator pr) {
            super(null);
            proxy = pr;
        }

        protected boolean preLoadNext() {
            while (proxy.hasNext()) {
                LayoutManager lm = (LayoutManager) proxy.next();
                if(lm.generatesInlineAreas()) {
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
                if (lm.generatesInlineAreas()) {
                    inlines.add(lm);
                } else {
                    proxy.previous();
                    break;
                }
            }
            LineLayoutManager child;
            child = new LineLayoutManager(fobj, inlines, lineHeight,
                                            lead, follow);
            return child;

        }
    }

    public BlockLayoutManager(FObj fobj) {
        super(fobj);
        childLMiter = new BlockLMiter(childLMiter);
    }

    public void setBlockTextInfo(TextInfo ti) {
        lead = ti.fs.getAscender();
        follow = ti.fs.getDescender();
        lineHeight = ti.lineHeight;
    }

    /**
     * This method provides a hook for a LayoutManager to intialize traits
     * for the areas it will create, based on Properties set on its FO.
     */
    protected void initProperties(PropertyManager pm) {
        layoutProps = pm.getLayoutProps();
        borderProps = pm.getBorderAndPadding();
        backgroundProps = pm.getBackgroundProps();
    }
    
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        LayoutManager curLM; // currently active LM

        int ipd = context.getRefIPD();

        MinOptMax stackSize = new MinOptMax();
        // if starting add space before
        stackSize.add(layoutProps.spaceBefore.space);

        BreakPoss lastPos = null;

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
                    stackSize.add(bp.getStackingSize());
                    if (stackSize.opt > context.getStackLimit().max) {
                        // reset to last break
                        if (lastPos != null) {
                            reset(lastPos.getPosition());
                        } else {
                            curLM.resetPosition(null);
                        }
                        over = true;
                        break;
                    }
                    lastPos = bp;
                    childBreaks.add(bp);

                    if (curLM.generatesInlineAreas()) {
                        // Reset stackLimit for non-first lines
                        childLC.setStackLimit(new MinOptMax(ipd/* - iIndents*/));
                    } else {
                        childLC.setStackLimit(MinOptMax.subtract(
                                                 context.getStackLimit(), stackSize));
                    }
                }
            }
            if(getChildLM() == null || over) {
                if(getChildLM() == null) {
                    setFinished(true);
                    stackSize.add(layoutProps.spaceAfter.space);
                }
                BreakPoss breakPoss = new BreakPoss(
                                    new LeafPosition(this, childBreaks.size() - 1));
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
        addBlockSpacing(adjust, layoutProps.spaceBefore.space);

        addID();

        LayoutManager childLM ;
        int iStartPos = 0;
        LayoutContext lc = new LayoutContext(0);
        while (parentIter.hasNext()) {
            LeafPosition lfp = (LeafPosition) parentIter.next();
            if (lfp.getLeafPos() == -2) {
                childBreaks.clear();
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

        flush();

        // if adjusted space after
        addBlockSpacing(adjust, layoutProps.spaceAfter.space);

        childBreaks.clear();
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
            addBorders(curBlockArea, borderProps);
            addBackground(curBlockArea, backgroundProps);

            // Set up dimensions
            // Must get dimensions from parent area
            Area parentArea = parentLM.getParentArea(curBlockArea);
            int referenceIPD = parentArea.getIPD();
            curBlockArea.setIPD(referenceIPD);
            curBlockArea.setWidth(referenceIPD);
            // Get reference IPD from parentArea
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
        } else {
            //reset(resetPos);
            LayoutManager lm = resetPos.getLM();
        }
    }
}

