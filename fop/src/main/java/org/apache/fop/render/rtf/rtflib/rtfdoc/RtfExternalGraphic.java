/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import org.apache.fop.render.rtf.rtflib.tools.ImageConstants;
import org.apache.fop.render.rtf.rtflib.tools.ImageUtil;

/**
 * <p>Creates an RTF image from an external graphic file.
 * This class belongs to the <fo:external-graphic> tag processing.</p>
 *
 * <p>Supports relative path like "../test.gif", too (01-08-24)</p>
 *
 * <p>Limitations:</p>
 * <ul>
 * <li>    Only the image types PNG, JPEG and EMF are supported
 * <li>    The GIF is supported, too, but will be converted to JPG
 * <li>    Only the attributes SRC (required), WIDTH, HEIGHT, SCALING are supported
 * <li>    The SCALING attribute supports (uniform | non-uniform)
 * </ul>
 *
 * <p>Known Bugs:</p>
 * <ul>
 * <li>    If the emf image has a desired size, the image will be clipped
 * <li>    The emf, jpg & png image will not be displayed in correct size
 * </ul>
 *
 * <p>This work was authored by Andreas Putz (a.putz@skynamics.com) and
 * Gianugo Rabellino (gianugo@rabellino.it).</p>
 */

public class RtfExternalGraphic extends RtfElement {
    /** Exception thrown when an image file/URL cannot be read */
    public static class ExternalGraphicException extends IOException {
        ExternalGraphicException(String reason) {
            super(reason);
        }
    }

    //////////////////////////////////////////////////
    // Supported Formats
    //////////////////////////////////////////////////
    private static class FormatBase {

        /**
         * Determines whether the image is in the according format.
         *
         * @param data Image
         *
         * @return
         * true    If according type\n
         * false   Other type
         */
        public static boolean isFormat(byte[] data) {
            return false;
        }

        /**
         * Convert image data if necessary - for example when format is not supported by rtf.
         *
         * @param format Format type
         * @param data Image
         */
        public FormatBase convert(FormatBase format, byte[] data) {
            return format;
        }

        /**
         * Determine image file format.
         *
         * @param data Image
         *
         * @return Image format class
         */

        public static FormatBase determineFormat(byte[] data) {

            if (FormatPNG.isFormat(data)) {
                return new FormatPNG();
            } else if (FormatJPG.isFormat(data)) {
                return new FormatJPG();
            } else if (FormatEMF.isFormat(data)) {
                return new FormatEMF();
            } else if (FormatGIF.isFormat(data)) {
                return new FormatGIF();
            } else if (FormatBMP.isFormat(data)) {
                return new FormatBMP();
            } else {
                return null;
            }
        }

        /**
         * Get image type.
         *
         * @return Image format class
         */
        public int getType() {
            return ImageConstants.I_NOT_SUPPORTED;
        }

        /**
         * Get rtf tag.
         *
         * @return Rtf tag for image format.
         */
        public String getRtfTag() {
            return "";
        }
    }

    private static class FormatGIF extends FormatBase {
        public static boolean isFormat(byte[] data) {
            // Indentifier "GIF8" on position 0
            byte [] pattern = new byte [] {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38};

            return ImageUtil.compareHexValues(pattern, data, 0, true);
        }

        public int getType() {
            return ImageConstants.I_GIF;
        }
    }

    private static class FormatEMF extends FormatBase {
        public static boolean isFormat(byte[] data) {
            // No offical Indentifier known
            byte [] pattern = new byte [] {(byte) 0x01, (byte) 0x00, (byte) 0x00};

            return ImageUtil.compareHexValues(pattern, data, 0, true);
        }

        public int getType() {
            return ImageConstants.I_EMF;
        }

        public String getRtfTag() {
            return "emfblip";
        }
    }

    private  static class FormatBMP extends FormatBase {
        public static boolean isFormat(byte[] data) {
            byte [] pattern = new byte [] {(byte) 0x42, (byte) 0x4D};

            return ImageUtil.compareHexValues(pattern, data, 0, true);
        }

        public int getType() {
            return ImageConstants.I_BMP;
        }
    }

    private static class FormatJPG extends FormatBase {
        public static boolean isFormat(byte[] data) {
            // Indentifier "0xFFD8" on position 0
            byte [] pattern = new byte [] {(byte) 0xFF, (byte) 0xD8};

            return ImageUtil.compareHexValues(pattern, data, 0, true);
        }

        public int getType() {
            return ImageConstants.I_JPG;
        }

        public String getRtfTag() {
            return "jpegblip";
        }
    }

    private static class FormatPNG extends FormatBase {
        public static boolean isFormat(byte[] data) {
            // Indentifier "PNG" on position 1
            byte [] pattern = new byte [] {(byte) 0x50, (byte) 0x4E, (byte) 0x47};

            return ImageUtil.compareHexValues(pattern, data, 1, true);
        }

