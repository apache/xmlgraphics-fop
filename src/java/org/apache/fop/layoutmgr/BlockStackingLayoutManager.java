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

/* $Id$ */

package org.apache.fop.layoutmgr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.fop.area.Area;
import org.apache.fop.area.BlockParent;
import org.apache.fop.area.Block;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.traits.MinOptMax;

/**
 * Base LayoutManager class for all areas which stack their child
 * areas in the block-progression direction, such as Flow, Block, ListBlock.
 */
public abstract class BlockStackingLayoutManager extends AbstractLayoutManager
                                                 implements BlockLevelLayoutManager {
    /**
     * Reference to FO whose areas it's managing or to the traits
     * of the FO.
     */
    //protected LayoutManager curChildLM = null; AbstractLayoutManager also defines this!
    protected BlockParent parentArea = null;

    /*LF*/
    /** Value of the block-progression-unit (non-standard property) */
    protected int bpUnit = 0;
    /** space-before value adjusted for block-progression-unit handling */
    protected int adjustedSpaceBefore = 0;
    /** space-after value adjusted for block-progression-unit handling */
    protected int adjustedSpaceAfter = 0;
    /** Only used to store the original list when createUnitElements is called */
    protected LinkedList storedList = null;
    protected FObj fobj;
    private boolean bBreakBeforeServed = false;
    private boolean bSpaceBeforeServed = false;
    protected int referenceIPD = 0;
    /*LF*/

    public BlockStackingLayoutManager(FObj node) {
        super(node);
        fobj = node;
    }

    private BreakCost evaluateBreakCost(Area parent, Area child) {
        return new BreakCost(child, 0);
    }

    /** return current area being filled
     */
    protected BlockParent getCurrentArea() {
        return this.parentArea;
    }


    /**
     * Set the current area being filled.
     */
    protected void setCurrentArea(BlockParent parentArea) {
        this.parentArea = parentArea;
    }

    protected MinOptMax resolveSpaceSpecifier(Area nextArea) {
        SpaceSpecifier spaceSpec = new SpaceSpecifier(false);
        // Area prevArea = getCurrentArea().getLast();
        // if (prevArea != null) {
        //     spaceSpec.addSpace(prevArea.getSpaceAfter());
        // }
        // spaceSpec.addSpace(nextArea.getSpaceBefore());
        return spaceSpec.resolve(false);
    }

    /**
     * Add a block spacer for space before and space after a block.
     * This adds an empty Block area that acts as a block space.
     *
     * @param adjust the adjustment value
     * @param minoptmax the min/opt/max value of the spacing
     */
    public void addBlockSpacing(double adjust, MinOptMax minoptmax) {
        if (minoptmax == null) {
            return;
        }
        int sp = minoptmax.opt;
        if (adjust > 0) {
            sp = sp + (int)(adjust * (minoptmax.max - minoptmax.opt));
        } else {
            sp = sp + (int)(adjust * (minoptmax.opt - minoptmax.min));
        }
        if (sp != 0) {
            Block spacer = new Block();
            spacer.setBPD(sp);
            parentLM.addChildArea(spacer);
        }
    }

    /**
     * Add the childArea to the passed area.
     * Called by child LayoutManager when it has filled one of its areas.
     * The LM should already have an Area in which to put the child.
     * See if the area will fit in the current area.
     * If so, add it. Otherwise initiate breaking.
     * @param childArea the area to add: will be some block-stacked Area.
     * @param parentArea the area in which to add the childArea
     */
    protected void addChildToArea(Area childArea,
                                     BlockParent parentArea) {
        // This should be a block-level Area (Block in the generic sense)
        if (!(childArea instanceof Block)) {
            //log.error("Child not a Block in BlockStackingLM!");
        }

        MinOptMax spaceBefore = resolveSpaceSpecifier(childArea);
        parentArea.addBlock((Block) childArea);
        flush(); // hand off current area to parent
    }


    /**
     * Add the childArea to the current area.
     * Called by child LayoutManager when it has filled one of its areas.
     * The LM should already have an Area in which to put the child.
     * See if the area will fit in the current area.
     * If so, add it. Otherwise initiate breaking.
     * @param childArea the area to add: will be some block-stacked Area.
     */
    public void addChildArea(Area childArea) {
        addChildToArea(childArea, getCurrentArea());
    }

    /**
     * Force current area to be added to parent area.
     */
    protected void flush() {
        if (getCurrentArea() != null) {
            parentLM.addChildArea(getCurrentArea());
        }
    }

    /**
     * @param len length in millipoints to span with bp units
     * @return the minimum integer n such that n * bpUnit >= len
     */
    protected int neededUnits(int len) {
        return (int) Math.ceil((float)len / bpUnit);
    }

    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        /* LF *///System.err.println("BLM.getNextKnuthElements> keep-together = "
              // + layoutProps.keepTogether.getType());
        /* LF *///System.err.println(" keep-with-previous = " +
              // layoutProps.keepWithPrevious.getType());
        /* LF *///System.err.println(" keep-with-next = " +
              // layoutProps.keepWithNext.getType());
        BlockLevelLayoutManager curLM; // currently active LM
        BlockLevelLayoutManager prevLM = null; // previously active LM

        referenceIPD = context.getRefIPD();
        int iIndents = 0;
        int bIndents = 0;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            iIndents = ((org.apache.fop.fo.flow.Block) fobj).getCommonMarginBlock().startIndent.getValue() 
                     + ((org.apache.fop.fo.flow.Block) fobj).getCommonMarginBlock().endIndent.getValue();
            bIndents = ((org.apache.fop.fo.flow.Block) fobj).getCommonBorderPaddingBackground().getBPPaddingAndBorder(false);
        }
        int ipd = referenceIPD - iIndents;

        MinOptMax stackSize = new MinOptMax();

        // Set context for percentage property values.
        fobj.setLayoutDimension(PercentBase.BLOCK_IPD, ipd);
        fobj.setLayoutDimension(PercentBase.BLOCK_BPD, -1);

        LinkedList returnedList = null;
        LinkedList contentList = new LinkedList();
        LinkedList returnList = new LinkedList();
        Position returnPosition = new NonLeafPosition(this, null);

        if (!bBreakBeforeServed) {
            try {
                if (addKnuthElementsForBreakBefore(returnList, returnPosition)) {
                    return returnList;
                }
            } finally {
                bBreakBeforeServed = true;
            }
        }

        if (!bSpaceBeforeServed) {
            addKnuthElementsForSpaceBefore(returnList, returnPosition, alignment);
            bSpaceBeforeServed = true;
        }
        
        addKnuthElementsForBorderPaddingBefore(returnList, returnPosition);
        
        while ((curLM = (BlockLevelLayoutManager) getChildLM()) != null) {
            LayoutContext childLC = new LayoutContext(0);
            if (curLM instanceof LineLayoutManager) {
                // curLM is a LineLayoutManager
                // set stackLimit for lines
                childLC.setStackLimit(new MinOptMax(ipd/*
                                                         * - iIndents -
                                                         * iTextIndent
                                                         */));
                childLC.setRefIPD(ipd);
            } else {
                // curLM is a ?
                childLC.setStackLimit(MinOptMax.subtract(context
                        .getStackLimit(), stackSize));
                childLC.setRefIPD(referenceIPD);
            }

            // get elements from curLM
            returnedList = curLM.getNextKnuthElements(childLC, alignment);
            if (returnedList.size() == 1
                    && ((KnuthElement) returnedList.getFirst()).isPenalty()
                    && ((KnuthPenalty) returnedList.getFirst()).getP() == -KnuthElement.INFINITE) {
                // a descendant of this block has break-before
                if (returnList.size() == 0) {
                    // the first child (or its first child ...) has
                    // break-before;
                    // all this block, including space before, will be put in
                    // the
                    // following page
                    bSpaceBeforeServed = false;
                }
                contentList.addAll(returnedList);

                /* extension: conversione di tutta la sequenza fin'ora ottenuta */
                if (bpUnit > 0) {
                    storedList = contentList;
                    contentList = createUnitElements(contentList);
                }
                /* end of extension */

                // "wrap" the Position inside each element
                // moving the elements from contentList to returnList
                returnedList = new LinkedList();
                wrapPositionElements(contentList, returnList);

                return returnList;
            } else {
                if (prevLM != null) {
                    // there is a block handled by prevLM
                    // before the one handled by curLM
                    if (mustKeepTogether() 
                            || prevLM.mustKeepWithNext()
                            || curLM.mustKeepWithPrevious()) {
                        // add an infinite penalty to forbid a break between
                        // blocks
                        contentList.add(new KnuthPenalty(0,
                                KnuthElement.INFINITE, false,
                                new Position(this), false));
                    } else if (!((KnuthElement) contentList.getLast()).isGlue()) {
                        // add a null penalty to allow a break between blocks
                        contentList.add(new KnuthPenalty(0, 0, false,
                                new Position(this), false));
                    } else {
                        // the last element in contentList is a glue;
                        // it is a feasible breakpoint, there is no need to add
                        // a penalty
                    }
                }
                if (returnedList.size() == 0) {
                    //Avoid NoSuchElementException below (happens with empty blocks)
                    continue;
                }
                contentList.addAll(returnedList);
                if (((KnuthElement) returnedList.getLast()).isPenalty()
                        && ((KnuthPenalty) returnedList.getLast()).getP() == -KnuthElement.INFINITE) {
                    // a descendant of this block has break-after
                    if (curLM.isFinished()) {
                        // there is no other content in this block;
                        // it's useless to add space after before a page break
                        setFinished(true);
                    }

                    /* extension: conversione di tutta la sequenza fin'ora ottenuta */
                    if (bpUnit > 0) {
                        storedList = contentList;
                        contentList = createUnitElements(contentList);
                    }
                    /* end of extension */

                    returnedList = new LinkedList();
                    wrapPositionElements(contentList, returnList);

                    return returnList;
                }
                /*
                if (allocatedSpace.min > context.getStackLimit().max) {
                    log.debug("Allocated space exceeds stack limit, returning early.");
                    return returnList;
                }*/
            }
            prevLM = curLM;
        }

        /* Extension: conversione di tutta la sequenza fin'ora ottenuta */
        if (bpUnit > 0) {
            storedList = contentList;
            contentList = createUnitElements(contentList);
        }
        /* end of extension */

        returnedList = new LinkedList();
        wrapPositionElements(contentList, returnList);

        addKnuthElementsForBorderPaddingAfter(returnList, returnPosition);
        addKnuthElementsForSpaceAfter(returnList, returnPosition, alignment);
        addKnuthElementsForBreakAfter(returnList, returnPosition);

        setFinished(true);

        return returnList;
    }

    public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
