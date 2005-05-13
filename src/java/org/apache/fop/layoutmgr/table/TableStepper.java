/*
 * Copyright 2005 The Apache Software Foundation.
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.table.TableContentLayoutManager.GridUnitPart;
import org.apache.fop.layoutmgr.table.TableContentLayoutManager.TableContentPosition;
import org.apache.fop.layoutmgr.table.TableContentLayoutManager.TableHFPenaltyPosition;

/**
 * This class processes row groups to create combined element lists for tables.
 */
public class TableStepper {

    /** Logger **/
    private static Log log = LogFactory.getLog(TableStepper.class);

    private TableContentLayoutManager tclm;
    
    private EffRow[] rowGroup;
    private int totalHeight;
    private int activeRow;
    private List[] elementLists;
    private int[] startRow;
    private int[] start;
    private int[] end;
    private int[] widths;
    private int[] baseWidth;
    private int[] borderBefore;
    private int[] borderAfter;
    private boolean rowBacktrackForLastStep;
    
    /**
     * Main constructor
     * @param tclm The parent TableContentLayoutManager
     */
    public TableStepper(TableContentLayoutManager tclm) {
        this.tclm = tclm;
        this.activeRow = 0;
    }
    
    private void setup(int columnCount) {
        elementLists = new List[columnCount];
        startRow = new int[columnCount];
        start = new int[columnCount];
        end = new int[columnCount];
        widths = new int[columnCount];
        baseWidth = new int[columnCount];
        borderBefore = new int[columnCount];
        borderAfter = new int[columnCount];
        Arrays.fill(end, -1);
    }
    
    private EffRow getActiveRow() {
        return rowGroup[activeRow];
    }
    
    private GridUnit getActiveGridUnit(int column) {
        return getActiveRow().getGridUnit(column);
    }
    
    private PrimaryGridUnit getActivePrimaryGridUnit(int column) {
        return getActiveGridUnit(column).getPrimary();
    }
    
    private void calcTotalHeight() {
        totalHeight = 0;
        for (int i = 0; i < rowGroup.length; i++) {
            totalHeight += rowGroup[i].getHeight().opt;
        }
        log.debug("totalHeight=" + totalHeight);
    }
    
    private int getMaxRemainingHeight() {
        int maxW = 0;
        if (!rowBacktrackForLastStep) {
            for (int i = 0; i < widths.length; i++) {
                if (elementLists[i] == null) {
                    continue;
                }
                if (getActivePrimaryGridUnit(i).getCell().getNumberRowsSpanned() > 1) {
                    continue;
                }
                int len = widths[i]; 
                if (len > 0) {
                    len += borderBefore[i] + borderAfter[i]; 
                }
                if (len == rowGroup[activeRow].getHeight().opt) {
                    //row is filled
                    maxW = 0;
                    break;
                }
                maxW = Math.max(maxW, rowGroup[activeRow].getHeight().opt - len);
            }
        }
        for (int i = activeRow + 1; i < rowGroup.length; i++) {
            maxW += rowGroup[i].getHeight().opt;
        }
        //log.debug("maxRemainingHeight=" + maxW);
        return maxW;
    }

    private void setupElementList(int column) {
        GridUnit gu = getActiveGridUnit(column);
        EffRow row = getActiveRow();
        if (gu.isPrimary() && !gu.isEmpty()) {
            PrimaryGridUnit pgu = (PrimaryGridUnit)gu;
            if (row.getExplicitHeight().min > 0) {
                boolean contentsSmaller = ElementListUtils.removeLegalBreaks(
                        pgu.getElements(), row.getExplicitHeight());
                if (contentsSmaller) {
                    List list = new java.util.ArrayList(1);
                    list.add(new KnuthBoxCellWithBPD(
                            row.getExplicitHeight().opt, pgu));
                    elementLists[column] = list;
                } else {
                    //Copy elements (LinkedList) to array lists to improve 
                    //element access performance
                    elementLists[column] = new java.util.ArrayList(pgu.getElements());
                }
            } else {
                //Copy elements (LinkedList) to array lists to improve 
                //element access performance
                elementLists[column] = new java.util.ArrayList(pgu.getElements());
            }
            if (isSeparateBorderModel()) {
                borderBefore[column] = pgu.getBorders().getBorderBeforeWidth(false);
            } else {
                borderBefore[column] = pgu.getBorders().getBorderBeforeWidth(false) / 2;
            }
            start[column] = 0;
            end[column] = -1;
            widths[column] = 0;
            startRow[column] = activeRow;
        }
    }
    
