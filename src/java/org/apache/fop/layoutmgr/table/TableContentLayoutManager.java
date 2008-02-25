/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.layoutmgr.SpaceResolver.SpaceHandlingBreakPosition;
import org.apache.fop.util.BreakUtil;

/**
 * Layout manager for table contents, particularly managing the creation of combined element lists.
 */
public class TableContentLayoutManager implements PercentBaseContext {

    /** Logger **/
    private static Log log = LogFactory.getLog(TableContentLayoutManager.class);

    private TableLayoutManager tableLM;
    private TableRowIterator bodyIter;
    private TableRowIterator headerIter;
    private TableRowIterator footerIter;
    private LinkedList headerList;
    private LinkedList footerList;
    private int headerNetHeight = 0;
    private int footerNetHeight = 0;

    private int startXOffset;
    private int usedBPD;
    
    private TableStepper stepper;
        
    /**
     * Main constructor
     * @param parent Parent layout manager
     */
    TableContentLayoutManager(TableLayoutManager parent) {
        this.tableLM = parent;
        Table table = getTableLM().getTable();
        this.bodyIter = new TableRowIterator(table, TableRowIterator.BODY);
        if (table.getTableHeader() != null) {
            headerIter = new TableRowIterator(table, TableRowIterator.HEADER);
        }
        if (table.getTableFooter() != null) {
            footerIter = new TableRowIterator(table, TableRowIterator.FOOTER);
        }
        stepper = new TableStepper(this);
    }
    
    /**
     * @return the table layout manager
     */
    TableLayoutManager getTableLM() {
        return this.tableLM;
    }
    
    /** @return true if the table uses the separate border model. */
    boolean isSeparateBorderModel() {
        return getTableLM().getTable().isSeparateBorderModel();
    }
    
    /**
     * @return the column setup of this table
     */
    ColumnSetup getColumns() {
        return getTableLM().getColumns();
    }

    /** @return the net header height */
    protected int getHeaderNetHeight() {
        return this.headerNetHeight;
    }

    /** @return the net footer height */
    protected int getFooterNetHeight() {
        return this.footerNetHeight;
    }

    /** @return the header element list */
    protected LinkedList getHeaderElements() {
        return this.headerList;
    }

    /** @return the footer element list */
    protected LinkedList getFooterElements() {
        return this.footerList;
    }

