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

// FOP
import org.apache.fop.render.Renderer;

// Java
import java.util.List;
import java.util.Iterator;

/**
 * This uses the store pages model to store the pages
 * each page is either rendered if ready or prepared
 * for later rendering.
 * Once a page is rendered it is cleared to release the
 * contents but the PageViewport is retained. So even
 * though the pages are stored the contents are discarded.
 */
public class RenderPagesModel extends StorePagesModel {
    /**
     * The renderer that will render the pages.
     */
    protected Renderer renderer;
    /**
     * Pages that have been prepared but not rendered yet.
     */
    protected List prepared = new java.util.ArrayList();
    private List pendingExt = new java.util.ArrayList();
    private List endDocExt = new java.util.ArrayList();

    /**
     * Create a new render pages model with the given renderer.
     * @param rend the renderer to render pages to
     */
    public RenderPagesModel(Renderer rend) {
        renderer = rend;
    }

    /**
     * Start a new page sequence.
     * This tells the renderer that a new page sequence has
     * started with the given title.
     * @param title the title of the new page sequence
     */
    public void startPageSequence(Title title) {
        super.startPageSequence(title);
        renderer.startPageSequence(title);
    }

    /**
     * Add a page to the render page model.
     * If the page is finished it can be rendered immediately.
     * If the page needs resolving then if the renderer supports
     * out of order rendering it can prepare the page. Otherwise
     * the page is added to a queue.
     * @param page the page to add to the model
     */
    public void addPage(PageViewport page) {
        super.addPage(page);

        // for links the renderer needs to prepare the page
        // it is more appropriate to do this after queued pages but
        // it will mean that the renderer has not prepared a page that
        // could be referenced
        boolean done = renderer.supportsOutOfOrder() && page.isResolved();
        if (done) {
            try {
                renderer.renderPage(page);
            } catch (Exception e) {
                // use error handler to handle this FOP or IO Exception
                e.printStackTrace();
            }
            page.clear();
        } else {
            preparePage(page);
        }


        // check prepared pages
        boolean cont = checkPreparedPages(page);

        if (cont) {
            renderExtensions(pendingExt);
            pendingExt.clear();
        }
    }

    /**
     * Check prepared pages
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
                try {
                    renderer.renderPage(p);
                } catch (Exception e) {
                    // use error handler to handle this FOP or IO Exception
                    e.printStackTrace();
                }
                p.clear();
                iter.remove();
            } else {
                // if keeping order then stop at first page not resolved
                if (!renderer.supportsOutOfOrder()) {
                    break;
                }
            }
        }
        return renderer.supportsOutOfOrder() || prepared.isEmpty();
    }

    /**
     * Prepare a page.
     * An unresolved page can be prepared if the renderer supports
     * it and the page will be rendered later.
     * @param page the page to prepare
     */
    protected void preparePage(PageViewport page) {
        if (renderer.supportsOutOfOrder()) {
            renderer.preparePage(page);
        }
        prepared.add(page);
    }

    /**
     * Add an extension to this model.
     * If handle immediately then send directly to the renderer.
     * The after page ones are handled after the next page is added.
     * End of document extensions are added to a list to be
     * handled at the end.
     * @param ext the extension
     * @param when when to render the extension
     */
    public void addExtension(TreeExt ext, int when) {
        switch(when) {
            case TreeExt.IMMEDIATELY:
                renderer.renderExtension(ext);
                break;
            case TreeExt.AFTER_PAGE:
                pendingExt.add(ext);
                break;
            case TreeExt.END_OF_DOC:
                endDocExt.add(ext);
                break;
        }
    }

    private void renderExtensions(List list) {
        for (int count = 0; count < list.size(); count++) {
            TreeExt ext = (TreeExt)list.get(count);
            renderer.renderExtension(ext);
        }
    }

    /**
     * End the document. Render any end document extensions.
     */
    public void endDocument() {
        // render any pages that had unresolved ids
        checkPreparedPages(null);

        renderExtensions(pendingExt);
        pendingExt.clear();

        renderExtensions(endDocExt);
    }
}

