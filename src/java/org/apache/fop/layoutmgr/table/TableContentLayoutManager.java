/*
 * Copyright 2005 The Apache Software Foundation.
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;

/**
 * Layout manager for table contents, particularly managing the creation of combined element lists.
 */
public class TableContentLayoutManager {

    /** Logger **/
    private static Log log = LogFactory.getLog(TableContentLayoutManager.class);

    private TableLayoutManager tableLM;
    private TableRowIterator trIter;

    private int startXOffset;
    private int usedBPD;
    
    public TableContentLayoutManager(TableLayoutManager parent) {
        this.tableLM = parent;
        this.trIter = new TableRowIterator(getTableLM().getTable(), getTableLM().getColumns());
    }
    
    public TableLayoutManager getTableLM() {
        return this.tableLM;
    }
    
    public ColumnSetup getColumns() {
        return getTableLM().getColumns();
    }
    
    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getNextKnuthElements(org.apache.fop.layoutmgr.LayoutContext, int)
     */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        System.out.println(getTableLM().getColumns());
        LinkedList returnList = new LinkedList();
        TableRowIterator.EffRow row = null;
        while ((row = trIter.getNextRow()) != null) {
            List pgus = new java.util.ArrayList();
            TableRow tableRow = null;
            int maxCellHeight = 0;
            for (int j = 0; j < row.getGridUnits().size(); j++) {
                GridUnit gu = (GridUnit)row.getGridUnits().get(j);
                if (gu.isPrimary() && !gu.isEmpty()) {
                    PrimaryGridUnit primary = (PrimaryGridUnit)gu;
                    primary.getCellLM().setParent(tableLM);

                    //Calculate width of cell
                    int spanWidth = 0;
                    for (int i = primary.getStartCol(); 
                            i < primary.getStartCol() + primary.getCell().getNumberColumnsSpanned();
                            i++) {
                        spanWidth += getTableLM().getColumns().getColumn(i + 1)
                            .getColumnWidth().getValue();
                    }
                    log.info("spanWidth=" + spanWidth);
                    LayoutContext childLC = new LayoutContext(0);
                    childLC.setStackLimit(context.getStackLimit()); //necessary?
                    childLC.setRefIPD(spanWidth);
                    
                    LinkedList elems = primary.getCellLM().getNextKnuthElements(childLC, alignment);
                    primary.setElements(elems);
                    log.debug("Elements: " + elems);
                    int len = calcCellHeightFromContents(elems);
                    pgus.add(primary);
                    maxCellHeight = Math.max(maxCellHeight, len);
                    if (len > row.getHeight().opt) {
                        row.setHeight(new MinOptMax(len));
                    }
                    LengthRangeProperty bpd = primary.getCell().getBlockProgressionDimension();
                    if (!bpd.getOptimum().isAuto()) {
                        if (bpd.getOptimum().getLength().getValue() > row.getHeight().opt) {
                            row.setHeight(new MinOptMax(bpd.getOptimum().getLength().getValue()));
                        }
                    }
                    if (tableRow == null) {
                        tableRow = primary.getRow();
                    }
                }
            }
            
            if (tableRow != null) {
                LengthRangeProperty bpd = tableRow.getBlockProgressionDimension();
                if (!bpd.getOptimum().isAuto()) {
                    if (bpd.getOptimum().getLength().getValue() > row.getHeight().opt) {
                        row.setHeight(new MinOptMax(bpd.getOptimum().getLength().getValue()));
                    }
                }
            }
            log.debug(row);
            
            PrimaryGridUnit[] pguArray = new PrimaryGridUnit[pgus.size()];
            pguArray = (PrimaryGridUnit[])pgus.toArray(pguArray);
            LinkedList returnedList = getCombinedKnuthElementsForRow(pguArray, row);
            if (returnedList != null) {
                returnList.addAll(returnedList);
            }

            if (row.getHeight().opt > maxCellHeight) {
                //TODO Fix me (additional spaces)
                log.warn("Knuth elements for additional space coming from height/bpd propertes NYI");
                int space = row.getHeight().opt - maxCellHeight;
                returnList.add(new KnuthGlue(space, 0, 0, new Position(null), false));
            }
        }
        return returnList;
    }
    
    private LinkedList getCombinedKnuthElementsForRow(PrimaryGridUnit[] pguArray, 
            TableRowIterator.EffRow row) {
        List[] elementLists = new List[pguArray.length];
        for (int i = 0; i < pguArray.length; i++) {
            //Copy elements to array lists to improve element access performance
            elementLists[i] = new java.util.ArrayList(pguArray[i].getElements());
        }
        int[] index = new int[pguArray.length];
        int[] start = new int[pguArray.length];
        int[] end = new int[pguArray.length];
        int[] widths = new int[pguArray.length];
        int[] fullWidths = new int[pguArray.length];
        Arrays.fill(end, -1);
        
        int totalHeight = 0;
        for (int i = 0; i < pguArray.length; i++) {
            fullWidths[i] = calcCellHeightFromContents(pguArray[i].getElements());
            totalHeight = Math.max(totalHeight, fullWidths[i]);
        }
        int laststep = 0;
        int step;
        int addedBoxLen = 0;
        LinkedList returnList = new LinkedList();
        while ((step = getNextStep(laststep, elementLists, index, start, end, 
                widths, fullWidths)) > 0) {
            int increase = step - laststep;
            int penaltyLen = step + getMaxRemainingHeight(fullWidths, widths) - totalHeight;
            int boxLen = step - addedBoxLen - penaltyLen;
            addedBoxLen += boxLen;
            
            log.debug(step + " " + increase + " box=" + boxLen + " penalty=" + penaltyLen);
            
            //Put all involved grid units into a list
            List gridUnitParts = new java.util.ArrayList(pguArray.length);
            for (int i = 0; i < pguArray.length; i++) {
                if (end[i] >= start[i]) {
                    gridUnitParts.add(new GridUnitPart(pguArray[i], start[i], end[i]));
                }
            }
            
            //Create elements for step
            TableContentPosition tcpos = new TableContentPosition(getTableLM(), 
                    gridUnitParts, row);
            returnList.add(new KnuthBox(boxLen, tcpos, false));
            returnList.add(new KnuthPenalty(penaltyLen, 0, false, null, false));
            laststep = step;
        }
        return returnList;
    }

    private int getMaxRemainingHeight(int[] fullWidths, int[] widths) {
        int maxW = 0;
        for (int i = 0; i < fullWidths.length; i++) {
            maxW = Math.max(maxW, fullWidths[i] - widths[i]);
        }
        return maxW;
    }
    
    private int getNextStep(int laststep, List[] elementLists, int[] index, 
            int[] start, int[] end, int[] widths, int[] fullWidths) {
        int backupWidths[] = new int[start.length];
        System.arraycopy(widths, 0, backupWidths, 0, backupWidths.length);
        //set starting points
        for (int i = 0; i < start.length; i++) {
            if (end[i] < elementLists[i].size()) {
                start[i] = end[i] + 1;
            } else {
                start[i] = -1; //end of list reached
                end[i] = -1;
            }
        }
        //Arrays.fill(widths, laststep);
        
        //Get next possible sequence for each cell
        int seqCount = 0;
        for (int i = 0; i < start.length; i++) {
            while (end[i] + 1 < elementLists[i].size()) {
                end[i]++;
                KnuthElement el = (KnuthElement)elementLists[i].get(end[i]);
                if (el.isPenalty()) {
                    if (el.getP() < KnuthElement.INFINITE) {
                        //First legal break point
                        break;
                    }
                } else if (el.isGlue()) {
                    KnuthElement prev = (KnuthElement)elementLists[i].get(end[i] - 1);
                    if (prev.isBox()) {
                        //Second legal break point
                        break;
                    }
                    widths[i] += el.getW();
                } else {
                    widths[i] += el.getW();
                }
            }
            if (end[i] < start[i]) {
                widths[i] = backupWidths[i];
            } else {
                seqCount++;
            }
            //System.out.println("part " + start[i] + "-" + end[i] + " " + widths[i]);
        }
        if (seqCount == 0) {
            return 0;
        }
        
        //Determine smallest possible step
        int minStep = Integer.MAX_VALUE;
        for (int i = 0; i < widths.length; i++) {
            if (end[i] >= start[i]) {
                minStep = Math.min(widths[i], minStep);
            }
        }

        //Reset bigger-than-minimum sequences
        for (int i = 0; i < widths.length; i++) {
            if (widths[i] > minStep) {
                widths[i] = backupWidths[i];
                end[i] = start[i] - 1;
            }
        }
        if (log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < widths.length; i++) {
                if (end[i] >= start[i]) {
                    sb.append(i + ": " + start[i] + "-" + end[i] + "(" + widths[i] + "), ");
                } else {
                    sb.append(i + ": skip, ");
                }
            }
            log.debug(sb.toString());
        }
        return minStep;
    }

    private int calcCellHeightFromContents(List elems, int start, int end) {
        ListIterator iter = elems.listIterator(start);
        int count = end - start + 1;
        int len = 0;
        while (iter.hasNext()) {
            KnuthElement el = (KnuthElement)iter.next();
            if (el.isBox()) {
                len += el.getW();
            } else if (el.isGlue()) {
                len += el.getW();
            } else {
                log.debug("Ignoring penalty: " + el);
                //ignore penalties
            }
            count--;
            if (count == 0) {
                break;
            }
        }
        return len;
    }
    
    private int calcCellHeightFromContents(List elems) {
        return calcCellHeightFromContents(elems, 0, elems.size() - 1);
    }
    
    protected int getXOffsetOfGridUnit(GridUnit gu) {
        int col = gu.getStartCol();
        return startXOffset + getTableLM().getColumns().getXOffset(col + 1);
    }
    
    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        this.usedBPD = 0;
        int colCount = getColumns().getColumnCount();
        TableRowIterator.EffRow lastRow = null;
        int lastRowHeight = 0;
        TableRow rowFO = null;
        int yoffset = 0;
        
        //These three variables are our buffer to recombine the individual steps into cells
        PrimaryGridUnit[] gridUnits = new PrimaryGridUnit[colCount];
        int[] start = new int[colCount];
        int[] end = new int[colCount];
        int[] partLength = new int[colCount];
        
        //Iterate over all steps
        while (parentIter.hasNext()) {
            Position pos = (Position)parentIter.next();
            rowFO = null;
            if (pos instanceof TableContentPosition) {
                TableContentPosition tcpos = (TableContentPosition)pos;
                if (lastRow != tcpos.row && lastRow != null) {
                    //yoffset += lastRow.getHeight().opt;
                    yoffset += lastRowHeight;
                    this.usedBPD += lastRowHeight;
                }
                lastRow = tcpos.row;
                Iterator iter = tcpos.gridUnitParts.iterator();
                //Iterate over all grid units in the current step
                while (iter.hasNext()) {
                    GridUnitPart gup = (GridUnitPart)iter.next();
                    log.debug(">" + gup);
                    int colIndex = gup.pgu.getStartCol();
                    if (gridUnits[colIndex] != gup.pgu) {
                        gridUnits[colIndex] = gup.pgu;
                        start[colIndex] = gup.start;
                        end[colIndex] = gup.end;
                    } else {
                        end[colIndex] = gup.end;
                    }
                    if (rowFO == null) {
                        //Find the row if any
                        rowFO = gridUnits[colIndex].getRow();
                    }
                }
                
                //Calculate the height of the row
                int maxLen = 0;
                for (int i = 0; i < gridUnits.length; i++) {
                    if ((gridUnits[i] != null) 
                            && (end[i] == gridUnits[i].getElements().size() - 1)) {
                        log.debug("getting len for " + i + " " 
                                + start[i] + "-" + end[i]);
                        int len = calcCellHeightFromContents(
                                gridUnits[i].getElements(), start[i], end[i]);
                        partLength[i] = len;
                        log.debug("len of part: " + len);
                        maxLen = Math.max(maxLen, len);
                        maxLen = Math.max(maxLen, getExplicitCellHeight(gridUnits[i]));
                    }
                }
                lastRowHeight = maxLen;
                
                //Add areas for row
                addRowBackgroundArea(rowFO, lastRowHeight, layoutContext.getRefIPD(), yoffset);
                for (int i = 0; i < gridUnits.length; i++) {
                    if ((gridUnits[i] != null) 
                            && (end[i] == gridUnits[i].getElements().size() - 1)) {
                        log.debug("flushing..." + i + " " 
                                + start[i] + "-" + end[i]);
                        addAreasForCell(gridUnits[i], start[i], end[i], 
                                layoutContext, lastRow, yoffset, partLength[i], maxLen);
                        gridUnits[i] = null;
                        start[i] = 0;
                        end[i] = 0;
                    }
                }
            }
        }
        
        //Calculate the height of the row
        int maxLen = 0;
        for (int i = 0; i < gridUnits.length; i++) {
            if (gridUnits[i] != null) {
                int len = calcCellHeightFromContents(
                        gridUnits[i].getElements(), start[i], end[i]);
                partLength[i] = len;
                log.debug("len of part: " + len);
                maxLen = Math.max(maxLen, len);
                maxLen = Math.max(maxLen, getExplicitCellHeight(gridUnits[i]));
            }
        }
        
        //Add areas now
        addRowBackgroundArea(rowFO, lastRowHeight, layoutContext.getRefIPD(), yoffset);
        for (int i = 0; i < gridUnits.length; i++) {
            if (gridUnits[i] != null) {
                log.debug("final flushing " + i + " " + start[i] + "-" + end[i]);
                addAreasForCell(gridUnits[i], start[i], end[i], 
                        layoutContext, lastRow, yoffset, partLength[i], maxLen);
            }
        }
        this.usedBPD += lastRowHeight; //for last row
    }
    
    private int getExplicitCellHeight(PrimaryGridUnit pgu) {
        int len = 0;
        if (!pgu.getCell().getBlockProgressionDimension().getOptimum().isAuto()) {
            len = pgu.getCell().getBlockProgressionDimension()
                    .getOptimum().getLength().getValue();
        }
        if (pgu.getRow() != null 
                && !pgu.getRow().getBlockProgressionDimension().getOptimum().isAuto()) {
            len = Math.max(len, pgu.getRow().getBlockProgressionDimension()
                    .getOptimum().getLength().getValue());
        }
        return len;
    }
    
    private void addAreasForCell(PrimaryGridUnit gu, int start, int end, 
            LayoutContext layoutContext, TableRowIterator.EffRow row, 
            int yoffset, int contentHeight, int rowHeight) {
        Cell cellLM = gu.getCellLM();
        cellLM.setXOffset(getXOffsetOfGridUnit(gu));
        cellLM.setYOffset(yoffset);
        cellLM.setContentHeight(contentHeight);
        cellLM.setRowHeight(rowHeight);
        //cellLM.setRowHeight(row.getHeight().opt);
        cellLM.addAreas(new KnuthPossPosIter(gu.getElements(), 
                start, end + 1), layoutContext);
    }
    
    /**
     * Get the area for a row for background.
     * @param row the table-row object or null
     * @return the row area or null if there's no background to paint
     */
    public Block getRowArea(TableRow row) {
        if (row == null || !row.getCommonBorderPaddingBackground().hasBackground()) {
            return null;
        } else {
            Block block = new Block();
            block.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
            block.setPositioning(Block.ABSOLUTE);
            TraitSetter.addBackground(block, row.getCommonBorderPaddingBackground());
            return block;
        }
    }

    public void addRowBackgroundArea(TableRow row, int bpd, int ipd, int yoffset) {
        //Add row background if any
        Block rowBackground = getRowArea(row);
        if (rowBackground != null) {
            rowBackground.setBPD(bpd);
            rowBackground.setIPD(ipd);
            rowBackground.setXOffset(this.startXOffset);
            rowBackground.setYOffset(yoffset);
            getTableLM().addChildArea(rowBackground);
        }
    }
    
    
    /**
     * Sets the overall starting x-offset. Used for proper placement of cells.
     * @param startXOffset starting x-offset (table's start-indent)
     */
    public void setStartXOffset(int startXOffset) {
        this.startXOffset = startXOffset;
    }

    public int getUsedBPD() {
        return this.usedBPD;
    }
    
    private class GridUnitPart {
        
        protected PrimaryGridUnit pgu;
        protected int start;
        protected int end;
        
        protected GridUnitPart(PrimaryGridUnit pgu, int start, int end) {
            this.pgu = pgu;
            this.start = start;
            this.end = end;
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            StringBuffer sb = new StringBuffer("Part: ");
            sb.append(start).append("-").append(end);
            sb.append(" ").append(pgu);
            return sb.toString();
        }
        
    }
    
    public class TableContentPosition extends Position {

        protected List gridUnitParts;
        protected TableRowIterator.EffRow row;
        
        protected TableContentPosition(LayoutManager lm, List gridUnitParts, 
                TableRowIterator.EffRow row) {
            super(lm);
            this.gridUnitParts = gridUnitParts;
            this.row = row;
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            StringBuffer sb = new StringBuffer("TableContentPosition {");
            sb.append(gridUnitParts);
            sb.append("}");
            return sb.toString();
        }
    }

}
