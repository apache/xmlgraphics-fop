/*
 * $Id: PDFXObject.java,v 1.22 2003/03/07 08:25:46 jeremias Exp $
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

/* modified by JKT to integrate with 0.12.0 */
/* modified by Eric SCHAEFFER to integrate with 0.13.0 */

/**
 * PDF XObject
 *
 * A derivative of the PDF Object, is a PDF Stream that has not only a
 * dictionary but a stream of image data.
 * The dictionary just provides information like the stream length.
 * This outputs the image dictionary and the image data.
 * This is used as a reference for inserting the same image in the
 * document in another place.
 */
public class PDFXObject extends PDFObject {
    private PDFImage pdfimage;
    private int xnum;

    /**
     * create an XObject with the given number and name and load the
     * image in the object
     *
     * @param number the pdf object number
     * @param xnumber the pdf object X number
     * @param img the pdf image that contains the image data
     */
    public PDFXObject(int number, int xnumber, PDFImage img) {
        super(number);
        this.xnum = xnumber;
        pdfimage = img;
    }

    /**
     * Get the xnumber for this pdf object.
     *
     * @return the PDF XObject number
     */
    public int getXNumber() {
        return this.xnum;
    }

    /**
     * Output the image as PDF.
     * This sets up the image dictionary and adds the image data stream.
     *
     * @param stream the output stream to write the data
     * @throws IOException if there is an error writing the data
     * @return the length of the data written
     */
    protected int output(OutputStream stream) throws IOException {
        int length = 0;
        int i = 0;

        if (pdfimage.isPS()) {
            length = outputEPSImage(stream);
        } else {

            PDFStream imgStream = pdfimage.getDataStream();

            String dictEntries = imgStream.applyFilters();

            String p = this.number + " " + this.generation + " obj\n";
            p = p + "<</Type /XObject\n";
            p = p + "/Subtype /Image\n";
            p = p + "/Name /Im" + xnum + "\n";
            p = p + "/Length " + (imgStream.getDataLength() + 1) + "\n";
            p = p + "/Width " + pdfimage.getWidth() + "\n";
            p = p + "/Height " + pdfimage.getHeight() + "\n";
            p = p + "/BitsPerComponent " + pdfimage.getBitsPerPixel()
                  + "\n";

            PDFICCStream pdfICCStream = pdfimage.getICCStream();
            if (pdfICCStream != null) {
                p = p + "/ColorSpace [/ICCBased "
                    + pdfICCStream.referencePDF() + "]\n";
            } else {
                PDFColorSpace cs = pdfimage.getColorSpace();
                p = p + "/ColorSpace /" + cs.getColorSpacePDFString()
                      + "\n";
            }

            /* PhotoShop generates CMYK values that's inverse,
               this will invert the values - too bad if it's not
               a PhotoShop image...
             */
            if (pdfimage.getColorSpace().getColorSpace()
                    == PDFColorSpace.DEVICE_CMYK) {
                p = p + "/Decode [ 1.0 0.0 1.0 0.0 1.0 0.0 1.1 0.0 ]\n";
            }

            if (pdfimage.isTransparent()) {
                PDFColor transp = pdfimage.getTransparentColor();
                p = p + "/Mask [" + transp.red255() + " "
                    + transp.red255() + " " + transp.green255()
                    + " " + transp.green255() + " "
                    + transp.blue255() + " " + transp.blue255() + "]\n";
            }
            String ref = pdfimage.getSoftMask();
            if (ref != null) {
                p = p + "/SMask " + ref + "\n";
            }

            p = p + dictEntries;
            p = p + ">>\n";

            // push the pdf dictionary on the writer
            byte[] pdfBytes = p.getBytes();
            stream.write(pdfBytes);
            length += pdfBytes.length;
            // push all the image data on the writer
            // and takes care of length for trailer
            length += imgStream.outputStreamData(stream);

            pdfBytes = ("endobj\n").getBytes();
            stream.write(pdfBytes);
            length += pdfBytes.length;
        }
        // let it gc
        // this object is retained as a reference to inserting
        // the same image but the image data is no longer needed
        pdfimage = null;
        return length;
    }

    byte[] toPDF() {
        return null;
    }

    private int outputEPSImage(OutputStream stream) throws IOException {
        int length = 0;
        int i = 0;

        PDFStream imgStream = pdfimage.getDataStream();
        String dictEntries = imgStream.applyFilters();

        String p = this.number + " " + this.generation + " obj\n";
        p = p + "<</Type /XObject\n";
        p = p + "/Subtype /PS\n";
        p = p + "/Length " + (imgStream.getDataLength() + 1);

        p = p + dictEntries;
        p = p + ">>\n";

        // push the pdf dictionary on the writer
        byte[] pdfBytes = p.getBytes();
        stream.write(pdfBytes);
        length += pdfBytes.length;
        // push all the image data on  the writer and takes care of length for trailer
        length += imgStream.outputStreamData(stream);

        pdfBytes = ("endobj\n").getBytes();
        stream.write(pdfBytes);
        length += pdfBytes.length;

        return length;
    }

}
