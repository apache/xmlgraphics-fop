/*-- $Id$ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */

package org.apache.fop.layout;

//fop
import org.apache.fop.render.Renderer;
import org.apache.fop.messaging.MessageHandler;
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

//java
import java.util.Vector;
import java.util.Enumeration;
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

    private FontState currentFontState; // not the nominal, which is
    // in this.fontState
    private float red, green, blue;
    private int wrapOption;
    private int whiteSpaceCollapse;
    int vAlign;

    /*hyphenation*/
    protected int hyphenate;
    protected char hyphenationChar;
    protected int hyphenationPushCharacterCount;
    protected int hyphenationRemainCharacterCount;
    protected String language;
    protected String country;

    /* the width of text that has definitely made it into the line
       area */
    protected int finalWidth = 0;

    /* the position to shift a link rectangle in order to compensate for links embedded within a word*/
    protected int embeddedLinkStart = 0;

    /* the width of the current word so far */
//    protected int wordWidth = 0;

    /* values that prev (below) may take */
    protected static final int NOTHING = 0;
    protected static final int WHITESPACE = 1;
    protected static final int TEXT = 2;

    /* the character type of the previous character */
    protected int prev = NOTHING;

    /* the position in data[] of the start of the current word */
//    protected int wordStart;

    /* the length (in characters) of the current word */
