/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.layoutmgr;

import org.apache.fop.area.extensions.BookmarkData;
import org.apache.fop.fo.extensions.Outline;
import org.apache.fop.apps.Document;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.LayoutStrategy;
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.Title;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.FOTreeHandler;
import org.apache.fop.apps.*;
import org.apache.fop.fo.extensions.*;

/**
 * The implementation of LayoutStrategy for the "redesign" or second generation
 * FOP layout.
 */
public class LayoutManagerLS extends LayoutStrategy {

    private static String name = "layoutmgr";

    public LayoutManagerLS(Document document) {
        super(document);
    }

    /**
     * Runs the formatting of this page sequence into the given area tree
     *
     * @param pageSeq the PageSequence to be formatted
     * @param areaTree the area tree to format this page sequence into
     * @throws FOPException if there is an error formatting the contents
     */
    public void format(PageSequence pageSeq, AreaTree areaTree) throws FOPException {
        Title title = null;
        if (pageSeq.getTitleFO() != null) {
            title = getTitleArea(pageSeq.getTitleFO());
        }
        areaTree.startPageSequence(title);
        // Make a new PageLayoutManager and a FlowLayoutManager
        // Run the PLM in a thread
        // Wait for them to finish.

        // If no main flow, nothing to layout!
        if (pageSeq.getMainFlow() == null) {
            return;
        }

        addBookmarksToAreaTree();

        // Initialize if already used?
        //    this.layoutMasterSet.resetPageMasters();
        if (pageSeq.getPageSequenceMaster() != null) {
            pageSeq.getPageSequenceMaster().reset();
        }

        int firstAvailPageNumber = 0;
        pageSeq.initPageNumber();

        // This will layout pages and add them to the area tree
        PageLayoutManager pageLM = new PageLayoutManager(areaTree, pageSeq);
        pageLM.setUserAgent(pageSeq.getUserAgent());
        pageLM.setFObj(pageSeq);
        pageLM.setPageCounting(pageSeq.getCurrentPageNumber(),
                               pageSeq.getPageNumberGenerator());

        // For now, skip the threading and just call run directly.
        pageLM.run();

        // Thread layoutThread = new Thread(pageLM);
//  layoutThread.start();
// log.debug("Layout thread started");

// // wait on both managers
// try {
//     layoutThread.join();
//     log.debug("Layout thread done");
// } catch (InterruptedException ie) {
//     log.error("PageSequence.format() interrupted waiting on layout");
// }
        pageSeq.setCurrentPageNumber(pageLM.getPageCount());
        // Tell the root the last page number we created.
        pageSeq.getRoot().setRunningPageNumberCounter(pageSeq.getCurrentPageNumber());
    }

    /**
     * When this element is finished then it can create
     * the bookmark data from the child elements and add
     * the extension to the area tree.
     */
    public void addBookmarksToAreaTree() {
        if (document.getBookmarks() == null) {
            return;
        }
        document.getDriver().getLogger().debug("adding bookmarks to area tree");
        BookmarkData data = new BookmarkData();
        for (int count = 0; count < document.getBookmarks().getOutlines().size(); count++) {
            Outline out = (Outline)(document.getBookmarks().getOutlines()).get(count);
            data.addSubData(createBookmarkData(out));
        }
        // add data to area tree for resolving and handling
        AreaTree at = document.getAreaTree();
        at.addTreeExtension(data);
        data.setAreaTree(at);
    }

    /**
     * Create and return the bookmark data for this outline.
     * This creates a bookmark data with the destination
     * and adds all the data from child outlines.
     *
     * @return the new bookmark data
     */
    public BookmarkData createBookmarkData(Outline outline) {
        BookmarkData data = new BookmarkData(outline.getInternalDestination());
        data.setLabel(outline.getLabel());
        for (int count = 0; count < outline.getOutlines().size(); count++) {
            Outline out = (Outline)(outline.getOutlines()).get(count);
            data.addSubData(createBookmarkData(out));
        }
        return data;
    }

    /**
     * @return the Title area
     */
    public org.apache.fop.area.Title getTitleArea(org.apache.fop.fo.pagination.Title foTitle) {
        org.apache.fop.area.Title title =
                 new org.apache.fop.area.Title();
        // use special layout manager to add the inline areas
        // to the Title.
        InlineStackingLayoutManager lm;
        lm = new InlineStackingLayoutManager();
        lm.setUserAgent(foTitle.getUserAgent());
        lm.setFObj(foTitle);
        lm.setLMiter(new LMiter(foTitle.children.listIterator()));
        lm.init();

        // get breaks then add areas to title

        ContentLayoutManager clm = new ContentLayoutManager(title);
        clm.setUserAgent(foTitle.getUserAgent());
        lm.setParent(clm);

        clm.fillArea(lm);

        return title;
    }

}
