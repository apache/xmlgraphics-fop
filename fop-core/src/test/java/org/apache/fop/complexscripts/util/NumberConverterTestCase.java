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

package org.apache.fop.complexscripts.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

// CSOFF: LineLengthCheck

/**
 * Test number converter functionality.
 */
public class NumberConverterTestCase {

    private static String[][] formatDecimal =
    {
        { "1" },
        { "0", "0" },
        { "1", "1" },
        { "1000", "1000" },
        { "1000000", "1000000" },
        { "1000000000", "1000000000" },
    };

    private static String[][] formatDecimalPadded =
    {
        { "001" },
        { "0", "000" },
        { "1", "001" },
        { "9", "009" },
        { "10", "010" },
        { "99", "099" },
        { "100", "100" },
        { "999", "999" },
        { "1000", "1000" },
    };

    private static String[][] formatDecimalGrouped =
    {
        { "1", ",", "1" },
        { "0", "0" },
        { "1", "1" },
        { "1000", "1,0,0,0" },
        { "1000000", "1,0,0,0,0,0,0" },
        { "1000000000", "1,0,0,0,0,0,0,0,0,0" },
    };

    private static String[][] formatDecimalGroupedPadded =
    {
        { "001", ",", "2" },
        { "0", "0,00" },
        { "1", "0,01" },
        { "9", "0,09" },
        { "10", "0,10" },
        { "99", "0,99" },
        { "100", "1,00" },
        { "999", "9,99" },
        { "1000", "10,00" },
    };

    private static String[][] formatDecimalArabic =
    {
        { "\u0661" },
        { "0", "\u0660" },
        { "1", "\u0661" },
        { "2", "\u0662" },
        { "3", "\u0663" },
        { "4", "\u0664" },
        { "5", "\u0665" },
        { "6", "\u0666" },
        { "7", "\u0667" },
        { "8", "\u0668" },
        { "9", "\u0669" },
        { "10", "\u0661\u0660" },
        { "1000", "\u0661\u0660\u0660\u0660" },
        { "1000000", "\u0661\u0660\u0660\u0660\u0660\u0660\u0660" },
        { "1000000000", "\u0661\u0660\u0660\u0660\u0660\u0660\u0660\u0660\u0660\u0660" },
    };

    private static String[][] formatDecimalArabicPadded =
    {
        { "\u0660\u0660\u0661" },
        { "0", "\u0660\u0660\u0660" },
        { "1", "\u0660\u0660\u0661" },
        { "9", "\u0660\u0660\u0669" },
        { "10", "\u0660\u0661\u0660" },
        { "99", "\u0660\u0669\u0669" },
        { "100", "\u0661\u0660\u0660" },
        { "999", "\u0669\u0669\u0669" },
        { "1000", "\u0661\u0660\u0660\u0660" },
    };

    private static String[][] formatDecimalArabicGrouped =
    {
        { "\u0661", "\u066c", "1" },
        { "0", "\u0660" },
        { "1", "\u0661" },
        { "1000", "\u0661\u066c\u0660\u066c\u0660\u066c\u0660" },
        { "1000000", "\u0661\u066c\u0660\u066c\u0660\u066c\u0660\u066c\u0660\u066c\u0660\u066c\u0660" },
        { "1000000000", "\u0661\u066c\u0660\u066c\u0660\u066c\u0660\u066c\u0660\u066c\u0660\u066c\u0660\u066c\u0660\u066c\u0660\u066c\u0660" },
    };

    private static String[][] formatDecimalArabicGroupedPadded =
    {
        { "\u0660\u0660\u0661", "\u066c", "2" },
        { "0", "\u0660\u066c\u0660\u0660" },
        { "1", "\u0660\u066c\u0660\u0661" },
        { "9", "\u0660\u066c\u0660\u0669" },
        { "10", "\u0660\u066c\u0661\u0660" },
        { "99", "\u0660\u066c\u0669\u0669" },
        { "100", "\u0661\u066c\u0660\u0660" },
        { "999", "\u0669\u066c\u0669\u0669" },
        { "1000", "\u0661\u0660\u066c\u0660\u0660" },
    };

    private static String[][] formatDecimalThai =
    {
        { "\u0E51" },
        { "0", "\u0E50" },
        { "1", "\u0E51" },
        { "2", "\u0E52" },
        { "3", "\u0E53" },
        { "4", "\u0E54" },
        { "5", "\u0E55" },
        { "6", "\u0E56" },
        { "7", "\u0E57" },
        { "8", "\u0E58" },
        { "9", "\u0E59" },
        { "10", "\u0E51\u0E50" },
        { "1000", "\u0E51\u0E50\u0E50\u0E50" },
        { "1000000", "\u0E51\u0E50\u0E50\u0E50\u0E50\u0E50\u0E50" },
        { "1000000000", "\u0E51\u0E50\u0E50\u0E50\u0E50\u0E50\u0E50\u0E50\u0E50\u0E50" },
    };

    private static String[][] formatDecimalThaiPadded =
    {
        { "\u0E50\u0E50\u0E51" },
        { "0", "\u0E50\u0E50\u0E50" },
        { "1", "\u0E50\u0E50\u0E51" },
        { "9", "\u0E50\u0E50\u0E59" },
        { "10", "\u0E50\u0E51\u0E50" },
        { "99", "\u0E50\u0E59\u0E59" },
        { "100", "\u0E51\u0E50\u0E50" },
        { "999", "\u0E59\u0E59\u0E59" },
        { "1000", "\u0E51\u0E50\u0E50\u0E50" },
    };

    private static String[][] formatRomanLower =
    {
        { "i" },
        { "0", "0" },
        { "1", "i" },
        { "2", "ii" },
        { "3", "iii" },
        { "4", "iv" },
        { "5", "v" },
        { "6", "vi" },
        { "7", "vii" },
        { "8", "viii" },
        { "9", "ix" },
        { "10", "x" },
        { "20", "xx" },
        { "30", "xxx" },
        { "40", "xl" },
        { "50", "l" },
        { "60", "lx" },
        { "70", "lxx" },
        { "80", "lxxx" },
        { "90", "xc" },
        { "100", "c" },
        { "200", "cc" },
        { "300", "ccc" },
        { "400", "cd" },
        { "500", "d" },
        { "600", "dc" },
        { "700", "dcc" },
        { "800", "dccc" },
        { "900", "cm" },
        { "1000", "m" },
        { "2000", "mm" },
        { "2011", "mmxi" },
        { "4999", "mmmmcmxcix" },
        { "5000", "5000" },
    };

