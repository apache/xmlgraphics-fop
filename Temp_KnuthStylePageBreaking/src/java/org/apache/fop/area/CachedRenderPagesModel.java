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
 
package org.apache.fop.area;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;

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

    /**
     * Constructor
     * @see org.apache.fop.area.RenderPagesModel(FOUserAgent, int, FontInfo, OutputStream)
     */
    public CachedRenderPagesModel (FOUserAgent userAgent, int renderType, 
        FontInfo fontInfo, OutputStream stream) throws FOPException {
        super(userAgent, renderType, fontInfo, stream);
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
                        File temp = new File(name);
                        System.out.println("page serialized to: " + temp.length());
                        ObjectInputStream in = new ObjectInputStream(
                                             new BufferedInputStream(
                                               new FileInputStream(temp)));
                        p.loadPage(in);
                        in.close();
                        temp.delete();
                        pageMap.remove(p);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    renderer.renderPage(p);
                    if (!p.isResolved()) {
                        String[] idrefs = p.getIDRefs();
                        for (int count = 0; count < idrefs.length; count++) {
                            log.warn("Page " + p.getPageNumberString() + 
                                ": Unresolved id reference \"" + idrefs[count] 
                                + "\" found.");
                        }
                    }
                } catch (Exception e) {
                    // use error handler to handle this FOP or IO Exception
                    e.printStackTrace();
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
            String fname = "page" + page.toString() + ".ser";
            tempstream = new ObjectOutputStream(new BufferedOutputStream(
                                                new FileOutputStream(fname)));
            page.savePage(tempstream);
            tempstream.close();
            pageMap.put(page, fname);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

