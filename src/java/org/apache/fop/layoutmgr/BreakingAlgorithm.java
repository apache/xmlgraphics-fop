/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

package org.apache.fop.layoutmgr;

import java.util.LinkedList;
import java.util.ListIterator;

public abstract class BreakingAlgorithm {

    private final int KNUTH_ALGORITHM = 0;
    private final int FIRST_FIT_ALGORITHM = 1;

    // this class represent a feasible breaking point
    public class KnuthNode {
        // index of the breakpoint represented by this node
        public int position;

        // number of the line ending at this breakpoint
        public int line;

        // fitness class of the line ending at his breakpoint
        public int fitness;

        // accumulated width of the KnuthElements
        public int totalWidth;

        // accumulated stretchability of the KnuthElements
        public int totalStretch;

        // accumulated shrinkability of the KnuthElements
        public int totalShrink;

        // adjustment ratio if the line ends at this breakpoint
        public double adjustRatio;

/*LF*/  public int availableShrink;
/*LF*/  public int availableStretch;

        // difference between target and actual line width
        public int difference;

        // minimum total demerits up to this breakpoint
        public double totalDemerits;

        // best node for the preceding breakpoint
        public KnuthNode previous;

        public KnuthNode(int position, int line, int fitness,
                         int totalWidth, int totalStretch, int totalShrink,
                         double adjustRatio, int availableShrink, int availableStretch, int difference,
                         double totalDemerits, KnuthNode previous) {
            this.position = position;
            this.line = line;
            this.fitness = fitness;
            this.totalWidth = totalWidth;
            this.totalStretch = totalStretch;
            this.totalShrink = totalShrink;
            this.adjustRatio = adjustRatio;
/*LF*/      this.availableShrink = availableShrink;
/*LF*/      this.availableStretch = availableStretch;
            this.difference = difference;
            this.totalDemerits = totalDemerits;
            this.previous = previous;
        }
    }

    // this class stores information about how the nodes
    // which could start a line
    // ending at the current element
    private class BestRecords {
        private static final double INFINITE_DEMERITS = 1E11;

        private double bestDemerits[] = {
            INFINITE_DEMERITS, INFINITE_DEMERITS,
            INFINITE_DEMERITS, INFINITE_DEMERITS
        };
        private KnuthNode bestNode[] = {null, null, null, null};
        private double bestAdjust[] = {0.0, 0.0, 0.0, 0.0};
        private int bestDifference[] = {0, 0, 0, 0};
/*LF*/  private int bestAvailableShrink[] = {0, 0, 0, 0};
/*LF*/  private int bestAvailableStretch[] = {0, 0, 0, 0};
        private int bestIndex = -1;

        public BestRecords() {
        }

        public void addRecord(double demerits, KnuthNode node, double adjust,
                              int availableShrink, int availableStretch, int difference, int fitness) {
            if (demerits > bestDemerits[fitness]) {
                //log.error("New demerits value greter than the old one");
            }
            bestDemerits[fitness] = demerits;
            bestNode[fitness] = node;
            bestAdjust[fitness] = adjust;
/*LF*/      bestAvailableShrink[fitness] = availableShrink;
/*LF*/      bestAvailableStretch[fitness] = availableStretch;
            bestDifference[fitness] = difference;
            if (bestIndex == -1 || demerits < bestDemerits[bestIndex]) {
                bestIndex = fitness;
            }
        }

        public boolean hasRecords() {
            return (bestIndex != -1);
        }

        public boolean notInfiniteDemerits(int fitness) {
            return (bestDemerits[fitness] != INFINITE_DEMERITS);
        }

        public double getDemerits(int fitness) {
            return bestDemerits[fitness];
        }

        public KnuthNode getNode(int fitness) {
            return bestNode[fitness];
        }

        public double getAdjust(int fitness) {
            return bestAdjust[fitness];
        }

/*LF*/  public int getAvailableShrink(int fitness) {
/*LF*/      return bestAvailableShrink[fitness];
/*LF*/  }

/*LF*/  public int getAvailableStretch(int fitness) {
/*LF*/      return bestAvailableStretch[fitness];
/*LF*/  }

