/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

// Java
import java.util.List;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.table.Cell;
import org.apache.fop.fo.properties.CommonBorderAndPadding;

/**
 * Class modelling the fo:table-cell object.
 * @todo check need for all instance variables stored here
 */
public class TableCell extends FObj {

    // private int spaceBefore;
    // private int spaceAfter;
    private ColorType backgroundColor;

    private int numColumnsSpanned;
    private int numRowsSpanned;
    private int iColNumber = -1;    // uninitialized

    /** used for FO validation */
    private boolean blockItemFound = false;

    /**
     * Offset of content rectangle in inline-progression-direction,
     * relative to table.
     */
    protected int startOffset;

    /**
     * Dimension of allocation rectangle in inline-progression-direction,
     * determined by the width of the column(s) occupied by the cell
     */
    protected int width;

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

    /** Minimum content height of cell. */
    protected int minCellHeight = 0;

    /** Height of cell */
    protected int height = 0;

    /** Ypos of cell ??? */
    protected int top;

    /** corresponds to display-align property */
    protected int verticalAlign;

    /** is this cell relatively aligned? */
    protected boolean bRelativeAlign = false;

    // boolean setup = false;
    private boolean bSepBorders = true;

    /**
     * Set to true if all content completely laid out.
     */
    private boolean bDone = false;

    /**
     * Border separation value in the block-progression dimension.
     * Used in calculating cells height.
     */
    private int borderSeparation = 0;

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableCell(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        this.iColNumber =
            propertyList.get(PR_COLUMN_NUMBER).getNumber().intValue();
        if (iColNumber < 0) {
            iColNumber = 0;
        }
        this.numColumnsSpanned =
            this.propertyList.get(PR_NUMBER_COLUMNS_SPANNED).getNumber().intValue();
        if (numColumnsSpanned < 1) {
            numColumnsSpanned = 1;
        }
        this.numRowsSpanned =
            this.propertyList.get(PR_NUMBER_ROWS_SPANNED).getNumber().intValue();
        if (numRowsSpanned < 1) {
            numRowsSpanned = 1;
        }

        this.backgroundColor =
            this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();

        bSepBorders = (getPropEnum(PR_BORDER_COLLAPSE) == BorderCollapse.SEPARATE);

        calcBorders(propMgr.getBorderAndPadding());

        // Vertical cell alignment
        verticalAlign = getPropEnum(PR_DISPLAY_ALIGN);
        if (verticalAlign == DisplayAlign.AUTO) {
            // Depends on all cells starting in row
            bRelativeAlign = true;
            verticalAlign = getPropEnum(PR_RELATIVE_ALIGN);
        } else {
            bRelativeAlign = false;    // Align on a per-cell basis
        }

        this.minCellHeight = getPropLength(PR_HEIGHT);
        getFOEventHandler().startCell(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (%block;)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
        if (nsURI == FO_URI && localName.equals("marker")) {
            if (blockItemFound) {
               nodesOutOfOrderError(loc, "fo:marker", "(%block;)");
            }
        } else if (!isBlockItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        } else {
            blockItemFound = true;
        }
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * FOEventHandler that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#end
     */
    protected void endOfNode() throws SAXParseException {
        if (!blockItemFound) {
            missingChildElementError("marker* (%block;)+");
        }
        getFOEventHandler().endCell(this);
    }

    /**
     * Set position relative to table (set by body?)
     */
    public void setStartOffset(int offset) {
        startOffset = offset;
    }

    /**
     * Sets the width of the cell. Initially this width is the same as the
     * width of the column containing this cell, or the sum of the spanned
     * columns if numColumnsSpanned > 1
     * @param width the width of the cell (in millipoints ??)
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return number of the column containing this cell
     */
    public int getColumnNumber() {
        return iColNumber;
    }

    /**
     * @return the number of columns spanned by this cell
     */
    public int getNumColumnsSpanned() {
        return numColumnsSpanned;
    }

    /**
     * @return the number of rows spanned by this cell
     */
    public int getNumRowsSpanned() {
        return numRowsSpanned;
    }

    /**
     * Calculate cell border and padding, including offset of content
     * rectangle from the theoretical grid position.
     */
    private void calcBorders(CommonBorderAndPadding bp) {
        if (this.bSepBorders) {
            /*
             * Easy case.
             * Cell border is the property specified directly on cell.
             * Offset content rect by half the border-separation value,
             * in addition to the border and padding values. Note:
             * border-separate should only be specified on the table object,
             * but it inherits.
             */
            int iSep = getPropLength(PR_BORDER_SEPARATION | 
                CP_INLINE_PROGRESSION_DIRECTION);
            this.startAdjust = iSep / 2 + bp.getBorderStartWidth(false)
                               + bp.getPaddingStart(false);

            this.widthAdjust = startAdjust + iSep - iSep / 2
                               + bp.getBorderEndWidth(false)
                               + bp.getPaddingEnd(false);

            // Offset of content rectangle in the block-progression direction
            borderSeparation = getPropLength(PR_BORDER_SEPARATION | 
                CP_BLOCK_PROGRESSION_DIRECTION);
            this.beforeOffset = borderSeparation / 2
                                + bp.getBorderBeforeWidth(false)
                                + bp.getPaddingBefore(false);

        } else {
            // System.err.println("Collapse borders");
            /*
             * Hard case.
             * Cell border is combination of other cell borders, or table
             * border for edge cells. Also seems to border values specified
             * on row and column FO in the table (if I read CR correclty.)
             */

            // Set up before and after borders, taking into account row
            // and table border properties.
            // ??? What about table-body, header,footer

            /*
             * We can't calculate before and after because we aren't sure
             * whether this row will be the first or last in its area, due
             * to redoing break decisions (at least in the "new" architecture.)
             * So in the general case, we will calculate two possible values:
             * the first/last one and the "middle" one.
             * Example: border-before
             * 1. If the cell is in the first row in the first table body, it
             * will combine with the last row of the header, or with the
             * top (before) table border if there is no header.
             * 2. Otherwise there are two cases:
             * a. the row is first in its (non-first) Area.
             * The border can combine with either:
             * i.  the last row of table-header and its cells, or
             * ii. the table before border (no table-header or it is
             * omitted on non-first Areas).
             * b. the row isn't first in its Area.
             * The border combines with the border of the previous
             * row and the cells which end in that row.
             */

            /*
             * if-first
             * Calculate the effective border of the cell before-border,
             * it's parent row before-border, the last header row after-border,
             * the after border of the cell(s) which end in the last header
             * row.
             */
            /*
             * if-not-first
             * Calculate the effective border of the cell before-border,
             * it's parent row before-border, the previous row after-border,
             * the after border of the cell(s) which end in the previous
             * row.
             */


            /* ivan demakov */
            int borderStart = bp.getBorderStartWidth(false);
            int borderEnd = bp.getBorderEndWidth(false);
            int borderBefore = bp.getBorderBeforeWidth(false);
            int borderAfter = bp.getBorderAfterWidth(false);

            this.startAdjust = borderStart / 2 + bp.getPaddingStart(false);

            this.widthAdjust = startAdjust + borderEnd / 2
                               + bp.getPaddingEnd(false);
            this.beforeOffset = borderBefore / 2 + bp.getPaddingBefore(false);
            // Half border height to fix overestimate of area size!
            this.borderHeight = (borderBefore + borderAfter) / 2;
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) {
        Cell clm = new Cell(this);
        list.add(clm);
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:table-cell";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE_CELL;
    }
}
