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

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.layoutmgr.AreaAdditionUtil;
import org.apache.fop.layoutmgr.BlockLevelLayoutManager;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.BreakPoss;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.MinOptMaxUtil;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.traits.MinOptMax;
import org.apache.tools.ant.taskdefs.condition.IsSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * LayoutManager for a table-cell FO.
 * A cell contains blocks. These blocks fill the cell.
 */
public class Cell extends BlockStackingLayoutManager implements BlockLevelLayoutManager {
    
    private TableCell fobj;
    private PrimaryGridUnit gridUnit;
    
    private Block curBlockArea;

    private List childBreaks = new ArrayList();

    private int inRowIPDOffset;
    
    private int xoffset;
    private int yoffset;
    private int referenceIPD;
    private int cellIPD;
    private int rowHeight;
    private int usedBPD;
    private int startBorderWidth;
    private int endBorderWidth;
    private int borderAndPaddingBPD;
    private boolean emptyCell = true;

    /** List of Lists containing OldGridUnit instances, one List per row. */
    private List rows = new java.util.ArrayList(); 
    
    /**
     * Create a new Cell layout manager.
     * @node table-cell FO for which to create the LM
     */
    public Cell(TableCell node, PrimaryGridUnit pgu) {
        super(node);
        fobj = node;
        this.gridUnit = pgu;
    }

    /** @return the table-cell FO */
    public TableCell getFObj() {
        return this.fobj;
    }
    
    private boolean isSeparateBorderModel() {
        return fobj.isSeparateBorderModel();
    }
    