    private static String[][] formatRomanUpper =
    {

        { "I" },
        { "0", "0" },
        { "1", "I" },
        { "2", "II" },
        { "3", "III" },
        { "4", "IV" },
        { "5", "V" },
        { "6", "VI" },
        { "7", "VII" },
        { "8", "VIII" },
        { "9", "IX" },
        { "10", "X" },
        { "20", "XX" },
        { "30", "XXX" },
        { "40", "XL" },
        { "50", "L" },
        { "60", "LX" },
        { "70", "LXX" },
        { "80", "LXXX" },
        { "90", "XC" },
        { "100", "C" },
        { "200", "CC" },
        { "300", "CCC" },
        { "400", "CD" },
        { "500", "D" },
        { "600", "DC" },
        { "700", "DCC" },
        { "800", "DCCC" },
        { "900", "CM" },
        { "1000", "M" },
        { "2000", "MM" },
        { "2011", "MMXI" },
        { "4999", "MMMMCMXCIX" },
        { "5000", "5000" },
    };

    private static String[][] formatRomanLargeLower =
    {
        { "i", null, null, null, "large" },
        { "0", "0" },
        { "1", "i" },
        { "2", "ii" },
        { "3", "iii" },
        { "4", "iv" },
        { "5", "v" },
        { "6", "vi" },
        { "7", "vii" },
        { "8", "viii" },
        { "9", "ix" },
        { "10", "x" },
        { "20", "xx" },
        { "30", "xxx" },
        { "40", "xl" },
        { "50", "l" },
        { "60", "lx" },
        { "70", "lxx" },
        { "80", "lxxx" },
        { "90", "xc" },
        { "100", "c" },
        { "200", "cc" },
        { "300", "ccc" },
        { "400", "cd" },
        { "500", "d" },
        { "600", "dc" },
        { "700", "dcc" },
        { "800", "dccc" },
        { "900", "cm" },
        { "1000", "m" },
        { "2000", "mm" },
        { "2011", "mmxi" },
        { "4999", "\u2180\u2181cmxcix" },
        { "5000", "\u2181" },
        { "5001", "\u2181i" },
        { "9999", "\u2180\u2182cmxcix" },
        { "10000", "\u2182" },
        { "10001", "\u2182i" },
        { "49999", "\u2182\u2187\u2180\u2182cmxcix" },
        { "99999", "\u2182\u2188\u2180\u2182cmxcix" },
        { "100000", "\u2188" },
        { "100001", "\u2188i" },
        { "199999", "\u2188\u2182\u2188\u2180\u2182cmxcix" },
        { "200000", "200000" },
    };

    private static String[][] formatRomanLargeUpper =
    {
        { "I", null, null, null, "large" },
        { "0", "0" },
        { "1", "I" },
        { "2", "II" },
        { "3", "III" },
        { "4", "IV" },
        { "5", "V" },
        { "6", "VI" },
        { "7", "VII" },
        { "8", "VIII" },
        { "9", "IX" },
        { "10", "X" },
        { "20", "XX" },
        { "30", "XXX" },
        { "40", "XL" },
        { "50", "L" },
        { "60", "LX" },
        { "70", "LXX" },
        { "80", "LXXX" },
        { "90", "XC" },
        { "100", "C" },
        { "200", "CC" },
        { "300", "CCC" },
        { "400", "CD" },
        { "500", "D" },
        { "600", "DC" },
        { "700", "DCC" },
        { "800", "DCCC" },
        { "900", "CM" },
        { "1000", "M" },
        { "2000", "MM" },
        { "2011", "MMXI" },
        { "4999", "\u2180\u2181CMXCIX" },
        { "5000", "\u2181" },
        { "5001", "\u2181I" },
        { "9999", "\u2180\u2182CMXCIX" },
        { "10000", "\u2182" },
        { "10001", "\u2182I" },
        { "49999", "\u2182\u2187\u2180\u2182CMXCIX" },
        { "99999", "\u2182\u2188\u2180\u2182CMXCIX" },
        { "100000", "\u2188" },
        { "100001", "\u2188I" },
        { "199999", "\u2188\u2182\u2188\u2180\u2182CMXCIX" },
        { "200000", "200000" },
    };

    private static String[][] formatRomanNumberFormsLower =
    {
        { "i", null, null, null, "unicode-number-forms" },
        { "0", "0" },
        { "1", "\u2170" },
        { "2", "\u2171" },
        { "3", "\u2172" },
        { "4", "\u2173" },
        { "5", "\u2174" },
        { "6", "\u2175" },
        { "7", "\u2176" },
        { "8", "\u2177" },
        { "9", "\u2178" },
        { "10", "\u2179" },
        { "11", "\u2179\u2170" },
        { "12", "\u2179\u2171" },
        { "13", "\u2179\u2172" },
        { "14", "\u2179\u2173" },
        { "15", "\u2179\u2174" },
        { "16", "\u2179\u2175" },
        { "17", "\u2179\u2176" },
        { "18", "\u2179\u2177" },
        { "19", "\u2179\u2178" },
        { "20", "\u2179\u2179" },
        { "30", "\u2179\u2179\u2179" },
        { "40", "\u2179\u217C" },
        { "50", "\u217C" },
        { "60", "\u217C\u2179" },
        { "70", "\u217C\u2179\u2179" },
        { "80", "\u217C\u2179\u2179\u2179" },
        { "90", "\u2179\u217D" },
        { "100", "\u217D" },
        { "200", "\u217D\u217D" },
        { "300", "\u217D\u217D\u217D" },
        { "400", "\u217D\u217E" },
        { "500", "\u217E" },
        { "600", "\u217E\u217D" },
        { "700", "\u217E\u217D\u217D" },
        { "800", "\u217E\u217D\u217D\u217D" },
        { "900", "\u217D\u217F" },
        { "999", "\u217D\u217F\u2179\u217D\u2178" },
        { "1000", "\u217F" },
        { "2000", "\u217F\u217F" },
        { "2011", "\u217F\u217F\u2179\u2170" },
        { "4999", "\u2180\u2181\u217D\u217F\u2179\u217D\u2178" },
        { "5000", "\u2181" },
        { "5001", "\u2181\u2170" },
        { "9999", "\u2180\u2182\u217D\u217F\u2179\u217D\u2178" },
        { "10000", "\u2182" },
        { "10001", "\u2182\u2170" },
        { "49999", "\u2182\u2187\u2180\u2182\u217D\u217F\u2179\u217D\u2178" },
        { "99999", "\u2182\u2188\u2180\u2182\u217D\u217F\u2179\u217D\u2178" },
        { "100000", "\u2188" },
        { "100001", "\u2188\u2170" },
        { "199999", "\u2188\u2182\u2188\u2180\u2182\u217D\u217F\u2179\u217D\u2178" },
        { "200000", "200000" },
    };

