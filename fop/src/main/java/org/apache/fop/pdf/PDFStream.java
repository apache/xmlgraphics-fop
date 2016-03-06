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

package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Class representing a PDF stream.
 * <p>
 * A derivative of the PDF Object, a PDF Stream has not only a dictionary
 * but a stream of PDF commands. The stream of commands is where the real
 * work is done, the dictionary just provides information like the stream
 * length.
 */
public class PDFStream extends AbstractPDFStream {

    /**
     * The stream of PDF commands
     */
    protected StreamCache data;

    private transient Writer streamWriter;
    private transient char[] charBuffer;

    /**
     * Create an empty stream object
     */
    public PDFStream() {
        setUp();
    }

    public PDFStream(PDFDictionary dictionary) {
        super(dictionary);
        setUp();
    }

    public PDFStream(PDFDictionary dictionary, boolean encodeOnTheFly) {
        super(dictionary, encodeOnTheFly);
        setUp();
    }

    public PDFStream(boolean encodeOnTheFly) {
        super(encodeOnTheFly);
        setUp();
    }

    private void setUp() {
        try {
            data = StreamCacheFactory.getInstance().createStreamCache();
            this.streamWriter = new OutputStreamWriter(
                    getBufferOutputStream(), PDFDocument.ENCODING);
            //Buffer to minimize calls to the converter
            this.streamWriter = new java.io.BufferedWriter(this.streamWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Append data to the stream
     *
     * @param s the string of PDF to add
     */
    public void add(String s) {
        try {
            this.streamWriter.write(s);
        } catch (IOException ex) {
            //TODO throw the exception and catch it elsewhere
            ex.printStackTrace();
        }
    }

    /**
     * Append data to the stream
     *
     * @param sb the string buffer of PDF to add
     */
    public void add(StringBuffer sb) {
        try {
            int nHave = sb.length();
            if (charBuffer == null) {
                charBuffer = new char [ nHave * 2 ];
            } else {
                int nAvail = charBuffer.length;
                if (nAvail < nHave) {
                    int nAlloc = nAvail;
                    while (nAlloc < nHave) {
                        nAlloc *= 2;
                    }
                    charBuffer = new char [ nAlloc ];
                }
            }
            sb.getChars(0, nHave, charBuffer, 0);
            this.streamWriter.write(charBuffer, 0, nHave);
        } catch (IOException ex) {
            //TODO throw the exception and catch it elsewhere
            ex.printStackTrace();
        }
    }

    private void flush() throws IOException {
        this.streamWriter.flush();
    }

    /**
     * Returns a Writer that writes to the OutputStream of the buffer.
     * @return the Writer
     */
    public Writer getBufferWriter() {
        return this.streamWriter;
    }

    /**
     * Returns an OutputStream that can be used to write to the buffer which is used
     * to build up the PDF stream.
     * @return the OutputStream
     * @throws IOException In case of an I/O problem
     */
    public OutputStream getBufferOutputStream() throws IOException {
        if (this.streamWriter != null) {
            flush(); //Just to be sure
        }
        return this.data.getOutputStream();
    }

    /**
     * Used to set the contents of the PDF stream.
     * @param data the contents as a byte array
     * @throws IOException in case of an I/O problem
     */
    public void setData(byte[] data) throws IOException {
        this.data.clear();
        this.data.write(data);
    }

    /**
     * Returns the size of the content.
     * @return size of the content
     */
    public int getDataLength() {
        try {
            flush();
            return data.getSize();
        } catch (Exception e) {
            //TODO throw the exception and catch it elsewhere
            e.printStackTrace();
            return 0;
        }
    }

    /** {@inheritDoc} */
    protected int getSizeHint() throws IOException {
        flush();
        return data.getSize();
    }

    /** {@inheritDoc} */
    protected void outputRawStreamData(OutputStream out) throws IOException {
        flush();
        data.outputContents(out);
    }

    /**
     * {@inheritDoc}
     */
    public int output(OutputStream stream) throws IOException {
        final int len = super.output(stream);

        //Now that the data has been written, it can be discarded.
//        this.data = null;
        return len;
    }

}
