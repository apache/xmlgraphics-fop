/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr.table;

import org.apache.fop.fo.PropertyManager;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
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
import java.util.List;

/**
 * LayoutManager for a table-header, table-footer and table body FO.
 * These fo objects have either rows or cells underneath.
 * Cells are organised into rows.
 */
public class Body extends BlockStackingLayoutManager {
    private BorderAndPadding borderProps = null;
    private BackgroundProps backgroundProps;

    private boolean rows = true;
    private List columns;

    private int yoffset;
    private int bodyHeight;

    private Block curBlockArea;

    private List childBreaks = new ArrayList();

    /**
     * Create a new body layout manager.
     *
     * @param fobj the formatting object that created this manager
     */
    public Body(FObj fobj) {
        super(fobj);
    }

    /**
     * Initialize properties for this layout manager.
     *
     * @param propMgr the property manager from the fo object
     */
    protected void initProperties(PropertyManager propMgr) {
        borderProps = propMgr.getBorderAndPadding();
        backgroundProps = propMgr.getBackgroundProps();
    }

    /**
     * Set the columns from the table.
     *
     * @param cols the list of columns used for this body
     */
    public void setColumns(List cols) {
        columns = cols;
    }

    /**
     * Breaks for this layout manager are of the form of before
     * or after a row and inside a row.
     *
     * @param context the layout context for finding the breaks
     * @return the next break possibility
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        Row curLM; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        BreakPoss lastPos = null;

        while ((curLM = (Row)getChildLM()) != null) {
            // Make break positions
            // Set up a LayoutContext
            int ipd = context.getRefIPD();
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(
                  MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));
            childLC.setRefIPD(ipd);

            curLM.setColumns(columns);

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
     * Set the y offset of this body within the table.
     * This is used to set the row offsets.
     *
     * @param off the y offset position
     */
    public void setYOffset(int off) {
        yoffset = off;
    }

    /**
     * Add the areas for the break points.
     * This sets the offset of each row as it is added.
     *
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);
        addID();

        Row childLM;
        int iStartPos = 0;
        LayoutContext lc = new LayoutContext(0);
        int rowoffset = 0;
        while (parentIter.hasNext()) {
            LeafPosition lfp = (LeafPosition) parentIter.next();
            // Add the block areas to Area
            PositionIterator breakPosIter =
              new BreakPossPosIter(childBreaks, iStartPos,
                                   lfp.getLeafPos() + 1);
            iStartPos = lfp.getLeafPos() + 1;
            int lastheight = 0;
            while ((childLM = (Row)breakPosIter.getNextChildLM()) != null) {
                childLM.setYOffset(yoffset + rowoffset);
                childLM.addAreas(breakPosIter, lc);
                lastheight = childLM.getRowHeight();
            }
            rowoffset += lastheight;
        }
        bodyHeight = rowoffset;

        flush();

        childBreaks.clear();
        curBlockArea = null;
    }

    /**
     * Get the body height of the body after adjusting.
     * Should only be called after adding the body areas.
     *
     * @return the body height of this body
     */
    public int getBodyHeight() {
        return bodyHeight;
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
     * @param childArea the child area
     * @return the parent are of the child
     */
    public Area getParentArea(Area childArea) {
        return parentLM.getParentArea(childArea);
    }

    /**
     * Add the child area.
     * The table-header, table-footer and table-body areas return
     * the areas return by the children.
     *
     * @param childArea the child area to add
     * @return unused
     */
    public void addChild(Area childArea) {
        parentLM.addChild(childArea);
    }

    /**
     * Reset the position of the layout manager.
     *
     * @param resetPos the position to reset to
     */
    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
        }
    }

    /**
     * Create a body area.
     * This area has the background and width set.
     *
     * @return the new body area
     */
    public Area createColumnArea() {
        Area curBlockArea = new Block();

        if(backgroundProps != null) {
            addBackground(curBlockArea, backgroundProps);
        }
        return curBlockArea;
    }
}

