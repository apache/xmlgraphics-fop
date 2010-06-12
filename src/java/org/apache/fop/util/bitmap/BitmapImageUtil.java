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

package org.apache.fop.util.bitmap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;

/**
 * Utility method for dealing with bitmap images.
 */
public class BitmapImageUtil {

    /**
     * Indicates whether an image is a monochrome (1 bit black and white) image.
     * @param img the image
     * @return true if it's a monochrome image
     */
    public static final boolean isMonochromeImage(RenderedImage img) {
        return (getColorIndexSize(img) == 2);
    }

    /**
     * Indicates whether a zero bit indicates a black/dark pixel for a monochrome image.
     * @param img the image (must be 1 bit monochrome)
     * @return true if a zero bit indicates a black/dark pixel, false for a white/bright pixel
     */
    public static final boolean isZeroBlack(RenderedImage img) {
        if (!isMonochromeImage(img)) {
            throw new IllegalArgumentException("Image is not a monochrome image!");
        }
        IndexColorModel icm = (IndexColorModel)img.getColorModel();
        int gray0 = convertToGray(icm.getRGB(0));
        int gray1 = convertToGray(icm.getRGB(1));
        return gray0 < gray1;
    }

    /**
     * Convert an RGB color value to a grayscale from 0 to 100.
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @return the gray value
     */
    public static final int convertToGray(int r, int g, int b) {
        return (r * 30 + g * 59 + b * 11) / 100;
    }

    /**
     * Convert an RGB color value to a grayscale from 0 to 100.
     * @param rgb the RGB value
     * @return the gray value
     */
    public static final int convertToGray(int rgb) {
        int r = (rgb & 0xFF0000) >> 16;
        int g = (rgb & 0xFF00) >> 8;
        int b = rgb & 0xFF;
        return convertToGray(r, g, b);
    }

