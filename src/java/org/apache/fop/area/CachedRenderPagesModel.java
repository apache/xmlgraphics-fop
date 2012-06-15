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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.SAXException;

import org.apache.commons.io.IOUtils;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.TempResourceURIGenerator;
import org.apache.fop.fonts.FontInfo;

/**
 * A simple cached render pages model.
 * If the page is prepared for later rendering then this saves
 * the page contents to a file and once the page is resolved
 * the contents are reloaded.
 */
public class CachedRenderPagesModel extends RenderPagesModel {

    private Map<PageViewport, URI> pageMap = new HashMap<PageViewport, URI>();

    /** Base directory to save temporary file in, typically points to the user's temp dir. */
    private final URI tempBaseURI;
    private static final TempResourceURIGenerator TEMP_URI_GENERATOR
            = new TempResourceURIGenerator("cached-pages");

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
        tempBaseURI = TEMP_URI_GENERATOR.generate();
    }

    /** {@inheritDoc} */
    @Override
    protected boolean checkPreparedPages(PageViewport newpage, boolean renderUnresolved) {
        for (Iterator iter = prepared.iterator(); iter.hasNext();) {
            PageViewport pageViewport = (PageViewport)iter.next();
            if (pageViewport.isResolved() || renderUnresolved) {
                if (pageViewport != newpage) {
                    try {
                        // load page from cache
                        URI tempURI = pageMap.get(pageViewport);
                        log.debug("Loading page from: " + tempURI);
                        InputStream inStream = renderer.getUserAgent().getResourceResolver().getResource(tempURI);
                        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(inStream));
                        try {
                            pageViewport.loadPage(in);
                        } finally {
                            IOUtils.closeQuietly(inStream);
                            IOUtils.closeQuietly(in);
                        }
                        pageMap.remove(pageViewport);
                    } catch (Exception e) {
                        AreaEventProducer eventProducer = AreaEventProducer.Provider.get(
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
            String fname = "fop-page-" + page.getPageIndex() + ".ser";
            URI tempURI = tempBaseURI.resolve(fname);
            OutputStream outStream = renderer.getUserAgent().getResourceResolver().getOutputStream(tempURI);
            tempstream = new ObjectOutputStream(new BufferedOutputStream(outStream));
            try {
                page.savePage(tempstream);
            } finally {
                IOUtils.closeQuietly(tempstream);
            }
            pageMap.put(page, tempURI);
            if (log.isDebugEnabled()) {
                log.debug("Page saved to temporary file: " + tempURI);
            }
        } catch (IOException ioe) {
            AreaEventProducer eventProducer
                = AreaEventProducer.Provider.get(
                    renderer.getUserAgent().getEventBroadcaster());
            eventProducer.pageSaveError(this, page.getPageNumberString(), ioe);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }
}

