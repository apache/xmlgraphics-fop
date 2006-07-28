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

import java.io.OutputStream;
import java.io.IOException;

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

    /**
     * Create an empty stream object
     */
    public PDFStream() {
        super();
        try {
            data = StreamCacheFactory.getInstance().createStreamCache();
        } catch (IOException ex) {
            //TODO throw the exception and catch it elsewhere
            ex.printStackTrace();
        }
    }

    /**
     * Append data to the stream
     *
     * @param s the string of PDF to add
     */
    public void add(String s) {
        try {
            data.getOutputStream().write(s.getBytes());
        } catch (IOException ex) {
            //TODO throw the exception and catch it elsewhere
            ex.printStackTrace();
        }

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
            return data.getSize();
        } catch (Exception e) {
            //TODO throw the exception and catch it elsewhere
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @see org.apache.fop.pdf.AbstractPDFStream#getSizeHint()
     */
    protected int getSizeHint() throws IOException {
        return data.getSize();
    }

    /**
     * @see org.apache.fop.pdf.AbstractPDFStream#outputRawStreamData(OutputStream)
     */
    protected void outputRawStreamData(OutputStream out) throws IOException {
        data.outputContents(out);
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#output(OutputStream)
     */
    protected int output(OutputStream stream) throws IOException {
        final int len = super.output(stream);
        
        //Now that the data has been written, it can be discarded.
        this.data = null;
        return len;
    }

}
