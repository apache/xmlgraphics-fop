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

import org.apache.fop.apps.MimeConstants;

/**
 * Constants for TIFF output.
 */
public interface TIFFConstants {

    /** The MIME type for tiff-Rendering */
    String MIME_TYPE = MimeConstants.MIME_TIFF;

    /** No compression */
    String COMPRESSION_NONE = "NONE";
    /** JPEG compression */
    String COMPRESSION_JPEG = "JPEG";
    /** Packbits (RLE) compression */
    String COMPRESSION_PACKBITS = "PackBits";
    /** Deflate compression */
    String COMPRESSION_DEFLATE = "Deflate";
    /** LZW compression */
    String COMPRESSION_LZW = "LZW";
    /** ZLib compression */
    String COMPRESSION_ZLIB = "ZLib";
    /** CCITT Group 4 (T.6) compression */
    String COMPRESSION_CCITT_T6 = "CCITT T.6"; //CCITT Group 4
    /** CCITT Group 3 (T.4) compression */
    String COMPRESSION_CCITT_T4 = "CCITT T.4"; //CCITT Group 3

}