    private static String[][] formatRomanNumberFormsUpper =
    {
        { "I", null, null, null, "unicode-number-forms" },
        { "0", "0" },
        { "1", "\u2160" },
        { "2", "\u2161" },
        { "3", "\u2162" },
        { "4", "\u2163" },
        { "5", "\u2164" },
        { "6", "\u2165" },
        { "7", "\u2166" },
        { "8", "\u2167" },
        { "9", "\u2168" },
        { "10", "\u2169" },
        { "11", "\u2169\u2160" },
        { "12", "\u2169\u2161" },
        { "13", "\u2169\u2162" },
        { "14", "\u2169\u2163" },
        { "15", "\u2169\u2164" },
        { "16", "\u2169\u2165" },
        { "17", "\u2169\u2166" },
        { "18", "\u2169\u2167" },
        { "19", "\u2169\u2168" },
        { "20", "\u2169\u2169" },
        { "30", "\u2169\u2169\u2169" },
        { "40", "\u2169\u216C" },
        { "50", "\u216C" },
        { "60", "\u216C\u2169" },
        { "70", "\u216C\u2169\u2169" },
        { "80", "\u216C\u2169\u2169\u2169" },
        { "90", "\u2169\u216D" },
        { "100", "\u216D" },
        { "200", "\u216D\u216D" },
        { "300", "\u216D\u216D\u216D" },
        { "400", "\u216D\u216E" },
        { "500", "\u216E" },
        { "600", "\u216E\u216D" },
        { "700", "\u216E\u216D\u216D" },
        { "800", "\u216E\u216D\u216D\u216D" },
        { "900", "\u216D\u216F" },
        { "999", "\u216D\u216F\u2169\u216D\u2168" },
        { "1000", "\u216F" },
        { "2000", "\u216F\u216F" },
        { "2011", "\u216F\u216F\u2169\u2160" },
        { "4999", "\u2180\u2181\u216D\u216F\u2169\u216D\u2168" },
        { "5000", "\u2181" },
        { "5001", "\u2181\u2160" },
        { "9999", "\u2180\u2182\u216D\u216F\u2169\u216D\u2168" },
        { "10000", "\u2182" },
        { "10001", "\u2182\u2160" },
        { "49999", "\u2182\u2187\u2180\u2182\u216D\u216F\u2169\u216D\u2168" },
        { "99999", "\u2182\u2188\u2180\u2182\u216D\u216F\u2169\u216D\u2168" },
        { "100000", "\u2188" },
        { "100001", "\u2188\u2160" },
        { "199999", "\u2188\u2182\u2188\u2180\u2182\u216D\u216F\u2169\u216D\u2168" },
        { "200000", "200000" },
    };

    private static String[][] formatAlphabeticLatinLower =
    {
        { "a" },
        { "0", "0" },
        { "1", "a" },
        { "2", "b" },
        { "3", "c" },
        { "10", "j" },
        { "20", "t" },
        { "26", "z" },
        { "27", "aa" },
        { "28", "ab" },
        { "29", "ac" },
        { "52", "az" },
        { "53", "ba" },
        { "702", "zz" },
        { "703", "aaa" },
        { "999999", "bdwgm" },
        { "1000000", "bdwgn" },
    };

    private static String[][] formatAlphabeticLatinUpper =
    {
        { "A" },
        { "0", "0" },
        { "1", "A" },
        { "2", "B" },
        { "3", "C" },
        { "10", "J" },
        { "20", "T" },
        { "26", "Z" },
        { "27", "AA" },
        { "28", "AB" },
        { "29", "AC" },
        { "52", "AZ" },
        { "53", "BA" },
        { "702", "ZZ" },
        { "703", "AAA" },
        { "999999", "BDWGM" },
        { "1000000", "BDWGN" },
    };

    private static String[][] formatAlphabeticArabicHijai =
    {
        { "\u0627", null, null, "alphabetic" },
        { "0", "0" },
        { "1", "\u0623" },
        { "2", "\u0628" },
        { "3", "\u062A" },
        { "4", "\u062B" },
        { "5", "\u062C" },
        { "6", "\u062D" },
        { "7", "\u062E" },
        { "8", "\u062F" },
        { "9", "\u0630" },
        { "10", "\u0631" },
        { "11", "\u0632" },
        { "12", "\u0633" },
        { "13", "\u0634" },
        { "14", "\u0635" },
        { "15", "\u0636" },
        { "16", "\u0637" },
        { "17", "\u0638" },
        { "18", "\u0639" },
        { "19", "\u063A" },
        { "20", "\u0641" },
        { "21", "\u0642" },
        { "22", "\u0643" },
        { "23", "\u0644" },
        { "24", "\u0645" },
        { "25", "\u0646" },
        { "26", "\u0647" },
        { "27", "\u0648" },
        { "28", "\u0649" },
        { "29", "\u0623\u0623" },
        { "56", "\u0623\u0649" },
        { "57", "\u0628\u0623" },
        { "812", "\u0649\u0649" },
        { "813", "\u0623\u0623\u0623" },
        { "999999", "\u0623\u0638\u0636\u0635\u062E" },
        { "1000000", "\u0623\u0638\u0636\u0635\u062F" },
    };

    private static String[][] formatAlphabeticArabicAbjadi =
    {
        { "\u0627", null, null, "traditional" },
        { "0", "0" },
        { "1", "\u0623" },
        { "2", "\u0628" },
        { "3", "\u062C" },
        { "4", "\u062F" },
        { "5", "\u0647" },
        { "6", "\u0648" },
        { "7", "\u0632" },
        { "8", "\u062D" },
        { "9", "\u0637" },
        { "10", "\u0649" },
        { "11", "\u0643" },
        { "12", "\u0644" },
        { "13", "\u0645" },
        { "14", "\u0646" },
        { "15", "\u0633" },
        { "16", "\u0639" },
        { "17", "\u0641" },
        { "18", "\u0635" },
        { "19", "\u0642" },
        { "20", "\u0631" },
        { "21", "\u0634" },
        { "22", "\u062A" },
        { "23", "\u062B" },
        { "24", "\u062E" },
        { "25", "\u0630" },
        { "26", "\u0636" },
        { "27", "\u0638" },
        { "28", "\u063A" },
        { "29", "\u0623\u0623" },
        { "56", "\u0623\u063A" },
        { "57", "\u0628\u0623" },
        { "812", "\u063A\u063A" },
        { "813", "\u0623\u0623\u0623" },
        { "999999", "\u0623\u0641\u0633\u0646\u0632" },
        { "1000000", "\u0623\u0641\u0633\u0646\u062D" },
    };

    private static String[][] formatNumeralArabicAbjadi =
    {
        { "\u0623", null, null, "traditional" },
        { "0", "0" },
        { "1", "\u0623" },
        { "2", "\u0628" },
        { "3", "\u062C" },
        { "4", "\u062F" },
        { "5", "\u0647" },
        { "6", "\u0648" },
        { "7", "\u0632" },
        { "8", "\u062D" },
        { "9", "\u0637" },
        { "10", "\u0649" },
        { "11", "\u0649\u0623" },
        { "12", "\u0649\u0628" },
        { "13", "\u0649\u062C" },
        { "14", "\u0649\u062F" },
        { "15", "\u0649\u0647" },
        { "16", "\u0649\u0648" },
        { "17", "\u0649\u0632" },
        { "18", "\u0649\u062D" },
        { "19", "\u0649\u0637" },
        { "20", "\u0643" },
        { "30", "\u0644" },
        { "40", "\u0645" },
        { "50", "\u0646" },
        { "60", "\u0633" },
        { "70", "\u0639" },
        { "80", "\u0641" },
        { "90", "\u0635" },
        { "99", "\u0635\u0637" },
        { "100", "\u0642" },
        { "101", "\u0642\u0623" },
        { "200", "\u0631" },
        { "300", "\u0634" },
        { "400", "\u062A" },
        { "500", "\u062B" },
        { "600", "\u062E" },
        { "700", "\u0630" },
        { "800", "\u0636" },
        { "900", "\u0638" },
        { "999", "\u0638\u0635\u0637" },
        { "1000", "\u063A" },
        { "1999", "\u063A\u0638\u0635\u0637" },
        { "2000", "2000" },
    };

