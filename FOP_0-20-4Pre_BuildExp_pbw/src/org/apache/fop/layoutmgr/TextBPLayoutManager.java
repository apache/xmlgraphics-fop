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

import java.util.Vector; // or use ArrayList ???

/**
 * LayoutManager for text (a sequence of characters) which generates one
 * or more inline areas.
 */
public class TextBPLayoutManager extends AbstractBPLayoutManager {
    /**
     * Private class to store information about the break index.
     * the field stores the index in the vector of AreaInfo which
     * corresponds to this break position.
     * Note: fields are directly readable in this class
     */
    private static class TextBreakPosition implements BreakPoss.Position {
        short m_iAreaIndex;

        TextBreakPosition(int iAreaIndex) {
            m_iAreaIndex = (short)iAreaIndex;
        }
    }

    /**
     * Store information about each potential word area.
     * Index of character which ends the area, IPD of area, including
     * any word-space and letter-space.
     * Number of word-spaces?
     */
    private class AreaInfo {
	short m_iStartIndex;
	short m_iBreakIndex;
	MinOptMax m_ipdArea;
	AreaInfo(short iStartIndex, short iBreakIndex, MinOptMax ipdArea) {
	    m_iStartIndex = iStartIndex;
	    m_iBreakIndex = iBreakIndex;
	    m_ipdArea = ipdArea;
	}
    }


    // Hold all possible breaks for the text in this LM's FO.
    private Vector m_vecAreaInfo;

    /** Non-space characters on which we can end a line. */
    static private final String s_breakChars = "-/" ;

    private char[] chars;
    private TextInfo textInfo;

    private static final char NEWLINE = '\n';
    private static final char RETURN = '\r';
    private static final char TAB = '\t';
    private static final char SPACE = ' ';
    private static final char LINEBREAK = '\u2028';
    private static final char ZERO_WIDTH_SPACE = '\u200B';
    // byte order mark
    private static final char ZERO_WIDTH_NOBREAK_SPACE = '\uFEFF';

    /* values that prev (below) may take */
    protected static final int NOTHING = 0;
    protected static final int WHITESPACE = 1;
    protected static final int TEXT = 2;

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
    /** 1/2 of word-spacing value */
    private SpaceVal m_halfWS;
    /** Number of space characters after previous possible break position. */
    private int  m_iNbSpacesPending;


