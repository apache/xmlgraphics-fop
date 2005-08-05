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

/* $Id: TableContext.java 227252 2005-08-03 19:30:55Z jeremias $ */

package org.apache.fop.render.rtf.rtflib.tools;

import java.util.List;

import org.apache.commons.logging.impl.SimpleLog;
import org.apache.commons.logging.Log;
import org.apache.fop.render.rtf.rtflib.rtfdoc.ITableColumnsInfo;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;


/** Used when handling fo:table to hold information to build the table.
 *
 *  Contributor(s):
 *  @author Bertrand Delacretaz <bdelacretaz@codeconsult.ch>
 *  @author Trembicki-Guy, Ed <GuyE@DNB.com>
 *  @author Boris Poudérous <boris.pouderous@eads-telecom.com>
 *  @author Peter Herweg <pherweg@web.de>
 *
 *  This class was originally developed for the JFOR project and
 *  is now integrated into FOP.
 */

public class TableContext implements ITableColumnsInfo {
    private final Log log = new SimpleLog("FOP/RTF");
    private final BuilderContext context;
    private final List colWidths = new java.util.ArrayList();
    private int colIndex;

    /**
     * Added by Peter Herweg on 2002-06-29
     * This ArrayList contains one element for each column in the table.
     * value == 0 means there is no row-spanning
     * value >  0 means there is row-spanning
     * Each value in the list is decreased by 1 after each finished table-row
     */
    private final List colRowSpanningNumber = new java.util.ArrayList();

    /**
     * Added by Peter Herweg on 2002-06-29
     * If there has a vertical merged cell to be created, its attributes are
     * inherited from the corresponding MERGE_START-cell.
     * For this purpose the attributes of a cell are stored in this array, as soon
     * as a number-rows-spanned attribute has been found.
     */
    private final List colRowSpanningAttrs = new java.util.ArrayList();

    private boolean bNextRowBelongsToHeader = false;

    public void setNextRowBelongsToHeader(boolean value) {
        this.bNextRowBelongsToHeader = value;
    }

    public boolean getNextRowBelongsToHeader() {
        return bNextRowBelongsToHeader;
    }

    public TableContext(BuilderContext ctx) {
        context = ctx;
    }

    public void setNextColumnWidth(Float width)
            throws Exception {
        colWidths.add(width);
    }

    //Added by Peter Herweg on 2002-06-29
    public RtfAttributes getColumnRowSpanningAttrs() {
        return (RtfAttributes)colRowSpanningAttrs.get(colIndex);
    }

    //Added by Peter Herweg on 2002-06-29
    public Integer getColumnRowSpanningNumber() {
        return (Integer)colRowSpanningNumber.get(colIndex);
    }

    //Added by Peter Herweg on 2002-06-29
    public void setCurrentColumnRowSpanning(Integer iRowSpanning,  RtfAttributes attrs)
            throws Exception {

        if (colIndex < colRowSpanningNumber.size()) {
            colRowSpanningNumber.set(colIndex, iRowSpanning);
            colRowSpanningAttrs.set(colIndex, attrs);
        } else {
            colRowSpanningNumber.add(iRowSpanning);
            colRowSpanningAttrs.add(colIndex, attrs);
        }
    }

    //Added by Peter Herweg on 2002-06-29
    public void setNextColumnRowSpanning(Integer iRowSpanning,
            RtfAttributes attrs) {
        colRowSpanningNumber.add(iRowSpanning);
        colRowSpanningAttrs.add(colIndex, attrs);
    }

    /**
     * Added by Peter Herweg on 2002-06-29
     * This function is called after each finished table-row.
     * It decreases all values in colRowSpanningNumber by 1. If a value
     * reaches 0 row-spanning is finished, and the value won't be decreased anymore.
     */
    public void decreaseRowSpannings() {
        for (int z = 0; z < colRowSpanningNumber.size(); ++z) {
            Integer i = (Integer)colRowSpanningNumber.get(z);

            if (i.intValue() > 0) {
                i = new Integer(i.intValue() - 1);
            }

            colRowSpanningNumber.set(z, i);

            if (i.intValue() == 0) {
                colRowSpanningAttrs.set(z, null);
            }
        }
    }

    /**
     * Reset the column iteration index, meant to be called when creating a new row
     * The 'public' modifier has been added by Boris Poudérous for
     * 'number-columns-spanned' processing
     */
    public void selectFirstColumn() {
        colIndex = 0;
    }

    /**
     * Increment the column iteration index
     * The 'public' modifier has been added by Boris Poudérous for
     * 'number-columns-spanned' processing
     */
    public void selectNextColumn() {
        colIndex++;
    }

    /**
     * Get current column width according to column iteration index
     * @return INVALID_COLUMN_WIDTH if we cannot find the value
     * The 'public' modifier has been added by Boris Poudérous for
     * 'number-columns-spanned' processing
     */
    public float getColumnWidth() {
        try {
            return ((Float)colWidths.get(colIndex)).floatValue();
        } catch (IndexOutOfBoundsException ex) {
            // this code contributed by Trembicki-Guy, Ed <GuyE@DNB.com>
            log.warn("fo:table-column width not defined, using " + INVALID_COLUM_WIDTH);
            return INVALID_COLUM_WIDTH;
        }
    }

     /** Added by Boris Poudérous on 07/22/2002 */
     public int getColumnIndex() {
       return colIndex;
     }
     /** - end - */

     /** Added by Boris Poudérous on 07/22/2002 */
     public int getNumberOfColumns() {
       return colWidths.size();
     }
     /** - end - */
}

