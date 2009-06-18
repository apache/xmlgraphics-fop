/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.LengthRangeProperty;

/**
 * Class modelling the fo:table-cell object.
 * @todo check need for all instance variables stored here
 */
public class TableCell extends TableFObj {
    // The value of properties relevant for fo:table-cell.
    private CommonAccessibility commonAccessibility;
    private CommonAural commonAural;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonRelativePosition commonRelativePosition;
    private LengthRangeProperty blockProgressionDimension;
    private Numeric columnNumber;
    private int displayAlign;
    private int relativeAlign;
    private int emptyCells;
    private int endsRow;
    private Length height;
    private String id;
    private LengthRangeProperty inlineProgressionDimension;
    private Numeric numberColumnsSpanned;
    private Numeric numberRowsSpanned;
    private int startsRow;
    private Length width;
    // End of property values

    /** used for FO validation */
    private boolean blockItemFound = false;

    /**
     * Offset of content rectangle in inline-progression-direction,
     * relative to table.
     */
    protected int startOffset;

    /**
     * Offset of content rectangle, in block-progression-direction,
     * relative to the row.
     */
    protected int beforeOffset = 0;

    /**
     * Offset of content rectangle, in inline-progression-direction,
     * relative to the column start edge.
     */
    protected int startAdjust = 0;

    /**
     * Adjust to theoretical column width to obtain content width
     * relative to the column start edge.
     */
    protected int widthAdjust = 0;

    /** For collapsed border style */
    protected int borderHeight = 0;

    /** Ypos of cell ??? */
    protected int top;

