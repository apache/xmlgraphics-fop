/*
 * $Id: Column.java,v 1.5 2003/03/07 07:58:51 jeremias Exp $
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
package org.apache.fop.layoutmgr.table;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyManager;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.BreakPoss;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonBackground;

/**
 * LayoutManager for a table-column FO.
 * The table creates an area for the table-column background, this class
 * is used to do the area creation. This is used during the layout to handle
 * column properties.
 */
public class Column extends AbstractLayoutManager {
    private int columnWidth;
    private CommonBorderAndPadding borderProps = null;
    private CommonBackground backgroundProps;

    /**
     * Create a new column layout manager.
     */
    public Column() {
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#setFObj(FObj)
     */
    public void setFObj(FObj fobj) {
        super.setFObj(fobj);
        columnWidth = ((TableColumn)fobj).getColumnWidth();
    }

    /**
     * @see org.apache.fop.layoutmgr.AbstractLayoutManager#initProperties(PropertyManager)
     */
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

        if (backgroundProps != null) {
            TraitSetter.addBackground(curBlockArea, backgroundProps);
        }
        return curBlockArea;
    }
}

