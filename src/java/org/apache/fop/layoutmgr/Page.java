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

package org.apache.fop.layoutmgr;

import java.awt.geom.Rectangle2D;

import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.pagination.SimplePageMaster;

/**
 * This object is used by the layout engine to represent a page. It provides access to the
 * simple-page-master that was used as a template for this page and it provides access to the
 * PageViewport which is the top-level area tree element. This class helps to decouple the
 * FO tree from the area tree to make the latter easily serializable.
 */
public class Page {

    private SimplePageMaster spm;
    private PageViewport pageViewport;

    /**
     * Main constructor
     * @param spm the simple-page-master used for this page
     * @param pageNumber the page number (as an int)
     * @param pageNumberStr the page number (as a String)
     * @param blank true if this is a blank page
     */
    public Page(SimplePageMaster spm, int pageNumber, String pageNumberStr, boolean blank) {
        this.spm = spm;
        this.pageViewport = new PageViewport(spm, pageNumber, pageNumberStr, blank);
    }

    /**
     * Auxiliary constructor used when there's no SimplePageMaster.
     * @param viewArea the view area of the page
     * @param pageNumber the page number (as an int)
     * @param pageNumberStr the page number (as a String)
     * @param blank true if this is a blank page
     */
    public Page(Rectangle2D viewArea, int pageNumber, String pageNumberStr, boolean blank) {
        this.spm = null;
        this.pageViewport = new PageViewport(viewArea, pageNumber, pageNumberStr, null, blank);
    }

    /** @return the simple-page-master that created this page */
    public SimplePageMaster getSimplePageMaster() {
        return this.spm;
    }

    /** @return the page viewport representing this page in the area tree */
    public PageViewport getPageViewport() {
        return this.pageViewport;
    }

}
