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
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.TableLayout;
import org.apache.fop.fo.properties.TableOmitHeaderAtBreak;
import org.apache.fop.fo.properties.TableOmitFooterAtBreak;
import org.apache.fop.fo.properties.BreakBefore;
import org.apache.fop.fo.properties.Position;
import org.apache.fop.fo.properties.BreakAfter;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AreaContainer;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.MarginProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.datatypes.LengthRange;
import org.apache.fop.datatypes.Length;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;

public class Table extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new Table(parent, propertyList, systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new Table.Maker();
    }

    private static final int MINCOLWIDTH = 10000; // 10pt
    int breakBefore;
    int breakAfter;
    int spaceBefore;
    int spaceAfter;
    LengthRange ipd;
    int height;
    String id;
    TableHeader tableHeader = null;
    TableFooter tableFooter = null;
    boolean omitHeaderAtBreak = false;
    boolean omitFooterAtBreak = false;

    ArrayList columns = new ArrayList();
    int bodyCount = 0;
    private boolean bAutoLayout=false;
    private int contentWidth = 0; // Sum of column widths
    /** Optimum inline-progression-dimension */
    private int optIPD;
    /** Minimum inline-progression-dimension */
    private int minIPD;
    /** Maximum inline-progression-dimension */
    private int maxIPD;

    //  public AreaContainer areaContainer;
    public java.lang.ref.WeakReference areaContainerRef;

    public Table(FObj parent, PropertyList propertyList,
                 String systemId, int line, int column) {
        super(parent, propertyList, systemId, line, column);
    }

    public String getName() {
        return "fo:table";
    }

    public int layout(Area area) throws FOPException {
        if (this.marker == BREAK_AFTER) {
            return Status.OK;
        }

        if (this.marker == START) {
            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            // Common Aural Properties
            AuralProps mAurProps = propMgr.getAuralProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Margin Properties-Block
            MarginProps mProps = propMgr.getMarginProps();

            // Common Relative Position Properties
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

            // this.properties.get("block-progression-dimension");
            // this.properties.get("border-after-precendence");
            // this.properties.get("border-before-precedence");
            // this.properties.get("border-collapse");
            // this.properties.get("border-end-precendence");
            // this.properties.get("border-separation");
            // this.properties.get("border-start-precendence");
            // this.properties.get("break-after");
            // this.properties.get("break-before");
            // this.properties.get("id");
            // this.properties.get("inline-progression-dimension");
            // this.properties.get("height");
            // this.properties.get("keep-together");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("table-layout");
            // this.properties.get("table-omit-footer-at-break");
            // this.properties.get("table-omit-header-at-break");
            // this.properties.get("width");
            // this.properties.get("writing-mode");

            this.breakBefore = this.properties.get("break-before").getEnum();
            this.breakAfter = this.properties.get("break-after").getEnum();
            this.spaceBefore =
                this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.properties.get("space-after.optimum").getLength().mvalue();
            this.ipd =
                this.properties.get("inline-progression-dimension").
                getLengthRange();
            this.height = this.properties.get("height").getLength().mvalue();
            this.bAutoLayout = (this.properties.get("table-layout").getEnum() ==
                TableLayout.AUTO);

            this.id = this.properties.get("id").getString();

            this.omitHeaderAtBreak =
                this.properties.get("table-omit-header-at-break").getEnum()
                == TableOmitHeaderAtBreak.TRUE;
            this.omitFooterAtBreak =
                this.properties.get("table-omit-footer-at-break").getEnum()
                == TableOmitFooterAtBreak.TRUE;

            if (area instanceof BlockArea) {
                area.end();
            }
            // check if anything was previously laid out
            if (this.areaContainerRef == null) {
                try {
                    area.getIDReferences().createID(id);
                }
                catch(FOPException e) {
                    if (!e.isLocationSet()) {
                        e.setLocation(systemId, line, column);
                    }
                    throw e;
                }
            }


            this.marker = 0;

            if (breakBefore == BreakBefore.PAGE) {
                return Status.FORCE_PAGE_BREAK;
            }

            if (breakBefore == BreakBefore.ODD_PAGE) {
                return Status.FORCE_PAGE_BREAK_ODD;
            }

            if (breakBefore == BreakBefore.EVEN_PAGE) {
                return Status.FORCE_PAGE_BREAK_EVEN;
            }

        }

        if ((spaceBefore != 0) && (this.marker == 0)) {
            area.addDisplaySpace(spaceBefore);
        }

        if (marker == 0 && this.areaContainerRef == null) {
            // configure id
            area.getIDReferences().configureID(id, area);
        }

        int spaceLeft = area.spaceLeft();
        // Hack alert!
        AreaContainer areaContainer =
            new AreaContainer(propMgr.getFontState(area.getFontInfo()), 0, 0,
                              area.getAllocationWidth(), area.spaceLeft(),
                              Position.STATIC);

        areaContainer.foCreator = this;    // G Seshadri
        areaContainer.setPage(area.getPage());
        areaContainer.setParent(area);
        areaContainer.setBackground(propMgr.getBackgroundProps());
        areaContainer.setBorderAndPadding(propMgr.getBorderAndPadding());
        areaContainer.start();

        areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
        areaContainer.setIDReferences(area.getIDReferences());
        this.areaContainerRef = new java.lang.ref.WeakReference(areaContainer);

        boolean addedHeader = false;
        boolean addedFooter = false;
        int numChildren = this.children.size();

        // Set up the column ArrayList;
        // calculate width of all columns and get total width
        if (columns.size()==0) {
            findColumns(areaContainer);
            if (this.bAutoLayout) {
                log.warn("table-layout=auto is not supported, using fixed!");
            }
            // Pretend it's fixed...
            this.contentWidth =
                calcFixedColumnWidths(areaContainer.getAllocationWidth());
        }
        areaContainer.setAllocationWidth(this.contentWidth);
        layoutColumns(areaContainer);

        for (int i = this.marker; i < numChildren; i++) {
            FONode fo = (FONode)children.get(i);
            if (fo instanceof Marker) {
                 ((Marker)fo).layout(area);
                 continue;
             }
            if (fo instanceof TableHeader) {
                if (columns.size() == 0) {
                    log.warn("current implementation of tables requires a table-column for each column, indicating column-width");
                    return Status.OK;
                }
                tableHeader = (TableHeader)fo;
                tableHeader.setColumns(columns);
            } else if (fo instanceof TableFooter) {
                if (columns.size() == 0) {
                    log.warn("current implementation of tables requires a table-column for each column, indicating column-width");
                    return Status.OK;
                }
                tableFooter = (TableFooter)fo;
                tableFooter.setColumns(columns);
            } else if (fo instanceof TableBody) {
                if (columns.size() == 0) {
                    log.warn("current implementation of tables requires a table-column for each column, indicating column-width");
                    return Status.OK;
                }
                int status;
                if (tableHeader != null &&!addedHeader) {
                    if (Status.isIncomplete((status =
                                                 tableHeader.layout(areaContainer)))) {
                        tableHeader.resetMarker();
                        return Status.AREA_FULL_NONE;
                    }
                    addedHeader = true;
                    tableHeader.resetMarker();
                    area.setMaxHeight(area.getMaxHeight() - spaceLeft
                                      + areaContainer.getMaxHeight());
                }
                if (tableFooter != null &&!this.omitFooterAtBreak
                        &&!addedFooter) {
                    if (Status.isIncomplete((status =
                                                 tableFooter.layout(areaContainer)))) {
                        return Status.AREA_FULL_NONE;
                    }
                    addedFooter = true;
                    tableFooter.resetMarker();
                }
                ((TableBody)fo).setColumns(columns);

                if (Status.isIncomplete((status = fo.layout(areaContainer)))) {
                    this.marker = i;
                    if (bodyCount == 0
                            && status == Status.AREA_FULL_NONE) {
                        if (tableHeader != null)
                            tableHeader.removeLayout(areaContainer);
                        if (tableFooter != null)
                            tableFooter.removeLayout(areaContainer);
                        resetMarker();
                        // status = new Status(Status.AREA_FULL_SOME);
                    }
                    // areaContainer.end();
                    if (areaContainer.getContentHeight() > 0) {
                        area.addChild(areaContainer);
                        area.increaseHeight(areaContainer.getHeight());
                        if (this.omitHeaderAtBreak) {
                            // remove header, no longer needed
                            tableHeader = null;
                        }
                        if (tableFooter != null &&!this.omitFooterAtBreak) {
                            // move footer to bottom of area and move up body
                            ((TableBody)fo).setYPosition(tableFooter.getYPosition());
                            tableFooter.setYPosition(tableFooter.getYPosition()
                                                     + ((TableBody)fo).getHeight());
                        }
                        setupColumnHeights(areaContainer);
                        status = Status.AREA_FULL_SOME;
                        this.areasGenerated++;
                    }
                    return status;
                } else {
                    bodyCount++;
                }
                area.setMaxHeight(area.getMaxHeight() - spaceLeft
                                  + areaContainer.getMaxHeight());
                if (tableFooter != null &&!this.omitFooterAtBreak) {
                    // move footer to bottom of area and move up body
                    // space before and after footer will make this wrong
                    ((TableBody)fo).setYPosition(tableFooter.getYPosition());
                    tableFooter.setYPosition(tableFooter.getYPosition()
                                             + ((TableBody)fo).getHeight());
                }
            }
        }
        this.areasGenerated++;

        if (tableFooter != null && this.omitFooterAtBreak) {
            if (Status.isIncomplete(tableFooter.layout(areaContainer))) {
                // this is a problem since we need to remove a row
                // from the last table body and place it on the
                // next page so that it can have a footer at
                // the end of the table.
                log.warn("footer could not fit on page, moving last body row to next page");
                area.addChild(areaContainer);
                area.increaseHeight(areaContainer.getHeight());
                if (this.omitHeaderAtBreak) {
                    // remove header, no longer needed
                    tableHeader = null;
                }
                tableFooter.removeLayout(areaContainer);
                tableFooter.resetMarker();
                return Status.AREA_FULL_SOME;
            }
        }

        if (height != 0)
            areaContainer.setHeight(height);

        setupColumnHeights(areaContainer);

        areaContainer.end();
        area.addChild(areaContainer);

        /* should this be combined into above? */
        area.increaseHeight(areaContainer.getHeight());

        if (spaceAfter != 0) {
            area.addDisplaySpace(spaceAfter);
        }

        if (area instanceof BlockArea) {
            area.start();
        }

        if (breakAfter == BreakAfter.PAGE) {
            this.marker = BREAK_AFTER;
            return Status.FORCE_PAGE_BREAK;
        }

        if (breakAfter == BreakAfter.ODD_PAGE) {
            this.marker = BREAK_AFTER;
            return Status.FORCE_PAGE_BREAK_ODD;
        }

        if (breakAfter == BreakAfter.EVEN_PAGE) {
            this.marker = BREAK_AFTER;
            return Status.FORCE_PAGE_BREAK_EVEN;
        }

        return Status.OK;
    }

    public void resetMarker() {
        this.areasGenerated=0;
        super.resetMarker();
    }

    protected void setupColumnHeights(AreaContainer areaContainer) {
        for (int i = 0; i < columns.size(); i++) {
            TableColumn c = (TableColumn)columns.get(i);
            if ( c != null) {
                c.setHeight(areaContainer.getContentHeight());
            }
        }
    }

    private void findColumns(Area areaContainer) throws FOPException {
        int nextColumnNumber = 1;
        for (int i = 0; i < children.size(); i++) {
            FONode fo = (FONode)children.get(i);
            if (fo instanceof TableColumn) {
                TableColumn c = (TableColumn)fo;
                c.doSetup(areaContainer);
                int numColumnsRepeated = c.getNumColumnsRepeated();
                int currentColumnNumber = c.getColumnNumber();
                if (currentColumnNumber == 0) {
                    currentColumnNumber = nextColumnNumber;
                }
                if (currentColumnNumber + numColumnsRepeated > columns.size()) {
                    columns.ensureCapacity(currentColumnNumber + numColumnsRepeated);
                }
                for (int j = 0; j < numColumnsRepeated; j++) {
                    if (currentColumnNumber <= columns.size()) {
                        if (columns.get(currentColumnNumber - 1) != null) {
                            log.warn("More than one column object assigned " +
                                     "to column " +
                                     currentColumnNumber);
                        }
                        columns.set(currentColumnNumber - 1, c);
                    } else {
                      columns.add(currentColumnNumber - 1, c);
                    }
                    currentColumnNumber++;
                }
                nextColumnNumber = currentColumnNumber;
            }
        }
    }



    private int calcFixedColumnWidths(int maxAllocationWidth) {
        int nextColumnNumber=1;
        int iEmptyCols=0;
        double dTblUnits=0.0;
        int iFixedWidth=0;
        double dWidthFactor = 0.0;
        double dUnitLength = 0.0;
        double tuMin = 100000.0 ; // Minimum number of proportional units
        for (int i = 0; i < columns.size(); i++) {
            TableColumn c = (TableColumn)columns.get(i);
            if (c == null) {
                log.warn("No table-column specification for column " +
                         nextColumnNumber);
                // What about sizing issues?
                iEmptyCols++;
            }
            else {
                Length colLength = c.getColumnWidthAsLength();
                double tu = colLength.getTableUnits();
                if (tu > 0 && tu < tuMin && colLength.mvalue()==0) {
                    /* Keep track of minimum number of proportional units
                     * in any column which has only proportional units.
                     */
                    tuMin = tu;
                }
                dTblUnits += tu;
                iFixedWidth +=  colLength.mvalue();
            }
            nextColumnNumber++;
        }

        setIPD((dTblUnits > 0.0), maxAllocationWidth);
        if (dTblUnits > 0.0) {
            int iProportionalWidth = 0;
            if (this.optIPD > iFixedWidth) {
                iProportionalWidth = this.optIPD - iFixedWidth;
            }
            else if (this.maxIPD > iFixedWidth) {
                iProportionalWidth = this.maxIPD - iFixedWidth;
            }
            else {
                iProportionalWidth = maxAllocationWidth - iFixedWidth;
            }
            if (iProportionalWidth > 0) {
                dUnitLength = ((double)iProportionalWidth)/dTblUnits;
            }
            else {
                log.error("Sum of fixed column widths " + iFixedWidth +
                          " greater than maximum available IPD " +
                          maxAllocationWidth + "; no space for " +
                          dTblUnits + " proportional units.");
                /* Set remaining proportional units to a number which
                 * will assure the minimum column size for tuMin.
                 */
                dUnitLength = MINCOLWIDTH/tuMin;
                // Reduce fixed column widths by this much???
            }
            //log.debug("1 table-unit = " + dUnitLength + " mpt");
        }
        else {
            /* No proportional units. If minimum IPD is specified, check
             * that sum of column widths > minIPD.
             */
            int iTableWidth = iFixedWidth;
            if (this.minIPD > iFixedWidth) {
                iTableWidth = this.minIPD;
                // Add extra space to each column
                dWidthFactor = (double)this.minIPD/(double)iFixedWidth;
            }
            else if (this.maxIPD < iFixedWidth) {
                // Note: if maxIPD=auto, use maxAllocWidth
                log.warn("Sum of fixed column widths " + iFixedWidth +
                         " greater than maximum specified IPD " + this.maxIPD);
            }
            else if (this.optIPD != -1 && iFixedWidth != this.optIPD) {
                log.warn("Sum of fixed column widths " + iFixedWidth +
                         " differs from specified optimum IPD " + this.optIPD);
            }
        }
        // Now distribute the extra units onto each column and set offsets
        int offset = 0;
        for (int i = 0; i < columns.size(); i++) {
            TableColumn c = (TableColumn)columns.get(i);
            if (c != null) {
                c.setColumnOffset(offset);
                Length l = c.getColumnWidthAsLength();
                if (dUnitLength > 0) {
                    l.resolveTableUnit(dUnitLength);
                }
                // Check minimum values and adjust if necessary
                int colWidth = l.mvalue();
                if (colWidth <= 0) {
                    log.warn("Zero-width table column!");
                }
                if (dWidthFactor > 0.0) {
                    // Increase column sizes to use up extra space
                    colWidth *= dWidthFactor;
                }
                c.setColumnWidth(colWidth);
                offset += colWidth;
            }
        }
        return offset;
    }

    private void layoutColumns(Area tableArea) throws FOPException  {
        for (int i = 0; i < columns.size(); i++) {
            TableColumn c = (TableColumn)columns.get(i);
            if (c != null) {
                c.layout(tableArea);
            }
        }
    }


    public int getAreaHeight() {
        return ((AreaContainer)areaContainerRef.get()).getHeight();
    }

    /**
     * Return the content width of the boxes generated by this table FO.
     */
    public int getContentWidth() {
        if (areaContainerRef != null)
            // getAllocationWidth()??
            return ((AreaContainer)areaContainerRef.get()).getContentWidth();
        else
            return 0;    // not laid out yet
    }

    /**
     * Initialize table inline-progression-properties values
     */
    private void setIPD(boolean bHasProportionalUnits, int maxAllocIPD) {
        boolean bMaxIsSpecified = !this.ipd.getMaximum().getLength().isAuto();
        if (bMaxIsSpecified) {
            this.maxIPD = ipd.getMaximum().getLength().mvalue();
        }
        else {
            this.maxIPD = maxAllocIPD;
        }

        if (ipd.getOptimum().getLength().isAuto()) {
            this.optIPD = -1;
        }
        else {
            this.optIPD = ipd.getMaximum().getLength().mvalue();
        }
        if (ipd.getMinimum().getLength().isAuto()) {
            this.minIPD = -1;
        }
        else {
            this.minIPD = ipd.getMinimum().getLength().mvalue();
        }
        if (bHasProportionalUnits && this.optIPD < 0) {
            if (this.minIPD > 0) {
                if (bMaxIsSpecified) {
                    this.optIPD = (minIPD + maxIPD)/2;
                }
                else {
                    this.optIPD = this.minIPD;
                }
            }
            else if (bMaxIsSpecified) {
                this.optIPD = this.maxIPD;
            }
            else {
                log.error("At least one of minimum, optimum, or maximum " +
                          "IPD must be specified on table.");
                this.optIPD = this.maxIPD;
            }
        }
    }


    // /**
    // * Return the last TableRow in the header or null if no header or
    // * no header in non-first areas.
    // * @param bForInitialArea If true, return the header row for the
    // * initial table area, else for a continuation area, taking into
    // * account the omit-header-at-break property.
    // */
    // TableRow getLastHeaderRow(boolean bForInitialArea) {
    // // Check omit...
    // if ((tableHeader != null)  &&
    // (bForInitialArea || omitHeaderAtBreak == false)) {
    // return tableHeader.children.lastElement();
    // }
    // return null;
    // }

    // /**
    // * Return the first TableRow in the footer or null if no footer or
    // * no footer in non-last areas.
    // * @param bForFinalArea If true, return the footer row for the
    // * final table area, else for a non-final area, taking into
    // * account the omit-footer-at-break property.
    // */
    // TableRow getLastFooterRow(boolean bForFinalArea) {
    // if ((tableFooter != null) &&
    // (bForFinalArea || omitFooterAtBreak == false)) {
    // return tableFooter.children.firstElement();
    // }
    // return null;
    // }


    // /**
    // * Return border information for the side (start/end) of the column
    // * whose number is iColNumber (first column = 1).
    // * ATTENTION: for now we assume columns are in order in the array!
    // */
    // BorderInfo getColumnBorder(BorderInfo.Side side, int iColNumber) {
    // TableColumn col = (TableColumn)columns.get(iColNumber);
    // return col.getBorderInfo(side);
    // }
}
