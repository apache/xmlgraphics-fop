/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.TextInfo;
import org.apache.fop.area.Area;
import org.apache.fop.area.BlockParent;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.fo.PropertyManager;
import org.apache.fop.layout.AbsolutePositionProps;
import org.apache.fop.fo.properties.AbsolutePosition;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.area.CTM;
import org.apache.fop.datatypes.FODimension;

import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;

import java.awt.geom.Rectangle2D;

/**
 * LayoutManager for a block FO.
 */
public class BlockContainerLayoutManager extends BlockStackingLayoutManager {

    private BlockViewport viewportBlockArea;
    private Block curBlockArea;

    List childBreaks = new ArrayList();

    AbsolutePositionProps abProps;
    FODimension relDims;
    CTM absoluteCTM;
    boolean clip = false;
    int overflow;
    PropertyManager propManager;

    public BlockContainerLayoutManager(FObj fobj) {
        super(fobj);
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
            absoluteCTM = pm.getCTMandRelDims(rect, relDims);
        }
    }

    protected int getRotatedIPD() {
        PropertyList props = propManager.getProperties();
        int height = props.get("height").getLength().mvalue();
        height = props.get("inline-progression-dimension.optimum").getLength().mvalue();

        return height;
    }

    public BreakPoss getNextBreakPoss(LayoutContext context) {

        if (abProps.absolutePosition == AbsolutePosition.ABSOLUTE) {
            return getAbsoluteBreakPoss(context);
        }

        Rectangle2D rect = new Rectangle2D.Double(0, 0, context.getRefIPD(),
                                                  context.getStackLimit().opt);
        relDims = new FODimension(0, 0);
        absoluteCTM = propManager.getCTMandRelDims(rect, relDims);
        double[] vals = absoluteCTM.toArray();

        MinOptMax stackLimit;
        int ipd = context.getRefIPD();
        boolean rotated = vals[0] == 0.0;
        if (rotated) {
            // rotated 90 degrees
            stackLimit = new MinOptMax(1000000);
            ipd = getRotatedIPD();
            absoluteCTM = new CTM(vals[0], vals[1], vals[2], vals[3], 0, 0);
        } else {
            if (vals[0] == -1.0) {
                absoluteCTM = new CTM(vals[0], vals[1], vals[2], vals[3], 0, 0);
            }
            stackLimit = context.getStackLimit();
        }

        LayoutManager curLM ; // currently active LM

        MinOptMax stackSize = new MinOptMax();
        // if starting add space before
        // stackSize.add(spaceBefore);
        BreakPoss lastPos = null;

        while ((curLM = getChildLM()) != null) {
            // Make break positions and return blocks!
            // Set up a LayoutContext
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
                childLC.setStackLimit(
                  MinOptMax.subtract(stackLimit,
                                     stackSize));
                childLC.setRefIPD(ipd);

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
                        break;
                    }
                    lastPos = bp;
                    childBreaks.add(bp);

                    childLC.setStackLimit(MinOptMax.subtract(
                                           stackLimit, stackSize));
                }
            }
            if (!rotated) {
                BreakPoss breakPoss;
                breakPoss = new BreakPoss(new LeafPosition(this,
                                                   childBreaks.size() - 1));
                breakPoss.setStackingSize(stackSize);
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
            if (overflow == Overflow.HIDDEN) {
                clip = true;
            } else if (overflow == Overflow.ERROR_IF_OVERFLOW) {
                //log.error("contents overflows block-container viewport: clipping");
                clip = true;
            }
        }

        return breakPoss;
    }

    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);

        LayoutManager childLM ;
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

        childBreaks.clear();
        viewportBlockArea = null;
        curBlockArea = null;
    }

    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            viewportBlockArea = new BlockViewport();
            if (abProps.absolutePosition == AbsolutePosition.ABSOLUTE) {
                viewportBlockArea.setXOffset(abProps.left);
                viewportBlockArea.setYOffset(abProps.top);
                viewportBlockArea.setWidth(abProps.right - abProps.left);
                viewportBlockArea.setHeight(abProps.bottom - abProps.top);

                viewportBlockArea.setCTM(absoluteCTM);
                viewportBlockArea.setClip(clip);
            } else {
                double[] vals = absoluteCTM.toArray();
                boolean rotated = vals[0] == 0.0;
                if (rotated) {
                    viewportBlockArea.setWidth(relDims.bpd);
                    viewportBlockArea.setHeight(getRotatedIPD());
                    viewportBlockArea.setCTM(absoluteCTM);
                    viewportBlockArea.setClip(clip);
                } else if (vals[0] == -1.0) {
                    // need to set bpd to actual size for rotation
                    // and stacking
                    viewportBlockArea.setWidth(relDims.ipd);
                    viewportBlockArea.setWidth(relDims.bpd);
                    viewportBlockArea.setCTM(absoluteCTM);
                    viewportBlockArea.setClip(clip);
                }
            }

            curBlockArea = new Block();
            viewportBlockArea.addBlock(curBlockArea);

            if (abProps.absolutePosition == AbsolutePosition.ABSOLUTE) {
                viewportBlockArea.setPositioning(Block.ABSOLUTE);
            }

            // Set up dimensions
            // Must get dimensions from parent area
            Area parentArea = parentLM.getParentArea(curBlockArea);
            int referenceIPD = parentArea.getIPD();
            curBlockArea.setIPD(referenceIPD);
            // Get reference IPD from parentArea
            setCurrentArea(viewportBlockArea); // ??? for generic operations
        }
        return curBlockArea;
    }


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

    public void addMarker(String name, LayoutManager lm, boolean start) {
        parentLM.addMarker(name, lm, start);
    }

}