/*LF*/  //System.out.println("  BLM.negotiateBPDAdjustment> " + adj);
/*LF*/  //System.out.println("  lastElement e' " + (lastElement.isPenalty() ? "penalty" : (lastElement.isGlue() ? "glue" : "box" )));
/*LF*/  //System.out.println("  position e' " + lastElement.getPosition().getClass().getName());
/*LF*/  //System.out.println("  " + (bpUnit > 0 ? "unit" : ""));
        Position innerPosition = ((NonLeafPosition) lastElement.getPosition()).getPosition();

        if (innerPosition == null && lastElement.isGlue()) {
            // this adjustment applies to space-before or space-after of this block
            if (((KnuthGlue) lastElement).getAdjustmentClass() == SPACE_BEFORE_ADJUSTMENT) {
                // this adjustment applies to space-before
                adjustedSpaceBefore += adj;
/*LF*/          //System.out.println("  BLM.negotiateBPDAdjustment> spazio prima: " + adj);
            } else {
                // this adjustment applies to space-after
                adjustedSpaceAfter += adj;
/*LF*/          //System.out.println("  BLM.negotiateBPDAdjustment> spazio dopo: " + adj);
            }
            return adj;
        } else if (innerPosition instanceof MappingPosition) {
            // this block has block-progression-unit > 0: the adjustment can concern
            // - the space-before or space-after of this block, 
            // - the line number of a descendant of this block
            if (lastElement.isGlue()) {
                // lastElement is a glue
/*LF*/          //System.out.println("  BLM.negotiateBPDAdjustment> bpunit con glue");
                ListIterator storedListIterator = storedList.listIterator(((MappingPosition) innerPosition).getFirstIndex());
                int newAdjustment = 0;
                while (storedListIterator.nextIndex() <= ((MappingPosition) innerPosition).getLastIndex()) {
                    KnuthElement storedElement = (KnuthElement) storedListIterator.next();
                    if (storedElement.isGlue()) {
                        newAdjustment += ((BlockLevelLayoutManager) storedElement.getLayoutManager()).negotiateBPDAdjustment(adj - newAdjustment, storedElement);
/*LF*/                  //System.out.println("  BLM.negotiateBPDAdjustment> (progressivo) righe: " + newAdjustment);
                    }
                }
                newAdjustment = (newAdjustment > 0 ? bpUnit * neededUnits(newAdjustment)
                                                   : - bpUnit * neededUnits(- newAdjustment));
                return newAdjustment;
            } else {
                // lastElement is a penalty: this means that the paragraph
                // has been split between consecutive pages:
                // this may involve a change in the number of lines
/*LF*/          //System.out.println("  BLM.negotiateBPDAdjustment> bpunit con penalty");
                KnuthPenalty storedPenalty = (KnuthPenalty)
                                             storedList.get(((MappingPosition) innerPosition).getLastIndex());
                if (storedPenalty.getW() > 0) {
                    // the original penalty has width > 0
/*LF*/              //System.out.println("  BLM.negotiateBPDAdjustment> chiamata passata");
                    return ((BlockLevelLayoutManager) storedPenalty.getLayoutManager())
                           .negotiateBPDAdjustment(storedPenalty.getW(), (KnuthElement) storedPenalty);
                } else {
                    // the original penalty has width = 0
                    // the adjustment involves only the spaces before and after
/*LF*/              //System.out.println("  BLM.negotiateBPDAdjustment> chiamata gestita");
                    return adj;
                }
            }
        } else if (innerPosition.getLM() != this) {
            // this adjustment concerns another LM
            NonLeafPosition savedPos = (NonLeafPosition) lastElement.getPosition();
            lastElement.setPosition(innerPosition);
            int returnValue = ((BlockLevelLayoutManager) lastElement.getLayoutManager()).negotiateBPDAdjustment(adj, lastElement);
            lastElement.setPosition(savedPos);
/*LF*/      //System.out.println("  BLM.negotiateBPDAdjustment> righe: " + returnValue);
            return returnValue;
        } else {
            // this should never happen
            System.err.println("BlockLayoutManager.negotiateBPDAdjustment(): unexpected Position");
            return 0;
        }
    }

    public void discardSpace(KnuthGlue spaceGlue) {
/*LF*/  //System.out.println("  BLM.discardSpace> " + spaceGlue.getPosition().getClass().getName());
        Position innerPosition = ((NonLeafPosition) spaceGlue.getPosition()).getPosition();

/*LF*/  if (innerPosition == null || innerPosition.getLM() == this) {
            // if this block has block-progression-unit > 0, innerPosition can be
            // a MappingPosition
            // spaceGlue represents space before or space after of this block
            if (spaceGlue.getAdjustmentClass() == SPACE_BEFORE_ADJUSTMENT) {
                // space-before must be discarded
                adjustedSpaceBefore = 0;
            } else {
                // space-after must be discarded
                adjustedSpaceAfter = 0;
                //TODO Why are both cases handled in the same way?
            }
/*LF*/  } else {
            // this element was not created by this BlockLM
            NonLeafPosition savedPos = (NonLeafPosition)spaceGlue.getPosition();
            spaceGlue.setPosition(innerPosition);
            ((BlockLevelLayoutManager) spaceGlue.getLayoutManager()).discardSpace(spaceGlue);
            spaceGlue.setPosition(savedPos);
        }
    }

    public LinkedList getChangedKnuthElements(List oldList, /*int flaggedPenalty,*/ int alignment) {
/*LF*/  //System.out.println("");
/*LF*/  //System.out.println("  BLM.getChangedKnuthElements> inizio: oldList.size() = " + oldList.size());
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement returnedElement;
        KnuthElement currElement = null;
        KnuthElement prevElement = null;
        LinkedList returnedList = new LinkedList();
        LinkedList returnList = new LinkedList();
        int fromIndex = 0;

        // "unwrap" the Positions stored in the elements
        KnuthElement oldElement = null;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement)oldListIterator.next();
            Position innerPosition = ((NonLeafPosition) oldElement.getPosition()).getPosition();
