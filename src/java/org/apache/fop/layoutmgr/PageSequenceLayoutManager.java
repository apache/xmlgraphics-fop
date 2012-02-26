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

/**
 * LayoutManager for a PageSequence.  This class is instantiated by
 * area.AreaTreeHandler for each fo:page-sequence found in the
 * input document.
 */
public class PageSequenceLayoutManager extends AbstractPageSequenceLayoutManager {

    private static Log log = LogFactory.getLog(PageSequenceLayoutManager.class);

    private PageProvider pageProvider;

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

    /** {@inheritDoc} */
    public void activateLayout() {
        initialize();

        // perform step 5.8 of refinement process (Unicode BIDI Processing)
        if ( areaTreeHandler.isComplexScriptFeaturesEnabled() ) {
            BidiResolver.resolveInlineDirectionality(getPageSequence());
        }

        LineArea title = null;
        if (getPageSequence().getTitleFO() != null) {
            try {
                ContentLayoutManager clm = getLayoutManagerMaker().
                    makeContentLayoutManager(this, getPageSequence().getTitleFO());
                title = (LineArea) clm.getParentArea(null);
            } catch (IllegalStateException e) {
                // empty title; do nothing
            }
        }

        AreaTreeModel areaTreeModel = areaTreeHandler.getAreaTreeModel();
        org.apache.fop.area.PageSequence pageSequenceAreaObject
                = new org.apache.fop.area.PageSequence(title);
        transferExtensions(pageSequenceAreaObject);
        pageSequenceAreaObject.setLanguage(getPageSequence().getLanguage());
        pageSequenceAreaObject.setCountry(getPageSequence().getCountry());
        areaTreeModel.startPageSequence(pageSequenceAreaObject);
        if (log.isDebugEnabled()) {
            log.debug("Starting layout");
        }

        curPage = makeNewPage(false);

        PageBreaker breaker = new PageBreaker(this);
        int flowBPD = getCurrentPV().getBodyRegion().getRemainingBPD();
        breaker.doLayout(flowBPD);

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
        Page newPage;

        do {
            newPage = super.makeNewPage(isBlank);
        } while (!getPageSequence().getMainFlow().getFlowName()
                .equals(newPage.getSimplePageMaster().getRegion(FO_REGION_BODY).getRegionName()));

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
        if (  lastPageNum % 2 != 0
                && ( getPageSequence().getForcePageCount() ==  Constants.EN_EVEN
                 || getPageSequence().getForcePageCount() ==  Constants.EN_END_ON_EVEN )) {
            forcedLastPageNum++;
        } else if ( lastPageNum % 2 == 0 && (
                getPageSequence().getForcePageCount() ==  Constants.EN_ODD
                ||  getPageSequence().getForcePageCount() ==  Constants.EN_END_ON_ODD )) {
            forcedLastPageNum++;
        }
        return forcedLastPageNum;
    }

}
