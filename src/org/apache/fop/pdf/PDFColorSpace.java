/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * PDF Color space.
 */
public class PDFColorSpace {
    private boolean hasICCProfile;
    private byte[] iccProfile;
    private int numComponents;

    // Ok... so I had some grand purpose for this, but I can't recall.
    // I'm just writing it

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
    public PDFColorSpace(int theColorSpace) {
        this.currentColorSpace = theColorSpace;
        hasICCProfile = false;
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
     * Check if this colorspace has an ICC profile.
     *
     * @return true if this has an ICC profile
     */
    public boolean hasICCProfile() {
        return hasICCProfile;
    }

    /**
     * Get the ICC profile for this colorspace
     *
     * @return the byte array containing the ICC profile data
     */
    public byte[] getICCProfile() {
        if (hasICCProfile) {
            return iccProfile;
        } else {
            return new byte[0];
        }
    }

    /**
     * Set the ICC profile for this colorspace.
     *
     * @param iccProfile the ICC profile data
     */
    public void setICCProfile(byte[] iccProfile) {
        this.iccProfile = iccProfile;
        hasICCProfile = true;
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

    /**
     * Get the PDF string for this colorspace.
     *
     * @return the PDF string for the colorspace
     */
    public String getColorSpacePDFString() {
        // shouldn't this be a select-case? I can never remember
        // the syntax for that.
        switch (currentColorSpace) {
            case DEVICE_CMYK:
                return "DeviceCMYK";
            //break;
            case DEVICE_GRAY:
                return "DeviceGray";
            //break;
            case DEVICE_RGB:
            default:
                // unknown... Error. Tell them it's RGB and hope they
                // don't notice.
                return ("DeviceRGB");
            //break;
        }
    }

}
