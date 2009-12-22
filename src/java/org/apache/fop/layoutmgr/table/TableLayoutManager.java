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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.ConditionalElementListener;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.RelSide;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.util.BreakUtil;

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

    /** See {@link TableLayoutManager#registerColumnBackgroundArea(TableColumn, Block, int)}. */
    private List columnBackgroundAreas;

    /**
     * Temporary holder of column background informations for a table-cell's area.
     *
     * @see TableLayoutManager#registerColumnBackgroundArea(TableColumn, Block, int)
     */
    private static final class ColumnBackgroundInfo {
        private TableColumn column;
        private Block backgroundArea;
        private int xShift;

        private ColumnBackgroundInfo(TableColumn column, Block backgroundArea, int xShift) {
            this.column = column;
            this.backgroundArea = backgroundArea;
            this.xShift = xShift;
        }
    }

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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public List getNextKnuthElements(LayoutContext context, int alignment) {

        List returnList = new LinkedList();

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
                BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                        getTable().getUserAgent().getEventBroadcaster());
                eventProducer.tableFixedAutoWidthNotSupported(this, getTable().getLocator());
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
            BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                    getTable().getUserAgent().getEventBroadcaster());
            eventProducer.objectTooWide(this, getTable().getName(),
                    getContentAreaIPD(), context.getRefIPD(),
                    getTable().getLocator());
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
        LinkedList contentKnuthElements;
        contentLM = new TableContentLayoutManager(this);
        LayoutContext childLC = new LayoutContext(0);
        /*
        childLC.setStackLimit(
              MinOptMax.subtract(context.getStackLimit(),
                                 stackSize));*/
        childLC.setRefIPD(context.getRefIPD());
        childLC.copyPendingMarksFrom(context);

        contentKnuthElements = contentLM.getNextKnuthElements(childLC, alignment);
        //Set index values on elements coming from the content LM
        Iterator iter = contentKnuthElements.iterator();
        while (iter.hasNext()) {
            ListElement el = (ListElement)iter.next();
            notifyPos(el.getPosition());
        }
        log.debug(contentKnuthElements);
        wrapPositionElements(contentKnuthElements, returnList);

        context.updateKeepWithPreviousPending(getKeepWithPrevious());
        context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());

        context.updateKeepWithNextPending(getKeepWithNext());
        context.updateKeepWithNextPending(childLC.getKeepWithNextPending());

        if (getTable().isSeparateBorderModel()) {
            addKnuthElementsForBorderPaddingAfter(returnList, true);
        }
        addKnuthElementsForSpaceAfter(returnList, alignment);

        if (!context.suppressBreakBefore()) {
            //addKnuthElementsForBreakBefore(returnList, context);
            int breakBefore = BreakUtil.compareBreakClasses(getTable().getBreakBefore(),
                    childLC.getBreakBefore());
            if (breakBefore != Constants.EN_AUTO) {
                returnList.add(0, new BreakElement(getAuxiliaryPosition(), 0,
                        -KnuthElement.INFINITE, breakBefore, context));
            }
        }

        //addKnuthElementsForBreakAfter(returnList, context);
        int breakAfter = BreakUtil.compareBreakClasses(getTable().getBreakAfter(),
                childLC.getBreakAfter());
        if (breakAfter != Constants.EN_AUTO) {
            returnList.add(new BreakElement(getAuxiliaryPosition(),
                    0, -KnuthElement.INFINITE, breakAfter, context));
        }

        setFinished(true);
        resetSpaces();
        return returnList;
    }

    /**
     * Registers the given area, that will be used to render the part of column background
     * covered by a table-cell. If percentages are used to place the background image, the
     * final bpd of the (fraction of) table that will be rendered on the current page must
     * be known. The traits can't then be set when the areas for the cell are created
     * since at that moment this bpd is yet unknown. So they will instead be set in
     * TableLM's {@link #addAreas(PositionIterator, LayoutContext)} method.
     *
     * @param column the table-column element from which the cell gets background
     * informations
     * @param backgroundArea the block of the cell's dimensions that will hold the column
     * background
     * @param xShift additional amount by which the image must be shifted to be correctly
     * placed (to counterbalance the cell's start border)
     */
    void registerColumnBackgroundArea(TableColumn column, Block backgroundArea, int xShift) {
        addBackgroundArea(backgroundArea);
        if (columnBackgroundAreas == null) {
            columnBackgroundAreas = new ArrayList();
        }
        columnBackgroundAreas.add(new ColumnBackgroundInfo(column, backgroundArea, xShift));
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
        addId();

        // add space before, in order to implement display-align = "center" or "after"
        if (layoutContext.getSpaceBefore() != 0) {
            addBlockSpacing(0.0, MinOptMax.getInstance(layoutContext.getSpaceBefore()));
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

        if (columnBackgroundAreas != null) {
            for (Iterator iter = columnBackgroundAreas.iterator(); iter.hasNext();) {
                ColumnBackgroundInfo b = (ColumnBackgroundInfo) iter.next();
                TraitSetter.addBackground(b.backgroundArea,
                        b.column.getCommonBorderPaddingBackground(), this,
                        b.xShift, -b.backgroundArea.getYOffset(),
                        b.column.getColumnWidth().getValue(this), tableHeight);
            }
            columnBackgroundAreas.clear();
        }

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

        notifyEndOfLayout();
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
            /*Area parentArea =*/ parentLayoutManager.getParentArea(curBlockArea);

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
     * Adds the given area to this layout manager's area, without updating the used bpd.
     *
     * @param background an area
     */
    void addBackgroundArea(Block background) {
        curBlockArea.addChildArea(background);
    }

    /** {@inheritDoc} */
    public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
        // TODO Auto-generated method stub
        return 0;
    }

    /** {@inheritDoc} */
    public void discardSpace(KnuthGlue spaceGlue) {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public KeepProperty getKeepTogetherProperty() {
        return getTable().getKeepTogether();
    }

    /** {@inheritDoc} */
    public KeepProperty getKeepWithPreviousProperty() {
        return getTable().getKeepWithPrevious();
    }

    /** {@inheritDoc} */
    public KeepProperty getKeepWithNextProperty() {
        return getTable().getKeepWithNext();
    }

    // --------- Property Resolution related functions --------- //

    /**
     * {@inheritDoc}
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
