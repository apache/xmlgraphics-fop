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

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.rtf.rtflib.rtfdoc;

import org.apache.fop.rtf.rtflib.tools.ImageConstants;
import org.apache.fop.rtf.rtflib.tools.ImageUtil;
//import org.apache.fop.rtf.rtflib.tools.jpeg.Encoder;
//import org.apache.fop.rtf.rtflib.tools.jpeg.JPEGException;

import java.io.BufferedInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Creates an RTF image from an external graphic file.
 * This class belongs to the <fo:external-graphic> tag processing. <br>
 *
 * Supports relative path like "../test.gif", too (01-08-24) <br>
 *
 * Limitations:
 * <li>    Only the image types PNG, JPEG and EMF are supported
 * <li>    The GIF is supported, too, but will be converted to JPG
 * <li>    Only the attributes SRC (required), WIDTH, HEIGHT, SCALING are supported
 * <li>    The SCALING attribute supports (uniform | non-uniform)
 *
 * Known Bugs:
 * <li>    If the emf image has a desired size, the image will be clipped
 * <li>    The emf, jpg & png image will not be displayed in correct size
 *
 *  @author <a href="mailto:a.putz@skynamics.com">Andreas Putz</a>
 *  @author Gianugo Rabellino gianugo@rabellino.it
 */

public class RtfExternalGraphic extends RtfElement {
    /** Exception thrown when an image file/URL cannot be read */
    public static class ExternalGraphicException extends IOException {
        ExternalGraphicException(String reason) {
            super(reason);
        }
    }

    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////


    /**
     * The url of the image
     */
    protected URL url = null;

    /**
     * The height of the image
     */
    protected int height = -1;

    /**
     * The desired percent value of the height
     */
    protected int heightPercent = -1;

    /**
     * The desired height
     */
    protected int heightDesired = -1;

    /**
     * Flag whether the desired height is a percentage
     */
    protected boolean perCentH = false;

    /**
     * The width of the image
     */
    protected int width = -1;

    /**
     * The desired percent value of the width
     */
    protected int widthPercent = -1;

    /**
     * The desired width
     */
    protected int widthDesired = -1;

    /**
     * Flag whether the desired width is a percentage
     */
    protected boolean perCentW = false;

    /**
     * Flag whether the image size shall be adjusted
     */
    protected boolean scaleUniform = false;

    /**
     * Graphic compression rate
     */
     protected int graphicCompressionRate = 80;


    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////


    /**
     * Default constructor.
     * Create an RTF element as a child of given container.
     *
     * @param container a <code>RtfContainer</code> value
     * @param writer a <code>Writer</code> value
     */
    public RtfExternalGraphic(RtfContainer container, Writer writer) throws IOException {
        super (container, writer);
    }

    /**
     * Default constructor.
     *
     * @param container a <code>RtfContainer</code> value
     * @param writer a <code>Writer</code> value
     * @param attributes a <code>RtfAttributes</code> value
     */
    public RtfExternalGraphic(RtfContainer container, Writer writer,
    RtfAttributes attributes) throws IOException {
        super (container, writer, attributes);
    }


    //////////////////////////////////////////////////
    // @@ RtfElement implementation
    //////////////////////////////////////////////////

        /** RtfElement override - catches ExternalGraphicException and writes a warning
         *  message to the document if image cannot be read
         */
    protected void writeRtfContent() throws IOException {
            try {
                writeRtfContentWithException();
            } catch (ExternalGraphicException ie) {
                writeExceptionInRtf(ie);
            }
        }

