/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

public class ColorSpace {
    private boolean hasICCProfile;
    private byte[] iccProfile;
    private int numComponents;
    
    // Ok... so I had some grand purpose for this, but I can't recall.
    // I'm just writing it
    
    public static int DEVICE_UNKNOWN = -1;
    public static int DEVICE_GRAY = 1;
    // what's the *official* spelling?
    // public static int DEVICE_GREY = 1;
    public static int DEVICE_RGB = 2;
    public static int DEVICE_CMYK = 3;
    
    
    // Are there any others?

    protected int currentColorSpace = -1;

    public ColorSpace(int theColorSpace) {
        this.currentColorSpace = theColorSpace;
        hasICCProfile = false;
        numComponents = calculateNumComponents();
    }
    
    private int calculateNumComponents() {
        if (currentColorSpace == DEVICE_GRAY)
            return 1;
        else if (currentColorSpace == DEVICE_RGB)
            return 3;
        else if (currentColorSpace == DEVICE_CMYK)
            return 4;
        else
            return 0;
    }

    public void setColorSpace(int theColorSpace) {
        this.currentColorSpace = theColorSpace;
        numComponents = calculateNumComponents();
    }
    
    public boolean hasICCProfile() {
        return hasICCProfile;
    }
    
    public byte[] getICCProfile() {
        if (hasICCProfile)
            return iccProfile;
        else
            return new byte[0];
    }
    
    public void setICCProfile(byte[] iccProfile) {
        this.iccProfile = iccProfile;
        hasICCProfile = true;
    }
    
    public int getColorSpace() {
        return (this.currentColorSpace);
    }
    
    public int getNumComponents() {
        return numComponents;
    }
    
    public String getColorSpacePDFString() {    // this is for PDF Output. Does anyone else need a string representation?
        
        
        // shouldn't this be a select-case? I can never remember
        // the syntax for that.
        if (this.currentColorSpace == this.DEVICE_RGB) {
            return ("DeviceRGB");
        } else if (this.currentColorSpace == this.DEVICE_CMYK) {
            return ("DeviceCMYK");
        } else if (this.currentColorSpace == this.DEVICE_GRAY) {
            return ("DeviceGray");
        } else {    // unknown... Error. Tell them it's RGB and hope they don't notice.
            return ("DeviceRGB");
        }
    }
    
}
