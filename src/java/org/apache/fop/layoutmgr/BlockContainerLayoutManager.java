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
import java.awt.geom.Rectangle2D;

import org.apache.fop.area.Area;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.Block;
import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.FixedLength;
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
        log.debug(fobj.getBlockProgressionDimension().getOptimum());
        log.debug(fobj.getInlineProgressionDimension().getOptimum());
        abProps = fobj.getCommonAbsolutePosition();
        log.debug(abProps);
        
        int iIndents = fobj.getCommonMarginBlock().startIndent.getValue();
        iIndents += fobj.getCommonMarginBlock().endIndent.getValue();
        int bIndents = fobj.getCommonBorderPaddingBackground().getBPPaddingAndBorder(false);

        
        if (abProps.absolutePosition == EN_ABSOLUTE && false) {
            Rectangle2D rect = new Rectangle2D.Double(abProps.left.getValue(),
                                abProps.top.getValue(), 
                                abProps.right.getValue() - abProps.left.getValue(),
                                abProps.bottom.getValue() - abProps.top.getValue());
            relDims = new FODimension(0, 0);
            absoluteCTM = CTM.getCTMandRelDims(fobj.getReferenceOrientation(),
                fobj.getWritingMode(), rect, relDims);
        } else if (abProps.absolutePosition == EN_FIXED
                || abProps.absolutePosition == EN_ABSOLUTE) {
            Rectangle2D viewArea = getPageViewport().getViewArea();
            double x = viewArea.getX() + abProps.left.getValue();
            double y = viewArea.getY() + abProps.top.getValue();
            double w = 0.0;
            if (abProps.right.getEnum() == Constants.EN_AUTO) {
                if (fobj.getWidth().getEnum() == Constants.EN_AUTO) {
                    w = viewArea.getWidth() - x;
                } else {
                    if (fobj.getReferenceOrientation() % 180 == 0) {
                        w = fobj.getInlineProgressionDimension().getOptimum().getLength().getValue();
                    } else {
                        w = fobj.getBlockProgressionDimension().getOptimum().getLength().getValue();
                    }
                }
            } else {
                w = viewArea.getWidth() - x - abProps.right.getValue();
            }
            double h = 0.0;
            if (abProps.bottom.getEnum() == Constants.EN_AUTO) {
                if (fobj.getHeight().getEnum() == Constants.EN_AUTO) {
                    h = viewArea.getHeight() - y;
                } else {
                    if (fobj.getReferenceOrientation() % 180 == 0) {
                        h = fobj.getBlockProgressionDimension().getOptimum().getLength().getValue();
                    } else {
                        h = fobj.getInlineProgressionDimension().getOptimum().getLength().getValue();
                    }
                }
            } else {
                h = viewArea.getHeight() - y - abProps.bottom.getValue();
            }
            log.debug("x=" + x + " y=" + y + " w=" + w + " h=" + h + " orient=" + fobj.getReferenceOrientation());
            if (w != 0) {
                this.width = new FixedLength((int)w);
            }
            if (h != 0) {
                this.height = new FixedLength((int)h);
            }
            Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
            relDims = new FODimension(0, 0);
            absoluteCTM = CTM.getCTMandRelDims(fobj.getReferenceOrientation(),
                fobj.getWritingMode(), rect, relDims);
        }
 
        if (height == null) {
            height = fobj.getBlockProgressionDimension().getOptimum().getLength();
        }
        if (width == null) {
            width = fobj.getInlineProgressionDimension().getOptimum().getLength();
        }
    }

    protected int getRotatedIPD() {
        return fobj.getInlineProgressionDimension().getOptimum().getLength().getValue();
    }

    private int getIPIndents() {
        int iIndents = 0;
        iIndents += fobj.getCommonMarginBlock().startIndent.getValue();
        iIndents += fobj.getCommonMarginBlock().endIndent.getValue();
        return iIndents;
    }
    
    public BreakPoss getNextBreakPoss(LayoutContext context) {

        if (abProps.absolutePosition == EN_ABSOLUTE) {
            return getAbsoluteBreakPoss(context);
        } else if (abProps.absolutePosition == EN_FIXED) {
            return getAbsoluteBreakPoss(context);
        }

        referenceIPD = context.getRefIPD();
        int bpd = context.getStackLimit().opt;
        if (width.getEnum() != EN_AUTO) {
            referenceIPD = width.getValue();
        }
        if (height.getEnum() != EN_AUTO) {
            bpd = height.getValue();
        }
        int contentIPD = referenceIPD - getIPIndents();
        
        Rectangle2D rect = new Rectangle2D.Double(0, 0, contentIPD, bpd);
        relDims = new FODimension(0, 0);
        absoluteCTM = CTM.getCTMandRelDims(fobj.getReferenceOrientation(),
                fobj.getWritingMode(), rect, relDims);
        double[] vals = absoluteCTM.toArray();

        MinOptMax stackLimit;
        boolean rotated = vals[0] == 0.0;
        if (rotated) {
            // rotated 90 degrees
            if (relDims.ipd > context.getRefIPD()) {
                relDims.ipd = context.getRefIPD();
            }
            stackLimit = new MinOptMax(relDims.ipd);
            if (width.getEnum() == EN_AUTO) {
                relDims.bpd = context.getStackLimit().opt;
            }
            absoluteCTM = new CTM(vals[0], vals[1], vals[2], vals[3], 0, 0);
        } else {
            if (vals[0] == -1.0) {
                absoluteCTM = new CTM(vals[0], vals[1], vals[2], vals[3], 0, 0);
            }
            stackLimit = context.getStackLimit();
        }

        LayoutManager curLM; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        // if starting add space before
        // stackSize.add(spaceBefore);
        BreakPoss lastPos = null;

        fobj.setLayoutDimension(PercentBase.BLOCK_IPD, contentIPD);
        fobj.setLayoutDimension(PercentBase.BLOCK_BPD, bpd);
        fobj.setLayoutDimension(PercentBase.REFERENCE_AREA_IPD, contentIPD);
        fobj.setLayoutDimension(PercentBase.REFERENCE_AREA_BPD, bpd);

        while ((curLM = getChildLM()) != null) {
            // Make break positions and return blocks!
            // Set up a LayoutContext
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
                childLC.setStackLimit(
                  MinOptMax.subtract(stackLimit,
                                     stackSize));
                childLC.setRefIPD(contentIPD);

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
            breakPoss.setStackingSize(new MinOptMax(contentIPD));
            return breakPoss;
        }
        return null;
    }

    public BreakPoss getAbsoluteBreakPoss(LayoutContext context) {

        LayoutManager curLM ; // currently active LM

        MinOptMax stackSize = new MinOptMax();

        int ipd = relDims.ipd;

        while ((curLM = getChildLM()) != null) {
            // Make break positions and return blocks!
            // Set up a LayoutContext
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(new MinOptMax(1000000));
            childLC.setRefIPD(ipd);

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
            TraitSetter.addBorders(viewportBlockArea, fobj.getCommonBorderPaddingBackground());
            TraitSetter.addBackground(viewportBlockArea, fobj.getCommonBorderPaddingBackground());
            
            if (abProps.absolutePosition == EN_ABSOLUTE 
                    || abProps.absolutePosition == EN_FIXED) {
                viewportBlockArea.setXOffset(abProps.left.getValue());
                viewportBlockArea.setYOffset(abProps.top.getValue());
                viewportBlockArea.setIPD(width.getValue());
                viewportBlockArea.setBPD(height.getValue());

                viewportBlockArea.setCTM(absoluteCTM);
                viewportBlockArea.setClip(clip);
                autoHeight = false;
            } else {
                double[] vals = absoluteCTM.toArray();
                boolean rotated = vals[0] == 0.0;
                if (rotated) {
                    viewportBlockArea.setIPD(relDims.ipd);
                    viewportBlockArea.setBPD(relDims.bpd);
                    viewportBlockArea.setCTM(absoluteCTM);
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
                    viewportBlockArea.setCTM(absoluteCTM);
                    viewportBlockArea.setClip(clip);
                } else {
                    viewportBlockArea.setIPD(relDims.ipd);
                    if (height.getEnum() != EN_AUTO) {
                        viewportBlockArea.setBPD(relDims.bpd);
                        autoHeight = false;
                    }
                }
            }

            curBlockArea = new Block();

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

