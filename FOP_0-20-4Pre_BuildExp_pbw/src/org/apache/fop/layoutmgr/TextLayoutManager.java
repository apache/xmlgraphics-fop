/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.TextInfo;
import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Word;
import org.apache.fop.area.inline.Space;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.fo.properties.VerticalAlign;

import org.apache.fop.fo.properties.*;

import java.util.ListIterator;
import java.util.ArrayList;

/**
 * LayoutManager for text (a sequence of characters) which generates one
 * or more inline areas.
 */
public class TextLayoutManager extends LeafNodeLayoutManager {

    private char[] chars;
    private TextInfo textInfo;

    ArrayList words = new ArrayList();

    private static final char NEWLINE = '\n';
    private static final char RETURN = '\r';
    private static final char TAB = '\t';
    private static final char LINEBREAK = '\u2028';
    private static final char ZERO_WIDTH_SPACE = '\u200B';
    // byte order mark
    private static final char ZERO_WIDTH_NOBREAK_SPACE = '\uFEFF';

    /* values that prev (below) may take */
    protected static final int NOTHING = 0;
    protected static final int WHITESPACE = 1;
    protected static final int TEXT = 2;

    public TextLayoutManager(FObj fobj, char[] chars,
                             TextInfo textInfo) {
        super(fobj);
        this.chars = chars;
        this.textInfo = textInfo;
    }

    public int size() {
        parseChars();
        return words.size();
    }

    public InlineArea get(int index) {
        parseChars();
        return (InlineArea)words.get(index);
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

    protected void parseChars() {
        if(chars == null) {
            return;
        }

    int whitespaceWidth;
        // With CID fonts, space isn't neccesary currentFontState.width(32)
        whitespaceWidth = CharUtilities.getCharWidth(' ', textInfo.fs);

        int wordStart = -1;
        int wordLength = 0;
        int wordWidth = 0;
        int spaceWidth = 0;

        int prev = NOTHING;
    int i = 0;

        /* iterate over each character */
        for (; i < chars.length; i++) {
            int charWidth;
            /* get the character */
            char c = chars[i];
            if (!(CharUtilities.isSpace(c) || (c == NEWLINE) ||
                    (c == RETURN) || (c == TAB) || (c == LINEBREAK))) {
                charWidth = CharUtilities.getCharWidth(c, textInfo.fs);
                prev = TEXT;
                wordLength++;
                wordWidth += charWidth;
                // Add support for zero-width spaces
                if (charWidth <= 0 && c != ZERO_WIDTH_SPACE &&
                                 c != ZERO_WIDTH_NOBREAK_SPACE)
                    charWidth = whitespaceWidth;
            } else {
                if ((c == NEWLINE) || (c == RETURN) || (c == TAB))
                    charWidth = whitespaceWidth;
                else
                    charWidth = CharUtilities.getCharWidth(c, textInfo.fs);

                if (prev == WHITESPACE) {

                    // if current & previous are WHITESPACE

                    if (textInfo.whiteSpaceCollapse ==
                            WhiteSpaceCollapse.FALSE) {
                        if (CharUtilities.isSpace(c)) {
                            spaceWidth += CharUtilities.getCharWidth(c,
                                          textInfo.fs);
                        } else if (c == NEWLINE || c == LINEBREAK) {
                            // force line break
                            if (spaceWidth > 0) {
                                Space is = new Space();
                                is.setWidth(spaceWidth);
                                spaceWidth = 0;
                                words.add(is);
                            }
                        } else if (c == TAB) {
                            spaceWidth += 8 * whitespaceWidth;
                        }
                    } else if (c == LINEBREAK) {
                        // Line separator
                        // Breaks line even if WhiteSpaceCollapse = True
                        if (spaceWidth > 0) {
                            Space is = new Space();
                            is.setWidth(spaceWidth);
                            is.info = new LayoutInfo();
                            is.info.breakAfter = true;
                            spaceWidth = 0;
                            words.add(is);
                        }
                    }

                } else if (prev == TEXT) {

                    // if current is WHITESPACE and previous TEXT
                    // the current word made it, so
                    // add the space before the current word (if there
                    // was some)

                    if (spaceWidth > 0) {
                        Space is = new Space();
                        is.setWidth(spaceWidth);
                        spaceWidth = 0;
                        words.add(is);
                    }

                    // add the current word

                    if (wordLength > 0) {
                        // The word might contain nonbreaking
                        // spaces. Split the word and add Space
                        // as necessary. All spaces inside the word
                        // Have a fixed width.
                        words.add(createWord(new String(chars, wordStart + 1,
                                                        wordLength), wordWidth));

                        // reset word width
                        wordWidth = 0;
                    }

                    // deal with this new whitespace following the
                    // word we just added
                    prev = WHITESPACE;

                    spaceWidth = CharUtilities.getCharWidth(c, textInfo.fs);

                    if (textInfo.whiteSpaceCollapse ==
                            WhiteSpaceCollapse.FALSE) {
                        if (c == NEWLINE || c == LINEBREAK) {
                            // force a line break
                        } else if (c == TAB) {
                            spaceWidth = whitespaceWidth;
                        }
                    } else if (c == LINEBREAK) {
                    }
                } else {

                    // if current is WHITESPACE and no previous

                    if (textInfo.whiteSpaceCollapse ==
                            WhiteSpaceCollapse.FALSE) {
                        if (CharUtilities.isSpace(c)) {
                            prev = WHITESPACE;
                            spaceWidth = CharUtilities.getCharWidth(c,
                                                                    textInfo.fs);
                        } else if (c == NEWLINE) {
                            // force line break
                            // textdecoration not used because spaceWidth is 0
                            Space is = new Space();
                            is.setWidth(spaceWidth);
                            words.add(is);
                        } else if (c == TAB) {
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
            // spaces. Split the word and add Space
            // as necessary. All spaces inside the word
            // Have a fixed width.
            if (wordStart + wordLength > chars.length - 1) {
                wordLength = chars.length - 1 - wordStart;
            }

            words.add(createWord(new String(chars, wordStart + 1, wordLength), wordWidth));
        }

        chars = null;
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

    /** Try to split the word area by hyphenating the word. */
    public boolean splitArea(Area areaToSplit, SplitContext context) {
        context.nextArea = areaToSplit;
        return false;
    }

}

