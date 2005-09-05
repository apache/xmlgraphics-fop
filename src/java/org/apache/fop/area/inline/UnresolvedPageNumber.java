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
import org.apache.fop.fonts.Font;

import java.util.List;

/**
 * Unresolvable page number area.
 * This is a word area that resolves itself to a page number
 * from an id reference.
 */
public class UnresolvedPageNumber extends TextArea implements Resolvable {
    private boolean resolved = false;
    private String pageIDRef;
    private Font font;

    /**
     * Create a new unresolvable page number.
     *
     * @param id the id reference for resolving this
     * @param f  the font for formatting the page number
     */
    public UnresolvedPageNumber(String id, Font f) {
        pageIDRef = id;
        font = f;
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
            setTextArea(page.getPageNumberString());
            // update ipd
            updateIPD(getStringWidth(text));
            // set the Font object to null, as we don't need it any more
            font = null;
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

    /**
     * recursively apply the variation factor to all descendant areas
     * @param variationFactor the variation factor that must be applied to adjustment ratios
     * @param lineStretch     the total stretch of the line
     * @param lineShrink      the total shrink of the line
     * @return true if there is an UnresolvedArea descendant
     */
    public boolean applyVariationFactor(double variationFactor,
                                        int lineStretch, int lineShrink) {
        return true;
    }

    /**
     * @param str string to be measured
     * @return width of the string
     */
    private int getStringWidth(String str) {
        int width = 0;
        for (int count = 0; count < str.length(); count++) {
            width += font.getCharWidth(str.charAt(count));
        }
        return width;
    }
}
