/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// Java
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;

// AWT
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.BufferedImage;

// JAI
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
// Sun codec
import com.sun.media.jai.codec.FileCacheSeekableStream;
// FOP
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.CCFFilter;
import org.apache.fop.pdf.DCTFilter;
import org.apache.fop.image.analyser.ImageReader;

/**
 * FopImage object using JAI for TIFF images.
 * Allows to store certain compresssed TIFF images directly
 * in the output stream. Does revert to the standard JAI
 * image for anything it doesn't understand.
 * @author Manuel MALL
 * @see AbstractFopImage
 * @see FopImage
 */
public class TiffImage extends JAIImage {
    public TiffImage(URL href) throws FopImageException {
        super(href);
    }

    public TiffImage(URL href,
                    ImageReader imgReader) throws FopImageException {
        super(href, imgReader);
    }

    protected void loadImage() throws FopImageException {
        try {
            InputStream inputStream = this.m_href.openStream();
            /*
             * BufferedInputStream inputStream = this.m_imageReader.getInputStream();
             * inputStream.reset();
             */
            com.sun.media.jai.codec.FileCacheSeekableStream seekableInput =
                new FileCacheSeekableStream(inputStream);

            com.sun.media.jai.codec.TIFFDirectory ifd = new com.sun.media.jai.codec.TIFFDirectory(seekableInput, 0);
            com.sun.media.jai.codec.TIFFField fld = null;

            this.m_height = (int)ifd.getFieldAsLong(0x101);
            this.m_width = (int)ifd.getFieldAsLong(0x100);

            fld = ifd.getField(0x115); // The samples per pixel field
            if (fld != null && fld.getAsInt(0) != 1) {
                throw new FopImageException("Error while loading image "
                                            + this.m_href.toString() + " : "
                                            + this.getClass() + " - "
                                            + "unsupported samples per pixel value " + fld.getAsInt(0));
            }

            this.m_bitsPerPixel = 1;
            fld = ifd.getField(0x102); // The bits per sample field
            if (fld != null) {
                this.m_bitsPerPixel = fld.getAsInt(0);
            }

            this.m_colorSpace = new ColorSpace(ColorSpace.DEVICE_GRAY);
            fld = ifd.getField(0x106); // The photometric interpretation field
            if (fld != null) {
                if (fld.getAsInt(0) == 0) {
                    // All is fine
                }
                else if (fld.getAsInt(0) == 2) {
                    this.m_colorSpace = new ColorSpace(ColorSpace.DEVICE_RGB);
                }
                else {
                    throw new FopImageException("Error while loading image "
                                                + this.m_href.toString() + " : "
                                                + this.getClass() + " - "
                                                + "unsupported photometric interpretation value " + fld.getAsInt(0));
                }
            }

            this.m_isTransparent = false;

            int comp = 1;
            fld = ifd.getField(0x103); // The compression field
            if (fld != null) {
                comp = fld.getAsInt(0);
            }
            if (comp == 1) {
            }
            else if (comp == 3) {
                this.m_compressionType = new CCFFilter();
                this.m_compressionType.setApplied(true);
            }
            else if (comp == 4) {
                CCFFilter ccf = new CCFFilter();
                this.m_compressionType = ccf;
                this.m_compressionType.setApplied(true);
                ccf.setDecodeParms("<< /K -1 /Columns " + this.m_width + " >>");
            }
            else if (comp == 6) {
                this.m_compressionType = new DCTFilter();
                this.m_compressionType.setApplied(true);
            }
            else {
                throw new FopImageException("Error while loading image "
                                            + this.m_href.toString() + " : "
                                            + this.getClass() + " - "
                                            + "unsupported compression value " + comp);
            }

            fld = ifd.getField(0x116); // The rows per strip field
            if (fld != null) {
                if (this.m_height != fld.getAsLong(0)) {
                    throw new FopImageException("Error while loading image "
                                                + this.m_href.toString() + " : "
                                                + this.getClass() + " - "
                                                + "only single strip images supported ");
                }
            }
            long offset = ifd.getFieldAsLong(0x111);
            long length = ifd.getFieldAsLong(0x117);

            byte[] readBuf = new byte[(int)length];
            int bytes_read;

            inputStream.close();
            inputStream = this.m_href.openStream();
            inputStream.skip(offset);
            bytes_read = inputStream.read(readBuf);
            if (bytes_read != length) {
                throw new FopImageException("Error while loading image "
                                            + this.m_href.toString() + " : "
                                            + this.getClass() + " - "
                                            + "length mismatch on read");
            }

            this.m_bitmaps = readBuf;
        } catch (FopImageException fie) {
            org.apache.fop.messaging.MessageHandler.logln("Reverting to TIFF image handling through JAI: "
                                                          + fie.getMessage());
            this.m_compressionType = null;
            super.loadImage();
        } catch (Exception ex) {
            throw new FopImageException("Error while loading image "
                                        + this.m_href.toString() + " : "
                                        + ex.getClass() + " - "
                                        + ex.getMessage());
        }
    }

}

