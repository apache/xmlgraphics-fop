/*
 * Copyright 1999-2005 The Apache Software Foundation.
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


package org.apache.fop.image;

// Java
import java.io.ByteArrayOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Enhanced metafile image.
 * This supports loading a emf image.
 *
 * @author Peter Herweg
 * @see AbstractFopImage
 * @see FopImage
 */
public class EmfImage extends AbstractFopImage {
    /**
     * Create a bitmap image with the image data.
     *
     * @param imgInfo the image information
     */
    public EmfImage(FopImage.ImageInfo imgInfo) {
        super(imgInfo);
    }

    /**
     * Load the original jpeg data.
     * This loads the original jpeg data and reads the color space,
     * and icc profile if any.
     *
     * @return true if loaded false for any error
     */
    protected boolean loadOriginalData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            byte[] readBuf = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(readBuf)) != -1) {
                baos.write(readBuf, 0, bytesRead);
            }
        } catch (java.io.IOException ex) {
            log.error("Error while loading image (Emf): " + ex.getMessage(), ex);
            return false;
        } finally {
            IOUtils.closeQuietly(inputStream);
            inputStream = null;
        }

        this.raw = baos.toByteArray();
        
        return true;
    }
}

