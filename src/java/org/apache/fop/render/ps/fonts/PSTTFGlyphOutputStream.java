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
 * Streams glyphs in accordance with the constraints of the PostScript file format.
 * Mainly, PostScript strings have a limited capacity and the font data may have to be
 * broken down into several strings; however, this must occur at well-defined places like
 * table or glyph boundaries. See also Adobe Technical Note #5012, <em>The Type 42 Font
 * Format Specification</em>.
 */
public class PSTTFGlyphOutputStream implements TTFGlyphOutputStream {

    /** Total number of bytes written so far. */
    private int byteCounter;

    private int lastStringBoundary;

    private PSTTFGenerator ttfGen;

    /**
     * Constructor
     * @param ttfGen PSTTFGenerator
     */
    public PSTTFGlyphOutputStream(PSTTFGenerator ttfGen) {
        this.ttfGen = ttfGen;
    }

    public void startGlyphStream() throws IOException {
        ttfGen.startString();
    }

    public void streamGlyph(byte[] glyphData, int offset, int size) throws IOException {
        if (size > PSTTFGenerator.MAX_BUFFER_SIZE) {
            throw new UnsupportedOperationException("The glyph is " + size
                    + " bytes. There may be an error in the font file.");
        }

        if (size + (byteCounter - lastStringBoundary) < PSTTFGenerator.MAX_BUFFER_SIZE) {
            ttfGen.streamBytes(glyphData, offset, size);
        } else {
            ttfGen.endString();
            lastStringBoundary = byteCounter;
            ttfGen.startString();
            ttfGen.streamBytes(glyphData, offset, size);
        }
        byteCounter += size;
    }

    public void endGlyphStream() throws IOException {
        ttfGen.endString();
    }

}