    private static String[][] formatAlphabeticHebrew =
    {
        { "\u05D0", null, null, "alphabetic" },
        { "0", "0" },
        { "1", "\u05D0" },
        { "2", "\u05D1" },
        { "3", "\u05D2" },
        { "4", "\u05D3" },
        { "5", "\u05D4" },
        { "6", "\u05D5" },
        { "7", "\u05D6" },
        { "8", "\u05D7" },
        { "9", "\u05D8" },
        { "10", "\u05D9" },
        { "11", "\u05DB" },
        { "12", "\u05DC" },
        { "13", "\u05DE" },
        { "14", "\u05E0" },
        { "15", "\u05E1" },
        { "16", "\u05E2" },
        { "17", "\u05E4" },
        { "18", "\u05E6" },
        { "19", "\u05E7" },
        { "20", "\u05E8" },
        { "21", "\u05E9" },
        { "22", "\u05EA" },
        { "23", "\u05DA" },
        { "24", "\u05DD" },
        { "25", "\u05DF" },
        { "26", "\u05E3" },
        { "27", "\u05E5" },
        { "28", "\u05D0\u05D0" },
        { "54", "\u05D0\u05E5" },
        { "55", "\u05D1\u05D0" },
        { "756", "\u05E5\u05E5" },
        { "757", "\u05D0\u05D0\u05D0" },
        { "999999", "\u05D0\u05DA\u05E9\u05E7\u05E5" },
        { "1000000", "\u05D0\u05DA\u05E9\u05E8\u05D0" },
    };

    private static String[][] formatNumeralHebrewGematria =
    {
        { "\u05D0", null, null, "traditional" },
        { "0", "0" },
        { "1", "\u05D0" },
        { "2", "\u05D1" },
        { "3", "\u05D2" },
        { "4", "\u05D3" },
        { "5", "\u05D4" },
        { "6", "\u05D5" },
        { "7", "\u05D6" },
        { "8", "\u05D7" },
        { "9", "\u05D8" },
        { "10", "\u05D9" },
        { "11", "\u05D9\u05D0" },
        { "12", "\u05D9\u05D1" },
        { "13", "\u05D9\u05D2" },
        { "14", "\u05D9\u05D3" },
        { "15", "\u05D8\u05F4\u05D5" },
        { "16", "\u05D8\u05F4\u05D6" },
        { "17", "\u05D9\u05D6" },
        { "18", "\u05D9\u05D7" },
        { "19", "\u05D9\u05D8" },
        { "20", "\u05DB" },
        { "30", "\u05DC" },
        { "40", "\u05DE" },
        { "50", "\u05E0" },
        { "60", "\u05E1" },
        { "70", "\u05E2" },
        { "80", "\u05E4" },
        { "90", "\u05E6" },
        { "99", "\u05E6\u05D8" },
        { "100", "\u05E7" },
        { "101", "\u05E7\u05D0" },
        { "200", "\u05E8" },
        { "300", "\u05E9" },
        { "400", "\u05EA" },
        { "500", "\u05EA\u05F4\u05E7" },
        { "600", "\u05EA\u05F4\u05E8" },
        { "700", "\u05EA\u05F4\u05E9" },
        { "800", "\u05EA\u05F4\u05EA" },
        { "900", "\u05EA\u05EA\u05F4\u05E7" },
        { "999", "\u05EA\u05EA\u05F4\u05E7\u05E6\u05D8" },
        { "1000", "\u05D0\u05F3" },
        { "1999", "\u05D0\u05F3\u05EA\u05EA\u05F4\u05E7\u05E6\u05D8" },
        { "2000", "2000" },
    };

    private static String[][] formatAlphabeticThai =
    {
        { "\u0E01", null, null, "alphabetic" },
        { "0", "0" },
        { "1", "\u0E01" },
        { "2", "\u0E02" },
        { "3", "\u0E03" },
        { "10", "\u0E0A" },
        { "20", "\u0E14" },
        { "30", "\u0E1E" },
        { "40", "\u0E2A" },
        { "44", "\u0E2E" },
        { "45", "\u0E01\u0E01" },
        { "88", "\u0E01\u0E2E" },
        { "89", "\u0E02\u0E01" },
        { "1980", "\u0E2E\u0E2E" },
        { "1981", "\u0E01\u0E01\u0E01" },
        { "999999", "\u0E0B\u0E20\u0E17\u0E0B" },
        { "1000000", "\u0E0B\u0E20\u0E17\u0E0C" },
    };

    private static String[][] formatWordEnglishLower =
    {
        { "w", null, null, null, null, "eng" },
        { "0", "zero" },
        { "1", "one" },
        { "2", "two" },
        { "3", "three" },
        { "4", "four" },
        { "5", "five" },
        { "6", "six" },
        { "7", "seven" },
        { "8", "eight" },
        { "9", "nine" },
        { "10", "ten" },
        { "99", "ninety nine" },
        { "100", "one hundred" },
        { "999", "nine hundred ninety nine" },
        { "1000", "one thousand" },
        { "999999", "nine hundred ninety nine thousand nine hundred ninety nine" },
        { "1000000", "one million" },
        { "999999999", "nine hundred ninety nine million nine hundred ninety nine thousand nine hundred ninety nine" },
        { "1000000000", "one billion" }
    };

    private static String[][] formatWordEnglishUpper =
    {
        { "W", null, null, null, null, "eng" },
        { "0", "ZERO" },
        { "1", "ONE" },
        { "2", "TWO" },
        { "3", "THREE" },
        { "4", "FOUR" },
        { "5", "FIVE" },
        { "6", "SIX" },
        { "7", "SEVEN" },
        { "8", "EIGHT" },
        { "9", "NINE" },
        { "10", "TEN" },
        { "99", "NINETY NINE" },
        { "100", "ONE HUNDRED" },
        { "999", "NINE HUNDRED NINETY NINE" },
        { "1000", "ONE THOUSAND" },
        { "999999", "NINE HUNDRED NINETY NINE THOUSAND NINE HUNDRED NINETY NINE" },
        { "1000000", "ONE MILLION" },
        { "999999999", "NINE HUNDRED NINETY NINE MILLION NINE HUNDRED NINETY NINE THOUSAND NINE HUNDRED NINETY NINE" },
        { "1000000000", "ONE BILLION" }
    };

