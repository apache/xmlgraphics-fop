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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import junit.framework.TestCase;

import org.mockito.InOrder;

/**
 * Test class for PSTTFGlyphOutputStream
 */
public class PSTTFGlyphOutputStreamTest extends TestCase {
    private PSTTFGenerator mockGen;
    private PSTTFGlyphOutputStream glyphOut;

    @Override
    public void setUp() {
        mockGen = mock(PSTTFGenerator.class);
        glyphOut = new PSTTFGlyphOutputStream(mockGen);
    }

    /**
     * Test startGlyphStream() - test that startGlyphStream() invokes reset() and startString() in
     * PSTTFGenerator.
     * @exception IOException file write error
     */
    public void testStartGlyphStream() throws IOException {
        glyphOut.startGlyphStream();
        verify(mockGen).startString();
    }

    /**
     * Test streamGlyph(byte[],int,int) - tests several paths:
     * 1) strings are properly appended
     * 2) when total strings size > PSTTFGenerator.MAX_BUFFER_SIZE, the strings is closed and a new
     * strings is started.
     * 3) if a glyph of size > PSTTFGenerator.MAX_BUFFER_SIZE is attempted, an exception is thrown.
     * @throws IOException file write error.
     */
    public void testStreamGlyph() throws IOException {
        int byteArraySize = 10;
        byte[] byteArray = new byte[byteArraySize];
        int runs = 100;
        for (int i = 0; i < runs; i++) {
            glyphOut.streamGlyph(byteArray, 0, byteArraySize);
        }
        verify(mockGen, times(runs)).streamBytes(byteArray, 0, byteArraySize);

        /*
         * We want to run this for MAX_BUFFER_SIZE / byteArraySize so that go over the string
         * boundary and enforce the ending and starting of a new string. Using mockito to ensure
         * that this behaviour is performed in order (since this is an integral behavioural aspect)
         */
        int stringLimit = PSTTFGenerator.MAX_BUFFER_SIZE / byteArraySize;
        for (int i = 0; i < stringLimit; i++) {
            glyphOut.streamGlyph(byteArray, 0, byteArraySize);
        }
        InOrder inOrder = inOrder(mockGen);
        inOrder.verify(mockGen, times(stringLimit)).streamBytes(byteArray, 0, byteArraySize);
        inOrder.verify(mockGen).endString();
        inOrder.verify(mockGen).startString();
        inOrder.verify(mockGen, times(runs)).streamBytes(byteArray, 0, byteArraySize);

        try {
            glyphOut.streamGlyph(byteArray, 0, PSTTFGenerator.MAX_BUFFER_SIZE + 1);
            fail("Shouldn't allow a length > PSTTFGenerator.MAX_BUFFER_SIZE");
        } catch (UnsupportedOperationException e) {
            // PASS
        }
    }

    /**
     * Test endGlyphStream() - tests that PSTTFGenerator.endString() is invoked when this method
     * is called.
     * @throws IOException file write exception
     */
    public void testEndGlyphStream() throws IOException {
        glyphOut.endGlyphStream();
        verify(mockGen).endString();
    }
}
