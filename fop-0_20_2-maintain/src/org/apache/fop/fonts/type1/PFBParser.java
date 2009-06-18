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
package org.apache.fop.fonts.type1;

import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;

//FOP
import org.apache.fop.tools.IOUtil;

/**
 * This class represents a parser for Adobe Type 1 PFB files.
 *
 * @see PFBData
 */
public class PFBParser {

    private static final byte[] CURRENTFILE_EEXEC;
    private static final byte[] CLEARTOMARK;

    static {
        try {
            CURRENTFILE_EEXEC = "currentfile eexec".getBytes("US-ASCII");
            CLEARTOMARK = "cleartomark".getBytes("US-ASCII");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Incompatible VM. It doesn't support the US-ASCII encoding");
        }
    }


    /**
     * Parses a PFB file into a PFBData object.
     * @param url URL to load the PFB file from
     * @return PFBData memory representation of the font
     * @throws IOException In case of an I/O problem
     */
    public PFBData parsePFB(java.net.URL url) throws IOException {
        InputStream in = url.openStream();
        try {
            return parsePFB(in);
        } finally {
            in.close();
        }
    }


    /**
     * Parses a PFB file into a PFBData object.
     * @param pfbFile File to load the PFB file from
     * @return PFBData memory representation of the font
     * @throws IOException In case of an I/O problem
     */
    public PFBData parsePFB(java.io.File pfbFile) throws IOException {
        InputStream in = new java.io.FileInputStream(pfbFile);
        try {
            return parsePFB(in);
        } finally {
            in.close();
        }
    }


    /**
     * Parses a PFB file into a PFBData object.
     * @param in InputStream to load the PFB file from
     * @return PFBData memory representation of the font
     * @throws IOException In case of an I/O problem
     */
    public PFBData parsePFB(InputStream in) throws IOException {
        PFBData pfb = new PFBData();
        BufferedInputStream bin = new BufferedInputStream(in);
        DataInputStream din = new DataInputStream(bin);
        din.mark(32);
        int firstByte = din.readUnsignedByte();
        din.reset();
        if (firstByte == 128) {
            pfb.setPFBFormat(PFBData.PFB_PC);
            parsePCFormat(pfb, din);
        } else {
            pfb.setPFBFormat(PFBData.PFB_RAW);
            parseRAWFormat(pfb, bin);
        }
        return pfb;
    }


    private static int swapInteger(final int value) {
        return (((value >> 0) & 0xff) << 24)
             + (((value >> 8) & 0xff) << 16)
             + (((value >> 16) & 0xff) << 8)
             + (((value >> 24) & 0xff) << 0);
    }


    private void parsePCFormat(PFBData pfb, DataInputStream din) throws IOException {
        int segmentHead;
        int segmentType;
        int bytesRead;

        //Read first segment
        segmentHead = din.readUnsignedByte();
        if (segmentHead != 128) {
            throw new IOException("Invalid file format. Expected ASCII 80hex");
        }
        segmentType = din.readUnsignedByte(); //Read
        int len1 = swapInteger(din.readInt());
        byte[] headerSegment = new byte[len1];
        din.readFully(headerSegment);
        pfb.setHeaderSegment(headerSegment);

        //Read second segment
        segmentHead = din.readUnsignedByte();
        if (segmentHead != 128) {
            throw new IOException("Invalid file format. Expected ASCII 80hex");
        }
        segmentType = din.readUnsignedByte();
        int len2 = swapInteger(din.readInt());
        byte[] encryptedSegment = new byte[len2];
        din.readFully(encryptedSegment);
        pfb.setEncryptedSegment(encryptedSegment);

        //Read third segment
        segmentHead = din.readUnsignedByte();
        if (segmentHead != 128) {
            throw new IOException("Invalid file format. Expected ASCII 80hex");
        }
        segmentType = din.readUnsignedByte();
        int len3 = swapInteger(din.readInt());
        byte[] trailerSegment = new byte[len3];
        din.readFully(trailerSegment);
        pfb.setTrailerSegment(trailerSegment);

        //Read EOF indicator
        segmentHead = din.readUnsignedByte();
        if (segmentHead != 128) {
            throw new IOException("Invalid file format. Expected ASCII 80hex");
        }
        segmentType = din.readUnsignedByte();
        if (segmentType != 3) {
            throw new IOException("Expected segment type 3, but found: " + segmentType);
        }
    }


    private static final boolean byteCmp(byte[] src, int srcOffset, byte[] cmp) {
        for (int i = 0; i < cmp.length; i++) {
            // System.out.println("Compare: " + src[srcOffset + i] + " " + cmp[i]);
            if (src[srcOffset + i] != cmp[i]) {
                return false;
            }
        }
        return true;
    }

    private void calcLengths(PFBData pfb, byte[] originalData) {
        // Calculate length 1 and 3
        // System.out.println ("Checking font, size = "+originalData.length);

        // Length1 is the size of the initial ascii portion
        // search for "currentfile eexec"
        // Get the first binary number and search backwards for "eexec"
        int len1 = 30;

        // System.out.println("Length1="+len1);
        while (!byteCmp(originalData, len1 - CURRENTFILE_EEXEC.length, CURRENTFILE_EEXEC)) {
            len1++;
        }

        // Skip newline
        len1++;

        // Length3 is length of the last portion of the file
        int len3 = 0;
        len3 -= CLEARTOMARK.length;
        while (!byteCmp(originalData, originalData.length + len3, CLEARTOMARK)) {
            len3--;
            // System.out.println("Len3="+len3);
        }
        len3 = -len3;
        len3++;
        // Eat 512 zeroes
        int numZeroes = 0;
        byte[] ws1 = new byte[]{0x0D}; //CR
        byte[] ws2 = new byte[]{0x0A}; //LF
        byte[] ws3 = new byte[]{0x30}; //"0"
        while ((originalData[originalData.length - len3] == ws1[0]
                || originalData[originalData.length - len3] == ws2[0]
                || originalData[originalData.length - len3] == ws3[0])
                && numZeroes < 512) {
            len3++;
            if (originalData[originalData.length - len3] == ws3[0]) {
                numZeroes++;
            }
        }
        // System.out.println("Length3="+len3);

        //Create the 3 segments
        byte[] buffer = new byte[len1];
        System.arraycopy(originalData, 0, buffer, 0, len1);
        pfb.setHeaderSegment(buffer);

        int len2 = originalData.length - len3 - len1;
        buffer = new byte[len2];
        System.arraycopy(originalData, len1, buffer, 0, len2);
        pfb.setEncryptedSegment(buffer);

        buffer = new byte[len3];
        System.arraycopy(originalData, len1 + len2, buffer, 0, len3);
        pfb.setTrailerSegment(buffer);
    }

    private void parseRAWFormat(PFBData pfb, BufferedInputStream bin)
            throws IOException {
        calcLengths(pfb, IOUtil.toByteArray(bin, 32768));
    }

}