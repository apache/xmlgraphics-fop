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
 * An OutputStream wrapper for a Writer.
 */
public class WriterOutputStream extends OutputStream {

    private Writer writer;
    private String encoding;

    /**
     * Creates a new WriterOutputStream.
     * @param writer the Writer to write to
     */
    public WriterOutputStream(Writer writer) {
        this(writer, null);
    }

    /**
     * Creates a new WriterOutputStream.
     * @param writer the Writer to write to
     * @param encoding the encoding to use, or null if the default encoding should be used
     */
    public WriterOutputStream(Writer writer, String encoding) {
        this.writer = writer;
        this.encoding = encoding;
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        writer.close();
    }

    /**
     * {@inheritDoc}
     */
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] buf, int offset, int length) throws IOException {
        if (encoding != null) {
            writer.write(new String(buf, offset, length, encoding));
        } else {
            writer.write(new String(buf, offset, length));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

    /**
     * {@inheritDoc}
     */
    public void write(int b) throws IOException {
        write(new byte[] {(byte)b});
    }

}
