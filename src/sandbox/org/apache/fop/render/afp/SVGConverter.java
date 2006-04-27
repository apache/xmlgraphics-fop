/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.render.afp;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.image.XMLImage;

/**
 * Helper class for converting SVG to bitmap images.
 */
public class SVGConverter extends ImageTranscoder {
    
    public void writeImage(BufferedImage img, TranscoderOutput output) {
        OutputStream os = output.getOutputStream();
        int w  = img.getWidth();
        int h  = img.getHeight();
        int[] tmpMap = img.getRGB(0, 0, w, h, null, 0, w);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int p = tmpMap[i * w + j];
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = (p) & 0xFF;
                try {
                    os.write((byte)(r & 0xFF));
                    os.write((byte)(g & 0xFF));
                    os.write((byte)(b & 0xFF));
                } catch (java.io.IOException ioex) {
                    
                }
            }
        }
    }
    
    public BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    /** logger instance */
    private static Log log = LogFactory.getLog(SVGConverter.class);

    /**
     * Converts a SVG image to a TIFF bitmap.
     * @param image the SVG image
     * @return a byte array containing the TIFF image
     */
    public static byte[] convertToTIFF(XMLImage image) {
        // TIFFTranscoder transcoder = new TIFFTranscoder();
        // JPEGTranscoder transcoder = new JPEGTranscoder();
        SVGConverter transcoder = new SVGConverter();
        transcoder.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 
                new Float(25.4f / 72)); //300dpi should be enough for now.
        transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, new Float((float)image.getWidth()));
        transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, new Float((float)image.getHeight()));
        TranscoderInput input = new TranscoderInput(image.getDocument());
        ByteArrayOutputStream baout = new ByteArrayOutputStream(image.getWidth() * image.getHeight() * 3);
        TranscoderOutput output = new TranscoderOutput(baout);
        try {
            transcoder.transcode(input, output);
            return baout.toByteArray();
        } catch (TranscoderException e) {
            log.error(e);
            return null;
        }
    }
    
    public static void main(String args[]) {
        // TIFFTranscoder transcoder = new TIFFTranscoder();
        // JPEGTranscoder transcoder = new JPEGTranscoder();
        SVGConverter transcoder = new SVGConverter();
        transcoder.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 
                new Float(25.4f / 300)); //300dpi should be enough for now.
        TranscoderInput input = new TranscoderInput("file:///home/mm/fop-trunk/test/resources/images/img.svg");
        try {
            java.io.FileOutputStream out = new java.io.FileOutputStream("/home/mm/fop-trunk/img.raw");;
            TranscoderOutput output = new TranscoderOutput(out);
            transcoder.transcode(input, output);
        } catch (Exception e) {
            log.error(e);
        }
        
    }
    
}
