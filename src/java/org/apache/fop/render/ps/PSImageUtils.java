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
 
package org.apache.fop.render.ps;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.image.EPSImage;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.JpegImage;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;

/**
 * Utility code for rendering images in PostScript. 
 */
public class PSImageUtils extends org.apache.xmlgraphics.ps.PSImageUtils {

    /** logging instance */
    protected static Log log = LogFactory.getLog(PSImageUtils.class);

    /**
     * Renders a bitmap image to PostScript.
     * @param img image to render
     * @param x x position
     * @param y y position
     * @param w width
     * @param h height
     * @param gen PS generator
     * @throws IOException In case of an I/O problem while rendering the image
     */
    public static void renderBitmapImage(FopImage img, 
                float x, float y, float w, float h, PSGenerator gen)
                    throws IOException {
        boolean isJPEG = (img instanceof JpegImage && (gen.getPSLevel() >= 3));
        byte[] imgmap = convertImageToRawBitmapArray(img, isJPEG);
        if (imgmap == null) {
            gen.commentln("%Image data is not available: " + img);
            return; //Image cannot be converted 
        }
        
        String imgDescription = img.getMimeType() + " " + img.getOriginalURI();
        Dimension imgDim = new Dimension(img.getWidth(), img.getHeight());
        Rectangle2D targetRect = new Rectangle2D.Double(x, y, w, h);
        writeImage(imgmap, imgDim, imgDescription, targetRect, isJPEG, 
                img.getColorSpace(), gen);
    }

    /**
     * Renders a bitmap image (as form) to PostScript.
     * @param img image to render
     * @param form the form resource
     * @param x x position
     * @param y y position
     * @param w width
     * @param h height
     * @param gen PS generator
     * @throws IOException In case of an I/O problem while rendering the image
     */
    public static void renderForm(FopImage img, PSResource form, 
                float x, float y, float w, float h, PSGenerator gen)
                    throws IOException {
        Rectangle2D targetRect = new Rectangle2D.Double(x, y, w, h);
        paintForm(form, targetRect, gen);
    }
    
    /**
     * Generates a form resource for a FopImage in PostScript.
     * @param img image to render
     * @param form the form resource
     * @param gen PS generator
     * @throws IOException In case of an I/O problem while rendering the image
     */
    public static void generateFormResourceForImage(FopImage img, PSResource form,
                PSGenerator gen) throws IOException {
        boolean isJPEG = (img instanceof JpegImage && (gen.getPSLevel() >= 3));
        byte[] imgmap = convertImageToRawBitmapArray(img, isJPEG);
        if (imgmap == null) {
            gen.commentln("%Image data is not available: " + img);
            return; //Image cannot be converted 
        }
        
        String imgDescription = img.getMimeType() + " " + img.getOriginalURI();
        Dimension imgDim = new Dimension(img.getWidth(), img.getHeight());
        writeReusableImage(imgmap, imgDim, form.getName(), imgDescription, isJPEG, 
                img.getColorSpace(), gen);
    }

    private static byte[] convertImageToRawBitmapArray(FopImage img, boolean allowUndecodedJPEG)
                throws IOException {
        if (img instanceof JpegImage && allowUndecodedJPEG) {
            if (!img.load(FopImage.ORIGINAL_DATA)) {
                return null;
            }
        } else {
            if (!img.load(FopImage.BITMAP)) {
                return null;
            }
        }
        byte[] imgmap;
        if (img.getBitmapsSize() > 0) {
            imgmap = img.getBitmaps();
        } else {
            imgmap = img.getRessourceBytes();
        }
        return imgmap;
    }

    /**
     * Renders an EPS image to PostScript.
     * @param img EPS image to render
     * @param x x position
     * @param y y position
     * @param w width
     * @param h height
     * @param gen PS generator
     */
    public static void renderEPS(EPSImage img, 
            float x, float y, float w, float h,
            PSGenerator gen) {
        try {
            if (!img.load(FopImage.ORIGINAL_DATA)) {
                gen.commentln("%EPS image could not be processed: " + img);
                return;
            }
            int[] bbox = img.getBBox();
            int bboxw = bbox[2] - bbox[0];
            int bboxh = bbox[3] - bbox[1];
            String name = img.getDocName();
            if (name == null || name.length() == 0) {
                name = img.getOriginalURI();
            }
            renderEPS(img.getEPSImage(), name,
                x, y, w, h,
                bbox[0], bbox[1], bboxw, bboxh, gen);

        } catch (Exception e) {
            log.error("PSRenderer.renderImageArea(): Error rendering bitmap ("
                                   + e.getMessage() + ")", e);
        }
    }

}
