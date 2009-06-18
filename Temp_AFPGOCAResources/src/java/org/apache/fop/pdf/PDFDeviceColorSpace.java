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
 * Represents a device-specific color space. Used for mapping DeviceRGB, DeviceCMYK and DeviceGray.
 */
public class PDFDeviceColorSpace implements PDFColorSpace {

    private int numComponents;

    /**
     * Unknown colorspace
     */
    public static final int DEVICE_UNKNOWN = -1;

    /**
     * Gray colorspace
     */
    public static final int DEVICE_GRAY = 1;

    /**
     * RGB colorspace
     */
    public static final int DEVICE_RGB = 2;

    /**
     * CMYK colorspace
     */
    public static final int DEVICE_CMYK = 3;

    // Are there any others?

    /**
     * Current color space value.
     */
    protected int currentColorSpace = DEVICE_UNKNOWN;

    /**
     * Create a PDF colorspace object.
     *
     * @param theColorSpace the current colorspace
     */
    public PDFDeviceColorSpace(int theColorSpace) {
        this.currentColorSpace = theColorSpace;
        numComponents = calculateNumComponents();
    }

    private int calculateNumComponents() {
        if (currentColorSpace == DEVICE_GRAY) {
            return 1;
        } else if (currentColorSpace == DEVICE_RGB) {
            return 3;
        } else if (currentColorSpace == DEVICE_CMYK) {
            return 4;
        } else {
            return 0;
        }
    }

    /**
     * Set the current colorspace.
     *
     * @param theColorSpace the new color space value
     */
    public void setColorSpace(int theColorSpace) {
        this.currentColorSpace = theColorSpace;
        numComponents = calculateNumComponents();
    }

    /**
     * Get the colorspace value
     *
     * @return the colorspace value
     */
    public int getColorSpace() {
        return (this.currentColorSpace);
    }

    /**
     * Get the number of color components for this colorspace
     *
     * @return the number of components
     */
    public int getNumComponents() {
        return numComponents;
    }

    /** @return the name of the color space */
    public String getName() {
        switch (currentColorSpace) {
        case DEVICE_CMYK:
            return "DeviceCMYK";
        case DEVICE_GRAY:
            return "DeviceGray";
        case DEVICE_RGB:
            return "DeviceRGB";
        default:
            throw new IllegalStateException("Unsupported color space in use.");
        }
    }

    /** {@inheritDoc} */
    public boolean isDeviceColorSpace() {
        return true;
    }

    /** {@inheritDoc} */
    public boolean isRGBColorSpace() {
        return getColorSpace() == DEVICE_RGB;
    }

    /** {@inheritDoc} */
    public boolean isCMYKColorSpace() {
        return getColorSpace() == DEVICE_CMYK;
    }

    /** {@inheritDoc} */
    public boolean isGrayColorSpace() {
        return getColorSpace() == DEVICE_GRAY;
    }

}
