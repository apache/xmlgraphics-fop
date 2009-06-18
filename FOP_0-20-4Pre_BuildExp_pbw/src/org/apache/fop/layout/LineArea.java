/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

// fop
import org.apache.fop.render.Renderer;
import org.apache.fop.layout.inline.*;
import org.apache.fop.datatypes.IDNode;
import org.apache.fop.fo.properties.WrapOption;
import org.apache.fop.fo.properties.WhiteSpaceCollapse;
import org.apache.fop.fo.properties.TextAlign;
import org.apache.fop.fo.properties.TextAlignLast;
import org.apache.fop.fo.properties.LeaderPattern;
import org.apache.fop.fo.properties.Hyphenate;
import org.apache.fop.fo.properties.CountryMaker;
import org.apache.fop.fo.properties.LanguageMaker;
import org.apache.fop.fo.properties.LeaderAlignment;
import org.apache.fop.fo.properties.VerticalAlign;
import org.apache.fop.layout.hyphenation.Hyphenation;
import org.apache.fop.layout.hyphenation.Hyphenator;
import org.apache.fop.configuration.Configuration;

// java
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.awt.Rectangle;

public class LineArea extends Area {

    protected int lineHeight;
    protected int halfLeading;
    protected int nominalFontSize;
    protected int nominalGlyphHeight;

    protected int allocationHeight;
    protected int startIndent;
    protected int endIndent;

    private int placementOffset;

    private FontState currentFontState;    // not the nominal, which is
    // in this.fontState
    private float red, green, blue;
    private int wrapOption;
    private int whiteSpaceCollapse;
    int vAlign;

    /* hyphenation */
    HyphenationProps hyphProps;

    /*
     * the width of text that has definitely made it into the line
     * area
     */
    protected int finalWidth = 0;

    /* the position to shift a link rectangle in order to compensate for links embedded within a word */
    protected int embeddedLinkStart = 0;

    /* the width of the current word so far */
    // protected int wordWidth = 0;

    /* values that prev (below) may take */
    protected static final int NOTHING = 0;
    protected static final int WHITESPACE = 1;
    protected static final int TEXT = 2;

    /* the character type of the previous character */
    protected int prev = NOTHING;

    /* the position in data[] of the start of the current word */
    // protected int wordStart;

    /* the length (in characters) of the current word */
    // protected int wordLength = 0;

    /* width of spaces before current word */
    protected int spaceWidth = 0;

    /*
     * the inline areas that have not yet been added to the line
     * because subsequent characters to come (in a different addText)
     * may be part of the same word
     */
    protected Vector pendingAreas = new Vector();

    /* the width of the pendingAreas */
    protected int pendingWidth = 0;

    /* text-decoration of the previous text */
    protected boolean prevUlState = false;
    protected boolean prevOlState = false;
    protected boolean prevLTState = false;

    public LineArea(FontState fontState, int lineHeight, int halfLeading,
                    int allocationWidth, int startIndent, int endIndent,
                    LineArea prevLineArea) {
        super(fontState);

        this.currentFontState = fontState;
        this.lineHeight = lineHeight;
        this.nominalFontSize = fontState.getFontSize();
        this.nominalGlyphHeight = fontState.getAscender()
                                  - fontState.getDescender();

        this.placementOffset = fontState.getAscender();
        this.contentRectangleWidth = allocationWidth - startIndent
                                     - endIndent;
        this.fontState = fontState;

        this.allocationHeight = this.nominalGlyphHeight;
        this.halfLeading = this.lineHeight - this.allocationHeight;

        this.startIndent = startIndent;
        this.endIndent = endIndent;

        if (prevLineArea != null) {
            Enumeration e = prevLineArea.pendingAreas.elements();
            Box b = null;
            // There might be InlineSpaces at the beginning
            // that should not be there - eat them
            boolean eatMoreSpace = true;
            int eatenWidth = 0;

            while (eatMoreSpace) {
                if (e.hasMoreElements()) {
                    b = (Box)e.nextElement();
                    if (b instanceof InlineSpace) {
                        InlineSpace is = (InlineSpace)b;
                        if (is.isEatable())
                            eatenWidth += is.getSize();
                        else
                            eatMoreSpace = false;
                    } else {
                        eatMoreSpace = false;
                    }
                } else {
                    eatMoreSpace = false;
                    b = null;
                }
            }

            while (b != null) {
                pendingAreas.addElement(b);
                if (e.hasMoreElements())
                    b = (Box)e.nextElement();
                else
                    b = null;
            }
            pendingWidth = prevLineArea.getPendingWidth() - eatenWidth;
        }
    }

    public int addPageNumberCitation(String refid, LinkSet ls) {

        /*
         * We should add code here to handle the case where the page number doesn't fit on the current line
         */

        // Space must be alloted to the page number, so currently we give it 3 spaces

        int width = currentFontState.width(currentFontState.mapChar(' '));


        PageNumberInlineArea pia = new PageNumberInlineArea(currentFontState,
                this.red, this.green, this.blue, refid, width);

        pia.setYOffset(placementOffset);
        pendingAreas.addElement(pia);
        pendingWidth += width;
        prev = TEXT;

        return -1;
    }


