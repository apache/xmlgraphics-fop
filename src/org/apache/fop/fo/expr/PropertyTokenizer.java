/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.fo.expr;

/**
 * Class to tokenize XSL FO property expression.
 * This class is heavily based on the epxression tokenizer in James Clark's
 * XT, an XSLT processor.
 */
class PropertyTokenizer {

    static final int TOK_EOF = 0;
    static final int TOK_NCNAME = TOK_EOF + 1;
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

    int currentToken = TOK_EOF;
    String currentTokenValue = null;
    protected int currentUnitLength = 0;

    private int currentTokenStartIndex = 0;
    private /* final */ String expr;
    private int exprIndex = 0;
    private int exprLength;
    private boolean recognizeOperator = false;


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
        boolean currentMaybeOperator = recognizeOperator;
        boolean bSawDecimal;
        recognizeOperator = true;
        for (; ; ) {
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
                recognizeOperator = false;
                currentToken = TOK_COMMA;
                return;
            case '+':
                recognizeOperator = false;
                currentToken = TOK_PLUS;
                return;
            case '-':
                recognizeOperator = false;
                currentToken = TOK_MINUS;
                return;
            case '(':
                currentToken = TOK_LPAR;
                recognizeOperator = false;
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
                } else
                    bSawDecimal = false;
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

            case '#':    // Start of color value
                if (exprIndex < exprLength
                        && isHexDigit(expr.charAt(exprIndex))) {
                    ++exprIndex;
                    scanHexDigits();
                    currentToken = TOK_COLORSPEC;
                    currentTokenValue = expr.substring(currentTokenStartIndex,
                                                       exprIndex);
                    // Probably should have some multiple of 3 for length!
                    return;
                } else
                    throw new PropertyException("illegal character '#'");

            default:
                --exprIndex;
                scanName();
                if (exprIndex == currentTokenStartIndex)
                    throw new PropertyException("illegal character");
                currentTokenValue = expr.substring(currentTokenStartIndex,
        exprIndex);
                // if (currentMaybeOperator) {
                if (currentTokenValue.equals("mod")) {
                    currentToken = TOK_MOD;
                    return;
                } else if (currentTokenValue.equals("div")) {
                    currentToken = TOK_DIV;
                    return;
                }
                /*
                 * else
                 * throw new PropertyException("unrecognized operator name");
                 * recognizeOperator = false;
                 * return;
                 * }
                 */
                if (followingParen()) {
                    currentToken = TOK_FUNCTION_LPAR;
                    recognizeOperator = false;
                } else {
                    currentToken = TOK_NCNAME;
                    recognizeOperator = false;
                }
                return;
            }
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
     * Attempt to recognize a valid sequence of hexadecimal digits in the
     * input expression.
     */
    private void scanHexDigits() {
        while (exprIndex < exprLength && isHexDigit(expr.charAt(exprIndex)))
            exprIndex++;
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

    private static final int CS = 1, nameStartChars = 1; // "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int C = 2, nameChars = 2; //".-0123456789";
    private static final int D = 4, digits = 4; //"0123456789";
    private static final int H = 8, hexchars = 8; //digits + "abcdefABCDEF";
    private static final int C_CS = C + CS;


    private static final int charMap[] = {
                                             0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, //0x00
                                             0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, //0x10
                                             0,0,0,0, 0,0,0,0, 0,0,0,0, 0,C,C,0, //0x20
                                             C+D+H,C+D+H,C+D+H,C+D+H, C+D+H,C+D+H,C+D+H,C+D+H, C+D+H,C+D+H,0,0, 0,0,0,0, //0x30
                                             0,CS+H,CS+H,CS+H, CS+H,CS+H,CS+H,CS, CS,CS,CS,CS, CS,CS,CS,CS, //0x40
                                             CS,CS,CS,CS, CS,CS,CS,CS, CS,CS,CS,0, 0,0,0,CS,  //0x50
                                             0,CS+H,CS+H,CS+H, CS+H,CS+H,CS+H,CS, CS,CS,CS,CS, CS,CS,CS,CS, //0x60
                                             CS,CS,CS,CS, CS,CS,CS,CS, CS,CS,CS,0, 0,0,0,0  //0x70
                                         };

    /**
     * Return a boolean value indicating whether the argument is a
     * decimal digit (0-9).
     * @param c The character to check
     */
    private static final boolean isDigit(char c) {
        return c > 0 && c < 128 && (charMap[ c] & digits) != 0;
    }

    /**
     * Return a boolean value indicating whether the argument is a
     * hexadecimal digit (0-9, A-F, a-f).
     * @param c The character to check
     */
    private static final boolean isHexDigit(char c) {
        return c > 0 && c < 128 && (charMap[ c] & hexchars) != 0;
        //return hexchars.indexOf(c) >= 0;
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
        return c >= 0x80 || c < 0 || (charMap[ c] & nameStartChars) != 0;
        //return nameStartChars.indexOf(c) >= 0 || c >= 0x80;
    }

    /**
     * Return a  boolean value indicating whether the argument is a valid name
     * character, ie. can occur in a NAME as defined by XSL.
     * @param c The character to check
     */
    private static final boolean isNameChar(char c) {
        return c > 0x80 || c < 0 || (charMap[ c] & C_CS) != 0;
        //return nameStartChars.indexOf(c) >= 0 || nameChars.indexOf(c) >= 0 || c >= 0x80;
    }

}
