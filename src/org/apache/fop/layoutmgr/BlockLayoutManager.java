/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.TextInfo;
import org.apache.fop.area.Area;
import org.apache.fop.area.BlockParent;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MinOptMax;

import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * LayoutManager for a block FO.
 */
public class BlockLayoutManager extends BlockStackingLayoutManager {

    private Block curBlockArea;

    int lead = 12000;
    int lineHeight = 14000;
    int follow = 2000;

    ArrayList childBreaks = new ArrayList();

    public BlockLayoutManager(FObj fobj) {
        super(fobj);
    }

    public void setBlockTextInfo(TextInfo ti) {
        lead = ti.fs.getAscender();
        follow = ti.fs.getDescender();
        lineHeight = ti.lineHeight;
    }

    /**
     * Called by child layout manager to get the available space for
     * content in the inline progression direction.
     * Note that a manager may need to ask its parent for this.
     * For a block area, available IPD is determined by indents.
     */
    public int getContentIPD() {
        // adjust for side floats and indents
        getParentArea(null); // make if not existing
        return curBlockArea.getIPD();
    }

    protected BPLayoutManager getChildLM() {
        if (m_curChildLM != null && !m_curChildLM.isFinished()) {
            return m_curChildLM;
        }
        while (m_childLMiter.hasNext()) {
            LayoutManager lm = (LayoutManager) m_childLMiter.next();
            if (lm.generatesInlineAreas()) {
                ArrayList inlines = new ArrayList();
                inlines.add(lm);
                //lms.remove(count);
                while (m_childLMiter.hasNext()) {
                    lm = (LayoutManager) m_childLMiter.next();
                    if (lm.generatesInlineAreas()) {
                        inlines.add(lm);
                        //lms.remove(count + 1);
                    } else {
                        m_childLMiter.previous();
                        break;
                    }
                }
                m_curChildLM = new LineBPLayoutManager(fobj, inlines,
                                                       lineHeight, lead, follow);
                m_curChildLM.setParentLM(this);
                m_curChildLM.init();
                return m_curChildLM;
                //lms.set(count, lm);
            } else if (lm instanceof BPLayoutManager) {
                m_curChildLM = (BPLayoutManager) lm;
                m_curChildLM.setParentLM(this);
                m_curChildLM.init();
                return m_curChildLM;
            } else {
                m_childLMiter.remove();
                //log.warn(
                //  "child LM not a BPLayoutManager: " +
                //  lm.getClass().getName());
            }
        }
        return null;
    }

    public BreakPoss getNextBreakPoss(LayoutContext context,
                                      Position prevLineBP) {

        BPLayoutManager curLM ; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        // if starting add space before
        // stackSize.add(spaceBefore);
        BreakPoss lastPos = null;

        while ((curLM = getChildLM()) != null) {
            // Make break positions and return blocks!
            // Set up a LayoutContext
            int ipd = context.getRefIPD();
            BreakPoss bp;

            LayoutContext childLC =
              new LayoutContext(0);
            if(curLM.generatesInlineAreas()) {
                // set stackLimit for lines
                childLC.setStackLimit(new MinOptMax(ipd/* - m_iIndents - m_iTextIndent*/));
            } else {
                childLC.setStackLimit(MinOptMax.subtract(context.getStackLimit(), stackSize));
                childLC.setRefIPD(ipd);
            }

            while (!curLM.isFinished()) {
                if ((bp = curLM.getNextBreakPoss(childLC, null)) != null) {
                        stackSize.add(bp.getStackingSize());
                        if(stackSize.min > context.getStackLimit().max) {
                            // reset to last break
                            if(lastPos != null) {
                                reset(lastPos.getPosition());
                            } else {
                                curLM.resetPosition(null);
                            }
                            break;
                        }
                        lastPos = bp;
                        childBreaks.add(bp);

                        if(curLM.generatesInlineAreas()) {
                        // Reset stackLimit for non-first lines
                        childLC.setStackLimit(new MinOptMax(ipd/* - m_iIndents*/));
                        } else {
                            childLC.setStackLimit(MinOptMax.subtract(context.getStackLimit(), stackSize));
                        }
                }
            }
            BreakPoss breakPoss = new BreakPoss(
                     new LeafPosition(this, childBreaks.size() - 1));
            breakPoss.setStackingSize(stackSize);
            return breakPoss;
        }
        setFinished(true);
        return null;
    }

    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        getParentArea(null);

        BPLayoutManager childLM ;
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


    public boolean addChild(Area childArea) {
        if (curBlockArea != null) {
            if (childArea instanceof LineArea) {
                // Something about widows and orphans
                // Position the line area and calculate size...
                curBlockArea.addLineArea((LineArea) childArea);

                MinOptMax targetDim = parentArea.getAvailBPD();
                MinOptMax currentDim = curBlockArea.getContentBPD();
                //if(currentDim.min > targetDim.max) {
                //    return true;
                //}

                return false;
            } else {
                curBlockArea.addBlock((Block) childArea);
                //return super.addChild(childArea);

                return false;
            }
        }
        return false;
    }

}

