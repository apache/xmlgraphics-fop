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
package org.apache.fop.fo.pagination;

// Avalon
import org.apache.avalon.framework.logger.Logger;

/**
 * This class uses the 'format', 'groupingSeparator', 'groupingSize',
 * and 'letterValue' properties on fo:page-sequence to return a String
 * corresponding to the supplied integer page number.
 */
public class PageNumberGenerator {

    private String format;
    private char groupingSeparator;
    private int groupingSize;
    private int letterValue;

    // constants
    private final int DECIMAL = 1;       // '0*1'
    private final int LOWERALPHA = 2;    // 'a'
    private final int UPPERALPHA = 3;    // 'A'
    private final int LOWERROMAN = 4;    // 'i'
    private final int UPPERROMAN = 5;    // 'I'

    // flags
    private int formatType = DECIMAL;
    private int minPadding = 0;    // for decimal formats

    // preloaded strings of zeros
    private String zeros[] = {
        "", "0", "00", "000", "0000", "00000"
    };

    private Logger log;

    public PageNumberGenerator(String format, char groupingSeparator,
                               int groupingSize, int letterValue) {
        this.format = format;
        this.groupingSeparator = groupingSeparator;
        this.groupingSize = groupingSize;
        this.letterValue = letterValue;

        // the only accepted format strings are currently '0*1' 'a', 'A', 'i'
        // and 'I'
        int fmtLen = format.length();
        if (fmtLen == 1) {
            if (format.equals("1")) {
                formatType = DECIMAL;
                minPadding = 0;
            } else if (format.equals("a")) {
                formatType = LOWERALPHA;
            } else if (format.equals("A")) {
                formatType = UPPERALPHA;
            } else if (format.equals("i")) {
                formatType = LOWERROMAN;
            } else if (format.equals("I")) {
                formatType = UPPERROMAN;
            } else {
                // token not handled
                //log.debug("'format' token not recognized; using '1'");
                formatType = DECIMAL;
                minPadding = 0;
            }
        } else {
            // only accepted token is '0+1'at this stage. Because of the
            // wonderful regular expression support in Java, we will resort to a
            // loop
            for (int i = 0; i < fmtLen - 1; i++) {
                if (format.charAt(i) != '0') {
                    //log.debug("'format' token not recognized; using '1'");
                    formatType = DECIMAL;
                    minPadding = 0;
                } else {
                    minPadding = fmtLen - 1;
                }
            }
        }
    }

    public void setLogger(Logger logger) {
        log = logger;
    }

    public String makeFormattedPageNumber(int number) {
        String pn = null;
        if (formatType == DECIMAL) {
            pn = Integer.toString(number);
            if (minPadding >= pn.length()) {
                int nz = minPadding - pn.length() + 1;
                pn = zeros[nz] + pn;
            }
        } else if ((formatType == LOWERROMAN) || (formatType == UPPERROMAN)) {
            pn = makeRoman(number);
            if (formatType == UPPERROMAN)
                pn = pn.toUpperCase();
        } else {
            // alphabetic
            pn = makeAlpha(number);
            if (formatType == UPPERALPHA)
                pn = pn.toUpperCase();
        }
        return pn;
    }

    private String makeRoman(int num) {
        int arabic[] = {
            1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
        };
        String roman[] = {
            "m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv",
            "i"
        };

        int i = 0;
        StringBuffer romanNumber = new StringBuffer();

        while (num > 0) {
            while (num >= arabic[i]) {
                num = num - arabic[i];
                romanNumber.append(roman[i]);
            }
            i = i + 1;
        }
        return romanNumber.toString();
    }

    private String makeAlpha(int num) {
        String letters = "abcdefghijklmnopqrstuvwxyz";
        StringBuffer alphaNumber = new StringBuffer();

        int base = 26;
        int rem = 0;

        num--;
        if (num < base) {
            alphaNumber.append(letters.charAt(num));
        } else {
            while (num >= base) {
                rem = num % base;
                alphaNumber.append(letters.charAt(rem));
                num = num / base;
            }
            alphaNumber.append(letters.charAt(num - 1));
        }
        return alphaNumber.reverse().toString();
    }

}

