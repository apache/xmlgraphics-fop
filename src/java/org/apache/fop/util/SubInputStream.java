/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.fop.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is a FilterInputStream descendant that reads from an underlying InputStream
 * up to a defined number of bytes or the end of the underlying stream. Closing this InputStream
 * will not result in the underlying InputStream to be closed, too.
 * <p>
 * This InputStream can be used to read chunks from a larger file of which the length is
 * known in advance.
 */
public class SubInputStream extends FilterInputStream {

    /** Indicates the number of bytes remaning to be read from the underlying InputStream. */
    private long bytesToRead;
    
    /**
     * Creates a new SubInputStream.
     * @param in the InputStream to read from
     * @param maxLen the maximum number of bytes to read from the underlying InputStream until
     *               the end-of-file is signalled.
     */
    public SubInputStream(InputStream in, long maxLen) {
        super(in);
        this.bytesToRead = maxLen;
    }

    /** @see java.io.InputStream#read() */
    public int read() throws IOException {
        if (bytesToRead > 0) {
            int result = super.read();
            if (result <= 0) {
                bytesToRead--;
                return result;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
    
    /** @see java.io.InputStream#read(byte[], int, int) */
    public int read(byte[] b, int off, int len) throws IOException {
        if (bytesToRead == 0) {
            return -1;
        }
        int effRead = (int)Math.min(bytesToRead, len);
        //cast to int is safe because len can never be bigger than Integer.MAX_VALUE
        
        int result = super.read(b, off, effRead);
        if (result >= 0) {
            bytesToRead -= result;
        }
        return result;
    }
    
    /** @see java.io.InputStream#skip(long) */
    public long skip(long n) throws IOException {
        long effRead = Math.min(bytesToRead, n);
        long result = super.skip(effRead);
        bytesToRead -= result;
        return result;
    }

    /** @see java.io.InputStream#close() */
    public void close() throws IOException {
        //Don't close the underlying InputStream!!!
        this.bytesToRead = 0;
    }
}
