/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

// this is the level that creates the page
// the page (reference area) is then rendered inside the page object
public class PageViewport {
    Page page;
    Rectangle2D viewArea;

    public PageViewport(Page p) {
        page = p;
    }

    // this list is only used when the page is discarded
    // the information is kept for future reference
    ArrayList idReferences = null;

    // a viewport area for page and reference areas
    public Page getPage() {
        return page;
    }
}
