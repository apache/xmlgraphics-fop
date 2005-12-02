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
 
package org.apache.fop.layoutmgr.table;

import java.util.LinkedList;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.AreaAdditionUtil;
import org.apache.fop.layoutmgr.BlockLevelLayoutManager;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for a table-cell FO.
 * A cell contains blocks. These blocks fill the cell.
 */
public class TableCellLayoutManager extends BlockStackingLayoutManager 
            implements BlockLevelLayoutManager {
    
    private PrimaryGridUnit gridUnit;
    
    private Block curBlockArea;

    private int inRowIPDOffset;
    
    private int xoffset;
    private int yoffset;
    private int cellIPD;
    private int rowHeight;
    private int usedBPD;
    private int startBorderWidth;
    private int endBorderWidth;
    private int borderAndPaddingBPD;
    private boolean emptyCell = true;

    /**
     * Create a new Cell layout manager.
     * @param node table-cell FO for which to create the LM
     * @param pgu primary grid unit for the cell 
     */
    public TableCellLayoutManager(TableCell node, PrimaryGridUnit pgu) {
        super(node);
        fobj = node;
        this.gridUnit = pgu;
    }

    /** @return the table-cell FO */
    public TableCell getTableCell() {
        return (TableCell)this.fobj;
    }
    
    private boolean isSeparateBorderModel() {
        return getTableCell().isSeparateBorderModel();
    }
    
    /** @see org.apache.fop.layoutmgr.LayoutManager#initialize() */
    public void initialize() {
        borderAndPaddingBPD = 0;
        borderAndPaddingBPD += getTableCell()
            .getCommonBorderPaddingBackground().getBorderBeforeWidth(false);
        borderAndPaddingBPD += getTableCell()
            .getCommonBorderPaddingBackground().getBorderAfterWidth(false);
        if (!isSeparateBorderModel()) {
            borderAndPaddingBPD /= 2;
        }
        borderAndPaddingBPD += getTableCell().getCommonBorderPaddingBackground()
                .getPaddingBefore(false, this);
        borderAndPaddingBPD += getTableCell().getCommonBorderPaddingBackground()
                .getPaddingAfter(false, this);
    }
    
    /**
     * @return the table owning this cell
     */
    public Table getTable() {
        FONode node = fobj.getParent();
        while (!(node instanceof Table)) {
            node = node.getParent();
        }
        return (Table)node;
    }
    

    /** @see org.apache.fop.layoutmgr.BlockStackingLayoutManager#getIPIndents() */
    protected int getIPIndents() {
        int iIndents = 0;
        int[] startEndBorderWidths = gridUnit.getStartEndBorderWidths();
        startBorderWidth += startEndBorderWidths[0];
        endBorderWidth += startEndBorderWidths[1];
        iIndents += startBorderWidth;
        iIndents += endBorderWidth;
        if (!isSeparateBorderModel()) {
            iIndents /= 2;
        }
        iIndents += getTableCell().getCommonBorderPaddingBackground().getPaddingStart(false, this);
        iIndents += getTableCell().getCommonBorderPaddingBackground().getPaddingEnd(false, this);
        return iIndents;
    }
    
    /**
     * @see org.apache.fop.layoutmgr.LayoutManager
     */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        MinOptMax stackLimit = new MinOptMax(context.getStackLimit());

        referenceIPD = context.getRefIPD(); 
        cellIPD = referenceIPD;
        cellIPD -= getIPIndents();
        if (isSeparateBorderModel()) {
            int borderSep = getTableCell().getBorderSeparation().getLengthPair()
                    .getIPD().getLength().getValue(this);
            cellIPD -= borderSep;
        }

        LinkedList returnedList = null;
        LinkedList contentList = new LinkedList();
        LinkedList returnList = new LinkedList();

        BlockLevelLayoutManager curLM; // currently active LM
        BlockLevelLayoutManager prevLM = null; // previously active LM
        while ((curLM = (BlockLevelLayoutManager) getChildLM()) != null) {
            LayoutContext childLC = new LayoutContext(0);
            // curLM is a ?
            childLC.setStackLimit(MinOptMax.subtract(context
                    .getStackLimit(), stackLimit));
            childLC.setRefIPD(cellIPD);

            // get elements from curLM
            returnedList = curLM.getNextKnuthElements(childLC, alignment);
            if (childLC.isKeepWithNextPending()) {
                log.debug("child LM signals pending keep with next");
            }
            if (contentList.size() == 0 && childLC.isKeepWithPreviousPending()) {
                context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
                childLC.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING, false);
            }
            
            if (returnedList.size() == 1
                    && ((ListElement)returnedList.getFirst()).isForcedBreak()) {
                // a descendant of this block has break-before
                if (returnList.size() == 0) {
                    // the first child (or its first child ...) has
                    // break-before;
                    // all this block, including space before, will be put in
                    // the
                    // following page
                }
                contentList.addAll(returnedList);

                // "wrap" the Position inside each element
                // moving the elements from contentList to returnList
                returnedList = new LinkedList();
                wrapPositionElements(contentList, returnList);

                //Space resolution
                SpaceResolver.resolveElementList(returnList);
                
                return returnList;
            } else {
                if (prevLM != null) {
                    // there is a block handled by prevLM
                    // before the one handled by curLM
                    if (mustKeepTogether() 
                            || context.isKeepWithNextPending()
                            || childLC.isKeepWithPreviousPending()) {
                        //Clear keep pending flag
                        context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING, false);
                        childLC.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING, false);
                        // add an infinite penalty to forbid a break between
                        // blocks
                        contentList.add(new BreakElement(
                                new Position(this), KnuthElement.INFINITE, context));
                        //contentList.add(new KnuthPenalty(0,
                        //        KnuthElement.INFINITE, false,
                        //        new Position(this), false));
                    } else if (!((ListElement) contentList.getLast()).isGlue()) {
                        // add a null penalty to allow a break between blocks
                        contentList.add(new BreakElement(
                                new Position(this), 0, context));
                        //contentList.add(new KnuthPenalty(0, 0, false,
                        //        new Position(this), false));
                    } else {
                        // the last element in contentList is a glue;
                        // it is a feasible breakpoint, there is no need to add
                        // a penalty
                    }
                }
                contentList.addAll(returnedList);
                if (returnedList.size() == 0) {
                    //Avoid NoSuchElementException below (happens with empty blocks)
                    continue;
                }
                if (((ListElement)returnedList.getLast()).isForcedBreak()) {
                    // a descendant of this block has break-after
                    if (curLM.isFinished()) {
                        // there is no other content in this block;
                        // it's useless to add space after before a page break
                        setFinished(true);
                    }

                    returnedList = new LinkedList();
                    wrapPositionElements(contentList, returnList);

                    //Space resolution
                    SpaceResolver.resolveElementList(returnList);
                    
                    return returnList;
                }
            }
            if (childLC.isKeepWithNextPending()) {
                //Clear and propagate
                childLC.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING, false);
                context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING);
            }
            prevLM = curLM;
        }

        returnedList = new LinkedList();
        wrapPositionElements(contentList, returnList);
        
        //Space resolution
        SpaceResolver.resolveElementList(returnList);
        
        setFinished(true);
        return returnList;
    }
    
    /**
     * Set the y offset of this cell.
     * This offset is used to set the absolute position of the cell.
     *
     * @param off the y direction offset
     */
    public void setYOffset(int off) {
        yoffset = off;
    }

    /**
     * Set the x offset of this cell (usually the same as its parent row).
     * This offset is used to determine the absolute position of the cell.
     *
     * @param off the x offset
     */
    public void setXOffset(int off) {
        xoffset = off;
    }

    /**
     * Set the IPD offset of this cell inside the table-row.
     * This offset is used to determine the absolute position of the cell.
     * @param off the IPD offset
     */
    public void setInRowIPDOffset(int off) {
        this.inRowIPDOffset = off;
    }
    
    /**
     * Set the content height for this cell. This method is used during
     * addAreas() stage.
     *
     * @param h the height of the contents of this cell
     */
    public void setContentHeight(int h) {
        usedBPD = h;
    }
    
    /**
     * Set the row height that contains this cell. This method is used during
     * addAreas() stage.
     *
     * @param h the height of the row
     */
    public void setRowHeight(int h) {
        rowHeight = h;
    }

    private int getContentHeight(int rowHeight, GridUnit gu) {
        int bpd = rowHeight;
        if (isSeparateBorderModel()) {
            bpd -= gu.getPrimary().getBorders().getBorderBeforeWidth(false);
            bpd -= gu.getPrimary().getBorders().getBorderAfterWidth(false);
        } else {
            bpd -= gu.getPrimary().getHalfMaxBorderWidth();
        }
        CommonBorderPaddingBackground cbpb 
            = gu.getCell().getCommonBorderPaddingBackground(); 
        bpd -= cbpb.getPaddingBefore(false, this);
        bpd -= cbpb.getPaddingAfter(false, this);
        bpd -= 2 * ((TableLayoutManager)getParent()).getHalfBorderSeparationBPD();
        return bpd;
    }
    
    /**
     * Add the areas for the break points.
     * The cell contains block stacking layout managers
     * that add block areas.
     *
     * @param parentIter the iterator of the break positions
     * @param layoutContext the layout context for adding the areas
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);

        getPSLM().addIDToPage(getTableCell().getId());

        if (isSeparateBorderModel()) {
            if (!emptyCell || getTableCell().showEmptyCells()) {
                TraitSetter.addBorders(curBlockArea, 
                        getTableCell().getCommonBorderPaddingBackground(), this);
            }
        } else {
            boolean[] outer = new boolean[] {
                    gridUnit.getFlag(GridUnit.FIRST_IN_TABLE), 
                    gridUnit.getFlag(GridUnit.LAST_IN_TABLE),
                    gridUnit.getFlag(GridUnit.IN_FIRST_COLUMN),
                    gridUnit.getFlag(GridUnit.IN_LAST_COLUMN)};
            if (!gridUnit.hasSpanning()) {
                //Can set the borders directly if there's no span
                TraitSetter.addCollapsingBorders(curBlockArea, 
                        gridUnit.getBorders(), outer, this);
            } else {
                int dy = yoffset;
                for (int y = 0; y < gridUnit.getRows().size(); y++) {
                    GridUnit[] gridUnits = (GridUnit[])gridUnit.getRows().get(y);
                    int dx = xoffset;
                    int lastRowHeight = 0;
                    for (int x = 0; x < gridUnits.length; x++) {
                        GridUnit gu = gridUnits[x];
                        if (!gu.hasBorders()) {
                            continue;
                        }
                        
                        //Blocks for painting grid unit borders
                        Block block = new Block();
                        block.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
                        block.setPositioning(Block.ABSOLUTE);

                        int bpd = getContentHeight(rowHeight, gu);
                        if (isSeparateBorderModel()) {
                            bpd += (gu.getBorders().getBorderBeforeWidth(false));
                            bpd += (gu.getBorders().getBorderAfterWidth(false));
                        } else {
                            bpd += gridUnit.getHalfMaxBeforeBorderWidth() 
                                    - (gu.getBorders().getBorderBeforeWidth(false) / 2);
                            bpd += gridUnit.getHalfMaxAfterBorderWidth() 
                                    - (gu.getBorders().getBorderAfterWidth(false) / 2);
                        }
                        block.setBPD(bpd);
                        lastRowHeight = rowHeight;
                        int ipd = gu.getColumn().getColumnWidth().getValue(this);
                        int borderStartWidth = gu.getBorders().getBorderStartWidth(false) / 2; 
                        ipd -= borderStartWidth;
                        ipd -= gu.getBorders().getBorderEndWidth(false) / 2;
                        block.setIPD(ipd);
                        block.setXOffset(dx + borderStartWidth);
                        int halfCollapsingBorderHeight = 0;
                        if (!isSeparateBorderModel()) {
                            halfCollapsingBorderHeight 
                                += gu.getBorders().getBorderBeforeWidth(false) / 2;
                        }
                        block.setYOffset(dy - halfCollapsingBorderHeight);
                        TraitSetter.addCollapsingBorders(block, gu.getBorders(), outer, this);
                        parentLM.addChildArea(block);
                        dx += gu.getColumn().getColumnWidth().getValue(this);
                    }
                    dy += lastRowHeight;
                }
                log.warn("TODO Add collapsed border painting for spanned cells");
            }
        }

        //Handle display-align
        int contentBPD = getContentHeight(rowHeight, gridUnit);
        if (usedBPD < contentBPD) {
            if (getTableCell().getDisplayAlign() == EN_CENTER) {
                Block space = new Block();
                space.setBPD((contentBPD - usedBPD) / 2);
                curBlockArea.addBlock(space);
            } else if (getTableCell().getDisplayAlign() == EN_AFTER) {
                Block space = new Block();
                space.setBPD((contentBPD - usedBPD));
                curBlockArea.addBlock(space);
            }
        }

        AreaAdditionUtil.addAreas(this, parentIter, layoutContext);
        
        curBlockArea.setBPD(contentBPD);

        // Add background after we know the BPD
        if (isSeparateBorderModel()) {
            if (!emptyCell || getTableCell().showEmptyCells()) {
                TraitSetter.addBackground(curBlockArea,
                        getTableCell().getCommonBorderPaddingBackground(),
                        this);
            }
        } else {
            TraitSetter.addBackground(curBlockArea,
                    getTableCell().getCommonBorderPaddingBackground(),
                    this);
        }
        
        flush();

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
            curBlockArea.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
            TraitSetter.setProducerID(curBlockArea, getTableCell().getId());
            curBlockArea.setPositioning(Block.ABSOLUTE);
            int indent = 0;
            indent += startBorderWidth;
            if (!isSeparateBorderModel()) {
                indent /= 2;
            }
            indent += getTableCell()
                    .getCommonBorderPaddingBackground().getPaddingStart(false, this);
            // set position
            int borderAdjust = 0;
            if (!isSeparateBorderModel()) {
                if (gridUnit.hasSpanning()) {
                    borderAdjust -= gridUnit.getHalfMaxBeforeBorderWidth();
                } else {
                    borderAdjust += gridUnit.getHalfMaxBeforeBorderWidth();
                }
            } else {
                //borderAdjust += gridUnit.getBorders().getBorderBeforeWidth(false);
            }
            TableLayoutManager tableLM = (TableLayoutManager)getParent();
            curBlockArea.setXOffset(xoffset + inRowIPDOffset 
                    + tableLM.getHalfBorderSeparationIPD() + indent);
            curBlockArea.setYOffset(yoffset - borderAdjust 
                    + tableLM.getHalfBorderSeparationBPD());
            curBlockArea.setIPD(cellIPD);
            //curBlockArea.setHeight();

            // Set up dimensions
            /*Area parentArea =*/ parentLM.getParentArea(curBlockArea);
            // Get reference IPD from parentArea
            setCurrentArea(curBlockArea); // ??? for generic operations
        }
        return curBlockArea;
    }

    /**
     * Add the child to the cell block area.
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
        }
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager
     */
    public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager
     */
    public void discardSpace(KnuthGlue spaceGlue) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepTogether()
     */
    public boolean mustKeepTogether() {
        //TODO Keeps will have to be more sophisticated sooner or later
        boolean keep = ((BlockLevelLayoutManager)getParent()).mustKeepTogether();
        if (gridUnit.getRow() != null) {
            keep |= gridUnit.getRow().mustKeepTogether();
        }
        return keep;
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithPrevious()
     */
    public boolean mustKeepWithPrevious() {
        return false; //TODO FIX ME
        /*
        return !fobj.getKeepWithPrevious().getWithinPage().isAuto()
            || !fobj.getKeepWithPrevious().getWithinColumn().isAuto();
            */
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithNext()
     */
    public boolean mustKeepWithNext() {
        return false; //TODO FIX ME
        /*
        return !fobj.getKeepWithNext().getWithinPage().isAuto()
            || !fobj.getKeepWithNext().getWithinColumn().isAuto();
            */
    }
    
    // --------- Property Resolution related functions --------- //
    
    /**
     * Returns the IPD of the content area
     * @return the IPD of the content area
     */
    public int getContentAreaIPD() {
        return cellIPD;
    }
   
    /**
     * Returns the BPD of the content area
     * @return the BPD of the content area
     */
    public int getContentAreaBPD() {
        if (curBlockArea != null) {
            return curBlockArea.getBPD();
        } else {
            log.error("getContentAreaBPD called on unknown BPD");
            return -1;
        }
    }
   
    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getGeneratesReferenceArea
     */
    public boolean getGeneratesReferenceArea() {
        return true;
    }
   
    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getGeneratesBlockArea
     */
    public boolean getGeneratesBlockArea() {
        return true;
    }

}

