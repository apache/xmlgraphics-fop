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
import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.fo.pagination.SimplePageMaster;

/**
 * <p>This class delivers Page instances. It also caches them as necessary.
 * </p>
 * <p>Additional functionality makes sure that surplus instances that are requested by the
 * page breaker are properly discarded, especially in situations where hard breaks cause
 * blank pages. The reason for that: The page breaker sometimes needs to preallocate
 * additional pages since it doesn't know exactly until the end how many pages it really needs.
 * </p>
 */
public class PageProvider implements Constants {

    private Log log = LogFactory.getLog(PageProvider.class);

    /** Indices are evaluated relative to the first page in the page-sequence. */
    public static final int RELTO_PAGE_SEQUENCE = 0;
    /** Indices are evaluated relative to the first page in the current element list. */
    public static final int RELTO_CURRENT_ELEMENT_LIST = 1;

    private int startPageOfPageSequence;
    private int startPageOfCurrentElementList;
    private int startColumnOfCurrentElementList;
    private boolean spanAllForCurrentElementList;
    private List<Page> cachedPages = new java.util.ArrayList<Page>();

    private int lastPageIndex = -1;
    private int indexOfCachedLastPage = -1;

    //Cache to optimize getAvailableBPD() calls
    private int lastRequestedIndex = -1;
    private int lastReportedBPD = -1;

    /**
     * AreaTreeHandler which activates the PSLM and controls
     * the rendering of its pages.
     */
    private AreaTreeHandler areaTreeHandler;

    /**
     * fo:page-sequence formatting object being
     * processed by this class
     */
    private PageSequence pageSeq;

    /**
     * Main constructor.
     * @param ath the area tree handler
     * @param ps The page-sequence the provider operates on
     */
    public PageProvider(AreaTreeHandler ath, PageSequence ps) {
        this.areaTreeHandler = ath;
        this.pageSeq = ps;
        this.startPageOfPageSequence = ps.getStartingPageNumber();
    }

    /**
     * The page breaker notifies the provider about the page number an element list starts
     * on so it can later retrieve PageViewports relative to this first page.
     * @param startPage the number of the first page for the element list.
     * @param startColumn the starting column number for the element list.
     * @param spanAll true if the current element list is for a column-spanning section
     */
    public void setStartOfNextElementList(int startPage, int startColumn, boolean spanAll) {
        if (log.isDebugEnabled()) {
            log.debug("start of the next element list is:"
                    + " page=" + startPage + " col=" + startColumn
                    + (spanAll ? ", column-spanning" : ""));
        }
        this.startPageOfCurrentElementList = startPage - startPageOfPageSequence + 1;
        this.startColumnOfCurrentElementList = startColumn;
        this.spanAllForCurrentElementList = spanAll;
        //Reset Cache
        this.lastRequestedIndex = -1;
        this.lastReportedBPD = -1;
    }

    /**
     * Sets the index of the last page. This is done as soon as the position of the last page
     * is known or assumed.
     * @param index the index relative to the first page in the page-sequence
     */
    public void setLastPageIndex(int index) {
        this.lastPageIndex = index;
    }

    /**
     * Returns the available BPD for the part/page indicated by the index parameter.
     * The index is the part/page relative to the start of the current element list.
     * This method takes multiple columns into account.
     * @param index zero-based index of the requested part/page
     * @return the available BPD
     */
    public int getAvailableBPD(int index) {
        //Special optimization: There may be many equal calls by the BreakingAlgorithm
        if (this.lastRequestedIndex == index) {
            if (log.isTraceEnabled()) {
                log.trace("getAvailableBPD(" + index + ") -> (cached) " + lastReportedBPD);
            }
            return this.lastReportedBPD;
        }
        int c = index;
        int pageIndex = 0;
        int colIndex = startColumnOfCurrentElementList;
        Page page = getPage(
                false, pageIndex, RELTO_CURRENT_ELEMENT_LIST);
        while (c > 0) {
            colIndex++;
            if (colIndex >= page.getPageViewport().getCurrentSpan().getColumnCount()) {
                colIndex = 0;
                pageIndex++;
                page = getPage(
                        false, pageIndex, RELTO_CURRENT_ELEMENT_LIST);
            }
            c--;
        }
        this.lastRequestedIndex = index;
        this.lastReportedBPD = page.getPageViewport().getBodyRegion().getRemainingBPD();
        if (log.isTraceEnabled()) {
            log.trace("getAvailableBPD(" + index + ") -> " + lastReportedBPD);
        }
        return this.lastReportedBPD;
    }