    private static String[][] formatWordEnglishTitle =
    {
        { "Ww", null, null, null, null, "eng" },
        { "0", "Zero" },
        { "1", "One" },
        { "2", "Two" },
        { "3", "Three" },
        { "4", "Four" },
        { "5", "Five" },
        { "6", "Six" },
        { "7", "Seven" },
        { "8", "Eight" },
        { "9", "Nine" },
        { "10", "Ten" },
        { "99", "Ninety Nine" },
        { "100", "One Hundred" },
        { "999", "Nine Hundred Ninety Nine" },
        { "1000", "One Thousand" },
        { "999999", "Nine Hundred Ninety Nine Thousand Nine Hundred Ninety Nine" },
        { "1000000", "One Million" },
        { "999999999", "Nine Hundred Ninety Nine Million Nine Hundred Ninety Nine Thousand Nine Hundred Ninety Nine" },
        { "1000000000", "One Billion" }
    };

    private static String[][] formatWordSpanishLower =
    {
        { "w", null, null, null, null, "spa" },
        { "0", "cero" },
        { "1", "uno" },
        { "2", "dos" },
        { "3", "tres" },
        { "4", "cuatro" },
        { "5", "cinco" },
        { "6", "seise" },
        { "7", "siete" },
        { "8", "ocho" },
        { "9", "nueve" },
        { "10", "diez" },
        { "11", "once" },
        { "12", "doce" },
        { "13", "trece" },
        { "14", "catorce" },
        { "15", "quince" },
        { "16", "diecis\u00e9is" },
        { "17", "diecisiete" },
        { "18", "dieciocho" },
        { "19", "diecinueve" },
        { "20", "veinte" },
        { "21", "veintiuno" },
        { "22", "veintid\u00f3s" },
        { "23", "veintitr\u00e9s" },
        { "24", "veinticuatro" },
        { "25", "veinticinco" },
        { "26", "veintis\u00e9is" },
        { "27", "veintisiete" },
        { "28", "veintiocho" },
        { "29", "veintinueve" },
        { "30", "treinta" },
        { "31", "treinta y uno" },
        { "32", "treinta y dos" },
        { "40", "cuarenta" },
        { "41", "cuarenta y uno" },
        { "42", "cuarenta y dos" },
        { "50", "cincuenta" },
        { "51", "cincuenta y uno" },
        { "52", "cincuenta y dos" },
        { "60", "sesenta" },
        { "61", "sesenta y uno" },
        { "62", "sesenta y dos" },
        { "70", "setenta" },
        { "71", "setenta y uno" },
        { "72", "setenta y dos" },
        { "80", "ochenta" },
        { "81", "ochenta y uno" },
        { "82", "ochenta y dos" },
        { "90", "noventa" },
        { "91", "noventa y uno" },
        { "92", "noventa y dos" },
        { "99", "noventa y nueve" },
        { "100", "cien" },
        { "101", "ciento uno" },
        { "102", "ciento dos" },
        { "200", "doscientos" },
        { "300", "trescientos" },
        { "400", "cuatrocientos" },
        { "500", "quinientos" },
        { "600", "seiscientos" },
        { "700", "setecientos" },
        { "800", "ochocientos" },
        { "900", "novecientos" },
        { "999", "novecientos noventa y nueve" },
        { "1000", "mil" },
        { "1001", "mil uno" },
        { "1002", "mil dos" },
        { "2000", "dos mil" },
        { "2001", "dos mil uno" },
        { "100000", "cien mil" },
        { "100001", "cien mil uno" },
        { "999999", "novecientos noventa y nueve mil novecientos noventa y nueve" },
        { "1000000", "un mill\u00f3n" },
        { "999999999", "novecientos noventa y nueve millones novecientos noventa y nueve mil novecientos noventa y nueve" },
        { "1000000000", "mil millones" }
    };

    private static String[][] formatWordSpanishUpper =
    {
        { "W", null, null, null, null, "spa" },
        { "0", "CERO" },
        { "1", "UNO" },
        { "2", "DOS" },
        { "3", "TRES" },
        { "4", "CUATRO" },
        { "5", "CINCO" },
        { "6", "SEISE" },
        { "7", "SIETE" },
        { "8", "OCHO" },
        { "9", "NUEVE" },
        { "10", "DIEZ" },
        { "11", "ONCE" },
        { "12", "DOCE" },
        { "13", "TRECE" },
        { "14", "CATORCE" },
        { "15", "QUINCE" },
        { "16", "DIECIS\u00c9IS" },
        { "17", "DIECISIETE" },
        { "18", "DIECIOCHO" },
        { "19", "DIECINUEVE" },
        { "20", "VEINTE" },
        { "21", "VEINTIUNO" },
        { "22", "VEINTID\u00d3S" },
        { "23", "VEINTITR\u00c9S" },
        { "24", "VEINTICUATRO" },
        { "25", "VEINTICINCO" },
        { "26", "VEINTIS\u00c9IS" },
        { "27", "VEINTISIETE" },
        { "28", "VEINTIOCHO" },
        { "29", "VEINTINUEVE" },
        { "30", "TREINTA" },
        { "31", "TREINTA Y UNO" },
        { "32", "TREINTA Y DOS" },
        { "40", "CUARENTA" },
        { "41", "CUARENTA Y UNO" },
        { "42", "CUARENTA Y DOS" },
        { "50", "CINCUENTA" },
        { "51", "CINCUENTA Y UNO" },
        { "52", "CINCUENTA Y DOS" },
        { "60", "SESENTA" },
        { "61", "SESENTA Y UNO" },
        { "62", "SESENTA Y DOS" },
        { "70", "SETENTA" },
        { "71", "SETENTA Y UNO" },
        { "72", "SETENTA Y DOS" },
        { "80", "OCHENTA" },
        { "81", "OCHENTA Y UNO" },
        { "82", "OCHENTA Y DOS" },
        { "90", "NOVENTA" },
        { "91", "NOVENTA Y UNO" },
        { "92", "NOVENTA Y DOS" },
        { "99", "NOVENTA Y NUEVE" },
        { "100", "CIEN" },
        { "101", "CIENTO UNO" },
        { "102", "CIENTO DOS" },
        { "200", "DOSCIENTOS" },
        { "300", "TRESCIENTOS" },
        { "400", "CUATROCIENTOS" },
        { "500", "QUINIENTOS" },
        { "600", "SEISCIENTOS" },
        { "700", "SETECIENTOS" },
        { "800", "OCHOCIENTOS" },
        { "900", "NOVECIENTOS" },
        { "999", "NOVECIENTOS NOVENTA Y NUEVE" },
        { "1000", "MIL" },
        { "1001", "MIL UNO" },
        { "1002", "MIL DOS" },
        { "2000", "DOS MIL" },
        { "2001", "DOS MIL UNO" },
        { "100000", "CIEN MIL" },
        { "100001", "CIEN MIL UNO" },
        { "999999", "NOVECIENTOS NOVENTA Y NUEVE MIL NOVECIENTOS NOVENTA Y NUEVE" },
        { "1000000", "UN MILL\u00d3N" },
        { "999999999", "NOVECIENTOS NOVENTA Y NUEVE MILLONES NOVECIENTOS NOVENTA Y NUEVE MIL NOVECIENTOS NOVENTA Y NUEVE" },
        { "1000000000", "MIL MILLONES" }
    };

