/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr.table;

import org.apache.fop.fo.PropertyManager;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.BreakPoss;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;

/**
 * LayoutManager for a table-column FO.
 * The table creates an area for the table-column background, this class
 * is used to do the area creation. This is used during the layout to handle
 * column properties.
 */
public class Column extends AbstractLayoutManager {
    private int columnWidth;
    private BorderAndPadding borderProps = null;
    private BackgroundProps backgroundProps;

    /**
     * Create a new column layout manager.
     *
     * @param fobj the table-column formatting object
     */
    public Column(TableColumn fobj) {
        super(fobj);
        columnWidth = fobj.getColumnWidth();
    }

    protected void initProperties(PropertyManager propMgr) {
        borderProps = propMgr.getBorderAndPadding();
        backgroundProps = propMgr.getBackgroundProps();
    }

    /**
     * Get the next break possibility.
     * Columns do not create or return any areas.
     *
     * @param context the layout context
     * @return the break possibility, always null
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        return null;
    }

    /**
     * Add the areas.
     * Although this adds no areas it is used to add the id
     * reference of the table-column.
     *
     * @param parentIter the position iterator
     * @param layoutContext the context
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        addID();
    }

    /**
     * Get the parent area.
     * This does nothing.
     *
     * @param childArea the child area
     * @return always null
     */
    public Area getParentArea(Area childArea) {
        return null;
    }

    /**
     * Get the width of this column.
     *
     * @return the width of the column
     */
    public int getWidth() {
        return columnWidth;
    }

    /**
     * Create a column area.
     * This area has the background and width set.
     * The Body manager will then set the offset of the column.
     *
     * @return the new column area
     */
    public Area createColumnArea() {
        Area curBlockArea = new Block();

        if(backgroundProps != null) {
            addBackground(curBlockArea, backgroundProps);
        }
        return curBlockArea;
    }
}

