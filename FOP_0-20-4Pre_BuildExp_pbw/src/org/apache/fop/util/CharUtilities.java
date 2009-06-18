/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.util;

import org.apache.fop.layout.FontState;

/**
 * This class provides utilities to distinguish various kinds of Unicode
 * whitespace and to get character widths in a given FontState.
 */
public class CharUtilities {

    /** Character code used to signal a character boundary in
     * inline content, such as an inline with borders and padding
     * or a nested block object.
     */
    public static final char CODE_EOT=0;

    public static final int UCWHITESPACE=0; // unicode white space
    public static final int LINEFEED=1;
    public static final int EOT=2; // Boundary beteween text runs
    public static final int NONWHITESPACE=3;
    public static final int XMLWHITESPACE=4;


    /**
     * Return the appropriate CharClass constant for the type
     * of the passed character.
     */
    public static int classOf(char c) {
	if (c == CODE_EOT) return EOT;
	if (c == '\n') return LINEFEED;
	if ( c==' '|| c == '\r' || c=='\t' ) return XMLWHITESPACE;
	if (isAnySpace(c)) return UCWHITESPACE;
	return NONWHITESPACE;
    }

    /**
     * Helper method for getting the width of a unicode char
     * from the current fontstate.
     * This also performs some guessing on widths on various
     * versions of space that might not exists in the font.
     */
    public static int getCharWidth(char c, FontState fs) {
        int width = fs.width(fs.mapChar(c));
        if (width <= 0) {
            // Estimate the width of spaces not represented in
            // the font
            int em = fs.width(fs.mapChar('m'));
            int en = fs.width(fs.mapChar('n'));
            if (em <= 0)
                em = 500 * fs.getFontSize();
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
                width = fs.getFontSize();
            if (c == '\u2004')
                width = em / 3;
            if (c == '\u2005')
                width = em / 4;
            if (c == '\u2006')
                width = em / 6;
            if (c == '\u2007')
                width = getCharWidth(' ', fs);
            if (c == '\u2008')
                width = getCharWidth('.', fs);
            if (c == '\u2009')
                width = em / 5;
            if (c == '\u200A')
                width = 5;
            if (c == '\u200B')
                width = 100;
            if (c == '\u00A0')
                width = getCharWidth(' ', fs);
            if (c == '\u202F')
                width = getCharWidth(' ', fs) / 2;
            if (c == '\u3000')
                width = getCharWidth(' ', fs) * 2;
            if ((c == '\n') || (c == '\r') || (c == '\t'))
                width = getCharWidth(' ', fs);
        }

        return width;
    }

    /**
     * Helper method to determine if the character is a
     * space with normal behaviour. Normal behaviour means that
     * it's not non-breaking
     */
    public static boolean isSpace(char c) {
        return (c == ' ' ||
		(c >= '\u2000' && c <= '\u200B'));
//         c == '\u2000'                   // en quad
//         c == '\u2001'                   // em quad
//         c == '\u2002'                   // en space
//         c == '\u2003'                   // em space
//         c == '\u2004'                   // three-per-em space
//         c == '\u2005'                   // four--per-em space
//         c == '\u2006'                   // six-per-em space
//         c == '\u2007'                   // figure space
//         c == '\u2008'                   // punctuation space
//         c == '\u2009'                   // thin space
//         c == '\u200A'                   // hair space
//         c == '\u200B'                   // zero width space
    }

    /**
     * Method to determine if the character is a nonbreaking
     * space.
     */
    public static boolean isNBSP(char c) {
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
    public static boolean isAnySpace(char c) {
        boolean ret = (isSpace(c) || isNBSP(c));
        return ret;
    }
}

