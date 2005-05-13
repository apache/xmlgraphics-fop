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

/* $Id: BlockLayoutManager.java,v 1.19 2004/05/26 04:22:39 gmazza Exp $ */

package org.apache.fop.layoutmgr;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.List;

import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fonts.Font;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for a block FO.
 */
public class BlockLayoutManager extends BlockStackingLayoutManager {
    
    private static final int FINISHED_LEAF_POS = -2;
        
    private Block curBlockArea;

    /** Iterator over the child layout managers. */
    protected ListIterator proxyLMiter;

    /* holds the (one-time use) fo:block space-before
       and -after properties.  Large fo:blocks are split
       into multiple Area.Blocks to accomodate the subsequent
       regions (pages) they are placed on.  space-before
       is applied at the beginning of the first
       Block and space-after at the end of the last Block
       used in rendering the fo:block.
    */
    private MinOptMax foBlockSpaceBefore = null;
    // need to retain foBlockSpaceAfter from previous instantiation
    //TODO this is very bad for multi-threading. fix me!
    private static MinOptMax foBlockSpaceAfter = null;
    private MinOptMax prevFoBlockSpaceAfter = null;

    private int lead = 12000;
    private int lineHeight = 14000;
    private int follow = 2000;
    private int middleShift = 0;

    private int iStartPos = 0;

    //private int contentIPD = 0;
    
    /** The list of child BreakPoss instances. */
    protected List childBreaks = new java.util.ArrayList();

    private boolean isfirst = true;
    
    private LineLayoutManager childLLM = null;

    /**
     * Creates a new BlockLayoutManager.
     * @param inBlock the block FO object to create the layout manager for.
     */
    public BlockLayoutManager(org.apache.fop.fo.flow.Block inBlock) {
        super(inBlock);
        proxyLMiter = new ProxyLMiter();

        Font fs = getBlockFO().getCommonFont().getFontState(
                  getBlockFO().getFOEventHandler().getFontInfo());
        
        lead = fs.getAscender();
        follow = -fs.getDescender();
        middleShift = -fs.getXHeight() / 2;
        lineHeight = getBlockFO().getLineHeight().getOptimum().getLength().getValue();
    }

    /**
     * @see org.apache.fop.layoutmgr.AbstractLayoutManager#initProperties()
     * @todo need to take into account somewhere the effects of fo:initial-property-set,
     *      if defined for the block.
     */
    protected void initProperties() {
        foBlockSpaceBefore = new SpaceVal(getBlockFO().getCommonMarginBlock().spaceBefore).getSpace();
        prevFoBlockSpaceAfter = foBlockSpaceAfter;
/*LF*/  bpUnit = 0; //layoutProps.blockProgressionUnit;
/*LF*/  if (bpUnit == 0) {
/*LF*/      // use optimum space values
/*LF*/      adjustedSpaceBefore = getBlockFO().getCommonMarginBlock().spaceBefore.getSpace().getOptimum().getLength().getValue();
/*LF*/      adjustedSpaceAfter = getBlockFO().getCommonMarginBlock().spaceAfter.getSpace().getOptimum().getLength().getValue();
/*LF*/  } else {
/*LF*/      // use minimum space values
/*LF*/      adjustedSpaceBefore = getBlockFO().getCommonMarginBlock().spaceBefore.getSpace().getMinimum().getLength().getValue();
/*LF*/      adjustedSpaceAfter = getBlockFO().getCommonMarginBlock().spaceAfter.getSpace().getMinimum().getLength().getValue();
/*LF*/  }
    }

    /**
     * Proxy iterator for Block LM.
     * This iterator creates and holds the complete list
     * of child LMs.
     * It uses fobjIter as its base iterator.
     * Block LM's preLoadNext uses this iterator
     * as its base iterator.
     */
    protected class ProxyLMiter extends LMiter {

        public ProxyLMiter() {
            super(BlockLayoutManager.this);
            listLMs = new java.util.ArrayList(10);
        }

        public boolean hasNext() {
            return (curPos < listLMs.size()) ? true : preLoadNext(curPos);
        }

