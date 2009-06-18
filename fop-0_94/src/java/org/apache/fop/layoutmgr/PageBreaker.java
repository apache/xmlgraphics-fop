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

package org.apache.fop.layoutmgr;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.fop.area.Block;
import org.apache.fop.area.Footnote;
import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.layoutmgr.PageBreakingAlgorithm.PageBreakingLayoutListener;
import org.apache.fop.traits.MinOptMax;

/**
 * Handles the breaking of pages in an fo:flow
 */
public class PageBreaker extends AbstractBreaker {
    
    private PageSequenceLayoutManager pslm;
    private boolean firstPart = true;
    private boolean pageBreakHandled;
    private boolean needColumnBalancing;
    private PageProvider pageProvider;
    private Block separatorArea;
    
    /**
     * The FlowLayoutManager object, which processes
     * the single fo:flow of the fo:page-sequence
     */
    private FlowLayoutManager childFLM = null;

    private StaticContentLayoutManager footnoteSeparatorLM = null;

    public PageBreaker(PageSequenceLayoutManager pslm) {
        this.pslm = pslm;
        this.pageProvider = pslm.getPageProvider();
        this.childFLM = pslm.getLayoutManagerMaker().makeFlowLayoutManager(
                pslm, pslm.getPageSequence().getMainFlow());
    }
    
    /** @see org.apache.fop.layoutmgr.AbstractBreaker */
    protected void updateLayoutContext(LayoutContext context) {
        int flowIPD = pslm.getCurrentPV().getCurrentSpan().getColumnWidth();
        context.setRefIPD(flowIPD);
    }
    
    /** @see org.apache.fop.layoutmgr.AbstractBreaker#getTopLevelLM() */
    protected LayoutManager getTopLevelLM() {
        return pslm;
    }
    
    /** @see org.apache.fop.layoutmgr.AbstractBreaker#getPageProvider() */
    protected PageProvider getPageProvider() {
        return pslm.getPageProvider();
    }
    
    /**
     * @see org.apache.fop.layoutmgr.AbstractBreaker#getLayoutListener()
     */
    protected PageBreakingLayoutListener getLayoutListener() {
        return new PageBreakingLayoutListener() {

            public void notifyOverflow(int part, FObj obj) {
                Page p = pageProvider.getPage(
                            false, part, PageProvider.RELTO_CURRENT_ELEMENT_LIST);
                RegionBody body = (RegionBody)p.getSimplePageMaster().getRegion(
                        Region.FO_REGION_BODY);
                String err = FONode.decorateWithContextInfo(
                        "Content of the region-body on page " 
                        + p.getPageViewport().getPageNumberString() 
                        + " overflows the available area in block-progression dimension.", 
                        obj);
                if (body.getOverflow() == Constants.EN_ERROR_IF_OVERFLOW) {
                    throw new RuntimeException(err);
                } else {
                    log.warn(err);
                }
            }
            
        };
    }

    /** @see org.apache.fop.layoutmgr.AbstractBreaker */
    protected int handleSpanChange(LayoutContext childLC, int nextSequenceStartsOn) {
        needColumnBalancing = false;
        if (childLC.getNextSpan() != Constants.NOT_SET) {
            //Next block list will have a different span.
            nextSequenceStartsOn = childLC.getNextSpan();
            needColumnBalancing = (childLC.getNextSpan() == Constants.EN_ALL);
        }
        if (needColumnBalancing) {
            AbstractBreaker.log.debug(
                    "Column balancing necessary for the next element list!!!");
        }
        return nextSequenceStartsOn;
    }

    /** @see org.apache.fop.layoutmgr.AbstractBreaker */
    protected int getNextBlockList(LayoutContext childLC, 
            int nextSequenceStartsOn) {
        if (!firstPart) {
            // if this is the first page that will be created by
            // the current BlockSequence, it could have a break
            // condition that must be satisfied;
            // otherwise, we may simply need a new page
            handleBreakTrait(nextSequenceStartsOn);
        }
        firstPart = false;
        pageBreakHandled = true;
        pageProvider.setStartOfNextElementList(pslm.getCurrentPageNum(), 
                pslm.getCurrentPV().getCurrentSpan().getCurrentFlowIndex());
        return super.getNextBlockList(childLC, nextSequenceStartsOn);
    }
    
