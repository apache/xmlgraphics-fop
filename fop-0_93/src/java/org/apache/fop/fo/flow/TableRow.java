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

package org.apache.fop.fo.flow;

import java.util.BitSet;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;

/**
 * Class modelling the fo:table-row object.
 */
public class TableRow extends TableFObj {
    // The value of properties relevant for fo:table-row.
    private LengthRangeProperty blockProgressionDimension;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private int breakAfter;
    private int breakBefore;
    private Length height;
    private String id;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    //     private CommonAural commonAural;
    //     private CommonRelativePosition commonRelativePosition;
    //     private int visibility;
    // End of property values

    private boolean setup = false;
    
    protected List pendingSpans;
    protected BitSet usedColumnIndices;
    private int columnIndex = 1;

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableRow(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        blockProgressionDimension 
            = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        id = pList.get(PR_ID).getString();
        height = pList.get(PR_HEIGHT).getLength();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        super.bind(pList);
    }

    /**
     * Adds a cell to this row (skips marker handling done by 
     * FObj.addChildNode().
     * Used by TableBody during the row building process when only cells are
     * used as direct children of a table-body/header/footer.
     * @param cell cell to add.
     */
    protected void addReplacedCell(TableCell cell) {
        if (childNodes == null) {
            childNodes = new java.util.ArrayList();
        }
        childNodes.add(cell);
    }
    
    /**
     * @see org.apache.fop.fo.FONode#processNode(String, Locator, 
     *                                  Attributes, PropertyList)
     */
    public void processNode(String elementName, Locator locator, 
            Attributes attlist, PropertyList pList) throws FOPException {
        if (!inMarker()) {
            TableBody body = (TableBody) parent;
            body.resetColumnIndex();
            pendingSpans = body.pendingSpans;
            usedColumnIndices = body.usedColumnIndices;
            while (usedColumnIndices.get(columnIndex - 1)) {
                columnIndex++;
            }
        }
        super.processNode(elementName, locator, attlist, pList);
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode child) throws FOPException {
        if (!inMarker()) {
            Table t = getTable();
            TableBody body = (TableBody) getParent();
            if (body.isFirst(this)) {
                TableCell cell = (TableCell) child;
                int colNr = cell.getColumnNumber();
                int colSpan = cell.getNumberColumnsSpanned();
                Length colWidth = null;

                if (cell.getWidth().getEnum() != EN_AUTO
                        && colSpan == 1) {
                    colWidth = cell.getWidth();
                }
                
                for (int i = colNr; i < colNr + colSpan; ++i) {
                    if (t.columns.size() < i
                            || t.columns.get(i - 1) == null) {
                        t.addDefaultColumn(colWidth, 
                                i == colNr 
                                    ? cell.getColumnNumber()
                                    : 0);
                    } else {
                        TableColumn col = (TableColumn) t.columns.get(i - 1);
                        if (!col.isDefaultColumn()
                                && colWidth != null) {
                            col.setColumnWidth(colWidth);
                        }
                    }
                }
            }
        }
        super.addChildNode(child);
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        checkId(id);
        getFOEventHandler().startRow(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        if (childNodes == null) {
            missingChildElementError("(table-cell+)");
        }
        if (!inMarker()) {
            pendingSpans = null;
            usedColumnIndices = null;
        }
        getFOEventHandler().endRow(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: (table-cell+)
     */
    protected void validateChildNode(Locator loc, String nsURI, 
                                     String localName) 
        throws ValidationException {
        if (!(FO_URI.equals(nsURI) && localName.equals("table-cell"))) {
            invalidChildError(loc, nsURI, localName);
        }
    }    
    
    /**
     * @return the "id" property.
     */
    public String getId() {
        return id;
    }

    /** @return the "break-after" property. */
    public int getBreakAfter() {
        return breakAfter;
    }

    /** @return the "break-before" property. */
    public int getBreakBefore() {
        return breakBefore;
    }

    /** @return the "keep-with-previous" property. */
    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    /** @return the "keep-with-next" property. */
    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    /** @return the "keep-together" property. */
    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    /**
     * Convenience method to check if a keep-together 
     * constraint is specified.
     * @return true if keep-together is active.
     */
    public boolean mustKeepTogether() {
        return !getKeepTogether().getWithinPage().isAuto()
                || !getKeepTogether().getWithinColumn().isAuto();
    }
    
    /**
     * Convenience method to check if a keep-with-next 
     * constraint is specified.
     * @return true if keep-with-next is active.
     */
    public boolean mustKeepWithNext() {
        return !getKeepWithNext().getWithinPage().isAuto()
                || !getKeepWithNext().getWithinColumn().isAuto();
    }
    
    /**
     * Convenience method to check if a keep-with-previous 
     * constraint is specified.
     * @return true if keep-with-previous is active.
     */
    public boolean mustKeepWithPrevious() {
        return !getKeepWithPrevious().getWithinPage().isAuto()
                || !getKeepWithPrevious().getWithinColumn().isAuto();
    }
    
    /**
     * @return the "block-progression-dimension" property.
     */
    public LengthRangeProperty getBlockProgressionDimension() {
        return blockProgressionDimension;
    }

    /**
     * @return the "height" property.
     */
    public Length getHeight() {
        return height;
    }

    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }
    
    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "table-row";
    }

    /** @see org.apache.fop.fo.FObj#getNameId() */
    public int getNameId() {
        return FO_TABLE_ROW;
    }
    
    /**
     * Returns the current column index of the TableRow
     *                                 
     * @return the next column number to use
     */
    public int getCurrentColumnIndex() {
        return columnIndex;
    }

    /**
     * Sets the current column index to a specific value
     * in case a column-number was explicitly specified
     * (used by ColumnNumberPropertyMaker.make())
     * 
     * @param newIndex  new value for column index
     */
    public void setCurrentColumnIndex(int newIndex) {
        columnIndex = newIndex;
    }

    /**
     * Checks whether a given column-number is already in use
     * for the current row (used by TableCell.bind());
     * 
     * @param colNr the column-number to check
     * @return true if column-number is already occupied
     */
    public boolean isColumnNumberUsed(int colNr) {
        return usedColumnIndices.get(colNr - 1);
    }
    
    /**
     * @see org.apache.fop.fo.flow.TableFObj#flagColumnIndices(int, int)
     */
    protected void flagColumnIndices(int start, int end) {
        for (int i = start; i < end; i++) {
            usedColumnIndices.set(i);
        }
        // update columnIndex for the next cell
        while (usedColumnIndices.get(columnIndex - 1)) {
            columnIndex++;
        }
    }    
}
