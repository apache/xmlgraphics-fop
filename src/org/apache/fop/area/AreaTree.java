/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import org.apache.fop.render.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Area tree for formatting objects.
 *
 * Concepts:
 * The area tree is to be as small as possible. With minimal classes
 * and data to fully represent an area tree for formatting objects.
 * The area tree needs to be simple to render and follow the spec
 * closely.
 * This area tree has the concept of page sequences.
 * Where ever possible information is discarded or optimised to
 * keep memory use low. The data is also organised to make it
 * possible for renderers to minimise their output.
 * A page can be saved if not fully resolved and once rendered
 * a page contains only size and id reference information.
 * The area tree pages are organised in a model that depends on the
 * type of renderer.
 */
public class AreaTree {
    // allows for different models to deal with adding/rendering
    // in different situations
    private AreaTreeModel model;

    // hashmap of arraylists containing pages with id area
    private HashMap idLocations = new HashMap();
    // list of id's yet to be resolved and arraylists of pages
    private HashMap resolve = new HashMap();
    private ArrayList treeExtensions = new ArrayList();

    /**
     * Create a render pages area tree model.
     * @param rend the renderer that will be used
     * @return RenderPagesModel the new area tree model
     */
    public static RenderPagesModel createRenderPagesModel(Renderer rend) {
        return new RenderPagesModel(rend);
    }

    /**
     * Create a new store pages model.
     * @return StorePagesModel the new model
     */
    public static StorePagesModel createStorePagesModel() {
        return new StorePagesModel();
    }

    /**
     * Set the tree model to use for this area tree.
     * The different models can have different behaviour
     * when pages area added and other changes.
     * @param m the area tree model
     */
    public void setTreeModel(AreaTreeModel m) {
        model = m;
    }

    /**
     * Start a new page sequence.
     * This signals that a new page sequence has started in the document.
     * @param title the title of the new page sequence or null if no title
     */
    public void startPageSequence(Title title) {
        model.startPageSequence(title);
    }

    /**
     * Add a new page to the area tree.
     * @param page the page to add
     */
    public void addPage(PageViewport page) {
        model.addPage(page);
    }

    /**
     * Add an id reference pointing to a page viewport.
     * @param id the id of the reference
     * @param pv the page viewport that contains the id reference
     */
    public void addIDRef(String id, PageViewport pv) {
        List list = (List)idLocations.get(id);
        if (list == null) {
            list = new ArrayList();
            idLocations.put(id, list);
        }
        list.add(pv);

        HashSet todo = (HashSet)resolve.get(id);
        if (todo != null) {
            for (Iterator iter = todo.iterator(); iter.hasNext();) {
                Resolveable res = (Resolveable)iter.next();
                res.resolve(id, list);
            }
            resolve.remove(id);
        }
    }

    /**
     * Get the list of id references for an id.
     * @param id the id to lookup
     * @return the list of id references.
     */
    public List getIDReferences(String id) {
        return (List)idLocations.get(id);
    }

    /**
     * Add an unresolved object with a given id.
     * @param id the id reference that needs resolving
     * @param res the Resolveable object to resolve
     */
    public void addUnresolvedID(String id, Resolveable res) {
        HashSet todo = (HashSet)resolve.get(id);
        if (todo == null) {
            todo = new HashSet();
            resolve.put(id, todo);
        }
        todo.add(res);
    }

    /**
     * Add a tree extension.
     * This checks if the extension is resolveable and attempts
     * to resolve or add the resolveable ids for later resolution.
     * @param ext the tree extension to add.
     */
    public void addTreeExtension(TreeExt ext) {
        treeExtensions.add(ext);
        if (ext.isResolveable()) {
            Resolveable res = (Resolveable)ext;
            String[] ids = res.getIDs();
            for (int count = 0; count < ids.length; count++) {
                if (idLocations.containsKey(ids[count])) {
                    res.resolve(ids[count], (ArrayList)idLocations.get(ids[count]));
                } else {
                    HashSet todo = (HashSet)resolve.get(ids[count]);
                    if (todo == null) {
                        todo = new HashSet();
                        resolve.put(ids[count], todo);
                    }
                    todo.add(ext);
                }
            }
        } else {
            handleTreeExtension(ext, TreeExt.IMMEDIATELY);
        }
    }

    /**
     * Handle a tree extension.
     * This sends the extension to the model for handling.
     * @param ext the tree extension to handle
     * @param when when the extension should be handled by the model
     */
    public void handleTreeExtension(TreeExt ext, int when) {
        // queue tree extension according to the when
        model.addExtension(ext, when);
    }

