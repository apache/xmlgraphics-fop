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
     * @param number the pdf object number
     * @param xnumber the pdf object X number
     * @param cont the pdf stream contents
     * @param ref the resource PDF reference
     */
    public PDFFormXObject(int number, int xnumber, PDFStream cont, String ref) {
        super(number, xnumber, null);
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

        String dictEntries = contents.applyFilters();

        String p = this.number + " " + this.generation + " obj\n";
        p = p + "<</Type /XObject\n";
        p = p + "/Subtype /Form\n";
        p = p + "/FormType 1\n";
        p = p + "/BBox [0 0 1000 1000]\n";
        p = p + "/Matrix [1 0 0 1 0 0]\n";
        p = p + "/Resources " + resRef + "\n";
        p = p + "/Length " + (contents.getDataLength() + 1) + "\n";

        p = p + dictEntries;
        p = p + ">>\n";

        // push the pdf dictionary on the writer
        byte[] pdfBytes = p.getBytes();
        stream.write(pdfBytes);
        length += pdfBytes.length;
        // push all the image data on the writer
        // and takes care of length for trailer
        length += contents.outputStreamData(stream);

        pdfBytes = ("endobj\n").getBytes();
        stream.write(pdfBytes);
        length += pdfBytes.length;
        // let it gc
        // this object is retained as a reference to inserting
        // the same image but the image data is no longer needed
        contents = null;
        return length;
    }
}

