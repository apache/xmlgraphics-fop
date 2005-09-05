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

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.fop.fo.FOText;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fonts.Font;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for text (a sequence of characters) which generates one
 * or more inline areas.
 */
public class TextLayoutManager extends LeafNodeLayoutManager {

    /**
     * Store information about each potential text area.
     * Index of character which ends the area, IPD of area, including
     * any word-space and letter-space.
     * Number of word-spaces?
     */
    private class AreaInfo {
        private short iStartIndex;
        private short iBreakIndex;
        private short iWScount;
        private short iLScount;
        private MinOptMax ipdArea;
        private boolean bHyphenated;
        public AreaInfo(short iSIndex, short iBIndex, short iWS, short iLS,
                        MinOptMax ipd, boolean bHyph) {
            iStartIndex = iSIndex;
            iBreakIndex = iBIndex;
            iWScount = iWS;
            iLScount = iLS;
            ipdArea = ipd;
            bHyphenated = bHyph;
        }
    }

    // this class stores information about changes in vecAreaInfo
    // which are not yet applied
    private class PendingChange {
        public AreaInfo ai;
        public int index;

        public PendingChange(AreaInfo ai, int index) {
            this.ai = ai;
            this.index = index;
        }
    }

    // Hold all possible breaks for the text in this LM's FO.
    private ArrayList vecAreaInfo;

    /** Non-space characters on which we can end a line. */
    private static final String BREAK_CHARS = "-/" ;

    private FOText foText;
    private char[] textArray;

    private static final char NEWLINE = '\n';
    private static final char SPACE = '\u0020'; // Normal space
    private static final char NBSPACE = '\u00A0'; // Non-breaking space
    private static final char LINEBREAK = '\u2028';
    private static final char ZERO_WIDTH_SPACE = '\u200B';
    // byte order mark
    private static final char ZERO_WIDTH_NOBREAK_SPACE = '\uFEFF';

    /** Start index of first character in this parent Area */
    private short iAreaStart = 0;
    /** Start index of next TextArea */
    private short iNextStart = 0;
    /** Size since last makeArea call, except for last break */
    private MinOptMax ipdTotal;
    /** Size including last break possibility returned */
    // private MinOptMax nextIPD = new MinOptMax(0);
    /** size of a space character (U+0020) glyph in current font */
    private int spaceCharIPD;
    private MinOptMax wordSpaceIPD;
    private MinOptMax letterSpaceIPD;
    /** size of the hyphen character glyph in current font */
    private int hyphIPD;
    /** 1/2 of word-spacing value */
    private SpaceVal halfWS;
    /** Number of space characters after previous possible break position. */
    private int iNbSpacesPending;
    private Font fs;

    private boolean bChanged = false;
    private int iReturnedIndex = 0;
    private short iThisStart = 0;
    private short iTempStart = 0;
    private LinkedList changeList = null;

    private int textHeight;
    private int lead = 0;
    private int total = 0;
    private int middle = 0;
    private int verticalAlignment = EN_BASELINE;
    /**
     * Create a Text layout manager.
     *
     * @param node The FOText object to be rendered
     */
    public TextLayoutManager(FOText node) {
        super();
        foText = node;
        
        textArray = new char[node.endIndex - node.startIndex];
        System.arraycopy(node.ca, node.startIndex, textArray, 0,
            node.endIndex - node.startIndex);

        vecAreaInfo = new java.util.ArrayList();
    }
    
