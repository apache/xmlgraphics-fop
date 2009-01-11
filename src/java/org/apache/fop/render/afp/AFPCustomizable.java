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

package org.apache.fop.render.afp;

/**
 * Interface used to customize the AFP renderer or document handler.
 */
public interface AFPCustomizable {

    /**
     * Sets the number of bits used per pixel
     *
     * @param bitsPerPixel
     *            number of bits per pixel
     */
    void setBitsPerPixel(int bitsPerPixel);

    /**
     * Sets whether images are color or not
     *
     * @param colorImages
     *            color image output
     */
    void setColorImages(boolean colorImages);

    /**
     * Sets whether images are supported natively or not
     *
     * @param nativeImages
     *            native image support
     */
    void setNativeImagesSupported(boolean nativeImages);

    /**
     * Sets the output/device resolution
     *
     * @param resolution
     *            the output resolution (dpi)
     */
    void setResolution(int resolution);

    /**
     * Returns the output/device resolution.
     *
     * @return the resolution in dpi
     */
    int getResolution();

    /**
     * Sets the default resource group file path
     * @param filePath the default resource group file path
     */
    void setDefaultResourceGroupFilePath(String filePath);

}
