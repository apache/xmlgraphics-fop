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

import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.SAXException;

import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.extensions.ExtensionObj;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.Page;
import org.apache.fop.render.Renderer;

import org.apache.avalon.framework.logger.Logger;

/**
  This class acts as a bridge between the XML:FO parser
  and the formatting/rendering classes. It will queue
  PageSequences up until all the IDs required by them
  are satisfied, at which time it will render the
  pages.<P>

  StreamRenderer is created by Driver and called from
  FOTreeBuilder when a PageSequence is created,
  and AreaTree when a Page is formatted.<P>
*/
public class StreamRenderer {
    private static final boolean MEM_PROFILE_WITH_GC = false;

    /**
      Somewhere to get our stats from.
    */
    private Runtime runtime = Runtime.getRuntime();

    /**
      Keep track of the number of pages rendered.
    */
    int pageCount = 0;

    /**
      Keep track of heap memory allocated,
      for statistical purposes.
    */
    private long initialMemory;

    /**
      Keep track of time used by renderer.
    */
    private long startTime;

    /**
      The stream to which this rendering is to be
      written to. <B>Note</B> that some renderers
      do not render to a stream, and that this
      member can therefore be null.
    */
    private OutputStream outputStream;

    /**
      The renderer being used.
    */
    private Renderer renderer;

    /**
     * The formatting results to be handed back to the caller.
     */
    private FormattingResults results = new FormattingResults();

    /**
      The FontInfo for this renderer.
    */
    private FontInfo fontInfo = new FontInfo();

    /**
      The list of pages waiting to be renderered.
    */
    private ArrayList renderQueue = new ArrayList();

    /**
      The current set of IDReferences, passed to the
      areatrees and pages. This is used by the AreaTree
      as a single map of all IDs.
    */
    private IDReferences idReferences = new IDReferences();

    /**
     * The list of extensions.
     */
    private ArrayList extensions = new ArrayList();

    /**
     * The list of markers.
     */
    private ArrayList documentMarkers;
    private ArrayList currentPageSequenceMarkers;
    private PageSequence currentPageSequence;
    
    private Logger log;

    public StreamRenderer(OutputStream outputStream, Renderer renderer) {
        this.outputStream = outputStream;
        this.renderer = renderer;
    }

    public void setLogger(Logger logger) {
        log = logger;
    }

    public IDReferences getIDReferences() {
        return idReferences;
    }

    public FormattingResults getResults() {
        return this.results;
    }

    public void addExtension(ExtensionObj ext) {
        extensions.add(ext);
    }

