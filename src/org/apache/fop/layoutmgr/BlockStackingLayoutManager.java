/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.area.Area;
import org.apache.fop.area.BlockParent;
import org.apache.fop.area.Block;
import org.apache.fop.area.MinOptMax;

import java.util.Iterator;

/**
 * Base LayoutManager class for all areas which stack their child
 * areas in the block-progression direction, such as Flow, Block, ListBlock.
 */
public abstract class BlockStackingLayoutManager extends AbstractLayoutManager {
    /** Reference to FO whose areas it's managing or to the traits
     * of the FO.
     */
    LayoutManager curChildLM = null;
    BlockParent parentArea = null;

    public BlockStackingLayoutManager(FObj fobj) {
        super(fobj);
    }

    public boolean splitArea(Area area, SplitContext splitContext) {
        // Divide area so that it will be within targetLength if possible
        // If not, it can be shorter, but not longer.
        /* Iterate over contents of the area. *

        // Need to figure out if we can do this generically
        // Logically a BlockStacking LM only handles Block-type areas
        if (!(area instanceof BlockParent)) {
            return false;
        }
        Iterator areaIter = ((BlockParent) area).getChildAreas().iterator();


        BreakCost minBreakCost = null;
        MinOptMax remainBPD = splitContext.targetBPD;
        splitContext.nextArea = area;

        while (areaIter.hasNext()) {
            Area childArea = (Area) areaIter.next();
            if (remainBPD.max < childArea.getAllocationBPD().min) {
                // Past the end point: try to break it
                // TODO: get a LayoutManager to do the split of the child
                // area, either Area => LM or Area => gen FO => LM
                LayoutManager childLM =
                  childArea.getGeneratingFObj(). getLayoutManager();
                splitContext.targetBPD = remainBPD;
                if (childLM.splitArea(childArea, splitContext) == false) {
                    // Can't split, so must split this area before childArea
                    // Can we pass the iter?
                    // If already saw several a potential break, use it
                    if (minBreakCost != null) {
                        /* Split 'area', placing all children after
                         * minBreakCost.getArea() into a new area,
                         * which we store in the splitContext.
                         *
                        // splitContext.nextArea = area.splitAfter(minBreakCost.getArea());
                    } else {
                        /* This area will be shorter than the desired minimum.
                         * Split before the current childArea (which will be
                         * the first area in the newly created Area.
                         *
                        //splitContext.nextArea = area.splitBefore(childArea);
                    }
                } else
                    return true; // childLM has done the work for us!
                // Set cost, dimension ???
                break;
            } else {
                remainBPD.subtract(childArea.getAllocationBPD());
                if (remainBPD.min < 0) {
                    // Potential breakpoint: remember break Position and
                    // break "cost" (constraint violation)
                    BreakCost breakCost =
                      evaluateBreakCost(area, childArea);
                    minBreakCost = breakCost.chooseLowest(minBreakCost);
                }
            }
            //Note: size of area when split can depend on conditional
            // space, border and padding of the split area!!!
        }
        // True if some part of area can be placed, false if none is placed
        return (splitContext.nextArea != area);
        */
        return false;
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
        // 	Area prevArea = getCurrentArea().getLast();
        // 	if (prevArea != null) {
        // 	    spaceSpec.addSpace(prevArea.getSpaceAfter());
        // 	}
        // 	spaceSpec.addSpace(nextArea.getSpaceBefore());
        return spaceSpec.resolve(false);
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
    protected boolean addChildToArea(Area childArea, BlockParent parentArea) {
        // This should be a block-level Area (Block in the generic sense)
        if (!(childArea instanceof Block)) {
            System.err.println("Child not a Block in BlockStackingLM!");
            return false;
        }

        // See if the whole thing fits, including space before
        // Calculate space between last child in curFlow and childArea
        MinOptMax targetDim = parentArea.getAvailBPD();
        MinOptMax spaceBefore = resolveSpaceSpecifier(childArea);
        targetDim.subtract(spaceBefore);
        if (targetDim.max >= childArea.getAllocationBPD().min) {
            //parentArea.addBlock(new InterBlockSpace(spaceBefore));
            parentArea.addBlock((Block) childArea);
            return false;
        } else {
            parentArea.addBlock((Block) childArea);
            flush(); // hand off current area to parent
            // Probably need something like max BPD so we don't get into
            // infinite loops with large unbreakable chunks
            //SplitContext splitContext = new SplitContext(targetDim);

            /*LayoutManager childLM =
              childArea.getGeneratingFObj(). getLayoutManager();
            if (childLM.splitArea(childArea, splitContext)) {
                //parentArea.addBlock(new InterBlockSpace(spaceBefore));
                parentArea.addBlock((Block) childArea);
            }*/
            //flush(); // hand off current area to parent
            //getParentArea(splitContext.nextArea);
            //getParentArea(childArea);
            // Check that reference IPD hasn't changed!!!
            // If it has, we must "reflow" the content
            //addChild(splitContext.nextArea);
            //addChild(childArea);
            return true;
        }
    }


    /**
     * Add the childArea to the current area.
     * Called by child LayoutManager when it has filled one of its areas.
     * The LM should already have an Area in which to put the child.
     * See if the area will fit in the current area.
     * If so, add it. Otherwise initiate breaking.
     * @param childArea the area to add: will be some block-stacked Area.
     */
    public boolean addChild(Area childArea) {
        return addChildToArea(childArea, getCurrentArea());
    }

    /**
     * Force current area to be added to parent area.
     */
    protected boolean flush() {
        if (getCurrentArea() != null)
            return parentLM.addChild(getCurrentArea());
        return false;
    }

}

