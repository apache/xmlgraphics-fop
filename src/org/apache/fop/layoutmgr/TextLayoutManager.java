/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.TextInfo;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.area.Area;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Word;
import org.apache.fop.area.inline.Space;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.fo.properties.VerticalAlign;

//import org.apache.fop.fo.properties.*;

import java.util.ArrayList;

/**
 * LayoutManager for text (a sequence of characters) which generates one
 * or more inline areas.
 */
public class TextLayoutManager extends AbstractLayoutManager {

    /**
     * Store information about each potential word area.
     * Index of character which ends the area, IPD of area, including
     * any word-space and letter-space.
     * Number of word-spaces?
     */
    private class AreaInfo {
        short m_iStartIndex;
        short m_iBreakIndex;
        short m_iWScount;
        MinOptMax m_ipdArea;
        AreaInfo(short iStartIndex, short iBreakIndex, short iWScount,
                 MinOptMax ipdArea) {
            m_iStartIndex = iStartIndex;
            m_iBreakIndex = iBreakIndex;
            m_iWScount = iWScount;
            m_ipdArea = ipdArea;
        }
    }


    // Hold all possible breaks for the text in this LM's FO.
    private ArrayList m_vecAreaInfo;

    /** Non-space characters on which we can end a line. */
    private static final String s_breakChars = "-/" ;

    private char[] chars;
    private TextInfo textInfo;

    private static final char NEWLINE = '\n';
    private static final char SPACE = '\u0020'; // Normal space
    private static final char NBSPACE = '\u00A0'; // Non-breaking space
    private static final char LINEBREAK = '\u2028';
    private static final char ZERO_WIDTH_SPACE = '\u200B';
    // byte order mark
    private static final char ZERO_WIDTH_NOBREAK_SPACE = '\uFEFF';

    /** Start index of first character in this parent Area */
    private short m_iAreaStart = 0;
    /** Start index of next "word" */
    private short m_iNextStart = 0;
    /** Size since last makeArea call, except for last break */
    private MinOptMax m_ipdTotal ;
    /** Size including last break possibility returned */
    // private MinOptMax m_nextIPD= new MinOptMax(0);
    /** size of a space character (U+0020) glyph in current font */
    private int m_spaceIPD;
    /** size of the hyphen character glyph in current font */
    private int m_hyphIPD;
    /** 1/2 of word-spacing value */
    private SpaceVal m_halfWS;
    /** Number of space characters after previous possible break position. */
    private int m_iNbSpacesPending;


    public TextLayoutManager(FObj fobj, char[] chars, TextInfo textInfo) {
        super(fobj);
        this.chars = chars;
        this.textInfo = textInfo;
        this.m_vecAreaInfo = new ArrayList();

        // With CID fonts, space isn't neccesary currentFontState.width(32)
        m_spaceIPD = CharUtilities.getCharWidth(' ', textInfo.fs);
        // Use hyphenationChar property
        m_hyphIPD = CharUtilities.getCharWidth('-', textInfo.fs);
        // Make half-space: <space> on either side of a word-space)
        SpaceVal ws = textInfo.wordSpacing;
        m_halfWS = new SpaceVal(MinOptMax.multiply(ws.space, 0.5),
                                ws.bConditional, ws.bForcing, ws.iPrecedence);
    }


    public boolean generatesInlineAreas() {
        return true;
    }

    /* METHODS FROM LeafNodeLayoutManager,
     * used in Keiron's implemenation, but not here (yet at least).
     */
    public int size() {
        return 0;
    }

    public InlineArea get(int index) {
        return null;
    }

    public void getWordChars(StringBuffer sbChars, Position bp1,
                             Position bp2) {
        LeafPosition endPos = (LeafPosition) bp2;
        AreaInfo ai =
          (AreaInfo) m_vecAreaInfo.get(endPos.getLeafPos());
        // Skip all leading spaces for hyphenation
        int i;
        for (i = ai.m_iStartIndex; i < ai.m_iBreakIndex &&
                CharUtilities.isAnySpace(chars[i]) == true ; i++)
            ;
        sbChars.append(new String(chars, i, ai.m_iBreakIndex - i));
    }

