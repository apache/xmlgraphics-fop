/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.util.ArrayList;

import org.apache.fop.fo.FOText;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.Space;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for text (a sequence of characters) which generates one
 * or more inline areas.
 */
public class TextLayoutManager extends AbstractLayoutManager {

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
        private MinOptMax ipdArea;
        public AreaInfo(short iSIndex, short iBIndex, short iWS,
                 MinOptMax ipd) {
            iStartIndex = iSIndex;
            iBreakIndex = iBIndex;
            iWScount = iWS;
            ipdArea = ipd;
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
    /** size of the hyphen character glyph in current font */
    private int hyphIPD;
    /** 1/2 of word-spacing value */
    private SpaceVal halfWS;
    /** Number of space characters after previous possible break position. */
    private int iNbSpacesPending;

    /**
     * Create a Text layout manager.
     *
     * @param node The FOText object to be rendered
     */
    public TextLayoutManager(FOText node) {
        super(node);
        foText = node;
        textArray = new char[node.endIndex - node.startIndex];
        System.arraycopy(node.ca, node.startIndex, textArray, 0,
            node.endIndex - node.startIndex);

        vecAreaInfo = new java.util.ArrayList();

        // With CID fonts, space isn't neccesary currentFontState.width(32)
        spaceCharIPD = foText.textInfo.fs.getCharWidth(' ');
        // Use hyphenationChar property
        hyphIPD = foText.textInfo.fs.getCharWidth('-');
        // Make half-space: <space> on either side of a word-space)
        SpaceVal ws = foText.textInfo.wordSpacing;
        halfWS = new SpaceVal(MinOptMax.multiply(ws.getSpace(), 0.5),
                ws.isConditional(), ws.isForcing(), ws.getPrecedence());
    }

    /**
     * Text always generates inline areas.
     *
     * @return true
     */
    public boolean generatesInlineAreas() {
        return true;
    }

    /**
     * Get the word characters between two positions.
     * This is used when doing hyphenation or other word manipulations.
     *
     * @param sbChars the string buffer to put the chars into
     * @param bp1 the start position
     * @param bp2 the end position
     */
    public void getWordChars(StringBuffer sbChars, Position bp1,
                             Position bp2) {
        LeafPosition endPos = (LeafPosition) bp2;
        AreaInfo ai =
          (AreaInfo) vecAreaInfo.get(endPos.getLeafPos());
        // Skip all leading spaces for hyphenation
        int i;
        for (i = ai.iStartIndex;
                i < ai.iBreakIndex && CharUtilities.isAnySpace(textArray[i]) == true;
                i++) {
            //nop
        }
        sbChars.append(new String(textArray, i, ai.iBreakIndex - i));
    }

    /**
     * Return value indicating whether the next area to be generated could
     * start a new line. This should only be called in the "START" condition
     * if a previous inline BP couldn't end the line.
     * Return true if the first character is a potential linebreak character.
     *
     * @param context the layout context for determining a break
     * @return true if can break before this text
     */
    public boolean canBreakBefore(LayoutContext context) {
        char c = textArray[iNextStart];
        return ((c == NEWLINE) || (foText.textInfo.bWrap 
                    && (CharUtilities.isBreakableSpace(c)
                    || (BREAK_CHARS.indexOf(c) >= 0 && (iNextStart == 0 
                        || Character.isLetterOrDigit(textArray[iNextStart-1]))))));
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
                log.error(
                  "TextLayoutManager.resetPosition: " + "LM mismatch!!!");
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

        if (textArray.length < iStopIndex || foText.textInfo.bCanHyphenate == false) {
            iStopIndex = textArray.length;
            bCanHyphenate = false;
        }
        hc.updateOffset(iStopIndex - iNextStart);

        for (; iNextStart < iStopIndex; iNextStart++) {
            char c = textArray[iNextStart];
            hyphIPD.opt += foText.textInfo.fs.getCharWidth(c);
            // letter-space?
        }
        // Need to include hyphen size too, but don't count it in the
        // stored running total, since it would be double counted
        // with later hyphenation points
        return bCanHyphenate;
    }

    /**
     * Return the next break possibility that fits the constraints.
     * @param context An object specifying the flags and input information
     * concerning the context of the BreakPoss.
     * @return BreakPoss An object containing information about the next
     * legal break position or the end of the text run if no break
     * was found.
     * <p>Assumptions: white-space-treatment and
     * linefeed-treatment processing
     * are already done, so there are no TAB or RETURN characters remaining.
     * white-space-collapse handling is also done
     * (but perhaps this shouldn't be true!)
     * There may be LINEFEED characters if they weren't converted
     * into spaces. A LINEFEED always forces a break.
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        /* On first call in a new Line, the START_AREA
         * flag in LC is set.
         */

        int iFlags = 0;

        if (context.startsNewArea()) {
            /* This could be first call on this LM, or the first call
             * in a new (possible) LineArea.
             */
            ipdTotal = new MinOptMax(0);
            iFlags |= BreakPoss.ISFIRST;
        }


        /* HANDLE SUPPRESSED LEADING SPACES
         * See W3C XSL Rec. 7.16.3.
         * Suppress characters whose "suppress-at-line-break" property = "suppress"
         * This can only be set on an explicit fo:character object. The default
         * behavior is that U+0020 is suppressed; all other character codes are
         * retained.
         */
        if (context.suppressLeadingSpace()) {
            for (; iNextStart < textArray.length
                    && textArray[iNextStart] == SPACE; iNextStart++) {
            }
            // If now at end, nothing to compose here!
            if (iNextStart >= textArray.length) {
                setFinished(true);
                return null; // Or an "empty" BreakPoss?
            }
        }


        /* Start of this TextArea, plus any non-suppressed leading space.
         * Collapse any remaining word-space with leading space from
         * ancestor FOs.
         * Add up other leading space which is counted in the TextArea IPD.
         */

        SpaceSpecifier pendingSpace = new SpaceSpecifier(false);
        short iThisStart = iNextStart; // Index of first character counted
        MinOptMax spaceIPD = new MinOptMax(0); // Extra IPD from word-spacing
        // Sum of glyph IPD of all characters in a word, inc. leading space
        int wordIPD = 0;
        short iWScount = 0; // Count of word spaces
        boolean bSawNonSuppressible = false;

        for (; iNextStart < textArray.length; iNextStart++) {
            char c = textArray[iNextStart];
            if (CharUtilities.isAnySpace(c) == false) {
                break;
            }
            if (c == SPACE || c == NBSPACE) {
                ++iWScount;
                // Counted as word-space
                if (iNextStart == iThisStart
                        && (iFlags & BreakPoss.ISFIRST) != 0) {
                    // If possible, treat as normal inter-word space
                    if (context.getLeadingSpace().hasSpaces()) {
                        context.getLeadingSpace().addSpace(halfWS);
                    } else {
                        // Doesn't combine with any other leading spaces
                        // from ancestors
                        spaceIPD.add(halfWS.getSpace());
                    }
                } else {
                    pendingSpace.addSpace(halfWS);
                    spaceIPD.add(pendingSpace.resolve(false));
                }
                wordIPD += spaceCharIPD; // Space glyph IPD
                pendingSpace.clear();
                pendingSpace.addSpace(halfWS);
                if (c == NBSPACE) {
                    bSawNonSuppressible = true;
                }
            } else {
                // If we have letter-space, so we apply this to fixed-
                // width spaces (which are not word-space) also?
                bSawNonSuppressible = true;
                spaceIPD.add(pendingSpace.resolve(false));
                pendingSpace.clear();
                wordIPD += foText.textInfo.fs.getCharWidth(c);
            }
        }

        if (iNextStart < textArray.length) {
            spaceIPD.add(pendingSpace.resolve(false));
        } else {
            // This FO ended with spaces. Return the BP
            if (!bSawNonSuppressible) {
                iFlags |= BreakPoss.ALL_ARE_SUPPRESS_AT_LB;
            }
            return makeBreakPoss(iThisStart, spaceIPD, wordIPD,
                                 context.getLeadingSpace(), pendingSpace, iFlags,
                                 iWScount);
        }

        if (context.tryHyphenate()) {
            // Get the size of the next syallable
            MinOptMax hyphIPD = new MinOptMax(0);
            if (getHyphenIPD(context.getHyphContext(), hyphIPD)) {
                iFlags |= (BreakPoss.CAN_BREAK_AFTER | BreakPoss.HYPHENATED);
            }
            wordIPD += hyphIPD.opt;
        } else {
            // Look for a legal line-break: breakable white-space and certain
            // characters such as '-' which can serve as word breaks.
            // Don't look for hyphenation points here though
            for (; iNextStart < textArray.length; iNextStart++) {
                char c = textArray[iNextStart];
                // Include any breakable white-space as break char
                if ((c == NEWLINE) || (foText.textInfo.bWrap 
                    && (CharUtilities.isBreakableSpace(c)
                    || (BREAK_CHARS.indexOf(c) >= 0 && (iNextStart == 0 
                        || Character.isLetterOrDigit(textArray[iNextStart-1])))))) {
                            iFlags |= BreakPoss.CAN_BREAK_AFTER;
                            if (c != SPACE) {
                                iNextStart++;
                                if (c != NEWLINE) {
                                    wordIPD += foText.textInfo.fs.getCharWidth(c);
                                } else {
                                    iFlags |= BreakPoss.FORCE;
                                }
                            }
                    // If all remaining characters would be suppressed at
                    // line-end, set a flag for parent LM.
                    int iLastChar;
                    for (iLastChar = iNextStart;
                            iLastChar < textArray.length
                            && textArray[iLastChar] == SPACE; iLastChar++) {
                        //nop
                    }
                    if (iLastChar == textArray.length) {
                        iFlags |= BreakPoss.REST_ARE_SUPPRESS_AT_LB;
                    }
                    return makeBreakPoss(iThisStart, spaceIPD, wordIPD,
                                         context.getLeadingSpace(), null, iFlags,
                                         iWScount);
                }
                wordIPD += foText.textInfo.fs.getCharWidth(c);
                // Note, if a normal non-breaking space, is it stretchable???
                // If so, keep a count of these embedded spaces.
            }
        }
        return makeBreakPoss(iThisStart, spaceIPD, wordIPD,
                             context.getLeadingSpace(), null, iFlags, iWScount);
    }

