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

package org.apache.fop.svg;

import java.util.List;

import org.apache.fop.pdf.PDFPage;

/**
 * Context class which holds state information which should remain in sync over multiple instances
 * of PDFDocumentGraphics2D.
 */
public class PDFContext {

    private PDFPage currentPage;
    private List fontList;

    /** number of pages generated */
    private int pagecount;
    
    /**
     * Sets the font list as creates by the FontSetup class.
     * @param list the font list
     */
    public void setFontList(List list) {
        this.fontList = list;
    }

    /** @return the font list */
    public List getFontList() {
        return this.fontList;
    }

    /** @return true if a page is set up for painting. */
    public boolean isPagePending() {
        return this.currentPage != null;
    }

    /**
     * After this call, there's no current page.
     */
    public void clearCurrentPage() {
        currentPage = null;
    }

    /** @return the current page or null if there is none */
    public PDFPage getCurrentPage() {
        return this.currentPage;
    }

    /**
     * Sets the current page
     * @param page the page
     */
    public void setCurrentPage(PDFPage page) {
        this.currentPage = page;
    }

    /** Notifies the context to increase the page count. */
    public void increasePageCount() {
        this.pagecount++;
    }

}
