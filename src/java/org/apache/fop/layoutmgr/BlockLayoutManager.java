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

import java.util.Iterator;
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
public class BlockLayoutManager extends BlockStackingLayoutManager
                                    implements BlockLevelLayoutManager {
    
    private static final int FINISHED_LEAF_POS = -2;
    
    private org.apache.fop.fo.flow.Block fobj;
    
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

    private int referenceIPD = 0;
    //private int contentIPD = 0;
    
    /** The list of child BreakPoss instances. */
    protected List childBreaks = new java.util.ArrayList();

    private boolean isfirst = true;
    
    /*LF*/
    /** Only used to store the original list when createUnitElements is called */
    private LinkedList storedList = null;

    private boolean bBreakBeforeServed = false;
    private boolean bSpaceBeforeServed = false;

    private LineLayoutManager childLLM = null;

    /**
     * Creates a new BlockLayoutManager.
     * @param inBlock the block FO object to create the layout manager for.
     */
    public BlockLayoutManager(org.apache.fop.fo.flow.Block inBlock) {
        super(inBlock);
        fobj = inBlock;
        proxyLMiter = new ProxyLMiter();

        Font fs = fobj.getCommonFont().getFontState(fobj.getFOEventHandler().getFontInfo());
        
        lead = fs.getAscender();
        follow = -fs.getDescender();
        middleShift = -fs.getXHeight() / 2;
        lineHeight = fobj.getLineHeight().getOptimum().getLength().getValue();
    }

    /**
     * @see org.apache.fop.layoutmgr.AbstractLayoutManager#initProperties()
     * @todo need to take into account somewhere the effects of fo:initial-property-set,
     *      if defined for the block.
     */
    protected void initProperties() {
        foBlockSpaceBefore = new SpaceVal(fobj.getCommonMarginBlock().spaceBefore).getSpace();
        prevFoBlockSpaceAfter = foBlockSpaceAfter;
/*LF*/  bpUnit = 0; //layoutProps.blockProgressionUnit;
/*LF*/  if (bpUnit == 0) {
/*LF*/      // use optimum space values
/*LF*/      adjustedSpaceBefore = fobj.getCommonMarginBlock().spaceBefore.getSpace().getOptimum().getLength().getValue();
/*LF*/      adjustedSpaceAfter = fobj.getCommonMarginBlock().spaceAfter.getSpace().getOptimum().getLength().getValue();
/*LF*/  } else {
/*LF*/      // use minimum space values
/*LF*/      adjustedSpaceBefore = fobj.getCommonMarginBlock().spaceBefore.getSpace().getMinimum().getLength().getValue();
/*LF*/      adjustedSpaceAfter = fobj.getCommonMarginBlock().spaceAfter.getSpace().getMinimum().getLength().getValue();
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
        llm = new LineLayoutManager(fobj, lineHeight, lead, follow, middleShift);
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
        iIndents += fobj.getCommonMarginBlock().startIndent.getValue();
        iIndents += fobj.getCommonMarginBlock().endIndent.getValue();
        return iIndents;
    }
    
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        /* LF *///System.err.println("BLM.getNextKnuthElements> keep-together = "
              // + layoutProps.keepTogether.getType());
        /* LF *///System.err.println(" keep-with-previous = " +
              // layoutProps.keepWithPrevious.getType());
        /* LF *///System.err.println(" keep-with-next = " +
              // layoutProps.keepWithNext.getType());
        BlockLevelLayoutManager curLM; // currently active LM
        BlockLevelLayoutManager prevLM = null; // previously active LM

        referenceIPD = context.getRefIPD();
        int iIndents = fobj.getCommonMarginBlock().startIndent.getValue() 
                + fobj.getCommonMarginBlock().endIndent.getValue();
        int bIndents = fobj.getCommonBorderPaddingBackground().getBPPaddingAndBorder(false);
        int ipd = referenceIPD - iIndents;

        MinOptMax stackSize = new MinOptMax();

        // Set context for percentage property values.
        fobj.setLayoutDimension(PercentBase.BLOCK_IPD, ipd);
        fobj.setLayoutDimension(PercentBase.BLOCK_BPD, -1);

        LinkedList returnedList = null;
        LinkedList contentList = new LinkedList();
        LinkedList returnList = new LinkedList();
        Position returnPosition = new NonLeafPosition(this, null);

        if (!bBreakBeforeServed) {
            try {
                if (addKnuthElementsForBreakBefore(returnList, returnPosition, 
                        fobj.getBreakBefore())) {
                    return returnList;
                }
            } finally {
                bBreakBeforeServed = true;
            }
        }

        if (!bSpaceBeforeServed) {
            addKnuthElementsForSpaceBefore(returnList, returnPosition, alignment, 
                    fobj.getCommonMarginBlock().spaceBefore);
            bSpaceBeforeServed = true;
        }
        
        addKnuthElementForBorderPaddingBefore(returnList, returnPosition, 
                fobj.getCommonBorderPaddingBackground());

        while ((curLM = (BlockLevelLayoutManager) getChildLM()) != null) {
            LayoutContext childLC = new LayoutContext(0);
            if (curLM instanceof LineLayoutManager) {
                // curLM is a LineLayoutManager
                // set stackLimit for lines
                childLC.setStackLimit(new MinOptMax(ipd/*
                                                         * - iIndents -
                                                         * iTextIndent
                                                         */));
                childLC.setRefIPD(ipd);
            } else {
                // curLM is a ?
                childLC.setStackLimit(MinOptMax.subtract(context
                        .getStackLimit(), stackSize));
                childLC.setRefIPD(referenceIPD);
            }

            // get elements from curLM
            returnedList = curLM.getNextKnuthElements(childLC, alignment);
            if (returnedList.size() == 1
                    && ((KnuthElement) returnedList.getFirst()).isPenalty()
                    && ((KnuthPenalty) returnedList.getFirst()).getP() == -KnuthElement.INFINITE) {
                // a descendant of this block has break-before
                if (returnList.size() == 0) {
                    // the first child (or its first child ...) has
                    // break-before;
                    // all this block, including space before, will be put in
                    // the
                    // following page
                    bSpaceBeforeServed = false;
                }
                contentList.addAll(returnedList);

                /* extension: conversione di tutta la sequenza fin'ora ottenuta */
                if (bpUnit > 0) {
                    storedList = contentList;
                    contentList = createUnitElements(contentList);
                }
                /* end of extension */

                // "wrap" the Position inside each element
                // moving the elements from contentList to returnList
                returnedList = new LinkedList();
                wrapPositionElements(contentList, returnList);

                return returnList;
            } else {
                if (prevLM != null) {
                    // there is a block handled by prevLM
                    // before the one handled by curLM
                    if (mustKeepTogether() 
                            || prevLM.mustKeepWithNext()
                            || curLM.mustKeepWithPrevious()) {
                        // add an infinite penalty to forbid a break between
                        // blocks
                        contentList.add(new KnuthPenalty(0,
                                KnuthElement.INFINITE, false,
                                new Position(this), false));
                    } else if (!((KnuthElement) contentList.getLast()).isGlue()) {
                        // add a null penalty to allow a break between blocks
                        contentList.add(new KnuthPenalty(0, 0, false,
                                new Position(this), false));
                    } else {
                        // the last element in contentList is a glue;
                        // it is a feasible breakpoint, there is no need to add
                        // a penalty
                    }
                }
                contentList.addAll(returnedList);
                if (returnedList.size() == 0) {
                    //Avoid NoSuchElementException below (happens with empty blocks)
                    continue;
                }
                if (((KnuthElement) returnedList.getLast()).isPenalty()
                        && ((KnuthPenalty) returnedList.getLast()).getP() == -KnuthElement.INFINITE) {
                    // a descendant of this block has break-after
                    if (curLM.isFinished()) {
                        // there is no other content in this block;
                        // it's useless to add space after before a page break
                        setFinished(true);
                    }

                    /* extension: conversione di tutta la sequenza fin'ora ottenuta */
                    if (bpUnit > 0) {
                        storedList = contentList;
                        contentList = createUnitElements(contentList);
                    }
                    /* end of extension */

                    returnedList = new LinkedList();
                    wrapPositionElements(contentList, returnList);

                    return returnList;
                }
            }
            prevLM = curLM;
        }

        /* Extension: conversione di tutta la sequenza fin'ora ottenuta */
        if (bpUnit > 0) {
            storedList = contentList;
            contentList = createUnitElements(contentList);
        }
        /* end of extension */

        returnedList = new LinkedList();
        wrapPositionElements(contentList, returnList);

        addKnuthElementsForBorderPaddingAfter(returnList, returnPosition, 
                fobj.getCommonBorderPaddingBackground());
        addKnuthElementsForSpaceAfter(returnList, returnPosition, alignment, 
                fobj.getCommonMarginBlock().spaceAfter);
        addKnuthElementsForBreakAfter(returnList, returnPosition, fobj.getBreakAfter());

        setFinished(true);

        return returnList;
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
        fobj.setLayoutDimension(PercentBase.BLOCK_IPD, contentipd);
        fobj.setLayoutDimension(PercentBase.BLOCK_BPD, -1);

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
                    stackSize.add(new SpaceVal(fobj.getCommonMarginBlock().spaceAfter).getSpace());
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

    protected LinkedList createUnitElements(LinkedList oldList) {
        //System.out.println(" ");
        //System.out.println("Inizio conversione: " + oldList.size() + " elementi, spazio minimo prima= " + layoutProps.spaceBefore.getSpace().min
        //                   + " spazio minimo dopo= " + layoutProps.spaceAfter.getSpace().min);
        // add elements at the beginning and at the end of oldList
        // representing minimum spaces
        LayoutManager lm = ((KnuthElement)oldList.getFirst()).getLayoutManager();
        boolean bAddedBoxBefore = false;
        boolean bAddedBoxAfter = false;
        if (adjustedSpaceBefore > 0) {
            oldList.addFirst(new KnuthBox(adjustedSpaceBefore,
                                          new Position(lm), true));
            bAddedBoxBefore = true;
        }
        if (adjustedSpaceAfter > 0) {
            oldList.addLast(new KnuthBox(adjustedSpaceAfter,
                                         new Position(lm), true));
            bAddedBoxAfter = true;
        }

        MinOptMax totalLength = new MinOptMax(0);
        MinOptMax totalUnits = new MinOptMax(0);
        LinkedList newList = new LinkedList();

        //System.out.println(" ");
        //System.out.println(" Prima scansione");
        // scan the list once to compute total min, opt and max length
        ListIterator oldListIterator = oldList.listIterator();
        while (oldListIterator.hasNext()) {
            KnuthElement element = (KnuthElement) oldListIterator.next();
            if (element.isBox()) {
/*LF*/          totalLength.add(new MinOptMax(element.getW()));
/*LF*/          //System.out.println("box " + element.getW());               
/*LF*/      } else if (element.isGlue()) {
/*LF*/          totalLength.min -= ((KnuthGlue) element).getZ();
/*LF*/          totalLength.max += ((KnuthGlue) element).getY();
/*LF*/          //leafValue = ((LeafPosition) element.getPosition()).getLeafPos();
/*LF*/          //System.out.println("glue " + element.getW() + " + " + ((KnuthGlue) element).getY() + " - " + ((KnuthGlue) element).getZ());
/*LF*/      } else {
/*LF*/          //System.out.println((((KnuthPenalty)element).getP() == KnuthElement.INFINITE ? "PENALTY " : "penalty ") + element.getW());
            }
        }
        // compute the total amount of "units"
        totalUnits = new MinOptMax(neededUnits(totalLength.min),
                                   neededUnits(totalLength.opt),
                                   neededUnits(totalLength.max));
        //System.out.println(" totalLength= " + totalLength);
        //System.out.println(" unita'= " + totalUnits);

        //System.out.println(" ");
        //System.out.println(" Seconda scansione");
        // scan the list once more, stopping at every breaking point
        // in order to compute partial min, opt and max length
        // and create the new elements
        oldListIterator = oldList.listIterator();
        boolean bPrevIsBox = false;
        MinOptMax lengthBeforeBreak = new MinOptMax(0);
        MinOptMax lengthAfterBreak = (MinOptMax) totalLength.clone();
        MinOptMax unitsBeforeBreak;
        MinOptMax unitsAfterBreak;
        MinOptMax unsuppressibleUnits = new MinOptMax(0);
        int firstIndex = 0;
        int lastIndex = -1;
        while (oldListIterator.hasNext()) {
            KnuthElement element = (KnuthElement) oldListIterator.next();
            lastIndex ++;
            if (element.isBox()) {
                lengthBeforeBreak.add(new MinOptMax(element.getW()));
                lengthAfterBreak.subtract(new MinOptMax(element.getW()));
                bPrevIsBox = true;
            } else if (element.isGlue()) {
                lengthBeforeBreak.min -= ((KnuthGlue) element).getZ();
                lengthAfterBreak.min += ((KnuthGlue) element).getZ();
                lengthBeforeBreak.max += ((KnuthGlue) element).getY();
                lengthAfterBreak.max -= ((KnuthGlue) element).getY();
                bPrevIsBox = false;
            } else {
                lengthBeforeBreak.add(new MinOptMax(element.getW()));
                bPrevIsBox = false;
            }

            // create the new elements
            if (element.isPenalty() && ((KnuthPenalty) element).getP() < KnuthElement.INFINITE
                || element.isGlue() && bPrevIsBox
                || !oldListIterator.hasNext()) {
                // suppress elements after the breaking point
                int iStepsForward = 0;
                while (oldListIterator.hasNext()) {
                    KnuthElement el = (KnuthElement) oldListIterator.next();
                    iStepsForward++;
                    if (el.isGlue()) {
                        // suppressed glue
                        lengthAfterBreak.min += ((KnuthGlue) el).getZ();
                        lengthAfterBreak.max -= ((KnuthGlue) el).getY();
                    } else if (el.isPenalty()) {
                        // suppressed penalty, do nothing
                    } else {
                        // box, end of suppressions
                        break;
                    }
                }
                // compute the partial amount of "units" before and after the break
                unitsBeforeBreak = new MinOptMax(neededUnits(lengthBeforeBreak.min),
                                                 neededUnits(lengthBeforeBreak.opt),
                                                 neededUnits(lengthBeforeBreak.max));
                unitsAfterBreak = new MinOptMax(neededUnits(lengthAfterBreak.min),
                                                neededUnits(lengthAfterBreak.opt),
                                                neededUnits(lengthAfterBreak.max));

                // rewind the iterator and lengthAfterBreak
                for (int i = 0; i < iStepsForward; i++) {
                    KnuthElement el = (KnuthElement) oldListIterator.previous();
                    if (el.isGlue()) {
                        lengthAfterBreak.min -= ((KnuthGlue) el).getZ();
                        lengthAfterBreak.max += ((KnuthGlue) el).getY();
                    }
                }

                // compute changes in length, stretch and shrink
                int uLengthChange = unitsBeforeBreak.opt + unitsAfterBreak.opt - totalUnits.opt;
                int uStretchChange = (unitsBeforeBreak.max + unitsAfterBreak.max - totalUnits.max)
                                     - (unitsBeforeBreak.opt + unitsAfterBreak.opt - totalUnits.opt);
                int uShrinkChange = (unitsBeforeBreak.opt + unitsAfterBreak.opt - totalUnits.opt)
                                    - (unitsBeforeBreak.min + unitsAfterBreak.min - totalUnits.min);

                // compute the number of normal, stretch and shrink unit
                // that must be added to the new sequence
                int uNewNormal = unitsBeforeBreak.opt - unsuppressibleUnits.opt;
                int uNewStretch = (unitsBeforeBreak.max - unitsBeforeBreak.opt)
                                  - (unsuppressibleUnits.max - unsuppressibleUnits.opt);
                int uNewShrink = (unitsBeforeBreak.opt - unitsBeforeBreak.min)
                                 - (unsuppressibleUnits.opt - unsuppressibleUnits.min);

/*LF*/          //System.out.println("(" + unsuppressibleUnits.min + "-" + unsuppressibleUnits.opt + "-" +  unsuppressibleUnits.max + ") "
/*LF*/          //                   + " -> " + unitsBeforeBreak.min + "-" + unitsBeforeBreak.opt + "-" +  unitsBeforeBreak.max
/*LF*/          //                   + " + " + unitsAfterBreak.min + "-" + unitsAfterBreak.opt + "-" +  unitsAfterBreak.max
/*LF*/          //                   + (uLengthChange != 0 ? " [length " + uLengthChange + "] " : "")
/*LF*/          //                   + (uStretchChange != 0 ? " [stretch " + uStretchChange + "] " : "")
/*LF*/          //                   + (uShrinkChange != 0 ? " [shrink " + uShrinkChange + "]" : "")
/*LF*/          //                   );

                // create the MappingPosition which will be stored in the new elements
                // correct firstIndex and lastIndex
                int firstIndexCorrection = 0;
                int lastIndexCorrection = 0;
                if (bAddedBoxBefore) {
                    if (firstIndex != 0) {
                        firstIndexCorrection ++;
                    }
                    lastIndexCorrection ++;
                }
                if (bAddedBoxAfter && lastIndex == (oldList.size() - 1)) {
                    lastIndexCorrection ++;
                }
                MappingPosition mappingPos = new MappingPosition(this,
                                                                 firstIndex - firstIndexCorrection,
                                                                 lastIndex - lastIndexCorrection);

                // new box
                newList.add(new KnuthBox((uNewNormal - uLengthChange) * bpUnit,
                                         mappingPos,
                                         false));
                unsuppressibleUnits.add(new MinOptMax(uNewNormal - uLengthChange));
                //System.out.println("        box " + (uNewNormal - uLengthChange));

                // new infinite penalty, glue and box, if necessary
                if (uNewStretch - uStretchChange > 0
                    || uNewShrink - uShrinkChange > 0) {
                    int iStretchUnits = (uNewStretch - uStretchChange > 0 ? (uNewStretch - uStretchChange) : 0);
                    int iShrinkUnits = (uNewShrink - uShrinkChange > 0 ? (uNewShrink - uShrinkChange) : 0);
                    newList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                                 mappingPos,
                                                 false));
                    newList.add(new KnuthGlue(0,
                                              iStretchUnits * bpUnit,
                                              iShrinkUnits * bpUnit,
                                              LINE_NUMBER_ADJUSTMENT,
                                              mappingPos,
                                              false));
                    //System.out.println("        PENALTY");
                    //System.out.println("        glue 0 " + iStretchUnits + " " + iShrinkUnits);
                    unsuppressibleUnits.max += iStretchUnits;
                    unsuppressibleUnits.min -= iShrinkUnits;
                    if (!oldListIterator.hasNext()) {
                        newList.add(new KnuthBox(0,
                                                 mappingPos,
                                                 false));
                        //System.out.println("        box 0");
                    }
                }

                // new breaking sequence
                if (uStretchChange != 0
                    || uShrinkChange != 0) {
                    // new infinite penalty, glue, penalty and glue
                    newList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                                 mappingPos,
                                                 false));
                    newList.add(new KnuthGlue(0,
                                              uStretchChange * bpUnit,
                                              uShrinkChange * bpUnit,
                                              LINE_NUMBER_ADJUSTMENT,
                                              mappingPos,
                                              false));
                    newList.add(new KnuthPenalty(uLengthChange * bpUnit,
                                                 0, false, element.getPosition(), false));
                    newList.add(new KnuthGlue(0,
                                              - uStretchChange * bpUnit,
                                              - uShrinkChange * bpUnit,
                                              LINE_NUMBER_ADJUSTMENT,
                                              mappingPos,
                                              false));
                    //System.out.println("        PENALTY");
                    //System.out.println("        glue 0 " + uStretchChange + " " + uShrinkChange);
                    //System.out.println("        penalty " + uLengthChange + " * unit");
                    //System.out.println("        glue 0 " + (- uStretchChange) + " " + (- uShrinkChange));
                } else if (oldListIterator.hasNext()){
                    // new penalty
                    newList.add(new KnuthPenalty(uLengthChange * bpUnit,
                                                 0, false,
                                                 mappingPos,
                                                 false));
                    //System.out.println("        penalty " + uLengthChange + " * unit");
                }
                // update firstIndex
                firstIndex = lastIndex + 1;
            }

            if (element.isPenalty()) {
                lengthBeforeBreak.add(new MinOptMax(-element.getW()));
            }

        }

        // remove elements at the beginning and at the end of oldList
        // representing minimum spaces
        if (adjustedSpaceBefore > 0) {
            oldList.removeFirst();
        }
        if (adjustedSpaceAfter > 0) {
            oldList.removeLast();
        }

        // if space-before.conditionality is "discard", correct newList
        if (fobj.getCommonMarginBlock().spaceBefore.getSpace().isDiscard()) {
            // remove the wrong element
            KnuthBox wrongBox = (KnuthBox) newList.removeFirst();
            // if this paragraph is at the top of a page, the space before
            // must be ignored; compute the length change
            int decreasedLength = (neededUnits(totalLength.opt)
                                   - neededUnits(totalLength.opt - adjustedSpaceBefore))
                                  * bpUnit;
            // insert the correct elements
            newList.addFirst(new KnuthBox(wrongBox.getW() - decreasedLength,
                                          wrongBox.getPosition(), false));
            newList.addFirst(new KnuthGlue(decreasedLength, 0, 0, SPACE_BEFORE_ADJUSTMENT,
                                           wrongBox.getPosition(), false));
            //System.out.println("        rimosso box " + neededUnits(wrongBox.getW()));
            //System.out.println("        aggiunto glue " + neededUnits(decreasedLength) + " 0 0");
            //System.out.println("        aggiunto box " + neededUnits(wrongBox.getW() - decreasedLength));
        }

        // if space-after.conditionality is "discard", correct newList
        if (fobj.getCommonMarginBlock().spaceAfter.getSpace().isDiscard()) {
            // remove the wrong element
            KnuthBox wrongBox = (KnuthBox) newList.removeLast();
            // if the old sequence is box(h) penalty(inf) glue(x,y,z) box(0)
            // (it cannot be parted and has some stretch or shrink)
            // the wrong box is the first one, not the last one
            LinkedList preserveList = new LinkedList();
            if (wrongBox.getW() == 0) {
                preserveList.add(wrongBox);
                preserveList.addFirst((KnuthGlue) newList.removeLast());
                preserveList.addFirst((KnuthPenalty) newList.removeLast());
                wrongBox = (KnuthBox) newList.removeLast();
            }

            // if this paragraph is at the bottom of a page, the space after
            // must be ignored; compute the length change
            int decreasedLength = (neededUnits(totalLength.opt)
                                   - neededUnits(totalLength.opt - adjustedSpaceAfter))
                                  * bpUnit;
            // insert the correct box
            newList.addLast(new KnuthBox(wrongBox.getW() - decreasedLength,
                                         wrongBox.getPosition(), false));
            // add preserved elements
            if (preserveList.size() > 0) {
                newList.addAll(preserveList);
            }
            // insert the correct glue
            newList.addLast(new KnuthGlue(decreasedLength, 0, 0, SPACE_AFTER_ADJUSTMENT,
                                          wrongBox.getPosition(), false));
            //System.out.println("        rimosso box " + neededUnits(wrongBox.getW()));
            //System.out.println("        aggiunto box " + neededUnits(wrongBox.getW() - decreasedLength));
            //System.out.println("        aggiunto glue " + neededUnits(decreasedLength) + " 0 0");
        }

        return newList;
    }