/*LF*/      //System.out.println(" BLM> unwrapping: " + (oldElement.isBox() ? "box    " : (oldElement.isGlue() ? "glue   " : "penalty")) + " creato da " + oldElement.getLayoutManager().getClass().getName());
/*LF*/      //System.out.println(" BLM> unwrapping:         " + oldElement.getPosition().getClass().getName());
            if (innerPosition != null) {
                // oldElement was created by a descendant of this BlockLM
                oldElement.setPosition(innerPosition);
            } else {
                // thisElement was created by this BlockLM
                // modify its position in order to recognize it was not created
                // by a child
                oldElement.setPosition(new Position(this));
            }
        }

        // create the iterator
        List workList;
        if (bpUnit == 0) {
            workList = oldList;
        } else {
            // the storedList must be used instead of oldList;
            // find the index of the first element of returnedList
            // corresponding to the first element of oldList
            oldListIterator = oldList.listIterator();
            KnuthElement el = (KnuthElement) oldListIterator.next();
            while (!(el.getPosition() instanceof MappingPosition)) {
                el = (KnuthElement) oldListIterator.next();
            }
            int iFirst = ((MappingPosition) el.getPosition()).getFirstIndex();

            // find the index of the last element of returnedList
            // corresponding to the last element of oldList
            oldListIterator = oldList.listIterator(oldList.size());
            el = (KnuthElement) oldListIterator.previous();
            while (!(el.getPosition() instanceof MappingPosition)) {
                el = (KnuthElement) oldListIterator.previous();
            }
            int iLast = ((MappingPosition) el.getPosition()).getLastIndex();

/*LF*/      //System.out.println("  si usa storedList da " + iFirst + " a " + iLast + " compresi su " + storedList.size() + " elementi totali");
            workList = storedList.subList(iFirst, iLast + 1);
        }
        ListIterator workListIterator = workList.listIterator();

