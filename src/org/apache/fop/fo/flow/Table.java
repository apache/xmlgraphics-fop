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
import org.apache.fop.datatypes.*;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;
import java.util.Iterator;

public class Table extends FObj {

    private static final int MINCOLWIDTH = 10000; // 10pt
    int breakBefore;
    int breakAfter;
    int spaceBefore;
    int spaceAfter;
    ColorType backgroundColor;
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

    AreaContainer areaContainer;

    public Table(FONode parent) {
        super(parent);
    }

    public Status layout(Area area) throws FOPException {
        if (this.marker == BREAK_AFTER) {
            return new Status(Status.OK);
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
            this.backgroundColor =
                this.properties.get("background-color").getColorType();
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
            if (this.areaContainer
                    == null) {    // check if anything was previously laid out
                area.getIDReferences().createID(id);
            }


            this.marker = 0;

            if (breakBefore == BreakBefore.PAGE) {
                return new Status(Status.FORCE_PAGE_BREAK);
            }

            if (breakBefore == BreakBefore.ODD_PAGE) {
                return new Status(Status.FORCE_PAGE_BREAK_ODD);
            }

            if (breakBefore == BreakBefore.EVEN_PAGE) {
                return new Status(Status.FORCE_PAGE_BREAK_EVEN);
            }

        }

        if ((spaceBefore != 0) && (this.marker == 0)) {
            area.addDisplaySpace(spaceBefore);
        }

        if (marker == 0 && areaContainer == null) {
            // configure id
            area.getIDReferences().configureID(id, area);
        }

        int spaceLeft = area.spaceLeft();
        this.areaContainer =
            new AreaContainer(propMgr.getFontState(area.getFontInfo()), 0, 0,
                              area.getAllocationWidth(), area.spaceLeft(),
                              Position.STATIC);

        areaContainer.foCreator = this;    // G Seshadri
        areaContainer.setPage(area.getPage());
        areaContainer.setBackgroundColor(backgroundColor);
        areaContainer.setBorderAndPadding(propMgr.getBorderAndPadding());
        areaContainer.start();

        areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
        areaContainer.setIDReferences(area.getIDReferences());

        boolean addedHeader = false;
        boolean addedFooter = false;
        int numChildren = this.children.size();

	// Set up the column vector;
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
            if (fo instanceof TableHeader) {
                if (columns.size() == 0) {
                    log.warn("current implementation of tables requires a table-column for each column, indicating column-width");
                    return new Status(Status.OK);
                }
                tableHeader = (TableHeader)fo;
                tableHeader.setColumns(columns);
            } else if (fo instanceof TableFooter) {
                if (columns.size() == 0) {
                    log.warn("current implementation of tables requires a table-column for each column, indicating column-width");
                    return new Status(Status.OK);
                }
                tableFooter = (TableFooter)fo;
                tableFooter.setColumns(columns);
            } else if (fo instanceof TableBody) {
                if (columns.size() == 0) {
                    log.warn("current implementation of tables requires a table-column for each column, indicating column-width");
                    return new Status(Status.OK);
                }
                Status status;
                if (tableHeader != null &&!addedHeader) {
                    if ((status =
                            tableHeader.layout(areaContainer)).isIncomplete()) {
                        tableHeader.resetMarker();
                        return new Status(Status.AREA_FULL_NONE);
                    }
                    addedHeader = true;
                    tableHeader.resetMarker();
                    area.setMaxHeight(area.getMaxHeight() - spaceLeft
                                      + this.areaContainer.getMaxHeight());
                }
                if (tableFooter != null &&!this.omitFooterAtBreak
                        &&!addedFooter) {
                    if ((status =
                            tableFooter.layout(areaContainer)).isIncomplete()) {
                        return new Status(Status.AREA_FULL_NONE);
                    }
                    addedFooter = true;
                    tableFooter.resetMarker();
                }
                //fo.setWidows(widows);
                //fo.setOrphans(orphans);
                ((TableBody)fo).setColumns(columns);

                if ((status = fo.layout(areaContainer)).isIncomplete()) {
                    this.marker = i;
                    if (bodyCount == 0
                            && status.getCode() == Status.AREA_FULL_NONE) {
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
                        area.setAbsoluteHeight(areaContainer.getAbsoluteHeight());
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
                        setupColumnHeights();
                        status = new Status(Status.AREA_FULL_SOME);
                    }
                    return status;
                } else {
                    bodyCount++;
                }
                area.setMaxHeight(area.getMaxHeight() - spaceLeft
                                  + this.areaContainer.getMaxHeight());
                if (tableFooter != null &&!this.omitFooterAtBreak) {
                    // move footer to bottom of area and move up body
                    // space before and after footer will make this wrong
                    ((TableBody)fo).setYPosition(tableFooter.getYPosition());
                    tableFooter.setYPosition(tableFooter.getYPosition()
                                             + ((TableBody)fo).getHeight());
                }
            }
        }

