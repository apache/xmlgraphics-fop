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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.GridUnit;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.flow.table.TablePart;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.FootenoteUtil;
import org.apache.fop.layoutmgr.FootnoteBodyLayoutManager;
import org.apache.fop.layoutmgr.Keep;
import org.apache.fop.layoutmgr.KnuthBlockBox;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.PageBreaker;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceResolver.SpaceHandlingBreakPosition;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.BreakUtil;

/**
 * Layout manager for table contents, particularly managing the creation of combined element lists.
 */
public class TableContentLayoutManager implements PercentBaseContext {

    /** Logger **/
    private static final Log LOG = LogFactory.getLog(TableContentLayoutManager.class);

    private TableLayoutManager tableLM;
    protected TableRowIterator bodyIter;
    private TableRowIterator headerIter;
    private TableRowIterator footerIter;
    private LinkedList headerList;
    private LinkedList footerList;
    private int headerNetHeight;
    private int footerNetHeight;

    private int startXOffset;
    private int usedBPD;

    private TableStepper stepper;

    private final Map<TableColumn, MinOptMax> baseLength = new HashMap<TableColumn, MinOptMax>();

    private boolean headerIsBeingRepeated;
    private boolean  atLeastOnce;

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
     * assigns a {@link MinOptMax} object to a specific {@link TableColumn}
     * @param key a {@link TableColumn}
     * @param mom a {@link MinOptMax} representing the width requirements of the <code>key</code>
     */
    public void setBaseLength(TableColumn key, MinOptMax mom) {
        this.baseLength.put(key, mom);
    }

    /**
     * returns the {@link MinOptMax} assigned to a table's {@link TableColumn}.
     * @param key a {@link TableColumn}
     * @return {@link MinOptMax} object representing the minimal, optimal and maximum width required for the
     * <code>key</code>
     */
    public final MinOptMax getBaseLength(final FObj key) {
        return this.baseLength.get(key);
    }

