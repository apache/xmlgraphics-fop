/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
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

import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.table.Column;


public class TableColumn extends FObj {

    ColorType backgroundColor;

    Length columnWidthPropVal;
    int columnWidth;
    int columnOffset;
    int numColumnsRepeated;
    int iColumnNumber;

    boolean setup = false;

    public TableColumn(FONode parent) {
        super(parent);
    }

    public LayoutManager getLayoutManager() {
        doSetup();
        return new Column(this);
    }

    public Length getColumnWidthAsLength() {
        return columnWidthPropVal;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    /**
     * Set the column width value in base units which overrides the
     * value from the column-width Property.
     */
    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
    }

    public int getColumnNumber() {
        return iColumnNumber;
    }

    public int getNumColumnsRepeated() {
        return numColumnsRepeated;
    }

    public void doSetup() {

        // Common Border, Padding, and Background Properties
        // only background apply, border apply if border-collapse
        // is collapse.
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // this.properties.get("column-width");
        // this.properties.get("number-columns-repeated");
        // this.properties.get("number-columns-spanned");
        // this.properties.get("visibility");

        iColumnNumber = properties.get("column-number").getNumber().intValue();

        numColumnsRepeated =
            properties.get("number-columns-repeated").getNumber().intValue();

        this.backgroundColor =
            this.properties.get("background-color").getColorType();

        Property prop = this.properties.get("column-width");
        if(prop != null) {
            columnWidthPropVal = properties.get("column-width").getLength();

            // This won't include resolved table-units or % values yet.
            columnWidth = columnWidthPropVal.mvalue();
        } else {
            columnWidth = 300000;
        }

        // initialize id
        setupID();

        setup = true;
    }

}

