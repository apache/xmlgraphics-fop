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

import java.util.LinkedList;
import java.util.List;

import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontSelector;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.util.CharUtilities;

/**
 * LayoutManager for the fo:character formatting object
 */
public class CharacterLayoutManager extends LeafNodeLayoutManager {
    private MinOptMax letterSpaceIPD;
    private int hyphIPD;
    private Font font;
    private CommonBorderPaddingBackground borderProps;

    /**
     * Constructor
     *
     * @param node the fo:character formatting object
     */
    public CharacterLayoutManager(Character node) {
        super(node);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize() {
        Character fobj = (Character)this.fobj;
        font = FontSelector.selectFontForCharacter(fobj, this);
        SpaceVal ls = SpaceVal.makeLetterSpacing(fobj.getLetterSpacing());
        letterSpaceIPD = ls.getSpace();
        hyphIPD = fobj.getCommonHyphenation().getHyphIPD(font);
        borderProps = fobj.getCommonBorderPaddingBackground();
        setCommonBorderPaddingBackground(borderProps);
    }

    private TextArea createCharacterArea() {
        Character fobj = (Character) this.fobj;
        TextArea text = new TextArea();
        char ch = fobj.getCharacter();
        int ipd = font.getCharWidth(ch);
        int blockProgressionOffset = 0;
        int level = fobj.getBidiLevel();
        if (CharUtilities.isAnySpace(ch)) {
            // add space unless it's zero-width:
            if (!CharUtilities.isZeroWidthSpace(ch)) {
                text.addSpace(ch, ipd, CharUtilities.isAdjustableSpace(ch),
                              blockProgressionOffset, level);
            }
        } else {
            int[] levels = (level >= 0) ? new int[] {level} : null;
            text.addWord(String.valueOf(ch), ipd, null, levels, null, blockProgressionOffset);
        }

        TraitSetter.setProducerID(text, fobj.getId());
        TraitSetter.addTextDecoration(text, fobj.getTextDecoration());
        text.setIPD(font.getCharWidth(fobj.getCharacter()));
        text.setBPD(font.getAscender() - font.getDescender());
        text.setBaselineOffset(font.getAscender());
        TraitSetter.addFontTraits(text, font);
        text.addTrait(Trait.COLOR, fobj.getColor());
        return text;
    }

    @Override
    protected InlineArea getEffectiveArea(LayoutContext layoutContext) {
        InlineArea area = createCharacterArea();
        if (!layoutContext.treatAsArtifact()) {
            TraitSetter.addStructureTreeElement(area, ((Character) fobj).getStructureTreeElement());
        }
        return area;
    }

    /** {@inheritDoc} */
    public List getNextKnuthElements(LayoutContext context, int alignment) {
        Character fobj = (Character) this.fobj;

        // TODO: may need some special handling for fo:character
        alignmentContext = new AlignmentContext(font
                                    , font.getFontSize()
                                    , fobj.getAlignmentAdjust()
                                    , fobj.getAlignmentBaseline()
                                    , fobj.getBaselineShift()
                                    , fobj.getDominantBaseline()
                                    , context.getAlignmentContext());

        KnuthSequence seq = new InlineKnuthSequence();
        addKnuthElementsForBorderPaddingStart(seq);

        // create the AreaInfo object to store the computed values
        MinOptMax ipd = MinOptMax.getInstance(font.getCharWidth(fobj.getCharacter()));
        areaInfo = new AreaInfo((short) 0, ipd, false, alignmentContext);

        // node is a fo:Character
        if (letterSpaceIPD.isStiff()) {
            // constant letter space, only return a box
            seq.add(new KnuthInlineBox(areaInfo.ipdArea.getOpt(), areaInfo.alignmentContext,
                                        notifyPos(new LeafPosition(this, 0)), false));
        } else {
            // adjustable letter space, return a sequence of elements;
            // at the moment the character is supposed to have no letter spaces,
            // but returning this sequence allows us to change only one element
            // if addALetterSpaceTo() is called
            seq.add(new KnuthInlineBox(areaInfo.ipdArea.getOpt(), areaInfo.alignmentContext,
                                        notifyPos(new LeafPosition(this, 0)), false));
            seq.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                            new LeafPosition(this, -1), true));
            seq.add(new KnuthGlue(0, 0, 0,
                                         new LeafPosition(this, -1), true));
            seq.add(new KnuthInlineBox(0, null,
                                        notifyPos(new LeafPosition(this, -1)), true));
        }

        addKnuthElementsForBorderPaddingEnd(seq);

        LinkedList<KnuthSequence> returnList = new LinkedList<KnuthSequence>();
        returnList.add(seq);
        setFinished(true);
        return returnList;
    }

    /** {@inheritDoc} */
    @Override
    public String getWordChars(Position pos) {
        return String.valueOf(((Character) fobj).getCharacter());
    }

    /** {@inheritDoc} */
    @Override
    public void hyphenate(Position pos, HyphContext hc) {
        if (hc.getNextHyphPoint() == 1) {
            // the character ends a syllable
            areaInfo.isHyphenated = true;
            somethingChanged = true;
        } else {
            // hc.getNextHyphPoint() returned -1 (no more hyphenation points)
            // or a number > 1;
            // the character does not end a syllable
        }
        hc.updateOffset(1);
    }

    /** {@inheritDoc} */
    @Override
    public boolean applyChanges(List oldList) {
        setFinished(false);
        return somethingChanged;
    }

    /** {@inheritDoc} */
    @Override
    public List getChangedKnuthElements(List oldList, int alignment) {
        if (isFinished()) {
            return null;
        }

        LinkedList<KnuthElement> returnList = new LinkedList<KnuthElement>();

        addKnuthElementsForBorderPaddingStart(returnList);

        if (letterSpaceIPD.isStiff() || areaInfo.letterSpaces == 0) {
            // constant letter space, or no letter space
            returnList.add(new KnuthInlineBox(areaInfo.ipdArea.getOpt(),
                                        areaInfo.alignmentContext,
                                        notifyPos(new LeafPosition(this, 0)), false));
            if (areaInfo.isHyphenated) {
                returnList.add(new KnuthPenalty(hyphIPD, KnuthPenalty.FLAGGED_PENALTY, true,
                        new LeafPosition(this, -1), false));
            }
        } else {
            // adjustable letter space
            returnList.add(new KnuthInlineBox(areaInfo.ipdArea.getOpt()
                    - areaInfo.letterSpaces * letterSpaceIPD.getOpt(), areaInfo.alignmentContext,
                    notifyPos(new LeafPosition(this, 0)), false));
            returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                    new LeafPosition(this, -1), true));
            returnList.add(new KnuthGlue(letterSpaceIPD.mult(areaInfo.letterSpaces),
                    new LeafPosition(this, -1), true));
            returnList.add(
                    new KnuthInlineBox(0, null, notifyPos(new LeafPosition(this, -1)), true));
            if (areaInfo.isHyphenated) {
                returnList.add(new KnuthPenalty(hyphIPD, KnuthPenalty.FLAGGED_PENALTY, true,
                        new LeafPosition(this, -1), false));
            }
        }

        addKnuthElementsForBorderPaddingEnd(returnList);

        setFinished(true);
        return returnList;
    }

}
