/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
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

