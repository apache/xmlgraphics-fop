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

package org.apache.fop.render.pcl;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.ColorCube;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;

/**
 * Implementation of the MonochromeBitmapConverter which uses Java Advanced Imaging (JAI)
 * to convert grayscale bitmaps to monochrome bitmaps. JAI provides better dithering options
 * including error diffusion dithering.
 * <p>
 * If you call setHint("quality", "true") on the instance you can enabled error diffusion
 * dithering which produces a nicer result but is also a lot slower.
 */
public class JAIMonochromeBitmapConverter implements
        MonochromeBitmapConverter {

    private boolean isErrorDiffusion = false;
    
    /** @see MonochromeBitmapConverter#setHint(java.lang.String, java.lang.String) */
    public void setHint(String name, String value) {
        if ("quality".equalsIgnoreCase(name)) {
            isErrorDiffusion = "true".equalsIgnoreCase(value);
        }
    }
    
    /** @see MonochromeBitmapConverter#convertToMonochrome(java.awt.image.BufferedImage) */
    public RenderedImage convertToMonochrome(BufferedImage img) {
        if (img.getColorModel().getColorSpace().getNumComponents() != 1) {
            throw new IllegalArgumentException("Source image must be a grayscale image!");
        }
        
        // Load the ParameterBlock for the dithering operation
        // and set the operation name.
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(img);
        String opName = null;
        if (isErrorDiffusion) {
            opName = "errordiffusion";
            LookupTableJAI lut = new LookupTableJAI(new byte[] {(byte)0x00, (byte)0xff});
            pb.add(lut);
            pb.add(KernelJAI.ERROR_FILTER_FLOYD_STEINBERG);
        } else {
            opName = "ordereddither";
            //Create the color cube.
            ColorCube colorMap = ColorCube.createColorCube(DataBuffer.TYPE_BYTE,
                    0, new int[] {2});
            pb.add(colorMap);
            pb.add(KernelJAI.DITHER_MASK_441);
        }
        
        //Create an image layout for a monochrome b/w image
        ImageLayout layout = new ImageLayout();
        byte[] map = new byte[] {(byte)0x00, (byte)0xff};
        ColorModel cm = new IndexColorModel(1, 2, map, map, map);
        layout.setColorModel(cm);

        // Create a hint containing the layout.
        RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);

        // Dither the image.
        PlanarImage dst = JAI.create(opName, pb, hints);        
        
        //Convert it to a BufferedImage
        return dst.getAsBufferedImage();
    }

}