    public TextBPLayoutManager(FObj fobj, char[] chars,
                             TextInfo textInfo) {
        super(fobj);
        this.chars = chars;
        this.textInfo = textInfo;
	this.m_vecAreaInfo = new Vector(chars.length/5); // Guess

        // With CID fonts, space isn't neccesary currentFontState.width(32)
        m_spaceIPD = CharUtilities.getCharWidth(' ', textInfo.fs);
	// Make half-space: <space> on either side of a word-space)
	SpaceVal ws = textInfo.wordSpacing;
	m_halfWS = new SpaceVal(MinOptMax.multiply(ws.space, 0.5),
				ws.bConditional, ws.bForcing,
				ws.iPrecedence);
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

    /**
     * Generate inline areas for words in text.
     */
    public boolean generateAreas() {
        // Handle white-space characteristics. Maybe there is no area to
        // generate....

        // Iterate over characters and make text areas.
        // Add each one to parent. Handle word-space.
        return false;
    }


    // NOTE: currently not used. Remove if decide it isn't necessary!
//     /**
//      * Get the BreakPoss at the start of the next line.
//      * @param bpPrevEnd The BreakPoss at the end of the previous line
//      * or null if we should return the point at the beginning of this
//      * text run.
//      */
//     public BreakPoss getStartBreakPoss(LayoutContext lc,
// 				       BreakPoss.Position bpPrevEnd) {
// 	BreakPoss bp = null;
// 	if (bpPrevEnd == null) {
// 	    bp = new BreakPoss(this, new TextBreakPosition(0));
// 	    // Set minimum bpd (character ascent and descent)
// 	    // Or do this at the line level???
// 	}
// 	else {
// 	    // Skip suppressible white-space
// 	    // ASSERT (((TextBreakPosition)bpPrevEnd).m_iAreaIndex =
// 	    //         m_iNextStart)
// 	    if ((lc.flags & LayoutContext.SUPPRESS_LEADING_SPACE)!=0) {
// 		/* Skip any leading word-space characters. */
// 		for (; m_iNextStart < chars.length &&
// 		     chars[m_iNextStart]==SPACE; m_iNextStart++);
// 	    }
// 	    // If now at end, nothing to compose here!
// 	    if (m_iNextStart >= chars.length) {
// 		return null; // Or an "empty" BreakPoss?
// 	    }
// 	    else {
// 		bp = new BreakPoss(this,
// 				   new TextBreakPosition(m_iNextStart));
// 	    }
// 	}
// 	return bp;
//     }


    /**
     * Return value indicating whether the next area to be generated could
     * start a new line. This should only be called in the "START" condition
     * if a previous inline BP couldn't end the line.
     * Return true if the first character is a potential linebreak character.
     */
    public boolean canBreakBefore(LayoutContext context) {
	char c = chars[m_iNextStart];
	return ((c == NEWLINE) || (textInfo.bWrap &&
		 (CharUtilities.isSpace(c) || s_breakChars.indexOf(c)>=0)));
    }

    /** Reset position for returning next BreakPossibility. */

    public void resetPosition(BreakPoss.Position prevPos) {
	if (prevPos != null) {
	    TextBreakPosition tbp = (TextBreakPosition)prevPos;
	    AreaInfo ai =
		 (AreaInfo) m_vecAreaInfo.elementAt(tbp.m_iAreaIndex);
	    if (ai.m_iBreakIndex != m_iNextStart) {
		m_iNextStart = ai.m_iBreakIndex;
		m_vecAreaInfo.setSize(tbp.m_iAreaIndex+1);
		System.err.println("Discarded previous text break pos");
		setFinished(false);
	    }
	}
	else {
	    // Reset to beginning!
	    System.err.println("TextBPLM: resetPosition(null)");
	    m_vecAreaInfo.setSize(0);
	    m_iNextStart = 0;
	    setFinished(false);
	}
    }


    /**
     * Return the next break possibility that fits the constraints.
     * @param context An object specifying the flags and input information
     * concerning the context of the BreakPoss.
     * @para prevPos An object specifying the previous Position returned
     * by a BreakPoss from this LM. It may be earlier than the current
     * pointer when doing hyphenation or starting a new line.
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
    public BreakPoss getNextBreakPoss(LayoutContext context,
				      BreakPoss.Position prevPos) {
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
	    // May have leading space too which can combine with a
	    // leading word-space or letter-space
	}


        // HANDLE SUPPRESSED LEADING SPACES
	if (context.suppressLeadingSpace()) {
	    /* If any leading space characters, ignore them. */ 
	    // NOTE: Skips word-space chars only, not other white-space!
	    for (; m_iNextStart < chars.length &&
		     chars[m_iNextStart]==SPACE; m_iNextStart++);
	    // If now at end, nothing to compose here!
	    if (m_iNextStart >= chars.length) {
		setFinished(true);
		return null; // Or an "empty" BreakPoss?
	    }
	}


	// Start of this "word", plus any non-suppressed leading space
	// This is any kind of white-space, not just word spaces

        short iThisStart = m_iNextStart;
        MinOptMax spaceIPD = new MinOptMax(0); // Variable IPD
        int wordIPD = 0;  // Non-stretching IPD (length in base units)

	// Handle inter-character spacing (word-space + letter-space)
	// What about context.getPendingSpace() on first char in word?
	SpaceSpecifier pendingSpace = new SpaceSpecifier(false);

        for (; m_iNextStart < chars.length; m_iNextStart++) {
            char c = chars[m_iNextStart];
	    if (CharUtilities.isAnySpace(c)==false) break;
            if (c==SPACE) {
		pendingSpace.addSpace(m_halfWS);
		spaceIPD.add(pendingSpace.resolve(false));
		wordIPD += m_spaceIPD; // Space glyph IPD
		pendingSpace.clear();
		pendingSpace.addSpace(m_halfWS);
	    }
	    else {
		// If we have letter-space, so we apply this to fixed-
		// width spaces (which are not word-space) also?
		spaceIPD.add(pendingSpace.resolve(false));
		pendingSpace.clear();
		wordIPD += CharUtilities.getCharWidth(c, textInfo.fs);
	    }
	}