       public int getDifference(int fitness) {
            return bestDifference[fitness];
        }

        public double getMinDemerits() {
            if (bestIndex != -1) {
                return getDemerits(bestIndex);
            } else {
                // anyway, this should never happen
                return INFINITE_DEMERITS;
            }
        }
    }

    //     parameters of Knuth's algorithm:
    // penalty value for flagged penalties
    private int flaggedPenalty = 50;
    // demerit for consecutive lines ending at flagged penalties
    private int repeatedFlaggedDemerit = 50;
    // demerit for consecutive lines belonging to incompatible fitness classes 
    private int incompatibleFitnessDemerit = 50;
    // suggested modification to the "optimum" number of lines
    private int looseness = 0;

    private static final int INFINITE_RATIO = 1000;
    private static int MAX_DEMERITS_INCREASE = 1000;

    protected LinkedList activeList = null;
    protected LinkedList auxActiveList = null;
//    private LinkedList inactiveList = null;
    private LinkedList auxInactiveList = null;
    protected KnuthNode lastDeactivatedNode = null;
    private KnuthNode lastTooLong;
    private KnuthNode lastTooShort;
    private KnuthNode lastDeactivated;

    protected int alignment;
    protected int alignmentLast;
    protected boolean bFirst;

    public BreakingAlgorithm(int align, int alignLast,
                             boolean first) {
        alignment = align;
        alignmentLast = alignLast;
        bFirst = first;
    }

    public abstract void updateData1(int total, double demerits) ;

    public abstract void updateData2(KnuthNode bestActiveNode,
                                     KnuthSequence sequence,
                                     int total) ;

    public int findBreakingPoints(KnuthSequence par, int lineWidth,
                                  double threshold, boolean force,
                                  boolean hyphenationAllowed) {

        findBreakingPoints(KNUTH_ALGORITHM, par, 0, lineWidth, threshold, force, hyphenationAllowed);

        boolean bForced = false;
        int optLineNumber;
        if (activeList.size() > 0) {
            // there is at least one set of breaking points
            // select one or more active nodes, removing the others from the list
            optLineNumber = filterActiveList();
/* a causa delle modifiche di Finn Bock, non c'e' piu' bisogno di usare first fit */
//        } else if (force) {
//            // no set of breaking points, but must find one
//            // use a different algorithm
///*LF*/      System.out.println("si ricorre all'algoritmo first fit dalla riga " + lastDeactivatedNode.line + " elemento " + (lastDeactivatedNode.position == 0 ? 0 :lastDeactivatedNode.position + 1));
//            findBreakingPoints(FIRST_FIT_ALGORITHM, par, lastDeactivatedNode.position == 0 ? 0 :lastDeactivatedNode.position + 1, lineWidth, threshold, force, hyphenationAllowed);
//            activeList.add(auxActiveList.getLast());
//            optLineNumber = ((KnuthNode) auxActiveList.getLast()).line;
//            bForced = true;
        } else {
            // no set of breaking points, do nothing
            return 0;
        }

        // now, activeList is not empty:
        // if bForced == true, there is ONLY one node in activeList
        // if bForced == false, there is AT LEAST one node in activeList
/*LF*/  //System.out.println("BA> ora activeList.size() = " + activeList.size());
        ListIterator activeListIterator = activeList.listIterator();
        while (activeListIterator.hasNext()) {
            KnuthNode activeNode = (KnuthNode)activeListIterator.next();
            int totalLines;
            if (bForced) {
                updateData1(optLineNumber, 0);
                totalLines = optLineNumber;
                auxActiveList.clear();
                auxInactiveList.clear();
            } else {
                updateData1(activeNode.line, activeNode.totalDemerits);
                totalLines = activeNode.line;
            }
            calculateBreakPoints(activeNode, par, totalLines);
        }

        activeList.clear();
//        inactiveList.clear();
        return optLineNumber;
    }

/* ---------------------------- */
    public int firstFit(KnuthSequence par, int lineWidth,
                        double threshold, boolean force) {
        lastDeactivatedNode = new KnuthNode(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, null);

        findBreakingPoints(FIRST_FIT_ALGORITHM, par, 0, lineWidth, threshold, force, true);

/*LF*/  activeList = new LinkedList();
///*LF*/  inactiveList = new LinkedList();
        activeList.add(auxActiveList.getLast());
        int optLineNumber = ((KnuthNode) auxActiveList.getLast()).line;

        // now, activeList is not empty:
        // if bForced == true, there is ONLY one node in activeList
        // if bForced == false, there is AT LEAST one node in activeList
/*LF*/  //System.out.println("BA> ora activeList.size() = " + activeList.size());
        ListIterator activeListIterator = activeList.listIterator();
        while (activeListIterator.hasNext()) {
            KnuthNode activeNode = (KnuthNode)activeListIterator.next();
            int totalLines;
            updateData1(optLineNumber, 0);
            totalLines = optLineNumber;
            auxActiveList.clear();
            auxInactiveList.clear();
            calculateBreakPoints(activeNode, par, totalLines);
        }

        activeList.clear();
//        inactiveList.clear();
        return optLineNumber;
    }
/* ---------------------------- */

