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
import org.apache.fop.area.Resolvable;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.UnresolvedPageNumber;
import org.apache.fop.fo.flow.PageNumberCitationLast;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;

/**
 * LayoutManager for the fo:page-number-citation-last formatting object
 */
public class PageNumberCitationLastLayoutManager extends AbstractPageNumberCitationLayoutManager {

    /**
     * Constructor
     *
     * @param node the formatting object that creates this area
     * TODO better retrieval of font info
     */
    public PageNumberCitationLastLayoutManager(PageNumberCitationLast node) {
        super(node);
        fobj = node;
    }

    /** {@inheritDoc} */
    public InlineArea get(LayoutContext context) {
        curArea = getPageNumberCitationLastInlineArea(parentLayoutManager);
        return curArea;
    }

    /**
     * if id can be resolved then simply return a word, otherwise
     * return a resolvable area
     */
    private InlineArea getPageNumberCitationLastInlineArea(LayoutManager parentLM) {
        TextArea text = null;
        resolved = false;
        if (!getPSLM().associateLayoutManagerID(fobj.getRefId())) {
            text = new UnresolvedPageNumber(fobj.getRefId(), font, UnresolvedPageNumber.LAST);
            getPSLM().addUnresolvedArea(fobj.getRefId(), (Resolvable)text);
            String str = "MMM"; // reserve three spaces for page number
            int width = getStringWidth(str);
            text.setIPD(width);
        } else {
            PageViewport page = getPSLM().getLastPVWithID(fobj.getRefId());
            String str = page.getPageNumberString();
            // get page string from parent, build area
            text = new TextArea();
            int width = getStringWidth(str);
            text.addWord(str, 0);
            text.setIPD(width);

            resolved = true;
        }

        updateTextAreaTraits(text);

        return text;
    }
}
