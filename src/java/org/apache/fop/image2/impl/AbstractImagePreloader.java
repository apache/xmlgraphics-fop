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
 
package org.apache.fop.image2.impl;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import org.apache.fop.image2.spi.ImagePreloader;

/**
 * Abstract base class for image preloaders. It provides helper methods for often-used tasks.
 */
public abstract class AbstractImagePreloader implements ImagePreloader {

    /**
     * Allows to read an image header (usually just a magic number). The InputStream is reset
     * to the position before the header was read.
     * @param in the ImageInputStream to read from 
     * @param size the size of the header
     * @return the loaded header
     * @throws IOException if an I/O error occurs while reading the header
     */
    protected byte[] getHeader(ImageInputStream in, int size)
                throws IOException {
        byte[] header = new byte[size];
        long startPos = in.getStreamPosition();
        in.readFully(header);
        in.seek(startPos); //Seek back to start position
        return header;
    }

}
