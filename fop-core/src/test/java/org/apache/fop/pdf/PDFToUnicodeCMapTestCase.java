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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.model.EventSeverity;

public class PDFToUnicodeCMapTestCase {

    private static final int UNICODE_CHAR_MAP_SIZE = 200;

    private static final char[] S_UNICODE_CHAR_MAP = new char[UNICODE_CHAR_MAP_SIZE];

    private EventBroadcaster eventBroadcaster;

    @Before
    public void initUnicodeChatMap() {
        for (int i = 0; i < UNICODE_CHAR_MAP_SIZE; ++i) {
            S_UNICODE_CHAR_MAP[i] = (char)(50 + i);
        }
    }

    @Before
    public void initEventBroadcaster() {
        URI config = URI.create("");
        FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(config);
        FopFactory fopFactory = fopFactoryBuilder.build();
        eventBroadcaster = fopFactory.newFOUserAgent().getEventBroadcaster();
    }

    private void assertHeader(String cmap) {
        Assert.assertTrue(cmap.contains("/CIDInit /ProcSet findresource begin\n"
                + "12 dict begin\n"
                + "begincmap\n"
                + "/CIDSystemInfo 3 dict dup begin\n"
                + "  /Registry (Adobe) def\n"
                + "  /Ordering (UCS) def\n"
                + "  /Supplement 0 def\n"
                + "end def\n"
                + "/CMapName /Adobe-Identity-UCS def\n"
                + "/CMapType 2 def\n"));
    }

    private void assertFooter(String cmap) {
        Assert.assertTrue(cmap.contains("endcmap\n"
                + "CMapName currentdict /CMap defineresource pop\n"
                + "end\n"
                + "end\n"));
    }

    private void assertHeaderAndFooter(String cmap) {
        assertHeader(cmap);
        assertFooter(cmap);
    }

    private void buildAndAssertLine(char[] unicodeCharMap, Boolean singleByte, String expected) throws IOException {
        PDFToUnicodeCMap cMap = new PDFToUnicodeCMap(unicodeCharMap,
                PDFCMap.ENC_GB_EUC_H,
                new PDFCIDSystemInfo("Adobe", "Identity", 0),
                singleByte, eventBroadcaster);

        CharArrayWriter writer = new CharArrayWriter();
        CMapBuilder builder = cMap.createCMapBuilder(writer);
        builder.writeCMap();
        String cmap = writer.toString();
        Assert.assertTrue(cmap.contains(expected));
    }