    private static String[][] formatWordSpanishTitle =
    {
        { "Ww", null, null, null, null, "spa" },
        { "0", "Cero" },
        { "1", "Uno" },
        { "2", "Dos" },
        { "3", "Tres" },
        { "4", "Cuatro" },
        { "5", "Cinco" },
        { "6", "Seise" },
        { "7", "Siete" },
        { "8", "Ocho" },
        { "9", "Nueve" },
        { "10", "Diez" },
        { "11", "Once" },
        { "12", "Doce" },
        { "13", "Trece" },
        { "14", "Catorce" },
        { "15", "Quince" },
        { "16", "Diecis\u00e9is" },
        { "17", "Diecisiete" },
        { "18", "Dieciocho" },
        { "19", "Diecinueve" },
        { "20", "Veinte" },
        { "21", "Veintiuno" },
        { "22", "Veintid\u00f3s" },
        { "23", "Veintitr\u00e9s" },
        { "24", "Veinticuatro" },
        { "25", "Veinticinco" },
        { "26", "Veintis\u00e9is" },
        { "27", "Veintisiete" },
        { "28", "Veintiocho" },
        { "29", "Veintinueve" },
        { "30", "Treinta" },
        { "31", "Treinta Y Uno" },
        { "32", "Treinta Y Dos" },
        { "40", "Cuarenta" },
        { "41", "Cuarenta Y Uno" },
        { "42", "Cuarenta Y Dos" },
        { "50", "Cincuenta" },
        { "51", "Cincuenta Y Uno" },
        { "52", "Cincuenta Y Dos" },
        { "60", "Sesenta" },
        { "61", "Sesenta Y Uno" },
        { "62", "Sesenta Y Dos" },
        { "70", "Setenta" },
        { "71", "Setenta Y Uno" },
        { "72", "Setenta Y Dos" },
        { "80", "Ochenta" },
        { "81", "Ochenta Y Uno" },
        { "82", "Ochenta Y Dos" },
        { "90", "Noventa" },
        { "91", "Noventa Y Uno" },
        { "92", "Noventa Y Dos" },
        { "99", "Noventa Y Nueve" },
        { "100", "Cien" },
        { "101", "Ciento Uno" },
        { "102", "Ciento Dos" },
        { "200", "Doscientos" },
        { "300", "Trescientos" },
        { "400", "Cuatrocientos" },
        { "500", "Quinientos" },
        { "600", "Seiscientos" },
        { "700", "Setecientos" },
        { "800", "Ochocientos" },
        { "900", "Novecientos" },
        { "999", "Novecientos Noventa Y Nueve" },
        { "1000", "Mil" },
        { "1001", "Mil Uno" },
        { "1002", "Mil Dos" },
        { "2000", "Dos Mil" },
        { "2001", "Dos Mil Uno" },
        { "100000", "Cien Mil" },
        { "100001", "Cien Mil Uno" },
        { "999999", "Novecientos Noventa Y Nueve Mil Novecientos Noventa Y Nueve" },
        { "1000000", "Un Mill\u00f3n" },
        { "999999999", "Novecientos Noventa Y Nueve Millones Novecientos Noventa Y Nueve Mil Novecientos Noventa Y Nueve" },
        { "1000000000", "Mil Millones" }
    };

    private static String[][] formatWordFrenchLower =
    {
        { "w", null, null, null, null, "fra" },
        { "0", "z\u00e9ro" },
        { "1", "un" },
        { "2", "deux" },
        { "3", "trois" },
        { "4", "quatre" },
        { "5", "cinq" },
        { "6", "six" },
        { "7", "sept" },
        { "8", "huit" },
        { "9", "neuf" },
        { "10", "dix" },
        { "11", "onze" },
        { "12", "douze" },
        { "13", "treize" },
        { "14", "quatorze" },
        { "15", "quinze" },
        { "16", "seize" },
        { "17", "dix-sept" },
        { "18", "dix-huit" },
        { "19", "dix-neuf" },
        { "20", "vingt" },
        { "21", "vingt et un" },
        { "22", "vingt-deux" },
        { "23", "vingt-trois" },
        { "24", "vingt-quatre" },
        { "25", "vingt-cinq" },
        { "26", "vingt-six" },
        { "27", "vingt-sept" },
        { "28", "vingt-huit" },
        { "29", "vingt-neuf" },
        { "30", "trente" },
        { "31", "trente et un" },
        { "32", "trente-deux" },
        { "40", "quarante" },
        { "41", "quarante et un" },
        { "42", "quarante-deux" },
        { "50", "cinquante" },
        { "51", "cinquante et un" },
        { "52", "cinquante-deux" },
        { "60", "soixante" },
        { "61", "soixante et un" },
        { "62", "soixante-deux" },
        { "70", "soixante-dix" },
        { "71", "soixante et onze" },
        { "72", "soixante-douze" },
        { "79", "soixante-dix-neuf" },
        { "80", "quatre-vingts" },
        { "81", "quatre-vingt-un" },
        { "82", "quatre-vingt-deux" },
        { "89", "quatre-vingt-neuf" },
        { "90", "quatre-vingt-dix" },
        { "91", "quatre-vingt-onze" },
        { "92", "quatre-vingt-douze" },
        { "99", "quatre-vingt-dix-neuf" },
        { "100", "cent" },
        { "101", "cent un" },
        { "102", "cent deux" },
        { "200", "deux cents" },
        { "201", "deux cent un" },
        { "202", "deux cent deux" },
        { "300", "trois cents" },
        { "301", "trois cent un" },
        { "400", "quatre cents" },
        { "401", "quatre cent un" },
        { "500", "cinq cents" },
        { "501", "cinq cent un" },
        { "600", "six cents" },
        { "601", "six cent un" },
        { "700", "sept cents" },
        { "701", "sept cent un" },
        { "800", "huit cents" },
        { "801", "huit cent un" },
        { "900", "neuf cents" },
        { "901", "neuf cent un" },
        { "999", "neuf cent quatre-vingt-dix-neuf" },
        { "1000", "mille" },
        { "1001", "mille un" },
        { "1002", "mille deux" },
        { "2000", "deux mille" },
        { "2001", "deux mille un" },
        { "100000", "cent mille" },
        { "100001", "cent mille un" },
        { "999999", "neuf cent quatre-vingt-dix-neuf mille neuf cent quatre-vingt-dix-neuf" },
        { "1000000", "un million" },
        { "999999999", "neuf cent quatre-vingt-dix-neuf millions neuf cent quatre-vingt-dix-neuf mille neuf cent quatre-vingt-dix-neuf" },
        { "1000000000", "un milliard" }
    };

