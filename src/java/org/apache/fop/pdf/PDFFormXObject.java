/*
 * $Id: PDFFormXObject.java,v 1.3 2003/03/07 08:25:46 jeremias Exp $
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

// Java
import java.io.IOException;
import java.io.OutputStream;

/**
 * PDF Form XObject
 *
 * A derivative of the PDFXObject, is a PDF Stream that has not only a
 * dictionary but a stream of image data.
 */
public class PDFFormXObject extends PDFXObject {
    private PDFStream contents;
    private String resRef;

    /**
     * create a FormXObject with the given number and name and load the
     * image in the object
     *
     * @param xnumber the pdf object X number
     * @param cont the pdf stream contents
     * @param ref the resource PDF reference
     */
    public PDFFormXObject(int xnumber, PDFStream cont, String ref) {
        super(xnumber, null);
        contents = cont;
        resRef = ref;
    }

    /**
     * Output the form stream as PDF.
     * This sets up the form XObject dictionary and adds the content
     * data stream.
     *
     * @param stream the output stream to write the data
     * @throws IOException if there is an error writing the data
     * @return the length of the data written
     */
    protected int output(OutputStream stream) throws IOException {
        int length = 0;

        String dictEntries = getFilterList().buildFilterDictEntries();

        final StreamCache encodedStream = encodeStream();        

        StringBuffer sb = new StringBuffer(128);
        sb.append(getObjectID());
        sb.append("<</Type /XObject\n");
        sb.append("/Subtype /Form\n");
        sb.append("/FormType 1\n");
        sb.append("/BBox [0 0 1000 1000]\n");
        sb.append("/Matrix [1 0 0 1 0 0]\n");
        sb.append("/Resources " + resRef + "\n");
        sb.append("/Length " + (encodedStream.getSize() + 1) + "\n");

        sb.append(dictEntries);
        sb.append(">>\n");

        // push the pdf dictionary on the writer
        byte[] pdfBytes = encode(sb.toString());
        stream.write(pdfBytes);
        length += pdfBytes.length;

        //Send encoded stream to target OutputStream
        length += outputStreamData(encodedStream, stream);
        encodedStream.clear(); //Encoded stream can now be discarded

        pdfBytes = encode("endobj\n");
        stream.write(pdfBytes);
        length += pdfBytes.length;
        
        // let it gc
        // this object is retained as a reference to inserting
        // the same image but the image data is no longer needed
        contents = null;
        return length;
    }
    
    /**
     * @see org.apache.fop.pdf.PDFStream#outputRawStreamData(OutputStream)
     */
    protected void outputRawStreamData(OutputStream out) throws IOException {
        contents.outputRawStreamData(out);
    }

    
}

