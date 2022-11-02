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

package org.apache.fop.render.bitmap;

import java.awt.image.BufferedImage;

/**
 * Compression constants for TIFF image output.
 */
public enum TIFFCompressionValue {
    /** No compression */
    NONE("NONE"),
    /** JPEG compression */
    JPEG("JPEG"),
    /** Packbits (RLE) compression */
    PACKBITS("PackBits"),
    /** Deflate compression */
    DEFLATE("Deflate"),
    /** LZW compression */
    LZW("LZW"),
    /** ZLib compression */
    ZLIB("ZLib"),
    /** CCITT Group 3 (T.4) compression */
    CCITT_T4("CCITT T.4", BufferedImage.TYPE_BYTE_BINARY, true),
    /** CCITT Group 4 (T.6) compression */
    CCITT_T6("CCITT T.6", BufferedImage.TYPE_BYTE_BINARY, true);

    private final String name;
    private final int imageType;
    private boolean isCcitt;

    private TIFFCompressionValue(String name, int imageType, boolean isCcitt) {
        this.name = name;
        this.imageType = imageType;
        this.isCcitt = isCcitt;
    }

    private TIFFCompressionValue(String name) {
        this(name, BufferedImage.TYPE_INT_ARGB, false);
    }

    /**
     * Returns the name of this compression type.
     * @return the compression name
     */
    String getName() {
        return name;
    }

    /**
     * Returns an image type for this compression type, a constant from {@link BufferedImage} e.g.
     * {@link BufferedImage#TYPE_INT_ARGB} for {@link #ZLIB}
     * @return the image type
     */
    int getImageType() {
        return imageType;
    }

    /**
     * Returns whether or not this compression type is a CCITT type.
     * @return true if the compression type is CCITT
     */
    boolean hasCCITTCompression() {
        return isCcitt;
    }

    /**
     * Return the TIFF compression constant given the string representing the type. In the case that
     * the name doesn't match any of the compression values, <code>null</code> is returned.
     * @param name the compression type name
     * @return the compression constant
     */
    static TIFFCompressionValue getType(String name) {
        for (TIFFCompressionValue tiffConst : TIFFCompressionValue.values()) {
            if (tiffConst.name.equalsIgnoreCase(name)) {
                return tiffConst;
            }
        }
        return null;
    }
}
