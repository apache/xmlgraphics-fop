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

package org.apache.fop.fo.expr;



/**
 * Class to tokenize XSL FO property expression.
 * This class is heavily based on the epxression tokenizer in James Clark's
 * XT, an XSLT processor.
 */
class PropertyTokenizer {

    static final int TOK_EOF = 0;
    static final int TOK_NCNAME = 1;
    static final int TOK_MULTIPLY = TOK_NCNAME + 1;
    static final int TOK_LPAR = TOK_MULTIPLY + 1;
    static final int TOK_RPAR = TOK_LPAR + 1;
    static final int TOK_LITERAL = TOK_RPAR + 1;
    static final int TOK_NUMBER = TOK_LITERAL + 1;
    static final int TOK_FUNCTION_LPAR = TOK_NUMBER + 1;
    static final int TOK_PLUS = TOK_FUNCTION_LPAR + 1;
    static final int TOK_MINUS = TOK_PLUS + 1;
    static final int TOK_MOD = TOK_MINUS + 1;
    static final int TOK_DIV = TOK_MOD + 1;
    static final int TOK_NUMERIC = TOK_DIV + 1;
    static final int TOK_COMMA = TOK_NUMERIC + 1;
    static final int TOK_PERCENT = TOK_COMMA + 1;
    static final int TOK_COLORSPEC = TOK_PERCENT + 1;
    static final int TOK_FLOAT = TOK_COLORSPEC + 1;
    static final int TOK_INTEGER = TOK_FLOAT + 1;

    protected int currentToken = TOK_EOF;
    protected String currentTokenValue = null;
    protected int currentUnitLength = 0;

    private int currentTokenStartIndex = 0;
    private /* final */ String expr;
    private int exprIndex = 0;
    private int exprLength;

    /**
     * Construct a new PropertyTokenizer object to tokenize the passed
     * String.
     * @param s The Property expressio to tokenize.
     */
    PropertyTokenizer(String s) {
        this.expr = s;
        this.exprLength = s.length();
    }

