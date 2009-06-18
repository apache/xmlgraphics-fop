/*
 * $Id$
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
package org.apache.fop.fonts;

import java.io.InputStream;
import java.io.File;
import java.io.IOException;

import org.apache.fop.tools.IOUtil;

/**
 * Reads a file into an array and
 * provides file like functions for array access.
 */
public class FontFileReader {
    private int fsize;      // file size
    private int current;    // current position in file
    private byte[] file;

    /**
     * Initializes class and reads stream. Init does not close stream
     * @param in InputStream to read from
     * new array with size + inc
     */
    private void init(InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
        try {
            this.file = IOUtil.toByteArray(in, 50000);
            this.fsize = this.file.length;
            this.current = 0;
        } finally {
            bout.close();
        }
    }


    /**
     * Constructor
     * @param fileName filename to read
     */
    public FontFileReader(String fileName) throws java.io.IOException {
        File f = new File(fileName);
        InputStream in = new java.io.FileInputStream(f);
        try {
            init(in);
        } finally {
            in.close();
        }
    }


    /**
     * Constructor
     * @param in InputStream to read from
     */
    public FontFileReader(InputStream in) throws java.io.IOException {
        init(in);
    }


    /**
     * Set current file position to offset
     */
    public void seek_set(long offset) throws IOException {
        if (offset > fsize || offset < 0)
            throw new java.io.EOFException("Reached EOF, file size=" + fsize
                                           + " offset=" + offset);
        current = (int)offset;
    }

    /**
     * Set current file position to offset
     */
    public void seek_add(long add) throws IOException {
        seek_set(current + add);
    }

    public void skip(long add) throws IOException {
        seek_add(add);
    }

    /**
     * return current file position
     */
    public int getCurrentPos() {
        return current;
    }

    public int getFileSize() {
        return fsize;
    }


    /**
     * Read 1 byte, throws EOFException on end of file
     */
    public byte read() throws IOException {
        if (current > fsize)
            throw new java.io.EOFException("Reached EOF, file size=" + fsize);

        byte ret = file[current++];
        return ret;
    }



    /**
     * Read 1 signed byte from InputStream
     */
    public final byte readTTFByte() throws IOException {
        return read();
    }

    /**
     * Read 1 unsigned byte from InputStream
     */
    public final int readTTFUByte() throws IOException {
        byte buf = read();

        if (buf < 0)
            return (int)(256 + buf);
        else
            return (int)buf;
    }

    /**
     * Read 2 bytes signed from InputStream
     */
    public final short readTTFShort() throws IOException {
        int ret = (readTTFUByte() << 8) + readTTFUByte();
        short sret = (short)ret;

        return sret;
    }

    /**
     * Read 2 bytes unsigned from InputStream
     */
    public final int readTTFUShort() throws IOException {
        int ret = (readTTFUByte() << 8) + readTTFUByte();

        return (int)ret;
    }

    /**
     * Write a USHort at a given position
     */
    public final void writeTTFUShort(int pos, int val) throws IOException {
        if ((pos + 2) > fsize)
            throw new java.io.EOFException("Reached EOF");
        byte b1 = (byte)((val >> 8) & 0xff);
        byte b2 = (byte)(val & 0xff);
        file[pos] = b1;
        file[pos + 1] = b2;
    }

    /**
     * Read 2 bytes signed from InputStream at position pos
     * without changing current position
     */
    public final short readTTFShort(long pos) throws IOException {
        long cp = getCurrentPos();
        seek_set(pos);
        short ret = readTTFShort();
        seek_set(cp);
        return ret;
    }

    /**
     * Read 2 bytes unsigned from InputStream at position pos
     * without changing current position
     */
    public final int readTTFUShort(long pos) throws IOException {
        long cp = getCurrentPos();
        seek_set(pos);
        int ret = readTTFUShort();
        seek_set(cp);
        return ret;
    }

    /**
     * Read 4 bytes from InputStream
     */
    public final int readTTFLong() throws IOException {
        long ret = readTTFUByte();    // << 8;
        ret = (ret << 8) + readTTFUByte();
        ret = (ret << 8) + readTTFUByte();
        ret = (ret << 8) + readTTFUByte();

        return (int)ret;
    }

    /**
     * Read 4 bytes from InputStream
     */
    public final long readTTFULong() throws IOException {
        long ret = readTTFUByte();
        ret = (ret << 8) + readTTFUByte();
        ret = (ret << 8) + readTTFUByte();
        ret = (ret << 8) + readTTFUByte();

        return ret;
    }

    /**
     * Read a 0 terminatet ISO-8859-1 string
     */
    public final String readTTFString() throws IOException {
        int i = current;
        while (file[i++] != 0) {
            if (i > fsize)
                throw new java.io.EOFException("Reached EOF, file size="
                                               + fsize);
        }

        byte[] tmp = new byte[i - current];
        System.arraycopy(file, current, tmp, 0, i - current);
        return new String(tmp, "ISO-8859-1");
    }


    /**
     * Read an ISO-8859-1 string of len bytes
     */
    public final String readTTFString(int len) throws IOException {
        if ((len + current) > fsize)
            throw new java.io.EOFException("Reached EOF, file size=" + fsize);

        byte[] tmp = new byte[len];
        System.arraycopy(file, current, tmp, 0, len);
        current += len;
        final String encoding;
        if ((tmp.length > 0) && (tmp[0] == 0)) {
            encoding = "UnicodeBig";
        } else {
            encoding = "ISO-8859-1";
        }
        return new String(tmp, encoding);
    }

    /**
     * Return a copy of the internal array
     * @throws IOException if out of bounds
     */
    public byte[] getBytes(int offset,
                           int length) throws java.io.IOException {
        if ((offset + length) > fsize)
            throw new java.io.IOException("Reached EOF");

        byte[] ret = new byte[length];
        System.arraycopy(file, offset, ret, 0, length);
        return ret;
    }


}
