/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 
package org.apache.fop.area.inline;

import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Resolvable;

import java.util.List;

/**
 * Unresolvable page number area.
 * This is a word area that resolves itself to a page number
 * from an id reference.
 */
public class UnresolvedPageNumber extends TextArea implements Resolvable {
    private boolean resolved = false;
    private String pageIDRef;

    /**
     * Create a new unresolvable page number.
     *
     * @param id the id reference for resolving this
     */
    public UnresolvedPageNumber(String id) {
        pageIDRef = id;
        text = "?";
    }

    /**
     * Get the id references for this area.
     *
     * @return the id reference for this unresolved page number
     */
    public String[] getIDRefs() {
        return new String[] {pageIDRef};
    }

    /**
     * Resolve the page number idref
     * This resolves the idref for this object by getting the page number
     * string from the first page in the list of pages that apply
     * for this ID.  The page number text is then set to the String value
     * of the page number.
     *
     * @param id an id whose PageViewports have been determined
     * @param pages the list of PageViewports associated with this ID
     */
    public void resolveIDRef(String id, List pages) {
        if (pageIDRef.equals(id) && pages != null) {
            resolved = true;
            PageViewport page = (PageViewport)pages.get(0);
            text = page.getPageNumberString();
            /**@todo Update IPD ??? */
        }
    }

    /**
     * Check if this is resolved.
     *
     * @return true when this has been resolved
     */
    public boolean isResolved() {
       return resolved;
    }
}
