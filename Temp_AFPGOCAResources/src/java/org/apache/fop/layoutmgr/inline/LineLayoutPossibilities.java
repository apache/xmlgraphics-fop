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
import org.apache.fop.layoutmgr.Position;

public class LineLayoutPossibilities {

    /** logger instance */
    protected static Log log = LogFactory.getLog(LineLayoutPossibilities.class);

    private class Possibility {
        private int lineCount;
        private double demerits;
        private List breakPositions;

        private Possibility(int lc, double dem) {
            lineCount = lc;
            demerits = dem;
            breakPositions = new java.util.ArrayList(lc);
        }

        private int getLineCount() {
            return lineCount;
        }

        private double getDemerits() {
            return demerits;
        }

        private void addBreakPosition(Position pos) {
            // Positions are always added with index 0 because
            // they are created backward, from the last one to
            // the first one
            breakPositions.add(0, pos);
        }

        private Position getBreakPosition(int i) {
            return (Position)breakPositions.get(i);
        }
    }

    private List possibilitiesList;
    private List savedPossibilities;
    private int minimumIndex;
    private int optimumIndex;
    private int maximumIndex;
    private int chosenIndex;
    private int savedOptLineCount;

    public LineLayoutPossibilities() {
        possibilitiesList = new java.util.ArrayList();
        savedPossibilities = new java.util.ArrayList();
        optimumIndex = -1;
    }

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

    /* save in a different array the computed Possibilities,
     * so possibilitiesList is ready to store different Possibilities
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

    /* replace the Possibilities stored in possibilitiesList with
     * the ones stored in savedPossibilities and having the same line number
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
                optimumIndex ++;
                maximumIndex ++;
                chosenIndex ++;
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
                    index ++;
                }
                if (getLineCount(index) == restoredPossibility.getLineCount()) {
                    possibilitiesList.set(index, restoredPossibility);
                } else {
                    // this should not happen
                    log.error("LineLayoutPossibilities restorePossibilities(),"
                        + " min= " + getMinLineCount()
                        + " max= " + getMaxLineCount()
                        + " restored= " + restoredPossibility.getLineCount());
                    return;
                }
            }
            // update optimumIndex and chosenIndex
            if (savedOptLineCount == 0 && getDemerits(optimumIndex) > restoredPossibility.getDemerits()
                || savedOptLineCount != 0 && restoredPossibility.getLineCount() == savedOptLineCount) {
                optimumIndex = index;
                chosenIndex = optimumIndex;
            }
        }
        //log.debug(">> minLineCount = " + getMinLineCount()
        //  + " optLineCount = " + getOptLineCount() + " maxLineCount() = " + getMaxLineCount());
    }

    public void addBreakPosition(Position pos, int i) {
        ((Possibility)possibilitiesList.get(i)).addBreakPosition(pos);
    }

    public boolean canUseMoreLines() {
        return (getOptLineCount() < getMaxLineCount());
    }

    public boolean canUseLessLines() {
        return (getMinLineCount() < getOptLineCount());
    }

    public int getMinLineCount() {
        return getLineCount(minimumIndex);
    }

    public int getOptLineCount() {
        return getLineCount(optimumIndex);
    }

    public int getMaxLineCount() {
        return getLineCount(maximumIndex);
    }

    public int getChosenLineCount() {
        return getLineCount(chosenIndex);
    }

    public int getLineCount(int i) {
        return ((Possibility)possibilitiesList.get(i)).getLineCount();
    }

    public double getChosenDemerits() {
        return getDemerits(chosenIndex);
    }

    public double getDemerits(int i) {
        return ((Possibility)possibilitiesList.get(i)).getDemerits();
    }

    public int getPossibilitiesNumber() {
        return possibilitiesList.size();
    }

    public Position getChosenPosition(int i) {
        return ((Possibility)possibilitiesList.get(chosenIndex)).getBreakPosition(i);
    }

    public int applyLineCountAdjustment(int adj) {
        if (adj >= (getMinLineCount() - getChosenLineCount())
            && adj <= (getMaxLineCount() - getChosenLineCount())
            && getLineCount(chosenIndex + adj) == getChosenLineCount() + adj) {
            chosenIndex += adj;
            log.debug("chosenLineCount= " + (getChosenLineCount() - adj) + " adjustment= " + adj
                               + " => chosenLineCount= " + getLineCount(chosenIndex));
            return adj;
        } else {
            // this should not happen!
            log.warn("Cannot apply the desired line count adjustment.");
            return 0;
        }
    }

    public void printAll() {
        System.out.println("++++++++++");
        System.out.println(" " + possibilitiesList.size() + " possibility':");
        for (int i = 0; i < possibilitiesList.size(); i ++) {
            System.out.println("   " + ((Possibility)possibilitiesList.get(i)).getLineCount()
                               + (i == optimumIndex ? " *" : "")
                               + (i == minimumIndex ? " -" : "")
                               + (i == maximumIndex ? " +" : ""));
        }
        System.out.println("++++++++++");
    }
}