/*LF*/

    public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
/*LF*/  //System.out.println("  BLM.negotiateBPDAdjustment> " + adj);
/*LF*/  //System.out.println("  lastElement e' " + (lastElement.isPenalty() ? "penalty" : (lastElement.isGlue() ? "glue" : "box" )));
/*LF*/  //System.out.println("  position e' " + lastElement.getPosition().getClass().getName());
/*LF*/  //System.out.println("  " + (bpUnit > 0 ? "unit" : ""));
        Position innerPosition = ((NonLeafPosition) lastElement.getPosition()).getPosition();

        if (innerPosition == null && lastElement.isGlue()) {
            // this adjustment applies to space-before or space-after of this block
            if (((KnuthGlue) lastElement).getAdjustmentClass() == SPACE_BEFORE_ADJUSTMENT) {
                // this adjustment applies to space-before
                adjustedSpaceBefore += adj;
/*LF*/          //System.out.println("  BLM.negotiateBPDAdjustment> spazio prima: " + adj);
            } else {
                // this adjustment applies to space-after
                adjustedSpaceAfter += adj;
/*LF*/          //System.out.println("  BLM.negotiateBPDAdjustment> spazio dopo: " + adj);
            }
            return adj;
        } else if (innerPosition instanceof MappingPosition) {
            // this block has block-progression-unit > 0: the adjustment can concern
            // - the space-before or space-after of this block, 
            // - the line number of a descendant of this block
            if (lastElement.isGlue()) {
                // lastElement is a glue
/*LF*/          //System.out.println("  BLM.negotiateBPDAdjustment> bpunit con glue");
                ListIterator storedListIterator = storedList.listIterator(((MappingPosition) innerPosition).getFirstIndex());
                int newAdjustment = 0;
                while (storedListIterator.nextIndex() <= ((MappingPosition) innerPosition).getLastIndex()) {
                    KnuthElement storedElement = (KnuthElement) storedListIterator.next();
                    if (storedElement.isGlue()) {
                        newAdjustment += ((BlockLevelLayoutManager) storedElement.getLayoutManager()).negotiateBPDAdjustment(adj - newAdjustment, storedElement);
/*LF*/                  //System.out.println("  BLM.negotiateBPDAdjustment> (progressivo) righe: " + newAdjustment);
                    }
                }
                newAdjustment = (newAdjustment > 0 ? bpUnit * neededUnits(newAdjustment)
                                                   : - bpUnit * neededUnits(- newAdjustment));
                return newAdjustment;
            } else {
                // lastElement is a penalty: this means that the paragraph
                // has been split between consecutive pages:
                // this may involve a change in the number of lines
/*LF*/          //System.out.println("  BLM.negotiateBPDAdjustment> bpunit con penalty");
                KnuthPenalty storedPenalty = (KnuthPenalty)
                                             storedList.get(((MappingPosition) innerPosition).getLastIndex());
                if (storedPenalty.getW() > 0) {
                    // the original penalty has width > 0
/*LF*/              //System.out.println("  BLM.negotiateBPDAdjustment> chiamata passata");
                    return ((BlockLevelLayoutManager) storedPenalty.getLayoutManager())
                           .negotiateBPDAdjustment(storedPenalty.getW(), (KnuthElement) storedPenalty);
                } else {
                    // the original penalty has width = 0
                    // the adjustment involves only the spaces before and after
/*LF*/              //System.out.println("  BLM.negotiateBPDAdjustment> chiamata gestita");
                    return adj;
                }
            }
        } else if (innerPosition.getLM() != this) {
            // this adjustment concerns another LM
            NonLeafPosition savedPos = (NonLeafPosition) lastElement.getPosition();
            lastElement.setPosition(innerPosition);
            int returnValue = ((BlockLevelLayoutManager) lastElement.getLayoutManager()).negotiateBPDAdjustment(adj, lastElement);
            lastElement.setPosition(savedPos);
/*LF*/      //System.out.println("  BLM.negotiateBPDAdjustment> righe: " + returnValue);
            return returnValue;
        } else {
            // this should never happen
            System.err.println("BlockLayoutManager.negotiateBPDAdjustment(): unexpected Position");
            return 0;
        }
    }

    public void discardSpace(KnuthGlue spaceGlue) {
/*LF*/  //System.out.println("  BLM.discardSpace> " + spaceGlue.getPosition().getClass().getName());
        Position innerPosition = ((NonLeafPosition) spaceGlue.getPosition()).getPosition();

/*LF*/  if (innerPosition == null || innerPosition.getLM() == this) {
            // if this block has block-progression-unit > 0, innerPosition can be
            // a MappingPosition
            // spaceGlue represents space before or space after of this block
            if (spaceGlue.getAdjustmentClass() == SPACE_BEFORE_ADJUSTMENT) {
                // space-before must be discarded
                adjustedSpaceBefore = 0;
            } else {
                // space-after must be discarded
                adjustedSpaceAfter = 0;
                //TODO Why are both cases handled in the same way?
            }
/*LF*/  } else {
            // this element was not created by this BlockLM
            NonLeafPosition savedPos = (NonLeafPosition)spaceGlue.getPosition();
            spaceGlue.setPosition(innerPosition);
            ((BlockLevelLayoutManager) spaceGlue.getLayoutManager()).discardSpace(spaceGlue);
            spaceGlue.setPosition(savedPos);
        }
    }

    public LinkedList getChangedKnuthElements(List oldList, /*int flaggedPenalty,*/ int alignment) {
/*LF*/  //System.out.println("");
/*LF*/  //System.out.println("  BLM.getChangedKnuthElements> inizio: oldList.size() = " + oldList.size());
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement returnedElement;
        KnuthElement currElement = null;
        KnuthElement prevElement = null;
        LinkedList returnedList = new LinkedList();
        LinkedList returnList = new LinkedList();
        int fromIndex = 0;

        // "unwrap" the Positions stored in the elements
        KnuthElement oldElement = null;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement)oldListIterator.next();
            Position innerPosition = ((NonLeafPosition) oldElement.getPosition()).getPosition();
