/*
 * Copyright 2004-2006 The Apache Software Foundation.
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
 
package org.apache.fop.render.ps;

import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.image.EPSImage;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.JpegImage;
import org.apache.fop.util.ASCII85OutputStream;
import org.apache.fop.util.Finalizable;
import org.apache.fop.util.FlateEncodeOutputStream;
import org.apache.fop.util.RunLengthEncodeOutputStream;

/**
 * Utility code for rendering images in PostScript. 
 */
public class PSImageUtils {

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
        if (img instanceof JpegImage) {
            if (!img.load(FopImage.ORIGINAL_DATA)) {
                gen.commentln("%JPEG image could not be processed: " + img);
                return;
            }
        } else {
            if (!img.load(FopImage.BITMAP)) {
                gen.commentln("%Bitmap image could not be processed: " + img);
                return;
            }
        }
        byte[] imgmap;
        if (img.getBitmapsSize() > 0) {
            imgmap = img.getBitmaps();
        } else {
            imgmap = img.getRessourceBytes();
        }
        
        String imgName = img.getMimeType() + " " + img.getOriginalURI();
        Dimension imgDim = new Dimension(img.getWidth(), img.getHeight());
        Rectangle2D targetRect = new Rectangle2D.Double(x, y, w, h);
        boolean isJPEG = (img instanceof JpegImage);
        writeImage(imgmap, imgDim, imgName, targetRect, isJPEG, 
                img.getColorSpace(), gen);
    }

    private static void writeImage(byte[] img,
            Dimension imgDim, String imgName,
            Rectangle2D targetRect, 
            boolean isJPEG, ColorSpace colorSpace,
            PSGenerator gen) throws IOException {
        boolean iscolor = colorSpace.getType() != ColorSpace.CS_GRAY;

        gen.saveGraphicsState();
        gen.writeln(gen.formatDouble(targetRect.getX()) + " " 
                + gen.formatDouble(targetRect.getY()) + " translate");
        gen.writeln(gen.formatDouble(targetRect.getWidth()) + " " 
                + gen.formatDouble(targetRect.getHeight()) + " scale");

        gen.commentln("%FOPBeginBitmap: " + imgName);
        if (colorSpace.getType() == ColorSpace.TYPE_CMYK) {
            gen.writeln("/DeviceCMYK setcolorspace");
        } else if (colorSpace.getType() == ColorSpace.CS_GRAY) {
            gen.writeln("/DeviceGray setcolorspace");
        } else {
            gen.writeln("/DeviceRGB setcolorspace");
        }

        gen.writeln("{{");
        // Template: (RawData is used for the EOF signal only)
        // gen.write("/RawData currentfile <first filter> filter def");
        // gen.write("/Data RawData <second filter> <third filter> [...] def");
        if (isJPEG) {
            gen.writeln("/RawData currentfile /ASCII85Decode filter def");
            gen.writeln("/Data RawData << >> /DCTDecode filter def");
        } else {
            if (gen.getPSLevel() >= 3) {
                gen.writeln("/RawData currentfile /ASCII85Decode filter def");
                gen.writeln("/Data RawData /FlateDecode filter def");
            } else {
                gen.writeln("/RawData currentfile /ASCII85Decode filter def");
                gen.writeln("/Data RawData /RunLengthDecode filter def");
            }
        }
        gen.writeln("<<");
        gen.writeln("  /ImageType 1");
        gen.writeln("  /Width " + imgDim.width);
        gen.writeln("  /Height " + imgDim.height);
        gen.writeln("  /BitsPerComponent 8");
        if (colorSpace.getType() == ColorSpace.TYPE_CMYK) {
            if (false /*TODO img.invertImage()*/) {
                gen.writeln("  /Decode [1 0 1 0 1 0 1 0]");
            } else {
                gen.writeln("  /Decode [0 1 0 1 0 1 0 1]");
            }
        } else if (iscolor) {
            gen.writeln("  /Decode [0 1 0 1 0 1]");
        } else {
            gen.writeln("  /Decode [0 1]");
        }
        // Setup scanning for left-to-right and top-to-bottom
        gen.writeln("  /ImageMatrix [" + imgDim.width + " 0 0 "
              + imgDim.height + " 0 0]");

        gen.writeln("  /DataSource Data");
        gen.writeln(">>");
        gen.writeln("image");
        /* the following two lines could be enabled if something still goes wrong
         * gen.write("Data closefile");
         * gen.write("RawData flushfile");
         */
        gen.writeln("} stopped {handleerror} if");
        gen.writeln("  RawData flushfile");
        gen.writeln("} exec");

        OutputStream out = gen.getOutputStream();
        out = new ASCII85OutputStream(out);
        if (isJPEG) {
            //nop
        } else {
            if (gen.getPSLevel() >= 3) {
                out = new FlateEncodeOutputStream(out);
            } else {
                out = new RunLengthEncodeOutputStream(out);
            }
        }
        out.write(img);
        if (out instanceof Finalizable) {
            ((Finalizable)out).finalizeStream();
        } else {
            out.flush();
        }

        gen.writeln("");
        gen.commentln("%FOPEndBitmap");
        gen.restoreGraphicsState();
    }

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
    public static void renderBitmapImage(RenderedImage img, 
                float x, float y, float w, float h, PSGenerator gen)
                    throws IOException {
        byte[] imgmap = getBitmapBytes(img);

        String imgName = img.getClass().getName();
        Dimension imgDim = new Dimension(img.getWidth(), img.getHeight());
        Rectangle2D targetRect = new Rectangle2D.Double(x, y, w, h);
        boolean isJPEG = false;
        writeImage(imgmap, imgDim, imgName, targetRect, isJPEG, 
                img.getColorModel().getColorSpace(), gen);
    }

    private static byte[] getBitmapBytes(RenderedImage img) {
        int[] tmpMap = getRGB(img, 0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        // Should take care of the ColorSpace and bitsPerPixel
        byte[] bitmaps = new byte[img.getWidth() * img.getHeight() * 3];
        for (int y = 0, my = img.getHeight(); y < my; y++) {
            for (int x = 0, mx = img.getWidth(); x < mx; x++) {
                int p = tmpMap[y * mx + x];
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = (p) & 0xFF;
                bitmaps[3 * (y * mx + x)] = (byte)(r & 0xFF);
                bitmaps[3 * (y * mx + x) + 1] = (byte)(g & 0xFF);
                bitmaps[3 * (y * mx + x) + 2] = (byte)(b & 0xFF);
            }
        }
        return bitmaps;
    }
    
    public static int[] getRGB(RenderedImage img,
                int startX, int startY, int w, int h,
                int[] rgbArray, int offset, int scansize) {
        Raster raster = img.getData();
        int yoff  = offset;
        int off;
        Object data;
        int nbands = raster.getNumBands();
        int dataType = raster.getDataBuffer().getDataType();
        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
            data = new byte[nbands];
            break;
        case DataBuffer.TYPE_USHORT:
            data = new short[nbands];
            break;
        case DataBuffer.TYPE_INT:
            data = new int[nbands];
            break;
        case DataBuffer.TYPE_FLOAT:
            data = new float[nbands];
            break;
        case DataBuffer.TYPE_DOUBLE:
            data = new double[nbands];
            break;
        default:
            throw new IllegalArgumentException("Unknown data buffer type: "+
                                               dataType);
        }
        
        if (rgbArray == null) {
            rgbArray = new int[offset+h*scansize];
        }
        
        ColorModel colorModel = img.getColorModel();
        for (int y = startY; y < startY+h; y++, yoff+=scansize) {
            off = yoff;
            for (int x = startX; x < startX+w; x++) {
                rgbArray[off++] = colorModel.getRGB(raster.getDataElements(x,
                                    y,
                                    data));
            }
        }
        
        return rgbArray;

    }
    
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

    /**
     * Places an EPS file in the PostScript stream.
     * @param rawEPS byte array containing the raw EPS data
     * @param name name for the EPS document
     * @param x x-coordinate of viewport in millipoints
     * @param y y-coordinate of viewport in millipoints
     * @param w width of viewport in millipoints
     * @param h height of viewport in millipoints
     * @param bboxx x-coordinate of EPS bounding box in points
     * @param bboxy y-coordinate of EPS bounding box in points
     * @param bboxw width of EPS bounding box in points
     * @param bboxh height of EPS bounding box in points
     * @param gen the PS generator
     * @throws IOException in case an I/O error happens during output
     */
    public static void renderEPS(byte[] rawEPS, String name,
                    float x, float y, float w, float h,
                    float bboxx, float bboxy, float bboxw, float bboxh,
                    PSGenerator gen) throws IOException {
        gen.notifyResourceUsage(PSProcSets.EPS_PROCSET, false);
        gen.writeln("%FOPBeginEPS: " + name);
        gen.writeln("BeginEPSF");

        gen.writeln(gen.formatDouble(x) + " " + gen.formatDouble(y) + " translate");
        gen.writeln("0 " + gen.formatDouble(h) + " translate");
        gen.writeln("1 -1 scale");
        float sx = w / bboxw;
        float sy = h / bboxh;
        if (sx != 1 || sy != 1) {
            gen.writeln(gen.formatDouble(sx) + " " + gen.formatDouble(sy) + " scale");
        }
        if (bboxx != 0 || bboxy != 0) {
            gen.writeln(gen.formatDouble(-bboxx) + " " + gen.formatDouble(-bboxy) + " translate");
        }
        gen.writeln(gen.formatDouble(bboxy) + " " + gen.formatDouble(bboxy) 
                + " " + gen.formatDouble(bboxw) + " " + gen.formatDouble(bboxh) + " re clip");
        gen.writeln("newpath");
        
        PSResource res = new PSResource(PSResource.TYPE_FILE, name);
        gen.notifyResourceUsage(res, false);
        gen.writeDSCComment(DSCConstants.BEGIN_DOCUMENT, res.getName());
        gen.writeByteArr(rawEPS);
        gen.writeDSCComment(DSCConstants.END_DOCUMENT);
        gen.writeln("EndEPSF");
        gen.writeln("%FOPEndEPS");
    }

}
