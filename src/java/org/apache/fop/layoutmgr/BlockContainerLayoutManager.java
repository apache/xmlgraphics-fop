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

package org.apache.fop.layoutmgr;

import java.util.List;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.apache.fop.area.Area;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.Block;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.area.CTM;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for a block FO.
 */
public class BlockContainerLayoutManager extends BlockStackingLayoutManager {
    private BlockContainer fobj;
    
    private BlockViewport viewportBlockArea;
    private Block curBlockArea;

    private List childBreaks = new java.util.ArrayList();

    private CommonAbsolutePosition abProps;
    private FODimension relDims;
    private CTM absoluteCTM;
    private boolean clip = false;
    private Length width;
    private Length height;
    private int vpContentIPD;
    private int vpContentBPD;

    // When viewport should grow with the content.
    private boolean autoHeight = true; 

    private int referenceIPD;
    
    /**
     * Create a new block container layout manager.
     * @param node block-container node to create the layout manager for.
     */
    public BlockContainerLayoutManager(BlockContainer node) {
        super(node);
        fobj = node;
    }
    
    /**
     * @return the currently applicable page viewport
     */
    protected PageViewport getPageViewport() {
        LayoutManager lm = this;
        while (lm != null && !(lm instanceof PageSequenceLayoutManager)) {
            lm = lm.getParent();
        }
        if (lm == null) {
            return null;
        } else {
            return ((PageSequenceLayoutManager)lm).getCurrentPageViewport();
        }
    }

    /**
     * @see org.apache.fop.layoutmgr.AbstractLayoutManager#initProperties()
     */
    protected void initProperties() {
        abProps = fobj.getCommonAbsolutePosition();
 
        boolean rotated = (fobj.getReferenceOrientation() % 180 != 0);
        if (rotated) {
            height = fobj.getInlineProgressionDimension().getOptimum().getLength();
            width = fobj.getBlockProgressionDimension().getOptimum().getLength();
        } else {
            height = fobj.getBlockProgressionDimension().getOptimum().getLength();
            width = fobj.getInlineProgressionDimension().getOptimum().getLength();
        }
    }

    protected int getRotatedIPD() {
        return fobj.getInlineProgressionDimension().getOptimum().getLength().getValue();
    }

    private int getSpaceBefore() {
        return fobj.getCommonMarginBlock().spaceBefore
                .getOptimum().getLength().getValue();
    }
    
    private int getBPIndents() {
        int indents = 0;
        indents += fobj.getCommonMarginBlock().spaceBefore.getOptimum().getLength().getValue();
        indents += fobj.getCommonMarginBlock().spaceAfter.getOptimum().getLength().getValue();
        indents += fobj.getCommonBorderPaddingBackground().getBPPaddingAndBorder(false);
        return indents;
    }
    
    private int getIPIndents() {
        int iIndents = 0;
        iIndents += fobj.getCommonMarginBlock().startIndent.getValue();
        iIndents += fobj.getCommonMarginBlock().endIndent.getValue();
        return iIndents;
    }
    
    private boolean isAbsoluteOrFixed() {
        return (abProps.absolutePosition == EN_ABSOLUTE) 
                || (abProps.absolutePosition == EN_FIXED);
    }