    private static String[][] formatWordFrenchUpper =
    {
        { "W", null, null, null, null, "fra" },
        { "0", "Z\u00c9RO" },
        { "1", "UN" },
        { "2", "DEUX" },
        { "3", "TROIS" },
        { "4", "QUATRE" },
        { "5", "CINQ" },
        { "6", "SIX" },
        { "7", "SEPT" },
        { "8", "HUIT" },
        { "9", "NEUF" },
        { "10", "DIX" },
        { "11", "ONZE" },
        { "12", "DOUZE" },
        { "13", "TREIZE" },
        { "14", "QUATORZE" },
        { "15", "QUINZE" },
        { "16", "SEIZE" },
        { "17", "DIX-SEPT" },
        { "18", "DIX-HUIT" },
        { "19", "DIX-NEUF" },
        { "20", "VINGT" },
        { "21", "VINGT ET UN" },
        { "22", "VINGT-DEUX" },
        { "23", "VINGT-TROIS" },
        { "24", "VINGT-QUATRE" },
        { "25", "VINGT-CINQ" },
        { "26", "VINGT-SIX" },
        { "27", "VINGT-SEPT" },
        { "28", "VINGT-HUIT" },
        { "29", "VINGT-NEUF" },
        { "30", "TRENTE" },
        { "31", "TRENTE ET UN" },
        { "32", "TRENTE-DEUX" },
        { "40", "QUARANTE" },
        { "41", "QUARANTE ET UN" },
        { "42", "QUARANTE-DEUX" },
        { "50", "CINQUANTE" },
        { "51", "CINQUANTE ET UN" },
        { "52", "CINQUANTE-DEUX" },
        { "60", "SOIXANTE" },
        { "61", "SOIXANTE ET UN" },
        { "62", "SOIXANTE-DEUX" },
        { "70", "SOIXANTE-DIX" },
        { "71", "SOIXANTE ET ONZE" },
        { "72", "SOIXANTE-DOUZE" },
        { "79", "SOIXANTE-DIX-NEUF" },
        { "80", "QUATRE-VINGTS" },
        { "81", "QUATRE-VINGT-UN" },
        { "82", "QUATRE-VINGT-DEUX" },
        { "89", "QUATRE-VINGT-NEUF" },
        { "90", "QUATRE-VINGT-DIX" },
        { "91", "QUATRE-VINGT-ONZE" },
        { "92", "QUATRE-VINGT-DOUZE" },
        { "99", "QUATRE-VINGT-DIX-NEUF" },
        { "100", "CENT" },
        { "101", "CENT UN" },
        { "102", "CENT DEUX" },
        { "200", "DEUX CENTS" },
        { "201", "DEUX CENT UN" },
        { "202", "DEUX CENT DEUX" },
        { "300", "TROIS CENTS" },
        { "301", "TROIS CENT UN" },
        { "400", "QUATRE CENTS" },
        { "401", "QUATRE CENT UN" },
        { "500", "CINQ CENTS" },
        { "501", "CINQ CENT UN" },
        { "600", "SIX CENTS" },
        { "601", "SIX CENT UN" },
        { "700", "SEPT CENTS" },
        { "701", "SEPT CENT UN" },
        { "800", "HUIT CENTS" },
        { "801", "HUIT CENT UN" },
        { "900", "NEUF CENTS" },
        { "901", "NEUF CENT UN" },
        { "999", "NEUF CENT QUATRE-VINGT-DIX-NEUF" },
        { "1000", "MILLE" },
        { "1001", "MILLE UN" },
        { "1002", "MILLE DEUX" },
        { "2000", "DEUX MILLE" },
        { "2001", "DEUX MILLE UN" },
        { "100000", "CENT MILLE" },
        { "100001", "CENT MILLE UN" },
        { "999999", "NEUF CENT QUATRE-VINGT-DIX-NEUF MILLE NEUF CENT QUATRE-VINGT-DIX-NEUF" },
        { "1000000", "UN MILLION" },
        { "999999999", "NEUF CENT QUATRE-VINGT-DIX-NEUF MILLIONS NEUF CENT QUATRE-VINGT-DIX-NEUF MILLE NEUF CENT QUATRE-VINGT-DIX-NEUF" },
        { "1000000000", "UN MILLIARD" }
    };

    private static String[][] formatWordFrenchTitle =
    {
        { "Ww", null, null, null, null, "fra" },
        { "0", "Z\u00e9ro" },
        { "1", "Un" },
        { "2", "Deux" },
        { "3", "Trois" },
        { "4", "Quatre" },
        { "5", "Cinq" },
        { "6", "Six" },
        { "7", "Sept" },
        { "8", "Huit" },
        { "9", "Neuf" },
        { "10", "Dix" },
        { "11", "Onze" },
        { "12", "Douze" },
        { "13", "Treize" },
        { "14", "Quatorze" },
        { "15", "Quinze" },
        { "16", "Seize" },
        { "17", "Dix-sept" },
        { "18", "Dix-huit" },
        { "19", "Dix-neuf" },
        { "20", "Vingt" },
        { "21", "Vingt Et Un" },
        { "22", "Vingt-deux" },
        { "23", "Vingt-trois" },
        { "24", "Vingt-quatre" },
        { "25", "Vingt-cinq" },
        { "26", "Vingt-six" },
        { "27", "Vingt-sept" },
        { "28", "Vingt-huit" },
        { "29", "Vingt-neuf" },
        { "30", "Trente" },
        { "31", "Trente Et Un" },
        { "32", "Trente-deux" },
        { "40", "Quarante" },
        { "41", "Quarante Et Un" },
        { "42", "Quarante-deux" },
        { "50", "Cinquante" },
        { "51", "Cinquante Et Un" },
        { "52", "Cinquante-deux" },
        { "60", "Soixante" },
        { "61", "Soixante Et Un" },
        { "62", "Soixante-deux" },
        { "70", "Soixante-dix" },
        { "71", "Soixante Et Onze" },
        { "72", "Soixante-douze" },
        { "79", "Soixante-dix-neuf" },
        { "80", "Quatre-vingts" },
        { "81", "Quatre-vingt-un" },
        { "82", "Quatre-vingt-deux" },
        { "89", "Quatre-vingt-neuf" },
        { "90", "Quatre-vingt-dix" },
        { "91", "Quatre-vingt-onze" },
        { "92", "Quatre-vingt-douze" },
        { "99", "Quatre-vingt-dix-neuf" },
        { "100", "Cent" },
        { "101", "Cent Un" },
        { "102", "Cent Deux" },
        { "200", "Deux Cents" },
        { "201", "Deux Cent Un" },
        { "202", "Deux Cent Deux" },
        { "300", "Trois Cents" },
        { "301", "Trois Cent Un" },
        { "400", "Quatre Cents" },
        { "401", "Quatre Cent Un" },
        { "500", "Cinq Cents" },
        { "501", "Cinq Cent Un" },
        { "600", "Six Cents" },
        { "601", "Six Cent Un" },
        { "700", "Sept Cents" },
        { "701", "Sept Cent Un" },
        { "800", "Huit Cents" },
        { "801", "Huit Cent Un" },
        { "900", "Neuf Cents" },
        { "901", "Neuf Cent Un" },
        { "999", "Neuf Cent Quatre-vingt-dix-neuf" },
        { "1000", "Mille" },
        { "1001", "Mille Un" },
        { "1002", "Mille Deux" },
        { "2000", "Deux Mille" },
        { "2001", "Deux Mille Un" },
        { "100000", "Cent Mille" },
        { "100001", "Cent Mille Un" },
        { "999999", "Neuf Cent Quatre-vingt-dix-neuf Mille Neuf Cent Quatre-vingt-dix-neuf" },
        { "1000000", "Un Million" },
        { "999999999", "Neuf Cent Quatre-vingt-dix-neuf Millions Neuf Cent Quatre-vingt-dix-neuf Mille Neuf Cent Quatre-vingt-dix-neuf" },
        { "1000000000", "Un Milliard" }
    };

