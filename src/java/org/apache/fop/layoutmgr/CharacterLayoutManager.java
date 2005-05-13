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

import org.apache.fop.fo.flow.Character;
import org.apache.fop.fonts.Font;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.Trait;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

import java.util.List;
import java.util.LinkedList;

/**
 * LayoutManager for the fo:character formatting object
 */
public class CharacterLayoutManager extends LeafNodeLayoutManager {
    private Character fobj;
    private MinOptMax letterSpaceIPD;
    private int hyphIPD;
    private Font fs;

    /**
     * Constructor
     *
     * @param node the fo:character formatting object
     * @todo better null checking of node
     */
    public CharacterLayoutManager(Character node) {
        super(node);
        fobj = node;
        InlineArea inline = getCharacterInlineArea(node);
        setCurrentArea(inline);
        setAlignment(fobj.getVerticalAlign());
        fs = fobj.getCommonFont().getFontState(fobj.getFOEventHandler().getFontInfo());

        SpaceVal ls = SpaceVal.makeLetterSpacing(fobj.getLetterSpacing());
        letterSpaceIPD = ls.getSpace();
        hyphIPD = fs.getCharWidth(fobj.getCommonHyphenation().hyphenationCharacter);
    }

    private InlineArea getCharacterInlineArea(Character node) {
        org.apache.fop.area.inline.Character ch =
            new org.apache.fop.area.inline.Character(node.getCharacter());
        TraitSetter.addTextDecoration(ch, fobj.getTextDecoration());
        return ch;
    }

    /**
     * Offset this area.
     * Offset the inline area in the bpd direction when adding the
     * inline area.
     * This is used for vertical alignment.
     * Subclasses should override this if necessary.
     * @param context the layout context used for adding the area
     */
    protected void offsetArea(LayoutContext context) {
        int bpd = curArea.getBPD();
        switch (verticalAlignment) {
            case EN_MIDDLE:
                curArea.setOffset(context.getMiddleBaseline() + fs.getXHeight() / 2);
            break;
            case EN_TOP:
                curArea.setOffset(fs.getAscender());
            break;
            case EN_BOTTOM:
                curArea.setOffset(context.getLineHeight() - bpd + fs.getAscender());
            break;
            case EN_BASELINE:
            default:
                curArea.setOffset(context.getBaseline());
            break;
        }
    }

