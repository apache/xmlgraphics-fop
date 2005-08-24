/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import java.awt.color.ColorSpace;
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
        boolean iscolor = img.getColorSpace().getType()
                          != ColorSpace.CS_GRAY;
        byte[] imgmap;
        if (img.getBitmapsSize() > 0) {
            imgmap = img.getBitmaps();
        } else {
            imgmap = img.getRessourceBytes();
        }

        gen.saveGraphicsState();
        gen.writeln(x + " " + y + " translate");
        gen.writeln(w + " " + h + " scale");

        gen.commentln("%FOPBeginBitmap: " + img.getMimeType() + " " + img.getOriginalURI());
        if (img.getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
            gen.writeln("/DeviceCMYK setcolorspace");
        } else if (img.getColorSpace().getType() == ColorSpace.CS_GRAY) {
            gen.writeln("/DeviceGray setcolorspace");
        } else {
            gen.writeln("/DeviceRGB setcolorspace");
        }

        gen.writeln("{{");
        // Template: (RawData is used for the EOF signal only)
        // gen.write("/RawData currentfile <first filter> filter def");
        // gen.write("/Data RawData <second filter> <third filter> [...] def");
        if (img instanceof JpegImage) {
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
        gen.writeln("  /Width " + img.getWidth());
        gen.writeln("  /Height " + img.getHeight());
        gen.writeln("  /BitsPerComponent 8");
        if (img.getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
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
        gen.writeln("  /ImageMatrix [" + img.getWidth() + " 0 0 "
              + img.getHeight() + " 0 0]");

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
        if (img instanceof JpegImage) {
            //nop
        } else {
            if (gen.getPSLevel() >= 3) {
                out = new FlateEncodeOutputStream(out);
            } else {
                out = new RunLengthEncodeOutputStream(out);
            }
        }
        out.write(imgmap);
        if (out instanceof Finalizable) {
            ((Finalizable)out).finalizeStream();
        } else {
            out.flush();
        }

        gen.writeln("");
        gen.commentln("%FOPEndBitmap");
        gen.restoreGraphicsState();
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
                    int bboxx, int bboxy, int bboxw, int bboxh,
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
