/*-- $Id$ --
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

// Java
import java.util.Vector;
import java.util.Enumeration;

public class TableBody extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new TableBody(parent, propertyList);
        }
    }

    public static FObj.Maker maker() {
        return new TableBody.Maker();
    }

    int spaceBefore;
    int spaceAfter;
    ColorType backgroundColor;
    String id;

    Vector columns;

    AreaContainer areaContainer;

    public TableBody(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);
        this.name = "fo:table-body";
    }

    public void setColumns(Vector columns) {
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

            this.spaceBefore = this.properties.get(
                                 "space-before.optimum").getLength().mvalue();
            this.spaceAfter = this.properties.get(
                                "space-after.optimum").getLength().mvalue();
            this.backgroundColor = this.properties.get(
                                     "background-color").getColorType();
            this.id = this.properties.get("id").getString();

            area.getIDReferences().createID(id);

            if (area instanceof BlockArea) {
                area.end();
            }

            //if (this.isInListBody) {
            //startIndent += bodyIndent + distanceBetweenStarts;
            //}

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

        /* Note: the parent FO must be a Table. The parent Area is the Block
         * type area created by the Table, which is also a reference area.
         * The content "width" (IPD) of the TableBody is the same as that
         * of the containing table area, and its relative position is 0,0.
         * Strictly speaking (CR), this FO should generate no areas!
         */
        this.areaContainer = new AreaContainer(
                               propMgr.getFontState(area.getFontInfo()), 0,
                               area.getContentHeight(), area.getContentWidth(), // IPD
                               area.spaceLeft() , Position.RELATIVE);
        areaContainer.foCreator = this; // G Seshadri
        areaContainer.setPage(area.getPage());
        areaContainer.setBackgroundColor(backgroundColor);
        areaContainer.setBorderAndPadding(propMgr.getBorderAndPadding());
        areaContainer.start();

        areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
        areaContainer.setIDReferences(area.getIDReferences());

        Vector keepWith = new Vector();
        int numChildren = this.children.size();
        TableRow lastRow = null;
        boolean endKeepGroup = true;
        for (int i = this.marker; i < numChildren; i++) {
            Object child = children.elementAt(i);
            if (!(child instanceof TableRow)) {
                throw new FOPException("Currently only Table Rows are supported in table body, header and footer");
            }
            TableRow row = (TableRow) child;

            row.setColumns(columns);
            row.doSetup(areaContainer);
            if (row.getKeepWithPrevious().getType() !=
                    KeepValue.KEEP_WITH_AUTO && lastRow != null &&
                    keepWith.indexOf(lastRow) == -1) {
                keepWith.addElement(lastRow);
            } else {
                if (endKeepGroup && keepWith.size() > 0) {
                    keepWith = new Vector();
                }
            }

            Status status;
            if ((status = row.layout(areaContainer)).isIncomplete()) {
                if (status.isPageBreak()) {
                    this.marker = i;
                    area.addChild(areaContainer);
                    //areaContainer.end();

                    area.increaseHeight(areaContainer.getHeight());
                    area.setAbsoluteHeight(
                      areaContainer.getAbsoluteHeight());
                    if (i == numChildren - 1) {
                        this.marker = BREAK_AFTER;
                        if (spaceAfter != 0) {
                            area.increaseHeight(spaceAfter);
                        }
                    }
                    return status;
                }
                if (keepWith.size() > 0) { // && status.getCode() == Status.AREA_FULL_NONE
                    row.removeLayout(areaContainer);
                    for (Enumeration e = keepWith.elements();
                            e.hasMoreElements();) {
                        TableRow tr = (TableRow) e.nextElement();
                        tr.removeLayout(areaContainer);
                        i--;
                    }
                    if (i == 0) {
                        resetMarker();
                        return new Status(Status.AREA_FULL_NONE);
                    }
                }
                this.marker = i;
                if ((i != 0) &&
                        (status.getCode() == Status.AREA_FULL_NONE)) {
                    status = new Status(Status.AREA_FULL_SOME);
                }
                // 								if (i < widows && numChildren >= widows) {
                // 										resetMarker();
                // 										return new Status(Status.AREA_FULL_NONE);
                // 								}
                // 								if (numChildren <= orphans) {
                // 										resetMarker();
                // 										return new Status(Status.AREA_FULL_NONE);
                // 								}
                // 								if (numChildren - i < orphans && numChildren >= orphans) {
                // 										for (int count = i;
                // 														count > numChildren - orphans - 1; count--) {
                // 												row = (TableRow) children.elementAt(count);
                // 												row.removeLayout(areaContainer);
                // 												i--;
                // 										}
                // 										if (i < widows && numChildren >= widows) {
                // 												resetMarker();
                // 												return new Status(Status.AREA_FULL_NONE);
                // 										}
                // 										this.marker = i;
                // 										area.addChild(areaContainer);
                // 										//areaContainer.end();

                // 										area.increaseHeight(areaContainer.getHeight());
                // 										area.setAbsoluteHeight(
                // 											areaContainer.getAbsoluteHeight());
                // 										return new Status(Status.AREA_FULL_SOME);
                // 								}
                if (!((i == 0) &&
                        (areaContainer.getContentHeight() <= 0))) {
                    area.addChild(areaContainer);
                    //areaContainer.end();

                    area.increaseHeight(areaContainer.getHeight());
                    area.setAbsoluteHeight(
                      areaContainer.getAbsoluteHeight());
                }
                return status;
            } else if (status.getCode() == Status.KEEP_WITH_NEXT) {
                keepWith.addElement(row);
                endKeepGroup = false;
            } else {
                endKeepGroup = true;
            }
            lastRow = row;
            area.setMaxHeight(area.getMaxHeight() - spaceLeft +
                              this.areaContainer.getMaxHeight());
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
