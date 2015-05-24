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

package org.apache.fop.layoutmgr.inline;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Line layout possibilities.
 */
public class LineLayoutPossibilities {

    /** logger instance */
    private static final Log LOG = LogFactory.getLog(LineLayoutPossibilities.class);

    private final class Possibility {
        private int lineCount;
        private double demerits;
        private List<LineLayoutManager.LineBreakPosition> breakPositions;

        private Possibility(int lc, double dem) {
            lineCount = lc;
            demerits = dem;
            breakPositions = new java.util.ArrayList<LineLayoutManager.LineBreakPosition>(lc);
        }

        private int getLineCount() {
            return lineCount;
        }

        private double getDemerits() {
            return demerits;
        }

        private void addBreakPosition(LineLayoutManager.LineBreakPosition pos) {
            // Positions are always added with index 0 because
            // they are created backward, from the last one to
            // the first one
            breakPositions.add(0, pos);
        }

        private LineLayoutManager.LineBreakPosition getBreakPosition(int i) {
            return breakPositions.get(i);
        }
    }

    private List possibilitiesList;
    private List savedPossibilities;
    private int minimumIndex;
    private int optimumIndex;
    private int maximumIndex;
    private int chosenIndex;
    private int savedOptLineCount;

    /** default constructor */
    public LineLayoutPossibilities() {
        possibilitiesList = new java.util.ArrayList();
        savedPossibilities = new java.util.ArrayList();
        optimumIndex = -1;
    }

    /**
     * Add possibility.
     * @param ln line number
     * @param dem demerits
     */
    public void addPossibility(int ln, double dem) {
        possibilitiesList.add(new Possibility(ln, dem));
        if (possibilitiesList.size() == 1) {
            // first Possibility added
            minimumIndex = 0;
            optimumIndex = 0;
            maximumIndex = 0;
            chosenIndex = 0;
        } else {
            if (dem < ((Possibility)possibilitiesList.get(optimumIndex)).getDemerits()) {
                optimumIndex = possibilitiesList.size() - 1;
                chosenIndex = optimumIndex;
            }
            if (ln < ((Possibility)possibilitiesList.get(minimumIndex)).getLineCount()) {
                minimumIndex = possibilitiesList.size() - 1;
            }
            if (ln > ((Possibility)possibilitiesList.get(maximumIndex)).getLineCount()) {
                maximumIndex = possibilitiesList.size() - 1;
            }
        }
    }

    /**
     * Save in a different array the computed Possibilities,
     * so possibilitiesList is ready to store different Possibilities.
     * @param bSaveOptLineCount true if should save optimum line count
     */
    public void savePossibilities(boolean bSaveOptLineCount) {
        if (bSaveOptLineCount) {
            savedOptLineCount = getOptLineCount();
        } else {
            savedOptLineCount = 0;
        }
        savedPossibilities = possibilitiesList;
        possibilitiesList = new java.util.ArrayList();
    }

    /**
     * Replace the Possibilities stored in possibilitiesList with
     * the ones stored in savedPossibilities and having the same line number.
     */
    public void restorePossibilities() {
        int index = 0;
        while (savedPossibilities.size() > 0) {
            Possibility restoredPossibility = (Possibility) savedPossibilities.remove(0);
            if (restoredPossibility.getLineCount() < getMinLineCount()) {
                // if the line number of restoredPossibility is less than the minimum one,
                // add restoredPossibility at the beginning of the list
                possibilitiesList.add(0, restoredPossibility);
                // update minimumIndex
                minimumIndex = 0;
                // shift the other indexes;
                optimumIndex++;
                maximumIndex++;
                chosenIndex++;
            } else if (restoredPossibility.getLineCount() > getMaxLineCount()) {
                // if the line number of restoredPossibility is greater than the maximum one,
                // add restoredPossibility at the end of the list
                possibilitiesList.add(possibilitiesList.size(), restoredPossibility);
                // update maximumIndex
                maximumIndex = possibilitiesList.size() - 1;
                index = maximumIndex;
            } else {
                // find the index of the Possibility that will be replaced
                while (index < maximumIndex
                       && getLineCount(index) < restoredPossibility.getLineCount()) {
                    index++;
                }
                if (getLineCount(index) == restoredPossibility.getLineCount()) {
                    possibilitiesList.set(index, restoredPossibility);
                } else {
                    // this should not happen
                    LOG.error("LineLayoutPossibilities restorePossibilities(),"
                        + " min= " + getMinLineCount()
                        + " max= " + getMaxLineCount()
                        + " restored= " + restoredPossibility.getLineCount());
                    return;
                }
            }
            // update optimumIndex and chosenIndex
            if (savedOptLineCount == 0
                && getDemerits(optimumIndex) > restoredPossibility.getDemerits()
                || savedOptLineCount != 0
                && restoredPossibility.getLineCount() == savedOptLineCount) {
                optimumIndex = index;
                chosenIndex = optimumIndex;
            }
        }
        //log.debug(">> minLineCount = " + getMinLineCount()
        //  + " optLineCount = " + getOptLineCount() + " maxLineCount() = " + getMaxLineCount());
    }

