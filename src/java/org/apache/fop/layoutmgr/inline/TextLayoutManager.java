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

import java.util.ArrayList;
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
import org.apache.fop.fo.properties.StructurePointerPropertySet;
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

    //TODO: remove all final modifiers at local variables

    // static final int SOFT_HYPHEN_PENALTY = KnuthPenalty.FLAGGED_PENALTY / 10;
    private static final int SOFT_HYPHEN_PENALTY = 1;

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
        private MinOptMax areaIPD;
        private final boolean isHyphenated;
        private final boolean isSpace;
        private boolean breakOppAfter;
        private final Font font;

        AreaInfo(                                                // CSOK: ParameterNumber
                int startIndex, int breakIndex, int wordSpaceCount, int letterSpaceCount,
                MinOptMax areaIPD, boolean isHyphenated, boolean isSpace, boolean breakOppAfter,
                Font font) {
            assert startIndex <= breakIndex;
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

        private int getCharLength() {
            return breakIndex - startIndex;
        }

        private void addToAreaIPD(MinOptMax idp) {
            areaIPD = areaIPD.plus(idp);
        }

        public String toString() {
            return "AreaInfo["
                    + "letterSpaceCount = " + letterSpaceCount
                    + ", wordSpaceCount = " + wordSpaceCount
                    + ", areaIPD = " + areaIPD
                    + ", startIndex = " + startIndex
                    + ", breakIndex = " + breakIndex
                    + ", isHyphenated = " + isHyphenated
                    + ", isSpace = " + isSpace
                    + ", font = " + font
                    + "]";
        }
    }

    /**
     * this class stores information about changes in vecAreaInfo which are not yet applied
     */
    private final class PendingChange {

        private final AreaInfo areaInfo;
        private final int index;

        private PendingChange(final AreaInfo areaInfo, final int index) {
            this.areaInfo = areaInfo;
            this.index = index;
        }
    }

    /**
     * logging instance
     */
    private static final Log LOG = LogFactory.getLog(TextLayoutManager.class);

    // Hold all possible breaks for the text in this LM's FO.
    private final List areaInfos;

    /** Non-space characters on which we can end a line. */
    private static final String BREAK_CHARS = "-/";

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

    private boolean hasChanged = false;
    private int returnedIndex = 0;
    private int thisStart = 0;
    private int tempStart = 0;
    private List changeList = new LinkedList();

    private AlignmentContext alignmentContext = null;

    /**
     * The width to be reserved for border and padding at the start of the line.
     */
    private int lineStartBAP = 0;

    /**
     * The width to be reserved for border and padding at the end of the line.
     */
    private int lineEndBAP = 0;

    private boolean keepTogether;

    private final Position auxiliaryPosition = new LeafPosition(this, -1);

    /**
     * Create a Text layout manager.
     *
     * @param node The FOText object to be rendered
     */
    public TextLayoutManager(FOText node) {
        foText = node;
        letterAdjustArray = new MinOptMax[node.length() + 1];
        areaInfos = new ArrayList();
    }

    private KnuthPenalty makeZeroWidthPenalty(int penaltyValue) {
        return new KnuthPenalty(0, penaltyValue, false, auxiliaryPosition, true);
    }

    private KnuthBox makeAuxiliaryZeroWidthBox() {
        return new KnuthInlineBox(0, null, notifyPos(new LeafPosition(this, -1)), true);
    }

    /** {@inheritDoc} */
    public void initialize() {

        foText.resetBuffer();

        spaceFont = FontSelector.selectFontForCharacterInText(' ', foText, this);

        // With CID fonts, space isn't necessary currentFontState.width(32)
        spaceCharIPD = spaceFont.getCharWidth(' ');

        // Use hyphenationChar property
        // TODO: Use hyphen based on actual font used!
        hyphIPD = foText.getCommonHyphenation().getHyphIPD(spaceFont);

        SpaceVal letterSpacing = SpaceVal.makeLetterSpacing(foText.getLetterSpacing());
        SpaceVal wordSpacing = SpaceVal.makeWordSpacing(foText.getWordSpacing(), letterSpacing,
                spaceFont);

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
        letterSpaceIPD = letterSpacing.getSpace();
        wordSpaceIPD = MinOptMax.getInstance(spaceCharIPD).plus(wordSpacing.getSpace());
        keepTogether = foText.getKeepTogether().getWithinLine().getEnum() == Constants.EN_ALWAYS;
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
        AreaInfo areaInfo;
        int wordSpaceCount = 0;
        int letterSpaceCount = 0;
        int firstAreaInfoIndex = -1;
        int lastAreaInfoIndex = 0;
        MinOptMax realWidth = MinOptMax.ZERO;

        /* On first area created, add any leading space.
         * Calculate word-space stretch value.
         */
        AreaInfo lastAreaInfo = null;
        while (posIter.hasNext()) {
            final LeafPosition tbpNext = (LeafPosition) posIter.next();
            if (tbpNext == null) {
                continue; //Ignore elements without Positions
            }
            if (tbpNext.getLeafPos() != -1) {
                areaInfo = (AreaInfo) areaInfos.get(tbpNext.getLeafPos());
                if (lastAreaInfo == null || areaInfo.font != lastAreaInfo.font) {
                    if (lastAreaInfo != null) {
                        addAreaInfoAreas(lastAreaInfo, wordSpaceCount,
                                letterSpaceCount, firstAreaInfoIndex,
                                lastAreaInfoIndex, realWidth, context);
                    }
                    firstAreaInfoIndex = tbpNext.getLeafPos();
                    wordSpaceCount = 0;
                    letterSpaceCount = 0;
                    realWidth = MinOptMax.ZERO;
                }
                wordSpaceCount += areaInfo.wordSpaceCount;
                letterSpaceCount += areaInfo.letterSpaceCount;
                realWidth = realWidth.plus(areaInfo.areaIPD);
                lastAreaInfoIndex = tbpNext.getLeafPos();
                lastAreaInfo = areaInfo;
            }
        }
        if (lastAreaInfo != null) {
            addAreaInfoAreas(lastAreaInfo, wordSpaceCount, letterSpaceCount, firstAreaInfoIndex,
                    lastAreaInfoIndex, realWidth, context);
        }
    }

    private void addAreaInfoAreas(AreaInfo areaInfo, int wordSpaceCount, int letterSpaceCount,
                                  int firstAreaInfoIndex, int lastAreaInfoIndex,
                                  MinOptMax realWidth, LayoutContext context) {

        // TODO: These two statements (if, for) were like this before my recent
        // changes. However, it seems as if they should use the AreaInfo from
        // firstAreaInfoIndex.. lastAreaInfoIndex rather than just the last areaInfo.
        // This needs to be checked.
        int textLength = areaInfo.getCharLength();
        if (areaInfo.letterSpaceCount == textLength && !areaInfo.isHyphenated
                && context.isLastArea()) {
            // the line ends at a character like "/" or "-";
            // remove the letter space after the last character
            realWidth = realWidth.minus(letterSpaceIPD);
            letterSpaceCount--;
        }

        for (int i = areaInfo.startIndex; i < areaInfo.breakIndex; i++) {
            MinOptMax letterAdjustment = letterAdjustArray[i + 1];
            if (letterAdjustment != null && letterAdjustment.isElastic()) {
                letterSpaceCount++;
            }
        }

        // add hyphenation character if the last word is hyphenated
        if (context.isLastArea() && areaInfo.isHyphenated) {
            realWidth = realWidth.plus(hyphIPD);
        }

        /* Calculate adjustments */
        double ipdAdjust = context.getIPDAdjust();

        // calculate total difference between real and available width
        int difference;
        if (ipdAdjust > 0.0) {
            difference = (int) (realWidth.getStretch() * ipdAdjust);
        } else {
            difference = (int) (realWidth.getShrink() * ipdAdjust);
        }

        // set letter space adjustment
        int letterSpaceDim = letterSpaceIPD.getOpt();
        if (ipdAdjust > 0.0) {
            letterSpaceDim += (int) (letterSpaceIPD.getStretch() * ipdAdjust);
        } else {
            letterSpaceDim += (int) (letterSpaceIPD.getShrink() * ipdAdjust);
        }
        int totalAdjust = (letterSpaceDim - letterSpaceIPD.getOpt()) * letterSpaceCount;

        // set word space adjustment
        int wordSpaceDim = wordSpaceIPD.getOpt();
        if (wordSpaceCount > 0) {
            wordSpaceDim += (difference - totalAdjust) / wordSpaceCount;
        }
        totalAdjust += (wordSpaceDim - wordSpaceIPD.getOpt()) * wordSpaceCount;
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

        TextArea textArea = new TextAreaBuilder(realWidth, totalAdjust, context, firstAreaInfoIndex,
                lastAreaInfoIndex, context.isLastArea(), areaInfo.font).build();

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
        textArea.setTextLetterSpaceAdjust(letterSpaceDim);
        textArea.setTextWordSpaceAdjust(wordSpaceDim - spaceCharIPD
                - 2 * textArea.getTextLetterSpaceAdjust());
        if (context.getIPDAdjust() != 0) {
            // add information about space width
            textArea.setSpaceDifference(wordSpaceIPD.getOpt() - spaceCharIPD
                    - 2 * textArea.getTextLetterSpaceAdjust());
        }
        parentLayoutManager.addChildArea(textArea);
    }

    private final class TextAreaBuilder {

        private final MinOptMax width;
        private final int adjust;
        private final LayoutContext context;
        private final int firstIndex;
        private final int lastIndex;
        private final boolean isLastArea;
        private final Font font;

        private int blockProgressionDimension;
        private AreaInfo areaInfo;
        private StringBuffer wordChars;
        private int[] letterAdjust;
        private int letterAdjustIndex;

        private TextArea textArea;

        /**
         * Creates a new <code>TextAreaBuilder</code> which itself builds an inline word area. This
         * creates a TextArea and sets up the various attributes.
         *
         * @param width      the MinOptMax width of the content
         * @param adjust     the total ipd adjustment with respect to the optimal width
         * @param context    the layout context
         * @param firstIndex the index of the first AreaInfo used for the TextArea
         * @param lastIndex  the index of the last AreaInfo used for the TextArea
         * @param isLastArea is this TextArea the last in a line?
         * @param font       Font to be used in this particular TextArea
         */
        private TextAreaBuilder(MinOptMax width, int adjust, LayoutContext context,
                                int firstIndex, int lastIndex, boolean isLastArea, Font font) {
            this.width = width;
            this.adjust = adjust;
            this.context = context;
            this.firstIndex = firstIndex;
            this.lastIndex = lastIndex;
            this.isLastArea = isLastArea;
            this.font = font;
        }

        private TextArea build() {
            createTextArea();
            setInlineProgressionDimension();
            calcBlockProgressionDimension();
            setBlockProgressionDimension();
            setBaselineOffset();
            setOffset();
            setText();
            TraitSetter.addFontTraits(textArea, font);
            textArea.addTrait(Trait.COLOR, foText.getColor());
            TraitSetter.addPtr(textArea, getPtr()); // used for accessibility
            TraitSetter.addTextDecoration(textArea, foText.getTextDecoration());
            TraitSetter.addFontTraits(textArea, font);
            return textArea;
        }

        /**
         * Creates an plain <code>TextArea</code> or a justified <code>TextArea</code> with
         * additional information.
         */
        private void createTextArea() {
            if (context.getIPDAdjust() == 0.0) {
                textArea = new TextArea();
            } else {
                textArea = new TextArea(width.getStretch(), width.getShrink(),
                        adjust);
            }
        }

        private void setInlineProgressionDimension() {
            textArea.setIPD(width.getOpt() + adjust);
        }

        private void calcBlockProgressionDimension() {
            blockProgressionDimension = font.getAscender() - font.getDescender();
        }

        private void setBlockProgressionDimension() {
            textArea.setBPD(blockProgressionDimension);
        }

        private void setBaselineOffset() {
            textArea.setBaselineOffset(font.getAscender());
        }

        private void setOffset() {
            if (blockProgressionDimension == alignmentContext.getHeight()) {
                textArea.setOffset(0);
            } else {
                textArea.setOffset(alignmentContext.getOffset());
            }
        }

        /**
         * Sets the text of the TextArea, split into words and spaces.
         */
        private void setText() {
            int wordStartIndex = -1;
            int wordCharLength = 0;
            for (int wordIndex = firstIndex; wordIndex <= lastIndex; wordIndex++) {
                areaInfo = getAreaInfo(wordIndex);
                if (areaInfo.isSpace) {
                    addSpaces();
                } else {
                    // areaInfo stores information about a word fragment
                    if (wordStartIndex == -1) {
                        // here starts a new word
                        wordStartIndex = wordIndex;
                        wordCharLength = 0;
                    }
                    wordCharLength += areaInfo.getCharLength();
                    if (isWordEnd(wordIndex)) {
                        addWord(wordStartIndex, wordIndex, wordCharLength);
                        wordStartIndex = -1;
                    }
                }
            }
        }

        private boolean isWordEnd(int areaInfoIndex) {
            return areaInfoIndex == lastIndex || getAreaInfo(areaInfoIndex + 1).isSpace;
        }

        private void addWord(int startIndex, int endIndex, int charLength) {
            if (isHyphenated(endIndex)) {
                charLength++;
            }
            initWord(charLength);
            for (int i = startIndex; i <= endIndex; i++) {
                AreaInfo wordAreaInfo = getAreaInfo(i);
                addWordChars(wordAreaInfo);
                addLetterAdjust(wordAreaInfo);
            }
            if (isHyphenated(endIndex)) {
                addHyphenationChar();
            }
            textArea.addWord(wordChars.toString(), 0, letterAdjust);
        }

        private void initWord(int charLength) {
            wordChars = new StringBuffer(charLength);
            letterAdjust = new int[charLength];
            letterAdjustIndex = 0;
        }

        private boolean isHyphenated(int endIndex) {
            return isLastArea && endIndex == lastIndex && areaInfo.isHyphenated;
        }

        private void addHyphenationChar() {
            wordChars.append(foText.getCommonHyphenation().getHyphChar(font));
        }

        private void addWordChars(AreaInfo wordAreaInfo) {
            for (int i = wordAreaInfo.startIndex; i < wordAreaInfo.breakIndex; i++) {
                wordChars.append(foText.charAt(i));
            }
        }

        private void addLetterAdjust(AreaInfo wordAreaInfo) {
            int letterSpaceCount = wordAreaInfo.letterSpaceCount;
            for (int i = wordAreaInfo.startIndex; i < wordAreaInfo.breakIndex; i++) {
                if (letterAdjustIndex > 0) {
                    MinOptMax adj = letterAdjustArray[i];
                    letterAdjust[letterAdjustIndex] = adj == null ? 0 : adj.getOpt();
                }
                if (letterSpaceCount > 0) {
                    letterAdjust[letterAdjustIndex] += textArea.getTextLetterSpaceAdjust();
                    letterSpaceCount--;
                }
                letterAdjustIndex++;
            }
        }

        /**
         * The <code>AreaInfo</code> stores information about spaces.
         * <p/>
         * Add the spaces - except zero-width spaces - to the TextArea.
         */
        private void addSpaces() {
            for (int i = areaInfo.startIndex; i < areaInfo.breakIndex; i++) {
                char spaceChar = foText.charAt(i);
                if (!CharUtilities.isZeroWidthSpace(spaceChar)) {
                    textArea.addSpace(spaceChar, 0, CharUtilities.isAdjustableSpace(spaceChar));
                }
            }
        }
    }

    /**
     * used for accessibility
     * @return ptr of fobj
     */
    private String getPtr() {
        FObj fobj = parentLayoutManager.getFObj();
        if (fobj instanceof StructurePointerPropertySet) {
            return (((StructurePointerPropertySet) fobj).getPtr());
        } else {
            //No structure pointer applicable
            return null;
        }
    }

    private AreaInfo getAreaInfo(int index) {
        return (AreaInfo) areaInfos.get(index);
    }

    private void addToLetterAdjust(int index, int width) {
        if (letterAdjustArray[index] == null) {
            letterAdjustArray[index] = MinOptMax.getInstance(width);
        } else {
            letterAdjustArray[index] = letterAdjustArray[index].plus(width);
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
        lineStartBAP = context.getLineStartBorderAndPaddingWidth();
        lineEndBAP = context.getLineEndBorderAndPaddingWidth();
        alignmentContext = context.getAlignmentContext();

        final List returnList = new LinkedList();
        KnuthSequence sequence = new InlineKnuthSequence();
        AreaInfo areaInfo = null;
        AreaInfo prevAreaInfo = null;
        returnList.add(sequence);

        LineBreakStatus lineBreakStatus = new LineBreakStatus();
        thisStart = nextStart;
        boolean inWord = false;
        boolean inWhitespace = false;
        char ch = 0;
        while (nextStart < foText.length()) {
            ch = foText.charAt(nextStart);
            boolean breakOpportunity = false;
            byte breakAction = keepTogether
                    ? LineBreakStatus.PROHIBITED_BREAK
                    : lineBreakStatus.nextChar(ch);
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
                    prevAreaInfo = processWord(alignment, sequence, prevAreaInfo, ch,
                            breakOpportunity, true);
                }
            } else if (inWhitespace) {
                if (ch != CharUtilities.SPACE || breakOpportunity) {
                    prevAreaInfo = processWhitespace(alignment, sequence, breakOpportunity);
                }
            } else {
                if (areaInfo != null) {
                    prevAreaInfo = areaInfo;
                    processLeftoverAreaInfo(alignment, sequence, areaInfo,
                            ch == CharUtilities.SPACE || breakOpportunity);
                    areaInfo = null;
                }
                if (breakAction == LineBreakStatus.EXPLICIT_BREAK) {
                    sequence = processLinebreak(returnList, sequence);
                }
            }

            if (ch == CharUtilities.SPACE
                    && foText.getWhitespaceTreatment() == Constants.EN_PRESERVE
                    || ch == CharUtilities.NBSPACE) {
                // preserved space or non-breaking space:
                // create the AreaInfo object
                areaInfo = new AreaInfo(nextStart, nextStart + 1, 1, 0, wordSpaceIPD, false, true,
                        breakOpportunity, spaceFont);
                thisStart = nextStart + 1;
            } else if (CharUtilities.isFixedWidthSpace(ch) || CharUtilities.isZeroWidthSpace(ch)) {
                // create the AreaInfo object
                Font font = FontSelector.selectFontForCharacterInText(ch, foText, this);
                MinOptMax ipd = MinOptMax.getInstance(font.getCharWidth(ch));
                areaInfo = new AreaInfo(nextStart, nextStart + 1, 0, 0, ipd, false, true,
                        breakOpportunity, font);
                thisStart = nextStart + 1;
            } else if (CharUtilities.isExplicitBreak(ch)) {
                //mandatory break-character: only advance index
                thisStart = nextStart + 1;
            }

            inWord = !TextLayoutManager.isSpace(ch) && !CharUtilities.isExplicitBreak(ch);
            inWhitespace = ch == CharUtilities.SPACE
                    && foText.getWhitespaceTreatment() != Constants.EN_PRESERVE;
            nextStart++;
        }

        // Process any last elements
        if (inWord) {
            processWord(alignment, sequence, prevAreaInfo, ch, false, false);
        } else if (inWhitespace) {
            processWhitespace(alignment, sequence, true);
        } else if (areaInfo != null) {
            processLeftoverAreaInfo(alignment, sequence, areaInfo,
                    ch == CharUtilities.ZERO_WIDTH_SPACE);
        } else if (CharUtilities.isExplicitBreak(ch)) {
            this.processLinebreak(returnList, sequence);
        }

        if (((List) ListUtil.getLast(returnList)).isEmpty()) {
            //Remove an empty sequence because of a trailing newline
            ListUtil.removeLast(returnList);
        }

        setFinished(true);
        if (returnList.isEmpty()) {
            return null;
        } else {
            return returnList;
        }
    }

    private KnuthSequence processLinebreak(List returnList, KnuthSequence sequence) {
        if (lineEndBAP != 0) {
            sequence.add(new KnuthGlue(lineEndBAP, 0, 0, auxiliaryPosition, true));
        }
        sequence.endSequence();
        sequence = new InlineKnuthSequence();
        returnList.add(sequence);
        return sequence;
    }

    private void processLeftoverAreaInfo(int alignment,
                                         KnuthSequence sequence, AreaInfo areaInfo,
                                         boolean breakOpportunityAfter) {
        areaInfos.add(areaInfo);
        areaInfo.breakOppAfter = breakOpportunityAfter;
        addElementsForASpace(sequence, alignment, areaInfo, areaInfos.size() - 1);
    }

    private AreaInfo processWhitespace(final int alignment,
            final KnuthSequence sequence, final boolean breakOpportunity) {
        // End of whitespace
        // create the AreaInfo object
        assert nextStart >= thisStart;
        AreaInfo areaInfo = new AreaInfo(thisStart, nextStart, nextStart - thisStart, 0,
                wordSpaceIPD.mult(nextStart - thisStart), false, true, breakOpportunity, spaceFont);

        areaInfos.add(areaInfo);

        // create the elements
        addElementsForASpace(sequence, alignment, areaInfo, areaInfos.size() - 1);

        thisStart = nextStart;
        return areaInfo;
    }

    private AreaInfo processWord(final int alignment, final KnuthSequence sequence,
            AreaInfo prevAreaInfo, final char ch, final boolean breakOpportunity,
            final boolean checkEndsWithHyphen) {

        //Word boundary found, process widths and kerning
        int lastIndex = nextStart;
        while (lastIndex > 0 && foText.charAt(lastIndex - 1) == CharUtilities.SOFT_HYPHEN) {
            lastIndex--;
        }
        final boolean endsWithHyphen = checkEndsWithHyphen
                && foText.charAt(lastIndex) == CharUtilities.SOFT_HYPHEN;
        Font font = FontSelector
            .selectFontForCharactersInText(foText, thisStart, lastIndex, foText, this);
        int wordLength = lastIndex - thisStart;
        boolean kerning = font.hasKerning();
        MinOptMax wordIPD = MinOptMax.ZERO;
        for (int i = thisStart; i < lastIndex; i++) {
            char currentChar = foText.charAt(i);

            //character width
            int charWidth = font.getCharWidth(currentChar);
            wordIPD = wordIPD.plus(charWidth);

            //kerning
            if (kerning) {
                int kern = 0;
                if (i > thisStart) {
                    char previousChar = foText.charAt(i - 1);
                    kern = font.getKernValue(previousChar, currentChar);
                } else if (prevAreaInfo != null
                           && !prevAreaInfo.isSpace && prevAreaInfo.breakIndex > 0) {
                    char previousChar = foText.charAt(prevAreaInfo.breakIndex - 1);
                    kern = font.getKernValue(previousChar, currentChar);
                }
                if (kern != 0) {
                    addToLetterAdjust(i, kern);
                    wordIPD = wordIPD.plus(kern);
                }
            }
        }
        if (kerning
                && breakOpportunity
                && !TextLayoutManager.isSpace(ch)
                && lastIndex > 0
                && endsWithHyphen) {
            final int kern = font.getKernValue(foText.charAt(lastIndex - 1), ch);
            if (kern != 0) {
                addToLetterAdjust(lastIndex, kern);
                //TODO: add kern to wordIPD?
            }
        }
        int iLetterSpaces = wordLength - 1;
        // if there is a break opportunity and the next one
        // is not a space, it could be used as a line end;
        // add one more letter space, in case other text follows
        if (breakOpportunity && !TextLayoutManager.isSpace(ch)) {
            iLetterSpaces++;
        }
        assert iLetterSpaces >= 0;
        wordIPD = wordIPD.plus(letterSpaceIPD.mult(iLetterSpaces));

        // create the AreaInfo object
        AreaInfo areaInfo = new AreaInfo(thisStart, lastIndex, 0,
                iLetterSpaces, wordIPD,
                endsWithHyphen,
                false, breakOpportunity, font);
        prevAreaInfo = areaInfo;
        areaInfos.add(areaInfo);
        tempStart = nextStart;

        //add the elements
        addElementsForAWordFragment(sequence, alignment, areaInfo, areaInfos.size() - 1);
        thisStart = nextStart;

        return prevAreaInfo;
    }

    /** {@inheritDoc} */
    public List addALetterSpaceTo(final List oldList) {
        // old list contains only a box, or the sequence: box penalty glue box;
        // look at the Position stored in the first element in oldList
        // which is always a box
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement knuthElement = (KnuthElement) oldListIterator.next();
        LeafPosition pos = (LeafPosition) ((KnuthBox) knuthElement).getPosition();
        int index = pos.getLeafPos();
        //element could refer to '-1' position, for non-collapsed spaces (?)
        if (index > -1) {
            AreaInfo areaInfo = getAreaInfo(index);
            areaInfo.letterSpaceCount++;
            areaInfo.addToAreaIPD(letterSpaceIPD);
            if (TextLayoutManager.BREAK_CHARS.indexOf(foText.charAt(tempStart - 1)) >= 0) {
                // the last character could be used as a line break
                // append new elements to oldList
                oldListIterator = oldList.listIterator(oldList.size());
                oldListIterator.add(new KnuthPenalty(0, KnuthPenalty.FLAGGED_PENALTY, true,
                        auxiliaryPosition, false));
                oldListIterator.add(new KnuthGlue(letterSpaceIPD, auxiliaryPosition, false));
            } else if (letterSpaceIPD.isStiff()) {
                // constant letter space: replace the box
                oldListIterator.set(new KnuthInlineBox(areaInfo.areaIPD.getOpt(),
                        alignmentContext, pos, false));
            } else {
                // adjustable letter space: replace the glue
                oldListIterator.next(); // this would return the penalty element
                oldListIterator.next(); // this would return the glue element
                oldListIterator.set(new KnuthGlue(letterSpaceIPD.mult(areaInfo.letterSpaceCount),
                        auxiliaryPosition, true));
            }
        }
        return oldList;
    }

    /**
     * Removes the <code>AreaInfo</code> object represented by the given elements, so that it won't
     * generate any element when <code>getChangedKnuthElements</code> is called.
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
        KnuthElement knuthElement = (KnuthElement) oldListIterator.next();
        int leafValue = ((LeafPosition) knuthElement.getPosition()).getLeafPos();
        // only the last word space can be a trailing space!
        if (leafValue == areaInfos.size() - 1) {
            areaInfos.remove(leafValue);
        } else {
            TextLayoutManager.LOG.error("trying to remove a non-trailing word space");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void hyphenate(Position pos, HyphContext hyphContext) {
        AreaInfo areaInfo = getAreaInfo(((LeafPosition) pos).getLeafPos());
        int startIndex = areaInfo.startIndex;
        int stopIndex;
        boolean nothingChanged = true;
        Font font = areaInfo.font;

        while (startIndex < areaInfo.breakIndex) {
            MinOptMax newIPD = MinOptMax.ZERO;
            boolean hyphenFollows;

            stopIndex = startIndex + hyphContext.getNextHyphPoint();
            if (hyphContext.hasMoreHyphPoints() && stopIndex <= areaInfo.breakIndex) {
                // stopIndex is the index of the first character
                // after a hyphenation point
                hyphenFollows = true;
            } else {
                // there are no more hyphenation points,
                // or the next one is after areaInfo.breakIndex
                hyphenFollows = false;
                stopIndex = areaInfo.breakIndex;
            }

            hyphContext.updateOffset(stopIndex - startIndex);

            //log.info("Word: " + new String(textArray, startIndex, stopIndex - startIndex));
            for (int i = startIndex; i < stopIndex; i++) {
                char ch = foText.charAt(i);
                newIPD = newIPD.plus(font.getCharWidth(ch));
                //if (i > startIndex) {
                if (i < stopIndex) {
                    MinOptMax letterAdjust = letterAdjustArray[i + 1];
                    if (i == stopIndex - 1 && hyphenFollows) {
                        //the letter adjust here needs to be handled further down during
                        //element generation because it depends on hyph/no-hyph condition
                        letterAdjust = null;
                    }
                    if (letterAdjust != null) {
                        newIPD = newIPD.plus(letterAdjust);
                    }
                }
            }

            // add letter spaces
            boolean isWordEnd
                = stopIndex == areaInfo.breakIndex
                && areaInfo.letterSpaceCount < areaInfo.getCharLength();
            int letterSpaceCount = isWordEnd ? stopIndex - startIndex - 1 : stopIndex - startIndex;

            assert letterSpaceCount >= 0;
            newIPD = newIPD.plus(letterSpaceIPD.mult(letterSpaceCount));

            if (!(nothingChanged && stopIndex == areaInfo.breakIndex && !hyphenFollows)) {
                // the new AreaInfo object is not equal to the old one
                changeList.add(new PendingChange(new AreaInfo(startIndex, stopIndex, 0,
                        letterSpaceCount, newIPD, hyphenFollows, false, false, font),
                        ((LeafPosition) pos).getLeafPos()));
                nothingChanged = false;
            }
            startIndex = stopIndex;
        }
        hasChanged |= !nothingChanged;
    }

    /** {@inheritDoc} */
    public boolean applyChanges(final List oldList) {
        setFinished(false);

        if (!changeList.isEmpty()) {
            int areaInfosAdded = 0;
            int areaInfosRemoved = 0;
            int oldIndex = -1, changeIndex;
            PendingChange currChange;
            ListIterator changeListIterator = changeList.listIterator();
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
                    areaInfos.remove(changeIndex);
                }
                areaInfos.add(changeIndex, currChange.areaInfo);
            }
            changeList.clear();
        }

        returnedIndex = 0;
        return hasChanged;
    }

    /** {@inheritDoc} */
    public List getChangedKnuthElements(final List oldList, final int alignment) {
        if (isFinished()) {
            return null;
        }

        final LinkedList returnList = new LinkedList();

        while (returnedIndex < areaInfos.size()) {
            AreaInfo areaInfo = getAreaInfo(returnedIndex);
            if (areaInfo.wordSpaceCount == 0) {
                // areaInfo refers either to a word or a word fragment
                addElementsForAWordFragment(returnList, alignment, areaInfo, returnedIndex);
            } else {
                // areaInfo refers to a space
                addElementsForASpace(returnList, alignment, areaInfo, returnedIndex);
            }
            returnedIndex++;
        }
        setFinished(true);
        //ElementListObserver.observe(returnList, "text-changed", null);
        return returnList;
    }

    /**
     * {@inheritDoc}
     */
    public String getWordChars(Position pos) {
        int leafValue = ((LeafPosition) pos).getLeafPos();
        if (leafValue != -1) {
            AreaInfo areaInfo = getAreaInfo(leafValue);
            StringBuffer buffer = new StringBuffer(areaInfo.getCharLength());
            for (int i = areaInfo.startIndex; i < areaInfo.breakIndex; i++) {
                buffer.append(foText.charAt(i));
            }
            return buffer.toString();
        } else {
            return "";
        }
    }

    private void addElementsForASpace(List baseList, int alignment, AreaInfo areaInfo,
                                      int leafValue) {
        LeafPosition mainPosition = new LeafPosition(this, leafValue);

        if (!areaInfo.breakOppAfter) {
            // a non-breaking space
            if (alignment == Constants.EN_JUSTIFY) {
                // the space can stretch and shrink, and must be preserved
                // when starting a line
                baseList.add(makeAuxiliaryZeroWidthBox());
                baseList.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(areaInfo.areaIPD, mainPosition, false));
            } else {
                // the space does not need to stretch or shrink, and must be
                // preserved when starting a line
                baseList.add(new KnuthInlineBox(areaInfo.areaIPD.getOpt(), null, mainPosition,
                        true));
            }
        } else {
            if (foText.charAt(areaInfo.startIndex) != CharUtilities.SPACE
                    || foText.getWhitespaceTreatment() == Constants.EN_PRESERVE) {
                // a breaking space that needs to be preserved
                baseList
                    .addAll(getElementsForBreakingSpace(alignment, areaInfo, auxiliaryPosition, 0,
                        mainPosition, areaInfo.areaIPD.getOpt(), true));
            } else {
                // a (possible block) of breaking spaces
                baseList
                    .addAll(getElementsForBreakingSpace(alignment, areaInfo, mainPosition,
                        areaInfo.areaIPD.getOpt(), auxiliaryPosition, 0, false));
            }
        }
    }

    private List getElementsForBreakingSpace(int alignment, AreaInfo areaInfo, Position pos2,
                                             int p2WidthOffset, Position pos3,
                                             int p3WidthOffset, boolean skipZeroCheck) {
        List elements = new ArrayList();

        switch (alignment) {
        case EN_CENTER:
            // centered text:
            // if the second element is chosen as a line break these elements
            // add a constant amount of stretch at the end of a line and at the
            // beginning of the next one, otherwise they don't add any stretch
            elements.add(new KnuthGlue(lineEndBAP, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                    auxiliaryPosition, false));
            elements.add(makeZeroWidthPenalty(0));
            elements.add(new KnuthGlue(p2WidthOffset - (lineStartBAP + lineEndBAP), -6
                    * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0, pos2, false));
            elements.add(makeAuxiliaryZeroWidthBox());
            elements.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
            elements.add(new KnuthGlue(lineStartBAP + p3WidthOffset,
                    3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0, pos3, false));
            break;

        case EN_START: // fall through
        case EN_END:
            // left- or right-aligned text:
            // if the second element is chosen as a line break these elements
            // add a constant amount of stretch at the end of a line, otherwise
            // they don't add any stretch
            KnuthGlue g;
            if (skipZeroCheck || lineStartBAP != 0 || lineEndBAP != 0) {
                g = new KnuthGlue
                    (lineEndBAP,
                     3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0, auxiliaryPosition, false);
                elements.add(g);
                elements.add(makeZeroWidthPenalty(0));
                g = new KnuthGlue
                    (p2WidthOffset - (lineStartBAP + lineEndBAP),
                     -3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0, pos2, false);
                elements.add(g);
                elements.add(makeAuxiliaryZeroWidthBox());
                elements.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
                g = new KnuthGlue(lineStartBAP + p3WidthOffset, 0, 0, pos3, false);
                elements.add(g);
            } else {
                g = new KnuthGlue
                    (0,
                     3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0, auxiliaryPosition, false);
                elements.add(g);
                elements.add(makeZeroWidthPenalty(0));
                g = new KnuthGlue
                    (areaInfo.areaIPD.getOpt(),
                     -3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0, pos2, false);
                elements.add(g);
            }
            break;

        case EN_JUSTIFY:
            // justified text:
            // the stretch and shrink depends on the space width
            elements.addAll(getElementsForJustifiedText(areaInfo, pos2, p2WidthOffset, pos3,
                    p3WidthOffset, skipZeroCheck, areaInfo.areaIPD.getShrink()));
            break;

        default:
            // last line justified, the other lines unjustified:
            // use only the space stretch
            elements.addAll(getElementsForJustifiedText(areaInfo, pos2, p2WidthOffset, pos3,
                    p3WidthOffset, skipZeroCheck, 0));
        }
        return elements;
    }

    private List getElementsForJustifiedText
        (AreaInfo areaInfo, Position pos2, int p2WidthOffset,
         Position pos3, int p3WidthOffset, boolean skipZeroCheck,
         int shrinkability) {

        int stretchability = areaInfo.areaIPD.getStretch();

        List elements = new ArrayList();
        if (skipZeroCheck || lineStartBAP != 0 || lineEndBAP != 0) {
            elements.add(new KnuthGlue(lineEndBAP, 0, 0, auxiliaryPosition, false));
            elements.add(makeZeroWidthPenalty(0));
            elements.add(new KnuthGlue(p2WidthOffset - (lineStartBAP + lineEndBAP),
                    stretchability, shrinkability, pos2, false));
            elements.add(makeAuxiliaryZeroWidthBox());
            elements.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
            elements.add(new KnuthGlue(lineStartBAP + p3WidthOffset, 0, 0, pos3, false));
        } else {
            elements.add(new KnuthGlue(areaInfo.areaIPD.getOpt(), stretchability, shrinkability,
                    pos2, false));
        }
        return elements;
    }

    private void addElementsForAWordFragment(List baseList, int alignment, AreaInfo areaInfo,
                                             int leafValue) {
        LeafPosition mainPosition = new LeafPosition(this, leafValue);

        // if the last character of the word fragment is '-' or '/',
        // the fragment could end a line; in this case, it loses one
        // of its letter spaces;
        boolean suppressibleLetterSpace = areaInfo.breakOppAfter && !areaInfo.isHyphenated;

        if (letterSpaceIPD.isStiff()) {
            // constant letter spacing
            baseList.add(new KnuthInlineBox(suppressibleLetterSpace
                    ? areaInfo.areaIPD.getOpt() - letterSpaceIPD.getOpt()
                    : areaInfo.areaIPD.getOpt(),
                    alignmentContext, notifyPos(mainPosition), false));
        } else {
            // adjustable letter spacing
            int unsuppressibleLetterSpaces = suppressibleLetterSpace
                    ? areaInfo.letterSpaceCount - 1
                    : areaInfo.letterSpaceCount;
            baseList.add(new KnuthInlineBox(areaInfo.areaIPD.getOpt()
                    - areaInfo.letterSpaceCount * letterSpaceIPD.getOpt(),
                            alignmentContext, notifyPos(mainPosition), false));
            baseList.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
            baseList.add(new KnuthGlue(letterSpaceIPD.mult(unsuppressibleLetterSpaces),
                    auxiliaryPosition, true));
            baseList.add(makeAuxiliaryZeroWidthBox());
        }

        // extra-elements if the word fragment is the end of a syllable,
        // or it ends with a character that can be used as a line break
        if (areaInfo.isHyphenated) {
            MinOptMax widthIfNoBreakOccurs = null;
            if (areaInfo.breakIndex < foText.length()) {
                //Add in kerning in no-break condition
                widthIfNoBreakOccurs = letterAdjustArray[areaInfo.breakIndex];
            }
            //if (areaInfo.breakIndex)

            // the word fragment ends at the end of a syllable:
            // if a break occurs the content width increases,
            // otherwise nothing happens
            addElementsForAHyphen(baseList, alignment, hyphIPD, widthIfNoBreakOccurs,
                    areaInfo.breakOppAfter && areaInfo.isHyphenated);
        } else if (suppressibleLetterSpace) {
            // the word fragment ends with a character that acts as a hyphen
            // if a break occurs the width does not increase,
            // otherwise there is one more letter space
            addElementsForAHyphen(baseList, alignment, 0, letterSpaceIPD, true);
        }
    }

    private void addElementsForAHyphen(List baseList, int alignment, int widthIfBreakOccurs,
                                       MinOptMax widthIfNoBreakOccurs, boolean unflagged) {

        if (widthIfNoBreakOccurs == null) {
            widthIfNoBreakOccurs = MinOptMax.ZERO;
        }

        switch (alignment) {
        case EN_CENTER:
            // centered text:
            baseList.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
            baseList.add(new KnuthGlue(lineEndBAP, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                    auxiliaryPosition, true));
            baseList.add(new KnuthPenalty(hyphIPD, unflagged
                    ? TextLayoutManager.SOFT_HYPHEN_PENALTY
                            : KnuthPenalty.FLAGGED_PENALTY, !unflagged, auxiliaryPosition, false));
            baseList.add(new KnuthGlue(-(lineEndBAP + lineStartBAP),
                    -6 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                    auxiliaryPosition, false));
            baseList.add(makeAuxiliaryZeroWidthBox());
            baseList.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
            baseList.add(new KnuthGlue(lineStartBAP, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH,
                    0, auxiliaryPosition, true));
            break;

        case EN_START: // fall through
        case EN_END:
            // left- or right-aligned text:
            if (lineStartBAP != 0 || lineEndBAP != 0) {
                baseList.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(lineEndBAP,
                        3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        auxiliaryPosition, false));
                baseList.add(new KnuthPenalty(widthIfBreakOccurs,
                        unflagged ? TextLayoutManager.SOFT_HYPHEN_PENALTY
                                : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                                auxiliaryPosition, false));
                baseList.add(new KnuthGlue(widthIfNoBreakOccurs.getOpt()
                        - (lineStartBAP + lineEndBAP),
                        -3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        auxiliaryPosition, false));
                baseList.add(makeAuxiliaryZeroWidthBox());
                baseList.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(lineStartBAP, 0, 0, auxiliaryPosition, false));
            } else {
                baseList.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        auxiliaryPosition, false));
                baseList.add(new KnuthPenalty(widthIfBreakOccurs,
                        unflagged ? TextLayoutManager.SOFT_HYPHEN_PENALTY
                                : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                                auxiliaryPosition, false));
                baseList.add(new KnuthGlue(widthIfNoBreakOccurs.getOpt(),
                        -3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        auxiliaryPosition, false));
            }
            break;

        default:
            // justified text, or last line justified:
            // just a flagged penalty
            if (lineStartBAP != 0 || lineEndBAP != 0) {
                baseList.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(lineEndBAP, 0, 0, auxiliaryPosition, false));
                baseList.add(new KnuthPenalty(widthIfBreakOccurs,
                        unflagged ? TextLayoutManager.SOFT_HYPHEN_PENALTY
                                : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                                auxiliaryPosition, false));
                // extra elements representing a letter space that is suppressed
                // if a break occurs
                if (widthIfNoBreakOccurs.isNonZero()) {
                    baseList.add(new KnuthGlue(widthIfNoBreakOccurs.getOpt()
                            - (lineStartBAP + lineEndBAP),
                            widthIfNoBreakOccurs.getStretch(),
                            widthIfNoBreakOccurs.getShrink(),
                            auxiliaryPosition, false));
                } else {
                    baseList.add(new KnuthGlue(-(lineStartBAP + lineEndBAP), 0, 0,
                            auxiliaryPosition, false));
                }
                baseList.add(makeAuxiliaryZeroWidthBox());
                baseList.add(makeZeroWidthPenalty(KnuthElement.INFINITE));
                baseList.add(new KnuthGlue(lineStartBAP, 0, 0,
                        auxiliaryPosition, false));
            } else {
                baseList.add(new KnuthPenalty(widthIfBreakOccurs,
                        unflagged ? TextLayoutManager.SOFT_HYPHEN_PENALTY
                                : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                                auxiliaryPosition, false));
                // extra elements representing a letter space that is suppressed
                // if a break occurs
                if (widthIfNoBreakOccurs.isNonZero()) {
                    baseList.add(new KnuthGlue(widthIfNoBreakOccurs, auxiliaryPosition, false));
                }
            }
        }

    }

}
