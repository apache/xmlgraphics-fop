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
package org.apache.fop.fo;

// Java
import java.util.HashSet;
import java.util.Iterator;

// SAX
import org.xml.sax.SAXException;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;

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

    /**
     * Collection of objects that have registered to be notified about
     * FOTreeEvent firings.
     */
    private HashSet foTreeListeners = new HashSet();

    /**
     * Main constructor
     * @param foTreeControl the FOTreeControl implementation that governs this
     * FO Tree
     * @param store if true then use the store pages model and keep the
     *              area tree in memory
     */
    public FOTreeHandler(FOTreeControl foTreeControl, boolean store) {
        super(foTreeControl);
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
        notifyDocumentComplete();

        if (collectStatistics) {
            if (MEM_PROFILE_WITH_GC) {
                // This takes time but gives better results
                System.gc();
            }
            long memoryNow = runtime.totalMemory() - runtime.freeMemory();
            long memoryUsed = (memoryNow - initialMemory) / 1024L;
            long timeUsed = System.currentTimeMillis() - startTime;
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Initial heap size: " + (initialMemory / 1024L) + "Kb");
                getLogger().debug("Current heap size: " + (memoryNow / 1024L) + "Kb");
                getLogger().debug("Total memory used: " + memoryUsed + "Kb");
                if (!MEM_PROFILE_WITH_GC) {
                    getLogger().debug("  Memory use is indicative; no GC was performed");
                    getLogger().debug("  These figures should not be used comparatively");
                }
                getLogger().debug("Total time used: " + timeUsed + "ms");
                getLogger().debug("Pages rendered: " + pageCount);
                if (pageCount > 0) {
                    getLogger().debug("Avg render time: " + (timeUsed / pageCount) + "ms/page");
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
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Current heap size: " + (memoryNow / 1024L) + "Kb");
            }
        }
        notifyPageSequenceComplete(pageSequence);
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
     * @see org.apache.fop.fo.FOInputHandler#startLink()
     */
    public void startLink() {
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
     * @see org.apache.fop.fo.FOInputHandler#footnote()
     */
    public void footnote() {
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
        return foTreeControl;
    }

    /**
     * Add an object to the collection of objects that should be notified about
     * FOTreeEvent firings.
     * @param listener the Object which should be notified
     */
    public void addFOTreeListener (FOTreeListener listener) {
        if (listener == null) {
            return;
        }
        foTreeListeners.add(listener);
    }

    /**
     * Remove an object from the collection of objects that should be notified
     * about FOTreeEvent firings.
     * @param listener the Object which should no longer be notified
     */
    public void removeFOTreeListener (FOTreeListener listener) {
        if (listener == null) {
            return;
        }
        foTreeListeners.remove(listener);
    }

    /**
     * Notify all objects in the foTreeListeners that a "Page Sequence Complete"
     * FOTreeEvent has been fired.
     * @param eventType integer indicating which type of event is created
     * @param event the Event object that should be passed to the listeners
     */
    private void notifyPageSequenceComplete(PageSequence pageSequence)
            throws FOPException {
        FOTreeEvent event = new FOTreeEvent(this);
        event.setPageSequence(pageSequence);
        Iterator iterator = foTreeListeners.iterator();
        FOTreeListener foTreeListenerItem = null;
        while (iterator.hasNext()) {
            foTreeListenerItem = (FOTreeListener)iterator.next();
            foTreeListenerItem.foPageSequenceComplete(event);
        }
    }

    /**
     * Notify all objects in the foTreeListeners that a "Document Complete"
     * FOTreeEvent has been fired.
     * @param eventType integer indicating which type of event is created
     * @param event the Event object that should be passed to the listeners
     */
    private void notifyDocumentComplete()
            throws SAXException {
        FOTreeEvent event = new FOTreeEvent(this);
        Iterator iterator = foTreeListeners.iterator();
        FOTreeListener foTreeListenerItem = null;
        while (iterator.hasNext()) {
            foTreeListenerItem = (FOTreeListener)iterator.next();
            foTreeListenerItem.foDocumentComplete(event);
        }
    }

}