    public void initialize() {
        fs = foText.getCommonFont().getFontState(foText.getFOEventHandler().getFontInfo(), this);
        
        // With CID fonts, space isn't neccesary currentFontState.width(32)
        spaceCharIPD = fs.getCharWidth(' ');
        // Use hyphenationChar property
        hyphIPD = fs.getCharWidth(foText.getCommonHyphenation().hyphenationCharacter);
        // Make half-space: <space> on either side of a word-space)
        SpaceVal ls = SpaceVal.makeLetterSpacing(foText.getLetterSpacing());
        SpaceVal ws = SpaceVal.makeWordSpacing(foText.getWordSpacing(), ls, fs);
        halfWS = new SpaceVal(MinOptMax.multiply(ws.getSpace(), 0.5),
                ws.isConditional(), ws.isForcing(), ws.getPrecedence());

        // letter space applies only to consecutive non-space characters,
        // while word space applies to space characters;
        // i.e. the spaces in the string "A SIMPLE TEST" are:
        //      A<<ws>>S<ls>I<ls>M<ls>P<ls>L<ls>E<<ws>>T<ls>E<ls>S<ls>T
        // there is no letter space after the last character of a word,
        // nor after a space character

        // set letter space and word space dimension;
        // the default value "normal" was converted into a MinOptMax value
        // in the SpaceVal.makeWordSpacing() method
        letterSpaceIPD = ls.getSpace();
        wordSpaceIPD = MinOptMax.add(new MinOptMax(spaceCharIPD), ws.getSpace());

        // set text height
        textHeight = fs.getAscender()
                     - fs.getDescender();

        // if the text node is son of an inline, set vertical align
        if (foText.getParent() instanceof Inline) {
            setAlignment(((Inline) foText.getParent()).getVerticalAlign());
        }
        switch (verticalAlignment) {
            case EN_MIDDLE  : middle = textHeight / 2 ;
                                         break;
            case EN_TOP     : // fall through
            case EN_BOTTOM  : total = textHeight;
                                         break;
            case EN_BASELINE: // fall through
            default                    : lead = fs.getAscender();
                                         total = textHeight;
                                         break;
        }
    }

    /**
     * Reset position for returning next BreakPossibility.
     *
     * @param prevPos the position to reset to
     */
    public void resetPosition(Position prevPos) {
        if (prevPos != null) {
            // ASSERT (prevPos.getLM() == this)
            if (prevPos.getLM() != this) {
                log.error("TextLayoutManager.resetPosition: "
                          + "LM mismatch!!!");
            }
            LeafPosition tbp = (LeafPosition) prevPos;
            AreaInfo ai =
              (AreaInfo) vecAreaInfo.get(tbp.getLeafPos());
            if (ai.iBreakIndex != iNextStart) {
                iNextStart = ai.iBreakIndex;
                vecAreaInfo.ensureCapacity(tbp.getLeafPos() + 1);
                // TODO: reset or recalculate total IPD = sum of all word IPD
                // up to the break position
                ipdTotal = ai.ipdArea;
                setFinished(false);
            }
        } else {
            // Reset to beginning!
            vecAreaInfo.clear();
            iNextStart = 0;
            setFinished(false);
        }
    }

    // TODO: see if we can use normal getNextBreakPoss for this with
    // extra hyphenation information in LayoutContext
    private boolean getHyphenIPD(HyphContext hc, MinOptMax hyphIPD) {
        // Skip leading word-space before calculating count?
        boolean bCanHyphenate = true;
        int iStopIndex = iNextStart + hc.getNextHyphPoint();

        if (textArray.length < iStopIndex) {
            iStopIndex = textArray.length;
            bCanHyphenate = false;
        }
        hc.updateOffset(iStopIndex - iNextStart);

        for (; iNextStart < iStopIndex; iNextStart++) {
            char c = textArray[iNextStart];
            hyphIPD.opt += fs.getCharWidth(c);
            // letter-space?
        }
        // Need to include hyphen size too, but don't count it in the
        // stored running total, since it would be double counted
        // with later hyphenation points
        return bCanHyphenate;
    }

