/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
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
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.util.Map;

public class FopPDFImage implements PDFImage {
    FopImage fopImage;
    PDFICCStream pdfICCStream = null;
    PDFFilter pdfFilter = null;
    String maskRef;
    String softMaskRef;
    boolean isPS = false;
    Map filters;
    String key;

    public FopPDFImage(FopImage im, String k) {
        fopImage = im;
        key = k;
        isPS = (fopImage instanceof EPSImage);
    }

    // key to look up XObject
    public String getKey() {
        return key;
    }

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

    public boolean isPS() {
        return isPS;
    }

    // image size
    public int getWidth() {
        return fopImage.getWidth();
    }

    public int getHeight() {
        return fopImage.getHeight();
    }

    // DeviceGray, DeviceRGB, or DeviceCMYK
    public PDFColorSpace getColorSpace() {
        return toPDFColorSpace(fopImage.getColorSpace());
    }

    public int getBitsPerPixel() {
        return fopImage.getBitsPerPixel();
    }

    // For transparent images
    public boolean isTransparent() {
        return fopImage.isTransparent();
    }

    public PDFColor getTransparentColor() {
        return fopImage.getTransparentColor();
    }

    public String getMask() {
        return maskRef;
    }

    public String getSoftMask() {
        return softMaskRef;
    }

    public PDFStream getDataStream() throws IOException {
        if(isPS) {
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


        preamble.append((double)(1f / (double) bboxw) + " " +
                        (double)(1f / (double) bboxh) + " scale\n");
        preamble.append(-bbox[0] + " " + (-bbox[1]) + " translate\n");
        preamble.append(bbox[0] + " " + bbox[1] + " " + bboxw + " " +
                        bboxh + " rectclip\n");
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
        byte[] imgData = new byte[preBytes.length + postBytes.length +
                                  epsLength];

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

    public PDFICCStream getICCStream() {
        return pdfICCStream;
    }

    protected PDFColorSpace toPDFColorSpace(ColorSpace cs) {
        if(cs == null) return null;

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

