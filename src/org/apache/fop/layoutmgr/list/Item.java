/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr.list;

import org.apache.fop.fo.PropertyManager;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.BreakPoss;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.BreakPossPosIter;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.fo.FObj;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;

import java.util.ArrayList;

/**
 * LayoutManager for a table-cell FO.
 * A cell contains blocks. These blocks fill the cell.
 */
public class Item extends BlockStackingLayoutManager {

    private BorderAndPadding borderProps = null;
    private BackgroundProps backgroundProps;

    private Block curBlockArea;

    private ArrayList childBreaks = new ArrayList();

    private int xoffset;
    private int itemIPD;

    /**
     * Create a new Cell layout manager.
     * @param fobj the formatting object for the cell
     */
    public Item(FObj fobj) {
        super(fobj);
    }

    protected void initProperties(PropertyManager propMgr) {
        borderProps = propMgr.getBorderAndPadding();
        backgroundProps = propMgr.getBackgroundProps();
    }

    /**
     * Get the next break possibility for this cell.
     * A cell contains blocks so there are breaks around the blocks
     * and inside the blocks.
     *
     * @param context the layout context
     * @return the next break possibility
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        LayoutManager curLM; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        // if starting add space before
        // stackSize.add(spaceBefore);
        BreakPoss lastPos = null;

        itemIPD = context.getRefIPD();

        while ((curLM = getChildLM()) != null) {
            if (curLM.generatesInlineAreas()) {
                // error
                curLM.setFinished(true);
                continue;
            }
            // Set up a LayoutContext
            int ipd = context.getRefIPD();
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));
            childLC.setRefIPD(ipd);

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

                    childLC.setStackLimit(MinOptMax.subtract(
                                             context.getStackLimit(), stackSize));
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

    /**
     * Set the x offset of this list item.
     * This offset is used to set the absolute position
     * of the list item within the parent block area.
     *
     * @param off the x offset
     */
    public void setXOffset(int off) {
        xoffset = off;
    }

    /**
     * Add the areas for the break points.
     * The list item contains block stacking layout managers
     * that add block areas.
     *
     * @param parentIter the iterator of the break positions
     * @param layoutContext the layout context for adding the areas
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);
        addID();

        LayoutManager childLM;
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

        if (borderProps != null) {
            addBorders(curBlockArea, borderProps);
        }
        if (backgroundProps != null) {
            addBackground(curBlockArea, backgroundProps);
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
     *
     * @param childArea the child area to get the parent for
     * @return the parent area
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();
            curBlockArea.setPositioning(Block.ABSOLUTE);
            // set position
            curBlockArea.setXOffset(xoffset);
            curBlockArea.setWidth(itemIPD);
            //curBlockArea.setHeight();

            // Set up dimensions
            Area parentArea = parentLM.getParentArea(curBlockArea);
            int referenceIPD = parentArea.getIPD();
            curBlockArea.setIPD(referenceIPD);
            // Get reference IPD from parentArea
            setCurrentArea(curBlockArea); // ??? for generic operations
        }
        return curBlockArea;
    }

    /**
     * Add the child to the list item area.
     *
     * @param childArea the child to add to the cell
     * @return unused
     */
    public boolean addChild(Area childArea) {
        if (curBlockArea != null) {
            curBlockArea.addBlock((Block) childArea);
            return false;
        }
        return false;
    }

    /**
     * Reset the position of the layout.
     *
     * @param resetPos the position to reset to
     */
    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
        }
    }
}