    /**
     * Generate and add areas to parent area.
     * This can either generate an area for each TextArea and each space, or
     * an area containing all text with a parameter controlling the size of
     * the word space. The latter is most efficient for PDF generation.
     * Set size of each area.
     * @param posIter Iterator over Position information returned
     * by this LayoutManager.
     * @param context LayoutContext for adjustments
     */
    public void addAreas(PositionIterator posIter, LayoutContext context) {

        // Add word areas
        AreaInfo ai = null;
        int iStart = -1;
        int iWScount = 0;
        int iLScount = 0;
        MinOptMax realWidth = new MinOptMax(0);

        /* On first area created, add any leading space.
         * Calculate word-space stretch value.
         */
        while (posIter.hasNext()) {
            LeafPosition tbpNext = (LeafPosition) posIter.next();
            //
            if (tbpNext.getLeafPos() != -1) {
                ai = (AreaInfo) vecAreaInfo.get(tbpNext.getLeafPos());
                if (iStart == -1) {
                    iStart = ai.iStartIndex;
                }
                iWScount += ai.iWScount;
                iLScount += ai.iLScount;
                realWidth.add(ai.ipdArea);
            }
        }
        if (ai == null) {
            return;
        }

        // Make an area containing all characters between start and end.
        InlineArea word = null;
        int adjust = 0;
        
        // ignore newline character
        if (textArray[ai.iBreakIndex - 1] == NEWLINE) {
            adjust = 1;
        }
        String str = new String(textArray, iStart,
                                ai.iBreakIndex - iStart - adjust);

        // add hyphenation character if the last word is hyphenated
        if (context.isLastArea() && ai.bHyphenated) {
            str += foText.getCommonHyphenation().hyphenationCharacter;
            realWidth.add(new MinOptMax(hyphIPD));
        }

        // Calculate adjustments
        int iDifference = 0;
        int iTotalAdjust = 0;
        int iWordSpaceDim = wordSpaceIPD.opt;
        int iLetterSpaceDim = letterSpaceIPD.opt;
        double dIPDAdjust = context.getIPDAdjust();
        double dSpaceAdjust = context.getSpaceAdjust(); // not used

        // calculate total difference between real and available width
        if (dIPDAdjust > 0.0) {
            iDifference = (int) ((double) (realWidth.max - realWidth.opt)
                                * dIPDAdjust);
        } else {
            iDifference = (int) ((double) (realWidth.opt - realWidth.min)
                                * dIPDAdjust);
        }
        
        // set letter space adjustment
        if (dIPDAdjust > 0.0) {
            iLetterSpaceDim
                += (int) ((double) (letterSpaceIPD.max - letterSpaceIPD.opt)
                         * dIPDAdjust);
        } else  {
            iLetterSpaceDim
                += (int) ((double) (letterSpaceIPD.opt - letterSpaceIPD.min)
                         * dIPDAdjust);
        }
        iTotalAdjust += (iLetterSpaceDim - letterSpaceIPD.opt) * iLScount;

        // set word space adjustment
        // 
        if (iWScount > 0) {
            iWordSpaceDim += (int) ((iDifference - iTotalAdjust) / iWScount);
        } else {
            // there are no word spaces in this area
        }
        iTotalAdjust += (iWordSpaceDim - wordSpaceIPD.opt) * iWScount;

        TextArea t = createTextArea(str, realWidth, iTotalAdjust, context,
                                    wordSpaceIPD.opt - spaceCharIPD);

        // iWordSpaceDim is computed in relation to wordSpaceIPD.opt
        // but the renderer needs to know the adjustment in relation
        // to the size of the space character in the current font;
        // moreover, the pdf renderer adds the character spacing even to
        // the last character of a word and to space characters: in order
        // to avoid this, we must subtract the letter space width twice;
        // the renderer will compute the space width as:
        //   space width = 
        //     = "normal" space width + letterSpaceAdjust + wordSpaceAdjust
        //     = spaceCharIPD + letterSpaceAdjust +
        //       + (iWordSpaceDim - spaceCharIPD -  2 * letterSpaceAdjust)
        //     = iWordSpaceDim - letterSpaceAdjust
        t.setTextLetterSpaceAdjust(iLetterSpaceDim);
        t.setTextWordSpaceAdjust(iWordSpaceDim - spaceCharIPD
                                 - 2 * t.getTextLetterSpaceAdjust());
        if (context.getIPDAdjust() != 0) {
            // add information about space width
            t.setSpaceDifference(wordSpaceIPD.opt - spaceCharIPD
                                 - 2 * t.getTextLetterSpaceAdjust());
        }
        word = t;
        if (word != null) {
            parentLM.addChildArea(word);
        }
    }

