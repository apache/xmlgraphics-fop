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

package org.apache.fop.fo.flow.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.fop.fo.properties.CommonBorderPaddingBackground;

/**
 * A class that implements the border-collapsing model.
 */
class CollapsingBorderResolver implements BorderResolver {

    private Table table;

    /**
     * The previously registered row, either in the header or the body(-ies), but not in
     * the footer (handled separately).
     */
    private List/*<GridUnit>*/ previousRow;

    private boolean firstInTable;

    private List/*<GridUnit>*/ footerFirstRow;

    /** The last currently registered footer row. */
    private List/*<GridUnit>*/ footerLastRow;

    private Resolver delegate;

    // Re-use the same ResolverInBody for every table-body
    // Important to properly handle firstInBody!!
    private Resolver resolverInBody = new ResolverInBody();

    private Resolver resolverInFooter;

    private List/*<ConditionalBorder>*/ leadingBorders;

    private List/*<ConditionalBorder>*/ trailingBorders;

    /* TODO Temporary hack for resolved borders in header */
    /* Currently the normal border is always used. */
    private List/*<GridUnit>*/ headerLastRow = null;
    /* End of temporary hack */

    /**
     * Base class for delegate resolvers. Implementation of the State design pattern: the
     * treatment differs slightly whether we are in the table's header, footer or body. To
     * avoid complicated if statements, specialised delegate resolvers will be used
     * instead.
     */
    private abstract class Resolver {

        protected TableBody tablePart;

        protected boolean firstInPart;

        /**
         * Integrates border-before specified on the table and its column.
         * 
         * @param row the first row of the table (in the header, or in the body if the
         * table has no header)
         * @param withLeadingTrailing
         * @param withNonLeadingTrailing
         * @param withRest
         */
        void resolveBordersFirstRowInTable(List/*<GridUnit>*/ row, boolean withLeadingTrailing,
                boolean withNonLeadingTrailing, boolean withRest) {
            assert firstInTable;
            for (int i = 0; i < row.size(); i++) {
                TableColumn column = table.getColumn(i);
                ((GridUnit) row.get(i)).integrateBorderSegment(
                        CommonBorderPaddingBackground.BEFORE, column, withLeadingTrailing,
                        withNonLeadingTrailing, withRest);
            }
            firstInTable = false;
        }

        /**
         * Resolves border-after for the first row, border-before for the second one.
         * 
         * @param rowBefore
         * @param rowAfter
         */
        void resolveBordersBetweenRows(List/*<GridUnit>*/ rowBefore, List/*<GridUnit>*/ rowAfter) {
            assert rowBefore != null && rowAfter != null;
            for (int i = 0; i < rowAfter.size(); i++) {
                GridUnit gu = (GridUnit) rowAfter.get(i);
                if (gu.getRowSpanIndex() == 0) {
                    GridUnit beforeGU = (GridUnit) rowBefore.get(i);
                    gu.resolveBorder(beforeGU, CommonBorderPaddingBackground.BEFORE);
                }
            }
        }

        /** Integrates the border-after of the part. */
        void resolveBordersLastRowInPart(List/*<GridUnit>*/ row, boolean withLeadingTrailing,
                boolean withNonLeadingTrailing, boolean withRest) {
            for (int i = 0; i < row.size(); i++) {
                ((GridUnit) row.get(i)).integrateBorderSegment(CommonBorderPaddingBackground.AFTER,
                        tablePart, withLeadingTrailing, withNonLeadingTrailing, withRest);
            }
        }

        /**
         * Integrates border-after specified on the table and its columns.
         * 
         * @param row the last row of the footer, or of the last body if the table has no
         * footer
         * @param withLeadingTrailing
         * @param withNonLeadingTrailing
         * @param withRest
         */
        void resolveBordersLastRowInTable(List/*<GridUnit>*/ row, boolean withLeadingTrailing,
                boolean withNonLeadingTrailing, boolean withRest) {
            for (int i = 0; i < row.size(); i++) {
                TableColumn column = table.getColumn(i);
                ((GridUnit) row.get(i)).integrateBorderSegment(CommonBorderPaddingBackground.AFTER,
                        column, withLeadingTrailing, withNonLeadingTrailing, withRest);
            }
        }

