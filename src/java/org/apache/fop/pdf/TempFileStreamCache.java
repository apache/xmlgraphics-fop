/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.pdf;

// Java
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;

//Commons
import org.apache.commons.io.CopyUtils;

/**
 * StreamCache implementation that uses temporary files rather than heap.
 */
public class TempFileStreamCache implements StreamCache {

    /**
     * The current output stream.
     */
    private OutputStream output;

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
            output = new java.io.BufferedOutputStream(
                       new java.io.FileOutputStream(tempFile));
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
     * Outputs the cached bytes to the given stream.
     *
     * @param out the output stream to write to
     * @return the number of bytes written
     * @throws IOException if there is an IO error
     */
    public int outputContents(OutputStream out) throws IOException {
        if (output == null) {
            return 0;
        }

        output.close();
        output = null;

        // don't need a buffer because streamCopy is buffered
        InputStream input = new java.io.FileInputStream(tempFile);
        final long bytesCopied = CopyUtils.copy(input, out);
        input.close();
        return (int)bytesCopied;
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
     * Clears and resets the cache.
     *
     * @throws IOException if there is an IO error
     */
    public void clear() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}
