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
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ColorSpace;

// FOP
import org.apache.fop.image.analyser.ImageReader;
import org.apache.fop.fo.FOUserAgent;

/**
 * FopImage object for JPEG images, Using Java native classes.
 * @author Eric Dalquist
 * @see AbstractFopImage
 * @see FopImage
 */
public class JpegImage extends AbstractFopImage {
    private ICC_Profile iccProfile = null;
    private boolean found_icc_profile = false;
    private boolean found_dimensions = false;

    /**
     * Create a jpeg image with the info.
     *
     * @param imgInfo the image info for this jpeg
     */
    public JpegImage(FopImage.ImageInfo imgInfo) {
        super(imgInfo);
    }

    /**
     * Load the original jpeg data.
     * This loads the original jpeg data and reads the color space,
     * and icc profile if any.
     *
     * @param ua the user agent
     * @return true if loaded false for any error
     */
    protected boolean loadOriginalData(FOUserAgent ua) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayOutputStream iccStream = new ByteArrayOutputStream();
        InputStream inStream;
        byte[] readBuf = new byte[4096];
        int bytes_read;
        int index = 0;
        boolean cont = true;

        try {
            inStream = inputStream;

            while ((bytes_read = inStream.read(readBuf)) != -1) {
                baos.write(readBuf, 0, bytes_read);
            }
            inputStream.close();
            inputStream = null;
        } catch (java.io.IOException ex) {
            ua.getLogger().error("Error while loading image " +
                                         "" + " : " + ex.getClass() +
                                         " - " + ex.getMessage(), ex);
            return false;
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
                            this.m_colorSpace = ColorSpace.getInstance(
                              ColorSpace.CS_GRAY);
                        } else if (this.m_bitmaps[index + 9] == 3) {
                            this.m_colorSpace = ColorSpace.getInstance(
                              ColorSpace.CS_LINEAR_RGB);
                        } else if (this.m_bitmaps[index + 9] == 4) {
                            // howto create CMYK color space
                            this.m_colorSpace = ColorSpace.getInstance(
                              ColorSpace.CS_CIEXYZ);
                        } else {
                            ua.getLogger().error("Unknown ColorSpace for image: "
                                                   + "");
                            return false;
                        }

                        found_dimensions = true;
                        if (found_icc_profile) {
                            cont = false;
                            break;
                        }
                        index += calcBytes(this.m_bitmaps[index + 2],
                                           this.m_bitmaps[index + 3]) + 2;

                    } else if (uByte(this.m_bitmaps[index + 1]) ==
                        226 && this.m_bitmaps.length > (index + 60)) {
                        // Check if ICC profile
                        byte[] icc_string = new byte[11];
                        System.arraycopy(this.m_bitmaps, index + 4,
                                         icc_string, 0, 11);

                        if ("ICC_PROFILE".equals(new String(icc_string))) {
                            int chunkSize = calcBytes(
                                              this.m_bitmaps[index + 2],
                                              this.m_bitmaps[index + 3]) + 2;

                            iccStream.write(this.m_bitmaps,
                                            index + 18, chunkSize - 18);

                        }

                        index += calcBytes(this.m_bitmaps[index + 2],
                                           this.m_bitmaps[index + 3]) + 2;
                    } else {
                        index += calcBytes(this.m_bitmaps[index + 2],
                                           this.m_bitmaps[index + 3]) + 2;
                    }

                } else {
                    cont = false;
                }
            }
        } else {
            ua.getLogger().error("Error while loading "
                                          + "JpegImage - Invalid JPEG Header.");
            return false;
        }
        if (iccStream.size() > 0) {
            byte[] align = new byte[((iccStream.size()) % 8) + 8];
            try {
                iccStream.write(align);
            } catch (Exception e) {
                ua.getLogger().error("Error while loading image "
                                              + " : "
                                              + e.getMessage(), e);
                return false;
            }
            try {
                iccProfile = ICC_Profile.getInstance(iccStream.toByteArray());
            } catch (Exception e) {
                ua.getLogger().error("Invalid ICC profile: " + e, e);
                return false;
            }
        } else if(this.m_colorSpace == null) {
            ua.getLogger().error("ColorSpace not specified for JPEG image");
            return false;
        }
        return true;
    }

    /**
     * Get the ICC profile for this Jpeg image.
     *
     * @return the icc profile or null if not found
     */
    public ICC_Profile getICCProfile() {
        return iccProfile;
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

