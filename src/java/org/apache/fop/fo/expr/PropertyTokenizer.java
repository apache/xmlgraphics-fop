/*
 * $Id: PropertyTokenizer.java,v 1.4.4.9 2003/06/12 18:19:36 pbwest Exp $
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */

package org.apache.fop.fo.expr;

import org.apache.fop.datatypes.Frequency;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Time;

/**
 * Class to tokenize XSL FO property expression.
 * This class is heavily based on the epxression tokenizer in James Clark's
 * XT, an XSLT processor.
 */
class PropertyTokenizer {

    private static final String tag = "$Name:  $";
    private static final String revision = "$Revision: 1.4.4.9 $";

    /*
     * Maintain the numbering of this list in (X)Emacs by issuing
     * a shell command on the region with replacement (M-1 M-|).  Use
     * the perl command:
     * perl -p -e 'BEGIN{$n=0};$n++ if s/= [0-9]+/= $n/'
     *
     * in vi, set mark `a' at the last line and
     * !'aperl... etc
     */
    static final int
                 EOF = 0
             ,NCNAME = 1
           ,MULTIPLY = 2
               ,LPAR = 3
               ,RPAR = 4
            ,LITERAL = 5
      ,FUNCTION_LPAR = 6
               ,PLUS = 7
              ,MINUS = 8
                ,MOD = 9
                ,DIV = 10
              ,COMMA = 11
            ,PERCENT = 12
          ,COLORSPEC = 13
              ,FLOAT = 14
            ,INTEGER = 15
    ,ABSOLUTE_LENGTH = 16
    ,RELATIVE_LENGTH = 17
               ,TIME = 18
               ,FREQ = 19
              ,ANGLE = 20
            ,INHERIT = 21
               ,AUTO = 22
               ,NONE = 23
               ,BOOL = 24
                ,URI = 25
           ,MIMETYPE = 26
              ,SLASH = 27
            // NO_UNIT is a transient token for internal use only.  It is
            // never set as the end result of parsing a token.
            ,NO_UNIT = 28
            //,NSPREFIX = 29
            //,WHITESPACE = 30
                     ;

    /*
     * Absolute unit type constants
     */
    int currentToken = EOF;
    String currentTokenValue = null;
    protected int currentUnitIndex = 0;
    protected int currentUnit;
    protected String unitString;
    protected String uri;

    private int currentTokenStartIndex = 0;
    private String expr = null;
    private int exprIndex = 0;
    private int exprLength;
    protected int property;

    protected PropertyTokenizer() {}

    /**
     * Initialize this tokenizer to tokenize the passed
     * String as a value of the passed property.
     * It is assumed that the subclass has made any necessary
     * synchronization arrangements.
     * @param property an <tt>int</tt> containing the property index.
     * @param s The Property expression to tokenize.
     */
    protected void initialize(int property, String s) {
        expr = s;
        exprLength = s.length();
        this.property = property;
        //System.out.println("-----Tokenizer initialized: " + expr);
    }

    /**
     * Reset the tokenizer to null (or equivalent) values.
     * Synchronization is achieved in the subclass.
     */
    protected void reset() {
        expr = null;
        exprIndex = 0;
        exprLength = 0;
        currentToken = EOF;
        currentTokenValue = null;
        property = 0;
        //System.out.println("-----Tokenizer reset.");
    }

    /**
     * Get the current expression
     * @return - the expression.
     */
    public String getExpr() {
        return expr;
    }

