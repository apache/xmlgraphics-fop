/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

// FOP
import org.apache.fop.render.Renderer;
import org.apache.fop.fo.properties.Position;
import org.apache.fop.datatypes.IDReferences;

// Java
import java.util.Vector;
import java.util.Enumeration;

public class ColumnArea extends AreaContainer {

    private int columnIndex;
    private int maxColumns;

    public ColumnArea(FontState fontState, int xPosition, int yPosition,
                      int allocationWidth, int maxHeight, int columnCount) {
        super(fontState, xPosition, yPosition, allocationWidth, maxHeight,
              Position.ABSOLUTE);
        this.maxColumns = columnCount;
        this.setAreaName("normal-flow-ref.-area");
    }

    public void end() {}

    public void start() {}

    public int spaceLeft() {
        return maxHeight - currentHeight;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public void incrementSpanIndex() {
        SpanArea span = (SpanArea)this.parent;
        span.setCurrentColumn(span.getCurrentColumn() + 1);
    }

}
