/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;
import java.util.Iterator;

public class TableBody extends FObj {

    int spaceBefore;
    int spaceAfter;
    ColorType backgroundColor;
    String id;

    ArrayList columns;
    RowSpanMgr rowSpanMgr;    // manage information about spanning rows

    AreaContainer areaContainer;

    public TableBody(FONode parent) {
        super(parent);
    }

    public void setColumns(ArrayList columns) {
        this.columns = columns;
    }

    public void setYPosition(int value) {
        areaContainer.setYPosition(value);
    }

    public int getYPosition() {
        return areaContainer.getCurrentYPosition();
    }

    public int getHeight() {
        return areaContainer.getHeight() + spaceBefore + spaceAfter;
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

            // Common Relative Position Properties        
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();
        
            // this.properties.get("id");

            this.spaceBefore =
                this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.properties.get("space-after.optimum").getLength().mvalue();
            this.backgroundColor =
                this.properties.get("background-color").getColorType();
            this.id = this.properties.get("id").getString();

            area.getIDReferences().createID(id);

            if (area instanceof BlockArea) {
                area.end();
            }

            if (rowSpanMgr == null) {
                rowSpanMgr = new RowSpanMgr(columns.size());
            }

            // if (this.isInListBody) {
            // startIndent += bodyIndent + distanceBetweenStarts;
            // }

            this.marker = 0;

        }

        if ((spaceBefore != 0) && (this.marker == 0)) {
            area.increaseHeight(spaceBefore);
        }

        if (marker == 0) {
            // configure id
            area.getIDReferences().configureID(id, area);
        }

        int spaceLeft = area.spaceLeft();

        /*
         * Note: the parent FO must be a Table. The parent Area is the Block
         * type area created by the Table, which is also a reference area.
         * The content "width" (IPD) of the TableBody is the same as that
         * of the containing table area, and its relative position is 0,0.
         * Strictly speaking (CR), this FO should generate no areas!
         */
        this.areaContainer =
            new AreaContainer(propMgr.getFontState(area.getFontInfo()), 0,
                              area.getContentHeight(),
                              area.getContentWidth(),    // IPD
        area.spaceLeft(), Position.RELATIVE);
        areaContainer.foCreator = this;                  // G Seshadri
        areaContainer.setPage(area.getPage());
        areaContainer.setBackgroundColor(backgroundColor);
        areaContainer.setBorderAndPadding(propMgr.getBorderAndPadding());
        areaContainer.start();

        areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
        areaContainer.setIDReferences(area.getIDReferences());

        ArrayList keepWith = new ArrayList();
        int numChildren = this.children.size();
        TableRow lastRow = null;
        boolean endKeepGroup = true;
        for (int i = this.marker; i < numChildren; i++) {
            Object child = children.get(i);
            if (!(child instanceof TableRow)) {
                throw new FOPException("Currently only Table Rows are supported in table body, header and footer");
            }
            TableRow row = (TableRow)child;

            row.setRowSpanMgr(rowSpanMgr);
            row.setColumns(columns);
            row.doSetup(areaContainer);
            if (row.getKeepWithPrevious().getType()
                    != KeepValue.KEEP_WITH_AUTO && lastRow != null
                                                && keepWith.indexOf(lastRow)
                                                   == -1) {
                keepWith.add(lastRow);
            } else {
                if (endKeepGroup && keepWith.size() > 0) {
                    keepWith = new ArrayList();
                }
            }

            Status status;
            if ((status = row.layout(areaContainer)).isIncomplete()) {
                // BUG!!! don't distinguish between break-before and after!
                if (status.isPageBreak()) {
                    this.marker = i;
                    area.addChild(areaContainer);
                    // areaContainer.end();

                    area.increaseHeight(areaContainer.getHeight());
                    area.setAbsoluteHeight(areaContainer.getAbsoluteHeight());
                    if (i == numChildren - 1) {
                        this.marker = BREAK_AFTER;
                        if (spaceAfter != 0) {
                            area.increaseHeight(spaceAfter);
                        }
                    }
                    return status;
                }
                if (keepWith.size()
                        > 0) {    // && status.getCode() == Status.AREA_FULL_NONE
                    // FIXME!!! Handle rows spans!!!
                    row.removeLayout(areaContainer);
                    for (Iterator e = keepWith.iterator();
                            e.hasNext(); ) {
                        TableRow tr = (TableRow)e.next();
                        tr.removeLayout(areaContainer);
                        i--;
                    }
                    if (i == 0) {
                        resetMarker();
                        return new Status(Status.AREA_FULL_NONE);
                    }
                }
                this.marker = i;
                if ((i != 0) && (status.getCode() == Status.AREA_FULL_NONE)) {
                    status = new Status(Status.AREA_FULL_SOME);
                }
                if (!((i == 0) && (areaContainer.getContentHeight() <= 0))) {
                    area.addChild(areaContainer);
                    // areaContainer.end();

                    area.increaseHeight(areaContainer.getHeight());
                    area.setAbsoluteHeight(areaContainer.getAbsoluteHeight());
                }
                return status;
            } else if (status.getCode() == Status.KEEP_WITH_NEXT
                       || rowSpanMgr.hasUnfinishedSpans()) {
                keepWith.add(row);
                endKeepGroup = false;
            } else {
                endKeepGroup = true;
            }
            lastRow = row;
            area.setMaxHeight(area.getMaxHeight() - spaceLeft
                              + this.areaContainer.getMaxHeight());
            spaceLeft = area.spaceLeft();
        }
        area.addChild(areaContainer);
        areaContainer.end();

        area.increaseHeight(areaContainer.getHeight());

        area.setAbsoluteHeight(areaContainer.getAbsoluteHeight());

        if (spaceAfter != 0) {
            area.increaseHeight(spaceAfter);
            area.setMaxHeight(area.getMaxHeight() - spaceAfter);
        }

        if (area instanceof BlockArea) {
            area.start();
        }

        return new Status(Status.OK);
    }

    public void removeLayout(Area area) {
        if (areaContainer != null) {
            area.removeChild(areaContainer);
        }
        if (spaceBefore != 0) {
            area.increaseHeight(-spaceBefore);
        }
        if (spaceAfter != 0) {
            area.increaseHeight(-spaceAfter);
        }
        this.resetMarker();
        this.removeID(area.getIDReferences());
    }

}