        /**
         * Integrates either border-before specified on the table and its columns if the
         * table has no header, or border-after specified on the cells of the header's
         * last row. For the case the grid unit are at the top of a page.
         * 
         * @param row
         */
        void integrateLeadingBorders(List/*<GridUnit>*/ row) {
            for (int i = 0; i < table.getNumberOfColumns(); i++) {
                GridUnit gu = (GridUnit) row.get(i);
                ConditionalBorder border = (ConditionalBorder) leadingBorders.get(i);
                gu.integrateCompetingBorder(CommonBorderPaddingBackground.BEFORE, border,
                        true, false, true);
            }
        }

        /**
         * Integrates either border-after specified on the table and its columns if the
         * table has no footer, or border-before specified on the cells of the footer's
         * first row. For the case the grid unit are at the bottom of a page.
         * 
         * @param row
         */
        void integrateTrailingBorders(List/*<GridUnit>*/ row) {
            for (int i = 0; i < table.getNumberOfColumns(); i++) {
                GridUnit gu = (GridUnit) row.get(i);
                ConditionalBorder border = (ConditionalBorder) trailingBorders.get(i);
                gu.integrateCompetingBorder(CommonBorderPaddingBackground.AFTER, border,
                        true, false, true);
            }
        }

        void startPart(TableBody part) {
            tablePart = part;
            firstInPart = true;
        }

        /**
         * Resolves the applicable borders for the given row.
         * <ul>
         * <li>Integrates the border-before/after of the containing table-row if any;</li>
         * <li>Integrates the border-before of the containing part, if first row;</li>
         * <li>Resolves border-start/end between grid units.</li>
         * </ul>
         * 
         * @param row the row being finished
         * @param container the containing element
         */
        void endRow(List/*<GridUnit>*/ row, TableCellContainer container) {
            // Resolve before- and after-borders for the table-row
            if (container instanceof TableRow) {
                TableRow tableRow = (TableRow) container;
                for (Iterator iter = row.iterator(); iter.hasNext();) {
                    GridUnit gu = (GridUnit) iter.next();
                    boolean first = (gu.getRowSpanIndex() == 0);
                    boolean last = gu.isLastGridUnitRowSpan();
                    gu.integrateBorderSegment(CommonBorderPaddingBackground.BEFORE, tableRow,
                            first, first, true);
                    gu.integrateBorderSegment(CommonBorderPaddingBackground.AFTER, tableRow,
                            last, last, true);
                }
            }
            if (firstInPart) {
                // Integrate the border-before of the part
                for (int i = 0; i < row.size(); i++) {
                    ((GridUnit) row.get(i)).integrateBorderSegment(
                            CommonBorderPaddingBackground.BEFORE, tablePart, true, true, true);
                }
                firstInPart = false;
            }
            // Resolve start/end borders in the row
            Iterator guIter = row.iterator();
            GridUnit gu = (GridUnit) guIter.next();
            Iterator colIter = table.getColumns().iterator();
            TableColumn col = (TableColumn) colIter.next();
            gu.integrateBorderSegment(CommonBorderPaddingBackground.START, col);
            gu.integrateBorderSegment(CommonBorderPaddingBackground.START, container);
            while (guIter.hasNext()) {
                GridUnit nextGU = (GridUnit) guIter.next();
                TableColumn nextCol = (TableColumn) colIter.next();
                if (gu.isLastGridUnitColSpan()) {
                    gu.integrateBorderSegment(CommonBorderPaddingBackground.END, col);
                    nextGU.integrateBorderSegment(CommonBorderPaddingBackground.START, nextCol);
                    gu.resolveBorder(nextGU, CommonBorderPaddingBackground.END);
                }
                gu = nextGU;
                col = nextCol;
            }
            gu.integrateBorderSegment(CommonBorderPaddingBackground.END, col);
            gu.integrateBorderSegment(CommonBorderPaddingBackground.END, container);
        }

