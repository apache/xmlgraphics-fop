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

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;

import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;

/**
 * This class is an implementation of the Image interface exposing a JPEG file. It provides an
 * InputStream to access the JPEG content and some additional information on the image. 
 */
public class ImageRawJPEG extends ImageRawStream {

    private int sofType;
    private ColorSpace colorSpace;
    private ICC_Profile iccProfile;
    private boolean invertImage = false;
    
    /**
     * Main constructor.
     * @param info the image info object
     * @param in the ImageInputStream with the raw content
     * @param sofType the SOFn identifier
     * @param colorSpace the color space
     * @param iccProfile an ICC color profile or null if no profile is associated
     * @param invertImage true if the image should be inverted when painting it
     */
    public ImageRawJPEG(ImageInfo info, java.io.InputStream in,
                int sofType, ColorSpace colorSpace, ICC_Profile iccProfile, boolean invertImage) {
        super(info, ImageFlavor.RAW_JPEG, in);
        this.sofType = sofType;
        this.colorSpace = colorSpace;
        this.iccProfile = iccProfile;
        this.invertImage = invertImage;
    }
    
    /**
     * Returns the SOFn identifier of the image which describes the coding format of the image.
     * @return the SOFn identifier
     */
    public int getSOFType() {
        return this.sofType;
    }
    
    /**
     * Returns the ICC color profile if one is associated with the JPEG image.
     * @return the ICC color profile or null if there's no profile
     */
    public ICC_Profile getICCProfile() {
        return this.iccProfile;
    }
    
    /**
     * Indicates whether the image should be inverted when interpreting it.
     * @return true if the image is to be inverted
     */
    public boolean isInverted() {
        return this.invertImage;
    }

    /**
     * Returns the image's color space
     * @return the color space
     */
    public ColorSpace getColorSpace() {
        return this.colorSpace;
    }
    
}
