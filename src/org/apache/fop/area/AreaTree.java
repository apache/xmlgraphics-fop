/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.util.ArrayList;


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

    public void createRenderPageModel(PageRenderListener listener) {

    }

    public static StorePagesModel createStorePagesModel() {
        return new StorePagesModel();
    }

    public void setTreeModel(AreaTreeModel m) {
        model = m;
    }

    public void startPageSequence(Area title) {
        model.startPageSequence(title);
    }

    public void addPage(PageViewport page) {
        model.addPage(page);
    }

    // this is the model for the area tree object
    public static abstract class AreaTreeModel {
        public abstract void startPageSequence(Area title);
        public abstract void addPage(PageViewport page);
    }

    // this class stores all the pages in the document
    // for interactive agents
    public static class StorePagesModel extends AreaTreeModel {
        ArrayList pageSequence = null;
        ArrayList titles = new ArrayList();
        ArrayList currSequence;

        public StorePagesModel() {}

        public void startPageSequence(Area title) {
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

    // this queues pages and will call the render listener
    // when the page is ready to be rendered
    // if the render supports out of order rendering
    // then a ready page is rendered immediately
    public static class RenderPagesModel extends StorePagesModel {
        public void startPageSequence(Area title) {}
        public void addPage(PageViewport page) {}
    }

    public static abstract class PageRenderListener {
        public abstract void renderPage(RenderPagesModel model,
                                        int pageseq, int count);
    }

}

