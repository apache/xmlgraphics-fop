/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

/* $Id$ */

package org.apache.fop.layoutmgr.table;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.layoutmgr.BlockLevelLayoutManager;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.BreakPoss;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.MinOptMaxUtil;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.BreakPossPosIter;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.traits.MinOptMax;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * LayoutManager for a table-row FO.
 * The row contains cells that are organised according to the columns.
 * A break in a table row will contain breaks for each table cell.
 * If there are row spanning cells then these cells belong to this row
 * but effect the occupied columns of future rows.
 */
public class Row extends BlockStackingLayoutManager implements BlockLevelLayoutManager {
    
    private TableRow fobj;
    
    private List gridUnits = null;
    private List columns = null;
    private int referenceIPD;
    private int rowHeight;
    private int xoffset;
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
     */
    public Row(TableRow node) {
        super(node);
        fobj = node;
    }

    /** @return the table-row FO */
    public TableRow getFObj() {
        return this.fobj;
    }
    
    /**
     * @return the table owning this row
     */
    public Table getTable() {
        FONode node = fobj.getParent();
        while (!(node instanceof Table)) {
            node = node.getParent();
        }
        return (Table)node;
    }
    
    /**
     * Set the columns from the table.
     *
     * @param cols the list of columns for this table
     */
    public void setColumns(List cols) {
        columns = cols;
    }

    /** @return true if this is the layout manager for the first row in a body. */
    public boolean isFirstInBody() {
        return ((TableBody)getFObj().getParent()).isFirst(getFObj());
    }
    
    /** @return true if this is the layout manager for the last row in a body. */
    public boolean isLastInBody() {
        return ((TableBody)getFObj().getParent()).isLast(getFObj());
    }
    
    /**
     * Gets the Column at a given index.
     * @param index index of the column (index must be >= 1)
     * @return the requested Column
     */
    private Column getColumn(int index) {
        int size = columns.size();
        if (index > size - 1) {
            return (Column)columns.get(size - 1);
        } else {
            return (Column)columns.get(index - 1);
        }
    }
    
    private void prepareGridUnits() {
        gridUnits = new java.util.ArrayList();
        List availableCells = new java.util.ArrayList();
        // add cells to list
        while (childLMiter.hasNext()) {
            curChildLM = (LayoutManager) childLMiter.next();
            curChildLM.setParent(this);
            curChildLM.initialize();
            availableCells.add(curChildLM);
        }
        
        //Transfer available cells to their slots
        int colnum = 1;
        ListIterator iter = availableCells.listIterator();
        while (iter.hasNext()) {
            Cell cellLM = (Cell)iter.next();
            TableCell cell = cellLM.getFObj();
            if (cell.hasColumnNumber()) {
                colnum = cell.getColumnNumber();
            }
            while (colnum > gridUnits.size()) {
                gridUnits.add(null);
            }
            if (gridUnits.get(colnum - 1) != null) {
                log.error("Overlapping cell at position " + colnum);
            }
            //Add cell info for primary slot
            OldGridUnit info = new OldGridUnit(cellLM);
            info.row = this;
            gridUnits.set(colnum - 1, info);
            info.column = getColumn(colnum);
            
            //Add cell infos on spanned slots if any
            for (int j = 1; j < cell.getNumberColumnsSpanned(); j++) {
                colnum++;
                OldGridUnit infoSpan = new OldGridUnit(cellLM, j);
                infoSpan.row = this;
                infoSpan.column = getColumn(colnum);
                if (colnum > gridUnits.size()) {
                    gridUnits.add(infoSpan);
                } else {
                    if (gridUnits.get(colnum - 1) != null) {
                        log.error("Overlapping cell at position " + colnum);
                        //TODO throw layout exception
                    }
                    gridUnits.set(colnum - 1, infoSpan);
                }
            }
            colnum++;
        }
        
        //Post-processing the list (looking for gaps and resolve start and end borders)
        postProcessGridUnits();
    }

