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

package org.apache.fop.pdf;

/**
 * This class represents a "Separation" color space. It is used in FOP to map named colors.
 */
public class PDFSeparationColorSpace extends PDFArray implements PDFColorSpace {

    /**
     * Creates a new "Separation" color space.
     * @param colorName the name of the colorant
     * @param tintFunction the tint function used as fallback
     */
    public PDFSeparationColorSpace(String colorName, PDFFunction tintFunction) {
        super();
        add(new PDFName("Separation"));
        add(new PDFName(colorName));
        add(new PDFName("DeviceRGB"));
        add(new PDFReference(tintFunction));
    }

    /** {@inheritDoc} */
    public String getName() {
        //return "CS" + this.getObjectNumber();
        return getColorName().toString();
    }

    /**
     * Returns the name of the colorant.
     * @return the name of the colorant
     */
    public PDFName getColorName() {
        return (PDFName)get(1);
    }

    /**
     * Returns a reference to the tint function that is used as a fallback if the colorant is
     * not available.
     * @return a reference to the tint function
     */
    public PDFReference getTintFunction() {
        return (PDFReference)get(2);
    }

    /** {@inheritDoc} */
    public int getNumComponents() {
        return 1;
    }

    /** {@inheritDoc} */
    public boolean isCMYKColorSpace() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isDeviceColorSpace() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isGrayColorSpace() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isRGBColorSpace() {
        return false;
    }

}