    /**
     * adds text to line area
     *
     * @return int character position
     */
    public int addText(char odata[], int start, int end, LinkSet ls,
                       TextState textState) {
        // this prevents an array index out of bounds
        // which occurs when some text is laid out again.
        if (start == -1)
            return -1;
        boolean overrun = false;

        int wordStart = start;
        int wordLength = 0;
        int wordWidth = 0;
        // With CID fonts, space isn't neccesary currentFontState.width(32)
        int whitespaceWidth = getCharWidth(' ');

        char[] data = new char[odata.length];
        char[] dataCopy = new char[odata.length];
        System.arraycopy(odata, 0, data, 0, odata.length);
        System.arraycopy(odata, 0, dataCopy, 0, odata.length);

        boolean isText = false;

        /* iterate over each character */
        for (int i = start; i < end; i++) {
            int charWidth;
            /* get the character */
            char c = data[i];
            if (!(isSpace(c) || (c == '\n') || (c == '\r') || (c == '\t')
                    || (c == '\u2028'))) {
                charWidth = getCharWidth(c);
                isText = true;
                // Add support for zero-width spaces
                if (charWidth <= 0 && c != '\u200B' && c != '\uFEFF')
                    charWidth = whitespaceWidth;
            } else {
                if ((c == '\n') || (c == '\r') || (c == '\t'))
                    charWidth = whitespaceWidth;
                else
                    charWidth = getCharWidth(c);

                isText = false;

                if (prev == WHITESPACE) {

                    // if current & previous are WHITESPACE

                    if (this.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE) {
                        if (isSpace(c)) {
                            spaceWidth += getCharWidth(c);
                        } else if (c == '\n' || c == '\u2028') {
                            // force line break
                            if (spaceWidth > 0) {
                                InlineSpace is = new InlineSpace(spaceWidth);
                                is.setUnderlined(textState.getUnderlined());
                                is.setOverlined(textState.getOverlined());
                                is.setLineThrough(textState.getLineThrough());
                                addChild(is);
                                finalWidth += spaceWidth;
                                spaceWidth = 0;
                            }
                            return i + 1;
                        } else if (c == '\t') {
                            spaceWidth += 8 * whitespaceWidth;
                        }
                    } else if (c == '\u2028') {
                        // Line separator
                        // Breaks line even if WhiteSpaceCollapse = True
                        if (spaceWidth > 0) {
                            InlineSpace is = new InlineSpace(spaceWidth);
                            is.setUnderlined(textState.getUnderlined());
                            is.setOverlined(textState.getOverlined());
                            is.setLineThrough(textState.getLineThrough());
                            addChild(is);
                            finalWidth += spaceWidth;
                            spaceWidth = 0;
                        }
                        return i + 1;
                    }

                } else if (prev == TEXT) {

                    // if current is WHITESPACE and previous TEXT
                    // the current word made it, so
                    // add the space before the current word (if there
                    // was some)

                    if (spaceWidth > 0) {
                        InlineSpace is = new InlineSpace(spaceWidth);
                        if (prevUlState) {
                            is.setUnderlined(textState.getUnderlined());
                        }
                        if (prevOlState) {
                            is.setOverlined(textState.getOverlined());
                        }
                        if (prevLTState) {
                            is.setLineThrough(textState.getLineThrough());
                        }
                        addChild(is);
                        finalWidth += spaceWidth;
                        spaceWidth = 0;
                    }

                    // add any pending areas

                    Enumeration e = pendingAreas.elements();
                    while (e.hasMoreElements()) {
                        Box box = (Box)e.nextElement();
                        if (box instanceof InlineArea) {
                            if (ls != null) {
                                Rectangle lr =
                                    new Rectangle(finalWidth, 0,
                                                  ((InlineArea)box).getContentWidth(),
                                                  fontState.getFontSize());
                                ls.addRect(lr, this, (InlineArea)box);
                            }
                        }
                        addChild(box);
                    }

                    finalWidth += pendingWidth;

                    // reset pending areas array
                    pendingWidth = 0;
                    pendingAreas = new Vector();

                    // add the current word

                    if (wordLength > 0) {
                        // The word might contain nonbreaking
                        // spaces. Split the word and add InlineSpace
                        // as necessary. All spaces inside the word
                        // Have a fixed width.
                        addSpacedWord(new String(data, wordStart, wordLength),
                                      ls, finalWidth, 0, textState, false);
                        finalWidth += wordWidth;

                        // reset word width
                        wordWidth = 0;
                    }

                    // deal with this new whitespace following the
                    // word we just added
                    prev = WHITESPACE;

                    embeddedLinkStart =
                        0;    // reset embeddedLinkStart since a space was encountered

                    spaceWidth = getCharWidth(c);

                    /*
                     * here is the place for space-treatment value 'ignore':
                     * if (this.spaceTreatment ==
                     * SpaceTreatment.IGNORE) {
                     * // do nothing
                     * } else {
                     * spaceWidth = currentFontState.width(32);
                     * }
                     */


                    if (this.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE) {
                        if (c == '\n' || c == '\u2028') {
                            // force a line break
                            return i + 1;
                        } else if (c == '\t') {
                            spaceWidth = whitespaceWidth;
                        }
                    } else if (c == '\u2028') {
                        return i + 1;
                    }
                } else {

                    // if current is WHITESPACE and no previous

                    if (this.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE) {
                        if (isSpace(c)) {
                            prev = WHITESPACE;
                            spaceWidth = getCharWidth(c);
                        } else if (c == '\n') {
                            // force line break
                            // textdecoration not used because spaceWidth is 0
                            InlineSpace is = new InlineSpace(spaceWidth);
                            addChild(is);
                            return i + 1;
                        } else if (c == '\t') {
                            prev = WHITESPACE;
                            spaceWidth = 8 * whitespaceWidth;
                        }

                    } else {
                        // skip over it
                        wordStart++;
                    }
                }

            }

            if (isText) {                        // current is TEXT

                if (prev == WHITESPACE) {

                    // if current is TEXT and previous WHITESPACE

                    wordWidth = charWidth;
                    if ((finalWidth + spaceWidth + wordWidth)
                            > this.getContentWidth()) {
                        if (overrun) {
                            //log.debug("area contents overflows area");
                        }
                        if (this.wrapOption == WrapOption.WRAP) {
                            return i;
                        }
                    }
                    prev = TEXT;
                    wordStart = i;
                    wordLength = 1;
                } else if (prev == TEXT) {
                    wordLength++;
                    wordWidth += charWidth;
                } else {                         // nothing previous

                    prev = TEXT;
                    wordStart = i;
                    wordLength = 1;
                    wordWidth = charWidth;
                }

                if ((finalWidth + spaceWidth + pendingWidth + wordWidth)
                        > this.getContentWidth()) {

                    // BREAK MID WORD
                    if (canBreakMidWord()) {
                        addSpacedWord(new String(data, wordStart, wordLength - 1),
                                      ls,
                                      finalWidth + spaceWidth
                                      + embeddedLinkStart, spaceWidth,
                                                           textState, false);
                        finalWidth += wordWidth;
                        wordWidth = 0;
                        return i;
                    }

                    if (this.wrapOption == WrapOption.WRAP) {

                        if (hyphProps.hyphenate == Hyphenate.TRUE) {
                            int ret = wordStart;
                            ret = this.doHyphenation(dataCopy, i, wordStart,
                                                     this.getContentWidth()
                                                     - (finalWidth
                                                        + spaceWidth
                                                        + pendingWidth));

                            // current word couldn't be hypenated
                            // couldn't fit first word
                            // I am at the beginning of my line
                            if ((ret == wordStart) &&
                                (wordStart == start) &&
                                (finalWidth == 0)) {

                                //log.debug("area contents overflows area");
                                addSpacedWord(new String(data, wordStart, wordLength - 1),
                                              ls,
                                              finalWidth + spaceWidth
                                              + embeddedLinkStart,
                                              spaceWidth, textState, false);

                                finalWidth += wordWidth;
                                wordWidth = 0;
                                ret = i;
                            }
                            return ret;
                        } else if (wordStart == start) {
                            // first word
                            overrun = true;
                            // if not at start of line, return word start
                            // to try again on a new line
                            if (finalWidth > 0) {
                                return wordStart;
                            }
                        } else {
                            return wordStart;
                        }

                    }
                }
            }
        } // end of iteration over text

        if (prev == TEXT) {

            if (spaceWidth > 0) {
                InlineSpace pis = new InlineSpace(spaceWidth);
                // Make sure that this space doesn't occur as
                // first thing in the next line
                pis.setEatable(true);
                if (prevUlState) {
                    pis.setUnderlined(textState.getUnderlined());
                }
                if (prevOlState) {
                    pis.setOverlined(textState.getOverlined());
                }
                if (prevLTState) {
                    pis.setLineThrough(textState.getLineThrough());
                }
                pendingAreas.addElement(pis);
                pendingWidth += spaceWidth;
                spaceWidth = 0;
            }

            addSpacedWord(new String(data, wordStart, wordLength), ls,
                          finalWidth + spaceWidth + embeddedLinkStart,
                          spaceWidth, textState, true);

            embeddedLinkStart += wordWidth;
            wordWidth = 0;
        }

        if (overrun) {
            //log.debug("area contents overflows area");
        }
        return -1;
    }

