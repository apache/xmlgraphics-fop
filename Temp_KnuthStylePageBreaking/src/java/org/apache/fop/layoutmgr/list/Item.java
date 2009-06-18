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
 
package org.apache.fop.layoutmgr.list;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.BreakPoss;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.traits.MinOptMax;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * LayoutManager for a table-cell FO.
 * A cell contains blocks. These blocks fill the cell.
 */
public class Item extends BlockStackingLayoutManager {
    private FObj fobj;

    private Block curBlockArea;

    private List childBreaks = new ArrayList();

    private int xoffset;
    private int itemIPD;

    private static class StackingIter extends PositionIterator {
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

    /**
     * Create a new Cell layout manager.
     */
    public Item(ListItemLabel node) {
        super(node);
        fobj = node;
    }

    /**
     * Create a new Cell layout manager.
     */
    public Item(ListItemBody node) {
        super(node);
        fobj = node;
    }

    /**
     * Get the next break possibility for this cell.
     * A cell contains blocks so there are breaks around the blocks
     * and inside the blocks.
     *
     * @param context the layout context
     * @return the next break possibility
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        LayoutManager curLM; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        // if starting add space before
        // stackSize.add(spaceBefore);
        BreakPoss lastPos = null;

        itemIPD = context.getRefIPD();

        while ((curLM = getChildLM()) != null) {
            if (curLM.generatesInlineAreas()) {
                // error
                curLM.setFinished(true);
                continue;
            }
            // Set up a LayoutContext
            int ipd = context.getRefIPD();
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));
            childLC.setRefIPD(ipd);

            boolean over = false;
            while (!curLM.isFinished()) {
                if ((bp = curLM.getNextBreakPoss(childLC)) != null) {
                    if (stackSize.opt + bp.getStackingSize().opt > context.getStackLimit().max) {
                        // reset to last break
                        if (lastPos != null) {
                            LayoutManager lm = lastPos.getLayoutManager();
                            lm.resetPosition(lastPos.getPosition());
                            if (lm != curLM) {
                                curLM.resetPosition(null);
                            }
                        } else {
                            curLM.resetPosition(null);
                        }
                        over = true;
                        break;
                    }
                    stackSize.add(bp.getStackingSize());
                    lastPos = bp;
                    childBreaks.add(bp);

                    if (bp.nextBreakOverflows()) {
                        over = true;
                        break;
                    }

                    childLC.setStackLimit(MinOptMax.subtract(
                                             context.getStackLimit(), stackSize));
                }
            }
            BreakPoss breakPoss = new BreakPoss(
                                    new LeafPosition(this, childBreaks.size() - 1));
            if (over) {
                breakPoss.setFlag(BreakPoss.NEXT_OVERFLOWS, true);
            }
            breakPoss.setStackingSize(stackSize);
            return breakPoss;
        }
        setFinished(true);
        return null;
    }

    /**
     * Set the x offset of this list item.
     * This offset is used to set the absolute position
     * of the list item within the parent block area.
     *
     * @param off the x offset
     */
    public void setXOffset(int off) {
        xoffset = off;
    }

    public LinkedList getChangedKnuthElements(List oldList, int alignment) {
        //log.debug("  Item.getChanged>");
        return super.getChangedKnuthElements(oldList, alignment);
    }

    /**
     * Add the areas for the break points.
     * The list item contains block stacking layout managers
     * that add block areas.
     *
     * @param parentIter the iterator of the break positions
     * @param layoutContext the layout context for adding the areas
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);
        
        int nameId = fobj.getNameId();
        if (nameId == FO_LIST_ITEM_LABEL) {
            getPSLM().addIDToPage(((ListItemLabel) fobj).getId());
        } else if (nameId == FO_LIST_ITEM_BODY) {
            getPSLM().addIDToPage(((ListItemBody) fobj).getId());
        }

        LayoutManager childLM = null;
        LayoutContext lc = new LayoutContext(0);
        LayoutManager firstLM = null;
        LayoutManager lastLM = null;

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list; 
        LinkedList positionList = new LinkedList();
        Position pos;
        while (parentIter.hasNext()) {
            pos = (Position)parentIter.next();
            if (pos instanceof NonLeafPosition) {
                // pos was created by a child of this ListBlockLM
                positionList.add(((NonLeafPosition) pos).getPosition());
                lastLM = ((NonLeafPosition) pos).getPosition().getLM();
                if (firstLM == null) {
                    firstLM = lastLM;
                }
            } else {
                // pos was created by this ListBlockLM, so it must be ignored
            }
        }

        StackingIter childPosIter = new StackingIter(positionList.listIterator());
        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // Add the block areas to Area
            lc.setFlags(LayoutContext.FIRST_AREA, childLM == firstLM);
            lc.setFlags(LayoutContext.LAST_AREA, childLM == lastLM);
            lc.setStackLimit(layoutContext.getStackLimit());
            childLM.addAreas(childPosIter, lc);
        }

        /*
        if (borderProps != null) {
            TraitSetter.addBorders(curBlockArea, borderProps);
        }
        if (backgroundProps != null) {
            TraitSetter.addBackground(curBlockArea, backgroundProps);
        }
        */

        flush();

        childBreaks.clear();
        curBlockArea = null;
    }

    /**
     * Return an Area which can contain the passed childArea. The childArea
     * may not yet have any content, but it has essential traits set.
     * In general, if the LayoutManager already has an Area it simply returns
     * it. Otherwise, it makes a new Area of the appropriate class.
     * It gets a parent area for its area by calling its parent LM.
     * Finally, based on the dimensions of the parent area, it initializes
     * its own area. This includes setting the content IPD and the maximum
     * BPD.
     *
     * @param childArea the child area to get the parent for
     * @return the parent area
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();
            curBlockArea.setPositioning(Block.ABSOLUTE);
            // set position
            curBlockArea.setXOffset(xoffset);
            curBlockArea.setIPD(itemIPD);
            //curBlockArea.setHeight();

            // Set up dimensions
            Area parentArea = parentLM.getParentArea(curBlockArea);
            int referenceIPD = parentArea.getIPD();
            curBlockArea.setIPD(referenceIPD);
            // Get reference IPD from parentArea
            setCurrentArea(curBlockArea); // ??? for generic operations
        }
        return curBlockArea;
    }

    /**
     * Add the child to the list item area.
     *
     * @param childArea the child to add to the cell
     */
    public void addChildArea(Area childArea) {
        if (curBlockArea != null) {
            curBlockArea.addBlock((Block) childArea);
        }
    }

    /**
     * Reset the position of the layout.
     *
     * @param resetPos the position to reset to
     */
    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
        } else {
            setFinished(false);
            //reset(resetPos);
        }
    }
}

