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
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.*;

public class TableColumn extends FObj {

    ColorType backgroundColor;

    int columnWidth;
    int columnOffset;
    int numColumnsRepeated;
    int iColumnNumber;

    boolean setup = false;

    AreaContainer areaContainer;

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new TableColumn(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new TableColumn.Maker();
    }

    public TableColumn(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);
        this.name = "fo:table-column";
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public int getColumnNumber() {
        return iColumnNumber;
    }

    public int getNumColumnsRepeated() {
        return numColumnsRepeated;
    }

    public void doSetup(Area area) throws FOPException {

        // Common Border, Padding, and Background Properties
        // only background apply, border apply if border-collapse
        // is collapse.
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // this.properties.get("column-width");
        // this.properties.get("number-columns-repeated");
        // this.properties.get("number-columns-spanned");
        // this.properties.get("visibility");

        this.iColumnNumber =
	    this.properties.get("column-number").getNumber().intValue();

        this.numColumnsRepeated =
            this.properties.get("number-columns-repeated").getNumber().intValue();

        this.backgroundColor =
            this.properties.get("background-color").getColorType();

        this.columnWidth =
            this.properties.get("column-width").getLength().mvalue();

        // initialize id
        String id = this.properties.get("id").getString();
        area.getIDReferences().initializeID(id, area);

        setup = true;
    }

    public Status layout(Area area) throws FOPException {
        if (this.marker == BREAK_AFTER) {
            return new Status(Status.OK);
        }

        if (this.marker == START) {
            if (!setup) {
                doSetup(area);
            }
        }

        // KL: don't take table borders into account!
        this.areaContainer =
            new AreaContainer(propMgr.getFontState(area.getFontInfo()),
                              columnOffset /* - area.getBorderLeftWidth() */,
                              /* -area.getBorderTopWidth() */
        0, columnWidth, area.getContentHeight(), Position.RELATIVE);
        // area.getHeight(), Position.RELATIVE);
        areaContainer.foCreator = this;    // G Seshadri
        areaContainer.setPage(area.getPage());
        areaContainer.setBorderAndPadding(propMgr.getBorderAndPadding());
        areaContainer.setBackgroundColor(this.backgroundColor);
        areaContainer.setHeight(area.getHeight());
        area.addChild(areaContainer);

        return new Status(Status.OK);
    }

    public void setColumnOffset(int columnOffset) {
        this.columnOffset = columnOffset;
    }

    public void setHeight(int height) {
        areaContainer.setMaxHeight(height);
        areaContainer.setHeight(height);
    }

}