/*LF*/  //System.out.println("");
/*LF*/  //System.out.println("  BLM.getChangedKnuthElements> workList.size() = " + workList.size() + " da 0 a " + (workList.size() - 1));

        while (workListIterator.hasNext()) {
            currElement = (KnuthElement) workListIterator.next();
/*LF*/      //System.out.println("elemento n. " + workListIterator.previousIndex() + " nella workList");
            if (prevElement != null
                && prevElement.getLayoutManager() != currElement.getLayoutManager()) {
                // prevElement is the last element generated by the same LM
                BlockLevelLayoutManager prevLM = (BlockLevelLayoutManager)
                                                 prevElement.getLayoutManager();
                BlockLevelLayoutManager currLM = (BlockLevelLayoutManager)
                                                 currElement.getLayoutManager();
                boolean bSomethingAdded = false;
                if (prevLM != this) {
/*LF*/              //System.out.println(" BLM.getChangedKnuthElements> chiamata da " + fromIndex + " a " + workListIterator.previousIndex() + " su " + prevLM.getClass().getName());
                    returnedList.addAll(prevLM.getChangedKnuthElements(workList.subList(fromIndex, workListIterator.previousIndex()),
                                                                       /*flaggedPenalty,*/ alignment));
                    bSomethingAdded = true;
                } else {
                    // prevLM == this
                    // do nothing
/*LF*/              //System.out.println(" BLM.getChangedKnuthElements> elementi propri, ignorati, da " + fromIndex + " a " + workListIterator.previousIndex() + " su " + prevLM.getClass().getName());
                }
                fromIndex = workListIterator.previousIndex();

                // there is another block after this one
                if (bSomethingAdded
                    && (this.mustKeepTogether()
                        || prevLM.mustKeepWithNext()
                        || currLM.mustKeepWithPrevious())) {
                    // add an infinite penalty to forbid a break between blocks
                    returnedList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, new Position(this), false));
                } else if (bSomethingAdded && !((KnuthElement) returnedList.getLast()).isGlue()) {
                    // add a null penalty to allow a break between blocks
                    returnedList.add(new KnuthPenalty(0, 0, false, new Position(this), false));
                }
            }
            prevElement = currElement;
        }
        if (currElement != null) {
            BlockLevelLayoutManager currLM = (BlockLevelLayoutManager)
                                             currElement.getLayoutManager();
            if (currLM != this) {
/*LF*/          //System.out.println(" BLM.getChangedKnuthElements> chiamata da " + fromIndex + " a " + oldList.size() + " su " + currLM.getClass().getName());
                returnedList.addAll(currLM.getChangedKnuthElements(workList.subList(fromIndex, workList.size()),
                                                                   /*flaggedPenalty,*/ alignment));
            } else {
                // currLM == this
                // there are no more elements to add
                // remove the last penalty added to returnedList
                if (returnedList.size() > 0) {
                    returnedList.removeLast();
                }
/*LF*/          //System.out.println(" BLM.getChangedKnuthElements> elementi propri, ignorati, da " + fromIndex + " a " + workList.size());
            }
        }

        // append elements representing space-before
        boolean spaceBeforeIsConditional = true;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            spaceBeforeIsConditional =
                ((org.apache.fop.fo.flow.Block) fobj).getCommonMarginBlock().spaceBefore.getSpace().isDiscard();
        }
        if (bpUnit > 0
            || adjustedSpaceBefore != 0) {
            if (!spaceBeforeIsConditional) {
                // add elements to prevent the glue to be discarded
                returnList.add(new KnuthBox(0,
                                            new NonLeafPosition(this, null), false));
                returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                                new NonLeafPosition(this, null), false));
            }
            if (bpUnit > 0) {
                returnList.add(new KnuthGlue(0, 0, 0,
                                             SPACE_BEFORE_ADJUSTMENT, new NonLeafPosition(this, null), true));
            } else {
                returnList.add(new KnuthGlue(adjustedSpaceBefore, 0, 0,
                                             SPACE_BEFORE_ADJUSTMENT, new NonLeafPosition(this, null), true));
            }
        }

/*LF*/  //System.out.println("  BLM.getChangedKnuthElements> intermedio: returnedList.size() = " + returnedList.size());

/* estensione: conversione complessiva */
/*LF*/  if (bpUnit > 0) {
/*LF*/      storedList = returnedList;
/*LF*/      returnedList = createUnitElements(returnedList);
/*LF*/  }
/* estensione */

        // "wrap" the Position stored in each element of returnedList
        // and add elements to returnList
        ListIterator listIter = returnedList.listIterator();
        while (listIter.hasNext()) {
            returnedElement = (KnuthElement)listIter.next();
            returnedElement.setPosition(new NonLeafPosition(this, returnedElement.getPosition()));
            returnList.add(returnedElement);
        }

        // append elements representing space-after
        boolean spaceAfterIsConditional = true;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            spaceAfterIsConditional =
                ((org.apache.fop.fo.flow.Block) fobj).getCommonMarginBlock().spaceAfter.getSpace().isDiscard();
        }
        if (bpUnit > 0
            || adjustedSpaceAfter != 0) {
            if (!spaceAfterIsConditional) {
                returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                                new NonLeafPosition(this, null), false));
            }
            if (bpUnit > 0) {
                returnList.add(new KnuthGlue(0, 0, 0,
                                             SPACE_AFTER_ADJUSTMENT, new NonLeafPosition(this, null),
                                             (!spaceAfterIsConditional) ? false : true));
            } else {
                returnList.add(new KnuthGlue(adjustedSpaceAfter, 0, 0,
                                             SPACE_AFTER_ADJUSTMENT, new NonLeafPosition(this, null),
                                             (!spaceAfterIsConditional) ? false : true));
            }
            if (!spaceAfterIsConditional) {
                returnList.add(new KnuthBox(0,
                                            new NonLeafPosition(this, null), true));
            }
        }