    private boolean isFixed() {
        return (abProps.absolutePosition == EN_FIXED);
    }
    
    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getNextBreakPoss(org.apache.fop.layoutmgr.LayoutContext)
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {

        if (isAbsoluteOrFixed()) {
            return getAbsoluteBreakPoss(context);
        }

        boolean rotated = (fobj.getReferenceOrientation() % 180 != 0); //vals[0] == 0.0;
        referenceIPD = context.getRefIPD();
        int maxbpd = context.getStackLimit().opt;
        int allocBPD, allocIPD;
        if (height.getEnum() != EN_AUTO) {
            allocBPD = height.getValue(); //this is the content-height
            allocBPD += getBPIndents();
        } else {
            allocBPD = maxbpd;
        }
        if (width.getEnum() != EN_AUTO) {
            allocIPD = width.getValue(); //this is the content-width
            allocIPD += getIPIndents();
        } else {
            allocIPD = referenceIPD;
        }

        vpContentBPD = allocBPD - getBPIndents();
        vpContentIPD = allocIPD - getIPIndents();
        
        double contentRectOffsetX = 0;
        contentRectOffsetX += fobj.getCommonMarginBlock().startIndent.getValue();
        double contentRectOffsetY = 0;
        //contentRectOffsetY += fobj.getCommonMarginBlock().startIndent.getValue();
        contentRectOffsetY += getSpaceBefore();
        contentRectOffsetY += fobj.getCommonBorderPaddingBackground().getBorderBeforeWidth(false);
        contentRectOffsetY += fobj.getCommonBorderPaddingBackground().getPaddingBefore(false);
        
        Rectangle2D rect = new Rectangle2D.Double(
                contentRectOffsetX, contentRectOffsetY, 
                vpContentIPD, vpContentBPD);
        relDims = new FODimension(0, 0);
        absoluteCTM = CTM.getCTMandRelDims(fobj.getReferenceOrientation(),
                fobj.getWritingMode(), rect, relDims);
        double[] vals = absoluteCTM.toArray();

        MinOptMax stackLimit;
        if (rotated) {
            // rotated 90 degrees
            /*
            if (relDims.ipd > context.getRefIPD()) {
                relDims.ipd = context.getRefIPD();
            }*/
            //stackLimit = new MinOptMax(relDims.ipd);
            /*
            if (width.getEnum() == EN_AUTO) {
                relDims.bpd = context.getStackLimit().opt;
            }
            absoluteCTM = new CTM(vals[0], vals[1], vals[2], vals[3], 0, 0);
            */
            //absoluteCTM = new CTM(vals[0], vals[1], vals[2], vals[3], vals[5], vals[4]);
        } else {
            /*
            if (vals[0] == -1.0) {
                absoluteCTM = new CTM(vals[0], vals[1], vals[2], vals[3], 0, 0);
            }*/
            //stackLimit = context.getStackLimit();
        }
        stackLimit = new MinOptMax(relDims.bpd);

        LayoutManager curLM; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        // if starting add space before
        // stackSize.add(spaceBefore);
        BreakPoss lastPos = null;

        //TODO fix layout dimensions!
        fobj.setLayoutDimension(PercentBase.BLOCK_IPD, allocIPD);
        fobj.setLayoutDimension(PercentBase.BLOCK_BPD, allocBPD);
        fobj.setLayoutDimension(PercentBase.REFERENCE_AREA_IPD, relDims.ipd);
        fobj.setLayoutDimension(PercentBase.REFERENCE_AREA_BPD, relDims.bpd);

        while ((curLM = getChildLM()) != null) {
            // Make break positions and return blocks!
            // Set up a LayoutContext
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
                childLC.setStackLimit(
                  MinOptMax.subtract(stackLimit,
                                     stackSize));
                childLC.setRefIPD(relDims.ipd);

            boolean over = false;
            while (!curLM.isFinished()) {
                if ((bp = curLM.getNextBreakPoss(childLC)) != null) {
                    stackSize.add(bp.getStackingSize());
                    if (stackSize.opt > stackLimit.max) {
                        // reset to last break
                        if (lastPos != null) {
                            reset(lastPos.getPosition());
                        } else {
                            curLM.resetPosition(null);
                        }
                        over = true;
                        break;
                    }
                    lastPos = bp;
                    childBreaks.add(bp);

                    if (bp.nextBreakOverflows()) {
                        over = true;
                        break;
                    }                    
                    
                    childLC.setStackLimit(MinOptMax.subtract(
                                           stackLimit, stackSize));
                }
            }
            if (!rotated) {
                BreakPoss breakPoss;
                breakPoss = new BreakPoss(new LeafPosition(this,
                                                   childBreaks.size() - 1));
                breakPoss.setStackingSize(stackSize);
                if (over) {
                    breakPoss.setFlag(BreakPoss.NEXT_OVERFLOWS, true);
                }
                return breakPoss;
            }
        }
        setFinished(true);
        if (rotated) {
            BreakPoss breakPoss;
            breakPoss = new BreakPoss(new LeafPosition(this,
                                               childBreaks.size() - 1));
            breakPoss.setStackingSize(new MinOptMax(relDims.ipd));
            return breakPoss;
        }
        return null;
    }

    private Point getAbsOffset() {
        int x = 0;
        int y = 0;
        if (abProps.left.getEnum() != EN_AUTO) {
            x = abProps.left.getValue();
        }
        if (abProps.top.getEnum() != EN_AUTO) {
            y = abProps.top.getValue();
        }
        return new Point(x, y);
    }
    
