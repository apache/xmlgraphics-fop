/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

import java.util.List;

import org.apache.fop.fo.pagination.PageSequence;

/**
 * Class for reporting back formatting results to the calling application.
 *
 * @author    Jeremias Maerki
 */
public class FormattingResults {

    private int pageCount = 0;
    private List pageSequences = null;

    /**
     * Constructor for the FormattingResults object
     */
    public FormattingResults() {
    }

    /**
     * Gets the number of pages rendered
     *
     * @return   The number of pages overall
     */
    public int getPageCount() {
        return this.pageCount;
    }

    /**
     * Gets the results for the individual page-sequences.
     *
     * @return   A List with PageSequenceResults objects
     */
    public List getPageSequences() {
        return this.pageSequences;
    }

    /**
     * Resets this object
     */
    public void reset() {
        this.pageCount = 0;
        if (this.pageSequences != null) {
            this.pageSequences.clear();
        }
    }

    /**
     * Description of the Method
     *
     * @param pageSequence  Description of Parameter
     */
    public void haveFormattedPageSequence(PageSequence pageSequence) {
        this.pageCount += pageSequence.getPageCount();
        if (this.pageSequences == null) {
            this.pageSequences = new java.util.ArrayList();
        }
        this.pageSequences.add(
                new PageSequenceResults(pageSequence.getProperty("id").getString(),
                                        pageSequence.getPageCount()));
    }
}