/*LF*/  //System.out.println("  BLM.getChangedKnuthElements> fine: returnList.size() = " + returnList.size());
        return returnList;
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepTogether()
     */
    public boolean mustKeepTogether() {
        return false;
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithPrevious()
     */
    public boolean mustKeepWithPrevious() {
        return false;
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithNext()
     */
    public boolean mustKeepWithNext() {
        return false;
    }

    /**
     * Creates Knuth elements for before border padding and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param returnPosition applicable return position
     */
    protected void addKnuthElementsForBorderPaddingBefore(LinkedList returnList, 
            Position returnPosition) {
        //Border and Padding (before)
        CommonBorderPaddingBackground borderAndPadding = null;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            borderAndPadding =
                ((org.apache.fop.fo.flow.Block) fobj).getCommonBorderPaddingBackground();
        } else if (fobj instanceof org.apache.fop.fo.flow.BlockContainer) {
            borderAndPadding =
                ((org.apache.fop.fo.flow.BlockContainer) fobj).getCommonBorderPaddingBackground();
        }
        if (borderAndPadding != null) {
            //TODO Handle conditionality
            int bpBefore = borderAndPadding.getBorderBeforeWidth(false)
                         + borderAndPadding.getPaddingBefore(false);
            if (bpBefore > 0) {
                returnList.add(new KnuthBox(bpBefore, returnPosition, true));
            }
        }
    }

    /**
     * Creates Knuth elements for after border padding and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param returnPosition applicable return position
     */
    protected void addKnuthElementsForBorderPaddingAfter(LinkedList returnList, 
            Position returnPosition) {
        //Border and Padding (after)
        CommonBorderPaddingBackground borderAndPadding = null;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            borderAndPadding =
                ((org.apache.fop.fo.flow.Block) fobj).getCommonBorderPaddingBackground();
        } else if (fobj instanceof org.apache.fop.fo.flow.BlockContainer) {
            borderAndPadding =
                ((org.apache.fop.fo.flow.BlockContainer) fobj).getCommonBorderPaddingBackground();
        }
        if (borderAndPadding != null) {
            //TODO Handle conditionality
            int bpAfter = borderAndPadding.getBorderAfterWidth(false)
                        + borderAndPadding.getPaddingAfter(false);
            if (bpAfter > 0) {
                returnList.add(new KnuthBox(bpAfter, returnPosition, true));
            }
        }
    }

    /**
     * Creates Knuth elements for break-before and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param returnPosition applicable return position
     * @return true if an element has been added due to a break-before.
     */
    protected boolean addKnuthElementsForBreakBefore(LinkedList returnList, 
            Position returnPosition) {
        int breakBefore = -1;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            breakBefore = ((org.apache.fop.fo.flow.Block) fobj).getBreakBefore();
        } else if (fobj instanceof org.apache.fop.fo.flow.BlockContainer) {
            breakBefore = ((org.apache.fop.fo.flow.BlockContainer) fobj).getBreakBefore();
        }
        if (breakBefore == EN_PAGE
                || breakBefore == EN_COLUMN 
                || breakBefore == EN_EVEN_PAGE 
                || breakBefore == EN_ODD_PAGE) {
            // return a penalty element, representing a forced page break
            returnList.add(new KnuthPenalty(0, -KnuthElement.INFINITE, false,
                    breakBefore, returnPosition, false));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates Knuth elements for break-after and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param returnPosition applicable return position
     * @return true if an element has been added due to a break-after.
     */
    protected boolean addKnuthElementsForBreakAfter(LinkedList returnList, 
            Position returnPosition) {
        int breakAfter = -1;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            breakAfter = ((org.apache.fop.fo.flow.Block) fobj).getBreakAfter();
        } else if (fobj instanceof org.apache.fop.fo.flow.BlockContainer) {
            breakAfter = ((org.apache.fop.fo.flow.BlockContainer) fobj).getBreakAfter();
        }
        if (breakAfter == EN_PAGE
                || breakAfter == EN_COLUMN
                || breakAfter == EN_EVEN_PAGE
                || breakAfter == EN_ODD_PAGE) {
            // add a penalty element, representing a forced page break
            returnList.add(new KnuthPenalty(0, -KnuthElement.INFINITE, false,
                    breakAfter, returnPosition, false));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates Knuth elements for space-before and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param returnPosition applicable return position
     * @param alignment vertical alignment
     */
    protected void addKnuthElementsForSpaceBefore(LinkedList returnList, 
            Position returnPosition, int alignment) {
        SpaceProperty spaceBefore = null;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            spaceBefore = ((org.apache.fop.fo.flow.Block) fobj).getCommonMarginBlock().spaceBefore;
        } else if (fobj instanceof org.apache.fop.fo.flow.BlockContainer) {
            spaceBefore = ((org.apache.fop.fo.flow.BlockContainer) fobj).getCommonMarginBlock().spaceBefore;
        }
        // append elements representing space-before
        if (bpUnit > 0
                || spaceBefore != null
                   && !(spaceBefore.getMinimum().getLength().getValue() == 0 
                        && spaceBefore.getMaximum().getLength().getValue() == 0)) {
            if (spaceBefore != null && !spaceBefore.getSpace().isDiscard()) {
                // add elements to prevent the glue to be discarded
                returnList.add(new KnuthBox(0, returnPosition, false));
                returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE,
                        false, returnPosition, false));
            }
            if (bpUnit > 0) {
                returnList.add(new KnuthGlue(0, 0, 0,
                        BlockLevelLayoutManager.SPACE_BEFORE_ADJUSTMENT, 
                        returnPosition, true));
            } else /*if (alignment == EN_JUSTIFY)*/ {
                returnList.add(new KnuthGlue(
                        spaceBefore.getOptimum().getLength().getValue(),
                        spaceBefore.getMaximum().getLength().getValue()
                                - spaceBefore.getOptimum().getLength().getValue(),
                        spaceBefore.getOptimum().getLength().getValue()
                                - spaceBefore.getMinimum().getLength().getValue(),
                        BlockLevelLayoutManager.SPACE_BEFORE_ADJUSTMENT, 
                        returnPosition, true));
            } /*else {
                returnList.add(new KnuthGlue(
                        spaceBefore.getOptimum().getLength().getValue(), 
                        0, 0, BlockLevelLayoutManager.SPACE_BEFORE_ADJUSTMENT,
                        returnPosition, true));
            }*/
        }
    }

    /**
     * Creates Knuth elements for space-after and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param returnPosition applicable return position
     * @param alignment vertical alignment
     */
    protected void addKnuthElementsForSpaceAfter(LinkedList returnList, Position returnPosition, 
                int alignment) {
        SpaceProperty spaceAfter = null;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            spaceAfter = ((org.apache.fop.fo.flow.Block) fobj).getCommonMarginBlock().spaceAfter;
        } else if (fobj instanceof org.apache.fop.fo.flow.Block) {
            spaceAfter = ((org.apache.fop.fo.flow.BlockContainer) fobj).getCommonMarginBlock().spaceAfter;
        }
        // append elements representing space-after
        if (bpUnit > 0
                || spaceAfter != null
                   && !(spaceAfter.getMinimum().getLength().getValue() == 0 
                        && spaceAfter.getMaximum().getLength().getValue() == 0)) {
            if (spaceAfter != null && !spaceAfter.getSpace().isDiscard()) {
                returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE,
                        false, returnPosition, false));
            }
            if (bpUnit > 0) {
                returnList.add(new KnuthGlue(0, 0, 0, 
                        BlockLevelLayoutManager.SPACE_AFTER_ADJUSTMENT,
                        returnPosition, true));
            } else /*if (alignment == EN_JUSTIFY)*/ {
                returnList.add(new KnuthGlue(
                        spaceAfter.getOptimum().getLength().getValue(),
                        spaceAfter.getMaximum().getLength().getValue()
                                - spaceAfter.getOptimum().getLength().getValue(),
                        spaceAfter.getOptimum().getLength().getValue()
                                - spaceAfter.getMinimum().getLength().getValue(),
                        BlockLevelLayoutManager.SPACE_AFTER_ADJUSTMENT, returnPosition,
                        (!spaceAfter.getSpace().isDiscard()) ? false : true));
            } /*else {
                returnList.add(new KnuthGlue(
                        spaceAfter.getOptimum().getLength().getValue(), 0, 0,
                        BlockLevelLayoutManager.SPACE_AFTER_ADJUSTMENT, returnPosition,
                        (!spaceAfter.getSpace().isDiscard()) ? false : true));
            }*/
            if (spaceAfter != null && !spaceAfter.getSpace().isDiscard()) {
                returnList.add(new KnuthBox(0, returnPosition, true));
            }
        }
    }

    protected LinkedList createUnitElements(LinkedList oldList) {
        //System.out.println(" ");
        //System.out.println("Inizio conversione: " + oldList.size() + " elementi, spazio minimo prima= " + layoutProps.spaceBefore.getSpace().min
        //                   + " spazio minimo dopo= " + layoutProps.spaceAfter.getSpace().min);
        // add elements at the beginning and at the end of oldList
        // representing minimum spaces
        LayoutManager lm = ((KnuthElement)oldList.getFirst()).getLayoutManager();
        boolean bAddedBoxBefore = false;
        boolean bAddedBoxAfter = false;
        if (adjustedSpaceBefore > 0) {
            oldList.addFirst(new KnuthBox(adjustedSpaceBefore,
                                          new Position(lm), true));
            bAddedBoxBefore = true;
        }
        if (adjustedSpaceAfter > 0) {
            oldList.addLast(new KnuthBox(adjustedSpaceAfter,
                                         new Position(lm), true));
            bAddedBoxAfter = true;
        }

        MinOptMax totalLength = new MinOptMax(0);
        MinOptMax totalUnits = new MinOptMax(0);
        LinkedList newList = new LinkedList();

        //System.out.println(" ");
        //System.out.println(" Prima scansione");
        // scan the list once to compute total min, opt and max length
        ListIterator oldListIterator = oldList.listIterator();
        while (oldListIterator.hasNext()) {
            KnuthElement element = (KnuthElement) oldListIterator.next();
            if (element.isBox()) {
/*LF*/          totalLength.add(new MinOptMax(element.getW()));
/*LF*/          //System.out.println("box " + element.getW());               
/*LF*/      } else if (element.isGlue()) {
/*LF*/          totalLength.min -= ((KnuthGlue) element).getZ();
/*LF*/          totalLength.max += ((KnuthGlue) element).getY();
/*LF*/          //leafValue = ((LeafPosition) element.getPosition()).getLeafPos();
/*LF*/          //System.out.println("glue " + element.getW() + " + " + ((KnuthGlue) element).getY() + " - " + ((KnuthGlue) element).getZ());
/*LF*/      } else {
/*LF*/          //System.out.println((((KnuthPenalty)element).getP() == KnuthElement.INFINITE ? "PENALTY " : "penalty ") + element.getW());
            }
        }
        // compute the total amount of "units"
        totalUnits = new MinOptMax(neededUnits(totalLength.min),
                                   neededUnits(totalLength.opt),
                                   neededUnits(totalLength.max));
        //System.out.println(" totalLength= " + totalLength);
        //System.out.println(" unita'= " + totalUnits);

        //System.out.println(" ");
        //System.out.println(" Seconda scansione");
        // scan the list once more, stopping at every breaking point
        // in order to compute partial min, opt and max length
        // and create the new elements
        oldListIterator = oldList.listIterator();
        boolean bPrevIsBox = false;
        MinOptMax lengthBeforeBreak = new MinOptMax(0);
        MinOptMax lengthAfterBreak = (MinOptMax) totalLength.clone();
        MinOptMax unitsBeforeBreak;
        MinOptMax unitsAfterBreak;
        MinOptMax unsuppressibleUnits = new MinOptMax(0);
        int firstIndex = 0;
        int lastIndex = -1;
        while (oldListIterator.hasNext()) {
            KnuthElement element = (KnuthElement) oldListIterator.next();
            lastIndex ++;
            if (element.isBox()) {
                lengthBeforeBreak.add(new MinOptMax(element.getW()));
                lengthAfterBreak.subtract(new MinOptMax(element.getW()));
                bPrevIsBox = true;
            } else if (element.isGlue()) {
                lengthBeforeBreak.min -= ((KnuthGlue) element).getZ();
                lengthAfterBreak.min += ((KnuthGlue) element).getZ();
                lengthBeforeBreak.max += ((KnuthGlue) element).getY();
                lengthAfterBreak.max -= ((KnuthGlue) element).getY();
                bPrevIsBox = false;
            } else {
                lengthBeforeBreak.add(new MinOptMax(element.getW()));
                bPrevIsBox = false;
            }

            // create the new elements
            if (element.isPenalty() && ((KnuthPenalty) element).getP() < KnuthElement.INFINITE
                || element.isGlue() && bPrevIsBox
                || !oldListIterator.hasNext()) {
                // suppress elements after the breaking point
                int iStepsForward = 0;
                while (oldListIterator.hasNext()) {
                    KnuthElement el = (KnuthElement) oldListIterator.next();
                    iStepsForward++;
                    if (el.isGlue()) {
                        // suppressed glue
                        lengthAfterBreak.min += ((KnuthGlue) el).getZ();
                        lengthAfterBreak.max -= ((KnuthGlue) el).getY();
                    } else if (el.isPenalty()) {
                        // suppressed penalty, do nothing
                    } else {
                        // box, end of suppressions
                        break;
                    }
                }
                // compute the partial amount of "units" before and after the break
                unitsBeforeBreak = new MinOptMax(neededUnits(lengthBeforeBreak.min),
                                                 neededUnits(lengthBeforeBreak.opt),
                                                 neededUnits(lengthBeforeBreak.max));
                unitsAfterBreak = new MinOptMax(neededUnits(lengthAfterBreak.min),
                                                neededUnits(lengthAfterBreak.opt),
                                                neededUnits(lengthAfterBreak.max));

                // rewind the iterator and lengthAfterBreak
                for (int i = 0; i < iStepsForward; i++) {
                    KnuthElement el = (KnuthElement) oldListIterator.previous();
                    if (el.isGlue()) {
                        lengthAfterBreak.min -= ((KnuthGlue) el).getZ();
                        lengthAfterBreak.max += ((KnuthGlue) el).getY();
                    }
                }

                // compute changes in length, stretch and shrink
                int uLengthChange = unitsBeforeBreak.opt + unitsAfterBreak.opt - totalUnits.opt;
                int uStretchChange = (unitsBeforeBreak.max + unitsAfterBreak.max - totalUnits.max)
                                     - (unitsBeforeBreak.opt + unitsAfterBreak.opt - totalUnits.opt);
                int uShrinkChange = (unitsBeforeBreak.opt + unitsAfterBreak.opt - totalUnits.opt)
                                    - (unitsBeforeBreak.min + unitsAfterBreak.min - totalUnits.min);

                // compute the number of normal, stretch and shrink unit
                // that must be added to the new sequence
                int uNewNormal = unitsBeforeBreak.opt - unsuppressibleUnits.opt;
                int uNewStretch = (unitsBeforeBreak.max - unitsBeforeBreak.opt)
                                  - (unsuppressibleUnits.max - unsuppressibleUnits.opt);
                int uNewShrink = (unitsBeforeBreak.opt - unitsBeforeBreak.min)
                                 - (unsuppressibleUnits.opt - unsuppressibleUnits.min);

/*LF*/          //System.out.println("(" + unsuppressibleUnits.min + "-" + unsuppressibleUnits.opt + "-" +  unsuppressibleUnits.max + ") "
/*LF*/          //                   + " -> " + unitsBeforeBreak.min + "-" + unitsBeforeBreak.opt + "-" +  unitsBeforeBreak.max
/*LF*/          //                   + " + " + unitsAfterBreak.min + "-" + unitsAfterBreak.opt + "-" +  unitsAfterBreak.max
/*LF*/          //                   + (uLengthChange != 0 ? " [length " + uLengthChange + "] " : "")
/*LF*/          //                   + (uStretchChange != 0 ? " [stretch " + uStretchChange + "] " : "")
/*LF*/          //                   + (uShrinkChange != 0 ? " [shrink " + uShrinkChange + "]" : "")
/*LF*/          //                   );

                // create the MappingPosition which will be stored in the new elements
                // correct firstIndex and lastIndex
                int firstIndexCorrection = 0;
                int lastIndexCorrection = 0;
                if (bAddedBoxBefore) {
                    if (firstIndex != 0) {
                        firstIndexCorrection ++;
                    }
                    lastIndexCorrection ++;
                }
                if (bAddedBoxAfter && lastIndex == (oldList.size() - 1)) {
                    lastIndexCorrection ++;
                }
                MappingPosition mappingPos = new MappingPosition(this,
                                                                 firstIndex - firstIndexCorrection,
                                                                 lastIndex - lastIndexCorrection);

                // new box
                newList.add(new KnuthBox((uNewNormal - uLengthChange) * bpUnit,
                                         mappingPos,
                                         false));
                unsuppressibleUnits.add(new MinOptMax(uNewNormal - uLengthChange));
                //System.out.println("        box " + (uNewNormal - uLengthChange));

                // new infinite penalty, glue and box, if necessary
                if (uNewStretch - uStretchChange > 0
                    || uNewShrink - uShrinkChange > 0) {
                    int iStretchUnits = (uNewStretch - uStretchChange > 0 ? (uNewStretch - uStretchChange) : 0);
                    int iShrinkUnits = (uNewShrink - uShrinkChange > 0 ? (uNewShrink - uShrinkChange) : 0);
                    newList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                                 mappingPos,
                                                 false));
                    newList.add(new KnuthGlue(0,
                                              iStretchUnits * bpUnit,
                                              iShrinkUnits * bpUnit,
                                              LINE_NUMBER_ADJUSTMENT,
                                              mappingPos,
                                              false));
                    //System.out.println("        PENALTY");
                    //System.out.println("        glue 0 " + iStretchUnits + " " + iShrinkUnits);
                    unsuppressibleUnits.max += iStretchUnits;
                    unsuppressibleUnits.min -= iShrinkUnits;
                    if (!oldListIterator.hasNext()) {
                        newList.add(new KnuthBox(0,
                                                 mappingPos,
                                                 false));
                        //System.out.println("        box 0");
                    }
                }

                // new breaking sequence
                if (uStretchChange != 0
                    || uShrinkChange != 0) {
                    // new infinite penalty, glue, penalty and glue
                    newList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                                 mappingPos,
                                                 false));
                    newList.add(new KnuthGlue(0,
                                              uStretchChange * bpUnit,
                                              uShrinkChange * bpUnit,
                                              LINE_NUMBER_ADJUSTMENT,
                                              mappingPos,
                                              false));
                    newList.add(new KnuthPenalty(uLengthChange * bpUnit,
                                                 0, false, element.getPosition(), false));
                    newList.add(new KnuthGlue(0,
                                              - uStretchChange * bpUnit,
                                              - uShrinkChange * bpUnit,
                                              LINE_NUMBER_ADJUSTMENT,
                                              mappingPos,
                                              false));
                    //System.out.println("        PENALTY");
                    //System.out.println("        glue 0 " + uStretchChange + " " + uShrinkChange);
                    //System.out.println("        penalty " + uLengthChange + " * unit");
                    //System.out.println("        glue 0 " + (- uStretchChange) + " " + (- uShrinkChange));
                } else if (oldListIterator.hasNext()){
                    // new penalty
                    newList.add(new KnuthPenalty(uLengthChange * bpUnit,
                                                 0, false,
                                                 mappingPos,
                                                 false));
                    //System.out.println("        penalty " + uLengthChange + " * unit");
                }
                // update firstIndex
                firstIndex = lastIndex + 1;
            }

            if (element.isPenalty()) {
                lengthBeforeBreak.add(new MinOptMax(-element.getW()));
            }

        }

        // remove elements at the beginning and at the end of oldList
        // representing minimum spaces
        if (adjustedSpaceBefore > 0) {
            oldList.removeFirst();
        }
        if (adjustedSpaceAfter > 0) {
            oldList.removeLast();
        }

        // if space-before.conditionality is "discard", correct newList
        boolean correctFirstElement = false;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            correctFirstElement =
                ((org.apache.fop.fo.flow.Block) fobj).getCommonMarginBlock().spaceBefore.getSpace().isDiscard();
        }
        if (correctFirstElement) {
            // remove the wrong element
            KnuthBox wrongBox = (KnuthBox) newList.removeFirst();
            // if this paragraph is at the top of a page, the space before
            // must be ignored; compute the length change
            int decreasedLength = (neededUnits(totalLength.opt)
                                   - neededUnits(totalLength.opt - adjustedSpaceBefore))
                                  * bpUnit;
            // insert the correct elements
            newList.addFirst(new KnuthBox(wrongBox.getW() - decreasedLength,
                                          wrongBox.getPosition(), false));
            newList.addFirst(new KnuthGlue(decreasedLength, 0, 0, SPACE_BEFORE_ADJUSTMENT,
                                           wrongBox.getPosition(), false));
            //System.out.println("        rimosso box " + neededUnits(wrongBox.getW()));
            //System.out.println("        aggiunto glue " + neededUnits(decreasedLength) + " 0 0");
            //System.out.println("        aggiunto box " + neededUnits(wrongBox.getW() - decreasedLength));
        }

        // if space-after.conditionality is "discard", correct newList
        boolean correctLastElement = false;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            correctLastElement =
                ((org.apache.fop.fo.flow.Block) fobj).getCommonMarginBlock().spaceAfter.getSpace().isDiscard();
        }
        if (correctLastElement) {
            // remove the wrong element
            KnuthBox wrongBox = (KnuthBox) newList.removeLast();
            // if the old sequence is box(h) penalty(inf) glue(x,y,z) box(0)
            // (it cannot be parted and has some stretch or shrink)
            // the wrong box is the first one, not the last one
            LinkedList preserveList = new LinkedList();
            if (wrongBox.getW() == 0) {
                preserveList.add(wrongBox);
                preserveList.addFirst((KnuthGlue) newList.removeLast());
                preserveList.addFirst((KnuthPenalty) newList.removeLast());
                wrongBox = (KnuthBox) newList.removeLast();
            }

            // if this paragraph is at the bottom of a page, the space after
            // must be ignored; compute the length change
            int decreasedLength = (neededUnits(totalLength.opt)
                                   - neededUnits(totalLength.opt - adjustedSpaceAfter))
                                  * bpUnit;
            // insert the correct box
            newList.addLast(new KnuthBox(wrongBox.getW() - decreasedLength,
                                         wrongBox.getPosition(), false));
            // add preserved elements
            if (preserveList.size() > 0) {
                newList.addAll(preserveList);
            }
            // insert the correct glue
            newList.addLast(new KnuthGlue(decreasedLength, 0, 0, SPACE_AFTER_ADJUSTMENT,
                                          wrongBox.getPosition(), false));
            //System.out.println("        rimosso box " + neededUnits(wrongBox.getW()));
            //System.out.println("        aggiunto box " + neededUnits(wrongBox.getW() - decreasedLength));
            //System.out.println("        aggiunto glue " + neededUnits(decreasedLength) + " 0 0");
        }

        return newList;
    }

    protected static class StackingIter extends PositionIterator {
        StackingIter(Iterator parentIter) {
            super(parentIter);
        }
    
        protected LayoutManager getLM(Object nextObj) {
            return ((Position) nextObj).getLM();
        }
    
        protected Position getPos(Object nextObj) {
            return ((Position) nextObj);
        }
    }

    protected static class MappingPosition extends Position {
        private int iFirstIndex;
        private int iLastIndex;
    
        public MappingPosition(LayoutManager lm, int first, int last) {
            super(lm);
            iFirstIndex = first;
            iLastIndex = last;
        }
        
        public int getFirstIndex() {
            return iFirstIndex;
        }
        
        public int getLastIndex() {
            return iLastIndex;
        }
    }

    /**
     * "wrap" the Position inside each element moving the elements from 
     * SourceList to targetList
     * @param sourceList source list
     * @param targetList target list receiving the wrapped position elements
     */
    protected void wrapPositionElements(List sourceList, List targetList) {
        ListIterator listIter = sourceList.listIterator();
        while (listIter.hasNext()) {
            KnuthElement tempElement;
            tempElement = (KnuthElement) listIter.next();
            if (tempElement.getLayoutManager() != this) {
                tempElement.setPosition(new NonLeafPosition(this,
                        tempElement.getPosition()));
            }
            targetList.add(tempElement);
        }
    }

}

