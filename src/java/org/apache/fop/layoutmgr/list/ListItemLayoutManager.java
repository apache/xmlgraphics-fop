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
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

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

    private Block curBlockArea = null;

    private LinkedList labelList = null;
    private LinkedList bodyList = null;

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

    private class ListItemPosition extends Position {
        private int iLabelFirstIndex;
        private int iLabelLastIndex;
        private int iBodyFirstIndex;
        private int iBodyLastIndex;

        public ListItemPosition(LayoutManager lm, int labelFirst, int labelLast, 
                int bodyFirst, int bodyLast) {
            super(lm);
            iLabelFirstIndex = labelFirst;
            iLabelLastIndex = labelLast;
            iBodyFirstIndex = bodyFirst;
            iBodyLastIndex = bodyLast;
        }
        
        public int getLabelFirstIndex() {
            return iLabelFirstIndex;
        }
        
        public int getLabelLastIndex() {
            return iLabelLastIndex;
        }

        public int getBodyFirstIndex() {
            return iBodyFirstIndex;
        }
        
        public int getBodyLastIndex() {
            return iBodyLastIndex;
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
     * @see org.apache.fop.layoutmgr.LayoutManager#getNextKnuthElements(org.apache.fop.layoutmgr.LayoutContext, int)
     */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        referenceIPD = context.getRefIPD();

        // label
        labelList = label.getNextKnuthElements(context, alignment);

        // body
        bodyList = body.getNextKnuthElements(context, alignment);

        // create a combined list
        LinkedList returnedList = getCombinedKnuthElementsForListItem(labelList, bodyList);

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

    private LinkedList getCombinedKnuthElementsForListItem(LinkedList labelElements,
                                                           LinkedList bodyElements) {
        //Copy elements to array lists to improve element access performance
        List[] elementLists = {new ArrayList(labelElements),
                               new ArrayList(bodyElements)};
        int[] fullHeights = {calcItemHeightFromContents(elementLists[0]),
                            calcItemHeightFromContents(elementLists[1])};
        int[] partialHeights = {0, 0};
        int[] start = {-1, -1};
        int[] end = {-1, -1};

        int totalHeight = Math.max(fullHeights[0], fullHeights[1]);
        int step;
        int addedBoxHeight = 0;

        LinkedList returnList = new LinkedList();
        while ((step = getNextStep(elementLists, start, end, partialHeights))
               > 0) {
            // compute penalty height and box height
            int penaltyHeight = step 
                + getMaxRemainingHeight(fullHeights, partialHeights) 
                - totalHeight;
            int boxHeight = step - addedBoxHeight - penaltyHeight;

            // add the new elements
            addedBoxHeight += boxHeight;
            ListItemPosition stepPosition = new ListItemPosition(this, 
                    start[0], end[0], start[1], end[1]);
            returnList.add(new KnuthBox(boxHeight, stepPosition, false));
            if (addedBoxHeight < totalHeight) {
                returnList.add(new KnuthPenalty(penaltyHeight, 0, false, stepPosition, false));
            }
        }

        return returnList;
    }

    private int calcItemHeightFromContents(List elements, int start, int end) {
        ListIterator iter = elements.listIterator(start);
        int count = end - start + 1;
        int len = 0;
        while (iter.hasNext()) {
            KnuthElement el = (KnuthElement)iter.next();
            if (el.isBox()) {
                len += el.getW();
            } else if (el.isGlue()) {
                len += el.getW();
            } else {
                log.debug("Ignoring penalty: " + el);
                //ignore penalties
            }
            count--;
            if (count == 0) {
                break;
            }
        }
        return len;
    }
    
    private int calcItemHeightFromContents(List elements) {
        return calcItemHeightFromContents(elements, 0, elements.size() - 1);
    }

    private int getNextStep(List[] elementLists, int[] start, int[] end, int[] partialHeights) {
        // backup of partial heights
        int[] backupHeights = {partialHeights[0], partialHeights[1]};

        // set starting points
        start[0] = end[0] + 1;
        start[1] = end[1] + 1;

        // get next possible sequence for label and body
        int seqCount = 0;
        for (int i = 0; i < start.length; i++) {
            while (end[i] + 1 < elementLists[i].size()) {
                end[i]++;
                KnuthElement el = (KnuthElement)elementLists[i].get(end[i]);
                if (el.isPenalty()) {
                    if (el.getP() < KnuthElement.INFINITE) {
                        //First legal break point
                        break;
                    }
                } else if (el.isGlue()) {
                    KnuthElement prev = (KnuthElement)elementLists[i].get(end[i] - 1);
                    if (prev.isBox()) {
                        //Second legal break point
                        break;
                    }
                    partialHeights[i] += el.getW();
                } else {
                    partialHeights[i] += el.getW();
                }
            }
            if (end[i] < start[i]) {
                partialHeights[i] = backupHeights[i];
            } else {
                seqCount++;
            }
        }
        if (seqCount == 0) {
            return 0;
        }
        
        // determine next step
        int step;
        if (backupHeights[0] == 0 && backupHeights[1] == 0) {
            // this is the first step: choose the maximum increase, so that
            // the smallest area in the first page will contain at least
            // a label area and a body area
            step = Math.max((end[0] >= start[0] ? partialHeights[0] : Integer.MIN_VALUE),
                            (end[1] >= start[1] ? partialHeights[1] : Integer.MIN_VALUE));
        } else {
            // this is not the first step: choose the minimum increase
            step = Math.min((end[0] >= start[0] ? partialHeights[0] : Integer.MAX_VALUE),
                            (end[1] >= start[1] ? partialHeights[1] : Integer.MAX_VALUE));
        }

        // reset bigger-than-step sequences
        for (int i = 0; i < partialHeights.length; i++) {
            if (partialHeights[i] > step) {
                partialHeights[i] = backupHeights[i];
                end[i] = start[i] - 1;
            }
        }

        return step;
    }

    private int getMaxRemainingHeight(int[] fullHeights, int[] partialHeights) {
        return Math.max(fullHeights[0] - partialHeights[0],
                        fullHeights[1] - partialHeights[1]);
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getChangedKnuthElements(java.util.List, int)
     */
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

        getPSLM().addIDToPage(fobj.getId());

        LayoutContext lc = new LayoutContext(0);

        // "unwrap" the NonLeafPositions stored in parentIter
        LinkedList positionList = new LinkedList();
        Position pos;
        while (parentIter.hasNext()) {
            pos = (Position) parentIter.next();
            if (pos instanceof NonLeafPosition) {
                // pos contains a ListItemPosition created by this ListBlockLM
                positionList.add(((NonLeafPosition) pos).getPosition());
            }
        }

        // use the first and the last ListItemPosition to determine the 
        // corresponding indexes in the original labelList and bodyList
        int labelFirstIndex = ((ListItemPosition) positionList.getFirst()).getLabelFirstIndex();
        int labelLastIndex = ((ListItemPosition) positionList.getLast()).getLabelLastIndex();
        int bodyFirstIndex = ((ListItemPosition) positionList.getFirst()).getBodyFirstIndex();
        int bodyLastIndex = ((ListItemPosition) positionList.getLast()).getBodyLastIndex();

        // add label areas
        if (labelFirstIndex <= labelLastIndex) {
            KnuthPossPosIter labelIter = new KnuthPossPosIter(labelList, 
                    labelFirstIndex, labelLastIndex + 1);
            lc.setFlags(LayoutContext.FIRST_AREA, layoutContext.isFirstArea());
            lc.setFlags(LayoutContext.LAST_AREA, layoutContext.isLastArea());
            // TO DO: use the right stack limit for the label
            lc.setStackLimit(layoutContext.getStackLimit());
            label.addAreas(labelIter, lc);
        }

        // reset the area bpd after adding the label areas and before adding the body areas
        int savedBPD = 0;
        if (labelFirstIndex <= labelLastIndex
            && bodyFirstIndex <= bodyLastIndex) {
            savedBPD = curBlockArea.getBPD();
            curBlockArea.setBPD(0);
        }

        // add body areas
        if (bodyFirstIndex <= bodyLastIndex) {
            KnuthPossPosIter bodyIter = new KnuthPossPosIter(bodyList, 
                    bodyFirstIndex, bodyLastIndex + 1);
            lc.setFlags(LayoutContext.FIRST_AREA, layoutContext.isFirstArea());
            lc.setFlags(LayoutContext.LAST_AREA, layoutContext.isLastArea());
            // TO DO: use the right stack limit for the body
            lc.setStackLimit(layoutContext.getStackLimit());
            body.addAreas(bodyIter, lc);
        }

        // after adding body areas, set the maximum area bpd
        if (curBlockArea.getBPD() < savedBPD) {
            curBlockArea.setBPD(savedBPD);
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

