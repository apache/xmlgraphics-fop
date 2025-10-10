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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.fop.area.Block;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.Footnote;
import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.layoutmgr.BreakingAlgorithm.KnuthNode;
import org.apache.fop.layoutmgr.PageBreakingAlgorithm.PageBreakingLayoutListener;
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;
import org.apache.fop.traits.MinOptMax;

/**
 * Handles the breaking of pages in an fo:flow
 */
public class PageBreaker extends AbstractBreaker {

    private boolean firstPart = true;
    private boolean pageBreakHandled;
    private boolean needColumnBalancing;
    private PageProvider pageProvider;
    private Block separatorArea;
    private boolean spanAllActive;
    private boolean layoutRedone;
    private int previousIndex;
    private boolean handlingStartOfFloat;
    private boolean handlingEndOfFloat;
    private int floatHeight;
    private int floatYOffset;

    private List<ListElement> relayedFootnotesList;
    private List<Integer> relayedLengthList;
    private int relayedTotalFootnotesLength;
    private int relayedInsertedFootnotesLength;
    private boolean relayedFootnotesPending;
    private boolean relayedNewFootnotes;
    private int relayedFirstNewFootnoteIndex;
    private int relayedFootnoteListIndex;
    private int relayedFootnoteElementIndex = -1;
    private MinOptMax relayedFootnoteSeparatorLength;
    private int previousFootnoteListIndex = -2;
    private int previousFootnoteElementIndex = -2;
    private int prevousColumnCount;

    /**
     * The FlowLayoutManager object, which processes
     * the single fo:flow of the fo:page-sequence
     */
    private FlowLayoutManager childFLM;

    private StaticContentLayoutManager footnoteSeparatorLM;

    /**
     * Construct page breaker.
     * @param pslm the page sequence layout manager
     */
    public PageBreaker(PageSequenceLayoutManager pslm) {
        this.pslm = pslm;
        this.pageProvider = pslm.getPageProvider();
        this.childFLM = pslm.getLayoutManagerMaker().makeFlowLayoutManager(
                pslm, pslm.getPageSequence().getMainFlow());
    }

    /** {@inheritDoc} */
    protected void updateLayoutContext(LayoutContext context) {
        int flowIPD = pslm.getCurrentColumnWidth();
        context.setRefIPD(flowIPD);
    }

    /** {@inheritDoc} */
    protected LayoutManager getTopLevelLM() {
        return pslm;
    }

    /** {@inheritDoc} */
    protected PageProvider getPageProvider() {
        return pslm.getPageProvider();
    }

    /**
     * Starts the page breaking process.
     * @param flowBPD the constant available block-progression-dimension (used for every part)
     */
    boolean doLayout(int flowBPD) {
        return doLayout(flowBPD, false);
    }