    public LinkedList getNextKnuthElements(LayoutContext context,
                                           int alignment) {
        MinOptMax ipd;
        curArea = get(context);
        LinkedList returnList = new LinkedList();

        if (curArea == null) {
            setFinished(true);
            return null;
        }

        ipd = new MinOptMax(fs.getCharWidth(((org.apache.fop.area.inline.Character) curArea).getChar().charAt(0)));

        curArea.setIPD(ipd.opt);
        curArea.setBPD(fs.getAscender() - fs.getDescender());

        // offset is set in the offsetArea() method
        //curArea.setOffset(textInfo.fs.getAscender());
        //curArea.setOffset(context.getBaseline()); 

        curArea.addTrait(Trait.FONT_NAME, fs.getFontName());
        curArea.addTrait(Trait.FONT_SIZE, new Integer(fs.getFontSize()));
        curArea.addTrait(Trait.COLOR, fobj.getColor());

        int bpd = curArea.getBPD();
        int lead = 0;
        int total = 0;
        int middle = 0;
        switch (verticalAlignment) {
            case EN_MIDDLE  : middle = bpd / 2 ;
                                         break;
            case EN_TOP     : // fall through
            case EN_BOTTOM  : total = bpd;
                                         break;
            case EN_BASELINE: // fall through
            default                    : lead = fs.getAscender();
                                         total = bpd;
                                         break;
        }

        // create the AreaInfo object to store the computed values
        areaInfo = new AreaInfo((short) 0, ipd, false,
                                lead, total, middle);

        // node is a fo:Character
        if (letterSpaceIPD.min == letterSpaceIPD.max) {
            // constant letter space, only return a box
            returnList.add(new KnuthInlineBox(areaInfo.ipdArea.opt, areaInfo.lead,
                                        areaInfo.total, areaInfo.middle,
                                        new LeafPosition(this, 0), false));
        } else {
            // adjustable letter space, return a sequence of elements;
            // at the moment the character is supposed to have no letter spaces,
            // but returning this sequence allows us to change only one element
            // if addALetterSpaceTo() is called
            returnList.add(new KnuthInlineBox(areaInfo.ipdArea.opt, areaInfo.lead,
                                        areaInfo.total, areaInfo.middle,
                                        new LeafPosition(this, 0), false));
            returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                            new LeafPosition(this, -1), true));
            returnList.add(new KnuthGlue(0, 0, 0,
                                         new LeafPosition(this, -1), true));
            returnList.add(new KnuthInlineBox(0, 0, 0, 0,
                                        new LeafPosition(this, -1), true));
        }

        setFinished(true);
        return returnList;
    }

    public void getWordChars(StringBuffer sbChars, Position bp) {
        sbChars.append
            (((org.apache.fop.area.inline.Character) curArea).getChar());
    }

    public KnuthElement addALetterSpaceTo(KnuthElement element) {
        areaInfo.iLScount ++;
        areaInfo.ipdArea.add(letterSpaceIPD);

        if (letterSpaceIPD.min == letterSpaceIPD.max) {
            // constant letter space, return a new box
            return new KnuthInlineBox(areaInfo.ipdArea.opt, areaInfo.lead,
                                areaInfo.total, areaInfo.middle,
                                new LeafPosition(this, 0), false);
        } else {
            // adjustable letter space, return a new glue
            return new KnuthGlue(letterSpaceIPD.opt,
                                 letterSpaceIPD.max - letterSpaceIPD.opt,
                                 letterSpaceIPD.opt - letterSpaceIPD.min,
                                 new LeafPosition(this, -1), true);
        }
    }

    public void hyphenate(Position pos, HyphContext hc) {
        if (hc.getNextHyphPoint() == 1) {
            // the character ends a syllable
            areaInfo.bHyphenated = true;
            bSomethingChanged = true;
        } else {
            // hc.getNextHyphPoint() returned -1 (no more hyphenation points)
            // or a number > 1;
            // the character does not end a syllable
        }
        hc.updateOffset(1);
    }

    public boolean applyChanges(List oldList) {
        setFinished(false);
        if (bSomethingChanged) {
            // there is nothing to do,
            // possible changes have already been applied
            // in the hyphenate() method
            return true;
        } else {
            return false;
        }
    }

    public LinkedList getChangedKnuthElements(List oldList,
                                              int flaggedPenalty,
                                              int alignment) {
        if (isFinished()) {
            return null;
        }

        LinkedList returnList = new LinkedList();

        if (letterSpaceIPD.min == letterSpaceIPD.max
            || areaInfo.iLScount == 0) {
            // constant letter space, or no letter space
            returnList.add(new KnuthInlineBox(areaInfo.ipdArea.opt, areaInfo.lead,
                                        areaInfo.total, areaInfo.middle,
                                        new LeafPosition(this, 0), false));
            if (areaInfo.bHyphenated) {
                returnList.add
                    (new KnuthPenalty(hyphIPD, flaggedPenalty, true,
                                      new LeafPosition(this, -1), false));
            }
        } else {
            // adjustable letter space
            returnList.add
                (new KnuthInlineBox(areaInfo.ipdArea.opt
                              - areaInfo.iLScount * letterSpaceIPD.opt,
                              areaInfo.lead, areaInfo.total, areaInfo.middle,
                              new LeafPosition(this, 0), false));
            returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                            new LeafPosition(this, -1), true));
            returnList.add
                (new KnuthGlue(areaInfo.iLScount * letterSpaceIPD.opt,
                               areaInfo.iLScount * letterSpaceIPD.max - letterSpaceIPD.opt,
                               areaInfo.iLScount * letterSpaceIPD.opt - letterSpaceIPD.min,
                               new LeafPosition(this, -1), true));
            returnList.add(new KnuthInlineBox(0, 0, 0, 0,
                                        new LeafPosition(this, -1), true));
            if (areaInfo.bHyphenated) {
                returnList.add
                    (new KnuthPenalty(hyphIPD, flaggedPenalty, true,
                                      new LeafPosition(this, -1), false));
            }
        }

        setFinished(true);
        return returnList;
    }

    protected void addId() {
        getPSLM().addIDToPage(fobj.getId());
    }
}