    /**
     * Generate and return the next break possibility for absolutely positioned
     * block-containers.
     * @param context LayoutContext to work with
     * @return the next break position
     * @see org.apache.fop.layoutmgr.LayoutManager#getNextBreakPoss(org.apache.fop.layoutmgr.LayoutContext)
     */
    public BreakPoss getAbsoluteBreakPoss(LayoutContext context) {

        LayoutManager curLM ; // currently active LM

        MinOptMax stackSize = new MinOptMax();

        Point offset = getAbsOffset();
        int allocBPD, allocIPD;
        if (height.getEnum() != EN_AUTO) {
            allocBPD = height.getValue(); //this is the content-height
            allocBPD += getBPIndents();
        } else {
            allocBPD = 0;
            if (abProps.bottom.getEnum() != EN_AUTO) {
                if (isFixed()) {
                    allocBPD = (int)getPageViewport().getViewArea().getHeight();
                } else {
                    allocBPD = context.getStackLimit().opt; 
                }
                allocBPD -= offset.y;
                if (abProps.bottom.getEnum() != EN_AUTO) {
                    allocBPD -= abProps.bottom.getValue();
                }
            }
        }
        if (width.getEnum() != EN_AUTO) {
            allocIPD = width.getValue(); //this is the content-width
            allocIPD += getIPIndents();
        } else {
            if (isFixed()) {
                allocIPD = (int)getPageViewport().getViewArea().getWidth(); 
            } else {
                allocIPD = context.getRefIPD();
            }
            if (abProps.left.getEnum() != EN_AUTO) {
                allocIPD -= abProps.left.getValue();
            }
            if (abProps.right.getEnum() != EN_AUTO) {
                allocIPD -= abProps.right.getValue();
            }
        }

        vpContentBPD = allocBPD - getBPIndents();
        vpContentIPD = allocIPD - getIPIndents();
        
        double contentRectOffsetX = offset.getX();
        contentRectOffsetX += fobj.getCommonMarginBlock().startIndent.getValue();
        double contentRectOffsetY = offset.getY();
        contentRectOffsetY += getSpaceBefore();
        contentRectOffsetY += fobj.getCommonBorderPaddingBackground().getBorderBeforeWidth(false);
        contentRectOffsetY += fobj.getCommonBorderPaddingBackground().getPaddingBefore(false);
        
        Rectangle2D rect = new Rectangle2D.Double(
                contentRectOffsetX, contentRectOffsetY, 
                vpContentIPD, vpContentBPD);
        relDims = new FODimension(0, 0);
        absoluteCTM = CTM.getCTMandRelDims(
                fobj.getReferenceOrientation(),
                fobj.getWritingMode(), 
                rect, relDims);
        //referenceIPD = relDims.ipd + getIPIndents();

        
        while ((curLM = getChildLM()) != null) {
            // Make break positions and return blocks!
            // Set up a LayoutContext
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(new MinOptMax(1000000));
            childLC.setRefIPD(relDims.ipd);

            while (!curLM.isFinished()) {
                if ((bp = curLM.getNextBreakPoss(childLC)) != null) {
                    stackSize.add(bp.getStackingSize());
                    childBreaks.add(bp);
                }
            }
        }
        setFinished(true);
        BreakPoss breakPoss = new BreakPoss(
                                new LeafPosition(this, childBreaks.size() - 1));
        // absolutely positioned areas do not contribute
        // to the normal stacking
        breakPoss.setStackingSize(new MinOptMax(0));

        if (stackSize.opt > relDims.bpd) {
            log.warn("Contents overflow block-container viewport: clipping");
            if (fobj.getOverflow() == EN_HIDDEN) {
                clip = true;
            } else if (fobj.getOverflow() == EN_ERROR_IF_OVERFLOW) {
                //TODO Throw layout exception
                clip = true;
            }
        }

        return breakPoss;
    }

    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);

        addID(fobj.getId());
        addMarkers(true, true);

        LayoutManager childLM;
        int iStartPos = 0;
        LayoutContext lc = new LayoutContext(0);
        while (parentIter.hasNext()) {
            LeafPosition lfp = (LeafPosition) parentIter.next();
            // Add the block areas to Area
            PositionIterator breakPosIter =
              new BreakPossPosIter(childBreaks, iStartPos,
                                   lfp.getLeafPos() + 1);
            iStartPos = lfp.getLeafPos() + 1;
            while ((childLM = breakPosIter.getNextChildLM()) != null) {
                childLM.addAreas(breakPosIter, lc);
            }
        }

        flush();
        addMarkers(true, true);

        childBreaks.clear();
        viewportBlockArea = null;
        curBlockArea = null;
    }

    /**
     * Get the parent area for children of this block container.
     * This returns the current block container area
     * and creates it if required.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager#getParentArea(Area)
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            viewportBlockArea = new BlockViewport();
            viewportBlockArea.addTrait(Trait.IS_VIEWPORT_AREA, Boolean.TRUE);
            TraitSetter.addBorders(viewportBlockArea, fobj.getCommonBorderPaddingBackground());
            TraitSetter.addBackground(viewportBlockArea, fobj.getCommonBorderPaddingBackground());
            TraitSetter.addMargins(viewportBlockArea, null, 
                    fobj.getCommonBorderPaddingBackground(),
                    fobj.getCommonMarginBlock());
            
            viewportBlockArea.setCTM(absoluteCTM);
            viewportBlockArea.setIPD(vpContentIPD);
            viewportBlockArea.setBPD(vpContentBPD);
            viewportBlockArea.setClip(clip);
            viewportBlockArea.addTrait(Trait.SPACE_BEFORE, new Integer(getSpaceBefore()));

            if (abProps.absolutePosition == EN_ABSOLUTE 
                    || abProps.absolutePosition == EN_FIXED) {
                Point offset = getAbsOffset();
                viewportBlockArea.setXOffset(offset.x);
                viewportBlockArea.setYOffset(offset.y);
                autoHeight = false;
            } else {
                //double[] vals = absoluteCTM.toArray();
                boolean rotated = (fobj.getReferenceOrientation() % 180 != 0); //vals[0] == 0.0;
                if (rotated) {
                    autoHeight = false;
                } else {
                    autoHeight = (height.getEnum() == EN_AUTO);
                    if (autoHeight) {
                        viewportBlockArea.setBPD(0);
                    }
                }
                /*
                boolean rotated = vals[0] == 0.0;
                if (rotated) {
                    viewportBlockArea.setIPD(vpContentIPD);
                    viewportBlockArea.setBPD(vpContentBPD);
                    viewportBlockArea.setClip(clip);
                    autoHeight = false;
                } else if (vals[0] == -1.0) {
                    // need to set bpd to actual size for rotation
                    // and stacking
                    viewportBlockArea.setIPD(relDims.ipd);
                    if (height.getEnum() != EN_AUTO) {
                        viewportBlockArea.setBPD(relDims.bpd);
                        autoHeight = false;
                    }
                    viewportBlockArea.setClip(clip);
                } else {
                    viewportBlockArea.setIPD(relDims.ipd);
                    if (height.getEnum() != EN_AUTO) {
                        viewportBlockArea.setBPD(relDims.bpd);
                        autoHeight = false;
                    }
                }*/
            }

            curBlockArea = new Block();
            curBlockArea.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);

            if (abProps.absolutePosition == EN_ABSOLUTE) {
                viewportBlockArea.setPositioning(Block.ABSOLUTE);
            } else if (abProps.absolutePosition == EN_FIXED) {
                viewportBlockArea.setPositioning(Block.FIXED);
            }

            // Set up dimensions
            // Must get dimensions from parent area
            Area parentArea = parentLM.getParentArea(curBlockArea);
            //int referenceIPD = parentArea.getIPD();
            curBlockArea.setIPD(relDims.ipd);
            // Get reference IPD from parentArea
            setCurrentArea(viewportBlockArea); // ??? for generic operations
        }
        return curBlockArea;
    }

    /**
     * Add the child to the block container.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager#addChild(Area)
     */
    public void addChild(Area childArea) {
        if (curBlockArea != null) {
            curBlockArea.addBlock((Block) childArea);
        }
    }

    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
        }
    }

    /*
     * Force current area to be added to parent area.
     */
    protected void flush() {
        viewportBlockArea.addBlock(curBlockArea, autoHeight);
        
        // Fake a 0 height for absolute positioned blocks.
        int height = viewportBlockArea.getBPD();
        if (viewportBlockArea.getPositioning() == Block.ABSOLUTE) {
            viewportBlockArea.setBPD(0);
        }
        super.flush();
        // Restore the right height.
        if (viewportBlockArea.getPositioning() == Block.ABSOLUTE) {
            viewportBlockArea.setBPD(height);
        }
    }
    
}