    /**
     * Create an inline word area.
     * This creates a TextArea and sets up the various attributes.
     *
     * @param str the string for the TextArea
     * @param width the MinOptMax width of the content
     * @param adjust the total ipd adjustment with respect to the optimal width
     * @param base the baseline position
     * @return the new word area
     */
    protected TextArea createTextArea(String str, MinOptMax width, int adjust,
                                      LayoutContext context, int spaceDiff) {
        TextArea textArea;
        if (context.getIPDAdjust() == 0.0) {
            // create just a TextArea
            textArea = new TextArea();
        } else {
            // justified area: create a TextArea with extra info
            // about potential adjustments
            textArea = new TextArea(width.max - width.opt,
                                    width.opt - width.min,
                                    adjust);
        }
        textArea.setIPD(width.opt + adjust);
        textArea.setBPD(fs.getAscender() - fs.getDescender());
        int bpd = textArea.getBPD();
        switch (verticalAlignment) {
            case EN_MIDDLE:
                textArea.setOffset(context.getMiddleBaseline() + fs.getXHeight() / 2);
            break;
            case EN_TOP:
                textArea.setOffset(context.getTopBaseline() + fs.getAscender());
            break;
            case EN_BOTTOM:
                textArea.setOffset(context.getBottomBaseline() - bpd + fs.getAscender());
            break;
            case EN_BASELINE:
            default:
                textArea.setOffset(context.getBaseline());
            break;
        }

        textArea.setTextArea(str);
        textArea.addTrait(Trait.FONT_NAME, fs.getFontName());
        textArea.addTrait(Trait.FONT_SIZE, new Integer(fs.getFontSize()));
        textArea.addTrait(Trait.COLOR, foText.getColor());
        
        TraitSetter.addTextDecoration(textArea, foText.getTextDecoration());
        
        return textArea;
    }

    /**
     * Set the alignment of the inline area.
     * @param al the vertical alignment positioning
     */
    public void setAlignment(int al) {
        verticalAlignment = al;
    }

    public LinkedList getNextKnuthElements(LayoutContext context,
                                           int alignment) {
        LinkedList returnList = new LinkedList();
        KnuthSequence sequence = new KnuthSequence(true);
        returnList.add(sequence);

        while (iNextStart < textArray.length) {
            if (textArray[iNextStart] == SPACE
                || textArray[iNextStart] == NBSPACE) {
                // normal, breaking space
                // or non-breaking space
                if (textArray[iNextStart] == NBSPACE) {
                    sequence.add
                        (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                          new LeafPosition(this, vecAreaInfo.size() - 1),
                                          false));
                }
                switch (alignment) {
                case EN_CENTER :
                    vecAreaInfo.add
                        (new AreaInfo(iNextStart, (short) (iNextStart + 1),
                                      (short) 1, (short) 0,
                                      wordSpaceIPD, false));
                    sequence.add
                        (new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                       new LeafPosition(this, vecAreaInfo.size() - 1), false));
                    sequence.add
                        (new KnuthPenalty(0, 0, false,
                                          new LeafPosition(this, -1), true));
                    sequence.add
                        (new KnuthGlue(wordSpaceIPD.opt,
                                       - 6 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                       new LeafPosition(this, -1), true));
                    sequence.add
                        (new KnuthInlineBox(0, 0, 0, 0,
                                      new LeafPosition(this, -1), true));
                    sequence.add
                        (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                          new LeafPosition(this, -1), true));
                    sequence.add
                        (new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                       new LeafPosition(this, -1), true));
                    iNextStart ++;
                    break;

                case EN_START  : // fall through
                case EN_END    :
                    vecAreaInfo.add
                        (new AreaInfo(iNextStart, (short) (iNextStart + 1),
                                      (short) 1, (short) 0,
                                      wordSpaceIPD, false));
                    sequence.add
                        (new KnuthGlue(0, 3 * wordSpaceIPD.opt, 0,
                                       new LeafPosition(this, vecAreaInfo.size() - 1), false));
                    sequence.add
                        (new KnuthPenalty(0, 0, false,
                                          new LeafPosition(this, -1), true));
                    sequence.add
                        (new KnuthGlue(wordSpaceIPD.opt,
                                       - 3 * wordSpaceIPD.opt, 0,
                                       new LeafPosition(this, -1), true));
                    iNextStart ++;
                    break;