/*LF*/      //System.out.println(" BLM> unwrapping: " + (oldElement.isBox() ? "box    " : (oldElement.isGlue() ? "glue   " : "penalty")) + " creato da " + oldElement.getLayoutManager().getClass().getName());
/*LF*/      //System.out.println(" BLM> unwrapping:         " + oldElement.getPosition().getClass().getName());
            if (innerPosition != null) {
                // oldElement was created by a descendant of this BlockLM
                oldElement.setPosition(innerPosition);
            } else {
                // thisElement was created by this BlockLM
                // modify its position in order to recognize it was not created
                // by a child
                oldElement.setPosition(new Position(this));
            }
        }

        // create the iterator
        List workList;
        if (bpUnit == 0) {
            workList = oldList;
        } else {
            // the storedList must be used instead of oldList;
            // find the index of the first element of returnedList
            // corresponding to the first element of oldList
            oldListIterator = oldList.listIterator();
            KnuthElement el = (KnuthElement) oldListIterator.next();
            while (!(el.getPosition() instanceof MappingPosition)) {
                el = (KnuthElement) oldListIterator.next();
            }
            int iFirst = ((MappingPosition) el.getPosition()).getFirstIndex();

            // find the index of the last element of returnedList
            // corresponding to the last element of oldList
            oldListIterator = oldList.listIterator(oldList.size());
            el = (KnuthElement) oldListIterator.previous();
            while (!(el.getPosition() instanceof MappingPosition)) {
                el = (KnuthElement) oldListIterator.previous();
            }
            int iLast = ((MappingPosition) el.getPosition()).getLastIndex();

/*LF*/      //System.out.println("  si usa storedList da " + iFirst + " a " + iLast + " compresi su " + storedList.size() + " elementi totali");
            workList = storedList.subList(iFirst, iLast + 1);
        }
        ListIterator workListIterator = workList.listIterator();

