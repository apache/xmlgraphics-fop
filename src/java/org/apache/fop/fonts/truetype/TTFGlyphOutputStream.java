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

package org.apache.fop.fonts.truetype;

import java.io.IOException;

/**
 * This is an interface for streaming individual glyphs from the glyf table in a True Type font.
 */
public interface TTFGlyphOutputStream {
    /**
     * Begins the streaming of glyphs.
     * @throws IOException file write exception
     */
    void startGlyphStream() throws IOException;

    /**
     * Streams an individual glyph at offset from a byte array.
     * @param byteArray byte[] the font byte array.
     * @param offset int the starting position to stream from.
     * @param length int the number of bytes to stream.
     * @throws IOException file write exception.
     */
    void streamGlyph(byte[] byteArray, int offset, int length) throws IOException;

    /**
     * Ends the streaming of glyphs.
     * @throws IOException file write exception.
     */
    void endGlyphStream() throws IOException;
}
