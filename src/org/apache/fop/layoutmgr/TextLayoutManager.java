/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOText; // For TextInfo: TODO: make independent!
import org.apache.fop.area.Area;
import org.apache.fop.area.Property;
import org.apache.fop.area.inline.Word;
import org.apache.fop.util.CharUtilities;

import org.apache.fop.fo.properties.*;

import java.util.ListIterator;

/**
 * LayoutManager for text (a sequence of characters) which generates one
 * or more inline areas.
 */
public class TextLayoutManager extends LeafNodeLayoutManager {

    private char[] chars;
    private FOText.TextInfo textInfo;

    /* values that prev (below) may take */
    protected static final int NOTHING = 0;
    protected static final int WHITESPACE = 1;
    protected static final int TEXT = 2;

    public TextLayoutManager(FObj fobj, char[] chars,
                             FOText.TextInfo textInfo) {
        super(fobj);
        this.chars = chars;
        this.textInfo = textInfo;
    }

    /**
     * Generate inline areas for words in text.
     */
    public void generateAreas() {
        // Handle white-space characteristics. Maybe there is no area to
        // generate....

        // Iterate over characters and make text areas.
        // Add each one to parent. Handle word-space.
//        Word curWordArea = new Word();
//        curWordArea.setWord(new String(chars));
//System.out.println("word:" + new String(chars));
        //parentLM.addChild(curWordArea);
parseChars();

        //setCurrentArea(curWordArea);
        //flush();
    }

    protected void parseChars() {

        // With CID fonts, space isn't neccesary currentFontState.width(32)
        int whitespaceWidth = CharUtilities.getCharWidth(' ', textInfo.fs);

        int wordStart = 0;
        int wordLength = 0;
        int wordWidth = 0; 
        int spaceWidth = 0;

        int prev = NOTHING;

        boolean isText = false;

        /* iterate over each character */ 
        for (int i = 0; i < chars.length; i++) {
            int charWidth;
            /* get the character */
            char c = chars[i];
            if (!(CharUtilities.isSpace(c) || (c == '\n') || (c == '\r') || (c == '\t')
                    || (c == '\u2028'))) {
                charWidth = CharUtilities.getCharWidth(c, textInfo.fs);
                isText = true; 
                prev = TEXT;
wordLength++;
wordWidth += charWidth;
                // Add support for zero-width spaces
                if (charWidth <= 0 && c != '\u200B' && c != '\uFEFF')
                    charWidth = whitespaceWidth;
            } else {
                if ((c == '\n') || (c == '\r') || (c == '\t'))
                    charWidth = whitespaceWidth;
                else
                    charWidth = CharUtilities.getCharWidth(c, textInfo.fs);

                isText = false;

                if (prev == WHITESPACE) {

                    // if current & previous are WHITESPACE

                    if (textInfo.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE) {
                        if (CharUtilities.isSpace(c)) { 
                            spaceWidth += CharUtilities.getCharWidth(c, textInfo.fs);
                        } else if (c == '\n' || c == '\u2028') {
                            // force line break 
                            if (spaceWidth > 0) {
                                /*InlineSpace is = new InlineSpace(spaceWidth);
                                addChild(is);*/
                                spaceWidth = 0;
                            }
                        } else if (c == '\t') {
                            spaceWidth += 8 * whitespaceWidth;
                        }
                    } else if (c == '\u2028') {
                        // Line separator
                        // Breaks line even if WhiteSpaceCollapse = True
                        if (spaceWidth > 0) {
                            /*InlineSpace is = new InlineSpace(spaceWidth);
                            is.setUnderlined(textState.getUnderlined());
                            is.setOverlined(textState.getOverlined());
                            is.setLineThrough(textState.getLineThrough());
                            addChild(is);*/
                            spaceWidth = 0;
                        }
                    }

                } else if (prev == TEXT) {

                    // if current is WHITESPACE and previous TEXT
                    // the current word made it, so
                    // add the space before the current word (if there
                    // was some)

                    if (spaceWidth > 0) {
                        /*InlineSpace is = new InlineSpace(spaceWidth);
                        if (prevUlState) {
                            is.setUnderlined(textState.getUnderlined());
                        }
                        if (prevOlState) {
                            is.setOverlined(textState.getOverlined());
                        }
                        if (prevLTState) {
                            is.setLineThrough(textState.getLineThrough());
                        }
                        addChild(is);*/
                        spaceWidth = 0;
                    }

                    // add the current word

                    if (wordLength > 0) {
                        // The word might contain nonbreaking
                        // spaces. Split the word and add InlineSpace
                        // as necessary. All spaces inside the word
                        // Have a fixed width.
        Word curWordArea = new Word(); 
curWordArea.setWidth(wordWidth);
        curWordArea.setWord(new String(chars, wordStart, wordLength + 1));
Property prop = new Property();
prop.propType = Property.FONT_STATE;
prop.data = textInfo.fs;
curWordArea.addProperty(prop);
        parentLM.addChild(curWordArea);

                        // reset word width
                        wordWidth = 0;
                    }

                    // deal with this new whitespace following the
                    // word we just added
                    prev = WHITESPACE;

                    spaceWidth = CharUtilities.getCharWidth(c, textInfo.fs);

                    if (textInfo.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE) {
                        if (c == '\n' || c == '\u2028') {
                            // force a line break
                        } else if (c == '\t') {
                            spaceWidth = whitespaceWidth;
                        }
                    } else if (c == '\u2028') {
                    }
                } else {

                    // if current is WHITESPACE and no previous

                    if (textInfo.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE) {
                        if (CharUtilities.isSpace(c)) {
                            prev = WHITESPACE;
                            spaceWidth = CharUtilities.getCharWidth(c, textInfo.fs);
                        } else if (c == '\n') {
                            // force line break
                            // textdecoration not used because spaceWidth is 0
                            /*InlineSpace is = new InlineSpace(spaceWidth);
                            addChild(is);*/
                        } else if (c == '\t') {
                            prev = WHITESPACE;
                            spaceWidth = 8 * whitespaceWidth;
                        }

                    } else {
                        // skip over it
                        wordStart++;
                    }
                }
                        wordStart = i;
wordLength = 0;
            }
        } // end of iteration over text

        if (wordLength > 0) {
            // The word might contain nonbreaking
            // spaces. Split the word and add InlineSpace
            // as necessary. All spaces inside the word
            // Have a fixed width.
if(wordStart + wordLength > chars.length - 1) {
wordLength = chars.length - 1 - wordStart;
}

            Word curWordArea = new Word();
curWordArea.setWidth(wordWidth);
            curWordArea.setWord(new String(chars, wordStart, wordLength + 1));
Property prop = new Property();
prop.propType = Property.FONT_STATE;
prop.data = textInfo.fs;
curWordArea.addProperty(prop);
            parentLM.addChild(curWordArea);

        }

        chars = null;
    }

    /** Try to split the word area by hyphenating the word. */
    public boolean splitArea(Area areaToSplit, SplitContext context) {
        context.nextArea = areaToSplit;
        return false;
    }

}