    /** {@inheritDoc} */
    protected PageBreakingLayoutListener createLayoutListener() {
        return new PageBreakingLayoutListener() {

            public void notifyOverflow(int part, int amount, FObj obj) {
                Page p = pageProvider.getPageFromColumnIndex(part);
                RegionBody body = (RegionBody)p.getSimplePageMaster().getRegion(
                        Region.FO_REGION_BODY);
                BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                        body.getUserAgent().getEventBroadcaster());

                boolean canRecover = (body.getOverflow() != Constants.EN_ERROR_IF_OVERFLOW);
                boolean needClip = (body.getOverflow() == Constants.EN_HIDDEN
                        || body.getOverflow() == Constants.EN_ERROR_IF_OVERFLOW);
                eventProducer.regionOverflow(this, body.getName(),
                        p.getPageViewport().getPageNumberString(),
                        amount, needClip, canRecover,
                        body.getLocator());
            }

        };
    }

    /** {@inheritDoc} */
    protected int handleSpanChange(LayoutContext childLC, int nextSequenceStartsOn) {
        needColumnBalancing = false;
        if (childLC.getNextSpan() != Constants.NOT_SET) {
            //Next block list will have a different span.
            nextSequenceStartsOn = childLC.getNextSpan();
            needColumnBalancing = childLC.getNextSpan() == Constants.EN_ALL
                    && childLC.getDisableColumnBalancing() == Constants.EN_FALSE;

        }
        if (needColumnBalancing) {
            log.debug(
                    "Column balancing necessary for the next element list!!!");
        }
        return nextSequenceStartsOn;
    }

    /** {@inheritDoc} */
    protected int getNextBlockList(LayoutContext childLC,
            int nextSequenceStartsOn) {
        return getNextBlockList(childLC, nextSequenceStartsOn, null, null, null);
    }

    /** {@inheritDoc} */
    protected int getNextBlockList(LayoutContext childLC, int nextSequenceStartsOn,
            Position positionAtIPDChange, LayoutManager restartLM, List<ListElement> firstElements) {
        if (!layoutRedone && !handlingFloat()) {
            if (!firstPart) {
                // if this is the first page that will be created by
                // the current BlockSequence, it could have a break
                // condition that must be satisfied;
                // otherwise, we may simply need a new page
                handleBreakTrait(nextSequenceStartsOn);
            }
            firstPart = false;
            pageBreakHandled = true;

            pageProvider.setStartOfNextElementList(pslm.getCurrentPageNum(), pslm.getCurrentPV()
                    .getCurrentSpan().getCurrentFlowIndex(), this.spanAllActive);
        }
        return super.getNextBlockList(childLC, nextSequenceStartsOn, positionAtIPDChange,
                restartLM, firstElements);
    }

    private boolean containsFootnotes(List<ListElement> contentList, LayoutContext context) {
        boolean containsFootnotes = false;
        if (contentList != null) {
            for (Object aContentList : contentList) {
                ListElement element = (ListElement) aContentList;
                if (element instanceof KnuthBlockBox
                        && ((KnuthBlockBox) element).hasAnchors()) {
                    // element represents a line with footnote citations
                    containsFootnotes = true;
                    KnuthBlockBox box = (KnuthBlockBox) element;
                    List<List<KnuthElement>> footnotes = getFootnoteKnuthElements(childFLM, context,
                            box.getFootnoteBodyLMs());
                    for (List<KnuthElement> footnote : footnotes) {
                        box.addElementList(footnote);
                    }
                }
            }
        }
        return containsFootnotes;
    }

    public static List<List<KnuthElement>> getFootnoteKnuthElements(FlowLayoutManager flowLM, LayoutContext context,
            List<FootnoteBodyLayoutManager> footnoteBodyLMs) {
        List<List<KnuthElement>> footnotes = new ArrayList<>();
        LayoutContext footnoteContext = LayoutContext.copyOf(context);
        footnoteContext.setStackLimitBP(context.getStackLimitBP());
        footnoteContext.setRefIPD(flowLM.getPSLM()
                .getCurrentPV().getRegionReference(Constants.FO_REGION_BODY).getIPD());
        for (FootnoteBodyLayoutManager fblm : footnoteBodyLMs) {
            fblm.setParent(flowLM);
            fblm.initialize();
            List<KnuthElement> footnote = fblm.getNextKnuthElements(footnoteContext, Constants.EN_START);
            // TODO this does not respect possible stacking constraints between footnotes
            SpaceResolver.resolveElementList(footnote);
            footnotes.add(footnote);
        }
        return footnotes;
    }

    private void handleFootnoteSeparator() {
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
            footnoteSeparatorLM
                    = pslm.getLayoutManagerMaker().makeStaticContentLayoutManager(
                        pslm, footnoteSeparator, separatorArea);
            footnoteSeparatorLM.doLayout();

            footnoteSeparatorLength = MinOptMax.getInstance(separatorArea.getBPD());
        }
    }

    /** {@inheritDoc} */
    protected List<ListElement> getNextKnuthElements(LayoutContext context, int alignment) {
        List<ListElement> contentList = null;

        while (!childFLM.isFinished() && contentList == null) {
            contentList = childFLM.getNextKnuthElements(context, alignment);
        }

        // scan contentList, searching for footnotes
        if (containsFootnotes(contentList, context)) {
            // handle the footnote separator
            handleFootnoteSeparator();
        }

        return contentList;
    }

    /** {@inheritDoc} */
    protected List<ListElement> getNextKnuthElements(LayoutContext context, int alignment,
            Position positionAtIPDChange, LayoutManager restartAtLM) {
        List<ListElement> contentList = null;

        do {
            contentList = childFLM.getNextKnuthElements(context, alignment, positionAtIPDChange,
                    restartAtLM);
        } while (!childFLM.isFinished() && contentList == null);

        // scan contentList, searching for footnotes
        if (containsFootnotes(contentList, context)) {
            // handle the footnote separator
            handleFootnoteSeparator();
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
            footnoteSeparatorLM = pslm.getLayoutManagerMaker().makeStaticContentLayoutManager(
            pslm, footnoteSeparator, separatorArea);
            footnoteSeparatorLM.doLayout();
        }

        childFLM.addAreas(posIter, context);
    }

    /**
     * {@inheritDoc}
     * This implementation checks whether to trigger column-balancing,
     * or whether to take into account a 'last-page' condition.
     */
    protected void doPhase3(PageBreakingAlgorithm alg, int partCount,
            BlockSequence originalList, BlockSequence effectiveList) {

        if (needColumnBalancing) {
            //column balancing for the last part
            redoLayout(alg, partCount, originalList, effectiveList);
            return;
        }

        if (shouldRedoLayout(partCount)) {
            redoLayout(alg, partCount, originalList, effectiveList);
            return;
        }

        //nothing special: just add the areas now
        addAreas(alg, partCount, originalList, effectiveList);
    }

    protected void prepareToRedoLayout(PageBreakingAlgorithm alg, int partCount,
            BlockSequence originalList,
            BlockSequence effectiveList) {
        int newStartPos = 0;
        int restartPoint = pageProvider.getStartingPartIndexForLastPage(partCount);
        if (restartPoint > 0 && !layoutRedone) {
            // Add definitive areas for the parts before the
            // restarting point
            addAreas(alg, restartPoint, originalList, effectiveList);
            // Get page break from which we restart
            PageBreakPosition pbp = alg.getPageBreaks().get(restartPoint - 1);
            newStartPos = alg.par.getFirstBoxIndex(pbp.getLeafPos() + 1);
            // Handle page break right here to avoid any side-effects
            if (newStartPos > 0) {
                handleBreakTrait(Constants.EN_PAGE);
            }
        }
        pageBreakHandled = true;
        // Update so the available BPD is reported correctly
        int currentPageNum = pslm.getCurrentPageNum();
        int currentColumn = pslm.getCurrentPV().getCurrentSpan().getCurrentFlowIndex();
        pageProvider.setStartOfNextElementList(currentPageNum, currentColumn, spanAllActive);

        // Make sure we only add the areas we haven't added already
        effectiveList.ignoreAtStart = newStartPos;
        if (!layoutRedone) {
            // Handle special page-master for last page
            setLastPageIndex(currentPageNum);
//          BodyRegion lastBody = pageProvider.getPage(false, currentPageNum).getPageViewport().getBodyRegion();
            pslm.setCurrentPage(pageProvider.getPage(false, currentPageNum));
            previousIndex = pageProvider.getIndexOfCachedLastPage();
        } else {
            setLastPageIndex(currentPageNum + 1);
//            pslm.setCurrentPage(previousPage);
            pageProvider.discardCacheStartingWith(previousIndex);
            pslm.setCurrentPage(pageProvider.getPage(false, currentPageNum));
        }
        layoutRedone = true;
    }

    /**
     * Restart the algorithm at the break corresponding to the given partCount. Used to
     * re-do the part after the last break in case of either column-balancing or a last
     * page-master.
     */
    private void redoLayout(PageBreakingAlgorithm alg, int partCount,
            BlockSequence originalList, BlockSequence effectiveList) {

        int newStartPos = 0;
        int restartPoint = pageProvider.getStartingPartIndexForLastPage(partCount);
        if (restartPoint > 0) {
            //Add definitive areas for the parts before the
            //restarting point
            addAreas(alg, restartPoint, originalList, effectiveList);
            //Get page break from which we restart
            PageBreakPosition pbp = alg.getPageBreaks().get(restartPoint - 1);
            newStartPos = alg.par.getFirstBoxIndex(pbp.getLeafPos() + 1);
            //Handle page break right here to avoid any side-effects
            if (newStartPos > 0) {
                handleBreakTrait(Constants.EN_PAGE);
            }
        }

        log.debug("Restarting at " + restartPoint
                + ", new start position: " + newStartPos);

        pageBreakHandled = true;
        //Update so the available BPD is reported correctly
        int currentPageNum = pslm.getCurrentPageNum();

        pageProvider.setStartOfNextElementList(currentPageNum,
                pslm.getCurrentPV().getCurrentSpan().getCurrentFlowIndex(), this.spanAllActive);

        //Make sure we only add the areas we haven't added already
        effectiveList.ignoreAtStart = newStartPos;

        PageBreakingAlgorithm algRestart;
        if (needColumnBalancing) {
            log.debug("Column balancing now!!!");
            log.debug("===================================================");

            //Restart last page
            algRestart = new BalancingColumnBreakingAlgorithm(
                    getTopLevelLM(), getPageProvider(), createLayoutListener(),
                    alignment, Constants.EN_START, footnoteSeparatorLength,
                    isPartOverflowRecoveryActivated(),
                    pslm.getCurrentPV().getBodyRegion().getColumnCount());
            log.debug("===================================================");
        } else  {
            // Handle special page-master for last page
            BodyRegion currentBody = pageProvider.getPage(false, currentPageNum)
                    .getPageViewport().getBodyRegion();

            setLastPageIndex(currentPageNum);

            BodyRegion lastBody = pageProvider.getPage(false, currentPageNum)
                    .getPageViewport().getBodyRegion();
            lastBody.getMainReference().setSpans(currentBody.getMainReference().getSpans());
            log.debug("Last page handling now!!!");
            log.debug("===================================================");
            //Restart last page
            algRestart = new PageBreakingAlgorithm(
                    getTopLevelLM(), getPageProvider(), createLayoutListener(),
                    alg.getAlignment(), alg.getAlignmentLast(),
                    footnoteSeparatorLength,
                    isPartOverflowRecoveryActivated(), false, false, null);
            log.debug("===================================================");
        }

        int optimalPageCount = algRestart.findBreakingPoints(effectiveList,
                    newStartPos,
                    1, true, BreakingAlgorithm.ALL_BREAKS);
        if (algRestart.getPageBreaks() != null) {
            log.debug("restart: optimalPageCount= " + optimalPageCount
                    + " pageBreaks.size()= " + algRestart.getPageBreaks().size());
        }

        boolean fitsOnePage
            = optimalPageCount <= pslm.getCurrentPV()
                .getBodyRegion().getMainReference().getCurrentSpan().getColumnCount();

        if (needColumnBalancing) {
            if (!fitsOnePage) {
                log.warn(
                        "Breaking algorithm produced more columns than are available.");
                /* reenable when everything works
                throw new IllegalStateException(
                        "Breaking algorithm must not produce more columns than available.");
                */
            }
        } else {
            boolean ipdChange = algRestart.getIPDdifference() != 0;
            if (fitsOnePage && !ipdChange) {
                //Replace last page
                pslm.setCurrentPage(pageProvider.getPage(false, currentPageNum));
            } else {
                if (optimalPageCount > pslm.getCurrentPV().getBodyRegion().getMainReference().getColumnCount()) {
                    setLastPageIndex(currentPageNum + 2);
                } else {
                    setLastPageIndex(currentPageNum + 1);
                }
                //Last page-master cannot hold the content.
                //Add areas now...
                addAreas(alg, restartPoint, partCount - restartPoint, originalList, effectiveList);
                if (!ipdChange && pslm.currentPageNum == currentPageNum) {
                    setLastPageIndex(currentPageNum + 1);
                    //...and add a blank last page
                    pslm.setCurrentPage(pslm.makeNewPage(true));
                }
                return;
            }
        }

        addAreas(algRestart, optimalPageCount, originalList, effectiveList);
    }

    private void setLastPageIndex(int currentPageNum) {
        int lastPageIndex = pslm.getForcedLastPageNum(currentPageNum);
        pageProvider.setLastPageIndex(lastPageIndex);
    }

    /** {@inheritDoc} */
    protected void startPart(BlockSequence list, int breakClass, boolean emptyContent) {
        log.debug("startPart() breakClass=" + getBreakClassName(breakClass));
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
                handleBreakTrait(breakClass, emptyContent);
            }
            pageProvider.setStartOfNextElementList(pslm.getCurrentPageNum(),
                    pslm.getCurrentPV().getCurrentSpan().getCurrentFlowIndex(),
                    this.spanAllActive);
        }
        pageBreakHandled = false;
        // add static areas and resolve any new id areas
        // finish page and add to area tree
        firstPart = false;
    }

    /** {@inheritDoc} */
    protected void handleEmptyContent() {
        pslm.getCurrentPV().getPage().fakeNonEmpty();
    }

    /** {@inheritDoc} */
    protected void finishPart(PageBreakingAlgorithm alg, PageBreakPosition pbp) {
        // add footnote areas
        if (!pslm.getTableHeaderFootnotes().isEmpty()
                || pbp.footnoteFirstListIndex < pbp.footnoteLastListIndex
                || pbp.footnoteFirstElementIndex <= pbp.footnoteLastElementIndex
                || !pslm.getTableFooterFootnotes().isEmpty()) {
            for (List<KnuthElement> footnote : pslm.getTableHeaderFootnotes()) {
                addFootnoteAreas(footnote);
            }
            // call addAreas() for each FootnoteBodyLM
            for (int i = pbp.footnoteFirstListIndex; i <= pbp.footnoteLastListIndex; i++) {
                List elementList = alg.getFootnoteList(i);
                int firstIndex = (i == pbp.footnoteFirstListIndex
                        ? pbp.footnoteFirstElementIndex : 0);
                int lastIndex = (i == pbp.footnoteLastListIndex
                        ? pbp.footnoteLastElementIndex : elementList.size() - 1);
                addFootnoteAreas(elementList, firstIndex, lastIndex + 1);
            }
            for (List<KnuthElement> footnote : pslm.getTableFooterFootnotes()) {
                addFootnoteAreas(footnote);
            }
            // set the offset from the top margin
            Footnote parentArea = pslm.getCurrentPV().getBodyRegion().getFootnote();
            int topOffset = pslm.getCurrentPV().getBodyRegion().getBPD() - parentArea.getBPD();
            if (separatorArea != null) {
                topOffset -= separatorArea.getBPD();
            }
            parentArea.setTop(topOffset);
            parentArea.setSeparator(separatorArea);
        }
        pslm.getCurrentPV().getCurrentSpan().notifyFlowsFinished();
        pslm.clearTableHeadingFootnotes();
    }

    private void addFootnoteAreas(List<KnuthElement> footnote) {
        addFootnoteAreas(footnote, 0, footnote.size());
    }

    private void addFootnoteAreas(List<KnuthElement> footnote, int startIndex, int endIndex) {
        SpaceResolver.performConditionalsNotification(footnote, startIndex, endIndex - 1, -1);
        LayoutContext childLC = LayoutContext.newInstance();
        AreaAdditionUtil.addAreas(null, new KnuthPossPosIter(footnote, startIndex, endIndex), childLC);
    }

    /** {@inheritDoc} */
    protected FlowLayoutManager getCurrentChildLM() {
        return childFLM;
    }

    /** {@inheritDoc} */
    protected void observeElementList(List elementList) {
        ElementListObserver.observe(elementList, "breaker",
                pslm.getFObj().getId());
    }

    /**
     * Depending on the kind of break condition, move to next column
     * or page. May need to make an empty page if next page would
     * not have the desired "handedness".
     * @param breakVal - value of break-before or break-after trait.
     */
    private void handleBreakTrait(int breakVal) {
        handleBreakTrait(breakVal, false);
    }

    private void handleBreakTrait(int breakVal, boolean emptyContent) {
        Page curPage = pslm.getCurrentPage();
        switch (breakVal) {
        case Constants.EN_ALL:
            //break due to span change in multi-column layout
            curPage.getPageViewport().createSpan(true);
            this.spanAllActive = true;
            return;
        case Constants.EN_NONE:
            curPage.getPageViewport().createSpan(false);
            this.spanAllActive = false;
            return;
        case Constants.EN_COLUMN:
        case Constants.EN_AUTO:
        case Constants.EN_PAGE:
        case -1:
            PageViewport pv = curPage.getPageViewport();

            //Check if previous page was spanned
            boolean forceNewPageWithSpan = false;
            RegionBody rb = (RegionBody)curPage.getSimplePageMaster().getRegion(
                    Constants.FO_REGION_BODY);
            forceNewPageWithSpan
                    = (rb.getColumnCount() > 1
                        && pv.getCurrentSpan().getColumnCount() == 1);

            if (forceNewPageWithSpan) {
                checkPagePositionOnly();
                log.trace("Forcing new page with span");
                curPage = pslm.makeNewPage(false);
                curPage.getPageViewport().createSpan(true);
            } else {
                if (breakVal == Constants.EN_PAGE) {
                    handleBreakBeforeFollowingPage(breakVal);
                } else {
                    if (pv.getCurrentSpan().hasMoreFlows()) {
                        log.trace("Moving to next flow");
                        pv.getCurrentSpan().moveToNextFlow();
                    } else {
                        checkPagePositionOnly();
                        log.trace("Making new page");
                        pslm.makeNewPage(false, emptyContent);
                    }
                }
            }
            return;
        default:
            handleBreakBeforeFollowingPage(breakVal);
        }
    }

    private void checkPagePositionOnly() {
        if (pslm.getCurrentPage().isPagePositionOnly && !pslm.fobj.getUserAgent().isLegacySkipPagePositionOnly()) {
            throw new PagePositionOnlyException();
        }
    }

    private void handleBreakBeforeFollowingPage(int breakVal) {
        log.debug("handling break-before after page " + pslm.getCurrentPageNum() + " breakVal="
                + getBreakClassName(breakVal));
        if (needBlankPageBeforeNew(breakVal)) {
            checkPagePositionOnly();
            log.trace("Inserting blank page");
            /* curPage = */pslm.makeNewPage(true);
        }
        if (needNewPage(breakVal)) {
            checkPagePositionOnly();
            log.trace("Making new page");
            /* curPage = */pslm.makeNewPage(false);
        }
    }

    /**
     * Check if a blank page is needed to accommodate
     * desired even or odd page number.
     * @param breakVal - value of break-before or break-after trait.
     */
    private boolean needBlankPageBeforeNew(int breakVal) {
        if (breakVal == Constants.EN_PAGE
                || (pslm.getCurrentPage().getPageViewport().getPage().isEmpty())) {
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

    protected boolean shouldRedoLayout() {
        return shouldRedoLayout(-1);
    }

    protected boolean shouldRedoLayout(int partCount) {
        boolean lastPageMasterDefined = pslm.getPageSequence().hasPagePositionLast();
        if (!lastPageMasterDefined && partCount != -1) {
            lastPageMasterDefined = pslm.getPageSequence().hasPagePositionOnly() && pslm.isOnFirstPage(partCount - 1);
        }
        return (!hasMoreContent() && lastPageMasterDefined && !layoutRedone);
    }

    protected boolean wasLayoutRedone() {
        return layoutRedone;
    }

    protected boolean lastPageHasIPDChange(int optimalPageCount) {
        boolean lastPageMasterDefined = pslm.getPageSequence().hasPagePositionLast();
        boolean onlyPageMasterDefined = pslm.getPageSequence().hasPagePositionOnly();
        if (lastPageMasterDefined && !onlyPageMasterDefined) {
            // code not very robust and unable to handle situations were only and last are defined
            int currentColumnCount = pageProvider.getCurrentColumnCount();
            boolean changeInColumnCount = prevousColumnCount > 0 && prevousColumnCount != currentColumnCount;
            prevousColumnCount = currentColumnCount;
            if ((currentColumnCount > 1 && optimalPageCount % currentColumnCount == 0) || changeInColumnCount) {
                return false;
            }
            int currentIPD = this.pageProvider.getCurrentIPD();
            int lastPageIPD = this.pageProvider.getLastPageIPD();
            return lastPageIPD != -1 && currentIPD != lastPageIPD;
        }
        return false;
    }

    protected boolean handlingStartOfFloat() {
        return handlingStartOfFloat;
    }

    protected void handleStartOfFloat(int fHeight, int fYOffset) {
        handlingStartOfFloat = true;
        handlingEndOfFloat = false;
        floatHeight = fHeight;
        floatYOffset = fYOffset;
        childFLM.handleFloatOn();
    }

    protected int getFloatHeight() {
        return floatHeight;
    }

    protected int getFloatYOffset() {
        return floatYOffset;
    }

    protected boolean handlingEndOfFloat() {
        return handlingEndOfFloat;
    }

    protected void handleEndOfFloat(int fHeight) {
        handlingEndOfFloat = true;
        handlingStartOfFloat = false;
        floatHeight = fHeight;
        childFLM.handleFloatOff();
    }

    protected boolean handlingFloat() {
        return (handlingStartOfFloat || handlingEndOfFloat);
    }

    public int getOffsetDueToFloat() {
        handlingEndOfFloat = false;
        return floatHeight + floatYOffset;
    }

    protected int handleFloatLayout(PageBreakingAlgorithm alg, int optimalPageCount, BlockSequence blockList,
            LayoutContext childLC) {
        pageBreakHandled = true;
        List firstElements = Collections.EMPTY_LIST;
        KnuthNode floatNode = alg.getBestFloatEdgeNode();
        int floatPosition = floatNode.position;
        KnuthElement floatElem = alg.getElement(floatPosition);
        Position positionAtBreak = floatElem.getPosition();
        if (!(positionAtBreak instanceof SpaceResolver.SpaceHandlingBreakPosition)) {
            throw new UnsupportedOperationException("Don't know how to restart at position" + positionAtBreak);
        }
        /* Retrieve the original position wrapped into this space position */
        positionAtBreak = positionAtBreak.getPosition();
        addAreas(alg, optimalPageCount, blockList, blockList);
        blockLists.clear();
        blockListIndex = -1;
        LayoutManager restartAtLM = null;
        if (positionAtBreak != null && positionAtBreak.getIndex() == -1) {
            if (positionAtBreak instanceof ListItemLayoutManager.ListItemPosition) {
                restartAtLM = positionAtBreak.getLM();
            } else {
                Position position;
                Iterator iter = blockList.listIterator(floatPosition + 1);
                do {
                    KnuthElement nextElement = (KnuthElement) iter.next();
                    position = nextElement.getPosition();
                } while (position == null || position instanceof SpaceResolver.SpaceHandlingPosition
                        || position instanceof SpaceResolver.SpaceHandlingBreakPosition
                        && position.getPosition().getIndex() == -1);
                LayoutManager surroundingLM = positionAtBreak.getLM();
                while (position.getLM() != surroundingLM) {
                    position = position.getPosition();
                }
                restartAtLM = position.getPosition().getLM();
            }
        }
        return getNextBlockList(childLC, Constants.EN_COLUMN, positionAtBreak,
                restartAtLM, firstElements);
    }

    protected void addAreasForFloats(PageBreakingAlgorithm alg, int startPart, int partCount,
            BlockSequence originalList, BlockSequence effectiveList, final LayoutContext childLC,
            int lastBreak, int startElementIndex, int endElementIndex) {
        FloatPosition pbp = alg.getFloatPosition();

        // Check the last break position for forced breaks
        int lastBreakClass;
        if (startElementIndex == 0) {
            lastBreakClass = effectiveList.getStartOn();
        } else {
            ListElement lastBreakElement = effectiveList.getElement(endElementIndex);
            if (lastBreakElement.isPenalty()) {
                KnuthPenalty pen = (KnuthPenalty) lastBreakElement;
                if (pen.getPenalty() == KnuthPenalty.INFINITE) {
                    /**
                     * That means that there was a keep.within-page="always", but that
                     * it's OK to break at a column. TODO The break class is being
                     * abused to implement keep.within-column and keep.within-page.
                     * This is very misleading and must be revised.
                     */
                    lastBreakClass = Constants.EN_COLUMN;
                } else {
                    lastBreakClass = pen.getBreakClass();
                }
            } else {
                lastBreakClass = Constants.EN_COLUMN;
            }
        }

        // the end of the new part
        endElementIndex = pbp.getLeafPos();

        // ignore the first elements added by the
        // PageSequenceLayoutManager
        startElementIndex += (startElementIndex == 0) ? effectiveList.ignoreAtStart : 0;

        log.debug("PLM> part: " + (startPart + partCount + 1) + ", start at pos " + startElementIndex
                + ", break at pos " + endElementIndex + ", break class = "
                + getBreakClassName(lastBreakClass));

        startPart(effectiveList, lastBreakClass, false);

        int displayAlign = getCurrentDisplayAlign();

        // The following is needed by SpaceResolver.performConditionalsNotification()
        // further down as there may be important Position elements in the element list trailer
        int notificationEndElementIndex = endElementIndex;

        // ignore the last elements added by the
        // PageSequenceLayoutManager
        endElementIndex -= (endElementIndex == (originalList.size() - 1)) ? effectiveList.ignoreAtEnd : 0;

        // ignore the last element in the page if it is a KnuthGlue
        // object
        if (((KnuthElement) effectiveList.get(endElementIndex)).isGlue()) {
            endElementIndex--;
        }

        // ignore KnuthGlue and KnuthPenalty objects
        // at the beginning of the line
        startElementIndex = alg.par.getFirstBoxIndex(startElementIndex);

        if (startElementIndex <= endElementIndex) {
            if (log.isDebugEnabled()) {
                log.debug("     addAreas from " + startElementIndex + " to " + endElementIndex);
            }
            // set the space adjustment ratio
            childLC.setSpaceAdjust(pbp.bpdAdjust);
            // add space before if display-align is center or bottom
            // add space after if display-align is distribute and
            // this is not the last page
            if (pbp.difference != 0 && displayAlign == Constants.EN_CENTER) {
                childLC.setSpaceBefore(pbp.difference / 2);
            } else if (pbp.difference != 0 && displayAlign == Constants.EN_AFTER) {
                childLC.setSpaceBefore(pbp.difference);
            }

            // Handle SpaceHandling(Break)Positions, see SpaceResolver!
            SpaceResolver.performConditionalsNotification(effectiveList, startElementIndex,
                    notificationEndElementIndex, lastBreak);
            // Add areas of lines, in the current page, before the float or during float
            addAreas(new KnuthPossPosIter(effectiveList, startElementIndex, endElementIndex + 1), childLC);
            // add areas for the float, if applicable
            if (alg.handlingStartOfFloat()) {
                for (int k = startElementIndex; k < endElementIndex + 1; k++) {
                    ListElement le = effectiveList.getElement(k);
                    if (le instanceof KnuthBlockBox) {
                        KnuthBlockBox kbb = (KnuthBlockBox) le;
                        for (FloatContentLayoutManager fclm : kbb.getFloatContentLMs()) {
                            fclm.processAreas(childLC);
                            int floatHeight = fclm.getFloatHeight();
                            int floatYOffset = fclm.getFloatYOffset();
                            PageSequenceLayoutManager pslm = (PageSequenceLayoutManager) getTopLevelLM();
                            pslm.recordStartOfFloat(floatHeight, floatYOffset);
                        }
                    }
                }
            }
            if (alg.handlingEndOfFloat()) {
                PageSequenceLayoutManager pslm = (PageSequenceLayoutManager) getTopLevelLM();
                pslm.setEndIntrusionAdjustment(0);
                pslm.setStartIntrusionAdjustment(0);
                int effectiveFloatHeight = alg.getFloatHeight();
                pslm.recordEndOfFloat(effectiveFloatHeight);
            }
            if (alg.handlingFloat()) {
                PageSequenceLayoutManager pslm = (PageSequenceLayoutManager) getTopLevelLM();
                alg.relayFootnotes(pslm);
            }
        } else {
            // no content for this part
            handleEmptyContent();
        }

        pageBreakHandled = true;
    }

    public void holdFootnotes(List fl, List<Integer> ll, int tfl, int ifl, boolean fp, boolean nf, int fnfi, int fli,
                              int fei, MinOptMax fsl, int pfli, int pfei) {
        relayedFootnotesList = fl;
        relayedLengthList = ll;
        relayedTotalFootnotesLength = tfl;
        relayedInsertedFootnotesLength = ifl;
        relayedFootnotesPending = fp;
        relayedNewFootnotes = nf;
        relayedFirstNewFootnoteIndex = fnfi;
        relayedFootnoteListIndex = fli;
        relayedFootnoteElementIndex = fei;
        relayedFootnoteSeparatorLength = fsl;
        previousFootnoteListIndex = pfli;
        previousFootnoteElementIndex = pfei;
    }

    public void retrieveFootones(PageBreakingAlgorithm alg) {
        if (relayedFootnotesList != null && relayedFootnotesList.size() > 0) {
            alg.loadFootnotes(relayedFootnotesList, relayedLengthList, relayedTotalFootnotesLength,
                    relayedInsertedFootnotesLength, relayedFootnotesPending, relayedNewFootnotes,
                    relayedFirstNewFootnoteIndex, relayedFootnoteListIndex, relayedFootnoteElementIndex,
                    relayedFootnoteSeparatorLength, previousFootnoteListIndex,
                    previousFootnoteElementIndex);
            if (alg.handlingFloat()) {
                relayedFootnotesList = null;
                relayedLengthList = null;
                relayedTotalFootnotesLength = 0;
                relayedInsertedFootnotesLength = 0;
                relayedFootnotesPending = false;
                relayedNewFootnotes = false;
                relayedFirstNewFootnoteIndex = 0;
                relayedFootnoteListIndex = 0;
                relayedFootnoteElementIndex = -1;
                relayedFootnoteSeparatorLength = null;
            }
        }
    }

    protected void addAreas(PageBreakingAlgorithm alg, int startPart, int partCount,
                            BlockSequence originalList, BlockSequence effectiveList, final LayoutContext childLC) {
        super.addAreas(alg, startPart, partCount, originalList, effectiveList, childLC);
        if (!alg.handlingFloat()) {
            PageSequenceLayoutManager pslm = (PageSequenceLayoutManager) getTopLevelLM();
            alg.relayFootnotes(pslm);
        }
    }
}
