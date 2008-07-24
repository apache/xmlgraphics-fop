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

package org.apache.fop.fo.pagination;

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
    private static final int DECIMAL = 1;       // '0*1'
    private static final int LOWERALPHA = 2;    // 'a'
    private static final int UPPERALPHA = 3;    // 'A'
    private static final int LOWERROMAN = 4;    // 'i'
    private static final int UPPERROMAN = 5;    // 'I'

    // flags
    private int formatType = DECIMAL;
    private int minPadding = 0;    // for decimal formats

    // preloaded strings of zeros
    private String[] zeros = {
        "", "0", "00", "000", "0000", "00000"
    };

    /**
     * Main constructor. For further information on the parameters see the XSLT
     * specs (Number to String Conversion Attributes).
     * @param format format for the page number
     * @param groupingSeparator grouping separator
     * @param groupingSize grouping size
     * @param letterValue letter value
     */
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
                //getLogger().debug("'format' token not recognized; using '1'");
                formatType = DECIMAL;
                minPadding = 0;
            }
        } else {
            // only accepted token is '0+1'at this stage. Because of the
            // wonderful regular expression support in Java, we will resort to a
            // loop
            for (int i = 0; i < fmtLen - 1; i++) {
                if (format.charAt(i) != '0') {
                    //getLogger().debug("'format' token not recognized; using '1'");
                    formatType = DECIMAL;
                    minPadding = 0;
                } else {
                    minPadding = fmtLen - 1;
                }
            }
        }
    }

    /**
     * Formats a page number.
     * @param number page number to format
     * @return the formatted page number as a String
     */
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
            if (formatType == UPPERROMAN) {
                pn = pn.toUpperCase();
            }
        } else {
            // alphabetic
            pn = makeAlpha(number);
            if (formatType == UPPERALPHA) {
                pn = pn.toUpperCase();
            }
        }
        return pn;
    }

    private String makeRoman(int num) {
        int[] arabic = {
            1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
        };
        String[] roman = {
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