    /**
     * Compute and set a set of {@link MinOptMax} widths for a {@link PrimaryGridUnit} (PGU).
     * Now also covers PGUs spanning multiple columns. However, if such a PGU is encountered
     * in the first row already, the table requires a second determination run.
     * @param primary
     * @return
     */
    private boolean setBaseLength(final PrimaryGridUnit primary, LayoutContext context) {
        final Table table = this.tableLM.getTable();
        final int index = primary.getColIndex();
        final int n = index + primary.getCell().getNumberColumnsSpanned();
        final TableColumn key = table.getColumn(index);

        int availableSpanWidth = 0;
        int minSpanWidth = 0;

        int min;
        int span;

        // calculate width (min and opt) of all columns spanned by primary
        for (int i = index; i < n; i++) {
            final TableColumn column = table.getColumn(i);
            span = column.getColumnWidth().getValue(this.tableLM);
            availableSpanWidth += span;

            min = span;
            if (column.isAutoLayout()) {
                final MinOptMax length = getBaseLength(column);
                if (length != null) {
                    min = length.getMin();
                }
            }
            minSpanWidth += min;
        }

        // retrieve the maximum width of the cell's content - problematic if col-span >1 for a static first column?
        int ipd = primary.getCellLM().getRefIPD();

        // retrieve the minimum width of the cell's content - also works for cells spanning columns
        int minIPD = primary.getCellLM().getMinimumIPD();

        final MinOptMax length = getBaseLength(key);
        if ((availableSpanWidth == 0) || (length == null)) {
            // TODO: remove the following IF as soon as the computation of minIPD is corrected
            if (minIPD > ipd) {    // happens e.g. for cells containing a space: ipd=0, minIPD=len(" ")
                ipd = minIPD;
            }
            // |_____c1_____|   <- width for both:    minSpanWidth <= optimal <= availableSpanWidth
            // |__c2__||___c3___| <- width for spanning cell: minIPD <= optimal <= ipd
            MinOptMax initialMinOptMax = MinOptMax.getInstance(minIPD, ipd, ipd);
            this.baseLength.put(key, initialMinOptMax);
        } else {
            if (index == n - 1) {    // a primary without col-span > 1
                if ((availableSpanWidth < ipd) || (length.getMin() < minIPD)) { // cell needs more space

                    if (minIPD > ipd) {
                        ipd = minIPD; // See: fop/test/layoutengine/standard-testcases/table-layout_auto_simple_nested.xml
                    }

                    MinOptMax possibleWidths =
                            MinOptMax.getInstance(
                                    Math.max(length.getMin(), minIPD),
                                    Math.max(length.getOpt(), ipd),
                                    Math.max(length.getMax(), ipd)
                            );
                    return length == this.baseLength.put(key, possibleWidths);
                }
            } else {
                // this primary spans multiple columns which may have to be resized!
                // |__c1__||__c2__|   <- width for both:    minSpanWidth <= optimal <= availableSpanWidth
                // |__c3 (span=2)___| <- width for spanning cell: minIPD <= optimal <= ipd
                // thus, if any of the following booleans are true, the spanned columns need to be widened!
                boolean isNotSufficientForMinIPD = minSpanWidth < minIPD;    // overflow even after linebreaks
                boolean isNotSufficientForIPD = availableSpanWidth < ipd;    // overflow because of more content

                if (isNotSufficientForMinIPD || isNotSufficientForIPD) {
                    // columns spanned by the primary do not offer enough width and, thus, need to
                    // be widened.

                    // first step: ignore static columns which cannot be resized
                    // this includes removing their width from the widths to process
                    List<TableColumn> columnsToWiden = new ArrayList<>();
                    for (Iterator iter = table.getColumns().subList(index, n).iterator(); iter.hasNext();) {
                        TableColumn column = (TableColumn)iter.next();
                        if (column.isAutoLayout()) {    // column can be resized
                            int width = column.getColumnWidth().getValue(this.tableLM);

                            if (tableLM.getPossibleWidths(column, context) == null) {
                                // ignore columns without PrimaryGridUnits
                            } else {
                                columnsToWiden.add(column);
                            }
                        } else {    // column is static and cannot be resized
                            int width = column.getColumnWidth().getValue(this.tableLM);
                            availableSpanWidth -= width;
                            minSpanWidth -= width;
                            ipd -= width;
                            minIPD -= width;
                        }
                    }

                    // true if only static columns are spanned -> no columns left to resize!
                    if (columnsToWiden.isEmpty()) {
                        LOG.warn("No columns to resize to fit a table-cell spanning these columns, expect overflows");
                        return false;
                    }

                    // minimal width reserved by the spanned columns insufficient -> resize
                    if (minSpanWidth < minIPD) {
                        if (LOG.isDebugEnabled()) {
                            LOG.warn("Cell (" + primary.getColIndex() + "," + primary.getRowIndex() + ") spanning "
                                    + primary.getCell().getNumberColumnsSpanned() + " columns requires at least "
                                    + minIPD + " -> widening MIN/OPT/MAX its spanned columns: " + columnsToWiden);
                        }

                        int totalIncrease = increaseMinimumWidthOfSpannedColumns(columnsToWiden, minSpanWidth, minIPD, context);
                        // resizing the columns led to additional space being reserved by these columns!
                        availableSpanWidth += totalIncrease;
                    }

                    // maximum width reserved by the spanned columns insufficient -> resize
                    if (availableSpanWidth < ipd) {
                        if (LOG.isDebugEnabled()) {
                            LOG.warn("Cell (" + primary.getColIndex() + "," + primary.getRowIndex() + ") spanning "
                                    + primary.getCell().getNumberColumnsSpanned() + " columns requires up to "
                                    + ipd + " -> widening OPT/MAX of its spanned columns: " + columnsToWiden);
                        }
                        increaseOptimalWidthOfSpannedColumns(columnsToWiden, availableSpanWidth, ipd);
                    }
                }

            }
        }
        return false;
    }

    /**
     * Takes a set of columns (minSpanWidthOfSpannedCells = sum of their minIPDs) which are spanned
     * by the cell we are currently processing. Since this current cell requires a wider minIPD than
     * all spanned columns combined, this method increases the min. width of these columns proportionally
     * in such a way that the sum of their min. widths is >= the minIPD of the current cell.<br>
     * Please note that for each column, all three values of its {@link MinOptMax} are increased accordingly.
     * After all columns were processed and widened, the sum of additional space reserved by these columns
     * is returned.
     * @param columnsToWiden set of non-static columns which can be resized
     * @param minSpanWidthOfSpannedCells sum of the minIPDs of the columns in columnsToWiden
     * @param minIPD minimal width required by the current cell
     * @param context
     * @return the total amount of width which was added to the columns in columnsToWiden
     */
    private int increaseMinimumWidthOfSpannedColumns(List columnsToWiden, int minSpanWidthOfSpannedCells, int minIPD, LayoutContext context) {
        int totalIncrease = 0;

        for (Iterator iter = columnsToWiden.iterator(); iter.hasNext();) {
            final TableColumn column = (TableColumn) iter.next();
            MinOptMax length = tableLM.getPossibleWidths(column, context);

            // calculate factor for increase of width
            double factor = (double)length.getMin() / minSpanWidthOfSpannedCells;

            // how much more space is required to display the spanning cell
            int totalMissingMinSpace = minIPD - minSpanWidthOfSpannedCells;

            int increaseForMinimum = (int) Math.ceil(factor * totalMissingMinSpace);

            MinOptMax newMom =
                    MinOptMax.getInstance(
                            length.getMin() + increaseForMinimum,
                            length.getOpt() + increaseForMinimum,
                            length.getMax() + increaseForMinimum
                    );
            setBaseLength(column, newMom);
            totalIncrease += increaseForMinimum;
        }
        return totalIncrease;
    }