    /** @see org.apache.fop.layoutmgr.AbstractBreaker */
    protected LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        LinkedList contentList = null;
        
        while (!childFLM.isFinished() && contentList == null) {
            contentList = childFLM.getNextKnuthElements(context, alignment);
        }

        // scan contentList, searching for footnotes
        boolean bFootnotesPresent = false;
        if (contentList != null) {
            ListIterator contentListIterator = contentList.listIterator();
            while (contentListIterator.hasNext()) {
                ListElement element = (ListElement) contentListIterator.next();
                if (element instanceof KnuthBlockBox
                    && ((KnuthBlockBox) element).hasAnchors()) {
                    // element represents a line with footnote citations
                    bFootnotesPresent = true;
                    LayoutContext footnoteContext = new LayoutContext(context);
                    footnoteContext.setStackLimit(context.getStackLimit());
                    footnoteContext.setRefIPD(pslm.getCurrentPV()
                            .getRegionReference(Constants.FO_REGION_BODY).getIPD());
                    LinkedList footnoteBodyLMs = ((KnuthBlockBox) element).getFootnoteBodyLMs();
                    ListIterator footnoteBodyIterator = footnoteBodyLMs.listIterator();
                    // store the lists of elements representing the footnote bodies
                    // in the box representing the line containing their references
                    while (footnoteBodyIterator.hasNext()) {
                        FootnoteBodyLayoutManager fblm 
                            = (FootnoteBodyLayoutManager) footnoteBodyIterator.next();
                        fblm.setParent(childFLM);
                        fblm.initialize();
                        ((KnuthBlockBox) element).addElementList(
                                fblm.getNextKnuthElements(footnoteContext, alignment));
                    }
                }
            }
        }