                case EN_JUSTIFY:
                    vecAreaInfo.add
                        (new AreaInfo(iNextStart, (short) (iNextStart + 1),
                                      (short) 1, (short) 0,
                                      wordSpaceIPD, false));
                    sequence.add
                        (new KnuthGlue(wordSpaceIPD.opt,
                                       wordSpaceIPD.max - wordSpaceIPD.opt,
                                       wordSpaceIPD.opt - wordSpaceIPD.min,
                                       new LeafPosition(this, vecAreaInfo.size() - 1), false));
                    iNextStart ++;
                    break;

                default:
                    vecAreaInfo.add
                        (new AreaInfo(iNextStart, (short) (iNextStart + 1),
                                      (short) 1, (short) 0,
                                      wordSpaceIPD, false));
                    sequence.add
                        (new KnuthGlue(wordSpaceIPD.opt,
                                       wordSpaceIPD.max - wordSpaceIPD.opt, 0,
                                       new LeafPosition(this, vecAreaInfo.size() - 1), false));
                    iNextStart ++;
                }
            } else if (textArray[iNextStart] == NBSPACE) {
                // non breaking space
                vecAreaInfo.add
                    (new AreaInfo(iNextStart, (short) (iNextStart + 1),
                                  (short) 1, (short) 0,
                                  wordSpaceIPD, false));
                sequence.add
                    (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                      new LeafPosition(this, vecAreaInfo.size() - 1), false));
                sequence.add
                    (new KnuthGlue(wordSpaceIPD.opt,
                                   wordSpaceIPD.max - wordSpaceIPD.opt,
                                   wordSpaceIPD.opt - wordSpaceIPD.min,
                                   new LeafPosition(this, vecAreaInfo.size() - 1), false));
                iNextStart ++;
            } else if (textArray[iNextStart] == NEWLINE) {
                // linefeed; this can happen when linefeed-treatment="preserve"
                // add a penalty item to the list and start a new sequence
                sequence.add
                    (new KnuthPenalty(0, -KnuthElement.INFINITE,
                                      false, null, false));
                sequence = new KnuthSequence(true);
                returnList.add(sequence);
                iNextStart ++;
            } else {
                // the beginning of a word
                iThisStart = iNextStart;
                iTempStart = iNextStart;
                MinOptMax wordIPD = new MinOptMax(0);
                for (; iTempStart < textArray.length
                        && textArray[iTempStart] != SPACE
                        && textArray[iTempStart] != NBSPACE
                     && textArray[iTempStart] != NEWLINE
                     && !(iTempStart > iNextStart
                          && alignment == EN_JUSTIFY
                          && BREAK_CHARS.indexOf(textArray[iTempStart - 1]) >= 0);
                        iTempStart++) {
                    wordIPD.add(fs.getCharWidth(textArray[iTempStart]));
                }
                int iLetterSpaces = iTempStart - iThisStart - 1;
                wordIPD.add(MinOptMax.multiply(letterSpaceIPD, iLetterSpaces));
                vecAreaInfo.add
                    (new AreaInfo(iThisStart, iTempStart, (short) 0,
                                  (short) iLetterSpaces,
                                  wordIPD, false));
                if (letterSpaceIPD.min == letterSpaceIPD.max) {
                    // constant letter space; simply return a box
                    // whose width includes letter spaces
                    sequence.add
                        (new KnuthInlineBox(wordIPD.opt, lead, total, middle,
                                      new LeafPosition(this, vecAreaInfo.size() - 1), false));
                } else {
                    // adjustable letter space;
                    // some other KnuthElements are needed
                    sequence.add
                        (new KnuthInlineBox(wordIPD.opt - iLetterSpaces * letterSpaceIPD.opt,
                                      lead, total, middle,
                                      new LeafPosition(this, vecAreaInfo.size() - 1), false));
                    sequence.add
                        (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                          new LeafPosition(this, -1), true));
                    sequence.add
                        (new KnuthGlue(iLetterSpaces * letterSpaceIPD.opt,
                                       iLetterSpaces * (letterSpaceIPD.max - letterSpaceIPD.opt),
                                       iLetterSpaces * (letterSpaceIPD.opt - letterSpaceIPD.min),
                                       new LeafPosition(this, -1), true));
                    sequence.add
                        (new KnuthInlineBox(0, lead, total, middle,
                                            new LeafPosition(this, -1), true));
                }
                // if the last character is '-' or '/', it could be used as a line end;
                // add a flagged penalty element and glue element representing a suppressible 
                // letter space if the next character is not a space
                if (BREAK_CHARS.indexOf(textArray[iTempStart - 1]) >= 0
                    && iTempStart < textArray.length
                    && textArray[iTempStart] != SPACE
                    && textArray[iTempStart] != NBSPACE) {
                    sequence.add
                        (new KnuthPenalty(0, KnuthPenalty.FLAGGED_PENALTY, true,
                                          new LeafPosition(this, -1), false));
                    sequence.add
                        (new KnuthGlue(letterSpaceIPD.opt,
                                       letterSpaceIPD.max - letterSpaceIPD.opt,
                                       letterSpaceIPD.opt - letterSpaceIPD.min,
                                       new LeafPosition(this, -1), false));
                    // update the information in the AreaInfo, adding one more letter space
                    AreaInfo ai = (AreaInfo) vecAreaInfo.get(vecAreaInfo.size() - 1);
                    ai.iLScount ++;
                }
                iNextStart = iTempStart;
            }
        } // end of while
        if (((List)returnList.getLast()).size() == 0) {
            //Remove an empty sequence because of a trailing newline
            returnList.removeLast();
        }
        setFinished(true);
        if (returnList.size() > 0) {
            return returnList;
        } else {
            return null;
        }
    }

    public List addALetterSpaceTo(List oldList) {
        // old list contains only a box, or the sequence: box penalty glue box;
        // look at the Position stored in the first element in oldList
        // which is always a box
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement el = (KnuthElement)oldListIterator.next();
        LeafPosition pos = (LeafPosition) ((KnuthBox) el).getPosition();
        AreaInfo ai = (AreaInfo) vecAreaInfo.get(pos.getLeafPos());
        ai.iLScount ++;
        ai.ipdArea.add(letterSpaceIPD);
        if (BREAK_CHARS.indexOf(textArray[iTempStart - 1]) >= 0) {
            // the last character could be used as a line break
            // append new elements to oldList
            oldListIterator = oldList.listIterator(oldList.size());
            oldListIterator.add(new KnuthPenalty(0, KnuthPenalty.FLAGGED_PENALTY, true,
                                                 new LeafPosition(this, -1), false));
            oldListIterator.add(new KnuthGlue(letterSpaceIPD.opt,
                                       letterSpaceIPD.max - letterSpaceIPD.opt,
                                       letterSpaceIPD.opt - letterSpaceIPD.min,
                                       new LeafPosition(this, -1), false));
        } else if (letterSpaceIPD.min == letterSpaceIPD.max) {
            // constant letter space: replace the box
            oldListIterator.set(new KnuthInlineBox(ai.ipdArea.opt, lead, total, middle, pos, false));
        } else {
            // adjustable letter space: replace the glue
            oldListIterator.next(); // this would return the penalty element
            oldListIterator.next(); // this would return the glue element
            oldListIterator.set(new KnuthGlue(ai.iLScount * letterSpaceIPD.opt,
                                              ai.iLScount * (letterSpaceIPD.max - letterSpaceIPD.opt),
                                              ai.iLScount * (letterSpaceIPD.opt - letterSpaceIPD.min),
                                              new LeafPosition(this, -1), true));
        }
        return oldList;
    }

    public void hyphenate(Position pos, HyphContext hc) {
        AreaInfo ai
            = (AreaInfo) vecAreaInfo.get(((LeafPosition) pos).getLeafPos());
        int iStartIndex = ai.iStartIndex;
        int iStopIndex;
        boolean bNothingChanged = true;

        while (iStartIndex < ai.iBreakIndex) {
            MinOptMax newIPD = new MinOptMax(0);
            boolean bHyphenFollows;

            if (hc.hasMoreHyphPoints()
                && (iStopIndex = iStartIndex + hc.getNextHyphPoint())
                <= ai.iBreakIndex) {
                // iStopIndex is the index of the first character
                // after a hyphenation point
                bHyphenFollows = true;
            } else {
                // there are no more hyphenation points,
                // or the next one is after ai.iBreakIndex
                bHyphenFollows = false;
                iStopIndex = ai.iBreakIndex;
            }

            hc.updateOffset(iStopIndex - iStartIndex);

            for (int i = iStartIndex; i < iStopIndex; i++) {
                char c = textArray[i];
                newIPD.add(new MinOptMax(fs.getCharWidth(c)));
            }
            // add letter spaces
            boolean bIsWordEnd
                = iStopIndex == ai.iBreakIndex
                && ai.iLScount < (ai.iBreakIndex - ai.iStartIndex);
            newIPD.add(MinOptMax.multiply(letterSpaceIPD,
                                          (bIsWordEnd
                                           ? (iStopIndex - iStartIndex - 1)
                                           : (iStopIndex - iStartIndex))));

            if (!(bNothingChanged
                  && iStopIndex == ai.iBreakIndex 
                  && bHyphenFollows == false)) {
                // the new AreaInfo object is not equal to the old one
                if (changeList == null) {
                    changeList = new LinkedList();
                }
                changeList.add
                    (new PendingChange
                     (new AreaInfo((short) iStartIndex, (short) iStopIndex,
                                   (short) 0,
                                   (short) (bIsWordEnd
                                            ? (iStopIndex - iStartIndex - 1)
                                            : (iStopIndex - iStartIndex)),
                                   newIPD, bHyphenFollows),
                      ((LeafPosition) pos).getLeafPos()));
                bNothingChanged = false;
            }
            iStartIndex = iStopIndex;
        }
        if (!bChanged && !bNothingChanged) {
            bChanged = true;
        }
    }

    public boolean applyChanges(List oldList) {
        setFinished(false);

        if (changeList != null) {
            int iAddedAI = 0;
            int iRemovedAI = 0;
            int iOldIndex = -1;
            PendingChange currChange = null;
            ListIterator changeListIterator = changeList.listIterator();
            while (changeListIterator.hasNext()) {
                currChange = (PendingChange) changeListIterator.next();
                if (currChange.index != iOldIndex) {
                    iRemovedAI ++;
                    iAddedAI ++;
                    iOldIndex = currChange.index;
                    vecAreaInfo.remove(currChange.index + iAddedAI - iRemovedAI);
                    vecAreaInfo.add(currChange.index + iAddedAI - iRemovedAI,
                                    currChange.ai);
                } else {
                    iAddedAI ++;
                    vecAreaInfo.add(currChange.index + iAddedAI - iRemovedAI,
                                    currChange.ai);
                }
            }
            changeList.clear();
        }

        iReturnedIndex = 0;
        return bChanged;
    }

    public LinkedList getChangedKnuthElements(List oldList,
                                              /*int flaggedPenalty,*/
                                              int alignment) {
        if (isFinished()) {
            return null;
        }

        LinkedList returnList = new LinkedList();

        while (iReturnedIndex < vecAreaInfo.size()) {
            AreaInfo ai = (AreaInfo) vecAreaInfo.get(iReturnedIndex);
            if (ai.iWScount == 0) {
                // ai refers either to a word or a word fragment

                // if the last character is '-' or '/' and the next character is not a space
                // one of the letter spaces must be represented using a penalty and a glue,
                // and its width must be subtracted
                if (BREAK_CHARS.indexOf(textArray[ai.iBreakIndex - 1]) >= 0
                    && ai.iLScount == (ai.iBreakIndex - ai.iStartIndex)) {
                    ai.ipdArea.add(new MinOptMax(-letterSpaceIPD.min, -letterSpaceIPD.opt, -letterSpaceIPD.max));
                }
                if (letterSpaceIPD.min == letterSpaceIPD.max) {
                    returnList.add
                        (new KnuthInlineBox(ai.ipdArea.opt, lead, total, middle,
                                      new LeafPosition(this, iReturnedIndex), false));
                } else {
                    returnList.add
                        (new KnuthInlineBox(ai.ipdArea.opt
                                      - ai.iLScount * letterSpaceIPD.opt,
                                      lead, total, middle, 
                                      new LeafPosition(this, iReturnedIndex), false));
                    returnList.add
                        (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                          new LeafPosition(this, -1), true));
                    returnList.add
                        (new KnuthGlue(ai.iLScount * letterSpaceIPD.opt,
                                       ai.iLScount * (letterSpaceIPD.max - letterSpaceIPD.opt),
                                       ai.iLScount * (letterSpaceIPD.opt - letterSpaceIPD.min),
                                       new LeafPosition(this, -1), true));
                    returnList.add
                        (new KnuthInlineBox(0, 0, 0, 0,
                                      new LeafPosition(this, -1), true));
                }
                if (ai.bHyphenated) {
                    returnList.add
                        (new KnuthPenalty(hyphIPD, KnuthPenalty.FLAGGED_PENALTY, true,
                                          new LeafPosition(this, -1), false));
                }
                // if the last character is '-' or '/', it could be used as a line end;
                // add a flagged penalty element and a glue element representing a suppressible 
                // letter space if the next character is not a space
                if (BREAK_CHARS.indexOf(textArray[ai.iBreakIndex - 1]) >= 0
                    && ai.iLScount == (ai.iBreakIndex - ai.iStartIndex)) {
                    returnList.add
                        (new KnuthPenalty(0, KnuthPenalty.FLAGGED_PENALTY, true,
                                          new LeafPosition(this, -1), false));
                    returnList.add
                        (new KnuthGlue(letterSpaceIPD.opt,
                                       letterSpaceIPD.max - letterSpaceIPD.opt,
                                       letterSpaceIPD.opt - letterSpaceIPD.min,
                                       new LeafPosition(this, -1), false));
                }
                iReturnedIndex ++;
            } else {
                // ai refers to a space
                if (textArray[ai.iStartIndex] == NBSPACE) {
                    returnList.add
                        (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                          new LeafPosition(this, -1),
                                          false));
                }
                switch (alignment) {
                case EN_CENTER :
                    returnList.add
                        (new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                       new LeafPosition(this, iReturnedIndex), false));
                    returnList.add
                        (new KnuthPenalty(0, 0, false,
                                          new LeafPosition(this, -1), true));
                    returnList.add
                        (new KnuthGlue(wordSpaceIPD.opt,
                                       - 6 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                       new LeafPosition(this, -1), true));
                    returnList.add
                        (new KnuthInlineBox(0, 0, 0, 0,
                                      new LeafPosition(this, -1), true));
                    returnList.add
                        (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                          new LeafPosition(this, -1), true));
                    returnList.add
                        (new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                       new LeafPosition(this, -1), true));
                    iReturnedIndex ++;
                    break;
                case EN_START  : // fall through
                case EN_END    :
                    returnList.add
                        (new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                       new LeafPosition(this, iReturnedIndex), false));
                    returnList.add
                        (new KnuthPenalty(0, 0, false,
                                          new LeafPosition(this, -1), true));
                    returnList.add
                        (new KnuthGlue(wordSpaceIPD.opt,
                                       - 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                       new LeafPosition(this, -1), true));
                    iReturnedIndex ++;
                    break;
                case EN_JUSTIFY:
                    returnList.add
                        (new KnuthGlue(wordSpaceIPD.opt,
                                       wordSpaceIPD.max - wordSpaceIPD.opt,
                                       wordSpaceIPD.opt - wordSpaceIPD.min,
                                       new LeafPosition(this, iReturnedIndex), false));
                    iReturnedIndex ++;
                    break;
                    
                default:
                    returnList.add
                        (new KnuthGlue(wordSpaceIPD.opt,
                                       wordSpaceIPD.max - wordSpaceIPD.opt, 0,
                                       new LeafPosition(this, iReturnedIndex), false));
                    iReturnedIndex ++;
                }
            }
        } // end of while
        setFinished(true);
        return returnList;
    }

    public void getWordChars(StringBuffer sbChars, Position pos) {
        int iLeafValue = ((LeafPosition) pos).getLeafPos();
        if (iLeafValue != -1) {
            AreaInfo ai = (AreaInfo) vecAreaInfo.get(iLeafValue);
            sbChars.append(new String(textArray, ai.iStartIndex,
                                      ai.iBreakIndex - ai.iStartIndex));
        }
    }
}

