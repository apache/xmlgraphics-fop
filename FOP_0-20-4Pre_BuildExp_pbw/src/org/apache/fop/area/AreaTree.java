/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import org.apache.fop.render.Renderer;

import java.util.ArrayList;
import java.util.HashMap;

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
                        todo.add(ext);
                        resolve.put(ids[count], todo);
                    } else {
                        todo.add(ext);
                    }
                }
            }
        }
    }

    // this is the model for the area tree object
    public static abstract class AreaTreeModel {
        public abstract void startPageSequence(Title title);
        public abstract void addPage(PageViewport page);
    }

    // this class stores all the pages in the document
    // for interactive agents
    public static class StorePagesModel extends AreaTreeModel {
        ArrayList pageSequence = null;
        ArrayList titles = new ArrayList();
        ArrayList currSequence;

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
    }

    // this uses the store pages model to store the pages
    // each page is either rendered if ready or prepared
    // for later rendering
    public static class RenderPagesModel extends StorePagesModel {
        Renderer renderer;
        ArrayList prepared = new ArrayList();

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
            // else prepare
            //renderer.preparePage(page);
            prepared.add(page);
        }
    }

}

