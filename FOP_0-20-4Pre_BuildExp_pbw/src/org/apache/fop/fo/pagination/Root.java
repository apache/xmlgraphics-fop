/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;

/**
 * The fo:root formatting object. Contains page masters, page-sequences.
 */
public class Root extends FObj {

    LayoutMasterSet layoutMasterSet;
    ArrayList pageSequences;

    /**
     * keeps count of page number from over PageSequence instances
     */
    private int runningPageNumberCounter = 0;

    public Root(FONode parent) {
        super(parent);

        // this.properties.get("media-usage");

        pageSequences = new ArrayList();

        if (parent != null) {
            //throw new FOPException("root must be root element");
        }
    }

    protected int getRunningPageNumberCounter() {
        return this.runningPageNumberCounter;
    }

    protected void setRunningPageNumberCounter(int count) {
        this.runningPageNumberCounter = count;
    }

    public int getPageSequenceCount() {
        return pageSequences.size();
    }

    /**
     * Some properties, such as 'force-page-count', require a
     * page-sequence to know about some properties of the next.
     * @returns succeeding PageSequence; null if none
     */
    public PageSequence getSucceedingPageSequence(PageSequence current) {
        int currentIndex = pageSequences.indexOf(current);
        if (currentIndex == -1)
            return null;
        if (currentIndex < (pageSequences.size() - 1)) {
            return (PageSequence)pageSequences.get(currentIndex + 1);
        } else {
            return null;
        }
    }

    public LayoutMasterSet getLayoutMasterSet() {
        return this.layoutMasterSet;
    }

    public void setLayoutMasterSet(LayoutMasterSet layoutMasterSet) {
        this.layoutMasterSet = layoutMasterSet;
    }
}
