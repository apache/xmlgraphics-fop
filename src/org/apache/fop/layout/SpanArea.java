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

public class SpanArea extends AreaContainer {

    private int columnCount;
    private int currentColumn = 1;
    private int columnGap = 0;

    // has the area been balanced?
    private boolean isBalanced = false;

    public SpanArea(FontState fontState, int xPosition, int yPosition,
                    int allocationWidth, int maxHeight, int columnCount,
                    int columnGap) {
        super(fontState, xPosition, yPosition, allocationWidth, maxHeight,
              Position.ABSOLUTE);

        this.contentRectangleWidth = allocationWidth;
        this.columnCount = columnCount;
        this.columnGap = columnGap;

        int columnWidth = (allocationWidth - columnGap * (columnCount - 1))
                          / columnCount;
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            int colXPosition = (xPosition
                                + columnIndex * (columnWidth + columnGap));
            int colYPosition = yPosition;
            ColumnArea colArea = new ColumnArea(fontState, colXPosition,
                                                colYPosition, columnWidth,
                                                maxHeight, columnCount);
            addChild(colArea);
            colArea.setColumnIndex(columnIndex + 1);
        }
    }

    public void end() {}

    public void start() {}

    public int spaceLeft() {
        return maxHeight - currentHeight;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getCurrentColumn() {
        return currentColumn;
    }

    public void setCurrentColumn(int currentColumn) {
        if (currentColumn <= columnCount)
            this.currentColumn = currentColumn;
        else
            this.currentColumn = columnCount;
    }

    public AreaContainer getCurrentColumnArea() {
        return (AreaContainer)getChildren().elementAt(currentColumn - 1);
    }

    public boolean isBalanced() {
        return isBalanced;
    }

    public void setIsBalanced() {
        this.isBalanced = true;
    }

    public int getTotalContentHeight() {
        int totalContentHeight = 0;
        for (Enumeration e = getChildren().elements();
                e.hasMoreElements(); ) {
            totalContentHeight +=
                ((AreaContainer)e.nextElement()).getContentHeight();
        }
        return totalContentHeight;
    }

    public int getMaxContentHeight() {
        int maxContentHeight = 0;
        for (Enumeration e = getChildren().elements();
                e.hasMoreElements(); ) {
            AreaContainer nextElm = (AreaContainer)e.nextElement();
            if (nextElm.getContentHeight() > maxContentHeight)
                maxContentHeight = nextElm.getContentHeight();
        }
        return maxContentHeight;
    }

    public void setPage(Page page) {
        this.page = page;
        for (Enumeration e = getChildren().elements();
                e.hasMoreElements(); ) {
            ((AreaContainer)e.nextElement()).setPage(page);
        }
    }

    public boolean isLastColumn() {
        return (currentColumn == columnCount);
    }

}
