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
import java.io.ByteArrayOutputStream;

/**
 * PDF XObject
 *
 * A derivative of the PDF Object, is a PDF Stream that has not only a
 * dictionary but a stream of image data.
 * the dictionary just provides information like the stream length
 */
public class PDFXObject extends PDFObject {
    PDFImage pdfimage;
    int Xnum;

    /**
     * create an XObject with the given number and name and load the
     * image in the object
     */
    public PDFXObject(int number, int Xnumber, PDFImage img) {
        super(number);
        this.Xnum = Xnumber;
        pdfimage = img;
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

        if (pdfimage.isPS()) {
            length = outputEPSImage(stream);
        } else {

            PDFStream imgStream = pdfimage.getDataStream();

            String dictEntries = imgStream.applyFilters();

            String p = this.number + " " + this.generation + " obj\n";
            p = p + "<</Type /XObject\n";
            p = p + "/Subtype /Image\n";
            p = p + "/Name /Im" + Xnum + "\n";
            p = p + "/Length " + imgStream.getDataLength() + "\n";
            p = p + "/Width " + pdfimage.getWidth() + "\n";
            p = p + "/Height " + pdfimage.getHeight() + "\n";
            p = p + "/BitsPerComponent " + pdfimage.getBitsPerPixel() +
                "\n";

            PDFICCStream pdfICCStream = pdfimage.getICCStream();
            if (pdfICCStream != null) {
                p = p + "/ColorSpace [/ICCBased " +
                    pdfICCStream.referencePDF() + "]\n";
            } else {
                PDFColorSpace cs = pdfimage.getColorSpace();
                p = p + "/ColorSpace /" + cs.getColorSpacePDFString() +
                    "\n";
            }

            /* PhotoShop generates CMYK values that's inverse,
               this will invert the values - too bad if it's not
               a PhotoShop image...
             */
            if (pdfimage.getColorSpace().getColorSpace() ==
                    PDFColorSpace.DEVICE_CMYK) {
                p = p + "/Decode [ 1.0 0.0 1.0 0.0 1.0 0.0 1.1 0.0 ]\n";
            }

            if (pdfimage.isTransparent()) {
                PDFColor transp = pdfimage.getTransparentColor();
                p = p + "/Mask [" + transp.red255() + " " +
                    transp.red255() + " " + transp.green255() +
                    " " + transp.green255() + " " +
                    transp.blue255() + " " + transp.blue255() + "]\n";
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

        return length;
    }

}
