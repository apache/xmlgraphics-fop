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

import java.util.List;

import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
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

/**
 * LayoutManager for a table-header, table-footer and table body FO.
 * These fo objects have either rows or cells underneath.
 * Cells are organised into rows.
 */
public class Body extends BlockStackingLayoutManager {
    private TableBody fobj;
    
    private List columns;

    private int xoffset;
    private int yoffset;
    private int bodyHeight;

    //private Block curBlockArea;

    private List childBreaks = new java.util.ArrayList();

    /**
     * Create a new body layout manager.
     * @param node the table-body FO
     */
    public Body(TableBody node) {
        super(node);
        fobj = node;
    }

    /** @return the table-body|header|footer FO */
    public TableBody getFObj() {
        return this.fobj;
    }
    
    /**
     * Set the columns from the table.
     *
     * @param cols the list of columns used for this body
     */
    public void setColumns(List cols) {
        columns = cols;
    }

    /**
     * Breaks for this layout manager are of the form of before
     * or after a row and inside a row.
     *
     * @param context the layout context for finding the breaks
     * @return the next break possibility
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        Row curLM; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        BreakPoss lastPos = null;

        while ((curLM = (Row)getChildLM()) != null) {
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
     * Set the x offset of this body within the table.
     * This is used to set the row offsets.
     * @param off the x offset
     */
    public void setXOffset(int off) {
        xoffset = off;
    }

    /**
     * Set the y offset of this body within the table.
     * This is used to set the row offsets.
     *
     * @param off the y offset position
     */
    public void setYOffset(int off) {
        yoffset = off;
    }

    /**
     * Add the areas for the break points.
     * This sets the offset of each row as it is added.
     *
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);

        Row childLM;
        int iStartPos = 0;
        LayoutContext lc = new LayoutContext(0);
        int rowoffset = 0;
        while (parentIter.hasNext()) {
            LeafPosition lfp = (LeafPosition) parentIter.next();
            // Add the block areas to Area
            PositionIterator breakPosIter 
                = new BreakPossPosIter(childBreaks, iStartPos,
                                   lfp.getLeafPos() + 1);
            iStartPos = lfp.getLeafPos() + 1;
            int lastheight = 0;
            while ((childLM = (Row)breakPosIter.getNextChildLM()) != null) {
                childLM.setXOffset(xoffset);
                childLM.setYOffset(yoffset + rowoffset);
                childLM.addAreas(breakPosIter, lc);
                lastheight = childLM.getRowHeight();
            }
            rowoffset += lastheight;
        }
        bodyHeight = rowoffset;

        flush();

        childBreaks.clear();
        //curBlockArea = null;
    }

    /**
     * Get the body height of the body after adjusting.
     * Should only be called after adding the body areas.
     *
     * @return the body height of this body
     */
    public int getBodyHeight() {
        return bodyHeight;
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
     * @return the parent are of the child
     */
    public Area getParentArea(Area childArea) {
        return parentLM.getParentArea(childArea);
    }

    /**
     * Add the child area.
     * The table-header, table-footer and table-body areas return
     * the areas return by the children.
     *
     * @param childArea the child area to add
     */
    public void addChild(Area childArea) {
        parentLM.addChild(childArea);
    }

    /**
     * Reset the position of the layout manager.
     *
     * @param resetPos the position to reset to
     */
    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
        }
    }

    /**
     * Create a body area.
     * This area has the background and width set.
     *
     * @return the new body area
     */
    public Area createColumnArea() {
        Area curBlockArea = new Block();

        TraitSetter.addBackground(curBlockArea, fobj.getCommonBorderPaddingBackground());
        return curBlockArea;
    }

}

