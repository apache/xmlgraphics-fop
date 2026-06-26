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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.RelativeNumericProperty;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.properties.TableColLength;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.WritingModeTraits;
import org.apache.fop.traits.WritingModeTraitsGetter;

/**
 * Class holding a number of columns making up the column setup of a row.
 */
public class ColumnSetup {

    /** Logger **/
    private static Log log = LogFactory.getLog(ColumnSetup.class);

    private Table table;
    private WritingModeTraitsGetter wmTraits;
    private List<TableColumn> columns = new java.util.ArrayList<TableColumn>();
    private List<Length> colWidths = new java.util.ArrayList<Length>();

    private int maxColIndexReferenced;

    /**
     * Main Constructor.
     * @param table the table to construct this column setup for
     */
    public ColumnSetup(Table table) {
        assert table != null;
        this.table = table;
        this.wmTraits = WritingModeTraits.getWritingModeTraitsGetter(table);
        prepareColumns();
        initializeColumnWidths();
    }

    private void prepareColumns() {
        List<FONode> rawCols = table.getColumns();
        if (rawCols != null) {
            int colnum = 1;
            for (Object rawCol : rawCols) {
                TableColumn col = (TableColumn) rawCol;
                if (col == null) {
                    continue;
                }
                colnum = col.getColumnNumber();
                for (int i = 0; i < col.getNumberColumnsRepeated(); i++) {
                    while (colnum > columns.size()) {
                        columns.add(null);
                    }
                    columns.set(colnum - 1, col);
                    colnum++;
                }
            }
            //Post-processing the list (looking for gaps)
            //TODO The following block could possibly be removed
            int pos = 1;
            for (TableColumn column : columns) {
                if (column == null) {
                    assert false; //Gaps are filled earlier by fo.flow.table.Table.finalizeColumns()
                    //log.error("Found a gap in the table-columns at position " + pos);
                }
                pos++;
            }
        }
    }

