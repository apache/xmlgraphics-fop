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

package org.apache.fop.layoutmgr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockParent;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.BreakPropertySet;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.layoutmgr.inline.InlineLayoutManager;
import org.apache.fop.layoutmgr.inline.LineLayoutManager;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.BreakUtil;

/**
 * Base LayoutManager class for all areas which stack their child
 * areas in the block-progression direction, such as Flow, Block, ListBlock.
 */
public abstract class BlockStackingLayoutManager extends AbstractLayoutManager
                                                 implements BlockLevelLayoutManager {

    /**
     * logging instance
     */
    private static Log log = LogFactory.getLog(BlockStackingLayoutManager.class);

    /**
     * Reference to FO whose areas it's managing or to the traits
     * of the FO.
     */
    //protected LayoutManager curChildLM = null; AbstractLayoutManager also defines this!
    protected BlockParent parentArea = null;

    /** Value of the block-progression-unit (non-standard property) */
    protected int bpUnit = 0;
    /** space-before value adjusted for block-progression-unit handling */
    protected int adjustedSpaceBefore = 0;
    /** space-after value adjusted for block-progression-unit handling */
    protected int adjustedSpaceAfter = 0;
    /** Only used to store the original list when createUnitElements is called */
    protected LinkedList storedList = null;
    /** Indicates whether break before has been served or not */
    protected boolean breakBeforeServed = false;
    /** Indicates whether the first visible mark has been returned by this LM, yet */
    protected boolean firstVisibleMarkServed = false;
    /** Reference IPD available */
    protected int referenceIPD = 0;
    /** the effective start-indent value */
    protected int startIndent = 0;
    /** the effective end-indent value */
    protected int endIndent = 0;
    /**
     * Holds the (one-time use) fo:block space-before
     * and -after properties.  Large fo:blocks are split
     * into multiple Area. Blocks to accomodate the subsequent
     * regions (pages) they are placed on.  space-before
     * is applied at the beginning of the first
     * Block and space-after at the end of the last Block
     * used in rendering the fo:block.
     */
    protected MinOptMax foSpaceBefore = null;
    /** see foSpaceBefore */
    protected MinOptMax foSpaceAfter = null;

    private Position auxiliaryPosition;

    private int contentAreaIPD = 0;
    
    /**
     * @param node the fo this LM deals with
     */
    public BlockStackingLayoutManager(FObj node) {
        super(node);
        setGeneratesBlockArea(true);
    }

    /** 
     * @return current area being filled
     */
    protected BlockParent getCurrentArea() {
        return this.parentArea;
    }


    /**
     * Set the current area being filled.
     * @param parentArea the current area to be filled
     */
    protected void setCurrentArea(BlockParent parentArea) {
        this.parentArea = parentArea;
    }

    /**
     * Add a block spacer for space before and space after a block.
     * This adds an empty Block area that acts as a block space.
     *
     * @param adjust the adjustment value
     * @param minoptmax the min/opt/max value of the spacing
     */
    public void addBlockSpacing(double adjust, MinOptMax minoptmax) {
        int sp = TraitSetter.getEffectiveSpace(adjust, minoptmax);
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

    /** @return a cached auxiliary Position instance used for things like spaces. */
    protected Position getAuxiliaryPosition() {
        if (this.auxiliaryPosition == null) {
            this.auxiliaryPosition = new NonLeafPosition(this, null);
        }
        return this.auxiliaryPosition;
    }
    
    /**
     * @param len length in millipoints to span with bp units
     * @return the minimum integer n such that n * bpUnit >= len
     */
    protected int neededUnits(int len) {
        return (int) Math.ceil((float)len / bpUnit);
    }

    /**
     * Determines and sets the content area IPD based on available reference area IPD, start- and
     * end-indent properties.
     * end-indent is adjusted based on overconstrained geometry rules, if necessary.
     * 
     * @return the resulting content area IPD
     */
    protected int updateContentAreaIPDwithOverconstrainedAdjust() {
        int ipd = referenceIPD - (startIndent + endIndent);
        if (ipd < 0) {
            //5.3.4, XSL 1.0, Overconstrained Geometry
            log.debug("Adjusting end-indent based on overconstrained geometry rules for " + fobj);
            endIndent += ipd;
            ipd = 0;
            //TODO Should we skip layout for a block that has ipd=0?
        }
        setContentAreaIPD(ipd);
        return ipd;
    }
    
    /**
     * Sets the content area IPD by directly supplying the value. 
     * end-indent is adjusted based on overconstrained geometry rules, if necessary.
     * 
     * @return the resulting content area IPD
     */
    protected int updateContentAreaIPDwithOverconstrainedAdjust(int contentIPD) {
        int ipd = referenceIPD - (contentIPD + (startIndent + endIndent));
        if (ipd < 0) {
            //5.3.4, XSL 1.0, Overconstrained Geometry
            log.debug("Adjusting end-indent based on overconstrained geometry rules for " + fobj);
            endIndent += ipd;
        }
        setContentAreaIPD(contentIPD);
        return contentIPD;
    }
    
    /** {@inheritDoc} */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        //log.debug("BLM.getNextKnuthElements> keep-together = "
              // + layoutProps.keepTogether.getType());
        //log.debug(" keep-with-previous = " +
              // layoutProps.keepWithPrevious.getType());
        //log.debug(" keep-with-next = " +
              // layoutProps.keepWithNext.getType());
        BlockLevelLayoutManager curLM; // currently active LM
        BlockLevelLayoutManager prevLM = null; // previously active LM

        referenceIPD = context.getRefIPD();
        
        updateContentAreaIPDwithOverconstrainedAdjust();

        LinkedList returnedList = null;
        LinkedList contentList = new LinkedList();
        LinkedList returnList = new LinkedList();

        if (!breakBeforeServed) {
            breakBeforeServed = true;
            if (!context.suppressBreakBefore()) {
                if (addKnuthElementsForBreakBefore(returnList, context)) {
                    return returnList;
                }
            }
        }

        if (!firstVisibleMarkServed) {
            addKnuthElementsForSpaceBefore(returnList, alignment);
        }
        
        addKnuthElementsForBorderPaddingBefore(returnList, !firstVisibleMarkServed);
        firstVisibleMarkServed = true;

        //Spaces, border and padding to be repeated at each break
        addPendingMarks(context);
        
        //Used to indicate a special break-after case when all content has already been generated.
        BreakElement forcedBreakAfterLast = null;
        
        while ((curLM = (BlockLevelLayoutManager) getChildLM()) != null) {
            LayoutContext childLC = new LayoutContext(0);
            childLC.copyPendingMarksFrom(context);
            if (curLM instanceof LineLayoutManager) {
                // curLM is a LineLayoutManager
                // set stackLimit for lines (stack limit is now i-p-direction, not b-p-direction!)
                childLC.setStackLimitBP(context.getStackLimitBP());
                childLC.setStackLimitIP(new MinOptMax(getContentAreaIPD()));
                childLC.setRefIPD(getContentAreaIPD());
            } else {
                // curLM is a ?
                //childLC.setStackLimit(MinOptMax.subtract(context
                //        .getStackLimit(), stackSize));
                childLC.setStackLimitBP(context.getStackLimitBP());
                childLC.setRefIPD(referenceIPD);
            }
            if (curLM == this.childLMs.get(0)) {
                childLC.setFlags(LayoutContext.SUPPRESS_BREAK_BEFORE);
                //Handled already by the parent (break collapsing, see above)
            }

            // get elements from curLM
            returnedList = curLM.getNextKnuthElements(childLC, alignment);
            if (contentList.size() == 0 && childLC.isKeepWithPreviousPending()) {
                context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
                childLC.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING, false);
            }
            if (returnedList != null
                    && returnedList.size() == 1
                    && ((ListElement) returnedList.getFirst()).isForcedBreak()) {

                if (curLM.isFinished() && !hasNextChildLM()) {
                    // a descendant of this block has break-before
                    forcedBreakAfterLast = (BreakElement) returnedList.getFirst();
                    context.clearPendingMarks();
                    break;
                }

                if (contentList.size() == 0) {
                    // Empty fo:block, zero-length box makes sure the IDs and/or markers 
                    // are registered and borders/padding are painted.
                    returnList.add(new KnuthBox(0, notifyPos(new Position(this)), false));
                }
                // a descendant of this block has break-before
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
                            || context.isKeepWithNextPending()
                            || childLC.isKeepWithPreviousPending()) {
                        // Clear keep pending flag
                        context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING, false);
                        // add an infinite penalty to forbid a break between
                        // blocks
                        contentList.add(new BreakElement(
                                new Position(this), KnuthElement.INFINITE, context));
                    } else if (!((ListElement) contentList.getLast()).isGlue()) {
                        // add a null penalty to allow a break between blocks
                        contentList.add(new BreakElement(
                                new Position(this), 0, context));
                    } else {
                        // the last element in contentList is a glue;
                        // it is a feasible breakpoint, there is no need to add
                        // a penalty
                        log.warn("glue-type break possibility not handled properly, yet");
                        //TODO Does this happen? If yes, need to deal with border and padding
                        //at the break possibility
                    }
                }
                if (returnedList == null || returnedList.size() == 0) {
                    //Avoid NoSuchElementException below (happens with empty blocks)
                    continue;
                }
                contentList.addAll(returnedList);
                if (((ListElement) returnedList.getLast()).isForcedBreak()) {
                    // a descendant of this block has break-after
                    if (curLM.isFinished() && !hasNextChildLM()) {
                        forcedBreakAfterLast = (BreakElement)contentList.removeLast();
                        context.clearPendingMarks();
                        break;
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
            }
            // propagate and clear
            context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING, childLC.isKeepWithNextPending());
            childLC.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING, false);
            childLC.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING, false);
            prevLM = curLM;
        }

        /* Extension: conversione di tutta la sequenza fin'ora ottenuta */
        if (bpUnit > 0) {
            storedList = contentList;
            contentList = createUnitElements(contentList);
        }
        /* end of extension */

        returnedList = new LinkedList();
        if (contentList.size() > 0) {
            wrapPositionElements(contentList, returnList);
        } else if (forcedBreakAfterLast == null) {
            // Empty fo:block, zero-length box makes sure the IDs and/or markers 
            // are registered.
            returnList.add(new KnuthBox(0, notifyPos(new Position(this)), true));
        }

        addKnuthElementsForBorderPaddingAfter(returnList, true);
        addKnuthElementsForSpaceAfter(returnList, alignment);
        
        //All child content is processed. Only break-after can occur now, so...        
        context.clearPendingMarks();
        if (forcedBreakAfterLast == null) {
            addKnuthElementsForBreakAfter(returnList, context);
        }

        if (forcedBreakAfterLast != null) {
            forcedBreakAfterLast.clearPendingMarks();
            wrapPositionElement(forcedBreakAfterLast, returnList, false);
        }
        
        if (mustKeepWithNext()) {
            context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING);
        }
        if (mustKeepWithPrevious()) {
            context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
        }
        
        setFinished(true);

        return returnList;
    }

    /**
     * {@inheritDoc} 
     */
    public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
