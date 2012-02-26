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
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.UnresolvedPageNumber;
import org.apache.fop.fo.flow.AbstractPageNumberCitation;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;

/**
 * LayoutManager for the fo:page-number-citation(-last) formatting object
 */
public abstract class AbstractPageNumberCitationLayoutManager extends LeafNodeLayoutManager {

    /** The page number citation object */
    protected AbstractPageNumberCitation fobj;
    /** Font for the page-number-citation */
    protected Font font;

    /** Indicates whether the page referred to by the citation has been resolved yet */
    protected boolean resolved = false;

    /**
     * Constructor
     *
     * @param node the formatting object that creates this area
     * TODO better retrieval of font info
     */
    public AbstractPageNumberCitationLayoutManager(AbstractPageNumberCitation node) {
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

    /**
     * {@inheritDoc}
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

    /** {@inheritDoc} */
    public abstract InlineArea get(LayoutContext context);

    /**
     * {@inheritDoc}
     *                                                                      , LayoutContext)
     */
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        super.addAreas(posIter, context);
        if (!resolved) {
            getPSLM().addUnresolvedArea(fobj.getRefId(), (Resolvable) curArea);
        }
    }

    /**
     * Updates the traits for the generated text area.
     * @param text the text area
     */
    protected void updateTextAreaTraits(TextArea text) {
        TraitSetter.setProducerID(text, fobj.getId());
        text.setBPD(font.getAscender() - font.getDescender());
        text.setBaselineOffset(font.getAscender());
        TraitSetter.addFontTraits(text, font);
        text.addTrait(Trait.COLOR, fobj.getColor());
        TraitSetter.addStructureTreeElement(text, fobj.getStructureTreeElement());
        TraitSetter.addTextDecoration(text, fobj.getTextDecoration());
    }

    /**
     * @param str string to be measured
     * @return width (in millipoints ??) of the string
     */
    protected int getStringWidth(String str) {
        int width = 0;
        for (int count = 0; count < str.length(); count++) {
            width += font.getCharWidth(str.charAt(count));
        }
        return width;
    }

    /**
     * @return bidi level governing abstract page number citation
     */
    protected int getBidiLevel() {
        return fobj.getBidiLevel();
    }

}

