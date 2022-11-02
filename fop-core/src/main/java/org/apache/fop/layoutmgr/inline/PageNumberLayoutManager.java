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

import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.ResolvedPageNumber;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for the fo:page-number formatting object
 */
public class PageNumberLayoutManager extends LeafNodeLayoutManager {

    private PageNumber fobj;
    private Font font;

    /**
     * Constructor
     *
     * @param node the fo:page-number formatting object that creates the area
     * TODO better null checking of node, font
     */
    public PageNumberLayoutManager(PageNumber node) {
        super(node);
        fobj = node;
    }

    /** {@inheritDoc} */
    public void initialize() {
        FontInfo fi = fobj.getFOEventHandler().getFontInfo();
        FontTriplet[] fontkeys = fobj.getCommonFont().getFontState(fi);
        font = fi.getFontInstance(fontkeys[0], fobj.getCommonFont().fontSize.getValue(this));
        setCommonBorderPaddingBackground(fobj.getCommonBorderPaddingBackground());
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public InlineArea get(LayoutContext context) {
        // get page string from parent, build area
        ResolvedPageNumber pn = new ResolvedPageNumber();
        String str = getCurrentPV().getPageNumberString();
        int width = getStringWidth(str);
        int level = getBidiLevel();
        pn.addWord(str, 0, level);
        pn.setBidiLevel(level);
        pn.setIPD(width);
        pn.setBPD(font.getAscender() - font.getDescender());
        pn.setBaselineOffset(font.getAscender());
        TraitSetter.addFontTraits(pn, font);
        pn.addTrait(Trait.COLOR, fobj.getColor());
        TraitSetter.addTextDecoration(pn, fobj.getTextDecoration());
        return pn;
    }

    /** {@inheritDoc} */
    protected InlineArea getEffectiveArea(LayoutContext layoutContext) {
        ResolvedPageNumber baseArea = (ResolvedPageNumber)curArea;
        //TODO Maybe replace that with a clone() call or better, a copy constructor
        //TODO or even better: delay area creation until addAreas() stage
        //ResolvedPageNumber is cloned because the LM is reused in static areas and the area can't be.
        ResolvedPageNumber pn = new ResolvedPageNumber();
        TraitSetter.setProducerID(pn, fobj.getId());
        pn.setIPD(baseArea.getIPD());
        pn.setBPD(baseArea.getBPD());
        pn.setBlockProgressionOffset(baseArea.getBlockProgressionOffset());
        pn.setBaselineOffset(baseArea.getBaselineOffset());
        pn.addTrait(Trait.COLOR, fobj.getColor()); //only to initialize the trait map
        pn.getTraits().putAll(baseArea.getTraits());
        if (!layoutContext.treatAsArtifact()) {
            TraitSetter.addStructureTreeElement(pn, fobj.getStructureTreeElement());
        }
        updateContent(pn);
        return pn;
    }

    private void updateContent(ResolvedPageNumber pn) {
        // get the page number of the page actually being built
        pn.removeText();
        pn.addWord(getCurrentPV().getPageNumberString(), 0, getBidiLevel());
        // update the ipd of the area
        pn.handleIPDVariation(getStringWidth(pn.getText()) - pn.getIPD());
        // update the width stored in the AreaInfo object
        areaInfo.ipdArea = MinOptMax.getInstance(pn.getIPD());
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

    protected int getBidiLevel() {
        return fobj.getBidiLevel();
    }

}

