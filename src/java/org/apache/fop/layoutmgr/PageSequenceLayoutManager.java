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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.LineArea;
import org.apache.fop.complexscripts.bidi.BidiResolver;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.PageSequenceMaster;
import org.apache.fop.fo.pagination.SideRegion;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.layoutmgr.inline.ContentLayoutManager;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for a PageSequence.  This class is instantiated by
 * area.AreaTreeHandler for each fo:page-sequence found in the
 * input document.
 */
public class PageSequenceLayoutManager extends AbstractPageSequenceLayoutManager {

    private static Log log = LogFactory.getLog(PageSequenceLayoutManager.class);

    private PageProvider pageProvider;

    private PageBreaker pageBreaker;

    /** Footnotes coming from repeated table headers, to be added before any other footnote. */
    private List<List<KnuthElement>> tableHeaderFootnotes;

    /** Footnotes coming from repeated table footers, to be added after any other footnote. */
    private List<List<KnuthElement>> tableFooterFootnotes;

    private int startIntrusionAdjustment;
    private int endIntrusionAdjustment;

    /**
     * Constructor
     *
     * @param ath the area tree handler object
     * @param pseq fo:page-sequence to process
     */
    public PageSequenceLayoutManager(AreaTreeHandler ath, PageSequence pseq) {
        super(ath, pseq);
        this.pageProvider = new PageProvider(ath, pseq);
    }

    /** @return the PageProvider applicable to this page-sequence. */
    public PageProvider getPageProvider() {
        return this.pageProvider;
    }

    /**
     * @return the PageSequence being managed by this layout manager
     */
    protected PageSequence getPageSequence() {
        return (PageSequence)pageSeq;
    }

    /**
     * Provides access to this object
     * @return this PageSequenceLayoutManager instance
     */
    public PageSequenceLayoutManager getPSLM() {
        return this;
    }

    public FlowLayoutManager getFlowLayoutManager() {
        if (pageBreaker == null) {
            throw new IllegalStateException("This method can be called only during layout");
        }
        return pageBreaker.getCurrentChildLM();
    }

    /** {@inheritDoc} */
    public void activateLayout() {
        initialize();

        // perform step 5.8 of refinement process (Unicode BIDI Processing)
        if (areaTreeHandler.isComplexScriptFeaturesEnabled()) {
            BidiResolver.resolveInlineDirectionality(getPageSequence());
        }

        LineArea title = null;
        if (getPageSequence().getTitleFO() != null) {
            try {
                ContentLayoutManager clm = getLayoutManagerMaker()
                    .makeContentLayoutManager(this, getPageSequence().getTitleFO());
                title = (LineArea) clm.getParentArea(null);
            } catch (IllegalStateException e) {
                // empty title; do nothing
            }
        }

        AreaTreeModel areaTreeModel = areaTreeHandler.getAreaTreeModel();
        org.apache.fop.area.PageSequence pageSequenceAreaObject
                = new org.apache.fop.area.PageSequence(title);
        transferExtensions(pageSequenceAreaObject);
        pageSequenceAreaObject.setLocale(getPageSequence().getLocale());
        areaTreeModel.startPageSequence(pageSequenceAreaObject);
        if (log.isDebugEnabled()) {
            log.debug("Starting layout");
        }

        curPage = makeNewPage(false);

        pageBreaker = new PageBreaker(this);
        int flowBPD = getCurrentPV().getBodyRegion().getRemainingBPD();
        pageBreaker.doLayout(flowBPD);

        finishPage();
    }

    /** {@inheritDoc} */
    public void finishPageSequence() {
        if (pageSeq.hasId()) {
            idTracker.signalIDProcessed(pageSeq.getId());
        }
        pageSeq.getRoot().notifyPageSequenceFinished(currentPageNum,
                (currentPageNum - startPageNum) + 1);
        areaTreeHandler.notifyPageSequenceFinished(pageSeq,
                (currentPageNum - startPageNum) + 1);
        getPageSequence().releasePageSequence();

        // If this sequence has a page sequence master so we must reset
        // it in preparation for the next sequence
        String masterReference = getPageSequence().getMasterReference();
        PageSequenceMaster pageSeqMaster
            = pageSeq.getRoot().getLayoutMasterSet().getPageSequenceMaster(masterReference);
        if (pageSeqMaster != null) {
            pageSeqMaster.reset();
        }

        if (log.isDebugEnabled()) {
            log.debug("Ending layout");
        }
    }

    /** {@inheritDoc} */
    protected Page createPage(int pageNumber, boolean isBlank) {
        return pageProvider.getPage(isBlank,
                pageNumber, PageProvider.RELTO_PAGE_SEQUENCE);
    }

    @Override
    protected Page makeNewPage(boolean isBlank) {
        Page newPage = super.makeNewPage(isBlank);

        // Empty pages (pages that have been generated from a SPM that has an un-mapped flow name)
        // cannot layout areas from the main flow.  Blank pages can be created from empty pages.

        if (!isBlank) {
            while (!getPageSequence().getMainFlow().getFlowName()
                    .equals(newPage.getSimplePageMaster()
                            .getRegion(FO_REGION_BODY).getRegionName())) {
                newPage = super.makeNewPage(isBlank);
            }
        }

        return newPage;
    }

    private void layoutSideRegion(int regionID) {
        SideRegion reg = (SideRegion)curPage.getSimplePageMaster().getRegion(regionID);
        if (reg == null) {
            return;
        }
        StaticContent sc = getPageSequence().getStaticContent(reg.getRegionName());
        if (sc == null) {
            return;
        }

        StaticContentLayoutManager lm = getLayoutManagerMaker()
                                            .makeStaticContentLayoutManager(
                                                this, sc, reg);
        lm.doLayout();
    }