    private void postProcessGridUnits() {
        for (int pos = 1; pos <= gridUnits.size(); pos++) {
            OldGridUnit gu = (OldGridUnit)gridUnits.get(pos - 1);
            
            //Empty grid units
            if (gu == null) {
                //Add grid unit
                gu = new OldGridUnit(null);
                gu.row = this;
                gu.column = getColumn(pos);
                gridUnits.set(pos - 1, gu);
            }
        }
            
        //Border resolution now that the empty grid units are filled
        for (int pos = 1; pos <= gridUnits.size(); pos++) {
            OldGridUnit starting = (OldGridUnit)gridUnits.get(pos - 1);
         
            //Border resolution
            if (getTable().isSeparateBorderModel()) {
                starting.assignBorder(starting.layoutManager);
            } else {
                //Neighbouring grid unit at start edge 
                OldGridUnit start = null;
                int find = pos - 1;
                while (find >= 1) {
                    OldGridUnit candidate = (OldGridUnit)gridUnits.get(find - 1);
                    if (candidate.isLastGridUnitColSpan()) {
                        start = candidate;
                        break;
                    }
                    find--;
                }
                
                //Ending grid unit for current cell
                OldGridUnit ending = null;
                if (starting.layoutManager != null) {
                    pos += starting.layoutManager.getFObj().getNumberColumnsSpanned() - 1;
                }
                ending = (OldGridUnit)gridUnits.get(pos - 1);
                
                //Neighbouring grid unit at end edge 
                OldGridUnit end = null;
                find = pos + 1;
                while (find <= gridUnits.size()) {
                    OldGridUnit candidate = (OldGridUnit)gridUnits.get(find - 1);
                    if (candidate.isPrimaryGridUnit()) {
                        end = candidate;
                        break;
                    }
                    find++;
                }
                CommonBorderPaddingBackground borders = new CommonBorderPaddingBackground();
                OldGridUnit.resolveBorder(getTable(), borders, starting, 
                        (start != null ? start : null), 
                        CommonBorderPaddingBackground.START);
                starting.effBorders = borders;
                if (starting != ending) {
                    borders = new CommonBorderPaddingBackground();
                }
                OldGridUnit.resolveBorder(getTable(), borders, ending, 
                        (end != null ? end : null), 
                        CommonBorderPaddingBackground.END);
                ending.effBorders = borders;
                //Only start and end borders here, before and after during layout
                //TODO resolve before and after borders during layout
            }
        }
    }
    
