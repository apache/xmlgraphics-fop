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
    private CommonBorderPaddingBackground borderProps = null;

    /**
     * Constructor
     *
     * @param node the fo:character formatting object
     */
    public CharacterLayoutManager(Character node) {
        super(node);
    }

    /** {@inheritDoc} */
    public void initialize() {
        Character fobj = (Character)this.fobj;
        font = FontSelector.selectFontForCharacter(fobj, this);
        SpaceVal ls = SpaceVal.makeLetterSpacing(fobj.getLetterSpacing());
        letterSpaceIPD = ls.getSpace();
        hyphIPD = fobj.getCommonHyphenation().getHyphIPD(font);
        borderProps = fobj.getCommonBorderPaddingBackground();
        setCommonBorderPaddingBackground(borderProps);
        org.apache.fop.area.inline.TextArea chArea = getCharacterInlineArea(fobj);
        chArea.setBaselineOffset(font.getAscender());
        setCurrentArea(chArea);
    }

    private org.apache.fop.area.inline.TextArea getCharacterInlineArea(Character node) {
        org.apache.fop.area.inline.TextArea text
            = new org.apache.fop.area.inline.TextArea();
        char ch = node.getCharacter();
        if (CharUtilities.isAnySpace(ch)) {
            // add space unless it's zero-width:
            if (!CharUtilities.isZeroWidthSpace(ch)) {
                text.addSpace(ch, 0, CharUtilities.isAdjustableSpace(ch));
            }
        } else {
            text.addWord(String.valueOf(ch), 0);
        }
        TraitSetter.setProducerID(text, node.getId());
        TraitSetter.addTextDecoration(text, node.getTextDecoration());
        return text;
    }

    /** {@inheritDoc} */
    public List getNextKnuthElements(LayoutContext context, int alignment) {
        MinOptMax ipd;
        curArea = get(context);
        KnuthSequence seq = new InlineKnuthSequence();

        if (curArea == null) {
            setFinished(true);
            return null;
        }

        Character fobj = (Character)this.fobj;

        ipd = new MinOptMax(font.getCharWidth(fobj.getCharacter()));

        curArea.setIPD(ipd.opt);
        curArea.setBPD(font.getAscender() - font.getDescender());

        TraitSetter.addFontTraits(curArea, font);
        curArea.addTrait(Trait.COLOR, fobj.getColor());

        // TODO: may need some special handling for fo:character
        alignmentContext = new AlignmentContext(font
                                    , font.getFontSize()
                                    , fobj.getAlignmentAdjust()
                                    , fobj.getAlignmentBaseline()
                                    , fobj.getBaselineShift()
                                    , fobj.getDominantBaseline()
                                    , context.getAlignmentContext());

        addKnuthElementsForBorderPaddingStart(seq);

        // create the AreaInfo object to store the computed values
        areaInfo = new AreaInfo((short) 0, ipd, false, alignmentContext);

        // node is a fo:Character
        if (letterSpaceIPD.min == letterSpaceIPD.max) {
            // constant letter space, only return a box
            seq.add(new KnuthInlineBox(areaInfo.ipdArea.opt, areaInfo.alignmentContext,
                                        notifyPos(new LeafPosition(this, 0)), false));
        } else {
            // adjustable letter space, return a sequence of elements;
            // at the moment the character is supposed to have no letter spaces,
            // but returning this sequence allows us to change only one element
            // if addALetterSpaceTo() is called
            seq.add(new KnuthInlineBox(areaInfo.ipdArea.opt, areaInfo.alignmentContext,
                                        notifyPos(new LeafPosition(this, 0)), false));
            seq.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                            new LeafPosition(this, -1), true));
            seq.add(new KnuthGlue(0, 0, 0,
                                         new LeafPosition(this, -1), true));
            seq.add(new KnuthInlineBox(0, null,
                                        notifyPos(new LeafPosition(this, -1)), true));
        }

        addKnuthElementsForBorderPaddingEnd(seq);

        LinkedList returnList = new LinkedList();
        returnList.add(seq);
        setFinished(true);
        return returnList;
    }

    /** {@inheritDoc} */
    public void getWordChars(StringBuffer sbChars, Position bp) {
        sbChars.append
            (((org.apache.fop.area.inline.TextArea) curArea).getText());
    }

    /** {@inheritDoc} */
    public void hyphenate(Position pos, HyphContext hc) {
        if (hc.getNextHyphPoint() == 1) {
            // the character ends a syllable
            areaInfo.bHyphenated = true;
            isSomethingChanged = true;
        } else {
            // hc.getNextHyphPoint() returned -1 (no more hyphenation points)
            // or a number > 1;
            // the character does not end a syllable
        }
        hc.updateOffset(1);
    }

    /** {@inheritDoc} */
    public boolean applyChanges(List oldList) {
        setFinished(false);
        return isSomethingChanged;
    }

    /** {@inheritDoc} */
    public List getChangedKnuthElements(List oldList, int alignment) {
        if (isFinished()) {
            return null;
        }

        LinkedList returnList = new LinkedList();

        addKnuthElementsForBorderPaddingStart(returnList);

        if (letterSpaceIPD.min == letterSpaceIPD.max
            || areaInfo.iLScount == 0) {
            // constant letter space, or no letter space
            returnList.add(new KnuthInlineBox(areaInfo.ipdArea.opt,
                                        areaInfo.alignmentContext,
                                        notifyPos(new LeafPosition(this, 0)), false));
            if (areaInfo.bHyphenated) {
                returnList.add
                    (new KnuthPenalty(hyphIPD, KnuthPenalty.FLAGGED_PENALTY, true,
                                      new LeafPosition(this, -1), false));
            }
        } else {
            // adjustable letter space
            returnList.add
                (new KnuthInlineBox(areaInfo.ipdArea.opt
                              - areaInfo.iLScount * letterSpaceIPD.opt,
                              areaInfo.alignmentContext,
                              notifyPos(new LeafPosition(this, 0)), false));
            returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                            new LeafPosition(this, -1), true));
            returnList.add
                (new KnuthGlue(areaInfo.iLScount * letterSpaceIPD.opt,
                               areaInfo.iLScount * letterSpaceIPD.max - letterSpaceIPD.opt,
                               areaInfo.iLScount * letterSpaceIPD.opt - letterSpaceIPD.min,
                               new LeafPosition(this, -1), true));
            returnList.add(new KnuthInlineBox(0, null,
                                        notifyPos(new LeafPosition(this, -1)), true));
            if (areaInfo.bHyphenated) {
                returnList.add
                    (new KnuthPenalty(hyphIPD, KnuthPenalty.FLAGGED_PENALTY, true,
                                      new LeafPosition(this, -1), false));
            }
        }

        addKnuthElementsForBorderPaddingEnd(returnList);

        setFinished(true);
        return returnList;
    }

}

