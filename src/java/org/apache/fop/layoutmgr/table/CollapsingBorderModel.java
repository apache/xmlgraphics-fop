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

package org.apache.fop.layoutmgr.table;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;

/**
 * This class is a superclass for the two collapsing border models defined
 * in the XSL 1.0 specification.
 */
public abstract class CollapsingBorderModel {

    /** before side */
    protected static final int BEFORE = CommonBorderPaddingBackground.BEFORE;
    /** after side */
    protected static final int AFTER = CommonBorderPaddingBackground.AFTER;
    /** start side */
    protected static final int START = CommonBorderPaddingBackground.START;
    /** end side */
    protected static final int END = CommonBorderPaddingBackground.END;
    
    /** Flag: current grid unit is either start or end of the table. */
    public static final int VERTICAL_START_END_OF_TABLE = 1;
    
    /** Indicates that the cell is/starts in the first row being painted on a particular page */
    //public static final int FIRST_ROW_IN_TABLE_PART = 1;
    /** Indicates that the cell is/ends in the last row being painted on a particular page */
    //public static final int LAST_ROW_IN_TABLE_PART  = 2;
    /** Indicates that the cell is/starts in the first row of a body/table-header/table-footer */
    //public static final int FIRST_ROW_IN_GROUP      = 4;
    /** Indicates that the cell is/end in the last row of a body/table-header/table-footer */
    //public static final int LAST_ROW_IN_GROUP       = 8;

    //These statics are used singleton-style. No MT issues here.
    private static CollapsingBorderModel collapse = null;
    private static CollapsingBorderModel collapseWithPrecedence = null;
    
    /**
     * @param borderCollapse border collapse control
     * @return the border model for the cell
     */
    public static CollapsingBorderModel getBorderModelFor(int borderCollapse) {
        switch (borderCollapse) {
            case Constants.EN_COLLAPSE:
                if (collapse == null) {
                    collapse = new CollapsingBorderModelEyeCatching();
                }
                return collapse;
            case Constants.EN_COLLAPSE_WITH_PRECEDENCE:
                if (collapseWithPrecedence == null) {
                    //collapseWithPrecedence = new CollapsingBorderModelWithPrecedence();
                }
                return collapseWithPrecedence;
            default:
                throw new IllegalArgumentException("Illegal border-collapse mode.");
        }
    }
    
    /**
     * @param side the side on the current cell
     * @return the adjacent side on the neighbouring cell
     */
    public static int getOtherSide(int side) {
        switch (side) {
            case CommonBorderPaddingBackground.BEFORE:
                return CommonBorderPaddingBackground.AFTER;
            case CommonBorderPaddingBackground.AFTER:
                return CommonBorderPaddingBackground.BEFORE;
            case CommonBorderPaddingBackground.START:
                return CommonBorderPaddingBackground.END;
            case CommonBorderPaddingBackground.END:
                return CommonBorderPaddingBackground.START;
            default:
                throw new IllegalArgumentException("Illegal parameter: side");
        }
    }
    
    /**
     * @param side the side to investigate
     * @return true if the adjacent cell is before or after
     */
    protected boolean isVerticalRelation(int side) {
        return (side == CommonBorderPaddingBackground.BEFORE 
                || side == CommonBorderPaddingBackground.AFTER);
    }

    
    /**
     * See rule 4 in 6.7.10 for the collapsing border model.
     * @param style the border style to get the preference value for
     * @return the preference value of the style
     */
    public int getPreferenceValue(int style) {
        switch (style) {
            case Constants.EN_DOUBLE: return 0;
            case Constants.EN_SOLID: return -1;
            case Constants.EN_DASHED: return -2;
            case Constants.EN_DOTTED: return -3;
            case Constants.EN_RIDGE: return -4;
            case Constants.EN_OUTSET: return -5;
            case Constants.EN_GROOVE: return -6;
            case Constants.EN_INSET: return -7;
            default: throw new IllegalStateException("Illegal border style: " + style);
        }
    }
    
    /**
     * Determines the winning BorderInfo.
     * @param current grid unit of the current element
     * @param neighbour grid unit of the neighbouring element
     * @return the winning BorderInfo
     */
    public abstract BorderInfo determineWinner(
            GridUnit current, GridUnit neighbour, int side, int flags);
    
}