    /**
     * Get the cell info for a cell.
     *
     * @param pos the position of the cell (must be >= 1)
     * @return the cell info object
     */
    protected OldGridUnit getCellInfo(int pos) {
        if (gridUnits == null) {
            prepareGridUnits();
        }
        if (pos <= gridUnits.size()) {
            return (OldGridUnit)gridUnits.get(pos - 1);
        } else {
            return null;
        }
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
        //LayoutManager curLM; // currently active LM
        OldGridUnit curGridUnit; //currently active grid unit

        BreakPoss lastPos = null;
        List breakList = new java.util.ArrayList();

        int min = 0;
        int opt = 0;
        int max = 0;

        // This is used for the displacement of the individual cells
        int ipdOffset = 0;
        
        int startColumn = 1;
        boolean over = false;

        while ((curGridUnit = getCellInfo(startColumn)) != null) {
            Cell cellLM = curGridUnit.layoutManager;
            if (curGridUnit.isColSpan()) {
                //skip spanned slots
                startColumn++;
                continue;
            }
            
            List childBreaks = new ArrayList();
            MinOptMax stackSize = new MinOptMax();

            // Set up a LayoutContext
            // the ipd is from the current column
            referenceIPD = context.getRefIPD();
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(
                  MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));

            //Determine which columns this cell will occupy
            List spannedGridUnits = new java.util.ArrayList();
            getGridUnitsForCell(cellLM, startColumn, spannedGridUnits);
            int childRefIPD = 0;
            for (int i = 0; i < spannedGridUnits.size(); i++) {
                Column col = ((OldGridUnit)spannedGridUnits.get(i)).column;
                childRefIPD += col.getWidth().getValue();
            }
            childLC.setRefIPD(childRefIPD);

            if (cellLM != null) {
                cellLM.addGridUnitsFromRow(spannedGridUnits);
                cellLM.setInRowIPDOffset(ipdOffset);
                while (!cellLM.isFinished()) {
                    if ((bp = cellLM.getNextBreakPoss(childLC)) != null) {
                        if (stackSize.opt + bp.getStackingSize().opt > context.getStackLimit().max) {
                            // reset to last break
                            if (lastPos != null) {
                                LayoutManager lm = lastPos.getLayoutManager();
                                lm.resetPosition(lastPos.getPosition());
                                if (lm != cellLM) {
                                    cellLM.resetPosition(null);
                                }
                            } else {
                                cellLM.resetPosition(null);
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
                startColumn += cellLM.getFObj().getNumberColumnsSpanned();
            } else {
                //Skipping empty cells
                //log.debug("empty cell at pos " + startColumn);
                startColumn++;
            }
            
            //Adjust in-row x offset for individual cells
            ipdOffset += childRefIPD;
            
            
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

            if (childBreaks.size() > 0) {
                breakList.add(childBreaks);
            }
        }
        MinOptMax rowSize = new MinOptMax(min, opt, max);
        LengthRangeProperty specifiedBPD = fobj.getBlockProgressionDimension();
        if (specifiedBPD.getEnum() != EN_AUTO) {
            if ((specifiedBPD.getMaximum().getEnum() != EN_AUTO)
                    && (specifiedBPD.getMaximum().getLength().getValue() < rowSize.min)) {
                log.warn("maximum height of row is smaller than the minimum "
                        + "height of its contents");
            }
            MinOptMaxUtil.restrict(rowSize, specifiedBPD);
        }
        rowHeight = rowSize.opt;

        boolean fin = true;
        startColumn = 1;
        //Check if any of the cell LMs haven't finished, yet
        while ((curGridUnit = getCellInfo(startColumn)) != null) {
            Cell cellLM = curGridUnit.layoutManager;
            if (cellLM == null) {
                //skip empty cell
                startColumn++;
                continue;
            }
            if (!cellLM.isFinished()) {
                fin = false;
                break;
            }
            startColumn += cellLM.getFObj().getNumberColumnsSpanned();
        }

        setFinished(fin);
        RowPosition rp = new RowPosition(this, breakList.size() - 1, breakList);
        BreakPoss breakPoss = new BreakPoss(rp);
        if (over) {
            breakPoss.setFlag(BreakPoss.NEXT_OVERFLOWS, true);
        }
        breakPoss.setStackingSize(rowSize);
        return breakPoss;
    }

    /**
     * Determines the grid units that are spanned by the given cell.
     * @param cellLM table-cell LM
     * @param startCell starting cell index (must be >= 1)
     * @param spannedGridUnits List to receive the applicable grid units
     */
    private void getGridUnitsForCell(Cell cellLM, int startCell, List spannedGridUnits) {
        int count;
        if (cellLM != null) {
            count = cellLM.getFObj().getNumberColumnsSpanned();
        } else {
            count = 1;
        }
        spannedGridUnits.clear();
        for (int i = 0; i < count; i++) {
            spannedGridUnits.add(this.gridUnits.get(startCell + i - 1));
        }
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
        //LayoutManager curLM; // currently active LM
        OldGridUnit curGridUnit;
        int cellIndex = 1;

        if (pos == null) {
            while ((curGridUnit = getCellInfo(cellIndex)) != null) {
                if (curGridUnit.layoutManager != null) {
                    curGridUnit.layoutManager.resetPosition(null);
                }
                cellIndex++;
            }
        } else {
            RowPosition rpos = (RowPosition)pos;
            List breaks = rpos.cellBreaks;

            while ((curGridUnit = getCellInfo(cellIndex)) != null) {
                if (curGridUnit.layoutManager != null) {
                    List childbreaks = (List)breaks.get(cellIndex);
                    curGridUnit.layoutManager.resetPosition(
                            (Position)childbreaks.get(childbreaks.size() - 1));
                }
                cellIndex++;
            }
        }

        setFinished(false);
    }

    /**
     * Set the x position offset of this row.
     * This is used to set the position of the areas returned by this row.
     *
     * @param off the x offset
     */
    public void setXOffset(int off) {
        xoffset = off;
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
        BreakPoss bp1 = (BreakPoss)parentIter.peekNext();
        bBogus = !bp1.generatesAreas();
        if (!isBogus()) {
            addID(fobj.getId());
        }

        Cell childLM;
        int iStartPos = 0;
        LayoutContext lc = new LayoutContext(0);
        while (parentIter.hasNext()) {
            RowPosition lfp = (RowPosition) parentIter.next();
            
            //area exclusively for painting the row background
            Block rowArea = getRowArea();
            if (rowArea != null) {
                rowArea.setBPD(rowHeight);
                rowArea.setIPD(referenceIPD);
                rowArea.setXOffset(xoffset);
                rowArea.setYOffset(yoffset);
                parentLM.addChildArea(rowArea);
            }

            for (Iterator iter = lfp.cellBreaks.iterator(); iter.hasNext();) {
                List cellsbr = (List)iter.next();
                BreakPossPosIter breakPosIter;
                breakPosIter = new BreakPossPosIter(cellsbr, 0, cellsbr.size());
                iStartPos = lfp.getLeafPos() + 1;

                while ((childLM = (Cell)breakPosIter.getNextChildLM()) != null) {
                    childLM.setXOffset(xoffset);
                    childLM.setYOffset(yoffset);
                    childLM.setRowHeight(rowHeight);
                    childLM.addAreas(breakPosIter, lc);
                }
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
     */
    public void addChildArea(Area childArea) {
        parentLM.addChildArea(childArea);
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
    public Block getRowArea() {
        if (fobj.getCommonBorderPaddingBackground().hasBackground()) {
            Block block = new Block();
            block.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
            block.setPositioning(Block.ABSOLUTE);
            TraitSetter.addBackground(block, fobj.getCommonBorderPaddingBackground());
            return block;
        } else {
            return null;
        }
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#negotiateBPDAdjustment(int, org.apache.fop.layoutmgr.KnuthElement)
     */
    public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#discardSpace(org.apache.fop.layoutmgr.KnuthGlue)
     */
    public void discardSpace(KnuthGlue spaceGlue) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepTogether()
     */
    public boolean mustKeepTogether() {
        //TODO Keeps will have to be more sophisticated sooner or later
        return ((BlockLevelLayoutManager)getParent()).mustKeepTogether() 
                || !fobj.getKeepTogether().getWithinPage().isAuto()
                || !fobj.getKeepTogether().getWithinColumn().isAuto();
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithPrevious()
     */
    public boolean mustKeepWithPrevious() {
        return !fobj.getKeepWithPrevious().getWithinPage().isAuto()
                || !fobj.getKeepWithPrevious().getWithinColumn().isAuto();
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithNext()
     */
    public boolean mustKeepWithNext() {
        return !fobj.getKeepWithNext().getWithinPage().isAuto()
                || !fobj.getKeepWithNext().getWithinColumn().isAuto();
    }

}

