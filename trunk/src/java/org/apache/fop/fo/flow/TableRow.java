/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

package org.apache.fop.fo.flow;

import java.util.BitSet;
import java.util.List;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
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
    private CommonAccessibility commonAccessibility;
    private LengthRangeProperty blockProgressionDimension;
    private CommonAural commonAural;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonRelativePosition commonRelativePosition;
    private int breakAfter;
    private int breakBefore;
    private Length height;
    private String id;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private int visibility;
    // End of property values

    private boolean setup = false;
    
    private List pendingSpans;
    private BitSet usedColumnIndices;
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
        commonAccessibility = pList.getAccessibilityProps();
        blockProgressionDimension = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        commonAural = pList.getAuralProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonRelativePosition = pList.getRelativePositionProps();
        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        id = pList.get(PR_ID).getString();
        height = pList.get(PR_HEIGHT).getLength();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        visibility = pList.get(PR_VISIBILITY).getEnum();
        super.bind(pList);
    }

    /**
     * Adds a cell to this row (skips marker handling done by FObj.addChildNode().
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
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        pendingSpans = ((TableBody) parent).pendingSpans;
        usedColumnIndices = ((TableBody) parent).usedColumnIndices;
        while (usedColumnIndices.get(columnIndex - 1)) {
            columnIndex++;
        }
        
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
        if (((TableBody) parent).isFirst(this) 
                && getTable().columns == null ) {
            //force parent body's pendingSpans
            //to the one accumulated after processing this row
            ((TableBody) parent).pendingSpans = pendingSpans;
        }
        ((TableBody) parent).resetColumnIndex();
        //release references
        pendingSpans = null;
        usedColumnIndices = null;
        getFOEventHandler().endRow(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: (table-cell+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (!(FO_URI.equals(nsURI) && localName.equals("table-cell"))) {
            invalidChildError(loc, nsURI, localName);
        }
    }
    
    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode child) throws FOPException {
        TableCell cell = (TableCell) child;
        int rowSpan = cell.getNumberRowsSpanned();
        int colSpan = cell.getNumberColumnsSpanned();
        if (((TableBody) parent).isFirst(this) 
                && getTable().columns == null ) {
            if (pendingSpans == null) {
                pendingSpans = new java.util.ArrayList();
            }
            pendingSpans.add(null);
            if (usedColumnIndices == null) {
                usedColumnIndices = new BitSet();
            }
        }
        //if the current cell spans more than one row,
        //update pending span list for the next row
        if (rowSpan > 1) {
            for (int i = colSpan; --i >= 0;) {
                pendingSpans.set(columnIndex - 1 + i, 
                        new PendingSpan(rowSpan));
            }
        }
        //flag column indices used by this cell,
        //take into account that possibly not all column-numbers
        //are used by columns in the parent table (if any),
        //so a cell spanning three columns, might actually
        //take up more than three columnIndices...
        int startIndex = columnIndex - 1;
        int endIndex = startIndex + colSpan;
        if (getTable().columns != null) {
            List cols = getTable().columns;
            int tmpIndex = endIndex;
            for (int i = startIndex; i <= tmpIndex; ++i) {
                if (i < cols.size() && cols.get(i) == null) {
                    endIndex++;
                }
            }
        }
        for (int i = startIndex; i < endIndex; i++) {
            usedColumnIndices.set(i);
        }
        //update columnIndex for the next cell
        while (usedColumnIndices.get(columnIndex - 1)) {
            columnIndex++;
        }
        super.addChildNode(cell);
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
     * Convenience method to check if a keep-together constraint is specified.
     * @return true if keep-together is active.
     */
    public boolean mustKeepTogether() {
        return !getKeepTogether().getWithinPage().isAuto()
                || !getKeepTogether().getWithinColumn().isAuto();
    }
    
    /**
     * Convenience method to check if a keep-with-next constraint is specified.
     * @return true if keep-with-next is active.
     */
    public boolean mustKeepWithNext() {
        return !getKeepWithNext().getWithinPage().isAuto()
                || !getKeepWithNext().getWithinColumn().isAuto();
    }
    
    /**
     * Convenience method to check if a keep-with-previous constraint is specified.
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
    
    /** @see org.apache.fop.fo.FObj#getName() */
    public String getName() {
        return "fo:table-row";
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
     * (used by TableCell.bind())
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
    protected boolean isColumnNumberUsed(int colNr) {
        return usedColumnIndices.get(colNr - 1);
    }
}
