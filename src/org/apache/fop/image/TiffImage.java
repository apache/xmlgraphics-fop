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
package org.apache.fop.image;

// Java
import java.net.URL;
import java.io.InputStream;

// Sun codec
import com.sun.media.jai.codec.FileCacheSeekableStream;
// FOP
import org.apache.fop.datatypes.ColorSpace;
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

