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
package org.apache.fop.render.ps;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * This class applies a RunLengthEncode filter to the stream.
 *
 * @author   <a href="mailto:smwolke@geistig.com">Stephen Wolke</a>
 * @version  $Id$
 */

public class RunLengthEncodeOutputStream extends FilterOutputStream
            implements Finalizable {

    private static final int MAX_SEQUENCE_COUNT = 127;
    private static final int END_OF_DATA = 128;
    private static final int BYTE_MAX = 256;

    private static final int NOT_IDENTIFY_SEQUENCE = 0;
    private static final int START_SEQUENCE = 1;
    private static final int IN_SEQUENCE = 2;
    private static final int NOT_IN_SEQUENCE = 3;

    private int runCount = 0;
    private int isSequence = NOT_IDENTIFY_SEQUENCE;
    private byte[] runBuffer = new byte[MAX_SEQUENCE_COUNT + 1];


    /**
     * Constructor for the RunLengthEncode Filter.
     *
     * @param out  The OutputStream to write to
     */
    public RunLengthEncodeOutputStream(OutputStream out) {
        super(out);
    }


    /**
     * @see        java.io.OutputStream#write(int)
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             output stream has been closed.
     */
    public void write(byte b)
        throws java.io.IOException {
        runBuffer[runCount] = b;

        switch (runCount) {
        case 0:
            runCount = 0;
            isSequence = NOT_IDENTIFY_SEQUENCE;
            runCount++;
            break;
        case 1:
            if (runBuffer[runCount] != runBuffer[runCount - 1]) {
                isSequence = NOT_IN_SEQUENCE;
            }
            runCount++;
            break;
        case 2:
            if (runBuffer[runCount] != runBuffer[runCount - 1]) {
                isSequence = NOT_IN_SEQUENCE;
            } else {
                if (isSequence == NOT_IN_SEQUENCE) {
                    isSequence = START_SEQUENCE;
                } else {
                    isSequence = IN_SEQUENCE;
                }
            }
            runCount++;
            break;
        case MAX_SEQUENCE_COUNT:
            if (isSequence == IN_SEQUENCE) {
                out.write(BYTE_MAX - (MAX_SEQUENCE_COUNT - 1));
                out.write(runBuffer[runCount - 1]);
                runBuffer[0] = runBuffer[runCount];
                runCount = 1;
            } else {
                out.write(MAX_SEQUENCE_COUNT);
                out.write(runBuffer, 0, runCount + 1);
                runCount = 0;
            }
            isSequence = NOT_IDENTIFY_SEQUENCE;
            break;
        default:
            switch (isSequence) {
            case IN_SEQUENCE:
                if (runBuffer[runCount] != runBuffer[runCount - 1]) {
                    out.write(BYTE_MAX - (runCount - 1));
                    out.write(runBuffer[runCount - 1]);
                    runBuffer[0] = runBuffer[runCount];
                    runCount = 1;
                    isSequence = NOT_IDENTIFY_SEQUENCE;
                    break;
                }
                runCount++;
                break;
            case NOT_IN_SEQUENCE:
                if (runBuffer[runCount] == runBuffer[runCount - 1]) {
                    isSequence = START_SEQUENCE;
                }
                runCount++;
                break;
            case START_SEQUENCE:
                if (runBuffer[runCount] == runBuffer[runCount - 1]) {
                    out.write(runCount - 3);
                    out.write(runBuffer, 0, runCount - 2);
                    runBuffer[0] = runBuffer[runCount];
                    runBuffer[1] = runBuffer[runCount];
                    runBuffer[2] = runBuffer[runCount];
                    runCount = 3;
                    isSequence = IN_SEQUENCE;
                    break;
                } else {
                    isSequence = NOT_IN_SEQUENCE;
                    runCount++;
                    break;
                }
            }
        }
    }


    /**
     * @see        java.io.OutputStream#write(byte[])
     * @param      b   the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(byte[] b)
        throws IOException {

        for (int i = 0; i < b.length; i++) {
            this.write(b[i]);
        }
    }


    /**
     * @see        java.io.OutputStream#write(byte[], int, int)
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    public void write(byte[] b, int off, int len)
        throws IOException {

        for (int i = 0; i < len; i++) {
            this.write(b[off + i]);
        }
    }


    /**
     * Flushes the the stream and writes out the trailer, but, unlike close(),
     * without closing the stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void finalizeStream()
        throws IOException {
        switch (isSequence) {
        case IN_SEQUENCE:
            out.write(BYTE_MAX - (runCount - 1));
            out.write(runBuffer[runCount - 1]);
            break;
        default:
            out.write(runCount - 1);
            out.write(runBuffer, 0, runCount);
        }

        out.write(END_OF_DATA);

        flush();
        if (out instanceof Finalizable) {
            ((Finalizable) out).finalizeStream();
        }
    }


    /**
     * Closes the stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close()
        throws IOException {
        finalizeStream();
        super.close();
    }

}

