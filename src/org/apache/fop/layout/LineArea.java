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
import org.apache.fop.layout.LeaderArea;
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
    protected int wordWidth = 0;

    /* values that prev (below) may take */
    protected static final int NOTHING = 0;
    protected static final int WHITESPACE = 1;
    protected static final int TEXT = 2;

    /* the character type of the previous character */
    protected int prev = NOTHING;

    /* the position in data[] of the start of the current word */
    protected int wordStart;

    /* the length (in characters) of the current word */
    protected int wordLength = 0;

    /* width of spaces before current word */
    protected int spaceWidth = 0;

    /* the inline areas that have not yet been added to the line
       because subsequent characters to come (in a different addText)
       may be part of the same word */
    protected Vector pendingAreas = new Vector();

    /* the width of the pendingAreas */
    protected int pendingWidth = 0;

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
        int width = currentFontState.width(32) * 3;

        PageNumberInlineArea pia =
          new PageNumberInlineArea(currentFontState, this.red,
                                   this.green, this.blue, refid, width);

        pendingAreas.addElement(pia);
        pendingWidth += width;
        wordWidth = 0;
        prev = TEXT;

        return -1;
    }


    /**
       * adds text to line area
       *
       * @return int character position
       */
    public int addText(char odata[], int start, int end, LinkSet ls,
                       boolean ul) {
        boolean overrun = false;

        wordStart = start;
        wordLength = 0;
        wordWidth = 0;
        char[] data = new char[odata.length];
        for (int count = 0; count < odata.length; count++) {
            data[count] = odata[count];
        }

        /* iterate over each character */
        for (int i = start; i < end; i++) {
            int charWidth;
            /* get the character */
            char c = data[i];

            if (c > 127) {
                /* this class shouldn't be hard coded */
                char d = org.apache.fop.render.pdf.CodePointMapping.map[c];
                if (d != 0) {
                    c = data[i] = d;
                } else {
                    MessageHandler.error("ch" + (int) c + "?");
                    c = data[i] = '#';
                }
            }

            charWidth = currentFontState.width(c);

            if ((c == ' ') || (c == '\n') || (c == '\r') ||
                    (c == '\t')) { // whitespace

                if (prev == WHITESPACE) {

                    // if current & previous are WHITESPACE

                    if (this.whiteSpaceCollapse ==
                            WhiteSpaceCollapse.FALSE) {
                        if (c == ' ') {
                            spaceWidth += currentFontState.width(32);
                        } else if (c == '\n') {
                            // force line break
                            return i;
                        } else if (c == '\t') {
                            spaceWidth += 8 * currentFontState.width(32);
                        }
                    } // else ignore it

                } else if (prev == TEXT) {

                    // if current is WHITESPACE and previous TEXT

                    // the current word made it, so

                    // add the space before the current word (if there
                    // was some)

                    if (spaceWidth > 0) {
                        addChild(new InlineSpace(spaceWidth));
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
                        InlineArea ia = new InlineArea(currentFontState,
                                                       this.red, this.green, this.blue,
                                                       new String(data, wordStart,
                                                                  wordLength), wordWidth);
                        ia.setUnderlined(ul);
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

                    spaceWidth = currentFontState.width(32);

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
                            spaceWidth = currentFontState.width(32);
                        }
                    }

                } else {

                    // if current is WHITESPACE and no previous

                    if (this.whiteSpaceCollapse ==
                            WhiteSpaceCollapse.FALSE) {
                        prev = WHITESPACE;
                        spaceWidth = currentFontState.width(32);
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
                        return this.doHyphenation(data,i,wordStart,this.getContentWidth()-finalWidth-pendingWidth-spaceWidth);
                      } else {
                        return wordStart;
                      }
                    }
                }

            }
        } // end of iteration over text

        if (prev == TEXT) {

            InlineArea pia = new InlineArea(currentFontState, this.red,
                                            this.green, this.blue,
                                            new String(data, wordStart, wordLength), wordWidth);

            pia.setUnderlined(ul);

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
        InlineArea leaderPatternArea;
        int leaderLength;
        int remainingWidth =
          this.getContentWidth() - this.getCurrentXPosition();

        //here is the point to decide which leader-length is to be used, either
        //optimum or maximum. At the moment maximum is used if the remaining
        //width isn't smaller. In this case only the remaining width is used for
        //the leader. Actually this means, optimum is never used at the moment.
        if (remainingWidth < leaderLengthMaximum) {
            leaderLength = remainingWidth;
        } else {
            leaderLength = leaderLengthMaximum;
        }
        switch (leaderPattern) {
            case LeaderPattern.SPACE:
                //whitespace setting must be false for this
                int whiteSpaceSetting = this.whiteSpaceCollapse;
                this.changeWhiteSpaceCollapse(WhiteSpaceCollapse.FALSE);
                pendingAreas.addElement(
                  this.buildSimpleLeader(32, leaderLength));
                this.changeWhiteSpaceCollapse(whiteSpaceSetting);
                break;
            case LeaderPattern.RULE:
                LeaderArea leaderArea =
                  new LeaderArea(fontState, red, green, blue, "",
                                 leaderLength, leaderPattern, ruleThickness,
                                 ruleStyle);
                pendingAreas.addElement(leaderArea);
                break;
            case LeaderPattern.DOTS:
                //if the width of a dot is larger than leader-pattern-width
                //ignore this property
                if (leaderPatternWidth < this.currentFontState.width(46)) {
                    leaderPatternWidth = 0;
                }
                //if value of leader-pattern-width is 'use-font-metrics' (0)
                if (leaderPatternWidth == 0) {
                    pendingAreas.addElement(
                      this.buildSimpleLeader(46, leaderLength));
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
                      new InlineSpace(leaderPatternWidth -
                                      this.currentFontState.width(46), false);
                    leaderPatternArea =
                      new InlineArea(currentFontState, this.red,
                                     this.green, this.blue, new String ("."),
                                     this.currentFontState.width(46));
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
       * normally done,if the line area is filled and
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
        return (prev == 0);
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
        int factor = (int) Math.floor (leaderLength /
                                       this.currentFontState.width(charNumber));
        char [] leaderChars = new char [factor];
        char fillChar = (char) charNumber;
        for (int i = 0; i < factor; i ++) {
            leaderChars[i] = fillChar;
        }
        InlineArea leaderPatternArea =
          new InlineArea(currentFontState, this.red, this.green,
                         this.blue, new String (leaderChars), leaderLength);
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
    private String getHyphenationWord (char [] characters, int wordStart) {
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


    private int doHyphenation (char [] characters, int position, int wordStart, int remainingWidth) {
        int hyphCharWidth = this.currentFontState.width(this.hyphenationChar);
        remainingWidth -= hyphCharWidth;

        String wordToHyphenate = getHyphenationWord(characters,wordStart);
        //check whether the language property has been set
        if (this.language.equalsIgnoreCase("none")) {
          MessageHandler.errorln("if property 'hyphenate' is used, a language must be specified");
          return wordStart;
        }
        //are there any hyphenation points
        Hyphenation hyph = Hyphenator.hyphenate(language,country,wordToHyphenate,hyphenationRemainCharacterCount,hyphenationPushCharacterCount);
        if (hyph != null) {
            int [] hyphenationPoints = hyph.getHyphenationPoints();

            int index = 0;
            String wordBegin = "";
            int wordBeginWidth = 0;

            while (wordBeginWidth < remainingWidth && hyph.length() > index) {
              wordBegin = hyph.getPreHyphenText(index);
              wordBeginWidth = getWordWidth(wordBegin);
              index++;
            }
            if (index > 1) {
              wordBegin = hyph.getPreHyphenText(index-1) + this.hyphenationChar;
              wordBeginWidth = getWordWidth(wordBegin);
              InlineArea hia = new InlineArea(currentFontState,
                                         this.red, this.green, this.blue,
                                         wordBegin,wordBegin.length());
              this.addChild(new InlineSpace(currentFontState.width(32)));
              this.addChild(hia);

              //calculate the space needed
              finalWidth += wordBeginWidth + currentFontState.width(32);
              return wordStart + wordBegin.length()-1;
            }
        }
        return wordStart;
    }

    private int getWordWidth (String word) {
      int wordLength = word.length();
      int width = 0;
      char [] characters = new char [wordLength];
      word.getChars(0,wordLength,characters,0);
      for (int i = 0; i < wordLength; i++) {
        width += this.currentFontState.width(characters[i]);
      }
      return width;
    }
}