    /**
     * Writes the RTF content to m_writer - this one throws ExternalGraphicExceptions
     *
     * @exception IOException On error
     */
    protected void writeRtfContentWithException() throws IOException {

        if (writer == null) {
            return;
        }


        if (url == null) {
            throw new ExternalGraphicException("The attribute 'url' of "
                    + "<fo:external-graphic> is null.");
        }

        String linkToRoot = System.getProperty("jfor_link_to_root");
        if (linkToRoot != null) {
            writer.write("{\\field {\\* \\fldinst { INCLUDEPICTURE \"");
            writer.write(linkToRoot);
            File urlFile = new File(url.getFile());
            writer.write(urlFile.getName());
            writer.write("\" \\\\* MERGEFORMAT \\\\d }}}");
            return;
        }

//        getRtfFile ().getLog ().logInfo ("Writing image '" + url + "'.");


        byte[] data = null;
        try {
            // image reading patch provided by Michael Krause <michakurt@web.de>
            final BufferedInputStream bin = new BufferedInputStream(url.openStream());
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            while (true) {
                final int datum = bin.read();
                if (datum == -1) {
                   break;
                }
                bout.write(datum);
             }
             bout.flush();
             data = bout.toByteArray();
        } catch (Exception e) {
            throw new ExternalGraphicException("The attribute 'src' of "
                    + "<fo:external-graphic> has a invalid value: '"
                    + url + "' (" + e + ")");
        }

        if (data == null) {
            return;
        }

        // Determine image file format
        String file = url.getFile ();
        int type = determineImageType(data, file.substring(file.lastIndexOf(".") + 1));

        if (type >= ImageConstants.I_TO_CONVERT_BASIS) {
            // convert
            int to = ImageConstants.CONVERT_TO [type - ImageConstants.I_TO_CONVERT_BASIS];

            if (to == ImageConstants.I_JPG) {
                ByteArrayOutputStream out = null;
//                try {
                    //convert to jpeg
//                    out = new ByteArrayOutputStream();
//                    Encoder jpgEncoder = new Encoder(graphicCompressionRate, out);
//                    jpgEncoder.encodeJPEG(data);
//                    data = out.toByteArray();
//                    type = to;
//                }
//                catch (JPEGException e) {
//                    e.setMessage("Image from tag <fo:external-graphic> could "
//                            + "not be created (src = '" + url + "'");
//                }
//                finally {
                    out.close();
//                }
            } else {
                type = ImageConstants.I_NOT_SUPPORTED;
            }
        }


        if (type == ImageConstants.I_NOT_SUPPORTED) {
            throw new ExternalGraphicException("The tag <fo:external-graphic> "
                    + "does not support "
                    + file.substring(file.lastIndexOf(".") + 1)
                    + " - image type.");
        }

        String rtfImageCode = ImageConstants.RTF_TAGS [type];

        // Writes the beginning of the rtf image

        writeGroupMark(true);
        writeStarControlWord("shppict");
        writeGroupMark(true);
        writeControlWord("pict");

        StringBuffer buf = new StringBuffer(data.length * 3);

        writeControlWord(rtfImageCode);

        if (type == ImageConstants.I_PNG) {
            width = ImageUtil.getIntFromByteArray(data, 16, 4, true);
            height = ImageUtil.getIntFromByteArray(data, 20, 4, true);
        } else if (type == ImageConstants.I_JPG) {
            int basis = -1;
            byte ff = (byte) 0xff;
            byte c0 = (byte) 0xc0;
            for (int i = 0; i < data.length; i++) {
                byte b = data[i];
                if (b != ff) {
                    continue;
                }
                if (i == data.length - 1) {
                    continue;
                }
                b = data[i + 1];
                if (b != c0) {
                    continue;
                }
                basis = i + 5;
                break;
            }

            if (basis != -1) {
                width = ImageUtil.getIntFromByteArray(data, basis + 2, 2, true);
                height = ImageUtil.getIntFromByteArray(data, basis, 2, true);
            }
        } else if (type == ImageConstants.I_EMF) {
            width = ImageUtil.getIntFromByteArray(data, 151, 4, false);
            height = ImageUtil.getIntFromByteArray(data, 155, 4, false);
        }

        // Set image size
        if (width != -1) {
            writeControlWord("picw" + width);
        }
        if (height != -1) {
            writeControlWord("pich" + height);
        }

        if (widthDesired != -1) {
            if (perCentW) {
                writeControlWord("picscalex" + widthDesired);
            } else {
                writeControlWord("picscalex" + widthDesired * 100 / width);
            }

            writeControlWord("picwgoal" + widthDesired);
        } else if (scaleUniform && heightDesired != -1) {
            if (perCentH) {
                writeControlWord("picscalex" + heightDesired);
            } else {
                writeControlWord("picscalex" + heightDesired * 100 / height);
            }
        }

        if (heightDesired != -1) {
            if (perCentH) {
                writeControlWord("picscaley" + heightDesired);
            } else {
                writeControlWord("picscaley" + heightDesired * 100 / height);
            }

            writeControlWord("pichgoal" + heightDesired);
        } else if (scaleUniform && widthDesired != -1) {
            if (perCentW) {
                writeControlWord("picscaley" + widthDesired);
            } else {
                writeControlWord("picscaley" + widthDesired * 100 / width);
            }
        }

        for (int i = 0; i < data.length; i++) {
            int iData = data [i];

            // Make positive byte
            if (iData < 0) {
                iData += 256;
            }

            if (iData < 16) {
                // Set leading zero and append
                buf.append('0');
            }

            buf.append(Integer.toHexString(iData));
        }

        int len = buf.length();
        char[] chars = new char[len];

        buf.getChars(0, len, chars, 0);
        writer.write(chars);

        // Writes the end of RTF image

        writeGroupMark(false);
        writeGroupMark(false);
    }


    //////////////////////////////////////////////////
    // @@ Member access
    //////////////////////////////////////////////////

    /**
     * Sets the desired height of the image.
     *
     * @param theHeight The desired image height
     */
    public void setHeight(String theHeight) {
        this.heightDesired = ImageUtil.getInt(theHeight);
        this.perCentH = ImageUtil.isPercent(theHeight);
    }

