/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;

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
     */
    public static void setCacheToFile(boolean tizit) {
        cacheToFile = tizit;
    }

    /**
     * Get the value of the global cacheToFile flag.
     */
    public static boolean getCacheToFile() {
        return cacheToFile;
    }

    /**
     * Get the correct implementation (based on cacheToFile) of
     * StreamCache.
     */
    public static StreamCache createStreamCache() throws IOException {
        if (cacheToFile)
            return new TempFileStreamCache();
        else
            return new InMemoryStreamCache();
    }

    /**
     * Get the current OutputStream. Do not store it - it may change
     * from call to call.
     */
    public abstract OutputStream getOutputStream() throws IOException;

    /**
     * Filter the cache with the supplied PDFFilter.
     */
    public abstract void applyFilter(PDFFilter filter) throws IOException;

    /**
     * Outputs the cached bytes to the given stream.
     */
    public abstract void outputStreamData(OutputStream stream) throws IOException;

    /**
     * Returns the current size of the stream.
     */
    public abstract int getSize() throws IOException;

    /**
     * Closes the cache and frees resources.
     */
    public abstract void close() throws IOException;

    /**
     * Clears and resets the cache.
     */
    public abstract void reset() throws IOException;
}

