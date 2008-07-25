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

import java.util.List;

/**
 * Represents a page sequence in the area tree.
 */
public class PageSequence {

    private List pages = new java.util.ArrayList();
    private LineArea title;
    private String language;
    private String country;

    /**
     * Main constructor
     * @param title the title for the page-sequence, may be null
     */
    public PageSequence(LineArea title) {
        setTitle(title);
    }

    /**
     * @return the title of the page sequence in form of a line area, or null if there's no title
     */
    public LineArea getTitle() {
        return this.title;
    }

    /**
     * Sets the page sequence's title.
     * @param title the title
     */
    public void setTitle(LineArea title) {
        this.title = title;
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

    /**
     * Returns the language of the page-sequence.
     * @return the language (the value of the language property, "none" is mapped to null)
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Sets the language that applies to this page-sequence.
     * @param language the language to set ("none" is mapped to null)
     */
    public void setLanguage(String language) {
        if ("none".equals(language)) {
            this.language = null;
        } else {
            this.language = language;
        }
    }

    /**
     * Returns the country of the page-sequence.
     * @return the country (the value of the country property, "none" is mapped to null)
     */
    public String getCountry() {
        return this.country;
    }

    /**
     * Sets the country that applies to this page-sequence.
     * @param country the country to set ("none" is mapped to null)
     */
    public void setCountry(String country) {
        if ("none".equals(country)) {
            this.country = null;
        } else {
            this.country = country;
        }
    }

}
