/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.layoutmgr.table;

import org.apache.fop.datatypes.Length;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.BreakPoss;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;

/**
 * LayoutManager for a table-column FO.
 * The table creates an area for the table-column background, this class
 * is used to do the area creation. This is used during the layout to handle
 * column properties.
 */
public class Column extends AbstractLayoutManager {
    
    private TableColumn fobj;
    
    private Length columnWidth;

    /**
     * Create a new column layout manager.
     * @param node the table-column FO
     */
    public Column(TableColumn node) {
         super(node);
         fobj = node;
         columnWidth = fobj.getColumnWidth();
    }

    /** @return the table-column FO */
    public TableColumn getFObj() {
        return this.fobj;
    }
    
    /** @return true if the column is the first column */
    public boolean isFirst() {
        return ((TableLayoutManager)getParent()).isFirst(this);
    }
    
    /** @return true if the column is the last column */
    public boolean isLast() {
        return ((TableLayoutManager)getParent()).isLast(this);
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
     * Overrides the default column-with coming from the FO.
     * @param width the new width to use
     */
    public void setWidth(Length width) {
        this.columnWidth = width;
    }
    
    /**
     * Get the width of this column.
     *
     * @return the width of the column
     */
    public Length getWidth() {
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

        TraitSetter.addBackground(curBlockArea, fobj.getCommonBorderPaddingBackground());
        return curBlockArea;
    }
}

