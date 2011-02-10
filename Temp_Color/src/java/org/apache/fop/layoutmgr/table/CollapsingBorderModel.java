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
import org.apache.fop.fo.flow.table.BorderSpecification;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;

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
    public/*TODO*/ static int getOtherSide(int side) {
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

    private static int compareInt(int value1, int value2) {
        if (value1 < value2) {
            return -1;
        } else if (value1 == value2) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * See rule 4 in 6.7.10 for the collapsing border model.
     * @param style the border style to get the preference value for
     * @return the preference value of the style
     */
    private static int getStylePreferenceValue(int style) {
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
     * Compares the two given styles (see {@link Constants}).
     *
     * @param style1 a style constant
     * @param style2 another style constant
     * @return a value &lt; 0 if style1 has less priority than style2, 0 if both are
     * equal, a value &gt; 0 if style1 has more priority than style2
     */
    static int compareStyles(int style1, int style2) {
        int value1 = getStylePreferenceValue(style1);
        int value2 = getStylePreferenceValue(style2);
        return compareInt(value1, value2);
    }

    private static int getHolderPreferenceValue(int id) {
        switch (id) {
        case Constants.FO_TABLE_CELL: return 0;
        case Constants.FO_TABLE_ROW: return -1;
        case Constants.FO_TABLE_HEADER:
        case Constants.FO_TABLE_FOOTER:
        case Constants.FO_TABLE_BODY:
            return -2;
        case Constants.FO_TABLE_COLUMN: return -3;
        // TODO colgroup
        case Constants.FO_TABLE: return -4;
        default: throw new IllegalStateException();
        }
    }

    /**
     * Compares the two given FO ids ({@link Constants}.FO*) in terms of border
     * declaration.
     *
     * @param id1 a FO id ({@link Constants#FO_TABLE}, {@link Constants#FO_TABLE_BODY},
     * etc.)
     * @param id2 another FO id
     * @return a value &lt; 0 if id1 has less priority than id2, 0 if both are equal, a
     * value &gt; 0 if id1 has more priority than id2
     */
    static int compareFOs(int id1, int id2) {
        int p1 = getHolderPreferenceValue(id1);
        int p2 = getHolderPreferenceValue(id2);
        return compareInt(p1, p2);
    }

    /**
     * Returns the border which wins the border conflict resolution. In case the two
     * borders are equivalent (identical, or only the color is different), null is
     * returned.
     *
     * @param border1 a border specification
     * @param border2 another border specification
     * @param discard true if the .conditionality component of the border width must be
     * taken into account
     * @return the winning border, null if the two borders are equivalent
     */
    public abstract BorderSpecification determineWinner(BorderSpecification border1,
            BorderSpecification border2, boolean discard);

    /**
     * Returns the border which wins the border conflict resolution. Same as
     * {@link #determineWinner(BorderSpecification, BorderSpecification, boolean)
     * determineWinner(border1, border2, false)}.
     *
     * @param border1 a border specification
     * @param border2 another border specification
     * @return the winning border, null if the two borders are equivalent
     * @see #determineWinner(BorderSpecification,BorderSpecification,boolean)
     */
    public abstract BorderSpecification determineWinner(BorderSpecification border1,
            BorderSpecification border2);

}
