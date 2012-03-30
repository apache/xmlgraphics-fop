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

import java.io.IOException;

import org.apache.fop.fonts.truetype.TTFGlyphOutputStream;

/**
 * This class streams glyphs from the "glyf" table in a True Type font.
 */
public class PSTTFGlyphOutputStream implements TTFGlyphOutputStream {
    /** This counts the total number of bytes written that have been streamed. */
    private int byteCounter = 0;

    /** This is a place-holder for the offset of the last string boundary. */
    private int lastStringBoundary = 0;
    private PSTTFGenerator ttfGen;

    /**
     * Constructor
     * @param ttfGen PSTTFGenerator
     */
    public PSTTFGlyphOutputStream(PSTTFGenerator ttfGen) {
        this.ttfGen = ttfGen;
    }

    /** {@inheritDoc} */
    public void startGlyphStream()  throws IOException {
        ttfGen.startString();
    }

    /** {@inheritDoc} */
    public void streamGlyph(byte[] byteArray, int offset, int length) throws IOException {
        if (length > PSTTFGenerator.MAX_BUFFER_SIZE) {
            throw new UnsupportedOperationException("The glyph is " + length + " there may be an "
                    + "error in the font file.");
        }

        if (length + (byteCounter - lastStringBoundary) < PSTTFGenerator.MAX_BUFFER_SIZE) {
            ttfGen.streamBytes(byteArray, offset, length);
        } else {
            ttfGen.endString();
            lastStringBoundary = byteCounter;
            ttfGen.startString();
            ttfGen.streamBytes(byteArray, offset, length);
        }
        byteCounter += length;
    }

    /** {@inheritDoc} */
    public void endGlyphStream() throws IOException {
        ttfGen.endString();
    }
}
