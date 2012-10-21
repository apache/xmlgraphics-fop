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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlgraphics.image.codec.tiff.TIFFImage;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;
import org.apache.xmlgraphics.ps.ImageEncoder;
import org.apache.xmlgraphics.ps.PSDictionary;

/**
 * ImageEncoder implementation for CCITT encoded images.
 */
public class ImageEncoderCCITTFax implements ImageEncoder {

    private final ImageRawCCITTFax ccitt;

    /**
     * Main constructor.
     * @param ccitt the CCITT encoded image
     */
    public ImageEncoderCCITTFax(ImageRawCCITTFax ccitt) {
        this.ccitt = ccitt;
    }

    /** {@inheritDoc} */
    public void writeTo(OutputStream out) throws IOException {
        ccitt.writeTo(out);
    }

    /** {@inheritDoc} */
    public String getImplicitFilter() {
        PSDictionary dict = new PSDictionary();
        dict.put("/Columns", new Integer(ccitt.getSize().getWidthPx()));
        int compression = ccitt.getCompression();
        switch (compression) {
        case TIFFImage.COMP_FAX_G3_1D :
            dict.put("/K", new Integer(0));
            break;
        case TIFFImage.COMP_FAX_G3_2D :
            dict.put("/K", new Integer(1));
            break;
        case TIFFImage.COMP_FAX_G4_2D :
            dict.put("/K", new Integer(-1));
            break;
        default:
            throw new IllegalStateException(
                    "Invalid compression scheme: " + compression);
        }

        return dict.toString() + " /CCITTFaxDecode";
    }
}
