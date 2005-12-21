/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.fop.layoutmgr;

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
    
    public Page(SimplePageMaster spm, String pageStr, boolean blank) {
        this.spm = spm;
        this.pageViewport = new PageViewport(spm, pageStr, blank);
    }
    
    public SimplePageMaster getSimplePageMaster() {
        return this.spm;
    }
    
    public PageViewport getPageViewport() {
        return this.pageViewport;
    }
    
}
