/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
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

    private final static int MAX_SEQUENCE_COUNT = 127;
    private final static int END_OF_DATA = 128;
    private final static int BYTE_MAX = 256;

    private final static int NOT_IDENTIFY_SEQUENCE = 0;
    private final static int START_SEQUENCE = 1;
    private final static int IN_SEQUENCE = 2;
    private final static int NOT_IN_SEQUENCE = 3;

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