    /** {@inheritDoc} */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        if (log.isDebugEnabled()) {
            log.debug("==> Columns: " + getTableLM().getColumns());
        }
        KnuthBox headerAsFirst = null;
        KnuthBox headerAsSecondToLast = null;
        KnuthBox footerAsLast = null;
        if (headerIter != null && headerList == null) {
            this.headerList = getKnuthElementsForRowIterator(
                    headerIter, context, alignment, TableRowIterator.HEADER);
            this.headerNetHeight
                    = ElementListUtils.calcContentLength(this.headerList);
            if (log.isDebugEnabled()) {
                log.debug("==> Header: " 
                        + headerNetHeight + " - " + this.headerList);
            }
            TableHeaderFooterPosition pos = new TableHeaderFooterPosition(
                    getTableLM(), true, this.headerList);
            KnuthBox box = new KnuthBox(headerNetHeight, pos, false);
            if (getTableLM().getTable().omitHeaderAtBreak()) {
                //We can simply add the table header at the start 
                //of the whole list
                headerAsFirst = box;
            } else {
                headerAsSecondToLast = box;
            }
        }
        if (footerIter != null && footerList == null) {
            this.footerList = getKnuthElementsForRowIterator(
                    footerIter, context, alignment, TableRowIterator.FOOTER);
            this.footerNetHeight
                    = ElementListUtils.calcContentLength(this.footerList);
            if (log.isDebugEnabled()) {
                log.debug("==> Footer: " 
                        + footerNetHeight + " - " + this.footerList);
            }
            //We can simply add the table footer at the end of the whole list
            TableHeaderFooterPosition pos = new TableHeaderFooterPosition(
                    getTableLM(), false, this.footerList);
            KnuthBox box = new KnuthBox(footerNetHeight, pos, false);
            footerAsLast = box;
        }
        LinkedList returnList = getKnuthElementsForRowIterator(
                bodyIter, context, alignment, TableRowIterator.BODY);
        if (headerAsFirst != null) {
            int insertionPoint = 0;
            if (returnList.size() > 0 && ((ListElement)returnList.getFirst()).isForcedBreak()) {
                insertionPoint++;
            }
            returnList.add(insertionPoint, headerAsFirst);
        } else if (headerAsSecondToLast != null) {
            int insertionPoint = returnList.size();
            if (returnList.size() > 0 && ((ListElement)returnList.getLast()).isForcedBreak()) {
                insertionPoint--;
            }
            returnList.add(insertionPoint, headerAsSecondToLast);
        }
        if (footerAsLast != null) {
            int insertionPoint = returnList.size();
            if (returnList.size() > 0 && ((ListElement)returnList.getLast()).isForcedBreak()) {
                insertionPoint--;
            }
            returnList.add(insertionPoint, footerAsLast);
        }
        return returnList;
    }
    
    /**
     * Creates Knuth elements by iterating over a TableRowIterator.
     * @param iter TableRowIterator instance to fetch rows from
     * @param context Active LayoutContext
     * @param alignment alignment indicator
     * @param bodyType Indicates what kind of body is being processed 
     *                  (BODY, HEADER or FOOTER)
     * @return An element list
     */
    private LinkedList getKnuthElementsForRowIterator(TableRowIterator iter, 
            LayoutContext context, int alignment, int bodyType) {
        LinkedList returnList = new LinkedList();
        EffRow[] rowGroup = iter.getNextRowGroup();
        // TODO homogenize the handling of keeps and breaks
        context.unsetFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING
                | LayoutContext.KEEP_WITH_NEXT_PENDING);
        context.setBreakBefore(Constants.EN_AUTO);
        context.setBreakAfter(Constants.EN_AUTO);
        boolean keepWithPrevious = false;
        int breakBefore = Constants.EN_AUTO;
        if (rowGroup != null) {
            RowGroupLayoutManager rowGroupLM = new RowGroupLayoutManager(getTableLM(), rowGroup,
                    stepper);
            List nextRowGroupElems = rowGroupLM.getNextKnuthElements(context, alignment, bodyType);
            keepWithPrevious = context.isKeepWithPreviousPending();
            boolean keepBetween = context.isKeepWithNextPending();
            breakBefore = context.getBreakBefore();
            int breakBetween = context.getBreakAfter();
            returnList.addAll(nextRowGroupElems);
            while ((rowGroup = iter.getNextRowGroup()) != null) {
                rowGroupLM = new RowGroupLayoutManager(getTableLM(), rowGroup, stepper);
                nextRowGroupElems = rowGroupLM.getNextKnuthElements(context, alignment, bodyType);
                int penaltyValue = 0;
                keepBetween |= context.isKeepWithPreviousPending();
                if (keepBetween || tableLM.getTable().mustKeepTogether()) {
                    penaltyValue = KnuthElement.INFINITE;
                }
                breakBetween = BreakUtil.compareBreakClasses(breakBetween,
                        context.getBreakBefore());
                if (breakBetween != Constants.EN_AUTO) {
                    penaltyValue = -KnuthElement.INFINITE;
                }
                TableHFPenaltyPosition penaltyPos = new TableHFPenaltyPosition(getTableLM());
                int penaltyLen = 0;
                if (bodyType == TableRowIterator.BODY) {
                    if (!getTableLM().getTable().omitHeaderAtBreak()) {
                        penaltyLen += getHeaderNetHeight();
                        penaltyPos.headerElements = getHeaderElements();
                    }
                    if (!getTableLM().getTable().omitFooterAtBreak()) {
                        penaltyLen += getFooterNetHeight();
                        penaltyPos.footerElements = getFooterElements();
                    }
                }
                returnList.add(new BreakElement(penaltyPos, 
                        penaltyLen, penaltyValue, breakBetween, context));
                returnList.addAll(nextRowGroupElems);
                breakBetween = context.getBreakAfter();
                keepBetween = context.isKeepWithNextPending();
            }
        }
        context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING, keepWithPrevious);
        context.setBreakBefore(breakBefore);

        //fox:widow-content-limit
        int widowContentLimit = getTableLM().getTable().getWidowContentLimit().getValue(); 
        if (widowContentLimit != 0 && bodyType == TableRowIterator.BODY) {
            ElementListUtils.removeLegalBreaks(returnList, widowContentLimit);
        }
        //fox:orphan-content-limit
        int orphanContentLimit = getTableLM().getTable().getOrphanContentLimit().getValue(); 
        if (orphanContentLimit != 0 && bodyType == TableRowIterator.BODY) {
            ElementListUtils.removeLegalBreaksFromEnd(returnList, orphanContentLimit);
        }
        
        return returnList;
    }

    /**
     * Retuns the X offset of the given grid unit.
     * @param gu the grid unit
     * @return the requested X offset
     */
    protected int getXOffsetOfGridUnit(PrimaryGridUnit gu) {
        int col = gu.getColIndex();
        return startXOffset + getTableLM().getColumns().getXOffset(col + 1, getTableLM());
    }
    
    /**
     * Adds the areas generated by this layout manager to the area tree.
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        this.usedBPD = 0;
        RowPainter painter = new RowPainter(this, layoutContext);

        List positions = new java.util.ArrayList();
        List headerElements = null;
        List footerElements = null;
        Position firstPos = null;
        Position lastPos = null;
        Position lastCheckPos = null;
        while (parentIter.hasNext()) {
            Position pos = (Position)parentIter.next();
            if (pos instanceof SpaceHandlingBreakPosition) {
                //This position has only been needed before addAreas was called, now we need the
                //original one created by the layout manager.
                pos = ((SpaceHandlingBreakPosition)pos).getOriginalBreakPosition();
            }
            if (pos == null) {
                continue;
            }
            if (firstPos == null) {
                firstPos = pos;
            }
            lastPos = pos;
            if (pos.getIndex() >= 0) {
                lastCheckPos = pos;
            }
            if (pos instanceof TableHeaderFooterPosition) {
                TableHeaderFooterPosition thfpos = (TableHeaderFooterPosition)pos;
                //these positions need to be unpacked
                if (thfpos.header) {
                    //Positions for header will be added first
                    headerElements = thfpos.nestedElements;
                } else {
                    //Positions for footers are simply added at the end
                    footerElements = thfpos.nestedElements;
                }
            } else if (pos instanceof TableHFPenaltyPosition) {
                //ignore for now, see special handling below if break is at a penalty
                //Only if the last position in this part/page us such a position it will be used 
            } else if (pos instanceof TableContentPosition) {
                positions.add(pos);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring position: " + pos);
                }
            }
        }
        if (lastPos instanceof TableHFPenaltyPosition) {
            TableHFPenaltyPosition penaltyPos = (TableHFPenaltyPosition)lastPos;
            log.debug("Break at penalty!");
            if (penaltyPos.headerElements != null) {
                //Header positions for the penalty position are in the last element and need to
                //be handled first before all other TableContentPositions
                headerElements = penaltyPos.headerElements;
            }
            if (penaltyPos.footerElements != null) {
                footerElements = penaltyPos.footerElements; 
            }
        }

        Map markers = getTableLM().getTable().getMarkers();
        if (markers != null) {
            getTableLM().getCurrentPV().addMarkers(markers, 
                    true, getTableLM().isFirst(firstPos), getTableLM().isLast(lastCheckPos));
        }
        
        if (headerElements != null) {
            //header positions for the last part are the second-to-last element and need to
            //be handled first before all other TableContentPositions
            PositionIterator nestedIter = new KnuthPossPosIter(headerElements);
            iterateAndPaintPositions(nestedIter, painter, false);
        }
        
        //Iterate over all steps
        Iterator posIter = positions.iterator();
        painter.startBody();
        // Here we are sure that posIter iterates only over TableContentPosition instances
        iterateAndPaintPositions(posIter, painter, footerElements == null);
        painter.endBody();

        if (footerElements != null) {
            //Positions for footers are simply added at the end
            PositionIterator nestedIter = new KnuthPossPosIter(footerElements);
            iterateAndPaintPositions(nestedIter, painter, true);
        }
        
        this.usedBPD += painter.getAccumulatedBPD();

        if (markers != null) {
            getTableLM().getCurrentPV().addMarkers(markers, 
                    false, getTableLM().isFirst(firstPos), getTableLM().isLast(lastCheckPos));
        }
    }

    /**
     * Iterates over a part of the table (header, footer, body) and paints the related
     * elements.
     * 
     * @param iterator iterator over Position elements. Those positions correspond to the
     * elements of the table present on the current page
     * @param painter
     * @param lastOnPage true if the corresponding part will be the last on the page
     * (either body or footer, obviously)
     */
    private void iterateAndPaintPositions(Iterator iterator, RowPainter painter,
            boolean lastOnPage) {
        List lst = new ArrayList();
        boolean firstPos = false;
        TableBody body = null;
        while (iterator.hasNext()) {
            Position pos = (Position)iterator.next();
            if (pos instanceof TableContentPosition) {
                TableContentPosition tcpos = (TableContentPosition)pos;
                lst.add(tcpos);
                CellPart part = (CellPart)tcpos.cellParts.get(0);
                if (body == null) {
                    body = part.pgu.getBody();
                }
                if (tcpos.getFlag(TableContentPosition.FIRST_IN_ROWGROUP)
                        && tcpos.getRow().getFlag(EffRow.FIRST_IN_PART)) {
                    firstPos = true;

                }
                if (tcpos.getFlag(TableContentPosition.LAST_IN_ROWGROUP) 
                        && tcpos.getRow().getFlag(EffRow.LAST_IN_PART)) {
                    log.trace("LAST_IN_ROWGROUP + LAST_IN_PART");
                    handleMarkersAndPositions(lst, body, firstPos, true, painter);
                    //reset
                    firstPos = false;
                    body = null;
                    lst.clear();
                }
            }
        }
        if (body != null) {
            // Entering this block means that the end of the current table-part hasn't
            // been reached (otherwise it would have been caught by the test above). So
            // lastPos is necessarily false
            handleMarkersAndPositions(lst, body, firstPos, false, painter);
        }
        painter.addAreasAndFlushRow(true, lastOnPage);
    }

    private void handleMarkersAndPositions(List positions, TableBody body, boolean firstPos,
            boolean lastPos, RowPainter painter) {
        getTableLM().getCurrentPV().addMarkers(body.getMarkers(), 
                true, firstPos, lastPos);
        int size = positions.size();
        for (int i = 0; i < size; i++) {
            painter.handleTableContentPosition((TableContentPosition)positions.get(i));
        }
        getTableLM().getCurrentPV().addMarkers(body.getMarkers(), 
                false, firstPos, lastPos);
    }

    /**
     * Get the area for a row for background.
     * @param row the table-row object or null
     * @return the row area or null if there's no background to paint
     */
    Block getRowArea(TableRow row) {
        if (row == null || !row.getCommonBorderPaddingBackground().hasBackground()) {
            return null;
        } else {
            Block block = new Block();
            block.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
            block.setPositioning(Block.ABSOLUTE);
            return block;
        }
    }

    /**
     * Adds the area for the row background if any.
     * @param row row for which to generate the background
     * @param bpd block-progression-dimension of the row
     * @param ipd inline-progression-dimension of the row
     * @param yoffset Y offset at which to paint
     */
    void addRowBackgroundArea(TableRow row, int bpd, int ipd, int yoffset) {
        //Add row background if any
        Block rowBackground = getRowArea(row);
        if (rowBackground != null) {
            rowBackground.setBPD(bpd);
            rowBackground.setIPD(ipd);
            rowBackground.setXOffset(this.startXOffset);
            rowBackground.setYOffset(yoffset);
            getTableLM().addChildArea(rowBackground);
            TraitSetter.addBackground(rowBackground, 
                    row.getCommonBorderPaddingBackground(), getTableLM());
        }
    }
    
    
    /**
     * Sets the overall starting x-offset. Used for proper placement of cells.
     * @param startXOffset starting x-offset (table's start-indent)
     */
    void setStartXOffset(int startXOffset) {
        this.startXOffset = startXOffset;
    }

    /**
     * @return the amount of block-progression-dimension used by the content
     */
    int getUsedBPD() {
        return this.usedBPD;
    }

    // --------- Property Resolution related functions --------- //

    /**
     * {@inheritDoc} 
     */
    public int getBaseLength(int lengthBase, FObj fobj) {
        return tableLM.getBaseLength(lengthBase, fobj);
    }

}