    /**
     * @param pos a position
     * @param i an index into posibilities list
     */
    public void addBreakPosition(LineLayoutManager.LineBreakPosition pos, int i) {
        ((Possibility)possibilitiesList.get(i)).addBreakPosition(pos);
    }

    /** @return true if can use more lines */
    public boolean canUseMoreLines() {
        return (getOptLineCount() < getMaxLineCount());
    }

    /** @return true if can use fewer lines */
    public boolean canUseLessLines() {
        return (getMinLineCount() < getOptLineCount());
    }

    /** @return the line count of the minimum index */
    public int getMinLineCount() {
        return getLineCount(minimumIndex);
    }

    /** @return the line count of the optimum index */
    public int getOptLineCount() {
        return getLineCount(optimumIndex);
    }

    /** @return the line count of the maximum index */
    public int getMaxLineCount() {
        return getLineCount(maximumIndex);
    }

    /** @return the line count of the chosen index */
    public int getChosenLineCount() {
        return getLineCount(chosenIndex);
    }

    /**
     * @param i the posibilities list index
     * @return the line count
     */
    public int getLineCount(int i) {
        return ((Possibility)possibilitiesList.get(i)).getLineCount();
    }

    /** @return the demerits of the chosen index */
    public double getChosenDemerits() {
        return getDemerits(chosenIndex);
    }

    /**
     * @param i the posibilities list index
     * @return the demerits
     */
    public double getDemerits(int i) {
        return ((Possibility)possibilitiesList.get(i)).getDemerits();
    }

    /** @return the possibilities count */
    public int getPossibilitiesNumber() {
        return possibilitiesList.size();
    }

    /**
     * @param i the break position index
     * @return the chosen position
     */
    public LineLayoutManager.LineBreakPosition getChosenPosition(int i) {
        return ((Possibility)possibilitiesList.get(chosenIndex)).getBreakPosition(i);
    }

    /**
     * @param adj the adjustment
     * @return the adjustment or zero
     */
    public int applyLineCountAdjustment(int adj) {
        if (adj >= (getMinLineCount() - getChosenLineCount())
            && adj <= (getMaxLineCount() - getChosenLineCount())
            && getLineCount(chosenIndex + adj) == getChosenLineCount() + adj) {
            chosenIndex += adj;
            LOG.debug("chosenLineCount= " + (getChosenLineCount() - adj) + " adjustment= " + adj
                               + " => chosenLineCount= " + getLineCount(chosenIndex));
            return adj;
        } else {
            // this should not happen!
            LOG.warn("Cannot apply the desired line count adjustment.");
            return 0;
        }
    }

    /** print all */
    public void printAll() {
        System.out.println("++++++++++");
        System.out.println(" " + possibilitiesList.size() + " possibility':");
        for (int i = 0; i < possibilitiesList.size(); i++) {
            System.out.println("   " + ((Possibility)possibilitiesList.get(i)).getLineCount()
                               + (i == optimumIndex ? " *" : "")
                               + (i == minimumIndex ? " -" : "")
                               + (i == maximumIndex ? " +" : ""));
        }
        System.out.println("++++++++++");
    }
}
