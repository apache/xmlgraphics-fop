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

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * This class applies a ASCII Hex encoding to the stream.
 *
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id$
 */
public class ASCIIHexOutputStream extends FilterOutputStream
        implements Finalizable {

    private static final int EOL   = 0x0A; //"\n"
    private static final int EOD   = 0x3E; //">"
    private static final int ZERO  = 0x30; //"0"
    private static final int NINE  = 0x39; //"9"
    private static final int A     = 0x41; //"A"
    private static final int ADIFF = A - NINE - 1;

    private int posinline = 0;


    /** @see java.io.FilterOutputStream **/
    public ASCIIHexOutputStream(OutputStream out) {
        super(out);
    }


    /** @see java.io.FilterOutputStream **/
    public void write(int b) throws IOException {
        b &= 0xFF;

        int digit1 = ((b & 0xF0) >> 4) + ZERO;
        if (digit1 > NINE) {
            digit1 += ADIFF;
        }
        out.write(digit1);

        int digit2 = (b & 0x0F) + ZERO;
        if (digit2 > NINE) {
            digit2 += ADIFF;
        }
        out.write(digit2);

        posinline++;
        checkLineWrap();
    }


    private void checkLineWrap() throws IOException {
        //Maximum line length is 80 characters
        if (posinline >= 40) {
            out.write(EOL);
            posinline = 0;
        }
    }


    /** @see Finalizable **/
    public void finalizeStream() throws IOException {
        checkLineWrap();
        //Write closing character ">"
        super.write(EOD);

        flush();
        if (out instanceof Finalizable) {
            ((Finalizable) out).finalizeStream();
        }
    }


    /** @see java.io.FilterOutputStream **/
    public void close() throws IOException {
        finalizeStream();
        super.close();
    }


}


