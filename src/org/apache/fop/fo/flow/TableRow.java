/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Property;
import org.apache.fop.fo.Status;
import org.apache.fop.fo.properties.Constants;
import org.apache.fop.fo.properties.Position;
import org.apache.fop.fo.properties.BreakAfter;
import org.apache.fop.datatypes.KeepValue;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.layout.AreaContainer;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;

public class TableRow extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                        String systemId, int line, int column)
            throws FOPException {
            return new TableRow(parent, propertyList, systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new TableRow.Maker();
    }

    boolean setup = false;

    int breakAfter;
    String id;

    KeepValue keepWithNext;
    KeepValue keepWithPrevious;
    KeepValue keepTogether;

    int widthOfCellsSoFar = 0;
    int largestCellHeight = 0;
    int minHeight = 0;    // force row height
    ArrayList columns;

    // public AreaContainer areaContainer;
    public java.lang.ref.WeakReference areaContainerRef;

    boolean areaAdded = false;

    boolean bIgnoreKeepTogether = false;

    private RowSpanMgr rowSpanMgr = null;
    private CellArray cellArray = null;

    private static class CellArray {
        public static final byte EMPTY = 0;
        public static final byte CELLSTART = 1;
        public static final byte CELLSPAN = 2;

        private TableCell[] cells;
        private byte[] states;

        public CellArray(RowSpanMgr rsi, int numColumns) {
            // Initialize the cell array by marking any cell positions
            // occupied by spans from previous rows
            cells = new TableCell[numColumns];
            states = new byte[numColumns];
            for (int i = 0; i < numColumns; i++) {
                if (rsi.isSpanned(i + 1)) {
                    cells[i] = rsi.getSpanningCell(i + 1);
                    states[i] = CELLSPAN;
                } else
                    states[i] = EMPTY;
            }
        }

        /**
         * Return column which doesn't already contain a span or a cell
         * If past the end or no free cells after colNum, return -1
         * Otherwise return value >= input value.
         */
        int getNextFreeCell(int colNum) {
            for (int i = colNum - 1; i < states.length; i++) {
                if (states[i] == EMPTY)
                    return i + 1;
            }
            return -1;
        }


        /**
         * Return type of cell in colNum (1 based)
         */
        int getCellType(int colNum) {
            if (colNum > 0 && colNum <= cells.length) {
                return states[colNum - 1];
            } else
                return -1;    // probably should throw exception
        }

        /**
         * Return cell in colNum (1 based)
         */
        TableCell getCell(int colNum) {
            if (colNum > 0 && colNum <= cells.length) {
                return cells[colNum - 1];
            } else
                return null;    // probably should throw exception
        }

        /**
         * Store cell starting at cellColNum (1 based) and spanning numCols
         * If any of the columns is already occupied, return false, else true
         */
        boolean storeCell(TableCell cell, int colNum, int numCols) {
            boolean rslt = true;
            int index = colNum - 1;
            for (int count = 0; index < cells.length && count < numCols;
                    count++, index++) {
                if (cells[index] == null) {
                    cells[index] = cell;
                    states[index] = (count == 0) ? CELLSTART : CELLSPAN;
                } else {
                    rslt = false;
                    // print a message but continue!!!
                }
            }
            return rslt;
        }
    }


    public TableRow(FObj parent, PropertyList propertyList,
                    String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);
        if (!(parent instanceof AbstractTableBody)) {
            throw new FOPException("A table row must be child of fo:table-body,"
                                   + " fo:table-header or fo:table-footer, not "
                                   + parent.getName(), systemId, line, column);
        }
    }

    public String getName() {
        return "fo:table-row";
    }

    public void setColumns(ArrayList columns) {
        this.columns = columns;
    }

    public KeepValue getKeepWithPrevious() {
        return keepWithPrevious;
    }

    public void doSetup(Area area) throws FOPException {

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // this.properties.get("block-progression-dimension");

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        // only background apply, border apply if border-collapse
        // is collapse.
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

        // this.properties.get("break-before");
        // this.properties.get("break-after");
        // this.properties.get("id");
        // this.properties.get("height");
        // this.properties.get("keep-together");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");


        this.breakAfter = this.properties.get("break-after").getEnum();

        this.keepTogether = getKeepValue("keep-together.within-column");
        this.keepWithNext = getKeepValue("keep-with-next.within-column");
        this.keepWithPrevious =
            getKeepValue("keep-with-previous.within-column");

        this.id = this.properties.get("id").getString();
        this.minHeight = this.properties.get("height").getLength().mvalue();
        setup = true;
    }

    private KeepValue getKeepValue(String sPropName) {
        Property p = this.properties.get(sPropName);
        Number n = p.getNumber();
        if (n != null)
            return new KeepValue(KeepValue.KEEP_WITH_VALUE, n.intValue());
        switch (p.getEnum()) {
        case Constants.ALWAYS:
            return new KeepValue(KeepValue.KEEP_WITH_ALWAYS, 0);
        // break;
        case Constants.AUTO:
        default:
            return new KeepValue(KeepValue.KEEP_WITH_AUTO, 0);
        // break;
        }
    }

    public int layout(Area area) throws FOPException {
        if (this.marker == BREAK_AFTER) {
            return Status.OK;
        }

        // Layout the first area for this FO
        if (this.marker == START) {
            if (!setup)
                doSetup(area);

                // Only do this once. If the row is "thrown" and we start
                // layout over again, we can skip this.
            if (cellArray == null) {
                initCellArray();
                // check to make sure this row hasn't been partially
                // laid out yet (with an id created already)
            }
            // create ID also in case the row has been reset
            try {
                area.getIDReferences().createID(id);
            }
            catch(FOPException e) {
                if (!e.isLocationSet()) {
                    e.setLocation(systemId, line, column);
                }
                throw e;
            }

            this.marker = 0;
            int breakStatus = propMgr.checkBreakBefore(area);
            if (breakStatus != Status.OK)
                return breakStatus;
        }

        if (marker == 0) {    // KDL: need to do this if thrown or if split?
            // configure id
            area.getIDReferences().configureID(id, area);
        }

        int spaceLeft = area.spaceLeft();

        AreaContainer areaContainer =
            new AreaContainer(propMgr.getFontState(area.getFontInfo()), 0, 0,
                              area.getContentWidth(), spaceLeft,
                              Position.RELATIVE);
        areaContainer.foCreator = this;    // G Seshadri
        areaContainer.setPage(area.getPage());
        areaContainer.setParent(area);

        areaContainer.setBackground(propMgr.getBackgroundProps());
        areaContainer.start();

        areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
        areaContainer.setIDReferences(area.getIDReferences());

        this.areaContainerRef = new java.lang.ref.WeakReference(areaContainer);

        largestCellHeight = minHeight;

        // Flag indicaing whether any cell didn't fit in available space
        boolean someCellDidNotLayoutCompletely = false;

        /*
         * If it takes multiple calls to completely layout the row,
         * we need to process all of the children (cells)
         * not just those from the marker so that the borders
         * will be drawn properly.
         */
        int offset = 0;       // Offset of each cell from table start edge
        int iColIndex = 0;    // 1-based column index
        /*
         * Ideas: set offset on each column when they are initialized
         * no need to calculate for each row.
         * Pass column object to cell to get offset and width and border
         * info if borders are "collapsed".
         */
        for (int i = 0; i < columns.size(); i++) {
            TableCell cell;
            ++iColIndex;
            TableColumn tcol = (TableColumn)columns.get(i);
            int colWidth = tcol.getColumnWidth();
            if (cellArray.getCellType(iColIndex) == CellArray.CELLSTART) {
                cell = cellArray.getCell(iColIndex);
            } else {
                /*
                 * If this cell is spanned from a previous row,
                 * and this is the last row, get the remaining height
                 * and use it to increase maxCellHeight if necessary
                 */
                if (rowSpanMgr.isInLastRow(iColIndex)) {
                    int h = rowSpanMgr.getRemainingHeight(iColIndex);
                    if (h > largestCellHeight)
                        largestCellHeight = h;
                }
                offset += colWidth;
                continue;
            }
            // cell.setTableColumn(tcol);
            cell.setStartOffset(offset);
            offset += colWidth;


            int rowSpan = cell.getNumRowsSpanned();
            int status;
            if (Status.isIncomplete((status = cell.layout(areaContainer)))) {
               if ((keepTogether.getType() == KeepValue.KEEP_WITH_ALWAYS && bIgnoreKeepTogether==false)
                        || (status == Status.AREA_FULL_NONE)
                        || rowSpan > 1) {
                    // We will put this row into the next column/page
                    // Note: the only time this shouldn't be honored is
                    // if this row is at the top of the column area.
                    // Remove spanning cells from RowSpanMgr?
                    this.resetMarker();
                    this.removeID(area.getIDReferences());
                    return Status.AREA_FULL_NONE;
                } else if (status == Status.AREA_FULL_SOME) {
                    /*
                     * Row is not keep-together, cell isn't spanning
                     * and part of it fits. We can break the cell and
                     * the row.
                     */
                    someCellDidNotLayoutCompletely = true;
                }
            }                            // else {
             // layout was complete for a particular cell
            int h = cell.getHeight();    // allocation height of cell
            if (rowSpan > 1) {           // pass cell fo or area???
                rowSpanMgr.addRowSpan(cell, iColIndex,
                                      cell.getNumColumnsSpanned(), h,
                                      rowSpan);
            } else if (h > largestCellHeight) {
                largestCellHeight = h;
            }
            // }
        }                                // end of loop over all columns/cells

        // This is in case a float was composed in the cells
        area.setMaxHeight(area.getMaxHeight() - spaceLeft
                          + areaContainer.getMaxHeight());

        // Only do this for "STARTCELL", ending spans are handled separately
        // What about empty cells? Yes, we should set their height too!
        for (int iCol = 1; iCol <= columns.size(); iCol++) {
            if (cellArray.getCellType(iCol) == CellArray.CELLSTART
                    && rowSpanMgr.isSpanned(iCol) == false) {
                cellArray.getCell(iCol).setRowHeight(largestCellHeight);
            }
        }

        // Adjust spanning row information
        // ??? what if some cells are broken???
        rowSpanMgr.finishRow(largestCellHeight);

        area.addChild(areaContainer);
        areaContainer.setHeight(largestCellHeight);
        areaAdded = true;
        areaContainer.end();

        /*
         * The method addDisplaySpace increases both the content
         * height of the parent area (table body, head or footer) and
         * also its "absolute height". So we don't need to do this
         * explicitly.
         *
         * Note: it doesn't look from the CR as though we should take
         * into account borders and padding on rows, only background.
         * The exception is perhaps if the borders are "collapsed", but
         * they should still be rendered only on cells and not on the
         * rows themselves. (Karen Lease - 01may2001)
         */
        area.addDisplaySpace(largestCellHeight
                             + areaContainer.getPaddingTop()
                             + areaContainer.getBorderTopWidth()
                             + areaContainer.getPaddingBottom()
                             + areaContainer.getBorderBottomWidth());


        // replaced by Hani Elabed 11/27/2000
        // return new Status(Status.OK);

        if (someCellDidNotLayoutCompletely) {
            return Status.AREA_FULL_SOME;
        } else {
            if (rowSpanMgr.hasUnfinishedSpans()) {
                // Ignore break after if row span!
                return Status.KEEP_WITH_NEXT;
            }
            if (breakAfter == BreakAfter.PAGE) {
                this.marker = BREAK_AFTER;
                return Status.FORCE_PAGE_BREAK;
            }

            if (breakAfter == BreakAfter.ODD_PAGE) {
                this.marker = BREAK_AFTER;
                return Status.FORCE_PAGE_BREAK_ODD;
            }

            if (breakAfter == BreakAfter.EVEN_PAGE) {
                this.marker = BREAK_AFTER;
                return Status.FORCE_PAGE_BREAK_EVEN;
            }

            if (breakAfter == BreakAfter.COLUMN) {
                this.marker = BREAK_AFTER;
                return Status.FORCE_COLUMN_BREAK;
            }
            if (keepWithNext.getType() != KeepValue.KEEP_WITH_AUTO) {
                return Status.KEEP_WITH_NEXT;
            }
            return Status.OK;
        }

    }

    public int getAreaHeight() {
        return ((AreaContainer)areaContainerRef.get()).getHeight();
    }

    public void removeLayout(Area area) {
        if (areaAdded) {
            area.removeChild((AreaContainer)areaContainerRef.get());
            areaAdded = false;
        }
        this.resetMarker();
        this.removeID(area.getIDReferences());
    }

    public void resetMarker() {
        super.resetMarker();
        // Just reset all the states to not laid out and fix up row spans
    }

    /**
     * Called by parent FO to initialize information about
     * cells started in previous rows which span into this row.
     * The layout operation modifies rowSpanMgr
     */
    public void setRowSpanMgr(RowSpanMgr rowSpanMgr) {
        this.rowSpanMgr = rowSpanMgr;
    }

    /**
     * Before starting layout for the first time, initialize information
     * about spanning rows, empty cells and spanning columns.
     */
    private void initCellArray() throws FOPException {
        cellArray = new CellArray(rowSpanMgr, columns.size());
        int colNum = 1;
        for (int i = 0; i< children.size(); i++) {
            colNum = cellArray.getNextFreeCell(colNum);
            // If off the end, the rest of the cells had better be
            // explicitly positioned!!! (returns -1)

            TableCell cell = (TableCell)children.get(i);
            int numCols = cell.getNumColumnsSpanned();
            int numRows = cell.getNumRowsSpanned();
            int cellColNum = cell.getColumnNumber();

            if (cellColNum == 0) {
                // Not explicitly specified, so put in next available colummn
                // cell.setColumnNumber(colNum);
                // If cellColNum "off the end", this cell is in limbo!
                if (colNum < 1) {
                    throw new FOPException("Cell (#"+i+") implicitely positioned beyond number of columns", cell.getSystemId(), cell.getLine(), cell.getColumn());
                } else
                    cellColNum = colNum;
            } else if (cellColNum > columns.size()) {
                // Explicit colomn number specification out of range, skip it.
                log.error(" "+systemId+':'+cell.getLine()+':'+cell.getColumn()+": Cell (#"+ i +") explicitely positioned beyond number of columns, dropped");
                continue;
            }
            // see if it fits and doesn't overwrite anything
            if (cellColNum + numCols - 1 > columns.size()) {
                // Too many columns spanned.
                log.error(" "+systemId+':'+cell.getLine()+':'+cell.getColumn()+": Cell (#"+i+") spans columns beyond available number, clipped");
                numCols = columns.size() - cellColNum + 1;
            }
            // Check for overwriting other cells (returns false)
            if (cellArray.storeCell(cell, cellColNum, numCols) == false) {
                log.error(" "+systemId+':'+cell.getLine()+':'+cell.getColumn()+": Cell +(#"+i+") overwrites other cells");
            }
            if (cellColNum > colNum) {
                // Cells are initialized as empty already
                colNum = cellColNum;
            } else if (cellColNum < colNum) {
                // Cells out of order
                colNum = cellColNum;    // CR "to the letter"!
                log.debug(" "+systemId+':'+line+':'+column+": Cell positioned out of order");
            }
            int cellWidth = getCellWidth(cellColNum, numCols);
            cell.setWidth(cellWidth);
            colNum += numCols;          // next cell in this column
        }
    }

    // ATTENTION if startCol + numCols > number of columns in table!
    private int getCellWidth(int startCol, int numCols) {
        int width = 0;
        for (int count = 0; count < numCols; count++) {
            width += ((TableColumn)columns.get(startCol + count
                                                     - 1)).getColumnWidth();
        }
        return width;
    }

    void setIgnoreKeepTogether(boolean bIgnoreKeepTogether) {
        this.bIgnoreKeepTogether = bIgnoreKeepTogether;
    }

}
