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

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

// SAX
import org.xml.sax.SAXException;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.StorePagesModel;
import org.apache.fop.area.Title;
import org.apache.fop.area.TreeExt;
import org.apache.fop.fo.StructureHandler;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.render.Renderer;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.Flow;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableRow;

/**
 * Layout handler that receives the structure events.
 * This initiates layout processes and corresponding
 * rendering processes such as start/end.
 */
public class LayoutHandler extends StructureHandler {

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
     * The stream to which this rendering is to be
     * written to. <B>Note</B> that some renderers
     * do not render to a stream, and that this
     * member can therefore be null.
     */
    private OutputStream outputStream;

    /**
     * The renderer being used.
     */
    private Renderer renderer;

    /**
     * The FontInfo for this renderer.
     */
    private FontInfo fontInfo = new FontInfo();

    /**
     * The current AreaTree for the PageSequence being rendered.
     */
    private AreaTree areaTree;
    private AreaTreeModel atModel;

    /**
     * Main constructor
     * @param outputStream the stream that the result is rendered to
     * @param renderer the renderer to call
     * @param store if true then use the store pages model and keep the
     *              area tree in memory
     */
    public LayoutHandler(OutputStream outputStream, Renderer renderer,
                         boolean store) {
        if (collectStatistics) {
            runtime = Runtime.getRuntime();
        }
        this.outputStream = outputStream;
        this.renderer = renderer;

        this.areaTree = new AreaTree();
        this.atModel = AreaTree.createRenderPagesModel(renderer);
        //this.atModel = new CachedRenderPagesModel(renderer);
        areaTree.setTreeModel(atModel);
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
        try {
            renderer.setupFontInfo(fontInfo);
            // check that the "any,normal,400" font exists
            if (!fontInfo.isSetupValid()) {
                throw new SAXException(new FOPException(
                        "No default font defined by OutputConverter"));
            }
            renderer.startRenderer(outputStream);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * End the document.
     *
     * @throws SAXException if there is some error
     */
    public void endDocument() throws SAXException {
        try {
            //processAreaTree(atModel);
            areaTree.endDocument();
            renderer.stopRenderer();
        } catch (Exception e) {
            throw new SAXException(e);
        }

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
        Title title = null;
        if (pageSeq.getTitleFO() != null) {
            title = pageSeq.getTitleFO().getTitleArea();
        }
        areaTree.startPageSequence(title);
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
        pageSequence.format(areaTree);
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startFlow(Flow)
     */
    public void startFlow(Flow fl) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endFlow(Flow)
     */
    public void endFlow(Flow fl) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startBlock(Block)
     */
    public void startBlock(Block bl) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endBlock(Block)
     */
    public void endBlock(Block bl) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startTable(Table)
     */
    public void startTable(Table tbl) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endTable(Table)
     */
    public void endTable(Table tbl) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startHeader(TableBody)
     */
    public void startHeader(TableBody th) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endHeader(TableBody)
     */
    public void endHeader(TableBody th) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startFooter(TableBody)
     */
    public void startFooter(TableBody tf) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endFooter(TableBody)
     */
    public void endFooter(TableBody tf) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startBody(TableBody)
     */
    public void startBody(TableBody tb) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endBody(TableBody)
     */
    public void endBody(TableBody tb) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startRow(TableRow)
     */
    public void startRow(TableRow tr) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endRow(TableRow)
     */
    public void endRow(TableRow tr) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startCell(TableCell)
     */
    public void startCell(TableCell tc) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endCell(TableCell)
     */
    public void endCell(TableCell tc) {
    }

    // Lists
    /**
     * @see org.apache.fop.apps.StructureHandler#startList(ListBlock)
     */
    public void startList(ListBlock lb) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endList(ListBlock)
     */
    public void endList(ListBlock lb) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startListItem(ListItem)
     */
    public void startListItem(ListItem li) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endListItem(ListItem)
     */
    public void endListItem(ListItem li) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startListLabel()
     */
    public void startListLabel() {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endListLabel()
     */
    public void endListLabel() {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startListBody()
     */
    public void startListBody() {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endListBody()
     */
    public void endListBody() {
    }

    // Static Regions
    /**
     * @see org.apache.fop.apps.StructureHandler#startStatic()
     */
    public void startStatic() {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endStatic()
     */
    public void endStatic() {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startMarkup()
     */
    public void startMarkup() {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endMarkup()
     */
    public void endMarkup() {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startLink()
     */
    public void startLink() {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endLink()
     */
    public void endLink() {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#image(ExternalGraphic)
     */
    public void image(ExternalGraphic eg) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#pageRef()
     */
    public void pageRef() {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#foreignObject(InstreamForeignObject)
     */
    public void foreignObject(InstreamForeignObject ifo) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#footnote()
     */
    public void footnote() {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#leader(Leader)
     */
    public void leader(Leader l) {
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#characters(char[], int, int)
     */
    public void characters(char[] data, int start, int length) {
    }

    /**
     * Process an area tree.
     * If a store pages model is used this can read and send all the
     * pages to the renderer.
     *
     * @param model the store pages model
     * @throws FOPException if there is an error
     */
    private void processAreaTree(StorePagesModel model) throws FOPException {
        int count = 0;
        int seqc = model.getPageSequenceCount();
        while (count < seqc) {
            Title title = model.getTitle(count);
            renderer.startPageSequence(title);
            int pagec = model.getPageCount(count);
            for (int c = 0; c < pagec; c++) {
                try {
                    renderer.renderPage(model.getPage(count, c));
                } catch (IOException ioex) {
                    throw new FOPException("I/O Error rendering page",
                                           ioex);
                }
            }
            count++;
        }
        List list = model.getEndExtensions();
        for (count = 0; count < list.size(); count++) {
            TreeExt ext = (TreeExt)list.get(count);
            renderer.renderExtension(ext);
        }
    }

    /**
     * Get the font information for the layout handler.
     *
     * @return the font information
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }
}

