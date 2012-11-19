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

package org.apache.fop.pdf;

import java.util.regex.Pattern;

/**
 * Class representing a PDF /PageLabels dictionary.
 */
public class PDFPageLabels extends PDFNumberTreeNode {

    // TODO: maybe merge these constants with similar ones in PageNumberGenerator
    private static final int DECIMAL = 1; // '0*1'
    private static final int LOWER_ALPHA = 2; // 'a'
    private static final int UPPER_ALPHA = 3; // 'A'
    private static final int LOWER_ROMAN = 4; // 'i'
    private static final int UPPER_ROMAN = 5; // 'I'
    private static final int PREFIX = 6;

    private static final PDFName S_D = new PDFName("D");
    private static final PDFName S_UR = new PDFName("R");
    private static final PDFName S_LR = new PDFName("r");
    private static final PDFName S_UA = new PDFName("A");
    private static final PDFName S_LA = new PDFName("a");

    private static final Pattern MATCH_DECIMAL = Pattern.compile("\\d+");
    private static final Pattern MATCH_ROMAN = Pattern.compile(
            "^M{0,3}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MATCH_LETTER = Pattern.compile("^[a-zA-Z]$");

    private int lastPageLabelType;
    private int lastPageNumber;
    private String lastZeroPaddingPrefix = "";

    /**
     * Create the /PageLabels dictionary
     */
    public PDFPageLabels() {
        super();
    }

    /**
     * Adds a new entry, if necessary, to the /PageLabels dictionary.
     * @param index the page index (0 for page 1)
     * @param pageLabel the page number as a string
     */
    public void addPageLabel(int index, String pageLabel) {
        boolean addNewPageLabel = false;
        String padding = "000000";
        int currentPageNumber = 0;
        int currentPageLabelType = 0;
        String currentZeroPaddingPrefix = "";
        if (MATCH_DECIMAL.matcher(pageLabel).matches()) {
            // since an integer is the most common case we start with that
            currentPageLabelType = DECIMAL;
            currentPageNumber = Integer.parseInt(pageLabel);
            int zeroPadding = 0;
            if (pageLabel.startsWith("0")) {
                while (pageLabel.charAt(zeroPadding) == '0') {
                    zeroPadding++;
                }
                currentZeroPaddingPrefix = padding.substring(0, zeroPadding);
                if (currentZeroPaddingPrefix.length() != lastZeroPaddingPrefix.length()) {
                    addNewPageLabel = true;
                }
            } else {
                if (lastZeroPaddingPrefix.length() != 0) {
                    addNewPageLabel = true;
                }
            }
        } else if (MATCH_ROMAN.matcher(pageLabel).matches()) {
            if (pageLabel.toLowerCase().equals(pageLabel)) {
                currentPageLabelType = LOWER_ROMAN;
            } else {
                currentPageLabelType = UPPER_ROMAN;
            }
            currentPageNumber = romanToArabic(pageLabel);
        } else if (MATCH_LETTER.matcher(pageLabel).matches()) {
            if (pageLabel.toLowerCase().equals(pageLabel)) {
                currentPageLabelType = LOWER_ALPHA;
            } else {
                currentPageLabelType = UPPER_ALPHA;
            }
            currentPageNumber = alphabeticToArabic(pageLabel);
        } else {
            // alphabetic numbering in XSL_FO and labelling in PDF are different after AA (AB versus BB)
            // we will use the /P (prefix) label in that case
            currentPageLabelType = PREFIX;
            addNewPageLabel = true;
        }
        if (lastPageLabelType != currentPageLabelType) {
            addNewPageLabel = true;
        }
        if (lastPageNumber != currentPageNumber - 1) {
            addNewPageLabel = true;
        }
        if (addNewPageLabel) {
            PDFNumsArray nums = getNums();
            PDFDictionary dict = new PDFDictionary(nums);
            PDFName pdfName = null;
            switch (currentPageLabelType) {
            case PREFIX:
                dict.put("P", pageLabel);
                break;
            default:
                switch (currentPageLabelType) {
                case DECIMAL:
                    pdfName = S_D;
                    if (currentZeroPaddingPrefix.length() != 0) {
                        dict.put("P", currentZeroPaddingPrefix);
                    }
                    break;
                case LOWER_ROMAN:
                    pdfName = S_LR;
                    break;
                case UPPER_ROMAN:
                    pdfName = S_UR;
                    break;
                case LOWER_ALPHA:
                    pdfName = S_LA;
                    break;
                case UPPER_ALPHA:
                    pdfName = S_UA;
                    break;
                default:
                }
                dict.put("S", pdfName);
                if (currentPageNumber != 1) {
                    dict.put("St", currentPageNumber);
                }
            }
            nums.put(index, dict);
        }
        lastPageLabelType = currentPageLabelType;
        lastPageNumber = currentPageNumber;
        lastZeroPaddingPrefix = currentZeroPaddingPrefix;
    }

    private int romanToArabic(String roman) {
        int arabic = 0;
        int previousValue = 0;
        int newValue = 0;
        String upperRoman = roman.toUpperCase();
        for (int i = 0; i < upperRoman.length(); i++) {
            char romanDigit = upperRoman.charAt(i);
            switch (romanDigit) {
            case 'I':
                newValue = 1;
                break;
            case 'V':
                newValue = 5;
                break;
            case 'X':
                newValue = 10;
                break;
            case 'L':
                newValue = 50;
                break;
            case 'C':
                newValue = 100;
                break;
            case 'D':
                newValue = 500;
                break;
            case 'M':
                newValue = 1000;
                break;
            default:
            }
            if (previousValue < newValue) {
                arabic -= previousValue;
            } else {
                arabic += previousValue;
            }
            previousValue = newValue;
        }
        arabic += previousValue;
        return arabic;
    }

    private int alphabeticToArabic(String alpha) {
        int arabic = 0;
        if (alpha.length() > 1) {
            // this should never happen
            return arabic;
        }
        String lowerAlpha = alpha.toLowerCase();
        arabic = (lowerAlpha.charAt(0) - 'a' + 1);
        return arabic;
    }
}
