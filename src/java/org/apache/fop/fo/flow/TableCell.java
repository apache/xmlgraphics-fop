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

// XML
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;

import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonRelativePosition;

/**
 * Class modelling the fo:table-cell object. See Sec. 6.7.10 of the XSL-FO
 * Standard.
 */
public class TableCell extends FObj {

    // private int spaceBefore;
    // private int spaceAfter;
    private ColorType backgroundColor;

    private int numColumnsSpanned;
    private int numRowsSpanned;
    private int iColNumber = -1;    // uninitialized

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
     * Overriden from FObj.
     * @param attlist Collection of attributes passed to us from the parser.
     * @throws FOPException for FO errors
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        doSetup();    // init some basic property values

        getFOTreeControl().getFOInputHandler().startCell(this);
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

    private void doSetup() {
        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // Common Relative Position Properties
        CommonRelativePosition mRelProps = propMgr.getRelativePositionProps();

        // this.propertyList.get("border-after-precedence");
        // this.propertyList.get("border-before-precendence");
        // this.propertyList.get("border-end-precendence");
        // this.propertyList.get("border-start-precendence");
        // this.propertyList.get("block-progression-dimension");
        // this.propertyList.get("column-number");
        // this.propertyList.get("display-align");
        // this.propertyList.get("relative-align");
        // this.propertyList.get("empty-cells");
        // this.propertyList.get("ends-row");
        // this.propertyList.get("height");
        setupID();
        // this.propertyList.get("number-columns-spanned");
        // this.propertyList.get("number-rows-spanned");
        // this.propertyList.get("starts-row");
        // this.propertyList.get("width");

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

        bSepBorders = (this.propertyList.get(PR_BORDER_COLLAPSE).getEnum()
                       == BorderCollapse.SEPARATE);

        calcBorders(propMgr.getBorderAndPadding());

        // Vertical cell alignment
        verticalAlign = this.propertyList.get(PR_DISPLAY_ALIGN).getEnum();
        if (verticalAlign == DisplayAlign.AUTO) {
            // Depends on all cells starting in row
            bRelativeAlign = true;
            verticalAlign = this.propertyList.get(PR_RELATIVE_ALIGN).getEnum();
        } else {
            bRelativeAlign = false;    // Align on a per-cell basis
        }

        this.minCellHeight =
            this.propertyList.get(PR_HEIGHT).getLength().getValue();
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
            int iSep = propertyList.get(
                    PR_BORDER_SEPARATION | CP_INLINE_PROGRESSION_DIRECTION).getLength().getValue();
            this.startAdjust = iSep / 2 + bp.getBorderStartWidth(false)
                               + bp.getPaddingStart(false);

            this.widthAdjust = startAdjust + iSep - iSep / 2
                               + bp.getBorderEndWidth(false)
                               + bp.getPaddingEnd(false);

            // Offset of content rectangle in the block-progression direction
            borderSeparation = propertyList.get(
                    PR_BORDER_SEPARATION | CP_BLOCK_PROGRESSION_DIRECTION).getLength().getValue();
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
     *
     * @return true (TableCell can contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveTableCell(this);
    }

    protected void end() {
        getFOTreeControl().getFOInputHandler().endCell(this);
    }
}