    /** {@inheritDoc} */
    protected void finishPage() {
        // Layout side regions
        layoutSideRegion(FO_REGION_BEFORE);
        layoutSideRegion(FO_REGION_AFTER);
        layoutSideRegion(FO_REGION_START);
        layoutSideRegion(FO_REGION_END);

        super.finishPage();
    }

    /**
     * The last page number of the sequence may be incremented, as determined by the
     *  force-page-count formatting property semantics
     * @param lastPageNum number of sequence
     * @return the forced last page number of sequence
     */
    protected int getForcedLastPageNum(final int lastPageNum) {
        int forcedLastPageNum = lastPageNum;
        int relativeLastPage = lastPageNum - startPageNum + 1;
        if (relativeLastPage % 2 != 0
                && (getPageSequence().getForcePageCount() ==  Constants.EN_EVEN
                 || getPageSequence().getForcePageCount() ==  Constants.EN_END_ON_EVEN)) {
            forcedLastPageNum++;
        } else if (relativeLastPage % 2 == 0 && (
                getPageSequence().getForcePageCount() ==  Constants.EN_ODD
                ||  getPageSequence().getForcePageCount() ==  Constants.EN_END_ON_ODD)) {
            forcedLastPageNum++;
        }
        return forcedLastPageNum;
    }

    /**
     * Indicates whether the column/page at the given index is on the first page of this page sequence.
     *
     * @return {@code true} if the given part is on the first page of the sequence
     */
    boolean isOnFirstPage(int partIndex) {
        return pageProvider.isOnFirstPage(partIndex);
    }

    /**
     * Registers the given footnotes so that they can be added to the current page, before any other footnote.
     *
     * @param headerFootnotes footnotes coming from a repeated table header
     */
    public void addTableHeaderFootnotes(List<List<KnuthElement>> headerFootnotes) {
        if (tableHeaderFootnotes == null) {
            tableHeaderFootnotes = new ArrayList<List<KnuthElement>>();
        }
        tableHeaderFootnotes.addAll(headerFootnotes);
    }

    public List<List<KnuthElement>> getTableHeaderFootnotes() {
        return getTableFootnotes(tableHeaderFootnotes);
    }

    /**
     * Registers the given footnotes so that they can be added to the current page, after any other footnote.
     *
     * @param footerFootnotes footnotes coming from a repeated table footer
     */
    public void addTableFooterFootnotes(List<List<KnuthElement>> footerFootnotes) {
        if (tableFooterFootnotes == null) {
            tableFooterFootnotes = new ArrayList<List<KnuthElement>>();
        }
        tableFooterFootnotes.addAll(footerFootnotes);
    }

    public List<List<KnuthElement>> getTableFooterFootnotes() {
        return getTableFootnotes(tableFooterFootnotes);
    }

    private List<List<KnuthElement>> getTableFootnotes(List<List<KnuthElement>> tableFootnotes) {
        if (tableFootnotes == null) {
            List<List<KnuthElement>> emptyList = Collections.emptyList();
            return emptyList;
        } else {
            return tableFootnotes;
        }
    }

    /**
     * Clears the footnotes coming from repeated table headers/footers, in order to start
     * afresh for a new page.
     */
    public void clearTableHeadingFootnotes() {
        if (tableHeaderFootnotes != null) {
            tableHeaderFootnotes.clear();
        }
        if (tableFooterFootnotes != null) {
            tableFooterFootnotes.clear();
        }
    }

    public void setStartIntrusionAdjustment(int sia) {
        startIntrusionAdjustment = sia;
    }

    public void setEndIntrusionAdjustment(int eia) {
        endIntrusionAdjustment = eia;
    }

    public int getStartIntrusionAdjustment() {
        return startIntrusionAdjustment;
    }

    public int getEndIntrusionAdjustment() {
        return endIntrusionAdjustment;
    }

    public void recordEndOfFloat(int fHeight) {
        pageBreaker.handleEndOfFloat(fHeight);
    }

    public boolean handlingEndOfFloat() {
        return pageBreaker.handlingEndOfFloat();
    }

    public int getOffsetDueToFloat() {
        return pageBreaker.getOffsetDueToFloat();
    }

    public void recordStartOfFloat(int fHeight, int fYOffset) {
        pageBreaker.handleStartOfFloat(fHeight, fYOffset);
    }

    public boolean handlingStartOfFloat() {
        return pageBreaker.handlingStartOfFloat();
    }

    public int getFloatHeight() {
        return pageBreaker.getFloatHeight();
    }

    public int getFloatYOffset() {
        return pageBreaker.getFloatYOffset();
    }

    public int getCurrentColumnWidth() {
        int flowIPD = getCurrentPV().getCurrentSpan().getColumnWidth();
        flowIPD -= startIntrusionAdjustment + endIntrusionAdjustment;
        return flowIPD;
    }

    public void holdFootnotes(List fl, List ll, int tfl, int ifl, boolean fp, boolean nf, int fnfi, int fli,
            int fei, MinOptMax fsl, int pfli, int pfei) {
        if (fl != null && fl.size() > 0) {
            pageBreaker.holdFootnotes(fl, ll, tfl, ifl, fp, nf, fnfi, fli, fei, fsl, pfli, pfei);
        }
    }

    public void retrieveFootnotes(PageBreakingAlgorithm alg) {
        pageBreaker.retrieveFootones(alg);
    }
}
