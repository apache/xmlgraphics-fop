/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

/**
 * Classes that implement this interface can be added to a PageSequenceMaster,
 * and are capable of looking up an appropriate PageMaster.
 */
public interface SubSequenceSpecifier {
    public String getNextPageMaster(int currentPageNumber,
                                    boolean thisIsFirstPage,
                                    boolean isEmptyPage);

    /**
     * Called before a new page sequence is rendered so subsequences can reset
     * any state they keep during the formatting process.
     */
    public void reset();

}