    /**
     * Tests decimal from latin script.
     * @throws Exception if the test fails
     */
    @Test
    public void testFormatDecimal() throws Exception {
        performConversions(formatDecimal);
        performConversions(formatDecimalPadded);
        performConversions(formatDecimalGrouped);
        performConversions(formatDecimalGroupedPadded);
    }

    /**
     * Tests decimal from arabic script.
     * @throws Exception if the test fails
     */
    @Test
    public void testFormatDecimalArabic() throws Exception {
        performConversions(formatDecimalArabic);
        performConversions(formatDecimalArabicPadded);
        performConversions(formatDecimalArabicGrouped);
        performConversions(formatDecimalArabicGroupedPadded);
    }

    /**
     * Tests decimal from thai script.
     * @throws Exception if the test fails
     */
    @Test
    public void testFormatDecimalThai() throws Exception {
        performConversions(formatDecimalThai);
        performConversions(formatDecimalThaiPadded);
    }

    /**
     * Tests roman numbers.
     * @throws Exception if the test fails
     */
    @Test
    public void testFormatRoman() throws Exception {
        performConversions(formatRomanLower);
        performConversions(formatRomanUpper);
        performConversions(formatRomanLargeLower);
        performConversions(formatRomanLargeUpper);
        performConversions(formatRomanNumberFormsLower);
        performConversions(formatRomanNumberFormsUpper);
    }

    /**
     * Tests latin alphabetic sequence numerals.
     * @throws Exception if the test fails
     */
    @Test
    public void testAlphabeticLatin() throws Exception {
        performConversions(formatAlphabeticLatinLower);
        performConversions(formatAlphabeticLatinUpper);
    }

    /**
     * Tests arabic alphabetic sequence numerals.
     * @throws Exception if the test fails
     */
    @Test
    public void testAlphabeticArabic() throws Exception {
        performConversions(formatAlphabeticArabicHijai);
        performConversions(formatAlphabeticArabicAbjadi);
    }

    /**
     * Tests hebrew alphabetic sequence numerals.
     * @throws Exception if the test fails
     */
    @Test
    public void testAlphabeticHebrew() throws Exception {
        performConversions(formatAlphabeticHebrew);
    }

    /**
     * Tests latin alphabetic sequence numerals.
     * @throws Exception if the test fails
     */
    @Test
    public void testAlphabeticThai() throws Exception {
        performConversions(formatAlphabeticThai);
    }

    /**
     * Tests arabic numerals..
     * @throws Exception if the test fails
     */
    @Test
    public void testNumeralArabic() throws Exception {
        performConversions(formatNumeralArabicAbjadi);
    }

    /**
     * Tests hebrew numerals.
     * @throws Exception if the test fails
     */
    @Test
    public void testNumeralHebrew() throws Exception {
        performConversions(formatNumeralHebrewGematria);
    }

    /**
     * Tests english word numerals.
     * @throws Exception if the test fails
     */
    @Test
    public void testWordEnglish() throws Exception {
        performConversions(formatWordEnglishLower);
        performConversions(formatWordEnglishUpper);
        performConversions(formatWordEnglishTitle);
    }

    /**
     * Tests spanish word numerals.
     * @throws Exception if the test fails
     */
    @Test
    public void testWordSpanish() throws Exception {
        performConversions(formatWordSpanishLower);
        performConversions(formatWordSpanishUpper);
        performConversions(formatWordSpanishTitle);
    }

    /**
     * Tests french word numerals.
     * @throws Exception if the test fails
     */
    @Test
    public void testWordFrench() throws Exception {
        performConversions(formatWordFrenchLower);
        performConversions(formatWordFrenchUpper);
        performConversions(formatWordFrenchTitle);
    }

    /**
     * Perform conversions according to test specification.
     * @param ts test specification
     */
    private void performConversions(String[][] ts) {
        assert ts != null;
        assert ts.length >= 2;
        String[] args = ts[0];
        assert args != null;
        assert args.length > 0;
        String format = args[0];
        assert format.length() > 0;
        char groupingSeparator;
        if (args.length > 1) {
            String s = args[1];
            if ((s != null) && (s.length() > 0)) {
                groupingSeparator = s.charAt(0);
            } else {
                groupingSeparator = 0;
            }
        } else {
            groupingSeparator = 0;
        }
        int groupingSize;
        if (args.length > 2) {
            String s = args[2];
            if ((s != null) && (s.length() > 0)) {
                groupingSize = Integer.parseInt(s);
            } else {
                groupingSize = 0;
            }
        } else {
            groupingSize = 0;
        }
        int letterValue;
        if (args.length > 3) {
            String s = args[3];
            if ((s != null) && (s.length() > 0)) {
                s = s.toLowerCase();
                if (s.equals("alphabetic")) {
                    letterValue = NumberConverter.LETTER_VALUE_ALPHABETIC;
                } else if (s.equals("traditional")) {
                    letterValue = NumberConverter.LETTER_VALUE_TRADITIONAL;
                } else {
                    letterValue = 0;
                }
            } else {
                letterValue = 0;
            }
        } else {
            letterValue = 0;
        }
        String features;
        if (args.length > 4) {
            String s = args[4];
            if ((s != null) && (s.length() > 0)) {
                features = s;
            } else {
                features = null;
            }
        } else {
            features = null;
        }
        String language;
        if (args.length > 5) {
            String s = args[5];
            if ((s != null) && (s.length() > 0)) {
                language = s;
            } else {
                language = null;
            }
        } else {
            language = null;
        }
        String country;
        if (args.length > 6) {
            String s = args[6];
            if ((s != null) && (s.length() > 0)) {
                country = s;
            } else {
                country = null;
            }
        } else {
            country = null;
        }
        NumberConverter nc = new NumberConverter(format, groupingSeparator, groupingSize, letterValue, features, language, country);
        for (int i = 1, nt = ts.length; i < nt; i++) {
            String[] sa = ts[i];
            assert sa != null;
            assert sa.length >= 2;
            List<Long> numbers = new ArrayList<Long>();
            for (int k = 0, nn = sa.length - 1; k < nn; k++) {
                String s = sa[k];
                numbers.add(Long.valueOf(s));
            }
            String expected = sa [ sa.length - 1 ];
            String actual = nc.convert(numbers);
            assertEquals(expected, actual);
        }
    }

}
