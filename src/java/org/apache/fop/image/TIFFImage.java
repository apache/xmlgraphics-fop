/*
 * Copyright 2004 The Apache Software Foundation
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

import org.apache.batik.ext.awt.image.codec.SeekableStream;
import org.apache.batik.ext.awt.image.rendered.CachableRed;

/**
 * @author Jeremias Maerki
 */
public class TIFFImage extends BatikImage {

    /**
     * Constructs a new BatikImage instance.
     * @param imgReader basic metadata for the image
     */
    public TIFFImage(FopImage.ImageInfo imgReader) {
        super(imgReader);
    }

    /**
     * @see org.apache.fop.image.BatikImage#decodeImage(org.apache.batik.ext.awt.image.codec.SeekableStream)
     */
    protected CachableRed decodeImage(SeekableStream stream) throws IOException {
        return new org.apache.batik.ext.awt.image.codec.tiff.TIFFImage
                (stream, null, 0);
    }
    
}
