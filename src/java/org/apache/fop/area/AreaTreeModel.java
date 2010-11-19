/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.area;

// Java
import java.util.List;

import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the model for the area tree object.
 * The model implementation can handle the page sequence,
 * page and off-document items.
 * The methods to access the page viewports can only
 * assume the PageViewport is valid as it remains for
 * the life of the area tree model.
 */
public class AreaTreeModel {
    private List/*<PageSequence>*/ pageSequenceList = null;
    private int currentPageSequenceIndex = -1;
    /** the current page sequence */
    protected PageSequence currentPageSequence;
//    private List offDocumentItems = new java.util.ArrayList();
    /** logger instance */
    protected static final Log log = LogFactory.getLog(AreaTreeModel.class);

    /**
     * Create a new store pages model
     */
    public AreaTreeModel() {
        pageSequenceList = new java.util.ArrayList/*<PageSequence>*/();
    }

    /**
     * Start a page sequence on this model.
     * @param pageSequence the page sequence about to start
     */
    public void startPageSequence(PageSequence pageSequence) {
        if (pageSequence == null) {
            throw new NullPointerException("pageSequence must not be null");
        }
        this.currentPageSequence = pageSequence;
        pageSequenceList.add(currentPageSequence);
        currentPageSequenceIndex = pageSequenceList.size() - 1;
    }

    /**
     * Add a page to this model.
     * @param page the page to add to the model.
     */
    public void addPage(PageViewport page) {
        currentPageSequence.addPage(page);
        int pageIndex = 0;
        for (int i = 0; i < currentPageSequenceIndex; i++) {
            pageIndex += ((PageSequence)pageSequenceList.get(i)).getPageCount();
        }
        pageIndex += currentPageSequence.getPageCount() - 1;
        page.setPageIndex(pageIndex);
        page.setPageSequence(currentPageSequence);
    }

    /**
     * Handle an OffDocumentItem
     * @param ext the extension to handle
     */
    public void handleOffDocumentItem(OffDocumentItem ext) { };

    /**
     * Signal the end of the document for any processing.
     * @throws SAXException if a problem was encountered.
     */
    public void endDocument() throws SAXException { };

    /**
     * Returns the currently active page-sequence.
     * @return the currently active page-sequence
     */
    public PageSequence getCurrentPageSequence() {
        return this.currentPageSequence;
    }

    /**
     * Get the page sequence count.
     * @return the number of page sequences in the document.
     */
    public int getPageSequenceCount() {
        return pageSequenceList.size();
    }

    /**
     * Get the page count.
     * @param seq the page sequence to count.
     * @return returns the number of pages in a page sequence
     */
    public int getPageCount(int seq) {
        PageSequence sequence = (PageSequence)pageSequenceList.get(seq - 1);
        return sequence.getPageCount();
    }

    /**
     * Get the page for a position in the document.
     * @param seq the page sequence number
     * @param count the page count in the sequence
     * @return the PageViewport for the particular page
     */
    public PageViewport getPage(int seq, int count) {
        PageSequence sequence = (PageSequence)pageSequenceList.get(seq - 1);
        return sequence.getPage(count);
    }
}
