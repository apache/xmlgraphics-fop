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
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

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

    private LinkedList labelList = null;
    private boolean labelAreasAdded = false;

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

    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        // label
        labelList = label.getNextKnuthElements(context, alignment);

        // body
        LinkedList returnedList = body.getNextKnuthElements(context, alignment);

        // "wrap" the Position inside each element
        LinkedList tempList = returnedList;
        KnuthElement tempElement;
        returnedList = new LinkedList();
        ListIterator listIter = tempList.listIterator();
        while (listIter.hasNext()) {
            tempElement = (KnuthElement)listIter.next();
            tempElement.setPosition(new NonLeafPosition(this, tempElement.getPosition()));
            returnedList.add(tempElement);
        }

        setFinished(true);
        return returnedList;
    }

    public LinkedList getChangedKnuthElements(List oldList, int alignment) {
/*LF*/  //log.debug(" LILM.getChanged> label");
        // label
        labelList = label.getChangedKnuthElements(labelList, alignment);

/*LF*/  //log.debug(" LILM.getChanged> body");
        // body
        // "unwrap" the Positions stored in the elements
        ListIterator oldListIterator = oldList.listIterator();
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

        LinkedList returnedList = body.getChangedKnuthElements(oldList, alignment);
        // "wrap" the Position inside each element
        LinkedList tempList = returnedList;
        KnuthElement tempElement;
        returnedList = new LinkedList();
        ListIterator listIter = tempList.listIterator();
        while (listIter.hasNext()) {
            tempElement = (KnuthElement)listIter.next();
            tempElement.setPosition(new NonLeafPosition(this, tempElement.getPosition()));
            returnedList.add(tempElement);
        }

        return returnedList;
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

        LayoutManager childLM = null;
        LayoutContext lc = new LayoutContext(0);
        LayoutManager firstLM = null;
        LayoutManager lastLM = null;

        // create a new list; 
        // add the label areas, if this is the first time addAreas() is called
        LinkedList positionList = new LinkedList();
        if (!labelAreasAdded) {
            KnuthPossPosIter labelPosIter = new KnuthPossPosIter(labelList, 0, labelList.size());
            while (labelPosIter.hasNext()) {
                positionList.add((Position) labelPosIter.next());
            }
        }
        // "unwrap" the NonLeafPositions stored in parentIter
        Position pos;
        while (parentIter.hasNext()) {
            pos = (Position) parentIter.next();
            if (pos instanceof NonLeafPosition
                && ((NonLeafPosition) pos).getPosition().getLM() != this) {
                // pos was created by a child of this ListBlockLM
                positionList.add(((NonLeafPosition) pos).getPosition());
                lastLM = ((NonLeafPosition) pos).getPosition().getLM();
                if (firstLM == null) {
                    firstLM = lastLM;
                }
            }
        }

        StackingIter childPosIter = new StackingIter(positionList.listIterator());
        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // Add the block areas to Area
            lc.setFlags(LayoutContext.FIRST_AREA, childLM == firstLM);
            lc.setFlags(LayoutContext.LAST_AREA, childLM == lastLM);
            // reset the area height after adding the label areas
            if (!labelAreasAdded && childLM == firstLM) {
                curBlockArea.setBPD(0);
                labelAreasAdded = true;
            }
            /* questo non e' correttissimo, bisogna avere due diversi valori per la label e il body */
            lc.setStackLimit(layoutContext.getStackLimit());
            childLM.addAreas(childPosIter, lc);
        }

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
    public void addChildArea(Area childArea) {
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

