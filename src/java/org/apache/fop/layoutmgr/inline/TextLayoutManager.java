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
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FOText;
import org.apache.fop.fonts.Font;
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
        private boolean isSpace;
        private boolean breakOppAfter;
        public AreaInfo(short iSIndex, short iBIndex, short iWS, short iLS,
                        MinOptMax ipd, boolean bHyph, boolean isSpace, boolean breakOppAfter) {
            iStartIndex = iSIndex;
            iBreakIndex = iBIndex;
            iWScount = iWS;
            iLScount = iLS;
            ipdArea = ipd;
            bHyphenated = bHyph;
            this.isSpace = isSpace;
            this.breakOppAfter = breakOppAfter;
        }
        
        public String toString() {
            return "[ lscnt=" +  iLScount
                + ", wscnt=" + iWScount
                + ", ipd=" + ipdArea.toString()
                + ", sidx=" + iStartIndex
                + ", bidx=" + iBreakIndex
                + ", hyph=" + bHyphenated
                + ", space=" + isSpace
                + "]";
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

    /**
     * logging instance
     */
    private static Log log = LogFactory.getLog(TextLayoutManager.class);

    // Hold all possible breaks for the text in this LM's FO.
    private ArrayList vecAreaInfo;

    /** Non-space characters on which we can end a line. */
    private static final String BREAK_CHARS = "-/";

    /** Used to reduce instantiation of MinOptMax with zero length. Do not modify! */
    private static final MinOptMax ZERO_MINOPTMAX = new MinOptMax(0);
    
    private FOText foText;
    private char[] textArray;
    /**
     * Contains an array of widths to adjust for kerning. The first entry can
     * be used to influence the start position of the first letter. The entry i+1 defines the
     * cursor advancement after the character i. A null entry means no special advancement.
     */
    private MinOptMax[] letterAdjustArray; //size = textArray.length + 1

    private static final char NEWLINE = '\n';

    private Font font = null;
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
    /** 1/1 of word-spacing value */
    private SpaceVal ws;
    /** 1/2 of word-spacing value */
    private SpaceVal halfWS;
    /** 1/2 of letter-spacing value */
    private SpaceVal halfLS; 
    /** Number of space characters after previous possible break position. */
    private int iNbSpacesPending;

    private boolean bChanged = false;
    private int iReturnedIndex = 0;
    private short iThisStart = 0;
    private short iTempStart = 0;
    private LinkedList changeList = null;

    private AlignmentContext alignmentContext = null;

    private int lineStartBAP = 0;
    private int lineEndBAP = 0;
    
    private boolean keepTogether;

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
        letterAdjustArray = new MinOptMax[textArray.length + 1];

        vecAreaInfo = new java.util.ArrayList();
    }
    
    /** @see org.apache.fop.layoutmgr.LayoutManager#initialize */
    public void initialize() {
        font = foText.getCommonFont().getFontState(foText.getFOEventHandler().getFontInfo(), this);
        
        // With CID fonts, space isn't neccesary currentFontState.width(32)
        spaceCharIPD = font.getCharWidth(' ');
        // Use hyphenationChar property
        hyphIPD = font.getCharWidth(foText.getCommonHyphenation().hyphenationCharacter);
        
        SpaceVal ls = SpaceVal.makeLetterSpacing(foText.getLetterSpacing());
        halfLS = new SpaceVal(MinOptMax.multiply(ls.getSpace(), 0.5),
                ls.isConditional(), ls.isForcing(), ls.getPrecedence());
        
        ws = SpaceVal.makeWordSpacing(foText.getWordSpacing(), ls, font);
        // Make half-space: <space> on either side of a word-space)
        halfWS = new SpaceVal(MinOptMax.multiply(ws.getSpace(), 0.5),
                ws.isConditional(), ws.isForcing(), ws.getPrecedence());

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
        letterSpaceIPD = ls.getSpace();
        wordSpaceIPD = MinOptMax.add(new MinOptMax(spaceCharIPD), ws.getSpace());
        
        keepTogether = foText.getKeepTogether().getWithinLine().getEnum() == Constants.EN_ALWAYS;

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
            AreaInfo ai = (AreaInfo) vecAreaInfo.get(tbp.getLeafPos());
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
            hyphIPD.opt += font.getCharWidth(c);
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
        int iWScount = 0;
        int iLScount = 0;
        int firstAreaInfoIndex = -1;
        int lastAreaInfoIndex = 0;
        MinOptMax realWidth = new MinOptMax(0);

        /* On first area created, add any leading space.
         * Calculate word-space stretch value.
         */
        while (posIter.hasNext()) {
            LeafPosition tbpNext = (LeafPosition) posIter.next();
            if (tbpNext == null) {
                continue; //Ignore elements without Positions
            }
            if (tbpNext.getLeafPos() != -1) {
                ai = (AreaInfo) vecAreaInfo.get(tbpNext.getLeafPos());
                if (firstAreaInfoIndex == -1) {
                    firstAreaInfoIndex = tbpNext.getLeafPos();
                }
                iWScount += ai.iWScount;
                iLScount += ai.iLScount;
                realWidth.add(ai.ipdArea);
                lastAreaInfoIndex = tbpNext.getLeafPos();
            }
        }
        if (ai == null) {
            return;
        }
        int textLength = ai.iBreakIndex - ai.iStartIndex;
        if (ai.iLScount == textLength && !ai.bHyphenated
                   && context.isLastArea()) {
            // the line ends at a character like "/" or "-";
            // remove the letter space after the last character
            realWidth.add(MinOptMax.multiply(letterSpaceIPD, -1));
            iLScount--;
        }
        
        for (int i = ai.iStartIndex; i < ai.iBreakIndex; i++) {
            MinOptMax ladj = letterAdjustArray[i + 1]; 
            if (ladj != null && ladj.isElastic()) {
                iLScount++;
            }
        }

        // add hyphenation character if the last word is hyphenated
        if (context.isLastArea() && ai.bHyphenated) {
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
        if (iTotalAdjust != iDifference) {
            // the applied adjustment is greater or smaller than the needed one
            log.trace("TextLM.addAreas: error in word / letter space adjustment = "
                    + (iTotalAdjust - iDifference));
            // set iTotalAdjust = iDifference, so that the width of the TextArea
            // will counterbalance the error and the other inline areas will be
            // placed correctly
            iTotalAdjust = iDifference;
        }

        TextArea t = createTextArea(realWidth, iTotalAdjust, context,
                                    wordSpaceIPD.opt - spaceCharIPD,
                                    firstAreaInfoIndex, lastAreaInfoIndex,
                                    context.isLastArea());

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
        parentLM.addChildArea(t);
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
     * @return the new text area
     */
    protected TextArea createTextArea(MinOptMax width, int adjust,
                                      LayoutContext context, int spaceDiff,
                                      int firstIndex, int lastIndex, boolean isLastArea) {
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
        if (textArea.getBPD() == alignmentContext.getHeight()) {
            textArea.setOffset(0);
        } else {
            textArea.setOffset(alignmentContext.getOffset());
        }

        // set the text of the TextArea, split into words and spaces
        int wordStartIndex = -1;
        AreaInfo areaInfo;
        int len = 0;
        for (int i = firstIndex; i <= lastIndex; i++) {
            areaInfo = (AreaInfo) vecAreaInfo.get(i);
            if (areaInfo.isSpace) {
                // areaInfo stores information about spaces
                // add the spaces - except zero-width spaces - to the TextArea
                for (int j = areaInfo.iStartIndex; j < areaInfo.iBreakIndex; j++) {
                    char spaceChar = textArray[j];
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
                len += areaInfo.iBreakIndex - areaInfo.iStartIndex;
                if (i == lastIndex || ((AreaInfo) vecAreaInfo.get(i + 1)).isSpace) {
                    // here ends a new word
                    // add a word to the TextArea
                    if (isLastArea
                        && i == lastIndex 
                        && areaInfo.bHyphenated) {
                        len++;
                    }
                    StringBuffer wordChars = new StringBuffer(len);
                    int[] letterAdjust = new int[len];
                    int letter = 0;
                    for (int j = wordStartIndex; j <= i; j++) {
                        AreaInfo ai = (AreaInfo) vecAreaInfo.get(j);
                        int lsCount = ai.iLScount;
                        wordChars.append(textArray, ai.iStartIndex, ai.iBreakIndex - ai.iStartIndex);
                        for (int k = 0; k < ai.iBreakIndex - ai.iStartIndex; k++) {
                            MinOptMax adj = letterAdjustArray[ai.iStartIndex + k];
                            if (letter > 0) {
                                letterAdjust[letter] = (adj != null ? adj.opt : 0);
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
                        && areaInfo.bHyphenated) {
                        // add the hyphenation character
                        wordChars.append(foText.getCommonHyphenation().hyphenationCharacter);
                    }
                    textArea.addWord(wordChars.toString(), 0, letterAdjust);
                    wordStartIndex = -1;
                }
            }
        }
        TraitSetter.addFontTraits(textArea, font);
        textArea.addTrait(Trait.COLOR, foText.getColor());
        
        TraitSetter.addTextDecoration(textArea, foText.getTextDecoration());
        
        return textArea;
    }
    
    private void addToLetterAdjust(int index, int width) {
        if (letterAdjustArray[index] == null) {
            letterAdjustArray[index] = new MinOptMax(width);
        } else {
            letterAdjustArray[index].add(width);
        }
    }

    private void addToLetterAdjust(int index, MinOptMax width) {
        if (letterAdjustArray[index] == null) {
            letterAdjustArray[index] = new MinOptMax(width);
        } else {
            letterAdjustArray[index].add(width);
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
    
    /** @see org.apache.fop.layoutmgr.LayoutManager#getNextKnuthElements(LayoutContext, int) */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        lineStartBAP = context.getLineStartBorderAndPaddingWidth();
        lineEndBAP = context.getLineEndBorderAndPaddingWidth();
        alignmentContext = context.getAlignmentContext();

        LinkedList returnList = new LinkedList();
        KnuthSequence sequence = new InlineKnuthSequence();
        AreaInfo ai = null;
        AreaInfo prevAi = null;
        returnList.add(sequence);

        LineBreakStatus lbs = new LineBreakStatus();
        iThisStart = iNextStart;
        boolean inWord = false;
        boolean inWhitespace = false;
        char ch = 0; 
        while (iNextStart < textArray.length) {
            ch = textArray[iNextStart]; 
            boolean breakOpportunity = false;
            byte breakAction = keepTogether? LineBreakStatus.PROHIBITED_BREAK : lbs.nextChar(ch);
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
                    log.error("Unexpected breakAction: " + breakAction);
            }
            if (inWord) {
                if (breakOpportunity || isSpace(ch) || ch == NEWLINE) {
                    //Word boundary found, process widths and kerning
                    int lastIndex = iNextStart;
                    while (lastIndex > 0 && textArray[lastIndex - 1] == CharUtilities.SOFT_HYPHEN) {
                        lastIndex--;
                    }
                    int wordLength = lastIndex - iThisStart;
                    boolean kerning = font.hasKerning();
                    MinOptMax wordIPD = new MinOptMax(0);
                    for (int i = iThisStart; i < lastIndex; i++) {
                        char c = textArray[i];

                        //character width
                        int charWidth = font.getCharWidth(c);
                        wordIPD.add(charWidth);

                        //kerning
                        if (kerning) {
                            int kern = 0;
                            if (i > iThisStart) {
                                char previous = textArray[i - 1];
                                kern = font.getKernValue(previous, c) * font.getFontSize() / 1000;
                            } else if (prevAi != null && !prevAi.isSpace && prevAi.iBreakIndex > 0) {
                                char previous = textArray[prevAi.iBreakIndex - 1];
                                kern = font.getKernValue(previous, c) * font.getFontSize() / 1000;
                            }
                            if (kern != 0) {
                                //log.info("Kerning between " + previous + " and " + c + ": " + kern);
                                addToLetterAdjust(i, kern);
                                wordIPD.add(kern);
                            }
                        }
                    }
                    if (kerning && breakOpportunity && !isSpace(ch) && lastIndex > 0 && textArray[lastIndex] == CharUtilities.SOFT_HYPHEN) {
                        int kern = font.getKernValue(textArray[lastIndex - 1], ch) * font.getFontSize() / 1000;
                        if (kern != 0) {
                            addToLetterAdjust(lastIndex, kern);
                        }
                    }
                    int iLetterSpaces = wordLength - 1;
                    // if there is a break opportunity and the next one
                    // is not a space, it could be used as a line end;
                    // add one more letter space, in case other text follows
                    if (breakOpportunity && !isSpace(ch)) {
                        iLetterSpaces++;
                    }
                    wordIPD.add(MinOptMax.multiply(letterSpaceIPD, iLetterSpaces));

                    // create the AreaInfo object
                    ai = new AreaInfo(iThisStart, (short)lastIndex, (short) 0,
                            (short) iLetterSpaces,
                            wordIPD, textArray[lastIndex] == CharUtilities.SOFT_HYPHEN, false, breakOpportunity);
                    vecAreaInfo.add(ai);
                    prevAi = ai;
                    iTempStart = iNextStart;

                    // create the elements
                    sequence.addAll(createElementsForAWordFragment(alignment, ai,
                            vecAreaInfo.size() - 1, letterSpaceIPD));
                    ai = null;

                    iThisStart = iNextStart;
                }
            } else if (inWhitespace) {
                if (ch != CharUtilities.SPACE || breakOpportunity) {
                    // End of whitespace
                    // create the AreaInfo object
                    ai = new AreaInfo(iThisStart, (short) (iNextStart),
                            (short) (iNextStart - iThisStart), (short) 0,
                            MinOptMax.multiply(wordSpaceIPD, iNextStart - iThisStart),
                            false, true, breakOpportunity); 
                    vecAreaInfo.add(ai);
                    prevAi = ai;

                    // create the elements
                    sequence.addAll
                        (createElementsForASpace(alignment, ai, vecAreaInfo.size() - 1));
                    ai = null;

                    iThisStart = iNextStart;
                }
            } else {
                if (ai != null) {
                    vecAreaInfo.add(ai);
                    prevAi = ai;
                    ai.breakOppAfter = ch == CharUtilities.SPACE || breakOpportunity;
                    sequence.addAll
                        (createElementsForASpace(alignment, ai, vecAreaInfo.size() - 1));
                    ai = null;
                }
                if (breakAction == LineBreakStatus.EXPLICIT_BREAK) {
                    if (lineEndBAP != 0) {
                        sequence.add
                            (new KnuthGlue(lineEndBAP, 0, 0,
                                           new LeafPosition(this, -1), true));
                    }
                    sequence.endSequence();
                    sequence = new InlineKnuthSequence();
                    returnList.add(sequence);
                }
            }
            
            if ((ch == CharUtilities.SPACE 
                    && foText.getWhitespaceTreatment() == Constants.EN_PRESERVE) 
                    || ch == CharUtilities.NBSPACE) {
                // preserved space or non-breaking space:
                // create the AreaInfo object
                ai = new AreaInfo(iNextStart, (short) (iNextStart + 1),
                        (short) 1, (short) 0,
                        wordSpaceIPD, false, true, breakOpportunity);
                iThisStart = (short) (iNextStart + 1);
            } else if (CharUtilities.isFixedWidthSpace(ch) || CharUtilities.isZeroWidthSpace(ch)) {
                // create the AreaInfo object
                MinOptMax ipd = new MinOptMax(font.getCharWidth(ch));
                ai = new AreaInfo(iNextStart, (short) (iNextStart + 1),
                        (short) 0, (short) 0,
                        ipd, false, true, breakOpportunity); 
                iThisStart = (short) (iNextStart + 1);
            } else if (ch == NEWLINE) {
                // linefeed; this can happen when linefeed-treatment="preserve"
                iThisStart = (short) (iNextStart + 1);
            }
            inWord = !isSpace(ch) && ch != NEWLINE;
            inWhitespace = ch == CharUtilities.SPACE && foText.getWhitespaceTreatment() != Constants.EN_PRESERVE;
            iNextStart++;
        } // end of while
        
        // Process any last elements
        if (inWord) {
            int lastIndex = iNextStart;
            if (textArray[iNextStart - 1] == CharUtilities.SOFT_HYPHEN) {
                lastIndex--;
            }
            int wordLength = lastIndex - iThisStart;
            boolean kerning = font.hasKerning();
            MinOptMax wordIPD = new MinOptMax(0);
            for (int i = iThisStart; i < lastIndex; i++) {
                char c = textArray[i];

                //character width
                int charWidth = font.getCharWidth(c);
                wordIPD.add(charWidth);

                //kerning
                if (kerning) {
                    int kern = 0;
                    if (i > iThisStart) {
                        char previous = textArray[i - 1];
                        kern = font.getKernValue(previous, c) * font.getFontSize() / 1000;
                    } else if (prevAi != null && !prevAi.isSpace) {
                        char previous = textArray[prevAi.iBreakIndex - 1];
                        kern = font.getKernValue(previous, c) * font.getFontSize() / 1000;
                    }
                    if (kern != 0) {
                        //log.info("Kerning between " + previous + " and " + c + ": " + kern);
                        addToLetterAdjust(i, kern);
                        wordIPD.add(kern);
                    }
                }
            }
            int iLetterSpaces = wordLength - 1;
            wordIPD.add(MinOptMax.multiply(letterSpaceIPD, iLetterSpaces));

            // create the AreaInfo object
            ai = new AreaInfo(iThisStart, (short)lastIndex, (short) 0,
                    (short) iLetterSpaces,
                    wordIPD, false, false, false);
            vecAreaInfo.add(ai);
            iTempStart = iNextStart;

            // create the elements
            sequence.addAll(createElementsForAWordFragment(alignment, ai,
                    vecAreaInfo.size() - 1, letterSpaceIPD));
            ai = null;
        } else if (inWhitespace) {
            ai = new AreaInfo(iThisStart, (short) (iNextStart),
                    (short) (iNextStart - iThisStart), (short) 0,
                    MinOptMax.multiply(wordSpaceIPD, iNextStart - iThisStart),
                    false, true, true); 
            vecAreaInfo.add(ai);

            // create the elements
            sequence.addAll
                (createElementsForASpace(alignment, ai, vecAreaInfo.size() - 1));
            ai = null;
        } else if (ai != null) {
            vecAreaInfo.add(ai);
            ai.breakOppAfter = ch == CharUtilities.ZERO_WIDTH_SPACE;
            sequence.addAll
                (createElementsForASpace(alignment, ai, vecAreaInfo.size() - 1));
            ai = null;
        } else if (ch == NEWLINE) {
            if (lineEndBAP != 0) {
                sequence.add
                    (new KnuthGlue(lineEndBAP, 0, 0,
                                   new LeafPosition(this, -1), true));
            }
            sequence.endSequence();
            sequence = new InlineKnuthSequence();
            returnList.add(sequence);
        }

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

    /** @see InlineLevelLayoutManager#addALetterSpaceTo(List) */
    public List addALetterSpaceTo(List oldList) {
        // old list contains only a box, or the sequence: box penalty glue box;
        // look at the Position stored in the first element in oldList
        // which is always a box
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement el = (KnuthElement)oldListIterator.next();
        LeafPosition pos = (LeafPosition) ((KnuthBox) el).getPosition();
        AreaInfo ai = (AreaInfo) vecAreaInfo.get(pos.getLeafPos());
        ai.iLScount++;
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
            oldListIterator.set(new KnuthInlineBox(ai.ipdArea.opt, alignmentContext, pos, false));
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

    /**
     * remove the AreaInfo object represented by the given elements,
     * so that it won't generate any element when getChangedKnuthElements
     * will be called
     *
     * @param oldList the elements representing the word space
     */
    public void removeWordSpace(List oldList) {
        // find the element storing the Position whose value
        // points to the AreaInfo object
        ListIterator oldListIterator = oldList.listIterator();
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
        int leafValue = ((LeafPosition) ((KnuthElement) oldListIterator.next()).getPosition()).getLeafPos();
        // only the last word space can be a trailing space!
        if (leafValue == vecAreaInfo.size() - 1) {
            vecAreaInfo.remove(leafValue);
        } else {
            log.error("trying to remove a non-trailing word space");
        }
    }

    /** @see InlineLevelLayoutManager#hyphenate(Position, HyphContext) */
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

            //log.info("Word: " + new String(textArray, iStartIndex, iStopIndex - iStartIndex));
            for (int i = iStartIndex; i < iStopIndex; i++) {
                char c = textArray[i];
                newIPD.add(new MinOptMax(font.getCharWidth(c)));
                //if (i > iStartIndex) {
                if (i < iStopIndex) {
                    MinOptMax la = this.letterAdjustArray[i + 1];
                    if ((i == iStopIndex - 1) && bHyphenFollows) {
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
                                   newIPD, bHyphenFollows, false, false),
                      ((LeafPosition) pos).getLeafPos()));
                bNothingChanged = false;
            }
            iStartIndex = iStopIndex;
        }
        if (!bChanged && !bNothingChanged) {
            bChanged = true;
        }
    }

    /** @see InlineLevelLayoutManager#applyChanges(List) */
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
                    iRemovedAI++;
                    iAddedAI++;
                    iOldIndex = currChange.index;
                    vecAreaInfo.remove(currChange.index + iAddedAI - iRemovedAI);
                    vecAreaInfo.add(currChange.index + iAddedAI - iRemovedAI,
                                    currChange.ai);
                } else {
                    iAddedAI++;
                    vecAreaInfo.add(currChange.index + iAddedAI - iRemovedAI,
                                    currChange.ai);
                }
            }
            changeList.clear();
        }

        iReturnedIndex = 0;
        return bChanged;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager#getChangedKnuthElements(List, int) */
    public LinkedList getChangedKnuthElements(List oldList,
                                              int alignment) {
        if (isFinished()) {
            return null;
        }

        LinkedList returnList = new LinkedList();

        while (iReturnedIndex < vecAreaInfo.size()) {
            AreaInfo ai = (AreaInfo) vecAreaInfo.get(iReturnedIndex);
            if (ai.iWScount == 0) {
                // ai refers either to a word or a word fragment
                returnList.addAll
                (createElementsForAWordFragment(alignment, ai, iReturnedIndex, letterSpaceIPD));
            } else {
                // ai refers to a space
                returnList.addAll
                (createElementsForASpace(alignment, ai, iReturnedIndex));
            }
            iReturnedIndex++;
        } // end of while
        setFinished(true);
        //ElementListObserver.observe(returnList, "text-changed", null);
        return returnList;
    }

    /** @see InlineLevelLayoutManager#getWordChars(StringBuffer, Position) */
    public void getWordChars(StringBuffer sbChars, Position pos) {
        int iLeafValue = ((LeafPosition) pos).getLeafPos();
        if (iLeafValue != -1) {
            AreaInfo ai = (AreaInfo) vecAreaInfo.get(iLeafValue);
            sbChars.append(new String(textArray, ai.iStartIndex,
                                      ai.iBreakIndex - ai.iStartIndex));
        }
    }

    private LinkedList createElementsForASpace(int alignment,
            AreaInfo ai, int leafValue) {
        LinkedList spaceElements = new LinkedList();
        LeafPosition mainPosition = new LeafPosition(this, leafValue);
        
        if (!ai.breakOppAfter) {
            // a non-breaking space
            if (alignment == EN_JUSTIFY) {
                // the space can stretch and shrink, and must be preserved
                // when starting a line
                spaceElements.add(new KnuthInlineBox(0, null,
                        notifyPos(new LeafPosition(this, -1)), true));
                spaceElements.add(new KnuthPenalty(0, KnuthElement.INFINITE,
                        false, new LeafPosition(this, -1), false));
                spaceElements.add(new KnuthGlue(ai.ipdArea.opt, ai.ipdArea.max - ai.ipdArea.opt, 
                        ai.ipdArea.opt - ai.ipdArea.min, mainPosition, false));
            } else {
                // the space does not need to stretch or shrink, and must be
                // preserved when starting a line
                spaceElements.add(new KnuthInlineBox(ai.ipdArea.opt, null,
                        mainPosition, true));
            }
        } else {
            if (textArray[ai.iStartIndex] != CharUtilities.SPACE
                    || foText.getWhitespaceTreatment() == Constants.EN_PRESERVE) {
                // a breaking space that needs to be preserved
                switch (alignment) {
                case EN_CENTER:
                    // centered text:
                    // if the second element is chosen as a line break these elements 
                    // add a constant amount of stretch at the end of a line and at the
                    // beginning of the next one, otherwise they don't add any stretch
                    spaceElements.add(new KnuthGlue(lineEndBAP,
                            3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            new LeafPosition(this, -1), false));
                    spaceElements
                            .add(new KnuthPenalty(
                                    0,
                                    0, false,
                                    new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthGlue(
                            - (lineStartBAP + lineEndBAP), -6
                            * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthInlineBox(0, null,
                            notifyPos(new LeafPosition(this, -1)), false));
                    spaceElements.add(new KnuthPenalty(0, KnuthElement.INFINITE,
                            false, new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthGlue(ai.ipdArea.opt + lineStartBAP,
                            3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            mainPosition, false));
                    break;

                case EN_START: // fall through
                case EN_END:
                    // left- or right-aligned text:
                    // if the second element is chosen as a line break these elements 
                    // add a constant amount of stretch at the end of a line, otherwise
                    // they don't add any stretch
                    spaceElements.add(new KnuthGlue(lineEndBAP,
                            3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthPenalty(0, 0, false,
                            new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthGlue(
                            - (lineStartBAP + lineEndBAP), -3
                            * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthInlineBox(0, null,
                            notifyPos(new LeafPosition(this, -1)), false));
                    spaceElements.add(new KnuthPenalty(0,
                            KnuthElement.INFINITE, false, new LeafPosition(
                                    this, -1), false));
                    spaceElements.add(new KnuthGlue(ai.ipdArea.opt + lineStartBAP, 0, 0,
                            mainPosition, false));
                    break;

                case EN_JUSTIFY:
                    // justified text:
                    // the stretch and shrink depends on the space width
                    spaceElements.add(new KnuthGlue(lineEndBAP, 0, 0,
                            new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthPenalty(0, 0, false,
                            new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthGlue(
                            - (lineStartBAP + lineEndBAP), ai.ipdArea.max
                            - ai.ipdArea.opt, ai.ipdArea.opt - ai.ipdArea.min,
                            new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthInlineBox(0, null,
                            notifyPos(new LeafPosition(this, -1)), false));
                    spaceElements.add(new KnuthPenalty(0,
                            KnuthElement.INFINITE, false, new LeafPosition(
                                    this, -1), false));
                    spaceElements.add(new KnuthGlue(lineStartBAP + ai.ipdArea.opt, 0, 0,
                            mainPosition, false));
                    break;

                default:
                    // last line justified, the other lines unjustified:
                    // use only the space stretch
                    spaceElements.add(new KnuthGlue(lineEndBAP, 0, 0,
                            new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthPenalty(0, 0, false,
                            new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthGlue(
                            - (lineStartBAP + lineEndBAP), ai.ipdArea.max
                            - ai.ipdArea.opt, 0,
                            new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthInlineBox(0, null,
                            notifyPos(new LeafPosition(this, -1)), false));
                    spaceElements.add(new KnuthPenalty(0,
                            KnuthElement.INFINITE, false, new LeafPosition(
                                    this, -1), false));
                    spaceElements.add(new KnuthGlue(lineStartBAP + ai.ipdArea.opt, 0, 0,
                            mainPosition, false));
                } 
            } else {
                // a (possible block) of breaking spaces
                switch (alignment) {
                case EN_CENTER:
                    // centered text:
                    // if the second element is chosen as a line break these elements 
                    // add a constant amount of stretch at the end of a line and at the
                    // beginning of the next one, otherwise they don't add any stretch
                    spaceElements.add(new KnuthGlue(lineEndBAP,
                            3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            new LeafPosition(this, -1), false));
                    spaceElements
                            .add(new KnuthPenalty(
                                    0, 0, false,
                                    new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthGlue(ai.ipdArea.opt
                            - (lineStartBAP + lineEndBAP), -6
                            * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            mainPosition, false));
                    spaceElements.add(new KnuthInlineBox(0, null,
                            notifyPos(new LeafPosition(this, -1)), false));
                    spaceElements.add(new KnuthPenalty(0, KnuthElement.INFINITE,
                            false, new LeafPosition(this, -1), false));
                    spaceElements.add(new KnuthGlue(lineStartBAP,
                            3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            new LeafPosition(this, -1), false));
                    break;

                case EN_START: // fall through
                case EN_END:
                    // left- or right-aligned text:
                    // if the second element is chosen as a line break these elements 
                    // add a constant amount of stretch at the end of a line, otherwise
                    // they don't add any stretch
                    if (lineStartBAP != 0 || lineEndBAP != 0) {
                        spaceElements.add(new KnuthGlue(lineEndBAP,
                                3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                new LeafPosition(this, -1), false));
                        spaceElements.add(new KnuthPenalty(0, 0, false,
                                new LeafPosition(this, -1), false));
                        spaceElements.add(new KnuthGlue(ai.ipdArea.opt
                                - (lineStartBAP + lineEndBAP), -3
                                * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                mainPosition, false));
                        spaceElements.add(new KnuthInlineBox(0, null,
                                notifyPos(new LeafPosition(this, -1)), false));
                        spaceElements.add(new KnuthPenalty(0,
                                KnuthElement.INFINITE, false, new LeafPosition(
                                        this, -1), false));
                        spaceElements.add(new KnuthGlue(lineStartBAP, 0, 0,
                                new LeafPosition(this, -1), false));
                    } else {
                        spaceElements.add(new KnuthGlue(0,
                                3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                new LeafPosition(this, -1), false));
                        spaceElements.add(new KnuthPenalty(0, 0, false,
                                new LeafPosition(this, -1), false));
                        spaceElements.add(new KnuthGlue(ai.ipdArea.opt, -3
                                * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                mainPosition, false));
                    }
                    break;

                case EN_JUSTIFY:
                    // justified text:
                    // the stretch and shrink depends on the space width
                    if (lineStartBAP != 0 || lineEndBAP != 0) {
                        spaceElements.add(new KnuthGlue(lineEndBAP, 0, 0,
                                new LeafPosition(this, -1), false));
                        spaceElements.add(new KnuthPenalty(0, 0, false,
                                new LeafPosition(this, -1), false));
                        spaceElements.add(new KnuthGlue(
                                ai.ipdArea.opt - (lineStartBAP + lineEndBAP),
                                ai.ipdArea.max - ai.ipdArea.opt,
                                ai.ipdArea.opt - ai.ipdArea.min,
                                mainPosition, false));
                        spaceElements.add(new KnuthInlineBox(0, null,
                                notifyPos(new LeafPosition(this, -1)), false));
                        spaceElements.add(new KnuthPenalty(0,
                                KnuthElement.INFINITE, false, new LeafPosition(
                                        this, -1), false));
                        spaceElements.add(new KnuthGlue(lineStartBAP, 0, 0,
                                new LeafPosition(this, -1), false));
                    } else {
                        spaceElements.add(new KnuthGlue(ai.ipdArea.opt,
                                ai.ipdArea.max - ai.ipdArea.opt,
                                ai.ipdArea.opt - ai.ipdArea.min,
                                mainPosition, false));
                    }
                    break;

                default:
                    // last line justified, the other lines unjustified:
                    // use only the space stretch
                    if (lineStartBAP != 0 || lineEndBAP != 0) {
                        spaceElements.add(new KnuthGlue(lineEndBAP, 0, 0,
                                new LeafPosition(this, -1), false));
                        spaceElements.add(new KnuthPenalty(0, 0, false,
                                new LeafPosition(this, -1), false));
                        spaceElements.add(new KnuthGlue(
                                ai.ipdArea.opt - (lineStartBAP + lineEndBAP),
                                ai.ipdArea.max - ai.ipdArea.opt,
                                0, mainPosition, false));
                        spaceElements.add(new KnuthInlineBox(0, null,
                                notifyPos(new LeafPosition(this, -1)), false));
                        spaceElements.add(new KnuthPenalty(0,
                                KnuthElement.INFINITE, false, new LeafPosition(
                                        this, -1), false));
                        spaceElements.add(new KnuthGlue(lineStartBAP, 0, 0,
                                new LeafPosition(this, -1), false));
                    } else {
                        spaceElements.add(new KnuthGlue(ai.ipdArea.opt,
                                ai.ipdArea.max - ai.ipdArea.opt, 0,
                                mainPosition, false));
                    }
                }
            }
        }
        
        return spaceElements;
    }

    private LinkedList createElementsForAWordFragment(int alignment,
            AreaInfo ai, int leafValue, MinOptMax letterSpaceWidth) {
        LinkedList wordElements = new LinkedList();
        LeafPosition mainPosition = new LeafPosition(this, leafValue);

        // if the last character of the word fragment is '-' or '/',
        // the fragment could end a line; in this case, it loses one
        // of its letter spaces;
        boolean bSuppressibleLetterSpace = ai.breakOppAfter && !ai.bHyphenated;

        if (letterSpaceWidth.min == letterSpaceWidth.max) {
            // constant letter spacing
            wordElements.add
                (new KnuthInlineBox(
                        bSuppressibleLetterSpace 
                                ? ai.ipdArea.opt - letterSpaceWidth.opt
                                : ai.ipdArea.opt,
                        alignmentContext,
                        notifyPos(mainPosition), false));
        } else {
            // adjustable letter spacing
            int unsuppressibleLetterSpaces 
                = bSuppressibleLetterSpace ? ai.iLScount - 1 : ai.iLScount;
            wordElements.add
                (new KnuthInlineBox(ai.ipdArea.opt
                        - ai.iLScount * letterSpaceWidth.opt,
                        alignmentContext,
                        notifyPos(mainPosition), false));
            wordElements.add
                (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                        new LeafPosition(this, -1), true));
            wordElements.add
                (new KnuthGlue(unsuppressibleLetterSpaces * letterSpaceWidth.opt,
                        unsuppressibleLetterSpaces * (letterSpaceWidth.max - letterSpaceWidth.opt),
                        unsuppressibleLetterSpaces * (letterSpaceWidth.opt - letterSpaceWidth.min),
                        new LeafPosition(this, -1), true));
            wordElements.add
                (new KnuthInlineBox(0, null,
                              notifyPos(new LeafPosition(this, -1)), true));
        }
 
        // extra-elements if the word fragment is the end of a syllable,
        // or it ends with a character that can be used as a line break
        if (ai.bHyphenated) {
            MinOptMax widthIfNoBreakOccurs = null;
            if (ai.iBreakIndex < textArray.length) {
                //Add in kerning in no-break condition
                widthIfNoBreakOccurs = letterAdjustArray[ai.iBreakIndex];
            }
            //if (ai.iBreakIndex)
            
            // the word fragment ends at the end of a syllable:
            // if a break occurs the content width increases,
            // otherwise nothing happens
            wordElements.addAll(createElementsForAHyphen(alignment, hyphIPD, widthIfNoBreakOccurs, ai.breakOppAfter && ai.bHyphenated));
        } else if (bSuppressibleLetterSpace) {
            // the word fragment ends with a character that acts as a hyphen
            // if a break occurs the width does not increase,
            // otherwise there is one more letter space
            wordElements.addAll(createElementsForAHyphen(alignment, 0, letterSpaceWidth, true));
        }
        return wordElements;
    }

    // static final int SOFT_HYPHEN_PENALTY = KnuthPenalty.FLAGGED_PENALTY / 10;
    static final int SOFT_HYPHEN_PENALTY = 1;
    private LinkedList createElementsForAHyphen(int alignment,
            int widthIfBreakOccurs, MinOptMax widthIfNoBreakOccurs, boolean unflagged) {
        if (widthIfNoBreakOccurs == null) {
            widthIfNoBreakOccurs = ZERO_MINOPTMAX;
        }
        LinkedList hyphenElements = new LinkedList();
        
        switch (alignment) {
        case EN_CENTER :
            // centered text:
            /*
            hyphenElements.add
                (new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        new LeafPosition(this, -1), false));
            hyphenElements.add
                (new KnuthPenalty(hyphIPD,
                        KnuthPenalty.FLAGGED_PENALTY, true,
                        new LeafPosition(this, -1), false));
            hyphenElements.add
                (new KnuthGlue(0,
                        - 6 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        new LeafPosition(this, -1), false));
            hyphenElements.add
                (new KnuthInlineBox(0, 0, 0, 0,
                        new LeafPosition(this, -1), false));
            hyphenElements.add
                (new KnuthPenalty(0, KnuthElement.INFINITE, true,
                        new LeafPosition(this, -1), false));
            hyphenElements.add
                (new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        new LeafPosition(this, -1), false));
            */
            hyphenElements.add
                (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                        new LeafPosition(this, -1), true));
            hyphenElements.add
                (new KnuthGlue(lineEndBAP, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                               new LeafPosition(this, -1), true));
            hyphenElements.add
                (new KnuthPenalty(hyphIPD,
                        unflagged ? SOFT_HYPHEN_PENALTY : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                        new LeafPosition(this, -1), false));
            hyphenElements.add
                (new KnuthGlue(-(lineEndBAP + lineStartBAP),
                        -6 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        new LeafPosition(this, -1), false));
            hyphenElements.add
                (new KnuthInlineBox(0, null,
                                    notifyPos(new LeafPosition(this, -1)), true));
            hyphenElements.add
               (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                 new LeafPosition(this, -1), true));
            hyphenElements.add
                (new KnuthGlue(lineStartBAP, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                               new LeafPosition(this, -1), true));
            break;
            
        case EN_START  : // fall through
        case EN_END    :
            // left- or right-aligned text:
            /*
            hyphenElements.add
                (new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        new LeafPosition(this, -1), false));
            hyphenElements.add
                (new KnuthPenalty(widthIfBreakOccurs,
                        KnuthPenalty.FLAGGED_PENALTY, true,
                        new LeafPosition(this, -1), false));
             hyphenElements.add
                (new KnuthGlue(widthIfNoBreakOccurs.opt,
                        - 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                        new LeafPosition(this, -1), false));
            */
            if (lineStartBAP != 0 || lineEndBAP != 0) {
                hyphenElements.add
                    (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                            new LeafPosition(this, -1), true));
                hyphenElements.add
                    (new KnuthGlue(lineEndBAP, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                   new LeafPosition(this, -1), false));
                hyphenElements.add
                    (new KnuthPenalty(widthIfBreakOccurs,
                            unflagged ? SOFT_HYPHEN_PENALTY : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                            new LeafPosition(this, -1), false));
                hyphenElements.add
                    (new KnuthGlue(widthIfNoBreakOccurs.opt - (lineStartBAP + lineEndBAP),
                                   -3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                   new LeafPosition(this, -1), false));
                hyphenElements.add
                    (new KnuthInlineBox(0, null,
                                        notifyPos(new LeafPosition(this, -1)), false));
                hyphenElements.add
                   (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                     new LeafPosition(this, -1), false));
                hyphenElements.add
                    (new KnuthGlue(lineStartBAP, 0, 0,
                                   new LeafPosition(this, -1), false));
            } else {
                hyphenElements.add
                    (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                            new LeafPosition(this, -1), true));
                hyphenElements.add
                    (new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            new LeafPosition(this, -1), false));
                hyphenElements.add
                    (new KnuthPenalty(widthIfBreakOccurs,
                            unflagged ? SOFT_HYPHEN_PENALTY : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                            new LeafPosition(this, -1), false));
                hyphenElements.add
                    (new KnuthGlue(widthIfNoBreakOccurs.opt,
                            -3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                            new LeafPosition(this, -1), false));
            }
            break;
            
        default:
            // justified text, or last line justified:
            // just a flagged penalty
            /*
            hyphenElements.add
                (new KnuthPenalty(widthIfBreakOccurs,
                        KnuthPenalty.FLAGGED_PENALTY, true,
                        new LeafPosition(this, -1), false));
            */
            if (lineStartBAP != 0 || lineEndBAP != 0) {
                hyphenElements.add
                    (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                            new LeafPosition(this, -1), true));

                hyphenElements.add
                    (new KnuthGlue(lineEndBAP, 0, 0,
                                   new LeafPosition(this, -1), false));
                hyphenElements.add
                    (new KnuthPenalty(widthIfBreakOccurs,
                            unflagged ? SOFT_HYPHEN_PENALTY : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                            new LeafPosition(this, -1), false));
                // extra elements representing a letter space that is suppressed
                // if a break occurs
                if (widthIfNoBreakOccurs.min != 0
                    || widthIfNoBreakOccurs.max != 0) {
                    hyphenElements.add
                        (new KnuthGlue(widthIfNoBreakOccurs.opt - (lineStartBAP + lineEndBAP),
                                widthIfNoBreakOccurs.max - widthIfNoBreakOccurs.opt,
                                widthIfNoBreakOccurs.opt - widthIfNoBreakOccurs.min,
                                new LeafPosition(this, -1), false));
                } else {
                    hyphenElements.add
                        (new KnuthGlue(-(lineStartBAP + lineEndBAP), 0, 0,
                                       new LeafPosition(this, -1), false));
                }
                hyphenElements.add
                    (new KnuthInlineBox(0, null,
                                        notifyPos(new LeafPosition(this, -1)), false));
                hyphenElements.add
                    (new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                      new LeafPosition(this, -1), false));
                hyphenElements.add
                    (new KnuthGlue(lineStartBAP, 0, 0,
                                   new LeafPosition(this, -1), false));
            } else {
                hyphenElements.add
                    (new KnuthPenalty(widthIfBreakOccurs,
                            unflagged ? SOFT_HYPHEN_PENALTY : KnuthPenalty.FLAGGED_PENALTY, !unflagged,
                            new LeafPosition(this, -1), false));
                // extra elements representing a letter space that is suppressed
                // if a break occurs
                if (widthIfNoBreakOccurs.min != 0
                    || widthIfNoBreakOccurs.max != 0) {
                    hyphenElements.add
                        (new KnuthGlue(widthIfNoBreakOccurs.opt,
                                widthIfNoBreakOccurs.max - widthIfNoBreakOccurs.opt,
                                widthIfNoBreakOccurs.opt - widthIfNoBreakOccurs.min,
                                new LeafPosition(this, -1), false));
                }
            }
        }
        
        return hyphenElements;
    }
    
}