    /**
     * adds a Leader; actually the method receives the leader properties
     * and creates a leader area or an inline area which is appended to
     * the children of the containing line area. <br>
     * leader pattern use-content is not implemented.
     */
    public void addLeader(int leaderPattern, int leaderLengthMinimum,
                          int leaderLengthOptimum, int leaderLengthMaximum,
                          int ruleStyle, int ruleThickness,
                          int leaderPatternWidth, int leaderAlignment) {
        WordArea leaderPatternArea;
        int leaderLength = 0;
        char dotIndex = '.';           // currentFontState.mapChar('.');
        int dotWidth =
            currentFontState.width(currentFontState.mapChar(dotIndex));
        char whitespaceIndex = ' ';    // currentFontState.mapChar(' ');
        int whitespaceWidth =
            currentFontState.width(currentFontState.mapChar(whitespaceIndex));

        int remainingWidth = this.getContentWidth()
                             - this.getCurrentXPosition();

        /**
         * checks whether leaderLenghtOptimum fits into rest of line;
         * should never overflow, as it has been checked already in BlockArea
         * first check: use remaining width if it smaller than optimum oder maximum
         */
        if ((remainingWidth <= leaderLengthOptimum)
                || (remainingWidth <= leaderLengthMaximum)) {
            leaderLength = remainingWidth;
        } else if ((remainingWidth > leaderLengthOptimum)
                   && (remainingWidth > leaderLengthMaximum)) {
            leaderLength = leaderLengthMaximum;
        } else if ((leaderLengthOptimum > leaderLengthMaximum)
                   && (leaderLengthOptimum < remainingWidth)) {
            leaderLength = leaderLengthOptimum;
        }

        // stop if leader-length is too small
        if (leaderLength <= 0) {
            return;
        }

        switch (leaderPattern) {
        case LeaderPattern.SPACE:
            InlineSpace spaceArea = new InlineSpace(leaderLength);
            pendingAreas.addElement(spaceArea);
            break;
        case LeaderPattern.RULE:
            LeaderArea leaderArea = new LeaderArea(fontState, red, green,
                                                   blue, "", leaderLength,
                                                   leaderPattern,
                                                   ruleThickness, ruleStyle);
            leaderArea.setYOffset(placementOffset);
            pendingAreas.addElement(leaderArea);
            break;
        case LeaderPattern.DOTS:
            // if the width of a dot is larger than leader-pattern-width
            // ignore this property
            if (leaderPatternWidth < dotWidth) {
                leaderPatternWidth = 0;
            }
            // if value of leader-pattern-width is 'use-font-metrics' (0)
            if (leaderPatternWidth == 0) {
                pendingAreas.addElement(this.buildSimpleLeader(dotIndex,
                        leaderLength));
            } else {
                // if leader-alignment is used, calculate space to insert before leader
                // so that all dots will be parallel.
                if (leaderAlignment == LeaderAlignment.REFERENCE_AREA) {
                    int spaceBeforeLeader =
                        this.getLeaderAlignIndent(leaderLength,
                                                  leaderPatternWidth);
                    // appending indent space leader-alignment
                    // setting InlineSpace to false, so it is not used in line justification
                    if (spaceBeforeLeader != 0) {
                        pendingAreas.addElement(new InlineSpace(spaceBeforeLeader,
                                                                false));
                        pendingWidth += spaceBeforeLeader;
                        // shorten leaderLength, otherwise - in case of
                        // leaderLength=remaining length - it will cut off the end of
                        // leaderlength
                        leaderLength -= spaceBeforeLeader;
                    }
                }

                // calculate the space to insert between the dots and create a
                // inline area with this width
                InlineSpace spaceBetweenDots =
                    new InlineSpace(leaderPatternWidth - dotWidth, false);

                leaderPatternArea = new WordArea(currentFontState, this.red,
                                                 this.green, this.blue,
                                                 new String("."), dotWidth);
                leaderPatternArea.setYOffset(placementOffset);
                int dotsFactor =
                    (int)Math.floor(((double)leaderLength)
                                    / ((double)leaderPatternWidth));

                // add combination of dot + space to fill leader
                // is there a way to do this in a more effective way?
                for (int i = 0; i < dotsFactor; i++) {
                    pendingAreas.addElement(leaderPatternArea);
                    pendingAreas.addElement(spaceBetweenDots);
                }
                // append at the end some space to fill up to leader length
                pendingAreas.addElement(new InlineSpace(leaderLength
                                                        - dotsFactor
                                                          * leaderPatternWidth));
            }
            break;
        // leader pattern use-content not implemented.
        case LeaderPattern.USECONTENT:
            //log.error("leader-pattern=\"use-content\" not "
            //                       + "supported by this version of Fop");
            return;
        }
        // adds leader length to length of pending inline areas
        pendingWidth += leaderLength;
        // sets prev to TEXT and makes so sure, that also blocks only
        // containing leaders are processed
        prev = TEXT;
    }

