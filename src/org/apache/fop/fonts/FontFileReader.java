/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Reads a file into an array and
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
            copyStream(in, bout);
            this.file = bout.toByteArray();
            this.fsize = this.file.length;
            this.current = 0;
        } finally {
            bout.close();
        }
    }

    /**@todo Use method from Avalon Excalibur IO or Jakarta Commons IO*/
    private void copyStream(InputStream in, OutputStream out) throws IOException {
        final int bufferSize = 2048;
        final byte[] buf = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = in.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
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