/*LF*/  //System.out.println("");
/*LF*/  //System.out.println("  BLM.getChangedKnuthElements> workList.size() = " + workList.size() + " da 0 a " + (workList.size() - 1));

        while (workListIterator.hasNext()) {
            currElement = (KnuthElement) workListIterator.next();
/*LF*/      //System.out.println("elemento n. " + workListIterator.previousIndex() + " nella workList");
            if (prevElement != null
                && prevElement.getLayoutManager() != currElement.getLayoutManager()) {
                // prevElement is the last element generated by the same LM
                BlockLevelLayoutManager prevLM = (BlockLevelLayoutManager)
                                                 prevElement.getLayoutManager();
                BlockLevelLayoutManager currLM = (BlockLevelLayoutManager)
                                                 currElement.getLayoutManager();
                boolean bSomethingAdded = false;
                if (prevLM != this) {
/*LF*/              //System.out.println(" BLM.getChangedKnuthElements> chiamata da " + fromIndex + " a " + workListIterator.previousIndex() + " su " + prevLM.getClass().getName());
                    returnedList.addAll(prevLM.getChangedKnuthElements(workList.subList(fromIndex, workListIterator.previousIndex()),
                                                                       /*flaggedPenalty,*/ alignment));
                    bSomethingAdded = true;
                } else {
                    // prevLM == this
                    // do nothing
/*LF*/              //System.out.println(" BLM.getChangedKnuthElements> elementi propri, ignorati, da " + fromIndex + " a " + workListIterator.previousIndex() + " su " + prevLM.getClass().getName());
                }
                fromIndex = workListIterator.previousIndex();

                // there is another block after this one
                if (bSomethingAdded
                    && (this.mustKeepTogether()
                        || prevLM.mustKeepWithNext()
                        || currLM.mustKeepWithPrevious())) {
                    // add an infinite penalty to forbid a break between blocks
                    returnedList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, new Position(this), false));
                } else if (bSomethingAdded && !((KnuthElement) returnedList.getLast()).isGlue()) {
                    // add a null penalty to allow a break between blocks
                    returnedList.add(new KnuthPenalty(0, 0, false, new Position(this), false));
                }
            }
            prevElement = currElement;
        }
        if (currElement != null) {
            BlockLevelLayoutManager currLM = (BlockLevelLayoutManager)
                                             currElement.getLayoutManager();
            if (currLM != this) {
/*LF*/          //System.out.println(" BLM.getChangedKnuthElements> chiamata da " + fromIndex + " a " + oldList.size() + " su " + currLM.getClass().getName());
                returnedList.addAll(currLM.getChangedKnuthElements(workList.subList(fromIndex, workList.size()),
                                                                   /*flaggedPenalty,*/ alignment));
            } else {
                // currLM == this
                // there are no more elements to add
                // remove the last penalty added to returnedList
                if (returnedList.size() > 0) {
                    returnedList.removeLast();
                }
/*LF*/          //System.out.println(" BLM.getChangedKnuthElements> elementi propri, ignorati, da " + fromIndex + " a " + workList.size());
            }
        }

        // append elements representing space-before
        boolean spaceBeforeIsConditional = fobj.getCommonMarginBlock().spaceBefore.getSpace().isDiscard();
        if (bpUnit > 0
            || adjustedSpaceBefore != 0) {
            if (!spaceBeforeIsConditional) {
                // add elements to prevent the glue to be discarded
                returnList.add(new KnuthBox(0,
                                            new NonLeafPosition(this, null), false));
                returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                                new NonLeafPosition(this, null), false));
            }
            if (bpUnit > 0) {
                returnList.add(new KnuthGlue(0, 0, 0,
                                             SPACE_BEFORE_ADJUSTMENT, new NonLeafPosition(this, null), true));
            } else {
                returnList.add(new KnuthGlue(adjustedSpaceBefore, 0, 0,
                                             SPACE_BEFORE_ADJUSTMENT, new NonLeafPosition(this, null), true));
            }
        }