    private void findBreakingPoints(int algorithm,
                                    KnuthSequence par, int firstIndex,
                                    int lineWidth,
                                    double threshold, boolean force,
                                    boolean hyphenationAllowed) {
        int totalWidth = 0;
        int totalStretch = 0;
        int totalShrink = 0;

        // reset lastTooShort and lastTooLong, as they could be not null
        // because of previous calls to findBreakingPoints
        lastTooShort = lastTooLong = null; 

        // current element in the paragraph
        KnuthElement thisElement = null;
        // previous element in the paragraph is a KnuthBox
        boolean previousIsBox = false;

/*LF*/  // index of the first KnuthBox in the sequence
/*LF*/  int firstBoxIndex = firstIndex;
/*LF*/  while (alignment != org.apache.fop.fo.Constants.EN_CENTER
/*LF*/         && ! ((KnuthElement) par.get(firstBoxIndex)).isBox()) {
/*LF*/      firstBoxIndex++;
/*LF*/  }
/*LF*/
        // create an active node representing the starting point
        if (algorithm == KNUTH_ALGORITHM) {
            activeList = new LinkedList();
/*LF*/      activeList.add(new KnuthNode(firstBoxIndex, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, null));
//            inactiveList = new LinkedList();
        } else {
            auxActiveList = new LinkedList();
/*LF*/      auxActiveList.add(new KnuthNode(lastDeactivatedNode.position, lastDeactivatedNode.line, 1,
                                            0, 0, 0,
                                            lastDeactivatedNode.adjustRatio,
                                            0, 0, lastDeactivatedNode.difference,
                                            0, lastDeactivatedNode.previous));
            auxInactiveList = new LinkedList();
        }

        KnuthNode lastForced = (KnuthNode) activeList.getFirst();
        // main loop
/*LF*/  ListIterator paragraphIterator = par.listIterator(firstBoxIndex);
/*LF*/  //System.out.println("");
/*LF*/  //System.out.println("inizio main loop");
        while (paragraphIterator.hasNext()) {
            thisElement = (KnuthElement) paragraphIterator.next();
/*LF*/      //System.out.println("main loop, elemento " + paragraphIterator.previousIndex());
            if (thisElement.isBox()) {
                // a KnuthBox object is not a legal line break
                totalWidth += thisElement.getW();
                previousIsBox = true;
            } else if (thisElement.isGlue()) {
                // a KnuthGlue object is a legal line break
                // only if the previous object is a KnuthBox
                if (previousIsBox) {
                    if (algorithm == KNUTH_ALGORITHM) {
                        considerLegalBreak(par, lineWidth, thisElement,
                                           totalWidth, totalStretch, totalShrink,
                                           threshold);
                    } else {
                        considerLegalBreak2(par, lineWidth, thisElement,
                                            totalWidth, totalStretch, totalShrink,
                                            threshold);
                    }
                }
                totalWidth += thisElement.getW();
                totalStretch += ((KnuthGlue) thisElement).getY();
                totalShrink += ((KnuthGlue) thisElement).getZ();
                previousIsBox = false;
            } else {
                // a KnuthPenalty is a legal line break
                // only if its penalty is not infinite;
                // if hyphenationAllowed is false, ignore flagged penalties
                if (((KnuthPenalty) thisElement).getP()
                    < KnuthElement.INFINITE
                    && (hyphenationAllowed || !((KnuthPenalty) thisElement).isFlagged())) {
                    if (algorithm == KNUTH_ALGORITHM) {
                        considerLegalBreak(par, lineWidth, thisElement,
                                           totalWidth, totalStretch, totalShrink,
                                           threshold);
                    } else {
                        considerLegalBreak2(par, lineWidth, thisElement,
                                            totalWidth, totalStretch, totalShrink,
                                            threshold);
                    }
                }
                previousIsBox = false;
            }
            /* *** inizio modifiche proposte da Finn Bock *** */
            if (activeList.size() == 0) {
                if (!force) {
                    return;
                }
/*LF*/          //System.out.println(" ");
/*LF*/          //System.out.println("  lastTooShort.position= " + (lastTooShort == null ? -1 : lastTooShort.position) + " lastTooLong.position= " + (lastTooLong == null ? -1 : lastTooLong.position));
                if (lastTooShort == null || lastForced.position == lastTooShort.position) {
                    lastForced = lastTooLong;
                } else {
                    lastForced = lastTooShort;
                }

/*LF*/          //System.out.println("  Restarting: position= " + lastForced.position);
/*LF*/          //System.out.println(" ");
                lastForced.totalDemerits = 0;
                activeList.add(lastForced);
                totalWidth = lastForced.totalWidth;
                totalStretch = lastForced.totalStretch;
                totalShrink = lastForced.totalShrink;
                lastTooShort = lastTooLong = null;
                while (paragraphIterator.nextIndex() > (lastForced.position + 1)) {
                    paragraphIterator.previous();
                }
            }
            /* *** fine modifiche proposte da Finn Bock  *** */
        }
    }

