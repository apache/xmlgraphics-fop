/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

/* modified by JKT to integrate with 0.12.0 */
/* modified by Eric SCHAEFFER to integrate with 0.13.0 */

package org.apache.fop.pdf;

// Java
import java.io.IOException;
import java.io.OutputStream;

// FOP
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.EPSImage;
import org.apache.fop.image.JpegImage;

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
        if (img == null) {
            //log.error("FISH");
        }
        fopimage = img;
        this.pdfDoc = pdfdoc;
        pdfICCStream = null;
        try {
            if (fopimage instanceof JpegImage) {
                    /* hasICCProfile is not initialized before
                       the bitmaps is read - should maybe fix this in
                       the JpegImage instead...
                    */
                fopimage.getBitmaps();
                JpegImage jpegimage = (JpegImage)fopimage;
                if (jpegimage.getColorSpace().hasICCProfile()) {
                    pdfICCStream = pdfDoc.makePDFICCStream();
                    pdfICCStream.setColorSpace(jpegimage.getColorSpace());
                    pdfICCStream.addDefaultFilters();
                }
            }
        } catch (Exception e) {
            //log.error("Error while reading image " +
            //                       fopimage.getURL() +
            //                       ": " + e.getMessage());
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
                
                byte[] preBytes = preamble.toString().getBytes();
                byte[] postBytes = post.toString().getBytes();
                byte[] imgData = new byte[preBytes.length + postBytes.length + fopimage.getBitmaps().length];
                
                System.arraycopy (preBytes, 0, imgData, 0, preBytes.length);
                System.arraycopy (fopimage.getBitmaps(), 0, imgData, preBytes.length, fopimage.getBitmaps().length);
                System.arraycopy (postBytes, 0, imgData, preBytes.length + fopimage.getBitmaps().length, postBytes.length);
                
                
                imgStream.setData(imgData);
                imgStream.addDefaultFilters();
                
                String dictEntries = imgStream.applyFilters();
                
                String p = this.number + " " + this.generation + " obj\n";
                p = p + "<</Type /XObject\n";
                p = p + "/Subtype /PS\n";
                p = p + "/Length " + imgStream.getDataLength();
                
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
                
            } else {

	            // delegate the stream work to PDFStream
                PDFStream imgStream = new PDFStream(0);
                
                imgStream.setData(fopimage.getBitmaps());
                
	            /*
	             * Added by Eric Dalquist
	             * If the DCT filter hasn't been added to the object we add it here
	             */
                if (fopimage.getPDFFilter() != null) {
                    imgStream.addFilter(fopimage.getPDFFilter());
                }
                
                imgStream.addDefaultFilters();
                
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
                       this will invert the values - too bad if it's not a PhotoShop image...*/
                if (fopimage.getColorSpace().getColorSpace() == ColorSpace.DEVICE_CMYK) {
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
                
	            // push the pdf dictionary on the writer
                byte[] pdfBytes = p.getBytes();
                stream.write(pdfBytes);
                length += pdfBytes.length;
	            // push all the image data on  the writer and takes care of length for trailer
                length += imgStream.outputStreamData(stream);
                
                pdfBytes = ("endobj\n").getBytes();
                stream.write(pdfBytes);
                length += pdfBytes.length;
            }
        } catch (Exception imgex) {
            //log.error("Error in XObject : "
            //                       + imgex.getMessage());
        }
        
        return length;
    }
    
    byte[] toPDF() {
        return null;
    }
}
