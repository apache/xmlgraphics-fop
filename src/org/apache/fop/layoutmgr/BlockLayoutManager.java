/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
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
    BackgroundProps backgroundsPops;

    int lead = 12000;
    int lineHeight = 14000;
    int follow = 2000;

    ArrayList childBreaks = new ArrayList();

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
                    m_listLMs.add(lineLM);
                } else {
                    m_listLMs.add(lm);
                }
                if (m_curPos < m_listLMs.size()) {
                    return true;
                }
            }
            return false;
        }

        protected LineLayoutManager createLineManager(
          LayoutManager firstlm) {
            LayoutManager lm;
            ArrayList inlines = new ArrayList();
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
        m_childLMiter = new BlockLMiter(m_childLMiter);
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
        backgroundsPops = pm.getBackgroundProps();
    }
    
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        LayoutManager curLM ; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        // if starting add space before
        stackSize.add(layoutProps.spaceBefore.space);

        BreakPoss lastPos = null;

        while ((curLM = getChildLM()) != null) {
            // Make break positions and return blocks!
            // Set up a LayoutContext
            int ipd = context.getRefIPD();
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            // if line layout manager then set stack limit to ipd
            // line LM actually generates a LineArea which is a block
            if (curLM.generatesInlineAreas()) {
                // set stackLimit for lines
                childLC.setStackLimit(new MinOptMax(ipd/* - m_iIndents - m_iTextIndent*/));
                childLC.setRefIPD(ipd);
            } else {
                childLC.setStackLimit(
                  MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));
                childLC.setRefIPD(ipd);
            }

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
                        break;
                    }
                    lastPos = bp;
                    childBreaks.add(bp);

                    if (curLM.generatesInlineAreas()) {
                        // Reset stackLimit for non-first lines
                        childLC.setStackLimit(new MinOptMax(ipd/* - m_iIndents*/));
                    } else {
                        childLC.setStackLimit( MinOptMax.subtract(
                                                 context.getStackLimit(), stackSize));
                    }
                }
            }
            if(getChildLM() == null) {
                stackSize.add(layoutProps.spaceAfter.space);
            }
            BreakPoss breakPoss = new BreakPoss(
                                    new LeafPosition(this, childBreaks.size() - 1));
            breakPoss.setStackingSize(stackSize);
            return breakPoss;
        }
        setFinished(true);
        return null;
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
            addBorders(curBlockArea);

            // Set up dimensions
            // Must get dimensions from parent area
            Area parentArea = parentLM.getParentArea(curBlockArea);
            int referenceIPD = parentArea.getIPD();
            curBlockArea.setIPD(referenceIPD);
            // Get reference IPD from parentArea
            setCurrentArea(curBlockArea); // ??? for generic operations
        }
        return curBlockArea;
    }

    public void addBorders(Block curBlockArea) {
        BorderProps bps = getBorderProps(BorderAndPadding.TOP);
        if(bps.width != 0) {
            curBlockArea.addTrait(Trait.BORDER_START, bps);
        }
        bps = getBorderProps(BorderAndPadding.BOTTOM);
        if(bps.width != 0) {
            curBlockArea.addTrait(Trait.BORDER_END, bps);
        }
        bps = getBorderProps(BorderAndPadding.LEFT);
        if(bps.width != 0) {
            curBlockArea.addTrait(Trait.BORDER_BEFORE, bps);
        }
        bps = getBorderProps(BorderAndPadding.RIGHT);
        if(bps.width != 0) {
            curBlockArea.addTrait(Trait.BORDER_AFTER, bps);
        }
    }

    private BorderProps getBorderProps(int side) {
        BorderProps bps;
        bps = new BorderProps(borderProps.getBorderStyle(side),
                              borderProps.getBorderWidth(side, false),
                              borderProps.getBorderColor(side));
        return bps;
    }

    public boolean addChild(Area childArea) {
        if (curBlockArea != null) {
            if (childArea instanceof LineArea) {
                curBlockArea.addLineArea((LineArea) childArea);

                return false;
            } else {
                curBlockArea.addBlock((Block) childArea);

                return false;
            }
        }
        return false;
    }

    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
        }
    }
}

