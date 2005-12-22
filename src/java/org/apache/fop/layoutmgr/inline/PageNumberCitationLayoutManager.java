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

package org.apache.fop.layoutmgr.inline;

import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Resolvable;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.UnresolvedPageNumber;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.fonts.Font;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;

/**
 * LayoutManager for the fo:page-number-citation formatting object
 */
public class PageNumberCitationLayoutManager extends LeafNodeLayoutManager {

    private PageNumberCitation fobj;
    private Font font;
    
    // whether the page referred to by the citation has been resolved yet
    private boolean resolved = false;
    
    /**
     * Constructor
     *
     * @param node the formatting object that creates this area
     * @todo better retrieval of font info
     */
    public PageNumberCitationLayoutManager(PageNumberCitation node) {
        super(node);
        fobj = node;
    }
    
    /** @see org.apache.fop.layoutmgr.LayoutManager#initialize */
    public void initialize() {
        font = fobj.getCommonFont().getFontState(fobj.getFOEventHandler().getFontInfo(), this);
        setCommonBorderPaddingBackground(fobj.getCommonBorderPaddingBackground());
    }

    /**
     * @see LeafNodeLayoutManager#makeAlignmentContext(LayoutContext)
     */
    protected AlignmentContext makeAlignmentContext(LayoutContext context) {
        return new AlignmentContext(
                font
                , fobj.getLineHeight().getOptimum(this).getLength().getValue(this)
                , fobj.getAlignmentAdjust()
                , fobj.getAlignmentBaseline()
                , fobj.getBaselineShift()
                , fobj.getDominantBaseline()
                , context.getAlignmentContext()
            );
    }

    /** @see org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager#get(LayoutContext) */
    public InlineArea get(LayoutContext context) {
        curArea = getPageNumberCitationInlineArea(parentLM);
        return curArea;
    }
    
    /**
     * @see org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager#addAreas(PositionIterator
     *                                                                      , LayoutContext) 
     */
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        super.addAreas(posIter, context);
        if (!resolved) {
            getPSLM().addUnresolvedArea(fobj.getRefId(), (Resolvable) curArea);
        }
    }
    
    /**
     * if id can be resolved then simply return a word, otherwise
     * return a resolvable area
     */
    private InlineArea getPageNumberCitationInlineArea(LayoutManager parentLM) {
        PageViewport page = getPSLM().getFirstPVWithID(fobj.getRefId());
        TextArea inline = null;
        if (page != null) {
            String str = page.getPageNumberString();
            // get page string from parent, build area
            TextArea text = new TextArea();
            inline = text;
            int width = getStringWidth(str);
            text.addWord(str, 0);
            inline.setIPD(width);
            
            resolved = true;
        } else {
            resolved = false;
            inline = new UnresolvedPageNumber(fobj.getRefId(), font);
            String str = "MMM"; // reserve three spaces for page number
            int width = getStringWidth(str);
            inline.setIPD(width);
            
        }
        TraitSetter.setProducerID(inline, fobj.getId());
        inline.setBPD(font.getAscender() - font.getDescender());
        inline.setBaselineOffset(font.getAscender());
        TraitSetter.addFontTraits(inline, font);
        inline.addTrait(Trait.COLOR, fobj.getColor());
        TraitSetter.addTextDecoration(inline, fobj.getTextDecoration());
        
        return inline;
    }
    
    /**
     * @param str string to be measured
     * @return width (in millipoints ??) of the string
     */
    private int getStringWidth(String str) {
        int width = 0;
        for (int count = 0; count < str.length(); count++) {
            width += font.getCharWidth(str.charAt(count));
        }
        return width;
    }

    /** @see org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager#addId() */
    protected void addId() {
        getPSLM().addIDToPage(fobj.getId());
    }
}

