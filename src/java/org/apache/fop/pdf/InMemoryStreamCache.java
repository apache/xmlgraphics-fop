/*
 * $Id: InMemoryStreamCache.java,v 1.3 2003/03/07 08:25:46 jeremias Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * StreamCache implementation that uses temporary files rather than heap.
 */
public class InMemoryStreamCache implements StreamCache {

    private int hintSize = -1;

    /**
     * The current output stream.
     */
    private ByteArrayOutputStream output;

    /**
     * Creates a new InMemoryStreamCache.
     */
    public InMemoryStreamCache() {
    }

    /**
     * Creates a new InMemoryStreamCache.
     * @param hintSize a hint about the approximate expected size of the buffer
     */
    public InMemoryStreamCache(int hintSize) {
        this.hintSize = hintSize;
    }

    /**
     * Get the current OutputStream. Do not store it - it may change
     * from call to call.
     * @throws IOException if there is an error getting the output stream
     * @return the output stream containing the data
     */
    public OutputStream getOutputStream() throws IOException {
        if (output == null) {
            if (this.hintSize <= 0) {
                output = new ByteArrayOutputStream(512);
            } else {
                output = new ByteArrayOutputStream(this.hintSize);
            }
        }
        return output;
    }

    /**
     * @see org.apache.fop.pdf.StreamCache#write(byte[])
     */
    public void write(byte[] data) throws IOException {
        getOutputStream().write(data);
    }
    
    /**
     * Filter the cache with the supplied PDFFilter.
     * @param filter the filter to apply
     * @throws IOException if an IO error occurs
     */
    public void applyFilter(PDFFilter filter) throws IOException {
        if (output == null) {
            return;
        }

        output.close();

        // make inputstream from copy of outputted bytes
        int size = getSize();
        ByteArrayInputStream input =
          new ByteArrayInputStream(output.toByteArray());

        // reset output
        output.reset();

        // run filter
        filter.encode(input, output, size);
        input.close();
        output.close();
    }

    /**
     * Outputs the cached bytes to the given stream.
     * @param out the output stream to write to
     * @return the number of bytes written
     * @throws IOException if there is an IO error writing to the output stream
     */
    public int outputContents(OutputStream out) throws IOException {
        if (output == null) {
            return 0;
        }

        output.writeTo(out);
        return output.size();
    }

    /**
     * Returns the current size of the stream.
     * @throws IOException if there is an error getting the size
     * @return the length of the stream
     */
    public int getSize() throws IOException {
        if (output == null) {
            return 0;
        } else {
            return output.size();
        }
    }

    /**
     * Clears and resets the cache.
     * @throws IOException if there is an error closing the stream
     */
    public void clear() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }
    }
}