    private void buildAndAssert(char[] unicodeCharMap, Map<Boolean, String> configPairs) throws IOException {
        Set<Map.Entry<Boolean, String>> configSet = configPairs.entrySet();
        for (Map.Entry<Boolean, String> entry : configSet) {
            buildAndAssertLine(unicodeCharMap, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Checks entire CMap of unmodified unicodeCharMap, including header and footer.
     * @throws IOException
     */
    @Test
    public void simpleTest() throws IOException {
        Map<Boolean, String> configPairs = new HashMap<>();
        configPairs.put(true, "1 begincodespacerange\n"  // Single-byte char map
                + "<00> <FF>\n"
                + "endcodespacerange\n"
                + "1 beginbfrange\n"
                + "<00> <c7> <0032>\n"
                + "endbfrange\n");
        configPairs.put(false, "1 begincodespacerange\n"  // Double-byte char map
                + "<0000> <FFFF>\n"
                + "endcodespacerange\n"
                + "1 beginbfrange\n"
                + "<0000> <00c7> <0032>\n"
                + "endbfrange\n");

        Set<Map.Entry<Boolean, String>> configSet = configPairs.entrySet();
        for (Map.Entry<Boolean, String> entry : configSet) {
            PDFToUnicodeCMap cMap = new PDFToUnicodeCMap(S_UNICODE_CHAR_MAP,
                    PDFCMap.ENC_GB_EUC_H,
                    new PDFCIDSystemInfo("Adobe", "Identity", 0),
                    entry.getKey(), eventBroadcaster);

            CharArrayWriter writer = new CharArrayWriter();
            CMapBuilder builder = cMap.createCMapBuilder(writer);
            builder.writeCMap();
            String cmap = writer.toString();
            assertHeaderAndFooter(cmap);
            Assert.assertTrue(cmap.contains(entry.getValue()));
        }
    }

    /**
     * Checks CMap of unicodeCharMap with one codepoint changed so it is out of sequence.
     * @throws IOException
     */
    @Test
    public void rangeTest() throws IOException {
        S_UNICODE_CHAR_MAP[0x32] = 0xfa;  // Interrupt the range with an oddity.

        Map<Boolean, String> configPairs = new HashMap<>();
        configPairs.put(true, "1 begincodespacerange\n"
                + "<00> <FF>\n"
                + "endcodespacerange\n"
                + "1 beginbfchar\n"
                + "<32> <00fa>\n"
                + "endbfchar\n"
                + "2 beginbfrange\n"
                + "<00> <31> <0032>\n"
                + "<33> <c7> <0065>\n"
                + "endbfrange");
        configPairs.put(false, "1 begincodespacerange\n"
                + "<0000> <FFFF>\n"
                + "endcodespacerange\n"
                + "1 beginbfchar\n"
                + "<0032> <00fa>\n"
                + "endbfchar\n"
                + "2 beginbfrange\n"
                + "<0000> <0031> <0032>\n"
                + "<0033> <00c7> <0065>\n"
                + "endbfrange");

        buildAndAssert(S_UNICODE_CHAR_MAP, configPairs);
    }

    /**
     * Checks that one surrogate pair is correctly handled, even when it crosses a section boundary.
     * @throws IOException
     */
    @Test
    public void surrogatePairTest() throws IOException {
        final int charMapSize = 157;

        char[] unicodeCharMap = new char[charMapSize];

        for (int i = 0; i < charMapSize; ++i) {
            unicodeCharMap[i] = (char)(50 + i * 2);
        }

        unicodeCharMap[99] = '\uD83C'; // High-surrogate code unit, last code unit of section.
        unicodeCharMap[100] = '\uDF65';

        Map<Boolean, String> configPairs = new HashMap<>();
        configPairs.put(true, "<60> <00f2>\n"
                + "<61> <00f4>\n"
                + "<62> <00f6>\n"
                + "<63> <d83cdf65>\n"
                + "endbfchar\n"
                + "56 beginbfchar\n"
                + "<65> <00fc>\n"
                + "<66> <00fe>");
        configPairs.put(false, "<0060> <00f2>\n"
                + "<0061> <00f4>\n"
                + "<0062> <00f6>\n"
                + "<0063> <d83cdf65>\n"
                + "endbfchar\n"
                + "56 beginbfchar\n"
                + "<0065> <00fc>\n"
                + "<0066> <00fe>");

        buildAndAssert(unicodeCharMap, configPairs);
    }

    /**
     * Checks that a range of surrogate pairs is correctly handled.
     * @throws IOException
     */
    @Test
    public void surrogatePairRangeTest() throws IOException {
        final int charMapSize = 20;

        char[] unicodeCharMap = new char[charMapSize];

        for (int i = 0; i < charMapSize; ++i) {
            unicodeCharMap[i] = (char)(50 + i * 2);
        }

        unicodeCharMap[9] = '\uD83C';
        unicodeCharMap[10] = '\uDF65';
        unicodeCharMap[11] = '\uD83C';
        unicodeCharMap[12] = '\uDF66';

        Map<Boolean, String> configPairs = new HashMap<>();
        configPairs.put(true, "1 beginbfrange\n"
                + "<09> <0b> <d83cdf65>\n"
                + "endbfrange");
        configPairs.put(false, "1 beginbfrange\n"
                + "<0009> <000b> <d83cdf65>\n"
                + "endbfrange");

        buildAndAssert(unicodeCharMap, configPairs);
    }

    /**
     * Checks that CMap is correct, even when made up of just one range of surrogate pairs.
     * @throws IOException
     */
    @Test
    public void surrogatePairsRangeTest() throws IOException {
        final int charMapSize = 20;

        char[] unicodeCharMap = new char[charMapSize];

        for (int i = 0; i < charMapSize; i = i + 2) {
            unicodeCharMap[i] = '\uD83C';
        }
        for (int i = 0; i < charMapSize / 2; ++i) {
            unicodeCharMap[1 + i * 2] = (char)('\uDF65' + i);
        }

        Map<Boolean, String> configPairs = new HashMap<>();
        configPairs.put(true, "1 beginbfrange\n"
                + "<00> <12> <d83cdf65>\n"
                + "endbfrange");
        configPairs.put(false, "1 beginbfrange\n"
                + "<0000> <0012> <d83cdf65>\n"
                + "endbfrange");

        buildAndAssert(unicodeCharMap, configPairs);
    }

    /**
     * Checks that an unpaired surrogate (a high-surrogate as the last code unit) is correctly handled.
     * @throws IOException
     */
    @Test
    public void unpairedHighSurrogateTest() throws IOException {
        final int charMapSize = 10;

        char[] unicodeCharMap = new char[charMapSize];

        for (int i = 0; i < charMapSize; ++i) {
            unicodeCharMap[i] = (char)(50 + i);
        }

        unicodeCharMap[9] = '\uD83C'; // High-surrogate code unit.

        Map<Boolean, String> configPairs = new HashMap<>();
        configPairs.put(true, "1 beginbfchar\n"
                + "<09> <d83c0000>\n"
                + "endbfchar");
        configPairs.put(false, "1 beginbfchar\n"
                + "<0009> <d83c0000>\n"
                + "endbfchar");

        Set<Map.Entry<Boolean, String>> configSet = configPairs.entrySet();
        for (Map.Entry<Boolean, String> entry : configSet) {
            MyEventListener listener = new MyEventListener();

            eventBroadcaster.addEventListener(listener);

            buildAndAssertLine(unicodeCharMap, entry.getKey(), entry.getValue());

            Event ev = listener.event;
            assertNotNull(ev);
            assertEquals("org.apache.fop.render.pdf.PDFEventProducer.unpairedSurrogate", listener.event.getEventID());
            assertEquals(EventSeverity.ERROR, listener.event.getSeverity());

            eventBroadcaster.removeEventListener(listener);
        }
    }

    private class MyEventListener implements EventListener {

        private Event event;

        public void processEvent(Event event) {
            if (this.event != null) {
                fail("Multiple events received");
            }
            this.event = event;
        }
    }

    /**
     * Checks that a range of non-surrogate pairs is limited in size.
     * @throws IOException
     */
    @Test
    public void rangeSizeTest() throws IOException {
        final int charMapSize = 300;

        char[] unicodeCharMap = new char[charMapSize];

        for (int i = 0; i < charMapSize; ++i) {
            unicodeCharMap[i] = (char)(50 + i);
        }

        Map<Boolean, String> configPairs = new HashMap<>();
        // PDFToUnicodeCMap CTOR rejects unicodeCharMap with > 256 elements where singleByte is true.
        configPairs.put(false, "2 beginbfrange\n"
                + "<0000> <00ff> <0032>\n"
                + "<0100> <012b> <0132>\n"
                + "endbfrange");

        buildAndAssert(unicodeCharMap, configPairs);
    }

    /**
     * Checks that a range of surrogate pairs is limited in size.
     * @throws IOException
     */
    @Test
    public void rangeSizeSurrogateTest() throws IOException {
        final int charMapSize = 300;

        char[] unicodeCharMap = new char[charMapSize];

        for (int i = 0; i < charMapSize; i = i + 2) {
            unicodeCharMap[i] = '\uD83C';
        }
        for (int i = 0; i < charMapSize / 2; ++i) {
            unicodeCharMap[1 + i * 2] = (char)('\uDF65' + i);
        }

        Map<Boolean, String> configPairs = new HashMap<>();
        // PDFToUnicodeCMap CTOR rejects unicodeCharMap with > 256 elements where singleByte is true.
        configPairs.put(false, "2 beginbfrange\n"
                + "<0000> <00fe> <d83cdf65>\n"
                + "<0100> <012a> <d83cdfe5>\n"
                + "endbfrange");

        buildAndAssert(unicodeCharMap, configPairs);
    }
}
