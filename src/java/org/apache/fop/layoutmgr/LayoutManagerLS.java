/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

/* $Id$ */

package org.apache.fop.layoutmgr;

import org.apache.fop.apps.FOPException;
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.Title;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.layout.LayoutStrategy;

/**
 * The implementation of LayoutStrategy for the "redesign" or second generation
 * FOP layout.
 */
public class LayoutManagerLS extends LayoutStrategy {

    private static String name = "layoutmgr";
    /** Useful only for allowing subclasses of AddLMVisitor to be set by those
     extending FOP **/
    private AddLMVisitor addLMVisitor = null;

    public LayoutManagerLS() {
        super();
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

        // Initialize if already used?
        //    this.layoutMasterSet.resetPageMasters();
        if (pageSeq.getPageSequenceMaster() != null) {
            pageSeq.getPageSequenceMaster().reset();
        }

        pageSeq.initPageNumber();

        // This will layout pages and add them to the area tree
        PageLayoutManager pageLM = new PageLayoutManager(areaTree, pageSeq, this);
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
     * @return the Title area
     */
    public org.apache.fop.area.Title getTitleArea(org.apache.fop.fo.pagination.Title foTitle) {
        // use special layout manager to add the inline areas
        // to the Title.
        InlineStackingLayoutManager lm;
        lm = new InlineStackingLayoutManager();
        lm.setUserAgent(foTitle.getUserAgent());
        lm.setFObj(foTitle);
        lm.setLMiter(new LMiter(lm, foTitle.children.listIterator()));
        lm.initialize();

        // get breaks then add areas to title
        org.apache.fop.area.Title title =
                 new org.apache.fop.area.Title();

        ContentLayoutManager clm = new ContentLayoutManager(title);
        clm.setUserAgent(foTitle.getUserAgent());
        lm.setParent(clm);

        clm.fillArea(lm);

        return title;
    }

    /**
     * Public accessor to set the AddLMVisitor object that should be used.
     * This allows subclasses of AddLMVisitor to be used, which can be useful
     * for extensions to the FO Tree.
     * @param addLMVisitor the AddLMVisitor object that should be used.
     */
    public void setAddLMVisitor(AddLMVisitor addLMVisitor) {
        this.addLMVisitor = addLMVisitor;
    }

    /**
     * Public accessor to get the AddLMVisitor object that should be used.
     * @return the AddLMVisitor object that should be used.
     */
    public AddLMVisitor getAddLMVisitor() {
        if (this.addLMVisitor == null) {
            this.addLMVisitor = new AddLMVisitor();
        }
        return this.addLMVisitor;
    }

}
