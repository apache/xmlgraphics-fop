/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// Java
import java.net.URL;
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

// FOP
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.DCTFilter;
import org.apache.fop.image.analyser.ImageReader;

/**
 * FopImage object for JPEG images, Using Java native classes.
 * @author Eric Dalquist
 * @see AbstractFopImage
 * @see FopImage
 */
public class JpegImage extends AbstractFopImage {
    public JpegImage(URL href) throws FopImageException {
        super(href);
    }

    public JpegImage(URL href,
                     ImageReader imgReader) throws FopImageException {
        super(href, imgReader);
    }

    protected void loadImage() throws FopImageException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream inStream;

        byte[] readBuf = new byte[4096];
        int bytes_read;
        int index = 0;
        boolean cont = true;

        this.m_compressionType = new DCTFilter();
        this.m_compressionType.setApplied(true);

        try {
            inStream = this.m_href.openStream();

            while ((bytes_read = inStream.read(readBuf)) != -1) {
                baos.write(readBuf, 0, bytes_read);
            }
        } catch (java.io.IOException ex) {
            throw new FopImageException("Error while loading image " +
                                        this.m_href.toString() + " : " + ex.getClass() +
                                        " - " + ex.getMessage());
        }

        this.m_bitmaps = baos.toByteArray();
        this.m_bitsPerPixel = 8;
        this.m_isTransparent = false;

        if (this.m_bitmaps.length > (index + 2) &&
                uByte(this.m_bitmaps[index]) == 255 &&
                uByte(this.m_bitmaps[index + 1]) == 216) {
            index += 2;

            while (index < this.m_bitmaps.length && cont) {
                //check to be sure this is the begining of a header
                if (this.m_bitmaps.length > (index + 2) &&
                        uByte(this.m_bitmaps[index]) == 255) {

                    //192 or 194 are the header bytes that contain the jpeg width height and color depth.
                    if (uByte(this.m_bitmaps[index + 1]) == 192 ||
                            uByte(this.m_bitmaps[index + 1]) == 194) {

                        this.m_height = calcBytes(this.m_bitmaps[index + 5],
                                                  this.m_bitmaps[index + 6]);
                        this.m_width = calcBytes(this.m_bitmaps[index + 7],
                                                 this.m_bitmaps[index + 8]);

                        if (this.m_bitmaps[index + 9] == 1) {
                            this.m_colorSpace = new ColorSpace(
                                                  ColorSpace.DEVICE_GRAY);
                        } else if (this.m_bitmaps[index + 9] == 3) {
                            this.m_colorSpace =
                              new ColorSpace(ColorSpace.DEVICE_RGB);
                        } else {
                            cont = false;
                            throw new FopImageException(
                              "\n2 Error while loading image " +
                              this.m_href.toString() +
                              " : JpegImage - Invalid JPEG Header (bad color space " +
                              this.m_bitmaps[index + 9] + ").");
                        }

                        cont = false;
                        break;

                    } else { // if (uByte(this.m_bitmaps[index + 1]) == headers[headerIndex]) {
                        index += calcBytes(this.m_bitmaps[index + 2],
                                           this.m_bitmaps[index + 3]) + 2;
                    }

                } else {
                    cont = false;
                    throw new FopImageException(
                      "\n2 Error while loading image " +
                      this.m_href.toString() + " : JpegImage - Invalid JPEG Header (bad header byte).");
                }
            }
        } else {
            throw new FopImageException( "\n1 Error while loading image " +
                                         this.m_href.toString() + " : JpegImage - Invalid JPEG Header.");
        }
    }

    private int calcBytes(byte bOne, byte bTwo) {
        return (uByte(bOne) * 256) + uByte(bTwo);
    }

    private int uByte(byte bIn) {
        if (bIn < 0) {
            return 256 + bIn;
        } else {
            return bIn;
        }
    }
}


