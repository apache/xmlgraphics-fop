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

import java.awt.color.ICC_Profile;

/**
 * Helper methods for handling color profiles.
 * @deprecated use org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil directly
 */
public final class ColorProfileUtil {

    private ColorProfileUtil() {
    }

    /**
     * Returns the profile description of an ICC profile
     * @param profile the profile
     * @return the description
     * @deprecated use org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil directly
     */
    public static String getICCProfileDescription(ICC_Profile profile) {
        return org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil
                .getICCProfileDescription(profile);
    }

    /**
     * Indicates whether a given color profile is identical to the default sRGB profile
     * provided by the Java class library.
     * @param profile the color profile to check
     * @return true if it is the default sRGB profile
     * @deprecated use org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil directly
     */
    public static boolean isDefaultsRGB(ICC_Profile profile) {
        return org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil
                .isDefaultsRGB(profile);
    }
}