    private BreakPoss makeBreakPoss(short iWordStart,
                                    MinOptMax spaceIPD, int wordDim,
                                    SpaceSpecifier leadingSpace, SpaceSpecifier trailingSpace,
                                    int flags, short iWScount) {
        MinOptMax ipd = new MinOptMax(wordDim);
        ipd.add(spaceIPD);
        if (ipdTotal != null) {
            ipd.add(ipdTotal); // sum of all words so far in line
        }
        // Note: break position now stores total size to here

        // Position is the index of the info for this word in the vector
        vecAreaInfo.add(
          new AreaInfo(iWordStart, iNextStart, iWScount, ipd));
        BreakPoss bp = new BreakPoss(
                         new LeafPosition(this, vecAreaInfo.size() - 1));
        ipdTotal = ipd;
        if ((flags & BreakPoss.HYPHENATED) != 0) {
            // Add the hyphen size, but don't change total IPD!
            bp.setStackingSize(
              MinOptMax.add(ipd, new MinOptMax(hyphIPD)));
        } else {
            bp.setStackingSize(ipd);
        }
        // TODO: make this correct (see Keiron's vertical alignment code)
        bp.setNonStackingSize(new MinOptMax(foText.textInfo.lineHeight));

        /* Set max ascender and descender (offset from baseline),
         * used for calculating the bpd of the line area containing
         * this text.
         */
        //bp.setDescender(foText.textInfo.fs.getDescender());
        //bp.setAscender(foText.textInfo.fs.getAscender());
        if (iNextStart == textArray.length) {
            flags |= BreakPoss.ISLAST;
            setFinished(true);
        }
        bp.setFlag(flags);
        if (trailingSpace != null) {
            bp.setTrailingSpace(trailingSpace);
        } else {
            bp.setTrailingSpace(new SpaceSpecifier(false));
        }
        if (leadingSpace != null) {
            bp.setLeadingSpace(leadingSpace);
        } else {
            bp.setLeadingSpace(new SpaceSpecifier(false));
        }
        return bp;
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

        /* On first area created, add any leading space.
         * Calculate word-space stretch value.
         */
        while (posIter.hasNext()) {
            LeafPosition tbpNext = (LeafPosition) posIter.next();
            ai = (AreaInfo) vecAreaInfo.get(tbpNext.getLeafPos());
            if (iStart == -1) {
                iStart = ai.iStartIndex;
            }
            iWScount += ai.iWScount;
        }
        if (ai == null) {
            return;
        }
        // Calculate total adjustment
        int iRealWidth = ai.ipdArea.opt;
        int iAdjust = 0;
        double dIPDAdjust = context.getIPDAdjust();
        double dSpaceAdjust = context.getSpaceAdjust();
        if (dIPDAdjust > 0.0) {
            iRealWidth += (int)((double)(ai.ipdArea.max - ai.ipdArea.opt) * dIPDAdjust);
        }
        else {
            iRealWidth += (int)((double)(ai.ipdArea.opt - ai.ipdArea.min) * dIPDAdjust);
        }
        iAdjust = (int)((double)(iRealWidth * dSpaceAdjust));
        //System.err.println(" ");
        //System.err.println("TextLayoutManager> recreated difference to fill= " + iAdjust);

        // Make an area containing all characters between start and end.
        InlineArea word = null;
        int adjust = 0;
        
        // ignore newline character
        if (textArray[ai.iBreakIndex - 1] == NEWLINE) {
            adjust = 1;
        }
        String str = new String(textArray, iStart, ai.iBreakIndex - iStart - adjust);

        if (" ".equals(str)) {
            word = new Space();
            word.setWidth(ai.ipdArea.opt + iAdjust);
        } else  {
            TextArea t = createTextArea(str, iRealWidth + iAdjust, 
                context.getBaseline());
            if (iWScount > 0) {
                //getLogger().error("Adjustment per word-space= " +
                //                   iAdjust / iWScount);
                t.setTextSpaceAdjust(iAdjust / iWScount);
                //System.err.println("TextLayoutManager> word spaces= " + iWScount + " adjustment per word space= " + (iAdjust/iWScount));
            }
            word = t;
        }
        if ((textArray[iStart] == SPACE || textArray[iStart] == NBSPACE)
                && context.getLeadingSpace().hasSpaces()) {
            context.getLeadingSpace().addSpace(halfWS);
        }
        // Set LAST flag if done making characters
        int iLastChar;
        for (iLastChar = ai.iBreakIndex;
                iLastChar < textArray.length && textArray[iLastChar] == SPACE;
                iLastChar++) {
            //nop
        }
        context.setFlags(LayoutContext.LAST_AREA,
                         iLastChar == textArray.length);

        // Can we have any trailing space? Yes, if last char was a space!
        context.setTrailingSpace(new SpaceSpecifier(false));
        if (textArray[ai.iBreakIndex - 1] == SPACE
                || textArray[ai.iBreakIndex - 1] == NBSPACE) {
            context.getTrailingSpace().addSpace(halfWS);
        }
        if (word != null) {
            parentLM.addChild(word);
        }
    }

    /**
     * Create an inline word area.
     * This creates a TextArea and sets up the various attributes.
     *
     * @param str the string for the TextArea
     * @param width the width that the TextArea uses
     * @param base the baseline position
     * @return the new word area
     */
    protected TextArea createTextArea(String str, int width, int base) {
        TextArea textArea = new TextArea();
        textArea.setWidth(width);
        textArea.setHeight(foText.textInfo.fs.getAscender()
                              - foText.textInfo.fs.getDescender());
        textArea.setOffset(foText.textInfo.fs.getAscender());
        textArea.setOffset(base);

        textArea.setTextArea(str);
        textArea.addTrait(Trait.FONT_NAME, foText.textInfo.fs.getFontName());
        textArea.addTrait(Trait.FONT_SIZE,
                             new Integer(foText.textInfo.fs.getFontSize()));
        textArea.addTrait(Trait.COLOR, foText.textInfo.color);
        return textArea;
    }

}