    /**
     * takes a set of columns (<b>columnsToWiden</b>) spanned by one cell (represented via a
     * {@link PrimaryGridUnit}) and increases their minimum width value (in case the spanning cell's
     * minIPD is bigger than the sum of
     * goes through a subset of the columns
     * @param columnsToWiden
     * @param availableWidth
     * @param requiredWidth
     * @return
     */
    private boolean increaseOptimalWidthOfSpannedColumns(List columnsToWiden, int availableWidth, int requiredWidth) {
        for (Iterator iter = columnsToWiden.iterator(); iter.hasNext();) {
            final TableColumn column = (TableColumn) iter.next();
            MinOptMax length = getBaseLength(column);

            // calculate factor for increase of width
            double factor = (double) length.getOpt() / availableWidth;

            // how much more space is required to display the spanning cell
            int totalMissingMaxSpace = requiredWidth - availableWidth;

            // ensure the content will fit by getting the ceiling of the product
            int increase = (int) Math.ceil(factor * totalMissingMaxSpace);
            MinOptMax newMom =
                    MinOptMax.getInstance(
                            length.getMin(),
                            length.getOpt() + increase,
                            length.getMax() + increase
                    );
            setBaseLength(column, newMom);
        }
        return false;
    }

    private boolean setBaseLength(final TableContentPosition position, LayoutContext context) {
        boolean done = false;
        final EffRow row = position.getRow();
        final Iterator grid = row.getGridUnits().iterator();

        while (grid.hasNext()) {
            final GridUnit unit = (GridUnit) grid.next();

            if (unit instanceof PrimaryGridUnit) {
                done = setBaseLength((PrimaryGridUnit) unit, context) || done;
            }
        }

        return done;
    }

    private boolean setBaseLength(final Iterator content,LayoutContext context) {
        boolean done = false;

        while (content.hasNext()) {
            final ListElement element = (ListElement) content.next();
            final Position position = element.getPosition();

            if (position instanceof TableContentPosition) {
                done = setBaseLength((TableContentPosition) position, context) || done;
            }
        }

        return done;
    }

