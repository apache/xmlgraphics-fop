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

package org.apache.fop.layoutmgr.inline;

import org.apache.fop.area.PageViewport;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.UnresolvedPageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.layoutmgr.LayoutContext;

/**
 * LayoutManager for the fo:page-number-citation formatting object
 */
public class PageNumberCitationLayoutManager extends AbstractPageNumberCitationLayoutManager {

    /**
     * Constructor
     *
     * @param node the formatting object that creates this area
     * TODO better retrieval of font info
     */
    public PageNumberCitationLayoutManager(PageNumberCitation node) {
        super(node);
    }

    /** {@inheritDoc} */
    public InlineArea get(LayoutContext context) {
        curArea = getPageNumberCitationInlineArea();
        return curArea;
    }

    /**
     * if id can be resolved then simply return a word, otherwise
     * return a resolvable area
     */
    private InlineArea getPageNumberCitationInlineArea() {
        PageViewport page = getPSLM().getFirstPVWithID(fobj.getRefId());
        TextArea text = null;
        if (page != null) {
            String str = page.getPageNumberString();
            // get page string from parent, build area
            text = new TextArea();
            int width = getStringWidth(str);
            text.addWord(str, 0);
            text.setIPD(width);
            resolved = true;
        } else {
            resolved = false;
            text = new UnresolvedPageNumber(fobj.getRefId(), font);
            String str = "MMM"; // reserve three spaces for page number
            int width = getStringWidth(str);
            text.setIPD(width);
        }
        updateTextAreaTraits(text);

        return text;
    }

}

