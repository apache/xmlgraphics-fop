/*
 * -- $Id$ --
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.*;

import org.xml.sax.Attributes;

public class TableCell extends FObj {

    // int spaceBefore;
    // int spaceAfter;
    ColorType backgroundColor;

    String id;
    int numColumnsSpanned;
    int numRowsSpanned;
    int iColNumber = -1;    // uninitialized

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

    /* For collapsed border style */
    protected int borderHeight = 0;

    /**
     * Minimum ontent height of cell.
     */
    protected int minCellHeight = 0;

    protected int height = 0;
    protected int top;      // Ypos of cell ???
    protected int verticalAlign;
    protected boolean bRelativeAlign = false;

    // boolean setup = false;
    boolean bSepBorders = true;
    
    /**
     * Set to true if all content completely laid out.
     */
    boolean bDone=false;

    /**
     * Border separation value in the block-progression dimension.
     * Used in calculating cells height.
     */
    int m_borderSeparation = 0;

    AreaContainer cellArea;

    public TableCell(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        doSetup();    // init some basic property values
    }

    // Set position relative to table (set by body?)
    public void setStartOffset(int offset) {
        startOffset = offset;
    }

    // Initially same as the column width containg this cell or the
    // sum of the spanned columns if numColumnsSpanned > 1
    public void setWidth(int width) {
        this.width = width;
    }

    public int getColumnNumber() {
        return iColNumber;
    }

    public int getNumColumnsSpanned() {
        return numColumnsSpanned;
    }

    public int getNumRowsSpanned() {
        return numRowsSpanned;
    }

    public void doSetup()    // throws FOPException
     {
        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

        // this.properties.get("border-after-precedence");
        // this.properties.get("border-before-precendence");
        // this.properties.get("border-end-precendence");
        // this.properties.get("border-start-precendence");
        // this.properties.get("block-progression-dimension");
        // this.properties.get("column-number");
        // this.properties.get("display-align");
        // this.properties.get("relative-align");
        // this.properties.get("empty-cells");
        // this.properties.get("ends-row");
        // this.properties.get("height");
        // this.properties.get("id");
        // this.properties.get("number-columns-spanned");
        // this.properties.get("number-rows-spanned");
        // this.properties.get("starts-row");
        // this.properties.get("width");

        this.iColNumber =
            properties.get("column-number").getNumber().intValue();
        if (iColNumber < 0) {
            iColNumber = 0;
        }
        this.numColumnsSpanned =
            this.properties.get("number-columns-spanned").getNumber().intValue();
        if (numColumnsSpanned < 1) {
            numColumnsSpanned = 1;
        }
        this.numRowsSpanned =
            this.properties.get("number-rows-spanned").getNumber().intValue();
        if (numRowsSpanned < 1) {
            numRowsSpanned = 1;
        }

        this.backgroundColor =
            this.properties.get("background-color").getColorType();

        this.id = this.properties.get("id").getString();

        bSepBorders = (this.properties.get("border-collapse").getEnum()
                       == BorderCollapse.SEPARATE);

        calcBorders(propMgr.getBorderAndPadding());

        // Vertical cell alignment
        verticalAlign = this.properties.get("display-align").getEnum();
        if (verticalAlign == DisplayAlign.AUTO) {
            // Depends on all cells starting in row
            bRelativeAlign = true;
            verticalAlign = this.properties.get("relative-align").getEnum();
        } else
            bRelativeAlign = false;    // Align on a per-cell basis

        this.minCellHeight =
            this.properties.get("height").getLength().mvalue();
    }


    public Status layout(Area area) throws FOPException {
        int originalAbsoluteHeight = area.getAbsoluteHeight();
        if (this.marker == BREAK_AFTER) {
            return new Status(Status.OK);
        }

        if (this.marker == START) {
            // if (!setup) {
            // doSetup(area);
            // }

            // Calculate cell borders
            // calcBorders(propMgr.getBorderAndPadding());

            area.getIDReferences().createID(id);

            this.marker = 0;
            this.bDone=false;
        }

        /*
         * if ((spaceBefore != 0) && (this.marker ==0)) {
         * area.increaseHeight(spaceBefore);
         * }
         */

        if (marker == 0) {
            // configure id
            area.getIDReferences().configureID(id, area);
        }

        // int spaceLeft = area.spaceLeft() - m_borderSeparation/2 + borderHeight/2 ;
        int spaceLeft = area.spaceLeft() - m_borderSeparation;
        // The Area position defines the content rectangle! Borders
        // and padding are outside of this rectangle.
        this.cellArea =
            new AreaContainer(propMgr.getFontState(area.getFontInfo()),
                              startOffset + startAdjust, beforeOffset,
                              width - widthAdjust, spaceLeft,
                              Position.RELATIVE);

        cellArea.foCreator = this;    // G Seshadri
        cellArea.setPage(area.getPage());
	try {
	    cellArea.setBorderAndPadding((BorderAndPadding)
				      propMgr.getBorderAndPadding().clone());
	} catch (CloneNotSupportedException e) {
            System.err.println("Can't clone BorderAndPadding: " + e) ;
            cellArea.setBorderAndPadding(propMgr.getBorderAndPadding());
        }
        cellArea.setBackgroundColor(this.backgroundColor);
        cellArea.start();

        cellArea.setAbsoluteHeight(area.getAbsoluteHeight());    // ???
        cellArea.setIDReferences(area.getIDReferences());
        // ******** CHECK THIS: we've changed startOffset (KL)
        cellArea.setTableCellXOffset(startOffset);

        int numChildren = this.children.size();
        for (int i = this.marker; bDone==false && i < numChildren; i++) {
            FObj fo = (FObj)children.get(i);
            fo.setIsInTableCell();
            fo.forceWidth(width);    // ???

            // Overflows may cause a row to be re-layedout,
            // need to pass already processed content.
            this.marker = i;

            Status status;
            if ((status = fo.layout(cellArea)).isIncomplete()) {
                // this.marker = i;
                if ((i == 0) && (status.getCode() == Status.AREA_FULL_NONE)) {
                    return new Status(Status.AREA_FULL_NONE);
                } else {
                    // hani Elabed 11/21/2000
                    area.addChild(cellArea);
                    // area.setAbsoluteHeight(cellArea.getAbsoluteHeight());
                    return new Status(Status.AREA_FULL_SOME);
                }
            }

            area.setMaxHeight(area.getMaxHeight() - spaceLeft
                              + this.cellArea.getMaxHeight());
        }
        this.bDone=true;
        cellArea.end();
        area.addChild(cellArea);

        // Adjust for minimum cell content height
        if (minCellHeight > cellArea.getContentHeight()) {
            cellArea.setHeight(minCellHeight);
        }

        // This is the allocation height of the cell (including borders
        // and padding
        // ALSO need to include offsets if using "separate borders"
        height = cellArea.getHeight();
        top = cellArea.getCurrentYPosition();    // CHECK THIS!!!

        // reset absoluteHeight to beginning of row
        // area.setHeight(cellArea.getHeight() + spaceBefore + spaceAfter);
        // I don't think we should do this here (KL) !!!
        // area.setHeight(cellArea.getHeight());
        // area.setAbsoluteHeight(originalAbsoluteHeight);

        return new Status(Status.OK);
    }

    /**
     * Return the allocation height of the cell area.
     * Note: called by TableRow.
     * We adjust the actual allocation height of the area by the value
     * of border separation (for separate borders) or border height
     * adjustment for collapse style (because current scheme makes cell
     * overestimate the allocation height).
     */
    public int getHeight() {
        return cellArea.getHeight() + m_borderSeparation - borderHeight;
    }

    /**
     * Set the final size of cell content rectangles to the actual row height
     * and to vertically align the actual content within the cell rectangle.
     * @param h Height of this row in the grid  which is based on
     * the allocation height of all the cells in the row, including any
     * border separation values.
     */
    public void setRowHeight(int h) {
        int delta = h - getHeight();
        // cellArea.increaseHeight(h + borderHeight/2 - cellArea.getHeight());
        if (bRelativeAlign) {
            // Must get info for all cells starting in row!
            // verticalAlign can be BEFORE or BASELINE
            // For now just treat like "before"
            cellArea.increaseHeight(delta);
        } else if (delta > 0) {
            BorderAndPadding cellBP = cellArea.getBorderAndPadding();
            switch (verticalAlign) {
            case DisplayAlign.CENTER:
                // Increase cell padding before and after and change
                // "Y" position of content rectangle
                cellArea.shiftYPosition(delta / 2);
                cellBP.setPaddingLength(BorderAndPadding.TOP,
                                        cellBP.getPaddingTop(false)
                                        + delta / 2);
                cellBP.setPaddingLength(BorderAndPadding.BOTTOM,
                                        cellBP.getPaddingBottom(false)
                                        + delta - delta / 2);
                break;
            case DisplayAlign.AFTER:
                // Increase cell padding before and change
                // "Y" position of content rectangle
                cellBP.setPaddingLength(BorderAndPadding.TOP,
                                        cellBP.getPaddingTop(false) + delta);
                cellArea.shiftYPosition(delta);
                break;
            case DisplayAlign.BEFORE:
                // cellArea.increaseHeight(delta);
                cellBP.setPaddingLength(BorderAndPadding.BOTTOM,
                                        cellBP.getPaddingBottom(false)
                                        + delta);

            default:    // OK
                break;
            }
        }
    }

    /**
     * Calculate cell border and padding, including offset of content
     * rectangle from the theoretical grid position.
     */
    private void calcBorders(BorderAndPadding bp) {
        if (this.bSepBorders) {
            /*
             * Easy case.
             * Cell border is the property specified directly on cell.
             * Offset content rect by half the border-separation value,
             * in addition to the border and padding values. Note:
             * border-separate should only be specified on the table object,
             * but it inherits.
             */
            int iSep =
                properties.get("border-separation.inline-progression-direction").getLength().mvalue();
            this.startAdjust = iSep / 2 + bp.getBorderLeftWidth(false)
                               + bp.getPaddingLeft(false);
            /*
             * int contentOffset = iSep + bp.getBorderStartWidth(false) +
             * bp.getPaddingStart(false);
             */
            this.widthAdjust = startAdjust + iSep - iSep / 2
                               + bp.getBorderRightWidth(false)
                               + bp.getPaddingRight(false);
            // bp.getBorderEndWidth(false) + bp.getPaddingEnd(false);
            // Offset of content rectangle in the block-progression direction
            m_borderSeparation =
                properties.get("border-separation.block-progression-direction").getLength().mvalue();
            this.beforeOffset = m_borderSeparation / 2
                                + bp.getBorderTopWidth(false)
                                + bp.getPaddingTop(false);
            // bp.getBorderBeforeWidth(false) +	bp.getPaddingBefore(false);

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
            int borderStart = bp.getBorderLeftWidth(false);
            int borderEnd = bp.getBorderRightWidth(false);
            int borderBefore = bp.getBorderTopWidth(false);
            int borderAfter = bp.getBorderBottomWidth(false);

            this.startAdjust = borderStart / 2 + bp.getPaddingLeft(false);

            this.widthAdjust = startAdjust + borderEnd / 2
                               + bp.getPaddingRight(false);
            this.beforeOffset = borderBefore / 2 + bp.getPaddingTop(false);
            // Half border height to fix overestimate of area size!
            this.borderHeight = (borderBefore + borderAfter) / 2;
        }
    }
    

}