    /**
     * Return the next token in the expression string.
     * This sets the following package visible variables:
     * currentToken  An enumerated value identifying the recognized token
     * currentTokenValue  A String containing the token contents
     * currentUnit  If currentToken = ABSOLUTE_LENGTH, TIME or FREQUENCY,
     * an enumerated value identifying the unit.
     * @throws PropertyException If un unrecognized token is encountered.
     */
    void next() throws PropertyException {
        //System.out.println("expr:" + expr + ":  exprIndex: " + exprIndex);
        currentTokenValue = null;
        currentTokenStartIndex = exprIndex;
        boolean bSawDecimal;
        for (; ; ) {
            if (exprIndex >= exprLength) {
                currentToken = EOF;
                return;
            }
            char c = expr.charAt(exprIndex++);
            switch (c) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                // Whitespace characters are valid within strings.
                // in font family names, sequences of whitespace are
                // compressed into a single space. (Rec 7.8.2)
                //scanWhitespace();
                //currentToken = WHITESPACE;
                //currentTokenValue = expr.substring(currentTokenStartIndex,
                //                                   exprIndex);
                //return;
                currentTokenStartIndex = exprIndex;
                break;
            case ',':
                currentToken = COMMA;
                return;
            case '+':
                currentToken = PLUS;
                return;
            case '-':
                currentToken = MINUS;
                return;
            case '(':
                currentToken = LPAR;
                return;
            case ')':
                currentToken = RPAR;
                return;
            case '"':
            case '\'':
                exprIndex = expr.indexOf(c, exprIndex);
                if (exprIndex < 0) {
                    exprIndex = currentTokenStartIndex + 1;
                    throw new PropertyException("missing quote");
                }
                currentTokenValue = expr.substring(currentTokenStartIndex
                                                   + 1, exprIndex++);
                currentToken = LITERAL;
                return;
            case '*':
                currentToken = MULTIPLY;
                return;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                scanDigits();
                if (exprIndex < exprLength && expr.charAt(exprIndex) == '.') {
                    exprIndex++;
                    bSawDecimal = true;
                    if (exprIndex < exprLength
                            && isDigit(expr.charAt(exprIndex))) {
                        exprIndex++;
                        scanDigits();
                    }
                } else
                    bSawDecimal = false;
                currentUnitIndex = exprIndex;
                if (exprIndex < exprLength && expr.charAt(exprIndex) == '%') {
                    currentToken = PERCENT;
                    unitString = "%";
                    exprIndex++;
                } else {
                    // Check for possible unit name following number
                    currentToken = scanUnitName();
                    if (currentToken == NO_UNIT)
                        currentToken = bSawDecimal ? FLOAT : INTEGER;
                }
                currentTokenValue = expr.substring(currentTokenStartIndex,
                                                   currentUnitIndex);
                return;

            case '.':
                if (exprIndex < exprLength
                        && isDigit(expr.charAt(exprIndex))) {
                    ++exprIndex;
                    scanDigits();
                    currentUnitIndex = exprIndex;
                    if (exprIndex < exprLength
                            && expr.charAt(exprIndex) == '%') {
                        exprIndex++;
                        currentToken = PERCENT;
                    } else {
                        // Check for possible unit name following number
                        currentToken = scanUnitName();
                        if (currentToken == NO_UNIT)
                            currentToken = FLOAT;
                    }
                    currentTokenValue = expr.substring(currentTokenStartIndex,
                                                       currentUnitIndex);
                    return;
                }
                throw new PropertyException("illegal character '.'");

            case '#':    // Start of color value
                if (exprIndex < exprLength
                        && isHexDigit(expr.charAt(exprIndex))) {
                    int len;
                    ++exprIndex;
                    scanHexDigits();
                    currentToken = COLORSPEC;
                    currentTokenValue = expr.substring(currentTokenStartIndex,
                                                       exprIndex);
                    // Probably should have some multiple of 3 for length!
                    len = exprIndex - currentTokenStartIndex;
                    if (len == 4 || len == 7) return;
                    throw new PropertyException("color not 3 or 6 hex digits");
                } else {
                    throw new PropertyException("illegal character '#'");
                }

            case '/':
                currentToken = SLASH;
                return;

            default:
                --exprIndex;
                scanName();
                if (exprIndex == currentTokenStartIndex)
                    // Not a name - must be a <string>
                    throw new PropertyException
                            ("illegal character '"
                             + expr.charAt(exprIndex) + "'");
                currentTokenValue = expr.substring(currentTokenStartIndex,
                                                   exprIndex);
                if (currentTokenValue.equals("mod")) {
                    currentToken = MOD;
                   return;
                }
                if (currentTokenValue.equals("div")) {
                    currentToken = DIV;
                    return;
                }
                if (currentTokenValue.equals("inherit")) {
                    currentToken = INHERIT;
                    return;
                }
                if (currentTokenValue.equals("auto")) {
                    currentToken = AUTO;
                    return;
                }
                if (currentTokenValue.equals("none")) {
                    currentToken = NONE;
                    return;
                }
                if (currentTokenValue.equals("true")
                    || currentTokenValue.equals("false")) {
                    currentToken = BOOL;
                    return;
                }
                // Quick and dirty url "parsing".  Assume that a
                // URI-SPECIFICATION must be the only component of a
                // property value expression
                if (currentTokenValue.equals("url")
                    && expr.charAt(exprIndex) == '(') {
                    if (! scanUrl()) {
                        throw new PropertyException
                                ("Invalid url expression :" +
                                 expr.substring(exprIndex));
                    }
                    currentToken = URI;
                    return;
                }
                if (currentTokenValue.equals("content-type")) {
                    // content-type attribute value.  Must be followed
                    // by a mime type
                    if (expr.charAt(exprIndex) == ':') {
                        int mimeptr = ++exprIndex;
                        scanMimeType();
                        currentToken = MIMETYPE;
                        currentTokenValue =
                                expr.substring(mimeptr, exprIndex);
                        return;
                    }
                    // else it's just a name
                }
                if (currentTokenValue.equals("namespace-prefix")) {
                    // content-type attribute value.  Must be followed
                    // by a declared namespace-prefix or null
                    if (expr.charAt(exprIndex) == ':') {
                        int nsptr = ++exprIndex;
                        scanName();   // Allowed to be empty
                        currentToken = NCNAME;
                        currentTokenValue =
                                expr.substring(nsptr, exprIndex);
                        return;
                    }
                    // else it's just a name
                }
                if (followingParen()) {
                    currentToken = FUNCTION_LPAR;
                } else {
                    currentToken = NCNAME;
                }
                return;
            }
        }
    }

    /**
     * Attempt to recognize a valid UnitName token in the input expression.
     * @return token value appropriate to UnitName: ABSOLUTE_LENGTH,
     * RELATIVE_LENGTH or NO_UNIT.
     * @exception PropertyException if an NCName not a UnitName recognized.
     */
    private int scanUnitName() throws PropertyException {
        currentUnitIndex = exprIndex;
        scanName();
        if (currentUnitIndex < exprIndex) {
            unitString = expr.substring(currentUnitIndex, exprIndex);
            if (unitString.equals("em")) return RELATIVE_LENGTH;
            if (unitString.equals("cm")) {
                currentUnit = Length.CM;
                return ABSOLUTE_LENGTH;
            }
            if (unitString.equals("mm")) {
                currentUnit = Length.MM;
                return ABSOLUTE_LENGTH;
            }
            if (unitString.equals("in")) {
                currentUnit = Length.IN;
                return ABSOLUTE_LENGTH;
            }
            if (unitString.equals("pt")) {
                currentUnit = Length.PT;
                return ABSOLUTE_LENGTH;
            }
            if (unitString.equals("pc")) {
                currentUnit = Length.PC;
                return ABSOLUTE_LENGTH;
            }
            if (unitString.equals("px")) {
                currentUnit = Length.PX;
                return ABSOLUTE_LENGTH;
            }
            if (unitString.equals("s")) {
                currentUnit = Time.SEC;
                return TIME;
            }
            if (unitString.equals("ms")) {
                currentUnit = Time.MSEC;
                return TIME;
            }
            if (unitString.equals("Hz")) {
                currentUnit = Frequency.HZ;
                return FREQ;
            }
            if (unitString.equals("kHz")) {
                currentUnit = Frequency.KHZ;
                return FREQ;
            }
            // Not a UnitName
            throw new PropertyException
                    ("NCName following a number is not a UnitName");
        } else { // No NCName found
            return NO_UNIT;
        }
    }

    /**
     * Attempt to recognize a valid NAME token in the input expression.
     */
    private void scanName() {
        if (exprIndex < exprLength && isNameStartChar(expr.charAt(exprIndex)))
            while (++exprIndex < exprLength
                   && isNameChar(expr.charAt(exprIndex)));
    }

    /**
     * Attempt to recognize a valid sequence of decimal digits in the
     * input expression.
     */
    private void scanDigits() {
        while (exprIndex < exprLength && isDigit(expr.charAt(exprIndex)))
            exprIndex++;
    }

    /**
     * Scan to the end of a sequence of whitespace characters in the
     * input expression.
     */
    private void scanWhitespace() {
        while (exprIndex < exprLength && isSpace(expr.charAt(exprIndex)))
            exprIndex++;
    }

    /**
     * Attempt to recognize a valid sequence of hexadecimal digits in the
     * input expression.
     */
    private void scanHexDigits() {
        while (exprIndex < exprLength && isHexDigit(expr.charAt(exprIndex)))
            exprIndex++;
    }

    /**
     * Attempt to recognize a mime-type.  Working definition here:
     * NCName/NCName (NCName as recognized by scanName()).
     */
    private void scanMimeType() throws PropertyException {
        int part1 = exprIndex;
        scanName();
        if (part1 != exprIndex) {
            if (expr.charAt(exprIndex) == '/') {
                int part2 = ++exprIndex;
                scanName();
                if (part2 != exprIndex)
                    return;
            }
        }
        throw new PropertyException("Mime type expected; found:" +
                                    expr.substring(part1));
    }

    /**
     * @return a boolean value indicating whether the following non-whitespace
     * character is an opening parenthesis.
     */
    private boolean followingParen() {
        for (int i = exprIndex; i < exprLength; i++) {
            switch (expr.charAt(i)) {
            case '(':
                exprIndex = i + 1;
                return true;
            case ' ':
            case '\r':
            case '\n':
            case '\t':
                break;
            default:
                return false;
            }
        }
        return false;
    }

    /**
     * Primitive URI extractor.  Assumes that the only contents of a
     * URI-SPECIFICATION property type is a complete uri-specification.
     * No checking is done on the syntactical validity of the URI.
     * @return a boolean indicating whether the remainder of the
     * characters form the body of a <tt>url(...)</tt> specification.
     * As a side-effect, sets the <tt>protected</tt> field <i>uri</i>
     * and sets <i>exprIndex</i> past the end of the expression, when
     * returning a <tt>true</tt> value.
     */
    private boolean scanUrl() {
        char ch;
        String str = expr.substring(exprIndex).trim();
        if (str.charAt(str.length() - 1) != ')') return false;
        // Remove closing parenthesis and trim
        str = str.substring(0, str.length() - 1).trim();
        if ((ch = str.charAt(0)) == '"' || ch == '\'') {
            if (str.charAt(str.length() - 1) != ch) return false;
            str = str.substring(1, str.length() - 1);
        }
        uri = str.trim();
        exprIndex = expr.length();
        return true;
    }

    static private final String nameStartChars =
        "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static private final String nameChars = ".-0123456789";
    static private final String digits = "0123456789";
    static private final String hexchars = digits + "abcdefABCDEF";

    /**
     * Return a boolean value indicating whether the argument is a
     * decimal digit (0-9).
     * @param c The character to check
     */
    private static final boolean isDigit(char c) {
        return digits.indexOf(c) >= 0;
    }

    /**
     * Return a boolean value indicating whether the argument is a
     * hexadecimal digit (0-9, A-F, a-f).
     * @param c The character to check
     */
    private static final boolean isHexDigit(char c) {
        return hexchars.indexOf(c) >= 0;
    }

    /**
     * Return a boolean value indicating whether the argument is whitespace
     * as defined by XSL (space, newline, CR, tab).
     * @param c The character to check
     */
    private static final boolean isSpace(char c) {
        switch (c) {
        case ' ':
        case '\r':
        case '\n':
        case '\t':
            return true;
        }
        return false;
    }

    /**
     * Return a  boolean value indicating whether the argument is a valid name
     * start character, ie. can start a NAME as defined by XSL.
     * @param c The character to check
     */
    private static final boolean isNameStartChar(char c) {
        return nameStartChars.indexOf(c) >= 0 || c >= 0x80;
    }

    /**
     * Return a  boolean value indicating whether the argument is a valid name
     * character, ie. can occur in a NAME as defined by XSL.
     * @param c The character to check
     */
    private static final boolean isNameChar(char c) {
        return nameStartChars.indexOf(c) >= 0 || nameChars.indexOf(c) >= 0
               || c >= 0x80;
    }

}
