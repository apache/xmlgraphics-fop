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
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * Class modelling the fo:table-body object.
 */
public class TableBody extends TableCellContainer {
    // The value of properties relevant for fo:table-body.
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    //     private CommonAural commonAural;
    //     private CommonRelativePosition commonRelativePosition;
    //    private int visibility;
    // End of property values

    /**
     * used for validation
     */
    protected boolean tableRowsFound = false;
    protected boolean tableCellsFound = false;

    private boolean firstRow = true;

    private boolean rowsStarted = false;

    private boolean lastCellEndsRow = true;

    /** The last encountered table-row. */
    private TableRow lastRow;

    private List rowGroups = new LinkedList();

    /**
     * @param parent FONode that is the parent of the object
     */
    public TableBody(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        super.bind(pList);
    }

    /**
     * {@inheritDoc}
     */
    public void processNode(String elementName, Locator locator,
                            Attributes attlist, PropertyList pList)
                    throws FOPException {
        if (!inMarker()) {
            Table t = getTable();
            if (t.hasExplicitColumns()) {
                int size = t.getNumberOfColumns();
                pendingSpans = new ArrayList(size);
                for (int i = 0; i < size; i++) {
                    pendingSpans.add(null);
                }
            } else {
                pendingSpans = new ArrayList();
            }
            columnNumberManager = new ColumnNumberManager();
        }
        super.processNode(elementName, locator, attlist, pList);
    }

    /**
     * {@inheritDoc}
     */
    public void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startBody(this);
    }

    /**
     * {@inheritDoc}
     */
    public void endOfNode() throws FOPException {

        if (!inMarker()) {
            pendingSpans = null;
            columnNumberManager = null;
        }

        getFOEventHandler().endBody(this);

        if (!(tableRowsFound || tableCellsFound)) {
            if (getUserAgent().validateStrictly()) {
                missingChildElementError("marker* (table-row+|table-cell+)");
            } else {
                log.error("fo:table-body must not be empty. "
                        + "Expected: marker* (table-row+|table-cell+)");
                getParent().removeChild(this);
            }
        } else {
            finishLastRowGroup();
        }
    }

    /** {@inheritDoc} */
    TableBody getTablePart() {
        return this;
    }

    protected void finishLastRowGroup() throws ValidationException {
        if (!inMarker()) {
            RowGroupBuilder rowGroupBuilder = getTable().getRowGroupBuilder();
            if (tableRowsFound) {
                rowGroupBuilder.endRow(lastRow);
            } else if (!lastCellEndsRow) {
                rowGroupBuilder.endRow(this);
            }
            try {
                rowGroupBuilder.endTablePart();
            } catch (ValidationException e) {
                e.setLocator(locator);
                throw e;
            }
        }
    }

    /**
     * {@inheritDoc} String, String)
     * XSL Content Model: marker* (table-row+|table-cell+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
        throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("marker")) {
                if (tableRowsFound || tableCellsFound) {
                   nodesOutOfOrderError(loc, "fo:marker", "(table-row+|table-cell+)");
                }
            } else if (localName.equals("table-row")) {
                tableRowsFound = true;
                if (tableCellsFound) {
                    invalidChildError(loc, nsURI, localName, "Either fo:table-rows" +
                      " or fo:table-cells may be children of an " + getName() +
                      " but not both");
                }
            } else if (localName.equals("table-cell")) {
                tableCellsFound = true;
                if (tableRowsFound) {
                    invalidChildError(loc, nsURI, localName,
                            "Either fo:table-rows or fo:table-cells "
                            + "may be children of an "
                            + getName() + " but not both");
                }
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        } else {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void addChildNode(FONode child) throws FOPException {
        if (!inMarker()) {
            switch (child.getNameId()) {
            case FO_TABLE_ROW:
                if (!rowsStarted) {
                    getTable().getRowGroupBuilder().startTablePart(this);
                } else {
                    columnNumberManager.prepareForNextRow(pendingSpans);
                    getTable().getRowGroupBuilder().endRow(lastRow);
                }
                rowsStarted = true;
                lastRow = (TableRow) child;
                getTable().getRowGroupBuilder().startRow(lastRow);
                break;
            case FO_TABLE_CELL:
                if (!rowsStarted) {
                    getTable().getRowGroupBuilder().startTablePart(this);
                }
                rowsStarted = true;
                TableCell cell = (TableCell) child;
                addTableCellChild(cell, firstRow);
                lastCellEndsRow = cell.endsRow();
                if (lastCellEndsRow) {
                    firstRow = false;
                    columnNumberManager.prepareForNextRow(pendingSpans);
                    getTable().getRowGroupBuilder().endRow(this);
                }
                break;
            default:
                //nop
            }
        }
        super.addChildNode(child);
    }

    /** {inheritDoc} */
    protected void setCollapsedBorders() {
        Table table = (Table) parent;
        createBorder(CommonBorderPaddingBackground.START, table);
        createBorder(CommonBorderPaddingBackground.END, table);
        createBorder(CommonBorderPaddingBackground.BEFORE);
        createBorder(CommonBorderPaddingBackground.AFTER);
    }

    void addRowGroup(List rowGroup) {
        rowGroups.add(rowGroup);
    }

    public List getRowGroups() {
        return rowGroups;
    }

    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "table-body";
    }

    /**
     * {@inheritDoc}
     */
    public int getNameId() {
        return FO_TABLE_BODY;
    }

    protected boolean isTableHeader() {
        return false;
    }

    protected boolean isTableFooter() {
        return false;
    }

    /**
     * @param obj table row in question
     * @return true if the given table row is the first row of this body.
     */
    public boolean isFirst(TableRow obj) {
        return (firstChild == null
                || firstChild == obj);
    }

    void signalNewRow() {
        if (rowsStarted) {
            firstRow = false;
            if (!lastCellEndsRow) {
                columnNumberManager.prepareForNextRow(pendingSpans);
                getTable().getRowGroupBuilder().endRow(this);
            }
        }
    }

}
