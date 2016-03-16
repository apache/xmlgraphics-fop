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
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.UnresolvedPageNumber;
import org.apache.fop.fo.flow.AbstractPageNumberCitation;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for the fo:page-number-citation(-last) formatting object
 */
public abstract class AbstractPageNumberCitationLayoutManager extends LeafNodeLayoutManager {

    /** The page number citation object */
    protected AbstractPageNumberCitation citation;

    /** Font for the page-number-citation */
    protected Font font;

    /** Indicates whether the page referred to by the citation has been resolved yet */
    private boolean resolved;

    private String citationString;

    /**
     * Constructor
     *
     * @param node the formatting object that creates this area
     * TODO better retrieval of font info
     */
    public AbstractPageNumberCitationLayoutManager(AbstractPageNumberCitation node) {
        super(node);
        citation = node;
    }

    /** {@inheritDoc} */
    public void initialize() {
        FontInfo fi = citation.getFOEventHandler().getFontInfo();
        FontTriplet[] fontkeys = citation.getCommonFont().getFontState(fi);
        font = fi.getFontInstance(fontkeys[0], citation.getCommonFont().fontSize.getValue(this));
        setCommonBorderPaddingBackground(citation.getCommonBorderPaddingBackground());
    }

    /**
     * {@inheritDoc}
     */
    protected AlignmentContext makeAlignmentContext(LayoutContext context) {
        return new AlignmentContext(
                font
                , citation.getLineHeight().getOptimum(this).getLength().getValue(this)
                , citation.getAlignmentAdjust()
                , citation.getAlignmentBaseline()
                , citation.getBaselineShift()
                , citation.getDominantBaseline()
                , context.getAlignmentContext()
            );
    }

    @Override
    protected MinOptMax getAllocationIPD(int refIPD) {
        determineCitationString();
        int ipd = getStringWidth(citationString);
        return MinOptMax.getInstance(ipd);
    }

    private void determineCitationString() {
        assert citationString == null;
        PageViewport page = getCitedPage();
        if (page != null) {
            resolved = true;
            citationString = page.getPageNumberString();
        } else {
            resolved = false;
            citationString = "MMM"; // Use a place holder
        }
    }

    private int getStringWidth(String str) {
        int width = 0;
        for (int count = 0; count < str.length(); count++) {
            width += font.getCharWidth(str.charAt(count));
        }
        return width;
    }

    protected abstract PageViewport getCitedPage();

    @Override
    protected InlineArea getEffectiveArea(LayoutContext layoutContext) {
        InlineArea area = getPageNumberCitationArea();
        if (!layoutContext.treatAsArtifact()) {
            TraitSetter.addStructureTreeElement(area, citation.getStructureTreeElement());
        }
        return area;
    }

    private InlineArea getPageNumberCitationArea() {
        TextArea text;
        if (resolved) {
            text = new TextArea();
            text.addWord(citationString, 0);
        } else {
            UnresolvedPageNumber unresolved = new UnresolvedPageNumber(citation.getRefId(), font,
                    getReferenceType());
            getPSLM().addUnresolvedArea(citation.getRefId(), unresolved);
            text = unresolved;
        }
        setTraits(text);
        return text;
    }

    /**
     * @return {@link org.apache.fop.area.inline.UnresolvedPageNumber#FIRST} or
     * {@link org.apache.fop.area.inline.UnresolvedPageNumber#LAST}
     */
    protected abstract boolean getReferenceType();

    private void setTraits(TextArea text) {
        TraitSetter.setProducerID(text, citation.getId());
        int bidiLevel = getBidiLevel();
        text.setBidiLevel(bidiLevel);
        int width = getStringWidth(citationString); // TODO: [GA] !I18N!
        text.setIPD(width); // TODO: [GA] !I18N!
        text.setBPD(font.getAscender() - font.getDescender());
        text.setBaselineOffset(font.getAscender());
        TraitSetter.addFontTraits(text, font);
        text.addTrait(Trait.COLOR, citation.getColor());
        TraitSetter.addTextDecoration(text, citation.getTextDecoration());
    }

    /**
     * @return bidi level governing abstract page number citation
     */
    protected int getBidiLevel() {
        return citation.getBidiLevel();
    }

}