        void endPart() {
            resolveBordersLastRowInPart(previousRow, true, true, true);
        }

        abstract void endTable();
    }

    private class ResolverInHeader extends Resolver {

        void endRow(List/*<GridUnit>*/ row, TableCellContainer container) {
            super.endRow(row, container);
            if (previousRow != null) {
                resolveBordersBetweenRows(previousRow, row);
            } else {
                /*
                 * This is a bit hacky...
                 * The two only sensible values for border-before on the header's first row are:
                 * - at the beginning of the table (normal case)
                 * - if the header is repeated after each page break
                 * To represent those values we (ab)use the nonLeadingTrailing and the rest
                 * fields of ConditionalBorder. But strictly speaking this is not their
                 * purposes.
                 */
                for (Iterator guIter = row.iterator(); guIter.hasNext();) {
                    ConditionalBorder borderBefore = ((GridUnit) guIter.next()).borderBefore;
                    borderBefore.leadingTrailing = borderBefore.nonLeadingTrailing;
                    borderBefore.rest = borderBefore.nonLeadingTrailing;
                }
                resolveBordersFirstRowInTable(row, false, true, true);
            }
            previousRow = row;
        }

        void endPart() {
            super.endPart();
            leadingBorders = new ArrayList(table.getNumberOfColumns());
            /*
             * Another hack...
             * The border-after of a header is always the same. Leading and rest don't
             * apply to cells in the header since they are never broken. To ease
             * resolution we override the (normally unused) leadingTrailing and rest
             * fields of ConditionalBorder with the only sensible nonLeadingTrailing
             * field. That way grid units from the body will always resolve against the
             * same, normal header border.
             */
            for (Iterator guIter = previousRow.iterator(); guIter.hasNext();) {
                ConditionalBorder borderAfter = ((GridUnit) guIter.next()).borderAfter;
                borderAfter.leadingTrailing = borderAfter.nonLeadingTrailing;
                borderAfter.rest = borderAfter.nonLeadingTrailing;
                leadingBorders.add(borderAfter);
            }
            /* TODO Temporary hack for resolved borders in header */
            headerLastRow = previousRow;
            /* End of temporary hack */
        }

        void endTable() {
            throw new IllegalStateException();
        }
    }

    private class ResolverInFooter extends Resolver {

        void endRow(List/*<GridUnit>*/ row, TableCellContainer container) {
            super.endRow(row, container);
            if (footerFirstRow == null) {
                footerFirstRow = row;
            } else {
                // There is a previous row
                resolveBordersBetweenRows(footerLastRow, row);
            }
            footerLastRow = row;
        }

        void endPart() {
            resolveBordersLastRowInPart(footerLastRow, true, true, true);
            trailingBorders = new ArrayList(table.getNumberOfColumns());
            // See same method in ResolverInHeader for an explanation of the hack
            for (Iterator guIter = footerFirstRow.iterator(); guIter.hasNext();) {
                ConditionalBorder borderBefore = ((GridUnit) guIter.next()).borderBefore;
                borderBefore.leadingTrailing = borderBefore.nonLeadingTrailing;
                borderBefore.rest = borderBefore.nonLeadingTrailing;
                trailingBorders.add(borderBefore);
            }
        }

        void endTable() {
            // Resolve after/before border between the last row of table-body and the
            // first row of table-footer
            resolveBordersBetweenRows(previousRow, footerFirstRow);
            // See endRow method in ResolverInHeader for an explanation of the hack
            for (Iterator guIter = footerLastRow.iterator(); guIter.hasNext();) {
                ConditionalBorder borderAfter = ((GridUnit) guIter.next()).borderAfter;
                borderAfter.leadingTrailing = borderAfter.nonLeadingTrailing;
                borderAfter.rest = borderAfter.nonLeadingTrailing;
            }
            resolveBordersLastRowInTable(footerLastRow, false, true, true);
        }
    }

