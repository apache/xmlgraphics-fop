/*
 * $Id: BlockContainerLayoutManager.java,v 1.13 2003/03/05 20:38:26 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.layoutmgr;

import java.util.List;
import java.awt.geom.Rectangle2D;

import org.apache.fop.area.Area;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.Block;
import org.apache.fop.fo.PropertyManager;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.area.CTM;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for a block FO.
 */
public class BlockContainerLayoutManager extends BlockStackingLayoutManager {

    private BlockViewport viewportBlockArea;
    private Block curBlockArea;

    private List childBreaks = new java.util.ArrayList();

    private CommonAbsolutePosition abProps;
    private CommonBorderAndPadding borderProps;
    private CommonMarginBlock marginProps;
    private FODimension relDims;
    private CTM absoluteCTM;
    private boolean clip = false;
    private int overflow;
    private PropertyManager propManager;
    private Length width;
    private Length height;

    // When viewport should grow with the content.
    private boolean autoHeight = true; 

    /**
     * Create a new block container layout manager.
     */
    public BlockContainerLayoutManager() {
    }

    public void setOverflow(int of) {
        overflow = of;
    }

    protected void initProperties(PropertyManager pm) {
        propManager = pm;

        abProps = pm.getAbsolutePositionProps();
        if (abProps.absolutePosition == AbsolutePosition.ABSOLUTE) {
            Rectangle2D rect = new Rectangle2D.Double(abProps.left,
                                abProps.top, abProps.right - abProps.left,
                                abProps.bottom - abProps.top);
            relDims = new FODimension(0, 0);
            absoluteCTM = CTM.getCTMandRelDims(pm.getAbsRefOrient(),
                pm.getWritingMode(), rect, relDims);
        }
        marginProps = pm.getMarginProps();
        borderProps = pm.getBorderAndPadding();
        height = pm.getPropertyList().get(PR_BLOCK_PROGRESSION_DIMENSION | CP_OPTIMUM).getLength();
        width = pm.getPropertyList().get(PR_INLINE_PROGRESSION_DIMENSION | CP_OPTIMUM).getLength();
    }

    protected int getRotatedIPD() {
        PropertyList props = propManager.getPropertyList();
        int height = props.get(PR_HEIGHT).getLength().getValue();
        height = props.get(PR_INLINE_PROGRESSION_DIMENSION | CP_OPTIMUM).getLength().getValue();

        return height;
    }

    public BreakPoss getNextBreakPoss(LayoutContext context) {

        if (abProps.absolutePosition == AbsolutePosition.ABSOLUTE) {
            return getAbsoluteBreakPoss(context);
        }

        int bIndents = borderProps.getBPPaddingAndBorder(false);
        int iIndents = marginProps.startIndent + marginProps.endIndent; 

        int ipd = context.getRefIPD();
        int bpd = context.getStackLimit().opt;
        if (!width.isAuto()) {
            ipd = width.getValue() + iIndents;
        }
        if (!height.isAuto()) {
            bpd = height.getValue() + bIndents;
        }
        Rectangle2D rect = new Rectangle2D.Double(0, 0, ipd, bpd);
        relDims = new FODimension(0, 0);
        absoluteCTM = CTM.getCTMandRelDims(propManager.getAbsRefOrient(),
                propManager.getWritingMode(), rect, relDims);
        double[] vals = absoluteCTM.toArray();

        ipd -= iIndents;

        MinOptMax stackLimit;
        boolean rotated = vals[0] == 0.0;
        if (rotated) {
            // rotated 90 degrees
            if (relDims.ipd > context.getRefIPD()) {
                relDims.ipd = context.getRefIPD();
            }
            stackLimit = new MinOptMax(relDims.ipd);
            if (width.isAuto()) {
                relDims.bpd = context.getStackLimit().opt;
            }
            absoluteCTM = new CTM(vals[0], vals[1], vals[2], vals[3], 0, 0);
        } else {
            if (vals[0] == -1.0) {
                absoluteCTM = new CTM(vals[0], vals[1], vals[2], vals[3], 0, 0);
            }
            stackLimit = context.getStackLimit();
        }

        LayoutProcessor curLM ; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        // if starting add space before
        // stackSize.add(spaceBefore);
        BreakPoss lastPos = null;

        fobj.setLayoutDimension(PercentBase.BLOCK_IPD, ipd);
        fobj.setLayoutDimension(PercentBase.BLOCK_BPD, bpd - bIndents);
        fobj.setLayoutDimension(PercentBase.REFERENCE_AREA_IPD, ipd);
        fobj.setLayoutDimension(PercentBase.REFERENCE_AREA_BPD, bpd - bIndents);

        while ((curLM = getChildLM()) != null) {
            // Make break positions and return blocks!
            // Set up a LayoutContext
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
                childLC.setStackLimit(
                  MinOptMax.subtract(stackLimit,
                                     stackSize));
                childLC.setRefIPD(ipd);

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
            breakPoss.setStackingSize(new MinOptMax(ipd));
            return breakPoss;
        }
        return null;
    }