//    protected int wordLength = 0;

    /* width of spaces before current word */
    protected int spaceWidth = 0;

    /* the inline areas that have not yet been added to the line
       because subsequent characters to come (in a different addText)
       may be part of the same word */
    protected Vector pendingAreas = new Vector();

    /* the width of the pendingAreas */
    protected int pendingWidth = 0;

    /* text-decoration of the previous text */
    protected boolean prevUlState = false;
    protected boolean prevOlState = false;
    protected boolean prevLTState = false;

    public LineArea(FontState fontState, int lineHeight,
                    int halfLeading, int allocationWidth, int startIndent,
                    int endIndent, LineArea prevLineArea) {
        super(fontState);

        this.currentFontState = fontState;
        this.lineHeight = lineHeight;
        this.nominalFontSize = fontState.getFontSize();
        this.nominalGlyphHeight =
          fontState.getAscender() - fontState.getDescender();

        this.placementOffset = fontState.getAscender();
        this.contentRectangleWidth =
          allocationWidth - startIndent - endIndent;
        this.fontState = fontState;

        this.allocationHeight = this.nominalGlyphHeight;
        this.halfLeading = this.lineHeight - this.allocationHeight;

        this.startIndent = startIndent;
        this.endIndent = endIndent;

        if (prevLineArea != null) {
            Enumeration e = prevLineArea.pendingAreas.elements();
            while (e.hasMoreElements()) {
                pendingAreas.addElement(e.nextElement());
            }
            pendingWidth = prevLineArea.getPendingWidth();
        }
    }

    public void render(Renderer renderer) {
        renderer.renderLineArea(this);
    }

    public int addPageNumberCitation(String refid, LinkSet ls) {

        /* We should add code here to handle the case where the page number doesn't fit on the current line
        */

        //Space must be alloted to the page number, so currently we give it 3 spaces
        
        int width = currentFontState.width(currentFontState.mapChar(' '));


        PageNumberInlineArea pia =
          new PageNumberInlineArea(currentFontState, this.red,
                                   this.green, this.blue, refid, width);

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
        if(start == -1) return -1;
        boolean overrun = false;

        int wordStart = start;
        int wordLength = 0;
        int wordWidth = 0;
            // With CID fonts, space isn't neccecary currentFontState.width(32)
        int whitespaceWidth =
            currentFontState.width(currentFontState.mapChar(' '));
        
        char[] data = new char[odata.length];
        for (int count = 0; count < odata.length; count++) {
            data[count] = odata[count];
        }

        /* iterate over each character */
        for (int i = start; i < end; i++) {
            int charWidth;
            /* get the character */
            char c = data[i];
            if (!((c == ' ') || (c == '\n') || (c == '\r') ||
                  (c == '\t'))) {
               c = data[i] = currentFontState.mapChar(c);
               charWidth = currentFontState.width(c);
               if (charWidth <= 0)
                  charWidth = whitespaceWidth;
            } else {
               charWidth = whitespaceWidth;
            }

            if ((c == ' ') || (c == '\n') || (c == '\r') ||
                    (c == '\t')) { // whitespace

                if (prev == WHITESPACE) {

                    // if current & previous are WHITESPACE

                    if (this.whiteSpaceCollapse ==
                            WhiteSpaceCollapse.FALSE) {
                        if (c == ' ') {
                            spaceWidth += whitespaceWidth;
                        } else if (c == '\n') {
                            // force line break
                            return i;
                        } else if (c == '\t') {
                            spaceWidth += 8 * whitespaceWidth;
                        }
                    } // else ignore it

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
                        Box box = (Box) e.nextElement();
                        if (box instanceof InlineArea) {
                            if (ls != null) {
                                Rectangle lr = new Rectangle(finalWidth, 0,
                                                             ((InlineArea) box).
                                                             getContentWidth(),
                                                             fontState.getFontSize());
                                ls.addRect(lr, this);
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
                        WordArea ia = new WordArea(currentFontState,
                                                       this.red, this.green, this.blue,
                                                       new String(data, wordStart,
                                                                  wordLength), wordWidth);
                        ia.setYOffset(placementOffset);
                        ia.setUnderlined(textState.getUnderlined());
                        prevUlState = textState.getUnderlined();
                        ia.setOverlined(textState.getOverlined());
                        prevOlState = textState.getOverlined();
                        ia.setLineThrough(textState.getLineThrough());
                        prevLTState = textState.getLineThrough();
                        ia.setVerticalAlign(vAlign);

                        addChild(ia);
                        if (ls != null) {
                            Rectangle lr = new Rectangle(finalWidth, 0,
                                                         ia.getContentWidth(),
                                                         fontState.getFontSize());
                            ls.addRect(lr, this);
                        }
                        finalWidth += wordWidth;

                        // reset word width
                        wordWidth = 0;
                    }

                    // deal with this new whitespace following the
                    // word we just added

                    prev = WHITESPACE;

                    embeddedLinkStart = 0; //reset embeddedLinkStart since a space was encountered

                    spaceWidth = whitespaceWidth;

                    /*
                    here is the place for space-treatment value 'ignore':
                    if (this.spaceTreatment ==
                            SpaceTreatment.IGNORE) {
                        // do nothing
                } else {
                            spaceWidth = currentFontState.width(32);
                }

                    */


                    if (this.whiteSpaceCollapse ==
                            WhiteSpaceCollapse.FALSE) {
                        if (c == '\n') {
                            // force a line break
                            return i;
                        } else if (c == '\t') {
                            spaceWidth = whitespaceWidth;
                        }
                    }

                } else {

                    // if current is WHITESPACE and no previous

                    if (this.whiteSpaceCollapse ==
                            WhiteSpaceCollapse.FALSE) {
                        prev = WHITESPACE;
                        spaceWidth = whitespaceWidth;
                    } else {
                        // skip over it
                        start++;
                    }
                }

            } else { // current is TEXT

                if (prev == WHITESPACE) {

                    // if current is TEXT and previous WHITESPACE

                    wordWidth = charWidth;
                    if ((finalWidth + spaceWidth + wordWidth) >
                            this.getContentWidth()) {
                        if (overrun)
                            MessageHandler.error(">");
                        if (this.wrapOption == WrapOption.WRAP)
                            return i;
                    }
                    prev = TEXT;
                    wordStart = i;
                    wordLength = 1;
                } else if (prev == TEXT) {

                    wordLength++;
                    wordWidth += charWidth;
                } else { // nothing previous

                    prev = TEXT;
                    wordStart = i;
                    wordLength = 1;
                    wordWidth = charWidth;
                }

                if ((finalWidth + spaceWidth + pendingWidth +
                        wordWidth) > this.getContentWidth()) {

                    // BREAK MID WORD
                    if (wordStart == start) { // if couldn't even fit
                        // first word
                        overrun = true;
                        // if not at start of line, return word start
                        // to try again on a new line
                        if (finalWidth > 0) {
                            return wordStart;
                        }
                    } else if (this.wrapOption == WrapOption.WRAP) {
                      if (this.hyphenate == Hyphenate.TRUE) {
                        return this.doHyphenation(data,i,wordStart,this.getContentWidth() - (finalWidth + spaceWidth + pendingWidth));
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

            WordArea pia = new WordArea(currentFontState, this.red,
                                            this.green, this.blue,
                                            new String(data, wordStart, wordLength), wordWidth);

            pia.setYOffset(placementOffset);
            pia.setUnderlined(textState.getUnderlined());
            prevUlState = textState.getUnderlined();
            pia.setOverlined(textState.getOverlined());
            prevOlState = textState.getOverlined();
            pia.setLineThrough(textState.getLineThrough());
            prevLTState = textState.getLineThrough();
            pia.setVerticalAlign(vAlign);

            if (ls != null) {
                Rectangle lr = new Rectangle(finalWidth + spaceWidth +
                                             embeddedLinkStart, spaceWidth,
                                             pia.getContentWidth(), fontState.getFontSize());
                ls.addRect(lr, this);
            }

            embeddedLinkStart += wordWidth;
            pendingAreas.addElement(pia);
            pendingWidth += wordWidth;
            wordWidth = 0;
        }

        if (overrun)
            MessageHandler.error(">");
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
                          int ruleStyle, int ruleThickness, int leaderPatternWidth,
                          int leaderAlignment) {
        WordArea leaderPatternArea;
        int leaderLength = 0;
        char dotIndex = currentFontState.mapChar('.');
        int dotWidth = currentFontState.width(dotIndex);
        char whitespaceIndex = currentFontState.mapChar(' ');
        int whitespaceWidth = currentFontState.width(whitespaceIndex);
        
        int remainingWidth =
          this.getContentWidth() - this.getCurrentXPosition();
        /** checks whether leaderLenghtOptimum fits into rest of line;
         *  should never overflow, as it has been checked already in BlockArea
         *  first check: use remaining width if it smaller than optimum oder maximum
         * */
        if ((remainingWidth <= leaderLengthOptimum) ||
            (remainingWidth <= leaderLengthMaximum)) {
            leaderLength = remainingWidth;
        } else if ((remainingWidth > leaderLengthOptimum) &&
                   ( remainingWidth > leaderLengthMaximum)) {
            leaderLength = leaderLengthMaximum;
        } else if ((leaderLengthOptimum > leaderLengthMaximum) &&
                   (leaderLengthOptimum < remainingWidth)) {
            leaderLength = leaderLengthOptimum;
        }

		//stop if leader-length is too small
		if (leaderLength <= 0 ) {
			return;
		}

        switch (leaderPattern) {
            case LeaderPattern.SPACE:
                //whitespace setting must be false for this
                int whiteSpaceSetting = this.whiteSpaceCollapse;
                this.changeWhiteSpaceCollapse(WhiteSpaceCollapse.FALSE);
                pendingAreas.addElement(
                  this.buildSimpleLeader(whitespaceIndex,
                                         leaderLength));
                this.changeWhiteSpaceCollapse(whiteSpaceSetting);
                break;
            case LeaderPattern.RULE:
                LeaderArea leaderArea =
                  new LeaderArea(fontState, red, green, blue, "",
                                 leaderLength, leaderPattern, ruleThickness,
                                 ruleStyle);
                leaderArea.setYOffset(placementOffset);
                pendingAreas.addElement(leaderArea);
                break;
            case LeaderPattern.DOTS:
                //if the width of a dot is larger than leader-pattern-width
                //ignore this property
               if (leaderPatternWidth < dotWidth) {
                    leaderPatternWidth = 0;
                }
                //if value of leader-pattern-width is 'use-font-metrics' (0)
                if (leaderPatternWidth == 0) {
                    pendingAreas.addElement(
                      this.buildSimpleLeader(dotIndex,
                                             leaderLength));
                } else {
                    //if leader-alignment is used, calculate space to insert before leader
                    //so that all dots will be parallel.
                    if (leaderAlignment == LeaderAlignment.REFERENCE_AREA) {
                        int spaceBeforeLeader = this.getLeaderAlignIndent(
                                                  leaderLength, leaderPatternWidth);
                        //appending indent space leader-alignment
                        //setting InlineSpace to false, so it is not used in line justification
                        if (spaceBeforeLeader != 0) {
                            pendingAreas.addElement(
                              new InlineSpace(spaceBeforeLeader,
                                              false));
                            pendingWidth += spaceBeforeLeader;
                            //shorten leaderLength, otherwise - in case of
                            //leaderLength=remaining length - it will cut off the end of
                            //leaderlength
                            leaderLength -= spaceBeforeLeader;
                        }
                    }

                    // calculate the space to insert between the dots and create a
                    //inline area with this width
                    InlineSpace spaceBetweenDots =
                       new InlineSpace(leaderPatternWidth - dotWidth, false);

                    leaderPatternArea =
                      new WordArea(currentFontState, this.red,
                                     this.green, this.blue, new String ("."),
                                     dotWidth);
                    leaderPatternArea.setYOffset(placementOffset);
                    int dotsFactor = (int) Math.floor (
                                       ((double) leaderLength) /
                                       ((double) leaderPatternWidth));

                    //add combination of dot + space to fill leader
                    //is there a way to do this in a more effective way?
                    for (int i = 0; i < dotsFactor; i++) {
                        pendingAreas.addElement(leaderPatternArea);
                        pendingAreas.addElement(spaceBetweenDots);
                    }
                    //append at the end some space to fill up to leader length
                    pendingAreas.addElement( new InlineSpace(leaderLength -
                                             dotsFactor * leaderPatternWidth));
                }
                break;
                //leader pattern use-content not implemented.
            case LeaderPattern.USECONTENT:
                MessageHandler.errorln(
                  "leader-pattern=\"use-content\" not " + "supported by this version of Fop");
                return;
        }
        //adds leader length to length of pending inline areas
        pendingWidth += leaderLength;
        //sets prev to TEXT and makes so sure, that also blocks only
        //containing leaders are processed
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
            Box box = (Box) e.nextElement();
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
            case TextAlign.START: // left
                padding = this.getContentWidth() - finalWidth;
                endIndent += padding;
                break;
            case TextAlign.END: // right
                padding = this.getContentWidth() - finalWidth;
                startIndent += padding;
                break;
            case TextAlign.CENTER: // center
                padding = (this.getContentWidth() - finalWidth) / 2;
                startIndent += padding;
                endIndent += padding;
                break;
            case TextAlign.JUSTIFY: // justify
                Vector spaceList = new Vector();

                int spaceCount = 0;
                Enumeration e = children.elements();
                while (e.hasMoreElements()) {
                    Box b = (Box) e.nextElement();
                    if (b instanceof InlineSpace) {
                        InlineSpace space = (InlineSpace) b;
                        if (space.getResizeable()) {
                            spaceList.addElement(space);
                            spaceCount++;
                        }
                    }
                }
                if (spaceCount > 0) {
                    padding = (this.getContentWidth() - finalWidth) /
                              spaceCount;
                } else { // no spaces
                    padding = 0;
                }
                Enumeration f = spaceList.elements();
                while (f.hasMoreElements()) {
                    InlineSpace space2 = (InlineSpace) f.nextElement();
                    int i = space2.getSize();
                    space2.setSize(i + padding);
                }
        }
    }

    /**
     * Balance (vertically) the inline areas within this line.
     */
    public void verticalAlign()
    {
        int superHeight = -this.placementOffset;
        int maxHeight = this.allocationHeight;
        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            Box b = (Box) e.nextElement();
            if(b instanceof InlineArea) {
                InlineArea ia = (InlineArea)b;
                if(ia instanceof WordArea) {
                    ia.setYOffset(placementOffset);
                }
                if(ia.getHeight() > maxHeight) {
                    maxHeight = ia.getHeight();
                }
                int vert = ia.getVerticalAlign();
                if(vert == VerticalAlign.SUPER) {
                    int fh = fontState.getAscender();
                    ia.setYOffset((int)(placementOffset - (fh / 3.0)));
                } else if(vert == VerticalAlign.SUB) {
                    int fh = fontState.getAscender();
                    ia.setYOffset((int)(placementOffset + (fh / 3.0)));
                }
            } else {
            }
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
//        return (prev == NOTHING);
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
    public void changeHyphenation(String language, String country,
                                  int hyphenate, char hyphenationChar,
                                  int hyphenationPushCharacterCount,
                                  int hyphenationRemainCharacterCount) {
        this.language = language;
        this.country = country;
        this.hyphenate = hyphenate;
        this.hyphenationChar = hyphenationChar;
        this.hyphenationPushCharacterCount = hyphenationPushCharacterCount;
        this.hyphenationRemainCharacterCount =
          hyphenationRemainCharacterCount;

    }


    /**
       * creates a leader as String out of the given char and the leader length
       * and wraps it in an InlineArea which is returned
       */
    private InlineArea buildSimpleLeader(int charNumber, int leaderLength) {
		int width = this.currentFontState.width(charNumber);
		if (width == 0)	{
			char c = (char) charNumber;
			MessageHandler.errorln("char " + c + " has width 0. Using width 100 instead.");
			width = 100;
		}
        int factor = (int) Math.floor (leaderLength /
                                       width);
        char [] leaderChars = new char [factor];
        char fillChar = (char) charNumber;
        for (int i = 0; i < factor; i ++) {
            leaderChars[i] = fillChar;
        }
        WordArea leaderPatternArea =
          new WordArea(currentFontState, this.red, this.green,
                         this.blue, new String (leaderChars), leaderLength);
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
    private int getLeaderAlignIndent (int leaderLength,
                                      int leaderPatternWidth) {
        //calculate position of used space in line area
        double position = getCurrentXPosition();
        //calculate factor of next leader pattern cycle
        double nextRepeatedLeaderPatternCycle =
          Math.ceil(position / leaderPatternWidth);
        //calculate difference between start of next leader
        //pattern cycle and already used space
        double difference = (leaderPatternWidth *
                             nextRepeatedLeaderPatternCycle) - position;
        return (int) difference;
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
    private String  getHyphenationWord (char [] characters, int wordStart) {
        boolean wordendFound = false;
        int counter = 0;
        char [] newWord = new char [100];  //create a buffer
        while ((!wordendFound) && ((wordStart + counter) < characters.length)) {
          char tk = characters[wordStart+counter];
          if (Character.isLetter(tk)) {
            newWord[counter] = tk;
            counter++;
          } else {
            wordendFound = true;
          }
        }
        return new String (newWord,0,counter);
    }


    /** extracts word for hyphenation and calls hyphenation package, 
     *  handles cases of inword punctuation and quotation marks at the beginning
     *  of words, but not in a internationalized way 
     */
    public int doHyphenation (char [] characters, int position, int wordStart, int remainingWidth) {
        //check whether the language property has been set
        if (this.language.equalsIgnoreCase("none")) {
          MessageHandler.errorln("if property 'hyphenate' is used, a language must be specified");
          return wordStart;
        }

        /** remaining part string of hyphenation */
        StringBuffer remainingString = new StringBuffer();

        /** for words with some inword punctuation like / or - */
        StringBuffer preString = null;

        /**  char before the word, probably whitespace  */
        char startChar = ' ' ;//characters[wordStart-1];

        /** in word punctuation character */
        char inwordPunctuation;

        /** the complete word handed to the hyphenator */
        String wordToHyphenate;

        //width of hyphenation character
        int hyphCharWidth = this.currentFontState.width(currentFontState.mapChar(this.hyphenationChar));
        remainingWidth -= hyphCharWidth;

        //handles ' or " at the beginning of the word
        if (characters[wordStart] == '"' || characters[wordStart] == '\'' ) {
            remainingString.append(characters[wordStart]);
            //extracts whole word from string
            wordToHyphenate = getHyphenationWord(characters,wordStart+1);
        } else {
            wordToHyphenate = getHyphenationWord(characters,wordStart);
        }

        //if the extracted word is smaller than the remaining width
        //we have a non letter character inside the word. at the moment
        //we will only handle hard hyphens and slashes
        if (getWordWidth(wordToHyphenate)< remainingWidth) {
            inwordPunctuation = characters[wordStart+wordToHyphenate.length()];
            if (inwordPunctuation == '-' ||
                inwordPunctuation == '/' ) {
                preString = new StringBuffer(wordToHyphenate);
                preString = preString.append(inwordPunctuation);
                wordToHyphenate = getHyphenationWord(characters,wordStart+wordToHyphenate.length()+1);
                remainingWidth -= (getWordWidth(wordToHyphenate) +
                                   this.currentFontState.width(currentFontState.mapChar(inwordPunctuation)));
            }
        }

        //are there any hyphenation points
        Hyphenation hyph = Hyphenator.hyphenate(language,country,wordToHyphenate,hyphenationRemainCharacterCount,hyphenationPushCharacterCount);
        //no hyphenation points and no inword non letter character
        if (hyph == null && preString == null) {
            if (remainingString.length() > 0) {
                return wordStart-1;
            } else {
                return wordStart;
            }

        //no hyphenation points, but a inword non-letter character
        } else if (hyph == null && preString != null){
            remainingString.append(preString);
            this.addWord(startChar,remainingString);
            return wordStart + remainingString.length();
        //hyphenation points and no inword non-letter character
        } else if (hyph != null && preString == null)  {
            int index = getFinalHyphenationPoint(hyph,remainingWidth);
            if (index != -1) {
                remainingString.append(hyph.getPreHyphenText(index));
                remainingString.append(this.hyphenationChar);
                this.addWord(startChar,remainingString);
                return wordStart + remainingString.length()-1;
            }
        //hyphenation points and a inword non letter character
        } else if (hyph != null && preString != null) {
            int index = getFinalHyphenationPoint(hyph,remainingWidth);
            if (index != -1) {
              remainingString.append(preString.append(hyph.getPreHyphenText(index)));
              remainingString.append(this.hyphenationChar);
              this.addWord(startChar,remainingString);
              return wordStart + remainingString.length()-1;
            } else {
              remainingString.append(preString) ;
              this.addWord(startChar,remainingString);
              return wordStart + remainingString.length();
            }
        }
        return wordStart;
    }


    /** calculates the wordWidth using the actual fontstate*/
    private int getWordWidth (String word) {
      int wordLength = word.length();
      int width = 0;
      char [] characters = new char [wordLength];
      word.getChars(0,wordLength,characters,0);
      for (int i = 0; i < wordLength; i++) {
        width += this.currentFontState.width(currentFontState.mapChar(characters[i]));
      }
      return width;
    }

    public int getRemainingWidth()
    {
        return this.getContentWidth() - this.getCurrentXPosition();
    }

    public void setLinkSet(LinkSet ls)
    {
    }

    public void addInlineArea(Area box)
    {
        addPending();
        addChild(box);
        prev = TEXT;
        finalWidth += box.getContentWidth();
    }

    public void addInlineSpace(InlineSpace is, int spaceWidth)
    {
        addChild(is);
        finalWidth += spaceWidth;
//        spaceWidth = 0;
    }

    /** adds a single character to the line area tree*/ 
    public int addCharacter (char data, LinkSet ls, boolean ul) {
        WordArea ia = null;
        int remainingWidth =
          this.getContentWidth() - this.getCurrentXPosition();
        int width = this.currentFontState.width(currentFontState.mapChar(data));
        //if it doesn't fit, return
        if (width > remainingWidth) {
          return org.apache.fop.fo.flow.Character.DOESNOT_FIT;
        } else {
          //if whitespace-collapse == true, discard character
          if (Character.isSpaceChar(data) && whiteSpaceCollapse == WhiteSpaceCollapse.TRUE) {
            return org.apache.fop.fo.flow.Character.OK;
          }
          //create new InlineArea
          ia = new WordArea(currentFontState,
                                         this.red, this.green, this.blue,
                                         new Character(data).toString(),width);
          ia.setYOffset(placementOffset);
          ia.setUnderlined(ul);
          pendingAreas.addElement(ia);
          if (Character.isSpaceChar(data)) {
            this.spaceWidth =+ width;
            prev = LineArea.WHITESPACE;
          } else {
            pendingWidth += width;
            prev = LineArea.TEXT;
          }
          return org.apache.fop.fo.flow.Character.OK;
        }
    }


    /** adds a InlineArea containing the String startChar+wordBuf to the line area children.  */
    private void addWord (char startChar, StringBuffer wordBuf) {
        String word = wordBuf.toString();
        WordArea hia;
        int startCharWidth = this.currentFontState.width(currentFontState.mapChar(startChar));
        if (startChar == ' ') {
            this.addChild(new InlineSpace(startCharWidth));
        } else {
            hia = new WordArea(currentFontState,
                                 this.red, this.green, this.blue,
                                 new Character(startChar).toString(),1);
            hia.setYOffset(placementOffset);
            this.addChild(hia);
        }
        int wordWidth = this.getWordWidth(word);
        hia = new WordArea(currentFontState,
                                 this.red, this.green, this.blue,
                                 word,word.length());
        hia.setYOffset(placementOffset);
        this.addChild(hia);

        //calculate the space needed
        finalWidth += startCharWidth + wordWidth ;
    }


    /** extracts from a hyphenated word the best (most greedy) fit */ 
    private int getFinalHyphenationPoint(Hyphenation hyph, int remainingWidth) {
        int [] hyphenationPoints = hyph.getHyphenationPoints();
        int numberOfHyphenationPoints = hyphenationPoints.length;

        int index = -1;
        String wordBegin = "";
        int wordBeginWidth = 0;

        for (int i = 0;i <  numberOfHyphenationPoints; i++){
            wordBegin = hyph.getPreHyphenText(i);
            if (this.getWordWidth(wordBegin) > remainingWidth){
                break;
            }
            index = i;
        }
        return index;
    }

}