	if (m_iNextStart < chars.length) {
	    spaceIPD.add(pendingSpace.resolve(false));
	}
	else {
	    // This FO ended with spaces. Return the BP
	    iFlags |= BreakPoss.ALL_ARE_SUPPRESS_AT_LB;
	    // lc.trailingSpaceSeq.addSpace(m_halfWS);
	    // Need to make SpaceSpecifier from m_halfWS!
	    // Or at least a spaceval
	    return makeBreakPoss(iThisStart, spaceIPD, 0, pendingSpace,
				 iFlags);
	}

        // Look for a legal line-break: breakable white-space and certain
        // characters such as '-' which can serve as word breaks.
        // Don't look for hyphenation points here though

        for (; m_iNextStart < chars.length; m_iNextStart++) {
            char c = chars[m_iNextStart];
            if ((c == NEWLINE) ||
		// Include any breakable white-space as break char
		//  even if fixed width
                (textInfo.bWrap &&
		 (CharUtilities.isSpace(c) ||
		  s_breakChars.indexOf(c)>=0))) {
		iFlags |= BreakPoss.CAN_BREAK_AFTER;
                if (c != SPACE) {
                    m_iNextStart++;
                    if (c != NEWLINE) {
                        wordIPD += CharUtilities.getCharWidth(c, textInfo.fs);
                    }
		    else {
			iFlags |= BreakPoss.FORCE;
		    }
                }
		return makeBreakPoss(iThisStart, spaceIPD, wordIPD, null,
				     iFlags);
            }
	    wordIPD += CharUtilities.getCharWidth(c, textInfo.fs);
	    // Note, if a normal non-breaking space, is it stretchable???
	    // If so, keep a count of these embedded spaces.
        }
	return makeBreakPoss(iThisStart, spaceIPD, wordIPD, null, iFlags);
    }


    private BreakPoss makeBreakPoss(short iWordStart, MinOptMax spaceIPD,
				    int wordDim,
				    SpaceSpecifier trailingSpace,
				    int flags)
    {
	MinOptMax ipd = new MinOptMax(wordDim);
	ipd.add(spaceIPD);

	// Position is the index of the info for this word in the vector
	m_vecAreaInfo.add(new AreaInfo(iWordStart, m_iNextStart, ipd));
        BreakPoss bp =
	    new BreakPoss(this,
			  new TextBreakPosition(m_vecAreaInfo.size()-1));

	ipd.add(m_ipdTotal); // sum of all words so far in line
	bp.setStackingSize(ipd);
	m_ipdTotal = ipd;
	// TODO: make this correct (see Keiron's code below!)
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
	}
	else {
	    bp.setTrailingSpace(new SpaceSpecifier(false));
	}
	bp.setLeadingSpace(new SpaceSpecifier(false));
        return bp;
    }


    /**
     * Add an area for each word and space (or one big one????)
     */
    public void addAreas(PositionIterator posIter) {
	// Add word areas
	TextBreakPosition tbpStart, tbpNext;
	while (posIter.hasNext()) {
	    tbpNext = (TextBreakPosition)posIter.next();
	    // System.err.println("tbp.pos = " + tbpNext.m_iAreaIndex);
	    AreaInfo ai = (AreaInfo)m_vecAreaInfo.
		elementAt(tbpNext.m_iAreaIndex);
	    // Make an area containing all characters between start and end.
	    Word word = createWord(new String(chars, ai.m_iStartIndex, 
				      ai.m_iBreakIndex- ai.m_iStartIndex),
				   ai.m_ipdArea.opt);
	    parentLM.addChild(word);
	}
    }



    protected Word createWord(String str, int width) {
        Word curWordArea = new Word();
        curWordArea.setWidth(width);
        curWordArea.setHeight(textInfo.fs.getAscender() - textInfo.fs.getDescender());
        curWordArea.setOffset(textInfo.fs.getAscender());
        curWordArea.info = new LayoutInfo();
        curWordArea.info.lead = textInfo.fs.getAscender();
        curWordArea.info.alignment = VerticalAlign.BASELINE;
        curWordArea.info.blOffset = true;

        curWordArea.setWord(str);
        Trait prop = new Trait();
        prop.propType = Trait.FONT_STATE;
        prop.data = textInfo.fs;
        curWordArea.addTrait(prop);
        return curWordArea;
    }


}