    /**
     * Return the next token in the expression string.
     * This sets the following package visible variables:
     * currentToken  An enumerated value identifying the recognized token
     * currentTokenValue  A String containing the token contents
     * currentUnitLength  If currentToken = TOK_NUMERIC, the number of
     * characters in the unit name.
     * @throws PropertyException If un unrecognized token is encountered.
     */
    void next() throws PropertyException {
        currentTokenValue = null;
        currentTokenStartIndex = exprIndex;
        boolean bSawDecimal;
        while ( true ) {
            if (exprIndex >= exprLength) {
                currentToken = TOK_EOF;
                return;
            }
            char c = expr.charAt(exprIndex++);
            switch (c) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                currentTokenStartIndex = exprIndex;
                break;
            case ',':
                currentToken = TOK_COMMA;
                return;
            case '+':
                currentToken = TOK_PLUS;
                return;
            case '-':
                currentToken = TOK_MINUS;
                return;
            case '(':
                currentToken = TOK_LPAR;
                return;
            case ')':
                currentToken = TOK_RPAR;
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
                currentToken = TOK_LITERAL;
                return;
            case '*':
                /*
                 * if (currentMaybeOperator) {
                 * recognizeOperator = false;
                 */
                currentToken = TOK_MULTIPLY;
                /*
                 * }
                 * else
                 * throw new PropertyException("illegal operator *");
                 */
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
                } else {
                    bSawDecimal = false;
                }
                if (exprIndex < exprLength && expr.charAt(exprIndex) == '%') {
                    exprIndex++;
                    currentToken = TOK_PERCENT;
                } else {
                    // Check for possible unit name following number
                    currentUnitLength = exprIndex;
                    scanName();
                    currentUnitLength = exprIndex - currentUnitLength;
                    currentToken = (currentUnitLength > 0) ? TOK_NUMERIC
                                   : (bSawDecimal ? TOK_FLOAT : TOK_INTEGER);
                }
                currentTokenValue = expr.substring(currentTokenStartIndex,
                                                   exprIndex);
                return;

            case '.':
                nextDecimalPoint();
                return;

            case '#':    // Start of color value
                nextColor();
                return;

            default:
                --exprIndex;
                scanName();
                if (exprIndex == currentTokenStartIndex) {
                    throw new PropertyException("illegal character");
                }
                currentTokenValue = expr.substring(currentTokenStartIndex, exprIndex);
                if (currentTokenValue.equals("mod")) {
                    currentToken = TOK_MOD;
                    return;
                } else if (currentTokenValue.equals("div")) {
                    currentToken = TOK_DIV;
                    return;
                }
                if (followingParen()) {
                    currentToken = TOK_FUNCTION_LPAR;
                } else {
                    currentToken = TOK_NCNAME;
                }
                return;
            }
        }
    }

    private void nextDecimalPoint() throws PropertyException {
        if (exprIndex < exprLength
                && isDigit(expr.charAt(exprIndex))) {
            ++exprIndex;
            scanDigits();
            if (exprIndex < exprLength
                    && expr.charAt(exprIndex) == '%') {
                exprIndex++;
                currentToken = TOK_PERCENT;
            } else {
                // Check for possible unit name following number
                currentUnitLength = exprIndex;
                scanName();
                currentUnitLength = exprIndex - currentUnitLength;
                currentToken = (currentUnitLength > 0) ? TOK_NUMERIC
                        : TOK_FLOAT;
            }
            currentTokenValue = expr.substring(currentTokenStartIndex,
                    exprIndex);
            return;
        }
        throw new PropertyException("illegal character '.'");
    }


    private void nextColor() throws PropertyException {
        if (exprIndex < exprLength) {
            ++exprIndex;
            scanHexDigits();
            int len = exprIndex - currentTokenStartIndex - 1;
            if (len % 3 == 0) {
                currentToken = TOK_COLORSPEC;
            } else {
                //Actually not a color at all, but an NCNAME starting with "#"
                scanRestOfName();
                currentToken = TOK_NCNAME;
            }
            currentTokenValue = expr.substring(currentTokenStartIndex,
                    exprIndex);
            return;
        } else {
            throw new PropertyException("illegal character '#'");
        }
    }

    /**
     * Attempt to recognize a valid NAME token in the input expression.
     */
    private void scanName() {
        if (exprIndex < exprLength && isNameStartChar(expr.charAt(exprIndex))) {
            scanRestOfName();
        }
    }

    private void scanRestOfName() {
        while ( ++exprIndex < exprLength ) {
            if ( !isNameChar ( expr.charAt ( exprIndex ) ) ) {
                break;
            }
        }
    }

    /**
     * Attempt to recognize a valid sequence of decimal DIGITS in the
     * input expression.
     */
    private void scanDigits() {
        while (exprIndex < exprLength && isDigit(expr.charAt(exprIndex))) {
            exprIndex++;
        }
    }

    /**
     * Attempt to recognize a valid sequence of hexadecimal DIGITS in the
     * input expression.
     */
    private void scanHexDigits() {
        while (exprIndex < exprLength && isHexDigit(expr.charAt(exprIndex))) {
            exprIndex++;
        }
    }

    /**
     * Return a boolean value indicating whether the following non-whitespace
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


    private static final String NAME_START_CHARS
        = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NAME_CHARS = ".-0123456789";
    private static final String DIGITS = "0123456789";
    private static final String HEX_CHARS = DIGITS + "abcdefABCDEF";

    /**
     * Return a boolean value indicating whether the argument is a
     * decimal digit (0-9).
     * @param c The character to check
     */
    private static boolean isDigit(char c) {
        return DIGITS.indexOf(c) >= 0;
    }

    /**
     * Return a boolean value indicating whether the argument is a
     * hexadecimal digit (0-9, A-F, a-f).
     * @param c The character to check
     */
    private static boolean isHexDigit(char c) {
        return HEX_CHARS.indexOf(c) >= 0;
    }

    /**
     * Return a boolean value indicating whether the argument is whitespace
     * as defined by XSL (space, newline, CR, tab).
     * @param c The character to check
     */
    private static boolean isSpace(char c) {
        switch (c) {
        case ' ':
        case '\r':
        case '\n':
        case '\t':
            return true;
        default:
            return false;
        }
    }

    /**
     * Return a  boolean value indicating whether the argument is a valid name
     * start character, ie. can start a NAME as defined by XSL.
     * @param c The character to check
     */
    private static boolean isNameStartChar(char c) {
        return NAME_START_CHARS.indexOf(c) >= 0 || c >= 0x80;
    }

    /**
     * Return a  boolean value indicating whether the argument is a valid name
     * character, ie. can occur in a NAME as defined by XSL.
     * @param c The character to check
     */
    private static boolean isNameChar(char c) {
        return NAME_START_CHARS.indexOf(c) >= 0 || NAME_CHARS.indexOf(c) >= 0
               || c >= 0x80;
    }

}

