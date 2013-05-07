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

package org.apache.fop.visual;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.util.ParsedURL;
import org.apache.commons.io.IOUtils;

/**
 * Helper class to visually compare two bitmap images.
 * <p>
 * This class was created by extracting reusable code from
 * org.apache.batik.test.util.ImageCompareText (Apache Batik)
 * into this separate class.
 * <p>
 * TODO Move as utility class to XML Graphics Commons when possible
 */
public class BitmapComparator {

    /**
     * Builds a new BufferedImage that is the difference between the two input images
     * @param ref the reference bitmap
     * @param gen the newly generated bitmap
     * @return the diff bitmap
     */
    public static BufferedImage buildDiffImage(BufferedImage ref,
                                               BufferedImage gen) {
        BufferedImage diff = new BufferedImage(ref.getWidth(),
                                               ref.getHeight(),
                                               BufferedImage.TYPE_INT_ARGB);
        WritableRaster refWR = ref.getRaster();
        WritableRaster genWR = gen.getRaster();
        WritableRaster dstWR = diff.getRaster();

        boolean refPre = ref.isAlphaPremultiplied();
        if (!refPre) {
            ColorModel     cm = ref.getColorModel();
            cm = GraphicsUtil.coerceData(refWR, cm, true);
            ref = new BufferedImage(cm, refWR, true, null);
        }
        boolean genPre = gen.isAlphaPremultiplied();
        if (!genPre) {
            ColorModel     cm = gen.getColorModel();
            cm = GraphicsUtil.coerceData(genWR, cm, true);
            gen = new BufferedImage(cm, genWR, true, null);
        }


        int w = ref.getWidth();
        int h = ref.getHeight();

        int y, i, val;
        int [] refPix = null;
        int [] genPix = null;
        for (y = 0; y < h; y++) {
            refPix = refWR.getPixels (0, y, w, 1, refPix);
            genPix = genWR.getPixels (0, y, w, 1, genPix);
            for (i = 0; i < refPix.length; i++) {
                // val = ((genPix[i] - refPix[i]) * 5) + 128;
                val = ((refPix[i] - genPix[i]) * 10) + 128;
                if ((val & 0xFFFFFF00) != 0) {
                    if ((val & 0x80000000) != 0) {
                        val = 0;
                    } else {
                        val = 255;
                    }
                }
                genPix[i] = val;
            }
            dstWR.setPixels(0, y, w, 1, genPix);
        }

        if (!genPre) {
            ColorModel cm = gen.getColorModel();
            cm = GraphicsUtil.coerceData(genWR, cm, false);
        }

        if (!refPre) {
            ColorModel cm = ref.getColorModel();
            cm = GraphicsUtil.coerceData(refWR, cm, false);
        }

        return diff;
    }

    /**
     * Builds a combined image that places a number of images next to each other for
     * manual, visual comparison.
     * @param images the array of bitmaps
     * @return the combined image
     */
    public static BufferedImage buildCompareImage(BufferedImage[] images) {
        BufferedImage cmp = new BufferedImage(
                images[0].getWidth() * images.length,
                images[0].getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = cmp.createGraphics();
        g.setPaint(Color.white);
        g.fillRect(0, 0, cmp.getWidth(), cmp.getHeight());
        int lastWidth = 0;
        for (int i = 0; i < images.length; i++) {
            if (lastWidth > 0) {
                g.translate(lastWidth, 0);
            }
            if (images[i] != null) {
                g.drawImage(images[i], 0, 0, null);
                lastWidth = images[i].getWidth();
            } else {
                lastWidth = 20; //Maybe add a special placeholder image instead later
            }
        }
        g.dispose();

        return cmp;
    }

    /**
     * Builds a combined image that places two images next to each other for
     * manual, visual comparison.
     * @param ref the reference image
     * @param gen the actual image
     * @return the combined image
     */
    public static BufferedImage buildCompareImage(BufferedImage ref,
            BufferedImage gen) {
        return buildCompareImage(new BufferedImage[] {ref, gen});
    }

    /**
     * Loads an image from a URL
     * @param url the URL to the image
     * @return the bitmap as BufferedImage
     * TODO This method doesn't close the InputStream opened by the URL.
     */
    public static BufferedImage getImage(URL url) {
        ImageTagRegistry reg = ImageTagRegistry.getRegistry();
        Filter filt = reg.readURL(new ParsedURL(url));
        if (filt == null) {
            return null;
        }

        RenderedImage red = filt.createDefaultRendering();
        if (red == null) {
            return null;
        }

        BufferedImage img = new BufferedImage(red.getWidth(),
                                              red.getHeight(),
                                              BufferedImage.TYPE_INT_ARGB);
        red.copyData(img.getRaster());

        return img;
    }

    /**
     * Loads an image from a URL
     * @param bitmapFile the bitmap file
     * @return the bitmap as BufferedImage
     */
    public static BufferedImage getImage(File bitmapFile) {
        try {
            InputStream in = new java.io.FileInputStream(bitmapFile);
            try {
                in = new java.io.BufferedInputStream(in);

                ImageTagRegistry reg = ImageTagRegistry.getRegistry();
                Filter filt = reg.readStream(in);
                if (filt == null) {
                    return null;
                }

                RenderedImage red = filt.createDefaultRendering();
                if (red == null) {
                    return null;
                }

                BufferedImage img = new BufferedImage(red.getWidth(),
                                                      red.getHeight(),
                                                      BufferedImage.TYPE_INT_ARGB);
                red.copyData(img.getRaster());
                return img;
            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (IOException e) {
            return null;
        }
    }

}