/*LF*/  //System.out.println("  BLM.getChangedKnuthElements> intermedio: returnedList.size() = " + returnedList.size());

/* estensione: conversione complessiva */
/*LF*/  if (bpUnit > 0) {
/*LF*/      storedList = returnedList;
/*LF*/      returnedList = createUnitElements(returnedList);
/*LF*/  }
/* estensione */

        // "wrap" the Position stored in each element of returnedList
        // and add elements to returnList
        ListIterator listIter = returnedList.listIterator();
        while (listIter.hasNext()) {
            returnedElement = (KnuthElement)listIter.next();
            returnedElement.setPosition(new NonLeafPosition(this, returnedElement.getPosition()));
            returnList.add(returnedElement);
        }

        // append elements representing space-after
        boolean spaceAfterIsConditional = fobj.getCommonMarginBlock().spaceAfter.getSpace().isDiscard();
        if (bpUnit > 0
            || adjustedSpaceAfter != 0) {
            if (!spaceAfterIsConditional) {
                returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                                new NonLeafPosition(this, null), false));
            }
            if (bpUnit > 0) {
                returnList.add(new KnuthGlue(0, 0, 0,
                                             SPACE_AFTER_ADJUSTMENT, new NonLeafPosition(this, null),
                                             (!spaceAfterIsConditional) ? false : true));
            } else {
                returnList.add(new KnuthGlue(adjustedSpaceAfter, 0, 0,
                                             SPACE_AFTER_ADJUSTMENT, new NonLeafPosition(this, null),
                                             (!spaceAfterIsConditional) ? false : true));
            }
            if (!spaceAfterIsConditional) {
                returnList.add(new KnuthBox(0,
                                            new NonLeafPosition(this, null), true));
            }
        }

