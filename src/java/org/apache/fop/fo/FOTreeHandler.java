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

package org.apache.fop.fo;

// Java
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

// SAX
import org.xml.sax.SAXException;

// FOP
import org.apache.fop.apps.Document;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.Title;
import org.apache.fop.fo.extensions.Bookmarks;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.layoutmgr.AddLMVisitor;
import org.apache.fop.layoutmgr.ContentLayoutManager;
import org.apache.fop.layoutmgr.InlineStackingLayoutManager;
import org.apache.fop.layoutmgr.LMiter;
import org.apache.fop.layoutmgr.PageLayoutManager;


/**
 * Defines how SAX events specific to XSL-FO input should be handled when
 * an FO Tree needs to be built.
 * This initiates layout processes and corresponding
 * rendering processes such as start/end.
 * @see FOInputHandler
 */
public class FOTreeHandler extends FOInputHandler {

    // TODO: Collecting of statistics should be configurable
    private final boolean collectStatistics = true;
    private static final boolean MEM_PROFILE_WITH_GC = false;

    /**
     * Somewhere to get our stats from.
     */
    private Runtime runtime;

    /**
     * Keep track of the number of pages rendered.
     */
    private int pageCount;

    /**
     * Keep track of heap memory allocated,
     * for statistical purposes.
     */
    private long initialMemory;

    /**
     * Keep track of time used by renderer.
     */
    private long startTime;

    /** Useful only for allowing subclasses of AddLMVisitor to be set by those
     extending FOP **/
    private AddLMVisitor addLMVisitor = null;

    /**
     * Main constructor
     * @param foTreeControl the FOTreeControl implementation that governs this
     * FO Tree
     * @param store if true then use the store pages model and keep the
     *              area tree in memory
     */
    public FOTreeHandler(Document doc, boolean store) {
        super(doc);
        if (collectStatistics) {
            runtime = Runtime.getRuntime();
        }
    }

    /**
     * Start the document.
     * This starts the document in the renderer.
     *
     * @throws SAXException if there is an error
     */
    public void startDocument() throws SAXException {
        //Initialize statistics
        if (collectStatistics) {
            pageCount = 0;
            if (MEM_PROFILE_WITH_GC) {
                System.gc(); // This takes time but gives better results
            }

            initialMemory = runtime.totalMemory() - runtime.freeMemory();
            startTime = System.currentTimeMillis();
        }
    }