        if (bFootnotesPresent) {
            // handle the footnote separator
            StaticContent footnoteSeparator;
            footnoteSeparator = pslm.getPageSequence().getStaticContent("xsl-footnote-separator");
            if (footnoteSeparator != null) {
                // the footnote separator can contain page-dependent content such as
                // page numbers or retrieve markers, so its areas cannot simply be 
                // obtained now and repeated in each page;
                // we need to know in advance the separator bpd: the actual separator
                // could be different from page to page, but its bpd would likely be
                // always the same

                // create a Block area that will contain the separator areas
                separatorArea = new Block();
                separatorArea.setIPD(pslm.getCurrentPV()
                            .getRegionReference(Constants.FO_REGION_BODY).getIPD());
                // create a StaticContentLM for the footnote separator
                footnoteSeparatorLM = (StaticContentLayoutManager)
                    pslm.getLayoutManagerMaker().makeStaticContentLayoutManager(
                    pslm, footnoteSeparator, separatorArea);
                footnoteSeparatorLM.doLayout();

                footnoteSeparatorLength = new MinOptMax(separatorArea.getBPD());
            }
        }
        return contentList;
    }
    
    /**
     * @return current display alignment
     */
    protected int getCurrentDisplayAlign() {
        return pslm.getCurrentPage().getSimplePageMaster().getRegion(
                Constants.FO_REGION_BODY).getDisplayAlign();
    }
    
    /**
     * @return whether or not this flow has more page break opportunities
     */
    protected boolean hasMoreContent() {
        return !childFLM.isFinished();
    }
    
    /**
     * Adds an area to the flow layout manager
     * @param posIter the position iterator
     * @param context the layout context
     */
    protected void addAreas(PositionIterator posIter, LayoutContext context) {
        if (footnoteSeparatorLM != null) {
            StaticContent footnoteSeparator = pslm.getPageSequence().getStaticContent(
                    "xsl-footnote-separator");
            // create a Block area that will contain the separator areas
            separatorArea = new Block();
            separatorArea.setIPD(
                    pslm.getCurrentPV().getRegionReference(Constants.FO_REGION_BODY).getIPD());
            // create a StaticContentLM for the footnote separator
            footnoteSeparatorLM = (StaticContentLayoutManager)
                pslm.getLayoutManagerMaker().makeStaticContentLayoutManager(
                pslm, footnoteSeparator, separatorArea);
            footnoteSeparatorLM.doLayout();
        }

        childFLM.addAreas(posIter, context);    
    }
    
    /**
     * Performs phase 3 operation
     * 
     * @param alg page breaking algorithm
     * @param partCount part count
     * @param originalList the block sequence original list
     * @param effectiveList the block sequence effective list
     */
    protected void doPhase3(PageBreakingAlgorithm alg, int partCount, 
            BlockSequence originalList, BlockSequence effectiveList) {
        if (needColumnBalancing) {
            doPhase3WithColumnBalancing(alg, partCount, originalList, effectiveList);
        } else {
            if (!hasMoreContent() && pslm.getPageSequence().hasPagePositionLast()) {
                //last part is reached and we have a "last page" condition
                doPhase3WithLastPage(alg, partCount, originalList, effectiveList);
            } else {
                //Directly add areas after finding the breaks
                addAreas(alg, partCount, originalList, effectiveList);
            }
        }
    }

    private void doPhase3WithLastPage(PageBreakingAlgorithm alg, int partCount, 
            BlockSequence originalList, BlockSequence effectiveList) {
        int newStartPos;
        int restartPoint = pageProvider.getStartingPartIndexForLastPage(partCount);
        if (restartPoint > 0) {
            //Add definitive areas before last page
            addAreas(alg, restartPoint, originalList, effectiveList);
            //Get page break from which we restart
            PageBreakPosition pbp = (PageBreakPosition)
                    alg.getPageBreaks().get(restartPoint - 1);
            newStartPos = pbp.getLeafPos();
            //Handle page break right here to avoid any side-effects
            if (newStartPos > 0) {
                handleBreakTrait(Constants.EN_PAGE);
            }
        } else {
            newStartPos = 0;
        }
        AbstractBreaker.log.debug("Last page handling now!!!");
        AbstractBreaker.log.debug("===================================================");
        AbstractBreaker.log.debug("Restarting at " + restartPoint 
                + ", new start position: " + newStartPos);

        pageBreakHandled = true;
        //Update so the available BPD is reported correctly
        int currentPageNum = pslm.getCurrentPageNum();
        pageProvider.setStartOfNextElementList(currentPageNum, 
                pslm.getCurrentPV().getCurrentSpan().getCurrentFlowIndex());
        pageProvider.setLastPageIndex(currentPageNum);

        //Restart last page
        PageBreakingAlgorithm algRestart = new PageBreakingAlgorithm(
                getTopLevelLM(),
                getPageProvider(), getLayoutListener(),
                alg.getAlignment(), alg.getAlignmentLast(), 
                footnoteSeparatorLength,
                isPartOverflowRecoveryActivated(), false, false);
        //alg.setConstantLineWidth(flowBPD);
        int iOptPageCount = algRestart.findBreakingPoints(effectiveList,
                    newStartPos,
                    1, true, BreakingAlgorithm.ALL_BREAKS);
        AbstractBreaker.log.debug("restart: iOptPageCount= " + iOptPageCount
                + " pageBreaks.size()= " + algRestart.getPageBreaks().size());
        boolean replaceLastPage 
                = iOptPageCount <= pslm.getCurrentPV().getBodyRegion().getColumnCount(); 
        if (replaceLastPage) {
            //Replace last page
            pslm.setCurrentPage(pageProvider.getPage(false, currentPageNum));
            //Make sure we only add the areas we haven't added already
            effectiveList.ignoreAtStart = newStartPos;
            addAreas(algRestart, iOptPageCount, originalList, effectiveList);
        } else {
            effectiveList.ignoreAtStart = newStartPos;
            addAreas(alg, restartPoint, partCount - restartPoint, originalList, effectiveList);
            //Add blank last page
            pageProvider.setLastPageIndex(currentPageNum + 1);
            pslm.setCurrentPage(pslm.makeNewPage(true, true));
        }
        AbstractBreaker.log.debug("===================================================");
    }

    private void doPhase3WithColumnBalancing(PageBreakingAlgorithm alg, int partCount, 
            BlockSequence originalList, BlockSequence effectiveList) {
        AbstractBreaker.log.debug("Column balancing now!!!");
        AbstractBreaker.log.debug("===================================================");
        int newStartPos;
        int restartPoint = pageProvider.getStartingPartIndexForLastPage(partCount);
        if (restartPoint > 0) {
            //Add definitive areas
            addAreas(alg, restartPoint, originalList, effectiveList);
            //Get page break from which we restart
            PageBreakPosition pbp = (PageBreakPosition)
                    alg.getPageBreaks().get(restartPoint - 1);
            newStartPos = pbp.getLeafPos();
            //Handle page break right here to avoid any side-effects
            if (newStartPos > 0) {
                handleBreakTrait(Constants.EN_PAGE);
            }
        } else {
            newStartPos = 0;
        }
        AbstractBreaker.log.debug("Restarting at " + restartPoint 
                + ", new start position: " + newStartPos);

        pageBreakHandled = true;
        //Update so the available BPD is reported correctly
        pageProvider.setStartOfNextElementList(pslm.getCurrentPageNum(), 
                pslm.getCurrentPV().getCurrentSpan().getCurrentFlowIndex());

        //Restart last page
        PageBreakingAlgorithm algRestart = new BalancingColumnBreakingAlgorithm(
                getTopLevelLM(),
                getPageProvider(), getLayoutListener(),
                alignment, Constants.EN_START, footnoteSeparatorLength,
                isPartOverflowRecoveryActivated(),
                pslm.getCurrentPV().getBodyRegion().getColumnCount());
        //alg.setConstantLineWidth(flowBPD);
        int iOptPageCount = algRestart.findBreakingPoints(effectiveList,
                    newStartPos,
                    1, true, BreakingAlgorithm.ALL_BREAKS);
        AbstractBreaker.log.debug("restart: iOptPageCount= " + iOptPageCount
                + " pageBreaks.size()= " + algRestart.getPageBreaks().size());
        if (iOptPageCount > pslm.getCurrentPV().getBodyRegion().getColumnCount()) {
            AbstractBreaker.log.warn(
                    "Breaking algorithm produced more columns than are available.");
            /* reenable when everything works
            throw new IllegalStateException(
                    "Breaking algorithm must not produce more columns than available.");
            */
        }
        //Make sure we only add the areas we haven't added already
        effectiveList.ignoreAtStart = newStartPos;
        addAreas(algRestart, iOptPageCount, originalList, effectiveList);
        AbstractBreaker.log.debug("===================================================");
    }
    
    protected void startPart(BlockSequence list, int breakClass) {
        AbstractBreaker.log.debug("startPart() breakClass=" + breakClass);
        if (pslm.getCurrentPage() == null) {
            throw new IllegalStateException("curPage must not be null");
        }
        if (!pageBreakHandled) {
            
            //firstPart is necessary because we need the first page before we start the 
            //algorithm so we have a BPD and IPD. This may subject to change later when we
            //start handling more complex cases.
            if (!firstPart) {
                // if this is the first page that will be created by
                // the current BlockSequence, it could have a break
                // condition that must be satisfied;
                // otherwise, we may simply need a new page
                handleBreakTrait(breakClass);
            }
            pageProvider.setStartOfNextElementList(pslm.getCurrentPageNum(), 
                    pslm.getCurrentPV().getCurrentSpan().getCurrentFlowIndex());
        }
        pageBreakHandled = false;
        // add static areas and resolve any new id areas
        // finish page and add to area tree
        firstPart = false;
    }
    
    /** @see org.apache.fop.layoutmgr.AbstractBreaker#handleEmptyContent() */
    protected void handleEmptyContent() {
        pslm.getCurrentPV().getPage().fakeNonEmpty();
    }
    
    protected void finishPart(PageBreakingAlgorithm alg, PageBreakPosition pbp) {
        // add footnote areas
        if (pbp.footnoteFirstListIndex < pbp.footnoteLastListIndex
            || pbp.footnoteFirstElementIndex <= pbp.footnoteLastElementIndex) {
            // call addAreas() for each FootnoteBodyLM
            for (int i = pbp.footnoteFirstListIndex; i <= pbp.footnoteLastListIndex; i++) {
                LinkedList elementList = alg.getFootnoteList(i);
                int firstIndex = (i == pbp.footnoteFirstListIndex 
                        ? pbp.footnoteFirstElementIndex : 0);
                int lastIndex = (i == pbp.footnoteLastListIndex 
                        ? pbp.footnoteLastElementIndex : elementList.size() - 1);

                SpaceResolver.performConditionalsNotification(elementList, 
                        firstIndex, lastIndex, -1);
                LayoutContext childLC = new LayoutContext(0);
                AreaAdditionUtil.addAreas(null, 
                        new KnuthPossPosIter(elementList, firstIndex, lastIndex + 1), 
                        childLC);
            }
            // set the offset from the top margin
            Footnote parentArea = (Footnote) pslm.getCurrentPV().getBodyRegion().getFootnote();
            int topOffset = (int) pslm.getCurrentPV().getBodyRegion().getBPD() - parentArea.getBPD();
            if (separatorArea != null) {
                topOffset -= separatorArea.getBPD();
            }
            parentArea.setTop(topOffset);
            parentArea.setSeparator(separatorArea);
        }
        pslm.getCurrentPV().getCurrentSpan().notifyFlowsFinished();
    }
    
    /**
     * @return the current child flow layout manager
     */
    protected LayoutManager getCurrentChildLM() {
        return childFLM;
    }
    
    /** @see org.apache.fop.layoutmgr.AbstractBreaker#observeElementList(java.util.List) */
    protected void observeElementList(List elementList) {
        ElementListObserver.observe(elementList, "breaker", 
                ((PageSequence)pslm.getFObj()).getId());
    }
    
    /**
     * Depending on the kind of break condition, move to next column
     * or page. May need to make an empty page if next page would
     * not have the desired "handedness".
     * @param breakVal - value of break-before or break-after trait.
     */
    private void handleBreakTrait(int breakVal) {
        Page curPage = pslm.getCurrentPage();
        if (breakVal == Constants.EN_ALL) {
            //break due to span change in multi-column layout
            curPage.getPageViewport().createSpan(true);
            return;
        } else if (breakVal == Constants.EN_NONE) {
            curPage.getPageViewport().createSpan(false);
            return;
        } else if (breakVal == Constants.EN_COLUMN || breakVal <= 0) {
            PageViewport pv = curPage.getPageViewport();
            
            //Check if previous page was spanned
            boolean forceNewPageWithSpan = false;
            RegionBody rb = (RegionBody)curPage.getSimplePageMaster().getRegion(
                    Constants.FO_REGION_BODY);
            if (breakVal < 0 
                    && rb.getColumnCount() > 1 
                    && pv.getCurrentSpan().getColumnCount() == 1) {
                forceNewPageWithSpan = true;
            }
            
            if (forceNewPageWithSpan) {
                curPage = pslm.makeNewPage(false, false);
                curPage.getPageViewport().createSpan(true);
            } else if (pv.getCurrentSpan().hasMoreFlows()) {
                pv.getCurrentSpan().moveToNextFlow();
            } else {
                curPage = pslm.makeNewPage(false, false);
            }
            return;
        }
        log.debug("handling break-before after page " + pslm.getCurrentPageNum() 
            + " breakVal=" + breakVal);
        if (needBlankPageBeforeNew(breakVal)) {
            curPage = pslm.makeNewPage(true, false);
        }
        if (needNewPage(breakVal)) {
            curPage = pslm.makeNewPage(false, false);
        }
    }
    
    /**
     * Check if a blank page is needed to accomodate
     * desired even or odd page number.
     * @param breakVal - value of break-before or break-after trait.
     */
    private boolean needBlankPageBeforeNew(int breakVal) {
        if (breakVal == Constants.EN_PAGE || (pslm.getCurrentPage().getPageViewport().getPage().isEmpty())) {
            // any page is OK or we already have an empty page
            return false;
        } else {
            /* IF we are on the kind of page we need, we'll need a new page. */
            if (pslm.getCurrentPageNum() % 2 == 0) { // even page
                return (breakVal == Constants.EN_EVEN_PAGE);
            } else { // odd page
                return (breakVal == Constants.EN_ODD_PAGE);
            }
        }
    }
    
    /**
     * See if need to generate a new page
     * @param breakVal - value of break-before or break-after trait.
     */
    private boolean needNewPage(int breakVal) {
        if (pslm.getCurrentPage().getPageViewport().getPage().isEmpty()) {
            if (breakVal == Constants.EN_PAGE) {
                return false;
            } else if (pslm.getCurrentPageNum() % 2 == 0) { // even page
                return (breakVal == Constants.EN_ODD_PAGE);
            } else { // odd page
                return (breakVal == Constants.EN_EVEN_PAGE);
            }
        } else {
            return true;
        }
    }
}