/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.Trait;
import org.apache.fop.fonts.Font;

/**
 * LayoutManager for the fo:page-number formatting object
 */
public class PageNumberLayoutManager extends LeafNodeLayoutManager {
    private PageNumber fobj;
    Font font = null;
    
    /**
     * Constructor
     *
     * @param node the fo:page-number formatting object that creates the area
     * @todo better null checking of node, font
     */
    public PageNumberLayoutManager(PageNumber node) {
        super(node);
        fobj = node;
        font = fobj.getCommonFont().getFontState(fobj.getFOEventHandler().getFontInfo());
    }

    public InlineArea get(LayoutContext context) {
        // get page string from parent, build area
        TextArea inline = new TextArea();
        String str = getCurrentPV().getPageNumberString();
        int width = 0;
        for (int count = 0; count < str.length(); count++) {
            width += font.getCharWidth(str.charAt(count));
        }
        inline.setTextArea(str);
        inline.setIPD(width);
        inline.setBPD(font.getAscender() - font.getDescender());
        inline.setOffset(font.getAscender());
        inline.addTrait(Trait.FONT_NAME, font.getFontName());
        inline.addTrait(Trait.FONT_SIZE,
                        new Integer(font.getFontSize()));

        TraitSetter.addTextDecoration(inline, fobj.getTextDecoration());

        return inline;
    }
    
    protected void offsetArea(LayoutContext context) {
        curArea.setOffset(context.getBaseline());
    }
    
    protected void addId() {
        getPSLM().addIDToPage(fobj.getId());
    }
}