        public int getType() {
            return ImageConstants.I_PNG;
        }

        public String getRtfTag() {
            return "pngblip";
        }
    }

    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////


    /**
     * The url of the image
     */
    protected URL url;

    /**
     * The height of the image (in pixels)
     */
    protected int height = -1;

    /**
     * The desired height (in twips)
     */
    protected int heightDesired = -1;

    /**
     * Flag whether the desired height is a percentage
     */
    protected boolean perCentH;

    /**
     * The width of the image (in pixels)
     */
    protected int width = -1;

    /**
     * The desired width (in twips)
     */
    protected int widthDesired = -1;

    /**
     * Flag whether the desired width is a percentage
     */
    protected boolean perCentW;

    /**
     * Flag whether the image size shall be adjusted
     */
    protected boolean scaleUniform;

    /** cropping on left/top/right/bottom edges for \piccrop*N */
    private int[] cropValues = new int[4];

    /**
     * Graphic compression rate
     */
     protected int graphicCompressionRate = 80;

     /** The image data */
     private byte[] imagedata;

     /** The image format */
     private FormatBase imageformat;

    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////


    /**
     * Default constructor.
     * Create an RTF element as a child of given container.
     *
     * @param container a <code>RtfContainer</code> value
     * @param writer a <code>Writer</code> value
     * @throws IOException for I/O problems
     */
    public RtfExternalGraphic(RtfContainer container, Writer writer) throws IOException {
        super(container, writer);
    }

    /**
     * Default constructor.
     *
     * @param container a <code>RtfContainer</code> value
     * @param writer a <code>Writer</code> value
     * @param attributes a <code>RtfAttributes</code> value
     * @throws IOException for I/O problems
     */
    public RtfExternalGraphic(RtfContainer container, Writer writer,
    RtfAttributes attributes) throws IOException {
        super(container, writer, attributes);
    }


    //////////////////////////////////////////////////
    // @@ RtfElement implementation
    //////////////////////////////////////////////////

        /**
         * RtfElement override - catches ExternalGraphicException and writes a warning
         * message to the document if image cannot be read
         * @throws IOException for I/O problems
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


        if (url == null && imagedata == null) {
            throw new ExternalGraphicException(
                    "No image data is available (neither URL, nor in-memory)");
        }

        String linkToRoot = System.getProperty("jfor_link_to_root");
        if (url != null && linkToRoot != null) {
            writer.write("{\\field {\\* \\fldinst { INCLUDEPICTURE \"");
            writer.write(linkToRoot);
            File urlFile = new File(url.getFile());
            writer.write(urlFile.getName());
            writer.write("\" \\\\* MERGEFORMAT \\\\d }}}");
            return;
        }

//        getRtfFile ().getLog ().logInfo ("Writing image '" + url + "'.");


        if (imagedata == null) {
            try {
                final InputStream in = url.openStream();
                try {
                    imagedata = IOUtils.toByteArray(url.openStream());
                } finally {
                    IOUtils.closeQuietly(in);
                }
            } catch (Exception e) {
                throw new ExternalGraphicException("The attribute 'src' of "
                        + "<fo:external-graphic> has a invalid value: '"
                        + url + "' (" + e + ")");
            }
        }

        if (imagedata == null) {
            return;
        }

        // Determine image file format
        String file = (url != null ? url.getFile() : "<unknown>");
        imageformat = FormatBase.determineFormat(imagedata);
        if (imageformat != null) {
            imageformat = imageformat.convert(imageformat, imagedata);
        }

        if (imageformat == null
                || imageformat.getType() == ImageConstants.I_NOT_SUPPORTED
                || "".equals(imageformat.getRtfTag())) {
            throw new ExternalGraphicException("The tag <fo:external-graphic> "
                    + "does not support "
                    + file.substring(file.lastIndexOf(".") + 1)
                    + " - image type.");
        }

        // Writes the beginning of the rtf image

        writeGroupMark(true);
        writeStarControlWord("shppict");
        writeGroupMark(true);
        writeControlWord("pict");

        StringBuffer buf = new StringBuffer(imagedata.length * 3);

        writeControlWord(imageformat.getRtfTag());

        computeImageSize();
        writeSizeInfo();
        writeAttributes(getRtfAttributes(), null);

        for (int i = 0; i < imagedata.length; i++) {
            int iData = imagedata [i];

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

    private void computeImageSize() {
        if (imageformat.getType() == ImageConstants.I_PNG) {
            width = ImageUtil.getIntFromByteArray(imagedata, 16, 4, true);
            height = ImageUtil.getIntFromByteArray(imagedata, 20, 4, true);
        } else if (imageformat.getType() == ImageConstants.I_JPG) {
            int basis = -1;
            byte ff = (byte) 0xff;
            byte c0 = (byte) 0xc0;
            for (int i = 0; i < imagedata.length; i++) {
                byte b = imagedata[i];
                if (b != ff) {
                    continue;
                }
                if (i == imagedata.length - 1) {
                    continue;
                }
                b = imagedata[i + 1];
                if (b != c0) {
                    continue;
                }
                basis = i + 5;
                break;
            }

            if (basis != -1) {
                width = ImageUtil.getIntFromByteArray(imagedata, basis + 2, 2, true);
                height = ImageUtil.getIntFromByteArray(imagedata, basis, 2, true);
            }
        } else if (imageformat.getType() == ImageConstants.I_EMF) {
            int i = 0;

            i = ImageUtil.getIntFromByteArray(imagedata, 151, 4, false);
            if (i != 0) {
                width = i;
            }

            i = ImageUtil.getIntFromByteArray(imagedata, 155, 4, false);
            if (i != 0) {
                height = i;
            }

        }
    }

    private void writeSizeInfo() throws IOException {
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
                //writeControlWord("picscalex" + widthDesired * 100 / width);
                writeControlWord("picwgoal" + widthDesired);
            }

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
                //writeControlWord("picscaley" + heightDesired * 100 / height);
                writeControlWord("pichgoal" + heightDesired);
            }

        } else if (scaleUniform && widthDesired != -1) {
            if (perCentW) {
                writeControlWord("picscaley" + widthDesired);
            } else {
                writeControlWord("picscaley" + widthDesired * 100 / width);
            }
        }

        if (this.cropValues[0] != 0) {
            writeOneAttribute("piccropl", new Integer(this.cropValues[0]));
        }
        if (this.cropValues[1] != 0) {
            writeOneAttribute("piccropt", new Integer(this.cropValues[1]));
        }
        if (this.cropValues[2] != 0) {
            writeOneAttribute("piccropr", new Integer(this.cropValues[2]));
        }
        if (this.cropValues[3] != 0) {
            writeOneAttribute("piccropb", new Integer(this.cropValues[3]));
        }
    }

    //////////////////////////////////////////////////
    // @@ Member access
    //////////////////////////////////////////////////

    /**
     * Sets the desired height of the image.
     *
     * @param theHeight The desired image height (as a string in twips or as a percentage)
     */
    public void setHeight(String theHeight) {
        this.heightDesired = ImageUtil.getInt(theHeight);
        this.perCentH = ImageUtil.isPercent(theHeight);
    }