    private void initializeElementLists() {
        for (int i = 0; i < start.length; i++) {
            setupElementList(i);
        }
    }

    /**
     * Creates the combined element list for a row group.
     * @param rowGroup the row group
     * @param maxColumnCount the maximum number of columns to expect
     * @param bodyType Indicates what type of body is processed (boder, header or footer)
     * @return the combined element list
     */
    public LinkedList getCombinedKnuthElementsForRowGroup( 
            EffRow[] rowGroup, int maxColumnCount, int bodyType) {
        this.rowGroup = rowGroup;
        setup(maxColumnCount);
        initializeElementLists();
        calcTotalHeight();
        
        int laststep = 0;
        int step;
        int addedBoxLen = 0;
        LinkedList returnList = new LinkedList();
        while ((step = getNextStep(laststep)) > 0) {
            if (rowBacktrackForLastStep) {
                //Even though we've already switched to the next row, we have to 
                //calculate as if we were still on the previous row
                activeRow--;
            }
            int increase = step - laststep;
            int penaltyLen = step + getMaxRemainingHeight() - totalHeight;
            int boxLen = step - addedBoxLen - penaltyLen;
            addedBoxLen += boxLen;
            
            //Put all involved grid units into a list
            List gridUnitParts = new java.util.ArrayList(maxColumnCount);
            for (int i = 0; i < start.length; i++) {
                if (end[i] >= start[i]) {
                    PrimaryGridUnit pgu = getActivePrimaryGridUnit(i);
                    if (start[i] == 0 && end[i] == 0 
                            && elementLists[i].size() == 1
                            && elementLists[i].get(0) instanceof KnuthBoxCellWithBPD) {
                        //Special case: Cell with fixed BPD
                        gridUnitParts.add(new GridUnitPart(pgu, 
                                0, pgu.getElements().size() - 1));
                    } else {
                        gridUnitParts.add(new GridUnitPart(pgu, start[i], end[i]));
                    }
                }
            }
            //log.debug(">>> guPARTS: " + gridUnitParts);
            
            //Create elements for step
            int effPenaltyLen = penaltyLen;
            if (isSeparateBorderModel()) {
                CommonBorderPaddingBackground borders 
                    = getTableLM().getTable().getCommonBorderPaddingBackground(); 
                effPenaltyLen += borders.getBorderBeforeWidth(false); 
                effPenaltyLen += borders.getBorderAfterWidth(false); 
            }
            TableContentPosition tcpos = new TableContentPosition(getTableLM(), 
                    gridUnitParts, getActiveRow());
            log.debug(" - " + rowBacktrackForLastStep + " - " + activeRow + " - " + tcpos);
            returnList.add(new KnuthBox(boxLen, tcpos, false));
            TableHFPenaltyPosition penaltyPos = new TableHFPenaltyPosition(getTableLM());
            if (bodyType == TableRowIterator.BODY) {
                if (!getTableLM().getTable().omitHeaderAtBreak()) {
                    effPenaltyLen += tclm.getHeaderNetHeight();
                    penaltyPos.headerElements = tclm.getHeaderElements();
                }
                if (!getTableLM().getTable().omitFooterAtBreak()) {
                    effPenaltyLen += tclm.getFooterNetHeight();
                    penaltyPos.footerElements = tclm.getFooterElements();
                }
            }
            returnList.add(new KnuthPenalty(effPenaltyLen, 0, false, penaltyPos, false));

            log.debug("step=" + step + " (+" + increase + ")"
                    + " box=" + boxLen 
                    + " penalty=" + penaltyLen
                    + " effPenalty=" + effPenaltyLen);
            
            laststep = step;
            if (rowBacktrackForLastStep) {
                //If row was set to previous, restore now
                activeRow++;
            }
        }
        return returnList;
    }
    
