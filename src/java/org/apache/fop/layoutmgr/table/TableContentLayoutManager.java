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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;
import org.apache.fop.fo.flow.table.TablePart;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.Keep;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceResolver.SpaceHandlingBreakPosition;
import org.apache.fop.util.BreakUtil;

/**
 * Layout manager for table contents, particularly managing the creation of combined element lists.
 */
public class TableContentLayoutManager implements PercentBaseContext {

    /** Logger **/
    private static final Log LOG = LogFactory.getLog(TableContentLayoutManager.class);

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

    private boolean headerIsBeingRepeated;

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

    /**
     * Get a sequence of KnuthElements representing the content
     * of the node assigned to the LM.
     *
     * @param context   the LayoutContext used to store layout information
     * @param alignment the desired text alignment
     * @return          the list of KnuthElements
     * @see org.apache.fop.layoutmgr.LayoutManager#getNextKnuthElements(LayoutContext, int)
     */
    public List getNextKnuthElements(LayoutContext context, int alignment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> Columns: " + getTableLM().getColumns());
        }
        KnuthBox headerAsFirst = null;
        KnuthBox headerAsSecondToLast = null;
        KnuthBox footerAsLast = null;
        if (headerIter != null && headerList == null) {
            this.headerList = getKnuthElementsForRowIterator(
                    headerIter, context, alignment, TableRowIterator.HEADER);
            this.headerNetHeight
                    = ElementListUtils.calcContentLength(this.headerList);
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> Header: "
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> Footer: "
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
        context.clearKeepsPending();
        context.setBreakBefore(Constants.EN_AUTO);
        context.setBreakAfter(Constants.EN_AUTO);
        Keep keepWithPrevious = Keep.KEEP_AUTO;
        int breakBefore = Constants.EN_AUTO;
        if (rowGroup != null) {
            RowGroupLayoutManager rowGroupLM = new RowGroupLayoutManager(getTableLM(), rowGroup,
                    stepper);
            List nextRowGroupElems = rowGroupLM.getNextKnuthElements(context, alignment, bodyType);
            keepWithPrevious = keepWithPrevious.compare(context.getKeepWithPreviousPending());
            breakBefore = context.getBreakBefore();
            int breakBetween = context.getBreakAfter();
            returnList.addAll(nextRowGroupElems);
            while ((rowGroup = iter.getNextRowGroup()) != null) {
                rowGroupLM = new RowGroupLayoutManager(getTableLM(), rowGroup, stepper);

                //Note previous pending keep-with-next and clear the strength
                //(as the layout context is reused)
                Keep keepWithNextPending = context.getKeepWithNextPending();
                context.clearKeepWithNextPending();

                //Get elements for next row group
                nextRowGroupElems = rowGroupLM.getNextKnuthElements(context, alignment, bodyType);
                /*
                 * The last break element produced by TableStepper (for the previous row
                 * group) may be used to represent the break between the two row groups.
                 * Its penalty value and break class must just be overridden by the
                 * characteristics of the keep or break between the two.
                 *
                 * However, we mustn't forget that if the after border of the last row of
                 * the row group is thicker in the normal case than in the trailing case,
                 * an additional glue will be appended to the element list. So we may have
                 * to go two steps backwards in the list.
                 */

                //Determine keep constraints
                Keep keep = keepWithNextPending.compare(context.getKeepWithPreviousPending());
                context.clearKeepWithPreviousPending();
                keep = keep.compare(getTableLM().getKeepTogether());
                int penaltyValue = keep.getPenalty();
                int breakClass = keep.getContext();

                breakBetween = BreakUtil.compareBreakClasses(breakBetween,
                        context.getBreakBefore());
                if (breakBetween != Constants.EN_AUTO) {
                    penaltyValue = -KnuthElement.INFINITE;
                    breakClass = breakBetween;
                }
                BreakElement breakElement;
                ListIterator elemIter = returnList.listIterator(returnList.size());
                ListElement elem = (ListElement) elemIter.previous();
                if (elem instanceof KnuthGlue) {
                    breakElement = (BreakElement) elemIter.previous();
                } else {
                    breakElement = (BreakElement) elem;
                }
                breakElement.setPenaltyValue(penaltyValue);
                breakElement.setBreakClass(breakClass);
                returnList.addAll(nextRowGroupElems);
                breakBetween = context.getBreakAfter();
            }
        }
        /*
         * The last break produced for the last row-group of this table part must be
         * removed, because the breaking after the table will be handled by TableLM.
         * Unless the element list ends with a glue, which must be kept to accurately
         * represent the content. In such a case the break is simply disabled by setting
         * its penalty to infinite.
         */
        ListIterator elemIter = returnList.listIterator(returnList.size());
        ListElement elem = (ListElement) elemIter.previous();
        if (elem instanceof KnuthGlue) {
            BreakElement breakElement = (BreakElement) elemIter.previous();
            breakElement.setPenaltyValue(KnuthElement.INFINITE);
        } else {
            elemIter.remove();
        }
        context.updateKeepWithPreviousPending(keepWithPrevious);
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
     * Returns the X offset of the given grid unit.
     * @param gu the grid unit
     * @return the requested X offset
     */
    protected int getXOffsetOfGridUnit(PrimaryGridUnit gu) {
        return getXOffsetOfGridUnit(gu.getColIndex(), gu.getCell().getNumberColumnsSpanned());
    }

    /**
     * Returns the X offset of the grid unit in the given column.
     * @param colIndex the column index (zero-based)
     * @param nrColSpan number columns spanned
     * @return the requested X offset
     */
    protected int getXOffsetOfGridUnit(int colIndex, int nrColSpan) {
        return startXOffset + getTableLM().getColumns().getXOffset(colIndex + 1, nrColSpan, getTableLM());
    }

    /**
     * Adds the areas generated by this layout manager to the area tree.
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        this.usedBPD = 0;
        RowPainter painter = new RowPainter(this, layoutContext);

        List tablePositions = new java.util.ArrayList();
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
                tablePositions.add(pos);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Ignoring position: " + pos);
                }
            }
        }
        boolean treatFooterAsArtifact = layoutContext.treatAsArtifact();
        if (lastPos instanceof TableHFPenaltyPosition) {
            TableHFPenaltyPosition penaltyPos = (TableHFPenaltyPosition)lastPos;
            LOG.debug("Break at penalty!");
            if (penaltyPos.headerElements != null) {
                //Header positions for the penalty position are in the last element and need to
                //be handled first before all other TableContentPositions
                headerElements = penaltyPos.headerElements;
            }
            if (penaltyPos.footerElements != null) {
                footerElements = penaltyPos.footerElements;
                treatFooterAsArtifact = true;
            }
        }

        // there may be table fragment markers stored; clear them since we are starting a new fragment
        tableLM.clearTableFragmentMarkers();

        // note: markers at table level are to be retrieved by the page, not by the table itself
        Map<String, Marker> markers = getTableLM().getTable().getMarkers();
        if (markers != null) {
            getTableLM().getCurrentPV().registerMarkers(markers,
                    true, getTableLM().isFirst(firstPos), getTableLM().isLast(lastCheckPos));
        }

        if (headerElements != null) {
            boolean ancestorTreatAsArtifact = layoutContext.treatAsArtifact();
            if (headerIsBeingRepeated) {
                layoutContext.setTreatAsArtifact(true);
            }
            //header positions for the last part are the second-to-last element and need to
            //be handled first before all other TableContentPositions
            addHeaderFooterAreas(headerElements, tableLM.getTable().getTableHeader(), painter,
                    false);
            if (!ancestorTreatAsArtifact) {
                headerIsBeingRepeated = true;
            }
            layoutContext.setTreatAsArtifact(ancestorTreatAsArtifact);
        }

        if (tablePositions.isEmpty()) {
            // TODO make sure this actually never happens
            LOG.error("tablePositions empty."
                    + " Please send your FO file to fop-users@xmlgraphics.apache.org");
        } else {
            // Here we are sure that posIter iterates only over TableContentPosition instances
            addBodyAreas(tablePositions.iterator(), painter, footerElements == null);
        }

        // if there are TCLMs saved because they have a RetrieveTableMarker, we repeat the header areas now;
        // this can also be done after the areas for the footer are added but should be the same as here
        tableLM.repeatAddAreasForSavedTableHeaderTableCellLayoutManagers();

        if (footerElements != null) {
            boolean ancestorTreatAsArtifact = layoutContext.treatAsArtifact();
            layoutContext.setTreatAsArtifact(treatFooterAsArtifact);
            //Positions for footers are simply added at the end
            addHeaderFooterAreas(footerElements, tableLM.getTable().getTableFooter(), painter,
                    true);
            layoutContext.setTreatAsArtifact(ancestorTreatAsArtifact);
        }

        this.usedBPD += painter.getAccumulatedBPD();

        if (markers != null) {
            getTableLM().getCurrentPV().registerMarkers(markers,
                    false, getTableLM().isFirst(firstPos), getTableLM().isLast(lastCheckPos));
        }
    }

    private void addHeaderFooterAreas(List elements, TablePart part, RowPainter painter,
            boolean lastOnPage) {
        List lst = new java.util.ArrayList(elements.size());
        for (Iterator iter = new KnuthPossPosIter(elements); iter.hasNext();) {
            Position pos = (Position) iter.next();
            /*
             * Unlike for the body the Positions associated to the glues generated by
             * TableStepper haven't been removed yet.
             */
            if (pos instanceof TableContentPosition) {
                lst.add((TableContentPosition) pos);
            }
        }
        addTablePartAreas(lst, painter, part, true, true, true, lastOnPage);
    }

    /**
     * Iterates over the positions corresponding to the table's body (which may contain
     * several table-body elements!) and adds the corresponding areas.
     *
     * @param iterator iterator over TableContentPosition elements. Those positions
     * correspond to the elements of the body present on the current page
     * @param painter
     * @param lastOnPage true if the table has no footer (then the last line of the table
     * that will be present on the page belongs to the body)
     */
    private void addBodyAreas(Iterator iterator, RowPainter painter,
            boolean lastOnPage) {
        painter.startBody();
        List lst = new java.util.ArrayList();
        TableContentPosition pos = (TableContentPosition) iterator.next();
        boolean isFirstPos = pos.getFlag(TableContentPosition.FIRST_IN_ROWGROUP)
                && pos.getRow().getFlag(EffRow.FIRST_IN_PART);
        TablePart part = pos.getTablePart();
        lst.add(pos);
        while (iterator.hasNext()) {
            pos = (TableContentPosition) iterator.next();
            if (pos.getTablePart() != part) {
                addTablePartAreas(lst, painter, part, isFirstPos, true, false, false);
                isFirstPos = true;
                lst.clear();
                part = pos.getTablePart();
            }
            lst.add(pos);
        }
        boolean isLastPos = pos.getFlag(TableContentPosition.LAST_IN_ROWGROUP)
                && pos.getRow().getFlag(EffRow.LAST_IN_PART);
        addTablePartAreas(lst, painter, part, isFirstPos, isLastPos, true, lastOnPage);
        painter.endBody();
    }

    /**
     * Adds the areas corresponding to a single fo:table-header/footer/body element.
     */
    private void addTablePartAreas(List positions, RowPainter painter, TablePart body,
            boolean isFirstPos, boolean isLastPos, boolean lastInBody, boolean lastOnPage) {
        getTableLM().getCurrentPV().registerMarkers(body.getMarkers(),
                true, isFirstPos, isLastPos);
        if (body instanceof TableBody) {
            getTableLM().registerMarkers(body.getMarkers(), true, isFirstPos, isLastPos);
        }
        painter.startTablePart(body);
        for (Iterator iter = positions.iterator(); iter.hasNext();) {
            painter.handleTableContentPosition((TableContentPosition) iter.next());
        }
        getTableLM().getCurrentPV().registerMarkers(body.getMarkers(),
                false, isFirstPos, isLastPos);
        if (body instanceof TableBody) {
            getTableLM().registerMarkers(body.getMarkers(), false, isFirstPos, isLastPos);
        }
        painter.endTablePart(lastInBody, lastOnPage);
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
