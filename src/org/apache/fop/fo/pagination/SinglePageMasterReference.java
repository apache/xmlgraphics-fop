/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.fo.pagination;

import org.apache.fop.fo.FONode;

/**
 * A single-page-master-reference formatting object.
 * This is a reference for a single page. It returns the
 * master name only once until reset.
 */
public class SinglePageMasterReference extends PageMasterReference
    implements SubSequenceSpecifier {

    private static final int FIRST = 0;
    private static final int DONE = 1;

    private int state;

    public SinglePageMasterReference(FONode parent) {
        super(parent);
        this.state = FIRST;
    }

    public String getNextPageMaster(int currentPageNumber,
                                    boolean thisIsFirstPage,
                                    boolean isEmptyPage) {
        if (this.state == FIRST) {
            this.state = DONE;
            return getMasterName();
        } else {
            return null;
        }
    }

    public void reset() {
        this.state = FIRST;
    }
}

