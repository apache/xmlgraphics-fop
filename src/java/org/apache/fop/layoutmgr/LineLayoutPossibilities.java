
package org.apache.fop.layoutmgr;

import java.util.ArrayList;

public class LineLayoutPossibilities {

    private class Possibility {
        private int lineNumber;
        private double demerits;
        private ArrayList breakPositions;

        private Possibility(int ln, double dem) {
            lineNumber = ln;
            demerits = dem;
            breakPositions = new ArrayList(ln);
        }

        private int getLineNumber() {
            return lineNumber;
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

    private ArrayList possibilitiesList;
    private ArrayList savedPossibilities;
    private int minimumIndex;
    private int optimumIndex;
    private int maximumIndex;
    private int chosenIndex;
    private int savedOptLineNumber;

    public LineLayoutPossibilities() {
        possibilitiesList = new ArrayList();
        savedPossibilities = new ArrayList();
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
            if (ln < ((Possibility)possibilitiesList.get(minimumIndex)).getLineNumber()) {
                minimumIndex = possibilitiesList.size() - 1;
            }
            if (ln > ((Possibility)possibilitiesList.get(maximumIndex)).getLineNumber()) {
                maximumIndex = possibilitiesList.size() - 1;
            }
        }
    }

    /* save in a different array the computed Possibilities,
     * so possibilitiesList is ready to store different Possibilities
     */
    public void savePossibilities(boolean bSaveOptLineNumber) {
        if (bSaveOptLineNumber) {
            savedOptLineNumber = getOptLineNumber();
        } else {
            savedOptLineNumber = 0;
        }
        savedPossibilities = possibilitiesList;
        possibilitiesList = new ArrayList();
    }

    /* replace the Possibilities stored in possibilitiesList with
     * the ones stored in savedPossibilities and having the same line number
     */
    public void restorePossibilities() {
        int index = 0;
        while (savedPossibilities.size() > 0) {
            Possibility restoredPossibility = (Possibility) savedPossibilities.remove(0);
            if (restoredPossibility.getLineNumber() < getMinLineNumber()) {
                // if the line number of restoredPossibility is less than the minimum one,
                // add restoredPossibility at the beginning of the list
                possibilitiesList.add(0, restoredPossibility);
                // update minimumIndex
                minimumIndex = 0;
                // shift the other indexes;
                optimumIndex ++;
                maximumIndex ++;
                chosenIndex ++;
            } else if (restoredPossibility.getLineNumber() > getMaxLineNumber()) {
                // if the line number of restoredPossibility is greater than the maximum one,
                // add restoredPossibility at the end of the list
                possibilitiesList.add(possibilitiesList.size(), restoredPossibility);
                // update maximumIndex
                maximumIndex = possibilitiesList.size() - 1;
                index = maximumIndex;
            } else {
                // find the index of the Possibility that will be replaced
                while (index < maximumIndex
                       && getLineNumber(index) < restoredPossibility.getLineNumber()) {
                    index ++;
                }
                if (getLineNumber(index) == restoredPossibility.getLineNumber()) {
                    possibilitiesList.set(index, restoredPossibility);
                } else {
                    // this should not happen
/*LF*/              System.out.println("ERRORE: LineLayoutPossibilities restorePossibilities(), min= " + getMinLineNumber() + " max= " + getMaxLineNumber() + " restored= " + restoredPossibility.getLineNumber());
                    return;
                }
            }
            // update optimumIndex and chosenIndex
            if (savedOptLineNumber == 0 && getDemerits(optimumIndex) > restoredPossibility.getDemerits()
                || savedOptLineNumber != 0 && restoredPossibility.getLineNumber() == savedOptLineNumber) {
                optimumIndex = index;
                chosenIndex = optimumIndex;
            }
        }
/*LF*/  //System.out.println(">> minLineNumber = " + getMinLineNumber() + " optLineNumber = " + getOptLineNumber() + " maxLineNumber() = " + getMaxLineNumber());
    }

    public void addBreakPosition(Position pos, int i) {
        ((Possibility)possibilitiesList.get(i)).addBreakPosition(pos);
    }

    public boolean canUseMoreLines() {
        return (getOptLineNumber() < getMaxLineNumber());
    }

    public boolean canUseLessLines() {
        return (getMinLineNumber() < getOptLineNumber());
    }

    public int getMinLineNumber() {
        return getLineNumber(minimumIndex);
    }

    public int getOptLineNumber() {
        return getLineNumber(optimumIndex);
    }

    public int getMaxLineNumber() {
        return getLineNumber(maximumIndex);
    }

    public int getChosenLineNumber() {
        return getLineNumber(chosenIndex);
    }

    public int getLineNumber(int i) {
        return ((Possibility)possibilitiesList.get(i)).getLineNumber();
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

    public int applyLineNumberAdjustment(int adj) {
        if (adj >= (getMinLineNumber() - getChosenLineNumber())
            && adj <= (getMaxLineNumber() - getChosenLineNumber())
            && getLineNumber(chosenIndex + adj) == getChosenLineNumber() + adj) {
            chosenIndex += adj;
            System.out.println("chosenLineNumber= " + (getChosenLineNumber() - adj) + " variazione= " + adj
                               + " => chosenLineNumber= " + getLineNumber(chosenIndex));
            return adj;
        } else {
            // this should not happen!
            System.out.println("Cannot apply the desired line number adjustment");
            return 0;
        }
    }

    public void printAll() {
        System.out.println("++++++++++");
        System.out.println(" " + possibilitiesList.size() + " possibilita':");
        for (int i = 0; i < possibilitiesList.size(); i ++) {
            System.out.println("   " + ((Possibility)possibilitiesList.get(i)).getLineNumber()
                               + (i == optimumIndex ? " *" : "")
                               + (i == minimumIndex ? " -" : "")
                               + (i == maximumIndex ? " +" : ""));
        }
        System.out.println("++++++++++");
    }
}
