/*
 * $Id: CachedRenderPagesModel.java,v 1.7 2003/03/05 15:19:31 jeremias Exp $
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
package org.apache.fop.area;

import org.apache.fop.render.Renderer;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
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
     * Create a new render pages model with the given renderer.
     * @param rend the renderer to render pages to
     */
    public CachedRenderPagesModel(Renderer rend) {
        super(rend);
    }

    /**
     * Check prepared pages
     * If a page is resolved it loads the page contents from
     * the file.
     *
     * @param newpage the new page being added
     * @return true if the current page should be rendered
     *         false if the renderer doesn't support out of order
     *         rendering and there are pending pages
     */
    protected boolean checkPreparedPages(PageViewport newpage) {
        for (Iterator iter = prepared.iterator(); iter.hasNext();) {
            PageViewport p = (PageViewport)iter.next();
            if (p.isResolved()) {
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