    /**
     * Return value indicating whether the next area to be generated could
     * start a new line. This should only be called in the "START" condition
     * if a previous inline BP couldn't end the line.
     * Return true if the first character is a potential linebreak character.
     */
    public boolean canBreakBefore(LayoutContext context) {
        char c = chars[m_iNextStart];
        return ((c == NEWLINE) || (textInfo.bWrap &&
                                   (CharUtilities.isSpace(c) ||
                                    s_breakChars.indexOf(c) >= 0)));
    }

    /** Reset position for returning next BreakPossibility. */

    public void resetPosition(Position prevPos) {
        if (prevPos != null) {
            // ASSERT (prevPos.getLM() == this)
            if (prevPos.getLM() != this) {
                //log.error(
                //  "TextLayoutManager.resetPosition: " + "LM mismatch!!!");
            }
            LeafPosition tbp = (LeafPosition) prevPos;
            AreaInfo ai =
              (AreaInfo) m_vecAreaInfo.get(tbp.getLeafPos());
            if (ai.m_iBreakIndex != m_iNextStart) {
                m_iNextStart = ai.m_iBreakIndex;
                m_vecAreaInfo.ensureCapacity(tbp.getLeafPos() + 1);
                // TODO: reset or recalculate total IPD = sum of all word IPD
                // up to the break position
                m_ipdTotal = ai.m_ipdArea;
                setFinished(false);
            }
        } else {
            // Reset to beginning!
            m_vecAreaInfo.clear();
            m_iNextStart = 0;
            setFinished(false);
        }
    }


