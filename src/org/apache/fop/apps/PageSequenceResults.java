/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

/**
 * Class for reporting back formatting results to the calling application. This
 * particular class is used to report the results of a single page-sequence.
 *
 * @author    Jeremias Maerki
 */
public class PageSequenceResults {

    private String id;
    private int pageCount;

    /**
     * Constructor for the PageSequenceResults object
     *
     * @param id         ID of the page-sequence, if available
     * @param pageCount  The number of resulting pages
     */
    public PageSequenceResults(String id, int pageCount) {
        this.id = id;
        this.pageCount = pageCount;
    }

    /**
     * Gets the ID of the page-sequence if one was specified.
     *
     * @return   The ID
     */
    public String getID() {
        return this.id;
    }

    /**
     * Gets the number of pages that resulted by processing the page-sequence.
     *
     * @return   The number of pages generated
     */
    public int getPageCount() {
        return this.pageCount;
    }
}