    /**
     * adds pending inline areas to the line area
     * normally done, when the line area is filled and
     * added as child to the parent block area
     */
    public void addPending() {
        if (spaceWidth > 0) {
            addChild(new InlineSpace(spaceWidth));
            finalWidth += spaceWidth;
            spaceWidth = 0;
        }

        Enumeration e = pendingAreas.elements();
        while (e.hasMoreElements()) {
            Box box = (Box)e.nextElement();
            addChild(box);
        }

        finalWidth += pendingWidth;

        // reset pending areas array
        pendingWidth = 0;
        pendingAreas = new Vector();
    }

    /**
     * aligns line area
     *
     */
    public void align(int type) {
        int padding = 0;

        switch (type) {
        case TextAlign.START:      // left
            padding = this.getContentWidth() - finalWidth;
            endIndent += padding;
            break;
        case TextAlign.END:        // right
            padding = this.getContentWidth() - finalWidth;
            startIndent += padding;
            break;
        case TextAlign.CENTER:     // center
            padding = (this.getContentWidth() - finalWidth) / 2;
            startIndent += padding;
            endIndent += padding;
            break;
        case TextAlign.JUSTIFY:    // justify
            // first pass - count the spaces
            int spaceCount = 0;
            Enumeration e = children.elements();
            while (e.hasMoreElements()) {
                Box b = (Box)e.nextElement();
                if (b instanceof InlineSpace) {
                    InlineSpace space = (InlineSpace)b;
                    if (space.getResizeable()) {
                        spaceCount++;
                    }
                }
            }
            if (spaceCount > 0) {
                padding = (this.getContentWidth() - finalWidth) / spaceCount;
            } else {               // no spaces
                padding = 0;
            }
            // second pass - add additional space
            spaceCount = 0;
            e = children.elements();
            while (e.hasMoreElements()) {
                Box b = (Box)e.nextElement();
                if (b instanceof InlineSpace) {
                    InlineSpace space = (InlineSpace)b;
                    if (space.getResizeable()) {
                        space.setSize(space.getSize() + padding);
                        spaceCount++;
                    }
                } else if (b instanceof InlineArea) {
                    ((InlineArea)b).setXOffset(spaceCount * padding);
                }

            }
        }
    }