    public void startRenderer()
    throws SAXException {
        pageCount = 0;

        if (MEM_PROFILE_WITH_GC)
            System.gc();    // This takes time but gives better results

        initialMemory = runtime.totalMemory() - runtime.freeMemory();
        startTime = System.currentTimeMillis();

        try {
            renderer.setupFontInfo(fontInfo);
            renderer.startRenderer(outputStream);
        } catch (FOPException fe) {
            throw new SAXException(fe);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void stopRenderer()
    throws SAXException {
        /*
          Force the processing of any more queue elements,
          even if they are not resolved.
        */
        try {
            processQueue(true);
            renderer.stopRenderer(outputStream);
        } catch (FOPException e) {
            throw new SAXException(e);
        }
        catch (IOException e) {
            throw new SAXException(e);
        }

        if (MEM_PROFILE_WITH_GC)
            System.gc();    // This takes time but gives better results

        long memoryNow = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = (memoryNow - initialMemory) / 1024L;

        log.debug("Initial heap size: " + (initialMemory/1024L) + "Kb");
        log.debug("Current heap size: " + (memoryNow/1024L) + "Kb");
        log.debug("Total memory used: " + memoryUsed + "Kb");

        if (!MEM_PROFILE_WITH_GC) {
            log.debug("  Memory use is indicative; no GC was performed");
            log.debug("  These figures should not be used comparatively");
        }

        long timeUsed = System.currentTimeMillis() - startTime;

        log.debug("Total time used: " + timeUsed + "ms");
        log.debug("Pages rendered: " + pageCount);
        if (pageCount != 0) {
            log.debug("Avg render time: " + (timeUsed / pageCount) + "ms/page");
        }
    }

    /**
      Format the PageSequence. The PageSequence
      formats Pages and adds them to the AreaTree,
      which subsequently calls the StreamRenderer
      instance (this) again to render the page.
      At this time the page might be printed
      or it might be queued. A page might not
      be renderable immediately if the IDReferences
      are not all valid. In this case we defer
      the rendering until they are all valid.
    */
    public void render(PageSequence pageSequence)
    throws SAXException {
        AreaTree a = new AreaTree(this);
        a.setFontInfo(fontInfo);

        for(int i = 0; i < extensions.size(); i++ ) {
            ExtensionObj ext = (ExtensionObj)extensions.get(i);
            try {
                ext.format(a);
            } catch (FOPException fope) {
                throw new SAXException(fope);
            }
        }

        try {
            pageSequence.format(a);
        } catch (FOPException e) {
            throw new SAXException(e);
        }
        this.results.haveFormattedPageSequence(pageSequence);
        log.debug("Last page-sequence produced "+pageSequence.getPageCount()+" pages.");
    }

    public synchronized void queuePage(Page page)
    throws FOPException, IOException {

        // process markers
        PageSequence pageSequence = page.getPageSequence();
        if (pageSequence != currentPageSequence) {
            currentPageSequence = pageSequence;
            currentPageSequenceMarkers = null;
        }
        ArrayList markers = page.getMarkers();
        if (markers != null) {
            if (documentMarkers == null) {
                documentMarkers = new ArrayList();
            }
            if (currentPageSequenceMarkers == null) {
                currentPageSequenceMarkers = new ArrayList();
            }
            for (int i=0;i<markers.size();i++) {
                Marker marker = (Marker)markers.get(i);
                marker.releaseRegistryArea();
                currentPageSequenceMarkers.add(marker);
                documentMarkers.add(marker);
            }
        }
        
        /*
          Try to optimise on the common case that there are
          no pages pending and that all ID references are
          valid on the current pages. This short-cuts the
          pipeline and renders the area immediately.
        */
        if ((renderQueue.size() == 0) && idReferences.isEveryIdValid()) {
            renderer.render(page, outputStream);
        } else {
            RenderQueueEntry entry = new RenderQueueEntry(page);
            renderQueue.add(entry);
            /*
              The just-added entry could (possibly) resolve the
              waiting entries, so we try to process the queue
              now to see.
            */
            processQueue(false);
        }
        pageCount++;
    }

    /**
      Try to process the queue from the first entry forward.
      If an entry can't be processed, then the queue can't
      move forward, so return.
    */
    private synchronized void processQueue(boolean force)
    throws FOPException, IOException {
        while (renderQueue.size() > 0) {
            RenderQueueEntry entry = (RenderQueueEntry) renderQueue.get(0);
            if ((!force) && (!entry.isResolved()))
                break;

            renderer.render(entry.getPage(), outputStream);
            renderQueue.remove(0);
        }
    }

    /**
      A RenderQueueEntry consists of the Page to be queued,
      plus a list of outstanding ID references that need to be
      resolved before the Page can be renderered.<P>
    */
    class RenderQueueEntry {
        /*
          The Page that has outstanding ID references.
        */
        private Page page;

        /*
          A list of ID references (names).
        */
        private ArrayList unresolvedIdReferences = new ArrayList();

        public RenderQueueEntry(Page page) {
            this.page = page;

            Iterator e = idReferences.getInvalidElements();
            while (e.hasNext())
                unresolvedIdReferences.add(e.next());
        }

        public Page getPage() {
            return page;
        }

        /**
          See if the outstanding references are resolved
          in the current copy of IDReferences.
        */
        public boolean isResolved() {
            if ((unresolvedIdReferences.size() == 0) || idReferences.isEveryIdValid())
                return true;

            //
            // See if any of the unresolved references are still unresolved.
            //
            for (int i = 0; i< unresolvedIdReferences.size(); i++)
                if (!idReferences.doesIDExist((String)unresolvedIdReferences.get(i)))
                    return false;

            unresolvedIdReferences.clear();
            return true;
        }
    }

    // Auxillary function for retrieving markers.
    public ArrayList getDocumentMarkers() {
        return documentMarkers;
    }

    // Auxillary function for retrieving markers.
    public PageSequence getCurrentPageSequence() {
        return currentPageSequence;
    }

    // Auxillary function for retrieving markers.
    public ArrayList getCurrentPageSequenceMarkers() {
        return currentPageSequenceMarkers;
    }
}
