/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr.table;

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
    private BorderAndPadding borderProps = null;
    private BackgroundProps backgroundProps;

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
     * Initialize properties for this layout manager.
     *
     * @param propMgr the property manager for the fo
     */
    protected void initProperties(PropertyManager propMgr) {
        borderProps = propMgr.getBorderAndPadding();
        backgroundProps = propMgr.getBackgroundProps();
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
        while (childLMiter.hasNext()) {
            curChildLM = (LayoutManager) childLMiter.next();
            curChildLM.setUserAgent(getUserAgent());
            curChildLM.setParentLM(this);
            curChildLM.init();
            cellList.add(curChildLM);
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
        List breakList = new ArrayList();

        int min = 0;
        int opt = 0;
        int max = 0;

        int cellcount = 0;
        boolean over = false;

        while ((curLM = getCellLM(cellcount++)) != null) {

            List childBreaks = new ArrayList();
            MinOptMax stackSize = new MinOptMax();

            // Set up a LayoutContext
            // the ipd is from the current column
            int ipd = context.getRefIPD();
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(
                  MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));

            int size = columns.size();
            Column col;
            if (cellcount > size - 1) {
                col = (Column)columns.get(size - 1);
            } else {
                col = (Column)columns.get(cellcount - 1);
            }
            childLC.setRefIPD(col.getWidth());

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

                    childLC.setStackLimit(MinOptMax.subtract(
                                             context.getStackLimit(), stackSize));
                }
            }
            // the min is the maximum min of all cells
            if (stackSize.min > min) {
                min = stackSize.min;
            }
            // the optimum is the maximum of all optimums
            if (stackSize.opt > opt) {
                opt = stackSize.opt;
            }
            // the maximum is the largest maximum
            if (stackSize.max > max) {
                max = stackSize.max;
            }

            breakList.add(childBreaks);
        }
        rowHeight = opt;

        MinOptMax rowSize = new MinOptMax(min, opt, max);

        setFinished(true);
        RowPosition rp = new RowPosition(this, breakList.size() - 1, breakList);
        BreakPoss breakPoss = new BreakPoss(rp);
        if (over) {
            breakPoss.setFlag(BreakPoss.NEXT_OVERFLOWS, true);
        }
        breakPoss.setStackingSize(rowSize);
        return breakPoss;
    }

    /**
     * Reset the layoutmanager "iterator" so that it will start
     * with the passed Position's generating LM
     * on the next call to getChildLM.
     * @param pos a Position returned by a child layout manager
     * representing a potential break decision.
     * If pos is null, then back up to the first child LM.
     */
    protected void reset(Position pos) {
        LayoutManager curLM; // currently active LM
        int cellcount = 0;

        if (pos == null) {
            while ((curLM = getCellLM(cellcount++)) != null) {
                curLM.resetPosition(null);
                cellcount++;
            }
        } else {
            RowPosition rpos = (RowPosition)pos;
            List breaks = rpos.cellBreaks;

            while ((curLM = getCellLM(cellcount++)) != null) {
                List childbreaks = (List)breaks.get(cellcount);
                curLM.resetPosition((Position)childbreaks.get(childbreaks.size() - 1));
                cellcount++;
            }
        }

        setFinished(false);
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

                int size = columns.size();
                Column col;
                if (cellcount > size - 1) {
                   col = (Column)columns.get(size - 1);
                } else {
                    col = (Column)columns.get(cellcount);
                    cellcount++;
                }

                while ((childLM = (Cell)breakPosIter.getNextChildLM()) != null) {
                    childLM.setXOffset(xoffset);
                    childLM.setYOffset(yoffset);
                    childLM.setRowHeight(rowHeight);
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
    public void addChild(Area childArea) {
        parentLM.addChild(childArea);
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


    /**
     * Get the area for this row for background.
     *
     * @return the row area
     */
    public Area getRowArea() {
        Area block = new Block();
        if(backgroundProps != null) {
            addBackground(block, backgroundProps);
        }
        return block;
    }
}

