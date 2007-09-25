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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.ConditionalElementListener;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.RelSide;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

import java.util.Iterator;
import java.util.LinkedList;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;

/**
 * LayoutManager for a table FO.
 * A table consists of columns, table header, table footer and multiple
 * table bodies.
 * The header, footer and body add the areas created from the table cells.
 * The table then creates areas for the columns, bodies and rows
 * the render background.
 */
public class TableLayoutManager extends BlockStackingLayoutManager 
                implements ConditionalElementListener {

    /**
     * logging instance
     */
    private static Log log = LogFactory.getLog(TableLayoutManager.class);
    
    private TableContentLayoutManager contentLM; 
    private ColumnSetup columns = null;

    private Block curBlockArea;

    private double tableUnit;
    private boolean autoLayout = true;

    private boolean discardBorderBefore;
    private boolean discardBorderAfter;
    private boolean discardPaddingBefore;
    private boolean discardPaddingAfter;
    private MinOptMax effSpaceBefore;
    private MinOptMax effSpaceAfter;
    
    private int halfBorderSeparationBPD;
    private int halfBorderSeparationIPD;
    
    /**
     * Create a new table layout manager.
     * @param node the table FO
     */
    public TableLayoutManager(Table node) {
        super(node);
        this.columns = new ColumnSetup(node);
    }

    /** @return the table FO */
    public Table getTable() {
        return (Table)this.fobj;
    }
    
    /**
     * @return the column setup for this table.
     */
    public ColumnSetup getColumns() {
        return this.columns;
    }
    
    /** @see org.apache.fop.layoutmgr.LayoutManager#initialize() */
    public void initialize() {
        foSpaceBefore = new SpaceVal(
                getTable().getCommonMarginBlock().spaceBefore, this).getSpace();
        foSpaceAfter = new SpaceVal(
                getTable().getCommonMarginBlock().spaceAfter, this).getSpace();
        startIndent = getTable().getCommonMarginBlock().startIndent.getValue(this);
        endIndent = getTable().getCommonMarginBlock().endIndent.getValue(this); 
        
        if (getTable().isSeparateBorderModel()) {
            this.halfBorderSeparationBPD = getTable().getBorderSeparation().getBPD().getLength()
                    .getValue(this) / 2;
            this.halfBorderSeparationIPD = getTable().getBorderSeparation().getIPD().getLength()
                    .getValue(this) / 2;
        } else {
            this.halfBorderSeparationBPD = 0;
            this.halfBorderSeparationIPD = 0;
        }
        
        if (!getTable().isAutoLayout() 
                && getTable().getInlineProgressionDimension().getOptimum(this).getEnum() 
                    != EN_AUTO) {
            autoLayout = false;
        }
    }

    private void resetSpaces() {
        this.discardBorderBefore = false;        
        this.discardBorderAfter = false;        
        this.discardPaddingBefore = false;        
        this.discardPaddingAfter = false;
        this.effSpaceBefore = null;
        this.effSpaceAfter = null;
    }
    
    /**
     * @return half the value of border-separation.block-progression-dimension, or 0 if
     * border-collapse="collapse".
     */
    public int getHalfBorderSeparationBPD() {
        return halfBorderSeparationBPD;
    }

    /**
     * @return half the value of border-separation.inline-progression-dimension, or 0 if
     * border-collapse="collapse".
     */
    public int getHalfBorderSeparationIPD() {
        return halfBorderSeparationIPD;
    }
    
    /**
     * Handles the Knuth elements at the table level: mainly breaks, spaces and borders
     * before and after the table. The Knuth elements for the table cells are handled by
     * TableContentLayoutManager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     * @see TableContentLayoutManager#getNextKnuthElements(LayoutContext, int)
     */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        
        LinkedList returnList = new LinkedList();
        
        if (!breakBeforeServed) {
            breakBeforeServed = true;
            if (addKnuthElementsForBreakBefore(returnList, context)) {
                return returnList;
            }
        }

        /*
         * Compute the IPD and adjust it if necessary (overconstrained)
         */
        referenceIPD = context.getRefIPD();
        if (getTable().getInlineProgressionDimension().getOptimum(this).getEnum() != EN_AUTO) {
            int contentIPD = getTable().getInlineProgressionDimension().getOptimum(this)
                    .getLength().getValue(this);
            updateContentAreaIPDwithOverconstrainedAdjust(contentIPD);
        } else {
            if (!getTable().isAutoLayout()) {
                log.info("table-layout=\"fixed\" and width=\"auto\", "
                        + "but auto-layout not supported " 
                        + "=> assuming width=\"100%\"");
            }
            updateContentAreaIPDwithOverconstrainedAdjust();
        }
        int sumOfColumns = columns.getSumOfColumnWidths(this);
        if (!autoLayout && sumOfColumns > getContentAreaIPD()) {
            log.debug(FONode.decorateWithContextInfo(
                    "The sum of all column widths is larger than the specified table width.", 
                    getTable()));
            updateContentAreaIPDwithOverconstrainedAdjust(sumOfColumns);
        }
        int availableIPD = referenceIPD - getIPIndents();
        if (getContentAreaIPD() > availableIPD) {
            log.warn(FONode.decorateWithContextInfo(
                    "The extent in inline-progression-direction (width) of a table is"
                    + " bigger than the available space (" 
                    + getContentAreaIPD() + "mpt > " + context.getRefIPD() + "mpt)", 
                    getTable()));
        }
        
        /* initialize unit to determine computed values
         * for proportional-column-width()
         */
        if (tableUnit == 0.0) {
            this.tableUnit = columns.computeTableUnit(this);
        }

        if (!firstVisibleMarkServed) {
            addKnuthElementsForSpaceBefore(returnList, alignment);
        }
        
        if (getTable().isSeparateBorderModel()) {
            addKnuthElementsForBorderPaddingBefore(returnList, !firstVisibleMarkServed);
            firstVisibleMarkServed = true;
            // Border and padding to be repeated at each break
            // This must be done only in the separate-border model, as in collapsing
            // tables have no padding and borders are determined at the cell level
            addPendingMarks(context);
        }


        // Elements for the table-header/footer/body
        LinkedList contentKnuthElements = null;
        LinkedList contentList = new LinkedList();
        //Position returnPosition = new NonLeafPosition(this, null);
        //Body prevLM = null;

        LayoutContext childLC = new LayoutContext(0);
        /*
        childLC.setStackLimit(
              MinOptMax.subtract(context.getStackLimit(),
                                 stackSize));*/
        childLC.setRefIPD(context.getRefIPD());
        childLC.copyPendingMarksFrom(context);

        if (contentLM == null) {
            contentLM = new TableContentLayoutManager(this);
        }
        contentKnuthElements = contentLM.getNextKnuthElements(childLC, alignment);
        if (childLC.isKeepWithNextPending()) {
            log.debug("TableContentLM signals pending keep-with-next");
            context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING);
        }
        if (childLC.isKeepWithPreviousPending()) {
            log.debug("TableContentLM signals pending keep-with-previous");
            context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
        }

        // Check if the table's content starts/ends with a forced break
        // TODO this is hacky and will need to be handled better eventually
        if (contentKnuthElements.size() > 0) {
            ListElement element = (ListElement)contentKnuthElements.getFirst();
            if (element.isForcedBreak()) {
                // The first row of the table(-body), or (the content of) one of its cells
                // has a forced break-before
                int breakBeforeTable = ((Table) fobj).getBreakBefore();
                if (breakBeforeTable == EN_PAGE
                        || breakBeforeTable == EN_COLUMN 
                        || breakBeforeTable == EN_EVEN_PAGE 
                        || breakBeforeTable == EN_ODD_PAGE) {
                    // There is already a forced break before the table; remove this one
                    // to prevent a double break
                    contentKnuthElements.removeFirst();
                } else {
                    element.setPosition(new NonLeafPosition(this, null));
                }
            }
            element = (ListElement)contentKnuthElements.getLast();
            if (element.isForcedBreak()) {
                // The last row of the table(-body), or (the content of) one of its cells
                // has a forced break-after
                element.setPosition(new NonLeafPosition(this, null));
            }
        }

        //Set index values on elements coming from the content LM
        Iterator iter = contentKnuthElements.iterator();
        while (iter.hasNext()) {
            ListElement el = (ListElement)iter.next();
            notifyPos(el.getPosition());
        }
        log.debug(contentKnuthElements);
        contentList.addAll(contentKnuthElements);
        wrapPositionElements(contentList, returnList);
        if (getTable().isSeparateBorderModel()) {
            addKnuthElementsForBorderPaddingAfter(returnList, true);
        }
        addKnuthElementsForSpaceAfter(returnList, alignment);
        addKnuthElementsForBreakAfter(returnList, context);
        if (mustKeepWithNext()) {
            context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING);
        }
        if (mustKeepWithPrevious()) {
            context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
        }
        setFinished(true);
        resetSpaces();
        return returnList;
    }
    
    /**
     * The table area is a reference area that contains areas for
     * columns, bodies, rows and the contents are in cells.
     *
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);
        getPSLM().addIDToPage(getTable().getId());

        // add space before, in order to implement display-align = "center" or "after"
        if (layoutContext.getSpaceBefore() != 0) {
            addBlockSpacing(0.0, new MinOptMax(layoutContext.getSpaceBefore()));
        }

        int startXOffset = getTable().getCommonMarginBlock().startIndent.getValue(this);
        
        // add column, body then row areas

        // BPD of the table, i.e., height of its content; table's borders and paddings not counted
        int tableHeight = 0;
        //Body childLM;
        LayoutContext lc = new LayoutContext(0);


        lc.setRefIPD(getContentAreaIPD());
        contentLM.setStartXOffset(startXOffset);
        contentLM.addAreas(parentIter, lc);
        tableHeight += contentLM.getUsedBPD();

        curBlockArea.setBPD(tableHeight);

        if (getTable().isSeparateBorderModel()) {
            TraitSetter.addBorders(curBlockArea, 
                    getTable().getCommonBorderPaddingBackground(), 
                    discardBorderBefore, discardBorderAfter, false, false, this);
            TraitSetter.addPadding(curBlockArea, 
                    getTable().getCommonBorderPaddingBackground(), 
                    discardPaddingBefore, discardPaddingAfter, false, false, this);
        }
        TraitSetter.addBackground(curBlockArea, 
                getTable().getCommonBorderPaddingBackground(),
                this);
        TraitSetter.addMargins(curBlockArea,
                getTable().getCommonBorderPaddingBackground(), 
                startIndent, endIndent,
                this);
        TraitSetter.addBreaks(curBlockArea, 
                getTable().getBreakBefore(), getTable().getBreakAfter());
        TraitSetter.addSpaceBeforeAfter(curBlockArea, layoutContext.getSpaceAdjust(), 
                effSpaceBefore, effSpaceAfter);

        flush();

        resetSpaces();
        curBlockArea = null;
        
        getPSLM().notifyEndOfLayout(((Table)getFObj()).getId());
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
     * @return the parent area of the child
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();
            // Set up dimensions
            // Must get dimensions from parent area
            /*Area parentArea =*/ parentLM.getParentArea(curBlockArea);
            
            TraitSetter.setProducerID(curBlockArea, getTable().getId());

            curBlockArea.setIPD(getContentAreaIPD());
            
            setCurrentArea(curBlockArea);
        }
        return curBlockArea;
    }

    /**
     * Add the child area to this layout manager.
     *
     * @param childArea the child area to add
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

    /** @see org.apache.fop.layoutmgr.BlockLevelLayoutManager */
    public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
        // TODO Auto-generated method stub
        return 0;
    }

    /** @see org.apache.fop.layoutmgr.BlockLevelLayoutManager */
    public void discardSpace(KnuthGlue spaceGlue) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepTogether()
     */
    public boolean mustKeepTogether() {
        //TODO Keeps will have to be more sophisticated sooner or later
        return super.mustKeepTogether() 
                || !getTable().getKeepTogether().getWithinPage().isAuto()
                || !getTable().getKeepTogether().getWithinColumn().isAuto();
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithPrevious()
     */
    public boolean mustKeepWithPrevious() {
        return !getTable().getKeepWithPrevious().getWithinPage().isAuto()
                || !getTable().getKeepWithPrevious().getWithinColumn().isAuto();
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithNext()
     */
    public boolean mustKeepWithNext() {
        return !getTable().getKeepWithNext().getWithinPage().isAuto()
                || !getTable().getKeepWithNext().getWithinColumn().isAuto();
    }

    // --------- Property Resolution related functions --------- //

    /**
     * @see org.apache.fop.datatypes.PercentBaseContext#getBaseLength(int, FObj)
     */
    public int getBaseLength(int lengthBase, FObj fobj) {
        // Special handler for TableColumn width specifications
        if (fobj instanceof TableColumn && fobj.getParent() == getFObj()) {
            switch (lengthBase) {
            case LengthBase.CONTAINING_BLOCK_WIDTH:
                return getContentAreaIPD();
            case LengthBase.TABLE_UNITS:
                return (int) this.tableUnit;
            default:
                log.error("Unknown base type for LengthBase.");
                return 0;
            }
        } else {
            switch (lengthBase) {
            case LengthBase.TABLE_UNITS:
                return (int) this.tableUnit;
            default:
                return super.getBaseLength(lengthBase, fobj);
            }
        }
    }
    
    /** @see org.apache.fop.layoutmgr.ConditionalElementListener */
    public void notifySpace(RelSide side, MinOptMax effectiveLength) {
        if (RelSide.BEFORE == side) {
            if (log.isDebugEnabled()) {
                log.debug(this + ": Space " + side + ", " 
                        + this.effSpaceBefore + "-> " + effectiveLength);
            }
            this.effSpaceBefore = effectiveLength;
        } else {
            if (log.isDebugEnabled()) {
                log.debug(this + ": Space " + side + ", " 
                        + this.effSpaceAfter + "-> " + effectiveLength);
            }
            this.effSpaceAfter = effectiveLength;
        }
    }

    /** @see org.apache.fop.layoutmgr.ConditionalElementListener */
    public void notifyBorder(RelSide side, MinOptMax effectiveLength) {
        if (effectiveLength == null) {
            if (RelSide.BEFORE == side) {
                this.discardBorderBefore = true;
            } else {
                this.discardBorderAfter = true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(this + ": Border " + side + " -> " + effectiveLength);
        }
    }

    /** @see org.apache.fop.layoutmgr.ConditionalElementListener */
    public void notifyPadding(RelSide side, MinOptMax effectiveLength) {
        if (effectiveLength == null) {
            if (RelSide.BEFORE == side) {
                this.discardPaddingBefore = true;
            } else {
                this.discardPaddingAfter = true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(this + ": Padding " + side + " -> " + effectiveLength);
        }
    }

}
