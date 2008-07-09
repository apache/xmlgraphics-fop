/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.render.rtf.rtflib.tools;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.pagination.PageSequence;

/**
 * PercentBaseContext implementation to track base widths for percentage calculations.
 */
public class PercentContext implements PercentBaseContext {
    private static Log log = LogFactory.getLog(PercentContext.class);

    /** Map containing the FObj and its width */
    private Map lengthMap = new java.util.HashMap();

    /** Map containing the Tables and their table units */
    private Map tableUnitMap = new java.util.HashMap();

    /** Variable to check if a base width is set */
    private boolean baseWidthSet = false;

    /**
     * Returns the available width for a specific FObj
     *
     * @param lengthBase
     *                lengthBase not used
     * @param fobj
     *                the FObj
     * @return Available Width
     */
    public int getBaseLength(int lengthBase, FObj fobj) {
        if (fobj == null) {
            return 0;
        }
        switch (lengthBase) {
        case LengthBase.CONTAINING_BLOCK_WIDTH:
        case LengthBase.PARENT_AREA_WIDTH:
        case LengthBase.CONTAINING_REFAREA_WIDTH:
            Object width = lengthMap.get(fobj);
            if (width != null) {
                return Integer.parseInt(width.toString());
            } else {
                return -1;
            }
        case LengthBase.TABLE_UNITS:
            Object unit = tableUnitMap.get(fobj);
            return (unit != null) ? ((Integer)unit).intValue() : 0;
        default:
            log.error(new Exception("Unsupported base type for LengthBase:" + lengthBase));
            return 0;
        }
    }

    /**
     * Elements having a width property can call this function if their width is
     * calculated in RTFHandler
     *
     * @param fobj the FObj
     * @param width width of the FObj (in millipoints)
     */
    public void setDimension(FObj fobj, int width) {
        // TODO ACCEPT only objects above for setting a width
        if (fobj instanceof PageSequence) {
            baseWidthSet = true;
        }
        // width in mpt
        lengthMap.put(fobj, new Integer(width));
    }

    /**
     * Records the calculated table unit for a given table.
     * @param table the table for which the table unit is set
     * @param tableUnit the table unit value (in millipoints)
     */
    public void setTableUnit(Table table, int tableUnit) {
        tableUnitMap.put(table, new Integer(tableUnit));
    }

    /**
     * Searches for the parent object of fobj.
     */
    private Integer findParent(FONode fobj) {
        if (fobj.getRoot() != fobj) {
            if (lengthMap.containsKey(fobj)) {
                return new Integer(lengthMap.get(fobj).toString());
            } else {
                return findParent(fobj.getParent());
            }
        } else {
            log.error("Base Value for element " + fobj.getName() + " not found");
            return new Integer(-1);
        }
    }

    /**
     * Elements willing to use this context have to register themselves by
     * calling this function.
     *
     * @param fobj the FObj
     */
    public void setDimension(FObj fobj) {
        if (baseWidthSet) {
            Integer width = findParent(fobj.getParent());
            if (width.intValue() != -1) {
                lengthMap.put(fobj, width);
            }
        }
    }
}