    private void considerLegalBreak2(LinkedList par, int lineWidth,
                                     KnuthElement element,
                                     int totalWidth, int totalStretch,
                                     int totalShrink, double threshold) {
        //System.out.println("(" + par.indexOf(element) + ")");
        KnuthNode startNode = (KnuthNode) auxActiveList.getFirst();
        KnuthNode endNode = auxActiveList.size() > 1 ? (KnuthNode) auxActiveList.getLast() : null;

        // these are the new values that must be computed
        // in order to define a new active node
        int newWidth;
        int newStretch;
        int newShrink;
        int newDifference;
        double newRatio;

        // compute width, stretch and shrink of the new node
        newWidth = totalWidth;
        newStretch = totalStretch;
        newShrink = totalShrink;
        ListIterator tempIterator = par.listIterator(par.indexOf(element));
        while (tempIterator.hasNext()) {
            KnuthElement tempElement = (KnuthElement)tempIterator.next();
            if (tempElement.isBox()) {
                break;
            } else if (tempElement.isGlue()) {
                newWidth += ((KnuthGlue) tempElement).getW();
                newStretch += ((KnuthGlue) tempElement).getY();
                newShrink += ((KnuthGlue) tempElement).getZ();
            } else if (((KnuthPenalty) tempElement).getP() 
                       == -KnuthElement.INFINITE
                       && tempElement != element) {
                break;
            }
        }

        if (endNode == null
            || totalWidth + (element.isPenalty() ? element.getW() : 0) - startNode.totalWidth <= lineWidth
            || alignment == org.apache.fop.fo.Constants.EN_JUSTIFY
               && totalWidth  + (element.isPenalty() ? element.getW() : 0)- startNode.totalWidth - (totalShrink - startNode.totalShrink) <= lineWidth) {
            // add content to the same line

            // compute difference and ratio
            int actualWidth = totalWidth - startNode.totalWidth;
            if (element.isPenalty()) {
                actualWidth += element.getW();
            }
            newDifference = lineWidth - actualWidth;
            int available = newDifference >= 0 ? totalStretch - startNode.totalStretch
                                               : totalShrink - startNode.totalShrink;
            newRatio = available != 0 ? (float) newDifference / available
                                      : 0;

            if (endNode != null) {
                auxActiveList.removeLast();
            }
            auxActiveList.add(new KnuthNode(par.indexOf(element), startNode.line + 1, 0,
                                            newWidth, newStretch, newShrink,
                                            newRatio, 0, 0, newDifference, 0.0,
                                            startNode));
            //System.out.println("da " + startNode.position + " a " + par.indexOf(element));
            //System.out.println("difference = " + newDifference + " available = " + available + " ratio = " + newRatio);
            //System.out.println(" ");
        } else {
            // start a new line

            // compute difference and ratio
            int actualWidth = totalWidth - endNode.totalWidth;
            if (element.isPenalty()) {
                actualWidth += element.getW();
            }
            newDifference = lineWidth - actualWidth;
            int available = newDifference >= 0 ? totalStretch - endNode.totalStretch
                                               : totalShrink - endNode.totalShrink;
            newRatio = available != 0 ? (float) newDifference / available
                                      : 0;

            auxInactiveList.add(auxActiveList.removeFirst());
            auxActiveList.add(new KnuthNode(par.indexOf(element), endNode.line + 1, 0,
                                            newWidth, newStretch, newShrink,
                                            newRatio, 0, 0, newDifference, 0.0,
                                            endNode));
            //System.out.println("da " + endNode.position + " a " + par.indexOf(element));
            //System.out.println("difference = " + newDifference + " available = " + available + " ratio = " + newRatio);
            //System.out.println(" ");
        }
    }