    public BreakPoss getAbsoluteBreakPoss(LayoutContext context) {

        LayoutProcessor curLM ; // currently active LM

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
            if (overflow == Overflow.HIDDEN) {
                clip = true;
            } else if (overflow == Overflow.ERROR_IF_OVERFLOW) {
                getLogger().error("contents overflows block-container viewport: clipping");
                clip = true;
            }
        }

        return breakPoss;
    }

    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);

        addID();
        addMarkers(true, true);

        LayoutProcessor childLM;
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
     * @see org.apache.fop.layoutmgr.LayoutProcessor#getParentArea(Area)
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            viewportBlockArea = new BlockViewport();
            TraitSetter.addBorders(viewportBlockArea,
                                   propManager.getBorderAndPadding());
            TraitSetter.addBackground(viewportBlockArea,
                                      propManager.getBackgroundProps());
            
            if (abProps.absolutePosition == AbsolutePosition.ABSOLUTE) {
                viewportBlockArea.setXOffset(abProps.left);
                viewportBlockArea.setYOffset(abProps.top);
                viewportBlockArea.setWidth(abProps.right - abProps.left);
                viewportBlockArea.setHeight(abProps.bottom - abProps.top);

                viewportBlockArea.setCTM(absoluteCTM);
                viewportBlockArea.setClip(clip);
                autoHeight = false;
            } else {
                double[] vals = absoluteCTM.toArray();
                boolean rotated = vals[0] == 0.0;
                if (rotated) {
                    viewportBlockArea.setWidth(relDims.ipd);
                    viewportBlockArea.setHeight(relDims.bpd);
                    viewportBlockArea.setCTM(absoluteCTM);
                    viewportBlockArea.setClip(clip);
                    autoHeight = false;
                } else if (vals[0] == -1.0) {
                    // need to set bpd to actual size for rotation
                    // and stacking
                    viewportBlockArea.setWidth(relDims.ipd);
                    if (!height.isAuto()) {
                        viewportBlockArea.setHeight(relDims.bpd);
                        autoHeight = false;
                    }
                    viewportBlockArea.setCTM(absoluteCTM);
                    viewportBlockArea.setClip(clip);
                } else {
                    viewportBlockArea.setWidth(relDims.ipd);
                    if (!height.isAuto()) {
                        viewportBlockArea.setHeight(relDims.bpd);
                        autoHeight = false;
                    }
                }
            }

            curBlockArea = new Block();

            if (abProps.absolutePosition == AbsolutePosition.ABSOLUTE) {
                viewportBlockArea.setPositioning(Block.ABSOLUTE);
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
     * @see org.apache.fop.layoutmgr.LayoutProcessor#addChild(Area)
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
        int height = viewportBlockArea.getHeight();
        if (viewportBlockArea.getPositioning() == Block.ABSOLUTE) {
            viewportBlockArea.setHeight(0);
        }
        super.flush();
        // Restore the right height.
        if (viewportBlockArea.getPositioning() == Block.ABSOLUTE) {
            viewportBlockArea.setHeight(height);
        }
    }
    
}

