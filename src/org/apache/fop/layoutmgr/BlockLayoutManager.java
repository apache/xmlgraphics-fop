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

    private static class BlockBreakPosition extends LeafPosition {
        List blockps;

        BlockBreakPosition(BPLayoutManager lm, int iBreakIndex, List bps) {
            super(lm, iBreakIndex);
            blockps = bps;
        }
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
                        m_childLMiter.previousIndex();
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
                System.err.println(
                  "WARNING: child LM not a BPLayoutManager: " +
                  lm.getClass().getName());
            }
        }
        return null;
    }

    public BreakPoss getNextBreakPoss(LayoutContext context,
                                      Position prevLineBP) {

        BPLayoutManager curLM ; // currently active LM
        ArrayList list = new ArrayList();

        while ((curLM = getChildLM()) != null) {
            // Make break positions and return lines!
            // Set up a LayoutContext
            int ipd = 0;
            BreakPoss bp;
            ArrayList vecBreakPoss = new ArrayList();

            // Force area creation on first call
            // NOTE: normally not necessary when fully integrated!
            LayoutContext childLC =
              new LayoutContext(LayoutContext.CHECK_REF_AREA);

            while (!curLM.isFinished()) {
                if ((bp = curLM.getNextBreakPoss(childLC, null)) != null) {
                    if (bp.checkIPD()) {
                        // Need IPD in order to layout lines!
                        // This is supposed to bubble up to PageLM to
                        // make the necessary flow reference area, depending
                        // on span and break-before flags set as the BreakPoss
                        // makes its way back up the call stack.
                        // Fake it for now!
                        getParentArea(null);
                        ipd = getContentIPD();
                        childLC.flags &= ~LayoutContext.CHECK_REF_AREA;
                        childLC.setStackLimit(new MinOptMax(ipd/* - m_iIndents -
                                                                                                                                                                                                                                                                                     m_iTextIndent*/));
                    } else {
                        vecBreakPoss.add(bp);
                        // Reset stackLimit for non-first lines
                        childLC.setStackLimit(new MinOptMax(ipd/* - m_iIndents*/));
                    }
                }
            }
            list.add(vecBreakPoss);
            return new BreakPoss(
                     new BlockBreakPosition(curLM, 0, vecBreakPoss));
        }
        setFinished(true);
        return new BreakPoss(new BlockBreakPosition(this, 0, list));
    }

    public void addAreas(PositionIterator parentIter, LayoutContext lc) {

        while (parentIter.hasNext()) {
            BlockBreakPosition bbp = (BlockBreakPosition) parentIter.next();
            bbp.getLM().addAreas( new BreakPossPosIter(bbp.blockps, 0,
                                  bbp.blockps.size()), null);
        }
        flush();
    }


    /**
     * Generate areas by telling all layout managers for its FO's
     * children to generate areas.
     */
    public boolean generateAreas() {
        ArrayList lms = new ArrayList();
        LayoutManager lm = null;
        FObj curFobj = fobj;
        if (fobj != null) {
            ListIterator children = fobj.getChildren();
            while (children.hasNext()) {
                Object childFO = children.next();
                if (childFO instanceof FObj) {
                    ((FObj) childFO).addLayoutManager(lms);
                }
            }
            //fobj = null;
        }

        ArrayList vecBreakPoss = new ArrayList();

        BreakPoss bp;
        LayoutContext childLC = new LayoutContext(0);
        while (!isFinished()) {
            if ((bp = getNextBreakPoss(childLC, null)) != null) {
                vecBreakPoss.add(bp);
            }
        }

        addAreas( new BreakPossPosIter(vecBreakPoss, 0,
                                       vecBreakPoss.size()), null);


        /*
                for (int count = 0; count < lms.size(); count++) {
                    lm = (LayoutManager) lms.get(count);
                    if (lm.generatesInlineAreas()) {
                        ArrayList inlines = new ArrayList();
                        inlines.add(lm);
                        //lms.remove(count);
                        while (count + 1 < lms.size()) {
                            lm = (LayoutManager) lms.get(count + 1);
                            if (lm.generatesInlineAreas()) {
                                inlines.add(lm);
                                lms.remove(count + 1);
                            } else {
                                break;
                            }
                        }
                        lm = new LineBPLayoutManager(curFobj, inlines,
                                                     lineHeight, lead, follow);
                        lms.set(count, lm);
                    }
                    lm.setParentLM(this);
                    if (lm.generateAreas()) {
                        if (flush()) {
                            return true;
                        }
                    }
                }
         */
        return flush(); // Add last area to parent
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



    //     /**
    //      * Called by child LayoutManager when it has filled one of its areas.
    //      * If no current container, make one.
    //      * See if the area will fit in the current container.
    //      * If so, add it.
    //      * @param childArea the area to add: will either be a LineArea or
    //      * a BlockArea.
    //      */
    //     public void  addChild(Area childArea) {
    // 	/* If the childArea fits entirely in the maximum available BPD
    // 	 * add it and return an OK status.
    // 	 * If it doesn't all fit, overrun or ask for split?
    // 	 * Might as well just add it since the page layout process
    // 	 * may need to make other adjustments, resulting in changing
    // 	 * split point.
    // 	 */
    // 	// Things like breaks on child area can cause premature
    // 	// termination of the current area.
    // 	/* We go past the theoretical maximum to be able to handle things
    // 	 * like widows.
    // 	 */
    // 	// WARNING: this doesn't take into account space-specifier
    // 	// adujstment between childArea and last content of blockArea!
    // 	if (blockArea.getContentBPD().min + childArea.getAllocationBPD().min
    // 	    > blockArea.getAvailBPD().max) {
    // 	    if (++extraLines <= iWidows) {
    // 		blockArea.add(childArea);
    // 	    }
    // 	    else {
    // 		blockArea.setIsLast(false);
    // 		parentLM.addChildArea(blockArea);
    // 		// Make a new one for this area
    // 		blockArea = makeAreaForChild(childArea);
    // 		extraLines = 0; // Count potential widows
    // 		blockArea.add(childArea);
    // 	    }
    // 	}
    // 	else {
    // 	    blockArea.add(childArea);
    // 	}
    //     }

}
