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
public abstract class BlockStackingLayoutManager extends AbstractLayoutManager {
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
    /*LF*/

    public BlockStackingLayoutManager(FObj node) {
        super(node);
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

    /**
     * Creates Knuth elements for before border padding and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param returnPosition applicable return position
     * @param borderAndPadding border and padding to work with
     */
    protected void addKnuthElementForBorderPaddingBefore(LinkedList returnList, 
            Position returnPosition, CommonBorderPaddingBackground borderAndPadding) {
        //Border and Padding (before)
        //TODO Handle conditionality
        int bpBefore = borderAndPadding.getBorderBeforeWidth(false)
                    + borderAndPadding.getPaddingBefore(false);
        if (bpBefore > 0) {
            returnList.add(new KnuthBox(bpBefore, returnPosition, true));
        }
    }

    /**
     * Creates Knuth elements for after border padding and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param returnPosition applicable return position
     * @param borderAndPadding border and padding to work with
     */
    protected void addKnuthElementsForBorderPaddingAfter(LinkedList returnList, 
            Position returnPosition, CommonBorderPaddingBackground borderAndPadding) {
        //Border and Padding (after)
        //TODO Handle conditionality
        int bpAfter = borderAndPadding.getBorderAfterWidth(false)
                    + borderAndPadding.getPaddingAfter(false);
        if (bpAfter > 0) {
            returnList.add(new KnuthBox(bpAfter, returnPosition, true));
        }
    }

    /**
     * Creates Knuth elements for break-before and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param returnPosition applicable return position
     * @param breakBefore break-before value
     * @return true if an element has been added due to a break-before.
     */
    protected boolean addKnuthElementsForBreakBefore(LinkedList returnList, 
            Position returnPosition, int breakBefore) {
        if (breakBefore == EN_PAGE
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
     * @param breakAfter break-after value
     * @return true if an element has been added due to a break-after.
     */
    protected boolean addKnuthElementsForBreakAfter(LinkedList returnList, 
            Position returnPosition, int breakAfter) {
        if (breakAfter == EN_PAGE
                || breakAfter == EN_EVEN_PAGE
                || breakAfter == EN_ODD_PAGE) {
            // add a penalty element, representing a forced page break
            returnList.add(new KnuthPenalty(0, -KnuthElement.INFINITE, false,
                    breakAfter, returnPosition, false));
            /* LF *///System.out.println("BLM - break after!!");
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
     * @param spaceBefore the space-before property
     */
    protected void addKnuthElementsForSpaceBefore(LinkedList returnList, 
            Position returnPosition, int alignment, SpaceProperty spaceBefore) {
        // append elements representing space-before
        if (bpUnit > 0
                || !(spaceBefore.getMinimum().getLength().getValue() == 0 
                        && spaceBefore.getMaximum().getLength().getValue() == 0)) {
            if (!spaceBefore.getSpace().isDiscard()) {
                // add elements to prevent the glue to be discarded
                returnList.add(new KnuthBox(0, returnPosition, false));
                returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE,
                        false, returnPosition, false));
            }
            if (bpUnit > 0) {
                returnList.add(new KnuthGlue(0, 0, 0,
                        BlockLevelLayoutManager.SPACE_BEFORE_ADJUSTMENT, 
                        returnPosition, true));
            } else if (alignment == EN_JUSTIFY) {
                returnList.add(new KnuthGlue(
                        spaceBefore.getOptimum().getLength().getValue(),
                        spaceBefore.getMaximum().getLength().getValue()
                                - spaceBefore.getOptimum().getLength().getValue(),
                        spaceBefore.getOptimum().getLength().getValue()
                                - spaceBefore.getMinimum().getLength().getValue(),
                        BlockLevelLayoutManager.SPACE_BEFORE_ADJUSTMENT, 
                        returnPosition, true));
            } else {
                returnList.add(new KnuthGlue(
                        spaceBefore.getOptimum().getLength().getValue(), 
                        0, 0, BlockLevelLayoutManager.SPACE_BEFORE_ADJUSTMENT,
                        returnPosition, true));
            }
        }
    }

    /**
     * Creates Knuth elements for space-after and adds them to the return list.
     * @param returnList return list to add the additional elements to
     * @param returnPosition applicable return position
     * @param alignment vertical alignment
     * @param spaceAfter the space-after property
     */
    protected void addKnuthElementsForSpaceAfter(LinkedList returnList, Position returnPosition, 
                int alignment, SpaceProperty spaceAfter) {
        // append elements representing space-after
        if (bpUnit > 0
                || !(spaceAfter.getMinimum().getLength().getValue() == 0 
                        && spaceAfter.getMaximum().getLength().getValue() == 0)) {
            if (!spaceAfter.getSpace().isDiscard()) {
                returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE,
                        false, returnPosition, false));
            }
            if (bpUnit > 0) {
                returnList.add(new KnuthGlue(0, 0, 0, 
                        BlockLevelLayoutManager.SPACE_AFTER_ADJUSTMENT,
                        returnPosition, true));
            } else if (alignment == EN_JUSTIFY) {
                returnList.add(new KnuthGlue(
                        spaceAfter.getOptimum().getLength().getValue(),
                        spaceAfter.getMaximum().getLength().getValue()
                                - spaceAfter.getOptimum().getLength().getValue(),
                        spaceAfter.getOptimum().getLength().getValue()
                                - spaceAfter.getMinimum().getLength().getValue(),
                        BlockLevelLayoutManager.SPACE_AFTER_ADJUSTMENT, returnPosition,
                        (!spaceAfter.getSpace().isDiscard()) ? false : true));
            } else {
                returnList.add(new KnuthGlue(
                        spaceAfter.getOptimum().getLength().getValue(), 0, 0,
                        BlockLevelLayoutManager.SPACE_AFTER_ADJUSTMENT, returnPosition,
                        (!spaceAfter.getSpace().isDiscard()) ? false : true));
            }
            if (!spaceAfter.getSpace().isDiscard()) {
                returnList.add(new KnuthBox(0, returnPosition, true));
            }
        }
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
            //if (tempElement.getLayoutManager() != this) {
            tempElement.setPosition(new NonLeafPosition(this,
                    tempElement.getPosition()));
            //}
            targetList.add(tempElement);
        }
    }

}

