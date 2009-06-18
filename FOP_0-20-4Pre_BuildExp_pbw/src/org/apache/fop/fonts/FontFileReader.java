/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;
import java.io.FileInputStream;
import java.io.InputStream;
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
     * Initialisez class and reads stream. Init does not close stream
     * @param stream InputStream to read from
     * @param start initial size av array to read to
     * @param inc if initial size isn't enough, create
     * new array with size + inc
     */
    private void init(InputStream stream, int start,
                      int inc) throws java.io.IOException {
        fsize = 0;
        current = 0;

        file = new byte[start];

        int l = stream.read(file, 0, start);
        fsize += l;

        if (l == start) {
            // More to read - needs to extend
            byte[] tmpbuf;

            while (l > 0) {
                tmpbuf = new byte[file.length + inc];
                System.arraycopy(file, 0, tmpbuf, 0, file.length);
                l = stream.read(tmpbuf, file.length, inc);
                fsize += l;
                file = tmpbuf;

                if (l < inc)    // whole file read. No need to loop again


                    l = 0;
            }
        }
    }

    /**
     * Constructor
     * @param fileName filename to read
     */
    public FontFileReader(String fileName) throws java.io.IOException {

        // Get estimates for file size and increment
        File f = new File(fileName);
        FileInputStream ins = new FileInputStream(fileName);
        init(ins, (int)(f.length() + 1), (int)(f.length() / 10));
        ins.close();
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
        return new String(tmp, "ISO-8859-1");
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
