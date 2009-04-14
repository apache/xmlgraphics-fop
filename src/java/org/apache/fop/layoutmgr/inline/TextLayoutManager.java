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
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.FObj;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontSelector;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.text.linebreak.LineBreakStatus;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.util.ListUtil;

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
        private final int startIndex;
        private final int breakIndex;
        private final int wordSpaceCount;
        private int letterSpaceCount;
        private final MinOptMax areaIPD;
        private final boolean isHyphenated;
        private final boolean isSpace;
        private boolean breakOppAfter;
        private final Font font;

        AreaInfo(final int startIndex,
                        final int breakIndex,
                        final int wordSpaceCount,
                        final int letterSpaceCount,
                        final MinOptMax areaIPD,
                        final boolean isHyphenated,
                        final boolean isSpace,
                        final boolean breakOppAfter,
                        final Font font) {
            this.startIndex = startIndex;
            this.breakIndex = breakIndex;
            this.wordSpaceCount = wordSpaceCount;
            this.letterSpaceCount = letterSpaceCount;
            this.areaIPD = areaIPD;
            this.isHyphenated = isHyphenated;
            this.isSpace = isSpace;
            this.breakOppAfter = breakOppAfter;
            this.font = font;
        }

        public String toString() {
            return "[ lscnt=" + this.letterSpaceCount
                + ", wscnt=" + this.wordSpaceCount
                + ", ipd=" + this.areaIPD.toString()
                + ", sidx=" + this.startIndex
                + ", bidx=" + this.breakIndex
                + ", hyph=" + this.isHyphenated
                + ", space=" + this.isSpace
                + ", font=" + this.font
                + "]";
        }

    }

    // this class stores information about changes in vecAreaInfo
    // which are not yet applied
    private final class PendingChange {
        private final AreaInfo ai;
        private final int index;

        private PendingChange(final AreaInfo ai, final int index) {
            this.ai = ai;
            this.index = index;
        }
    }

    /**
     * logging instance
     */
    private static final Log LOG = LogFactory.getLog(TextLayoutManager.class);

    // Hold all possible breaks for the text in this LM's FO.
    private final List vecAreaInfo;

    /** Non-space characters on which we can end a line. */
    private static final String BREAK_CHARS = "-/";

    /** Used to reduce instantiation of MinOptMax with zero length. Do not modify! */
    private static final MinOptMax ZERO_MINOPTMAX = new MinOptMax(0);

    private final FOText foText;

    /**
     * Contains an array of widths to adjust for kerning. The first entry can
     * be used to influence the start position of the first letter. The entry i+1 defines the
     * cursor advancement after the character i. A null entry means no special advancement.
     */
    private final MinOptMax[] letterAdjustArray; //size = textArray.length + 1

    /** Font used for the space between words. */
    private Font spaceFont = null;
    /** Start index of next TextArea */
    private int nextStart = 0;
    /** size of a space character (U+0020) glyph in current font */
    private int spaceCharIPD;
    private MinOptMax wordSpaceIPD;
    private MinOptMax letterSpaceIPD;
    /** size of the hyphen character glyph in current font */
    private int hyphIPD;
    /** 1/1 of word-spacing value */
    private SpaceVal ws;

    private boolean hasChanged = false;
    private int returnedIndex = 0;
    private int thisStart = 0;
    private int tempStart = 0;
    private List changeList = null;

    private AlignmentContext alignmentContext = null;

    private int lineStartBAP = 0;
    private int lineEndBAP = 0;

    private boolean keepTogether;

    private final Position auxiliaryPosition = new LeafPosition(this, -1);

    /**
     * Create a Text layout manager.
     *
     * @param node The FOText object to be rendered
     */
    public TextLayoutManager(final FOText node) {
        super();
        this.foText = node;

        this.letterAdjustArray = new MinOptMax[node.length() + 1];

        this.vecAreaInfo = new java.util.ArrayList();
    }

    private KnuthPenalty makeZeroWidthPenalty(final int penaltyValue) {
        return new KnuthPenalty(
                0,
                penaltyValue,
                false,
                this.auxiliaryPosition,
                true);
    }

    private KnuthBox makeAuxiliaryZeroWidthBox() {
        return new KnuthInlineBox(
                0,
                null,
                this.notifyPos(new LeafPosition(this, -1)),
                true);
    }

    /** {@inheritDoc} */
    public void initialize() {

        this.foText.resetBuffer();

        this.spaceFont = FontSelector.selectFontForCharacterInText(' ', this.foText, this);

        // With CID fonts, space isn't neccesary currentFontState.width(32)
        this.spaceCharIPD = this.spaceFont.getCharWidth(' ');
        // Use hyphenationChar property

        // TODO: Use hyphen based on actual font used!
        this.hyphIPD = this.foText.getCommonHyphenation().getHyphIPD(this.spaceFont);

        final SpaceVal ls = SpaceVal.makeLetterSpacing(this.foText.getLetterSpacing());

        this.ws = SpaceVal.makeWordSpacing(this.foText.getWordSpacing(), ls, this.spaceFont);

        // letter space applies only to consecutive non-space characters,
        // while word space applies to space characters;
        // i.e. the spaces in the string "A SIMPLE TEST" are:
        //      A<<ws>>S<ls>I<ls>M<ls>P<ls>L<ls>E<<ws>>T<ls>E<ls>S<ls>T
        // there is no letter space after the last character of a word,
        // nor after a space character
        // NOTE: The above is not quite correct. Read on in XSL 1.0, 7.16.2, letter-spacing

        // set letter space and word space dimension;
        // the default value "normal" was converted into a MinOptMax value
        // in the SpaceVal.makeWordSpacing() method
        this.letterSpaceIPD = ls.getSpace();
        this.wordSpaceIPD = MinOptMax.add(new MinOptMax(this.spaceCharIPD), this.ws.getSpace());

        this.keepTogether = this.foText.getKeepTogether().getWithinLine()
                .getEnum() == Constants.EN_ALWAYS;

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
    public void addAreas(final PositionIterator posIter, final LayoutContext context) {

        // Add word areas
        AreaInfo ai;
        int wordSpaceCount = 0;
        int letterSpaceCount = 0;
        int firstAreaInfoIndex = -1;
        int lastAreaInfoIndex = 0;
        MinOptMax realWidth = new MinOptMax(0);

        /* On first area created, add any leading space.
         * Calculate word-space stretch value.
         */
        AreaInfo lastAi = null;
        while (posIter.hasNext()) {
            final LeafPosition tbpNext = (LeafPosition) posIter.next();
            if (tbpNext == null) {
                continue; //Ignore elements without Positions
            }
            if (tbpNext.getLeafPos() != -1) {
                ai = (AreaInfo) this.vecAreaInfo.get(tbpNext.getLeafPos());
                if (lastAi == null || ai.font != lastAi.font) {
                    if (lastAi != null) {
                        this.addAreaInfoAreas(lastAi, wordSpaceCount,
                                letterSpaceCount, firstAreaInfoIndex,
                                lastAreaInfoIndex, realWidth, context);
                    }
                    firstAreaInfoIndex = tbpNext.getLeafPos();
                    wordSpaceCount = 0;
                    letterSpaceCount = 0;
                    realWidth = new MinOptMax(0);
                }
                wordSpaceCount += ai.wordSpaceCount;
                letterSpaceCount += ai.letterSpaceCount;
                realWidth.add(ai.areaIPD);
                lastAreaInfoIndex = tbpNext.getLeafPos();
                lastAi = ai;
            }
        }
        if (lastAi != null) {
            this.addAreaInfoAreas(lastAi, wordSpaceCount, letterSpaceCount,
                    firstAreaInfoIndex, lastAreaInfoIndex, realWidth, context);
        }
    }

    private void addAreaInfoAreas(final AreaInfo ai, final int wordSpaceCount,
            int letterSpaceCount, final int firstAreaInfoIndex,
            final int lastAreaInfoIndex, final MinOptMax realWidth, final LayoutContext context) {

        // TODO: These two statements (if, for) were like this before my recent
        // changes. However, it seems as if they should use the AreaInfo from
        // firstAreaInfoIndex.. lastAreaInfoIndex rather than just the last ai.
        // This needs to be checked.
        final int textLength = ai.breakIndex - ai.startIndex;
        if (ai.letterSpaceCount == textLength && !ai.isHyphenated
                   && context.isLastArea()) {
            // the line ends at a character like "/" or "-";
            // remove the letter space after the last character
            realWidth.add(MinOptMax.multiply(this.letterSpaceIPD, -1));
            letterSpaceCount--;
        }

        for (int i = ai.startIndex; i < ai.breakIndex; i++) {
            final MinOptMax ladj = this.letterAdjustArray[i + 1];
            if (ladj != null && ladj.isElastic()) {
                letterSpaceCount++;
            }
        }

        // add hyphenation character if the last word is hyphenated
        if (context.isLastArea() && ai.isHyphenated) {
            realWidth.add(new MinOptMax(this.hyphIPD));
        }

        // Calculate adjustments
        int difference = 0;
        int totalAdjust = 0;
        int wordSpaceDim = this.wordSpaceIPD.opt;
        int letterSpaceDim = this.letterSpaceIPD.opt;
        final double ipdAdjust = context.getIPDAdjust();

        // calculate total difference between real and available width
        if (ipdAdjust > 0.0) {
            difference = (int) ((realWidth.max - realWidth.opt)
                                * ipdAdjust);
        } else {
            difference = (int) ((realWidth.opt - realWidth.min)
                                * ipdAdjust);
        }

        // set letter space adjustment
        if (ipdAdjust > 0.0) {
            letterSpaceDim
                += (int) ((this.letterSpaceIPD.max - this.letterSpaceIPD.opt)
                         * ipdAdjust);
        } else  {
            letterSpaceDim
                += (int) ((this.letterSpaceIPD.opt - this.letterSpaceIPD.min)
                         * ipdAdjust);
        }
        totalAdjust += (letterSpaceDim - this.letterSpaceIPD.opt) * letterSpaceCount;

        // set word space adjustment
        //
        if (wordSpaceCount > 0) {
            wordSpaceDim += (difference - totalAdjust) / wordSpaceCount;
        }
        totalAdjust += (wordSpaceDim - this.wordSpaceIPD.opt) * wordSpaceCount;
        if (totalAdjust != difference) {
            // the applied adjustment is greater or smaller than the needed one
            TextLayoutManager.LOG
                    .trace("TextLM.addAreas: error in word / letter space adjustment = "
                            + (totalAdjust - difference));
            // set totalAdjust = difference, so that the width of the TextArea
            // will counterbalance the error and the other inline areas will be
            // placed correctly
            totalAdjust = difference;
        }

        final TextArea t = this.createTextArea(realWidth, totalAdjust, context,
                this.wordSpaceIPD.opt - this.spaceCharIPD, firstAreaInfoIndex,
                lastAreaInfoIndex, context.isLastArea(), ai.font);

        // wordSpaceDim is computed in relation to wordSpaceIPD.opt
        // but the renderer needs to know the adjustment in relation
        // to the size of the space character in the current font;
        // moreover, the pdf renderer adds the character spacing even to
        // the last character of a word and to space characters: in order
        // to avoid this, we must subtract the letter space width twice;
        // the renderer will compute the space width as:
        //   space width =
        //     = "normal" space width + letterSpaceAdjust + wordSpaceAdjust
        //     = spaceCharIPD + letterSpaceAdjust +
        //       + (wordSpaceDim - spaceCharIPD -  2 * letterSpaceAdjust)
        //     = wordSpaceDim - letterSpaceAdjust
        t.setTextLetterSpaceAdjust(letterSpaceDim);
        t.setTextWordSpaceAdjust(wordSpaceDim - this.spaceCharIPD
                                 - 2 * t.getTextLetterSpaceAdjust());
        if (context.getIPDAdjust() != 0) {
            // add information about space width
            t.setSpaceDifference(this.wordSpaceIPD.opt - this.spaceCharIPD
                                 - 2 * t.getTextLetterSpaceAdjust());
        }
        this.parentLM.addChildArea(t);
    }

    /**
     * Create an inline word area.
     * This creates a TextArea and sets up the various attributes.
     *
     * @param width the MinOptMax width of the content
     * @param adjust the total ipd adjustment with respect to the optimal width
     * @param context the layout context
     * @param spaceDiff unused
     * @param firstIndex the index of the first AreaInfo used for the TextArea
     * @param lastIndex the index of the last AreaInfo used for the TextArea
     * @param isLastArea is this TextArea the last in a line?
     * @param font Font to be used in this particular TextArea
     * @return the new text area
     */
    protected TextArea createTextArea(final MinOptMax width, final int adjust,
            final LayoutContext context, final int spaceDiff, final int firstIndex,
            final int lastIndex, final boolean isLastArea, final Font font) {
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
        textArea.setBPD(font.getAscender() - font.getDescender());

        textArea.setBaselineOffset(font.getAscender());
        if (textArea.getBPD() == this.alignmentContext.getHeight()) {
            textArea.setOffset(0);
        } else {
            textArea.setOffset(this.alignmentContext.getOffset());
        }

        // set the text of the TextArea, split into words and spaces
        int wordStartIndex = -1;
        AreaInfo areaInfo;
        int len = 0;
        for (int i = firstIndex; i <= lastIndex; i++) {
            areaInfo = (AreaInfo) this.vecAreaInfo.get(i);
            if (areaInfo.isSpace) {
                // areaInfo stores information about spaces
                // add the spaces - except zero-width spaces - to the TextArea
                for (int j = areaInfo.startIndex; j < areaInfo.breakIndex; j++) {
                    final char spaceChar = this.foText.charAt(j);
                    if (!CharUtilities.isZeroWidthSpace(spaceChar)) {
                        textArea.addSpace(spaceChar, 0,
                                CharUtilities.isAdjustableSpace(spaceChar));
                    }
                }
            } else {
                // areaInfo stores information about a word fragment
                if (wordStartIndex == -1) {
                    // here starts a new word
                    wordStartIndex = i;
                    len = 0;
                }
                len += areaInfo.breakIndex - areaInfo.startIndex;
                if (i == lastIndex || ((AreaInfo) this.vecAreaInfo.get(i + 1)).isSpace) {
                    // here ends a new word
                    // add a word to the TextArea
                    if (isLastArea
                        && i == lastIndex
                        && areaInfo.isHyphenated) {
                        len++;
                    }
                    final StringBuffer wordChars = new StringBuffer(len);
                    final int[] letterAdjust = new int[len];
                    int letter = 0;
                    for (int j = wordStartIndex; j <= i; j++) {
                        final AreaInfo ai = (AreaInfo) this.vecAreaInfo.get(j);
                        int lsCount = ai.letterSpaceCount;
                        /* TODO: in Java 5, StringBuffer has an append() variant
                         * for CharSequence, so the below iteration can be replaced
                         * by:
                         *  wordChars.append(this.foText, ai.startIndex,
                         *       ai.breakIndex - ai.startIndex);
                         */
                        for (int ci = ai.startIndex; ci < ai.breakIndex; ci++) {
                            wordChars.append(this.foText.charAt(ci));
                        }
                        for (int k = 0; k < ai.breakIndex - ai.startIndex; k++) {
                            final MinOptMax adj = this.letterAdjustArray[ai.startIndex + k];
                            if (letter > 0) {
                                letterAdjust[letter] = adj == null ? 0
                                        : adj.opt;
                            }
                            if (lsCount > 0) {
                                letterAdjust[letter] += textArea.getTextLetterSpaceAdjust();
                                lsCount--;
                            }
                            letter++;
                        }
                    }
                    // String wordChars = new String(textArray, wordStartIndex, len);
                    if (isLastArea
                        && i == lastIndex
                        && areaInfo.isHyphenated) {
                        // add the hyphenation character
                        wordChars.append(this.foText.getCommonHyphenation().getHyphChar(font));
                    }
                    textArea.addWord(wordChars.toString(), 0, letterAdjust);
                    wordStartIndex = -1;
                }
            }
        }
        TraitSetter.addFontTraits(textArea, font);
        textArea.addTrait(Trait.COLOR, this.foText.getColor());
        TraitSetter.addPtr(textArea, getPtr()); // used for accessibility
        TraitSetter.addTextDecoration(textArea, this.foText.getTextDecoration());

        return textArea;
    }

    /**
     * used for accessibility
     * @return ptr of fobj
     */
    private String getPtr() {
        FObj fobj = this.parentLM.getFObj();
        if (fobj instanceof org.apache.fop.fo.flow.Block) {
            return (((org.apache.fop.fo.flow.Block) fobj).getPtr());
        } else if (fobj instanceof org.apache.fop.fo.flow.Inline) {
            return (((org.apache.fop.fo.flow.Inline) fobj).getPtr());
        } else {
            log.warn("Accessibility: TLM.getPtr-no Ptr found");
            return "";
        }
    }

    private void addToLetterAdjust(final int index, final int width) {
        if (this.letterAdjustArray[index] == null) {
            this.letterAdjustArray[index] = new MinOptMax(width);
        } else {
            this.letterAdjustArray[index].add(width);
        }
    }

    /**
     * Indicates whether a character is a space in terms of this layout manager.
     * @param ch the character
     * @return true if it's a space
     */
    private static boolean isSpace(final char ch) {
        return ch == CharUtilities.SPACE
            || CharUtilities.isNonBreakableSpace(ch)
            || CharUtilities.isFixedWidthSpace(ch);
    }

    /** {@inheritDoc} */
    public List getNextKnuthElements(final LayoutContext context, final int alignment) {
        this.lineStartBAP = context.getLineStartBorderAndPaddingWidth();
        this.lineEndBAP = context.getLineEndBorderAndPaddingWidth();
        this.alignmentContext = context.getAlignmentContext();

        final List returnList = new LinkedList();
        KnuthSequence sequence = new InlineKnuthSequence();
        AreaInfo ai = null;
        AreaInfo prevAi = null;
        returnList.add(sequence);

        final LineBreakStatus lbs = new LineBreakStatus();
        this.thisStart = this.nextStart;
        boolean inWord = false;
        boolean inWhitespace = false;
        char ch = 0;
        while (this.nextStart < this.foText.length()) {
            ch = this.foText.charAt(this.nextStart);
            boolean breakOpportunity = false;
            final byte breakAction = this.keepTogether ? LineBreakStatus.PROHIBITED_BREAK
                    : lbs.nextChar(ch);
            switch (breakAction) {
                case LineBreakStatus.COMBINING_PROHIBITED_BREAK:
                case LineBreakStatus.PROHIBITED_BREAK:
                    break;
                case LineBreakStatus.EXPLICIT_BREAK:
                    break;
                case LineBreakStatus.COMBINING_INDIRECT_BREAK:
                case LineBreakStatus.DIRECT_BREAK:
                case LineBreakStatus.INDIRECT_BREAK:
                    breakOpportunity = true;
                    break;
                default:
                    TextLayoutManager.LOG.error("Unexpected breakAction: " + breakAction);
            }
            if (inWord) {
                if (breakOpportunity
                        || TextLayoutManager.isSpace(ch)
                        || CharUtilities.isExplicitBreak(ch)) {
                    // this.foText.charAt(lastIndex) == CharUtilities.SOFT_HYPHEN
                    prevAi = this.processWord(alignment, sequence, prevAi, ch,
                            breakOpportunity, true);
                }
            } else if (inWhitespace) {
                if (ch != CharUtilities.SPACE || breakOpportunity) {
                    prevAi = this.processWhitespace(alignment, sequence,
                            breakOpportunity);
                }
            } else {
                if (ai != null) {
                    prevAi = ai;
                    ai = this.processLeftoverAi(alignment, sequence, ai, ch,
                            ch == CharUtilities.SPACE || breakOpportunity);
                }
                if (breakAction == LineBreakStatus.EXPLICIT_BREAK) {
                    sequence = this.processLinebreak(returnList, sequence);
                }
            }

            if (ch == CharUtilities.SPACE
                    && this.foText.getWhitespaceTreatment() == Constants.EN_PRESERVE
                    || ch == CharUtilities.NBSPACE) {
                // preserved space or non-breaking space:
                // create the AreaInfo object
                ai = new AreaInfo(this.nextStart, this.nextStart + 1,
                        1, 0, this.wordSpaceIPD, false, true,
                        breakOpportunity, this.spaceFont);
                this.thisStart = this.nextStart + 1;
            } else if (CharUtilities.isFixedWidthSpace(ch) || CharUtilities.isZeroWidthSpace(ch)) {
                // create the AreaInfo object
                final Font font = FontSelector.selectFontForCharacterInText(ch,
                        this.foText, this);
                final MinOptMax ipd = new MinOptMax(font.getCharWidth(ch));
                ai = new AreaInfo(this.nextStart, this.nextStart + 1,
                        0, 0, ipd, false, true,
                        breakOpportunity, font);
                this.thisStart = this.nextStart + 1;
            } else if (CharUtilities.isExplicitBreak(ch)) {
                //mandatory break-character: only advance index
                this.thisStart = this.nextStart + 1;
            }

            inWord = !TextLayoutManager.isSpace(ch)
                    && !CharUtilities.isExplicitBreak(ch);
            inWhitespace = ch == CharUtilities.SPACE
                    && this.foText.getWhitespaceTreatment() != Constants.EN_PRESERVE;
            this.nextStart++;
        } // end of while

        // Process any last elements
        if (inWord) {
            this.processWord(alignment, sequence, prevAi, ch, false, false);
        } else if (inWhitespace) {
            this.processWhitespace(alignment, sequence, true);
        } else if (ai != null) {
            this.processLeftoverAi(alignment, sequence, ai, ch,
                    ch == CharUtilities.ZERO_WIDTH_SPACE);
        } else if (CharUtilities.isExplicitBreak(ch)) {
            this.processLinebreak(returnList, sequence);
        }

        if (((List) ListUtil.getLast(returnList)).isEmpty()) {
            //Remove an empty sequence because of a trailing newline
            ListUtil.removeLast(returnList);
        }

        this.setFinished(true);
        if (returnList.isEmpty()) {
            return null;
        } else {
            return returnList;
        }
    }

    private KnuthSequence processLinebreak(final List returnList,
            KnuthSequence sequence) {
        if (this.lineEndBAP != 0) {
            sequence.add(
                    new KnuthGlue(this.lineEndBAP, 0, 0,
                                  this.auxiliaryPosition, true));
        }
        sequence.endSequence();
        sequence = new InlineKnuthSequence();
        returnList.add(sequence);
        return sequence;
    }

    private AreaInfo processLeftoverAi(final int alignment,
            final KnuthSequence sequence, AreaInfo ai, final char ch,
            final boolean breakOpportunityAfter) {
        this.vecAreaInfo.add(ai);
        ai.breakOppAfter = breakOpportunityAfter;
        this.addElementsForASpace(sequence, alignment, ai, this.vecAreaInfo.size() - 1);
        ai = null;
        return ai;
    }

    private AreaInfo processWhitespace(final int alignment,
            final KnuthSequence sequence, final boolean breakOpportunity) {
        // End of whitespace
        // create the AreaInfo object
        AreaInfo ai = new AreaInfo(this.thisStart, this.nextStart,
                this.nextStart - this.thisStart, 0,
                MinOptMax.multiply(this.wordSpaceIPD, this.nextStart
                        - this.thisStart), false, true,
                breakOpportunity, this.spaceFont);
        this.vecAreaInfo.add(ai);

        // create the elements
        this.addElementsForASpace(sequence, alignment, ai, this.vecAreaInfo.size() - 1);

        this.thisStart = this.nextStart;
        return ai;
    }

    private AreaInfo processWord(final int alignment, final KnuthSequence sequence,
            AreaInfo prevAi, final char ch, final boolean breakOpportunity,
            final boolean checkEndsWithHyphen) {
        AreaInfo ai;
        //Word boundary found, process widths and kerning
        int lastIndex = this.nextStart;
        while (lastIndex > 0
                && this.foText.charAt(lastIndex - 1) == CharUtilities.SOFT_HYPHEN) {
            lastIndex--;
        }
        final boolean endsWithHyphen = checkEndsWithHyphen
                && this.foText.charAt(lastIndex) == CharUtilities.SOFT_HYPHEN;
        final Font font = FontSelector
                .selectFontForCharactersInText(this.foText,
                        this.thisStart, lastIndex, this.foText, this);
        final int wordLength = lastIndex - this.thisStart;
        final boolean kerning = font.hasKerning();
        final MinOptMax wordIPD = new MinOptMax(0);
        for (int i = this.thisStart; i < lastIndex; i++) {
            final char c = this.foText.charAt(i);

            //character width
            final int charWidth = font.getCharWidth(c);
            wordIPD.add(charWidth);

            //kerning
            if (kerning) {
                int kern = 0;
                if (i > this.thisStart) {
                    final char previous = this.foText.charAt(i - 1);
                    kern = font.getKernValue(previous, c) * font.getFontSize() / 1000;
                } else if (prevAi != null && !prevAi.isSpace && prevAi.breakIndex > 0) {
                    final char previous = this.foText.charAt(prevAi.breakIndex - 1);
                    kern = font.getKernValue(previous, c) * font.getFontSize() / 1000;
                }
                if (kern != 0) {
                    this.addToLetterAdjust(i, kern);
                    wordIPD.add(kern);
                }
            }
        }
        if (kerning
                && breakOpportunity
                && !TextLayoutManager.isSpace(ch)
                && lastIndex > 0
                && endsWithHyphen) {
            final int kern = font.getKernValue(
                    this.foText.charAt(lastIndex - 1), ch)
                    * font.getFontSize() / 1000;
            if (kern != 0) {
                this.addToLetterAdjust(lastIndex, kern);
            }
        }
        int iLetterSpaces = wordLength - 1;
        // if there is a break opportunity and the next one
        // is not a space, it could be used as a line end;
        // add one more letter space, in case other text follows
        if (breakOpportunity && !TextLayoutManager.isSpace(ch)) {
            iLetterSpaces++;
        }
        wordIPD.add(MinOptMax.multiply(this.letterSpaceIPD, iLetterSpaces));

        // create the AreaInfo object
        ai = new AreaInfo(this.thisStart, lastIndex, 0,
                iLetterSpaces, wordIPD,
                endsWithHyphen,
                false, breakOpportunity, font);
        prevAi = ai;
        this.vecAreaInfo.add(ai);
        this.tempStart = this.nextStart;

        //add the elements
        this.addElementsForAWordFragment(sequence, alignment, ai,
                this.vecAreaInfo.size() - 1, this.letterSpaceIPD);
        ai = null;
        this.thisStart = this.nextStart;
        return prevAi;
    }

    /** {@inheritDoc} */
    public List addALetterSpaceTo(final List oldList) {
        // old list contains only a box, or the sequence: box penalty glue box;
        // look at the Position stored in the first element in oldList
        // which is always a box
        ListIterator oldListIterator = oldList.listIterator();
        final KnuthElement el = (KnuthElement)oldListIterator.next();
        final LeafPosition pos = (LeafPosition) ((KnuthBox) el).getPosition();
        final int idx = pos.getLeafPos();
        //element could refer to '-1' position, for non-collapsed spaces (?)
        if (idx > -1) {
            final AreaInfo ai = (AreaInfo) this.vecAreaInfo.get(idx);
            ai.letterSpaceCount++;
            ai.areaIPD.add(this.letterSpaceIPD);
            if (TextLayoutManager.BREAK_CHARS.indexOf(this.foText.charAt(this.tempStart - 1)) >= 0) {
                // the last character could be used as a line break
                // append new elements to oldList
                oldListIterator = oldList.listIterator(oldList.size());
                oldListIterator.add(new KnuthPenalty(0, KnuthPenalty.FLAGGED_PENALTY, true,
                                                     this.auxiliaryPosition, false));
                oldListIterator.add(new KnuthGlue(this.letterSpaceIPD.opt,
                                           this.letterSpaceIPD.max - this.letterSpaceIPD.opt,
                                           this.letterSpaceIPD.opt - this.letterSpaceIPD.min,
                                           this.auxiliaryPosition, false));
            } else if (this.letterSpaceIPD.min == this.letterSpaceIPD.max) {
                // constant letter space: replace the box
                oldListIterator.set(new KnuthInlineBox(ai.areaIPD.opt,
                        this.alignmentContext, pos, false));
            } else {
                // adjustable letter space: replace the glue
                oldListIterator.next(); // this would return the penalty element
                oldListIterator.next(); // this would return the glue element
                oldListIterator
                        .set(new KnuthGlue(
                                ai.letterSpaceCount * this.letterSpaceIPD.opt,
                                ai.letterSpaceCount
                                        * (this.letterSpaceIPD.max - this.letterSpaceIPD.opt),
                                ai.letterSpaceCount
                                        * (this.letterSpaceIPD.opt - this.letterSpaceIPD.min),
                                this.auxiliaryPosition, true));
            }
        }
        return oldList;
    }

    /**
     * remove the AreaInfo object represented by the given elements,
     * so that it won't generate any element when getChangedKnuthElements
     * will be called
     *
     * @param oldList the elements representing the word space
     */
    public void removeWordSpace(final List oldList) {
        // find the element storing the Position whose value
        // points to the AreaInfo object
        final ListIterator oldListIterator = oldList.listIterator();
        if (((KnuthElement) ((LinkedList) oldList).getFirst()).isPenalty()) {
            // non breaking space: oldList starts with a penalty
            oldListIterator.next();
        }
        if (oldList.size() > 2) {
            // alignment is either center, start or end:
            // the first two elements does not store the needed Position
            oldListIterator.next();
            oldListIterator.next();
        }
        final int leafValue = ((LeafPosition) ((KnuthElement) oldListIterator
                .next()).getPosition()).getLeafPos();
        // only the last word space can be a trailing space!
        if (leafValue == this.vecAreaInfo.size() - 1) {
            this.vecAreaInfo.remove(leafValue);
        } else {
            TextLayoutManager.LOG.error("trying to remove a non-trailing word space");
        }
    }

    /** {@inheritDoc} */
    public void hyphenate(final Position pos, final HyphContext hc) {
        final AreaInfo ai
            = (AreaInfo) this.vecAreaInfo.get(((LeafPosition) pos).getLeafPos());
        int startIndex = ai.startIndex;
        int stopIndex;
        boolean nothingChanged = true;
        final Font font = ai.font;

        while (startIndex < ai.breakIndex) {
            final MinOptMax newIPD = new MinOptMax(0);
            boolean hyphenFollows;

            stopIndex = startIndex + hc.getNextHyphPoint();
            if (hc.hasMoreHyphPoints() && stopIndex <= ai.breakIndex) {
                // stopIndex is the index of the first character
                // after a hyphenation point
                hyphenFollows = true;
            } else {
                // there are no more hyphenation points,
                // or the next one is after ai.breakIndex
                hyphenFollows = false;
                stopIndex = ai.breakIndex;
            }

            hc.updateOffset(stopIndex - startIndex);

            //log.info("Word: " + new String(textArray, startIndex, stopIndex - startIndex));
            for (int i = startIndex; i < stopIndex; i++) {
                final char c = this.foText.charAt(i);
                newIPD.add(new MinOptMax(font.getCharWidth(c)));
                //if (i > startIndex) {
                if (i < stopIndex) {
                    MinOptMax la = this.letterAdjustArray[i + 1];
                    if (i == stopIndex - 1 && hyphenFollows) {
                        //the letter adjust here needs to be handled further down during
                        //element generation because it depends on hyph/no-hyph condition
                        la = null;
                    }
                    if (la != null) {
                        newIPD.add(la);
                    }
                }
            }
            // add letter spaces
            final boolean isWordEnd
                = stopIndex == ai.breakIndex
                && ai.letterSpaceCount < ai.breakIndex - ai.startIndex;
            newIPD.add(MinOptMax.multiply(this.letterSpaceIPD,
                                          (isWordEnd
                                           ? stopIndex - startIndex - 1
                                           : stopIndex - startIndex)));

            if (!(nothingChanged
                  && stopIndex == ai.breakIndex
                  && !hyphenFollows)) {
                // the new AreaInfo object is not equal to the old one
                if (this.changeList == null) {
                    this.changeList = new LinkedList();
                }
                this.changeList.add(new PendingChange(new AreaInfo(
                        startIndex, stopIndex, 0,
                        (isWordEnd ? stopIndex - startIndex - 1
                                : stopIndex - startIndex), newIPD,
                        hyphenFollows, false, false, font),
                        ((LeafPosition) pos).getLeafPos()));
                nothingChanged = false;
            }
            startIndex = stopIndex;
        }
        this.hasChanged = (this.hasChanged || !nothingChanged);
    }

    /** {@inheritDoc} */
    public boolean applyChanges(final List oldList) {
        this.setFinished(false);

        if (this.changeList != null && !this.changeList.isEmpty()) {
            int areaInfosAdded = 0;
            int areaInfosRemoved = 0;
            int oldIndex = -1, changeIndex;
            PendingChange currChange;
            final ListIterator changeListIterator = this.changeList.listIterator();
            while (changeListIterator.hasNext()) {
                currChange = (PendingChange) changeListIterator.next();
                if (currChange.index == oldIndex) {
                    areaInfosAdded++;
                    changeIndex = currChange.index + areaInfosAdded - areaInfosRemoved;
                } else {
                    areaInfosRemoved++;
                    areaInfosAdded++;
                    oldIndex = currChange.index;
                    changeIndex = currChange.index + areaInfosAdded - areaInfosRemoved;
                    this.vecAreaInfo.remove(changeIndex);
                }
                this.vecAreaInfo.add(changeIndex, currChange.ai);
            }
            this.changeList.clear();
        }

        this.returnedIndex = 0;
        return this.hasChanged;
    }

    /** {@inheritDoc} */
    public List getChangedKnuthElements(final List oldList,
                                              final int alignment) {
        if (this.isFinished()) {
            return null;
        }

        final LinkedList returnList = new LinkedList();

        while (this.returnedIndex < this.vecAreaInfo.size()) {
            final AreaInfo ai = (AreaInfo) this.vecAreaInfo.get(this.returnedIndex);
            if (ai.wordSpaceCount == 0) {
                // ai refers either to a word or a word fragment
                this.addElementsForAWordFragment(returnList, alignment, ai,
                        this.returnedIndex, this.letterSpaceIPD);
            } else {
                // ai refers to a space
                this.addElementsForASpace(returnList, alignment, ai, this.returnedIndex);
            }
            this.returnedIndex++;
        } // end of while
        this.setFinished(true);
        //ElementListObserver.observe(returnList, "text-changed", null);
        return returnList;
    }

    /** {@inheritDoc} */
    public void getWordChars(final StringBuffer sbChars, final Position pos) {
        final int leafValue = ((LeafPosition) pos).getLeafPos();
        if (leafValue != -1) {
            final AreaInfo ai = (AreaInfo) this.vecAreaInfo.get(leafValue);
            for (int i = ai.startIndex; i < ai.breakIndex; ++i) {
                sbChars.append(this.foText.charAt(i));
            }
        }
    }

    private void addElementsForASpace(final List baseList,
                                      final int alignment,
                                      final AreaInfo ai,
                                      final int leafValue) {
        final LeafPosition mainPosition = new LeafPosition(this, leafValue);

        if (!ai.breakOppAfter) {
            // a non-breaking space
            if (alignment == Constants.EN_JUSTIFY) {
                // the space can stretch and shrink, and must be preserved
                // when starting a line
                baseList.add(this.makeAuxiliaryZeroWidthBox());
                baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(ai.areaIPD.opt, ai.areaIPD.max - ai.areaIPD.opt,
                        ai.areaIPD.opt - ai.areaIPD.min, mainPosition, false));
            } else {
                // the space does not need to stretch or shrink, and must be
                // preserved when starting a line
                baseList.add(new KnuthInlineBox(ai.areaIPD.opt, null,
                        mainPosition, true));
            }
        } else {
            if (this.foText.charAt(ai.startIndex) != CharUtilities.SPACE
                    || this.foText.getWhitespaceTreatment() == Constants.EN_PRESERVE) {
                // a breaking space that needs to be preserved
                this.addElementsForBreakingSpace(baseList, alignment, ai,
                                this.auxiliaryPosition, 0, mainPosition,
                                ai.areaIPD.opt, true);
            } else {
                // a (possible block) of breaking spaces
                this.addElementsForBreakingSpace(baseList, alignment, ai,
                                mainPosition, ai.areaIPD.opt,
                                this.auxiliaryPosition, 0, false);
            }
        }
    }

    private void addElementsForBreakingSpace(final List baseList,
            final int alignment, final AreaInfo ai, final Position pos2,
            final int p2WidthOffset, final Position pos3,
            final int p3WidthOffset, final boolean skipZeroCheck) {
        switch (alignment) {
        case EN_CENTER:
            // centered text:
            // if the second element is chosen as a line break these elements
            // add a constant amount of stretch at the end of a line and at the
            // beginning of the next one, otherwise they don't add any stretch
            baseList.add(new KnuthGlue(this.lineEndBAP,
                    3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                    this.auxiliaryPosition, false));
            baseList.add(this.makeZeroWidthPenalty(0));
            baseList.add(new KnuthGlue(p2WidthOffset
                    - (this.lineStartBAP + this.lineEndBAP), -6
                    * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0, pos2, false));
            baseList.add(this.makeAuxiliaryZeroWidthBox());
            baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
            baseList.add(new KnuthGlue(this.lineStartBAP + p3WidthOffset,
                    3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0, pos3, false));
            break;

        case EN_START: // fall through
        case EN_END:
            // left- or right-aligned text:
            // if the second element is chosen as a line break these elements
            // add a constant amount of stretch at the end of a line, otherwise
            // they don't add any stretch
            if (skipZeroCheck || this.lineStartBAP != 0 || this.lineEndBAP != 0) {
                baseList.add(new KnuthGlue(this.lineEndBAP,
                        3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        this.auxiliaryPosition, false));
                baseList.add(this.makeZeroWidthPenalty(0));
                baseList.add(new KnuthGlue(p2WidthOffset
                                - (this.lineStartBAP + this.lineEndBAP), -3
                                * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                pos2, false));
                baseList.add(this.makeAuxiliaryZeroWidthBox());
                baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(this.lineStartBAP + p3WidthOffset,
                        0, 0, pos3, false));
            } else {
                baseList.add(new KnuthGlue(0,
                        3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        this.auxiliaryPosition, false));
                baseList.add(this.makeZeroWidthPenalty(0));
                baseList.add(new KnuthGlue(ai.areaIPD.opt, -3
                                * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                pos2, false));
            }
            break;

        case EN_JUSTIFY:
            // justified text:
            // the stretch and shrink depends on the space width
            if (skipZeroCheck || this.lineStartBAP != 0 || this.lineEndBAP != 0) {
                baseList.add(new KnuthGlue(this.lineEndBAP, 0, 0,
                        this.auxiliaryPosition, false));
                baseList.add(this.makeZeroWidthPenalty(0));
                baseList.add(new KnuthGlue(p2WidthOffset
                        - (this.lineStartBAP + this.lineEndBAP), ai.areaIPD.max
                        - ai.areaIPD.opt, ai.areaIPD.opt - ai.areaIPD.min,
                        pos2, false));
                baseList.add(this.makeAuxiliaryZeroWidthBox());
                baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(this.lineStartBAP + p3WidthOffset,
                        0, 0, pos3, false));
            } else {
                baseList.add(new KnuthGlue(ai.areaIPD.opt, ai.areaIPD.max
                        - ai.areaIPD.opt, ai.areaIPD.opt - ai.areaIPD.min,
                        pos2, false));
            }
            break;

        default:
            // last line justified, the other lines unjustified:
            // use only the space stretch
            if (skipZeroCheck || this.lineStartBAP != 0 || this.lineEndBAP != 0) {
                baseList.add(new KnuthGlue(this.lineEndBAP, 0, 0,
                        this.auxiliaryPosition, false));
                baseList.add(this.makeZeroWidthPenalty(0));
                baseList.add(new KnuthGlue(p2WidthOffset
                        - (this.lineStartBAP + this.lineEndBAP), ai.areaIPD.max
                        - ai.areaIPD.opt, 0, pos2, false));
                baseList.add(this.makeAuxiliaryZeroWidthBox());
                baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(this.lineStartBAP + p3WidthOffset,
                        0, 0, pos3, false));
            } else {
                baseList.add(new KnuthGlue(ai.areaIPD.opt, ai.areaIPD.max
                        - ai.areaIPD.opt, 0, pos2, false));
            }
        }
    }

    private void addElementsForAWordFragment(final List baseList,
                                             final int alignment,
                                             final AreaInfo ai,
                                             final int leafValue,
                                             final MinOptMax letterSpaceWidth) {

        final LeafPosition mainPosition = new LeafPosition(this, leafValue);

        // if the last character of the word fragment is '-' or '/',
        // the fragment could end a line; in this case, it loses one
        // of its letter spaces;
        final boolean suppressibleLetterSpace = ai.breakOppAfter && !ai.isHyphenated;

        if (letterSpaceWidth.min == letterSpaceWidth.max) {
            // constant letter spacing
            baseList.add(new KnuthInlineBox(
                        suppressibleLetterSpace
                                ? ai.areaIPD.opt - letterSpaceWidth.opt
                                : ai.areaIPD.opt,
                        this.alignmentContext,
                        this.notifyPos(mainPosition), false));
        } else {
            // adjustable letter spacing
            final int unsuppressibleLetterSpaces
                = suppressibleLetterSpace ? ai.letterSpaceCount - 1 : ai.letterSpaceCount;
            baseList.add
                (new KnuthInlineBox(ai.areaIPD.opt
                        - ai.letterSpaceCount * letterSpaceWidth.opt,
                        this.alignmentContext,
                        this.notifyPos(mainPosition), false));
            baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
            baseList.add
                (new KnuthGlue(unsuppressibleLetterSpaces * letterSpaceWidth.opt,
                        unsuppressibleLetterSpaces * (letterSpaceWidth.max - letterSpaceWidth.opt),
                        unsuppressibleLetterSpaces * (letterSpaceWidth.opt - letterSpaceWidth.min),
                        this.auxiliaryPosition, true));
            baseList.add(this.makeAuxiliaryZeroWidthBox());
        }

        // extra-elements if the word fragment is the end of a syllable,
        // or it ends with a character that can be used as a line break
        if (ai.isHyphenated) {
            MinOptMax widthIfNoBreakOccurs = null;
            if (ai.breakIndex < this.foText.length()) {
                //Add in kerning in no-break condition
                widthIfNoBreakOccurs = this.letterAdjustArray[ai.breakIndex];
            }
            //if (ai.breakIndex)

            // the word fragment ends at the end of a syllable:
            // if a break occurs the content width increases,
            // otherwise nothing happens
            this.addElementsForAHyphen(baseList, alignment, this.hyphIPD,
                    widthIfNoBreakOccurs, ai.breakOppAfter && ai.isHyphenated);
        } else if (suppressibleLetterSpace) {
            // the word fragment ends with a character that acts as a hyphen
            // if a break occurs the width does not increase,
            // otherwise there is one more letter space
            this.addElementsForAHyphen(baseList, alignment, 0, letterSpaceWidth, true);
        }
    }

    // static final int SOFT_HYPHEN_PENALTY = KnuthPenalty.FLAGGED_PENALTY / 10;
    private static final int SOFT_HYPHEN_PENALTY = 1;

    private void addElementsForAHyphen(final List baseList,
                                       final int alignment,
                                       final int widthIfBreakOccurs,
                                       MinOptMax widthIfNoBreakOccurs,
                                       final boolean unflagged) {
        if (widthIfNoBreakOccurs == null) {
            widthIfNoBreakOccurs = TextLayoutManager.ZERO_MINOPTMAX;
        }

        switch (alignment) {
        case EN_CENTER :
            // centered text:
            baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
            baseList.add(new KnuthGlue(this.lineEndBAP,
                    3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                    this.auxiliaryPosition, true));
            baseList.add(new KnuthPenalty(this.hyphIPD,
                    unflagged ? TextLayoutManager.SOFT_HYPHEN_PENALTY
                            : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                    this.auxiliaryPosition, false));
            baseList.add(new KnuthGlue(-(this.lineEndBAP + this.lineStartBAP),
                    -6 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                    this.auxiliaryPosition, false));
            baseList.add(this.makeAuxiliaryZeroWidthBox());
            baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
            baseList.add(new KnuthGlue(this.lineStartBAP,
                    3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                    this.auxiliaryPosition, true));
            break;

        case EN_START  : // fall through
        case EN_END    :
            // left- or right-aligned text:
            if (this.lineStartBAP != 0 || this.lineEndBAP != 0) {
                baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(this.lineEndBAP,
                        3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        this.auxiliaryPosition, false));
                baseList.add(new KnuthPenalty(widthIfBreakOccurs,
                        unflagged ? TextLayoutManager.SOFT_HYPHEN_PENALTY
                                : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                        this.auxiliaryPosition, false));
                baseList.add(new KnuthGlue(widthIfNoBreakOccurs.opt
                        - (this.lineStartBAP + this.lineEndBAP), -3
                        * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        this.auxiliaryPosition, false));
                baseList.add(this.makeAuxiliaryZeroWidthBox());
                baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(this.lineStartBAP, 0, 0,
                                   this.auxiliaryPosition, false));
            } else {
                baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            this.auxiliaryPosition, false));
                baseList.add(new KnuthPenalty(widthIfBreakOccurs,
                        unflagged ? TextLayoutManager.SOFT_HYPHEN_PENALTY
                                : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                        this.auxiliaryPosition, false));
                baseList.add(new KnuthGlue(widthIfNoBreakOccurs.opt,
                            -3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            this.auxiliaryPosition, false));
            }
            break;

        default:
            // justified text, or last line justified:
            // just a flagged penalty
            if (this.lineStartBAP != 0 || this.lineEndBAP != 0) {
                baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(this.lineEndBAP, 0, 0,
                                   this.auxiliaryPosition, false));
                baseList.add(new KnuthPenalty(widthIfBreakOccurs,
                        unflagged ? TextLayoutManager.SOFT_HYPHEN_PENALTY
                                : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                        this.auxiliaryPosition, false));
                // extra elements representing a letter space that is suppressed
                // if a break occurs
                if (widthIfNoBreakOccurs.min != 0
                    || widthIfNoBreakOccurs.max != 0) {
                    baseList
                            .add(new KnuthGlue(widthIfNoBreakOccurs.opt
                                    - (this.lineStartBAP + this.lineEndBAP),
                                    widthIfNoBreakOccurs.max
                                            - widthIfNoBreakOccurs.opt,
                                    widthIfNoBreakOccurs.opt
                                            - widthIfNoBreakOccurs.min,
                                    this.auxiliaryPosition, false));
                } else {
                    baseList.add(new KnuthGlue(-(this.lineStartBAP + this.lineEndBAP), 0, 0,
                                       this.auxiliaryPosition, false));
                }
                baseList.add(this.makeAuxiliaryZeroWidthBox());
                baseList.add(this.makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(this.lineStartBAP, 0, 0,
                                   this.auxiliaryPosition, false));
            } else {
                baseList.add(new KnuthPenalty(widthIfBreakOccurs,
                        unflagged ? TextLayoutManager.SOFT_HYPHEN_PENALTY
                                : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                        this.auxiliaryPosition, false));
                // extra elements representing a letter space that is suppressed
                // if a break occurs
                if (widthIfNoBreakOccurs.min != 0
                    || widthIfNoBreakOccurs.max != 0) {
                    baseList.add(new KnuthGlue(widthIfNoBreakOccurs.opt,
                                widthIfNoBreakOccurs.max - widthIfNoBreakOccurs.opt,
                                widthIfNoBreakOccurs.opt - widthIfNoBreakOccurs.min,
                                this.auxiliaryPosition, false));
                }
            }
        }

    }

}
