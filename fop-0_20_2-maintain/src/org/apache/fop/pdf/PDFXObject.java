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
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;

// FOP
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.EPSImage;
import org.apache.fop.image.JpegImage;
import org.apache.fop.image.FopImageException;

/* modified by JKT to integrate with 0.12.0 */
/* modified by Eric SCHAEFFER to integrate with 0.13.0 */

/**
 * PDF XObject
 *
 * A derivative of the PDF Object, is a PDF Stream that has not only a
 * dictionary but a stream of image data.
 * the dictionary just provides information like the stream length
 */
public class PDFXObject extends PDFObject {
    private boolean isPS;
    private PDFDocument pdfDoc;
    private PDFICCStream pdfICCStream;

    FopImage fopimage;
    int Xnum;

    /**
     * create an Xobject with the given number and name and load the
     * image in the object
     */
    public PDFXObject(int number, int Xnumber, FopImage img) {
        this(number, Xnumber, img, null);
    }

    public PDFXObject(int number, int Xnumber, FopImage img, PDFDocument pdfdoc) {
        super(number);
        isPS = false;
        this.Xnum = Xnumber;
        if (img == null)
            MessageHandler.errorln("FISH");
        fopimage = img;
        this.pdfDoc = pdfdoc;
        pdfICCStream = null;
        try {
            if (fopimage instanceof JpegImage) {
                fopimage.getBitmaps();
                JpegImage jpegimage = (JpegImage)fopimage;
                if (jpegimage.getColorSpace().hasICCProfile()) {
                        pdfICCStream = pdfDoc.makePDFICCStream();
                        pdfICCStream.setColorSpace(jpegimage.getColorSpace());
                        pdfICCStream.addDefaultFilters();
                        if (pdfDoc.encryption != null) {
                            pdfICCStream.addFilter(pdfDoc.encryption.makeFilter(pdfICCStream.number, pdfICCStream.generation));
                        }
                    }
            }
        } catch (Exception e) {
            MessageHandler.errorln("Error while reading image " + fopimage.getURL() +
                            ": " + e.getMessage());
        }
    }

    /**
     * @return the PDF XObject number
     */
    public int getXNumber() {
        return this.Xnum;
    }

