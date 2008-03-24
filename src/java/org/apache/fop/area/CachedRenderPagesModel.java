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
 
package org.apache.fop.area;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.SAXException;

import org.apache.commons.io.IOUtils;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.events.ResourceEventProducer;
import org.apache.fop.fonts.FontInfo;

/**
 * A simple cached render pages model.
 * If the page is prepared for later rendering then this saves
 * the page contents to a file and once the page is resolved
 * the contents are reloaded.
 */
public class CachedRenderPagesModel extends RenderPagesModel {
    private Map pageMap = new HashMap();

    /** Base directory to save temporary file in, typically points to the user's temp dir. */
    protected File baseDir;
    
    /**
     * Main Constructor
     * @param userAgent FOUserAgent object for process
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @param fontInfo FontInfo object
     * @param stream OutputStream
     * @throws FOPException if the renderer cannot be properly initialized
     */
    public CachedRenderPagesModel (FOUserAgent userAgent, String outputFormat, 
            FontInfo fontInfo, OutputStream stream) throws FOPException {
        super(userAgent, outputFormat, fontInfo, stream);
        this.baseDir = new File(System.getProperty("java.io.tmpdir"));
    }

    /**
     * {@inheritDoc} 
     */
    protected boolean checkPreparedPages(PageViewport newpage, boolean renderUnresolved) {
        for (Iterator iter = prepared.iterator(); iter.hasNext();) {
            PageViewport pageViewport = (PageViewport)iter.next();
            if (pageViewport.isResolved() || renderUnresolved) {
                if (pageViewport != newpage) {
                    try {
                        // load page from cache
                        String name = (String)pageMap.get(pageViewport);
                        File tempFile = new File(baseDir, name);
                        log.debug("Loading page from: " + tempFile);
                        ObjectInputStream in = new ObjectInputStream(
                                             new BufferedInputStream(
                                               new FileInputStream(tempFile)));
                        try {
                            pageViewport.loadPage(in);
                        } finally {
                            IOUtils.closeQuietly(in);
                        }
                        if (!tempFile.delete()) {
                            ResourceEventProducer eventProducer
                                = ResourceEventProducer.Factory.create(
                                        renderer.getUserAgent().getEventBroadcaster());
                            eventProducer.cannotDeleteTempFile(this, tempFile);
                        }
                        pageMap.remove(pageViewport);
                    } catch (Exception e) {
                        AreaEventProducer eventProducer
                            = AreaEventProducer.Factory.create(
                                renderer.getUserAgent().getEventBroadcaster());
                        eventProducer.pageLoadError(this, pageViewport.getPageNumberString(), e);
                    }
                }

                renderPage(pageViewport);
                pageViewport.clear();
                iter.remove();
            } else {
                if (!renderer.supportsOutOfOrder()) {
                    break;
                }
            }
        }
        if (newpage != null && newpage.getPage() != null) {
            savePage(newpage);
            newpage.clear();
        }
        return renderer.supportsOutOfOrder() || prepared.isEmpty();
    }

    /**
     * Save a page.
     * It saves the contents of the page to a file.
     *
     * @param page the page to prepare
     */
    protected void savePage(PageViewport page) {
        try {
            // save page to cache
            ObjectOutputStream tempstream;
            String fname = "fop-page-" + page.toString() + ".ser";
            File tempFile = new File(baseDir, fname);
            tempFile.deleteOnExit();
            tempstream = new ObjectOutputStream(new BufferedOutputStream(
                                                new FileOutputStream(tempFile)));
            try {
                page.savePage(tempstream);
            } finally {
                IOUtils.closeQuietly(tempstream);
            }
            pageMap.put(page, fname);
            if (log.isDebugEnabled()) {
                log.debug("Page saved to temporary file: " + tempFile);
            }
        } catch (IOException ioe) {
            AreaEventProducer eventProducer
                = AreaEventProducer.Factory.create(
                    renderer.getUserAgent().getEventBroadcaster());
            eventProducer.pageSaveError(this, page.getPageNumberString(), ioe);
        }
    }

    /** {@inheritDoc} */
    public void endDocument() throws SAXException {
        super.endDocument();
    }
}

