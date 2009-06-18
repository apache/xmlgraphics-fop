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
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;

/**
 * Implements the normal "collapse" border model defined in 6.7.10 in XSL 1.0.
 * 
 * TODO Column groups are not yet checked in this algorithm!
 */
public class CollapsingBorderModelEyeCatching extends CollapsingBorderModel {

    public BorderInfo determineWinner(GridUnit currentGridUnit, 
            GridUnit otherGridUnit, int side, int flags) {
        final boolean vertical = isVerticalRelation(side);
        final int otherSide = getOtherSide(side);
        
        //Get cells
        TableCell currentCell = currentGridUnit.getCell();
        TableCell otherCell = null;
        if (otherGridUnit != null) {
            otherCell = otherGridUnit.getCell();
        }
        
        //Get rows
        TableRow currentRow = currentGridUnit.getRow();
        TableRow otherRow = null;
        if (vertical && otherCell != null) {
            otherRow = otherGridUnit.getRow();
        }
        
        //get bodies
        TableBody currentBody = currentGridUnit.getBody();
        TableBody otherBody = null;
        if (otherRow != null) {
            otherBody = otherGridUnit.getBody();
        }

        //get columns
        TableColumn currentColumn = currentGridUnit.getColumn();
        TableColumn otherColumn = null;
        if (otherGridUnit != null) {
            otherColumn = otherGridUnit.getColumn();
        }
        
        //TODO get column groups
        
        //Get table
        Table table = currentGridUnit.getTable();
        
        //----------------------------------------------------------------------
        //We're creating two arrays containing the applicable BorderInfos for
        //each cell in question.
        //0 = cell, 1 = row, 2 = row group (body), 3 = column, 
        //4 = col group (spanned column, see 6.7.3), 5 = table

        BorderInfo[] current = new BorderInfo[6];
        BorderInfo[] other = new BorderInfo[6];
        //cell
        current[0] = currentGridUnit.getOriginalBorderInfoForCell(side);
        if (otherGridUnit != null) {
            other[0] = otherGridUnit.getOriginalBorderInfoForCell(otherSide);
        }
        if ((currentRow != null) 
                && (side == BEFORE 
                    || side == AFTER
                    || (currentGridUnit.getFlag(GridUnit.IN_FIRST_COLUMN) && side == START)
                    || (currentGridUnit.getFlag(GridUnit.IN_LAST_COLUMN) && side == END))) {
            //row
            current[1] = currentRow.getCommonBorderPaddingBackground().getBorderInfo(side);
        }
        if (otherRow != null) {
            //row
            other[1] = otherRow.getCommonBorderPaddingBackground().getBorderInfo(otherSide);
        }
        if (currentBody != null
                && ((side == BEFORE && currentGridUnit.getFlag(GridUnit.FIRST_IN_PART))
                || (side == AFTER && currentGridUnit.getFlag(GridUnit.LAST_IN_PART))
                || (currentGridUnit.getFlag(GridUnit.IN_FIRST_COLUMN) && side == START)
                || (currentGridUnit.getFlag(GridUnit.IN_LAST_COLUMN) && side == END))) {
            //row group (=body, table-header or table-footer)
            current[2] = currentBody.getCommonBorderPaddingBackground().getBorderInfo(side);
        }
        if (otherGridUnit != null
                && otherBody != null
                && ((otherSide == BEFORE && otherGridUnit.getFlag(GridUnit.FIRST_IN_PART))
                    || (otherSide == AFTER && otherGridUnit.getFlag(GridUnit.LAST_IN_PART)))) {
            //row group (=body, table-header or table-footer)
            other[2] = otherBody.getCommonBorderPaddingBackground().getBorderInfo(otherSide);
        }
        if ((side == BEFORE && otherGridUnit == null)
                || (side == AFTER && otherGridUnit == null)
                || (side == START)
                || (side == END)) {
            //column
            current[3] = currentColumn.getCommonBorderPaddingBackground().getBorderInfo(side);
        }
        if (otherColumn != null) {
            //column
            other[3] = otherColumn.getCommonBorderPaddingBackground().getBorderInfo(otherSide);
        }
        //TODO current[4] and other[4] for column groups
        if (otherGridUnit == null
            && ((side == BEFORE && (flags & VERTICAL_START_END_OF_TABLE) > 0)
                    || (side == AFTER && (flags & VERTICAL_START_END_OF_TABLE) > 0)
                    || (side == START)
                    || (side == END))) {
            //table
            current[5] = table.getCommonBorderPaddingBackground().getBorderInfo(side);
        }
        //other[6] is always null, since it's always the same table
        
        BorderInfo resolved = null;
        
        // *** Rule 1 ***
        resolved = doRule1(current, other);
        if (resolved != null) {
            return resolved;
        }
        
        // *** Rule 2 ***
        if (!doRule2(current, other)) {
        }
        
        // *** Rule 3 ***
        resolved = doRule3(current, other);
        if (resolved != null) {
            return resolved;
        }
        
        // *** Rule 4 ***
        resolved = doRule4(current, other);
        if (resolved != null) {
            return resolved;
        }
        
        // *** Rule 5 ***
        resolved = doRule5(current, other);
        if (resolved != null) {
            return resolved;
        }
        
        return null; //no winner, no border
    }

