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

import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.BreakPoss;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.BreakPossPosIter;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * LayoutManager for a list-item FO.
 * The list item contains a list item label and a list item body.
 */
public class ListItemLayoutManager extends BlockStackingLayoutManager {
    private ListItem fobj;
    
    private Item label;
    private Item body;

    private int referenceIPD = 0;

    private Block curBlockArea = null;

    //private List cellList = null;
    private int listItemHeight;

    //TODO space-before|after: handle space-resolution rules
    private MinOptMax spaceBefore;
    private MinOptMax spaceAfter;
    
    private class ItemPosition extends LeafPosition {
        protected List cellBreaks;
        protected ItemPosition(LayoutManager lm, int pos, List l) {
            super(lm, pos);
            cellBreaks = l;
        }
    }

    /**
     * Create a new list item layout manager.
     * @param node list-item to create the layout manager for
     */
    public ListItemLayoutManager(ListItem node) {
        super(node);
        fobj = node;
        setLabel(node.getLabel());
        setBody(node.getBody());
    }

    /**
     * Create a LM for the fo:list-item-label object
     * @param node the fo:list-item-label FO
     */
    public void setLabel(ListItemLabel node) {
        label = new Item(node);
        label.setParent(this);
    }

    /**
     * Create a LM for the fo:list-item-body object
     * @param node the fo:list-item-body FO
     */
    public void setBody(ListItemBody node) {
        body = new Item(node); 
        body.setParent(this);
    }

    /** @see org.apache.fop.layoutmgr.AbstractLayoutManager#initProperties() */
    protected void initProperties() {
        super.initProperties();
        spaceBefore = new SpaceVal(fobj.getCommonMarginBlock().spaceBefore).getSpace();
        spaceAfter = new SpaceVal(fobj.getCommonMarginBlock().spaceAfter).getSpace();
    }

    private int getIPIndents() {
        int iIndents = 0;
        iIndents += fobj.getCommonMarginBlock().startIndent.getValue();
        iIndents += fobj.getCommonMarginBlock().endIndent.getValue();
        return iIndents;
    }
    
    /**
     * Get the next break possibility.
     *
     * @param context the layout context for getting breaks
     * @return the next break possibility
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        // currently active LM
        Item curLM;

        //int allocBPD = context.
        referenceIPD = context.getRefIPD();

        BreakPoss lastPos = null;
        List breakList = new ArrayList();

        int min = 0;
        int opt = 0;
        int max = 0;

        int stage = 0;
        boolean over = false;
        while (true) {
            if (stage == 0) {
                curLM = label;
            } else if (stage == 1) {
                curLM = body;
            } else {
                break;
            }
            List childBreaks = new ArrayList();
            MinOptMax stackSize = new MinOptMax();

            // Set up a LayoutContext
            // the ipd is from the current column
            //int ipd = context.getRefIPD();
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(
                  MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));
            childLC.setRefIPD(referenceIPD);
            
            stage++;
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
                    } else {
                        lastPos = bp;
                    }
                    stackSize.add(bp.getStackingSize());
                    childBreaks.add(bp);

                    if (bp.nextBreakOverflows()) {
                        over = true;
                        break;
                    }

                    childLC.setStackLimit(MinOptMax.subtract(
                                             context.getStackLimit(), stackSize));
                }
            }
            // the min is the maximum min of the label and body
            if (stackSize.min > min) {
                min = stackSize.min;
            }
            // the optimum is the minimum of all optimums
            if (stackSize.opt > opt) {
                opt = stackSize.opt;
            }
            // the maximum is the largest maximum
            if (stackSize.max > max) {
                max = stackSize.max;
            }

            breakList.add(childBreaks);
        }
        listItemHeight = opt;

        if (label.isFinished() && body.isFinished()) {
            setFinished(true);
        }

        MinOptMax itemSize = new MinOptMax(min, opt, max);
        
        //Add spacing
        if (spaceAfter != null) {
            itemSize.add(spaceAfter);
        }
        if (spaceBefore != null) {
            itemSize.add(spaceBefore);
        }
        
        ItemPosition rp = new ItemPosition(this, breakList.size() - 1, breakList);
        BreakPoss breakPoss = new BreakPoss(rp);
        if (over) {
            breakPoss.setFlag(BreakPoss.NEXT_OVERFLOWS, true);
        }
        breakPoss.setStackingSize(itemSize);
        return breakPoss;
    }

    /**
     * Add the areas for the break points.
     * This sets the offset of each cell as it is added.
     *
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);

        // if adjusted space before
        double adjust = layoutContext.getSpaceAdjust();
        addBlockSpacing(adjust, spaceBefore);
        spaceBefore = null;

        addID(fobj.getId());

        Item childLM;
        LayoutContext lc = new LayoutContext(0);
        while (parentIter.hasNext()) {
            ItemPosition lfp = (ItemPosition) parentIter.next();
            // Add the block areas to Area

            for (Iterator iter = lfp.cellBreaks.iterator(); iter.hasNext();) {
                List cellsbr = (List)iter.next();
                PositionIterator breakPosIter;
                breakPosIter = new BreakPossPosIter(cellsbr, 0, cellsbr.size());

                while ((childLM = (Item)breakPosIter.getNextChildLM()) != null) {
                    childLM.addAreas(breakPosIter, lc);
                }
            }
        }

        curBlockArea.setBPD(listItemHeight);

        flush();

        // if adjusted space after
        addBlockSpacing(adjust, spaceAfter);
        
        curBlockArea = null;
    }

    /**
     * Get the height of the list item after adjusting.
     * Should only be called after adding the list item areas.
     *
     * @return the height of this list item after adjustment
     */
    public int getListItemHeight() {
        return listItemHeight;
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
     * @param childArea the child area
     * @return the parent are for the child
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();

            // Set up dimensions
            /*Area parentArea =*/ parentLM.getParentArea(curBlockArea);
            
            // set traits
            TraitSetter.addBorders(curBlockArea, fobj.getCommonBorderPaddingBackground());
            TraitSetter.addBackground(curBlockArea, fobj.getCommonBorderPaddingBackground());
            TraitSetter.addMargins(curBlockArea,
                    fobj.getCommonBorderPaddingBackground(), 
                    fobj.getCommonMarginBlock());
            TraitSetter.addBreaks(curBlockArea, 
                    fobj.getBreakBefore(), fobj.getBreakAfter());
            
            int contentIPD = referenceIPD - getIPIndents();
            curBlockArea.setIPD(contentIPD);

            setCurrentArea(curBlockArea);
        }
        return curBlockArea;
    }

    /**
     * Add the child.
     * Rows return the areas returned by the child elements.
     * This simply adds the area to the parent layout manager.
     *
     * @param childArea the child area
     */
    public void addChild(Area childArea) {
        if (curBlockArea != null) {
            curBlockArea.addBlock((Block) childArea);
        }
    }

    /**
     * Reset the position of this layout manager.
     *
     * @param resetPos the position to reset to
     */
    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
        }
    }
}