    /**
     * Sets the desired width of the image.
     *
     * @param theWidth The desired image width (as a string in twips or as a percentage)
     */
    public void setWidth(String theWidth) {
        this.widthDesired = ImageUtil.getInt(theWidth);
        this.perCentW = ImageUtil.isPercent(theWidth);
    }

    /**
     * Sets the desired width of the image.
     * @param twips The desired image width (in twips)
     */
    public void setWidthTwips(int twips) {
        this.widthDesired = twips;
        this.perCentW = false;
    }

    /**
     * Sets the desired height of the image.
     * @param twips The desired image height (in twips)
     */
    public void setHeightTwips(int twips) {
        this.heightDesired = twips;
        this.perCentH = false;
    }

    /**
     * Sets the flag whether the image size shall be adjusted.
     *
     * @param value
     * true    image width or height shall be adjusted automatically\n
     * false   no adjustment
     */
    public void setScaling(String value) {
        setUniformScaling("uniform".equalsIgnoreCase(value));
    }

    /**
     * Sets the flag whether the image size shall be adjusted.
     *
     * @param uniform
     *                true    image width or height shall be adjusted automatically\n
     *                false   no adjustment
     */
    public void setUniformScaling(boolean uniform) {
        this.scaleUniform = uniform;
    }

    /**
     * Sets cropping values for all four edges for the \piccrop*N commands.
     * A positive value crops toward the center of the picture;
     * a negative value crops away from the center, adding a space border around the picture
     * @param left left cropping value (in twips)
     * @param top top cropping value (in twips)
     * @param right right cropping value (in twips)
     * @param bottom bottom cropping value (in twips)
     */
    public void setCropping(int left, int top, int right, int bottom) {
        this.cropValues[0] = left;
        this.cropValues[1] = top;
        this.cropValues[2] = right;
        this.cropValues[3] = bottom;
    }

    /**
     * Sets the binary imagedata of the image.
     *
     * @param data  binary imagedata as read from file.
     * @throws IOException On error
     */
    public void setImageData(byte[] data) throws IOException {
        this.imagedata = data;
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
            tmpUrl = new URL(urlString);
        } catch (MalformedURLException e) {
            try {
                tmpUrl = new File(urlString).toURI().toURL();
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
    public int getCompressionRate() {
        return graphicCompressionRate;
    }

    /**
     * Sets the compression rate for the image in percent.
     *
     * @param percent Compression rate
     * @return true if the compression rate is valid (0..100), false if invalid
     */
    public boolean setCompressionRate(int percent) {
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
     * @return true if this element would generate no "useful" RTF content
     */
    public boolean isEmpty() {
        return url == null;
    }
}
