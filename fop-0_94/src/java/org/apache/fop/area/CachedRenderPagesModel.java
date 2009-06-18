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

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.xml.sax.SAXException;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;

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
     * @see org.apache.fop.area.RenderPagesModel#checkPreparedPages(PageViewport, boolean)
     */
    protected boolean checkPreparedPages(PageViewport newpage, boolean renderUnresolved) {
        for (Iterator iter = prepared.iterator(); iter.hasNext();) {
            PageViewport p = (PageViewport)iter.next();
            if (p.isResolved() || renderUnresolved) {
                if (p != newpage) {
                    try {
                        // load page from cache
                        String name = (String)pageMap.get(p);
                        File tempFile = new File(baseDir, name);
                        log.debug("Loading page from: " + tempFile);
                        ObjectInputStream in = new ObjectInputStream(
                                             new BufferedInputStream(
                                               new FileInputStream(tempFile)));
                        try {
                            p.loadPage(in);
                        } finally {
                            IOUtils.closeQuietly(in);
                        }
                        if (!tempFile.delete()) {
                            log.warn("Temporary file could not be deleted: " + tempFile);
                        }
                        pageMap.remove(p);
                    } catch (Exception e) {
                        log.error(e);
                    }
                }

                try {
                    renderer.renderPage(p);
                    if (!p.isResolved()) {
                        String[] idrefs = p.getIDRefs();
                        for (int count = 0; count < idrefs.length; count++) {
                            log.warn("Page " + p.getPageNumberString()
                                + ": Unresolved id reference \"" + idrefs[count] 
                                + "\" found.");
                        }
                    }
                } catch (Exception e) {
                    // use error handler to handle this FOP or IO Exception
                    log.error(e);
                }
                p.clear();
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
        } catch (Exception e) {
            log.error(e);
        }
    }

    /** @see org.apache.fop.area.RenderPagesModel#endDocument() */
    public void endDocument() throws SAXException {
        super.endDocument();
    }
}

