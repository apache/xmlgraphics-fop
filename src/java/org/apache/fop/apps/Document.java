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

package org.apache.fop.apps;

// Java
import java.util.Map;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

// FOP
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.AreaTreeControl;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.Title;

import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.fo.FOTreeControl;
import org.apache.fop.fo.FOTreeEvent;
import org.apache.fop.fo.FOTreeListener;
import org.apache.fop.fo.extensions.Bookmarks;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.layoutmgr.AddLMVisitor;
import org.apache.fop.layoutmgr.ContentLayoutManager;
import org.apache.fop.layoutmgr.InlineStackingLayoutManager;
import org.apache.fop.layoutmgr.LMiter;
import org.apache.fop.layoutmgr.PageLayoutManager;


import org.apache.commons.logging.Log;

// SAX
import org.xml.sax.SAXException;

/**
 * Class storing information for the FOP Document being processed, and managing
 * the processing of it.
 */
public class Document implements FOTreeControl, FOTreeListener,
        AreaTreeControl {
            
    /** The parent Driver object */
    private Driver driver;

    /** The Font information relevant for this document */
    private FontInfo fontInfo;
    
    /** The current AreaTree for the PageSequence being rendered. */
    public AreaTree areaTree;

    /** The AreaTreeModel for the PageSequence being rendered. */
    public AreaTreeModel atModel;

    private Bookmarks bookmarks = null;

    /** Useful only for allowing subclasses of AddLMVisitor to be set by those
     extending FOP **/
     private AddLMVisitor addLMVisitor = null;

    /**
     * The current set of id's in the FO tree.
     * This is used so we know if the FO tree contains duplicates.
     */
    private Set idReferences = new HashSet();

    /**
     * Structure handler used to notify structure events
     * such as start end element.
     */
    public FOInputHandler foInputHandler;

    /**
     * Main constructor
     * @param driver the Driver object that is the "parent" of this Document
     */
    public Document(Driver driver) {
        this.driver = driver;
        this.fontInfo = new FontInfo();
    }

    /**
     * Retrieve the font information for this document
     * @return the FontInfo instance for this document
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    /**
     * Public accessor for the parent Driver of this Document
     * @return the parent Driver for this Document
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * Required by the FOTreeListener interface. It handles an
     * FOTreeEvent that is fired when a PageSequence object has been completed.
     * @param event the FOTreeEvent that was fired
     * @throws FOPException for errors in building the PageSequence
     */
    public void foPageSequenceComplete (FOTreeEvent event) throws FOPException {
        PageSequence pageSeq = event.getPageSequence();
        areaTree.addBookmarksToAreaTree();
        formatPageSequence(pageSeq, areaTree);
    }

    /**
     * Required by the FOTreeListener interface. It handles an FOTreeEvent that
     * is fired when the Document has been completely parsed.
     * @param event the FOTreeEvent that was fired
     * @throws SAXException for parsing errors
     */
    public void foDocumentComplete (FOTreeEvent event) throws SAXException {
        //processAreaTree(atModel);
        try {
            areaTree.endDocument();
            driver.getRenderer().stopRenderer();
        } catch (IOException ex) {
            throw new SAXException(ex);
        }
    }

    /**
     * Get the area tree for this layout handler.
     *
     * @return the area tree for this document
     */
    public AreaTree getAreaTree() {
        return areaTree;
    }

    /**
     * Set the Bookmarks object for this Document
     * @param bookmarks the Bookmarks object containing the bookmarks for this
     * Document
     */
    public void setBookmarks(Bookmarks bookmarks) {
        this.bookmarks = bookmarks;
    }

    /**
     * Public accessor for the Bookmarks for this Document
     * @return the Bookmarks for this Document
     */
    public Bookmarks getBookmarks() {
        return bookmarks;
    }

    /**
     * Retuns the set of ID references.
     * @return the ID references
     */
    public Set getIDReferences() {
        return idReferences;
    }

    /**
     * @return the FOInputHandler for parsing this FO Tree
     */
    public FOInputHandler getFOInputHandler() {
        return foInputHandler;
    }

    /**
     * Runs the formatting of this page sequence into the given area tree
     *
     * @param pageSeq the PageSequence to be formatted
     * @param areaTree the area tree to format this page sequence into
     * @throws FOPException if there is an error formatting the contents
     */
    private void formatPageSequence(PageSequence pageSeq, AreaTree areaTree) 
            throws FOPException {
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
        lm = new InlineStackingLayoutManager(foTitle);
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
