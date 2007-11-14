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

import javax.imageio.stream.ImageInputStream;

import org.apache.xmlgraphics.image.codec.util.SeekableStream;

/**
 * Adapter which provides a SeekableStream interface over an ImageInputStream.
 */
public class SeekableStreamAdapter extends SeekableStream {

    private ImageInputStream iin;
    
    /**
     * Main constructor
     * @param iin the ImageInputStream to operate on
     */
    public SeekableStreamAdapter(ImageInputStream iin) {
        this.iin = iin;
    }
    
    /** {@inheritDoc} */
    public long getFilePointer() throws IOException {
        return iin.getStreamPosition();
    }

    /** {@inheritDoc} */
    public int read() throws IOException {
        return iin.read();
    }

    /** {@inheritDoc} */
    public int read(byte[] b, int off, int len) throws IOException {
        return iin.read(b, off, len);
    }

    /** {@inheritDoc} */
    public void seek(long pos) throws IOException {
        iin.seek(pos);
    }

    /** {@inheritDoc} */
    public boolean canSeekBackwards() {
        return true;
    }

    /** {@inheritDoc} */
    public int skipBytes(int n) throws IOException {
        return iin.skipBytes(n);
    }

}