    private static class Column {

        final Page page;

        final int pageIndex;

        final int colIndex;

        final int columnCount;

        Column(Page page, int pageIndex, int colIndex, int columnCount) {
            this.page = page;
            this.pageIndex = pageIndex;
            this.colIndex = colIndex;
            this.columnCount = columnCount;
        }

    }

    private Column getColumn(int index) {
        int columnCount = 0;
        int colIndex = startColumnOfCurrentElementList + index;
        int pageIndex = -1;
        Page page;
        do {
            colIndex -= columnCount;
            pageIndex++;
            page = getPage(false, pageIndex, RELTO_CURRENT_ELEMENT_LIST);
            columnCount = page.getPageViewport().getCurrentSpan().getColumnCount();
        } while (colIndex >= columnCount);
        return new Column(page, pageIndex, colIndex, columnCount);
    }

    /**
     * Compares the IPD of the given part with the following one.
     *
     * @param index index of the current part
     * @return a negative integer, zero or a positive integer as the current IPD is less
     * than, equal to or greater than the IPD of the following part
     */
    public int compareIPDs(int index) {
        Column column = getColumn(index);
        if (column.colIndex + 1 < column.columnCount) {
            // Next part is a column on same page => same IPD
            return 0;
        } else {
            Page nextPage = getPage(false, column.pageIndex + 1, RELTO_CURRENT_ELEMENT_LIST);
            return column.page.getPageViewport().getBodyRegion().getIPD()
                    - nextPage.getPageViewport().getBodyRegion().getIPD();
        }
    }

    /**
     * Checks if a break at the passed index would start a new page
     * @param index the index of the element before the break
     * @return  {@code true} if the break starts a new page
     */
    boolean startPage(int index) {
        return getColumn(index).colIndex == 0;
    }

    /**
     * Checks if a break at the passed index would end a page
     * @param index the index of the element before the break
     * @return  {@code true} if the break ends a page
     */
    boolean endPage(int index) {
        Column column = getColumn(index);
        return column.colIndex == column.columnCount - 1;
    }

    /**
     * Obtain the applicable column-count for the element at the
     * passed index
     * @param index the index of the element
     * @return  the number of columns
     */
    int getColumnCount(int index) {
        return getColumn(index).columnCount;
    }

    /**
     * Returns the part index (0<x<partCount) which denotes the first part on the last page
     * generated by the current element list.
     * @param partCount Number of parts determined by the breaking algorithm
     * @return the requested part index
     */
    public int getStartingPartIndexForLastPage(int partCount) {
        int lastPartIndex = partCount - 1;
        return lastPartIndex - getColumn(lastPartIndex).colIndex;
    }

    Page getPageFromColumnIndex(int columnIndex) {
        return getColumn(columnIndex).page;
    }

    /**
     * Returns a Page.
     * @param isBlank true if this page is supposed to be blank.
     * @param index Index of the page (see relativeTo)
     * @param relativeTo Defines which value the index parameter should be evaluated relative
     * to. (One of PageProvider.RELTO_*)
     * @return the requested Page
     */
    public Page getPage(boolean isBlank, int index, int relativeTo) {
        if (relativeTo == RELTO_PAGE_SEQUENCE) {
            return getPage(isBlank, index);
        } else if (relativeTo == RELTO_CURRENT_ELEMENT_LIST) {
            int effIndex = startPageOfCurrentElementList + index;
            effIndex += startPageOfPageSequence - 1;
            return getPage(isBlank, effIndex);
        } else {
            throw new IllegalArgumentException(
                    "Illegal value for relativeTo: " + relativeTo);
        }
    }

