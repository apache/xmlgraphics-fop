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

package org.apache.fop.afp.fonts;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

/**
 * An abstraction that wraps the encoding mechanism for encoding a Unicode character sequence into a
 * specified format.
 */
public abstract class CharactersetEncoder {

    private final CharsetEncoder encoder;

    private CharactersetEncoder(String encoding) {
        this.encoder = Charset.forName(encoding).newEncoder();
        this.encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    /**
     * Tells whether or not this encoder can encode the given character.
     *
     * @param c the character
     * @return true if, and only if, this encoder can encode the given character
     * @throws IllegalStateException - If an encoding operation is already in progress
     */
    final boolean canEncode(char c) {
        return encoder.canEncode(c);
    }

    /**
     * Encodes a character sequence to a byte array.
     *
     * @param chars the character sequence
     * @return the encoded character sequence
     * @throws CharacterCodingException if the encoding operation fails
     */
    final EncodedChars encode(CharSequence chars) throws CharacterCodingException {
        ByteBuffer bb;
        // encode method is not thread safe
        synchronized (encoder) {
            bb = encoder.encode(CharBuffer.wrap(chars));
        }
        if (bb.hasArray()) {
            return getEncodedChars(bb.array(), bb.limit());
        } else {
            bb.rewind();
            byte[] bytes = new byte[bb.remaining()];
            bb.get(bytes);
            return getEncodedChars(bytes, bytes.length);
        }
    }

    abstract EncodedChars getEncodedChars(byte[] byteArray, int length);

    /**
     * Encodes <code>chars</code> into a format specified by <code>encoding</code>.
     *
     * @param chars the character sequence
     * @param encoding the encoding type
     * @param isEDBCS if this encoding represents a double-byte character set
     * @return encoded data
     * @throws CharacterCodingException if encoding fails
     */
    public static EncodedChars encodeSBCS(CharSequence chars, String encoding, boolean isEDBCS)
            throws CharacterCodingException {
        CharactersetEncoder encoder = newInstance(encoding, isEDBCS);
        return encoder.encode(chars);
    }

    /**
     * The EBCDIC double byte encoder is used for encoding IBM format DBCS (double byte character
     * sets) with an EBCDIC code-page. Given a double byte EBCDIC code page and a Unicode character
     * sequence it will return its EBCDIC code-point, however, the "Shift In - Shift Out" operators
     * are removed from the sequence of bytes. These are only used in Line Data.
     */
    private static final class EbcdicDoubleByteEncoder extends CharactersetEncoder {
        private EbcdicDoubleByteEncoder(String encoding) {
            super(encoding);
        }
        @Override
        EncodedChars getEncodedChars(byte[] byteArray, int length) {
            if (byteArray[0] == 0x0E && byteArray[length - 1] == 0x0F) {
                return new EncodedChars(byteArray, 1, length - 2, true);
            }
            return new EncodedChars(byteArray, true);
        }
    }

    /**
     * The default encoder is used for encoding IBM format SBCS (single byte character sets), this
     * the primary format for most Latin character sets. This can also be used for Unicode double-
     * byte character sets (DBCS).
     */
    private static final class DefaultEncoder extends CharactersetEncoder {
        private DefaultEncoder(String encoding) {
            super(encoding);
        }

        @Override
        EncodedChars getEncodedChars(byte[] byteArray, int length) {
            return new EncodedChars(byteArray, false);
        }
    }

    /**
     * Returns an new instance of a {@link CharactersetEncoder}.
     *
     * @param encoding the encoding for the underlying character encoder
     * @param isEbcdicDBCS whether or not this wraps a double-byte EBCDIC code page.
     * @return the CharactersetEncoder
     */
    static CharactersetEncoder newInstance(String encoding, boolean isEbcdicDBCS) {
        if (isEbcdicDBCS) {
            return new EbcdicDoubleByteEncoder(encoding);
        } else {
            return new DefaultEncoder(encoding);
        }
    }

    /**
     * A container for encoded character bytes
     */
    public static class EncodedChars {

        private final byte[] bytes;
        private final int offset;
        private final int length;
        private final boolean isDBCS;

        private EncodedChars(byte[] bytes, int offset, int length, boolean isDBCS) {
            if (offset < 0 || length < 0 || offset + length > bytes.length) {
                throw new IllegalArgumentException();
            }
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
            this.isDBCS = isDBCS;
        }

        private EncodedChars(byte[] bytes, boolean isDBCS) {
            this(bytes, 0, bytes.length, isDBCS);
        }

        /**
         * write <code>length</code> bytes from <code>offset</code> to the output stream
         *
         * @param out output to write the bytes to
         * @param offset the offset where to write
         * @param length the length to write
         * @throws IOException if an I/O error occurs
         */
        public void writeTo(OutputStream out, int offset, int length) throws IOException {
            if (offset < 0 || length < 0 || offset + length > bytes.length) {
                throw new IllegalArgumentException();
            }
            out.write(bytes, this.offset + offset, length);
        }

        /**
         * The number of containing bytes.
         *
         * @return the length
         */
        public int getLength() {
            return length;
        }

        /**
         * Indicates whether or not the EncodedChars object wraps double byte characters.
         *
         * @return true if the wrapped characters are double byte (DBCSs)
         */
        public boolean isDBCS() {
            return isDBCS;
        }

        /**
         * The bytes
         *
         * @return the bytes
         */
        public byte[] getBytes() {
            // return copy just in case
            byte[] copy = new byte[bytes.length];
            System.arraycopy(bytes, 0, copy, 0, bytes.length);
            return copy;
        }
    }
}
