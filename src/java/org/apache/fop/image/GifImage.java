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

package org.apache.fop.image;

// Java
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.color.ColorSpace;
import java.awt.Color;
import java.io.InputStream;
import java.io.IOException;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

/**
 * FopImage object for GIF images, using Java native classes.
 * @author Eric SCHAEFFER
 * @author Modified by Eric Dalquist - 9/14/2001 - ebdalqui@mtu.edu
 * @see AbstractFopImage
 * @see FopImage
 */
public class GifImage extends AbstractFopImage {

    /**
     * Create a new gif image.
     *
     * @param imgInfo the image info for this gif image
     */
    public GifImage(FopImage.ImageInfo imgInfo) {
        super(imgInfo);
    }

    /**
     * Load the bitmap for this gif image.
     * This loads the data and creates a bitmap byte array
     * of the image data.
     * To decode the image a dummy URLConnection is used that
     * will do the conversion.
     *
     * @return True if the load process succeeded
     */
    protected boolean loadBitmap() {
        int[] tmpMap = null;
        try {
            URLConnection con = new DummyConnection(inputStream);

            ImageProducer ip = (ImageProducer) con.getContent();
            if (ip == null) {
                return false;
            }
            FopImageConsumer consumer = new FopImageConsumer(ip);
            ip.startProduction(consumer);

            //Load the image into memory
            while (!consumer.isImageReady()) {
                Thread.sleep(500);
            }

            this.height = consumer.getHeight();
            this.width = consumer.getWidth();

            try {
                tmpMap = consumer.getImage();
            } catch (Exception ex) {
                log.error("Image grabbing interrupted : "
                              + ex.getMessage(), ex);
                return false;
            }

            ColorModel cm = consumer.getColorModel();
            this.bitsPerPixel = 8;
            // this.bitsPerPixel = cm.getPixelSize();
            this.colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
            if (cm.hasAlpha()) {
                // java.awt.Transparency. BITMASK or OPAQUE or TRANSLUCENT
                int transparencyType = cm.getTransparency();

                if (transparencyType == java.awt.Transparency.OPAQUE) {
                    this.isTransparent = false;
                } else if (transparencyType == java.awt.Transparency.BITMASK) {
                    if (cm instanceof IndexColorModel) {
                        IndexColorModel indexcm = (IndexColorModel) cm;
                        this.isTransparent = false;
                        byte[] alphas = new byte[indexcm.getMapSize()];
                        byte[] reds = new byte[indexcm.getMapSize()];
                        byte[] greens = new byte[indexcm.getMapSize()];
                        byte[] blues = new byte[indexcm.getMapSize()];
                        indexcm.getAlphas(alphas);
                        indexcm.getReds(reds);
                        indexcm.getGreens(greens);
                        indexcm.getBlues(blues);
                        for (int i = 0;
                                i < indexcm.getMapSize();
                                i++) {
                            if ((alphas[i] & 0xFF) == 0) {
                                this.isTransparent = true;
                                this.transparentColor = new Color(
                                                            (int)(reds[i] & 0xFF),
                                                            (int)(greens[i] & 0xFF),
                                                            (int)(blues[i] & 0xFF));
                                break;
                            }
                        }
                    } else {
                        // TRANSLUCENT
                        /*
                         * this.isTransparent = false;
                         * for (int i = 0; i < this.width * this.height; i++) {
                         * if (cm.getAlpha(tmpMap[i]) == 0) {
                         * this.isTransparent = true;
                         * this.transparentColor = new PDFColor(cm.getRed(tmpMap[i]),
                         *       cm.getGreen(tmpMap[i]), cm.getBlue(tmpMap[i]));
                         * break;
                         * }
                         * }
                         */
                        // use special API...
                        this.isTransparent = false;
                    }
                } else {
                    this.isTransparent = false;
                }
            } else {
                this.isTransparent = false;
            }
        } catch (Exception ex) {
            log.error("Error while loading image (Gif): " + ex.getMessage(), ex);
            return false;
        } finally {
            IOUtils.closeQuietly(inputStream);
            inputStream = null;
        }

        // Should take care of the ColorSpace and bitsPerPixel
        this.bitmaps = new byte[this.width * this.height * 3];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                int p = tmpMap[i * this.width + j];
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = (p) & 0xFF;
                this.bitmaps[3 * (i * this.width + j)] = (byte)(r & 0xFF);
                this.bitmaps[3 * (i * this.width + j) + 1] = (byte)(g & 0xFF);
                this.bitmaps[3 * (i * this.width + j) + 2] = (byte)(b & 0xFF);
            }
        }
        return true;
    }

    /** @see org.apache.fop.image.AbstractFopImage#loadOriginalData() */
    protected boolean loadOriginalData() {
        return loadDefaultOriginalData();
    }
    
    /**
     * A dummy url connection for a gif image in an input stream.
     */
    protected static class DummyConnection extends URLConnection {
        private InputStream inputStream;

        DummyConnection(InputStream is) {
            super(null);
            inputStream = is;
        }

        /**
         * @see java.net.URLConnection#getInputStream()
         */
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }

        /**
         * @see java.net.URLConnection#connect()
         */
        public void connect() throws IOException {
            // do nothing
        }

        /**
         * @see java.net.URLConnection#getContentType()
         */
        public String getContentType() {
            return "image/gif";
        }

        /**
         * @see java.net.URLConnection#getContentLength()
         */
        public int getContentLength() {
            try {
                return inputStream.available();
            } catch (IOException e) {
                return -1;
            }
        }

    }
}

