/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.util;

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

    private static final int MAX_SEQUENCE_COUNT    = 127;
    private static final int END_OF_DATA           = 128;
    private static final int BYTE_MAX              = 256;

    private static final int NOT_IDENTIFY_SEQUENCE = 0;
    private static final int START_SEQUENCE        = 1;
    private static final int IN_SEQUENCE           = 2;
    private static final int NOT_IN_SEQUENCE       = 3;

    private int runCount = 0;
    private int isSequence = NOT_IDENTIFY_SEQUENCE;
    private byte[] runBuffer = new byte[MAX_SEQUENCE_COUNT + 1];


    /** @see java.io.FilterOutputStream **/
    public RunLengthEncodeOutputStream(OutputStream out) {
        super(out);
    }


    /** @see java.io.FilterOutputStream **/
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


    /** @see java.io.FilterOutputStream **/
    public void write(byte[] b)
        throws IOException {

        for (int i = 0; i < b.length; i++) {
            this.write(b[i]);
        }
    }


    /** @see java.io.FilterOutputStream **/
    public void write(byte[] b, int off, int len)
        throws IOException {

        for (int i = 0; i < len; i++) {
            this.write(b[off + i]);
        }
    }


    /** @see Finalizable **/
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


    /** @see java.io.FilterOutputStream **/
    public void close()
        throws IOException {
        finalizeStream();
        super.close();
    }

}