    /**
     * Set to true if all content completely laid out.
     */
    private boolean bDone = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableCell(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = pList.getAccessibilityProps();
        commonAural = pList.getAuralProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonRelativePosition = pList.getRelativePositionProps();
        blockProgressionDimension = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        columnNumber = pList.get(PR_COLUMN_NUMBER).getNumeric();
        displayAlign = pList.get(PR_DISPLAY_ALIGN).getEnum();
        relativeAlign = pList.get(PR_RELATIVE_ALIGN).getEnum();
        emptyCells = pList.get(PR_EMPTY_CELLS).getEnum();
        endsRow = pList.get(PR_ENDS_ROW).getEnum();
        height = pList.get(PR_HEIGHT).getLength();
        id = pList.get(PR_ID).getString();
        inlineProgressionDimension = pList.get(PR_INLINE_PROGRESSION_DIMENSION).getLengthRange();
        numberColumnsSpanned = pList.get(PR_NUMBER_COLUMNS_SPANNED).getNumeric();
        numberRowsSpanned = pList.get(PR_NUMBER_ROWS_SPANNED).getNumeric();
        startsRow = pList.get(PR_STARTS_ROW).getEnum();
        width = pList.get(PR_WIDTH).getLength();
        super.bind(pList);
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        checkId(id);
        getFOEventHandler().startCell(this);
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * FOEventHandler that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        if (!blockItemFound) {
            if (getUserAgent().validateStrictly()) {
                missingChildElementError("marker* (%block;)+");
            } else if (childNodes != null && childNodes.size() > 0) {
                getLogger().warn("fo:table-cell content that is not enclosed by a "
                        + "fo:block will be dropped/ignored.");
            }
        }
        if ((startsRow() || endsRow()) 
                && getParent().getNameId() == FO_TABLE_ROW ) {
            getLogger().warn("starts-row/ends-row for fo:table-cells "
                    + "non-applicable for children of an fo:table-row.");
        }
        getFOEventHandler().endCell(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (%block;)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (FO_URI.equals(nsURI) && localName.equals("marker")) {
            if (blockItemFound) {
               nodesOutOfOrderError(loc, "fo:marker", "(%block;)");
            }
        } else if (!isBlockItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        } else {
            blockItemFound = true;
        }
    }

    /** @see org.apache.fop.fo.FObj#generatesReferenceAreas() */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /**
     * Set position relative to table (set by body?)
     * 
     * @param offset    new offset
     */
    public void setStartOffset(int offset) {
        startOffset = offset;
    }

    /**
     * Calculate cell border and padding, including offset of content
     * rectangle from the theoretical grid position.
     */
// TODO This whole method is not used it refers to padding which requires layout
// context to evaluate
//    private void calcBorders(CommonBorderPaddingBackground bp) {
//        if (this.borderCollapse == EN_SEPARATE) {
//            /*
//             * Easy case.
//             * Cell border is the property specified directly on cell.
//             * Offset content rect by half the border-separation value,
//             * in addition to the border and padding values. Note:
//             * border-separate should only be specified on the table object,
//             * but it inherits.
//             */
//            int iSep = borderSeparation.getIPD().getLength().getValue();
//            this.startAdjust = iSep / 2 + bp.getBorderStartWidth(false)
//                               + bp.getPaddingStart(false);
//
//            this.widthAdjust = startAdjust + iSep - iSep / 2
//                               + bp.getBorderEndWidth(false)
//                               + bp.getPaddingEnd(false);
//
//            // Offset of content rectangle in the block-progression direction
//            int bSep = borderSeparation.getBPD().getLength().getValue();
//            this.beforeOffset = bSep / 2
//                                + bp.getBorderBeforeWidth(false)
//                                + bp.getPaddingBefore(false);
//
//        } else {
//            // System.err.println("Collapse borders");
//            /*
//             * Hard case.
//             * Cell border is combination of other cell borders, or table
//             * border for edge cells. Also seems to border values specified
//             * on row and column FO in the table (if I read CR correclty.)
//             */
//
//            // Set up before and after borders, taking into account row
//            // and table border properties.
//            // ??? What about table-body, header,footer
//
//            /*
//             * We can't calculate before and after because we aren't sure
//             * whether this row will be the first or last in its area, due
//             * to redoing break decisions (at least in the "new" architecture.)
//             * So in the general case, we will calculate two possible values:
//             * the first/last one and the "middle" one.
//             * Example: border-before
//             * 1. If the cell is in the first row in the first table body, it
//             * will combine with the last row of the header, or with the
//             * top (before) table border if there is no header.
//             * 2. Otherwise there are two cases:
//             * a. the row is first in its (non-first) Area.
//             * The border can combine with either:
//             * i.  the last row of table-header and its cells, or
//             * ii. the table before border (no table-header or it is
//             * omitted on non-first Areas).
//             * b. the row isn't first in its Area.
//             * The border combines with the border of the previous
//             * row and the cells which end in that row.
//             */
//
//            /*
//             * if-first
//             * Calculate the effective border of the cell before-border,
//             * it's parent row before-border, the last header row after-border,
//             * the after border of the cell(s) which end in the last header
//             * row.
//             */
//            /*
//             * if-not-first
//             * Calculate the effective border of the cell before-border,
//             * it's parent row before-border, the previous row after-border,
//             * the after border of the cell(s) which end in the previous
//             * row.
//             */
//
//
//            /* ivan demakov */
//            int borderStart = bp.getBorderStartWidth(false);
//            int borderEnd = bp.getBorderEndWidth(false);
//            int borderBefore = bp.getBorderBeforeWidth(false);
//            int borderAfter = bp.getBorderAfterWidth(false);
//
//            this.startAdjust = borderStart / 2 + bp.getPaddingStart(false);
//
//            this.widthAdjust = startAdjust + borderEnd / 2
//                               + bp.getPaddingEnd(false);
//            this.beforeOffset = borderBefore / 2 + bp.getPaddingBefore(false);
//            // Half border height to fix overestimate of area size!
//            this.borderHeight = (borderBefore + borderAfter) / 2;
//        }
//    }

    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * @return the "column-number" property.
     */
    public int getColumnNumber() {
        return columnNumber.getValue();
    }

    /** @return true if "empty-cells" is "show" */
    public boolean showEmptyCells() {
        return (this.emptyCells == EN_SHOW);
    }
    
    /**
     * @return the "id" property.
     */
    public String getId() {
        return id;
    }

    /**
     * @return the "number-columns-spanned" property.
     */
    public int getNumberColumnsSpanned() {
        return Math.max(numberColumnsSpanned.getValue(), 1);
    }

    /**
     * @return the "number-rows-spanned" property.
     */
    public int getNumberRowsSpanned() {
        return Math.max(numberRowsSpanned.getValue(), 1);
    }
    
    /**
     * @return the "block-progression-dimension" property.
     */
    public LengthRangeProperty getBlockProgressionDimension() {
        return blockProgressionDimension;
    }

    /** @return the display-align property. */
    public int getDisplayAlign() {
        return displayAlign;
    }
    
    /** @return true if the cell starts a row. */
    public boolean startsRow() {
        return (startsRow == EN_TRUE);
    }
    
    /** @return true if the cell ends a row. */
    public boolean endsRow() {
        return (endsRow == EN_TRUE);
    }
    
    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "table-cell";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE_CELL;
    }
}
