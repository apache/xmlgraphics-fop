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

package org.apache.fop.text.linebreak;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit test case for the LineBreakStatus class
 */
public class LineBreakStatusTest extends TestCase {

    /*
     * These symbols are used to indicate the break action returned
     * by the paragraph breaking. Their meaning is as per Unicode
     * <a href="http://unicode.org/reports/tr14/#PairBasedImplementation">technical
     * report #14</a>.
     */
    private static final String BREAK_ACTION = "_%#@^!";

    /**
     * Creates the test with the given name.
     * @param testName The name for this test.
     */
    public LineBreakStatusTest(String testName) {
        super(testName);
    }

    /**
     * Returns an TestSuite constructed from this class.
     * @return the TestSuite
     * @see junit.framework.TestSuite#TestSuite(class)
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(LineBreakStatusTest.class);

        return suite;
    }

    /**
     * Test of reset method, of class org.apache.commons.text.linebreak.LineBreakStatus.
     */
    public void testReset() {
        System.out.println("testReset");
        // TODO
    }

    /**
     * Test of nextChar method, of class org.apache.commons.text.linebreak.LineBreakStatus.
     * Runs tests for most of the Line Breaking Properties defined in the Unicode standard.
     */
    public void testNextChar() {
        System.out.println("testNextChar");

        // AL -- Ordinary Alphabetic and Symbol Characters (XP)
        assertTrue(testBreak(
            "Nobreak",
            "^^^^^^^"
            ));

        // BA -- Break Opportunity After (A)
        assertTrue(testBreak(
            "Thin Space" + "\u2009" + "break",
            "^^^^^%^^^^" + "^"      + "_^^^^"
            ));

        assertTrue(testBreak(
            "Shy" + "\u00AD" + "break",
            "^^^" + "^"      + "_^^^^"
            ));


        // BB -- Break opportunites before characters (B)
        assertTrue(testBreak(
            "Acute Accent" + "\u00B4" + "break",
            "^^^^^^%^^^^^" + "_"      + "^^^^^"
            ));

        // B2 -- Break Opportunity Before and After (B/A/XP)
        assertTrue(testBreak(
            "Em Dash" + "\u2014" + "break",
            "^^^%^^^" + "_"      + "_^^^^"
            ));

        assertTrue(testBreak(
            "Em Dash Dash" + "\u2014" + "\u2014" + "break",
            "^^^%^^^^%^^^" + "_"      + "^"      + "_^^^^"
            ));

        // BK Mandatory Break (A) -- normative
        assertTrue(testBreak(
            "Form Feed" + "\u000C" + "break",
            "^^^^^%^^^" + "^"      + "!^^^^"
            ));

        assertTrue(testBreak(
            "Line Separator" + "\u2028" + "break",
            "^^^^^%^^^^^^^^" + "^"      + "!^^^^"
            ));

        assertTrue(testBreak(
            "Paragraph Separator" + "\u2029" + "break",
            "^^^^^^^^^^%^^^^^^^^" + "^"      + "!^^^^"
            ));

        // CB Contingent Break Opportunity (B/A) -- normative
        // TODO Don't know quite what to do here

        // CL -- Closing Punctuation (XB)
        assertTrue(testBreak(
            "Right Parenthesis ) break",
            "^^^^^^%^^^^^^^^^^^^^%^^^^"
            ));

        // CM -- Attached Characters and Combining Marks (XB) -- normative
        assertTrue(testBreak(
            "Grave Accent" + "\u0300" + " break",
            "^^^^^^%^^^^^" + "^"      + "^%^^^^"
            ));

        // CR -- Carriage Return (A) -- normative
        assertTrue(testBreak(
            "CR" + "\r" + "break",
            "^^" + "^"  + "!^^^^"
            ));

        assertTrue(testBreak(
            "CRLF" + "\r\n" + "break",
            "^^^^" + "^^"   + "!^^^^"
            ));

        // EX -- Exclamation / interrogation (XB)
        assertTrue(testBreak(
            "EX CL ! ) break",
            "^^^%^^^^^^%^^^^"
            ));

        assertTrue(testBreak(
            "EX Wave Dash ! " + "\u301C" + " break",
            "^^^%^^^^%^^^^^^" + "%"      + "^_^^^^"
            ));

        // GL -- Non-breaking ("Glue") (XB/XA) -- normative
        assertTrue(testBreak(
            "No" + "\u00a0" + "break",
            "^^" + "^"      + "^^^^^"
            ));

        assertTrue(testBreak(
            "Non" + "\u2011" + " Hyphen",
            "^^^" + "^"      + "^%^^^^^"
            ));

        // H2 -- Hangul LVT Syllable (B/A)
        // TODO

        // H3 -- Hangul LVT Syllable (B/A)
        // TODO

        // HY -- Hyphen Minus
        assertTrue(testBreak(
            "Normal-Hyphen",
            "^^^^^^^_^^^^^"
            ));

        assertTrue(testBreak(
            "Normal - Hyphen",
            "^^^^^^^%^_^^^^^"
            ));

        assertTrue(testBreak(
            "123-456",
            "^^^^^^^"
            ));

        assertTrue(testBreak(
            "123 - 456",
            "^^^^%^%^^"
            ));

        // ID -- Ideographic (B/A)
        assertTrue(testBreak(
            "\u4E00" + "\u3000" + "\u4E02",
            "^"      + "_"      + "_"
            ));

        // IN -- Inseperable characters (XP)
        assertTrue(testBreak(
            "IN " + "\u2024" + "\u2025" + "\u2026",
            "^^^" + "%"      + "^"      + "^"
            ));

        // IS -- Numeric Separator (Infix) (XB)
        assertTrue(testBreak(
            "123,456.00 12:59",
            "^^^^^^^^^^^%^^^^"
            ));

        // JL -- Hangul L Jamo (B)
        // TODO

        // JT -- Hangul T Jamo (A)
        // TODO

        // JV -- Hangul V Jamo (XA/XB)
        // TODO

        // LF -- Line Feed (A) -- normative
        assertTrue(testBreak(
            "Simple" + "\n" + "\n" + "break",
            "^^^^^^" + "^" + "!"  + "!^^^^"
            ));

        // NL -- Next Line (A) -- normative
        assertTrue(testBreak(
            "NL" + "\u0085" + "break",
            "^^" + "^"      + "!^^^^"
            ));

        // NS -- Non-starters (XB)
        // TODO

        // NU -- Numeric (XP)
        // Tested as part of IS

        // OP -- Opening Punctuation (XA)
        assertTrue(testBreak(
            "[ Bracket ( Parenthesis",
            "^^^^^^^^^^%^^^^^^^^^^^^"
            ));

        // PO -- Postfix (Numeric) (XB)
        assertTrue(testBreak(
            "(12.00)%",
            "^^^^^^^^"
            ));

        // PR -- Prefix (Numeric) (XA)
        assertTrue(testBreak(
            "$1000.00",
            "^^^^^^^^"
            ));

        // QU -- Ambiguous Quotation (XB/XA)
        assertTrue(testBreak(
            "'In Quotes'",
            "^^^^%^^^^^^"
            ));

        assertTrue(testBreak(
            "' (In Quotes) '",
            "^^^^^^%^^^^^^^%"
            ));

        // SA -- Complex-context Dependent Characters (South East Asian) (P)
        // TODO

        // SP -- Space (A) -- normative
        assertTrue(testBreak(
            "Simple break",
            "^^^^^^^%^^^^"
            ));

        assertTrue(testBreak(
            "Simple    break2",
            "^^^^^^^^^^%^^^^^"
            ));

        // SY -- Symbols Allowing Break After (A)
        assertTrue(testBreak(
            "http://xmlgraphics.apache.org/fop",
            "^^^^^^^_^^^^^^^^^^^^^^^^^^^^^^_^^"
            ));

        assertTrue(testBreak(
            "1/2 31/10/2005",
            "^^^^%^^^^^^^^^"
            ));

        // WJ -- Word Joiner (XA/XB) -- (normative)
        assertTrue(testBreak(
            "http://" + "\u2060" + "xmlgraphics.apache.org/" + "\uFEFF" + "fop",
            "^^^^^^^" + "^"      + "^^^^^^^^^^^^^^^^^^^^^^^" + "^"      + "^^^"
            ));

        assertTrue(testBreak(
            "Simple " + "\u2060" + "break",
            "^^^^^^^" + "^"      + "^^^^^"
            ));

        assertTrue(testBreak(
            "Simple" + "\u200B" + "\u2060" + "break",
            "^^^^^^" + "^"      + "_"      + "^^^^^"
            ));

        // XX -- Unknown (XP)
        // TODO

        // ZW -- Zero Width Space (A) -- (normative)
        assertTrue(testBreak(
            "Simple" + "\u200B" + "break",
            "^^^^^^" + "^"      + "_^^^^"
            ));

    }

    /**
     * Tests the paragraph break status (break actions) returned from calling
     * LineBreakStatus.nextChar() on each character of paragraph against
     * the expected break actions. There must be a positional match between
     * the characters in paragraph and characters in breakAction.
     * @param paragraph The text to be analysed for line breaks
     * @param breakActions The symbolic representation of the break actions
     * expected to be returned.
     */
    private boolean testBreak(String paragraph, String breakActions) {
        boolean result = true;
        int length = paragraph.length();
        LineBreakStatus lbs = new LineBreakStatus();
        for (int i = 0; i < length; i++) {
            byte breakAction = lbs.nextChar(paragraph.charAt(i));
            if (BREAK_ACTION.charAt(breakAction) != breakActions.charAt(i)) {
                System.err.println(paragraph);
                System.err.println(breakActions);
                System.err.println("pos = " + i
                    + " expected '" + breakActions.charAt(i)
                    + "' got '" + BREAK_ACTION.charAt(breakAction) + "'");
                result = false;
            }
        }
        return result;
    }
}