    private int getNextStep(int lastStep) {
        int[] backupWidths = new int[start.length];
        System.arraycopy(widths, 0, backupWidths, 0, backupWidths.length);

        //set starting points
        int rowPendingIndicator = 0;
        for (int i = 0; i < start.length; i++) {
            if (elementLists[i] == null) {
                continue;
            }
            if (end[i] < elementLists[i].size()) {
                start[i] = end[i] + 1;
                if (end[i] + 1 < elementLists[i].size() 
                        && getActivePrimaryGridUnit(i).isLastGridUnitRowSpan()) {
                    rowPendingIndicator++;
                }
            } else {
                start[i] = -1; //end of list reached
                end[i] = -1;
            }
        }

        if (rowPendingIndicator == 0) {
            if (activeRow < rowGroup.length - 1) {
                activeRow++;
                log.debug("===> new row: " + activeRow);
                initializeElementLists();
                for (int i = 0; i < backupWidths.length; i++) {
                    if (end[i] < 0) {
                        backupWidths[i] = 0;
                    }
                }
            }
        }

        //Get next possible sequence for each cell
        int seqCount = 0;
        for (int i = 0; i < start.length; i++) {
            if (elementLists[i] == null) {
                continue;
            }
            while (end[i] + 1 < elementLists[i].size()) {
                end[i]++;
                KnuthElement el = (KnuthElement)elementLists[i].get(end[i]);
                if (el.isPenalty()) {
                    if (el.getP() < KnuthElement.INFINITE) {
                        //First legal break point
                        break;
                    }
                } else if (el.isGlue()) {
                    KnuthElement prev = (KnuthElement)elementLists[i].get(end[i] - 1);
                    if (prev.isBox()) {
                        //Second legal break point
                        break;
                    }
                    widths[i] += el.getW();
                } else {
                    widths[i] += el.getW();
                }
            }
            if (end[i] < start[i]) {
                widths[i] = backupWidths[i];
            } else {
                seqCount++;
            }
            //log.debug("part " + start[i] + "-" + end[i] + " " + widths[i]);
            if (end[i] + 1 >= elementLists[i].size()) {
                //element list for this cell is finished
                if (isSeparateBorderModel()) {
                    borderAfter[i] = getActivePrimaryGridUnit(i)
                            .getBorders().getBorderAfterWidth(false);
                } else {
                    borderAfter[i] = getActivePrimaryGridUnit(i).getHalfMaxAfterBorderWidth();
                }
            } else {
                //element list for this cell is not finished
                if (isSeparateBorderModel()) {
                    borderAfter[i] = getActivePrimaryGridUnit(i)
                            .getBorders().getBorderAfterWidth(false);
                } else {
                    //TODO fix me!
                    borderAfter[i] = getActivePrimaryGridUnit(i).getHalfMaxAfterBorderWidth();
                }
            }
            log.debug("borders before=" + borderBefore[i] + " after=" + borderAfter[i]);
        }
        if (seqCount == 0) {
            return 0;
        }

        //Determine smallest possible step
        int minStep = Integer.MAX_VALUE;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < widths.length; i++) {
            baseWidth[i] = 0;
            for (int prevRow = 0; prevRow < startRow[i]; prevRow++) {
                baseWidth[i] += rowGroup[prevRow].getHeight().opt;
            }
            baseWidth[i] += borderBefore[i] + borderAfter[i];
            if (end[i] >= start[i]) {
                int len = baseWidth[i] + widths[i];
                sb.append(len + " ");
                minStep = Math.min(len, minStep);
            }
        }
        log.debug("candidate steps: " + sb);

        //Check for constellations that would result in overlapping borders
        /*
        for (int i = 0; i < widths.length; i++) {
            
        }*/
        
        //Reset bigger-than-minimum sequences
        rowBacktrackForLastStep = false;
        for (int i = 0; i < widths.length; i++) {
            int len = baseWidth[i] + widths[i];
            if (len > minStep) {
                widths[i] = backupWidths[i];
                end[i] = start[i] - 1;
                if (baseWidth[i] + widths[i] > minStep) {
                    log.debug("Meeeeep!");
                    rowBacktrackForLastStep = true;
                }
            }
        }
        if (log.isDebugEnabled()) {
            /*StringBuffer*/ sb = new StringBuffer();
            for (int i = 0; i < widths.length; i++) {
                if (end[i] >= start[i]) {
                    sb.append(i + ": " + start[i] + "-" + end[i] + "(" + widths[i] + "), ");
                } else {
                    sb.append(i + ": skip, ");
                }
            }
            log.debug(sb.toString());
        }

        return minStep;
    }
    
    
    /** @return true if the table uses the separate border model. */
    private boolean isSeparateBorderModel() {
        return getTableLM().getTable().isSeparateBorderModel();
    }

    /** @return the table layout manager */
    private TableLayoutManager getTableLM() {
        return this.tclm.getTableLM();
    }

    private class KnuthBoxCellWithBPD extends KnuthBox {
        
        private PrimaryGridUnit pgu;
        
        public KnuthBoxCellWithBPD(int w, PrimaryGridUnit pgu) {
            super(w, null, true);
            this.pgu = pgu;
        }
    }
    
}