    /**
     * End the document.
     *
     * @throws SAXException if there is some error
     */
    public void endDocument() throws SAXException {
        try {
            getAreaTree().endDocument();
            getDriver().getRenderer().stopRenderer();
        } catch (IOException ex) {
            throw new SAXException(ex);
        }

        if (collectStatistics) {
            if (MEM_PROFILE_WITH_GC) {
                // This takes time but gives better results
                System.gc();
            }
            long memoryNow = runtime.totalMemory() - runtime.freeMemory();
            long memoryUsed = (memoryNow - initialMemory) / 1024L;
            long timeUsed = System.currentTimeMillis() - startTime;
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug("Initial heap size: " + (initialMemory / 1024L) + "Kb");
                logger.debug("Current heap size: " + (memoryNow / 1024L) + "Kb");
                logger.debug("Total memory used: " + memoryUsed + "Kb");
                if (!MEM_PROFILE_WITH_GC) {
                    logger.debug("  Memory use is indicative; no GC was performed");
                    logger.debug("  These figures should not be used comparatively");
                }
                logger.debug("Total time used: " + timeUsed + "ms");
                logger.debug("Pages rendered: " + pageCount);
                if (pageCount > 0) {
                    logger.debug("Avg render time: " + (timeUsed / pageCount) + "ms/page");
                }
            }
        }
    }

    /**
     * Start a page sequence.
     * At the start of a page sequence it can start the page sequence
     * on the area tree with the page sequence title.
     *
     * @param pageSeq the page sequence starting
     */
    public void startPageSequence(PageSequence pageSeq) {
    }

    /**
     * End the PageSequence.
     * The PageSequence formats Pages and adds them to the AreaTree.
     * The area tree then handles what happens with the pages.
     *
     * @param pageSequence the page sequence ending
     * @throws FOPException if there is an error formatting the pages
     */
    public void endPageSequence(PageSequence pageSequence)
                throws FOPException {
        //areaTree.setFontInfo(fontInfo);

        if (collectStatistics) {
            if (MEM_PROFILE_WITH_GC) {
                // This takes time but gives better results
                System.gc();
            }
            long memoryNow = runtime.totalMemory() - runtime.freeMemory();
            if (logger != null) {
                logger.debug("Current heap size: " + (memoryNow / 1024L) + "Kb");
            }
        }

        getAreaTree().addBookmarksToAreaTree();
        formatPageSequence(pageSequence, getAreaTree());
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startFlow(Flow)
     */
    public void startFlow(Flow fl) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endFlow(Flow)
     */
    public void endFlow(Flow fl) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startBlock(Block)
     */
    public void startBlock(Block bl) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endBlock(Block)
     */
    public void endBlock(Block bl) {
    }
    
    /**
     *
     * @param inl Inline that is starting.
     */
    public void startInline(Inline inl){
    }

    /**
     *
     * @param inl Inline that is ending.
     */
    public void endInline(Inline inl){
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startTable(Table)
     */
    public void startTable(Table tbl) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endTable(Table)
     */
    public void endTable(Table tbl) {
    }

    /**
     *
     * @param tc TableColumn that is starting;
     */
    public void startColumn(TableColumn tc) {
    }

    /**
     *
     * @param tc TableColumn that is ending;
     */
    public void endColumn(TableColumn tc) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startHeader(TableBody)
     */
    public void startHeader(TableBody th) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endHeader(TableBody)
     */
    public void endHeader(TableBody th) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startFooter(TableBody)
     */
    public void startFooter(TableBody tf) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endFooter(TableBody)
     */
    public void endFooter(TableBody tf) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startBody(TableBody)
     */
    public void startBody(TableBody tb) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endBody(TableBody)
     */
    public void endBody(TableBody tb) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startRow(TableRow)
     */
    public void startRow(TableRow tr) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endRow(TableRow)
     */
    public void endRow(TableRow tr) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startCell(TableCell)
     */
    public void startCell(TableCell tc) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endCell(TableCell)
     */
    public void endCell(TableCell tc) {
    }

    // Lists
    /**
     * @see org.apache.fop.fo.FOInputHandler#startList(ListBlock)
     */
    public void startList(ListBlock lb) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endList(ListBlock)
     */
    public void endList(ListBlock lb) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startListItem(ListItem)
     */
    public void startListItem(ListItem li) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endListItem(ListItem)
     */
    public void endListItem(ListItem li) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startListLabel()
     */
    public void startListLabel() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endListLabel()
     */
    public void endListLabel() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startListBody()
     */
    public void startListBody() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endListBody()
     */
    public void endListBody() {
    }

    // Static Regions
    /**
     * @see org.apache.fop.fo.FOInputHandler#startStatic()
     */
    public void startStatic() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endStatic()
     */
    public void endStatic() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startMarkup()
     */
    public void startMarkup() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endMarkup()
     */
    public void endMarkup() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startLink(BasicLink basicLink)
     */
    public void startLink(BasicLink basicLink) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endLink()
     */
    public void endLink() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#image(ExternalGraphic)
     */
    public void image(ExternalGraphic eg) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#pageRef()
     */
    public void pageRef() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#foreignObject(InstreamForeignObject)
     */
    public void foreignObject(InstreamForeignObject ifo) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startFootnote(Footnote)
     */
    public void startFootnote(Footnote footnote) {
    }
    
    /**
     * @see org.apache.fop.fo.FOInputHandler#endFootnote(Footnote)
     */
    public void endFootnote(Footnote footnote) {
    }
    
    /**
     * @see org.apache.fop.fo.FOInputHandler#startFootnoteBody(FootnoteBody)
     */
    public void startFootnoteBody(FootnoteBody body) {
    }
    
    /**
     * @see org.apache.fop.fo.FOInputHandler#endFootnoteBody(FootnoteBody)
     */
    public void endFootnoteBody(FootnoteBody body) {
    }
    
    /**
     * @see org.apache.fop.fo.FOInputHandler#leader(Leader)
     */
    public void leader(Leader l) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#characters(char[], int, int)
     */
    public void characters(char[] data, int start, int length) {
    }

    /**
     * Get the font information for the layout handler.
     *
     * @return the font information
     */
    public FOTreeControl getFontInfo() {
        return doc;
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
        PageLayoutManager pageLM = new PageLayoutManager(areaTree, pageSeq, 
            this);
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
    private org.apache.fop.area.Title getTitleArea(org.apache.fop.fo.pagination.Title foTitle) {
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

    /**
     *
     * @param pagenum PageNumber that is starting.
     */
    public void startPageNumber(PageNumber pagenum) {
    }

    /**
     *
     * @param pagenum PageNumber that is ending.
     */
    public void endPageNumber(PageNumber pagenum) {
    }
}
