/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.properties.*;

// Avalon
import org.apache.avalon.framework.logger.Logger;

// Java
import java.util.*;

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
    private int DECIMAL = 1;       // '0*1'
    private int LOWERALPHA = 2;    // 'a'
    private int UPPERALPHA = 3;    // 'A'
    private int LOWERROMAN = 4;    // 'i'
    private int UPPERROMAN = 5;    // 'I'

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
            // only accepted token is '0+1'at this stage.	 Because of the
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

