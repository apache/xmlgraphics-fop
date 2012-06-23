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

import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.util.io.ASCIIHexOutputStream;

/**
 * This is a wrapper for {@link PSGenerator} that contains some members specific for streaming
 * TrueType fonts to a PostScript document.
 */
public class PSTTFGenerator {
    private PSGenerator gen;
    private ASCIIHexOutputStream hexOut;

    /**
     * The buffer is used to store the font file in an array of hex-encoded strings. Strings are
     * limited to 65535 characters, string will start with a newline, 2 characters are needed to
     * hex-encode each byte.
     */
    public static final int MAX_BUFFER_SIZE = 32764;

    /**
     * Creates a new instance wrapping the given generator.
     * @param gen the PSGenerator to wrap
     */
    public PSTTFGenerator(PSGenerator gen) {
        this.gen = gen;
        hexOut = new ASCIIHexOutputStream(gen.getOutputStream());
    }

    /**
     * Writes the '&lt;' character that starts a string.
     */
    public void startString() throws IOException {
        // We need to reset the streamer so that it starts a new line in the PS document
        hexOut = new ASCIIHexOutputStream(gen.getOutputStream());
        gen.writeln("<");
    }

    /**
     * Writes the given string to the output.
     * @param cmd a string
     */
    public void write(String cmd) throws IOException {
        gen.write(cmd);
    }

    /**
     * Writes the given string to the output, followed by a newline.
     * @param cmd a string
     */
    public void writeln(String cmd) throws IOException {
        gen.writeln(cmd);
    }

    /**
     * Writes bytes from the given byte array to the output.
     *
     * @param byteArray byte[] a byte array
     * @param offset the position in the byte array where the streaming must start
     * @param length the number of bytes to stream. This MUST be less than
     * {@link #MAX_BUFFER_SIZE} - 1 since strings are suffixed by '00' (see Section 4.2 of
     * Adobe Technical Note #5012, <em>The Type 42 Font Format Specification</em>.).
     */
    public void streamBytes(byte[] byteArray, int offset, int length) throws IOException {
        if (length > MAX_BUFFER_SIZE) {
            throw new UnsupportedOperationException("Attempting to write a string to a PostScript"
                    + " file that is greater than the buffer size.");
        }
        hexOut.write(byteArray, offset, length);
    }

    /**
     * Finishes writing a string by appending '00' and '>' to the end.
     */
    public void endString() throws IOException {
        /* Appends a '00' to the end of the string as specified in the spec */
        gen.write("00\n> ");
    }

}
