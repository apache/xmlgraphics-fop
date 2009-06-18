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

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * This class applies a ASCII Hex encoding to the stream.
 *
 * @author Jeremias Maerki
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


