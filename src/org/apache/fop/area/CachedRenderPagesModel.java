/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
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
 * the contents a reloaded.
 */
public class CachedRenderPagesModel extends AreaTree.RenderPagesModel {
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
     * @return true if the current page should be rendered
     *         false if the renderer doesn't support out of order
     *         rendering and there are pending pages
     */
    protected boolean checkPreparedPages(PageViewport newpage) {
        for (Iterator iter = prepared.iterator(); iter.hasNext();) {
            PageViewport p = (PageViewport)iter.next();
            if (p.isResolved()) {
                if(p != newpage) {
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
        if(newpage != null && newpage.getPage() != null) {
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