    /**
     * represent as PDF
     */
    protected int output(OutputStream stream) throws IOException {
        int length = 0;
        int i = 0;

        try {
            if (fopimage instanceof EPSImage) {
                isPS = true;
                EPSImage epsImage = (EPSImage)fopimage;
                int[] bbox = epsImage.getBBox();
                int bboxw = bbox[2] - bbox[0];
                int bboxh = bbox[3] - bbox[1];

                // delegate the stream work to PDFStream
                PDFStream imgStream = new PDFStream(0);

                StringBuffer preamble = new StringBuffer();
                preamble.append("%%BeginDocument: " + epsImage.getDocName() + "\n");

                preamble.append("userdict begin                 % Push userdict on dict stack\n");
                preamble.append("/PreEPS_state save def\n");
                preamble.append("/dict_stack countdictstack def\n");
                preamble.append("/ops_count count 1 sub def\n");
                preamble.append("/showpage {} def\n");


                preamble.append((double)(1f/(double)bboxw) + " " + (double)(1f/(double)bboxh) + " scale\n");
                preamble.append(-bbox[0] + " " + (-bbox[1]) + " translate\n");
                preamble.append(bbox[0] + " " + bbox[1] + " " + bboxw + " " + bboxh + " rectclip\n");
                preamble.append("newpath\n");

                StringBuffer post = new StringBuffer();
                post.append("%%EndDocument\n");
                post.append("count ops_count sub {pop} repeat\n");
                post.append("countdictstack dict_stack sub {end} repeat\n");
                post.append("PreEPS_state restore\n");
                post.append("end % userdict\n");

                byte[] preBytes;
                try {
                    preBytes = preamble.toString().getBytes(PDFDocument.ENCODING);
                } catch (UnsupportedEncodingException ue) {
                    preBytes = preamble.toString().getBytes();
                }
                byte[] postBytes;
                try {
                    postBytes = post.toString().getBytes(PDFDocument.ENCODING);
                } catch (UnsupportedEncodingException ue) {
                    postBytes = post.toString().getBytes();
                }
                byte[] imgData = new byte[preBytes.length + postBytes.length + fopimage.getBitmaps().length];

                System.arraycopy (preBytes, 0, imgData, 0, preBytes.length);
                System.arraycopy (fopimage.getBitmaps(), 0, imgData, preBytes.length, fopimage.getBitmaps().length);
                System.arraycopy (postBytes, 0, imgData, preBytes.length + fopimage.getBitmaps().length, postBytes.length);


                imgStream.setData(imgData);
                //imgStream.addFilter(new FlateFilter());
                imgStream.addDefaultFilters();
                if (pdfDoc.encryption != null) {
                    imgStream.addFilter(pdfDoc.encryption.makeFilter(this.number,this.generation));
                }

                String dictEntries = imgStream.applyFilters();

                String p = this.number + " " + this.generation + " obj\n";
                p = p + "<</Type /XObject\n";
                p = p + "/Subtype /PS\n";
                p = p + "/Length " + imgStream.getDataLength();

                // don't know if it's the good place (other objects can have references to it)
                //fopimage.close(); //Not really necessary, is it? Only leads to image reloading.

                p = p + dictEntries;
                p = p + ">>\n";

                // push the pdf dictionary on the writer
                byte[] pdfBytes;
                try {
                    pdfBytes = p.getBytes(PDFDocument.ENCODING);
                } catch (UnsupportedEncodingException ue) {
                    pdfBytes = p.getBytes();
                }
                stream.write(pdfBytes);
                length += pdfBytes.length;
                // push all the image data on  the writer and takes care of length for trailer
                length += imgStream.outputStreamData(stream);

                try {
                    pdfBytes = ("endobj\n").getBytes(PDFDocument.ENCODING);
                } catch (UnsupportedEncodingException ue) {
                    pdfBytes = ("endobj\n").getBytes();
                }
                stream.write(pdfBytes);
                length += pdfBytes.length;

            } else {

                // delegate the stream work to PDFStream
                PDFStream imgStream = new PDFStream(0);

                imgStream.setData(fopimage.getBitmaps());

                /*
                 * Added by Eric Dalquist
                 * If the DCT filter hasn't been added to the object we add it here
                 */
                /*
                 * Added by Manuel Mall
                 * Only add the default filters if we don't have an image filter to
                 * avoid double encoding of images
                 */
                 
                if (fopimage.getPDFFilter() != null) {
                    imgStream.addFilter(fopimage.getPDFFilter());
                } else {
                    imgStream.addDefaultFilters();
                }
                if (pdfDoc.encryption != null) {
                    imgStream.addFilter(pdfDoc.encryption.makeFilter(this.number,this.generation));
                }
                
                String dictEntries = imgStream.applyFilters();

                String p = this.number + " " + this.generation + " obj\n";
                p = p + "<</Type /XObject\n";
                p = p + "/Subtype /Image\n";
                p = p + "/Name /Im" + Xnum + "\n";
                p = p + "/Length " + imgStream.getDataLength() + "\n";
                p = p + "/Width " + fopimage.getWidth() + "\n";
                p = p + "/Height " + fopimage.getHeight() + "\n";
                p = p + "/BitsPerComponent " + fopimage.getBitsPerPixel() + "\n";

                if (pdfICCStream != null ) {
                    p = p + "/ColorSpace [/ICCBased " + pdfICCStream.referencePDF() + "]\n";
                } else {
                    ColorSpace cs = fopimage.getColorSpace();
                    p = p + "/ColorSpace /" + cs.getColorSpacePDFString() + "\n";
                }

                    /* PhotoShop generates CMYK values that's inverse,
                     */
                if (fopimage.getColorSpace().getColorSpace() == ColorSpace.DEVICE_CMYK &&
                    fopimage.invertImage()) {
                    p = p + "/Decode [ 1.0 0.0 1.0 0.0 1.0 0.0 1.1 0.0 ]\n";
                }

                if (fopimage.isTransparent()) {
                    PDFColor transp = fopimage.getTransparentColor();
                    p = p + "/Mask [" + transp.red255() + " " + transp.red255()
                        + " " + transp.green255() + " " + transp.green255() + " "
                        + transp.blue255() + " " + transp.blue255() + "]\n";
                }
                p = p + dictEntries;
                p = p + ">>\n";

                // don't know if it's the good place (other objects can have references to it)
                //fopimage.close(); //Not really necessary, is it? Only leads to image reloading.

                // push the pdf dictionary on the writer
                byte[] pdfBytes;
                try {
                    pdfBytes = p.getBytes(PDFDocument.ENCODING);
                } catch (UnsupportedEncodingException ue) {
                    pdfBytes = p.getBytes();
                }
                stream.write(pdfBytes);
                length += pdfBytes.length;
                // push all the image data on  the writer and takes care of length for trailer
                length += imgStream.outputStreamData(stream);

                try {
                    pdfBytes = ("endobj\n").getBytes(PDFDocument.ENCODING);
                } catch (UnsupportedEncodingException ue) {
                    pdfBytes = ("endobj\n").getBytes();
                }
                stream.write(pdfBytes);
                length += pdfBytes.length;
            }
        } catch (FopImageException imgex) {
            MessageHandler.errorln("Error in XObject : "
                                   + imgex.getMessage());
        }

        return length;
    }

    byte[] toPDF() {
        /*
         * Not used any more
         * String p = this.number + " " + this.generation + " obj\n";
         * p = p + "<</Type /XObject\n";
         * p = p + "/Subtype /Image\n";
         * p = p + "/Name /Im"+Xnum+"\n";
         * p = p + "/Width "+fopimage.getpixelwidth()+"\n";
         * p = p + "/Height "+fopimage.getpixelheight()+"\n";
         * p = p + "/BitsPerComponent 8\n";
         * if (fopimage.getcolor())
         * p = p + "/ColorSpace /DeviceRGB\n";
         * else
         * p = p + "/ColorSpace /DeviceGray\n";
         * p = p + "/Filter /ASCIIHexDecode\n";
         * p = p + "/Length ";
         * return p;
         */
        return null;
    }

}
