/*
 * $Id: ASCIIHexFilter.java,v 1.5 2003/03/07 08:25:46 jeremias Exp $
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
package org.apache.fop.pdf;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;

import org.apache.fop.render.ps.ASCIIHexOutputStream;

/**
 * ASCII Hex filter for PDF streams.
 * This filter converts a pdf stream to ASCII hex data.
 */
public class ASCIIHexFilter extends PDFFilter {
    private static final String ASCIIHEX_EOD = ">";

    /**
     * Get the name of this filter.
     *
     * @return the name of this filter for pdf
     */
    public String getName() {
        return "/ASCIIHexDecode";
    }

    /**
     * @see org.apache.fop.pdf.PDFFilter#isASCIIFilter()
     */
    public boolean isASCIIFilter() {
        return true;
    }
    
    /**
     * Get the decode params.
     *
     * @return always null
     */
    public String getDecodeParms() {
        return null;
    }

    /**
     * Encode the pdf stream using this filter.
     *
     * @param in the input stream to read the data from
     * @param out the output stream to write data to
     * @param length the length of data to read from the stream
     * @throws IOException if an error occurs reading or writing to
     *                     the streams
     */
    public void encode(InputStream in, OutputStream out, int length) throws IOException {
        Writer writer = new OutputStreamWriter(out);
        for (int i = 0; i < length; i++) {
            int val = (int)(in.read() & 0xFF);
            if (val < 16) {
                writer.write("0");
            }
            writer.write(Integer.toHexString(val));
        }
        writer.write(ASCIIHEX_EOD);
        writer.close();
    }

    /**
     * @see org.apache.fop.pdf.PDFFilter#applyFilter(OutputStream)
     */
    public OutputStream applyFilter(OutputStream out) throws IOException {
        return new ASCIIHexOutputStream(out);
    }

}