    /**
     * Sets the desired width of the image.
     *
     * @param theWidth The desired image width
     */
    public void setWidth(String theWidth) {
        this.widthDesired = ImageUtil.getInt(theWidth);
        this.perCentW = ImageUtil.isPercent(theWidth);
    }

    /**
     * Sets the flag whether the image size shall be adjusted.
     *
     * @param value
     * true    image width or height shall be adjusted automatically\n
     * false   no adjustment
     */
    public void setScaling(String value) {
        if (value.equalsIgnoreCase("uniform")) {
            this.scaleUniform = true;
        }
    }

    /**
     * Sets the url of the image.
     *
     * @param urlString Image url like "file://..."
     * @throws IOException On error
     */
    public void setURL(String urlString) throws IOException {
        URL tmpUrl = null;
        try {
            tmpUrl = new URL (urlString);
        } catch (MalformedURLException e) {
            try {
                tmpUrl = new File (urlString).toURL ();
            } catch (MalformedURLException ee) {
                throw new ExternalGraphicException("The attribute 'src' of "
                        + "<fo:external-graphic> has a invalid value: '"
                        + urlString + "' (" + ee + ")");
            }
        }
        this.url = tmpUrl;
    }

    /**
     * Gets  the compression rate for the image in percent.
     * @return Compression rate
     */
    public int getCompressionRate () {
        return graphicCompressionRate;
    }

    /**
     * Sets the compression rate for the image in percent.
     *
     * @param percent Compression rate
     * @return
     *  true:   The compression rate is valid (0..100)\n
     *  false:  The compression rate is invalid
     */
    public boolean setCompressionRate (int percent) {
        if (percent < 1 || percent > 100) {
            return false;
        }

        graphicCompressionRate = percent;
        return true;
    }


    //////////////////////////////////////////////////
    // @@ Helpers
    //////////////////////////////////////////////////


    /**
     * Determines wheter the image is a jpeg.
     *
     * @param data Image
     *
     * @return
     * true    If JPEG type\n
     * false   Other type
     */
    private boolean isJPEG(byte[] data) {
        // Indentifier "0xFFD8" on position 0
        byte [] pattern = new byte [] {(byte) 0xFF, (byte) 0xD8};

        return ImageUtil.compareHexValues(pattern, data, 0, true);
    }

    /**
     * Determines wheter the image is a png.
     *
     * @param data Image
     *
     * @return
     * true    If PNG type\n
     * false   Other type
     */
    private boolean isPNG(byte[] data) {
        // Indentifier "PNG" on position 1
        byte [] pattern = new byte [] {(byte) 0x50, (byte) 0x4E, (byte) 0x47};

        return ImageUtil.compareHexValues(pattern, data, 1, true);
    }

    /**
     * Determines wheter the image is a emf.
     *
     * @param data Image
     *
     * @return
     * true    If EMF type\n
     * false   Other type
     */
    private boolean isEMF(byte[] data) {
        // No offical Indentifier known
        byte [] pattern = new byte [] {(byte) 0x01, (byte) 0x00, (byte) 0x00};

        return ImageUtil.compareHexValues(pattern, data, 0, true);
    }

    /**
     * Determines wheter the image is a gif.
     *
     * @param data Image
     *
     * @return
     * true    If GIF type\n
     * false   Other type
     */
    private boolean isGIF(byte[] data) {
        // Indentifier "GIF8" on position 0
        byte [] pattern = new byte [] {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38};

        return ImageUtil.compareHexValues(pattern, data, 0, true);
    }

    /**
     * Determines wheter the image is a gif.
     *
     * @param data Image
     *
     * @return
     * true    If BMP type\n
     * false   Other type
     */
    private boolean isBMP(byte[] data) {
        // Indentifier "BM" on position 0
        byte [] pattern = new byte [] {(byte) 0x42, (byte) 0x4D};

        return ImageUtil.compareHexValues(pattern, data, 0, true);
    }

    /**
     * Determine image file format.
     *
     * @param data Image
     * @param ext Image extension
     *
     * @return Image type by ImageConstants.java
     */
    private int determineImageType(byte [] data, String ext) {
        int type = ImageConstants.I_NOT_SUPPORTED;

        if (isPNG(data)) {
            type = ImageConstants.I_PNG;
        } else if (isJPEG(data)) {
            type = ImageConstants.I_JPG_C;
        } else if (isEMF(data)) {
            type = ImageConstants.I_EMF;
        } else if (isGIF(data)) {
            type = ImageConstants.I_GIF;
        } else {
            Object tmp = ImageConstants.SUPPORTED_IMAGE_TYPES.get(ext.toLowerCase());
            if (tmp != null) {
                type = ((Integer) tmp).intValue();
            }
        }

        return type;
    }

    /** true if this element would generate no "useful" RTF content */
    public boolean isEmpty() {
        return url == null;
    }
}
