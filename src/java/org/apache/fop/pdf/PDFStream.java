/*
 * $Id: PDFStream.java,v 1.20 2003/03/07 08:25:47 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
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
            /**@todo Log with Logger */
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
            /**@todo Log with Logger */
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