    private void considerLegalBreak(LinkedList par, int lineWidth,
                                    KnuthElement element,
                                    int totalWidth, int totalStretch,
                                    int totalShrink, double threshold) {
        KnuthNode activeNode = null;

        ListIterator activeListIterator = activeList.listIterator();
        if (activeListIterator.hasNext()) {
            activeNode = (KnuthNode)activeListIterator.next();
        } else {
            activeNode = null;
        }

        lastDeactivated = null;
        lastTooLong = null;

        while (activeNode != null) {
            BestRecords best = new BestRecords();

            // these are the new values that must be computed
            // in order to define a new active node
            int newLine = 0;
            int newFitnessClass = 0;
            int newWidth = 0;
            int newStretch = 0;
            int newShrink = 0;
            double newIPDAdjust = 0;
            double newDemerits = 0;

            while (activeNode != null) {
                // compute the line number
                newLine = activeNode.line + 1;

                // compute the adjustment ratio
                int actualWidth = totalWidth - activeNode.totalWidth;
                if (element.isPenalty()) {
                    actualWidth += element.getW();
                }
                int neededAdjustment = lineWidth - actualWidth;
                int maxAdjustment = 0;
/*LF*/          int availableShrink = totalShrink - activeNode.totalShrink;
/*LF*/          int availableStretch = totalStretch - activeNode.totalStretch;
                if (neededAdjustment > 0) {
                    maxAdjustment = availableStretch;
                    if (maxAdjustment > 0) {
                        newIPDAdjust
                            = (double) neededAdjustment / maxAdjustment;
                    } else {
                        newIPDAdjust = INFINITE_RATIO;
                    }
                } else if (neededAdjustment < 0) {
                    maxAdjustment = availableShrink;
                    if (maxAdjustment > 0) {
                        newIPDAdjust
                            = (double) neededAdjustment / maxAdjustment;
                    } else {
                        newIPDAdjust = -INFINITE_RATIO;
                    }
/*LF*/          } else {
/*LF*/              // neededAdjustment == 0
/*LF*/              newIPDAdjust = 0;
                }

                /* calcola demeriti e fitness class in ogni caso, poi,
                 * a seconda che il coefficiente sia o meno nel range permesso,
                 * aggiorna i record o i migliori nodi disattivati
                 */
                // compute demerits and fitness class
                if (element.isPenalty()
                    && ((KnuthPenalty) element).getP() >= 0) {
                    newDemerits
                        = Math.pow((1
                                    + 100 * Math.pow(Math.abs(newIPDAdjust), 3)
                                    + ((KnuthPenalty) element).getP()), 2);
                } else if (element.isPenalty()
                           && ((KnuthPenalty)element).getP()
                                > - KnuthElement.INFINITE) {
                                /*> -INFINITE_RATIO) { ??? */
                    newDemerits
                        = Math.pow((1
                                    + 100 * Math.pow(Math.abs(newIPDAdjust), 3)), 2)
                        - Math.pow(((KnuthPenalty) element).getP(), 2);
                } else {
                    newDemerits
                        = Math.pow((1
                                    + 100 * Math.pow(Math.abs(newIPDAdjust), 3)), 2);
                }
                if (element.isPenalty()
                    && ((KnuthPenalty) element).isFlagged()
                    && ((KnuthElement) par.get(activeNode.position)).isPenalty()
                    && ((KnuthPenalty) par.get(activeNode.position)).isFlagged()) {
                    // add demerit for consecutive breaks at flagged penalties
                    newDemerits += repeatedFlaggedDemerit;
                }
                if (newIPDAdjust < -0.5) {
                    newFitnessClass = 0;
                } else if (newIPDAdjust <= 0.5) {
                    newFitnessClass = 1;
                } else if (newIPDAdjust <= 1) {
                    newFitnessClass = 2;
                } else {
                    newFitnessClass = 3;
                }
                if (Math.abs(newFitnessClass - activeNode.fitness) > 1) {
                    // add demerit for consecutive breaks
                    // with very different fitness classes
                    newDemerits += incompatibleFitnessDemerit;
                }
                newDemerits += activeNode.totalDemerits;

/*LF*/          //System.out.println(" possibile soluzione, da " + activeNode.position + " a " + par.indexOf(element));
                if ((-1 <= newIPDAdjust) && (newIPDAdjust <= threshold)) {
/*LF*/              //System.out.println("  possibile soluzione buona");
                    if (newDemerits < best.getDemerits(newFitnessClass)) {
                        // updates best demerits data
                        best.addRecord(newDemerits, activeNode, newIPDAdjust,
                                       availableShrink, availableStretch, neededAdjustment, newFitnessClass);
/*LF*/                  lastTooShort = null;
                    }
                } else {
                     // la linea e' troppo piena o troppo vuota
                    if (newIPDAdjust <= -1) {
/*LF*/                  //System.out.println("  possibile soluzione lunga");
                        if (lastTooLong == null || newDemerits < lastTooLong.totalDemerits) {
/*LF*/                      //System.out.println("  aggiornato lastTooLong, da " + activeNode.position + " a " + par.indexOf(element));
                            lastTooLong = new KnuthNode(par.indexOf(element), newLine, newFitnessClass,
                                    totalWidth, totalStretch, totalShrink,
                                    newIPDAdjust, availableShrink, availableStretch, neededAdjustment, newDemerits, activeNode);
                        }
                    } else {
/*LF*/                  //System.out.println("  possibile soluzione corta");
/*LF*/                  //if (lastTooShort != null) {
/*LF*/                  //    System.out.println("  vecchia soluzione corta da " + lastTooShort.previous.position + " a " + lastTooShort.position);
/*LF*/                  //}
                        if (lastTooShort == null || newDemerits <= lastTooShort.totalDemerits) {
/*LF*/                      //System.out.println("  aggiornato lastTooShort, da " + activeNode.position + " a " + par.indexOf(element));
                            lastTooShort = new KnuthNode(par.indexOf(element), newLine, newFitnessClass,
                                    totalWidth, totalStretch, totalShrink,
                                    newIPDAdjust, availableShrink, availableStretch, neededAdjustment, newDemerits, activeNode);
                        }
                    }
                }
                /* disattivazione di un nodo */
                if (newIPDAdjust < -1
                    || (element.isPenalty() 
                        && ((KnuthPenalty)element).getP()
                             == -KnuthElement.INFINITE)
/*LF*/                  && !(activeNode.position
                             == par.indexOf(element))) {
                    // deactivate activeNode
                    KnuthNode tempNode
                        = (KnuthNode)activeListIterator.previous();
                    int iCallNext = 0;
                    while (tempNode != activeNode) {
                        // this is not the node we meant to remove!
                        tempNode = (KnuthNode)activeListIterator.previous();
                        iCallNext ++;
                    }
                    activeListIterator.remove();
                    for (int i = 0; i < iCallNext; i++) {
                        activeListIterator.next();
                    }
                }

                if (activeListIterator.hasNext()) {
                    activeNode = (KnuthNode) activeListIterator.next();
                } else {
                    activeNode = null;
                    break;
                }
                if (activeNode.line >= newLine) {
                    break;
                }
            } // end of the inner while

            if (best.hasRecords()) {
                // compute width, stratchability and shrinkability
                newWidth = totalWidth;
                newStretch = totalStretch;
                newShrink = totalShrink;
                ListIterator tempIterator
                    = par.listIterator(par.indexOf(element));
                while (tempIterator.hasNext()) {
                    KnuthElement tempElement
                        = (KnuthElement)tempIterator.next();
                    if (tempElement.isBox()) {
                        break;
                    } else if (tempElement.isGlue()) {
                        newWidth += ((KnuthGlue) tempElement).getW();
                        newStretch += ((KnuthGlue) tempElement).getY();
                        newShrink += ((KnuthGlue) tempElement).getZ();
                    } else if (((KnuthPenalty) tempElement).getP() 
                               == -KnuthElement.INFINITE
                               && tempElement != element) {
                        break;
                    }
                }

                // add nodes to the active nodes list
                for (int i = 0; i <= 3; i++) {
                    if (best.notInfiniteDemerits(i)
                        && best.getDemerits(i)
                           <= (best.getMinDemerits()
                               + incompatibleFitnessDemerit)) {
                        // the nodes in activeList must be ordered
                        // by line number and position;
                        // so:
                        // 1) advance in the list until the end,
                        // or a node with a higher line number, is reached
/*LF*/                  int iStepsForward = 0;
/*LF*/                  KnuthNode tempNode;
/*LF*/                  while (activeListIterator.hasNext()) {
/*LF*/                      iStepsForward ++;
/*LF*/                      tempNode = (KnuthNode) activeListIterator.next();
/*LF*/                      if (tempNode.line > (best.getNode(i).line + 1)) {
/*LF*/                          activeListIterator.previous();
/*LF*/                          iStepsForward --;
/*LF*/                          break;
/*LF*/                      }
/*LF*/                  }
                        // 2) add the new node
                        activeListIterator.add
                            (new KnuthNode(par.indexOf(element),
                                           best.getNode(i).line + 1, i,
                                           newWidth, newStretch, newShrink,
                                           best.getAdjust(i),
                                           best.getAvailableShrink(i), best.getAvailableStretch(i),
                                           best.getDifference(i),
                                           best.getDemerits(i), 
                                           best.getNode(i)));
                        // 3) go back
/*LF*/                  for (int j = 0;
/*LF*/                       j <= iStepsForward;
/*LF*/                       j ++) {
/*LF*/                      activeListIterator.previous();
/*LF*/                  }
                    }
                }
            }
            if (activeNode == null) {
                break;
            }
        } // end of the outer while
    }

    protected abstract int filterActiveList() ;

    private void calculateBreakPoints(KnuthNode node, KnuthSequence par,
                                      int total) {
        KnuthNode bestActiveNode = node;
        // use bestActiveNode to determine the optimum breakpoints
        for (int i = node.line; i > 0; i--) {
            updateData2(bestActiveNode, par, total);
            bestActiveNode = bestActiveNode.previous;
        }
    }
}
