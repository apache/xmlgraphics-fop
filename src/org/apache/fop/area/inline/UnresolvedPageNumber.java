/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.Trait;

import java.util.List;

/**
 * Unresolveable page number area.
 * This is a word area that resolves itself to a page number
 * from an id reference.
 */
public class UnresolvedPageNumber extends Word implements Resolveable {
    private boolean resolved = false;
    private String pageRefId;

    /**
     * Create a new unresolveable page number.
     *
     * @param id the id reference for resolving this
     */
    public UnresolvedPageNumber(String id) {
        pageRefId = id;
        word = "?";
    }

    /**
     * Get the id references for this area.
     *
     * @return the id reference for this unresolved page number
     */
    public String[] getIDs() {
        return new String[] {pageRefId};
    }

    /**
     * Resolve this page number reference.
     * This resolves the reference by getting the page number
     * string from the first page in the list of pages that apply
     * for the id reference. The word string is then set to the
     * page number string.
     *
     * @param id the id reference being resolved
     * @param pages the list of pages for the id reference
     */
    public void resolve(String id, List pages) {
        resolved = true;
        if (pages != null) {
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

    /**
     * Check if this is resolved.
     *
     * @return true when this has been resolved
     */
    public boolean isResolved() {
       return resolved;
    }
}
