/*
 * Copyright 2004 The Apache Software Foundation.
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

    /**
     * Renders an image to PostScript.
     * @param img image to render
     * @param x x position
     * @param y y position
     * @param w width
     * @param h height
     * @param gen PS generator
     * @throws IOException In case of an I/O problem while rendering the image
     */
    public static void renderFopImage(FopImage img, int x, int y, int w, int h, PSGenerator gen) throws IOException {
        boolean iscolor = img.getColorSpace().getType()
                          != ColorSpace.CS_GRAY;
        byte[] imgmap = img.getBitmaps();

        gen.saveGraphicsState();
        if (img.getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
            gen.writeln("/DeviceCMYK setcolorspace");
        } else if (img.getColorSpace().getType() == ColorSpace.CS_GRAY) {
            gen.writeln("/DeviceGray setcolorspace");
        } else {
            gen.writeln("/DeviceRGB setcolorspace");
        }

        gen.writeln(x + " " + y + " translate");
        gen.writeln(w + " " + h + " scale");

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

        /*
         * for (int y=0; y<img.getHeight(); y++) {
         * int indx = y * img.getWidth();
         * if (iscolor) indx*= 3;
         * for (int x=0; x<img.getWidth(); x++) {
         * if (iscolor) {
         * writeASCIIHex(imgmap[indx++] & 0xFF);
         * writeASCIIHex(imgmap[indx++] & 0xFF);
         * writeASCIIHex(imgmap[indx++] & 0xFF);
         * } else {
         * writeASCIIHex(imgmap[indx++] & 0xFF);
         * }
         * }
         * }
         */

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
        gen.restoreGraphicsState();
    }


}
