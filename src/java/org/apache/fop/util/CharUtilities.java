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

package org.apache.fop.util;

/**
 * This class provides utilities to distinguish various kinds of Unicode
 * whitespace and to get character widths in a given FontState.
 */
public class CharUtilities {

    /**
     * Character code used to signal a character boundary in
     * inline content, such as an inline with borders and padding
     * or a nested block object.
     */
    public static final char CODE_EOT = 0;

    /**
     * Character class: Unicode white space
     */
    public static final int UCWHITESPACE = 0;
    /**
     * Character class: Line feed
     */
    public static final int LINEFEED = 1;
    /**
     * Character class: Boundary between text runs
     */
    public static final int EOT = 2;
    /**
     * Character class: non-whitespace
     */
    public static final int NONWHITESPACE = 3;
    /**
     * Character class: XML whitespace
     */
    public static final int XMLWHITESPACE = 4;


    /**
     * Utility class: Constructor prevents instantiating when subclassed.
     */
    protected CharUtilities() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return the appropriate CharClass constant for the type
     * of the passed character.
     * @param c character to inspect
     * @return the determined character class
     */
    public static int classOf(char c) {
        if (c == CODE_EOT) { return EOT; }
        if (c == '\n') { return LINEFEED; }
        if (c == ' ' || c == '\r' || c == '\t') { return XMLWHITESPACE; }
        if (isAnySpace(c)) { return UCWHITESPACE; }
        return NONWHITESPACE;
    }


    /**
     * Helper method to determine if the character is a
     * space with normal behavior. Normal behavior means that
     * it's not non-breaking.
     * @param c character to inspect
     * @return True if the character is a normal space
     */
    public static boolean isBreakableSpace(char c) {
        return (c == ' '
               || (c >= '\u2000' && c <= '\u200B'));
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
     * @param c character to check
     * @return True if the character is a nbsp
     */
    public static boolean isNonBreakableSpace(char c) {
        return
            (c == '\u00A0'      // no-break space
            || c == '\u202F'    // narrow no-break space
            || c == '\u3000'    // ideographic space
            || c == '\uFEFF');  // zero width no-break space
    }

    /**
     * Determines if the character represents any kind of space.
     * @param c character to check
     * @return True if the character represents any kind of space
     */
    public static boolean isAnySpace(char c) {
        boolean ret = (isBreakableSpace(c) || isNonBreakableSpace(c));
        return ret;
    }
}