    /**
     * @see org.apache.fop.layoutmgr.AbstractLayoutManager#initProperties()
     */
    protected void initProperties() {
        super.initProperties();
        borderAndPaddingBPD = 0;
        borderAndPaddingBPD += fobj.getCommonBorderPaddingBackground().getBorderBeforeWidth(false);
        borderAndPaddingBPD += fobj.getCommonBorderPaddingBackground().getBorderAfterWidth(false);
        if (!isSeparateBorderModel()) {
            borderAndPaddingBPD /= 2;
        }
        borderAndPaddingBPD += fobj.getCommonBorderPaddingBackground().getPaddingBefore(false);
        borderAndPaddingBPD += fobj.getCommonBorderPaddingBackground().getPaddingAfter(false);
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
    

    /**
     * Called by Row LM to register the grid units occupied by this cell for a row.
     * @param spannedGridUnits a List of GridUnits
     */
    public void addGridUnitsFromRow(List spannedGridUnits) {
        log.debug("Getting another row, " + spannedGridUnits.size() + " grid units");
        this.rows.add(spannedGridUnits);
        
    }

    private int getIPIndents() {
        int iIndents = 0;
        int[] startEndBorderWidths = gridUnit.getStartEndBorderWidths();
        startBorderWidth += startEndBorderWidths[0];
        endBorderWidth += startEndBorderWidths[1];
        iIndents += startBorderWidth;
        iIndents += endBorderWidth;
        if (!isSeparateBorderModel()) {
            iIndents /= 2;
        }
        iIndents += fobj.getCommonBorderPaddingBackground().getPaddingStart(false);
        iIndents += fobj.getCommonBorderPaddingBackground().getPaddingEnd(false);
        return iIndents;
    }
    
    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getNextKnuthElements(org.apache.fop.layoutmgr.LayoutContext, int)
     */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        MinOptMax stackSize = new MinOptMax();
        MinOptMax stackLimit = new MinOptMax(context.getStackLimit());

        BreakPoss lastPos = null;

        referenceIPD = context.getRefIPD(); 
        cellIPD = referenceIPD;
        cellIPD -= getIPIndents();
        if (isSeparateBorderModel()) {
            int borderSep = fobj.getBorderSeparation().getLengthPair()
                    .getIPD().getLength().getValue();
            cellIPD -= borderSep;
        }

        LinkedList returnedList = null;
        LinkedList contentList = new LinkedList();
        LinkedList returnList = new LinkedList();
        Position returnPosition = new NonLeafPosition(this, null);

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
                }
                contentList.addAll(returnedList);

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
                contentList.addAll(returnedList);
                if (returnedList.size() == 0) {
                    //Avoid NoSuchElementException below (happens with empty blocks)
                    continue;
                }
                if (((KnuthElement) returnedList.getLast()).isPenalty()
                        && ((KnuthPenalty) returnedList.getLast()).getP() == -KnuthElement.INFINITE) {
                    // a descendant of this block has break-after
                    if (curLM.isFinished()) {
                        // there is no other content in this block;
                        // it's useless to add space after before a page break
                        setFinished(true);
                    }

                    returnedList = new LinkedList();
                    wrapPositionElements(contentList, returnList);

                    return returnList;
                }
            }
            prevLM = curLM;
        }

        returnedList = new LinkedList();
        wrapPositionElements(contentList, returnList);
        
        setFinished(true);
        return returnList;
    }
    
    /**
     * Get the next break possibility for this cell.
     * A cell contains blocks so there are breaks around the blocks
     * and inside the blocks.
     *
     * @param context the layout context
     * @return the next break possibility
     */
    public BreakPoss getNextBreakPossOLDOLDOLD(LayoutContext context) {
        LayoutManager curLM; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        BreakPoss lastPos = null;

        referenceIPD = context.getRefIPD(); 
        cellIPD = referenceIPD;
        cellIPD -= getIPIndents();
        if (fobj.isSeparateBorderModel()) {
            int borderSep = fobj.getBorderSeparation().getLengthPair()
                    .getIPD().getLength().getValue();
            cellIPD -= borderSep;
        }

        while ((curLM = getChildLM()) != null) {
            if (curLM.generatesInlineAreas()) {
                log.error("table-cell must contain block areas - ignoring");
                curLM.setFinished(true);
                continue;
            }
            // Set up a LayoutContext
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));
            childLC.setRefIPD(cellIPD);

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
            
            usedBPD = stackSize.opt;
            if (usedBPD > 0) {
                emptyCell = false;
            }
            
            LengthRangeProperty specifiedBPD = fobj.getBlockProgressionDimension();
            if (specifiedBPD.getEnum() != EN_AUTO) {
                if ((specifiedBPD.getMaximum().getEnum() != EN_AUTO)
                        && (specifiedBPD.getMaximum().getLength().getValue() < stackSize.min)) {
                    log.warn("maximum height of cell is smaller than the minimum "
                            + "height of its contents");
                }
                MinOptMaxUtil.restrict(stackSize, specifiedBPD);
            }
            stackSize = MinOptMax.add(stackSize, new MinOptMax(borderAndPaddingBPD));

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
        bpd -= cbpb.getPaddingBefore(false);
        bpd -= cbpb.getPaddingAfter(false);
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
        //BreakPoss bp1 = (BreakPoss)parentIter.peekNext();
        bBogus = false;//!bp1.generatesAreas(); 

        if (!isBogus()) {
            getPSLM().addIDToPage(fobj.getId());
        }

        if (isSeparateBorderModel()) {
            if (!emptyCell || fobj.showEmptyCells()) {
                TraitSetter.addBorders(curBlockArea, fobj.getCommonBorderPaddingBackground());
                TraitSetter.addBackground(curBlockArea, fobj.getCommonBorderPaddingBackground());
            }
        } else {
            TraitSetter.addBackground(curBlockArea, fobj.getCommonBorderPaddingBackground());
            boolean[] outer = new boolean[] {
                    gridUnit.getFlag(GridUnit.FIRST_IN_TABLE), 
                    gridUnit.getFlag(GridUnit.LAST_IN_TABLE),
                    gridUnit.getFlag(GridUnit.IN_FIRST_COLUMN),
                    gridUnit.getFlag(GridUnit.IN_LAST_COLUMN)};
            if (!gridUnit.hasSpanning()) {
                //Can set the borders directly if there's no span
                TraitSetter.addCollapsingBorders(curBlockArea, 
                        gridUnit.getBorders(), outer);
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
                        //TODO This needs to be fixed for row spanning
                        lastRowHeight = rowHeight;
                        int ipd = gu.getColumn().getColumnWidth().getValue();
                        int borderStartWidth = gu.getBorders().getBorderStartWidth(false) / 2; 
                        ipd -= borderStartWidth;
                        ipd -= gu.getBorders().getBorderEndWidth(false) / 2;
                        block.setIPD(ipd);
                        block.setXOffset(dx + borderStartWidth);
                        int halfCollapsingBorderHeight = 0;
                        if (!isSeparateBorderModel()) {
                            halfCollapsingBorderHeight += 
                                gu.getBorders().getBorderBeforeWidth(false) / 2;
                        }
                        block.setYOffset(dy - halfCollapsingBorderHeight);
                        TraitSetter.addCollapsingBorders(block, gu.getBorders(), outer);
                        parentLM.addChildArea(block);
                        dx += gu.getColumn().getColumnWidth().getValue();
                    }
                    dy += lastRowHeight;
                }
                log.warn("TODO Add collapsed border painting for spanned cells");
            }
        }

        //Handle display-align
        if (usedBPD < rowHeight) {
            if (fobj.getDisplayAlign() == EN_CENTER) {
                Block space = new Block();
                space.setBPD((rowHeight - usedBPD) / 2);
                curBlockArea.addBlock(space);
            } else if (fobj.getDisplayAlign() == EN_AFTER) {
                Block space = new Block();
                space.setBPD((rowHeight - usedBPD));
                curBlockArea.addBlock(space);
            }
        }

        AreaAdditionUtil.addAreas(parentIter, layoutContext);
        
        int contentBPD = getContentHeight(rowHeight, gridUnit);
        curBlockArea.setBPD(contentBPD);

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
            curBlockArea.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
            curBlockArea.setPositioning(Block.ABSOLUTE);
            int indent = 0;
            indent += startBorderWidth;
            if (!isSeparateBorderModel()) {
                indent /= 2;
            }
            indent += fobj.getCommonBorderPaddingBackground().getPaddingStart(false);
            // set position
            int halfBorderSep = 0;
            if (isSeparateBorderModel()) {
                halfBorderSep = fobj.getBorderSeparation().getLengthPair()
                        .getIPD().getLength().getValue() / 2;
            }
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
            curBlockArea.setXOffset(xoffset + inRowIPDOffset + halfBorderSep + indent);
            curBlockArea.setYOffset(yoffset - borderAdjust);
            curBlockArea.setIPD(cellIPD);
            //curBlockArea.setHeight();

            // Set up dimensions
            Area parentArea = parentLM.getParentArea(curBlockArea);
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
            childBreaks.clear();
        }
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#negotiateBPDAdjustment(int, org.apache.fop.layoutmgr.KnuthElement)
     */
    public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#discardSpace(org.apache.fop.layoutmgr.KnuthGlue)
     */
    public void discardSpace(KnuthGlue spaceGlue) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepTogether()
     */
    public boolean mustKeepTogether() {
        //TODO Keeps will have to be more sophisticated sooner or later
        return ((BlockLevelLayoutManager)getParent()).mustKeepTogether()/* 
                || !fobj.getKeepTogether().getWithinPage().isAuto()
                || !fobj.getKeepTogether().getWithinColumn().isAuto()*/;
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

}