    /**
     * Returns a column. If the index of the column is bigger than the number of explicitly
     * defined columns the last column is returned.
     * @param index index of the column (1 is the first column)
     * @return the requested column
     */
    public TableColumn getColumn(int index) {
        int size = columns.size();
        if (index > size) {
            if (index > maxColIndexReferenced) {
                maxColIndexReferenced = index;
                TableColumn col = getColumn(1);
                if (!(size == 1 && col.isImplicitColumn())) {
                    assert false; //TODO Seems to be removable as this is now done in the FO tree
                    log.warn(FONode.decorateWithContextInfo(
                            "There are fewer table-columns than are needed. "
                            + "Column " + index + " was accessed, but only "
                            + size + " columns have been defined. "
                            + "The last defined column will be reused."
                        , table));
                    if (!table.isAutoLayout()) {
                        log.warn("Please note that according XSL-FO 1.0 (7.26.9) says that "
                                + "the 'column-width' property must be specified for every "
                                + "column, unless the automatic table layout is used.");
                    }
                }
            }
            return columns.get(size - 1);
        } else {
            return columns.get(index - 1);
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        return columns.toString();
    }

    /** @return the number of columns in the setup. */
    public int getColumnCount() {
        if (maxColIndexReferenced > columns.size()) {
            return maxColIndexReferenced;
        } else {
            return columns.size();
        }
   }

    /** @return an Iterator over all columns */
    public Iterator<TableColumn> iterator() {
        return this.columns.iterator();
    }

    /*
    private void createColumnsFromFirstRow() {
        //TODO Create oldColumns from first row here
        //--> rule 2 in "fixed table layout", see CSS2, 17.5.2
        //Alternative: extend oldColumns on-the-fly, but in this case we need the
        //new property evaluation context so proportional-column-width() works
        //correctly.
        if (columns.size() == 0) {
            this.columns.add(table.getDefaultColumn());
        }
    }
    */

    /**
     * Initializes the column's widths
     *
     */
    private void initializeColumnWidths() {

        TableColumn col;
        Length colWidth;

        for (int i = columns.size(); --i >= 0;) {
            if (columns.get(i) != null) {
                col = columns.get(i);
                colWidth = col.getColumnWidth();
                colWidths.add(0, colWidth);
            }
        }
        colWidths.add(0, null);
    }

    /**
     * Works out the base unit for resolving proportional-column-width()
     * [p-c-w(x) = x * base_unit_ipd]
     *
     * @param tlm   the TableLayoutManager
     * @param context
     * @return the computed base unit (in millipoint)
     */
    protected double computeTableUnit(TableLayoutManager tlm, LayoutContext context) {
        return computeTableUnit(tlm, tlm.getContentAreaIPD(), context);
    }

    /**
     * Works out the base unit for resolving proportional-column-width()
     * [p-c-w(x) = x * base_unit_ipd]
     *
     * @param percentBaseContext the percent base context for relative values
     * @param contentAreaIPD the IPD of the available content area
     * @param context
     * @return the computed base unit (in millipoints)
     */
    public float computeTableUnit(PercentBaseContext percentBaseContext, int contentAreaIPD, LayoutContext context) {

        int sumCols = 0;
        float factors = 0;
        float unit = 0;

        /* calculate the total width (specified absolute/percentages),
         * and work out the total number of factors to use to distribute
         * the remaining space (if any)
         */
        for (Length colWidth : colWidths) {
            if (colWidth != null) {
                sumCols += colWidth.getValue(percentBaseContext);
                if (colWidth instanceof RelativeNumericProperty) {
                    factors += ((RelativeNumericProperty) colWidth).getTableUnits();
                } else if (colWidth instanceof TableColLength) {
                    factors += ((TableColLength) colWidth).getTableUnits();
                }
            }
        }

        /* distribute the remaining space over the accumulated
         * factors (if any)
         */
        if (factors > 0) {
            if (sumCols < contentAreaIPD) {
                unit = (contentAreaIPD - sumCols) / factors;
            } else {
                // this warning occurs during the pre-processing (AutoLayoutDeterminationMode)
                // and can be ignored in these cases.
                if (percentBaseContext instanceof TableLayoutManager) {
                    if (context.isInAutoLayoutDeterminationMode()) {
                        return unit;
                    }
                }
                log.warn("No space remaining to distribute over columns.");
            }
        }

        return unit;
    }

    /**
     * Determine the X offset of the indicated column, where this
     * offset denotes the left edge of the column irrespective of writing
     * mode. If writing mode's column progression direction is right-to-left,
     * then the first column is the right-most column and the last column is
     * the left-most column; otherwise, the first column is the left-most
     * column.
     * @param col column index (1 is first column)
     * @param nrColSpan number columns spanned (for calculating offset in rtl mode)
     * @param context the context for percentage based calculations
     * @return the X offset of the requested column
     */
    public int getXOffset(int col, int nrColSpan, PercentBaseContext context) {
        // TODO handle vertical WMs [GA]
        if ((wmTraits != null) && (wmTraits.getColumnProgressionDirection() == Direction.RL)) {
            return getXOffsetRTL(col, nrColSpan, context);
        } else {
            return getXOffsetLTR(col, context);
        }
    }

    /*
     * Determine x offset by summing widths of columns to left of specified
     * column; i.e., those columns whose column numbers are greater than the
     * specified column number.
     */
    private int getXOffsetRTL(int col, int nrColSpan, PercentBaseContext context) {
        int xoffset = 0;
        for (int i = (col + nrColSpan - 1), nc = colWidths.size(); ++i < nc;) {
            int effCol = i;
            if (colWidths.get(effCol) != null) {
                xoffset += colWidths.get(effCol).getValue(context);
            }
        }
        return xoffset;
    }

    /*
     * Determine x offset by summing widths of columns to left of specified
     * column; i.e., those columns whose column numbers are less than the
     * specified column number.
     */
    private int getXOffsetLTR(int col, PercentBaseContext context) {
        int xoffset = 0;
        for (int i = col; --i >= 0;) {
            int effCol;
            if (i < colWidths.size()) {
                effCol = i;
            } else {
                effCol = colWidths.size() - 1;
            }
            if (colWidths.get(effCol) != null) {
                xoffset += colWidths.get(effCol).getValue(context);
            }
        }
        return xoffset;
    }

    /**
     * Calculates the sum of all column widths.
     * @param context the context for percentage based calculations
     * @return the requested sum in millipoints
     */
    public int getSumOfColumnWidths(PercentBaseContext context) {
        int sum = 0;
        for (int i = 1, c = getColumnCount(); i <= c; i++) {
            int effIndex = i;
            if (i >= colWidths.size()) {
                effIndex = colWidths.size() - 1;
            }
            if (colWidths.get(effIndex) != null) {
                sum += colWidths.get(effIndex).getValue(context);
            }
        }
        return sum;
    }

    /**
     * Computes for each of the table's columns the optimal width and adjusts each column if necessary.
     * This method relies on the fact, that a column's OPT value is initialized equal to its MAX value.
     *
     * @param tLM the TableLayoutManager
     * @param context
     * @param width the Table width
     * @return int maximum width to be propagated to containing layout manager or -1
     */
    public int computeOptimalColumnWidthsForAutoLayout(TableLayoutManager tLM, LayoutContext context, Length width) {
        int maxSumCols = 0; // collects OPT values of the individual columns
        int minSumCols = 0;
        int contentAreaIPD = tLM.getContentAreaIPD();

        for (TableColumn tcol : columns) {
            if (tcol != null) {
                if (tcol.isAutoLayout()) {
                    MinOptMax possibleWidth = tLM.getPossibleWidths(tcol, context);
                    if (possibleWidth == null) {
                        // this column does not have a PrimaryGridUnit by itself
                        // Just assume that no space is required for such an 'empty' column
                        // TODO: validate this assumption (looks good after rendering it!)
                    } else {
                        maxSumCols += possibleWidth.getOpt();
                        minSumCols += possibleWidth.getMin();
                    }
                } else {
                    int staticWidth = tcol.getColumnWidth().getValue(tLM);
                    maxSumCols += staticWidth;
                    minSumCols += staticWidth;
                }
            }
        }

         /*
          * distribute the remaining space over the accumulated factors (if any)
          */
        // TODO: DO NOT DO THIS IN CASE WE ARE IN AN AUTOMATIC LAYOUT PARENT WHICH NEEDS
        // AUTHENTIC MIN/MAX VALUES TO DETERMINE ITS OWN WIDTH REQUIREMENTS
        if (context.isChildOfAutoLayoutElement() && context.isInAutoLayoutDeterminationMode()) {
            return maxSumCols;
        } else {
            if (maxSumCols > contentAreaIPD) {
                if (minSumCols < contentAreaIPD) {
                    // redistribute by setting OPT values
                    if (log.isDebugEnabled()) {
                        log.debug("Sum (" + maxSumCols + ") > Available Area (" + contentAreaIPD + "): Redistributing");
                    }

                    // create a second list from which we can remove individual items after we are done with them
                    List<TableColumn> columnsToProcess = new ArrayList<TableColumn>();
                    columnsToProcess.addAll(columns);
                    redistribute(tLM, contentAreaIPD, maxSumCols, columnsToProcess, context);
                } else {
                    // set all OPTs to the respective MINIMUM of each column
                    if (minSumCols != contentAreaIPD) {
                        // communicate this case as a warning to the user
                        Table table = tLM.getTable();
                        BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                                table.getUserAgent().getEventBroadcaster());
                        eventProducer.columnsInAutoTableTooWide(this, minSumCols,
                                contentAreaIPD, table.getLocator());
                    }
                    boolean computeAuto = false;
                    for (TableColumn tcol : columns) {
                        if (tcol.isAutoLayout()) {
                            MinOptMax possibleWidth = tLM.getPossibleWidths(tcol, context);
                            if (possibleWidth == null) {
                                // ignore columns which do not contain PGUs -> their width is zero
                            } else {
                                computeAuto = true;
                                int min = possibleWidth.getMin();
                                int max = possibleWidth.getMax();
                                MinOptMax minWidth = MinOptMax.getInstance(min, min, max);
                                tLM.setPossibleWidths(tcol, minWidth);
                            }
                        } else {
                            // DO NOT CHANGE THE OPT-VALUE OF A COLUMN WITH STATIC WIDTH - IT IS
                            // ALREADY THE STATIC VALUE WE MUST USE (AS DEFINED IN THE FO-FILE)
                        }
                    }
                    if (computeAuto) {
                        // table overflows over the max content area. Redistribute the columns with none fixed width
                        // create a second list from which we can remove individual items after we are done with them
                        List<TableColumn> columnsToProcess = new ArrayList<TableColumn>();
                        columnsToProcess.addAll(columns);
                        redistributeAuto(tLM, contentAreaIPD, columnsToProcess, context);
                    }
                }
            } else {
                if (width.getEnum() != Constants.EN_AUTO) {
                    // create a second list from which we can remove individual items after we are done with them
                    List<TableColumn> columnsToProcess = new ArrayList<TableColumn>();
                    columnsToProcess.addAll(columns);
                    redistributeAuto(tLM, contentAreaIPD, columnsToProcess, context);
                }
            }
        }
        return -1;
    }

