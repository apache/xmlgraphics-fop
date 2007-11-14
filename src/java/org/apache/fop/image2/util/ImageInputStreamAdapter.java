/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.image2.util;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

/**
 * Decorates an ImageInputStream with an InputStream interface. The methods <code>mark()</code>
 * and <code>reset()</code> are fully supported. The method <code>available()</code> will 
 * always return 0.
 */
public class ImageInputStreamAdapter extends InputStream {

    private ImageInputStream iin;
    
    private long lastMarkPosition;
    
    /**
     * Creates a new ImageInputStreamAdapter.
     * @param iin the underlying ImageInputStream
     */
    public ImageInputStreamAdapter(ImageInputStream iin) {
        this.iin = iin;
    }
    
    /** {@inheritDoc} */
    public int read(byte[] b, int off, int len) throws IOException {
        return iin.read(b, off, len);
    }

    /** {@inheritDoc} */
    public int read(byte[] b) throws IOException {
        return iin.read(b);
    }

    /** {@inheritDoc} */
    public int read() throws IOException {
        return iin.read();
    }

    /** {@inheritDoc} */
    public long skip(long n) throws IOException {
        return iin.skipBytes(n);
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
        iin.close();
        iin = null;
    }

    /** {@inheritDoc} */
    public synchronized void mark(int readlimit) {
        //Parameter readlimit is ignored
        try {
            //Cannot use mark()/reset() since they are nestable, and InputStream's are not
            this.lastMarkPosition = iin.getStreamPosition();
        } catch (IOException ioe) {
            throw new RuntimeException(
                    "Unexpected IOException in ImageInputStream.getStreamPosition()", ioe);
        }
    }

    /** {@inheritDoc} */
    public boolean markSupported() {
        return true;
    }

    /** {@inheritDoc} */
    public synchronized void reset() throws IOException {
        iin.seek(this.lastMarkPosition);
    }

    /** {@inheritDoc} */
    public int available() throws IOException {
        return 0;
    }

}
