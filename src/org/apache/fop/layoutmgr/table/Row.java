/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr.table;

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
import org.apache.fop.area.MinOptMax;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * LayoutManager for a table-row FO.
 * The row contains cells that are organised according to the columns.
 * A break in a table row will contain breaks for each table cell.
 * If there are row spanning cells then these cells belong to this row
 * but effect the occupied columns of future rows.
 */
public class Row extends BlockStackingLayoutManager {

    private List cellList = null;
    private List columns = null;
    private int rowHeight;
    private int yoffset;

    private class RowPosition extends LeafPosition {
        protected List cellBreaks;
        protected RowPosition(LayoutManager lm, int pos, List l) {
            super(lm, pos);
            cellBreaks = l;
        }
    }

    /**
     * Create a new row layout manager.
     *
     * @param fobj the table-row formatting object
     */
    public Row(FObj fobj) {
        super(fobj);
    }

    /**
     * Set the columns from the table.
     *
     * @param cols the list of columns for this table
     */
    public void setColumns(List cols) {
        columns = cols;
    }

    private void setupCells() {
        cellList = new ArrayList();
        // add cells to list
        while (m_childLMiter.hasNext()) {
            m_curChildLM = (LayoutManager) m_childLMiter.next();
            m_curChildLM.setParentLM(this);
            m_curChildLM.init();
            cellList.add(m_curChildLM);
        }
    }

    /**
     * Get the layout manager for a cell.
     *
     * @param pos the position of the cell
     * @return the cell layout manager
     */
    protected Cell getCellLM(int pos) {
        if (cellList == null) {
            setupCells();
        }
        if (pos < cellList.size()) {
            return (Cell)cellList.get(pos);
        }
        return null;
    }

    /**
     * Get the next break possibility.
     * A row needs to get the possible breaks for each cell
     * in the row and find a suitable break across all cells.
     *
     * @param context the layout context for getting breaks
     * @return the next break possibility
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        LayoutManager curLM; // currently active LM

        BreakPoss lastPos = null;
        ArrayList breakList = new ArrayList();

        int min = 0;
        int opt = 0;
        int max = 0;

        int cellcount = 0;
        while ((curLM = getCellLM(cellcount++)) != null) {

            ArrayList childBreaks = new ArrayList();
            MinOptMax stackSize = new MinOptMax();

            // Set up a LayoutContext
            // the ipd is from the current column
            int ipd = context.getRefIPD();
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(
                  MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));

            Column col = (Column)columns.get(cellcount - 1);
            childLC.setRefIPD(col.getWidth());

            while (!curLM.isFinished()) {
                if ((bp = curLM.getNextBreakPoss(childLC)) != null) {
                    stackSize.add(bp.getStackingSize());
                    if (stackSize.min > context.getStackLimit().max) {
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
            // the min is the maximum min of all cells
            if (stackSize.min > min) {
                min = stackSize.min;
            }
            // the optimum is the average of all optimums
            opt += stackSize.opt;
            // the maximum is the largest maximum
            if (stackSize.max > max) {
                max = stackSize.max;
            }

            breakList.add(childBreaks);
        }
        opt = opt / cellcount;
        if (opt < min) {
            opt = min;
        }
        rowHeight = opt;

        MinOptMax rowSize = new MinOptMax(min, opt, max);

        setFinished(true);
        RowPosition rp = new RowPosition(this, breakList.size() - 1, breakList);
        BreakPoss breakPoss = new BreakPoss(rp);
        breakPoss.setStackingSize(rowSize);
        return breakPoss;
    }

    /**
     * Set the y position offset of this row.
     * This is used to set the position of the areas returned by this row.
     *
     * @param off the y offset
     */
    public void setYOffset(int off) {
        yoffset = off;
    } 

    /**
     * Add the areas for the break points.
     * This sets the offset of each cell as it is added.
     *
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);
        addID();

        Cell childLM;
        int iStartPos = 0;
        LayoutContext lc = new LayoutContext(0);
        while (parentIter.hasNext()) {
            RowPosition lfp = (RowPosition) parentIter.next();
            // Add the block areas to Area

            int cellcount = 0;
            int xoffset = 0;
            for (Iterator iter = lfp.cellBreaks.iterator(); iter.hasNext();) {
                List cellsbr = (List)iter.next();
                PositionIterator breakPosIter;
                breakPosIter = new BreakPossPosIter(cellsbr, 0, cellsbr.size());
                iStartPos = lfp.getLeafPos() + 1;
                Column col = (Column)columns.get(cellcount++);
                while ((childLM = (Cell)breakPosIter.getNextChildLM()) != null) {
                    childLM.setXOffset(xoffset);
                    childLM.setYOffset(yoffset);
                    childLM.addAreas(breakPosIter, lc);
                }
                xoffset += col.getWidth();
            }
        }

        flush();

    }

    /**
     * Get the row height of the row after adjusting.
     * Should only be called after adding the row areas.
     *
     * @return the row height of this row after adjustment
     */
    public int getRowHeight() {
        return rowHeight;
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
     * @return the parent are for the child
     */
    public Area getParentArea(Area childArea) {
        return parentLM.getParentArea(childArea);
    }

    /**
     * Add the child.
     * Rows return the areas returned by the child elements.
     * This simply adds the area to the parent layout manager.
     *
     * @param childArea the child area
     * @return unused
     */
    public boolean addChild(Area childArea) {
        return parentLM.addChild(childArea);
    }

    /**
     * Reset the position of this layout manager.
     *
     * @param resetPos the position to reset to
     */
    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
        }
    }
}

