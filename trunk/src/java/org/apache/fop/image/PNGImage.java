/*
 * Copyright 2004-2005 The Apache Software Foundation 
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

package org.apache.fop.image;

import java.io.IOException;

import org.apache.batik.ext.awt.image.codec.PNGRed;
import org.apache.batik.ext.awt.image.codec.PNGDecodeParam;
import org.apache.batik.ext.awt.image.codec.SeekableStream;
import org.apache.batik.ext.awt.image.rendered.CachableRed;

/**
 * FopImage object using PNG
 * @author Eric SCHAEFFER
 * @see AbstractFopImage
 * @see FopImage
 */
public class PNGImage extends BatikImage {

    /**
     * Constructs a new PNGImage instance.
     * @param imgReader basic metadata for the image
     */
    public PNGImage(FopImage.ImageInfo imgReader) {
        super(imgReader);
        this.loaded = 0; //TODO The PNGReader cannot read the resolution, yet. 
    }

    /**
     * @see org.apache.fop.image.BatikImage#decodeImage(org.apache.batik.ext.awt.image.codec.SeekableStream)
     */
    protected CachableRed decodeImage(SeekableStream stream) throws IOException {
        PNGDecodeParam param = new PNGDecodeParam();
        param.setPerformGammaCorrection(true);
        param.setDisplayExponent(2.2f); // sRGB gamma
        PNGRed red = new PNGRed(stream, param); 
        String unit = (String)red.getProperty("pixel_units");
        if ("Meters".equals(unit)) {
            this.dpiHorizontal = ((Integer)red.getProperty("x_pixels_per_unit")).intValue() 
                * 25.4f / 1000f;
            this.dpiVertical = ((Integer)red.getProperty("y_pixels_per_unit")).intValue()
                * 25.4f / 1000f;
        }
        return red;
    }
    
}
