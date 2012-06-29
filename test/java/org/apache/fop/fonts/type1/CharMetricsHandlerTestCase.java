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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.junit.Test;

import org.apache.fop.fonts.NamedCharacter;
import org.apache.fop.fonts.type1.AFMParser.ValueHandler;

/**
 * Test case for {@link CharMetricsHandler}.
 */
public class CharMetricsHandlerTestCase {

    private static final String GOOD_LINE = "C 32 ; WX 32 ; N space ; B 1 1 1 1";

    private static final AFMCharMetrics EXPECTED_CHM;

    static {
        EXPECTED_CHM = new AFMCharMetrics();
        EXPECTED_CHM.setCharCode(32);
        EXPECTED_CHM.setWidthX(32.0);
        EXPECTED_CHM.setCharacter(new NamedCharacter("space"));
        EXPECTED_CHM.setBBox(new Rectangle(1, 1, 0, 0));
    }

    @Test
    public void testHandlers() throws IOException {
        testEncodingWithMetricsLine("", GOOD_LINE);
        testEncodingWithMetricsLine("WrongEncoding", GOOD_LINE);
        testEncodingWithMetricsLine("AdobeStandardEncoding", GOOD_LINE);
    }

    private void testEncodingWithMetricsLine(String encoding, String line) throws IOException {
        Map<String, ValueHandler> valueParsers = mock(HashMap.class);
        ValueHandler cHandler = mock(ValueHandler.class);
        ValueHandler wxHandler = mock(ValueHandler.class);
        ValueHandler nHandler = mock(ValueHandler.class);
        ValueHandler bHandler = mock(ValueHandler.class);
        when(valueParsers.get("C")).thenReturn(cHandler);
        when(valueParsers.get("WX")).thenReturn(wxHandler);
        when(valueParsers.get("N")).thenReturn(nHandler);
        when(valueParsers.get("B")).thenReturn(bHandler);

        CharMetricsHandler handler = CharMetricsHandler.getHandler(valueParsers, encoding);
        Stack<Object> stack = new Stack<Object>();
        handler.parse(line, stack, null);

        verify(valueParsers).get("C");
        verify(valueParsers).get("WX");
        verify(valueParsers).get("N");
        verify(valueParsers).get("B");
        verify(cHandler).parse("32", 0, new Stack<Object>());
        verify(wxHandler).parse("32", 0, new Stack<Object>());
        verify(nHandler).parse("space", 0, new Stack<Object>());
        verify(bHandler).parse("1 1 1 1", 0, new Stack<Object>());
    }
}
