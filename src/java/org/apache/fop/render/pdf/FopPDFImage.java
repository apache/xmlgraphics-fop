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

package org.apache.fop.render.pdf;

import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFImage;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.DCTFilter;
import org.apache.fop.pdf.PDFColorSpace;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.pdf.BitmapImage;

import org.apache.fop.image.FopImage;
import org.apache.fop.image.JpegImage;
import org.apache.fop.image.EPSImage;

import java.io.IOException;
import java.io.OutputStream;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;

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
        if ("image/jpeg".equals(fopImage.getMimeType())) {
            pdfFilter = new DCTFilter();
            pdfFilter.setApplied(true);

            JpegImage jpegimage = (JpegImage) fopImage;
            ICC_Profile prof = jpegimage.getICCProfile();
            PDFColorSpace pdfCS = toPDFColorSpace(jpegimage.getColorSpace());
            if (prof != null) {
                pdfICCStream = doc.getFactory().makePDFICCStream();
                pdfICCStream.setColorSpace(prof, pdfCS);
            }
        }

        //Handle transparency mask if applicable
        if (fopImage.hasSoftMask()) {
            byte [] softMask = fopImage.getSoftMask();
            if (softMask == null) {
                return;
            }
            BitmapImage fopimg = new BitmapImage
                ("Mask:" + key, fopImage.getWidth(), fopImage.getHeight(), 
                 softMask, null);
            fopimg.setColorSpace(new PDFColorSpace(PDFColorSpace.DEVICE_GRAY));
            PDFXObject xobj = doc.addImage(null, fopimg);
            softMaskRef = xobj.referencePDF();
        }
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#isPS()
     */
    public boolean isPS() {
        return isPS;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#isDCT()
     */
    public boolean isDCT() {
        return fopImage.getMimeType().equals("image/jpeg");
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
        return new PDFColor(fopImage.getTransparentColor().getRed(),
                            fopImage.getTransparentColor().getGreen(),
                            fopImage.getTransparentColor().getBlue());
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
     * @see org.apache.fop.pdf.PDFImage#outputContents(OutputStream)
     */
    public void outputContents(OutputStream out) throws IOException {
        if (isPS) {
            outputPostScriptContents(out);
        } else {
            out.write(fopImage.getBitmaps());
        }
    }

    /**
     * Serializes an EPS image to an OutputStream.
     * @param out OutputStream to write to
     * @throws IOException in case of an I/O problem
     */
    protected void outputPostScriptContents(OutputStream out) throws IOException {
        EPSImage epsImage = (EPSImage) fopImage;
        int[] bbox = epsImage.getBBox();
        int bboxw = bbox[2] - bbox[0];
        int bboxh = bbox[3] - bbox[1];

        // delegate the stream work to PDFStream
        //PDFStream imgStream = new PDFStream(0);

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

        //Write Preamble
        out.write(PDFDocument.encode(preamble.toString()));
        //Write EPS contents
        out.write(((EPSImage)fopImage).getEPSImage());
        //Writing trailer
        out.write(PDFDocument.encode(post.toString()));
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

    /**
     * @see org.apache.fop.pdf.PDFImage#getFilterHint()
     */
    public String getFilterHint() {
        if (isPS()) {
            return PDFFilterList.CONTENT_FILTER;
        } else if (fopImage.getMimeType().equals("image/jpeg")) {
            return PDFFilterList.JPEG_FILTER;
        } else {
            return PDFFilterList.IMAGE_FILTER;
        }
    }

}

