/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * StreamCache implementation that uses temporary files rather than heap.
 */
public class InMemoryStreamCache extends StreamCache {

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
     * Get the current OutputStream. Do not store it - it may change
     * from call to call.
     */
    public OutputStream getOutputStream() throws IOException {
        if (output == null)
            output = new ByteArrayOutputStream();
        return output;
    }

    /**
     * Filter the cache with the supplied PDFFilter.
     */
    public void applyFilter(PDFFilter filter) throws IOException {
        if (output == null)
            return;

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
     */
    public void outputStreamData(OutputStream stream) throws IOException {
        if (output == null)
            return;

        output.writeTo(stream);
    }

    /**
     * Returns the current size of the stream.
     */
    public int getSize() throws IOException {
        if (output == null)
            return 0;
        else
            return output.size();
    }

    /**
     * Closes the cache and frees resources.
     */
    public void close() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }
    }

    /**
     * Clears and resets the cache.
     */
    public void reset() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }
    }
}
