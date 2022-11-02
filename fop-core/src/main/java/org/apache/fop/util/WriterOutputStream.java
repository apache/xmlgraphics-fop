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

package org.apache.fop.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @deprecated use org.apache.xmlgraphics.util.WriterOutputStream instead
 * @see org.apache.xmlgraphics.util.WriterOutputStream
 */
public class WriterOutputStream extends OutputStream {

    private final org.apache.xmlgraphics.util.WriterOutputStream writerOutputStream;

    /**
     * @param writer a writer
     */
    public WriterOutputStream(Writer writer) {
        writerOutputStream = new org.apache.xmlgraphics.util.WriterOutputStream(
                writer);
    }

    /**
     * @param writer a writer
     * @param encoding stream encoding
     */
    public WriterOutputStream(Writer writer, String encoding) {
        writerOutputStream = new org.apache.xmlgraphics.util.WriterOutputStream(
                writer, encoding);
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
        writerOutputStream.close();
    }

    /** {@inheritDoc} */
    public void flush() throws IOException {
        writerOutputStream.flush();
    }

    /** {@inheritDoc} */
    public void write(byte[] buf, int offset, int length) throws IOException {
        writerOutputStream.write(buf, offset, length);
    }

    /** {@inheritDoc} */
    public void write(byte[] buf) throws IOException {
        writerOutputStream.write(buf);
    }

    /** {@inheritDoc} */
    public void write(int b) throws IOException {
        writerOutputStream.write(b);
    }

}