        if (tableFooter != null && this.omitFooterAtBreak) {
            if (tableFooter.layout(areaContainer).isIncomplete()) {
                // this is a problem since we need to remove a row
                // from the last table body and place it on the
                // next page so that it can have a footer at
                // the end of the table.
                log.warn("footer could not fit on page, moving last body row to next page");
                area.addChild(areaContainer);
                area.increaseHeight(areaContainer.getHeight());
                area.setAbsoluteHeight(areaContainer.getAbsoluteHeight());
                if (this.omitHeaderAtBreak) {
                    // remove header, no longer needed
                    tableHeader = null;
                }
                tableFooter.removeLayout(areaContainer);
                tableFooter.resetMarker();
                return new Status(Status.AREA_FULL_SOME);
            }
        }

        if (height != 0)
            areaContainer.setHeight(height);

        setupColumnHeights();

        areaContainer.end();
        area.addChild(areaContainer);

        /* should this be combined into above? */
        area.increaseHeight(areaContainer.getHeight());

        area.setAbsoluteHeight(areaContainer.getAbsoluteHeight());

        if (spaceAfter != 0) {
            area.addDisplaySpace(spaceAfter);
        }

        if (area instanceof BlockArea) {
            area.start();
        }

        if (breakAfter == BreakAfter.PAGE) {
            this.marker = BREAK_AFTER;
            return new Status(Status.FORCE_PAGE_BREAK);
        }

        if (breakAfter == BreakAfter.ODD_PAGE) {
            this.marker = BREAK_AFTER;
            return new Status(Status.FORCE_PAGE_BREAK_ODD);
        }

        if (breakAfter == BreakAfter.EVEN_PAGE) {
            this.marker = BREAK_AFTER;
            return new Status(Status.FORCE_PAGE_BREAK_EVEN);
        }

        return new Status(Status.OK);
    }

    protected void setupColumnHeights() {
	Iterator eCol = columns.iterator();
	while (eCol.hasNext()) {
	    TableColumn c = (TableColumn)eCol.next();
            if ( c != null) {
                c.setHeight(areaContainer.getContentHeight());
            }
        }
    }

    private void findColumns(Area areaContainer) throws FOPException {
	int nextColumnNumber = 1;
	Iterator e = children.iterator();
	while (e.hasNext()) {
            FONode fo = (FONode)e.next();
            if (fo instanceof TableColumn) {
                TableColumn c = (TableColumn)fo;
                c.doSetup(areaContainer);
                int numColumnsRepeated = c.getNumColumnsRepeated();
                int currentColumnNumber = c.getColumnNumber();
		if (currentColumnNumber == 0) {
		    currentColumnNumber = nextColumnNumber;
		}

                for (int j = 0; j < numColumnsRepeated; j++) {
                    if (currentColumnNumber > columns.size()) {
                        columns.ensureCapacity(currentColumnNumber);
                    }
		    if (columns.get(currentColumnNumber - 1) != null) {
			log.warn("More than one column object assigned " +
				 "to column " +
				 currentColumnNumber);
		    }
                    columns.set(currentColumnNumber - 1, c);
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
	Iterator eCol = columns.iterator();
	while (eCol.hasNext()) {
	    TableColumn c = (TableColumn)eCol.next();
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
	eCol = columns.iterator();
	while (eCol.hasNext()) {
	    TableColumn c = (TableColumn)eCol.next();
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
	Iterator eCol = columns.iterator();
	while (eCol.hasNext()) {
	    TableColumn c = (TableColumn)eCol.next();
	    if (c != null) {
		c.layout(tableArea);
	    }
	}
    }


    public int getAreaHeight() {
        return areaContainer.getHeight();
    }

    /**
     * Return the content width of the boxes generated by this table FO.
     */
    public int getContentWidth() {
        if (areaContainer != null)
            return areaContainer.getContentWidth();    // getAllocationWidth()??
        else
            return 0;    // not laid out yet
    }

    public boolean generatesInlineAreas() {
        return false;
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
