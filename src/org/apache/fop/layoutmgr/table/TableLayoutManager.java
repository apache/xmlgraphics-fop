/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr.table;

import org.apache.fop.fo.PropertyManager;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.BreakPoss;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.BreakPossPosIter;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.fo.FObj;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;

import java.util.ArrayList;
import java.util.List;

/**
 * LayoutManager for a table FO.
 * A table consists of columns, table header, table footer and multiple
 * table bodies.
 * The header, footer and body add the areas created from the table cells.
 * The table then creates areas for the columns, bodies and rows
 * the render background.
 */
public class TableLayoutManager extends BlockStackingLayoutManager {
    private List columns = null;
    private Body tableHeader = null;
    private Body tableFooter = null;

    private BorderAndPadding borderProps = null;
    private BackgroundProps backgroundProps;

    private Block curBlockArea;

    private List bodyBreaks = new ArrayList();
    private BreakPoss headerBreak;
    private BreakPoss footerBreak;

    private class SectionPosition extends LeafPosition {
        protected List list;
        protected SectionPosition(LayoutManager lm, int pos, List l) {
            super(lm, pos);
            list = l;
        }
    }

    /**
     * Create a new table layout manager.
     *
     * @param fobj the table formatting object
     */
    public TableLayoutManager(FObj fobj) {
        super(fobj);
    }

    protected void initProperties(PropertyManager propMgr) {
        borderProps = propMgr.getBorderAndPadding();
        backgroundProps = propMgr.getBackgroundProps();
    }

    /**
     * Set the columns for this table.
     *
     * @param cols the list of column layout managers
     */
    public void setColumns(List cols) {
        columns = cols;
    }

    /**
     * Set the table header.
     *
     * @param th the table header layout manager
     */
    public void setTableHeader(Body th) {
        tableHeader = th;
        tableHeader.setParentLM(this);
    }

    /**
     * Set the table footer.
     *
     * @param tf the table footer layout manager
     */
    public void setTableFooter(Body tf) {
        tableFooter = tf;
        tableFooter.setParentLM(this);
    }

    /**
     * Get the next break possibility.
     * The break possibility depends on the height of the header and footer
     * and possible breaks inside the table body.
     *
     * @param context the layout context for finding breaks
     * @return the next break possibility
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        Body curLM; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        // if starting add space before
        // stackSize.add(spaceBefore);
        BreakPoss lastPos = null;

        MinOptMax headerSize = null;
        if (tableHeader != null) {
            tableHeader.resetPosition(null);
            headerBreak = getHeight(tableHeader, context);
            headerSize = headerBreak.getStackingSize();
            stackSize.add(headerSize);
        }

        MinOptMax footerSize = null;
        if (tableFooter != null) {
            tableFooter.resetPosition(null);
            footerBreak = getHeight(tableFooter, context);
            footerSize = footerBreak.getStackingSize();
            stackSize.add(footerSize);
        }

        if (stackSize.opt > context.getStackLimit().max) {
            BreakPoss breakPoss = new BreakPoss(
                                    new LeafPosition(this, 0));
            breakPoss.setFlag(BreakPoss.NEXT_OVERFLOWS, true);
            breakPoss.setStackingSize(stackSize);
            return breakPoss;
        }

        while ((curLM = (Body)getChildLM()) != null) {
            // Make break positions
            // Set up a LayoutContext
            int ipd = context.getRefIPD();
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(
                  MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));
            childLC.setRefIPD(ipd);

            curLM.setColumns(columns);

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
                    bodyBreaks.add(bp);

                    if (bp.nextBreakOverflows()) {
                        over = true;
                        break;
                    }

                    childLC.setStackLimit(MinOptMax.subtract(
                                             context.getStackLimit(), stackSize));
                }
            }
            BreakPoss breakPoss = new BreakPoss(
                                    new LeafPosition(this, bodyBreaks.size() - 1));
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
     * Get the break possibility and height of the table header or footer.
     *
     * @param lm the header or footer layout manager
     * @param context the parent layout context
     * @return the break possibility containing the stacking size
     */
    protected BreakPoss getHeight(Body lm, LayoutContext context) {
        int ipd = context.getRefIPD();
        BreakPoss bp;

        MinOptMax stackSize = new MinOptMax();

        LayoutContext childLC = new LayoutContext(0);
        childLC.setStackLimit(context.getStackLimit());
        childLC.setRefIPD(ipd);

        lm.setColumns(columns);

        List breaks = new ArrayList();
        while (!lm.isFinished()) {
            if ((bp = lm.getNextBreakPoss(childLC)) != null) {
                stackSize.add(bp.getStackingSize());
                breaks.add(bp);
                childLC.setStackLimit(MinOptMax.subtract(
                                         context.getStackLimit(), stackSize));
            }
        }
        BreakPoss breakPoss = new BreakPoss(
                               new SectionPosition(this, breaks.size() - 1, breaks));
        breakPoss.setStackingSize(stackSize);
        return breakPoss;
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
        addID();

        // add column, body then row areas

        int tableHeight = 0;
        Body childLM;
        LayoutContext lc = new LayoutContext(0);

        // add table header areas
        if (headerBreak != null) {
            SectionPosition pos = (SectionPosition)headerBreak.getPosition();
            List list = pos.list;
            PositionIterator breakPosIter = new BreakPossPosIter(list, 0, list.size() + 1);
            while ((childLM = (Body)breakPosIter.getNextChildLM()) != null) {
                childLM.addAreas(breakPosIter, lc);
                tableHeight += childLM.getBodyHeight();
            }
        }

        int iStartPos = 0;
        while (parentIter.hasNext()) {
            LeafPosition lfp = (LeafPosition) parentIter.next();
            // Add the block areas to Area
            PositionIterator breakPosIter =
              new BreakPossPosIter(bodyBreaks, iStartPos,
                                   lfp.getLeafPos() + 1);
            iStartPos = lfp.getLeafPos() + 1;
            while ((childLM = (Body)breakPosIter.getNextChildLM()) != null) {
                childLM.setYOffset(tableHeight);
                childLM.addAreas(breakPosIter, lc);
                tableHeight += childLM.getBodyHeight();
            }
        }

        // add footer areas
        if (footerBreak != null) {
            SectionPosition pos = (SectionPosition)footerBreak.getPosition();
            List list = pos.list;
            PositionIterator breakPosIter = new BreakPossPosIter(list, 0, list.size() + 1);
            while ((childLM = (Body)breakPosIter.getNextChildLM()) != null) {
                childLM.setYOffset(tableHeight);
                childLM.addAreas(breakPosIter, lc);
                tableHeight += childLM.getBodyHeight();
            }
        }

        curBlockArea.setHeight(tableHeight);

        if(borderProps != null) { 
            addBorders(curBlockArea, borderProps);
        }
        if(backgroundProps != null) {
            addBackground(curBlockArea, backgroundProps);
        }

        flush();

        bodyBreaks.clear();
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
     * @param childArea the child area
     * @return the parent area of the child
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();
            // Set up dimensions
            // Must get dimensions from parent area
            Area parentArea = parentLM.getParentArea(curBlockArea);
            int referenceIPD = parentArea.getIPD();
            curBlockArea.setIPD(referenceIPD);
            // Get reference IPD from parentArea
            setCurrentArea(curBlockArea); // ??? for generic operations
        }
        return curBlockArea;
    }

    /**
     * Add the child area to this layout manager.
     *
     * @param childArea the child area to add
     * @return unused
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

