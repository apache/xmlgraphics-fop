/*
 * $Id: FontFileReader.java,v 1.2 2003/03/06 17:43:06 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.fonts.truetype;

import java.io.InputStream;
import java.io.File;
import java.io.IOException;

import org.apache.fop.util.StreamUtilities;

/**
 * Reads a TrueType font file into a byte array and
 * provides file like functions for array access.
 */
public class FontFileReader {

    private int fsize;      // file size
    private int current;    // current position in file
    private byte[] file;

    /**
     * Initializes class and reads stream. Init does not close stream.
     *
     * @param in InputStream to read from new array with size + inc
     * @throws IOException In case of an I/O problem
     */
    private void init(InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
        try {
            StreamUtilities.streamCopy(in, bout);
            this.file = bout.toByteArray();
            this.fsize = this.file.length;
            this.current = 0;
        } finally {
            bout.close();
        }
    }

    /**
     * Constructor
     *
     * @param fileName filename to read
     * @throws IOException In case of an I/O problem
     */
    public FontFileReader(String fileName) throws IOException {
        final File f = new File(fileName);
        InputStream in = new java.io.FileInputStream(f);
        try {
            init(in);
        } finally {
            in.close();
        }
    }


    /**
     * Constructor
     *
     * @param in InputStream to read from
     * @throws IOException In case of an I/O problem
     */
    public FontFileReader(InputStream in) throws IOException {
        init(in);
    }


    /**
     * Set current file position to offset
     *
     * @param offset The new offset to set
     * @throws IOException In case of an I/O problem
     */
    public void seekSet(long offset) throws IOException {
        if (offset > fsize || offset < 0) {
            throw new java.io.EOFException("Reached EOF, file size=" + fsize
                                           + " offset=" + offset);
        }
        current = (int)offset;
    }

    /**
     * Set current file position to offset
     *
     * @param add The number of bytes to advance
     * @throws IOException In case of an I/O problem
     */
    public void seekAdd(long add) throws IOException {
        seekSet(current + add);
    }

    /**
     * Skip a given number of bytes.
     *
     * @param add The number of bytes to advance
     * @throws IOException In case of an I/O problem
     */
    public void skip(long add) throws IOException {
        seekAdd(add);
    }

    /**
     * Returns current file position.
     *
     * @return int The current position.
     */
    public int getCurrentPos() {
        return current;
    }

    /**
     * Returns the size of the file.
     *
     * @return int The filesize
     */
    public int getFileSize() {
        return fsize;
    }

    /**
     * Read 1 byte.
     *
     * @return One byte
     * @throws IOException If EOF is reached
     */
    public byte read() throws IOException {
        if (current > fsize) {
            throw new java.io.EOFException("Reached EOF, file size=" + fsize);
        }

        final byte ret = file[current++];
        return ret;
    }

    /**
     * Read 1 signed byte.
     *
     * @return One byte
     * @throws IOException If EOF is reached
     */
    public final byte readTTFByte() throws IOException {
        return read();
    }

    /**
     * Read 1 unsigned byte.
     *
     * @return One unsigned byte
     * @throws IOException If EOF is reached
     */
    public final int readTTFUByte() throws IOException {
        final byte buf = read();

        if (buf < 0) {
            return (int)(256 + buf);
        } else {
            return (int)buf;
        }
    }

    /**
     * Read 2 bytes signed.
     *
     * @return One signed short
     * @throws IOException If EOF is reached
     */
    public final short readTTFShort() throws IOException {
        final int ret = (readTTFUByte() << 8) + readTTFUByte();
        final short sret = (short)ret;
        return sret;
    }

    /**
     * Read 2 bytes unsigned.
     *
     * @return One unsigned short
     * @throws IOException If EOF is reached
     */
    public final int readTTFUShort() throws IOException {
        final int ret = (readTTFUByte() << 8) + readTTFUByte();
        return (int)ret;
    }

    /**
     * Write a USHort at a given position.
     *
     * @param pos The absolute position to write to
     * @param val The value to write
     * @throws IOException If EOF is reached
     */
    public final void writeTTFUShort(int pos, int val) throws IOException {
        if ((pos + 2) > fsize) {
            throw new java.io.EOFException("Reached EOF");
        }
        final byte b1 = (byte)((val >> 8) & 0xff);
        final byte b2 = (byte)(val & 0xff);
        file[pos] = b1;
        file[pos + 1] = b2;
    }

    /**
     * Read 2 bytes signed at position pos without changing current position.
     *
     * @param pos The absolute position to read from
     * @return One signed short
     * @throws IOException If EOF is reached
     */
    public final short readTTFShort(long pos) throws IOException {
        final long cp = getCurrentPos();
        seekSet(pos);
        final short ret = readTTFShort();
        seekSet(cp);
        return ret;
    }

    /**
     * Read 2 bytes unsigned at position pos without changing current position.
     *
     * @param pos The absolute position to read from
     * @return One unsigned short
     * @throws IOException If EOF is reached
     */
    public final int readTTFUShort(long pos) throws IOException {
        long cp = getCurrentPos();
        seekSet(pos);
        int ret = readTTFUShort();
        seekSet(cp);
        return ret;
    }

    /**
     * Read 4 bytes.
     *
     * @return One signed integer
     * @throws IOException If EOF is reached
     */
    public final int readTTFLong() throws IOException {
        long ret = readTTFUByte();    // << 8;
        ret = (ret << 8) + readTTFUByte();
        ret = (ret << 8) + readTTFUByte();
        ret = (ret << 8) + readTTFUByte();

        return (int)ret;
    }

    /**
     * Read 4 bytes.
     *
     * @return One unsigned integer
     * @throws IOException If EOF is reached
     */
    public final long readTTFULong() throws IOException {
        long ret = readTTFUByte();
        ret = (ret << 8) + readTTFUByte();
        ret = (ret << 8) + readTTFUByte();
        ret = (ret << 8) + readTTFUByte();

        return ret;
    }

    /**
     * Read a NUL terminated ISO-8859-1 string.
     *
     * @return A String
     * @throws IOException If EOF is reached
     */
    public final String readTTFString() throws IOException {
        int i = current;
        while (file[i++] != 0) {
            if (i > fsize) {
                throw new java.io.EOFException("Reached EOF, file size="
                                               + fsize);
            }
        }

        byte[] tmp = new byte[i - current];
        System.arraycopy(file, current, tmp, 0, i - current);
        return new String(tmp, "ISO-8859-1");
    }


    /**
     * Read an ISO-8859-1 string of len bytes.
     *
     * @param len The length of the string to read
     * @return A String
     * @throws IOException If EOF is reached
     */
    public final String readTTFString(int len) throws IOException {
        if ((len + current) > fsize) {
            throw new java.io.EOFException("Reached EOF, file size=" + fsize);
        }

        byte[] tmp = new byte[len];
        System.arraycopy(file, current, tmp, 0, len);
        current += len;
        return new String(tmp, "ISO-8859-1");
    }

    /**
     * Return a copy of the internal array
     *
     * @param offset The absolute offset to start reading from
     * @param length The number of bytes to read
     * @return An array of bytes
     * @throws IOException if out of bounds
     */
    public byte[] getBytes(int offset,
                           int length) throws IOException {
        if ((offset + length) > fsize) {
            throw new java.io.IOException("Reached EOF");
        }

        byte[] ret = new byte[length];
        System.arraycopy(file, offset, ret, 0, length);
        return ret;
    }


}