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
import org.apache.fop.messaging.MessageHandler;
import java.io.OutputStream;

// FOP
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.FopImageException;

/**
 * PDF XObject
 *
 * A derivative of the PDF Object, is a PDF Stream that has not only a
 * dictionary but a stream of image data.
 * the dictionary just provides information like the stream length
 */
public class PDFXObject extends PDFObject {

    FopImage fopimage;
    int Xnum;


    /**
     * create an Xobject with the given number and name and load the
     * image in the object
     */
    public PDFXObject(int number, int Xnumber, FopImage img) {
        super(number);
        this.Xnum = Xnumber;
        if (img == null)
            MessageHandler.errorln("FISH");
        fopimage = img;
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
        int x, y;

        try {
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
            ColorSpace cs = fopimage.getColorSpace();
            p = p + "/ColorSpace /" + cs.getColorSpacePDFString() + "\n";
            if (fopimage.isTransparent()) {
                PDFColor transp = fopimage.getTransparentColor();
                p = p + "/Mask [" + transp.red255() + " " + transp.red255()
                    + " " + transp.green255() + " " + transp.green255() + " "
                    + transp.blue255() + " " + transp.blue255() + "]\n";
            }
            p = p + dictEntries;
            p = p + ">>\n";

            // don't know if it's the good place (other objects can have references to it)
            fopimage.close();

            // push the pdf dictionary on the writer
            byte[] pdfBytes = p.getBytes();
            stream.write(pdfBytes);
            length += pdfBytes.length;
            // push all the image data on  the writer and takes care of length for trailer
            length += imgStream.outputStreamData(stream);

            pdfBytes = ("endobj\n").getBytes();
            stream.write(pdfBytes);
            length += pdfBytes.length;
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