    /**
     * Balance (vertically) the inline areas within this line.
     */
    public void verticalAlign() {
        int superHeight = -this.placementOffset;
        int maxHeight = this.allocationHeight;
        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            Box b = (Box)e.nextElement();
            if (b instanceof InlineArea) {
                InlineArea ia = (InlineArea)b;
                if (ia instanceof WordArea) {
                    ia.setYOffset(placementOffset);
                }
                if (ia.getHeight() > maxHeight) {
                    maxHeight = ia.getHeight();
                }
                int vert = ia.getVerticalAlign();
                if (vert == VerticalAlign.SUPER) {
                    int fh = fontState.getAscender();
                    ia.setYOffset((int)(placementOffset - (2 * fh / 3.0)));
                } else if (vert == VerticalAlign.SUB) {
                    int fh = fontState.getAscender();
                    ia.setYOffset((int)(placementOffset + (2 * fh / 3.0)));
                }
            } else {}
        }
        // adjust the height of this line to the
        // resulting alignment height.
        this.allocationHeight = maxHeight;
    }

    public void changeColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void changeFont(FontState fontState) {
        this.currentFontState = fontState;
    }

    public void changeWhiteSpaceCollapse(int whiteSpaceCollapse) {
        this.whiteSpaceCollapse = whiteSpaceCollapse;
    }

    public void changeWrapOption(int wrapOption) {
        this.wrapOption = wrapOption;
    }

    public void changeVerticalAlign(int vAlign) {
        this.vAlign = vAlign;
    }

    public int getEndIndent() {
        return endIndent;
    }

    public int getHeight() {
        return this.allocationHeight;
    }

    public int getPlacementOffset() {
        return this.placementOffset;
    }

    public int getStartIndent() {
        return startIndent;
    }

    public boolean isEmpty() {
        return !(pendingAreas.size() > 0 || children.size() > 0);
        // return (prev == NOTHING);
    }

    public Vector getPendingAreas() {
        return pendingAreas;
    }

    public int getPendingWidth() {
        return pendingWidth;
    }

    public void setPendingAreas(Vector areas) {
        pendingAreas = areas;
    }

    public void setPendingWidth(int width) {
        pendingWidth = width;
    }

    /**
     * sets hyphenation related traits: language, country, hyphenate, hyphenation-character
     * and minimum number of character to remain one the previous line and to be on the
     * next line.
     */
    public void changeHyphenation(HyphenationProps hyphProps) {
        this.hyphProps = hyphProps;
    }


    /**
     * creates a leader as String out of the given char and the leader length
     * and wraps it in an InlineArea which is returned
     */
    private InlineArea buildSimpleLeader(char c, int leaderLength) {
        int width = this.currentFontState.width(currentFontState.mapChar(c));
        if (width == 0) {
            //log.error("char " + c
            //                       + " has width 0. Using width 100 instead.");
            width = 100;
        }
        int factor = (int)Math.floor(leaderLength / width);
        char[] leaderChars = new char[factor];
        for (int i = 0; i < factor; i++) {
            leaderChars[i] = c;    // currentFontState.mapChar(c);
        }
        WordArea leaderPatternArea = new WordArea(currentFontState, this.red,
                                                  this.green, this.blue,
                                                  new String(leaderChars),
                                                  leaderLength);
        leaderPatternArea.setYOffset(placementOffset);
        return leaderPatternArea;
    }

    /**
     * calculates the width of space which has to be inserted before the
     * start of the leader, so that all leader characters are aligned.
     * is used if property leader-align is set. At the moment only the value
     * for leader-align="reference-area" is supported.
     *
     */
    private int getLeaderAlignIndent(int leaderLength,
                                     int leaderPatternWidth) {
        // calculate position of used space in line area
        double position = getCurrentXPosition();
        // calculate factor of next leader pattern cycle
        double nextRepeatedLeaderPatternCycle = Math.ceil(position
                / leaderPatternWidth);
        // calculate difference between start of next leader
        // pattern cycle and already used space
        double difference =
            (leaderPatternWidth * nextRepeatedLeaderPatternCycle) - position;
        return (int)difference;
    }

    /**
     * calculates the used space in this line area
     */
    private int getCurrentXPosition() {
        return finalWidth + spaceWidth + startIndent + pendingWidth;
    }

    /**
     * extracts a complete word from the character data
     */
    private String getHyphenationWord(char[] characters, int wordStart) {
        boolean wordendFound = false;
        int counter = 0;
        char[] newWord = new char[characters.length];    // create a buffer
        while ((!wordendFound)
               && ((wordStart + counter) < characters.length)) {
            char tk = characters[wordStart + counter];
            if (Character.isLetter(tk)) {
                newWord[counter] = tk;
                counter++;
            } else {
                wordendFound = true;
            }
        }
        return new String(newWord, 0, counter);
    }


    /**
     * extracts word for hyphenation and calls hyphenation package,
     * handles cases of inword punctuation and quotation marks at the beginning
     * of words, but not in a internationalized way
     */
    public int doHyphenation(char[] characters, int position, int wordStart,
                             int remainingWidth) {
        // check whether the language property has been set
        if (this.hyphProps.language.equalsIgnoreCase("none")) {
            //log.error("if property 'hyphenate' is used, a language must be specified");
            return wordStart;
        }

        /**
         * remaining part string of hyphenation
         */
        StringBuffer remainingString = new StringBuffer();

        /**
         * for words with some inword punctuation like / or -
         */
        StringBuffer preString = null;

        /**
         * char before the word, probably whitespace
         */
        char startChar = ' ';    // characters[wordStart-1];

        /**
         * in word punctuation character
         */
        char inwordPunctuation;

        /**
         * the complete word handed to the hyphenator
         */
        String wordToHyphenate;

        // width of hyphenation character
        int hyphCharWidth =
            this.currentFontState.width(currentFontState.mapChar(this.hyphProps.hyphenationChar));
        remainingWidth -= hyphCharWidth;

        // handles ' or " at the beginning of the word
        if (characters[wordStart] == '"' || characters[wordStart] == '\'') {
            remainingString.append(characters[wordStart]);
            // extracts whole word from string
            wordToHyphenate = getHyphenationWord(characters, wordStart + 1);
        } else {
            wordToHyphenate = getHyphenationWord(characters, wordStart);
        }

        // if the extracted word is smaller than the remaining width
        // we have a non letter character inside the word. at the moment
        // we will only handle hard hyphens and slashes
        if (getWordWidth(wordToHyphenate) < remainingWidth) {
            inwordPunctuation =
                characters[wordStart + wordToHyphenate.length()];
            if (inwordPunctuation == '-' || inwordPunctuation == '/') {
                preString = new StringBuffer(wordToHyphenate);
                preString = preString.append(inwordPunctuation);
                wordToHyphenate =
                    getHyphenationWord(characters,
                                       wordStart + wordToHyphenate.length()
                                       + 1);
                remainingWidth -=
                    (getWordWidth(wordToHyphenate)
                     + this.currentFontState.width(currentFontState.mapChar(inwordPunctuation)));
            }
        }

        // are there any hyphenation points
        Hyphenation hyph =
            Hyphenator.hyphenate(hyphProps.language, hyphProps.country,
                                 wordToHyphenate,
                                 hyphProps.hyphenationRemainCharacterCount,
                                 hyphProps.hyphenationPushCharacterCount);
        // no hyphenation points and no inword non letter character
        if (hyph == null && preString == null) {
            if (remainingString.length() > 0) {
                return wordStart - 1;
            } else {
                return wordStart;
            }

            // no hyphenation points, but a inword non-letter character
        } else if (hyph == null && preString != null) {
            remainingString.append(preString);
            // is.addMapWord(startChar,remainingString);
            this.addWord(startChar, remainingString);
            return wordStart + remainingString.length();
            // hyphenation points and no inword non-letter character
        } else if (hyph != null && preString == null) {
            int index = getFinalHyphenationPoint(hyph, remainingWidth);
            if (index != -1) {
                remainingString.append(hyph.getPreHyphenText(index));
                remainingString.append(this.hyphProps.hyphenationChar);
                // is.addMapWord(startChar,remainingString);
                this.addWord(startChar, remainingString);
                return wordStart + remainingString.length() - 1;
            }
            // hyphenation points and a inword non letter character
        } else if (hyph != null && preString != null) {
            int index = getFinalHyphenationPoint(hyph, remainingWidth);
            if (index != -1) {
                remainingString.append(preString.append(hyph.getPreHyphenText(index)));
                remainingString.append(this.hyphProps.hyphenationChar);
                // is.addMapWord(startChar,remainingString);
                this.addWord(startChar, remainingString);
                return wordStart + remainingString.length() - 1;
            } else {
                remainingString.append(preString);
                // is.addMapWord(startChar,remainingString);
                this.addWord(startChar, remainingString);
                return wordStart + remainingString.length();
            }
        }
        return wordStart;
    }


    /**
     * Calculates the wordWidth using the actual fontstate
     */
    private int getWordWidth(String word) {
        if (word == null)
            return 0;
        int wordLength = word.length();
        int width = 0;
        char[] characters = new char[wordLength];
        word.getChars(0, wordLength, characters, 0);

        for (int i = 0; i < wordLength; i++) {
            width += getCharWidth(characters[i]);
        }
        return width;
    }

    public int getRemainingWidth() {
        return this.getContentWidth() - this.getCurrentXPosition();
    }

    public void setLinkSet(LinkSet ls) {}

    public void addInlineArea(Area box) {
        addPending();
        addChild(box);
        prev = TEXT;
        finalWidth += box.getContentWidth();
    }

    public void addInlineSpace(InlineSpace is, int spaceWidth) {
        addChild(is);
        finalWidth += spaceWidth;
        // spaceWidth = 0;
    }

    /**
     * adds a single character to the line area tree
     */
    public int addCharacter(char data, LinkSet ls, boolean ul) {
        WordArea ia = null;
        int remainingWidth = this.getContentWidth()
                             - this.getCurrentXPosition();
        int width =
            this.currentFontState.width(currentFontState.mapChar(data));
        // if it doesn't fit, return
        if (width > remainingWidth) {
            return org.apache.fop.fo.flow.Character.DOESNOT_FIT;
        } else {
            // if whitespace-collapse == true, discard character
            if (Character.isSpaceChar(data)
                    && whiteSpaceCollapse == WhiteSpaceCollapse.TRUE) {
                return org.apache.fop.fo.flow.Character.OK;
            }
            // create new InlineArea
            ia = new WordArea(currentFontState, this.red, this.green,
                              this.blue, new Character(data).toString(),
                              width);
            ia.setYOffset(placementOffset);
            ia.setUnderlined(ul);
            pendingAreas.addElement(ia);
            if (Character.isSpaceChar(data)) {
                this.spaceWidth = +width;
                prev = LineArea.WHITESPACE;
            } else {
                pendingWidth += width;
                prev = LineArea.TEXT;
            }
            return org.apache.fop.fo.flow.Character.OK;
        }
    }


    /**
     * Same as addWord except that characters in wordBuf is mapped
     * to the current fontstate's encoding
     */
    private void addMapWord(char startChar, StringBuffer wordBuf) {
        StringBuffer mapBuf = new StringBuffer(wordBuf.length());
        for (int i = 0; i < wordBuf.length(); i++) {
            mapBuf.append(currentFontState.mapChar(wordBuf.charAt(i)));
        }

        addWord(startChar, mapBuf);
    }

    /**
     * adds a InlineArea containing the String startChar+wordBuf to the line area children.
     */
    private void addWord(char startChar, StringBuffer wordBuf) {
        String word = (wordBuf != null) ? wordBuf.toString() : "";
        WordArea hia;
        int startCharWidth = getCharWidth(startChar);

        if (isAnySpace(startChar)) {
            this.addChild(new InlineSpace(startCharWidth));
        } else {
            hia = new WordArea(currentFontState, this.red, this.green,
                               this.blue,
                               new Character(startChar).toString(), 1);
            hia.setYOffset(placementOffset);
            this.addChild(hia);
        }
        int wordWidth = this.getWordWidth(word);
        hia = new WordArea(currentFontState, this.red, this.green, this.blue,
                           word, word.length());
        hia.setYOffset(placementOffset);
        this.addChild(hia);

        // calculate the space needed
        finalWidth += startCharWidth + wordWidth;
    }


    /**
     * extracts from a hyphenated word the best (most greedy) fit
     */
    private int getFinalHyphenationPoint(Hyphenation hyph,
                                         int remainingWidth) {
        int[] hyphenationPoints = hyph.getHyphenationPoints();
        int numberOfHyphenationPoints = hyphenationPoints.length;

        int index = -1;
        String wordBegin = "";
        int wordBeginWidth = 0;

        for (int i = 0; i < numberOfHyphenationPoints; i++) {
            wordBegin = hyph.getPreHyphenText(i);
            if (this.getWordWidth(wordBegin) > remainingWidth) {
                break;
            }
            index = i;
        }
        return index;
    }

    /**
     * Checks if it's legal to break a word in the middle
     * based on the current language property.
     * @return true if legal to break word in the middle
     */
    private boolean canBreakMidWord() {
        boolean ret = false;
        if (hyphProps != null && hyphProps.language != null
                &&!hyphProps.language.equals("NONE")) {
            String lang = hyphProps.language.toLowerCase();
            if ("zh".equals(lang) || "ja".equals(lang) || "ko".equals(lang)
                    || "vi".equals(lang))
                ret = true;
        }
        return ret;
    }

    /**
     * Helper method for getting the width of a unicode char
     * from the current fontstate.
     * This also performs some guessing on widths on various
     * versions of space that might not exists in the font.
     */
    private int getCharWidth(char c) {
        int width = currentFontState.width(currentFontState.mapChar(c));
        if (width <= 0) {
            // Estimate the width of spaces not represented in
            // the font
            int em = currentFontState.width(currentFontState.mapChar('m'));
            int en = currentFontState.width(currentFontState.mapChar('n'));
            if (em <= 0)
                em = 500 * currentFontState.getFontSize();
            if (en <= 0)
                en = em - 10;

            if (c == ' ')
                width = em;
            if (c == '\u2000')
                width = en;
            if (c == '\u2001')
                width = em;
            if (c == '\u2002')
                width = em / 2;
            if (c == '\u2003')
                width = currentFontState.getFontSize();
            if (c == '\u2004')
                width = em / 3;
            if (c == '\u2005')
                width = em / 4;
            if (c == '\u2006')
                width = em / 6;
            if (c == '\u2007')
                width = getCharWidth(' ');
            if (c == '\u2008')
                width = getCharWidth('.');
            if (c == '\u2009')
                width = em / 5;
            if (c == '\u200A')
                width = 5;
            if (c == '\u200B')
                width = 100;
            if (c == '\u00A0')
                width = getCharWidth(' ');
            if (c == '\u202F')
                width = getCharWidth(' ') / 2;
            if (c == '\u3000')
                width = getCharWidth(' ') * 2;
            if ((c == '\n') || (c == '\r') || (c == '\t'))
                width = getCharWidth(' ');
        }

        return width;
    }


    /**
     * Helper method to determine if the character is a
     * space with normal behaviour. Normal behaviour means that
     * it's not non-breaking
     */
    private boolean isSpace(char c) {
        if (c == ' ' || c == '\u2000' ||    // en quad
        c == '\u2001' ||                    // em quad
        c == '\u2002' ||                    // en space
        c == '\u2003' ||                    // em space
        c == '\u2004' ||                    // three-per-em space
        c == '\u2005' ||                    // four--per-em space
        c == '\u2006' ||                    // six-per-em space
        c == '\u2007' ||                    // figure space
        c == '\u2008' ||                    // punctuation space
        c == '\u2009' ||                    // thin space
        c == '\u200A' ||                    // hair space
        c == '\u200B')                      // zero width space
            return true;
        else
            return false;
    }


    /**
     * Method to determine if the character is a nonbreaking
     * space.
     */
    private boolean isNBSP(char c) {
        if (c == '\u00A0' || c == '\u202F' ||    // narrow no-break space
        c == '\u3000' ||                    // ideographic space
        c == '\uFEFF') {                    // zero width no-break space
            return true;
        } else
            return false;
    }

    /**
     * @return true if the character represents any kind of space
     */
    private boolean isAnySpace(char c) {
        boolean ret = (isSpace(c) || isNBSP(c));
        return ret;
    }

    /**
     * Add a word that might contain non-breaking spaces.
     * Split the word into WordArea and InlineSpace and add it.
     * If addToPending is true, add to pending areas.
     */
    private void addSpacedWord(String word, LinkSet ls, int startw,
                               int spacew, TextState textState,
                               boolean addToPending) {
        StringTokenizer st = new StringTokenizer(word, "\u00A0\u202F\u3000\uFEFF", true);
        int extraw = 0;
        while (st.hasMoreTokens()) {
            String currentWord = st.nextToken();

            if (currentWord.length() == 1
                    && (isNBSP(currentWord.charAt(0)))) {
                // Add an InlineSpace
                int spaceWidth = getCharWidth(currentWord.charAt(0));
                if (spaceWidth > 0) {
                    InlineSpace is = new InlineSpace(spaceWidth);
                    extraw += spaceWidth;
                    if (prevUlState) {
                        is.setUnderlined(textState.getUnderlined());
                    }
                    if (prevOlState) {
                        is.setOverlined(textState.getOverlined());
                    }
                    if (prevLTState) {
                        is.setLineThrough(textState.getLineThrough());
                    }

                    if (addToPending) {
                        pendingAreas.addElement(is);
                        pendingWidth += spaceWidth;
                    } else {
                        addChild(is);
                    }
                }
            } else {
                WordArea ia = new WordArea(currentFontState, this.red,
                                           this.green, this.blue,
                                           currentWord,
                                           getWordWidth(currentWord));
                ia.setYOffset(placementOffset);
                ia.setUnderlined(textState.getUnderlined());
                prevUlState = textState.getUnderlined();
                ia.setOverlined(textState.getOverlined());
                prevOlState = textState.getOverlined();
                ia.setLineThrough(textState.getLineThrough());
                prevLTState = textState.getLineThrough();
                ia.setVerticalAlign(vAlign);

                if (addToPending) {
                    pendingAreas.addElement(ia);
                    pendingWidth += getWordWidth(currentWord);
                } else {
                    addChild(ia);
                }
                if (ls != null) {
                    Rectangle lr = new Rectangle(startw + extraw, spacew,
                                                 ia.getContentWidth(),
                                                 fontState.getFontSize());
                    ls.addRect(lr, this, ia);
                }
            }
        }
    }

}

