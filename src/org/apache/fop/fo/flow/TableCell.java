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
import org.apache.fop.fo.Status;
import org.apache.fop.fo.properties.BorderCollapse;
import org.apache.fop.fo.properties.DisplayAlign;
import org.apache.fop.fo.properties.Position;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.layout.AreaContainer;
import org.apache.fop.apps.FOPException;

public class TableCell extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                        String systemId, int line, int column)
            throws FOPException {
            return new TableCell(parent, propertyList, systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new TableCell.Maker();
    }

    // int spaceBefore;
    // int spaceAfter;

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

    // public AreaContainer cellArea;
    public java.lang.ref.WeakReference areaContainerRef;

    public TableCell(FObj parent, PropertyList propertyList,
                        String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);
        if (!(parent instanceof TableRow)) {
            throw new FOPException("A table cell must be child of fo:table-row,"
                                   + " not " + parent.getName(), systemId, line, column);
        }
        doSetup();    // init some basic property values
    }

    public String getName() {
        return "fo:table-cell";
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


    public int layout(Area area) throws FOPException {
        int originalAbsoluteHeight = area.getAbsoluteHeight();
        if (this.marker == BREAK_AFTER) {
            return Status.OK;
        }

        if (this.marker == START) {
            // if (!setup) {
            // doSetup(area);
            // }

            // Calculate cell borders
            // calcBorders(propMgr.getBorderAndPadding());

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
        AreaContainer cellArea =
            new AreaContainer(propMgr.getFontState(area.getFontInfo()),
                              startOffset + startAdjust, beforeOffset,
                              width - widthAdjust, spaceLeft,
                              Position.RELATIVE);

        cellArea.foCreator = this;    // G Seshadri
        cellArea.setPage(area.getPage());
        cellArea.setParent(area);
        try {
            cellArea.setBorderAndPadding((BorderAndPadding)
                    propMgr.getBorderAndPadding().clone());
        } catch (CloneNotSupportedException e) {
            System.err.println("Can't clone BorderAndPadding: " + e) ;
            cellArea.setBorderAndPadding(propMgr.getBorderAndPadding());
        }
        cellArea.setBackground(propMgr.getBackgroundProps());
        cellArea.start();

        cellArea.setAbsoluteHeight(area.getAbsoluteHeight());    // ???
        cellArea.setIDReferences(area.getIDReferences());
        // Add adjust for padding and border to fix link alignment!
        cellArea.setTableCellXOffset(startOffset + startAdjust);

        this.areaContainerRef = new java.lang.ref.WeakReference(cellArea);

        int numChildren = this.children.size();
        for (int i = this.marker; bDone==false && i < numChildren; i++) {
            FObj fo = (FObj)children.get(i);
            fo.setIsInTableCell();
            fo.forceWidth(width);    // ???

            // Overflows may cause a row to be re-layedout,
            // need to pass already processed content.
            this.marker = i;

            int status;
            if (Status.isIncomplete((status = fo.layout(cellArea)))) {
                // this.marker = i;
                if ((i == 0) && (status == Status.AREA_FULL_NONE)) {
                    return Status.AREA_FULL_NONE;
                } else {
                    // hani Elabed 11/21/2000
                    area.addChild(cellArea);
                    // area.setAbsoluteHeight(cellArea.getAbsoluteHeight());
                    return Status.AREA_FULL_SOME;
                }
            }

            area.setMaxHeight(area.getMaxHeight() - spaceLeft
                              + cellArea.getMaxHeight());
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

        return Status.OK;
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
        return ((AreaContainer)areaContainerRef.get()).getHeight() + m_borderSeparation - borderHeight;
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
            ((AreaContainer)areaContainerRef.get()).increaseHeight(delta);
        } else if (delta > 0) {
            BorderAndPadding cellBP = ((AreaContainer)areaContainerRef.get()).getBorderAndPadding();
            switch (verticalAlign) {
            case DisplayAlign.CENTER:
                // Increase cell padding before and after and change
                // "Y" position of content rectangle
                ((AreaContainer)areaContainerRef.get()).shiftYPosition(delta / 2);
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
                ((AreaContainer)areaContainerRef.get()).shiftYPosition(delta);
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
             //border-spacing is a shorthand of border-separation
            int iSpacing =
                properties.get("border-spacing.inline-progression-direction").getLength().mvalue();
            if (iSpacing > iSep)
                iSep = iSpacing;
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
            int m_borderSpacing =
                properties.get("border-spacing.block-progression-direction").getLength().mvalue();
            if (m_borderSpacing > m_borderSeparation)
                m_borderSeparation = m_borderSpacing;
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
