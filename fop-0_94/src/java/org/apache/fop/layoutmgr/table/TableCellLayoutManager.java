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

package org.apache.fop.layoutmgr.table;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.datatypes.PercentBaseContext;
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
import org.apache.fop.layoutmgr.KnuthPenalty;
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

    /**
     * logging instance
     */
    private static Log log = LogFactory.getLog(TableCellLayoutManager.class);

    private PrimaryGridUnit primaryGridUnit;

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
        this.primaryGridUnit = pgu;
    }

    /** @return the table-cell FO */
    public TableCell getTableCell() {
        return (TableCell)this.fobj;
    }

    private boolean isSeparateBorderModel() {
        return getTable().isSeparateBorderModel();
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
        int[] startEndBorderWidths = primaryGridUnit.getStartEndBorderWidths();
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
            int borderSep = getTable().getBorderSeparation().getLengthPair()
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
                } else if (!(((ListElement) contentList.getLast()).isGlue()
                        || (((ListElement)contentList.getLast()).isPenalty()
                                && ((KnuthPenalty)contentList.getLast()).getP() < KnuthElement.INFINITE)
                                || (contentList.getLast() instanceof BreakElement
                                        && ((BreakElement)contentList.getLast()).getPenaltyValue() < KnuthElement.INFINITE))) {
                    // TODO vh: this is hacky
                    // The getNextKnuthElements method of TableCellLM must not be called
                    // twice, otherwise some settings like indents or borders will be
                    // counted several times and lead to a wrong output. Anyway the
                    // getNextKnuthElements methods should be called only once eventually
                    // (i.e., when multi-threading the code), even when there are forced
                    // breaks.
                    // If we add a break possibility after a forced break the
                    // AreaAdditionUtil.addAreas method will act on a sequence starting
                    // with a SpaceResolver.SpaceHandlingBreakPosition element, having no
                    // LM associated to it. Thus it will stop early instead of adding
                    // areas for following Positions. The above test aims at preventing
                    // such a situation from occuring. add a null penalty to allow a break
                    // between blocks
                    contentList.add(new BreakElement(
                            new Position(this), 0, context));
                    //contentList.add(new KnuthPenalty(0, 0, false,
                    //        new Position(this), false));
                } else {
                    // the last element in contentList is a feasible breakpoint, there is
                    // no need to add a penalty
                }
            }
            contentList.addAll(returnedList);
            if (returnedList.size() == 0) {
                //Avoid NoSuchElementException below (happens with empty blocks)
                continue;
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

        getPSLM().notifyEndOfLayout(((TableCell)getFObj()).getId());

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

    /**
     * Returns the bpd of the given grid unit.
     * @param gu a grid unit belonging to this cell
     * @return the content height of the grid unit
     */
    private int getContentHeight(GridUnit gu) {
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
     * Add the areas for the break points. The cell contains block stacking layout
     * managers that add block areas.
     * 
     * <p>In the collapsing-border model, the borders of a cell that spans over several
     * rows or columns are drawn separately for each grid unit. Therefore we must know the
     * height of each grid row spanned over by the cell. Also, if the cell is broken over
     * two pages we must know which spanned grid rows are present on the current page.</p>
     * 
     * @param parentIter the iterator of the break positions
     * @param layoutContext the layout context for adding the areas
     * @param spannedGridRowHeights in collapsing-border model for a spanning cell, height
     * of each spanned grid row
     * @param startRow first grid row on the current page spanned over by the cell,
     * inclusive
     * @param endRow last grid row on the current page spanned over by the cell, exclusive
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext,
                         int[] spannedGridRowHeights,
                         int startRow,
                         int endRow) {
        getParentArea(null);

        getPSLM().addIDToPage(getTableCell().getId());

        if (isSeparateBorderModel()) {
            if (!emptyCell || getTableCell().showEmptyCells()) {
                TraitSetter.addBorders(curBlockArea, getTableCell().getCommonBorderPaddingBackground(),
                        false, false, false, false, this);
                TraitSetter.addPadding(curBlockArea, getTableCell().getCommonBorderPaddingBackground(),
                        false, false, false, false, this);
            }
        } else {
            if (!primaryGridUnit.hasSpanning()) {
                //Can set the borders directly if there's no span
                boolean[] outer = new boolean[] {
                        primaryGridUnit.getFlag(GridUnit.FIRST_IN_TABLE),
                        primaryGridUnit.getFlag(GridUnit.LAST_IN_TABLE),
                        primaryGridUnit.getFlag(GridUnit.IN_FIRST_COLUMN),
                        primaryGridUnit.getFlag(GridUnit.IN_LAST_COLUMN)};
                TraitSetter.addCollapsingBorders(curBlockArea,
                        primaryGridUnit.getBorders(), outer, this);
            } else {
                boolean[] outer = new boolean[4];
                int dy = yoffset;
                for (int y = startRow; y < endRow; y++) {
                    GridUnit[] gridUnits = (GridUnit[])primaryGridUnit.getRows().get(y);
                    int dx = xoffset;
                    for (int x = 0; x < gridUnits.length; x++) {
                        GridUnit gu = gridUnits[x];
                        if (gu.hasBorders()) {
                            //Blocks for painting grid unit borders
                            Block block = new Block();
                            block.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
                            block.setPositioning(Block.ABSOLUTE);
    
                            int bpd = spannedGridRowHeights[y - startRow];
                            bpd -= gu.getBorders().getBorderBeforeWidth(false) / 2;
                            bpd -= gu.getBorders().getBorderAfterWidth(false) / 2;
                            block.setBPD(bpd);
                            if (log.isTraceEnabled()) {
                                log.trace("pgu: " + primaryGridUnit + "; gu: " + gu + "; yoffset: "
                                        + (dy - gu.getBorders().getBorderBeforeWidth(false) / 2)
                                        + "; bpd: " + bpd);
                            }
                            int ipd = gu.getColumn().getColumnWidth().getValue(
                                    (PercentBaseContext) getParent());
                            int borderStartWidth = gu.getBorders().getBorderStartWidth(false) / 2;
                            ipd -= borderStartWidth;
                            ipd -= gu.getBorders().getBorderEndWidth(false) / 2;
                            block.setIPD(ipd);
                            block.setXOffset(dx + borderStartWidth);
                            block.setYOffset(dy - gu.getBorders().getBorderBeforeWidth(false) / 2);
                            outer[0] = gu.getFlag(GridUnit.FIRST_IN_TABLE);
                            outer[1] = gu.getFlag(GridUnit.LAST_IN_TABLE);
                            outer[2] = gu.getFlag(GridUnit.IN_FIRST_COLUMN);
                            outer[3] = gu.getFlag(GridUnit.IN_LAST_COLUMN);
                            TraitSetter.addCollapsingBorders(block, gu.getBorders(), outer, this);
                            parentLM.addChildArea(block);
                        }
                        dx += gu.getColumn().getColumnWidth().getValue(
                                (PercentBaseContext) getParent());
                    }
                    dy += spannedGridRowHeights[y - startRow];
                }
            }
        }
        TraitSetter.addPadding(curBlockArea, primaryGridUnit.getBorders(),
                false, false, false, false, this);

        //Handle display-align
        int contentBPD = getContentHeight(primaryGridUnit);
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
            int indent = startBorderWidth;
            if (!isSeparateBorderModel()) {
                indent /= 2;
            }
            indent += getTableCell()
                    .getCommonBorderPaddingBackground().getPaddingStart(false, this);
            // set position
            int borderAdjust = 0;
            if (!isSeparateBorderModel()) {
                if (primaryGridUnit.hasSpanning()) {
                    borderAdjust -= primaryGridUnit.getHalfMaxBeforeBorderWidth();
                } else {
                    borderAdjust += primaryGridUnit.getHalfMaxBeforeBorderWidth();
                }
            } else {
                //borderAdjust += primaryGridUnit.getBorders().getBorderBeforeWidth(false);
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
        if (primaryGridUnit.getRow() != null) {
            keep |= primaryGridUnit.getRow().mustKeepTogether();
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
