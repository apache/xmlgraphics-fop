/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render.ps;

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * This class applies a ASCII Hex encoding to the stream.
 *
 * @author <a href="mailto:jeremias.maerki@outline.ch">Jeremias Maerki</a>
 * @version $Id$
 */
public class ASCIIHexOutputStream extends FilterOutputStream {

    private static final int EOL   = 0x0A; //"\n"
    private static final int EOD   = 0x3E; //">"
    private static final int ZERO  = 0x30; //"0"
    private static final int NINE  = 0x39; //"9"
    private static final int A     = 0x41; //"A"
    private static final int ADIFF = A - NINE -1;

    private int posinline = 0;


    public ASCIIHexOutputStream(OutputStream out) {
        super(out);
    }


    public void write(int b) throws IOException {
        b &= 0xFF;

        int digit1 = ((b & 0xF0) >> 4) + ZERO;
        if (digit1 > NINE) digit1 += ADIFF;
        out.write(digit1);

        int digit2 = (b & 0x0F) + ZERO;
        if (digit2 > NINE) digit2 += ADIFF;
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


    public void finalizeStream() throws IOException {
        checkLineWrap();
        //Write closing character ">"
        super.write(EOD);

        flush();
        if (out instanceof Finalizable) {
            ((Finalizable)out).finalizeStream();
        }
    }


    public void close() throws IOException {
        finalizeStream();
        super.close();
    }


}


