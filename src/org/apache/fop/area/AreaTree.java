/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import org.apache.fop.render.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
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
    AreaTreeModel model;

    // hashmap of arraylists containing pages with id area
    HashMap idLocations = new HashMap();
    // list of id's yet to be resolved and arraylists of pages
    HashMap resolve = new HashMap();
    ArrayList treeExtensions = new ArrayList();

    public RenderPagesModel createRenderPagesModel(Renderer rend) {
        return new RenderPagesModel(rend);
    }

    public static StorePagesModel createStorePagesModel() {
        return new StorePagesModel();
    }

    public void setTreeModel(AreaTreeModel m) {
        model = m;
    }

    public void startPageSequence(Title title) {
        model.startPageSequence(title);
    }

    public void addPage(PageViewport page) {
        model.addPage(page);
    }

    public void addIDRef(String id, PageViewport pv) {
        ArrayList list = (ArrayList)idLocations.get(id);
        if(list == null) {
            list = new ArrayList();
            idLocations.put(id, list);
        }
        list.add(pv);

        ArrayList todo = (ArrayList)resolve.get(id);
        if(todo != null) {
            for(int count = 0; count < todo.size(); count++) {
                Resolveable res = (Resolveable)todo.get(count);
                res.resolve(id, list);
            }
            resolve.remove(id);
        }
    }

    public void addUnresolvedID(String id, Resolveable res) {
        ArrayList todo = (ArrayList)resolve.get(id);
        if(todo == null) {
            todo = new ArrayList();
            resolve.put(id, todo);
        }
        todo.add(res);
    }

    public void addTreeExtension(TreeExt ext) {
        treeExtensions.add(ext);
        if(ext.isResolveable()) {
            Resolveable res = (Resolveable)ext;
            String[] ids = res.getIDs();
            for(int count = 0; count < ids.length; count++) {
                if(idLocations.containsKey(ids[count])) {
                    res.resolve(ids[count], (ArrayList)idLocations.get(ids[count]));
                } else {
                    ArrayList todo = (ArrayList)resolve.get(ids[count]);
                    if(todo == null) {
                        todo = new ArrayList();
                        resolve.put(ids[count], todo);
                    }
                    todo.add(ext);
                }
            }
        }
    }

    public void handleTreeExtension(TreeExt ext, int when) {
        // queue tree extension according to the when
        model.addExtension(ext, when);
    }

    public void endDocument() {
        for(Iterator iter = resolve.keySet().iterator(); iter.hasNext(); ) {
            String id = (String)iter.next();
            ArrayList list = (ArrayList)resolve.get(id);
            for(int count = 0; count < list.size(); count++) {
                Resolveable res = (Resolveable)list.get(count);
                if(!res.isResolved()) {
                    res.resolve(id, null);
                }
            }
        }
        model.endDocument();
    }

    // this is the model for the area tree object
    public static abstract class AreaTreeModel {
        public abstract void startPageSequence(Title title);
        public abstract void addPage(PageViewport page);
        public abstract void addExtension(TreeExt ext, int when);
        public abstract void endDocument();
    }

    // this class stores all the pages in the document
    // for interactive agents
    public static class StorePagesModel extends AreaTreeModel {
        ArrayList pageSequence = null;
        ArrayList titles = new ArrayList();
        ArrayList currSequence;
        ArrayList extensions = new ArrayList();

        public StorePagesModel() {}

        public void startPageSequence(Title title) {
            titles.add(title);
            if (pageSequence == null) {
                pageSequence = new ArrayList();
            }
            currSequence = new ArrayList();
            pageSequence.add(currSequence);
        }

        public void addPage(PageViewport page) {
            currSequence.add(page);
        }

        public int getPageSequenceCount() {
            return pageSequence.size();
        }

        public Title getTitle(int count) {
            return (Title) titles.get(count);
        }

        public int getPageCount(int seq) {
            ArrayList sequence = (ArrayList) pageSequence.get(seq);
            return sequence.size();
        }

        public PageViewport getPage(int seq, int count) {
            ArrayList sequence = (ArrayList) pageSequence.get(seq);
            return (PageViewport) sequence.get(count);
        }

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

        public List getExtensions(int seq, int count) {
            return null;
        }

        public List getEndExtensions() {
            return extensions;
        }

        public void endDocument() {
        }
    }

    // this uses the store pages model to store the pages
    // each page is either rendered if ready or prepared
    // for later rendering
    public static class RenderPagesModel extends StorePagesModel {
        Renderer renderer;
        ArrayList prepared = new ArrayList();
        ArrayList pendingExt = new ArrayList();
        ArrayList endDocExt = new ArrayList();

        public RenderPagesModel(Renderer rend) {
            renderer = rend;
        }

        public void startPageSequence(Title title) {
            super.startPageSequence(title);
            renderer.startPageSequence(title);
        }

        public void addPage(PageViewport page) {
            super.addPage(page);
            // if page finished
            try {
                renderer.renderPage(page);
            } catch(Exception e) {
                // use error handler to handle this FOP or IO Exception
            }
            page.clear();

            renderExtensions(pendingExt);
            pendingExt.clear();

            // else prepare
            //renderer.preparePage(page);
            prepared.add(page);
        }

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
            for(int count = 0; count < list.size(); count++) {
                TreeExt ext = (TreeExt)list.get(count);
                renderer.renderExtension(ext);
            }
        }

        public void endDocument() {
            renderExtensions(endDocExt);
        }
    }

}

