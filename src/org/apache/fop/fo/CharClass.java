/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.fo;


/**
 * A character class allowing to distinguish whitespace, LF, other text.
 */
public class CharClass {

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
     * Helper method to determine if the character is a
     * space with normal behaviour. Normal behaviour means that
     * it's not non-breaking
     */
    private static boolean isSpace(char c) {
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
    private static boolean isNBSP(char c) {
        return (c == '\u00A0' ||   // normal no-break space
		c == '\u202F' ||   // narrow no-break space
		c == '\u3000' ||   // ideographic space
		c == '\uFEFF') ;   // zero width no-break space
    }

    /**
     * @return true if the character represents any kind of space
     */
    private static boolean isAnySpace(char c) {
        return (isSpace(c) || isNBSP(c));
    }

}