    /**
     * Signal end of document.
     * This indicates that the document is complete and any unresolved
     * reference can be dealt with.
     */
    public void endDocument() {
        for (Iterator iter = resolve.keySet().iterator(); iter.hasNext();) {
            String id = (String)iter.next();
            HashSet list = (HashSet)resolve.get(id);
            for (Iterator resIter = list.iterator(); resIter.hasNext();) {
                Resolveable res = (Resolveable)resIter.next();
                if (!res.isResolved()) {
                    res.resolve(id, null);
                }
            }
        }
        model.endDocument();
    }

    /**
     * This is the model for the area tree object.
     * The model implementation can handle the page sequence,
     * page and extensions.
     */
    public abstract static class AreaTreeModel {
        /**
         * Start a page sequence on this model.
         * @param title the title of the new page sequence
         */
        public abstract void startPageSequence(Title title);

        /**
         * Add a page to this moel.
         * @param page the page to add to the model.
         */
        public abstract void addPage(PageViewport page);

        /**
         * Add an extension to this model.
         * @param ext the extension to add
         * @param when when the extension should be handled
         */
        public abstract void addExtension(TreeExt ext, int when);

        /**
         * Signal the end of the document for any processing.
         */
        public abstract void endDocument();
    }

    /**
     * This class stores all the pages in the document
     * for interactive agents.
     * The pages are stored and can be retrieved in any order.
     */
    public static class StorePagesModel extends AreaTreeModel {
        private ArrayList pageSequence = null;
        private ArrayList titles = new ArrayList();
        private ArrayList currSequence;
        private ArrayList extensions = new ArrayList();

        /**
         * Create a new store pages model
         */
        public StorePagesModel() {
        }

        /**
         * Start a new page sequence.
         * This creates a new list for the pages in the new page sequence.
         * @param title the title of the page sequence.
         */
        public void startPageSequence(Title title) {
            titles.add(title);
            if (pageSequence == null) {
                pageSequence = new ArrayList();
            }
            currSequence = new ArrayList();
            pageSequence.add(currSequence);
        }

        /**
         * Add a page.
         * @param page the page to add to the current page sequence
         */
        public void addPage(PageViewport page) {
            currSequence.add(page);
        }

        /**
         * Get the page sequence count.
         * @return the number of page sequences in the document.
         */
        public int getPageSequenceCount() {
            return pageSequence.size();
        }

        /**
         * Get the title for a page sequence.
         * @param count the page sequence count
         * @return the title of the page sequence
         */
        public Title getTitle(int count) {
            return (Title) titles.get(count);
        }

        /**
         * Get the page count.
         * @param seq the page sequence to count.
         * @return returns the number of pages in a page sequence
         */
        public int getPageCount(int seq) {
            ArrayList sequence = (ArrayList) pageSequence.get(seq);
            return sequence.size();
        }

        /**
         * Get the page for a position in the document.
         * @param seq the page sequence number
         * @param count the page count in the sequence
         * @return the PageViewport for the particular page
         */
        public PageViewport getPage(int seq, int count) {
            ArrayList sequence = (ArrayList) pageSequence.get(seq);
            return (PageViewport) sequence.get(count);
        }

        /**
         * Add an extension to the store page model.
         * The extension is stored so that it can be retrieved in the
         * appropriate position.
         * @param ext the extension to add
         * @param when when the extension should be handled
         */
        public void addExtension(TreeExt ext, int when) {
            int seq, page;
            switch(when) {
                case TreeExt.IMMEDIATELY:
                    seq = pageSequence == null ? 0 : pageSequence.size();
                    page = currSequence == null ? 0 : currSequence.size();
                break;
                case TreeExt.AFTER_PAGE:
                break;
                case TreeExt.END_OF_DOC:
                break;
            }
            extensions.add(ext);
        }

        /**
         * Get the list of extensions that apply at a particular
         * position in the document.
         * @param seq the page sequence number
         * @param count the page count in the sequence
         * @return the list of extensions
         */
        public List getExtensions(int seq, int count) {
            return null;
        }

        /**
         * Get the end of document extensions for this stroe pages model.
         * @return the list of end extensions
         */
        public List getEndExtensions() {
            return extensions;
        }

        /**
         * End document, do nothing.
         */
        public void endDocument() {
        }
    }

    /**
     * This uses the store pages model to store the pages
     * each page is either rendered if ready or prepared
     * for later rendering.
     * Once a page is rendered it is cleared to release the
     * contents but the PageViewport is retained. So even
     * though the pages are stored the contents are discarded.
     */
    public static class RenderPagesModel extends StorePagesModel {
        /**
         * The renderer that will render the pages.
         */
        protected Renderer renderer;
        /**
         * Pages that have been prepared but not rendered yet.
         */
        protected ArrayList prepared = new ArrayList();
        private ArrayList pendingExt = new ArrayList();
        private ArrayList endDocExt = new ArrayList();

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

        private void renderExtensions(ArrayList list) {
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

}

