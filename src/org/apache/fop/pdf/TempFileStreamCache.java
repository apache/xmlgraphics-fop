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
package org.apache.fop.pdf;

import org.apache.fop.util.StreamUtilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;

/**
 * StreamCache implementation that uses temporary files rather than heap.
 */
public class TempFileStreamCache extends StreamCache {

    /**
     * The current output stream.
     */
    private BufferedOutputStream output;

    /**
     * The temp file.
     */
    private File tempFile;

    /**
     * Creates a new TempFileStreamCache.
     *
     * @throws IOException if there is an IO error
     */
    public TempFileStreamCache() throws IOException {
        tempFile = File.createTempFile("org.apache.fop.pdf.StreamCache-",
                                       ".temp");
        tempFile.deleteOnExit();
    }

    /**
     * Get the current OutputStream. Do not store it - it may change
     * from call to call.
     *
     * @throws IOException if there is an IO error
     * @return the output stream for this cache
     */
    public OutputStream getOutputStream() throws IOException {
        if (output == null) {
            output = new BufferedOutputStream(
                       new FileOutputStream(tempFile));
        }
        return output;
    }

    /**
     * Filter the cache with the supplied PDFFilter.
     *
     * @param filter the filter to apply
     * @throws IOException if there is an IO error
     */
    public void applyFilter(PDFFilter filter) throws IOException {
        if (output == null) {
            return;
        }

        output.close();
        output = null;

        // need a place to put results
        File newTempFile =
          File.createTempFile("org.apache.fop.pdf.StreamCache-",
                              ".temp");
        newTempFile.deleteOnExit();

        // filter may not be buffered
        BufferedInputStream input =
          new BufferedInputStream(new FileInputStream(tempFile));
        BufferedOutputStream output = new BufferedOutputStream(
                                        new FileOutputStream(newTempFile));
        filter.encode(input, output, (int) tempFile.length());
        input.close();
        output.close();
        tempFile.delete();
        tempFile = newTempFile;
    }

    /**
     * Outputs the cached bytes to the given stream.
     *
     * @param stream the output stream to write to
     * @throws IOException if there is an IO error
     */
    public void outputStreamData(OutputStream stream) throws IOException {
        if (output == null) {
            return;
        }

        output.close();
        output = null;

        // don't need a buffer because streamCopy is buffered
        FileInputStream input = new FileInputStream(tempFile);
        StreamUtilities.streamCopy(input, output);
        input.close();
    }

    /**
     * Returns the current size of the stream.
     *
     * @throws IOException if there is an IO error
     * @return the size of the cache
     */
    public int getSize() throws IOException {
        if (output != null) {
            output.flush();
        }
        return (int) tempFile.length();
    }

    /**
     * Closes the cache and frees resources.
     *
     * @throws IOException if there is an IO error
     */
    public void close() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    /**
     * Clears and resets the cache.
     *
     * @throws IOException if there is an IO error
     */
    public void reset() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}