    /**
     * Returns the size of the color index if the given image has one.
     * @param img the image
     * @return the size of the color index or 0 if there's no color index
     */
    public static final int getColorIndexSize(RenderedImage img) {
        ColorModel cm = img.getColorModel();
        if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)cm;
            return icm.getMapSize();
        } else {
            return 0;
        }
    }

    /**
     * Indicates whether an image is a grayscale image.
     * @param img the image
     * @return true if it's a grayscale image
     */
    public static final boolean isGrayscaleImage(RenderedImage img) {
        return (img.getColorModel().getColorSpace().getNumComponents() == 1);
    }

    /**
     * Converts an image to sRGB. Optionally, the image can be scaled.
     * @param img the image to be converted
     * @param targetDimension the new target dimensions or null if no scaling is necessary
     * @return the sRGB image
     */
    public static final BufferedImage convertTosRGB(RenderedImage img,
            Dimension targetDimension) {
        return convertAndScaleImage(img, targetDimension, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Converts an image to a grayscale (8 bits) image. Optionally, the image can be scaled.
     * @param img the image to be converted
     * @param targetDimension the new target dimensions or null if no scaling is necessary
     * @return the grayscale image
     */
    public static final BufferedImage convertToGrayscale(RenderedImage img,
            Dimension targetDimension) {
        return convertAndScaleImage(img, targetDimension, BufferedImage.TYPE_BYTE_GRAY);
    }

    /**
     * Converts an image to a monochrome 1-bit image. Optionally, the image can be scaled.
     * @param img the image to be converted
     * @param targetDimension the new target dimensions or null if no scaling is necessary
     * @return the monochrome image
     */
    public static final BufferedImage convertToMonochrome(RenderedImage img,
            Dimension targetDimension) {
        return toBufferedImage(convertToMonochrome(img, targetDimension, 0.0f));
    }

    /**
     * Converts an image to a monochrome 1-bit image. Optionally, the image can be scaled.
     * @param img the image to be converted
     * @param targetDimension the new target dimensions or null if no scaling is necessary
     * @param quality Defines the desired quality level for the conversion.
     *                  Valid values: a value between 0.0f (fastest) and 1.0f (best)
     * @return the monochrome image
     */
    public static final RenderedImage convertToMonochrome(RenderedImage img,
            Dimension targetDimension, float quality) {
        if (!isMonochromeImage(img)) {
            if (quality >= 0.5f) {
                BufferedImage bi;
                Dimension orgDim = new Dimension(img.getWidth(), img.getHeight());
                if (targetDimension != null && !orgDim.equals(targetDimension)) {
                    //Scale only before dithering
                    ColorModel cm = img.getColorModel();
                    BufferedImage tgt = new BufferedImage(cm,
                            cm.createCompatibleWritableRaster(
                                    targetDimension.width, targetDimension.height),
                            cm.isAlphaPremultiplied(), null);
                    transferImage(img, tgt);
                    bi = tgt;
                } else {
                    bi = toBufferedImage(img);
                }
                //Now convert to monochrome (dithering if available)
                MonochromeBitmapConverter converter = createDefaultMonochromeBitmapConverter();
                if (quality >= 0.8f) {
                    //Activates error diffusion if JAI is available
                    converter.setHint("quality", Boolean.TRUE.toString());
                    //Need to convert to grayscale first since otherwise, there may be encoding
                    //problems later with the images JAI can generate.
                    bi = convertToGrayscale(bi, targetDimension);
                }
                try {
                    return converter.convertToMonochrome(bi);
                } catch (Exception e) {
                    //Provide a fallback if exotic formats are encountered
                    bi = convertToGrayscale(bi, targetDimension);
                    return converter.convertToMonochrome(bi);
                }
            }
        }
        return convertAndScaleImage(img, targetDimension, BufferedImage.TYPE_BYTE_BINARY);
    }

    private static BufferedImage convertAndScaleImage(RenderedImage img,
            Dimension targetDimension, int imageType) {
        Dimension bmpDimension = targetDimension;
        if (bmpDimension == null) {
            bmpDimension = new Dimension(img.getWidth(), img.getHeight());
        }
        BufferedImage target = new BufferedImage(bmpDimension.width, bmpDimension.height,
                imageType);
        transferImage(img, target);
        return target;
    }

    /**
     * Returns a BufferedImage based on the given RenderedImage. In the easiest case,
     * this is a simple typecast. Otherwise, the image is converted to a BufferedImage.
     * @param img the original image
     * @return the buffered image
     */
    public static BufferedImage toBufferedImage(RenderedImage img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage)img;
        } else {
            WritableRaster wr = img.getColorModel().createCompatibleWritableRaster(
                    img.getWidth(), img.getHeight());
            boolean premult = img.getColorModel().isAlphaPremultiplied();
            return new BufferedImage(img.getColorModel(), wr, premult, null);
        }
    }

    private static void transferImage(RenderedImage source, BufferedImage target) {
        Graphics2D g2d = target.createGraphics();
        try {
            g2d.setBackground(Color.white);
            g2d.setColor(Color.black);
            g2d.clearRect(0, 0, target.getWidth(), target.getHeight());

            AffineTransform at = new AffineTransform();
            if (source.getWidth() != target.getWidth()
                    || source.getHeight() != target.getHeight()) {
                double sx = target.getWidth() / (double)source.getWidth();
                double sy = target.getHeight() / (double)source.getHeight();
                at.scale(sx, sy);
            }
            g2d.drawRenderedImage(source, at);
        } finally {
            g2d.dispose();
        }
    }

    public static MonochromeBitmapConverter createDefaultMonochromeBitmapConverter() {
        MonochromeBitmapConverter converter = null;
        try {
            String clName = "org.apache.fop.util.bitmap.JAIMonochromeBitmapConverter";
            Class clazz = Class.forName(clName);
            converter = (MonochromeBitmapConverter)clazz.newInstance();
        } catch (ClassNotFoundException cnfe) {
            // Class was not compiled so is not available. Simply ignore.
        } catch (LinkageError le) {
            // This can happen if fop was build with support for a
            // particular provider (e.g. a binary fop distribution)
            // but the required support files (i.e. JAI) are not
            // available in the current runtime environment.
            // Simply continue with the backup implementation.
        } catch (InstantiationException e) {
            // Problem instantiating the class, simply continue with the backup implementation
        } catch (IllegalAccessException e) {
            // Problem instantiating the class, simply continue with the backup implementation
        }
        if (converter == null) {
            converter = new DefaultMonochromeBitmapConverter();
        }
        return converter;
    }


}
