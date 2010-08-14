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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.IDTracker;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Resolvable;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.pagination.AbstractPageSequence;

/**
 * Abstract base class for a page sequence layout manager.
 */
public abstract class AbstractPageSequenceLayoutManager extends AbstractLayoutManager
            implements TopLevelLayoutManager {

    private static Log log = LogFactory.getLog(AbstractPageSequenceLayoutManager.class);

    /**
     * AreaTreeHandler which activates the PSLM and controls
     * the rendering of its pages.
     */
    protected AreaTreeHandler areaTreeHandler;

    /** ID tracker supplied by the AreaTreeHandler */
    protected IDTracker idTracker;

    /** page sequence formatting object being processed by this class */
    protected AbstractPageSequence pageSeq;

    /** Current page with page-viewport-area being filled by the PSLM. */
    protected Page curPage;

    /** the current page number */
    protected int currentPageNum = 0;
    /** The stating page number */
    protected int startPageNum = 0;

    /**
     * Constructor
     *
     * @param ath the area tree handler object
     * @param pseq fo:page-sequence to process
     */
    public AbstractPageSequenceLayoutManager(AreaTreeHandler ath, AbstractPageSequence pseq) {
        super(pseq);
        this.areaTreeHandler = ath;
        this.idTracker = ath.getIDTracker();
        this.pageSeq = pseq;
    }

    /**
     * @return the LayoutManagerMaker object associated to the areaTreeHandler
     */
    public LayoutManagerMaker getLayoutManagerMaker() {
        return areaTreeHandler.getLayoutManagerMaker();
    }

    /**
     * Provides access to the current page.
     * @return the current Page
     */
    public Page getCurrentPage() {
        return curPage;
    }

    /**
     * Provides access for setting the current page.
     * @param currentPage the new current Page
     */
    protected void setCurrentPage(Page currentPage) {
        this.curPage = currentPage;
    }

    /**
     * Provides access to the current page number
     * @return the current page number
     */
    protected int getCurrentPageNum() {
        return currentPageNum;
    }

    /** {@inheritDoc} */
    public void initialize() {
        startPageNum = pageSeq.getStartingPageNumber();
        currentPageNum = startPageNum - 1;
    }

    /**
     * This returns the first PageViewport that contains an id trait
     * matching the idref argument, or null if no such PV exists.
     *
     * @param idref the idref trait needing to be resolved
     * @return the first PageViewport that contains the ID trait
     */
    public PageViewport getFirstPVWithID(String idref) {
        List list = idTracker.getPageViewportsContainingID(idref);
        if (list != null && list.size() > 0) {
            return (PageViewport) list.get(0);
        }
        return null;
    }

    /**
     * This returns the last PageViewport that contains an id trait
     * matching the idref argument, or null if no such PV exists.
     *
     * @param idref the idref trait needing to be resolved
     * @return the last PageViewport that contains the ID trait
     */
    public PageViewport getLastPVWithID(String idref) {
        List list = idTracker.getPageViewportsContainingID(idref);
        if (list != null && list.size() > 0) {
            return (PageViewport) list.get(list.size() - 1);
        }
        return null;
    }

    /**
     * Add an ID reference to the current page.
     * When adding areas the area adds its ID reference.
     * For the page layout manager it adds the id reference
     * with the current page to the area tree.
     *
     * @param id the ID reference to add
     */
    public void addIDToPage(String id) {
        if (id != null && id.length() > 0) {
            idTracker.associateIDWithPageViewport(id, curPage.getPageViewport());
        }
    }

    /**
     * Add an id reference of the layout manager in the AreaTreeHandler,
     * if the id hasn't been resolved yet
     * @param id the id to track
     * @return a boolean indicating if the id has already been resolved
     * TODO Maybe give this a better name
     */
    public boolean associateLayoutManagerID(String id) {
        if (log.isDebugEnabled()) {
            log.debug("associateLayoutManagerID(" + id + ")");
        }
        if (!idTracker.alreadyResolvedID(id)) {
            idTracker.signalPendingID(id);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Notify the areaTreeHandler that the LayoutManagers containing
     * idrefs have finished creating areas
     * @param id the id for which layout has finished
     */
    public void notifyEndOfLayout(String id) {
        idTracker.signalIDProcessed(id);
    }

    /**
     * Identify an unresolved area (one needing an idref to be
     * resolved, e.g. the internal-destination of an fo:basic-link)
     * for both the AreaTreeHandler and PageViewport object.
     *
     * The IDTracker keeps a document-wide list of idref's
     * and the PV's needing them to be resolved.  It uses this to
     * send notifications to the PV's when an id has been resolved.
     *
     * The PageViewport keeps lists of id's needing resolving, along
     * with the child areas (page-number-citation, basic-link, etc.)
     * of the PV needing their resolution.
     *
     * @param id the ID reference to add
     * @param res the resolvable object that needs resolving
     */
    public void addUnresolvedArea(String id, Resolvable res) {
        curPage.getPageViewport().addUnresolvedIDRef(id, res);
        idTracker.addUnresolvedIDRef(id, curPage.getPageViewport());
    }

    /**
     * Bind the RetrieveMarker to the corresponding Marker subtree.
     * If the boundary is page then it will only check the
     * current page. For page-sequence and document it will
     * lookup preceding pages from the area tree and try to find
     * a marker.
     * If we retrieve a marker from a preceding page,
     * then the containing page does not have a qualifying area,
     * and all qualifying areas have ended.
     * Therefore we use last-ending-within-page (Constants.EN_LEWP)
     * as the position.
     *
     * @param rm the RetrieveMarker instance whose properties are to
     * used to find the matching Marker.
     * @return a bound RetrieveMarker instance, or null if no Marker
     * could be found.
     */
    public RetrieveMarker resolveRetrieveMarker(RetrieveMarker rm) {
        AreaTreeModel areaTreeModel = areaTreeHandler.getAreaTreeModel();
        String name = rm.getRetrieveClassName();
        int pos = rm.getRetrievePosition();
        int boundary = rm.getRetrieveBoundary();

        // get marker from the current markers on area tree
        Marker mark = (Marker)getCurrentPV().getMarker(name, pos);
        if (mark == null && boundary != EN_PAGE) {
            // go back over pages until mark found
            // if document boundary then keep going
            boolean doc = boundary == EN_DOCUMENT;
            int seq = areaTreeModel.getPageSequenceCount();
            int page = areaTreeModel.getPageCount(seq) - 1;
            while (page < 0 && doc && seq > 1) {
                seq--;
                page = areaTreeModel.getPageCount(seq) - 1;
            }
            while (page >= 0) {
                PageViewport pv = areaTreeModel.getPage(seq, page);
                mark = (Marker)pv.getMarker(name, Constants.EN_LEWP);
                if (mark != null) {
                    break;
                }
                page--;
                if (page < 0 && doc && seq > 1) {
                    seq--;
                    page = areaTreeModel.getPageCount(seq) - 1;
                }
            }
        }

        if (mark == null) {
            log.debug("found no marker with name: " + name);
            return null;
        } else {
            rm.bindMarker(mark);
            return rm;
        }
    }

    /**
     * Creates and returns a new page.
     * @param pageNumber the page number
     * @param isBlank true if it's a blank page
     * @return the newly created page
     */
    protected abstract Page createPage(int pageNumber, boolean isBlank);

    /**
     * Makes a new page
     *
     * @param isBlank whether this page is blank or not
     * @param isLast whether this page is the last page or not
     * @return a new page
     */
    protected Page makeNewPage(boolean isBlank, boolean isLast) {
        if (curPage != null) {
            finishPage();
        }

        currentPageNum++;

        curPage = createPage(currentPageNum, isBlank);

        if (log.isDebugEnabled()) {
            log.debug("[" + curPage.getPageViewport().getPageNumberString()
                    + (isBlank ? "*" : "") + "]");
        }

        addIDToPage(pageSeq.getId());
        return curPage;
    }

    /**
     * Finishes a page in preparation for a new page.
     */
    protected void finishPage() {
        if (log.isTraceEnabled()) {
            curPage.getPageViewport().dumpMarkers();
        }

        // Try to resolve any unresolved IDs for the current page.
        //
        idTracker.tryIDResolution(curPage.getPageViewport());
        // Queue for ID resolution and rendering
        areaTreeHandler.getAreaTreeModel().addPage(curPage.getPageViewport());
        if (log.isDebugEnabled()) {
            log.debug("page finished: " + curPage.getPageViewport().getPageNumberString()
                    + ", current num: " + currentPageNum);
        }
        curPage = null;
    }

    /** {@inheritDoc} */
    public void doForcePageCount(Numeric nextPageSeqInitialPageNumber) {

        int forcePageCount = pageSeq.getForcePageCount();

        // xsl-spec version 1.0   (15.oct 2001)
        // auto | even | odd | end-on-even | end-on-odd | no-force | inherit
        // auto:
        // Force the last page in this page-sequence to be an odd-page
        // if the initial-page-number of the next page-sequence is even.
        // Force it to be an even-page
        // if the initial-page-number of the next page-sequence is odd.
        // If there is no next page-sequence
        // or if the value of its initial-page-number is "auto" do not force any page.

        // if force-page-count is auto then set the value of forcePageCount
        // depending on the initial-page-number of the next page-sequence
        if (nextPageSeqInitialPageNumber != null && forcePageCount == Constants.EN_AUTO) {
            if (nextPageSeqInitialPageNumber.getEnum() != 0) {
                // auto | auto-odd | auto-even
                int nextPageSeqPageNumberType = nextPageSeqInitialPageNumber.getEnum();
                if (nextPageSeqPageNumberType == Constants.EN_AUTO_ODD) {
                    forcePageCount = Constants.EN_END_ON_EVEN;
                } else if (nextPageSeqPageNumberType == Constants.EN_AUTO_EVEN) {
                    forcePageCount = Constants.EN_END_ON_ODD;
                } else {   // auto
                    forcePageCount = Constants.EN_NO_FORCE;
                }
            } else { // <integer> for explicit page number
                int nextPageSeqPageStart = nextPageSeqInitialPageNumber.getValue();
                // spec rule
                nextPageSeqPageStart = (nextPageSeqPageStart > 0) ? nextPageSeqPageStart : 1;
                if (nextPageSeqPageStart % 2 == 0) {   // explicit even startnumber
                    forcePageCount = Constants.EN_END_ON_ODD;
                } else {    // explicit odd startnumber
                    forcePageCount = Constants.EN_END_ON_EVEN;
                }
            }
        }

        if (forcePageCount == Constants.EN_EVEN) {
            if ((currentPageNum - startPageNum + 1) % 2 != 0) { // we have an odd number of pages
                curPage = makeNewPage(true, false);
            }
        } else if (forcePageCount == Constants.EN_ODD) {
            if ((currentPageNum - startPageNum + 1) % 2 == 0) { // we have an even number of pages
                curPage = makeNewPage(true, false);
            }
        } else if (forcePageCount == Constants.EN_END_ON_EVEN) {
            if (currentPageNum % 2 != 0) { // we are now on an odd page
                curPage = makeNewPage(true, false);
            }
        } else if (forcePageCount == Constants.EN_END_ON_ODD) {
            if (currentPageNum % 2 == 0) { // we are now on an even page
                curPage = makeNewPage(true, false);
            }
        } else if (forcePageCount == Constants.EN_NO_FORCE) {
            // i hope: nothing special at all
        }

        if (curPage != null) {
            finishPage();
        }
    }

    /** {@inheritDoc} */
    public void reset() {
        throw new IllegalStateException();
    }

}
