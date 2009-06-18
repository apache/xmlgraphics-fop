/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

