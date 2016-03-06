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

package org.apache.fop.fonts.type1;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.fop.fonts.type1.PostscriptParser.PSDictionary;
import org.apache.fop.fonts.type1.PostscriptParser.PSElement;
import org.apache.fop.fonts.type1.PostscriptParser.PSFixedArray;
import org.apache.fop.fonts.type1.PostscriptParser.PSSubroutine;
import org.apache.fop.fonts.type1.PostscriptParser.PSVariable;
import org.apache.fop.fonts.type1.PostscriptParser.PSVariableArray;

public class PostscriptParserTestCase {
    private PostscriptParser parser;
    private String eol = new String(new byte[] {13});
    private String postscriptElements =
            "/myVariable 100 def" + eol
            + "/-| {def} executeonly def" + eol
            + "/myFixedArray 6 array" + eol
            + "0 1 5 {1 index exch /.notdef put } for" + eol
            + "dup 1 /a put" + eol
            + "dup 2 /b put" + eol
            + "dup 3 /c put" + eol
            + "dup 4 /d put" + eol
            + "readonly def" + eol
            + "/myVariableArray [ { this } { is } { a } { test } ] no access def" + eol
            + "/refVarSubr myValue -|";

    @Before
    public void setUp() {
        parser = new PostscriptParser();
    }

    /**
     * Tests parsing an example Postscript document and verifying what
     * has been read.
     * @throws IOException
     */
    @Test
    public void testPostscriptParsing() throws IOException {
        List<PSElement> elements = parser.parse(postscriptElements.getBytes());
        assertEquals(elements.size(), 5);
        assertTrue(elements.get(0) instanceof PSVariable);
        assertTrue(elements.get(2) instanceof PSFixedArray);
        assertTrue(elements.get(3) instanceof PSVariableArray);
        PSFixedArray fixedArray = (PSFixedArray)elements.get(2);
        assertEquals(fixedArray.getEntries().size(), 4);
        assertEquals(fixedArray.getEntries().get(2), "dup 2 /b put ");
        PSVariableArray variableArray = (PSVariableArray)elements.get(3);
        assertEquals(variableArray.getEntries().size(), 4);
        /* Currently only variable arrays containing subroutines are supported, though
         * this can be modified to support single values and also strip out unnecessary
         * characters like the { } below. */
        assertEquals(variableArray.getEntries().get(0).trim(), "{  this  }");
    }

    /**
     * Tests that the correct element is returned given the operator and element ID provided
     */
    @Test
    public void testCreateElement() {
        assertTrue(parser.createElement("/custDictionary", "dict", -1) instanceof PSDictionary);
        assertEquals(parser.createElement("/Private", "dict", -1), null);
        assertTrue(parser.createElement("/aFixedArray", "array", -1) instanceof PSFixedArray);
        assertTrue(parser.createElement("/aVariableArray", "[", -1) instanceof PSVariableArray);
        assertTrue(parser.createElement("/aSubroutine", "{", -1) instanceof PSSubroutine);
    }
}
