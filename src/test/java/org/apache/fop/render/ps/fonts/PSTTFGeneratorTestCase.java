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

package org.apache.fop.render.ps.fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.xmlgraphics.ps.PSGenerator;

/**
 * The test class for org.apache.fop.render.ps.fonts.PSGenerator
 */
public class PSTTFGeneratorTestCase {
    private PSTTFGenerator ttfGen;
    private ByteArrayOutputStream out = new ByteArrayOutputStream();
    private PSGenerator gen = new PSGenerator(out);
    private byte[] byteArray;

    /**
     * Constructor
     */
    public PSTTFGeneratorTestCase() {
        byteArray = new byte[65536];
        for (int i = 0; i < 65536; i++) {
            byteArray[i] = (byte) i;
        }
    }

    @Before
    public void setUp() {
        ttfGen = new PSTTFGenerator(gen);
    }

    /**
     * Tests startString() - starts the string in an appropriate way for a PostScript file.
     * @exception IOException write error
     */
    @Test
    public void testStartString() throws IOException {
        ttfGen.startString();
        assertEquals("<\n", out.toString());
    }

    /**
     * Test streamBytes() - tests that strings are written to file in the proper format.
     * @throws IOException write error.
     */
    @Test
    public void testStreamBytes() throws IOException {
        ttfGen.streamBytes(byteArray, 0, 16);
        assertEquals("000102030405060708090A0B0C0D0E0F", out.toString());
        /*
         * 65520 is the closes multiple of 80 to 65535 (max string size in PS document) and since
         * one byte takes up two characters, 65520 / 2 - 16 (16 bytes already written)= 32744.
         */
        ttfGen.streamBytes(byteArray, 0, 32744);
        // Using a regex to ensure that the format is correct
        assertTrue(out.toString().matches("([0-9A-F]{80}\n){819}"));
        try {
            ttfGen.streamBytes(byteArray, 0, PSTTFGenerator.MAX_BUFFER_SIZE + 1);
            fail("Shouldn't be able to write more than MAX_BUFFER_SIZE to a PS document");
        } catch (UnsupportedOperationException e) {
            // PASS
        }
    }

    /**
     * Test reset() - reset should reset the line counter such that when reset() is invoked the
     * following string streamed to the PS document should be 80 chars long.
     * @throws IOException file write error.
     */
    @Test
    public void testReset() throws IOException {
        ttfGen.streamBytes(byteArray, 0, 40);
        assertTrue(out.toString().matches("([0-9A-F]{80}\n)"));
        ttfGen.streamBytes(byteArray, 0, 40);
        assertTrue(out.toString().matches("([0-9A-F]{80}\n){2}"));

    }

    /**
     * Test endString() - ensures strings are ended in the PostScript document in the correct
     * format, a "00" needs to be appended to the end of a string.
     * @throws IOException file write error
     */
    @Test
    public void testEndString() throws IOException {
        ttfGen.endString();
        assertEquals("00\n> ", out.toString());
        out.reset();
        // we need to check that this doesn't write more than 80 chars per line
        ttfGen.streamBytes(byteArray, 0, 40);
        ttfGen.endString();
        assertTrue(out.toString().matches("([0-9A-F]{80}\n)00\n> "));
    }
}
