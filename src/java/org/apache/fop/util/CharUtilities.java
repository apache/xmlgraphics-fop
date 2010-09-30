/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// CSOFF: AvoidNestedBlocksCheck
// CSOFF: InnerAssignmentCheck
// CSOFF: SimplifyBooleanReturnCheck

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


    /** null char */
    public static final char NULL_CHAR = '\u0000';
    /** linefeed character */
    public static final char LINEFEED_CHAR = '\n';
    /** carriage return */
    public static final char CARRIAGE_RETURN = '\r';
    /** normal tab */
    public static final char TAB = '\t';
    /** normal space */
    public static final char SPACE = '\u0020';
    /** non-breaking space */
    public static final char NBSPACE = '\u00A0';
    /** next line control character */
    public static final char NEXT_LINE = '\u0085';
    /** zero-width space */
    public static final char ZERO_WIDTH_SPACE = '\u200B';
    /** word joiner */
    public static final char WORD_JOINER = '\u2060';
    /** zero-width joiner */
    public static final char ZERO_WIDTH_JOINER = '\u200D';
    /** left-to-right mark */
    public static final char LRM = '\u200E';
    /** right-to-left mark */
    public static final char RLM = '\u202F';
    /** left-to-right embedding */
    public static final char LRE = '\u202A';
    /** right-to-left embedding */
    public static final char RLE = '\u202B';
    /** pop directional formatting */
    public static final char PDF = '\u202C';
    /** left-to-right override */
    public static final char LRO = '\u202D';
    /** right-to-left override */
    public static final char RLO = '\u202E';
    /** zero-width no-break space (= byte order mark) */
    public static final char ZERO_WIDTH_NOBREAK_SPACE = '\uFEFF';
    /** soft hyphen */
    public static final char SOFT_HYPHEN = '\u00AD';
    /** line-separator */
    public static final char LINE_SEPARATOR = '\u2028';
    /** paragraph-separator */
    public static final char PARAGRAPH_SEPARATOR = '\u2029';
    /** missing ideograph */
    public static final char MISSING_IDEOGRAPH = '\u25A1';
    /** Ideogreaphic space */
    public static final char IDEOGRAPHIC_SPACE = '\u3000';
    /** Unicode value indicating the the character is "not a character". */
    public static final char NOT_A_CHARACTER = '\uFFFF';


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
    public static int classOf ( int c ) {
        switch (c) {
            case CODE_EOT:
                return EOT;
            case LINEFEED_CHAR:
                return LINEFEED;
            case SPACE:
            case CARRIAGE_RETURN:
            case TAB:
                return XMLWHITESPACE;
            default:
                return isAnySpace(c) ? UCWHITESPACE : NONWHITESPACE;
        }
    }


    /**
     * Helper method to determine if the character is a
     * space with normal behavior. Normal behavior means that
     * it's not non-breaking.
     * @param c character to inspect
     * @return True if the character is a normal space
     */
    public static boolean isBreakableSpace ( int c ) {
        return (c == SPACE || isFixedWidthSpace(c));
    }

    /**
     * Method to determine if the character is a zero-width space.
     * @param c the character to check
     * @return true if the character is a zero-width space
     */
    public static boolean isZeroWidthSpace ( int c ) {
        return c == ZERO_WIDTH_SPACE           // 200Bh
            || c == WORD_JOINER                // 2060h
            || c == ZERO_WIDTH_NOBREAK_SPACE;  // FEFFh (also used as BOM)
    }

    /**
     * Method to determine if the character is a (breakable) fixed-width space.
     * @param c the character to check
     * @return true if the character has a fixed-width
     */
    public static boolean isFixedWidthSpace ( int c ) {
        return (c >= '\u2000' && c <= '\u200B')
                || c == '\u3000';
//      c == '\u2000'                   // en quad
//      c == '\u2001'                   // em quad
//      c == '\u2002'                   // en space
//      c == '\u2003'                   // em space
//      c == '\u2004'                   // three-per-em space
//      c == '\u2005'                   // four-per-em space
//      c == '\u2006'                   // six-per-em space
//      c == '\u2007'                   // figure space
//      c == '\u2008'                   // punctuation space
//      c == '\u2009'                   // thin space
//      c == '\u200A'                   // hair space
//      c == '\u200B'                   // zero width space
//      c == '\u3000'                   // ideographic space
    }

    /**
     * Method to determine if the character is a nonbreaking
     * space.
     * @param c character to check
     * @return True if the character is a nbsp
     */
    public static boolean isNonBreakableSpace ( int c ) {
        return
            (c == NBSPACE       // no-break space
            || c == '\u202F'    // narrow no-break space
            || c == '\u3000'    // ideographic space
            || c == WORD_JOINER // word joiner
            || c == ZERO_WIDTH_NOBREAK_SPACE);  // zero width no-break space
    }

    /**
     * Method to determine if the character is an adjustable
     * space.
     * @param c character to check
     * @return True if the character is adjustable
     */
    public static boolean isAdjustableSpace ( int c ) {
        //TODO: are there other kinds of adjustable spaces?
        return
            (c == '\u0020'    // normal space
            || c == NBSPACE); // no-break space
    }

    /**
     * Determines if the character represents any kind of space.
     * @param c character to check
     * @return True if the character represents any kind of space
     */
    public static boolean isAnySpace ( int c ) {
        return (isBreakableSpace(c) || isNonBreakableSpace(c));
    }

    /**
     * Indicates whether a character is classified as "Alphabetic" by the Unicode standard.
     * @param c the character
     * @return true if the character is "Alphabetic"
     */
    public static boolean isAlphabetic ( int c ) {
        //http://www.unicode.org/Public/UNIDATA/UCD.html#Alphabetic
        //Generated from: Other_Alphabetic + Lu + Ll + Lt + Lm + Lo + Nl
        int generalCategory = Character.getType((char)c);
        switch (generalCategory) {
            case Character.UPPERCASE_LETTER: //Lu
            case Character.LOWERCASE_LETTER: //Ll
            case Character.TITLECASE_LETTER: //Lt
            case Character.MODIFIER_LETTER: //Lm
            case Character.OTHER_LETTER: //Lo
            case Character.LETTER_NUMBER: //Nl
                return true;
            default:
                //TODO if (ch in Other_Alphabetic) return true; (Probably need ICU4J for that)
                //Other_Alphabetic contains mostly more exotic characters
                return false;
        }
    }

    /**
     * Indicates whether the given character is an explicit break-character
     * @param c    the character to check
     * @return  true if the character represents an explicit break
     */
    public static boolean isExplicitBreak ( int c ) {
        return (c == LINEFEED_CHAR
            || c == CARRIAGE_RETURN
            || c == NEXT_LINE
            || c == LINE_SEPARATOR
            || c == PARAGRAPH_SEPARATOR);
    }


    /** hebrew script constant */
    public static final int SCRIPT_HEBREW                               = 125;  // 'hebr'
    /** mongolian script constant */
    public static final int SCRIPT_MONGOLIAN                            = 145;  // 'mong'
    /** arabic script constant */
    public static final int SCRIPT_ARABIC                               = 160;  // 'arab'
    /** greek script constant */
    public static final int SCRIPT_GREEK                                = 200;  // 'grek'
    /** latin script constant */
    public static final int SCRIPT_LATIN                                = 215;  // 'latn'
    /** cyrillic script constant */
    public static final int SCRIPT_CYRILLIC                             = 220;  // 'cyrl'
    /** georgian script constant */
    public static final int SCRIPT_GEORGIAN                             = 240;  // 'geor'
    /** bopomofo script constant */
    public static final int SCRIPT_BOPOMOFO                             = 285;  // 'bopo'
    /** hangul script constant */
    public static final int SCRIPT_HANGUL                               = 286;  // 'hang'
    /** gurmukhi script constant */
    public static final int SCRIPT_GURMUKHI                             = 310;  // 'guru'
    /** devanagari script constant */
    public static final int SCRIPT_DEVANAGARI                           = 315;  // 'deva'
    /** gujarati script constant */
    public static final int SCRIPT_GUJARATI                             = 320;  // 'gujr'
    /** bengali script constant */
    public static final int SCRIPT_BENGALI                              = 326;  // 'beng'
    /** oriya script constant */
    public static final int SCRIPT_ORIYA                                = 327;  // 'orya'
    /** tibetan script constant */
    public static final int SCRIPT_TIBETAN                              = 330;  // 'tibt'
    /** telugu script constant */
    public static final int SCRIPT_TELUGU                               = 340;  // 'telu'
    /** tamil script constant */
    public static final int SCRIPT_TAMIL                                = 346;  // 'taml'
    /** malayalam script constant */
    public static final int SCRIPT_MALAYALAM                            = 347;  // 'mlym'
    /** sinhalese script constant */
    public static final int SCRIPT_SINHALESE                            = 348;  // 'sinh'
    /** burmese script constant */
    public static final int SCRIPT_BURMESE                              = 350;  // 'mymr'
    /** thai script constant */
    public static final int SCRIPT_THAI                                 = 352;  // 'thai'
    /** khmer script constant */
    public static final int SCRIPT_KHMER                                = 355;  // 'khmr'
    /** lao script constant */
    public static final int SCRIPT_LAO                                  = 356;  // 'laoo'
    /** hiragana script constant */
    public static final int SCRIPT_HIRAGANA                             = 410;  // 'hira'
    /** ethiopic script constant */
    public static final int SCRIPT_ETHIOPIC                             = 430;  // 'ethi'
    /** han script constant */
    public static final int SCRIPT_HAN                                  = 500;  // 'hani'
    /** katakana script constant */
    public static final int SCRIPT_KATAKANA                             = 410;  // 'kana'
    /** math script constant */
    public static final int SCRIPT_MATH                                 = 995;  // 'zmth'
    /** symbol script constant */
    public static final int SCRIPT_SYMBOL                               = 996;  // 'zsym'
    /** undetermined script constant */
    public static final int SCRIPT_UNDETERMINED                         = 998;  // 'zyyy'
    /** uncoded script constant */
    public static final int SCRIPT_UNCODED                              = 999;  // 'zzzz'

    /**
     * Determine if character c is punctuation.
     * @param c a character represented as a unicode scalar value
     * @return true if character is punctuation
     */
    public static boolean isPunctuation ( int c ) {
        if ( ( c >= 0x0021 ) && ( c <= 0x002F ) ) {             // basic latin punctuation
            return true;
        } else if ( ( c >= 0x003A ) && ( c <= 0x0040 ) ) {      // basic latin punctuation
            return true;
        } else if ( ( c >= 0x005F ) && ( c <= 0x0060 ) ) {      // basic latin punctuation
            return true;
        } else if ( ( c >= 0x007E ) && ( c <= 0x007E ) ) {      // basic latin punctuation
            return true;
        } else if ( ( c >= 0x007E ) && ( c <= 0x007E ) ) {      // basic latin punctuation
            return true;
        } else if ( ( c >= 0x00A1 ) && ( c <= 0x00BF ) ) {      // latin supplement punctuation
            return true;
        } else if ( ( c >= 0x00D7 ) && ( c <= 0x00D7 ) ) {      // latin supplement punctuation
            return true;
        } else if ( ( c >= 0x00F7 ) && ( c <= 0x00F7 ) ) {      // latin supplement punctuation
            return true;
        } else if ( ( c >= 0x2000 ) && ( c <= 0x206F ) ) {      // general punctuation
            return true;
        } else {                                                // [TBD] - not complete
            return false;
        }
    }

    /**
     * Determine if character c is a digit.
     * @param c a character represented as a unicode scalar value
     * @return true if character is a digit
     */
    public static boolean isDigit ( int c ) {
        if ( ( c >= 0x0030 ) && ( c <= 0x0039 ) ) {             // basic latin digits
            return true;
        } else {                                                // [TBD] - not complete
            return false;
        }
    }

    /**
     * Determine if character c belong to the hebrew script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to hebrew script
     */
    public static boolean isHebrew ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the mongolian script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to mongolian script
     */
    public static boolean isMongolian ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the arabic script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to arabic script
     */
    public static boolean isArabic ( int c ) {
        if ( ( c >= 0x0600 ) && ( c <= 0x06FF ) ) {             // arabic block
            return true;
        } else if ( ( c >= 0x0750 ) && ( c <= 0x077F ) ) {      // arabic supplement block
            return true;
        } else if ( ( c >= 0xFB50 ) && ( c <= 0xFDFF ) ) {      // arabic presentation forms a block
            return true;
        } else if ( ( c >= 0xFE70 ) && ( c <= 0xFEFF ) ) {      // arabic presentation forms b block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the greek script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to greek script
     */
    public static boolean isGreek ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the latin script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to latin script
     */
    public static boolean isLatin ( int c ) {
        if ( ( c >= 0x0041 ) && ( c <= 0x005A ) ) {             // basic latin upper case
            return true;
        } else if ( ( c >= 0x0061 ) && ( c <= 0x007A ) ) {      // basic latin lower case
            return true;
        } else if ( ( c >= 0x00C0 ) && ( c <= 0x00D6 ) ) {      // latin supplement upper case
            return true;
        } else if ( ( c >= 0x00D8 ) && ( c <= 0x00DF ) ) {      // latin supplement upper case
            return true;
        } else if ( ( c >= 0x00E0 ) && ( c <= 0x00F6 ) ) {      // latin supplement lower case
            return true;
        } else if ( ( c >= 0x00F8 ) && ( c <= 0x00FF ) ) {      // latin supplement lower case
            return true;
        } else if ( ( c >= 0x0100 ) && ( c <= 0x017F ) ) {      // latin extended a
            return true;
        } else if ( ( c >= 0x0180 ) && ( c <= 0x024F ) ) {      // latin extended b
            return true;
        } else if ( ( c >= 0x1E00 ) && ( c <= 0x1EFF ) ) {      // latin extended additional
            return true;
        } else if ( ( c >= 0x2C60 ) && ( c <= 0x2C7F ) ) {      // latin extended c
            return true;
        } else if ( ( c >= 0xA720 ) && ( c <= 0xA7FF ) ) {      // latin extended d
            return true;
        } else if ( ( c >= 0xFB00 ) && ( c <= 0xFB0F ) ) {      // latin ligatures
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the cyrillic script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to cyrillic script
     */
    public static boolean isCyrillic ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the georgian script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to georgian script
     */
    public static boolean isGeorgian ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the hangul script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to hangul script
     */
    public static boolean isHangul ( int c ) {
        if ( ( c >= 0x1100 ) && ( c <= 0x11FF ) ) {             // hangul jamo
            return true;
        } else if ( ( c >= 0x3130 ) && ( c <= 0x318F ) ) {      // hangul compatibility jamo
            return true;
        } else if ( ( c >= 0xA960 ) && ( c <= 0xA97F ) ) {      // hangul jamo extended a
            return true;
        } else if ( ( c >= 0xAC00 ) && ( c <= 0xD7A3 ) ) {      // hangul syllables
            return true;
        } else if ( ( c >= 0xD7B0 ) && ( c <= 0xD7FF ) ) {      // hangul jamo extended a
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the gurmukhi script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to gurmukhi script
     */
    public static boolean isGurmukhi ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the devanagari script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to devanagari script
     */
    public static boolean isDevanagari ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the gujarati script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to gujarati script
     */
    public static boolean isGujarati ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the bengali script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to bengali script
     */
    public static boolean isBengali ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the oriya script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to oriya script
     */
    public static boolean isOriya ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the tibetan script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to tibetan script
     */
    public static boolean isTibetan ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the telugu script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to telugu script
     */
    public static boolean isTelugu ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the tamil script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to tamil script
     */
    public static boolean isTamil ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the malayalam script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to malayalam script
     */
    public static boolean isMalayalam ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the sinhalese script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to sinhalese script
     */
    public static boolean isSinhalese ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the burmese script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to burmese script
     */
    public static boolean isBurmese ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the thai script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to thai script
     */
    public static boolean isThai ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the khmer script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to khmer script
     */
    public static boolean isKhmer ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the lao script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to lao script
     */
    public static boolean isLao ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the ethiopic (amharic) script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to ethiopic (amharic) script
     */
    public static boolean isEthiopic ( int c ) {
        return false; // [TBD] - implement me
    }

    /**
     * Determine if character c belong to the han (unified cjk) script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to han (unified cjk) script
     */
    public static boolean isHan ( int c ) {
        if ( ( c >= 0x3400 ) && ( c <= 0x4DBF ) ) {             
            return true; // cjk unified ideographs extension a
        } else if ( ( c >= 0x4E00 ) && ( c <= 0x9FFF ) ) {      
            return true; // cjk unified ideographs
        } else if ( ( c >= 0xF900 ) && ( c <= 0xFAFF ) ) {      
            return true; // cjk compatibility ideographs
        } else if ( ( c >= 0x20000 ) && ( c <= 0x2A6DF ) ) {    
            return true; // cjk unified ideographs extension b
        } else if ( ( c >= 0x2A700 ) && ( c <= 0x2B73F ) ) {    
            return true; // cjk unified ideographs extension c
        } else if ( ( c >= 0x2F800 ) && ( c <= 0x2FA1F ) ) {    
            return true; // cjk compatibility ideographs supplement
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the bopomofo script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to bopomofo script
     */
    public static boolean isBopomofo ( int c ) {
        if ( ( c >= 0x3100 ) && ( c <= 0x312F ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the hiragana script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to hiragana script
     */
    public static boolean isHiragana ( int c ) {
        if ( ( c >= 0x3040 ) && ( c <= 0x309F ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the katakana script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to katakana script
     */
    public static boolean isKatakana ( int c ) {
        if ( ( c >= 0x30A0 ) && ( c <= 0x30FF ) ) {
            return true;
        } else if ( ( c >= 0x31F0 ) && ( c <= 0x31FF ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Obtain ISO15924 numeric script code of character. If script is not or cannot be determined,
     * then the script code 998 ('zyyy') is returned.
     * @param c the character to obtain script
     * @return an ISO15924 script code
     */
    public static int scriptOf ( int c ) { // [TBD] - needs optimization!!!
        if ( isAnySpace ( c ) ) {
            return SCRIPT_UNDETERMINED;
        } else if ( isPunctuation ( c ) ) {
            return SCRIPT_UNDETERMINED;
        } else if ( isDigit ( c ) ) {
            return SCRIPT_UNDETERMINED;
        } else if ( isLatin ( c ) ) {
            return SCRIPT_LATIN;
        } else if ( isCyrillic ( c ) ) {
            return SCRIPT_CYRILLIC;
        } else if ( isGreek ( c ) ) {
            return SCRIPT_GREEK;
        } else if ( isHan ( c ) ) {
            return SCRIPT_HAN;
        } else if ( isBopomofo ( c ) ) {
            return SCRIPT_BOPOMOFO;
        } else if ( isKatakana ( c ) ) {
            return SCRIPT_KATAKANA;
        } else if ( isHiragana ( c ) ) {
            return SCRIPT_HIRAGANA;
        } else if ( isHangul ( c ) ) {
            return SCRIPT_HANGUL;
        } else if ( isArabic ( c ) ) {
            return SCRIPT_ARABIC;
        } else if ( isHebrew ( c ) ) {
            return SCRIPT_HEBREW;
        } else if ( isMongolian ( c ) ) {
            return SCRIPT_MONGOLIAN;
        } else if ( isGeorgian ( c ) ) {
            return SCRIPT_GEORGIAN;
        } else if ( isGurmukhi ( c ) ) {
            return SCRIPT_GURMUKHI;
        } else if ( isDevanagari ( c ) ) {
            return SCRIPT_DEVANAGARI;
        } else if ( isGujarati ( c ) ) {
            return SCRIPT_GUJARATI;
        } else if ( isBengali ( c ) ) {
            return SCRIPT_BENGALI;
        } else if ( isOriya ( c ) ) {
            return SCRIPT_ORIYA;
        } else if ( isTibetan ( c ) ) {
            return SCRIPT_TIBETAN;
        } else if ( isTelugu ( c ) ) {
            return SCRIPT_TELUGU;
        } else if ( isTamil ( c ) ) {
            return SCRIPT_TAMIL;
        } else if ( isMalayalam ( c ) ) {
            return SCRIPT_MALAYALAM;
        } else if ( isSinhalese ( c ) ) {
            return SCRIPT_SINHALESE;
        } else if ( isBurmese ( c ) ) {
            return SCRIPT_BURMESE;
        } else if ( isThai ( c ) ) {
            return SCRIPT_THAI;
        } else if ( isKhmer ( c ) ) {
            return SCRIPT_KHMER;
        } else if ( isLao ( c ) ) {
            return SCRIPT_LAO;
        } else if ( isEthiopic ( c ) ) {
            return SCRIPT_ETHIOPIC;
        } else {
            return SCRIPT_UNDETERMINED;
        }
    }

    /**
     * Obtain the  script codes of each character in a character sequence. If script
     * is not or cannot be determined for some character, then the script code 998
     * ('zyyy') is returned.
     * @param cs the character sequence
     * @return a (possibly empty) array of script codes
     */
    public static int[] scriptsOf ( CharSequence cs ) {
        Set s = new HashSet();
        for ( int i = 0, n = cs.length(); i < n; i++ ) {
            s.add ( Integer.valueOf ( scriptOf ( cs.charAt ( i ) ) ) );
        }
        int[] sa = new int [ s.size() ];
        int ns = 0;
        for ( Iterator it = s.iterator(); it.hasNext();) {
            sa [ ns++ ] = ( (Integer) it.next() ) .intValue();
        }
        Arrays.sort ( sa );
        return sa;
    }

    /**
     * Determine the dominant script of a character sequence.
     * @param cs the character sequence
     * @return the dominant script or SCRIPT_UNDETERMINED
     */
    public static int dominantScript ( CharSequence cs ) {
        Map m = new HashMap();
        for ( int i = 0, n = cs.length(); i < n; i++ ) {
            int c = cs.charAt ( i );
            int s = scriptOf ( c );
            Integer k = Integer.valueOf ( s );
            Integer v = (Integer) m.get ( k );
            if ( v != null ) {
                m.put ( k, Integer.valueOf ( v.intValue() + 1 ) );
            } else {
                m.put ( k, Integer.valueOf ( 0 ) );
            }
        }
        int sMax = -1;
        int cMax = -1;
        for ( Iterator it = m.entrySet().iterator(); it.hasNext();) {
            Map.Entry e = (Map.Entry) it.next();
            Integer k = (Integer) e.getKey();
            int s = k.intValue();
            switch ( s ) {
            case SCRIPT_UNDETERMINED:
            case SCRIPT_UNCODED:
                break;
            default:
                {
                    Integer v = (Integer) e.getValue();
                    assert v != null;
                    int c = v.intValue();
                    if ( c > cMax ) {
                        cMax = c; sMax = s;
                    }
                    break;
                }
            }
        }
        if ( sMax < 0 ) {
            sMax = SCRIPT_UNDETERMINED;
        }
        return sMax;
    }

    /**
     * Determine the ISO script tag associated with an internal
     * script code.
     * @param code the script code
     * @return an ISO script tag
     */
    public static String scriptTagFromCode ( int code ) {
        String tag;
        if ( scriptTagsMap == null ) {
            scriptTagsMap = makeScriptTagsMap();
        }
        if ( ( tag = (String) scriptTagsMap.get ( Integer.valueOf ( code ) ) ) == null ) {
            tag = scriptTagFromCode ( SCRIPT_UNDETERMINED );
        }
        return tag;
    }

    /**
     * Convert a single unicode scalar value to an XML numeric character
     * reference. If in the BMP, four digits are used, otherwise 6 digits are used.
     * @param c a unicode scalar value
     * @return a string representing a numeric character reference
     */
    public static String charToNCRef ( int c ) {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0, nDigits = ( c > 0xFFFF ) ? 6 : 4; i < nDigits; i++, c >>= 4 ) {
            int d = c & 0xF;
            char hd;
            if ( d < 10 ) {
                hd = (char) ( (int) '0' + d );
            } else {
                hd = (char) ( (int) 'A' + ( d - 10 ) );
            }
            sb.append ( hd );
        }
        return "&#x" + sb.reverse() + ";";
    }

    /**
     * Convert a string to a sequence of ASCII or XML numeric character references.
     * @param s a java string (encoded in UTF-16)
     * @return a string representing a sequence of numeric character reference or
     * ASCII characters
     */
    public static String toNCRefs ( String s ) {
        StringBuffer sb = new StringBuffer();
        if ( s != null ) {
            for ( int i = 0; i < s.length(); i++ ) {
                char c = s.charAt(i);
                if ( ( c >= 32 ) && ( c < 127 ) ) {
                    if ( c == '<' ) {
                        sb.append ( "&lt;" );
                    } else if ( c == '>' ) {
                        sb.append ( "&gt;" );
                    } else if ( c == '&' ) {
                        sb.append ( "&amp;" );
                    } else {
                        sb.append ( c );
                    }
                } else {
                    sb.append ( charToNCRef ( c ) );
                }
            }
        }
        return sb.toString();
    }

    private static String padLeft ( String s, int width, char pad ) {
        StringBuffer sb = new StringBuffer();
        for ( int i = s.length(); i < width; i++ ) {
            sb.append(pad);
        }
        sb.append ( s );
        return sb.toString();
    }

    /**
     * Format character for debugging output, which it is prefixed with "0x", padded left with '0'
     * and either 4 or 6 hex characters in width according to whether it is in the BMP or not.
     * @param c character code
     * @return formatted character string
     */
    public static String format ( int c ) {
        if ( c < 1114112 ) {
            return "0x" + padLeft ( Integer.toString ( c, 16 ), ( c < 65536 ) ? 4 : 6, '0' );
        } else {
            return "!NOT A CHARACTER!";
        }
    }

    private static Map scriptTagsMap = null;

    private static void putScriptTag ( Map m, int code, String tag ) {
        m.put ( Integer.valueOf ( code ), tag );
    }

    private static Map makeScriptTagsMap() {
        HashMap m = new HashMap();
        putScriptTag ( m, SCRIPT_HEBREW, "hebr" );
        putScriptTag ( m, SCRIPT_MONGOLIAN, "mong" );
        putScriptTag ( m, SCRIPT_ARABIC, "arab" );
        putScriptTag ( m, SCRIPT_GREEK, "grek" );
        putScriptTag ( m, SCRIPT_LATIN, "latn" );
        putScriptTag ( m, SCRIPT_CYRILLIC, "cyrl" );
        putScriptTag ( m, SCRIPT_GEORGIAN, "geor" );
        putScriptTag ( m, SCRIPT_BOPOMOFO, "bopo" );
        putScriptTag ( m, SCRIPT_HANGUL, "hang" );
        putScriptTag ( m, SCRIPT_GURMUKHI, "guru" );
        putScriptTag ( m, SCRIPT_DEVANAGARI, "deva" );
        putScriptTag ( m, SCRIPT_GUJARATI, "gujr" );
        putScriptTag ( m, SCRIPT_BENGALI, "beng" );
        putScriptTag ( m, SCRIPT_ORIYA, "orya" );
        putScriptTag ( m, SCRIPT_TIBETAN, "tibt" );
        putScriptTag ( m, SCRIPT_TELUGU, "telu" );
        putScriptTag ( m, SCRIPT_TAMIL, "taml" );
        putScriptTag ( m, SCRIPT_MALAYALAM, "mlym" );
        putScriptTag ( m, SCRIPT_SINHALESE, "sinh" );
        putScriptTag ( m, SCRIPT_BURMESE, "mymr" );
        putScriptTag ( m, SCRIPT_THAI, "thai" );
        putScriptTag ( m, SCRIPT_KHMER, "khmr" );
        putScriptTag ( m, SCRIPT_LAO, "laoo" );
        putScriptTag ( m, SCRIPT_HIRAGANA, "hira" );
        putScriptTag ( m, SCRIPT_ETHIOPIC, "ethi" );
        putScriptTag ( m, SCRIPT_HAN, "hani" );
        putScriptTag ( m, SCRIPT_KATAKANA, "kana" );
        putScriptTag ( m, SCRIPT_MATH, "zmth" );
        putScriptTag ( m, SCRIPT_SYMBOL, "zsym" );
        putScriptTag ( m, SCRIPT_UNDETERMINED, "zyyy" );
        putScriptTag ( m, SCRIPT_UNCODED, "zzzz" );
        return m;
    }

    /**
     * Mirror characters that are designated as having the bidi mirrorred property.
     * @param s a string whose characters are to be mirrored
     * @return the resulting string
     */
    public static String mirror ( String s ) {
        StringBuffer sb = new StringBuffer ( s );
        for ( int i = 0, n = sb.length(); i < n; i++ ) {
            sb.setCharAt ( i, (char) mirror ( sb.charAt ( i ) ) );
        }
        return sb.toString();
    }

    private static int[] mirroredCharacters = {
        0x0028,
        0x0029,
        0x003C,
        0x003E,
        0x005B,
        0x005D,
        0x007B,
        0x007D,
        0x00AB,
        0x00BB,
        0x0F3A,
        0x0F3B,
        0x0F3C,
        0x0F3D,
        0x169B,
        0x169C,
        0x2039,
        0x203A,
        0x2045,
        0x2046,
        0x207D,
        0x207E,
        0x208D,
        0x208E,
        0x2208,
        0x2209,
        0x220A,
        0x220B,
        0x220C,
        0x220D,
        0x2215,
        0x223C,
        0x223D,
        0x2243,
        0x2252,
        0x2253,
        0x2254,
        0x2255,
        0x2264,
        0x2265,
        0x2266,
        0x2267,
        0x2268,
        0x2269,
        0x226A,
        0x226B,
        0x226E,
        0x226F,
        0x2270,
        0x2271,
        0x2272,
        0x2273,
        0x2274,
        0x2275,
        0x2276,
        0x2277,
        0x2278,
        0x2279,
        0x227A,
        0x227B,
        0x227C,
        0x227D,
        0x227E,
        0x227F,
        0x2280,
        0x2281,
        0x2282,
        0x2283,
        0x2284,
        0x2285,
        0x2286,
        0x2287,
        0x2288,
        0x2289,
        0x228A,
        0x228B,
        0x228F,
        0x2290,
        0x2291,
        0x2292,
        0x2298,
        0x22A2,
        0x22A3,
        0x22A6,
        0x22A8,
        0x22A9,
        0x22AB,
        0x22B0,
        0x22B1,
        0x22B2,
        0x22B3,
        0x22B4,
        0x22B5,
        0x22B6,
        0x22B7,
        0x22C9,
        0x22CA,
        0x22CB,
        0x22CC,
        0x22CD,
        0x22D0,
        0x22D1,
        0x22D6,
        0x22D7,
        0x22D8,
        0x22D9,
        0x22DA,
        0x22DB,
        0x22DC,
        0x22DD,
        0x22DE,
        0x22DF,
        0x22E0,
        0x22E1,
        0x22E2,
        0x22E3,
        0x22E4,
        0x22E5,
        0x22E6,
        0x22E7,
        0x22E8,
        0x22E9,
        0x22EA,
        0x22EB,
        0x22EC,
        0x22ED,
        0x22F0,
        0x22F1,
        0x22F2,
        0x22F3,
        0x22F4,
        0x22F6,
        0x22F7,
        0x22FA,
        0x22FB,
        0x22FC,
        0x22FD,
        0x22FE,
        0x2308,
        0x2309,
        0x230A,
        0x230B,
        0x2329,
        0x232A,
        0x2768,
        0x2769,
        0x276A,
        0x276B,
        0x276C,
        0x276D,
        0x276E,
        0x276F,
        0x2770,
        0x2771,
        0x2772,
        0x2773,
        0x2774,
        0x2775,
        0x27C3,
        0x27C4,
        0x27C5,
        0x27C6,
        0x27C8,
        0x27C9,
        0x27D5,
        0x27D6,
        0x27DD,
        0x27DE,
        0x27E2,
        0x27E3,
        0x27E4,
        0x27E5,
        0x27E6,
        0x27E7,
        0x27E8,
        0x27E9,
        0x27EA,
        0x27EB,
        0x27EC,
        0x27ED,
        0x27EE,
        0x27EF,
        0x2983,
        0x2984,
        0x2985,
        0x2986,
        0x2987,
        0x2988,
        0x2989,
        0x298A,
        0x298B,
        0x298C,
        0x298D,
        0x298E,
        0x298F,
        0x2990,
        0x2991,
        0x2992,
        0x2993,
        0x2994,
        0x2995,
        0x2996,
        0x2997,
        0x2998,
        0x29B8,
        0x29C0,
        0x29C1,
        0x29C4,
        0x29C5,
        0x29CF,
        0x29D0,
        0x29D1,
        0x29D2,
        0x29D4,
        0x29D5,
        0x29D8,
        0x29D9,
        0x29DA,
        0x29DB,
        0x29F5,
        0x29F8,
        0x29F9,
        0x29FC,
        0x29FD,
        0x2A2B,
        0x2A2C,
        0x2A2D,
        0x2A2E,
        0x2A34,
        0x2A35,
        0x2A3C,
        0x2A3D,
        0x2A64,
        0x2A65,
        0x2A79,
        0x2A7A,
        0x2A7D,
        0x2A7E,
        0x2A7F,
        0x2A80,
        0x2A81,
        0x2A82,
        0x2A83,
        0x2A84,
        0x2A8B,
        0x2A8C,
        0x2A91,
        0x2A92,
        0x2A93,
        0x2A94,
        0x2A95,
        0x2A96,
        0x2A97,
        0x2A98,
        0x2A99,
        0x2A9A,
        0x2A9B,
        0x2A9C,
        0x2AA1,
        0x2AA2,
        0x2AA6,
        0x2AA7,
        0x2AA8,
        0x2AA9,
        0x2AAA,
        0x2AAB,
        0x2AAC,
        0x2AAD,
        0x2AAF,
        0x2AB0,
        0x2AB3,
        0x2AB4,
        0x2AC3,
        0x2AC4,
        0x2AC5,
        0x2AC6,
        0x2ACD,
        0x2ACE,
        0x2ACF,
        0x2AD0,
        0x2AD1,
        0x2AD2,
        0x2AD3,
        0x2AD4,
        0x2AD5,
        0x2AD6,
        0x2ADE,
        0x2AE3,
        0x2E02,
        0x2E03,
        0x2E04,
        0x2E05,
        0x2E09,
        0x2E0A,
        0x2E0C,
        0x2E0D,
        0x2E1C,
        0x2E1D,
        0x2E20,
        0x2E21,
        0x2E22,
        0x2E23,
        0x2E24,
        0x2E25,
        0x2E26,
        0x300E,
        0x300F,
        0x3010,
        0x3011,
        0x3014,
        0x3015,
        0x3016,
        0x3017,
        0x3018,
        0x3019,
        0x301A,
        0x301B,
        0xFE59,
        0xFE5A,
        0xFF3B,
        0xFF3D,
        0xFF5B,
        0xFF5D,
        0xFF5F,
        0xFF60,
        0xFF62,
        0xFF63
    };

    private static int[] mirroredCharactersMapping = {
        0x0029,
        0x0028,
        0x003E,
        0x003C,
        0x005D,
        0x005B,
        0x007D,
        0x007B,
        0x00BB,
        0x00AB,
        0x0F3B,
        0x0F3A,
        0x0F3D,
        0x0F3C,
        0x169C,
        0x169B,
        0x203A,
        0x2039,
        0x2046,
        0x2045,
        0x207E,
        0x207D,
        0x208E,
        0x208D,
        0x220B,
        0x220C,
        0x220D,
        0x2208,
        0x2209,
        0x220A,
        0x29F5,
        0x223D,
        0x223C,
        0x22CD,
        0x2253,
        0x2252,
        0x2255,
        0x2254,
        0x2265,
        0x2264,
        0x2267,
        0x2266,
        0x2269,
        0x2268,
        0x226B,
        0x226A,
        0x226F,
        0x226E,
        0x2271,
        0x2270,
        0x2273,
        0x2272,
        0x2275,
        0x2274,
        0x2277,
        0x2276,
        0x2279,
        0x2278,
        0x227B,
        0x227A,
        0x227D,
        0x227C,
        0x227F,
        0x227E,
        0x2281,
        0x2280,
        0x2283,
        0x2282,
        0x2285,
        0x2284,
        0x2287,
        0x2286,
        0x2289,
        0x2288,
        0x228B,
        0x228A,
        0x2290,
        0x228F,
        0x2292,
        0x2291,
        0x29B8,
        0x22A3,
        0x22A2,
        0x2ADE,
        0x2AE4,
        0x2AE3,
        0x2AE5,
        0x22B1,
        0x22B0,
        0x22B3,
        0x22B2,
        0x22B5,
        0x22B4,
        0x22B7,
        0x22B6,
        0x22CA,
        0x22C9,
        0x22CC,
        0x22CB,
        0x2243,
        0x22D1,
        0x22D0,
        0x22D7,
        0x22D6,
        0x22D9,
        0x22D8,
        0x22DB,
        0x22DA,
        0x22DD,
        0x22DC,
        0x22DF,
        0x22DE,
        0x22E1,
        0x22E0,
        0x22E3,
        0x22E2,
        0x22E5,
        0x22E4,
        0x22E7,
        0x22E6,
        0x22E9,
        0x22E8,
        0x22EB,
        0x22EA,
        0x22ED,
        0x22EC,
        0x22F1,
        0x22F0,
        0x22FA,
        0x22FB,
        0x22FC,
        0x22FD,
        0x22FE,
        0x22F2,
        0x22F3,
        0x22F4,
        0x22F6,
        0x22F7,
        0x2309,
        0x2308,
        0x230B,
        0x230A,
        0x232A,
        0x2329,
        0x2769,
        0x2768,
        0x276B,
        0x276A,
        0x276D,
        0x276C,
        0x276F,
        0x276E,
        0x2771,
        0x2770,
        0x2773,
        0x2772,
        0x2775,
        0x2774,
        0x27C4,
        0x27C3,
        0x27C6,
        0x27C5,
        0x27C9,
        0x27C8,
        0x27D6,
        0x27D5,
        0x27DE,
        0x27DD,
        0x27E3,
        0x27E2,
        0x27E5,
        0x27E4,
        0x27E7,
        0x27E6,
        0x27E9,
        0x27E8,
        0x27EB,
        0x27EA,
        0x27ED,
        0x27EC,
        0x27EF,
        0x27EE,
        0x2984,
        0x2983,
        0x2986,
        0x2985,
        0x2988,
        0x2987,
        0x298A,
        0x2989,
        0x298C,
        0x298B,
        0x2990,
        0x298F,
        0x298E,
        0x298D,
        0x2992,
        0x2991,
        0x2994,
        0x2993,
        0x2996,
        0x2995,
        0x2998,
        0x2997,
        0x2298,
        0x29C1,
        0x29C0,
        0x29C5,
        0x29C4,
        0x29D0,
        0x29CF,
        0x29D2,
        0x29D1,
        0x29D5,
        0x29D4,
        0x29D9,
        0x29D8,
        0x29DB,
        0x29DA,
        0x2215,
        0x29F9,
        0x29F8,
        0x29FD,
        0x29FC,
        0x2A2C,
        0x2A2B,
        0x2A2E,
        0x2A2D,
        0x2A35,
        0x2A34,
        0x2A3D,
        0x2A3C,
        0x2A65,
        0x2A64,
        0x2A7A,
        0x2A79,
        0x2A7E,
        0x2A7D,
        0x2A80,
        0x2A7F,
        0x2A82,
        0x2A81,
        0x2A84,
        0x2A83,
        0x2A8C,
        0x2A8B,
        0x2A92,
        0x2A91,
        0x2A94,
        0x2A93,
        0x2A96,
        0x2A95,
        0x2A98,
        0x2A97,
        0x2A9A,
        0x2A99,
        0x2A9C,
        0x2A9B,
        0x2AA2,
        0x2AA1,
        0x2AA7,
        0x2AA6,
        0x2AA9,
        0x2AA8,
        0x2AAB,
        0x2AAA,
        0x2AAD,
        0x2AAC,
        0x2AB0,
        0x2AAF,
        0x2AB4,
        0x2AB3,
        0x2AC4,
        0x2AC3,
        0x2AC6,
        0x2AC5,
        0x2ACE,
        0x2ACD,
        0x2AD0,
        0x2ACF,
        0x2AD2,
        0x2AD1,
        0x2AD4,
        0x2AD3,
        0x2AD6,
        0x2AD5,
        0x22A6,
        0x22A9,
        0x2E03,
        0x2E02,
        0x2E05,
        0x2E04,
        0x2E0A,
        0x2E09,
        0x2E0D,
        0x2E0C,
        0x2E1D,
        0x2E1C,
        0x2E21,
        0x2E20,
        0x2E23,
        0x2E22,
        0x2E25,
        0x2E24,
        0x2E27,
        0x300F,
        0x300E,
        0x3011,
        0x3010,
        0x3015,
        0x3014,
        0x3017,
        0x3016,
        0x3019,
        0x3018,
        0x301B,
        0x301A,
        0xFE5A,
        0xFE59,
        0xFF3D,
        0xFF3B,
        0xFF5D,
        0xFF5B,
        0xFF60,
        0xFF5F,
        0xFF63,
        0xFF62
    };

    private static int mirror ( int c ) {
        int i = Arrays.binarySearch ( mirroredCharacters, c );
        if ( i < 0 ) {
            return c;
        } else {
            return mirroredCharactersMapping [ i ];
        }
    }

}