    private class ResolverInBody extends Resolver {

        private boolean firstInBody = true;

        void endRow(List/*<GridUnit>*/ row, TableCellContainer container) {
            super.endRow(row, container);
            if (firstInTable) {
                resolveBordersFirstRowInTable(row, true, true, true);
            } else {
                // Either there is a header, and then previousRow is set to the header's last row,
                // or this is not the first row in the body, and previousRow is not null
                resolveBordersBetweenRows(previousRow, row);
                integrateLeadingBorders(row);
            }
            integrateTrailingBorders(row);
            previousRow = row;
            if (firstInBody) {
                firstInBody = false;
                for (Iterator iter = row.iterator(); iter.hasNext();) {
                    GridUnit gu = (GridUnit) iter.next();
                    gu.borderBefore.leadingTrailing = gu.borderBefore.nonLeadingTrailing;
                }
            }
        }

        void endTable() {
            if (resolverInFooter != null) {
                resolverInFooter.endTable();
            } else {
                // Trailing and rest borders already resolved with integrateTrailingBorders
                resolveBordersLastRowInTable(previousRow, false, true, false);
            }
            for (Iterator iter = previousRow.iterator(); iter.hasNext();) {
                GridUnit gu = (GridUnit) iter.next();
                gu.borderAfter.leadingTrailing = gu.borderAfter.nonLeadingTrailing;
            }
        }
    }

    CollapsingBorderResolver(Table table) {
        this.table = table;
        firstInTable = true;
    }

    /** {@inheritDoc} */
    public void endRow(List/*<GridUnit>*/ row, TableCellContainer container) {
        delegate.endRow(row, container);
    }

    /** {@inheritDoc} */
    public void startPart(TableBody part) {
        if (part.isTableHeader()) {
            delegate = new ResolverInHeader();
        } else {
            if (leadingBorders == null || table.omitHeaderAtBreak()) {
                // No header, leading borders determined by the table
                leadingBorders = new ArrayList(table.getNumberOfColumns());
                for (Iterator colIter = table.getColumns().iterator(); colIter.hasNext();) {
                    // See endRow method in ResolverInHeader for an explanation of the hack
                    ConditionalBorder border = ((TableColumn) colIter.next()).borderBefore;
                    border.leadingTrailing = border.rest;
                    leadingBorders.add(border);
                }
            }
            if (part.isTableFooter()) {
                resolverInFooter = new ResolverInFooter();
                delegate = resolverInFooter;
            } else {
                if (trailingBorders == null || table.omitFooterAtBreak()) {
                    // No footer, trailing borders determined by the table
                    trailingBorders = new ArrayList(table.getNumberOfColumns());
                    for (Iterator colIter = table.getColumns().iterator(); colIter.hasNext();) {
                        // See endRow method in ResolverInHeader for an explanation of the hack
                        ConditionalBorder border = ((TableColumn) colIter.next()).borderAfter;
                        border.leadingTrailing = border.rest;
                        trailingBorders.add(border);
                    }
                }
                delegate = resolverInBody;
            }
        }
        delegate.startPart(part);
    }

    /** {@inheritDoc} */
    public void endPart() {
        delegate.endPart();
    }

    /** {@inheritDoc} */
    public void endTable() {
        delegate.endTable();
        delegate = null;
        /* TODO Temporary hack for resolved borders in header */
        if (headerLastRow != null) {
            for (Iterator iter = headerLastRow.iterator(); iter.hasNext();) {
                GridUnit gu = (GridUnit) iter.next();
                gu.borderAfter.leadingTrailing = gu.borderAfter.nonLeadingTrailing;
            }
        }
        if (footerLastRow != null) {
            for (Iterator iter = footerLastRow.iterator(); iter.hasNext();) {
                GridUnit gu = (GridUnit) iter.next();
                gu.borderAfter.leadingTrailing = gu.borderAfter.nonLeadingTrailing;
            }
        }
        /* End of temporary hack */
    }
}
