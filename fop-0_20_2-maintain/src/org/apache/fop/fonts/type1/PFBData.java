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

import java.io.OutputStream;
import java.io.IOException;

/**
 * Class that represents the contents of a PFB file.
 * 
 * @see PFBParser
 */
public class PFBData {

    /**
     * Raw format, no special file structure
     */
    public static final int PFB_RAW = 0;
    
    /**
     * PC format
     */
    public static final int PFB_PC  = 1;
    
    /**
     * MAC Format (unsupported, yet)
     */
    public static final int PFB_MAC = 2;

    private int pfbFormat; //One of the PFB_* constants
    private byte[] headerSegment;
    private byte[] encryptedSegment;
    private byte[] trailerSegment;


    /**
     * Sets the PFB format the font was loaded with.
     * @param format one of the PFB_* constants
     */
    public void setPFBFormat(int format) {
        switch (format) {
            case PFB_RAW:
            case PFB_PC:
                this.pfbFormat = format;
                break;
            case PFB_MAC:
                throw new UnsupportedOperationException("Mac format is not yet implemented");
            default:
                throw new IllegalArgumentException("Invalid value for PFB format: " + format);
        }
    }


    /**
     * Returns the format the font was loaded with.
     * @return int one of the PFB_* constants
     */
    public int getPFBFormat() {
        return this.pfbFormat;
    }

    /**
     * Sets the header segment of the font file.
     * @param headerSeg the header segment
     */
    public void setHeaderSegment(byte[] headerSeg) {
        this.headerSegment = headerSeg;
    }

    /**
     * Sets the encrypted segment of the font file.
     * @param encryptedSeg the encrypted segment
     */
    public void setEncryptedSegment(byte[] encryptedSeg) {
        this.encryptedSegment = encryptedSeg;
    }

    /**
     * Sets the trailer segment of the font file.
     * @param trailerSeg the trailer segment
     */
    public void setTrailerSegment(byte[] trailerSeg) {
        this.trailerSegment = trailerSeg;
    }

    /**
     * Returns the full length of the raw font file.
     * @return int the raw file length
     */
    public int getLength() {
        return getLength1() + getLength2() + getLength3();
    }


    /**
     * Returns the Length1 (length of the header segment).
     * @return int Length1
     */
    public int getLength1() {
        return this.headerSegment.length;
    }


    /**
     * Returns the Length2 (length of the encrypted segment).
     * @return int Length2
     */
    public int getLength2() {
        return this.encryptedSegment.length;
    }


    /**
     * Returns the Length3 (length of the trailer segment).
     * @return int Length3
     */
    public int getLength3() {
        return this.trailerSegment.length;
    }


    /**
     * Writes the PFB file in raw format to an OutputStream.
     * @param out the OutputStream to write to
     * @throws IOException In case of an I/O problem
     */
    public void outputAllParts(OutputStream out) throws IOException {
        out.write(this.headerSegment);
        out.write(this.encryptedSegment);
        out.write(this.trailerSegment);
    }


    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "PFB: format=" + getPFBFormat()
                + " len1=" + getLength1()
                + " len2=" + getLength2()
                + " len3=" + getLength3();
    }

}