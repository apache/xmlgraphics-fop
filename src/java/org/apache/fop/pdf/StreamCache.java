/*
 * $Id: StreamCache.java,v 1.3 2003/03/07 08:25:47 jeremias Exp $
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
import java.io.IOException;

/**
 * class used to store the bytes for a PDFStream. It's actually a generic
 * cached byte array, along with a factory that returns either an
 * in-memory or tempfile based implementation based on the global
 * cacheToFile setting.
 */
public abstract class StreamCache {

    /**
     * Global setting; controls whether to use tempfiles or not.
     */
    private static boolean cacheToFile = false;

    /**
     * Change the global cacheToFile flag.
     *
     * @param tizit true if cache to file
     */
    public static void setCacheToFile(boolean tizit) {
        cacheToFile = tizit;
    }

    /**
     * Get the value of the global cacheToFile flag.
     *
     * @return the current cache to file flag
     */
    public static boolean getCacheToFile() {
        return cacheToFile;
    }

    /**
     * Get the correct implementation (based on cacheToFile) of
     * StreamCache.
     *
     * @throws IOException if there is an IO error
     * @return a new StreamCache for caching streams
     */
    public static StreamCache createStreamCache() throws IOException {
        if (cacheToFile) {
            return new TempFileStreamCache();
        } else {
            return new InMemoryStreamCache();
        }
    }

    /**
     * Get the current OutputStream. Do not store it - it may change
     * from call to call.
     *
     * @throws IOException if there is an IO error
     * @return an output stream for this cache
     */
    public abstract OutputStream getOutputStream() throws IOException;

    /**
     * Filter the cache with the supplied PDFFilter.
     *
     * @param filter the filter to apply
     * @throws IOException if there is an IO error
     */
    public abstract void applyFilter(PDFFilter filter) throws IOException;

    /**
     * Outputs the cached bytes to the given stream.
     *
     * @param stream the stream to write to
     * @throws IOException if there is an IO error
     */
    public abstract void outputStreamData(OutputStream stream) throws IOException;

    /**
     * Returns the current size of the stream.
     *
     * @throws IOException if there is an IO error
     * @return the size of the cache
     */
    public abstract int getSize() throws IOException;

    /**
     * Closes the cache and frees resources.
     *
     * @throws IOException if there is an IO error
     */
    public abstract void close() throws IOException;

    /**
     * Clears and resets the cache.
     *
     * @throws IOException if there is an IO error
     */
    public abstract void reset() throws IOException;
}

