/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.awt.geom.Rectangle2D;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

// this is the level that creates the page
// the page (reference area) is then rendered inside the page object
public class PageViewport implements Cloneable {
    Page page;
    Rectangle2D viewArea;
    boolean clip = false;

    public PageViewport(Page p, Rectangle2D bounds) {
        page = p;
        viewArea = bounds;
    }

    // this list is only used when the page is discarded
    // the information is kept for future reference
    ArrayList idReferences = null;

    // this keeps a list of currently unresolved areas or extensions
    // once the thing is resolved it is removed
    // when this is empty the page can be rendered
    ArrayList unresolved = null;

    public void setClip(boolean c) {
        clip = c;
    }

    public Rectangle2D getViewArea() {
        return viewArea;
    }

    // a viewport area for page and reference areas
    public Page getPage() {
        return page;
    }

    public void savePage(ObjectOutputStream out) throws Exception {
        out.writeObject(page);
        page = null;
    }

    public void loadPage(ObjectInputStream in) throws Exception {
        page = (Page) in.readObject();
    }

    public Object clone() {
        Page p = (Page)page.clone();
        PageViewport ret = new PageViewport(p, (Rectangle2D)viewArea.clone());
        return ret;
    }

    /**
     * Clear the page contents to save memory.
     * This object is kept for the life of the area tree since
     * it holds id information and is used as a key.
     */
    public void clear() {
        page = null;
    }
}