    private BorderInfo doRule1(BorderInfo[] current, BorderInfo[] other) {
        for (int i = 0; i < current.length; i++) {
            if ((current[i] != null) && (current[i].getStyle() == Constants.EN_HIDDEN)) {
                return current[i];
            }
            if ((other[i] != null) && (other[i].getStyle() == Constants.EN_HIDDEN)) {
                return other[i];
            }
        }
        return null;
    }
    
    private boolean doRule2(BorderInfo[] current, BorderInfo[] other) {
        boolean found = false;
        for (int i = 0; i < current.length; i++) {
            if ((current[i] != null) && (current[i].getStyle() != Constants.EN_NONE)) {
                found = true;
                break;
            }
            if ((other[i] != null) && (other[i].getStyle() != Constants.EN_NONE)) {
                found = true;
                break;
            }
        }
        return found;
    }

    private BorderInfo doRule3(BorderInfo[] current, BorderInfo[] other) {
        int width = 0;
        //Find max border width
        for (int i = 0; i < current.length; i++) {
            if ((current[i] != null) && (current[i].getRetainedWidth() > width)) {
                width = current[i].getRetainedWidth();
            }
            if ((other[i] != null) && (other[i].getRetainedWidth() > width)) {
                width = other[i].getRetainedWidth();
            }
        }
        BorderInfo widest = null;
        int count = 0;
        //See if there's only one with the widest border
        for (int i = 0; i < current.length; i++) {
            if ((current[i] != null) && (current[i].getRetainedWidth() == width)) {
                count++;
                if (widest == null) {
                    widest = current[i];
                }
            } else {
                current[i] = null; //Discard the narrower ones
            }
            if ((other[i] != null) && (other[i].getRetainedWidth() == width)) {
                count++;
                if (widest == null) {
                    widest = other[i];
                }
            } else {
                other[i] = null; //Discard the narrower ones
            }
        }
        if (count == 1) {
            return widest;
        } else {
            return null;
        }
    }

    private BorderInfo doRule4(BorderInfo[] current, BorderInfo[] other) {
        int pref = getPreferenceValue(Constants.EN_INSET); //Lowest preference
        //Find highest preference value
        for (int i = 0; i < current.length; i++) {
            if (current[i] != null) {
                int currPref = getPreferenceValue(current[i].getStyle());
                if (currPref > pref) {
                    pref = currPref;
                }
            }
            if (other[i] != null) {
                int currPref = getPreferenceValue(other[i].getStyle());
                if (currPref > pref) {
                    pref = currPref;
                }
            }
        }
        BorderInfo preferred = null;
        int count = 0;
        //See if there's only one with the preferred border style
        for (int i = 0; i < current.length; i++) {
            if (current[i] != null) {
                int currPref = getPreferenceValue(current[i].getStyle());
                if (currPref == pref) {
                    count++;
                    if (preferred == null) {
                        preferred = current[i];
                    }
                    break;
                }
            } else {
                current[i] = null; //Discard the ones that are not preferred
            }
            if (other[i] != null) {
                int currPref = getPreferenceValue(other[i].getStyle());
                if (currPref == pref) {
                    count++;
                    if (preferred == null) {
                        preferred = other[i];
                    }
                    break;
                }
            } else {
                other[i] = null; //Discard the ones that are not preferred
            }
        }
        if (count == 1) {
            return preferred;
        } else {
            return null;
        }
    }

    private BorderInfo doRule5(BorderInfo[] current, BorderInfo[] other) {
        for (int i = 0; i < current.length; i++) {
            if (current[i] != null) {
                return current[i];
            }
            if (other[i] != null) {
                return other[i];
            }
        }
        return null;
    }

}