    private boolean setBaseLength(final List content, LayoutContext context) {
        final Table table = this.tableLM.getTable();
        return table.isAutoLayout() && setBaseLength(content.iterator(), context);
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
    public List<ListElement> getNextKnuthElements(LayoutContext context, int alignment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> Columns: " + getTableLM().getColumns());
        }
        KnuthBox headerAsFirst = null;
        KnuthBox headerAsSecondToLast = null;
        KnuthBox footerAsLast = null;
        LinkedList<ListElement> returnList = new LinkedList<>();
        int headerFootnoteBPD = 0;
        if (headerIter != null && headerList == null) {
            this.headerList = getKnuthElementsForRowIterator(
                    headerIter, context, alignment, TableRowIterator.HEADER);

            setBaseLength(this.headerList, context);
            /* NOT sure why we need to recreate header iterator, this can lead to an endless loop since we get the same
               table over and over again!
            if (setBaseLength(this.headerList, context)) {
                final Table table = this.tableLM.getTable();
                this.headerIter = new TableRowIterator(table, TableRowIterator.HEADER);
                this.headerList = null;
                return getNextKnuthElements(context, alignment);
            }
            */

            this.headerNetHeight
                    = ElementListUtils.calcContentLength(this.headerList);
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> Header: "
                        + headerNetHeight + " - " + this.headerList);
            }
            TableHeaderFooterPosition pos = new TableHeaderFooterPosition(
                    getTableLM(), true, this.headerList);
            List<FootnoteBodyLayoutManager> footnoteList = FootenoteUtil.getFootnotes(headerList);
            KnuthBox box = (footnoteList.isEmpty() || !getTableLM().getTable().omitHeaderAtBreak())
                    ? new KnuthBox(headerNetHeight, pos, false)
                    : new KnuthBlockBox(headerNetHeight, footnoteList, pos, false);
            if (getTableLM().getTable().omitHeaderAtBreak()) {
                //We can simply add the table header at the start
                //of the whole list
                headerAsFirst = box;
            } else {
                if (!footnoteList.isEmpty()) {
                    List<List<KnuthElement>> footnotes = PageBreaker.getFootnoteKnuthElements(
                            getTableLM().getPSLM().getFlowLayoutManager(), context, footnoteList);
                    getTableLM().setHeaderFootnotes(footnotes);
                    headerFootnoteBPD = getFootnotesBPD(footnotes);
                    returnList.add(new KnuthBlockBox(-headerFootnoteBPD, footnoteList,
                            new Position(getTableLM()), true));
                    headerNetHeight += headerFootnoteBPD;
                }
                headerAsSecondToLast = box;
            }
        }
        if (footerIter != null && footerList == null) {
            this.footerList = getKnuthElementsForRowIterator(
                    footerIter, context, alignment, TableRowIterator.FOOTER);

            setBaseLength(this.footerList, context);
            /* NOT sure why we need to recreate footer iterator, this can lead to an endless loop since we get the same
               table over and over again!
            if (setBaseLength(this.footerList, context)) {
                final Table table = this.tableLM.getTable();

                if (this.headerIter != null) {
                    this.headerIter = new TableRowIterator(table, TableRowIterator.HEADER);
                    this.headerList = null;
                }

                this.footerIter = new TableRowIterator(table, TableRowIterator.FOOTER);
                this.footerList = null;
                return getNextKnuthElements(context, alignment);
            }
            */

            this.footerNetHeight
                    = ElementListUtils.calcContentLength(this.footerList);
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> Footer: "
                        + footerNetHeight + " - " + this.footerList);
            }
            //We can simply add the table footer at the end of the whole list
            TableHeaderFooterPosition pos = new TableHeaderFooterPosition(
                    getTableLM(), false, this.footerList);
            List<FootnoteBodyLayoutManager> footnoteList = FootenoteUtil.getFootnotes(footerList);
            footerAsLast = footnoteList.isEmpty()
                    ? new KnuthBox(footerNetHeight, pos, false)
                    : new KnuthBlockBox(footerNetHeight, footnoteList, pos, false);
            if (!(getTableLM().getTable().omitFooterAtBreak() || footnoteList.isEmpty())) {
                List<List<KnuthElement>> footnotes = PageBreaker.getFootnoteKnuthElements(
                        getTableLM().getPSLM().getFlowLayoutManager(), context, footnoteList);
                getTableLM().setFooterFootnotes(footnotes);
                footerNetHeight += getFootnotesBPD(footnotes);
            }
        }
        returnList.addAll(getKnuthElementsForRowIterator(
                bodyIter, context, alignment, TableRowIterator.BODY));

        setBaseLength(returnList, context);
        /* NOT sure why we need to recreate table iterators, this can lead to an endless loop since we get the same
           table over and over again!
           causes java.lang.StackOverflowError: see fop/test/layoutengine/standard-testcases/table-layout_auto_nested_2.xml
        if (setBaseLength(returnList, context)) {
            final Table table = this.tableLM.getTable();

            if (this.headerIter != null) {
                this.headerIter = new TableRowIterator(table, TableRowIterator.HEADER);
                this.headerList = null;
            }

            if (this.footerIter != null) {
                this.footerIter = new TableRowIterator(table, TableRowIterator.FOOTER);
                this.footerList = null;
            }

            this.bodyIter = new TableRowIterator(table, TableRowIterator.BODY);
            return getNextKnuthElements(context, alignment);
        }
           */

        if (headerAsFirst != null) {
            int insertionPoint = 0;
            if (returnList.size() > 0 && returnList.getFirst().isForcedBreak()) {
                insertionPoint++;
            }
            returnList.add(insertionPoint, headerAsFirst);
        } else if (headerAsSecondToLast != null) {
            int insertionPoint = returnList.size();
            if (returnList.size() > 0 && returnList.getLast().isForcedBreak()) {
                insertionPoint--;
            }
            returnList.add(insertionPoint, headerAsSecondToLast);
        }
        if (footerAsLast != null) {
            int insertionPoint = returnList.size();
            if (returnList.size() > 0 && returnList.getLast().isForcedBreak()) {
                insertionPoint--;
            }
            returnList.add(insertionPoint, footerAsLast);
        }
        if (headerFootnoteBPD != 0) {
            returnList.add(new KnuthBox(headerFootnoteBPD, new Position(getTableLM()), true));
        }
        return returnList;
    }

    private int getFootnotesBPD(List<List<KnuthElement>> footnotes) {
        int bpd = 0;
        for (List<KnuthElement> footnote : footnotes) {
            bpd += ElementListUtils.calcContentLength(footnote);
        }
        return bpd;
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
        LinkedList<ListElement> returnList = new LinkedList<>();
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
            List<ListElement> nextRowGroupElems = rowGroupLM.getNextKnuthElements(context, alignment, bodyType);
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
        if (!returnList.isEmpty()) {
            ListIterator elemIter = returnList.listIterator(returnList.size());
            ListElement elem = (ListElement) elemIter.previous();
            if (elem instanceof KnuthGlue) {
                BreakElement breakElement = (BreakElement) elemIter.previous();
                breakElement.setPenaltyValue(KnuthElement.INFINITE);
            } else {
                elemIter.remove();
            }
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

        List<Position> tablePositions = new java.util.ArrayList<>();
        List<ListElement> headerElements = null;
        List<ListElement> footerElements = null;
        Position firstPos = null;
        Position lastPos = null;
        Position lastCheckPos = null;
        while (parentIter.hasNext()) {
            Position pos = parentIter.next();
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
                if (!getTableLM().getHeaderFootnotes().isEmpty()) {
                    getTableLM().getPSLM().addTableHeaderFootnotes(getTableLM().getHeaderFootnotes());
                }
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
        tableLM.setRepeateHeader(atLeastOnce);
        tableLM.repeatAddAreasForSavedTableHeaderTableCellLayoutManagers();
        atLeastOnce = true;

        if (footerElements != null && !footerElements.isEmpty()) {
            boolean ancestorTreatAsArtifact = layoutContext.treatAsArtifact();
            layoutContext.setTreatAsArtifact(treatFooterAsArtifact);
            //Positions for footers are simply added at the end
            addHeaderFooterAreas(footerElements, tableLM.getTable().getTableFooter(), painter, true);
            if (lastPos instanceof TableHFPenaltyPosition && !tableLM.getFooterFootnotes().isEmpty()) {
                tableLM.getPSLM().addTableFooterFootnotes(getTableLM().getFooterFootnotes());
            }
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
        List<Position> lst = new java.util.ArrayList<>(elements.size());
        for (Iterator iter = new KnuthPossPosIter(elements); iter.hasNext();) {
            Position pos = (Position) iter.next();
            /*
             * Unlike for the body the Positions associated to the glues generated by
             * TableStepper haven't been removed yet.
             */
            if (pos instanceof TableContentPosition) {
                lst.add(pos);
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
        List<TableContentPosition> lst = new java.util.ArrayList<>();
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
        for (Object position : positions) {
            painter.handleTableContentPosition((TableContentPosition) position);
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

    /**
     * essentially, do the same things as {@link TableContentLayoutManager#getNextKnuthElements(LayoutContext, int)},
     * but only do the bare minimum required to get the {@link MinOptMax} values for each column of the
     * table with automatic layout. Thus, this computation must not have any side effects on the/any
     * subsequent call to {@link TableContentLayoutManager#getNextKnuthElements(LayoutContext, int)}.
     * @param context
     * @param alignment
     */
    public void determineAutoLayoutWidths(LayoutContext context, int alignment) {
        List<PrimaryGridUnit> colspanningPGUs = new LinkedList<PrimaryGridUnit>();
        Table table = getTableLM().getTable();

        TableRowIterator tempbodyIter = new TableRowIterator(table, TableRowIterator.BODY);
        TableRowIterator tempheaderIter = null;
        TableRowIterator tempfooterIter = null;

        if (table.getTableHeader() != null) {
            tempheaderIter = new TableRowIterator(table, TableRowIterator.HEADER);
            iterateOverTableRows(tempheaderIter, context, alignment, TableRowIterator.HEADER, colspanningPGUs);
        }

        iterateOverTableRows(tempbodyIter, context, alignment, TableRowIterator.BODY, colspanningPGUs);

        if (table.getTableFooter() != null) {
            tempfooterIter = new TableRowIterator(table, TableRowIterator.FOOTER);
            iterateOverTableRows(tempfooterIter, context, alignment, TableRowIterator.FOOTER, colspanningPGUs);
        }

        for (PrimaryGridUnit primary : colspanningPGUs) {
            determineWidthOfPrimary(primary, context, alignment);
        }
    }

    /**
     * To be used only during the preprocessing run which determines the dimensions of Tables with table-layout="auto".
     * Iterates over all rows of the provided iterator (either for the header, footer or body of a table depending
     * on the parameter <code>bodyType</code>). For each row, the contained {@link PrimaryGridUnit}s are taken to
     * determine their widths based on their content. These widths are then propagated to the individual
     * {@link TableColumn}s via {@link #setBaseLength(PrimaryGridUnit)}. Afterwards, the {@link PrimaryGridUnit}'s
     * elements reset so that they are properly processed during the rendering run (this is necessary since the
     * dimensions obtained for a column are still subject to changes). <br>
     * Based on
     * {@link TableContentLayoutManager#getKnuthElementsForRowIterator(TableRowIterator, LayoutContext, int, int)}
     * However, since we are only interested in the widths of the contained PGUs, most of the original method was
     * removed.
     * @param iter Iterator providing access to rows which belong either to the table's body, header or footer
     * @param context layout context
     * @param alignment
     * @param bodyType indicates which part of a table is processed (actually not required)
     */
    private void iterateOverTableRows(TableRowIterator iter,    // returns indiv. rows
                                      LayoutContext context, int alignment, int bodyType,
                                      List<PrimaryGridUnit> colspanningPGUs) {
        EffRow[] rowGroup;
        while ((rowGroup = iter.getNextRowGroup()) != null) {
            //RowGroupLayoutManager rowGroupLM = new RowGroupLayoutManager(getTableLM(), rowGroup,
            //        null);    // the actual tablestepper might lead to undesired side effects!
            /**
             * based on RowGroupLayoutManager#createElementsForRowGroup
             * initializes the PGUs of one row at a time
             */
            for (EffRow row : rowGroup) {
                for (GridUnit gu : row.getGridUnits()) {
                    if (gu.isPrimary()) {
                        PrimaryGridUnit primary = gu.getPrimary();

                        // during this iteration, the width of PGUs in entries which span multiple columns
                        // cannot be determined
                        if (primary.getCell().getNumberColumnsSpanned() > 1) {
                            LOG.debug("Will revisit later");
                            colspanningPGUs.add(primary);
                        } else {
                            determineWidthOfPrimary(primary, context, alignment);
                        }
                    }
                }
            }
        }
    }

    private void determineWidthOfPrimary(PrimaryGridUnit primary, LayoutContext context, int alignment) {
        // recursively retrieve (and thereby calculate the dimensions of)
        // all KnuthElements of all contained LayoutManagers for the given cell
        primary.createCellLM();
        TableCellLayoutManager cellLM = primary.getCellLM();
        cellLM.setParent(tableLM);
        //Calculate width of cell
        int spanWidth = 0;
        Iterator<TableColumn> colIter = tableLM.getTable().getColumns().stream()
                .map(c -> (TableColumn) c)
                .collect(Collectors.toList())
                .listIterator(primary.getColIndex());
        for (int i = 0, c = primary.getCell().getNumberColumnsSpanned(); i < c; i++) {
            spanWidth += colIter.next().getColumnWidth().getValue(tableLM);
        }

        LayoutContext childLC = LayoutContext.offspringOf(context);
        childLC.setStackLimitBP(context.getStackLimitBP());
        childLC.setRefIPD(spanWidth);

        /* Works fine.  See: fop/test/layoutengine/standard-testcases/table-layout_auto_single_column.xml
        // TODO: ugly workaround to deal with one-column tables which would be rendered broken otherwise
        if (tableLM.getTable().getColumns().size() == 1) {
            childLC.setRefIPD(context.getRefIPD());
        } else {
            childLC.setRefIPD(spanWidth);
        }
        */

        //Get the element list for the cell contents
        List elems = cellLM.getNextKnuthElements(childLC, alignment);
        // temporarily assign these KnuthElements to the PGU to calculate its dimensions
        primary.setElements(elems);
        setBaseLength(primary, context);

        // reset the PGU (and thereby reset (even destroy?) all contained LayoutManagers)
        // the dimensions, however, are still present in form of a MinOptMax!
        primary.setElements(null);
    }

}
