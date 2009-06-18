/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.List;

/**
 * Represents a page sequence in the area tree.
 */
public class PageSequence {

    private List pages = new java.util.ArrayList();
    private LineArea title;
    
    /**
     * Main constructor
     * @param title the title for the page-sequence, may be null
     */
    public PageSequence(LineArea title) {
        this.title = title;
    }
    
    /**
     * @return the title of the page sequence in form of a line area, or null if there's no title
     */
    public LineArea getTitle() {
        return this.title;
    }
    
    /**
     * Adds a new page to the page sequence
     * @param page the page to be added
     */
    public void addPage(PageViewport page) {
        this.pages.add(page);
    }
    
    /**
     * @return the number of pages currently in this page sequence
     */
    public int getPageCount() {
        return this.pages.size();
    }

    /**
     * Returns the page at the given index.
     * @param idx the index of the requested page
     * @return the requested page or null if it was not found
     */
    public PageViewport getPage(int idx) {
        return (PageViewport)this.pages.get(idx);
    }
    
    /**
     * Indicates whether a page is the first in this page sequence.
     * @param page the page to be inspected
     * @return true if the page is the first in this page sequence, false otherwise
     */
    public boolean isFirstPage(PageViewport page) {
        return page.equals(getPage(0));
    }
}