    // TODO: see if we can use normal getNextBreakPoss for this with
    // extra hyphenation information in LayoutContext
    private boolean getHyphenIPD(HyphContext hc, MinOptMax hyphIPD) {
        // Skip leading word-space before calculating count?
        boolean bCanHyphenate = true;
        int iStopIndex = m_iNextStart + hc.getNextHyphPoint();

        if (chars.length < iStopIndex || textInfo.bCanHyphenate == false) {
            iStopIndex = chars.length;
            bCanHyphenate = false;
        }
        hc.updateOffset(iStopIndex - m_iNextStart);

        for (; m_iNextStart < iStopIndex; m_iNextStart++) {
            char c = chars[m_iNextStart];
            hyphIPD.opt += CharUtilities.getCharWidth(c, textInfo.fs);
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
            m_ipdTotal = new MinOptMax(0);
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
            for (; m_iNextStart < chars.length &&
                    chars[m_iNextStart] == SPACE; m_iNextStart++)
                ;
            // If now at end, nothing to compose here!
            if (m_iNextStart >= chars.length) {
                setFinished(true);
                return null; // Or an "empty" BreakPoss?
            }
        }


        /* Start of this "word", plus any non-suppressed leading space.
         * Collapse any remaining word-space with leading space from
         * ancestor FOs.
         * Add up other leading space which is counted in the word IPD.
         */

        SpaceSpecifier pendingSpace = new SpaceSpecifier(false);
        short iThisStart = m_iNextStart; // Index of first character counted
        MinOptMax spaceIPD = new MinOptMax(0); // Extra IPD from word-spacing
        // Sum of glyph IPD of all characters in a word, inc. leading space
        int wordIPD = 0;
        short iWScount = 0; // Count of word spaces
        boolean bSawNonSuppressible = false;

        for (; m_iNextStart < chars.length; m_iNextStart++) {
            char c = chars[m_iNextStart];
            if (CharUtilities.isAnySpace(c) == false)
                break;
            if (c == SPACE || c == NBSPACE) {
                ++iWScount;
                // Counted as word-space
                if (m_iNextStart == iThisStart &&
                        (iFlags & BreakPoss.ISFIRST) != 0) {
                    // If possible, treat as normal inter-word space
                    if (context.getLeadingSpace().hasSpaces()) {
                        context.getLeadingSpace().addSpace(m_halfWS);
                    } else {
                        // Doesn't combine with any other leading spaces
                        // from ancestors
                        spaceIPD.add(m_halfWS.space);
                    }
                } else {
                    pendingSpace.addSpace(m_halfWS);
                    spaceIPD.add(pendingSpace.resolve(false));
                }
                wordIPD += m_spaceIPD; // Space glyph IPD
                pendingSpace.clear();
                pendingSpace.addSpace(m_halfWS);
                if (c == NBSPACE) {
                    bSawNonSuppressible = true;
                }
            } else {
                // If we have letter-space, so we apply this to fixed-
                // width spaces (which are not word-space) also?
                bSawNonSuppressible = true;
                spaceIPD.add(pendingSpace.resolve(false));
                pendingSpace.clear();
                wordIPD += CharUtilities.getCharWidth(c, textInfo.fs);
            }
        }

        if (m_iNextStart < chars.length) {
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
                iFlags |= (BreakPoss.CAN_BREAK_AFTER |
                           BreakPoss.HYPHENATED);
            }
            wordIPD += hyphIPD.opt;
        } else {
            // Look for a legal line-break: breakable white-space and certain
            // characters such as '-' which can serve as word breaks.
            // Don't look for hyphenation points here though
            for (; m_iNextStart < chars.length; m_iNextStart++) {
                char c = chars[m_iNextStart];
                if ((c == NEWLINE) || // Include any breakable white-space as break char
                        //  even if fixed width
                        (textInfo.bWrap && (CharUtilities.isSpace(c) ||
                                            s_breakChars.indexOf(c) >= 0))) {
                    iFlags |= BreakPoss.CAN_BREAK_AFTER;
                    if (c != SPACE) {
                        m_iNextStart++;
                        if (c != NEWLINE) {
                            wordIPD += CharUtilities.getCharWidth(c,
                                                                  textInfo.fs);
                        } else {
                            iFlags |= BreakPoss.FORCE;
                        }
                    }
                    // If all remaining characters would be suppressed at
                    // line-end, set a flag for parent LM.
                    int iLastChar;
                    for (iLastChar = m_iNextStart;
                            iLastChar < chars.length &&
                            chars[iLastChar] == SPACE; iLastChar++)
                        ;
                    if (iLastChar == chars.length) {
                        iFlags |= BreakPoss.REST_ARE_SUPPRESS_AT_LB;
                    }
                    return makeBreakPoss(iThisStart, spaceIPD, wordIPD,
                                         context.getLeadingSpace(), null, iFlags,
                                         iWScount);
                }
                wordIPD += CharUtilities.getCharWidth(c, textInfo.fs);
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
        if (m_ipdTotal != null) {
            ipd.add(m_ipdTotal); // sum of all words so far in line
        }
        // Note: break position now stores total size to here

        // Position is the index of the info for this word in the vector
        m_vecAreaInfo.add(
          new AreaInfo(iWordStart, m_iNextStart, iWScount, ipd));
        BreakPoss bp = new BreakPoss(
                         new LeafPosition(this, m_vecAreaInfo.size() - 1));
        m_ipdTotal = ipd;
        if ((flags & BreakPoss.HYPHENATED) != 0) {
            // Add the hyphen size, but don't change total IPD!
            bp.setStackingSize(
              MinOptMax.add(ipd, new MinOptMax(m_hyphIPD)));
        } else {
            bp.setStackingSize(ipd);
        }
        // TODO: make this correct (see Keiron's vertical alignment code)
        bp.setNonStackingSize(new MinOptMax(textInfo.lineHeight));

        /* Set max ascender and descender (offset from baseline),
         * used for calculating the bpd of the line area containing
         * this text.
         */
        //bp.setDescender(textInfo.fs.getDescender());
        //bp.setAscender(textInfo.fs.getAscender());
        if (m_iNextStart == chars.length) {
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
     * This can either generate an area for each "word" and each space, or
     * an area containing all text with a parameter controlling the size of
     * the word space. The latter is most efficient for PDF generation.
     * Set size of each area.
     * @param parentIter Iterator over Position information returned
     * by this LayoutManager.
     * @param dSpaceAdjust Factor controlling how much extra space to add
     * in order to justify the line.
     */
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        // Add word areas
        AreaInfo ai = null ;
        int iStart = -1;
        int iWScount = 0;

        /* On first area created, add any leading space.
         * Calculate word-space stretch value.
         */
        while (posIter.hasNext()) {
            LeafPosition tbpNext = (LeafPosition) posIter.next();
            ai = (AreaInfo) m_vecAreaInfo.get(tbpNext.getLeafPos());
            if (iStart == -1) {
                iStart = ai.m_iStartIndex;
            }
            iWScount += ai.m_iWScount;
        }
        if(ai == null) {
            return;
        }
        // Calculate total adjustment
        int iAdjust = 0;
        double dSpaceAdjust = context.getSpaceAdjust();
        if (dSpaceAdjust > 0.0) {
            // Stretch by factor
            //     System.err.println("Potential stretch = " +
            //        (ai.m_ipdArea.max - ai.m_ipdArea.opt));
            iAdjust = (int)((double)(ai.m_ipdArea.max -
                                     ai.m_ipdArea.opt) * dSpaceAdjust);
        } else if (dSpaceAdjust < 0.0) {
            // Shrink by factor
            //     System.err.println("Potential shrink = " +
            //        (ai.m_ipdArea.opt - ai.m_ipdArea.min));
            iAdjust = (int)((double)(ai.m_ipdArea.opt -
                                     ai.m_ipdArea.min) * dSpaceAdjust);
        }
        // System.err.println("Text adjustment factor = " + dSpaceAdjust +
        //    " total=" + iAdjust);

        // Make an area containing all characters between start and end.
        Word word = null;
        String str = new String(chars, iStart, ai.m_iBreakIndex - iStart);
        //if(!"".equals(str.trim())) {
            word = createWord(
                      str,
                      ai.m_ipdArea.opt + iAdjust, context.getBaseline());
            if (iWScount > 0) {
                //log.error("Adjustment per word-space= " +
                //                   iAdjust / iWScount);
                word.setWSadjust(iAdjust / iWScount);
            }
        //}
        if ((chars[iStart] == SPACE || chars[iStart] == NBSPACE) &&
                context.getLeadingSpace().hasSpaces()) {
            context.getLeadingSpace().addSpace(m_halfWS);
        }
        // Set LAST flag if done making characters
        int iLastChar;
        for (iLastChar = ai.m_iBreakIndex;
                iLastChar < chars.length && chars[iLastChar] == SPACE;
                iLastChar++)
            ;
        context.setFlags(LayoutContext.LAST_AREA,
                         iLastChar == chars.length);

        // Can we have any trailing space? Yes, if last char was a space!
        context.setTrailingSpace(new SpaceSpecifier(false));
        if (chars[ai.m_iBreakIndex - 1] == SPACE ||
                chars[ai.m_iBreakIndex - 1] == NBSPACE) {
            context.getTrailingSpace().addSpace(m_halfWS);
        }
        if(word != null) {
            parentLM.addChild(word);
        }
    }



    protected Word createWord(String str, int width, int base) {
        Word curWordArea = new Word();
        curWordArea.setWidth(width);
        curWordArea.setHeight(textInfo.fs.getAscender() -
                              textInfo.fs.getDescender());
        curWordArea.setOffset(textInfo.fs.getAscender());
        curWordArea.setOffset(base);

        curWordArea.setWord(str);
        curWordArea.addTrait(Trait.FONT_NAME, textInfo.fs.getFontName());
        curWordArea.addTrait(Trait.FONT_SIZE,
                             new Integer(textInfo.fs.getFontSize()));
        return curWordArea;
    }


}

