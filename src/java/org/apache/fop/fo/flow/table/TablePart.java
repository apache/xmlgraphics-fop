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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;

/**
 * An abstract base class modelling a TablePart
 * (i.e. fo:table-header, fo:table-footer and fo:table-body).
 */
public abstract class TablePart extends TableCellContainer {
    // The value of properties relevant for fo:table-body.
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    //     private CommonAural commonAural;
    //     private CommonRelativePosition commonRelativePosition;
    //    private int visibility;
    // End of property values

    /** table rows found */
    protected boolean tableRowsFound = false;
    /** table cells found */
    protected boolean tableCellsFound = false;

    private boolean firstRow = true;

    private boolean rowsStarted = false;

    private boolean lastCellEndsRow = true;

    private List rowGroups = new LinkedList();

    /**
     * Create a TablePart instance with the given {@link FONode}
     * as parent.
     * @param parent FONode that is the parent of the object
     */
    public TablePart(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    protected Object clone() {
        TablePart clone = (TablePart) super.clone();
        clone.rowGroups = new LinkedList(rowGroups);
        return clone;
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        super.bind(pList);
    }

    /** {@inheritDoc} */
    public void processNode(String elementName, Locator locator,
                            Attributes attlist, PropertyList pList)
                    throws FOPException {

        super.processNode(elementName, locator, attlist, pList);
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

    }

    /** {@inheritDoc} */
    public void finalizeNode() throws FOPException {
        if (!inMarker()) {
            pendingSpans = null;
            columnNumberManager = null;
        }
        if (!(tableRowsFound || tableCellsFound)) {
            missingChildElementError("marker* (table-row+|table-cell+)", true);
            getParent().removeChild(this);
        } else {
            finishLastRowGroup();
        }

    }

    /** {@inheritDoc} */
    TablePart getTablePart() {
        return this;
    }

    /**
     * Finish last row group.
     * @throws ValidationException if content validation exception
     */
    protected void finishLastRowGroup() throws ValidationException {
        if (!inMarker()) {
            RowGroupBuilder rowGroupBuilder = getTable().getRowGroupBuilder();
            if (tableRowsFound) {
                rowGroupBuilder.endTableRow();
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
     * {@inheritDoc}
     * <br>XSL Content Model: marker* (table-row+|table-cell+)
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
                    TableEventProducer eventProducer = TableEventProducer.Provider.get(
                            getUserAgent().getEventBroadcaster());
                    eventProducer.noMixRowsAndCells(this, getName(), getLocator());
                }
            } else if (localName.equals("table-cell")) {
                tableCellsFound = true;
                if (tableRowsFound) {
                    TableEventProducer eventProducer = TableEventProducer.Provider.get(
                            getUserAgent().getEventBroadcaster());
                    eventProducer.noMixRowsAndCells(this, getName(), getLocator());
                }
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /** {@inheritDoc} */
    protected void addChildNode(FONode child) throws FOPException {
        if (!inMarker()) {
            switch (child.getNameId()) {
            case FO_TABLE_ROW:
                if (!rowsStarted) {
                    getTable().getRowGroupBuilder().startTablePart(this);
                } else {
                    columnNumberManager.prepareForNextRow(pendingSpans);
                    getTable().getRowGroupBuilder().endTableRow();
                }
                rowsStarted = true;
                getTable().getRowGroupBuilder().startTableRow((TableRow)child);
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
        //TODO: possible performance problems in case of large tables...
        //If the number of children grows significantly large, the default
        //implementation in FObj will get slower and slower...
        super.addChildNode(child);
    }

    void addRowGroup(List rowGroup) {
        rowGroups.add(rowGroup);
    }

    /** @return list of row groups */
    public List getRowGroups() {
        return rowGroups;
    }

    /**
     * Get the {@link CommonBorderPaddingBackground} instance attached
     * to this TableBody.
     * @return the {@link CommonBorderPaddingBackground} instance.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
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
