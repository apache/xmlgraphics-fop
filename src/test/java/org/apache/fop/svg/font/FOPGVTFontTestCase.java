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

package org.apache.fop.svg.font;

import java.text.StringCharacterIterator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.fonts.Font;

public class FOPGVTFontTestCase {

    private FOPGVTFont font;

    @Before
    public void createFont() {
        Font f = mock(Font.class);
        when(f.hasChar(eq((char) 0))).thenReturn(false);
        when(f.hasChar(eq((char) 1))).thenReturn(true);
        font = new FOPGVTFont(f, null);
    }

    @Test
    public void testCanDisplayUpTo() {
        char[] text = new char[] {1, 1, 1};
        testCanDisplayUpToVariants(text, -1, 0, 3);
        testCanDisplayUpToVariants(text, -1, 1, 3);
        text = new char[] {1, 1, 0, 1};
        testCanDisplayUpToVariants(text, 2, 0, 4);
        testCanDisplayUpToVariants(text, 2, 1, 4);
        testCanDisplayUpToVariants(text, 2, 2, 4);
        testCanDisplayUpToVariants(text, -1, 3, 4);
        testCanDisplayUpToVariants(text, -1, 1, 2);
    }

    @Test
    public void testCanDisplayUpToString() {
        assertEquals(-1, font.canDisplayUpTo(new String(new char[] {1, 1, 1})));
        assertEquals(0, font.canDisplayUpTo(new String(new char[] {0, 1, 1})));
        assertEquals(1, font.canDisplayUpTo(new String(new char[] {1, 0, 1})));
        assertEquals(2, font.canDisplayUpTo(new String(new char[] {1, 1, 0})));
    }

    private void testCanDisplayUpToVariants(char[] text, int expected, int start, int limit) {
        assertEquals(expected, font.canDisplayUpTo(text, start, limit));
        assertEquals(expected, font.canDisplayUpTo(new StringCharacterIterator(new String(text)), start, limit));
    }
}
