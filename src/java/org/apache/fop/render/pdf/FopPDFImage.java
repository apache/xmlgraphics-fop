/*
 * $Id: FopPDFImage.java,v 1.7 2003/03/07 09:46:32 jeremias Exp $
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
package org.apache.fop.render.pdf;

import org.apache.fop.pdf.PDFImage;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.DCTFilter;
import org.apache.fop.pdf.PDFColorSpace;

import org.apache.fop.image.FopImage;
import org.apache.fop.image.JpegImage;
import org.apache.fop.image.EPSImage;

import java.io.IOException;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.util.Map;

/**
 * PDFImage implementation for the PDF renderer.
 */
public class FopPDFImage implements PDFImage {
    
    private FopImage fopImage;
    private PDFICCStream pdfICCStream = null;
    private PDFFilter pdfFilter = null;
    private String maskRef;
    private String softMaskRef;
    private boolean isPS = false;
    private Map filters;
    private String key;

    /**
     * Creates a new PDFImage from a FopImage
     * @param image Image
     * @param key XObject key
     */
    public FopPDFImage(FopImage image, String key) {
        fopImage = image;
        this.key = key;
        isPS = (fopImage instanceof EPSImage);
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getKey()
     */
    public String getKey() {
        // key to look up XObject
        return this.key;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#setup(PDFDocument)
     */
    public void setup(PDFDocument doc) {
        filters = doc.getFilterMap();
        if ("image/jpeg".equals(fopImage.getMimeType())) {
            pdfFilter = new DCTFilter();
            pdfFilter.setApplied(true);

            JpegImage jpegimage = (JpegImage) fopImage;
            ICC_Profile prof = jpegimage.getICCProfile();
            PDFColorSpace pdfCS = toPDFColorSpace(jpegimage.getColorSpace());
            if (prof != null) {
                pdfICCStream = doc.makePDFICCStream();
                pdfICCStream.setColorSpace(prof, pdfCS);
                pdfICCStream.addDefaultFilters(filters, PDFStream.CONTENT_FILTER);
            }
        }
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#isPS()
     */
    public boolean isPS() {
        return isPS;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getWidth()
     */
    public int getWidth() {
        return fopImage.getWidth();
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getHeight()
     */
    public int getHeight() {
        return fopImage.getHeight();
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getColorSpace()
     */
    public PDFColorSpace getColorSpace() {
        // DeviceGray, DeviceRGB, or DeviceCMYK
        return toPDFColorSpace(fopImage.getColorSpace());
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getBitsPerPixel()
     */
    public int getBitsPerPixel() {
        return fopImage.getBitsPerPixel();
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#isTransparent()
     */
    public boolean isTransparent() {
        return fopImage.isTransparent();
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getTransparentColor()
     */
    public PDFColor getTransparentColor() {
        return fopImage.getTransparentColor();
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getMask()
     */
    public String getMask() {
        return maskRef;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getSoftMask()
     */
    public String getSoftMask() {
        return softMaskRef;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getDataStream()
     */
    public PDFStream getDataStream() throws IOException {
        if (isPS) {
            return getPSDataStream();
        } else {
            // delegate the stream work to PDFStream
            PDFStream imgStream = new PDFStream(0);

            imgStream.setData(fopImage.getBitmaps());

            /*
             * Added by Eric Dalquist
             * If the DCT filter hasn't been added to the object we add it here
             */
            if (pdfFilter != null) {
                imgStream.addFilter(pdfFilter);
            }

            imgStream.addDefaultFilters(filters, PDFStream.IMAGE_FILTER);
            return imgStream;
        }
    }

    /**
     * Returns a PDFStream for an EPS image.
     * @return PDFStream the newly creates PDFStream
     * @throws IOException in case of an I/O problem
     */
    protected PDFStream getPSDataStream() throws IOException {
        int length = 0;
        int i = 0;
        EPSImage epsImage = (EPSImage) fopImage;
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


        preamble.append((double)(1f / (double) bboxw) + " "
                      + (double)(1f / (double) bboxh) + " scale\n");
        preamble.append(-bbox[0] + " " + (-bbox[1]) + " translate\n");
        preamble.append(bbox[0] + " " + bbox[1] + " " 
                        + bboxw + " " + bboxh + " rectclip\n");
        preamble.append("newpath\n");

        StringBuffer post = new StringBuffer();
        post.append("%%EndDocument\n");
        post.append("count ops_count sub {pop} repeat\n");
        post.append("countdictstack dict_stack sub {end} repeat\n");
        post.append("PreEPS_state restore\n");
        post.append("end % userdict\n");

        byte[] preBytes = preamble.toString().getBytes();
        byte[] postBytes = post.toString().getBytes();
        byte[] epsBytes = ((EPSImage)fopImage).getEPSImage();
        int epsLength = epsBytes.length;
        byte[] imgData = new byte[preBytes.length 
                                + postBytes.length 
                                + epsLength];

        System.arraycopy (preBytes, 0, imgData, 0, preBytes.length);
        System.arraycopy (epsBytes, 0, imgData,
                          preBytes.length, epsBytes.length);
        System.arraycopy (postBytes, 0, imgData,
                          preBytes.length + epsBytes.length,
                          postBytes.length);


        imgStream.setData(imgData);
        imgStream.addDefaultFilters(filters, PDFStream.CONTENT_FILTER);

        return imgStream;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getICCStream()
     */
    public PDFICCStream getICCStream() {
        return pdfICCStream;
    }

    /**
     * Converts a ColorSpace object to a PDFColorSpace object.
     * @param cs ColorSpace instance
     * @return PDFColorSpace new converted object
     */
    public static PDFColorSpace toPDFColorSpace(ColorSpace cs) {
        if (cs == null) {
            return null;
        }

        PDFColorSpace pdfCS = new PDFColorSpace(0);
        switch(cs.getType()) {
            case ColorSpace.TYPE_CMYK:
                pdfCS.setColorSpace(PDFColorSpace.DEVICE_CMYK);
            break;
            case ColorSpace.TYPE_RGB:
                pdfCS.setColorSpace(PDFColorSpace.DEVICE_RGB);
            break;
            case ColorSpace.TYPE_GRAY:
                pdfCS.setColorSpace(PDFColorSpace.DEVICE_GRAY);
            break;
        }
        return pdfCS;
    }
}

