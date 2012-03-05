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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable;
import org.apache.fop.complexscripts.util.CharScript;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FOText;
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
        private int wordCharLength;
        private final int wordSpaceCount;
        private int letterSpaceCount;
        private MinOptMax areaIPD;
        private final boolean isHyphenated;
        private final boolean isSpace;
        private boolean breakOppAfter;
        private final Font font;
        private final int level;
        private final int[][] gposAdjustments;

        AreaInfo                                                // CSOK: ParameterNumber
            (int startIndex, int breakIndex, int wordSpaceCount, int letterSpaceCount,
             MinOptMax areaIPD, boolean isHyphenated, boolean isSpace, boolean breakOppAfter,
             Font font, int level, int[][] gposAdjustments) {
            assert startIndex <= breakIndex;
            this.startIndex = startIndex;
            this.breakIndex = breakIndex;
            this.wordCharLength = -1;
            this.wordSpaceCount = wordSpaceCount;
            this.letterSpaceCount = letterSpaceCount;
            this.areaIPD = areaIPD;
            this.isHyphenated = isHyphenated;
            this.isSpace = isSpace;
            this.breakOppAfter = breakOppAfter;
            this.font = font;
            this.level = level;
            this.gposAdjustments = gposAdjustments;
        }

        /**
         * Obtain number of 'characters' contained in word. If word
         * is mapped, then this number may be less than or greater than the
         * original length (breakIndex - startIndex). We compute and
         * memoize thius length upon first invocation of this method.
         */
        private int getWordLength() {
            if ( wordCharLength == -1 ) {
                if ( foText.hasMapping ( startIndex, breakIndex ) ) {
                    wordCharLength = foText.getMapping ( startIndex, breakIndex ).length();
                } else {
                    assert breakIndex >= startIndex;
                    wordCharLength = breakIndex - startIndex;
                }
            }
            return wordCharLength;
        }

        private void addToAreaIPD(MinOptMax idp) {
            areaIPD = areaIPD.plus(idp);
        }

        public String toString() {
            return super.toString() + "{"
                    + "interval = [" + startIndex + "," + breakIndex + "]"
                    + ", isSpace = " + isSpace
                    + ", level = " + level
                    + ", areaIPD = " + areaIPD
                    + ", letterSpaceCount = " + letterSpaceCount
                    + ", wordSpaceCount = " + wordSpaceCount
                    + ", isHyphenated = " + isHyphenated
                    + ", font = " + font
                    + "}";
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
    private final MinOptMax[] letterSpaceAdjustArray; //size = textArray.length + 1

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
    private int[] returnedIndices = {0, 0};
    private int changeOffset = 0;
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
        letterSpaceAdjustArray = new MinOptMax[node.length() + 1];
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
                if (lastAreaInfo == null
                    || ( areaInfo.font != lastAreaInfo.font )
                    || ( areaInfo.level != lastAreaInfo.level ) ) {
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
        int textLength = areaInfo.getWordLength();
        if (areaInfo.letterSpaceCount == textLength && !areaInfo.isHyphenated
                && context.isLastArea()) {
            // the line ends at a character like "/" or "-";
            // remove the letter space after the last character
            realWidth = realWidth.minus(letterSpaceIPD);
            letterSpaceCount--;
        }

        for (int i = areaInfo.startIndex; i < areaInfo.breakIndex; i++) {
            MinOptMax letterSpaceAdjustment = letterSpaceAdjustArray[i + 1];
            if (letterSpaceAdjustment != null && letterSpaceAdjustment.isElastic()) {
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

        // constructor initialized state
        private final MinOptMax width;          // content ipd
        private final int adjust;               // content ipd adjustment
        private final LayoutContext context;    // layout context
        private final int firstIndex;           // index of first AreaInfo
        private final int lastIndex;            // index of last AreaInfo
        private final boolean isLastArea;       // true if last inline area in line area
        private final Font font;                // applicable font

        // other, non-constructor state
        private TextArea textArea;              // text area being constructed
        private int blockProgressionDimension;  // calculated bpd
        private AreaInfo areaInfo;              // current area info when iterating over words
        private StringBuffer wordChars;         // current word's character buffer
        private int[] letterSpaceAdjust;        // current word's letter space adjustments
        private int letterSpaceAdjustIndex;     // last written letter space adjustment index
        private int[] wordLevels;               // current word's bidi levels
        private int wordLevelsIndex;            // last written bidi level index
        private int wordIPD;                    // accumulated ipd of current word
        private int[][] gposAdjustments;        // current word's glyph position adjustments
        private int gposAdjustmentsIndex;       // last written glyph position adjustment index

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
            setBlockProgressionOffset();
            setText();
            TraitSetter.addFontTraits(textArea, font);
            textArea.addTrait(Trait.COLOR, foText.getColor());
            TraitSetter.addTextDecoration(textArea, foText.getTextDecoration());
            TraitSetter.addStructureTreeElement(textArea, foText.getStructureTreeElement());
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

        private void setBlockProgressionOffset() {
            if (blockProgressionDimension == alignmentContext.getHeight()) {
                textArea.setBlockProgressionOffset(0);
            } else {
                textArea.setBlockProgressionOffset(alignmentContext.getOffset());
            }
        }

        /**
         * Sets the text of the TextArea, split into words and spaces.
         */
        private void setText() {
            int areaInfoIndex = -1;
            int wordCharLength = 0;
            for (int wordIndex = firstIndex; wordIndex <= lastIndex; wordIndex++) {
                areaInfo = getAreaInfo(wordIndex);
                if (areaInfo.isSpace) {
                    addSpaces();
                } else {
                    // areaInfo stores information about a word fragment
                    if (areaInfoIndex == -1) {
                        // here starts a new word
                        areaInfoIndex = wordIndex;
                        wordCharLength = 0;
                    }
                    wordCharLength += areaInfo.getWordLength();
                    if (isWordEnd(wordIndex)) {
                        addWord(areaInfoIndex, wordIndex, wordCharLength);
                        areaInfoIndex = -1;
                    }
                }
            }
        }

        private boolean isWordEnd(int areaInfoIndex) {
            return areaInfoIndex == lastIndex || getAreaInfo(areaInfoIndex + 1).isSpace;
        }

        /**
         * Add word with fragments from STARTINDEX to ENDINDEX, where
         * total length of (possibly mapped) word is CHARLENGTH.
         * A word is composed from one or more word fragments, where each
         * fragment corresponds to distinct instance in a sequence of
         * area info instances starting at STARTINDEX continuing through (and
         * including)  ENDINDEX.
         * @param startIndex index of first area info of word to add
         * @param endIndex index of last area info of word to add
         * @param wordLength number of (mapped) characters in word
         */
        private void addWord(int startIndex, int endIndex, int wordLength) {
            int blockProgressionOffset = 0;
            boolean gposAdjusted = false;
            if (isHyphenated(endIndex)) {
                // TODO may be problematic in some I18N contexts [GA]
                wordLength++;
            }
            initWord(wordLength);
            // iterate over word's fragments, adding word chars (with bidi
            // levels), letter space adjustments, and glyph position adjustments
            for (int i = startIndex; i <= endIndex; i++) {
                AreaInfo wordAreaInfo = getAreaInfo(i);
                addWordChars(wordAreaInfo);
                addLetterAdjust(wordAreaInfo);
                if ( addGlyphPositionAdjustments(wordAreaInfo) ) {
                    gposAdjusted = true;
                }
            }
            if (isHyphenated(endIndex)) {
                // TODO may be problematic in some I18N contexts [GA]
                addHyphenationChar();
            }
            if ( !gposAdjusted ) {
                gposAdjustments = null;
            }
            textArea.addWord(wordChars.toString(), wordIPD, letterSpaceAdjust,
                             getNonEmptyLevels(), gposAdjustments, blockProgressionOffset);
        }

        private int[] getNonEmptyLevels() {
            if ( wordLevels != null ) {
                assert wordLevelsIndex <= wordLevels.length;
                boolean empty = true;
                for ( int i = 0, n = wordLevelsIndex; i < n; i++ ) {
                    if ( wordLevels [ i ] >= 0 ) {
                        empty = false;
                        break;
                    }
                }
                return empty ? null : wordLevels;
            } else {
                return null;
            }
        }

        /**
         * Fully allocate word character buffer, letter space adjustments
         * array, bidi levels array, and glyph position adjustments array.
         * based on full word length, including all (possibly mapped) fragments.
         * @param wordLength length of word including all (possibly mapped) fragments
         */
        private void initWord(int wordLength) {
            wordChars = new StringBuffer(wordLength);
            letterSpaceAdjust = new int[wordLength];
            letterSpaceAdjustIndex = 0;
            wordLevels = new int[wordLength];
            wordLevelsIndex = 0;
            Arrays.fill ( wordLevels, -1 );
            gposAdjustments = new int[wordLength][4];
            gposAdjustmentsIndex = 0;
            wordIPD = 0;
        }

        private boolean isHyphenated(int endIndex) {
            return isLastArea && endIndex == lastIndex && areaInfo.isHyphenated;
        }

        private void addHyphenationChar() {
            wordChars.append(foText.getCommonHyphenation().getHyphChar(font));
            // [TBD] expand bidi word levels, letter space adjusts, gpos adjusts
            // [TBD] [GA] problematic in bidi context... what is level of hyphen?
        }

        /**
         * Given a word area info associated with a word fragment,
         * (1) concatenate (possibly mapped) word characters to word character buffer;
         * (2) concatenante (possibly mapped) word bidi levels to levels buffer;
         * (3) update word's IPD with optimal IPD of fragment.
         * @param wordAreaInfo fragment info
         */
        private void addWordChars(AreaInfo wordAreaInfo) {
            int s = wordAreaInfo.startIndex;
            int e = wordAreaInfo.breakIndex;
            if ( foText.hasMapping ( s, e ) ) {
                wordChars.append ( foText.getMapping ( s, e ) );
                addWordLevels ( foText.getMappingBidiLevels ( s, e ) );
            } else {
                for (int i = s; i < e; i++) {
                    wordChars.append(foText.charAt(i));
                }
                addWordLevels ( foText.getBidiLevels ( s, e ) );
            }
            wordIPD += wordAreaInfo.areaIPD.getOpt();
        }

        /**
         * Given a (possibly null) bidi levels array associated with a word fragment,
         * concatenante (possibly mapped) word bidi levels to levels buffer.
         * @param levels bidi levels array or null
         */
        private void addWordLevels ( int[] levels ) {
            int numLevels = ( levels != null ) ? levels.length : 0;
            if ( numLevels > 0 ) {
                int need = wordLevelsIndex + numLevels;
                if ( need <= wordLevels.length ) {
                    System.arraycopy ( levels, 0, wordLevels, wordLevelsIndex, numLevels );
                } else {
                    throw new IllegalStateException
                        ( "word levels array too short: expect at least "
                          + need + " entries, but has only " + wordLevels.length + " entries" );
                }
            }
            wordLevelsIndex += numLevels;
        }

        /**
         * Given a word area info associated with a word fragment,
         * concatenate letter space adjustments for each (possibly mapped) character.
         * @param wordAreaInfo fragment info
         */
        private void addLetterAdjust(AreaInfo wordAreaInfo) {
            int letterSpaceCount = wordAreaInfo.letterSpaceCount;
            int wordLength = wordAreaInfo.getWordLength();
            int taAdjust = textArea.getTextLetterSpaceAdjust();
            for ( int i = 0, n = wordLength; i < n; i++ ) {
                int j = letterSpaceAdjustIndex + i;
                if ( j > 0 ) {
                    int k = wordAreaInfo.startIndex + i;
                    MinOptMax adj = ( k < letterSpaceAdjustArray.length )
                        ? letterSpaceAdjustArray [ k ] : null;
                    letterSpaceAdjust [ j ] = ( adj == null ) ? 0 : adj.getOpt();
                }
                if ( letterSpaceCount > 0 ) {
                    letterSpaceAdjust [ j ] += taAdjust;
                    letterSpaceCount--;
                }
            }
            letterSpaceAdjustIndex += wordLength;
        }

        /**
         * Given a word area info associated with a word fragment,
         * concatenate glyph position adjustments for each (possibly mapped) character.
         * @param wordAreaInfo fragment info
         * @return true if an adjustment was non-zero
         */
        private boolean addGlyphPositionAdjustments(AreaInfo wordAreaInfo) {
            boolean adjusted = false;
            int[][] gpa = wordAreaInfo.gposAdjustments;
            int numAdjusts = ( gpa != null ) ? gpa.length : 0;
            int wordLength = wordAreaInfo.getWordLength();
            if ( numAdjusts > 0 ) {
                int need = gposAdjustmentsIndex + numAdjusts;
                if ( need <= gposAdjustments.length ) {
                    for ( int i = 0, n = wordLength, j = 0; i < n; i++ ) {
                        if ( i < numAdjusts ) {
                            int[] wpa1 = gposAdjustments [ gposAdjustmentsIndex + i ];
                            int[] wpa2 = gpa [ j++ ];
                            for ( int k = 0; k < 4; k++ ) {
                                int a = wpa2 [ k ];
                                if ( a != 0 ) {
                                    wpa1 [ k ] += a;
                                    adjusted = true;
                                }
                            }
                        }
                    }
                } else {
                    throw new IllegalStateException
                        ( "gpos adjustments array too short: expect at least "
                          + need + " entries, but has only " + gposAdjustments.length
                          + " entries" );
                }
            }
            gposAdjustmentsIndex += wordLength;
            return adjusted;
        }

        /**
         * The <code>AreaInfo</code> stores information about spaces.
         * <p/>
         * Add the spaces - except zero-width spaces - to the TextArea.
         */
        private void addSpaces() {
            int blockProgressionOffset = 0;
            // [TBD] need to better handling of spaceIPD assignment, for now,
            // divide the area info's allocated IPD evenly among the
            // non-zero-width space characters
            int numZeroWidthSpaces = 0;
            for (int i = areaInfo.startIndex; i < areaInfo.breakIndex; i++) {
                char spaceChar = foText.charAt(i);
                if (CharUtilities.isZeroWidthSpace(spaceChar)) {
                    numZeroWidthSpaces++;
                }
            }
            int numSpaces = areaInfo.breakIndex - areaInfo.startIndex - numZeroWidthSpaces;
            int spaceIPD = areaInfo.areaIPD.getOpt() / ( ( numSpaces > 0 ) ? numSpaces : 1 );
            // add space area children, one for each non-zero-width space character
            for (int i = areaInfo.startIndex; i < areaInfo.breakIndex; i++) {
                char spaceChar = foText.charAt(i);
                int level = foText.bidiLevelAt(i);
                if (!CharUtilities.isZeroWidthSpace(spaceChar)) {
                    textArea.addSpace
                        ( spaceChar, spaceIPD,
                          CharUtilities.isAdjustableSpace(spaceChar),
                          blockProgressionOffset, level );
                }
            }
        }

    }

    private void addAreaInfo ( AreaInfo ai ) {
        addAreaInfo ( areaInfos.size(), ai );
    }

    private void addAreaInfo ( int index, AreaInfo ai ) {
        areaInfos.add ( index, ai );
    }

    private void removeAreaInfo ( int index ) {
        areaInfos.remove ( index );
    }

    private AreaInfo getAreaInfo(int index) {
        return (AreaInfo) areaInfos.get(index);
    }

    private void addToLetterAdjust(int index, int width) {
        if (letterSpaceAdjustArray[index] == null) {
            letterSpaceAdjustArray[index] = MinOptMax.getInstance(width);
        } else {
            letterSpaceAdjustArray[index] = letterSpaceAdjustArray[index].plus(width);
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

        if (LOG.isDebugEnabled()) {
            LOG.debug ( "GK: [" + nextStart + "," + foText.length() + "]" );
        }
        LineBreakStatus lineBreakStatus = new LineBreakStatus();
        thisStart = nextStart;
        boolean inWord = false;
        boolean inWhitespace = false;
        char ch = 0;
        int level = -1;
        int prevLevel = -1;
        while (nextStart < foText.length()) {
            ch = foText.charAt(nextStart);
            level = foText.bidiLevelAt(nextStart);
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
            if (LOG.isDebugEnabled()) {
                LOG.debug ( "GK: {"
                            + " index = " + nextStart
                            + ", char = " + CharUtilities.charToNCRef ( ch )
                            + ", level = " + level
                            + ", levelPrev = " + prevLevel
                            + ", inWord = " + inWord
                            + ", inSpace = " + inWhitespace
                            + "}" );
            }
            if (inWord) {
                if ( breakOpportunity
                     || TextLayoutManager.isSpace(ch)
                     || CharUtilities.isExplicitBreak(ch)
                     || ( ( prevLevel != -1 ) && ( level != prevLevel ) ) ) {
                    // this.foText.charAt(lastIndex) == CharUtilities.SOFT_HYPHEN
                    prevAreaInfo = processWord(alignment, sequence, prevAreaInfo, ch,
                            breakOpportunity, true, prevLevel);
                }
            } else if (inWhitespace) {
                if (ch != CharUtilities.SPACE || breakOpportunity) {
                    prevAreaInfo = processWhitespace(alignment, sequence,
                                                     breakOpportunity, prevLevel);
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
                                        breakOpportunity, spaceFont, level, null);
                thisStart = nextStart + 1;
            } else if (CharUtilities.isFixedWidthSpace(ch) || CharUtilities.isZeroWidthSpace(ch)) {
                // create the AreaInfo object
                Font font = FontSelector.selectFontForCharacterInText(ch, foText, this);
                MinOptMax ipd = MinOptMax.getInstance(font.getCharWidth(ch));
                areaInfo = new AreaInfo(nextStart, nextStart + 1, 0, 0, ipd, false, true,
                                        breakOpportunity, font, level, null);
                thisStart = nextStart + 1;
            } else if (CharUtilities.isExplicitBreak(ch)) {
                //mandatory break-character: only advance index
                thisStart = nextStart + 1;
            }

            inWord = !TextLayoutManager.isSpace(ch) && !CharUtilities.isExplicitBreak(ch);
            inWhitespace = ch == CharUtilities.SPACE
                    && foText.getWhitespaceTreatment() != Constants.EN_PRESERVE;
            prevLevel = level;
            nextStart++;
        }

        // Process any last elements
        if (inWord) {
            processWord(alignment, sequence, prevAreaInfo, ch, false, false, prevLevel);
        } else if (inWhitespace) {
            processWhitespace(alignment, sequence, !keepTogether, prevLevel);
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
        addAreaInfo(areaInfo);
        areaInfo.breakOppAfter = breakOpportunityAfter;
        addElementsForASpace(sequence, alignment, areaInfo, areaInfos.size() - 1);
    }

    private AreaInfo processWhitespace(final int alignment,
            final KnuthSequence sequence, final boolean breakOpportunity, int level) {

        if (LOG.isDebugEnabled()) {
            LOG.debug ( "PS: [" + thisStart + "," + nextStart + "]" );
        }

        // End of whitespace
        // create the AreaInfo object
        assert nextStart >= thisStart;
        AreaInfo areaInfo = new AreaInfo
            ( thisStart, nextStart, nextStart - thisStart, 0,
              wordSpaceIPD.mult(nextStart - thisStart),
              false, true, breakOpportunity, spaceFont, level, null );

        addAreaInfo(areaInfo);

        // create the elements
        addElementsForASpace(sequence, alignment, areaInfo, areaInfos.size() - 1);

        thisStart = nextStart;
        return areaInfo;
    }

    private AreaInfo processWordMapping
        ( int lastIndex, final Font font, AreaInfo prevAreaInfo, final char breakOpportunityChar,
          final boolean endsWithHyphen, int level ) {
        int s = this.thisStart; // start index of word in FOText character buffer
        int e = lastIndex;      // end index of word in FOText character buffer
        int nLS = 0;            // # of letter spaces
        String script = foText.getScript();
        String language = foText.getLanguage();

        if (LOG.isDebugEnabled()) {
            LOG.debug ( "PW: [" + thisStart + "," + lastIndex + "]: {"
                        + " +M"
                        + ", level = " + level
                        + " }" );
        }

        // 1. extract unmapped character sequence
        CharSequence ics = foText.subSequence ( s, e );

        // 2. if script is not specified (by FO property) or it is specified as 'auto',
        // then compute dominant script
        if ( ( script == null ) || "auto".equals(script) ) {
            script = CharScript.scriptTagFromCode ( CharScript.dominantScript ( ics ) );
        }
        if ( ( language == null ) || "none".equals(language) ) {
            language = "dflt";
        }

        // 3. perform mapping of chars to glyphs ... to glyphs ... to chars
        CharSequence mcs = font.performSubstitution ( ics, script, language );

        // 4. compute glyph position adjustments on (substituted) characters
        int[][] gpa;
        if ( font.performsPositioning() ) {
            // handle GPOS adjustments
            gpa = font.performPositioning ( mcs, script, language );
        } else if ( font.hasKerning() ) {
            // handle standard (non-GPOS) kerning adjustments
            gpa = getKerningAdjustments ( mcs, font );
        } else {
            gpa = null;
        }

        // 5. reorder combining marks so that they precede (within the mapped char sequence) the
        // base to which they are applied; N.B. position adjustments (gpa) are reordered in place
        mcs = font.reorderCombiningMarks ( mcs, gpa, script, language );

        // 6. if mapped sequence differs from input sequence, then memoize mapped sequence
        if ( !CharUtilities.isSameSequence ( mcs, ics ) ) {
            foText.addMapping ( s, e, mcs );
        }

        // 7. compute word ipd based on final position adjustments
        MinOptMax ipd = MinOptMax.ZERO;
        for ( int i = 0, n = mcs.length(); i < n; i++ ) {
            int c = mcs.charAt ( i );
            // TODO !BMP
            int  w = font.getCharWidth ( c );
            if ( w < 0 ) {
                w = 0;
            }
            if ( gpa != null ) {
                w += gpa [ i ] [ GlyphPositioningTable.Value.IDX_X_ADVANCE ];
            }
            ipd = ipd.plus ( w );
        }

        // [TBD] - handle letter spacing

        return new AreaInfo
            ( s, e, 0, nLS, ipd, endsWithHyphen, false,
              breakOpportunityChar != 0, font, level, gpa );
    }

    /**
     * Given a mapped character sequence MCS, obtain glyph position adjustments
     * from the font's kerning data.
     * @param mcs mapped character sequence
     * @param font applicable font
     * @return glyph position adjustments (or null if no kerning)
     */
    private int[][] getKerningAdjustments ( CharSequence mcs, final Font font ) {
        int nc = mcs.length();
        // extract kerning array
        int[] ka = new int [ nc ]; // kerning array
        for ( int i = 0, n = nc, cPrev = -1; i < n; i++ ) {
            int c = mcs.charAt ( i );
            // TODO !BMP
            if ( cPrev >= 0 ) {
                ka[i] = font.getKernValue ( cPrev, c );
            }
            cPrev = c;
        }
        // was there a non-zero kerning?
        boolean hasKerning = false;
        for ( int i = 0, n = nc; i < n; i++ ) {
            if ( ka[i] != 0 ) {
                hasKerning = true;
                break;
            }
        }
        // if non-zero kerning, then create and return glyph position adjustment array
        if ( hasKerning ) {
            int[][] gpa = new int [ nc ] [ 4 ];
            for ( int i = 0, n = nc; i < n; i++ ) {
                if ( i > 0 ) {
                    gpa [ i - 1 ] [ GlyphPositioningTable.Value.IDX_X_ADVANCE ] = ka [ i ];
                }
            }
            return gpa;
        } else {
            return null;
        }
    }

    private AreaInfo processWordNoMapping(int lastIndex, final Font font, AreaInfo prevAreaInfo,
            final char breakOpportunityChar, final boolean endsWithHyphen, int level) {
        boolean kerning = font.hasKerning();
        MinOptMax wordIPD = MinOptMax.ZERO;

        if (LOG.isDebugEnabled()) {
            LOG.debug ( "PW: [" + thisStart + "," + lastIndex + "]: {"
                        + " -M"
                        + ", level = " + level
                        + " }" );
        }

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
                && ( breakOpportunityChar != 0 )
                && !TextLayoutManager.isSpace(breakOpportunityChar)
                && lastIndex > 0
                && endsWithHyphen) {
            int kern = font.getKernValue(foText.charAt(lastIndex - 1), breakOpportunityChar);
            if (kern != 0) {
                addToLetterAdjust(lastIndex, kern);
                //TODO: add kern to wordIPD?
            }
        }
        // shy+chars at start of word: wordLength == 0 && breakOpportunity
        // shy only characters in word: wordLength == 0 && !breakOpportunity
        int wordLength = lastIndex - thisStart;
        int letterSpaces = 0;
        if (wordLength != 0) {
            letterSpaces = wordLength - 1;
            // if there is a break opportunity and the next one (break character)
            // is not a space, it could be used as a line end;
            // add one more letter space, in case other text follows
            if (( breakOpportunityChar != 0 ) && !TextLayoutManager.isSpace(breakOpportunityChar)) {
                  letterSpaces++;
            }
        }
        assert letterSpaces >= 0;
        wordIPD = wordIPD.plus(letterSpaceIPD.mult(letterSpaces));

        // create and return the AreaInfo object
        return new AreaInfo(thisStart, lastIndex, 0,
                            letterSpaces, wordIPD,
                            endsWithHyphen,
                            false, breakOpportunityChar != 0, font, level, null);
    }

    private AreaInfo processWord(final int alignment, final KnuthSequence sequence,
            AreaInfo prevAreaInfo, final char ch, final boolean breakOpportunity,
            final boolean checkEndsWithHyphen, int level) {

        //Word boundary found, process widths and kerning
        int lastIndex = nextStart;
        while (lastIndex > 0 && foText.charAt(lastIndex - 1) == CharUtilities.SOFT_HYPHEN) {
            lastIndex--;
        }
        final boolean endsWithHyphen = checkEndsWithHyphen
                && foText.charAt(lastIndex) == CharUtilities.SOFT_HYPHEN;
        Font font = FontSelector.selectFontForCharactersInText
            ( foText, thisStart, lastIndex, foText, this );
        AreaInfo areaInfo;
        if ( font.performsSubstitution() || font.performsPositioning() ) {
            areaInfo = processWordMapping
                ( lastIndex, font, prevAreaInfo, breakOpportunity ? ch : 0, endsWithHyphen, level );
        } else {
            areaInfo = processWordNoMapping
                ( lastIndex, font, prevAreaInfo, breakOpportunity ? ch : 0, endsWithHyphen, level );
        }
        prevAreaInfo = areaInfo;
        addAreaInfo(areaInfo);
        tempStart = nextStart;

        //add the elements
        addElementsForAWordFragment(sequence, alignment, areaInfo, areaInfos.size() - 1);
        thisStart = nextStart;

        return prevAreaInfo;
    }

    /** {@inheritDoc} */
    public List addALetterSpaceTo(List oldList) {
        return addALetterSpaceTo(oldList, 0);
    }

    /** {@inheritDoc} */
    public List addALetterSpaceTo(final List oldList, int depth) {
        // old list contains only a box, or the sequence: box penalty glue box;
        // look at the Position stored in the first element in oldList
        // which is always a box
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement knuthElement = (KnuthElement) oldListIterator.next();
        Position pos = knuthElement.getPosition();
        LeafPosition leafPos = (LeafPosition) pos.getPosition(depth);
        int index = leafPos.getLeafPos();
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
                // give it the unwrapped position of the replaced element
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

    /** {@inheritDoc} */
    public void hyphenate(Position pos, HyphContext hyphContext) {
        AreaInfo areaInfo = getAreaInfo(((LeafPosition) pos).getLeafPos() + changeOffset);
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
                    MinOptMax letterSpaceAdjust = letterSpaceAdjustArray[i + 1];
                    if (i == stopIndex - 1 && hyphenFollows) {
                        //the letter adjust here needs to be handled further down during
                        //element generation because it depends on hyph/no-hyph condition
                        letterSpaceAdjust = null;
                    }
                    if (letterSpaceAdjust != null) {
                        newIPD = newIPD.plus(letterSpaceAdjust);
                    }
                }
            }

            // add letter spaces
            boolean isWordEnd
                = (stopIndex == areaInfo.breakIndex)
                && (areaInfo.letterSpaceCount < areaInfo.getWordLength());
            int letterSpaceCount = isWordEnd ? stopIndex - startIndex - 1 : stopIndex - startIndex;

            assert letterSpaceCount >= 0;
            newIPD = newIPD.plus(letterSpaceIPD.mult(letterSpaceCount));

            if (!(nothingChanged && stopIndex == areaInfo.breakIndex && !hyphenFollows)) {
                // the new AreaInfo object is not equal to the old one
                changeList.add
                    ( new PendingChange
                      ( new AreaInfo(startIndex, stopIndex, 0,
                                     letterSpaceCount, newIPD, hyphenFollows,
                                     false, false, font, -1, null),
                        ((LeafPosition) pos).getLeafPos() + changeOffset));
                nothingChanged = false;
            }
            startIndex = stopIndex;
        }
        hasChanged |= !nothingChanged;
    }

    /** {@inheritDoc} */
    public boolean applyChanges(final List oldList) {
        return applyChanges(oldList, 0);
    }

    /** {@inheritDoc} */
    public boolean applyChanges(final List oldList, int depth) {

        // make sure the LM appears unfinished in between this call
        // and the next call to getChangedKnuthElements()
        setFinished(false);

        if (oldList.isEmpty()) {
            return false;
        }

        // Find the first and last positions in oldList that point to an AreaInfo
        // (i.e. getLeafPos() != -1)
        LeafPosition startPos = null;
        LeafPosition endPos = null;
        ListIterator oldListIter;
        for (oldListIter = oldList.listIterator(); oldListIter.hasNext();) {
            Position pos = ((KnuthElement) oldListIter.next()).getPosition();
            startPos = (LeafPosition) pos.getPosition(depth);
            if (startPos != null && startPos.getLeafPos() != -1) {
                break;
            }
        }
        for (oldListIter = oldList.listIterator(oldList.size()); oldListIter.hasPrevious();) {
            Position pos = ((KnuthElement) oldListIter.previous()).getPosition();
            endPos = (LeafPosition) pos.getPosition(depth);
            if (endPos != null && endPos.getLeafPos() != -1) {
                break;
            }
        }

        // set start/end index, taking into account any offset due to
        // changes applied to previous paragraphs
        returnedIndices[0] = (startPos != null ? startPos.getLeafPos() : -1) + changeOffset;
        returnedIndices[1] = (endPos != null ? endPos.getLeafPos() : -1) + changeOffset;

        int areaInfosAdded = 0;
        int areaInfosRemoved = 0;

        if (!changeList.isEmpty()) {
            int oldIndex = -1;
            int changeIndex;
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
                    removeAreaInfo(changeIndex);
                }
                addAreaInfo(changeIndex, currChange.areaInfo);
            }
            changeList.clear();
        }

        // increase the end index for getChangedKnuthElements()
        returnedIndices[1] += (areaInfosAdded - areaInfosRemoved);
        // increase offset to use for subsequent paragraphs
        changeOffset += (areaInfosAdded - areaInfosRemoved);

        return hasChanged;
    }

    /** {@inheritDoc} */
    public List getChangedKnuthElements(final List oldList, final int alignment) {
        if (isFinished()) {
            return null;
        }

        final LinkedList returnList = new LinkedList();

        for (; returnedIndices[0] <= returnedIndices[1]; returnedIndices[0]++) {
            AreaInfo areaInfo = getAreaInfo(returnedIndices[0]);
            if (areaInfo.wordSpaceCount == 0) {
                // areaInfo refers either to a word or a word fragment
                addElementsForAWordFragment(returnList, alignment, areaInfo, returnedIndices[0]);
            } else {
                // areaInfo refers to a space
                addElementsForASpace(returnList, alignment, areaInfo, returnedIndices[0]);
            }
        }
        setFinished(returnedIndices[0] == areaInfos.size() - 1);
        //ElementListObserver.observe(returnList, "text-changed", null);
        return returnList;
    }

    /** {@inheritDoc} */
    public String getWordChars(Position pos) {
        int leafValue = ((LeafPosition) pos).getLeafPos() + changeOffset;
        if (leafValue != -1) {
            AreaInfo areaInfo = getAreaInfo(leafValue);
            StringBuffer buffer = new StringBuffer(areaInfo.getWordLength());
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
                widthIfNoBreakOccurs = letterSpaceAdjustArray[areaInfo.breakIndex];
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

    /** {@inheritDoc} */
    public String toString() {
        return super.toString() + "{"
            + "chars = \'"
            + CharUtilities.toNCRefs ( foText.getCharSequence().toString() )
            + "\'"
            + ", len = " + foText.length()
            + "}";
    }

}
