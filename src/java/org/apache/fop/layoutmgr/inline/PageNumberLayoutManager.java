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

import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.Trait;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;

import org.axsl.fontR.Font;
import org.axsl.fontR.FontConsumer;
import org.axsl.fontR.FontUse;

/**
 * LayoutManager for the fo:page-number formatting object
 */
public class PageNumberLayoutManager extends LeafNodeLayoutManager {
    
    private PageNumber fobj;
    private FontConsumer fontConsumer;
    private FontUse fontUse;
    private int fontSize;
        
    /**
     * Constructor
     *
     * @param node the fo:page-number formatting object that creates the area
     * @todo better null checking of node, font
     */
    public PageNumberLayoutManager(PageNumber node) {
        super(node);
        fobj = node;
    }
    
    /** @see org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager#get(LayoutContext) */
    public void initialize() {
        fontConsumer = fobj.getFOEventHandler().getFontConsumer();
        CommonFont commonFont = fobj.getCommonFont();
        fontUse = commonFont.getFontState(fontConsumer, this);
        fontSize = commonFont.getFontSize(this);
        setCommonBorderPaddingBackground(fobj.getCommonBorderPaddingBackground());
    }

    /**
     * @see org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager
     *                                                      #makeAlignmentContext(LayoutContext)
     */
    protected AlignmentContext makeAlignmentContext(LayoutContext context) {
        return new AlignmentContext(
                fontUse.getFont()
                , fontSize
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
        // get page string from parent, build area
        TextArea text = new TextArea();
        String str = getCurrentPV().getPageNumberString();
        int width = getStringWidth(str);
        fontUse.registerCharsUsed(str);
        text.addWord(str, 0);
        text.setIPD(width);
        Font f = fontUse.getFont();
        text.setBPD(f.getAscender(fontSize) - f.getDescender(fontSize));
        text.setBaselineOffset(f.getAscender(fontSize));
        TraitSetter.addFontTraits(text, fontUse, fontSize);
        text.addTrait(Trait.COLOR, fobj.getColor());        

        TraitSetter.addTextDecoration(text, fobj.getTextDecoration());

        return text;
    }
    
    /** @see org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager#getEffectiveArea() */
    protected InlineArea getEffectiveArea() {
        TextArea baseArea = (TextArea)curArea;
        //TODO Maybe replace that with a clone() call or better, a copy constructor
        //TODO or even better: delay area creation until addAreas() stage
        //TextArea is cloned because the LM is reused in static areas and the area can't be.
        TextArea ta = new TextArea();
        TraitSetter.setProducerID(ta, fobj.getId());
        ta.setIPD(baseArea.getIPD());
        ta.setBPD(baseArea.getBPD());
        ta.setOffset(baseArea.getOffset());
        ta.setBaselineOffset(baseArea.getBaselineOffset());
        ta.addTrait(Trait.COLOR, fobj.getColor()); //only to initialize the trait map
        ta.getTraits().putAll(baseArea.getTraits());
        updateContent(ta);
        return ta;
    }
    
    private void updateContent(TextArea area) {
        // get the page number of the page actually being built
        area.removeText();
        area.addWord(getCurrentPV().getPageNumberString(), 0);
        // update the ipd of the area
        area.updateIPD(getStringWidth(area.getText()));
        // update the width stored in the AreaInfo object
        areaInfo.ipdArea = new MinOptMax(area.getIPD());
    }

    /**
     * @param str string to be measured
     * @return width of the string
     */
    private int getStringWidth(String str) {
        int width = 0;
        Font f = fontUse.getFont();
        for (int count = 0; count < str.length(); count++) {
            width += f.width(str.charAt(count), fontSize);
        }
        return width;
    }
    
    /** @see org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager#addId() */
    protected void addId() {
        getPSLM().addIDToPage(fobj.getId());
    }
}

