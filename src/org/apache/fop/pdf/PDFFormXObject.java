/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
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