    /**
     * This method redistributes the remaining width of the table recursively.
     * At first, all static columns are excluded (i.e., the redistribution is invoked
     * for all but these columns), since we cannot shrink them.
     * Afterwards, we try to proportionally shrink each remaining column by a factor of
     * factor = remainingArea / sum of max width of remaining columns
     * After applying this factor to a column's MAX width, we check if the result is less
     * than the column's minimal width - if so, this minimal width is used instead and we
     * invoke the method again with
     * <ul>
     * <li> the remaining columns without the column we just changed</li>
     * <li> the remaining area - the minimal width of the column we just changed</li>
     * <li> an updated factor (based on the remaining area and the remaining columns)</li>
     * </ul>
     *
     * @param tLM              the TableLayoutManager which is used to store the dimensions of all columns
     * @param remainingArea    the remaining width which we may still distribute among the columnsToProcess
     * @param columnsToProcess list of table columns to process
     * @param context          the layout context
     * @return boolean The return value indicates whether the layout changed due to the redistribution.
     */
    private boolean redistribute(TableLayoutManager tLM, int remainingArea, int maxSumCols, List<TableColumn> columnsToProcess, LayoutContext context) {
        double factor = (double) remainingArea / maxSumCols;

        // step 1: check if applying the current factor leads to a value below the minimum of a column
        for (TableColumn tcol : columnsToProcess) {
            // ignore columns which have a static width since we must use their assigned value
            // ignoring them = excluding them from the columns we want to shrink
            if (!tcol.isAutoLayout()) {
                int staticWidth = tcol.getColumnWidth().getValue(tLM);
                remainingArea -= staticWidth;
                maxSumCols -= staticWidth;
                columnsToProcess.remove(tcol);
                if (log.isDebugEnabled()) {
                    log.debug("| Col " + tcol.getColumnNumber() + " -> STATIC(" + staticWidth + ") |");
                }
                return redistribute(tLM, remainingArea, maxSumCols, columnsToProcess, context);
            } else {
                MinOptMax possibleWidth = tLM.getPossibleWidths(tcol, context);
                if (possibleWidth == null) {
                    // no PrimaryGridUnits in this column
                    columnsToProcess.remove(tcol);
                    if (log.isDebugEnabled()) {
                        log.debug("| Col " + tcol.getColumnNumber() + " -> EMPTY (0) |");
                    }
                    return redistribute(tLM, remainingArea, maxSumCols, columnsToProcess, context);
                }
                int max = possibleWidth.getMax();
                int min = possibleWidth.getMin();

                if ((max * factor) < min) {
                    // for this column: opt = min
                    MinOptMax newWidths = MinOptMax.getInstance(min, min, max);
                    tLM.setPossibleWidths(tcol, newWidths);
                    // remove this column from the list, decrease the remaining area (-min), and recalculate the factor
                    remainingArea -= min;
                    maxSumCols -= max;
                    // continue with all other columns which may still be shrinked
                    columnsToProcess.remove(tcol);
                    if (log.isDebugEnabled()) {
                        log.debug("| Col " + tcol.getColumnNumber() + " -> MIN(" + min + ") |");
                    }
                    return redistribute(tLM, remainingArea, maxSumCols, columnsToProcess, context);
                } else {
                    // current column could be shrunk using the current factor
                    // however, subsequent columns might not be -> wait until such columns are sorted out
                }
            }
        }

        // step 2: now we know that all remaining columns can be shrunk by the factor
        for (TableColumn tcol : columnsToProcess) {
            MinOptMax possibleWidth = tLM.getPossibleWidths(tcol, context);
            int max = possibleWidth.getMax();
            int min = possibleWidth.getMin();
            int newOpt = (int) (max * factor);
            if (log.isDebugEnabled()) {
                log.debug("| Col " + tcol.getColumnNumber() + " -> OPT(" + newOpt + ") |");
            }
            MinOptMax newWidths = MinOptMax.getInstance(min, newOpt, max);
            // ASSIGN to column
            tLM.setPossibleWidths(tcol, newWidths);
        }
        if (log.isDebugEnabled()) {
            log.debug("Redistribution finished");
        }
        return true;
    }