/*LF*/  //System.out.println("  BLM.getChangedKnuthElements> fine: returnList.size() = " + returnList.size());
        return returnList;
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepTogether()
     */
    public boolean mustKeepTogether() {
        return ((BlockLevelLayoutManager)getParent()).mustKeepTogether() 
                || !fobj.getKeepTogether().getWithinPage().isAuto();
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithPrevious()
     */
    public boolean mustKeepWithPrevious() {
        return !fobj.getKeepWithPrevious().getWithinPage().isAuto();
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithNext()
     */
    public boolean mustKeepWithNext() {
        return !fobj.getKeepWithNext().getWithinPage().isAuto();
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
            addID(fobj.getId());
            addMarkers(true, bp1.isFirstArea(), bp1.isLastArea());
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
                addMarkers(false, bp1.isFirstArea(), bp1.isLastArea());
            }
            flush();

            // if adjusted space after
            foBlockSpaceAfter = new SpaceVal(fobj.getCommonMarginBlock().spaceAfter).getSpace();
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

        addID(fobj.getId());
        //addMarkers(true, bp1.isFirstArea(), bp1.isLastArea());
        addMarkers(true, true, false);

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
            /* LF *///System.out.println("pos = " + pos.getClass().getName());
            Position innerPosition = ((NonLeafPosition) pos).getPosition();
            if (innerPosition == null) {
                // pos was created by this BlockLM and was inside an element
                // representing space before or after
                // this means the space was not discarded
                if (positionList.size() == 0) {
                    // pos was in the element representing space-before
                    bSpaceBefore = true;
                    /* LF *///System.out.println(" spazio prima");
                } else {
                    // pos was in the element representing space-after
                    bSpaceAfter = true;
                    /* LF *///System.out.println(" spazio dopo");
                }
            } else if (innerPosition.getLM() == this
                    && !(innerPosition instanceof MappingPosition)) {
                // pos was created by this BlockLM and was inside a penalty
                // allowing or forbidding a page break
                // nothing to do
                /* LF *///System.out.println(" penalty");
            } else {
                // innerPosition was created by another LM
                positionList.add(innerPosition);
                lastLM = innerPosition.getLM();
                /* LF *///System.out.println(" " +
                      // innerPosition.getClass().getName());
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
                foBlockSpaceBefore = new SpaceVal(fobj.getCommonMarginBlock().spaceBefore).getSpace();
                foBlockSpaceAfter = new SpaceVal(fobj.getCommonMarginBlock().spaceAfter).getSpace();
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

        int bIndents = fobj.getCommonBorderPaddingBackground().getBPPaddingAndBorder(false);

        addMarkers(false, false, true);

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
                    fobj.getBreakBefore(), fobj.getBreakAfter());

            // Must get dimensions from parent area
            //Don't optimize this line away. It can have ugly side-effects.
            /*Area parentArea =*/ parentLM.getParentArea(curBlockArea);

            // set traits
            TraitSetter.addBorders(curBlockArea, 
                    fobj.getCommonBorderPaddingBackground());
            TraitSetter.addBackground(curBlockArea, 
                    fobj.getCommonBorderPaddingBackground());
            TraitSetter.addMargins(curBlockArea,
                    fobj.getCommonBorderPaddingBackground(), 
                    fobj.getCommonMarginBlock());

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
}

