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

package org.apache.fop.render;

import java.util.Map;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.fo.extensions.ExtensionElementMapping;

/**
 * Utility methods for image handling.
 */
public class ImageHandlerUtil {

    /** conversion-mode extension attribute */
    public static final QName CONVERSION_MODE = new QName(
            ExtensionElementMapping.URI, null, "conversion-mode");

    /** Conversion mode: indicates that the image shall be converted to a bitmap. */
    public static final String CONVERSION_MODE_BITMAP = "bitmap";

    /**
     * Indicates whether the image conversion mode is set to bitmap mode, i.e. the image shall
     * be converted to a bitmap.
     * @param mode the conversion mode
     * @return true if conversion mode is "bitmap"
     */
    public static boolean isConversionModeBitmap(String mode) {
        return CONVERSION_MODE_BITMAP.equalsIgnoreCase(mode);
    }

    /**
     * Indicates whether the image conversion mode is set to bitmap mode, i.e. the image shall
     * be converted to a bitmap.
     * @param foreignAttributes a map of foreign attributes (Map&lt;QName, Object&gt;)
     * @return true if conversion mode is "bitmap"
     */
    public static boolean isConversionModeBitmap(Map foreignAttributes) {
        if (foreignAttributes == null) {
            return false;
        }
        String conversionMode = (String)foreignAttributes.get(CONVERSION_MODE);
        return isConversionModeBitmap(conversionMode);
    }

}