    /**
     * This method redistributes the remaining width of the table to honor the table width.
     * At first, all static columns are excluded, since we cannot change them.
     * Afterwards, we try to proportionally increase each remaining column by a factor of
     * factor = column width / sum of max width of remaining columns
     *
     * @param tLM              the TableLayoutManager which is used to store the dimensions of all columns
     * @param maxArea          the table width
     * @param columnsToProcess list of table columns to process
     * @param context          the layout context
     * @return boolean The return value indicates whether the layout changed due to the redistribution.
     */
    private boolean redistributeAuto(TableLayoutManager tLM, int maxArea, List<TableColumn> columnsToProcess, LayoutContext context) {

        int autoWidth = 0;
        int fixedWidth = 0;

        for (TableColumn tcol : columnsToProcess) {
            int width = tcol.getColumnWidth().getValue(tLM);
            if (tcol.isAutoLayout()) {
                autoWidth += width;
            } else {
                fixedWidth += width;
            }
        }

        int remainingArea = maxArea - fixedWidth;

         if (remainingArea > 0) {
            for (TableColumn tcol : columnsToProcess) {
                if (tcol.isAutoLayout()) {
                    MinOptMax possibleWidth = tLM.getPossibleWidths(tcol, context);
                    int max = possibleWidth.getMax();
                    int min = possibleWidth.getMin();
                    double width = tcol.getColumnWidth().getValue(tLM);
                    double factor = width / autoWidth;
                    int newMax =  (int) (factor * remainingArea);
                    MinOptMax newWidths = MinOptMax.getInstance(newMax, newMax, newMax);
                    // ASSIGN to column
                    tLM.setPossibleWidths(tcol, newWidths);
                }
            }
        }
        return true;
    }


}