        protected boolean preLoadNext(int pos) {
            List newLMs = preLoadList(pos + 1 - listLMs.size());
            if (newLMs != null) {
                listLMs.addAll(newLMs);
            }
            return pos < listLMs.size();
        }
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#preLoadNext
     */
    public boolean preLoadNext(int pos) {

        while (proxyLMiter.hasNext()) {
            LayoutManager lm = (LayoutManager) proxyLMiter.next();
            if (lm.generatesInlineAreas()) {
                LineLayoutManager lineLM = createLineManager(lm);
                addChildLM(lineLM);
            } else {
                addChildLM(lm);
            }
            if (pos < childLMs.size()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a new LineLM, and collect all consecutive
     * inline generating LMs as its child LMs.
     * @param firstlm First LM in new LineLM
     * @return the newly created LineLM
     */
    private LineLayoutManager createLineManager(LayoutManager firstlm) {
        LineLayoutManager llm;
        llm = new LineLayoutManager(getBlockFO(), lineHeight, lead, follow, middleShift);
        List inlines = new java.util.ArrayList();
        inlines.add(firstlm);
        while (proxyLMiter.hasNext()) {
            LayoutManager lm = (LayoutManager) proxyLMiter.next();
            if (lm.generatesInlineAreas()) {
                inlines.add(lm);
            } else {
                proxyLMiter.previous();
                break;
            }
        }
        llm.addChildLMs(inlines);
        return llm;
    }

    private int getIPIndents() {
        int iIndents = 0;
        iIndents += getBlockFO().getCommonMarginBlock().startIndent.getValue();
        iIndents += getBlockFO().getCommonMarginBlock().endIndent.getValue();
        return iIndents;
    }
    
    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getNextBreakPoss(org.apache.fop.layoutmgr.LayoutContext)
     */
    public BreakPoss getNextBreakPossOLDOLDOLD(LayoutContext context) {
        LayoutManager curLM; // currently active LM

        //int refipd = context.getRefIPD();
        referenceIPD = context.getRefIPD();
        int contentipd = referenceIPD - getIPIndents();

        MinOptMax stackSize = new MinOptMax();

        if (prevFoBlockSpaceAfter != null) {
            stackSize.add(prevFoBlockSpaceAfter);
            prevFoBlockSpaceAfter = null;
        }

        if (foBlockSpaceBefore != null) {
            // this function called before addAreas(), so
            // resetting foBlockSpaceBefore = null in addAreas()
            stackSize.add(foBlockSpaceBefore);
        }

        BreakPoss lastPos = null;

        // Set context for percentage property values.
        getBlockFO().setLayoutDimension(PercentBase.BLOCK_IPD, contentipd);
        getBlockFO().setLayoutDimension(PercentBase.BLOCK_BPD, -1);

        while ((curLM = getChildLM()) != null) {
            // Make break positions and return blocks!
            // Set up a LayoutContext
            BreakPoss bp;

            LayoutContext childLC = new LayoutContext(0);
            // if line layout manager then set stack limit to ipd
            // line LM actually generates a LineArea which is a block
            if (curLM.generatesInlineAreas()) {
                // set stackLimit for lines
                childLC.setStackLimit(new MinOptMax(contentipd));
                childLC.setRefIPD(contentipd);
            } else {
                childLC.setStackLimit(
                  MinOptMax.subtract(context.getStackLimit(),
                                     stackSize));
                childLC.setRefIPD(referenceIPD);
            }
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

                    if (curLM.generatesInlineAreas()) {
                        // Reset stackLimit for non-first lines
                        childLC.setStackLimit(new MinOptMax(contentipd));
                    } else {
                        childLC.setStackLimit(MinOptMax.subtract(
                                                 context.getStackLimit(), stackSize));
                    }
                }
            }
            if (getChildLM() == null || over) {
                if (getChildLM() == null) {
                    setFinished(true);
                    stackSize.add(new SpaceVal(getBlockFO().getCommonMarginBlock().spaceAfter).getSpace());
                }
                BreakPoss breakPoss = new BreakPoss(
                                    new LeafPosition(this, childBreaks.size() - 1));
                if (over) {
                    breakPoss.setFlag(BreakPoss.NEXT_OVERFLOWS, true);
                }
                breakPoss.setStackingSize(stackSize);
                if (isfirst && breakPoss.getStackingSize().opt > 0) {
                    breakPoss.setFlag(BreakPoss.ISFIRST, true);
                    isfirst = false;
                }
                if (isFinished()) {
                    breakPoss.setFlag(BreakPoss.ISLAST, true);
                }
                return breakPoss;
            }
        }
        setFinished(true);
        BreakPoss breakPoss = new BreakPoss(new LeafPosition(this, FINISHED_LEAF_POS));
        breakPoss.setStackingSize(stackSize);
        breakPoss.setFlag(BreakPoss.ISFIRST, isfirst);
        breakPoss.setFlag(BreakPoss.ISLAST, true);
        return breakPoss;
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepTogether()
     */
    public boolean mustKeepTogether() {
        //TODO Keeps will have to be more sophisticated sooner or later
        return ((BlockLevelLayoutManager)getParent()).mustKeepTogether() 
                || !getBlockFO().getKeepTogether().getWithinPage().isAuto()
                || !getBlockFO().getKeepTogether().getWithinColumn().isAuto();
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithPrevious()
     */
    public boolean mustKeepWithPrevious() {
        return !getBlockFO().getKeepWithPrevious().getWithinPage().isAuto()
            || !getBlockFO().getKeepWithPrevious().getWithinColumn().isAuto();
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithNext()
     */
    public boolean mustKeepWithNext() {
        return !getBlockFO().getKeepWithNext().getWithinPage().isAuto()
                || !getBlockFO().getKeepWithNext().getWithinColumn().isAuto();
    }

    //TODO this method is no longer used
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        setFinished(true);
        return null;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addAreas(org.apache.fop.layoutmgr.PositionIterator, org.apache.fop.layoutmgr.LayoutContext)
     */
    public void addAreasOLDOLDOLD(PositionIterator parentIter,
                         LayoutContext layoutContext) {
        getParentArea(null);

        BreakPoss bp1 = (BreakPoss)parentIter.peekNext();
        bBogus = !bp1.generatesAreas(); 
        
        // if adjusted space before
        double adjust = layoutContext.getSpaceAdjust();
        addBlockSpacing(adjust, foBlockSpaceBefore);
        foBlockSpaceBefore = null;

        if (!isBogus()) {
            getPSLM().addIDToPage(getBlockFO().getId());
            getCurrentPV().addMarkers(markers, true, bp1.isFirstArea(), 
                    bp1.isLastArea());
        }

        try {
            LayoutManager childLM;
            LayoutContext lc = new LayoutContext(0);
            while (parentIter.hasNext()) {
                LeafPosition lfp = (LeafPosition) parentIter.next();
                if (lfp.getLeafPos() == FINISHED_LEAF_POS) {
                    return;
                }
                // Add the block areas to Area
                PositionIterator breakPosIter 
                    = new BreakPossPosIter(childBreaks, iStartPos,
                                       lfp.getLeafPos() + 1);
                iStartPos = lfp.getLeafPos() + 1;
                while ((childLM = breakPosIter.getNextChildLM()) != null) {
                    childLM.addAreas(breakPosIter, lc);
                }
            }
        } finally {
            if (!isBogus()) {
                getCurrentPV().addMarkers(markers, false, bp1.isFirstArea(), 
                    bp1.isLastArea());
            }
            flush();

            // if adjusted space after
            foBlockSpaceAfter = new SpaceVal(getBlockFO().getCommonMarginBlock().spaceAfter).getSpace();
            addBlockSpacing(adjust, foBlockSpaceAfter);
            curBlockArea = null;
        }
    }

    public void addAreas(PositionIterator parentIter,
            LayoutContext layoutContext) {
        /* LF *///System.out.println(" BLM.addAreas>");
        getParentArea(null);

        // if this will create the first block area in a page
        // and display-align is bottom or center, add space before
        if (layoutContext.getSpaceBefore() > 0) {
            addBlockSpacing(0.0, new MinOptMax(layoutContext.getSpaceBefore()));
        }

        getPSLM().addIDToPage(getBlockFO().getId());
        //addMarkersToPV(true, bp1.isFirstArea(), bp1.isLastArea());
        getCurrentPV().addMarkers(markers, true, true, false);

        LayoutManager childLM = null;
        LayoutManager lastLM = null;
        LayoutContext lc = new LayoutContext(0);
        /* LF */// set space after in the LayoutContext for children
        /* LF */if (layoutContext.getSpaceAfter() > 0) {
            /* LF */lc.setSpaceAfter(layoutContext.getSpaceAfter());
            /* LF */}
        /* LF */PositionIterator childPosIter;

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
        LinkedList positionList = new LinkedList();
        Position pos;
        boolean bSpaceBefore = false;
        boolean bSpaceAfter = false;
        while (parentIter.hasNext()) {
            pos = (Position) parentIter.next();
            //log.trace("pos = " + pos.getClass().getName() + "; " + pos);
            Position innerPosition = pos;
            if (pos instanceof NonLeafPosition) {
                //Not all elements are wrapped
                innerPosition = ((NonLeafPosition) pos).getPosition();
            }
            if (innerPosition == null) {
                // pos was created by this BlockLM and was inside an element
                // representing space before or after
                // this means the space was not discarded
                if (positionList.size() == 0) {
                    // pos was in the element representing space-before
                    bSpaceBefore = true;
                    //log.trace(" space before");
                } else {
                    // pos was in the element representing space-after
                    bSpaceAfter = true;
                    //log.trace(" space-after");
                }
            } else if (innerPosition.getLM() == this
                    && !(innerPosition instanceof MappingPosition)) {
                // pos was created by this BlockLM and was inside a penalty
                // allowing or forbidding a page break
                // nothing to do
                //log.trace(" penalty");
            } else {
                // innerPosition was created by another LM
                positionList.add(innerPosition);
                lastLM = innerPosition.getLM();
                //log.trace(" " + innerPosition.getClass().getName());
            }
        }

        if (bpUnit == 0) {
            // the Positions in positionList were inside the elements
            // created by the LineLM
            childPosIter = new StackingIter(positionList.listIterator());
            } else {
            // the Positions in positionList were inside the elements
            // created by the BlockLM in the createUnitElements() method
            //if (((Position) positionList.getLast()) instanceof
                  // LeafPosition) {
            //    // the last item inside positionList is a LeafPosition
            //    // (a LineBreakPosition, more precisely); this means that
            //    // the whole paragraph is on the same page
            //    System.out.println("paragrafo intero");
            //    childPosIter = new KnuthPossPosIter(storedList, 0,
                  // storedList.size());
            //} else {
            //    // the last item inside positionList is a Position;
            //    // this means that the paragraph has been split
            //    // between consecutive pages
            LinkedList splitList = new LinkedList();
            int splitLength = 0;
            int iFirst = ((MappingPosition) positionList.getFirst()).getFirstIndex();
            int iLast = ((MappingPosition) positionList.getLast()).getLastIndex();
            // copy from storedList to splitList all the elements from
            // iFirst to iLast
            ListIterator storedListIterator = storedList.listIterator(iFirst);
            while (storedListIterator.nextIndex() <= iLast) {
                KnuthElement element = (KnuthElement) storedListIterator
                        .next();
                // some elements in storedList (i.e. penalty items) were created
                // by this BlockLM, and must be ignored
                if (element.getLayoutManager() != this) {
                    splitList.add(element);
                    splitLength += element.getW();
                    lastLM = element.getLayoutManager();
                }
                }
            //System.out.println("addAreas riferito a storedList da " +
                  // iFirst + " a " + iLast);
            //System.out.println("splitLength= " + splitLength
            //                   + " (" + neededUnits(splitLength) + " unita') "
            //                   + (neededUnits(splitLength) * bpUnit - splitLength) + " spazi");
            // add space before and / or after the paragraph
            // to reach a multiple of bpUnit
            if (bSpaceBefore && bSpaceAfter) {
                foBlockSpaceBefore = new SpaceVal(getBlockFO().getCommonMarginBlock().spaceBefore).getSpace();
                foBlockSpaceAfter = new SpaceVal(getBlockFO().getCommonMarginBlock().spaceAfter).getSpace();
                adjustedSpaceBefore = (neededUnits(splitLength
                        + foBlockSpaceBefore.min
                        + foBlockSpaceAfter.min)
                        * bpUnit - splitLength) / 2;
                adjustedSpaceAfter = neededUnits(splitLength
                        + foBlockSpaceBefore.min
                        + foBlockSpaceAfter.min)
                        * bpUnit - splitLength - adjustedSpaceBefore;
                } else if (bSpaceBefore) {
                adjustedSpaceBefore = neededUnits(splitLength
                        + foBlockSpaceBefore.min)
                        * bpUnit - splitLength;
                } else {
                adjustedSpaceAfter = neededUnits(splitLength
                        + foBlockSpaceAfter.min)
                        * bpUnit - splitLength;
                }
            //System.out.println("spazio prima = " + adjustedSpaceBefore
                  // + " spazio dopo = " + adjustedSpaceAfter + " totale = " +
                  // (adjustedSpaceBefore + adjustedSpaceAfter + splitLength));
            childPosIter = new KnuthPossPosIter(splitList, 0, splitList
                    .size());
            //}
            }

        // if adjusted space before
        if (bSpaceBefore) {
            addBlockSpacing(0, new MinOptMax(adjustedSpaceBefore));
        }

        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // set last area flag
            lc.setFlags(LayoutContext.LAST_AREA,
                    (layoutContext.isLastArea() && childLM == lastLM));
            /*LF*/lc.setStackLimit(layoutContext.getStackLimit());
            // Add the line areas to Area
            childLM.addAreas(childPosIter, lc);
        }

        int bIndents = getBlockFO().getCommonBorderPaddingBackground().getBPPaddingAndBorder(false);

        getCurrentPV().addMarkers(markers, false, false, true);

        flush();

        // if adjusted space after
        if (bSpaceAfter) {
            addBlockSpacing(0, new MinOptMax(adjustedSpaceAfter));
        }

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
     * @param childArea area to get the parent area for
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();

            TraitSetter.addBreaks(curBlockArea, 
                    getBlockFO().getBreakBefore(), getBlockFO().getBreakAfter());

            // Must get dimensions from parent area
            //Don't optimize this line away. It can have ugly side-effects.
            /*Area parentArea =*/ parentLM.getParentArea(curBlockArea);

            // set traits
            TraitSetter.addBorders(curBlockArea, 
                    getBlockFO().getCommonBorderPaddingBackground());
            TraitSetter.addBackground(curBlockArea, 
                    getBlockFO().getCommonBorderPaddingBackground());
            TraitSetter.addMargins(curBlockArea,
                    getBlockFO().getCommonBorderPaddingBackground(), 
                    getBlockFO().getCommonMarginBlock());

            // Set up dimensions
            // Get reference IPD from parentArea
            //int referenceIPD = parentArea.getIPD();
            //curBlockArea.setIPD(referenceIPD);

            // Set the width of the block based on the parent block
            // Need to be careful though, if parent is BC then width may not be set
            /* TODO remove if really not used anymore
            int parentwidth = 0;
            if (parentArea instanceof BlockParent) {
                parentwidth = ((BlockParent) parentArea).getIPD();
            }
            if (parentwidth == 0) {
                parentwidth = referenceIPD;
            }
            parentwidth -= getIPIndents();
            */

            int contentIPD = referenceIPD - getIPIndents();
            
            curBlockArea.setIPD(contentIPD/*parentwidth*/);
            setCurrentArea(curBlockArea); // ??? for generic operations
        }
        return curBlockArea;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addChildArea(Area)
     */
    public void addChildArea(Area childArea) {
        if (curBlockArea != null) {
            if (childArea instanceof LineArea) {
                curBlockArea.addLineArea((LineArea) childArea);
            } else {
                curBlockArea.addBlock((Block) childArea);
            }
        }
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#resetPosition(org.apache.fop.layoutmgr.Position)
     */
    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
            childBreaks.clear();
            iStartPos = 0;
        } else {
            //reset(resetPos);
            LayoutManager lm = resetPos.getLM();
        }
    }

    /**
     * convenience method that returns the Block node
     */
    protected org.apache.fop.fo.flow.Block getBlockFO() {
        return (org.apache.fop.fo.flow.Block) fobj;
    }
}