    /**
     * Returns a Page.
     * @param isBlank true if the Page should be a blank one
     * @param index the Page's index
     * @return a Page instance
     */
    protected Page getPage(boolean isBlank, int index) {
        boolean isLastPage = (lastPageIndex >= 0) && (index == lastPageIndex);
        if (log.isTraceEnabled()) {
            log.trace("getPage(" + index + " " + (isBlank ? "blank" : "non-blank")
                    + (isLastPage ? " <LAST>" : "") + ")");
        }
        int intIndex = index - startPageOfPageSequence;
        if (log.isTraceEnabled()) {
            if (isBlank) {
                log.trace("blank page requested: " + index);
            }
            if (isLastPage) {
                log.trace("last page requested: " + index);
            }
        }
        if (intIndex > cachedPages.size()) {
            throw new UnsupportedOperationException("Cannot handle holes in page cache");
        } else if (intIndex == cachedPages.size()) {
            if (log.isTraceEnabled()) {
                log.trace("Caching " + index);
            }
            cacheNextPage(index, isBlank, isLastPage, this.spanAllForCurrentElementList);
        }
        Page page = cachedPages.get(intIndex);
        boolean replace = false;
        if (page.getPageViewport().isBlank() != isBlank) {
            log.debug("blank condition doesn't match. Replacing PageViewport.");
            replace = true;
        }
        if (page.getPageViewport().getCurrentSpan().getColumnCount() == 1
                && !this.spanAllForCurrentElementList) {
            RegionBody rb = (RegionBody)page.getSimplePageMaster().getRegion(Region.FO_REGION_BODY);
            int colCount = rb.getColumnCount();
            if (colCount > 1) {
                log.debug("Span doesn't match. Replacing PageViewport.");
                replace = true;
            }
        }
        if ((isLastPage && indexOfCachedLastPage != intIndex)
                || (!isLastPage && indexOfCachedLastPage >= 0)) {
            log.debug("last page condition doesn't match. Replacing PageViewport.");
            replace = true;
            indexOfCachedLastPage = (isLastPage ? intIndex : -1);
        }
        if (replace) {
            discardCacheStartingWith(intIndex);
            PageViewport oldPageVP = page.getPageViewport();
            page = cacheNextPage(index, isBlank, isLastPage, this.spanAllForCurrentElementList);
            PageViewport newPageVP = page.getPageViewport();
            newPageVP.replace(oldPageVP);
            this.areaTreeHandler.getIDTracker().replacePageViewPort(oldPageVP, newPageVP);
        }
        return page;
    }

    private void discardCacheStartingWith(int index) {
        while (index < cachedPages.size()) {
            this.cachedPages.remove(cachedPages.size() - 1);
            if (!pageSeq.goToPreviousSimplePageMaster()) {
                log.warn("goToPreviousSimplePageMaster() on the first page called!");
            }
        }
    }

    private Page cacheNextPage(int index, boolean isBlank, boolean isLastPage, boolean spanAll) {
        String pageNumberString = pageSeq.makeFormattedPageNumber(index);
        boolean isFirstPage = (startPageOfPageSequence == index);
        SimplePageMaster spm = pageSeq.getNextSimplePageMaster(
                index, isFirstPage, isLastPage, isBlank);
        Page page = new Page(spm, index, pageNumberString, isBlank, spanAll);
        //Set unique key obtained from the AreaTreeHandler
        page.getPageViewport().setKey(areaTreeHandler.generatePageViewportKey());
        page.getPageViewport().setForeignAttributes(spm.getForeignAttributes());
        page.getPageViewport().setWritingModeTraits(pageSeq);
        cachedPages.add(page);
        return page;
    }

    /**
     * Indicates whether the column/page at the given index is on the first page of the page sequence.
     *
     * @return {@code true} if the given part is on the first page of the sequence
     */
    boolean isOnFirstPage(int partIndex) {
        Column column = getColumn(partIndex);
        return startPageOfCurrentElementList + column.pageIndex == startPageOfPageSequence;
    }

}
