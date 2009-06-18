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

package org.apache.fop.util;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.UnsupportedEncodingException;

/**
 * Helper methods for handling color profiles.
 */
public class ColorProfileUtil {

    /**
     * Returns the profile description of an ICC profile
     * @param profile the profile
     * @return the description
     */
    public static String getICCProfileDescription(ICC_Profile profile) {
        byte[] data = profile.getData(ICC_Profile.icSigProfileDescriptionTag);
        if (data == null) {
            return null;
        } else {
            //Info on the data format: http://www.color.org/ICC-1_1998-09.PDF
            int length = (data[8] << 3 * 8) | (data[9] << 2 * 8) | (data[10] << 8) | data[11];
            length--; //Remove trailing NUL character
            try {
                return new String(data, 12, length, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedOperationException("Incompatible VM");
            }
        }
    }
    
    /**
     * Indicates whether a given color profile is identical to the default sRGB profile
     * provided by the Java class library.
     * @param profile the color profile to check
     * @return true if it is the default sRGB profile
     */
    public static boolean isDefaultsRGB(ICC_Profile profile) {
        ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ICC_Profile sRGBProfile = null;
        if (sRGB instanceof ICC_ColorSpace) {
            sRGBProfile = ((ICC_ColorSpace)sRGB).getProfile();
        }
        return profile == sRGBProfile;
    }
    
}
