/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.Trait;

import java.util.List;

public class UnresolvedPageNumber extends Word implements Resolveable {
    private boolean resolved = false;
    private String pageRefId;

    public UnresolvedPageNumber(String id) {
        pageRefId = id;
        word = "?";
    }

    public String[] getIDs() {
        return new String[] {pageRefId};
    }

    public void resolve(String id, List pages) {
        resolved = true;
        if(pages != null) {
            PageViewport page = (PageViewport)pages.get(0);
            String str = page.getPageNumber();
            word = str;

            // update ipd
            String name = (String) getTrait(Trait.FONT_NAME);
            int size = ((Integer) getTrait(Trait.FONT_SIZE)).intValue();
            //FontMetric metrics = fontInfo.getMetricsFor(name);
            //FontState fs = new FontState(name, metrics, size);
        }
    }

    public boolean isResolved() {
       return resolved;
    }
}
