/*
 * $Id$
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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.fop.util.CloseBlockerOutputStream;

/**
 * This is an abstract base class for PDF streams.
 */
public abstract class AbstractPDFStream extends PDFObject {

    /** The filters that should be applied */
    private PDFFilterList filters;

    /**
     * Constructor for AbstractPDFStream.
     */
    public AbstractPDFStream() {
        super();
    }

    /**
     * Sets up the default filters for this stream if they haven't been set 
     * from outside.
     */
    protected void setupFilterList() {
        if (!getFilterList().isInitialized()) {
            getFilterList().addDefaultFilters(
                getDocumentSafely().getFilterMap(), 
                PDFFilterList.DEFAULT_FILTER);
        }
        prepareImplicitFilters();
        getDocument().applyEncryption(this);
    }

    /**
     * Returns the associated filter list.
     * @return the filter list
     */
    public PDFFilterList getFilterList() {
        if (this.filters == null) {
            if (getDocument() == null) {
                this.filters = new PDFFilterList();
            } else {
                this.filters = new PDFFilterList(getDocument().isEncryptionActive());
                //this.filters = new PDFFilterList(false);
            }
        }
        return this.filters;
    }

    /**
     * Returns a value that hints at the size of the encoded stream. This is
     * used to optimize buffer allocation so fewer buffer reallocations are
     * necessary. 
     * @return an estimated size (0 if no hint can be given)
     * @throws IOException in case of an I/O problem
     */
    protected abstract int getSizeHint() throws IOException;
    
    /**
     * Sends the raw stream data to the target OutputStream.
     * @param out OutputStream to write to
     * @throws IOException In case of an I/O problem
     */
    protected abstract void outputRawStreamData(OutputStream out) 
            throws IOException;

    /**
     * Output just the stream data enclosed by stream/endstream markers
     * @param encodedStream already encoded/filtered stream to write
     * @param out OutputStream to write to
     * @return int number of bytes written
     * @throws IOException in case of an I/O problem
     */
    protected int outputStreamData(StreamCache encodedStream, OutputStream out) throws IOException {
        int length = 0;
        byte[] p = encode("stream\n");
        out.write(p);
        length += p.length;
        
        encodedStream.outputContents(out);
        length += encodedStream.getSize();
        
        p = encode("\nendstream");
        out.write(p);
        length += p.length;
        return length;
    }

    /**
     * Encodes the raw data stream for output to a PDF file.
     * @return the encoded stream
     * @throws IOException in case of an I/O problem
     */
    protected StreamCache encodeStream() throws IOException {
        //Allocate a temporary buffer to find out the size of the encoded stream
        final StreamCache encodedStream = StreamCacheFactory.getInstance().
                createStreamCache(getSizeHint());
        OutputStream filteredOutput = 
                getFilterList().applyFilters(encodedStream.getOutputStream());
        outputRawStreamData(filteredOutput);
        filteredOutput.flush();
        filteredOutput.close();
        return encodedStream;
    }

    /**
     * Encodes and writes a stream directly to an OutputStream. The length of
     * the stream, in this case, is set on a PDFNumber object that has to be
     * prepared beforehand.
     * @param out OutputStream to write to
     * @param refLength PDFNumber object to receive the stream length
     * @return number of bytes written (header and trailer included)
     * @throws IOException in case of an I/O problem
     */
    protected int encodeAndWriteStream(OutputStream out, PDFNumber refLength)
                throws IOException {
        int bytesWritten = 0;
        //Stream header
        byte[] buf = encode("stream\n");
        out.write(buf);
        bytesWritten += buf.length;
        
        //Stream contents
        CloseBlockerOutputStream cbout = new CloseBlockerOutputStream(out);
        CountingOutputStream cout = new CountingOutputStream(cbout);
        OutputStream filteredOutput = 
                getFilterList().applyFilters(cout);
        outputRawStreamData(filteredOutput);
        filteredOutput.close();
        refLength.setNumber(new Integer(cout.getCount()));
        bytesWritten += cout.getCount();
            
        //Stream trailer
        buf = encode("\nendstream");
        out.write(buf);
        bytesWritten += buf.length;
        
        return bytesWritten;
    }

    /**
     * Overload the base object method so we don't have to copy
     * byte arrays around so much
     * @see org.apache.fop.pdf.PDFObject#output(OutputStream)
     */
    protected int output(OutputStream stream) throws IOException {
        int length = 0;
        setupFilterList();
        
        StreamCache encodedStream = null;
        PDFNumber refLength = null;
        final String lengthEntry;
        if (getDocument().isEncodingOnTheFly()) {
            refLength = new PDFNumber();
            getDocumentSafely().registerObject(refLength);
            lengthEntry = refLength.referencePDF();
        } else {
            encodedStream = encodeStream();
            lengthEntry = Integer.toString(encodedStream.getSize() + 1);
        }
        
        String filterEntry = getFilterList().buildFilterDictEntries();
        byte[] p = encode(buildStreamDict(lengthEntry));

        stream.write(p);
        length += p.length;
        
        //Send encoded stream to target OutputStream
        if (encodedStream == null) {
            int bytesWritten = encodeAndWriteStream(stream, refLength);
            length += bytesWritten;
        } else {
            length += outputStreamData(encodedStream, stream);
            encodedStream.clear(); //Encoded stream can now be discarded
        }
        
        p = encode("\nendobj\n");
        stream.write(p);
        length += p.length;
        return length;
    }

    /**
     * Constructs the dictionary for the stream. Override this method if you
     * need additional entries.
     * @param lengthEntry value for the /Length entry
     * @return the newly constructed dictionary
     */
    protected String buildStreamDict(String lengthEntry) {
        final String filterEntry = getFilterList().buildFilterDictEntries();
        return (getObjectID()
            + "<< /Length " + lengthEntry + "\n"
            + filterEntry
            + "\n>>\n");
    }

    /**
     * Prepares implicit filters (such as the DCTFilter for JPEG images). You
     * must make sure that the appropriate filters are in the filter list at
     * the right places.
     */
    protected void prepareImplicitFilters() {
        //nop: No default implicit filters
    }

}
