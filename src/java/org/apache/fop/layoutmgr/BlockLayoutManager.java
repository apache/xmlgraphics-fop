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

import org.apache.fop.fonts.Font;
import org.apache.fop.layoutmgr.inline.InlineLevelLayoutManager;
import org.apache.fop.layoutmgr.inline.LineLayoutManager;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for a block FO.
 */
public class BlockLayoutManager extends BlockStackingLayoutManager {
    
    private Block curBlockArea;

    /** Iterator over the child layout managers. */
    protected ListIterator proxyLMiter;

    private int lead = 12000;
    private int lineHeight = 14000;
    private int follow = 2000;
    private int middleShift = 0;

    /** The list of child BreakPoss instances. */
    protected List childBreaks = new java.util.ArrayList();

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
        initialize();
    }

    private void initialize() {
        foSpaceBefore = new SpaceVal(getBlockFO().getCommonMarginBlock().spaceBefore).getSpace();
        foSpaceAfter = new SpaceVal(getBlockFO().getCommonMarginBlock().spaceAfter).getSpace();
        bpUnit = 0; // non-standard extension
        if (bpUnit == 0) {
            // use optimum space values
            adjustedSpaceBefore = getBlockFO().getCommonMarginBlock().spaceBefore.getSpace().getOptimum().getLength().getValue();
            adjustedSpaceAfter = getBlockFO().getCommonMarginBlock().spaceAfter.getSpace().getOptimum().getLength().getValue();
        } else {
            // use minimum space values
            adjustedSpaceBefore = getBlockFO().getCommonMarginBlock().spaceBefore.getSpace().getMinimum().getLength().getValue();
            adjustedSpaceAfter = getBlockFO().getCommonMarginBlock().spaceAfter.getSpace().getMinimum().getLength().getValue();
        }
    }

    /**
     * Proxy iterator for Block LM.
     * This iterator creates and holds the complete list
     * of child LMs.
     * It uses fobjIter as its base iterator.
     * Block LM's createNextChildLMs uses this iterator
     * as its base iterator.
     */
    protected class ProxyLMiter extends LMiter {

        public ProxyLMiter() {
            super(BlockLayoutManager.this);
            listLMs = new java.util.ArrayList(10);
        }

        public boolean hasNext() {
            return (curPos < listLMs.size()) ? true : createNextChildLMs(curPos);
        }

        protected boolean createNextChildLMs(int pos) {
            List newLMs = createChildLMs(pos + 1 - listLMs.size());
            if (newLMs != null) {
                listLMs.addAll(newLMs);
            }
            return pos < listLMs.size();
        }
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#createNextChildLMs
     */
    public boolean createNextChildLMs(int pos) {

        while (proxyLMiter.hasNext()) {
            LayoutManager lm = (LayoutManager) proxyLMiter.next();
            if (lm instanceof InlineLevelLayoutManager) {
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
            if (lm instanceof InlineLevelLayoutManager) {
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

    public void addAreas(PositionIterator parentIter,
            LayoutContext layoutContext) {
        //System.out.println(" BLM.addAreas>");
        getParentArea(null);

        // if this will create the first block area in a page
        // and display-align is after or center, add space before
        if (layoutContext.getSpaceBefore() > 0) {
            addBlockSpacing(0.0, new MinOptMax(layoutContext.getSpaceBefore()));
        }

        LayoutManager childLM = null;
        LayoutManager lastLM = null;
        LayoutContext lc = new LayoutContext(0);
        // set space after in the LayoutContext for children
        if (layoutContext.getSpaceAfter() > 0) {
            lc.setSpaceAfter(layoutContext.getSpaceAfter());
        }
        PositionIterator childPosIter;

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
        LinkedList positionList = new LinkedList();
        Position pos;
        boolean bSpaceBefore = false;
        boolean bSpaceAfter = false;
        Position firstPos = null;
        Position lastPos = null;
        while (parentIter.hasNext()) {
            pos = (Position) parentIter.next();
            //log.trace("pos = " + pos.getClass().getName() + "; " + pos);
            if (pos.getIndex() >= 0) {
                if (firstPos == null) {
                    firstPos = pos;
                }
                lastPos = pos;
            }
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

        getPSLM().addIDToPage(getBlockFO().getId());
        /* TODO remove when markers are really ok
        log.debug("Checking on " + this);
        log.debug("Checking first=" + firstPos);
        log.debug("Checking last=" + lastPos);
        log.debug("->" + isFirst(firstPos) + "/" + isLast(lastPos));
        */
        if (markers != null) {
            getCurrentPV().addMarkers(markers, true, isFirst(firstPos), isLast(lastPos));
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
                foSpaceBefore = new SpaceVal(getBlockFO().getCommonMarginBlock().spaceBefore).getSpace();
                foSpaceAfter = new SpaceVal(getBlockFO().getCommonMarginBlock().spaceAfter).getSpace();
                adjustedSpaceBefore = (neededUnits(splitLength
                        + foSpaceBefore.min
                        + foSpaceAfter.min)
                        * bpUnit - splitLength) / 2;
                adjustedSpaceAfter = neededUnits(splitLength
                        + foSpaceBefore.min
                        + foSpaceAfter.min)
                        * bpUnit - splitLength - adjustedSpaceBefore;
                } else if (bSpaceBefore) {
                adjustedSpaceBefore = neededUnits(splitLength
                        + foSpaceBefore.min)
                        * bpUnit - splitLength;
                } else {
                adjustedSpaceAfter = neededUnits(splitLength
                        + foSpaceAfter.min)
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
        double adjust = layoutContext.getSpaceAdjust();
        addBlockSpacing(adjust, foSpaceBefore);
        foSpaceBefore = null;
        //if (bSpaceBefore) {
        //    addBlockSpacing(0, new MinOptMax(adjustedSpaceBefore));
        //}

        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // set last area flag
            lc.setFlags(LayoutContext.LAST_AREA,
                    (layoutContext.isLastArea() && childLM == lastLM));
            lc.setStackLimit(layoutContext.getStackLimit());
            // Add the line areas to Area
            childLM.addAreas(childPosIter, lc);
        }

        //int bIndents = getBlockFO().getCommonBorderPaddingBackground().getBPPaddingAndBorder(false);

        if (markers != null) {
            getCurrentPV().addMarkers(markers, false, isFirst(firstPos), isLast(lastPos));
        }

        flush();

        // if adjusted space after
        addBlockSpacing(adjust, foSpaceAfter);
        //if (bSpaceAfter) {
        //    addBlockSpacing(0, new MinOptMax(adjustedSpaceAfter));
        //}

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
            TraitSetter.setProducerID(curBlockArea, getBlockFO().getId());
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

