/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
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
     * @throws IOException if there is an error getting the output stream
     * @return the output stream containing the data
     */
    public OutputStream getOutputStream() throws IOException {
        if (output == null) {
            output = new ByteArrayOutputStream();
        }
        return output;
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
     * @param stream the output stream to write to
     * @throws IOException if there is an IO error writing to the output stream
     */
    public void outputStreamData(OutputStream stream) throws IOException {
        if (output == null) {
            return;
        }

        output.writeTo(stream);
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
     * Closes the cache and frees resources.
     * @throws IOException if there is an error closing the stream
     */
    public void close() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }
    }

    /**
     * Clears and resets the cache.
     * @throws IOException if there is an error closing the stream
     */
    public void reset() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }
    }
}
