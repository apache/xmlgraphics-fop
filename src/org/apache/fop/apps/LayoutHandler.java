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
package org.apache.fop.apps;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

// SAX
import org.xml.sax.SAXException;

// FOP
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.StorePagesModel;
import org.apache.fop.area.Title;
import org.apache.fop.area.TreeExt;
import org.apache.fop.fo.pagination.LayoutMasterSet;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.render.Renderer;

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
     * @param seqTitle the title of the page sequence
     * @param lms the layout master set
     */
    public void startPageSequence(PageSequence pageSeq, 
                                  org.apache.fop.fo.Title seqTitle, 
                                  LayoutMasterSet lms) {
        Title title = null;
        if (seqTitle != null) {
            title = seqTitle.getTitleArea();
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