/*LF*/  //log.debug("  BLM.negotiateBPDAdjustment> " + adj);
/*LF*/  //log.debug("  lastElement e' " + (lastElement.isPenalty() 
        //      ? "penalty" : (lastElement.isGlue() ? "glue" : "box" )));
/*LF*/  //log.debug("  position e' " + lastElement.getPosition().getClass().getName());
/*LF*/  //log.debug("  " + (bpUnit > 0 ? "unit" : ""));
        Position innerPosition = ((NonLeafPosition) lastElement.getPosition()).getPosition();

        if (innerPosition == null && lastElement.isGlue()) {
            // this adjustment applies to space-before or space-after of this block
            if (((KnuthGlue) lastElement).getAdjustmentClass() == SPACE_BEFORE_ADJUSTMENT) {
                // this adjustment applies to space-before
                adjustedSpaceBefore += adj;
/*LF*/          //log.debug("  BLM.negotiateBPDAdjustment> spazio prima: " + adj);
            } else {
                // this adjustment applies to space-after
                adjustedSpaceAfter += adj;
/*LF*/          //log.debug("  BLM.negotiateBPDAdjustment> spazio dopo: " + adj);
            }
            return adj;
        } else if (innerPosition instanceof MappingPosition) {
            // this block has block-progression-unit > 0: the adjustment can concern
            // - the space-before or space-after of this block, 
            // - the line number of a descendant of this block
            MappingPosition mappingPos = (MappingPosition)innerPosition; 
            if (lastElement.isGlue()) {
                // lastElement is a glue
/*LF*/          //log.debug("  BLM.negotiateBPDAdjustment> bpunit con glue");
                ListIterator storedListIterator = storedList.listIterator(
                        mappingPos.getFirstIndex());
                int newAdjustment = 0;
                while (storedListIterator.nextIndex() <= mappingPos.getLastIndex()) {
                    KnuthElement storedElement = (KnuthElement)storedListIterator.next();
                    if (storedElement.isGlue()) {
                        newAdjustment += ((BlockLevelLayoutManager)storedElement
                                .getLayoutManager()).negotiateBPDAdjustment(
                                        adj - newAdjustment, storedElement);
/*LF*/                  //log.debug("  BLM.negotiateBPDAdjustment> (progressivo) righe: " 
                        //  + newAdjustment);
                    }
                }
                newAdjustment = (newAdjustment > 0 ? bpUnit * neededUnits(newAdjustment)
                                                   : -bpUnit * neededUnits(-newAdjustment));
                return newAdjustment;
            } else {
                // lastElement is a penalty: this means that the paragraph
                // has been split between consecutive pages:
                // this may involve a change in the number of lines
/*LF*/          //log.debug("  BLM.negotiateBPDAdjustment> bpunit con penalty");
                KnuthPenalty storedPenalty = (KnuthPenalty)
                                             storedList.get(mappingPos.getLastIndex());
                if (storedPenalty.getW() > 0) {
                    // the original penalty has width > 0
/*LF*/              //log.debug("  BLM.negotiateBPDAdjustment> chiamata passata");
                    return ((BlockLevelLayoutManager)storedPenalty.getLayoutManager())
                           .negotiateBPDAdjustment(storedPenalty.getW(), 
                                   (KnuthElement)storedPenalty);
                } else {
                    // the original penalty has width = 0
                    // the adjustment involves only the spaces before and after
/*LF*/              //log.debug("  BLM.negotiateBPDAdjustment> chiamata gestita");
                    return adj;
                }
            }
        } else if (innerPosition.getLM() != this) {
            // this adjustment concerns another LM
            NonLeafPosition savedPos = (NonLeafPosition) lastElement.getPosition();
            lastElement.setPosition(innerPosition);
            int returnValue = ((BlockLevelLayoutManager)lastElement.getLayoutManager())
                    .negotiateBPDAdjustment(adj, lastElement);
            lastElement.setPosition(savedPos);
/*LF*/      //log.debug("  BLM.negotiateBPDAdjustment> righe: " + returnValue);
            return returnValue;
        } else {
            // this should never happen
            log.error("BlockLayoutManager.negotiateBPDAdjustment(): unexpected Position");
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void discardSpace(KnuthGlue spaceGlue) {
        //log.debug("  BLM.discardSpace> " + spaceGlue.getPosition().getClass().getName());
        Position innerPosition = ((NonLeafPosition) spaceGlue.getPosition()).getPosition();

        if (innerPosition == null || innerPosition.getLM() == this) {
            // if this block has block-progression-unit > 0, innerPosition can be
            // a MappingPosition
            // spaceGlue represents space before or space after of this block
            if (spaceGlue.getAdjustmentClass() == SPACE_BEFORE_ADJUSTMENT) {
                // space-before must be discarded
                adjustedSpaceBefore = 0;
                foSpaceBefore = new MinOptMax(0);
            } else {
                // space-after must be discarded
                adjustedSpaceAfter = 0;
                foSpaceAfter = new MinOptMax(0);
                //TODO Why are both cases handled in the same way?
            }
        } else {
            // this element was not created by this BlockLM
            NonLeafPosition savedPos = (NonLeafPosition)spaceGlue.getPosition();
            spaceGlue.setPosition(innerPosition);
            ((BlockLevelLayoutManager) spaceGlue.getLayoutManager()).discardSpace(spaceGlue);
            spaceGlue.setPosition(savedPos);
        }
    }

    /**
     * {@inheritDoc} 
     */
    public LinkedList getChangedKnuthElements(List oldList, int alignment) {
/*LF*/  //log.debug("");
/*LF*/  //log.debug("  BLM.getChangedKnuthElements> inizio: oldList.size() = " 
        //  + oldList.size());
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
            //log.debug(" BLM> unwrapping: " 
            //  + (oldElement.isBox() ? "box    " : (oldElement.isGlue() ? "glue   " : "penalty")) 
            //  + " creato da " + oldElement.getLayoutManager().getClass().getName());
            //log.debug(" BLM> unwrapping:         " 
            //  + oldElement.getPosition().getClass().getName());
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

            //log-debug("  si usa storedList da " + iFirst + " a " + iLast 
            //  + " compresi su " + storedList.size() + " elementi totali");
            workList = storedList.subList(iFirst, iLast + 1);
        }
        ListIterator workListIterator = workList.listIterator();

        //log.debug("  BLM.getChangedKnuthElements> workList.size() = " 
        //  + workList.size() + " da 0 a " + (workList.size() - 1));

        while (workListIterator.hasNext()) {
            currElement = (KnuthElement) workListIterator.next();
            //log.debug("elemento n. " + workListIterator.previousIndex() 
            //  + " nella workList");
            if (prevElement != null
                && prevElement.getLayoutManager() != currElement.getLayoutManager()) {
                // prevElement is the last element generated by the same LM
                BlockLevelLayoutManager prevLM = (BlockLevelLayoutManager)
                                                 prevElement.getLayoutManager();
                BlockLevelLayoutManager currLM = (BlockLevelLayoutManager)
                                                 currElement.getLayoutManager();
                boolean bSomethingAdded = false;
                if (prevLM != this) {
                    //log.debug(" BLM.getChangedKnuthElements> chiamata da " 
                    //    + fromIndex + " a " + workListIterator.previousIndex() + " su " 
                    //    + prevLM.getClass().getName());
                    returnedList.addAll(prevLM.getChangedKnuthElements(workList.subList(
                                fromIndex, workListIterator.previousIndex()), alignment));
                    bSomethingAdded = true;
                } else {
                    // prevLM == this
                    // do nothing
                    //log.debug(" BLM.getChangedKnuthElements> elementi propri, "
                    //  + "ignorati, da " + fromIndex + " a " + workListIterator.previousIndex() 
                    //  + " su " + prevLM.getClass().getName());
                }
                fromIndex = workListIterator.previousIndex();

                /*
                 * TODO: why are KnuthPenalties added here,
                 *       while in getNextKE they were changed to BreakElements?
                 */
                // there is another block after this one
                if (bSomethingAdded
                    && (this.mustKeepTogether()
                        || prevLM.mustKeepWithNext()
                        || currLM.mustKeepWithPrevious())) {
                    // add an infinite penalty to forbid a break between blocks
                    returnedList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, 
                            new Position(this), false));
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
                //log.debug(" BLM.getChangedKnuthElements> chiamata da " + fromIndex 
                //  + " a " + oldList.size() + " su " + currLM.getClass().getName());
                returnedList.addAll(currLM.getChangedKnuthElements(
                        workList.subList(fromIndex, workList.size()), alignment));
            } else {
                // currLM == this
                // there are no more elements to add
                // remove the last penalty added to returnedList
                if (returnedList.size() > 0) {
                    returnedList.removeLast();
                }
                //log.debug(" BLM.getChangedKnuthElements> elementi propri, ignorati, da " 
                //  + fromIndex + " a " + workList.size());
            }
        }

        // append elements representing space-before
        boolean spaceBeforeIsConditional = true;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            spaceBeforeIsConditional = ((org.apache.fop.fo.flow.Block)fobj)
                    .getCommonMarginBlock().spaceBefore.getSpace().isDiscard();
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

        //log.debug("  BLM.getChangedKnuthElements> intermedio: returnedList.size() = " 
        //    + returnedList.size());

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
            spaceAfterIsConditional = ((org.apache.fop.fo.flow.Block)fobj)
                        .getCommonMarginBlock().spaceAfter.getSpace().isDiscard();
        }
        if (bpUnit > 0 || adjustedSpaceAfter != 0) {
            if (!spaceAfterIsConditional) {
                returnList.add(new KnuthPenalty(0, 
                        KnuthElement.INFINITE, false,
                        new NonLeafPosition(this, null), false));
            }
            if (bpUnit > 0) {
                returnList.add(new KnuthGlue(0, 0, 0,
                        SPACE_AFTER_ADJUSTMENT, 
                        new NonLeafPosition(this, null),
                        (!spaceAfterIsConditional) ? false : true));
            } else {
                returnList.add(new KnuthGlue(adjustedSpaceAfter, 0, 0,
                        SPACE_AFTER_ADJUSTMENT, 
                        new NonLeafPosition(this, null),
                        (!spaceAfterIsConditional) ? false : true));
            }
            if (!spaceAfterIsConditional) {
                returnList.add(new KnuthBox(0,
                        new NonLeafPosition(this, null), true));
            }
        }

        //log.debug("  BLM.getChangedKnuthElements> finished: returnList.size() = " 
        //  + returnList.size());
        return returnList;
    }

    /**
     * {@inheritDoc}
     */
    // default action: ask parentLM
    public boolean mustKeepTogether() {
        return ((getParent() instanceof BlockLevelLayoutManager
                    && ((BlockLevelLayoutManager) getParent()).mustKeepTogether())
                || (getParent() instanceof InlineLayoutManager
                    && ((InlineLayoutManager) getParent()).mustKeepTogether()));
    }

    /**
     * {@inheritDoc}
     */
    public boolean mustKeepWithPrevious() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean mustKeepWithNext() {
        return false;
    }

    /**
     * Adds the unresolved elements for border and padding to a layout context so break
     * possibilities can be properly constructed.
     * @param context the layout context
     */
    protected void addPendingMarks(LayoutContext context) {
        CommonBorderPaddingBackground borderAndPadding = getBorderPaddingBackground();
        if (borderAndPadding != null) {
            if (borderAndPadding.getBorderBeforeWidth(false) > 0) {
                context.addPendingBeforeMark(new BorderElement(
                        getAuxiliaryPosition(), 
                        borderAndPadding.getBorderInfo(
                                CommonBorderPaddingBackground.BEFORE).getWidth(),
                                RelSide.BEFORE,
                                false, false, this));
            }
            if (borderAndPadding.getPaddingBefore(false, this) > 0) {
                context.addPendingBeforeMark(new PaddingElement(
                        getAuxiliaryPosition(),
                        borderAndPadding.getPaddingLengthProperty(
                                CommonBorderPaddingBackground.BEFORE),
                                RelSide.BEFORE, 
                                false, false, this));
            }
            if (borderAndPadding.getBorderAfterWidth(false) > 0) {
                context.addPendingAfterMark(new BorderElement(
                        getAuxiliaryPosition(), 
                        borderAndPadding.getBorderInfo(
                                CommonBorderPaddingBackground.AFTER).getWidth(),
                                RelSide.AFTER, 
                                false, false, this));
            }
            if (borderAndPadding.getPaddingAfter(false, this) > 0) {
                context.addPendingAfterMark(new PaddingElement(
                        getAuxiliaryPosition(),
                        borderAndPadding.getPaddingLengthProperty(
                                CommonBorderPaddingBackground.AFTER),
                                RelSide.AFTER, 
                                false, false, this));
            }
        }
    }
    
    /** @return the border, padding and background info structure */
    private CommonBorderPaddingBackground getBorderPaddingBackground() {
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            return ((org.apache.fop.fo.flow.Block)fobj)
                .getCommonBorderPaddingBackground();
        } else if (fobj instanceof org.apache.fop.fo.flow.BlockContainer) {
            return ((org.apache.fop.fo.flow.BlockContainer)fobj)
                .getCommonBorderPaddingBackground();
        } else if (fobj instanceof org.apache.fop.fo.flow.ListBlock) {
            return ((org.apache.fop.fo.flow.ListBlock)fobj)
                .getCommonBorderPaddingBackground();
        } else if (fobj instanceof org.apache.fop.fo.flow.ListItem) {
            return ((org.apache.fop.fo.flow.ListItem)fobj)
                .getCommonBorderPaddingBackground();
        } else if (fobj instanceof org.apache.fop.fo.flow.table.Table) {
            return ((org.apache.fop.fo.flow.table.Table)fobj)
                .getCommonBorderPaddingBackground();
        } else {
            return null;
        }
    }
    
    /** @return the space-before property */
    private SpaceProperty getSpaceBeforeProperty() {
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            return ((org.apache.fop.fo.flow.Block)fobj)
                .getCommonMarginBlock().spaceBefore;
        } else if (fobj instanceof org.apache.fop.fo.flow.BlockContainer) {
            return ((org.apache.fop.fo.flow.BlockContainer)fobj)
                .getCommonMarginBlock().spaceBefore;
        } else if (fobj instanceof org.apache.fop.fo.flow.ListBlock) {
            return ((org.apache.fop.fo.flow.ListBlock)fobj)
                .getCommonMarginBlock().spaceBefore;
        } else if (fobj instanceof org.apache.fop.fo.flow.ListItem) {
            return ((org.apache.fop.fo.flow.ListItem)fobj)
                .getCommonMarginBlock().spaceBefore;
        } else if (fobj instanceof org.apache.fop.fo.flow.table.Table) {
            return ((org.apache.fop.fo.flow.table.Table)fobj)
                .getCommonMarginBlock().spaceBefore;
        } else {
            return null;
        }
    }
    
    /** @return the space-after property */
    private SpaceProperty getSpaceAfterProperty() {
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            return ((org.apache.fop.fo.flow.Block)fobj)
                .getCommonMarginBlock().spaceAfter;
        } else if (fobj instanceof org.apache.fop.fo.flow.BlockContainer) {
            return ((org.apache.fop.fo.flow.BlockContainer)fobj)
                .getCommonMarginBlock().spaceAfter;
        } else if (fobj instanceof org.apache.fop.fo.flow.ListBlock) {
            return ((org.apache.fop.fo.flow.ListBlock)fobj)
                .getCommonMarginBlock().spaceAfter;
        } else if (fobj instanceof org.apache.fop.fo.flow.ListItem) {
            return ((org.apache.fop.fo.flow.ListItem)fobj)
                .getCommonMarginBlock().spaceAfter;
        } else if (fobj instanceof org.apache.fop.fo.flow.table.Table) {
            return ((org.apache.fop.fo.flow.table.Table)fobj)
                .getCommonMarginBlock().spaceAfter;
        } else {
            return null;
        }
    }
    
    /**
     * Creates Knuth elements for before border padding and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param isFirst true if this is the first time a layout manager instance needs to generate 
     *                border and padding
     */
    protected void addKnuthElementsForBorderPaddingBefore(LinkedList returnList, boolean isFirst) {
        //Border and Padding (before)
        CommonBorderPaddingBackground borderAndPadding = getBorderPaddingBackground();
        if (borderAndPadding != null) {
            if (borderAndPadding.getBorderBeforeWidth(false) > 0) {
                returnList.add(new BorderElement(
                        getAuxiliaryPosition(), 
                        borderAndPadding.getBorderInfo(CommonBorderPaddingBackground.BEFORE)
                                .getWidth(),
                        RelSide.BEFORE, isFirst, false, this));
            }
            if (borderAndPadding.getPaddingBefore(false, this) > 0) {
                returnList.add(new PaddingElement(
                        getAuxiliaryPosition(),
                        borderAndPadding.getPaddingLengthProperty(
                                CommonBorderPaddingBackground.BEFORE), 
                        RelSide.BEFORE, isFirst, false, this));
            }
        }
    }

    /**
     * Creates Knuth elements for after border padding and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param isLast true if this is the last time a layout manager instance needs to generate 
     *               border and padding
     */
    protected void addKnuthElementsForBorderPaddingAfter(LinkedList returnList, boolean isLast) {
        //Border and Padding (after)
        CommonBorderPaddingBackground borderAndPadding = getBorderPaddingBackground();
        if (borderAndPadding != null) {
            if (borderAndPadding.getPaddingAfter(false, this) > 0) {
                returnList.add(new PaddingElement(
                        getAuxiliaryPosition(),
                        borderAndPadding.getPaddingLengthProperty(
                                CommonBorderPaddingBackground.AFTER),
                        RelSide.AFTER, false, isLast, this));
            }
            if (borderAndPadding.getBorderAfterWidth(false) > 0) {
                returnList.add(new BorderElement(
                        getAuxiliaryPosition(), 
                        borderAndPadding.getBorderInfo(CommonBorderPaddingBackground.AFTER)
                                .getWidth(),
                        RelSide.AFTER, false, isLast, this));
            }
        }
    }

    /**
     * Creates Knuth elements for break-before and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param context the layout context
     * @return true if an element has been added due to a break-before.
     */
    protected boolean addKnuthElementsForBreakBefore(LinkedList returnList, 
            LayoutContext context) {
        int breakBefore = getBreakBefore();
        if (breakBefore == EN_PAGE
                || breakBefore == EN_COLUMN 
                || breakBefore == EN_EVEN_PAGE 
                || breakBefore == EN_ODD_PAGE) {
            // return a penalty element, representing a forced page break
            returnList.add(new BreakElement(getAuxiliaryPosition(), 
                    0, -KnuthElement.INFINITE, breakBefore, context));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the break-before value of the current formatting object.
     * @return the break-before value (Constants.EN_*)
     */
    private int getBreakBefore() {
        int breakBefore = EN_AUTO;
        if (fobj instanceof BreakPropertySet) {
            breakBefore = ((BreakPropertySet)fobj).getBreakBefore();
        }
        if (true /* uncomment to only partially merge: && breakBefore != EN_AUTO*/) {
            LayoutManager lm = getChildLM();
            //It is assumed this is only called when the first LM is active.
            if (lm instanceof BlockStackingLayoutManager) {
                BlockStackingLayoutManager bslm = (BlockStackingLayoutManager)lm;
                breakBefore = BreakUtil.compareBreakClasses(
                        breakBefore, bslm.getBreakBefore());
            }
        }
        return breakBefore;
    }
    
    /**
     * Creates Knuth elements for break-after and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param context the layout context
     * @return true if an element has been added due to a break-after.
     */
    protected boolean addKnuthElementsForBreakAfter(LinkedList returnList, 
            LayoutContext context) {
        int breakAfter = -1;
        if (fobj instanceof BreakPropertySet) {
            breakAfter = ((BreakPropertySet)fobj).getBreakAfter();
        }
        if (breakAfter == EN_PAGE
                || breakAfter == EN_COLUMN
                || breakAfter == EN_EVEN_PAGE
                || breakAfter == EN_ODD_PAGE) {
            // add a penalty element, representing a forced page break
            returnList.add(new BreakElement(getAuxiliaryPosition(), 
                    0, -KnuthElement.INFINITE, breakAfter, context));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates Knuth elements for space-before and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param alignment vertical alignment
     */
    protected void addKnuthElementsForSpaceBefore(LinkedList returnList/*, 
            Position returnPosition*/, int alignment) {
        SpaceProperty spaceBefore = getSpaceBeforeProperty();
        // append elements representing space-before
        if (spaceBefore != null
                   && !(spaceBefore.getMinimum(this).getLength().getValue(this) == 0 
                        && spaceBefore.getMaximum(this).getLength().getValue(this) == 0)) {
            returnList.add(new SpaceElement(getAuxiliaryPosition(), spaceBefore,
                    RelSide.BEFORE, 
                    true, false, this));
        }
        /*
        if (bpUnit > 0
                || spaceBefore != null
                   && !(spaceBefore.getMinimum(this).getLength().getValue(this) == 0 
                        && spaceBefore.getMaximum(this).getLength().getValue(this) == 0)) {
            if (spaceBefore != null && !spaceBefore.getSpace().isDiscard()) {
                // add elements to prevent the glue to be discarded
                returnList.add(new KnuthBox(0, getAuxiliaryPosition(), false));
                returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE,
                        false, getAuxiliaryPosition(), false));
            }
            if (bpUnit > 0) {
                returnList.add(new KnuthGlue(0, 0, 0,
                        BlockLevelLayoutManager.SPACE_BEFORE_ADJUSTMENT, 
                        getAuxiliaryPosition(), true));
            } else { //if (alignment == EN_JUSTIFY) {
                returnList.add(new KnuthGlue(
                        spaceBefore.getOptimum(this).getLength().getValue(this),
                        spaceBefore.getMaximum(this).getLength().getValue(this)
                                - spaceBefore.getOptimum(this).getLength().getValue(this),
                        spaceBefore.getOptimum(this).getLength().getValue(this)
                                - spaceBefore.getMinimum(this).getLength().getValue(this),
                        BlockLevelLayoutManager.SPACE_BEFORE_ADJUSTMENT, 
                        getAuxiliaryPosition(), true));
//            } else {
//                returnList.add(new KnuthGlue(
//                        spaceBefore.getOptimum().getLength().getValue(this), 
//                        0, 0, BlockLevelLayoutManager.SPACE_BEFORE_ADJUSTMENT,
//                        returnPosition, true));
            }
        }*/
    }

    /**
     * Creates Knuth elements for space-after and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param alignment vertical alignment
     */
    protected void addKnuthElementsForSpaceAfter(LinkedList returnList/*, Position returnPosition*/, 
                int alignment) {
        SpaceProperty spaceAfter = getSpaceAfterProperty();
        // append elements representing space-after
        if (spaceAfter != null
                && !(spaceAfter.getMinimum(this).getLength().getValue(this) == 0 
                     && spaceAfter.getMaximum(this).getLength().getValue(this) == 0)) {
            returnList.add(new SpaceElement(getAuxiliaryPosition(), spaceAfter,
                    RelSide.AFTER, 
                    false, true, this));
        }
        /*
        if (bpUnit > 0
                || spaceAfter != null
                   && !(spaceAfter.getMinimum(this).getLength().getValue(this) == 0 
                        && spaceAfter.getMaximum(this).getLength().getValue(this) == 0)) {
            if (spaceAfter != null && !spaceAfter.getSpace().isDiscard()) {
                returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE,
                        false, getAuxiliaryPosition(), false));
            }
            if (bpUnit > 0) {
                returnList.add(new KnuthGlue(0, 0, 0, 
                        BlockLevelLayoutManager.SPACE_AFTER_ADJUSTMENT,
                        getAuxiliaryPosition(), true));
            } else { //if (alignment == EN_JUSTIFY) {
                returnList.add(new KnuthGlue(
                        spaceAfter.getOptimum(this).getLength().getValue(this),
                        spaceAfter.getMaximum(this).getLength().getValue(this)
                                - spaceAfter.getOptimum(this).getLength().getValue(this),
                        spaceAfter.getOptimum(this).getLength().getValue(this)
                                - spaceAfter.getMinimum(this).getLength().getValue(this),
                        BlockLevelLayoutManager.SPACE_AFTER_ADJUSTMENT, getAuxiliaryPosition(),
                        (!spaceAfter.getSpace().isDiscard()) ? false : true));
//            } else {
//                returnList.add(new KnuthGlue(
//                        spaceAfter.getOptimum().getLength().getValue(this), 0, 0,
//                        BlockLevelLayoutManager.SPACE_AFTER_ADJUSTMENT, returnPosition,
//                        (!spaceAfter.getSpace().isDiscard()) ? false : true));
            }
            if (spaceAfter != null && !spaceAfter.getSpace().isDiscard()) {
                returnList.add(new KnuthBox(0, getAuxiliaryPosition(), true));
            }
        }*/
    }

    protected LinkedList createUnitElements(LinkedList oldList) {
        //log.debug("Start conversion: " + oldList.size() 
        //  + " elements, space-before.min=" + layoutProps.spaceBefore.getSpace().min
        //  + " space-after.min=" + layoutProps.spaceAfter.getSpace().min);
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

        //log.debug(" Prima scansione");
        // scan the list once to compute total min, opt and max length
        ListIterator oldListIterator = oldList.listIterator();
        while (oldListIterator.hasNext()) {
            KnuthElement element = (KnuthElement) oldListIterator.next();
            if (element.isBox()) {
                totalLength.add(new MinOptMax(element.getW()));
                //log.debug("box " + element.getW());               
            } else if (element.isGlue()) {
                totalLength.min -= ((KnuthGlue) element).getZ();
                totalLength.max += ((KnuthGlue) element).getY();
                //leafValue = ((LeafPosition) element.getPosition()).getLeafPos();
                //log.debug("glue " + element.getW() + " + " 
                //    + ((KnuthGlue) element).getY() + " - " + ((KnuthGlue) element).getZ());
            } else {
                //log.debug((((KnuthPenalty)element).getP() == KnuthElement.INFINITE 
                //    ? "PENALTY " : "penalty ") + element.getW());
            }
        }
        // compute the total amount of "units"
        totalUnits = new MinOptMax(neededUnits(totalLength.min),
                                   neededUnits(totalLength.opt),
                                   neededUnits(totalLength.max));
        //log.debug(" totalLength= " + totalLength);
        //log.debug(" unita'= " + totalUnits);

        //log.debug(" Seconda scansione");
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
            lastIndex++;
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

                //log.debug("(" 
                //    + unsuppressibleUnits.min + "-" + unsuppressibleUnits.opt + "-" 
                //         + unsuppressibleUnits.max + ") "
                //    + " -> " + unitsBeforeBreak.min + "-" + unitsBeforeBreak.opt + "-" 
                //         + unitsBeforeBreak.max
                //    + " + " + unitsAfterBreak.min + "-" + unitsAfterBreak.opt + "-" 
                //         + unitsAfterBreak.max
                //    + (uLengthChange != 0 ? " [length " + uLengthChange + "] " : "")
                //    + (uStretchChange != 0 ? " [stretch " + uStretchChange + "] " : "")
                //    + (uShrinkChange != 0 ? " [shrink " + uShrinkChange + "]" : ""));

                // create the MappingPosition which will be stored in the new elements
                // correct firstIndex and lastIndex
                int firstIndexCorrection = 0;
                int lastIndexCorrection = 0;
                if (bAddedBoxBefore) {
                    if (firstIndex != 0) {
                        firstIndexCorrection++;
                    }
                    lastIndexCorrection++;
                }
                if (bAddedBoxAfter && lastIndex == (oldList.size() - 1)) {
                    lastIndexCorrection++;
                }
                MappingPosition mappingPos = new MappingPosition(this,
                                                                 firstIndex - firstIndexCorrection,
                                                                 lastIndex - lastIndexCorrection);

                // new box
                newList.add(new KnuthBox((uNewNormal - uLengthChange) * bpUnit,
                                         mappingPos,
                                         false));
                unsuppressibleUnits.add(new MinOptMax(uNewNormal - uLengthChange));
                //log.debug("        box " + (uNewNormal - uLengthChange));

                // new infinite penalty, glue and box, if necessary
                if (uNewStretch - uStretchChange > 0
                    || uNewShrink - uShrinkChange > 0) {
                    int iStretchUnits = (uNewStretch - uStretchChange > 0 
                            ? (uNewStretch - uStretchChange) : 0);
                    int iShrinkUnits = (uNewShrink - uShrinkChange > 0 
                            ? (uNewShrink - uShrinkChange) : 0);
                    newList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                                 mappingPos,
                                                 false));
                    newList.add(new KnuthGlue(0,
                                              iStretchUnits * bpUnit,
                                              iShrinkUnits * bpUnit,
                                              LINE_NUMBER_ADJUSTMENT,
                                              mappingPos,
                                              false));
                    //log.debug("        PENALTY");
                    //log.debug("        glue 0 " + iStretchUnits + " " + iShrinkUnits);
                    unsuppressibleUnits.max += iStretchUnits;
                    unsuppressibleUnits.min -= iShrinkUnits;
                    if (!oldListIterator.hasNext()) {
                        newList.add(new KnuthBox(0,
                                                 mappingPos,
                                                 false));
                        //log.debug("        box 0");
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
                                              -uStretchChange * bpUnit,
                                              -uShrinkChange * bpUnit,
                                              LINE_NUMBER_ADJUSTMENT,
                                              mappingPos,
                                              false));
                    //log.debug("        PENALTY");
                    //log.debug("        glue 0 " + uStretchChange + " " + uShrinkChange);
                    //log.debug("        penalty " + uLengthChange + " * unit");
                    //log.debug("        glue 0 " + (- uStretchChange) + " " 
                    //      + (- uShrinkChange));
                } else if (oldListIterator.hasNext()) {
                    // new penalty
                    newList.add(new KnuthPenalty(uLengthChange * bpUnit,
                                                 0, false,
                                                 mappingPos,
                                                 false));
                    //log.debug("        penalty " + uLengthChange + " * unit");
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
            correctFirstElement = ((org.apache.fop.fo.flow.Block)fobj)
                .getCommonMarginBlock().spaceBefore.getSpace().isDiscard();
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
            //log.debug("        rimosso box " + neededUnits(wrongBox.getW()));
            //log.debug("        aggiunto glue " + neededUnits(decreasedLength) + " 0 0");
            //log.debug("        aggiunto box " + neededUnits(
            //       wrongBox.getW() - decreasedLength));
        }

        // if space-after.conditionality is "discard", correct newList
        boolean correctLastElement = false;
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            correctLastElement = ((org.apache.fop.fo.flow.Block)fobj)
                    .getCommonMarginBlock().spaceAfter.getSpace().isDiscard();
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
            //log.debug("        rimosso box " + neededUnits(wrongBox.getW()));
            //log.debug("        aggiunto box " + neededUnits(
            //      wrongBox.getW() - decreasedLength));
            //log.debug("        aggiunto glue " + neededUnits(decreasedLength) + " 0 0");
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
        wrapPositionElements(sourceList, targetList, false);
    }
    
    /**
     * "wrap" the Position inside each element moving the elements from 
     * SourceList to targetList
     * @param sourceList source list
     * @param targetList target list receiving the wrapped position elements
     * @param force if true, every Position is wrapped regardless of its LM of origin
     */
    protected void wrapPositionElements(List sourceList, List targetList, boolean force) {
          
        ListIterator listIter = sourceList.listIterator();
        while (listIter.hasNext()) {
            ListElement tempElement;
            tempElement = (ListElement) listIter.next();
            wrapPositionElement(tempElement, targetList, force);
        }
    }

    /**
     * "wrap" the Position inside the given element and add it to the target list.
     * @param el the list element
     * @param targetList target list receiving the wrapped position elements
     * @param force if true, every Position is wrapped regardless of its LM of origin
     */
    protected void wrapPositionElement(ListElement el, List targetList, boolean force) {
        if (force || el.getLayoutManager() != this) {
            el.setPosition(notifyPos(new NonLeafPosition(this,
                    el.getPosition())));
        }
        targetList.add(el);
    }

    
    /** @return the sum of start-indent and end-indent */
    protected int getIPIndents() {
        return startIndent + endIndent;
    }
    
    /**
     * Returns the IPD of the content area
     * @return the IPD of the content area
     */
    public int getContentAreaIPD() {
        return contentAreaIPD;
    }
   
    /**
     * Sets the IPD of the content area
     * @param contentAreaIPD the IPD of the content area
     */
    protected void setContentAreaIPD(int contentAreaIPD) {
        this.contentAreaIPD = contentAreaIPD;
    }
    
    /**
     * Returns the BPD of the content area
     * @return the BPD of the content area
     */
    public int getContentAreaBPD() {
        return -1;
    }
    
}

